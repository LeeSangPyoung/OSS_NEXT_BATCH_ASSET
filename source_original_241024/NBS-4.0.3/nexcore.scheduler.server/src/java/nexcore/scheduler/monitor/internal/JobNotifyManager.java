package nexcore.scheduler.monitor.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;

import com.ibatis.sqlmap.client.SqlMapClient;

import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobNotify;
import nexcore.scheduler.entity.JobNotifyReceiver;
import nexcore.scheduler.entity.JobNotifySendInfo;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.monitor.IJobEndNotifyProcessor;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Notify, NotifyReceiver 관리 </li>
 * <li>작성일 : 2011. 2. 5.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class JobNotifyManager {
	private SqlMapClient                  sqlMapClient;
	private List<IJobEndNotifyProcessor>  notifyProcessors;

	private Map<Integer, JobNotify>       cache  = new ConcurrentHashMap<Integer, JobNotify>();  // JobNotify 는 메모리 캐쉬한다.
	
	private long                          lastCacheRefreshTime; // 캐쉬를 최종 refresh 한 시각. 1분에 한번씩
	
	private Log                           log;

	public void init() {
		log = LogManager.getSchedulerLog();
		try {
			getAllJobNotifies();  // cache 에 올리기 위함.
		} catch (SQLException e) {
			throw new SchedulerException("main.jobnotify.maxid.error", e);
		}
		Util.logServerInitConsole("JobNotifyManager", String.valueOf(notifyProcessors));
	}

	public void destroy() {
	}
	
	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public List<IJobEndNotifyProcessor> getNotifyProcessors() {
		return notifyProcessors;
	}

	public void setNotifyProcessors(List<IJobEndNotifyProcessor> notifyProcessors) {
		this.notifyProcessors = notifyProcessors;
	}
	
	// #####################################################################
	// ##########  JOB NOTIFY 
	// #####################################################################

	public boolean addJobNotify(JobNotify jobNotify)  throws SQLException {
		jobNotify.setLastModifyTime(DateUtil.getCurrentTimestampString());
		int cnt = insertJobNotify(jobNotify);
		return cnt>0;
	}

	public boolean removeJobNotify(int id) throws SQLException {
		int cnt = deleteJobNotify(id);
		cache.remove(id);
		return cnt > 0;

	}
	
	public boolean modifyJobNotify(JobNotify jobNotify)  throws SQLException {
		jobNotify.setLastModifyTime(DateUtil.getCurrentTimestampString());
		int cnt = updateJobNotify(jobNotify);
		if (cnt > 0) {
			cache.put(jobNotify.getId(), jobNotify);
		}
		return cnt > 0;
	}

	public JobNotify getJobNotify(int id) throws SQLException {
		JobNotify jobNotify = cache.get(id);  // 캐쉬에서 읽어옴.
		if (jobNotify == null) { 
			jobNotify = selectJobNotify(id);
			if (jobNotify != null) {
				cache.put(id, jobNotify);
			}
		}
		return jobNotify;
	}

	/**
	 * 전체 조회는 테이블에서 다시 읽으며 메모리 cache 도 refresh 한다.
	 * @return
	 * @throws SQLException
	 */
	public List<JobNotify> getAllJobNotifies() throws SQLException {
		List<JobNotify> jobNotifyList = selectJobNotifyByQuery("", "ORDER BY NOTIFY_ID"); // 전체 리스트 조회는 캐쉬에서 읽지 않음
		Map cacheNew  = new ConcurrentHashMap<Integer, JobNotify>();  // JobNotify 는 메모리 캐쉬한다.
		for (JobNotify jobNotify : jobNotifyList) {
			cacheNew.put(jobNotify.getId(), jobNotify);
		}
		Map cacheOld = cache;
		cache = cacheNew;
		cacheOld.clear();
		
		lastCacheRefreshTime = System.currentTimeMillis();
		return jobNotifyList;
	}
	
	private int insertJobNotify(JobNotify jobNotify) throws SQLException {
		int cnt = sqlMapClient.update("nbs.monitor.insertJobNotify", jobNotify);
		return cnt;
	}

	private JobNotify selectJobNotify(int id) throws SQLException {
		return (JobNotify)sqlMapClient.queryForObject("nbs.monitor.selectJobNotify", id);
	}

	private List<JobNotify> selectJobNotifyByQuery(String query, String orderBy) throws SQLException {
		Map m = new HashMap();
		m.put("queryCondition", query);
		m.put("orderBy",        orderBy);
		return (List<JobNotify>)sqlMapClient.queryForList("nbs.monitor.selectJobNotifyByQuery", m);
	}

	private int updateJobNotify(JobNotify jobNotify) throws SQLException {
		return sqlMapClient.update("nbs.monitor.updateJobNotify", jobNotify);
	}
	
	private int deleteJobNotify(int id) throws SQLException {
		return sqlMapClient.delete("nbs.monitor.deleteJobNotify", id);
	}
	
	/**
	 * 최소 3분에 한번은 cache를 강제 refresh 한다.
	 * peer 에서 admin 으로 jobnotify CUD 했을때도 강제 refresh 한다.
	 * @param forceRefresh 강제로 refresh 할 것인지 여부.
	 */
	public void checkAndRefreshCache(boolean forceRefresh) {
		if (forceRefresh || System.currentTimeMillis() > lastCacheRefreshTime + 180 * 1000) {
			try {
				getAllJobNotifies();
			} catch (SQLException e) {
				Util.logError(log, "[JobNofityManager] Refresh cache fail.", e);
			}
		}
	}

	// #####################################################################
	// ##########  JOB NOTIFY RECEIVER
	// #####################################################################
	
	public boolean addJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver)  throws SQLException {
		jobNotifyReceiver.setLastModifyTime(DateUtil.getCurrentTimestampString());
		int cnt = insertJobNotifyReceiver(jobNotifyReceiver);
		return cnt>0;
	}

	public boolean removeJobNotifyReceiver(int id) throws SQLException {
		int cnt = deleteJobNotifyReceiver(id);
		return cnt > 0;
	}
	
	public boolean modifyJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver)  throws SQLException {
		jobNotifyReceiver.setLastModifyTime(DateUtil.getCurrentTimestampString());
		int cnt = updateJobNotifyReceiver(jobNotifyReceiver);
		return cnt > 0;
	}

	public JobNotifyReceiver getJobNotifyReceiver(int id) throws SQLException {
		return selectJobNotifyReceiver(id);
	}

	public List<JobNotifyReceiver> getAllJobNotifyReceivers() throws SQLException {
		List<JobNotifyReceiver> jobNotifyReceiverList = selectJobNotifyReceiverByQuery("", "ORDER BY RECEIVER_NAME"); // 전체 리스트 조회는 캐쉬에서 읽지 않음
		return jobNotifyReceiverList;
	}
	
	public Map<Integer, JobNotifyReceiver> getAllJobNotifyReceiversMap() throws SQLException {
		List<JobNotifyReceiver> jobNotifyReceiverList = selectJobNotifyReceiverByQuery("", ""); // 전체 리스트 조회는 캐쉬에서 읽지 않음
		Map<Integer, JobNotifyReceiver> map = new HashMap<Integer, JobNotifyReceiver>();
		for (JobNotifyReceiver receiver : jobNotifyReceiverList) {
			map.put(receiver.getId(), receiver);
		}
		return map;
	}

	public List<JobNotifyReceiver> getJobNotifyReceiversByNotifyId(int notifyId) throws SQLException {
		JobNotify jobNotify = getJobNotify(notifyId);
		List<JobNotifyReceiver> jobNotifyReceiverList = selectJobNotifyReceiverByIdList(jobNotify.getReceiverList());
		return jobNotifyReceiverList;
	}
	
	private int insertJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver) throws SQLException {
		int cnt = sqlMapClient.update("nbs.monitor.insertJobNotifyReceiver", jobNotifyReceiver);
		return cnt;
	}

	private JobNotifyReceiver selectJobNotifyReceiver(int id) throws SQLException {
		return (JobNotifyReceiver)sqlMapClient.queryForObject("nbs.monitor.selectJobNotifyReceiver", id);
	}

	private List<JobNotifyReceiver> selectJobNotifyReceiverByIdList(List receiverIdList) throws SQLException {
		Map param = new HashMap();
		param.put("idList",  receiverIdList);
		param.put("orderBy", "ORDER BY RECEIVER_NAME");
		
		return (List<JobNotifyReceiver>)sqlMapClient.queryForList("nbs.monitor.selectJobNotifyReceiverByIdList", param);
	}

	private List<JobNotifyReceiver> selectJobNotifyReceiverByQuery(String query, String orderBy) throws SQLException {
		Map m = new HashMap();
		m.put("queryCondition", query);
		m.put("orderBy",        orderBy);
		return (List<JobNotifyReceiver>)sqlMapClient.queryForList("nbs.monitor.selectJobNotifyReceiverByQuery", m);
	}

	private int updateJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver) throws SQLException {
		return sqlMapClient.update("nbs.monitor.updateJobNotifyReceiver", jobNotifyReceiver);
	}
	
	private int deleteJobNotifyReceiver(int id) throws SQLException {
		return sqlMapClient.delete("nbs.monitor.deleteJobNotifyReceiver", id);
	}
	
	public List<JobNotifySendInfo> selectJobNotifySendList(Map queryParamMap) throws SQLException {
		return (List<JobNotifySendInfo>)sqlMapClient.queryForList("nbs.monitor.selectJobNotifySendListByDynamicQuery", queryParamMap);
	}
	
	public int getJobNotifySendListCount(Map queryParamMap) throws SQLException {
		return (Integer)sqlMapClient.queryForObject("nbs.monitor.getJobNotifySendListCount", queryParamMap);
	}


	/**
	 * JobEndCallback을 받아 JobNotify 여부를 체크함.
	 * @throws Exception
	 */
	public void processJobEndCallback(JobExecution jobexe) throws Exception {
		
		checkAndRefreshCache(false);  // 캐쉬 한번 점검한다.
		
		JobNotify matchingNotify = null;
		if (notifyProcessors != null) {
			// 통지 대상자 선정
			Set<Integer> selectedReceivers = new HashSet<Integer>();
			for (JobNotify notify :  cache.values()) {
				Matcher mat = notify.getJobIdExpressionPattern().matcher(jobexe.getJobId());
				if (mat.matches()) {
					matchingNotify = notify;
					if ("EO".equals(notify.getWhen()) && jobexe.getReturnCode()==0) { // End OK
						selectedReceivers.addAll(notify.getReceiverList());
					}else if ("EF".equals(notify.getWhen())  && jobexe.getReturnCode()!=0) { // End Fail
						selectedReceivers.addAll(notify.getReceiverList());
					}
					// 통지 설정이 LongRun 이고 LongRun으로 통지가 되었던 Job인 경우 수행
					if ("LONGRUN".equals(notify.getWhen())){
						long thresTime = Long.parseLong(notify.getCheckValue1()) * 60000;        // LongRun 시간
						long executionTime = DateUtil.getCurrentTimestampLong() - jobexe.getStartTime();  // Job 수행시간
						if(executionTime > thresTime){
							List<Map<String, String>> sendInfoList = sqlMapClient.queryForList("nbs.monitor.selectJobNotifySendListForCount", jobexe.getJobId());
							if(sendInfoList != null && sendInfoList.size() > 0) { // 조회건수가 없더라도 empty list 가 리턴되므로 size() 로 체크해야함 
								selectedReceivers.addAll(notify.getReceiverList());
							}
						}
					}
				}
			}

			// 수신자 목록 상세 작성
			if (selectedReceivers.size()  > 0) {
				List<JobNotifyReceiver> receiverList = new ArrayList<JobNotifyReceiver>(selectedReceivers.size());
				for (int receiverId : selectedReceivers) {
					JobNotifyReceiver receiver = getJobNotifyReceiver(receiverId);
					if (receiver != null) {
						receiverList.add(receiver);
					}
				}
				
				// 통지 수행
				if (receiverList.size() > 0) {
					for (IJobEndNotifyProcessor processor : notifyProcessors) {
						try {
							processor.doNotify(jobexe, matchingNotify, receiverList);
						}catch(Throwable e) {
							// 하나 에러나더라도 다음꺼 계속 실행함.
							Util.logError(log, MSG.get("main.jobnotify.notify.process.error", jobexe.getJobExecutionId(), processor.toString()), e); // {0} Job 통지 수행 중 {1} 에서 에러가 발생하였습니다
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void processJobLongRun(Map<String, JobExecution> jobexeMap) throws Exception {
		
		checkAndRefreshCache(false);  // 캐쉬 한번 점검한다.
		
		Map<Integer, JobNotify> receiverMap  = new HashMap<Integer, JobNotify>(); // Integer: receiverId
		Map<JobNotify, Set<Integer>> notifyMap = new HashMap<JobNotify, Set<Integer>>();
		
		if (notifyProcessors != null) {
			//
			// Job Execution 별 LongRun 설정된 통지 리스트 추출
			// 수산지별로 통지 정보 관리
			//
			Set<String> jobexeIdSet = jobexeMap.keySet();
			for(String jobexeId : jobexeIdSet){
				receiverMap.clear();
				notifyMap.clear();
				
				// 해당 Job Execution이 매칭 되는 LongRun 통지 리스트 추출 및 통지 수신자 리스트 세팅
				List<JobNotify> notifyList =  selectJobNotifyByQuery("", "ORDER BY JOB_ID_EXP");
				for (JobNotify notify :  notifyList) {
					if ("LONGRUN".equals(notify.getWhen())) {
						Matcher mat = notify.getJobIdExpressionPattern().matcher(jobexeMap.get(jobexeId).getJobId());
						if (mat.matches()) {
							List<Integer> receiverList = notify.getReceiverList();
							for(int receiverId : receiverList){
								if(!receiverMap.containsKey(receiverId))
									receiverMap.put(receiverId, notify);
							}
						}
					}
				}
			
				if(!receiverMap.isEmpty()){
					//
					// 위에서 추출한 통지 리스트로 실제 통지가 필요한 퉁지 수신자 리스트 추출
					//
					List<Map<String, String>> sendInfoList = sqlMapClient.queryForList("nbs.monitor.selectJobNotifySendListForCount", jobexeId);
					
					Set<Integer> receiverSet = receiverMap.keySet();
					for(int receiverId : receiverSet) {
						JobNotify notify = receiverMap.get(receiverId);
					
						long thresTime = Long.parseLong(notify.getCheckValue1()) * 60000;        // LongRun 시간
						long reculsiveTime = Long.parseLong(notify.getCheckValue2()) * 60000;    // 전송 주기
						int notifyMaxCount = Integer.parseInt(notify.getCheckValue3());          // 최대 전송 횟수
						long executionTime = DateUtil.getCurrentTimestampLong() - jobexeMap.get(jobexeId).getStartTime();  // Job 수행시간
						int sendCount = -1;
						long sendTime = 0;
						
						// 통지 수신자에 대한 전송 횟수 및 전송 시간 추출 
						for(Map<String, String> sendInfo : sendInfoList){
							if(Integer.parseInt(sendInfo.get("receiverId")) == receiverId){
								sendCount = Integer.parseInt(sendInfo.get("sendCount"));
								sendTime = DateUtil.getTimestampLong(sendInfo.get("sendTime"));
								break;
							}
						}
						long intervalTime = DateUtil.getCurrentTimestampLong() - sendTime;       // 지난 통지 후 경과 시간 추출
						
						// 실행 시간이 LongRun 시간 보다 크고, 통지 횟수가 최대 전송 횟수보다 작고, 전송 주기가 지났으면 해당 수신자를 Set 에 추가
						if((thresTime < executionTime) && (sendCount==0 || (sendCount < notifyMaxCount && intervalTime > reculsiveTime))){
							Set<Integer> notifyReceiverSet = notifyMap.get(notify);
							if(notifyReceiverSet == null){
								notifyReceiverSet = new HashSet<Integer>();
								notifyMap.put(notify, notifyReceiverSet);
							}
							notifyReceiverSet.add(receiverId);
						}
					}
					
					//
					// 통지 작업 수행
					//
					if (notifyMap.size() > 0) {
						Set<JobNotify> notifySet = notifyMap.keySet();
						for(JobNotify notify : notifySet){
							Set<Integer> notifyReceiverSet = notifyMap.get(notify);
							
							List<JobNotifyReceiver> receiverList = new ArrayList<JobNotifyReceiver>(notifyReceiverSet.size());
							for (int receiverId : notifyReceiverSet) {
								JobNotifyReceiver receiver = getJobNotifyReceiver(receiverId);
								if (receiver != null) {
									receiverList.add(receiver);
								}
							}
							
							// 통지 수행
							if (receiverList.size() > 0) {
								for (IJobEndNotifyProcessor processor : notifyProcessors) {
									try {
										processor.doNotify(jobexeMap.get(jobexeId), notify, receiverList);
									}catch(Throwable e) {
										// 하나 에러나더라도 다음꺼 계속 실행함.
										Util.logError(log, MSG.get("main.jobnotify.notify.process.error", jobexeId, processor.toString()), e); // {0} Job 통지 수행 중 {1} 에서 에러가 발생하였습니다
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 해당 Job 의 통지 대상자를 조회함.
	 * 통지 목적이 아니라 Admin 화면에서 각 Job 별로 통지 대상자를 확인하고자 할때 사용함  
	 * @param jobid
	 * @param event "EO" | "EF" or null (all) 
	 * @return List of JobNotifyReceiver
	 * @throws Exception
	 * @since 3.6.4
	 */
	public List<JobNotifyReceiver> getJobNotifyReceiverList(String jobid, String event) throws SQLException {
		checkAndRefreshCache(false);  // 캐쉬 한번 점검한다.
		// 통지 대상자 선정
		Set<Integer> selectedReceivers = new HashSet<Integer>();
		for (JobNotify notify :  cache.values()) {
			Matcher mat = notify.getJobIdExpressionPattern().matcher(jobid);
			if (mat.matches()) {
				if (event == null || Util.equalsIgnoreNull(event, notify.getWhen())) { // null 인 경우는 
					selectedReceivers.addAll(notify.getReceiverList());
				}
			}
		}

		// 수신자 목록 상세 작성
		if (selectedReceivers.size()  > 0) {
			List<JobNotifyReceiver> receiverList = new ArrayList<JobNotifyReceiver>(selectedReceivers.size());
			for (int receiverId : selectedReceivers) {
				JobNotifyReceiver receiver = getJobNotifyReceiver(receiverId);
				if (receiver != null) {
					receiverList.add(receiver);
				}
			}
			return receiverList;
		}else {
			return Collections.EMPTY_LIST;
		}
	}
}
