package nexcore.scheduler.entity;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.util.ByteArray;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Controller 서비스 </li>
 * <li>작성일 : 2010. 4. 22.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IControllerService {
	
	//############################ License ################################
	/**
	 * LicenseManager 의 정보로 위반 사항이 없는지 체크 
	 * @return Map<String, Integer> 객체를 리턴함.
	 */
	public boolean checkLicenseAndCurrentInfo();
	/**
	 * LicenseManager에 저장된 agentCount, maxAgentCount, jobDefinitionCount, maxJobDefinitionCount 추출 
	 * @return Map<String, Integer> 객체를 리턴함.
	 */
	public Map<String, Integer> getLicenseAndCurrentInfo();
	

	//############################ Agent 로부터의 callback ################################
	
	public boolean isAlive();
	
	/**
	 * 스케줄러 JVM 의 NEXCORE_ID 를 획득
	 * @return
	 */
	public String getSystemId();
	
	/**
	 * Job 실행 결과를 Controller 로 리턴함
	 * 
	 * @param je
	 */
	public boolean callBackJobEnd(JobExecution je);

	/**
	 * Job suspend 결과를 Controller 로 리턴함
	 * 
	 * @param je
	 */
	public boolean callBackJobSuspend(JobExecution je);

	/**
	 * Job resume 결과를 Controller 로 리턴함
	 * 
	 * @param je
	 */
	public boolean callBackJobResume(JobExecution je);

	
	
	//############################ JOB 모니터링 API ################################
	
	/**
	 * JobExecution 의 상태 정보를 리턴함.
	 * @param jobExecutionId
	 * 
	 * @return 상태값. JobExecution.STATE_### 상수값
	 */
	public int getJobExecutionState(String jobExecutionId);

	/**
	 * JobExecution 의 실행 결과코드 리턴
	 * @param jobExecutionId
	 * @return
	 */
	public int getJobExecutionReturnCode(String jobExecutionId);

	/**
	 * JobExecution 의 실행 결과 값 리턴
	 * @param jobExecutionId
	 * @return
	 */
	public Properties getJobExecutionReturnValues(String jobExecutionId);
	
	/**
	 * 현재 running 상태인 모든 JobExecution 리스트 조회.
	 * @return List of all running JobExecutions.
	 */
	public Collection<JobExecution> getRunningJobExecutions();

	
	//------- 여기부터는 Admin function 들
	
	/**
	 * JobDefinition 정보를 query 조건에 따라 검색하여 리턴함 
	 * @param queryCondition NBS_JOB_DEF 테이블을 select 할때 사용된 where 절을 입력함 이후의 조건을 입력함. 
	 *         예) "WHERE JOB_GROUP_ID LIKE '%XYZ%'
	 * @return List of JobDefinition 객체를 리턴함
	 */
	public List<JobDefinition> getJobDefinitionList(String queryCondition, boolean deep);

	/**
	 * Dynamic 쿼리 방식으로 JobDefinition들 조회
	 * @param queryParamMap 검색 조건을 담고 있는 Map.
	 * {viewFilterId, jobIdLike, jobGroupIdLike, jobDescLike, jobType, preJobIdLike, triggerJobIdLike, agentId, ownerLike, authorizedJobGroupIdViewList} 
	 * @param deep (PreJob List, Param) 포함 여부
	 * @return
	 */
	public List<JobDefinition> getJobDefinitionList(Map queryParamMap, boolean deep);

	/** 
	 * Job Id List 로 JobDefinition 목록 조회함
	 * @param jobIdList
	 * @return List of JobDefinition 객체를 리턴함
	 */
	public List<JobDefinition> getJobDefinitionListByJobIdList(List jobIdList, boolean deep);

	/**
	 * JobDefinition 정보중 원하는 컬럼만 조회한다.
	 * queryParamMap 에 "COLUMN_LIST" 키값으로 컬럼 리스트 지정함
	 * @param queryParamMap 
	 * @return
	 */
	public List<Map> getJobDefinitionListFreeColumn(Map queryParamMap);

	/**
	 * JobDefinition Deep 정보를 리턴함
	 * @param jobId
	 * @return
	 */
	public JobDefinition getJobDefinition(String jobId);
	
	/**
	 * Job Definition 존재여부 체크
	 * @param jobId
	 * @return
	 */
	public boolean existJobDefinition(String jobId);
	
	
	/**
	 * 해당일이 activate 되도록 schedule 된 일인지 체크함
	 * @param jobdef
	 * @param yyyymmdd 체크하려는 일자
	 * @return
	 */
	public boolean isScheduledDay(JobDefinition jobdef, String yyyymmdd);
	
	
	/**
	 * JobInstance 정보를 query 조건에 따라 검색하여 리턴함
	 * @param queryCondition NBS_JOB_INS 테이블을 select 할때 사용된 where 절을 입력함 이후의 조건을 입력함. 
	 *         예) "WHERE JOB_GROUP_ID LIKE '%XYZ%'
	 * @param orderBy 정렬조건. "ORDER BY "
	 * @return list of JobInstance 객체
	 */
	public List<JobInstance> getJobInstanceList(String queryCondition, String orderBy, boolean deep);

	/**
	 * JobInstance 정보를 query 조건에 따라 검색하여 리턴함 
	 * @param queryParamMap 테이블 조회에 필요한 조건을 Map 방식으로 입력함. ibatis dynamic 쿼리 방식. 
	 * @return list of JobInstance 객체
	 */
	public List<JobInstance> getJobInstanceList(Map queryParamMap, boolean deep);

	/**
	 * JobInstance 정보를 query 조건에 따라 검색하여 리턴함
	 * 페이징처리를 위해 결과의 skip ~ maxResult 만 리턴받음.
	 *  
	 * @param queryParamMap 테이블 조회에 필요한 조건을 Map 방식으로 입력함. ibatis dynamic 쿼리 방식.
	 * @param deep 
	 * @param skip 결과중 skip 건수
	 * @param maxResult 결과 리스트 크기
	 * @return list of JobInstance 객체
	 */
	public List<JobInstance> getJobInstanceList(Map queryParamMap, boolean deep, int skip, int maxResult);

	/**
	 * 검색 조건에 맞는 JobInstance 전체 개수
	 * 페이징 처리에 사용됨
	 * @param queryParamMap
	 * @return
	 */
	public int getJobInstanceCount(Map queryParamMap);
	
	/**
	 * iBATIS 의 RowHandler 를 이용하여 대랑 JobInstance 조회를 함.
	 * 대량 조회는 기본적으로 no-deep 쿼리한다.
	 * @param queryParamMap
	 * @param rowHandler
	 */
	public void getJobInstanceListWithRowHandler(Map queryParamMap, Object rowHandler);

	/**
	 * 필요한 컬럼만 조회한다. ($columnList$ 에 컬럼 목록 담는다)
	 * iBATIS 의 RowHandler 를 이용하여 대랑 JobInstance 조회를 함.
	 * 대량 조회는 기본적으로 no-deep 쿼리한다.
	 * @param queryParamMap
	 * @param rowHandler
	 */
	public void getJobInstanceListFreeColumnWithRowHandler(Map queryParamMap, Object rowHandler);
	
	/**
	 * 필요한 컬럼만 조회한다. ($columnList$ 에 컬럼 목록 담는다)
	 * $queryCondition$ 방식이 아닌 ibatis dynamic 태그를 이용한 쿼리. 
	 * 대량 조회를 대비해 rowHandler 를 이용한다.
	 * @param queryParamMap
	 * @return List of Map (컬럼명, 값)
	 * @throws SQLException
	 * @since 3.6.3
	 */ // 2013-08-16. 성능을 위해 불필요한 컬럼 배제
	public List<Map> getJobInstanceListFreeColumn(Map queryParamMap);

	/**
	 * JobInstance Deep 정보를 리턴함.
	 * @param jobInstanceId
	 * @return
	 */
	public JobInstance getJobInstance(String jobInstanceId);

	/**
	 * JobInstance No Deep 정보를 리턴함.
	 * @param jobInstanceId
	 * @return
	 */
	public JobInstance getJobInstanceSimple(String jobInstanceId);
	
	/**
	 * JobInstance 존재여부 리턴
	 * @param jobInstanceId
	 * @return
	 */
	public boolean existJobInstance(String jobInstanceId);
	
	/**
	 * JobExecution 정보를 Job Instance Id 에 따라 검색함 
	 * @param jobInstanceId  
	 * @return List of JobExecution 객체를 리턴함
	 */
	public List<JobExecution> getJobExecutionListByJobInstanceId(String jobInstanceId, boolean deep);

	public JobExecution getJobExecutionDeep(String jobExecutionId);
	
	/**
	 * Agent 에 있는 로그 파일 이름, 위치, 크기 조회
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getJobLogFileLocation(String jobInstanceId);
	
	/**
	 * Agent 에 있는 SUB 로그 파일 이름, 위치, 크기 조회
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getSubJobLogFileLocation(String jobInstanceId);

	/**
	 * Agent 에 있는 SUB 로그 파일 이름, 위치, 크기 조회
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getStdoutJobLogFileLocation(String jobInstanceId);

	/**
	 * Agent 에 있는 로그파일 내용 읽어 리턴
	 * @param jobInstanceId
	 * @param location
	 * @param offset
	 * @param length
	 * @return
	 */
	public ByteArray readLogFile(String jobInstanceId, JobLogFileLocation location, int offset, int length);
	
	/**
	 * Agent 에 있는 파일 내용 읽어 리턴
	 * @param agentId
	 * @param filename
	 * @param offset
	 * @param length
	 * @return
	 */
	public ByteArray readAgentFile(String agentId, String filename, int offset, int length);

	/**
	 * Agent로부터 실제 실행 스레드 dump를 view 함 
	 * @param jobInstanceId
	 * @return
	 */
	public Map getJobExecutionThreadStackTrace(String jobInstanceId);
	
	/**
	 * 해당 일자에 Activate 될 Job Id 목록을 리트스함.
	 * @param yyyymmdd
	 * @return list of Job ID 
	 */
	public List getJobListWillBeActivated(String yyyymmdd);
	
	/**
	 * 특정 Job 의 해당월에 Activate 될 리스트 리턴
	 * @return list of yyyymmdd 
	 */
	public List getDayListWillBeActivated(String jobId, String yyyymm);
	
	/**
	 * 특정 Job 의 해당월에 Activate 될 리스트 리턴. 아직 등록되지 않은 Job 으로 스케줄 시뮬레이션 할때 사용됨.
	 * @return list of yyyymmdd 
	 */
	public List getDayListWillBeActivated(JobDefinition jobdef, String yyyymm);
	
	/**
	 * 특정월의 기준일 계산 결과.
	 * @param jobId
	 * @param yyyymm
	 * @return
	 */
	public Map getBaseDateMonthlyMap(String jobId, String yyyymm);

	/**
	 * 특정월의 기준일 계산 결과. 아직 등록되지 않은 JobDef를 시뮬레이션.
	 * @param jobId
	 * @param yyyymm
	 * @return
	 */
	public Map getBaseDateMonthlyMap(JobDefinition jobdef, String yyyymm);

	
	//############################ JOB RUNTIME API ################################

	/**
	 * 온디멘트 배치를 호출함
	 * @param jobId
	 * @param inParam
	 * @param callerId
	 * @param callerIp
	 * @param onlineContextData
	 * @return JobExecutionId를 리턴함
	 */
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp, byte[] onlineContextData);


	/**
	 * 온디멘트 배치를 호출함
	 * @param jobId
	 * @param inParam
	 * @param callerId
	 * @param callerIp
	 * @return JobExecutionId를 리턴함
	 */
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp);

	
	//############################ JOB Instance 컨트롤 API ################################
	
	/** 
	 * 수동으로 하나의 Job을 activate 함.
	 * @param jobId
	 * @param procDate
	 * @param auth
	 * @return Job Instance Id
	 */
	public String activateJob(String jobId, String procDate, AdminAuth auth);
	
	
	/** 
	 * 수동으로 하나의 Job을 activate 함. activate 후에는 상태가 LOCK 상태가 됨.
	 * @param jobId
	 * @param procDate
	 * @param auth
	 * @return Job Instance Id
	 */
	public String activateAndLockJob(String jobId, String procDate, AdminAuth auth);

	/**
	 * Job Instance 를 LOCK 함. LOCK 상태에서는 STATE 변경이 안됨.
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public boolean lockJob(String jobInstanceId, AdminAuth auth);
	
	/**
	 * Job Instance 를 UNLOCK 함. LOCK 상태에서는 STATE 변경이 안됨.
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public boolean unlockJob(String jobInstanceId, AdminAuth auth);
	
	/**
	 * END 상태의 JOB 을 WAIT 상태로 변경
	 * @param jobInstanceId
	 * @param auth
	 */
	public void reRunJob(String jobInstanceId, AdminAuth auth);
	
	/**
	 * RUNNING, SUSPEND 상태의 Job을 kill함.
	 * @param jobInstanceId
	 * @param auth
	 */
	public void stopJob(String jobInstanceId, AdminAuth auth);

	/**
	 * RUNNING 상태의 Job을 일시정지시킴
	 * @param jobInstanceId
	 * @param auth
	 */
	public void suspendJob(String jobInstanceId, AdminAuth auth);
	
	/**
	 * SUSPEND 상태의 Job을 resume 시킴
	 * @param jobInstanceId
	 * @param auth
	 */
	public void resumeJob(String jobInstanceId, AdminAuth auth);
	
	/**
	 * Confirm 이 필요한 Job 에게 confirm 을 줌
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void confirmJob(String jobInstanceId, AdminAuth auth);
	
	/**
	 * 상태에 상관없이 (이미 RUNNING 상태인것은 제외), 조건에 상관없이 해당 Job 을 즉시 실행시킴. 
	 * 이것을 실행시키면 trigger 도 돌아가고, 후행 Job 에도 영향을 미친다.
	 * @param jobInstanceId
	 * @param auth
	 * @return JobExecutionId or null if fail
	 */
	public String forceRunJob(String jobInstanceId, AdminAuth auth);
	
	/**
	 * ENDED_FAIL 인 Job을 ENDED_OK고 강제 상태 변경후, 후행 Job 들이 동작하도록 함
	 * 후행 Job, Trigger 를 기동시킴. 
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void forceEndOk(String jobInstanceId, AdminAuth auth);

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
	public void forceChangeToGhost(String jobInstanceId, AdminAuth auth);

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
	public JobInstance modifyJobInstance(JobInstance jobInstance, AdminAuth auth);
	
	
	/**
	 * Job Instance 파라미터 변경.
	 * @param jobInstanceId
	 * @param newParams 새 파라미터 Map
	 * @param auth
	 * @return 변경된 JobInstance
	 */
	public JobInstance modifyJobInstanceParameters(String jobInstanceId, Map newParams, AdminAuth auth);
	
	/**
	 * Job 인스턴스의 Agent Id 를 변경함.
	 * Running, suspended 일때는 안됨.
	 * @param jobInstanceId
	 * @param newAgentId
	 * @param auth
	 * @return true if success, false fail
	 * @since 3.6.3
	 */
	public boolean modifyJobInstanceAgentId(String jobInstanceId, String newAgentId, AdminAuth auth);

	//############################ JOB Definition 컨트롤 API ################################
	
	/**
	 * Job Definition 추가.
	 * @return insert count
	 */
	public int addJobDefinition(JobDefinition jobdef, AdminAuth auth);
	
	/**
	 * 변경
	 * @param jobdef
	 * @param auth
	 * @return 변경된 JobDefinition 
	 */
	public JobDefinition modifyJobDefinition(JobDefinition jobdef, AdminAuth auth);
	
	/**
	 * 삭제
	 * @param jobDefId
	 * @param auth
	 * @return 성공
	 */
	public boolean deleteJobDefinition(String jobDefId, AdminAuth auth);

	/**
	 * 등록 요청 취소
	 * @param reqNo
	 * @param auth
	 * @return 성공
	 */
	public boolean deleteJobDefinitionStg(String reqNo, AdminAuth auth);
	
	/** 
	 * 등록된 Calendar 정보를 리스트함.
	 * @return
	 */
	public Map getCalendarList();
	
	/** 
	 * Calendar의 일자 조회
	 * @param calendarId
	 * @return
	 */
	public List getCalendarDayList(String calendarId);
	
	/** 
	 * Calendar 정보를 파일,DB 로부터 다시 메모리로 로드함
	 */
	public void reloadCalendar();

	/**
	 * JobDefinition 변경/신규/삭제를 위한 요청 등록.
	 * @param jobdef
	 * @param auth
	 * @return 요청번호
	 */
	public String addJobDefinitionStg(JobDefinitionStg jobdef, AdminAuth auth);


	/**
	 * JobDefinition 변경/신규/삭제 요청목록 조회
	 */
	public List<JobDefinitionStg> getJobDefinitionStgList(String queryCondition, boolean deep);

	public void getJobDefinitionStgListWithRH(String queryCondition, Object rowHandler);

	/**
	 * JobDefinition 변경/신규/삭제 요청 조회
	 */
	public JobDefinitionStg getJobDefinitionStg(String reqNo, String jobId);

	/**
	 * 요청 내역을 JOBDEF 로 승인 반영함.
	 * @param jobdef
	 * @param auth
	 */
	public void approveJobDefinitionStgToJobDefinition(String reqNo, String jobId, String reqARReason, AdminAuth auth);
	
	/**
	 * 요청 내역을 반려함.
	 * @param jobdef
	 * @param auth
	 */
	public void rejectJobDefinitionStgToJobDefinition(String reqNo, String jobId, String reqARReason, AdminAuth auth);
	
	public List validateJobDefinition(JobDefinition jobdef);

	public List validateJobInstance(JobInstance jobins);
	
	/**
	 * 본 사이트에서 사용하는 Job 타입 리스트 리턴. nexcore-scheduler-server.properties 에 설정
	 * @return
	 */
	public List<String> getJobTypeUsingList();
	
	/**
	 * 본 사이트에서 사용하는 로그레벨 리스트 리턴. nexcore-scheduler-server.properties 에 설정
	 * @return
	 */
	public List<String> getLogLevelUsingList();

	/**
	 * Job 등록정보의 로그 레벨 변경
	 * 변경후에 생성되는 인스턴스들은 기본적으로 이 로그레벨로 생성됨.
	 * 이미 생성된 인스턴스는 기존 레벨 유지
	 * @param jobId
	 * @param loglevel
	 * @param auth
	 * @return
	 */
	public boolean changeJobDefinitionLogLevel(String jobId, String loglevel, AdminAuth auth);

	/**
	 * Job 인스턴스의 로그레벨 변경
	 * Job 등록정보의 로그레벨에는 영향주지 않음
	 * Job 인스턴스의 로그 레벨은 내장 Job 파라미터 (LOG_LEVEL)  값으로 전달된 (v 3.6 부터)
	 * @param jobInstanceId
	 * @param loglevel
	 * @param auth
	 * @return
	 */
	public boolean changeJobInstanceLogLevel(String jobInstanceId, String loglevel, AdminAuth auth);

	/**
	 * EXACT 방식의 반복일 경우 정규표현식을 해석하여 실행 시각을 예측함
	 * From, Until 도 고려하여 계산함
	 * @param jobdef
	 * @return
	 */
	public List<String> getTimePlanForExactRepeat(JobDefinition jobdef);

	/**
	 * EXACT 방식의 반복일 경우 정규표현식을 해석하여 실행 시각을 예측함
	 * From, Until 도 고려하여 계산함
	 * @param jobdef
	 * @return
	 */
	public List<String> getTimePlanForExactRepeat(JobInstance jobins);

	//**********************************************************************
	//****  Global Parameter
	//**********************************************************************
	
	public void addGlobalParameter(String paramName, String paramValue, AdminAuth auth);

	public void modifyGlobalParameter(String paramName, String paramValue, AdminAuth auth);

	public void deleteGlobalParameter(String paramName, AdminAuth auth);

	public Map getGlobalParameters();

	/**
	 * 글로벌 파라미터를 DB에서 다시 읽는다.
	 */
	public void reloadGlobalParameters(AdminAuth auth);
	
	/**
	 * 표현식으로 되어있는 파라미터 값을 상수 값으로 evaluate 함.
	 * Job 등록시 등록 버튼 누르기 전에 미리 검토해볼 수 있게 하기 위한 기능. 
	 * 
	 * @param agentId 
	 * @param jobdef Job등록정보 폼 화면에 입력한 값들
	 * @param paramMap Param Name, Param Value Map
	 * @param baseDateCalId basedate calendar id
	 * @param baseDateLogic basedate logic
	 */
	public Map evaluateParameters(String agentId, JobDefinition jobdef, Map paramMap, String baseDateCalId, String baseDateLogic);

	
	//############################ admin API ################################
	
	//**********************************************************************
	//****  Parallel Group 
	//**********************************************************************
	public void addParallelGroup(ParallelGroup pg, AdminAuth auth);

	public void modifyParallelGroup(ParallelGroup pg, AdminAuth auth);

	public void deleteParallelGroup(String groupName, AdminAuth auth);

	public List<ParallelGroup> getAllParallelGroups();
	
	public ParallelGroup getParallelGroup(String groupName);
	
	/**
	 * 스케줄러 셧다운
	 */
	public void shutdown(String id, String password, String ip);

	/**
	 * 시스템 설정 값을 조회함.
	 * 미리 정의된 값들만 조회 가능함. (예, "DAILY_ACTIVATION_TIME")
	 * @param key
	 * @return
	 */
	public String getSystemConfigValue(String key);
	
}