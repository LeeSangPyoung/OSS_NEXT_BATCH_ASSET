package nexcore.scheduler.controller.admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.AgentInfo;
import nexcore.scheduler.entity.AgentMonitoringSummary;
import nexcore.scheduler.entity.IControllerService;
import nexcore.scheduler.entity.IMonitorService;
import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobDefinitionStg;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobGroup;
import nexcore.scheduler.entity.JobGroupAttrDef;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.JobLogFileLocation;
import nexcore.scheduler.entity.JobNotify;
import nexcore.scheduler.entity.JobNotifyReceiver;
import nexcore.scheduler.entity.JobNotifySendInfo;
import nexcore.scheduler.entity.ParallelGroup;
import nexcore.scheduler.entity.User;
import nexcore.scheduler.entity.ViewFilter;
import nexcore.scheduler.util.ByteArray;

/**
 *  
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : ControllerAdmin, ControllerAdminLocal 의 super 클래스.</li>
 * <li>작성일 : 2010. 8. 30.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ControllerAdmin00 {
	protected IControllerService    controllerService;
	protected IMonitorService       monitorService;
	
	public ControllerAdmin00() {
	}
	
	public boolean isAlive() {
		try {
			return controllerService.isAlive();
		}catch(Throwable e) {
			return false;
		}
	}
	
	//********************************************************************
	//***********************       License      ************************* 
	//********************************************************************
	/**
	 * LicenseManager 의 정보로 위반 사항이 없는지 체크 
	 * @return Map<String, Integer> 객체를 리턴함.
	 */
	public boolean checkLicenseAndCurrentInfo(){
		return controllerService.checkLicenseAndCurrentInfo();
	}
	
	/**
	 * LicenseManager에 저장된 agentCount, maxAgentCount, jobDefinitionCount, maxJobDefinitionCount 추출 
	 * @return Map<String, Integer> 객체를 리턴함.
	 */
	public Map<String, Integer> getLicenseAndCurrentInfo(){
		return controllerService.getLicenseAndCurrentInfo();
	}
	
	
	//********************************************************************
	//***********************    Job Definition  ************************* 
	//********************************************************************

	/**
	 * @return Deep select된 JobDefinition 객체를 리턴함. (선행조건, 파라미터 값들 포함됨)
	 */
	public JobDefinition getJobDefinition(String jobId) {
		return controllerService.getJobDefinition(jobId);
	}

	/**
	 * @return 해당 Job Definition 이 존재하는지 체크
	 */
	public boolean existJobDefinition(String jobId) {
		return controllerService.existJobDefinition(jobId);
	}

	/**
	 * JobDefinition 정보를 query 조건에 따라 검색하여 리턴함 
	 * @param queryCondition NBS_JOB_DEF 테이블을 select 할때 사용된 where 절을 입력함 이후의 조건을 입력함. 
	 *         예) "WHERE JOB_GROUP_ID LIKE '%XYZ%'
	 * @return Deep select된 List of JobDefinition 객체를 리턴함. (선행조건, 파라미터 값들 포함됨)
	 */
	public List<JobDefinition> getJobDefinitionList(String queryCondition, boolean deep) {
		return controllerService.getJobDefinitionList(queryCondition, deep);
	}

	public List<JobDefinition> getJobDefinitionList(String queryCondition) {
		return getJobDefinitionList(queryCondition, true);
	}

	/**
	 * Dynamic 쿼리 방식으로 JobDefinition들 조회
	 * @param queryParamMap 검색 조건을 담고 있는 Map.
	 * {viewFilterId, jobIdLike, jobGroupIdLike, jobDescLike, jobType, preJobIdLike, triggerJobIdLike, agentId, ownerLike, authorizedJobGroupIdViewList} 
	 * @param deep (PreJob List, Param) 포함 여부
	 * @return
	 */
	public List<JobDefinition> getJobDefinitionList(Map queryParamMap, boolean deep) {
		return controllerService.getJobDefinitionList(queryParamMap, deep);
	}
	
	public List<JobDefinition> getJobDefinitionList(Map queryParamMap) {
		return getJobDefinitionList(queryParamMap, true);
	}

	/**
	 * JobDefinition 정보를 JOb Id list 로 검색함 
	 * @return Deep select된 List of JobDefinition 객체를 리턴함. (선행조건, 파라미터 값들 포함됨)
	 */
	public List<JobDefinition> getJobDefinitionListByJobIdList(List jobIdList, boolean deep) {
		return controllerService.getJobDefinitionListByJobIdList(jobIdList, deep);
	}

	/**
	 * JobDefinition 정보를 JOb Id list 로 검색함 
	 * @return Deep select된 List of JobDefinition 객체를 리턴함. (선행조건, 파라미터 값들 포함됨)
	 */
	public List<JobDefinition> getJobDefinitionListByJobIdList(List jobIdList) {
		return getJobDefinitionListByJobIdList(jobIdList, true);
	}

	/**
	 * JobDefinition 정보중 원하는 컬럼만 조회한다.
	 * queryParamMap 에 "columnList" 키값으로 컬럼 리스트 지정함
	 * @param queryParamMap 
	 * @return
	 */
	public List<Map> getJobDefinitionListFreeColumn(Map queryParamMap) {
		return controllerService.getJobDefinitionListFreeColumn(queryParamMap);
	}
	
	/**
	 * 해당일이 activate 되도록 schedule 된 일인지 체크함
	 * @param jobdef
	 * @param yyyymmdd 체크하려는 일자
	 * @return
	 */
	public boolean isScheduledDay(JobDefinition jobdef, String yyyymmdd) {
		return controllerService.isScheduledDay(jobdef, yyyymmdd);
	}
	
	//********************************************************************
	//***********************    Job Instance    ************************* 
	//********************************************************************

	public JobInstance getJobInstance(String jobInstanceId) {
		return controllerService.getJobInstance(jobInstanceId);
	}
	
	public JobInstance getJobInstanceSimple(String jobInstanceId) {
		return controllerService.getJobInstanceSimple(jobInstanceId);
	}

	/**
	 * JobInstance 정보를 query 조건에 따라 검색하여 리턴함 
	 * @param queryCondition NBS_JOB_INS 테이블을 select 할때 사용된 where 절을 입력함 이후의 조건을 입력함. 
	 *         예) "WHERE JOB_GROUP_ID LIKE '%XYZ%'
	 * @param orderBy "ORDER BY 포함 "
	 * @return Deep select 된 List of JobInstance  객체를 리턴함. (선행조건, 파라미터 값들 포함됨)
	 */
	public List<JobInstance> getJobInstanceList(String queryCondition, String orderBy, boolean deep) {
		return controllerService.getJobInstanceList(queryCondition, orderBy, deep);
	}

	public List<JobInstance> getJobInstanceList(String queryCondition, String orderBy) {
		return getJobInstanceList(queryCondition, orderBy, true);
	}
	
	public List<JobInstance> getJobInstanceList(Map queryParamMap, boolean deep) {
		return controllerService.getJobInstanceList(queryParamMap, deep);
	}
	
	public List<JobInstance> getJobInstanceList(Map queryParamMap, boolean deep, int skip, int maxResult) {
		return controllerService.getJobInstanceList(queryParamMap, deep, skip, maxResult);
	}

	public List<JobInstance> getJobInstanceList(Map queryParamMap) {
		return getJobInstanceList(queryParamMap, true);
	}
	
	/**
	 * iBATIS 의 RowHandler 를 이용하여 대랑 JobInstance 조회를 함.
	 * 대량 조회는 기본적으로 no-deep 쿼리한다.
	 * @param queryParamMap
	 * @param rowHandler
	 */
	public void getJobInstanceListWithRowHandler(Map queryParamMap, Object rowHandler) {
		controllerService.getJobInstanceListWithRowHandler(queryParamMap, rowHandler);
	}

	/**
	 * 필요한 컬럼만 조회한다.
	 * iBATIS 의 RowHandler 를 이용하여 대랑 JobInstance 조회를 함.
	 * 대량 조회는 기본적으로 no-deep 쿼리한다.
	 * @param queryParamMap
	 * @param rowHandler
	 */
	public void getJobInstanceListFreeColumnWithRowHandler(Map queryParamMap, Object rowHandler) {
		controllerService.getJobInstanceListFreeColumnWithRowHandler(queryParamMap, rowHandler);
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
	public List<Map> getJobInstanceListFreeColumn(Map queryParamMap) {
		return controllerService.getJobInstanceListFreeColumn(queryParamMap);
	}


	public int getJobInstanceCount(Map queryParamMap) {
		return controllerService.getJobInstanceCount(queryParamMap);
	}

	//********************************************************************
	//***********************    Job Execution   ************************* 
	//********************************************************************

	/**
	 * JobExecution 정보를 Job Instance Id 에 따라 검색함 
	 * @param jobInstanceId  
	 * @return non-deep select된 List of JobExecution 객체를 리턴함. (파라미터, 리턴값 포함됨)
	 */
	public List<JobExecution> getJobExecutionListByJobInstanceId(String jobInstanceId, boolean deep) {
		return controllerService.getJobExecutionListByJobInstanceId(jobInstanceId, deep);
	}
	
	public List<JobExecution> getJobExecutionListByJobInstanceId(String jobInstanceId) {
		return getJobExecutionListByJobInstanceId(jobInstanceId, true);
	}

	/**
	 * JobExecution 정보를 조회 
	 * @return Deep select된 JobExecution.
	 */
	public JobExecution getJobExecution(String jobExecutionId) {
		return controllerService.getJobExecutionDeep(jobExecutionId);
	}

	/**
	 * Agent 에 있는 로그 파일 이름, 위치, 크기 조회
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getJobLogFileLocation(String jobInstanceId) {
		return controllerService.getJobLogFileLocation(jobInstanceId);
	}
	
	/**
	 * Agent 에 있는 SUB 로그 파일 이름, 위치, 크기 조회
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getSubJobLogFileLocation(String jobInstanceId) {
		return controllerService.getSubJobLogFileLocation(jobInstanceId);
	}

	/**
	 * Agent 에 있는 stoud 로그 파일 이름, 위치, 크기 조회
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getStdoutJobLogFileLocation(String jobInstanceId) {
		return controllerService.getStdoutJobLogFileLocation(jobInstanceId);
	}
	
	/**
	 * Agent 에 있는 로그파일 내용 읽어 리턴
	 * @param jobInstanceId
	 * @param location
	 * @param offset
	 * @param length
	 * @return
	 */
	public ByteArray readLogFile(String jobInstanceId, JobLogFileLocation location, int offset, int length) {
		return controllerService.readLogFile(jobInstanceId, location, offset, length);
	}
	
	public long[] getJobProgress(String jobExecutionId) {
		return monitorService.getJobProgress(jobExecutionId);
	}

	/**
	 * 현재 running 상태인 모든 JobExecution 리스트 조회.
	 * @return List of all running JobExecutions.
	 */
	public Collection<JobExecution> getRunningJobExecutions() {
		return controllerService.getRunningJobExecutions();
	}
	
	//********************************************************************
	//***********************    Job Control     ************************* 
	//********************************************************************

	/**
	 * 수동으로 Job 하나를 activate 함.
	 * @return Job Instance Id
	 */
	public String activateJob(String jobId, String procDate, AdminAuth auth) {
		return controllerService.activateJob(jobId, procDate, auth);
	}
	
	/**
	 * 수동으로 Job 하나를 activateAndLock 함.
	 * @return Job Instance Id
	 */
	public String activateAndLockJob(String jobId, String procDate, AdminAuth auth) {
		return controllerService.activateAndLockJob(jobId, procDate, auth);
	}
	
	/**
	 * Job Instance 를 LOCK 함. LOCK 상태에서는 STATE 변경이 안됨.
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public boolean lockJob(String jobInstanceId, AdminAuth auth) {
		return controllerService.lockJob(jobInstanceId, auth);
	}

	/**
	 * Job Instance 를 UNLOCK 함. LOCK 상태에서는 STATE 변경이 안됨.
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public boolean unlockJob(String jobInstanceId, AdminAuth auth) {
		return controllerService.unlockJob(jobInstanceId, auth);
	}
	
	/**
	 * 상태에 상관없이 (이미 RUNNING 상태인것, SUSPENDED 상태인것을 제회), 조건에 상관없이 해당 Job 을 즉시 실행시킴. 
	 * 이것을 실행시키면 trigger 도 돌아가고, 후행 Job 에도 영향을 미친다.
	 * @param jobInstanceId
	 * @param auth
	 * @return JobExecutionId or null if fail
	 */
	public String forceRunJob(String jobInstanceId, AdminAuth auth) {
		return controllerService.forceRunJob(jobInstanceId, auth);
	}
	
	/**
	 * END 상태의 JOB 을 다시 WAIT 상태로 변경함.
	 * @param jobInstanceId
	 * @param auth
	 * @return 다시 실행한 Job Execution Id
	 */
	public void reRunJob(String jobInstanceId, AdminAuth auth) {
		controllerService.reRunJob(jobInstanceId, auth);
	}
	
	/**
	 * RUNNING, SUSPEND 상태의 Job을 kill함.
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void stopJob(String jobInstanceId, AdminAuth auth) {
		controllerService.stopJob(jobInstanceId, auth);
	}

	/**
	 * RUNNING 상태의 Job을 일시정지시킴
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void suspendJob(String jobInstanceId, AdminAuth auth) {
		controllerService.suspendJob(jobInstanceId, auth);
	}
	
	/**
	 * SUSPEND 상태의 Job을 resume 시킴
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void resumeJob(String jobInstanceId, AdminAuth auth) {
		controllerService.resumeJob(jobInstanceId, auth);
	}
	
	/**
	 * ENDED_FAIL 인 Job을 ENDED_OK고 강제 상태 변경후, 후행 Job 들이 동작하도록 함
	 * 후행 Job, Trigger 를 기동시킴. 
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void forceEndOk(String jobInstanceId, AdminAuth auth) {
		controllerService.forceEndOk(jobInstanceId, auth);
	}
	
	/**
	 * 강제로 Ghost 상태로 전환한다.
	 * 에이전트가 다운되어 Running 상태에서 빠져나오지 못할때 
	 * 강제로 Ghost 상태로 전환한 후에 다른 에이전트에서 재실행 하기 위한 조치
	 * 
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 * @since 3.6.3
	 */
	public void forceChangeToGhost(String jobInstanceId, AdminAuth auth) {
		controllerService.forceChangeToGhost(jobInstanceId, auth);
	}
	
	/**
	 * Job Instance 정보 변경.
	 * 변경가능 항목 : 
	 *       TIME_FROM, TIME_UNTIL, REPEAT_YN, REPEAT_INTVAL, REPEAT_INTVAL_GB, REPEAT_IF_ERROR, 
	 * 	     CONFIRM_NEED_YN, PARALLEL_GROUP, AGENT_NODE, TRIGGER_JOB_ID, JOB_DESC,
	 *       [PRO_JOB_CONDITIONS], [PARAMETERS]
	 * @param jobInstance
	 * @param auth
	 * @return 변경된 JobInstance
	 */
	public JobInstance modifyJobInstance(JobInstance jobInstance, AdminAuth auth) {
		return controllerService.modifyJobInstance(jobInstance, auth);
	}
	
	/**
	 * Job Instance 파라미터 변경.
	 * @param jobInstanceId
	 * @param newParams 새 파라미터 Map
	 * @param auth
	 * @return 변경된 JobInstance
	 */
	public JobInstance modifyJobInstanceParameters(String jobInstanceId, Map newParams, AdminAuth auth) {
		return controllerService.modifyJobInstanceParameters(jobInstanceId, newParams, auth);
	}

	/**
	 * Job 인스턴스의 Agent Id 를 변경함.
	 * Running, suspended 일때는 안됨.
	 * @param jobInstanceId
	 * @param newAgentId
	 * @param auth
	 * @return true if success, false fail
	 * @since 3.6.3
	 */
	public boolean modifyJobInstanceAgentId(String jobInstanceId, String newAgentId, AdminAuth auth) {
		return controllerService.modifyJobInstanceAgentId(jobInstanceId, newAgentId, auth);
	}
		
	/**
	 * Job Definition 추가.
	 * @return insert count
	 */
	public int addJobDefinition(JobDefinition jobdef, AdminAuth auth) {
		return controllerService.addJobDefinition(jobdef, auth);
	}
	
	/**
	 * 변경
	 * @param jobdef
	 * @param auth
	 * @return 변경된 JobDefinition 
	 */
	public JobDefinition modifyJobDefinition(JobDefinition jobdef, AdminAuth auth) {
		return controllerService.modifyJobDefinition(jobdef, auth);
	}
	
	/**
	 * 삭제
	 * @param jobDefId
	 * @param auth
	 * @return 성공
	 */
	public boolean deleteJobDefinition(String jobDefId, AdminAuth auth) {
		return controllerService.deleteJobDefinition(jobDefId, auth);
	}

	/**
	 * 등록 요청 취소.
	 * @param reqNo
	 * @param auth
	 * @return 성공
	 */
	public boolean deleteJobDefinitionStg(String reqNo, AdminAuth auth) {
		return controllerService.deleteJobDefinitionStg(reqNo, auth);
	}

	/**
	 * 컨트롤러 셧다운
	 * @param auth
	 */
	public void shutdown(String id, String password, String ip) {
		controllerService.shutdown(id, password, ip);
	}
	
	/**
	 * Confirm 
	 * @param jobInsIs
	 * @param auth
	 */
	public void confirm(String jobInsIs, AdminAuth auth) {
		controllerService.confirmJob(jobInsIs, auth);
	}

	/**
	 * Calendar 리스트함.
	 * @return
	 */
	public Map getCalendarList() {
		return controllerService.getCalendarList();
	}

	/** 
	 * Calendar의 일자 조회
	 * @param calendarId
	 * @return
	 */
	public List getCalendarDayList(String calendarId) {
		return controllerService.getCalendarDayList(calendarId);
	}

	/** 
	 * Calendar 정보를 파일,DB 로부터 다시 메모리로 로드함
	 * @return
	 */
	public void reloadCalendar() {
		controllerService.reloadCalendar();
	}


	/**
	 * 해당 일자에 Activate 될 Job Id 목록을 리트스함.
	 * @param date
	 * @return list of Job ID 
	 */
	public List getJobListWillBeActivated(String yyyymmdd) {
		return controllerService.getJobListWillBeActivated(yyyymmdd);
	}
	
	/**
	 * 특정 Job 의 해당월에 Activate 될 리스트 리턴
	 * @return list of yyyymmdd 
	 */
	public List getDayListWillBeActivated(String jobId, String yyyymm) {
		return controllerService.getDayListWillBeActivated(jobId, yyyymm);
	}
	
	/**
	 * 특정 Job 의 해당월에 Activate 될 리스트 리턴
	 * @return list of yyyymmdd 
	 */
	public List getDayListWillBeActivated(JobDefinition jobdef, String yyyymm) {
		return controllerService.getDayListWillBeActivated(jobdef, yyyymm);
	}

	/**
	 * 특정월의 기준일 계산 결과.
	 * @param jobId
	 * @param yyyymm
	 * @return
	 */
	public Map getBaseDateMonthlyMap(String jobId, String yyyymm) {
		return controllerService.getBaseDateMonthlyMap(jobId, yyyymm);
	}

	/**
	 * 특정월의 기준일 계산 결과. 아직 등록되지 않은 JobDef를 시뮬레이션.
	 * @param jobdef
	 * @param yyyymm
	 * @return
	 */
	public Map getBaseDateMonthlyMap(JobDefinition jobdef, String yyyymm) {
		return controllerService.getBaseDateMonthlyMap(jobdef, yyyymm);
	}

	//**********************************************************************
	//****  Agent Monitoring
	//**********************************************************************
	

	public List<AgentInfo> getAllAgentInfos() {
		return monitorService.getAllAgentInfos();
	}

	public AgentInfo getAgentInfo(String id) {
		return monitorService.getAgentInfo(id);
	}
	
	public List<String> getAgentIdList() {
		List<String> retval = new ArrayList<String>();
		for (AgentInfo agentInfo : getAllAgentInfos()) {
			retval.add(agentInfo.getId());
		}
		return retval;
	}

	/**
	 * @return list of string "agentid,agentName"
	 */
	public List<String> getAgentIdNameList() {
		List<String> retval = new ArrayList<String>();
		for (AgentInfo agentInfo : getAllAgentInfos()) {
			retval.add(agentInfo.getId()+","+agentInfo.getName());
		}
		return retval;
	}

	/**
	 * agent 리스트 들의 상태를 체크해서 리턴함.
	 * @return Map&lt;String:(agentId), String:(checkResult)&gt;. checkResult="OK"이면 정상 
	 */
	public Map getAgentCheckList() {
		return monitorService.getAgentCheckList();
	}
	
	/**
	 * 에이전트의 상태 체크. AgentMonitor 개 2초마다 폴링하여 캐쉬한 정보를 리턴함
	 * @param agentId
	 * @return "OK" if normal, other message if abnormal
	 */
	public String getAgentCheck(String agentId) {
		return monitorService.getAgentCheck(agentId);
	}

	/**
	 * 에이전트의 상태 체크. 캐쉬정보를 이용하지 않고 즉시 체크한다.
	 * @param agentId
	 * @return "OK" if normal, other message if abnormal
	 */
	public String getAgentCheckNoCache(String agentId) {
		return monitorService.getAgentCheckNoCache(agentId);
	}
	
	public boolean addAgentInfo(AgentInfo agentInfo, AdminAuth auth) {
		return monitorService.addAgentInfo(agentInfo, auth);		
	}

	public boolean removeAgentInfo(String agentId, AdminAuth auth) {
		return monitorService.removeAgentInfo(agentId, auth);
	}
	
	public boolean modifyAgentInfo(AgentInfo agentInfo, AdminAuth auth) {
		return monitorService.modifyAgentInfo(agentInfo, auth);
	}

	public AgentMonitoringSummary getAgentMonitoringSummary(String agentId) {
		return monitorService.getAgentMonitoringSummary(agentId);
	}
	
	public boolean closeOrOpenAgent(String agentId, boolean close, AdminAuth auth) {
		return monitorService.closeOrOpenAgent(agentId, close, auth);
	}
	
	public List<JobExecution> getAgentRunningJobExecutions(String agentId) {
		return monitorService.getAgentRunningJobExecutions(agentId);
	}
	
	public Map getAgentAllThreadStackTrace(String agentId) {
		return monitorService.getAgentAllThreadStackTrace(agentId);
	}

	public Properties getAgentSystemProperties(String agentId) {
		return monitorService.getAgentSystemProperties(agentId);
	}

	public Map getAgentSystemEnv(String agentId) {
		return monitorService.getAgentSystemEnv(agentId);
	}

	public List<String> getAgentConfigFiles(String agentId) {
		return monitorService.getAgentConfigFiles(agentId);
	}

	/**
	 * Agent 에 있는 설정파일 내용 읽어 리턴
	 * @param agentId
	 * @param abs filename
	 * @param offset
	 * @param length
	 * @return
	 */
	public ByteArray readAgentFile(String agentId, String filename, int offset, int length) {
		return controllerService.readAgentFile(agentId, filename, offset, length);
	}

	/**
	 * JobExecution 의 스레드 dump
	 * @param jobExecutionId
	 * @return
	 */
	public Map getJobExecutionThreadStackTrace(String jobInstanceId) {
		return controllerService.getJobExecutionThreadStackTrace(jobInstanceId);
	}

	//**********************************************************************
	//****  JobDefinition
	//**********************************************************************
	
	/**
	 * JobDefinition 변경/신규/삭제를 위한 요청 등록.
	 * @param jobdef
	 * @param auth
	 * @return 요청번호
	 */
	public String addJobDefinitionStg(JobDefinitionStg jobdef, AdminAuth auth) {
		return controllerService.addJobDefinitionStg(jobdef, auth);
	}
	
	/**
	 * JobDefinition 변경/신규/삭제 요청 조회
	 */
	public JobDefinitionStg getJobDefinitionStg(String reqNo, String jobId) {
		return controllerService.getJobDefinitionStg(reqNo, jobId);
	}
	
	/**
	 * JobDefinition 변경/신규/삭제 요청 목록 조회
	 */
	public List<JobDefinitionStg> getJobDefinitionStgList(String queryCondition, boolean deep) {
		return controllerService.getJobDefinitionStgList(queryCondition, deep);
	}

	public List<JobDefinitionStg> getJobDefinitionStgList(String queryCondition) {
		return getJobDefinitionStgList(queryCondition, true);
	}

	
	public void getJobDefinitionStgListWithRH(String queryCondition, Object rowHandler) throws SQLException {
		controllerService.getJobDefinitionStgListWithRH(queryCondition, rowHandler);
	}

	/**
	 * JobDefinition 변경/신규/삭제 요청 승인
	 */
	public void approveJobDefinitionStgToJobDefinition(String reqNo, String jobId, String reqARReason, AdminAuth auth) {
		controllerService.approveJobDefinitionStgToJobDefinition(reqNo, jobId, reqARReason, auth);
	}

	/**
	 * JobDefinition 변경/신규/삭제 요청 반려
	 */
	public void rejectJobDefinitionStgToJobDefinition(String reqNo, String jobId, String reqARReason, AdminAuth auth) {
		controllerService.rejectJobDefinitionStgToJobDefinition(reqNo, jobId, reqARReason, auth);
	}

	/**
	 * Job 등록 정보 validation
	 * @param jobdef
	 * @return list of not-valid error messages
	 */
	public List validateJobDefinition(JobDefinition jobdef) {
		return controllerService.validateJobDefinition(jobdef);
	}
	
	/**
	 * Job Instance 정보 validation
	 * @param jobins
	 * @return list of not-valid error messages
	 */
	public List validateJobInstance(JobInstance jobins) {
		return controllerService.validateJobInstance(jobins);
	}
	
	/**
	 * 이 프로젝트에서 사용하는 Job 타입 목록 출력
	 * @param jobins
	 * @return list of jobtype string
	 */
	public List<String> getJobTypeUsingList() {
		return controllerService.getJobTypeUsingList();
	}

	/**
	 * 이 프로젝트에서 사용하는 로그 레벨 목록 출력
	 * @return
	 */
	public List<String> getLogLevelUsingList() {
		return controllerService.getLogLevelUsingList();
	}
	
	public boolean changeJobDefinitionLogLevel(String jobId, String loglevel, AdminAuth auth) {
		return controllerService.changeJobDefinitionLogLevel(jobId, loglevel, auth);
	}

	public boolean changeJobInstanceLogLevel(String jobInstanceId, String loglevel, AdminAuth auth) {
		return controllerService.changeJobInstanceLogLevel(jobInstanceId, loglevel, auth);
	}

	/**
	 * EXACT 방식의 반복일 경우 정규표현식을 해석하여 실행 시각을 예측함
	 * From, Until 도 고려하여 계산함
	 * @param jobdef
	 * @return
	 */
	public List<String> getTimePlanForExactRepeat(JobDefinition jobdef) {
		return controllerService.getTimePlanForExactRepeat(jobdef);
	}

	/**
	 * EXACT 방식의 반복일 경우 정규표현식을 해석하여 실행 시각을 예측함
	 * From, Until 도 고려하여 계산함
	 * @param jobins
	 * @return
	 */
	public List<String> getTimePlanForExactRepeat(JobInstance jobins) {
		return controllerService.getTimePlanForExactRepeat(jobins);
	}
	
	//**********************************************************************
	//****  Global Parameter
	//**********************************************************************
	
	public void addGlobalParameter(String paramName, String paramValue, AdminAuth auth) {
		controllerService.addGlobalParameter(paramName, paramValue, auth);
	}

	public void modifyGlobalParameter(String paramName, String paramValue, AdminAuth auth) {
		controllerService.modifyGlobalParameter(paramName, paramValue, auth);
	}

	public void deleteGlobalParameter(String paramName, AdminAuth auth) {
		controllerService.deleteGlobalParameter(paramName, auth);
	}

	public Map getGlobalParameters() {
		return controllerService.getGlobalParameters();
	}
	
	/**
	 * Global 파라미터를 DB에서 다시 읽음
	 */
	public void reloadGlobalParameters(AdminAuth auth) {
		controllerService.reloadGlobalParameters(auth);
	}
	
	/**
	 * 표현식으로 되어있는 파라미터 값을 상수 값으로 evaluate 함.
	 * Job 등록시 등록 버튼 누르기 전에 미리 검토해볼 수 있게 하기 위한 기능. 
	 * @param agentId 
	 * @param paramMap Param Name, Param Value Map
	 * @param baseDateCalId basedate calendar id
	 * @param baseDateLogic basedate logic
	 */
	public Map evaluateParameters(String agentId, JobDefinition jobdef, Map paramMap, String baseDateCalId, String baseDateLogic) {
		return controllerService.evaluateParameters(agentId, jobdef, paramMap, baseDateCalId, baseDateLogic);
	}
	
	//**********************************************************************
	//****  Parallel Group 
	//**********************************************************************
	public void addParallelGroup(ParallelGroup pg, AdminAuth auth) {
		controllerService.addParallelGroup(pg, auth);
	}

	public void modifyParallelGroup(ParallelGroup pg, AdminAuth auth) {
		controllerService.modifyParallelGroup(pg, auth);
	}

	public void deleteParallelGroup(String groupName, AdminAuth auth) {
		controllerService.deleteParallelGroup(groupName, auth);
	}

	public List<ParallelGroup> getAllParallelGroups() {
		return controllerService.getAllParallelGroups();
	}
	//**********************************************************************
	//****  Parallel Group 
	//**********************************************************************
	

	//**********************************************************************
	//****  View Filter
	//**********************************************************************
	public boolean addViewFilter(ViewFilter vf, AdminAuth auth) {
		return monitorService.addViewFilter(vf, auth);
	}
	
	public ViewFilter getViewFilter(int vfid) {
		return monitorService.getViewFilter(vfid);
	}
	
	public ViewFilter getViewFilterDeep(int vfid) {
		return monitorService.getViewFilterDeep(vfid);
	}
	
	public List<ViewFilter> getViewFiltersByQuery(String query, String orderBy) {
		return monitorService.getViewFiltersByQuery(query, orderBy);
	}
	
	public List<JobDefinition> getJobDefinitionsByViewFilter(int vfid) {
		return monitorService.getJobDefinitionsByViewFilter(vfid);
	}
	
	public boolean removeViewFilter(int vfId, AdminAuth auth) {
		return monitorService.removeViewFilter(vfId, auth);
	}
	
	public boolean modifyViewFilter(ViewFilter vf, AdminAuth auth) {
		return monitorService.modifyViewFilter(vf, auth);
	}

	public boolean modifyViewFilterNoJobList(ViewFilter vf, AdminAuth auth) {
		return monitorService.modifyViewFilterNoJobList(vf, auth);
	}
	
	public boolean modifyViewFilterAddJobList(int vfid, List<String> jobIdList, AdminAuth auth) {
		return monitorService.modifyViewFilterAddJobList(vfid, jobIdList, auth);
	}
	
	public boolean modifyViewFilterDelJobList(int vfid, List<String> jobIdList, AdminAuth auth) {
		return monitorService.modifyViewFilterDelJobList(vfid, jobIdList, auth);
	}
	
	// =================================== JobNotify =========================================

	public boolean addJobNotify(JobNotify jobNotify, AdminAuth auth) {
		return monitorService.addJobNotify(jobNotify, auth);
	}
	public boolean removeJobNotify(int id, AdminAuth auth) {
		return monitorService.removeJobNotify(id, auth);
	}
	public boolean modifyJobNotify(JobNotify jobNotify, AdminAuth auth) {
		return monitorService.modifyJobNotify(jobNotify, auth);
	}
	public JobNotify getJobNotify(int id) {
		return monitorService.getJobNotify(id);
	}
	public List<JobNotify> getAllJobNotifies() {
		return monitorService.getAllJobNotifies();
	}
	public boolean addJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver, AdminAuth auth) {
		return monitorService.addJobNotifyReceiver(jobNotifyReceiver, auth);
	}
	public boolean removeJobNotifyReceiver(int id, AdminAuth auth) {
		return monitorService.removeJobNotifyReceiver(id, auth);
	}
	public boolean modifyJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver, AdminAuth auth) {
		return monitorService.modifyJobNotifyReceiver(jobNotifyReceiver, auth);
	}
	public JobNotifyReceiver getJobNotifyReceiver(int receiverId) {
		return monitorService.getJobNotifyReceiver(receiverId);
	}
	public List<JobNotifyReceiver> getAllJobNotifyReceivers() {
		return monitorService.getAllJobNotifyReceivers();
	}
	public Map<Integer, JobNotifyReceiver> getAllJobNotifyReceiversMap() {
		return monitorService.getAllJobNotifyReceiversMap();
	}
	public List<JobNotifyReceiver> getJobNotifyReceiversByNotifyId(int notifyId) {
		return monitorService.getJobNotifyReceiversByNotifyId(notifyId);
	}
	public List<JobNotifySendInfo> getJobNotifySendList(Map queryParamMap) {
		return monitorService.getJobNotifySendList(queryParamMap);
	}
	public int getJobNotifySendListCount(Map queryParamMap) {
		return monitorService.getJobNotifySendListCount(queryParamMap);
	}

	/**
	 * 해당 Job ID 에 설저된 통지 수신자 목록을 가져옴
	 * @param jobid
	 * @param event "EO", "EF", null (all)
	 * @return 설정된 수신자 목록
	 * @since 3.6.3-u1
	 */
	public List<JobNotifyReceiver> getJobNotifyReceiverList(String jobid, String event) {
		return monitorService.getJobNotifyReceiverList(jobid, event);
	}
		
	public String getSystemMonitorText() {
		return monitorService.getSystemMonitorText();
	}

	// =============================================================
	
	public boolean addJobGroupAttrDef(JobGroupAttrDef jobGroupAttrDef, AdminAuth auth) {
		return monitorService.addJobGroupAttrDef(jobGroupAttrDef, auth);
	}
	
	public boolean removeJobGroupAttrDef(String id, AdminAuth auth) { 
		return monitorService.removeJobGroupAttrDef( id,  auth);
	}
	
	public boolean modifyJobGroupAttrDef(JobGroupAttrDef jobGroupAttrDef, AdminAuth auth) { 
		return monitorService.modifyJobGroupAttrDef( jobGroupAttrDef,  auth);
	}
	
	public JobGroupAttrDef getJobGroupAttrDef(String jobGroupAttrDefId) { 
		return monitorService.getJobGroupAttrDef( jobGroupAttrDefId);
	}
	
	public List<JobGroupAttrDef> getAllJobGroupAttrDefs() { 
		return monitorService.getAllJobGroupAttrDefs();
	}
	
	public List<JobGroupAttrDef> getJobGroupAttrDefsByQuery(String queryCondition, String orderBy) { 
		return monitorService.getJobGroupAttrDefsByQuery( queryCondition,  orderBy);
	}
	
	// =============================================================
	
	public boolean addJobGroup(JobGroup jobGroup, AdminAuth auth) {
		return monitorService.addJobGroup(jobGroup, auth);
	}
	
	public boolean removeJobGroup(String id, AdminAuth auth) { 
		return monitorService.removeJobGroup( id,  auth);
	}

	public boolean removeJobGroupRecursively(String id, AdminAuth auth) { 
		return monitorService.removeJobGroupRecursively( id,  auth);
	}
	
	public boolean modifyJobGroup(JobGroup jobGroup, AdminAuth auth) { 
		return monitorService.modifyJobGroup( jobGroup,  auth);
	}
	
	public JobGroup getJobGroup(String jobGroupId) { 
		return monitorService.getJobGroup( jobGroupId);
	}
	
	public List<JobGroup> getAllJobGroups() { 
		return monitorService.getAllJobGroups();
	}
	
	public List<JobGroup> getJobGroupsByQuery(String queryCondition, String orderBy) { 
		return monitorService.getJobGroupsByQuery( queryCondition,  orderBy);
	}

	public List<JobGroup> getJobGroupsByDynamicQuery(Map queryParamMap) { 
		return monitorService.getJobGroupsByDynamicQuery(queryParamMap);
	}
	
	public List<JobGroup> analyzeToJobGroupsTreeList(List<JobGroup> flatJobGroupList) {
		return monitorService.analyzeToJobGroupsTreeList(flatJobGroupList);
	}
	
	public List<JobGroup> getJobGroupsTreeListByQuery(String queryCondition, String orderBy) { 
		return monitorService.getJobGroupsTreeListByQuery( queryCondition,  orderBy);
	}
	
	// =============================================================
	public boolean addUser(User user, AdminAuth auth) {
		return monitorService.addUser(user, auth);
	}
	
	public boolean removeUser(String id, AdminAuth auth) {
		return monitorService.removeUser(id, auth);
	}
	
	public boolean modifyUser(User user, AdminAuth auth) {
		return monitorService.modifyUser(user, auth);
	}
	
	public boolean modifyUserPassword(User user, AdminAuth auth) {
		return monitorService.modifyUserPassword(user, auth);
	}
	
	public User getUser(String userId) {
		return monitorService.getUser(userId);
	}
	
	public List<User> getAllUsers() {
		return monitorService.getAllUsers();
	}
	
	public List<User> getUsersByQuery(String queryCondition, String orderBy) {
		return monitorService.getUsersByQuery(queryCondition, orderBy);
	}

	public boolean isAllowedForOperation(String jobGroupId, String jobId, User user) {
		return monitorService.isAllowedForOperation(jobGroupId, jobId, user);
	}
	
	public User login(String id, String password, String ip) {
		return monitorService.login(id, password, ip);
	}
	
	/**
	 * 시스템 설정 값을 조회함.
	 * 미리 정의된 값들만 조회 가능함. (예, "DAILY_ACTIVATION_TIME")
	 * @param key
	 * @return
	 */
	public String getSystemConfigValue(String key) {
		return controllerService.getSystemConfigValue(key);
	}
}
