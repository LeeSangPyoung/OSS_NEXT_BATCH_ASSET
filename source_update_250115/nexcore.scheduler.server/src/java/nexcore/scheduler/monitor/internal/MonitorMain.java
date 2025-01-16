package nexcore.scheduler.monitor.internal;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import com.ibatis.sqlmap.client.SqlMapClient;

import nexcore.scheduler.agent.client.IAgentClient;
import nexcore.scheduler.controller.ha.IPeerClient;
import nexcore.scheduler.controller.internal.JobExecutionManager;
import nexcore.scheduler.core.internal.JobInstanceManager;
import nexcore.scheduler.core.internal.LicenseManager;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.AgentInfo;
import nexcore.scheduler.entity.AgentMonitoringSummary;
import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobGroup;
import nexcore.scheduler.entity.JobGroupAttrDef;
import nexcore.scheduler.entity.JobNotify;
import nexcore.scheduler.entity.JobNotifyReceiver;
import nexcore.scheduler.entity.JobNotifySendInfo;
import nexcore.scheduler.entity.User;
import nexcore.scheduler.entity.ViewFilter;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : MonitorMain </li>
 * <li>작성일 : 2011. 1. 14.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class MonitorMain {
	private SqlMapClient              sqlMapClient;
	private ViewFilterManager         viewFilterManager;
	private AgentInfoManager          agentInfoManager;
	private AgentMonitor              agentMonitor;
	private JobNotifyManager          jobNotifyManager;
	private UserManager               userManager;
	private JobExecutionManager       jobExecutionManager;
	private JobInstanceManager        jobInstanceManager;
	private JobProgressStatusManager  jobProgressStatusManager;
	private JobGroupManager           jobGroupManager;
	private IPeerClient               peerClient;
	private LicenseManager            licenseManager;
	
	private Log                       log;
	
	public MonitorMain() {
	}
	
	public void init() {
		log = LogManager.getSchedulerLog();
		Util.logServerInitConsole("MonitorMain");
	}
	
	public void destroy() {
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public ViewFilterManager getViewFilterManager() {
		return viewFilterManager;
	}

	public void setViewFilterManager(ViewFilterManager viewFilterManager) {
		this.viewFilterManager = viewFilterManager;
	}

	public AgentInfoManager getAgentInfoManager() {
		return agentInfoManager;
	}

	public void setAgentInfoManager(AgentInfoManager agentInfoManager) {
		this.agentInfoManager = agentInfoManager;
	}

	public AgentMonitor getAgentMonitor() {
		return agentMonitor;
	}

	public void setAgentMonitor(AgentMonitor agentMonitor) {
		this.agentMonitor = agentMonitor;
	}

	public JobNotifyManager getJobNotifyManager() {
		return jobNotifyManager;
	}

	public void setJobNotifyManager(JobNotifyManager jobNotifyManager) {
		this.jobNotifyManager = jobNotifyManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}
	
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}
	
	public JobExecutionManager getJobExecutionManager() {
		return jobExecutionManager;
	}

	public void setJobExecutionManager(JobExecutionManager jobExecutionManager) {
		this.jobExecutionManager = jobExecutionManager;
	}

	public JobInstanceManager getJobInstanceManager() {
		return jobInstanceManager;
	}
	
	public void setJobInstanceManager(JobInstanceManager jobInstanceManager) {
		this.jobInstanceManager = jobInstanceManager;
	}
	
	public JobProgressStatusManager getJobProgressStatusManager() {
		return jobProgressStatusManager;
	}

	public void setJobProgressStatusManager(JobProgressStatusManager jobProgressStatusManager) {
		this.jobProgressStatusManager = jobProgressStatusManager;
	}
	
	public JobGroupManager getJobGroupManager() {
		return jobGroupManager;
	}

	public void setJobGroupManager(JobGroupManager jobGroupManager) {
		this.jobGroupManager = jobGroupManager;
	}

	public IPeerClient getPeerClient() {
		return peerClient;
	}
	
	public void setPeerClient(IPeerClient peerClient) {
		this.peerClient = peerClient;
	}
	
	public LicenseManager getLicenseManager() {
		return licenseManager;
	}

	public void setLicenseManager(LicenseManager licenseManager) {
		this.licenseManager = licenseManager;
	}
	
	
	
	// ==============================
	

	private SchedulerException logAndMakeSchedulerException(String msgName, Throwable e, Object ... param) {
		if (e==null) {
			Util.logError(log, MSG.get(msgName, param));
			return new SchedulerException(msgName, param);
		}else {
			Util.logError(log, MSG.get(msgName, param), e);
			if (e instanceof SchedulerException) {
				return (SchedulerException) e;
			}else {
				return new SchedulerException(msgName, e, param);
			}
		}
	}
	
	private SchedulerException logAndMakeSchedulerException(String msgName, AdminAuth auth, Throwable e, Object ... param) {
		if (e==null) {
			Util.logError(log, MSG.get(msgName, param)+" ["+auth.toString()+"]");
			return new SchedulerException(msgName, param);
		}else {
			Util.logError(log, MSG.get(msgName, param)+" ["+auth.toString()+"]", e);
			if (e instanceof SchedulerException) {
				return (SchedulerException) e;
			}else {
				return new SchedulerException(msgName, e, param);
			}
		}
	}

	// #############################################################
	// ###                       ViewFilter 
	// #############################################################
	
	public boolean addViewFilter(ViewFilter vf, AdminAuth auth) {
		try {
			sqlMapClient.startTransaction();
			int cnt =viewFilterManager.addViewFilter(vf);
			sqlMapClient.commitTransaction();
			return cnt > 0;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.vf.admin.error", e, 0, vf.toString()); // 뷰필터 생성시 오류가 발생하였습니다
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public ViewFilter getViewFilter(int id) {
		try {
			return viewFilterManager.getViewFilter(id);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.vf.admin.error", e, 1, id); // 뷰필터 조회시 오류가 발생하였습니다
		}
	}
	
	public ViewFilter getViewFilterDeep(int id) {
		try {
			return viewFilterManager.getViewFilterDeep(id);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.vf.admin.error", e, 1, id); // 뷰필터 조회시 오류가 발생하였습니다
		}
	}
	
	public List<ViewFilter> getViewFiltersByQuery(String query, String orderBy) {
		try {
			return viewFilterManager.getViewFiltersByQuery(query, orderBy);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.vf.admin.error", e, 1, query); // 뷰필터 조회시 오류가 발생하였습니다
		}
	}
	
	public List<JobDefinition> getJobDefinitionsByViewFilter(int id) {
		try {
			return viewFilterManager.getJobDefinitionsByViewFilter(id);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.vf.getjobdef.error", e, id); // 뷰필터의 Job 등록정보 조회시 오류가 발생하였습니다
		}
	}

	
	public boolean removeViewFilter(int id, AdminAuth auth) {
		try {
			sqlMapClient.startTransaction();
			int cnt = viewFilterManager.removeViewFilter(id);
			sqlMapClient.commitTransaction();
			return cnt > 0;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.vf.admin.error", auth, e, 3, id); // 뷰필터 삭제 중 오류가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public boolean modifyViewFilter(ViewFilter vf, AdminAuth auth) {
		try {
			sqlMapClient.startTransaction();
			int cnt = viewFilterManager.modifyViewFilter(vf);
			sqlMapClient.commitTransaction();
			return cnt > 0;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.vf.admin.error", auth, e, 2, vf.toString()); // 뷰필터 변경 중 오류가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public boolean modifyViewFilterNoJobList(ViewFilter vf, AdminAuth auth) {
		try {
			sqlMapClient.startTransaction();
			int cnt = viewFilterManager.modifyViewFilterNoJobList(vf);
			sqlMapClient.commitTransaction();
			return cnt > 0;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.vf.admin.error", auth, e, 2, vf.toString()); // 뷰필터 변경 중 오류가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}

	public boolean modifyViewFilterAddJobList(int id, List<String> jobIdList, AdminAuth auth) {
		try {
			sqlMapClient.startTransaction();
			int cnt = viewFilterManager.modifyViewFilterAddJobList(id, jobIdList);
			sqlMapClient.commitTransaction();
			return cnt > 0;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.vf.admin.error", auth, e, 2, id); // 뷰필터 변경 중 오류가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}

	public boolean modifyViewFilterDelJobList(int id, List<String> jobIdList, AdminAuth auth) {
		try {
			sqlMapClient.startTransaction();
			int cnt = viewFilterManager.modifyViewFilterDelJobList(id, jobIdList);
			sqlMapClient.commitTransaction();
			return cnt > 0;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.vf.admin.error", auth, e, 2, id); // 뷰필터 변경 중 오류가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}

	
	// #############################################################
	// ###                       Agent  
	// #############################################################
	
	public List<JobExecution> getAgentRunningJobExecutions(String agentId) {
		IAgentClient agentClient = agentInfoManager.getAgentClient(agentId);
		return agentClient.getRunningJobExecutions();
	}
	
	public Map getAgentAllThreadStackTrace(String agentId) {
		IAgentClient agentClient = agentInfoManager.getAgentClient(agentId);
		return agentClient.getAllThreadStackTrace();
	}
	
	/**
	 * 에이전트의 JVM Properties 조회
	 */
	public Properties getAgentSystemProperties(String agentId) {
		IAgentClient agentClient = agentInfoManager.getAgentClient(agentId);
		return agentClient.getSystemProperties();
	}

	/**
	 * 에이전트의 JVM System Env 조회
	 * @param agentId
	 * @return
	 */
	public Map getAgentSystemEnv(String agentId) {
		IAgentClient agentClient = agentInfoManager.getAgentClient(agentId);
		return agentClient.getSystemEnv();
	}

	/**
	 * 에이전트의 설정 파일 목록을 조회
	 * @param agentId
	 * @return
	 */
	public List<String> getAgentConfigFiles(String agentId) {
		IAgentClient agentClient = agentInfoManager.getAgentClient(agentId);
		return agentClient.getConfigFilenames();
	}

	/**
	 * agent 리스트 들의 상태를 체크해서 리턴함.
	 * @return Map&lt;String:(agentId), String:(checkResult)&gt;. checkResult="OK"이면 정상 
	 */
	public Map getAgentCheckList() {
		return agentMonitor.getAgentAliveMsgList();
	}

	/**
	 * 에이전트의 상태 체크. AgentMonitor 개 2초마다 폴링하여 캐쉬한 정보를 리턴함
	 * @param agentId
	 * @return "OK" if normal, other message if abnormal
	 */
	public String getAgentCheck(String agentId) {
		try {
			String msg = agentMonitor.getAgentAliveMsg(agentId);
			if (msg==null) {
				return MSG.get("main.agent.wrong.agentid", agentId);
			}else {
				return msg;
			}
		}catch (Exception e) {
			return "FAIL:"+e.getMessage();
		}
	}
	
	/**
	 * 에이전트의 상태 체크. 캐쉬정보를 이용하지 않고 즉시 체크한다.
	 * @param agentId
	 * @return "OK" if normal, other message if abnormal
	 */
	public String getAgentCheckNoCache(String agentId) {
		try {
			IAgentClient agentClient = agentInfoManager.getAgentClient(agentId);
			return agentClient.isAlive();
		}catch (Exception e) {
			return "FAIL:"+e.getMessage();
		}
	}
	

	/**
	 * Job Execution 의 진행 상태 값 (Progress)을 조회.
	 * 실시간 즉시 조회. 이 메소드는 성능 문제를 일으킬 수 있으므로 사용 자제.
	 * @return Long[] ([0]=progressTotal, [1]=progressCurrent)
	 */
	public long[] getJobProgressNoCache(String jobExecutionId, String agentId) {
		try {
			IAgentClient agentClient = agentInfoManager.getAgentClient(agentId);
			JobExecution jobexe      = agentClient.getJobExecution(jobExecutionId);
			if (jobexe == null) {
				// 이미 종료된 상태이므로 DB 에 최종 progress 값을 select 해서 리턴한다.
				jobexe = jobExecutionManager.getJobExecution(jobExecutionId);
			}
			
			if (jobexe == null) {
				return null;
			}else {
				return new long[]{jobexe.getProgressTotal(), jobexe.getProgressCurrent()};
			}
		}catch (Exception e) {
			throw logAndMakeSchedulerException("main.agent.get.progress.info.error", e); // Job 진행 값 조회 중 에러가 발생하였습니다
		}
	}

	/**
	 * Job Execution 의 진행 상태 값 (Progress)을 조회.
	 * 실시간 즉시 조회. 이 메소드는 성능 문제를 일으킬 수 있으므로 사용 자제.
	 * @return Long[] ([0]=progressTotal, [1]=progressCurrent)
	 */
	public long[] getJobProgress(String jobExecutionId) {
		return jobProgressStatusManager.getJobExeProgress(jobExecutionId);
	}

	/**
	 * Job Instance 의 진행 상태 값 (Progress)을 조회. 
	 * 에이전트와 통신하여 현재 running 인 Job 인스턴스의 진행값만 조회함.
	 * 결과 Map의 key는 Job Instance ID
	 * @return &lt; jobInstanceId, long[] ([0]=progressTotal, [1]=progressCurrent) &gt;
	 */
	public Map<String, long[]> getJobProgressMapFromAgent() {
		List<JobExecution> runningJobExecutions = new LinkedList<JobExecution>();
		try {
			for (AgentInfo agentInfo : agentInfoManager.getAllAgentInfos()) {
				try {
					runningJobExecutions.addAll(getAgentRunningJobExecutions(agentInfo.getId()));
				}catch(Exception e){
					// 하나의 agent 가 에러나도 그냥 나머지 돌린다.
				}
			}
		}catch (Exception e) {
			throw logAndMakeSchedulerException("main.agent.get.progress.info.error", e); // Job 진행 값 조회 중 에러가 발생하였습니다
		}
		
		Map<String, long[]> retMap = new HashMap<String, long[]>();
		for (JobExecution jobexe : runningJobExecutions) {
			retMap.put(jobexe.getJobInstanceId(), new long[]{jobexe.getProgressTotal(), jobexe.getProgressCurrent()});
		}

		return retMap;
	}

	public AgentMonitoringSummary getAgentMonitoringSummary(String agentId) {
		AgentMonitoringSummary summary = new AgentMonitoringSummary();
		summary.setAgentId(agentId);
		
		AgentInfo    agentInfo   = null;
		IAgentClient agentClient = null;
		try {
			agentInfo = agentInfoManager.getAgentInfo(agentId);
			if (!agentInfo.isInUse()) {
				summary.setAgentConnectionError(MSG.get("main.agent.unused"));
				return summary;
			}
			
			String agentAliveMsg = agentMonitor.getAgentAliveMsg(agentId);
			if ("OK".equals(agentAliveMsg)) { // 에이전트가 정상일때만 통신시도함.
				agentClient = agentInfoManager.getAgentClient(agentId);
				
				summary.setBootTime(              agentClient.getBootTime());
				summary.setAlive(                 agentClient.isAlive());
				summary.setClosed(                agentClient.isClosed());
				summary.setJvmMonitoringInfo(     agentClient.getJVMMonitoringInfo());
				summary.setRunningJobCount(       agentClient.getRunningJobExecutionsCount());
				summary.setThreadCount(           agentClient.getAllThreadCount());
			}else {
				String msg = MSG.get("main.agent.monitoring.info.error", agentId); // 에이전트의 모니터링 정보 수집 중 에러가 발생하였습니다
				summary.setAgentConnectionError(msg+"/"+MSG.get("main.agent.agentclient.connect.error", agentId)+"/"+agentAliveMsg);
			}
		}catch (Exception e) {
			String msg = MSG.get("main.agent.monitoring.info.error", agentId); // 에이전트의 모니터링 정보 수집 중 에러가 발생하였습니다
			Util.logError(log, msg, e);
			summary.setAgentConnectionError(msg+"/"+e.getMessage());
		}
		
		return summary;
	}
	
	public List<AgentInfo> getAllAgentInfos() {
		try {
			return agentInfoManager.getAllAgentInfos();
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.agent.admin.error", e, 1, "ALL"); // 에이전트 정보 조회중 에러가 발생하였습니다
		}
	}

	/**
	 * 캐쉬된 AgentInfo 목록 가져오기.
	 * @return
	 */
	public List<AgentInfo> getCachedAgentInfos() {
		try {
			return agentInfoManager.getCachedAgentInfos();
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.agent.admin.error", e, 1, "ALL"); // 에이전트 정보 조회중 에러가 발생하였습니다
		}
	}

	public AgentInfo getAgentInfo(String id) {
		try {
			return agentInfoManager.getAgentInfo(id);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.agent.admin.error", e, 1, id); // 에이전트 정보 조회 중 에러가 발생하였습니다
		}
	}

	public boolean addAgentInfo(AgentInfo agentInfo, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		
		try {
			int agentCount = agentInfoManager.getAgentInfoCountExceptInternal();
			int maxAgentCount = licenseManager.getMaxAgentCount();
			
			if(maxAgentCount != 0 && agentCount >= maxAgentCount){
				throw logAndMakeSchedulerException("main.agent.maxcount.error", null, agentCount, maxAgentCount);
			}
			
			sqlMapClient.startTransaction();
			int cnt = agentInfoManager.addAgentInfo(agentInfo);
			sqlMapClient.commitTransaction();
			
			log.info(MSG.get("main.agent.admin", auth, 0, agentInfo)); // [{0}]에서 에이전트 정보를 {1,choice,0#추가|1#조회|2#변경|3#삭제} 합니다 {2}
			return cnt > 0;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.agent.admin.error", e, 0, agentInfo.toString()); // 에이전트 정보 추가 중 에러가 발생하였습니다 {0}
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
			if (peerClient.isPeerExist()) {
				peerClient.refreshMemoryCache("AGENT_INFO", agentInfo.getId());
			}
		}
	}

	public boolean removeAgentInfo(String agentId, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			int cnt = agentInfoManager.removeAgentInfo(agentId);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.agent.admin", auth, 3, agentId)); // [{0}]에서 에이전트 정보를 {1,choice,0#추가|1#조회|2#변경|3#삭제} 합니다 {2}
			return cnt > 0;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.agent.admin.error", e, 3, agentId); // 에이전트({0}) 정보 삭제 중 에러가 발생하였습니다
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
			if (peerClient.isPeerExist()) {
				peerClient.refreshMemoryCache("AGENT_INFO", agentId);
			}
		}
	}
	
	public boolean modifyAgentInfo(AgentInfo agentInfo, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			int cnt = agentInfoManager.modifyAgentInfo(agentInfo);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.agent.admin", auth, 2, agentInfo)); // [{0}]에서 에이전트 정보를 {1,choice,0#추가|1#조회|2#변경|3#삭제} 합니다 {2}
			return cnt > 0;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.agent.admin.error", e, 2, agentInfo.toString()); // 에이전트({0}) 정보 변경 중 에러가 발생하였습니다
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
			if (peerClient.isPeerExist()) {
				peerClient.refreshMemoryCache("AGENT_INFO", agentInfo.getId());
			}
		}
	}

	/**
	 * Agent 에서 Job 이 실행되지 않도록 close 함. 
	 * @return
	 */
	public boolean closeOrOpenAgent(String agentId, boolean close, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		AgentInfo    agentInfo   = null;
		IAgentClient agentClient = null;
		try {
			agentInfo = agentInfoManager.getAgentInfo(agentId);
			if (!agentInfo.isInUse()) {
				throw logAndMakeSchedulerException("main.agent.unused", auth, null);
			}
			agentClient = agentInfoManager.getAgentClient(agentId);
			if (close) {
				agentClient.closeAgent(auth);
				log.info(MSG.get("main.agent.closeopen", auth, agentId, close?"CLOSE":"OPEN")); // [{0}]에서 에이전트({1}) 를 {2} 합니다
				return agentClient.isClosed();
			}else {
				agentClient.openAgent(auth);
				log.info(MSG.get("main.agent.closeopen", auth, agentId, close?"CLOSE":"OPEN")); // [{0}]에서 에이전트({1}) 를 {2} 합니다
				return !agentClient.isClosed();
			}
		}catch (Exception e) {
			throw logAndMakeSchedulerException("main.agent.closeopen.error", e, agentId, close?"CLOSE":"OPEN"); // 에이전트({0}) {1} 중 에러가 발생하였습니다
		}
	}
	
	// #############################################################
	// ###                       JobNotify 
	// #############################################################

	public boolean addJobNotify(JobNotify jobNotify, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = jobNotifyManager.addJobNotify(jobNotify);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.admin.action", auth, "addJobNotify", jobNotify));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.admin.error", e, 0, jobNotify.toString()); // Job 통지 정보 추가 중 에러가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
			if (peerClient.isPeerExist()) {
				peerClient.refreshMemoryCache("NOTIFY", null);
			}
		}
	}
	public boolean removeJobNotify(int id, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = jobNotifyManager.removeJobNotify(id);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.admin.action", auth, "removeJobNotify", id));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.admin.error", e, 3, id); // Job 통지 정보 삭제 중 에러가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
			if (peerClient.isPeerExist()) {
				peerClient.refreshMemoryCache("NOTIFY", null);
			}
		}
	}

	public boolean modifyJobNotify(JobNotify jobNotify, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = jobNotifyManager.modifyJobNotify(jobNotify);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.admin.action", auth, "modifyJobNotify", jobNotify));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.admin.error", e, 2, jobNotify.toString()); // Job 통지 정보 변경 중 에러가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
			if (peerClient.isPeerExist()) {
				peerClient.refreshMemoryCache("NOTIFY", null);
			}
		}
	}
	public JobNotify getJobNotify(int id) {
		try {
			return jobNotifyManager.getJobNotify(id);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.admin.error", e, 1, id); // Job 통지 정보 조회 중 에러가 발생하였습니다.
		}
	}
	public List<JobNotify> getAllJobNotifies() {
		try {
			return jobNotifyManager.getAllJobNotifies();
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.admin.error", e, 1, "ALL"); // Job 통지 정보 조회 중 에러가 발생하였습니다.
		}
	}
		
	public boolean addJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = jobNotifyManager.addJobNotifyReceiver(jobNotifyReceiver);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.admin.action", auth, "addJobNotifyReceiver", jobNotifyReceiver));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.receiver.admin.error", e, 0, jobNotifyReceiver); // Job 통지 수신자 정보 조회 중 에러가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public boolean removeJobNotifyReceiver(int id, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = jobNotifyManager.removeJobNotifyReceiver(id);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.admin.action", auth, "removeJobNotifyReceiver", id));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.receiver.admin.error", e, 3, id); // Job 통지 수신자 정보 삭제 중 에러가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public boolean modifyJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = jobNotifyManager.modifyJobNotifyReceiver(jobNotifyReceiver);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.admin.action", auth, "modifyJobNotifyReceiver", jobNotifyReceiver));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.receiver.admin.error", e, 2,jobNotifyReceiver); // Job 통지 수신자 정보 변경 중 에러가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public JobNotifyReceiver getJobNotifyReceiver(int id) {
		try {
			return jobNotifyManager.getJobNotifyReceiver(id);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.receiver.admin.error", e, 1, id); // Job 통지 수신자 정보 조회 중 에러가 발생하였습니다.
		}
	}
	
	public List<JobNotifyReceiver> getAllJobNotifyReceivers() {
		try {
			return jobNotifyManager.getAllJobNotifyReceivers();
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.receiver.admin.error", e, 1, "ALL"); // Job 통지 수신자 정보 조회 중 에러가 발생하였습니다.
		}
	}
	
	public Map<Integer, JobNotifyReceiver> getAllJobNotifyReceiversMap() {
		try {
			return jobNotifyManager.getAllJobNotifyReceiversMap();
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.receiver.admin.error", e, 1, "ALL"); // Job 통지 수신자 정보 조회 중 에러가 발생하였습니다.
		}
	}
	
	public List<JobNotifyReceiver> getJobNotifyReceiversByNotifyId(int id) {
		try {
			return jobNotifyManager.getJobNotifyReceiversByNotifyId(id);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.receiver.admin.error", e, 1, "ByNotifyId"); // Job 통지 수신자 정보 조회 중 에러가 발생하였습니다.
		}
	}
	
	public List<JobNotifySendInfo> getJobNotifySendList(Map queryParamMap) {
		try {
			return jobNotifyManager.selectJobNotifySendList(queryParamMap);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.notifylist.admin.error", e , "ALL"); // Job 통지 목록 조회 중 에러가 발생하였습니다.
		}
	}
	
	public int getJobNotifySendListCount(Map queryParamMap) {
		try {
			return jobNotifyManager.getJobNotifySendListCount(queryParamMap);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.notifylist.count.admin.error", e , "ALL"); // Job 통지 목록  개수 조회 중 에러가 발생하였습니다.
		}
	}

	public List<JobNotifyReceiver> getJobNotifyReceiverList(String jobid, String event) {
		try {
			return jobNotifyManager.getJobNotifyReceiverList(jobid, event);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobnotify.receiver.admin.error", e, 1, "ALL"); // Job 통지 수신자 정보 조회 중 에러가 발생하였습니다.
		}
	}
	
	// #############################################################
	// ###                       JobGroupAttrDef
	// #############################################################
	
	public boolean addJobGroupAttrDef(JobGroupAttrDef jobGroupAttrDef, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = jobGroupManager.insertJobGroupAttrDef(jobGroupAttrDef);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.jobgroup.admin", auth, jobGroupAttrDef, 0));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", auth, e, 0, jobGroupAttrDef.toString());
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public boolean removeJobGroupAttrDef(String id, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = jobGroupManager.deleteJobGroupAttrDef(id);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.jobgroup.admin", auth, id, 3));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", auth, e, 3, id);
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public boolean modifyJobGroupAttrDef(JobGroupAttrDef jobGroupAttrDef, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = jobGroupManager.updateJobGroupAttrDef(jobGroupAttrDef);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.jobgroup.admin", auth, jobGroupAttrDef, 2));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", auth, e, 2, jobGroupAttrDef);
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public JobGroupAttrDef getJobGroupAttrDef(String jobGroupAttrDefId) {
		try {
			return jobGroupManager.selectJobGroupAttrDef(jobGroupAttrDefId);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", e, 1, jobGroupAttrDefId);
		}
	}
	
	public List<JobGroupAttrDef> getAllJobGroupAttrDefs() {
		try {
			return jobGroupManager.selectJobGroupAttrDefByQuery("", " ORDER BY ATTR_ID ");
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", e, 1, "ALL");
		}
	}
	
	public List<JobGroupAttrDef> getJobGroupAttrDefsByQuery(String queryCondition, String orderBy) {
		try {
			return jobGroupManager.selectJobGroupAttrDefByQuery(queryCondition, orderBy);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", e, 1, queryCondition);
		}
	}

	// #############################################################
	// ###                       JobGroup
	// #############################################################
	
	public boolean addJobGroup(JobGroup jobGroup, AdminAuth auth) {
		
// 권한 관리 상세히..		
		try {
			sqlMapClient.startTransaction();
			int cnt = jobGroupManager.insertJobGroup(jobGroup);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.jobgroup.admin", auth, jobGroup, 0));
			return cnt > 0;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", auth, e, 0, jobGroup.toString());
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	/**
	 * 그룹 삭제. 하위 그룹 삭제 안함.
	 * @param id
	 * @param auth
	 * @return
	 */
	public boolean removeJobGroup(String id, AdminAuth auth) {
// 권한 관리 상세히..
		
		try {
			sqlMapClient.startTransaction();
			boolean b = jobGroupManager.deleteJobGroup(id);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.jobgroup.admin", auth, id, 3));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", auth, e, 3, id);
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	/**
	 * Job 그룹 삭제. 하위 그룹도 삭제
	 * @param id
	 * @param auth
	 * @return
	 */
	public boolean removeJobGroupRecursively(String id, AdminAuth auth) {
// 권한 관리 상세히..
		try {
			// child 들을 알아내기위해 전체 대상 tree 모델 구성해야한다
			List<JobGroup> list = getAllJobGroups();
			JobGroupModel  model = JobGroupModel.createModel(list);
			List<JobGroup> childNodeList = model.getChildNodeListRecursively(id);
			
			sqlMapClient.startTransaction();
			boolean b = jobGroupManager.deleteJobGroup(id);
			log.info(MSG.get("main.jobgroup.admin", auth, id, 3));
			// child 들도 지운다.
			for (JobGroup group : childNodeList) {
				jobGroupManager.deleteJobGroup(group.getId());
				log.info(MSG.get("main.jobgroup.admin", auth, id, 3));
			}
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.jobgroup.admin", auth, id, 3));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", auth, e, 3, id);
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
				
			
	public boolean modifyJobGroup(JobGroup jobGroup, AdminAuth auth) {
// 권한 관리 상세히..
		try {
			sqlMapClient.startTransaction();
			boolean b = jobGroupManager.updateJobGroup(jobGroup);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.jobgroup.admin", auth, jobGroup, 2));
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", auth, e, 2, jobGroup);
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public JobGroup getJobGroup(String jobGroupId) {
		try {
			return jobGroupManager.selectJobGroup(jobGroupId);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", e, 1, jobGroupId);
		}
	}
	
	public List<JobGroup> getAllJobGroups() {
		try {
			return jobGroupManager.selectJobGroupByQuery("", " ORDER BY a.GROUP_ID");
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", e, 1, "ALL");
		}
	}

	public List<JobGroup> getJobGroupsByQuery(String queryCondition, String orderBy) {
		try {
			return jobGroupManager.selectJobGroupByQuery(queryCondition, orderBy);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", e, 1, queryCondition);
		}
	}
	
	public List<JobGroup> getJobGroupsByDynamicQuery(Map queryParamMan) {
		try {
			return jobGroupManager.selectJobGroupByDynamicQuery(queryParamMan);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobgroup.admin.error", e, 1, queryParamMan.toString());
		}
	}
	
	/**
	 * 트리구조를 분석하여 top-down 식으로 리스트함
	 * @param flatJobGroupList 트리구조 분석되지 않은 원시 JobGroup list
	 * @return 트리구조로 분석된 JobGroup list
	 */
	public List<JobGroup> analyzeToJobGroupsTreeList(List<JobGroup> flatJobGroupList) {
		JobGroupModel   model = JobGroupModel.createModel(flatJobGroupList);
		return model.traverseDepthFirst();
	}
	
	public List<JobGroup> getJobGroupsTreeListByQuery(String queryCondition, String orderBy) {
		List<JobGroup> list = getJobGroupsByQuery(queryCondition, orderBy);
		JobGroupModel   model = JobGroupModel.createModel(list);
		return model.traverseDepthFirst();
	}
	
	// #############################################################
	// ###                       User 
	// #############################################################
	
	public boolean addUser(User user, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		if (user !=null) {
			try {
				Pattern.compile(user.getOperateJobIdExp());
			}catch(Exception e) {
				// 패턴이 잘못된 경우 
				throw logAndMakeSchedulerException("main.user.jobid.wrong", auth, e, user.getOperateJobIdExp());
			}
		}
		try {
			sqlMapClient.startTransaction();
			boolean b = userManager.addUser(user);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.user.admin", auth, user, 0)); // [{0}]에서 사용자 ({1}) 를 {2,choice,0#추가|1#조회|2#변경|3#삭제|4#비밀번호 변경} 합니다
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.user.admin.error", auth, e, 0, user.toString()); // 사용자 {} 중 에러가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public boolean removeUser(String id, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = userManager.removeUser(id);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.user.admin", auth, id, 3));  // [{0}]에서 사용자 ({1}) 를 {2,choice,0#추가|1#조회|2#변경|3#삭제|4#비밀번호 변경} 합니다
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.user.admin.error", auth, e, 3, id); // 사용자 {} 중 에러가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public boolean modifyUser(User user, AdminAuth auth) {
		userManager.checkAdminPermission(auth);
		try {
			sqlMapClient.startTransaction();
			boolean b = userManager.modifyUser(user);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.user.admin", auth, user, 2));  // [{0}]에서 사용자 ({1}) 를 {2,choice,0#추가|1#조회|2#변경|3#삭제|4#비밀번호 변경} 합니다
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.user.admin.error", auth, e, 2, user); // 사용자 {} 중 에러가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	
	public boolean modifyUserPassword(User user, AdminAuth auth) {
		try {
			sqlMapClient.startTransaction();
			boolean b = userManager.modifyUserPassword(user);
			sqlMapClient.commitTransaction();
			log.info(MSG.get("main.user.admin", auth, user, 4));  // [{0}]에서 사용자 ({1}) 를 {2,choice,0#추가|1#조회|2#변경|3#삭제|4#비밀번호 변경} 합니다
			return b;
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.user.admin.error", auth, e, 4, user); // 사용자 {} 중 에러가 발생하였습니다.
		} finally {
			try {
				sqlMapClient.endTransaction();
			} catch (Exception ignore) {
			}
		}
	}
	

	public User getUser(String userId) {
		try {
			return userManager.getUser(userId);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.user.admin.error", e, 1, userId); // 사용자 {} 중 에러가 발생하였습니다.
		}
	}
	
	public List<User> getAllUsers() {
		try {
			return userManager.getUserByQuery("", "ORDER BY USER_NAME");
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.user.admin.error", e, 1, "ALL"); // 사용자 {} 중 에러가 발생하였습니다.
		}
	}
	
	public List<User> getUsersByQuery(String queryCondition, String orderBy) {
		try {
			return userManager.getUserByQuery(queryCondition, orderBy);
		} catch (SQLException e) {
			throw logAndMakeSchedulerException("main.user.admin.error", e, 1, queryCondition); // 사용자 {} 중 에러가 발생하였습니다.
		}
	}
	
	public boolean isAllowedForOperation(String jobGroupId, String jobId, User user) {
		return userManager.isAllowedForOperation(jobGroupId, jobId, user);
	}

	public User login(String id, String password, String ip) {
		try {
			User user = userManager.login(id, password, ip);
			log.info(MSG.get("main.user.login.ok", id, ip));
			return user;
		}catch (Exception e) {
			throw logAndMakeSchedulerException("main.user.login.fail", e, id, ip); // 로그인 실패하였습니다
		}
	}
	
}
