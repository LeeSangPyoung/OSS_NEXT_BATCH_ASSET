package nexcore.scheduler.controller.internal;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.IControllerService;
import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobDefinitionStg;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.JobLogFileLocation;
import nexcore.scheduler.entity.ParallelGroup;
import nexcore.scheduler.util.ByteArray;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Controller RMI Service.</li>
 * <li>작성일 : 2010. 8. 30.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ControllerServiceImpl implements IControllerService {
	private transient ControllerMain controllerMain;

	public void init() {
		Util.logServerInitConsole("ControllerService");
	}
	public void destroy() {
	}

	public ControllerMain getControllerMain() {
		return controllerMain;
	}

	public void setControllerMain(ControllerMain controllerMain) {
		this.controllerMain = controllerMain;
	}
	
	/**
	 * LicenseManager 의 정보로 위반 사항이 없는지 체크 
	 * @return Map<String, Integer> 객체를 리턴함.
	 */
	public boolean checkLicenseAndCurrentInfo(){
		return controllerMain.checkLicenseAndCurrentInfo();
	}
	/**
	 * LicenseManager에 저장된 agentCount, maxAgentCount, jobDefinitionCount, maxJobDefinitionCount 추출 
	 * @return Map<String, Integer> 객체를 리턴함.
	 */
	public Map<String, Integer> getLicenseAndCurrentInfo(){
		return controllerMain.getLicenseAndCurrentInfo();
	}

	public boolean isAlive() {
		return true;
	}
	
	public String getSystemId() {
		return Util.getSystemId();
	}
	
	/**
	 * agent 로 부터 Job 실행 결과를 수신함.
	 * @param je
	 * @param returnCode
	 * @param returnValues
	 */
	public boolean callBackJobEnd(JobExecution je) {
		return controllerMain.getJobRunResultProcessor().callBackJobEnd(je);
	}
	
	public boolean callBackJobSuspend(JobExecution je) {
		return controllerMain.getJobRunResultProcessor().callBackJobSuspend(je);
	}
	
	public boolean callBackJobResume(JobExecution je) {
		return controllerMain.getJobRunResultProcessor().callBackJobResume(je);
	}
	
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp, byte[] onlineCtxData) {
		return controllerMain.invokeOnDemandJob(jobId, inParam, callerId, callerIp, onlineCtxData);
	}
	
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp) {
		return controllerMain.invokeOnDemandJob(jobId, inParam, callerId, callerIp);
	}

	public int getJobExecutionState(String jobExecutionId) {
		return controllerMain.getJobExecutionState(jobExecutionId);
	}
	
	public int getJobExecutionReturnCode(String jobExecutionId) {
		return controllerMain.getJobReturnCode(jobExecutionId);
	}
	public Properties getJobExecutionReturnValues(String jobExecutionId) {
		return controllerMain.getJobReturnValue(jobExecutionId);
	}
	
	/**
	 * 현재 running 상태인 모든 JobExecution 리스트 조회.
	 * @return List of all running JobExecutions.
	 */
	public Collection<JobExecution> getRunningJobExecutions() {
		return controllerMain.getRunningJobExecutions();
	}

	public List<JobDefinition> getJobDefinitionList(String queryCondition, boolean deep) {
		return controllerMain.getJobDefinitionListByQuery(queryCondition, deep);
	}
	
	public List<JobDefinition> getJobDefinitionList(Map queryParamMap, boolean deep) {
		return controllerMain.getJobDefinitionListByDynamicQuery(queryParamMap, deep);
	}

	public List<JobDefinition> getJobDefinitionListByJobIdList(List jobIdList, boolean deep) {
		return controllerMain.getJobDefinitionListByJobIdList(jobIdList, deep);
	}

	/**
	 * JobDefinition 정보중 원하는 컬럼만 조회한다.
	 * queryParamMap 에 "columnList" 키값으로 컬럼 리스트 지정함
	 * @param queryParamMap 
	 * @return
	 */
	public List<Map> getJobDefinitionListFreeColumn(Map queryParamMap) {
		return controllerMain.getJobDefinitionListFreeColumn(queryParamMap);
	}

	public JobDefinition getJobDefinition(String jobid) {
		return controllerMain.getJobDefinition(jobid);
	}
	
	public boolean existJobDefinition(String jobId) {
		return controllerMain.existJobDefinition(jobId);
	}
	
	/**
	 * 해당일이 activate 되도록 schedule 된 일인지 체크함
	 * @param jobdef
	 * @param yyyymmdd 체크하려는 일자
	 * @return
	 */
	public boolean isScheduledDay(JobDefinition jobdef, String yyyymmdd) {
		return controllerMain.isScheduledDay(jobdef, yyyymmdd);
	}
	
	
	public List<JobInstance> getJobInstanceList(String queryCondition, String orderBy, boolean deep) {
		return controllerMain.getJobInstanceListByQuery(queryCondition, orderBy, deep);
	}
	public List<JobInstance> getJobInstanceList(Map queryParamMap, boolean deep) {
		return controllerMain.getJobInstanceListByDynamicQuery(queryParamMap, deep);
	}
	public List<JobInstance> getJobInstanceList(Map queryParamMap, boolean deep, int skip, int maxResult) {
		return controllerMain.getJobInstanceListByDynamicQuery(queryParamMap, deep, skip, maxResult);
	}
	public int getJobInstanceCount(Map queryParamMap) {
		return controllerMain.getJobInstanceCountByDynamicQuery(queryParamMap);
	}
	
	/**
	 * iBATIS 의 RowHandler 를 이용하여 대랑 JobInstance 조회를 함.
	 * 대량 조회는 기본적으로 no-deep 쿼리한다.
	 * @param queryParamMap
	 * @param rowHandler
	 */
	public void getJobInstanceListWithRowHandler(Map queryParamMap, Object rowHandler) {
		controllerMain.getJobInstanceListByDynamicQueryWithRowHandler(queryParamMap, rowHandler);
	}
	
	/**
	 * 필요한 컬럼만 조회한다. ($columnList$ 에 컬럼 목록 담는다)
	 * iBATIS 의 RowHandler 를 이용하여 대랑 JobInstance 조회를 함.
	 * 대량 조회는 기본적으로 no-deep 쿼리한다.
	 * @param queryParamMap
	 * @param rowHandler
	 */
	public void getJobInstanceListFreeColumnWithRowHandler(Map queryParamMap, Object rowHandler) {
		controllerMain.getJobInstanceListFreeColumnWithRowHandler(queryParamMap, rowHandler);
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
		return controllerMain.getJobInstanceListFreeColumn(queryParamMap);
	}
	
	public JobInstance getJobInstance(String jobInstanceId) {
		return controllerMain.getJobInstance(jobInstanceId);
	}

	public JobInstance getJobInstanceSimple(String jobInstanceId) {
		return controllerMain.getJobInstanceSimple(jobInstanceId);
	}

	public boolean existJobInstance(String jobInstanceId) {
		return controllerMain.existJobInstance(jobInstanceId);
	}

	public List<JobExecution> getJobExecutionListByQuery(String jobInstanceId, boolean deep) {
		return controllerMain.getJobExecutionListByQuery(jobInstanceId, deep);
	}
	
	public List<JobExecution> getJobExecutionListByJobInstanceId(String jobInstanceId, boolean deep) {
		return controllerMain.getJobExecutionListByJobInstanceId(jobInstanceId, deep);
	}
	
	public JobExecution getJobExecutionDeep(String jobExecutionId) {
		return controllerMain.getJobExecutionDeep(jobExecutionId);
	}
	
	/**
	 * Agent 에 있는 로그 파일 이름, 위치, 크기 조회
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getJobLogFileLocation(String jobInstanceId) {
		return controllerMain.getJobLogFileLocation(jobInstanceId);
	}
	
	/**
	 * Agent 에 있는 SUB 로그 파일 이름, 위치, 크기 조회
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getSubJobLogFileLocation(String jobInstanceId) {
		return controllerMain.getSubJobLogFileLocation(jobInstanceId);
	}

	/**
	 * Agent 에 있는 stdout 로그 파일 이름, 위치, 크기 조회
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getStdoutJobLogFileLocation(String jobInstanceId) {
		return controllerMain.getStdoutJobLogFileLocation(jobInstanceId);
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
		return controllerMain.readLogFile(jobInstanceId, location, offset, length);
	}
	
	/**
	 * Agent 에 있는 파일 내용 읽어 리턴
	 * @param agentId
	 * @param filename
	 * @param offset
	 * @param length
	 * @return
	 */
	public ByteArray readAgentFile(String agentId, String filename, int offset, int length) {
		return controllerMain.readAgentFile(agentId, filename, offset, length);
	}

	/**
	 * Agent 로 부터 JobExecution Thread stack trace를 가져옴.
	 */
	public Map getJobExecutionThreadStackTrace(String jobInstanceId) {
		return controllerMain.getJobExecutionThreadStackTrace(jobInstanceId);
	}
	
	/**
	 * 해당 일자에 Activate 될 Job Id 목록을 리트스함.
	 * @param date
	 * @return list of Job ID 
	 */
	public List getJobListWillBeActivated(String yyyymmdd) {
		return controllerMain.getJobListWillBeActivated(yyyymmdd);
	}
	
	/**
	 * 특정 Job 의 해당월에 Activate 될 리스트 리턴
	 * @return list of yyyymmdd 
	 */
	public List getDayListWillBeActivated(String jobId, String yyyymm) {
		return controllerMain.getDayListWillBeActivated(jobId, yyyymm);
	}
	
	/**
	 * 특정 Job 의 해당월에 Activate 될 리스트 리턴. 아직 등록되지 않은 Job 으로 스케줄 시뮬레이션 할때 사용됨.
	 * @return list of yyyymmdd 
	 */
	public List getDayListWillBeActivated(JobDefinition jobdef, String yyyymm) {
		return controllerMain.getDayListWillBeActivated(jobdef, yyyymm);
	}

	/**
	 * 특정월의 기준일 계산 결과.
	 * @param jobId
	 * @param yyyymm
	 * @return
	 */
	public Map getBaseDateMonthlyMap(String jobId, String yyyymm) {
		return controllerMain.getBaseDateMonthlyMap(jobId, yyyymm);
	}
		
	/**
	 * 특정월의 기준일 계산 결과. 아직 등록되지 않은 JobDef를 시뮬레이션.
	 * @param jobdef
	 * @param yyyymm
	 * @return
	 */
	public Map getBaseDateMonthlyMap(JobDefinition jobdef, String yyyymm) {
		return controllerMain.getBaseDateMonthlyMap(jobdef, yyyymm);
	}

	/**
	 * 수동으로 Job 하나를 activate 함.
	 * @return Job Instance Id
	 */
	public String activateJob(String jobId, String procDate, AdminAuth auth) {
		return controllerMain.activateJob(jobId, procDate, auth);
	}

	/**
	 * 수동으로 Job 하나를 activateAndLock
	 * @return Job Instance Id
	 */
	public String activateAndLockJob(String jobId, String procDate, AdminAuth auth) {
		return controllerMain.activateAndLockJob(jobId, procDate, auth);
	}

	/**
	 * Job Instance 를 LOCK 함. LOCK 상태에서는 STATE 변경이 안됨.
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public boolean lockJob(String jobInstanceId, AdminAuth auth) {
		return controllerMain.lockJob(jobInstanceId, auth);
	}
	
	/**
	 * Job Instance 를 LOCK 함. LOCK 상태에서는 STATE 변경이 안됨.
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public boolean unlockJob(String jobInstanceId, AdminAuth auth) {
		return controllerMain.unlockJob(jobInstanceId, auth);
	}

	/**
	 * 상태에 상관없이 (이미 RUNNING, SUSPENDED 상태인것은 제외), 조건에 상관없이 해당 Job 을 즉시 실행시킴. 
	 * 이것을 실행시키면 trigger 도 돌아가고, 후행 Job 에도 영향을 미친다.
	 * @param jobInstanceId
	 * @param auth
	 * @return JobExecutionId or null if fail
	 */
	public String forceRunJob(String jobInstanceId, AdminAuth auth) {
		return controllerMain.forceRunJob(jobInstanceId, auth);
	}

	/**
	 * END 상태의 JOB 을 WAIT 상태로 변경
	 * @param jobInstanceId
	 * @param auth
	 * @return 다시 실행한 Job Execution Id
	 */
	public void reRunJob(String jobInstanceId, AdminAuth auth) {
		controllerMain.reRunJob(jobInstanceId, auth);
	}

	/**
	 * END 상태의 JOB 을 WAIT 상태로 변경
	 * @param jobInstanceId
	 * @param auth
	 */
	public void stopJob(String jobInstanceId, AdminAuth auth) {
		controllerMain.stopJob(jobInstanceId, auth);
	}
	
	/**
	 * RUNNING 상태의 Job을 일시정지시킴
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void suspendJob(String jobInstanceId, AdminAuth auth) {
		controllerMain.suspendJob(jobInstanceId, auth);
	}

	/**
	 * SUSPENDED 상태의 Job을 계속실행시킴
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void resumeJob(String jobInstanceId, AdminAuth auth) {
		controllerMain.resumeJob(jobInstanceId, auth);
	}

	/**
	 * Confirm 이 필요한 Job 에게 confirm 을 줌
	 * @param jobInstanceId
	 * @param auth
	 */
	public void confirmJob(String jobInstanceId, AdminAuth auth) {
		controllerMain.confirmJob(jobInstanceId, auth);
	}
	
	/**
	 * ENDED_FAIL 인 Job을 ENDED_OK고 강제 상태 변경후, 후행 Job 들이 동작하도록 함
	 * 후행 Job, Trigger 를 기동시킴. 
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void forceEndOk(String jobInstanceId, AdminAuth auth) {
		controllerMain.forceEndOk(jobInstanceId, auth);
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
		controllerMain.forceChangeToGhost(jobInstanceId, auth);
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
		return controllerMain.modifyJobInstance(jobInstance, auth);
	}

	/**
	 * Job Instance 파라미터 변경.
	 * @param jobInstanceId
	 * @param newParams 새 파라미터 Map
	 * @param auth
	 * @return 변경된 JobInstance
	 */
	public JobInstance modifyJobInstanceParameters(String jobInstanceId, Map newParams, AdminAuth auth) {
		return controllerMain.modifyJobInstanceParameters(jobInstanceId, newParams, auth);
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
		return controllerMain.modifyJobInstanceAgentId(jobInstanceId, newAgentId, auth);
	}
	
	/**
	 * Job Definition 추가.
	 * @return insert count
	 */
	public int addJobDefinition(JobDefinition jobdef, AdminAuth auth) {
		return controllerMain.addJobDefinition(jobdef, auth);
	}
	
	/**
	 * 변경
	 * @param jobdef
	 * @param auth
	 * @return 변경된 JobDefinition 
	 */
	public JobDefinition modifyJobDefinition(JobDefinition jobdef, AdminAuth auth) {
		return controllerMain.modifyJobDefinition(jobdef, auth);
	}
	
	/**
	 * 삭제
	 * @param jobDefId
	 * @param auth
	 * @return 성공
	 */
	public boolean deleteJobDefinition(String jobDefId, AdminAuth auth) {
		return controllerMain.deleteJobDefinition(jobDefId, auth);
	}
	
	/**
	 * 등록요청 취소
	 * @param reqNo
	 * @param auth
	 * @return 성공
	 */
	public boolean deleteJobDefinitionStg(String reqNo, AdminAuth auth) {
		return controllerMain.deleteJobDefinitionStg(reqNo, auth);
	}

	/** 
	 * 등록된 Calendar 정보를 리스트함.
	 * @return
	 */
	public Map getCalendarList() {
		return controllerMain.getCalendarList();
	}

	/** 
	 * Calendar의 일자 조회
	 * @param calendarId
	 * @return
	 */
	public List getCalendarDayList(String calendarId) {
		return controllerMain.getCalendarDayList(calendarId);
	}

	/** 
	 * Calendar 정보를 파일,DB 로부터 다시 메모리로 로드함
	 */
	public void reloadCalendar() {
		controllerMain.reloadCalendar();
	}
	
	/**
	 * JobDefinition 변경/신규/삭제를 위한 요청 등록.
	 * @param jobdef
	 * @param auth
	 * @return 요청번호
	 */
	public String addJobDefinitionStg(JobDefinitionStg jobdef, AdminAuth auth) {
		return controllerMain.addJobDefinitionStg(jobdef, auth);
	}

	/**
	 * JobDefinition 변경/신규/삭제 요청목록 조회
	 */
	public List<JobDefinitionStg> getJobDefinitionStgList(String queryCondition, boolean deep) {
		return controllerMain.getJobDefinitionStgList(queryCondition, deep);
	}

	public void getJobDefinitionStgListWithRH(String queryCondition, Object rowHandler) {
		controllerMain.getJobDefinitionStgListWithRH(queryCondition, rowHandler);
	}

	/**
	 * JobDefinition 변경/신규/삭제 요청 조회
	 */
	public JobDefinitionStg getJobDefinitionStg(String reqNo, String jobId) {
		return controllerMain.getJobDefinitionStg(reqNo, jobId);
	}


	/**
	 * 요청 내역을 JOBDEF 로 승인 반영함.
	 * @param jobdef
	 * @param auth
	 */
	public void approveJobDefinitionStgToJobDefinition(String reqNo, String jobId, String reqARReason, AdminAuth auth) {
		controllerMain.approveJobDefinitionStgToJobDefinition(reqNo, jobId, reqARReason, auth);
	}

	/**
	 * 요청 내역을 반려함.
	 * @param jobdef
	 * @param auth
	 */
	public void rejectJobDefinitionStgToJobDefinition(String reqNo, String jobId, String reqARReason, AdminAuth auth) {
		controllerMain.rejectJobDefinitionStgToJobDefinition(reqNo, jobId, reqARReason, auth);
	}

	/**
	 * Job 등록 정보 validation
	 * @param jobdef
	 * @return
	 */
	public List validateJobDefinition(JobDefinition jobdef) {
		return controllerMain.validateJobDefinition(jobdef);
	}
	
	public List validateJobInstance(JobInstance jobins) {
		return controllerMain.validateJobInstance(jobins);
	}
	
	public List<String> getJobTypeUsingList() {
		return controllerMain.getJobTypeUsingList();
	}

	public List<String> getLogLevelUsingList() {
		return controllerMain.getLogLevelUsingList();
	}
	
	public boolean changeJobDefinitionLogLevel(String jobId, String loglevel, AdminAuth auth) {
		return controllerMain.setJobDefinitionLogLevel(jobId, loglevel, auth);
	}

	public boolean changeJobInstanceLogLevel(String jobInstanceId, String loglevel, AdminAuth auth) {
		return controllerMain.setJobInstanceLogLevel(jobInstanceId, loglevel, auth);
	}
	
	/**
	 * EXACT 방식의 반복일 경우 정규표현식을 해석하여 실행 시각을 예측함
	 * From, Until 도 고려하여 계산함
	 * @param jobdef
	 * @return
	 */
	public List<String> getTimePlanForExactRepeat(JobDefinition jobdef) {
		return controllerMain.getTimePlanForExactRepeat(jobdef);
	}

	/**
	 * EXACT 방식의 반복일 경우 정규표현식을 해석하여 실행 시각을 예측함
	 * From, Until 도 고려하여 계산함
	 * @param jobins
	 * @return
	 */
	public List<String> getTimePlanForExactRepeat(JobInstance jobins) {
		return controllerMain.getTimePlanForExactRepeat(jobins);
	}
	
	//**********************************************************************
	//****  Global Parameter
	//**********************************************************************
	
	public void addGlobalParameter(String paramName, String paramValue, AdminAuth auth) {
		controllerMain.addGlobalParameter(paramName, paramValue, auth);
	}

	public void modifyGlobalParameter(String paramName, String paramValue, AdminAuth auth) {
		controllerMain.modifyGlobalParameter(paramName, paramValue, auth);
	}

	public void deleteGlobalParameter(String paramName, AdminAuth auth) {
		controllerMain.deleteGlobalParameter(paramName, auth);
	}

	public Map getGlobalParameters() {
		return controllerMain.getGlobalParameters();
	}

	/**
	 * Job 등록 정보 validation
	 */
	public void reloadGlobalParameters(AdminAuth auth) {
		controllerMain.reloadGlobalParameter(auth);
	}

	/**
	 * 표현식으로 되어있는 파라미터 값을 상수 값으로 evaluate 함.
	 * Job 등록시 등록 버튼 누르기 전에 미리 검토해볼 수 있게 하기 위한 기능. 
	 * @param agentId 
	 * @param jobdef Job등록정보 폼 화면에 입력한 값들
	 * @param paramMap Param Name, Param Value Map
	 * @param baseDateCalId basedate calendar id
	 * @param baseDateLogic basedate logic
	 */
	public Map evaluateParameters(String agentId, JobDefinition jobdef, Map paramMap, String baseDateCalId, String baseDateLogic) {
		return controllerMain.evaluateParameters(agentId, jobdef, paramMap, baseDateCalId, baseDateLogic);
	}
	
	//**********************************************************************
	//****  Parallel Group 
	//**********************************************************************
	public void addParallelGroup(ParallelGroup pg, AdminAuth auth) {
		controllerMain.addParallelGroup(pg, auth);
	}

	public void modifyParallelGroup(ParallelGroup pg, AdminAuth auth) {
		controllerMain.modifyParallelGroup(pg, auth);
	}

	public void deleteParallelGroup(String groupName, AdminAuth auth) {
		controllerMain.deleteParallelGroup(groupName, auth);
	}

	public List<ParallelGroup> getAllParallelGroups() {
		return controllerMain.getAllParallelGroups();
	}

	public ParallelGroup getParallelGroup(String groupName) {
		return controllerMain.getParallelGroup(groupName);
	}

	//**********************************************************************
	//****  Parallel Group 
	//**********************************************************************

	
	//**********************************************************************

	/**
	 * 컨트롤러, 스케줄러 셧다운
	 * @param auth
	 */
	public void shutdown(String id, String password, String ip) {
		controllerMain.shutdown(id,password,ip);
	}

	/**
	 * 시스템 설정 값을 조회함.
	 * 미리 정의된 값들만 조회 가능함. (예, "DAILY_ACTIVATION_TIME")
	 * @param key
	 * @return
	 */
	public String getSystemConfigValue(String key) {
		return controllerMain.getSystemConfigValue(key);
	}
	
}