package nexcore.scheduler.core.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.execution.SqlExecutor;

import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.entity.PreJobCondition;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;
import nexcore.scheduler.util.XmlUtil;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Active Job Instance 관리 </li>
 * <li>작성일 : 2010. 5. 4.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobInstanceManager {
	private SqlMapClient sqlMapClient;
	private Log          log;
	
	public void init() {
		log = LogManager.getSchedulerLog();
	}
	
	public void destroy() {
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	/**
	 * DB로 부터 Job Instance 객체 정보를 모두 새로 읽음.
	 */
	public void refresh() {
	}
	
	private void setLastModifyTime(JobInstance jobins) {
		if (jobins!=null) {
			jobins.setLastModifyTime(DateUtil.getCurrentTimestampString());
		}
	}
	
	/**
	 * 해당 Job Instance 객체 정보를 리턴함
	 * @param jobInstanceId
	 * @return
	 * @throws SchedulerException if not exist  
	 */
	public JobInstance getJobInstance(String jobInstanceId) throws SQLException {
		JobInstance jobins = (JobInstance) sqlMapClient.queryForObject("nbs.scheduler.selectJobInstance", jobInstanceId);
		if (jobins == null) {
			throw new SchedulerException("main.jobins.notfound", jobInstanceId);
		}
		return jobins;
	}
	
	/**
	 * 해당 Job Instance 객체 정보를 리턴함. 다른 점은 NotFoundException 을 throw 하지 않음
	 * @param jobInstanceId
	 * @return
	 */
	public JobInstance getJobInstanceWithoutError(String jobInstanceId) throws SQLException {
		return (JobInstance) sqlMapClient.queryForObject("nbs.scheduler.selectJobInstance", jobInstanceId);
	}
	
	/**
	 * 해당 Job Instance 객체 정보를 리턴함
	 * @param jobInstanceId
	 * @return
	 */
	public JobInstance getJobInstanceDeep(String jobInstanceId) throws SQLException {
		JobInstance jobins = getJobInstance(jobInstanceId);
		loadPreJobConditions(jobins);
		loadPostJobTriggers(jobins);
		loadParameters(jobins);
		return jobins;
	}

	/**
	 * 해당 Job Instance 객체 정보를 리턴함. 파라미터 미포함. 실행조건판단시에는 파라미터가 필요없다. 파라미터를 로드하는 부하를 줄이기 위함.
	 * @param jobInstanceId
	 * @return
	 */
	public JobInstance getJobInstanceNoParams(String jobInstanceId) throws SQLException {
		JobInstance jobins = getJobInstance(jobInstanceId);
		loadPreJobConditions(jobins);
		loadPostJobTriggers(jobins);
		return jobins;
	}

	/**
	 * 해당 Job Instance 의 상태값을 리턴함
	 * @param jobInstanceId
	 * @return
	 */
	public String getJobState(String jobInstanceId) throws SQLException {
		JobInstance jobIns = getJobInstance(jobInstanceId);
		return jobIns == null ? null : jobIns.getJobState();
	}

	public void loadPreJobConditions(JobInstance jobins) throws SQLException {
		List<Map> preJobCondList = sqlMapClient.queryForList("nbs.scheduler.selectJobInsPreJobConditions", jobins.getJobInstanceId());
		
		List<PreJobCondition> list = new ArrayList();
		for (Map<String, String> preJobCond : preJobCondList) {
			list.add(new PreJobCondition(preJobCond));
		}
		jobins.setPreJobConditions(list);
	}

	public void loadPostJobTriggers(JobInstance jobins) throws SQLException {
		List<Map> triggerList = sqlMapClient.queryForList("nbs.scheduler.selectJobInsPostJobTriggers", jobins.getJobInstanceId());
		
		List<PostJobTrigger> list = new ArrayList();
		for (Map<String, String> postJobTrigger : triggerList) {
			list.add(new PostJobTrigger(postJobTrigger));
		}
		jobins.setTriggerList(list);
	}

	/**
	 * 선행 조건 정보를 DB 테이블에서 로드함
	 * @param jobIns
	 */
	public void loadParameters(JobInstance jobins) throws SQLException {
		Map retval = (Map)sqlMapClient.queryForObject("nbs.scheduler.selectJobInsParams", jobins.getJobInstanceId());
		String xml = retval == null ? null : (String)retval.get("DATA_XML");
		jobins.setInParameters(xml == null ? new LinkedHashMap() : XmlUtil.toMap(xml)); 
	}

	/**
	 * JobInstance deep query 시 3개 테이블을 읽어와 하나의 JobInstance 객체로 구선한다.
	 * NBS_JOB_INS, NBS_JOB_INS_PREJOB, NBS_JOB_INS_OBJ_STORE 테이블은 1:N:M 관계이지만
	 * 성능을 위해 개별로 쿼리한 후 메모리상에서 조립한다.
	 * 
	 * @param jobinsMap
	 * @param preJobsList
	 * @param triggersList
	 * @param paramsList
	 */
	private void assembleJobInstancesDeep(Map<String, JobInstance> jobinsMap, List<Map> preJobsList, List<Map> triggersList, List<Map> paramsList) {
		// Prejob 구성 
		for (Map<String, String> preJobs : preJobsList) {
			JobInstance jobins = jobinsMap.get(preJobs.get("JOB_INSTANCE_ID"));
			if (jobins != null) {
				jobins.getPreJobConditions().add(new PreJobCondition(preJobs));
			}
		}

		// PostJobTrigger 구성 
		for (Map<String, String> trigger : triggersList) {
			JobInstance jobins = jobinsMap.get(trigger.get("JOB_INSTANCE_ID"));
			if (jobins != null) {
				jobins.getTriggerList().add(new PostJobTrigger(trigger));
			}
		}
		
		// 파라미터 구성
		for (Map<String, String> params : paramsList) {
			JobInstance jobins = jobinsMap.get(params.get("JOB_INSTANCE_ID"));
			if (jobins != null) {
				String xml = (String)params.get("DATA_XML");
				jobins.getInParameters().putAll(xml == null ? new LinkedHashMap() : XmlUtil.toMap(xml));
			}
		}
	}
	
	/**
	 * 선행조건 체크 목적으로 JobInstance 의 상태 정보를 조회하되
	 * 불필요한 컬럼은 제외하고 JobInstanceId, JobState, LastJobExeId 만 조회한다.
	 * @param jobId
	 * @param procDate
	 * @return List of Map(JobInstance). Keys of map is {"JOB_INSTANCE_ID", "JOB_ID", "JOB_STATE", "LAST_JOB_EXE_ID"}
	 * @since 3.6.3 (2013.7.28)
	 */
	public List<Map> getJobInstancesStateByJobId(String jobId, String procDate) throws SQLException {
		Map param = new HashMap();
		param.put("columnList",         "JOB_INSTANCE_ID, JOB_ID, JOB_STATE, LAST_JOB_EXE_ID");
		param.put("jobInstanceIdLike",  jobId+procDate+"%");
		param.put("jobId",              jobId);
		param.put("orderBy",            "");
		
		return (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobInstancesFreeColumnByDynamicQuery", param);
	}

	public List<JobInstance> getJobInstancesByQuery(String query, String orderBy) throws SQLException {
		Map param = new HashMap();
		param.put("queryCondition", query);
		param.put("orderBy",        orderBy);
		
		return (List<JobInstance>)sqlMapClient.queryForList("nbs.scheduler.selectJobInstancesByQuery", param);
	}

	public List<JobInstance> getJobInstancesDeepByQuery(String query, String orderBy) throws SQLException {
		Map param = new HashMap();
		param.put("queryCondition", query);
		param.put("orderBy",        orderBy);
		
		List<JobInstance> jobinsList = (List<JobInstance>)sqlMapClient.queryForList("nbs.scheduler.selectJobInstancesByQuery", param);
		List<Map> preJobsList  = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobInsPreJobConditionsByQuery", param);
		List<Map> triggersList = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobInsPostJobTriggersByQuery", param);
		List<Map> paramsList   = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobInsParamsByQuery", param);
		
		// JobInstance map 구성
		Map<String, JobInstance> jobinsMap = new LinkedHashMap<String, JobInstance>();
		for (JobInstance jobins : jobinsList) {
			jobinsMap.put(jobins.getJobInstanceId(), jobins);
		}
		
		// 선행 조건, 파라미터 구성 
		assembleJobInstancesDeep(jobinsMap, preJobsList, triggersList, paramsList);

		return new ArrayList<JobInstance>(jobinsMap.values());
	}

	/**
	 * $queryCondition$ 방식이 아닌 ibatis dynamic 태그를 이용한 쿼리.
	 * @param queryParamMap
	 * @return
	 * @throws SQLException
	 */
	public List<JobInstance> getJobInstancesByDynamicQuery(Map queryParamMap) throws SQLException {
		return getJobInstancesByDynamicQuery(queryParamMap, SqlExecutor.NO_SKIPPED_RESULTS, SqlExecutor.NO_MAXIMUM_RESULTS);
	}

	/**
	 * $queryCondition$ 방식이 아닌 ibatis dynamic 태그를 이용한 쿼리.
	 * @param queryParamMap
	 * @return
	 * @throws SQLException
	 */
	public List<JobInstance> getJobInstancesDeepByDynamicQuery(Map queryParamMap) throws SQLException {
		return getJobInstancesDeepByDynamicQuery(queryParamMap, SqlExecutor.NO_SKIPPED_RESULTS, SqlExecutor.NO_MAXIMUM_RESULTS);
	}

	/**
	 * $queryCondition$ 방식이 아닌 ibatis dynamic 태그를 이용한 쿼리. skip, max 를 이용하여 부분만 조회한다.
	 * @param queryParamMap
	 * @param skip
	 * @param maxResult
	 * @return
	 * @throws SQLException
	 */ // 2012-08-29. 배치 Admin 에서 JobIns 페이징 지원을 위해 추가됨
	public List<JobInstance> getJobInstancesByDynamicQuery(Map queryParamMap, int skip, int maxResult) throws SQLException {
		return (List<JobInstance>)sqlMapClient.queryForList("nbs.scheduler.selectJobInstancesByDynamicQuery", queryParamMap, skip, maxResult);
	}

	/**
	 * $queryCondition$ 방식이 아닌 ibatis dynamic 태그를 이용한 쿼리. skip, max 를 이용하여 부분만 조회한다.
	 * @param queryParamMap
	 * @param skip
	 * @param maxResult
	 * @return
	 * @throws SQLException
	 */ // 2012-08-29. 배치 Admin 에서 JobIns 페이징 지원을 위해 추가됨
	public List<JobInstance> getJobInstancesDeepByDynamicQuery(Map queryParamMap, int skip, int maxResult) throws SQLException {
		// 여기서는 위의 prejob, param 들을 조회시에 SQL을 이용하여 부분 조회를 하는 것이 아니므로 위와는 다르게 해야한다.
		List<JobInstance> jobinsList = (List<JobInstance>)sqlMapClient.queryForList("nbs.scheduler.selectJobInstancesByDynamicQuery", queryParamMap, skip, maxResult);
		
		// JobInstance map 구성
		Map<String, JobInstance> jobinsMap = new LinkedHashMap<String, JobInstance>();
		for (JobInstance jobins : jobinsList) {
			jobinsMap.put(jobins.getJobInstanceId(), jobins);
		}
		
		// Prejob, Param 정보 조회한다. 100 개씩 짤라서 여러번 쿼리한다.
		List<String> jobInsIdList = new ArrayList(110);
		for (JobInstance jobins : jobinsList) {
			jobInsIdList.add(jobins.getJobInstanceId());
			
			if (jobInsIdList.size() == 100) { // 100 개씩 잘라서 하위 쿼리한다.
				Map sqlin = new HashMap();
				sqlin.put("jobInstanceIdList", jobInsIdList);
				List<Map> preJobsList  = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobInsPreJobConditionsByJobInsIdList", sqlin);
				List<Map> triggersList = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobInsPostJobTriggersByJobInsIdList",  sqlin);
				List<Map> paramsList   = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobInsParamsByJobInsIdList",           sqlin);
				
				// 선행 조건, 파라미터 구성 
				assembleJobInstancesDeep(jobinsMap, preJobsList, triggersList, paramsList);
				
				// list 청소
				jobInsIdList.clear();
			}
		}
		
		// 100 * n 이후의 짜투리 처리
		if (jobInsIdList.size() > 0) { 
			Map sqlin = new HashMap();
			sqlin.put("jobInstanceIdList", jobInsIdList);
			List<Map> preJobsList  = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobInsPreJobConditionsByJobInsIdList", sqlin);
			List<Map> triggersList = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobInsPostJobTriggersByJobInsIdList",  sqlin);
			List<Map> paramsList   = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobInsParamsByJobInsIdList",           sqlin);
			
			// 선행 조건, 파라미터 구성 
			assembleJobInstancesDeep(jobinsMap, preJobsList, triggersList, paramsList);
		}
		
		return new ArrayList<JobInstance>(jobinsMap.values());
	}
	
	/**
	 * $queryCondition$ 방식이 아닌 ibatis dynamic 태그를 이용한 쿼리.
	 * 결과 건수 조회
	 * @param queryParamMap
	 * @return
	 * @throws SQLException
	 */
	public int getJobInstanceCountByDynamicQuery(Map queryParamMap) throws SQLException {
		return (Integer)sqlMapClient.queryForObject("nbs.scheduler.selectJobInstanceCountByDynamicQuery", queryParamMap);
	}

	/**
	 * $queryCondition$ 방식이 아닌 ibatis dynamic 태그를 이용한 쿼리. 
	 * 대량 조회를 대비해 rowHandler 를 이용한다.
	 * @param queryParamMap
	 * @param rowHandler
	 * @throws SQLException
	 */ // 2013-03-04. 배치 Admin 에서 엑셀다운로드시 대량 조회를 대비
	public void getJobInstancesByDynamicQueryWithRowHandler(Map queryParamMap, Object rowHandler) throws SQLException {
		sqlMapClient.queryWithRowHandler("nbs.scheduler.selectJobInstancesByDynamicQuery", queryParamMap, (RowHandler)rowHandler);
	}
	
	/**
	 * 필요한 컬럼만 조회한다. ($columnList$ 에 컬럼 목록 담는다)
	 * $queryCondition$ 방식이 아닌 ibatis dynamic 태그를 이용한 쿼리. 
	 * 대량 조회를 대비해 rowHandler 를 이용한다.
	 * @param queryParamMap
	 * @param rowHandler
	 * @throws SQLException
	 */ // 2013-03-04. Job 그룹 모니터링시 성능을 위해 불필요한 컬럼 배제
	public void getJobInstanceListFreeColumnWithRowHandler(Map queryParamMap, Object rowHandler) throws SQLException {
		sqlMapClient.queryWithRowHandler("nbs.scheduler.selectJobInstancesFreeColumnByDynamicQuery", queryParamMap, (RowHandler)rowHandler);
	}
	
	
	/**
	 * 필요한 컬럼만 조회한다. ($columnList$ 에 컬럼 목록 담는다)
	 * $queryCondition$ 방식이 아닌 ibatis dynamic 태그를 이용한 쿼리. 
	 * 대량 조회를 대비해 rowHandler 를 이용한다.
	 * @param queryParamMap
	 * @return List of Map (컬럼명, 값)
	 * @throws SQLException
	 * @since 3.6.3
	 */ // 2013-08-16. 성능을 위해 불필요한 컬럼 배제
	public List<Map> getJobInstanceListFreeColumn(Map queryParamMap) throws SQLException {
		return sqlMapClient.queryForList("nbs.scheduler.selectJobInstancesFreeColumnByDynamicQuery", queryParamMap);
	}
	
	//================================================================================================================================
	//================================================================================================================================

	public String getLastJobInstanceId(String jobId, String procDate) throws SQLException {
		Map param = new HashMap();
		param.put("jobId",    jobId);
		param.put("procDate", procDate);
		
		return (String)sqlMapClient.queryForObject("nbs.scheduler.selectLastJobInstanceId", param);
	}
	
	/**
	 * 테이블, 메모리에 Job Instance 하나 추가.
	 * @param jobins
	 */
	public int insertJobInstance(JobInstance jobins) throws SQLException {
		// NBS_JOB_INS 테이블 insert 
		setLastModifyTime(jobins);
		int cnt = sqlMapClient.update("nbs.scheduler.insertJobInstance", jobins);
		if (cnt > 0) {
			// NBS_JOB_INS_PREJOB 테이블 insert
			insertPreJobCondition(jobins);

			// NBS_JOB_INS_TRIGGER 테이블 insert
			insertPostJobTrigger(jobins);
			
			// NBS_JOB_INS_OBJ_STORE 테이블 insert
			insertParameter(jobins);
		}
		return cnt;
	}
	
	public void insertPreJobCondition(JobInstance jobins) throws SQLException {
		int i=0;
		sqlMapClient.startBatch();
		for (PreJobCondition cond : jobins.getPreJobConditions()) {
			Map map = new HashMap();
			map.put("jobInstanceId", jobins.getJobInstanceId());
			map.put("seq",           (++i));
			map.put("preJobId",      cond.getPreJobId());
			map.put("okFail",        cond.getOkFail());
			map.put("andOr",         cond.getAndOr());
			sqlMapClient.update("nbs.scheduler.insertJobInsPreJobList", map);
		}
		sqlMapClient.executeBatch();
	}

	public void insertPostJobTrigger(JobInstance jobins) throws SQLException {
		int i=0;
		sqlMapClient.startBatch();
		for (PostJobTrigger trigger : jobins.getTriggerList()) {
			Map map = new HashMap();
			map.put("jobInstanceId", jobins.getJobInstanceId());
			map.put("seq",           (++i));
			map.put("when",          trigger.getWhen());
			map.put("checkValue1",   trigger.getCheckValue1());
			map.put("checkValue2",   trigger.getCheckValue2());
			map.put("checkValue3",   trigger.getCheckValue3());
			map.put("triggerJobId",  trigger.getTriggerJobId());
			map.put("instanceCount", trigger.getJobInstanceCount());
			sqlMapClient.update("nbs.scheduler.insertJobInsPostJobTrigger", map);
		}
		sqlMapClient.executeBatch();
	}

	public void insertParameter(JobInstance jobins) throws SQLException {
		Map map = new HashMap();
		map.put("jobInstanceId", jobins.getJobInstanceId());
		map.put("xml",           XmlUtil.toXml(jobins.getInParameters()));
		sqlMapClient.update("nbs.scheduler.insertJobInsParam", map);
	}

	/**
	 * NBS_JOB_INS 테이블만 UDPATE 함
	 * @param jobins
	 * @throws SQLException
	 */
	public int updateJobInstance(JobInstance jobins) throws SQLException {
		// NBS_JOB_INS 테이블 update 
		setLastModifyTime(jobins);
		JobInstance beforeJobins = getJobInstanceDeep(jobins.getJobInstanceId());

		if (!beforeJobins.isLocked()) {
			throw new SchedulerException("main.jobins.lock.required.for.update"); // 변경전엔 먼저 Lock 되어있어야 합니다
		}

		int cnt = sqlMapClient.update("nbs.scheduler.updateJobInstance", jobins);
		if (cnt > 0) {
			// NBS_JOB_INS 테이블 정상 UPDATE
			
			if (beforeJobins.getPreJobConditions() != null && !beforeJobins.getPreJobConditions().equals(jobins.getPreJobConditions())) {
				// 선행조건 변경함. delete & insert
				deletePreJobCondition(jobins);
				insertPreJobCondition(jobins);
			}
			
			if (beforeJobins.getTriggerList() != null && !beforeJobins.getTriggerList().equals(jobins.getTriggerList())) {
				// trigger 변경함. delete & insert
				deletePostJobTrigger(jobins);
				insertPostJobTrigger(jobins);
			}
			
			if (beforeJobins.getInParameters() != null && !beforeJobins.getInParameters().equals(jobins.getInParameters())) {
				// 파라미터 변경함. delete & insert
				deleteParameter(jobins);
				insertParameter(jobins);
			}
		}
		return cnt;
	}

	/**
	 * NBS_JOB_INS_PREJOB 테이블에서 Instance 하나의 정보를 삭제함
	 * @param jobins
	 * @throws SQLException
	 */
	public int deletePreJobCondition(JobInstance jobins) throws SQLException {
		return sqlMapClient.delete("nbs.scheduler.deleteJobInsPreJobList", jobins.getJobInstanceId());
	}

	/**
	 * NBS_JOB_INS_TRIGGER 테이블에서 Instance 하나의 정보를 삭제함
	 * @param jobins
	 * @throws SQLException
	 */
	public int deletePostJobTrigger(JobInstance jobins) throws SQLException {
		return sqlMapClient.delete("nbs.scheduler.deleteJobInsPostJobTrigger", jobins.getJobInstanceId());
	}
	
	/**
	 * NBS_JOB_INS_PREJOB 테이블에서 Instance 하나의 정보를 삭제함ㄴ
	 * @param jobins
	 * @throws SQLException
	 */
	public int deleteParameter(JobInstance jobins) throws SQLException {
		return sqlMapClient.delete("nbs.scheduler.deleteJobInsParam", jobins.getJobInstanceId());
	}

	/**
	 * Job 이 start 되면서 상태 변경을함.
	 * @param jobInstanceId
	 * @param startTime
	 * @param reason
	 * @param beforeState
	 * @param parallelGroup
	 * @param pgMax
	 * @param oldLastModifyTime
	 * @return true if updated successfully
	 * @throws SQLException
	 */
	public boolean setJobStateForStart(String jobInstanceId, long startTime, String reason, String beforeState, String parallelGroup, int pgMax, String oldLastModifyTime) throws SQLException {
		Map param = new HashMap();
		param.put("jobInstanceId",    jobInstanceId);
		param.put("jobState",         JobInstance.JOB_STATE_RUNNING);
		param.put("beforeJobState",   beforeState);
		param.put("parallelGroup",    parallelGroup); 
		param.put("parallelGroupMax", pgMax); // 이 값을 서브쿼리로 하지 않고 이렇게 파라미터로 받는 이유는 수동 트랜잭션 Lock 을 이용하여 (for update) 이중화 환경에서 동시성 문제를 해결하기 위함이다. 
		param.put("lastStartTime",    startTime == 0 ? null : Util.getYYYYMMDDHHMMSS(startTime));
		param.put("lastModifyTime",   DateUtil.getCurrentTimestampString());
		param.put("oldLastModifyTime",oldLastModifyTime);
		
		int cnt = sqlMapClient.update("nbs.scheduler.updateJobStateForStart", param);
		if (cnt == 1) {
			logJobStateChange(jobInstanceId, beforeState, JobInstance.JOB_STATE_RUNNING, null);
			return true;
		}else {
		    log.warn("[JobInstanceManager] setJobStateForStart fail. param="+param+", update result="+cnt);
		    JobInstance jobins = getJobInstanceWithoutError(jobInstanceId);
		    log.warn("[JobInstanceManager] ID="+jobInstanceId+" JobInstance="+jobins);
			return false;
		}
		
		/* 2012-12-13.
		 * 이중화 환경에서는 parallel max 체크 이후에 R 상태 변경 직전에 peer 에서 동일하게 R 상태 변경이 먼저 일어나는 경우
		 * max 값을 넘어서서 병렬 실행되는 경우가 발생한다.
		 * 따라서 R 상태 변경 쿼리에서 한 트랜잭션으로 max 를 확인하도록 쿼리를 수정한다. 
		 */
	}
	
	/**
	 * Job 이 end 되면서 상태 변경을함.
	 * @param jobInstanceId
	 * @param isEndedOK 정상종료인지, 에러종료인지?
	 * @param reason 사유
	 * @param endTime 종료시각
	 * @param forcedEndOkManually 수동으로 force endedok 된 경우인가? true 이면, lastEndTime 을 변경하지 않는다.
	 * @param beforeState 이전 상태
	 * @param lastJobExeId 최종 JobExeId. callback 으로 인행 End 상태 변경시에는 이 값을 꼭 넣어야한다. 
	 * @return true if updated
	 */
	public boolean setJobStateForEnd(String jobInstanceId, boolean isEndedOK, String reason, long endTime, boolean forcedEndOkManually, String beforeState, String lastJobExeId) throws SQLException {
		String newState = isEndedOK ? JobInstance.JOB_STATE_ENDED_OK : JobInstance.JOB_STATE_ENDED_FAIL;
		Map param = new HashMap();
		param.put("jobInstanceId",    jobInstanceId);
		param.put("jobState",         newState);
		if (!Util.isBlank(beforeState)) {
			param.put("beforeJobState",   beforeState);
		}
		if (!Util.isBlank(lastJobExeId)) {
			param.put("lastJobExeId",   lastJobExeId);
		}
			
		param.put("jobStateReason",   Util.fitLength(reason, 200));  // 정상이던 에러이던 간에 모두 사유를 기록한다.
		if (!forcedEndOkManually) {
			// 수동으로 force endedok 처리된 경우는 실제 실행 시각을 남겨두기 위해 lastEndTime 을 update 하지 않는다.
			// force endedok를 이 메소드를 사용하는 이유는 END_OK_COUNT 를 increase 하기 위함.
			// [수정. 2012-12-07] force endok시에는 END_OK_COUNT를 increate 하지 않는다. 쿼리에서 dynamic 처리함.
			param.put("lastEndTime",  endTime==0 ? null : Util.getYYYYMMDDHHMMSS(endTime));
		}
		param.put("lastModifyTime",   DateUtil.getCurrentTimestampString());

		if (sqlMapClient.update("nbs.scheduler.updateJobStateForEnd", param) == 1) {
			logJobStateChange(jobInstanceId, beforeState, newState, reason);
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * LAST_JOB_EXE_ID 를 업데이트함
	 * @param jobInstanceId
	 * @param lastJobExeId
	 * @param lastAgentNode 이중화 환경을 고려한 에이전트ID.
	 * @return true if updated successfully
	 * @throws SQLException
	 * 2016.6.2 에이전트 이중화 환경에서 실제 수행된 에이전트 ID 값을 NBS_JOB_INS 테이블에 LAST_AGENT_NODE 컬럼에 넣어준다. (상태 모니터링용)
	 */
	public boolean setLastJobExeId(String jobInstanceId, String lastJobExeId, String lastAgentNode) throws SQLException {
		Map param = new HashMap();
		param.put("jobInstanceId",  jobInstanceId);
		param.put("lastJobExeId",   lastJobExeId);
		param.put("lastAgentNode",  lastAgentNode);
		param.put("lastModifyTime", DateUtil.getCurrentTimestampString());
		
		if (sqlMapClient.update("nbs.scheduler.updateLastJobExeId", param) == 1) {
			return true;
		}else {
			return false;
		}
	}

	/**
	 * Job State 바꿈. 상태와 사유를 변경.
	 * @param jobInstanceId
	 * @param newState
	 * @param newStateReason
	 * @return true if updated
	 */
	public boolean setJobState(String jobInstanceId, String newState, String newStateReason) throws SQLException {
		// 상태와 Reason 이 변경된 경우에만 DB Update 를 한다. 변경없는 경우에 괜히 DB Update 할 필요없다. 
		Map param = new HashMap();
		param.put("jobInstanceId",    jobInstanceId);
		param.put("jobState",         newState);
		param.put("jobStateReason",   Util.fitLength(newStateReason, 200));
		param.put("lastModifyTime",   DateUtil.getCurrentTimestampString());

		if (sqlMapClient.update("nbs.scheduler.updateJobState", param) == 1) {
			logJobStateChange(jobInstanceId, null, newState, Util.fitLength(newStateReason, 200));
			return true;
		}else {
			return false;
		}
	}

	
	/**
	 * Job State 바꿈. 상태와 사유를 변경. 이전상태도 체크함. 
	 * @param jobInstanceId
	 * @param beforeState 이전 상태
	 * @param newState
	 * @param newStateReason
	 * @return true if updated
	 */
	public boolean setJobStateWithCheck(String jobInstanceId, String beforeState, String newState, String newStateReason) throws SQLException {
		// 상태와 Reason 이 변경된 경우에만 DB Update 를 한다. 변경없는 경우에 괜히 DB Update 할 필요없다. 
		Map param = new HashMap();
		param.put("jobInstanceId",    jobInstanceId);
		param.put("beforeJobState",   beforeState);
		param.put("jobState",         newState);
		param.put("jobStateReason",   Util.fitLength(newStateReason, 200));
		param.put("lastModifyTime",   DateUtil.getCurrentTimestampString());

		if (sqlMapClient.update("nbs.scheduler.updateJobStateWithCheck", param) == 1) {
			logJobStateChange(jobInstanceId, beforeState, newState, Util.fitLength(newStateReason,200));
			return true;
		}else {
			return false;
		}
	}

	/**
	 * Job State바꿈. 상태(jobState)와 사유(jobStateReason)를 변경.
	 * @param jobIns
	 * @return
	 */
	public boolean setJobState(JobInstance jobins) throws SQLException {
		return setJobState(jobins.getJobInstanceId(), jobins.getJobState(), jobins.getJobStateReason());
	}

	/**
	 * Job State바꿈. 상태와 사유를 변경. 이전상태도 체크함.
	 * @param jobIns
	 * @param beforeState
	 * @param newState
	 * @param newStateReason
	 * @return
	 */
	public boolean setJobStateWithCheck(JobInstance jobins, String beforeState) throws SQLException {
		return setJobStateWithCheck(jobins.getJobInstanceId(), beforeState, jobins.getJobState(), jobins.getJobStateReason());
	}

	/**
	 * 최종 변경시각을 수정함. 하루 이상 돌아가능 job 의 경우 LastModifyTime 을 변경시켜서 모니터링의 범위를 벗어나지 못하도록 끌어옴
	 * @param jobInstanceId
	 * @param lastModifyTime
	 * @return
	 * @throws SQLException
	 */
	public boolean updateJobLastModifyTime(String jobInstanceId, long lastModifyTime) throws SQLException {
		Map param = new HashMap();
		param.put("jobInstanceId",    jobInstanceId);
		param.put("lastModifyTime",   DateUtil.getTimestampString(lastModifyTime));
		return sqlMapClient.update("nbs.scheduler.updateJobLastModifyTime", param) == 1;
	}
	
	/**
	 * 로그 레벨 변경
	 * @param jobInstanceId
	 * @param logLevel
	 * @return
	 * @throws SQLException
	 */
	public int updateJobInstanceLogLevel(String jobInstanceId, String logLevel) throws SQLException {
		Map param = new HashMap();
		param.put("logLevel",       logLevel==null ? null : logLevel.toUpperCase());
		param.put("jobInstanceId",  jobInstanceId);
		return sqlMapClient.update("nbs.scheduler.updateJobInstanceLogLevel", param);
	}
	

	/**
	 * Job Instance 를 Lock 함.
	 * @param jobInstanceId
	 * @param operatorId
	 * @return
	 */
	public boolean lockJob(String jobInstanceId, String operatorId, String operatorIp) throws SQLException {
		JobInstance jobins = (JobInstance)getJobInstance(jobInstanceId);
		if (jobins != null && !jobins.isLocked()) {
			// 메모리 update
			jobins.setLockedBy(Util.getCurrentYYYYMMDDHHMMSS()+"_"+operatorId+"_"+operatorIp);
			setLastModifyTime(jobins);
			// DB update
			int resultCnt = sqlMapClient.update("nbs.scheduler.updateJobInstanceLockedBy", jobins);
			if (resultCnt > 0) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Job Instance 를 Lock 함.
	 * @param jobInstanceId
	 * @param operator
	 * @return
	 */
	public boolean unlockJob(String jobInstanceId, String operator) throws SQLException {
		JobInstance jobins = (JobInstance)getJobInstance(jobInstanceId);
		if (jobins != null && jobins.isLocked()) {
			// 메모리 update
			jobins.setLockedBy(null);
			setLastModifyTime(jobins);
			// DB update
			int resultCnt = sqlMapClient.update("nbs.scheduler.updateJobInstanceLockedBy", jobins);
			if (resultCnt > 0) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Job Instance 를 confirm 함.
	 * @param jobInstanceId
	 * @param operatorId
	 * @param operatorIp
	 * @return
	 */
	public boolean setConfirmed(String jobInstanceId, String operatorId, String operatorIp) throws SQLException {
		JobInstance jobins = (JobInstance)getJobInstance(jobInstanceId);
		if (jobins != null) {
			// 메모리 update
			jobins.setConfirmed(Util.getCurrentYYYYMMDDHHMMSS()+"_"+operatorId+"_"+operatorIp);
			setLastModifyTime(jobins);
			// DB update
			int resultCnt = sqlMapClient.update("nbs.scheduler.updateJobInstanceConfirmed", jobins);
			if (resultCnt > 0) {
				return true;
			}
		}
		
		return false;
	}

    private void logJobStateChange(String jobInstanceId, String oldState, String newState, String newStateReason) {
    	StringBuilder sb = new StringBuilder(120);
    	sb.append("[JSC] [");
    	sb.append(Thread.currentThread().getName()); // 2013.09.05. 상태 변경 쓰레드 정보도 표시
    	sb.append("] [");
    	sb.append(jobInstanceId);
    	sb.append("] ");
    	sb.append(Util.nvl(oldState, "*"));
    	sb.append(" > ");
    	sb.append(newState);
    	sb.append(" (");
    	sb.append(Util.nvl(newStateReason));
    	sb.append(")");
    	Util.logInfo(log, sb.toString());
    }
    
	/**
	 * 해당 Job 의 그룹 ID 를 리턴함.
	 * @param jobInsId
	 * @return
	 * @throws SQLException
	 */
	public String getJobGroupId(String jobInsId) throws SQLException {
		return (String)sqlMapClient.queryForObject("nbs.scheduler.getJobInstanceGroupId", jobInsId);
	}

	/**
	 * 에이전트 ID 만 변경함.
	 * 에이전트 장애시 다른 에이전트로 넘길때 활용됨.
	 * Running, Suspended 가 아닐때만 가능함. (쿼리에서 체크)
	 * @param jobInstanceId
	 * @param newAgentId
	 * @return true if success, false others
	 * @throws SQLException
	 * @since 3.6.3
	 */
	public boolean updateAgentId(String jobInstanceId, String newAgentId) throws SQLException {
		Map param = new HashMap();
		param.put("jobInstanceId",    jobInstanceId);
		param.put("newAgentId",       newAgentId);
		param.put("lastModifyTime",   DateUtil.getCurrentTimestampString());
		
		if (sqlMapClient.update("nbs.scheduler.updateJobInstanceAgentId", param) == 1) {
			return true;
		}else {
			return false;
		}
	}
}
