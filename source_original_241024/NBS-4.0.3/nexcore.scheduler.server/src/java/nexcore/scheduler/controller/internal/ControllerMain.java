package nexcore.scheduler.controller.internal;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

import com.ibatis.sqlmap.client.SqlMapClient;

import nexcore.scheduler.agent.client.IAgentClient;
import nexcore.scheduler.controller.IJobExecutionIdMaker;
import nexcore.scheduler.controller.IJobInfoValidator;
import nexcore.scheduler.controller.IJobRunPreProcessor;
import nexcore.scheduler.controller.ha.IPeerClient;
import nexcore.scheduler.core.IDayScheduler;
import nexcore.scheduler.core.IScheduleCalendar;
import nexcore.scheduler.core.internal.Activator;
import nexcore.scheduler.core.internal.CalendarUtil;
import nexcore.scheduler.core.internal.DailyActivator;
import nexcore.scheduler.core.internal.JobDefinitionManager;
import nexcore.scheduler.core.internal.JobDefinitionStgManager;
import nexcore.scheduler.core.internal.JobInstanceIdMap;
import nexcore.scheduler.core.internal.JobInstanceManager;
import nexcore.scheduler.core.internal.JobRunResultProcessor;
import nexcore.scheduler.core.internal.JobStarter;
import nexcore.scheduler.core.internal.LicenseManager;
import nexcore.scheduler.core.internal.ParallelRunningCounter;
import nexcore.scheduler.core.internal.RepeatManager;
import nexcore.scheduler.core.internal.TimeScheduler;
import nexcore.scheduler.core.internal.TriggerProcessor;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.AgentInfo;
import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobDefinitionStg;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.JobLogFileLocation;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.entity.JobType;
import nexcore.scheduler.entity.ParallelGroup;
import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.entity.User;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.ioc.BeanRegistry;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.monitor.internal.AgentInfoManager;
import nexcore.scheduler.monitor.internal.UserManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.ByteArray;
import nexcore.scheduler.util.SchedulerUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 배치 컨트롤러 Main 클래스 </li>
 * <li>작성일 : 2010. 4. 22.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ControllerMain {
	private DupStartChecker           dupStartChecker;
	private JobDefinitionManager      jobDefinitionManager;
	private JobDefinitionStgManager   jobDefinitionStgManager;
	private JobInstanceManager        jobInstanceManager;
	private JobExecutionManager       jobExecutionManager;
	private IJobExecutionIdMaker      jobExecutionIdMaker;     // Id 만드는 로직.
	private JobRunResultProcessor     jobRunResultProcessor;   // callBackJobRunResult 처리를 위함.
	private RunningJobStateMonitor    runningJobStateMonitor;
	private ParameterManager          parameterManager;        // 파라미터 해석을 위한놈
	private AgentInfoManager          agentInfoManager;
	private LicenseManager            licenseManager;
	private Activator                 activator;
	private JobStarter                jobStarter;
	private TimeScheduler             timeScheduler;
	private DailyActivator            dailyActivator;
	private IScheduleCalendar	      scheduleCalendar;
	private IDayScheduler             dayScheduler;
	private IJobInfoValidator         jobInfoValidator;
	private JobTypeManager            jobTypeManager;
	private CustomConfig              customConfig;
	private IJobRunPreProcessor       jobRunPreProcessor;
	private ParallelRunningCounter    parallelRunningCounter;
	private UserManager               userManager;
	private JobInstanceIdMap          jobInstanceIdMap;
	private RepeatManager             repeatManager;
	private IPeerClient               peerClient;
	private SqlMapClient              sqlMapClient;
	
	private Log                       log;
	
	public void init() {
		log = LogManager.getSchedulerLog();
		Connection conn = null;
		try {
			conn = sqlMapClient.getDataSource().getConnection();
			DatabaseMetaData dbmd = conn.getMetaData();
			Util.logInfoConsole("Main DBMS : "+dbmd.getURL());
		}catch(Exception e) {
			throw Util.toRuntimeException(e);
		}finally {
			try {conn.close(); }catch(Exception ignore) {}
		}
		
		boolean systemIdEqual = false;
		try {
			systemIdEqual = Util.equalsIgnoreNull(Util.getSystemId(), peerClient.getSystemId());
		}catch(Exception ignore) { // peer 가 아직 미실행중이라면 에러 무시한다. peer 의 start 시에 에러가 체크될 것이다
		}

		if (systemIdEqual) {
			throw new SchedulerException("main.peer.same.systemid");
		}
		
		Util.logServerInitConsole("ControllerMain");
		Util.logServerInitConsole("JobInfoValidator", String.valueOf(jobInfoValidator));
		
	}
	
	public void destroy() {
		Util.logInfo(log, MSG.get("main.shutdown.stop.module", "TimeScheduler")); // {0} 를 정지합니다.
		do {
			timeScheduler.destroy();
			Util.sleep(1000);
		}while(timeScheduler.isAlive());

		Util.logInfo(log, MSG.get("main.shutdown.stop.module", "JobRunResultProcessor")); // {0} 를 정지합니다.
		jobRunResultProcessor.setClosed(true);

		Util.logInfo(log, MSG.get("main.shutdown.stop.module", "CommandQueue")); // {0} 를 정지합니다.
		jobStarter.setQueueClosed(true);
		do {
			Util.sleep(1000);
		}while(jobStarter.getDecisionQueueSize() > 0 || jobStarter.getRunQueueSize() > 0);

//		Util.logInfo(log, MSG.get("main.shutdown.stop.module", "DailyActivator")); // {0} 를 정지합니다.
//		do {
//			dailyActivator.destroy();
//			Util.sleep(1000);
//		}while(dailyActivator.isAlive());
	}

	public DupStartChecker getDupStartChecker() {
		return dupStartChecker;
	}

	public void setDupStartChecker(DupStartChecker dupStartChecker) {
		this.dupStartChecker = dupStartChecker;
	}

	public JobDefinitionManager getJobDefinitionManager() {
		return jobDefinitionManager;
	}

	public void setJobDefinitionManager(JobDefinitionManager jobDefinitionManager) {
		this.jobDefinitionManager = jobDefinitionManager;
	}

	public JobDefinitionStgManager getJobDefinitionStgManager() {
		return jobDefinitionStgManager;
	}

	public void setJobDefinitionStgManager(JobDefinitionStgManager jobDefinitionStgManager) {
		this.jobDefinitionStgManager = jobDefinitionStgManager;
	}

	public JobInstanceManager getJobInstanceManager() {
		return jobInstanceManager;
	}

	public void setJobInstanceManager(JobInstanceManager jobInstanceManager) {
		this.jobInstanceManager = jobInstanceManager;
	}

	public JobExecutionManager getJobExecutionManager() {
		return jobExecutionManager;
	}

	public void setJobExecutionManager(JobExecutionManager jobExecutionManager) {
		this.jobExecutionManager = jobExecutionManager;
	}

	public IJobExecutionIdMaker getJobExecutionIdMaker() {
		return jobExecutionIdMaker;
	}

	public void setJobExecutionIdMaker(IJobExecutionIdMaker jobExecutionIdMaker) {
		this.jobExecutionIdMaker = jobExecutionIdMaker;
	}

	public JobRunResultProcessor getJobRunResultProcessor() {
		return jobRunResultProcessor;
	}

	public void setJobRunResultProcessor(JobRunResultProcessor jobRunResultProcessor) {
		this.jobRunResultProcessor = jobRunResultProcessor;
	}

	public RunningJobStateMonitor getRunningJobStateMonitor() {
		return runningJobStateMonitor;
	}

	public void setRunningJobStateMonitor(RunningJobStateMonitor runningJobStateMonitor) {
		this.runningJobStateMonitor = runningJobStateMonitor;
	}

	public ParameterManager getParameterManager() {
		return parameterManager;
	}

	public void setParameterManager(ParameterManager parameterManager) {
		this.parameterManager = parameterManager;
	}

	public AgentInfoManager getAgentInfoManager() {
		return agentInfoManager;
	}

	public void setAgentInfoManager(AgentInfoManager agentManager) {
		this.agentInfoManager = agentManager;
	}

	public LicenseManager getLicenseManager() {
		return licenseManager;
	}

	public void setLicenseManager(LicenseManager licenseManager) {
		this.licenseManager = licenseManager;
	}

	public Activator getActivator() {
		return activator;
	}

	public void setActivator(Activator activator) {
		this.activator = activator;
	}

	public JobStarter getJobStarter() {
		return jobStarter;
	}

	public void setJobStarter(JobStarter jobStarter) {
		this.jobStarter = jobStarter;
	}

	public TimeScheduler getTimeScheduler() {
		return timeScheduler;
	}

	public void setTimeScheduler(TimeScheduler timeScheduler) {
		this.timeScheduler = timeScheduler;
	}

	public DailyActivator getDailyActivator() {
		return dailyActivator;
	}

	public void setDailyActivator(DailyActivator dailyActivator) {
		this.dailyActivator = dailyActivator;
	}

	public IScheduleCalendar getScheduleCalendar() {
		return scheduleCalendar;
	}

	public void setScheduleCalendar(IScheduleCalendar scheduleCalendar) {
		this.scheduleCalendar = scheduleCalendar;
	}

	public IJobInfoValidator getJobInfoValidator() {
		return jobInfoValidator;
	}

	public void setJobInfoValidator(IJobInfoValidator jobInfoValidator) {
		this.jobInfoValidator = jobInfoValidator;
	}

	public JobTypeManager getJobTypeManager() {
		return jobTypeManager;
	}

	public void setJobTypeManager(JobTypeManager jobTypeManager) {
		this.jobTypeManager = jobTypeManager;
	}

	public CustomConfig getCustomConfig() {
		return customConfig;
	}

	public void setCustomConfig(CustomConfig customConfig) {
		this.customConfig = customConfig;
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public IDayScheduler getDayScheduler() {
		return dayScheduler;
	}

	public void setDayScheduler(IDayScheduler dayScheduler) {
		this.dayScheduler = dayScheduler;
	}
	
	public IJobRunPreProcessor getJobRunPreProcessor() {
		return jobRunPreProcessor;
	}

	public void setJobRunPreProcessor(IJobRunPreProcessor jobRunPreProcessor) {
		this.jobRunPreProcessor = jobRunPreProcessor;
	}

	public ParallelRunningCounter getParallelRunningCounter() {
		return parallelRunningCounter;
	}

	public void setParallelRunningCounter(ParallelRunningCounter parallelRunningCounter) {
		this.parallelRunningCounter = parallelRunningCounter;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public JobInstanceIdMap getJobInstanceIdMap() {
		return jobInstanceIdMap;
	}
	
	public void setJobInstanceIdMap(JobInstanceIdMap jobInstanceIdMap) {
		this.jobInstanceIdMap = jobInstanceIdMap;
	}
	
	public IPeerClient getPeerClient() {
		return peerClient;
	}

	public void setPeerClient(IPeerClient peerClient) {
		this.peerClient = peerClient;
	}

	public RepeatManager getRepeatManager() {
		return repeatManager;
	}
	
	public void setRepeatManager(RepeatManager repeatManager) {
		this.repeatManager = repeatManager;
	}
	
	
	
	// ============================================================================================================
    // =====       	
	// ============================================================================================================
	
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
	
	public String getAgentNodeId(String jobExecutionId) {
		try {
			return jobExecutionManager.getJobExecutionAgentNode(jobExecutionId);
		}catch(Exception e) {
			throw new SchedulerException("main.job.dbselect.error", e, jobExecutionId);
		}
	}
	
	public IAgentClient getAgentClient(String agentSystemId) {
		return agentInfoManager.getAgentClient(agentSystemId);
	}

	public AgentInfo getAgentInfo(String agentSystemId) throws SQLException {
		return agentInfoManager.getAgentInfo(agentSystemId);
	}

	public int getJobExecutionState(String jobExecutionId) {
		try {
			return jobExecutionManager.getJobExecutionState(jobExecutionId);
		}catch(Exception e) {
			throw new SchedulerException("main.job.dbselect.error", e, jobExecutionId); // {0} 의 Job 정보 조회 중 에러가 발생하였습니다 
		}
	}
	
	/**
	 * Agent 로 부터 JobExecution Thread stack trace를 가져옴.
	 * @param jobInstanceId
	 */
	public Map getJobExecutionThreadStackTrace(String jobInstanceId) {
		// 현재 running 중인 JobExe 만 가능한 기능이므로, runningJobStateMonitor 에서 조회한다.
		JobExecution jobexe = runningJobStateMonitor.getRunningJobExecutionByJobInsId(jobInstanceId);
		
		if (jobexe == null) {
			throw new SchedulerException("main.jobexe.get.threadstack.null", jobInstanceId); // {0} 은 이미 종료되어 스레드 스택을 조회할 수 없습니다  
		}
		
		if (JobType.JOB_TYPE_DUMMY.equals(jobexe.getJobType()) || 
			JobType.JOB_TYPE_SLEEP.equals(jobexe.getJobType()) ||
			JobType.JOB_TYPE_FILEWATCH.equals(jobexe.getJobType())) {
			
			throw new SchedulerException("main.jobexe.threadstack.wrong.type", jobexe.getJobType()); // {0} 타입의 Job 은 스레드 스택을 조회할 수 없습니다
		}
		
		IAgentClient agent = getAgentClient(jobexe.getAgentNode());
		Map map = agent.getJobExecutionThreadStackTrace(jobexe.getJobExecutionId());

		if (map == null) {
			// 이미 종료됐거나, peer의 internal 에서 수행중이거나
			try {
				if (agentInfoManager.isInternalAgent(jobexe.getAgentNode())) {
					map = peerClient.getInternalJobExecutionThreadStackTrace(jobexe.getAgentNode(), jobexe.getJobExecutionId());
					// map 이 null 이라도 별 수 없이 null 리턴한다.
				}
			}catch(Exception e) {
				Util.logError(log, String.format("getJobExecutionThreadStackTrace() peer operation fail. (%s, %s)", jobexe.getAgentNode(), jobexe.getJobExecutionId()), e);
			}
		}
		return map;
	}
	
	/**
	 * 온디맨드 배치 실행함.
	 * @param jobId
	 * @param inParam
	 * @param callerId
	 * @param callerIp
	 * @return Job Execution Id 리턴
	 */
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp, byte[] onlineCtxData) {
		SchedulerUtil.checkStartedWithNoRun(); // startedWithNorun 모드인지 체크함.

		try {
			// JobDefinition 읽기. 파라미터, 선행조건 포함됨.
			JobDefinition jobdef = jobDefinitionManager.getJobDefinitionDeep(jobId);
	
			// 1. activation 조건 
			//  -- 선행조건 없음
			//  -- 시간조건 없음
			//  -- Repeat   없음
			//  -- parallel 없음
			//  -- confirm  없음
			//  -- trigger  있음
			//  -- 파라미터 있음
			//  -- 상태는 HOLD
			
			// 선행조건 무시
			jobdef.setPreJobConditions(new ArrayList());
	
			// 시간조건 무시
			jobdef.setTimeFrom(null);
			jobdef.setTimeUntil(null);
			
			// Repeat 조건 무시
			jobdef.setRepeatYN("N");
			jobdef.setRepeatIntvalGb(null);
			jobdef.setRepeatIntval(0);
			jobdef.setRepeatIfError(null);
			
			// parallel 조건 무시
			jobdef.setParallelGroup(null);
			
			// confirm 무시
			jobdef.setConfirmNeedYN("N");
			
			// inParam 은 설정. 기본 등록 정보 이외에 추가 inParam 로 섞는다.
			jobdef.setInParameters(inParam);
			
			// 처리일자
			String procDate = Util.nvl((String)inParam.get("PROC_DATE"), Util.getCurrentYYYYMMDD());

			// WHO
			// id = "OnDemand:"+onlineCtx.getUserInfo().getLoginId();  // 온디맨드호출과 OP를 구분.
            // ip = onlineCtx.getUserInfo().getIp();
			
			// JobInstance 생성 - activation... LOCK 상태로 하지 않으면 이 시점에 바로 실행된다.  
			JobInstance jobins = activator.activateAndLock(jobdef, procDate, null, new AdminAuth(callerId, callerIp));
			
			// JobExecution 생성
			JobExecution je = new JobExecution();
			je.setJobId(            jobins.getJobId());
			je.setJobInstanceId(    jobins.getJobInstanceId());
			je.setProcDate(         jobins.getProcDate());
			je.setBaseDate(         jobins.getBaseDate());
			je.setRunCount(         jobins.getRunCount()+1);
			je.setJobExecutionId(   jobExecutionIdMaker.makeJobExecutionId(jobins.getJobInstanceId()));
//			je.setAgentNode(        jobins.getAgentNode()); // 에이전트 이중화를 위해 여기서 에이전트 세팅을 하지않고 orderJobExecutionToAgent 안에서 한다.
			je.setComponentName(    jobins.getComponentName());
			je.setJobType(          jobins.getJobType());
			je.setInParameters(     jobins.getInParameters());
			je.setLogLevel(         jobins.getLogLevel());
			je.setOperatorType(     "OND");
			je.setOperatorId(       callerId);
			je.setOperatorIp(       callerIp);
			je.setOptionalData(     onlineCtxData);
			je.setOnDemand(         true);
			je.setDescription(      jobins.getDescription());
			je.setJobGroupId(       jobins.getJobGroupId());

			jobins.setLastJobExeId( je.getJobExecutionId());
			
			// Job 실행
			try {
				// ondemand 실행에서는 병렬제한그룹 체크를 하지 않으므로 , parallelgroup 을 null 로 준다. 2012-12-13
				if (!jobInstanceManager.setJobStateForStart(
						jobins.getJobInstanceId(), 
						System.currentTimeMillis(), 
						MSG.get("main.ondemand.invoked.state", callerId, callerIp),
						jobins.getJobState(), null, 0, jobins.getLastModifyTime())) {
					throw new SchedulerException("main.jobctl.state.inconsistent.error", callerId, jobins.getJobInstanceId()); // [{0}]에서 인스턴스({1}) 컨트롤 중 상태 불일치 현상이 발생하였습니다
				}
			} catch (SQLException e) {
				throw new SchedulerException("main.jobins.change.jobstate.error", e, jobins.getJobInstanceId()); // {0}의 Job 인스턴스 상태 변경 중 에러가 발생하였습니다
			}
			Util.logInfo(log, MSG.get("main.ondemand.invoked", jobId, callerId, callerIp)); // {1}/{2}에서 온디맨드 배치({0})를 요청하여 기동합니다
			orderJobExecutionToAgent(jobins, je);
			return je.getJobExecutionId();
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.ondemand.error", e, jobId, callerId, callerIp); // {1}/{2}에서 요청한 온디멘드 배치({0})를 기동 하는 중 에러가 발생하였습니다
		}
	}
	
	/**
	 * 온디맨드 배치 실행함.
	 * IOnlineContext 에 종속안되는 버전
	 * @param jobId
	 * @param inParam
	 * @param callerId
	 * @param callerIp
	 * @return Job Execution Id 리턴
	 */
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp) {
		return invokeOnDemandJob(jobId, inParam, callerId, callerIp, null);
	}

	public int getJobReturnCode(String jobExecutionId) {
		try {
			return jobExecutionManager.getReturnCode(jobExecutionId);
		}catch(Exception e) {
			throw new SchedulerException("main.job.dbselect.error", e, jobExecutionId); // {0} 의 Job 정보 조회 중 에러가 발생하였습니다 
		}
	}
	
	public Properties getJobReturnValue(String jobExecutionId) {
		try {
			return jobExecutionManager.getReturnValues(jobExecutionId);
		}catch(Exception e) {
			throw new SchedulerException("main.job.dbselect.error", e, jobExecutionId); // {0} 의 Job 정보 조회 중 에러가 발생하였습니다 
		}
	}

	/**
	 * 현재 running 상태인 모든 JobExecution 리스트 조회.
	 * @return List of all running JobExecutions.
	 */
	public Collection<JobExecution> getRunningJobExecutions() {
		return runningJobStateMonitor.getRunningJobExecutions();
	}
	
	private boolean isDummyJob(JobExecution jobexe) {
		return JobType.JOB_TYPE_DUMMY.equals(jobexe.getJobType());
	}
	
	private boolean isFileWatchJob(JobExecution jobexe) {
		return JobType.JOB_TYPE_FILEWATCH.equals(jobexe.getJobType());
	}
	
	/**
	 * 파일감시 Job 일 경우 파라미터에 POOLING_TIME 값이 설정되어있지 않을 경우 TimeUntil 값을 계산하여 POLLING_TIME 으로 넣어줌
	 *  
	 * @param jobexe
	 * @param timeUntil
	 */
	private void setupFileWatchJobPollingTime(JobExecution jobexe, String timeUntil) {
        // 파라미터 중에 POLLING_TIME 값이 없을 경우 Time until 값을 이용하여 POLLING_TIME 을 대신함
	    int until = Integer.parseInt(timeUntil);
	    if (until >= 2400) {        // until 이 익일까지인 경우
	        until -= 2400;        // 다음날 시각 찾기
	        int untilHH = until/100;
	        int untilMM = until - ((int)(until/100))*100;
	        
	        Calendar current = Calendar.getInstance();
	        current.add(Calendar.DATE, 1);                // 다음날
	        current.set(Calendar.HOUR_OF_DAY, untilHH);   // HH 
	        current.set(Calendar.MINUTE,      untilMM);   // MM
	        
	        long pollingTimeMs = current.getTime().getTime() - System.currentTimeMillis();
	        jobexe.getInParameters().put("POLLING_TIME", String.valueOf( Math.max(0, pollingTimeMs/1000/60 )));
	    }else {     // until 이 당일인 경우
	        int untilHH = until/100;
	        int untilMM = until - ((int)(until/100))*100;
	        
	        Calendar current = Calendar.getInstance();
	        current.set(Calendar.HOUR_OF_DAY, untilHH);   // HH 
	        current.set(Calendar.MINUTE,      untilMM);   // MM
	        
	        long pollingTimeMs = current.getTime().getTime() - System.currentTimeMillis();
	        jobexe.getInParameters().put("POLLING_TIME", String.valueOf( Math.max(0, pollingTimeMs/1000/60 )));
	    }
	}
	
	/**
	 * Agent 에게 Job 실행을 명령함.
	 * @param jobexe
	 */
	public void orderJobExecutionToAgent(JobInstance jobins, JobExecution jobexe) {
		SchedulerUtil.checkStartedWithNoRun(); // startedWithNorun 모드인지 체크함.

		IAgentClient agentclient = null;
		Throwable    error       = null;
		
		String       masterAgentNodeId = jobins.getAgentNodeMaster();
		String       slaveAgentNodeId  = jobins.getAgentNodeSlave();
		
		// ▶▶▶▶▶▶▶▶ 준비
		try {
		    // 주 에이전트 먼저 가능한 상태인지 확인한다.
			if (agentInfoManager.isInUseAgent(masterAgentNodeId)) {
				agentclient = getAgentClient(masterAgentNodeId);
				if ("OK".equals(agentclient.isAlive()) && !agentclient.isClosed()) {
					jobexe.setAgentNode(masterAgentNodeId);
					Util.logInfo(log, MSG.get("main.job.prepare.to.launch", jobexe.getJobInstanceId(), jobexe.getJobExecutionId(), masterAgentNodeId+"(master)")); // {0}/{1} 를 {2} 서버에 실행 하도록 준비합니다
				}
			}
			
			if (Util.isBlank(jobexe.getAgentNode())) {
				// master 가 미사용이거나 다운되어있어서 위에서 setAgentNode() 가 안된 경우임.
				if (!Util.isBlank(slaveAgentNodeId) && agentInfoManager.isInUseAgent(slaveAgentNodeId)) {
					// 주 에이전트가 불가한 상태라면. 부 에이전트 체크해본다.
					Util.logInfo(log, masterAgentNodeId+"(master) fail. checking slave:"+slaveAgentNodeId);
					agentclient = getAgentClient(slaveAgentNodeId);
					if ("OK".equals(agentclient.isAlive()) && !agentclient.isClosed()) {
						jobexe.setAgentNode(slaveAgentNodeId);
						Util.logInfo(log, MSG.get("main.job.prepare.to.launch", jobexe.getJobInstanceId(), jobexe.getJobExecutionId(), slaveAgentNodeId+"(slave)")); // {0}/{1} 를 {2} 서버에 실행 하도록 준비합니다
					}
				}
			}

			if (Util.isBlank(jobexe.getAgentNode())) {
			    // MASTER, SLAVE 모두 불가상태..에러낸다. 상황에 맞게 적절한 에러메세지를 낸다.
                jobexe.setAgentNode(masterAgentNodeId);
                agentclient = getAgentClient(masterAgentNodeId);
                if (!"OK".equals(agentclient.isAlive())) {
                    throw new SchedulerException("main.runcheck.agentdown", masterAgentNodeId);  // 에이전트({0})가 다운 상태입니다
                }else if (agentclient.isClosed()) {
                    throw new SchedulerException("main.runcheck.agentclosed", masterAgentNodeId);  // 에이전트({0})가 차단 상태입니다 
                }else if (!agentInfoManager.isInUseAgent(masterAgentNodeId)) {
                    throw new SchedulerException("main.runcheck.agentnotinuse", masterAgentNodeId);  // 에이전트({0})가 미사용 상태입니다 
                }else {
                    throw new SchedulerException("com.error.occurred.while", "Agent Check");  // {0} 중 에러가 발생하였습니다
                }
			}
			
			jobexe.setStartTime(System.currentTimeMillis());
			
			parameterManager.setupJobParameter(jobexe, agentclient.getSystemProperties(), agentclient.getSystemEnv());
			
			if (isFileWatchJob(jobexe) && Util.isBlank(jobexe.getInParameters().get("POLLING_TIME")) && !Util.isBlank(jobins.getTimeUntil())) {
			    setupFileWatchJobPollingTime(jobexe, jobins.getTimeUntil());
			}			
		}catch(Throwable e) {
			// agent 에 장애가 발생하거나, 파라미터해석중에 에러발생하면, order를 할 수 없다.
			error = e;
			jobexe.setReturnCode(9); // TODO  에러코드 정리.
			jobexe.setErrorMsg(e.toString());
		} finally {
			// JOB_EXE 테이블에 등록
			try {
				// INS 테이블의 LAST_JOB_EXE_ID 에 해당하는 EXE 가 불일치가 발행하지 않도록 하기 위해 이렇게 트랜잭션 처리한다. 2012-11-30. redmine #20073 참조.
				sqlMapClient.startTransaction();
				jobexe.setState(JobExecution.STATE_INIT);
				jobExecutionManager.addJobExecution(jobexe);
				jobInstanceManager.setLastJobExeId(jobexe.getJobInstanceId(), jobexe.getJobExecutionId(), jobexe.getAgentNode());
				sqlMapClient.commitTransaction();
			}catch(Exception e2) {
				Util.logError(log, MSG.get("main.jobexe.crud.error", e2, 0, jobexe.getJobExecutionId())); // JobExe {0} 를 생성하는 중에 에러가 발생하였습니다. 
				error = error == null ? e2 : error; // 위에서 exception 발생시에는 그 e 가 우선이다. 
			}finally {
				try {
					sqlMapClient.endTransaction();
				}catch (Exception e3){
				}
			}
		}
		
		// ▶▶▶▶▶▶▶▶ 실행.
		try {
			if (error == null) {
				// 지정된 전처리기를 돌린다. 여기서 에러나면 아래 catch 에 걸리면서 End Fail 처리된다.
				if (jobRunPreProcessor != null) {
					jobRunPreProcessor.doPreProcess(jobins, jobexe);
				}
				
				if (!isDummyJob(jobexe)) { // DUMMY JOB 이 아닌 경우 에이전트에 실행 명령내림.
					// 준비과정에서 문제가 없을때만 그리고 dummy, sleep 이 아닐때만 start 함.
					/* 
					 * 2012-06-04. start 하기 전에 runningJobStateMonitor 에 먼저 add 한후 start 에러 나면 밑에 catch 에서 빼내도록 한다.
					 * 이렇게 하지 않으면 agentclient.start(jobexe); 하자 마자 agent 에서 에러가 나서 callbackJobEnd() 가 먼저 날라오면, 
					 * monitor 에 remove 가 먼저 일어나고 add 가 나중에 되어 계속 running 상태로 남게 되는 문제가 있다. 
					 */
					runningJobStateMonitor.addRunningJobExecution(jobexe);
					agentclient.start(jobexe);
					jobexe.setState(JobExecution.STATE_RUNNING);
					// DB 에 Job Exe 상태를 update 한다. RUNNING 상태인 것들만 RunningJobMonitor 에서 ghost 여부 체크한다.
					/*
					 * 2013.09.02 agentClient.start() 후에 아래 JobExe 의 상태 업데이트 이전에 먼저 callback 이 와서 callback 처리를 한 후에 아래 updateJobExecutionStateOnly 가 돌면
					 * JobExe 상태가 영원히 RUNNING 으로 남을 수 있다. 따라서 이전 상태가 INIT 일때만 아래 쿼리를 돌리도록 수정한다.
					 */
					jobExecutionManager.updateJobExecutionStateOnly(jobexe.getJobExecutionId(), JobExecution.STATE_RUNNING, JobExecution.STATE_INIT); 
					
					// 정상적으로 start 된 경우는 JobExecution 상태를 RUNNING 으로 변경
					Util.logInfo(log, MSG.get("main.job.launched", jobexe.getJobExecutionId(), jobexe.getAgentNode(), jobexe.getOperatorId(), jobexe.getOperatorIp())); // (START) {0} 이 {1} 서버에서 실행되었습니다 
				}
			}
		}catch(Throwable e) {
			error = e;
			jobexe.setReturnCode(9); // TODO 에러코드 정리.
			jobexe.setErrorMsg(e.toString());
			runningJobStateMonitor.removeRunningJobExecution(jobexe); // start() 중에 에러가 난 경우이므로 여기서 remove 한다.
		}

		//  ▶▶▶▶▶▶▶▶  에러처리. 준비과정, start 과정에서 에러 발생시 에러 처리 해주어야함
		if (error != null) {
			try {
				if (jobexe.getEndTime() == 0) {
					// start 못하고 에러난 경우는 endTime 이 0 이다.  
					jobexe.setEndTime(System.currentTimeMillis());
				}
				jobexe.setState(JobExecution.STATE_ENDED);
				jobRunResultProcessor.callBackJobEnd(jobexe);
			}catch(Throwable e2) { 
				Util.logError(log, MSG.get("main.job.launch.error.callback.fail", jobexe.getJobExecutionId()), e2); // {0} 의 기동 에러 콜백 처리 과정에서 에러가 발생하였습니다 
			}
			throw logAndMakeSchedulerException("main.job.launch.error", error, jobexe.getJobExecutionId()); // {0} 기동 중에 에러가 발생하였습니다.
		}else { 	
			// ▶▶▶▶▶▶▶▶ dummy job 일 경우 여기서 종료처리한다.
			if (isDummyJob(jobexe)) {
				jobexe.setEndTime(System.currentTimeMillis());
				jobexe.setState(JobExecution.STATE_ENDED);
				jobexe.setReturnCode(0);
				try {
					jobRunResultProcessor.callBackJobEnd(jobexe);
				}catch(Throwable e2) { 
					Util.logError(log, MSG.get("main.job.dummy.callback.fail", jobexe.getJobExecutionId()), e2); // {0} 더미 Job 콜백 처리 과정에서 에러가 발생하였습니다 
				}
			}
		}
	}

	/**
	 * JobDefinition 정보를 query 조건에 따라 검색하여 리턴함 
	 * @param queryCondition NBS_JOB_DEF 테이블을 select 할때 사용된 where 절을 입력함 이후의 조건을 입력함. 
	 *         예) "WHERE JOB_GROUP_ID LIKE '%XYZ%'
	 * @return List of JobDefinition 객체를 리턴함.
	 */
	public List<JobDefinition> getJobDefinitionListByQuery(String queryCondition, boolean deep) {
		try {
			if (deep) {
				return jobDefinitionManager.getJobDefinitionsDeepByQuery(queryCondition);
			}else {
				return jobDefinitionManager.getJobDefinitionsByQuery(queryCondition);
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryCondition); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * Dynamic 쿼리 방식으로 JobDefinition들 조회
	 * @param queryParamMap 검색 조건을 담고 있는 Map.
	 * {viewFilterId, jobIdLike, jobGroupIdLike, jobDescLike, jobType, preJobIdLike, triggerJobIdLike, agentId, ownerLike, authorizedJobGroupIdViewList} 
	 * @param deep (PreJob List, Param) 포함 여부
	 * @return
	 */
	public List<JobDefinition> getJobDefinitionListByDynamicQuery(Map queryParamMap, boolean deep) {
		try {
			if (deep) {
				return jobDefinitionManager.getJobDefinitionsDeepByDynamicQuery(queryParamMap);
			}else {
				return jobDefinitionManager.getJobDefinitionsByDynamicQuery(queryParamMap);
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryParamMap.toString()); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	public List<JobDefinition> getJobDefinitionListByJobIdList(List jobIdList, boolean deep) {
		try {
			if (deep) {
				return jobDefinitionManager.getJobDefinitionsDeepByJobIdList(jobIdList);
			}else {
				return jobDefinitionManager.getJobDefinitionsByJobIdList(jobIdList);
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, "JobIdList.size()="+jobIdList.size()); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * JobDefinition 정보중 원하는 컬럼만 조회한다.
	 * queryParamMap 에 "columnList" 키값으로 컬럼 리스트 지정함
	 * NO-DEEP 쿼리함.
	 * @param queryParamMap 
	 * @return
	 */
	public List<Map> getJobDefinitionListFreeColumn(Map queryParamMap) {
		try {
			return jobDefinitionManager.getJobDefinitionsFreeColumnByDynamicQuery(queryParamMap);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryParamMap.toString()); // Job 검색 중 에러가 발생하였습니다 
		}
	}
	
	public JobDefinition getJobDefinition(String jobId) {
		try {
			return jobDefinitionManager.getJobDefinitionDeep(jobId);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, jobId); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	public boolean existJobDefinition(String jobId) {
		try {
			return jobDefinitionManager.getJobDefinition(jobId) != null;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, jobId); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * 해당일이 activate 되도록 schedule 된 일인지 체크함
	 * @param jobdef
	 * @param yyyymmdd 체크하려는 일자
	 * @return
	 */
	public boolean isScheduledDay(JobDefinition jobdef, String yyyymmdd) {
		return dayScheduler.isScheduledDay(jobdef, CalendarUtil.convYYYYMMDDToCalendar(yyyymmdd));
	}
	
	/**
	 * JobInstance 정보를 query 조건에 따라 검색하여 리턴함 
	 * @param queryCondition NBS_JOB_INS 테이블을 select 할때 사용된 where 절을 입력함 이후의 조건을 입력함. 
	 *         예) "WHERE JOB_GROUP_ID LIKE '%XYZ%'
	 * @return List of JobInstance  객체를 리턴함
	 */
	public List<JobInstance> getJobInstanceListByQuery(String queryCondition, String orderBy, boolean deep) {
		try {
			if (deep) {
				return jobInstanceManager.getJobInstancesDeepByQuery(queryCondition, orderBy);
			}else {
				return jobInstanceManager.getJobInstancesByQuery(queryCondition, orderBy);
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryCondition); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * JobInstance 정보를 조건에 따라 검색하나 dynamic 방식으로 조회함
	 * @param queryParamMap
	 * @param deep
	 * @return
	 */
	public List<JobInstance> getJobInstanceListByDynamicQuery(Map queryParamMap, boolean deep) {
		try {
			if (deep) {
				return jobInstanceManager.getJobInstancesDeepByDynamicQuery(queryParamMap);
			}else {
				return jobInstanceManager.getJobInstancesByDynamicQuery(queryParamMap);
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryParamMap.toString()); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * JobInstance 정보를 조건에 따라 검색하나 dynamic 방식으로 조회함
	 * @param queryParamMap
	 * @param deep
	 * @param skip
	 * @param maxResult
	 * @return
	 */
	public List<JobInstance> getJobInstanceListByDynamicQuery(Map queryParamMap, boolean deep, int skip, int maxResult) {
		try {
			if (deep) {
				return jobInstanceManager.getJobInstancesDeepByDynamicQuery(queryParamMap, skip, maxResult);
			}else {
				return jobInstanceManager.getJobInstancesByDynamicQuery(queryParamMap, skip, maxResult);
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryParamMap.toString()); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * iBATIS 의 RowHandler 를 이용하여 대랑 JobInstance 조회를 함.
	 * 대량 조회는 기본적으로 no-deep 쿼리한다.
	 * @param queryParamMap
	 * @param rowHandler
	 */
	public void getJobInstanceListByDynamicQueryWithRowHandler(Map queryParamMap, Object rowHandler) {
		try {
			jobInstanceManager.getJobInstancesByDynamicQueryWithRowHandler(queryParamMap, rowHandler);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryParamMap.toString()); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * 필요한 컬럼만 조회한다.
	 * iBATIS 의 RowHandler 를 이용하여 대랑 JobInstance 조회를 함.
	 * 대량 조회는 기본적으로 no-deep 쿼리한다.
	 * @param queryParamMap
	 * @param rowHandler
	 */
	public void getJobInstanceListFreeColumnWithRowHandler(Map queryParamMap, Object rowHandler) {
		try {
			jobInstanceManager.getJobInstanceListFreeColumnWithRowHandler(queryParamMap, rowHandler);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryParamMap.toString()); // Job 검색 중 에러가 발생하였습니다 
		}
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
		try {
			return jobInstanceManager.getJobInstanceListFreeColumn(queryParamMap);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryParamMap.toString()); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * JobInstance 정보를 조건에 따라 검색한 결과 건수
	 * @param queryParamMap
	 * @return
	 */
	public int getJobInstanceCountByDynamicQuery(Map queryParamMap) {
		try {
			return jobInstanceManager.getJobInstanceCountByDynamicQuery(queryParamMap);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryParamMap.toString()); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * 파라미터, 선행 조건 포함 Instance deep 조회
	 * @param jobInstanceId
	 * @return
	 */
	public JobInstance getJobInstance(String jobInstanceId) {
		try {
			return jobInstanceManager.getJobInstanceDeep(jobInstanceId);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, jobInstanceId); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * 파라미터, 선행 조건 없이 Instance 자체만 조회함
	 * @param jobInstanceId
	 * @return
	 */
	public JobInstance getJobInstanceSimple(String jobInstanceId) {
		try {
			return jobInstanceManager.getJobInstance(jobInstanceId);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, jobInstanceId); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	public boolean existJobInstance(String jobInstanceId) {
		try {
			return jobInstanceManager.getJobInstanceWithoutError(jobInstanceId) != null;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, jobInstanceId); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	// ============================= JOB EXECUTION =========================================
	
	/**
	 * JobExecution 정보를 Job Instance Id 에 따라 검색함 
	 * @param queryCondition NBS_JOB_EXE 테이블을 select 할때 사용된 where 절을 입력함 이후의 조건을 입력함. 
	 *         예) "WHERE JOB_ID LIKE '%XYZ%'
	 * @return Deep select된 List of JobExecution 객체를 리턴함. (파라미터, 리턴값 포함됨)
	 */
	public List<JobExecution> getJobExecutionListByQuery(String queryCondition, boolean deep) {
		try {
			if (deep) {
				return jobExecutionManager.getJobExecutionsDeepByQuery(queryCondition);
			}else {
				return jobExecutionManager.getJobExecutionsByQuery(queryCondition);
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, queryCondition); // Job 검색 중 에러가 발생하였습니다 
		}
	}

	/**
	 * JobExecution 정보를 Job Instance Id 에 따라 검색함 
	 * @param jobInstanceId  
	 * @return Deep select된 List of JobExecution 객체를 리턴함. (파라미터, 리턴값 포함됨)
	 */
	public List<JobExecution> getJobExecutionListByJobInstanceId(String jobInstanceId, boolean deep) {
		try {
			if (deep) {
				return jobExecutionManager.getJobExecutionsDeepByQuery("WHERE JOB_EXECUTION_ID LIKE '"+jobInstanceId+"%'");
			}else {
				return jobExecutionManager.getJobExecutionsByQuery("WHERE JOB_EXECUTION_ID LIKE '"+jobInstanceId+"%'");
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, jobInstanceId); // Job 검색 중 에러가 발생하였습니다
		}
	}
	
	public JobExecution getJobExecutionDeep(String jobExecutionId) {
		try {
			return jobExecutionManager.getJobExecutionDeep(jobExecutionId);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, jobExecutionId); // Job 검색 중 에러가 발생하였습니다
		}
	}

	/**
	 * Agent 에 쌓인 로그 파일을 찾아 경로와 길이 정보를 가져온다.
	 * 에이전트 이중화 환경을 대비해서 최종 실행된 에이전트로 부터 가져온다. (since 3.9)
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getJobLogFileLocation(String jobInstanceId) {
		JobInstance  jobins         = null;
		IAgentClient agentClient    = null;
		try {
			jobins = jobInstanceManager.getJobInstance(jobInstanceId);
			if (jobins.getLastAgentNode() != null) {
			    agentClient = getAgentClient(jobins.getLastAgentNode()); 
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
		}

		if (jobins.getLastAgentNode() == null) {
		    throw new SchedulerException("main.jobins.notyetstarted", jobInstanceId); // {0} 는 아직 실행 전입니다.
		}
		
		try {
			JobLogFilenameInfo jlfi = new JobLogFilenameInfo(jobins);
			JobLogFileLocation retval = new JobLogFileLocation();
			
			String logFilename     = agentClient.getLogFilename(jlfi);
			long   logFileLength   = agentClient.getLogFileLength(logFilename);
			String logFileEncoding = agentClient.getJobLogFileEncoding();
			
			retval.setAgentId(      jobins.getLastAgentNode());
			retval.setPeerInternal( false);
			retval.setFilename(     logFilename);
			retval.setLength(       logFileLength);
			retval.setEncoding(     logFileEncoding);

			if (!agentClient.isFileExist(logFilename)) {
				// 로그 파일 생성 전이거나, Peer 의 internal 에서 실행된 경우.
				if (agentInfoManager.isInternalAgent(jobins.getLastAgentNode())) {
					try {
						String logFilename2   = peerClient.getInternalJobLogFilename(jobins.getLastAgentNode(), jlfi);
						long   logFileLength2 = peerClient.getInternalJobLogFileLength(jobins.getLastAgentNode(), logFilename2);
	
						if (peerClient.isFileExist(jobins.getLastAgentNode(), logFilename2)) {
							retval.setPeerInternal(true);
							retval.setFilename(    logFilename2);
							retval.setLength(      logFileLength2);
						}
					}catch(Exception e) {
						// 이 시점에 peer 가 down 상태이면 그냥 pass 한다.
					}
				}
			}
			return retval;
		}catch(Exception e) {
			throw new SchedulerException("main.agent.agentclient.connect.error", e, jobins.getLastAgentNode());
		}
	}
	
	/**
	 * Agent 에 쌓인 SUB 로그 파일을 찾아 경로와 길이 정보를 가져온다.
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getSubJobLogFileLocation(String jobInstanceId) {
		JobInstance  jobins         = null;
		IAgentClient agentClient    = null;
		String       lastRunAgentId = null;
		try {
		    jobins    = jobInstanceManager.getJobInstance(jobInstanceId);
		    
		    // JobInstance 의 agentNode 값에는 멀티정보가 들어있을 수 있으므로
		    // JobExecutionManager를 통해 최종 실행 agent id를 찾아온다.
		    lastRunAgentId = jobExecutionManager.getJobExecutionAgentNode(jobins.getLastJobExeId()); // 마지막으로 실행했던 agent id
		    if (lastRunAgentId != null) {
		        agentClient = getAgentClient(lastRunAgentId);
		    }
		}catch(Exception e) {
		    throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
		}
		
		if (lastRunAgentId == null) {
		    throw new SchedulerException("main.jobins.notyetstarted", jobInstanceId); // {0} 는 아직 실행 전입니다.
		}
		
		try {
			JobLogFilenameInfo jlfi = new JobLogFilenameInfo(jobins);
			JobLogFileLocation retval = new JobLogFileLocation();
			
			String logFilename   = agentClient.getSubLogFilename(jlfi);
			long   logFileLength = agentClient.getLogFileLength(logFilename);
			
			retval.setAgentId(      lastRunAgentId);
			retval.setPeerInternal( false);
			retval.setFilename(     logFilename);
			retval.setLength(       logFileLength);
			
			if (!agentClient.isFileExist(logFilename)) {
				// 로그 파일 생성 전이거나, Peer 의 internal 에서 실행된 경우.
				if (agentInfoManager.isInternalAgent(lastRunAgentId)) {
					try {
						String logFilename2   = peerClient.getInternalJobSubLogFilename(lastRunAgentId, jlfi);
						long   logFileLength2 = peerClient.getInternalJobLogFileLength(lastRunAgentId, logFilename2);
						
						if (peerClient.isFileExist(lastRunAgentId, logFilename2)) {
							retval.setPeerInternal(true);
							retval.setFilename(    logFilename2);
							retval.setLength(      logFileLength2);
						}
					}catch(Exception e) {
						// 이 시점에 peer 가 down 상태이면 그냥 pass 한다.
					}
				}
			}
			return retval;
		}catch(Exception e) {
			throw new SchedulerException("main.agent.agentclient.connect.error", e, lastRunAgentId);
		}
	}

	/**
	 * Agent 에 쌓인 stdout 로그 파일을 찾아 경로와 길이 정보를 가져온다. (쉘실행, CBATCH 타입인 경우 유효)
	 * @param jobInstanceId
	 * @return
	 */
	public JobLogFileLocation getStdoutJobLogFileLocation(String jobInstanceId) {
        JobInstance  jobins         = null;
        IAgentClient agentClient    = null;
        String       lastRunAgentId = null;
        try {
            jobins    = jobInstanceManager.getJobInstance(jobInstanceId);
            
            // JobInstance 의 agentNode 값에는 멀티정보가 들어있을 수 있으므로
            // JobExecutionManager를 통해 최종 실행 agent id를 찾아온다.
            lastRunAgentId = jobExecutionManager.getJobExecutionAgentNode(jobins.getLastJobExeId()); // 마지막으로 실행했던 agent id
            if (lastRunAgentId != null) {
                agentClient = getAgentClient(lastRunAgentId);
            }
        }catch(Exception e) {
            throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
        }
        
        if (lastRunAgentId == null) {
            throw new SchedulerException("main.jobins.notyetstarted", jobInstanceId); // {0} 는 아직 실행 전입니다.
        }
		
		try {
			JobLogFilenameInfo jlfi = new JobLogFilenameInfo(jobins);
			JobLogFileLocation retval = new JobLogFileLocation();
			
			String logFilename   = agentClient.getLogFilename(jlfi);
			// stdout 로그 파일은 뒤에 -stdout.log 가 붙는다.
			logFilename = logFilename.substring(0, logFilename.lastIndexOf("."))+"-stdout.log";  
			long   logFileLength = agentClient.getLogFileLength(logFilename);
			
			retval.setAgentId(      lastRunAgentId);
			retval.setPeerInternal( false);
			retval.setFilename(     logFilename);
			retval.setLength(       logFileLength);
			
			if (!agentClient.isFileExist(logFilename)) {
				// 로그 파일 생성 전이거나, Peer 의 internal 에서 실행된 경우.
				if (agentInfoManager.isInternalAgent(lastRunAgentId)) {
					try {
						String logFilename2   = peerClient.getInternalJobLogFilename(lastRunAgentId, jlfi);
						// stdout 로그 파일은 뒤에 -stdout.log 가 붙는다.
						logFilename2 = logFilename2.substring(0, logFilename2.lastIndexOf("."))+"-stdout.log";  
						long   logFileLength2 = peerClient.getInternalJobLogFileLength(lastRunAgentId, logFilename2);
						
						if (peerClient.isFileExist(lastRunAgentId, logFilename2)) {
							retval.setPeerInternal(true);
							retval.setFilename(    logFilename2);
							retval.setLength(      logFileLength2);
						}
					}catch(Exception e) {
						// 이 시점에 peer 가 down 상태이면 그냥 pass 한다.
					}
				}
			}
			return retval;
		}catch(Exception e) {
			throw new SchedulerException("main.agent.agentclient.connect.error", e, lastRunAgentId);
		}
	}
	
	/**
	 * Agent 의 Job 로그 파일을 읽어 리턴함
	 * @param jobInstanceId
	 * @param location
	 * @param offset
	 * @param length
	 * @return
	 */
	public ByteArray readLogFile(String jobInstanceId, JobLogFileLocation location, int offset, int length) {
		if (location.isPeerInternal()) {
			return peerClient.readInternalJobLogFile(location.getAgentId(), location.getFilename(), offset, length);
		}else {
			IAgentClient agentClient = null;
			try {
				agentClient = getAgentClient(location.getAgentId());
			}catch(Exception e) {
				throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
			}
			try {
				return agentClient.readLogFile(location.getFilename(), offset, length);
			}catch(Exception e) {
				throw new SchedulerException("main.agent.readlogfile.error", e, location.getFilename()); // Job 로그 파일 {0} 를 읽을 수 없습니다. 파일이 생성되지 않았거나 삭제됐을 수 있습니다
			}
		}
	}

	/**
	 * Agent 의 파일을 읽어 리턴함
	 * @param agentid
	 * @param filename
	 * @param offset
	 * @param length
	 * @return
	 */
	public ByteArray readAgentFile(String agentId, String filename, int offset, int length) {
		IAgentClient agentClient = null;
		try {
			agentClient = getAgentClient(agentId);
			return agentClient.readLogFile(filename, offset, length);
		}catch(Exception e) {
			throw new SchedulerException("main.agent.readlogfile.error", e, filename); // Job 로그 파일 {0} 를 읽을 수 없습니다. 파일이 생성되지 않았거나 삭제됐을 수 있습니다
		}
	}
	
	/** 
	 * 수동으로 하나의 Job을 activate 함.
	 * @param jobId
	 * @param procDate
	 * @param auth
	 * @return Job Instance Id
	 */
	public String activateJob(String jobId, String procDate, AdminAuth auth) {
		try {
			Util.checkDateYYYYMMDD(procDate);  //  날자 형식 체크.

			// JobDefinition 읽기. 파라미터, 선행조건 포함됨.
			JobDefinition jobdef = jobDefinitionManager.getJobDefinitionDeep(jobId);
			userManager.checkOperationPermission(jobdef.getJobGroupId(), jobId, "activateJob", auth); // 권한 체크.
			
			// JobInstance 생성 - activation.
			// 여기서는 LOCK 하지 않고 activate 시켜놓기만하고 scheduler 가 실행하도록 내버려준다.
			sqlMapClient.startTransaction();
			JobInstance jobins = activator.activate(jobdef, procDate, null, auth);
			sqlMapClient.commitTransaction();

			jobStarter.askToStart(jobins);
			
			Util.logInfo(log, MSG.get("main.jobctl.activate", auth, jobins.getJobInstanceId(), procDate)); // [{0}]에서 처리일({2}) 로 인스턴스({1})를 생성합니다
			return jobins.getJobInstanceId();
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.activate.error", e, auth, jobId); // [{0}]에서 {1}의 인스턴스를 생성하는 중에 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
	}
	
	/** 
	 * 수동으로 하나의 Job을 activate 함. activate 후에는 상태가 HOLD 상태가 됨.
	 * @param jobId
	 * @param procDate
	 * @param auth
	 * @return Job Instance Id
	 */
	public String activateAndLockJob(String jobId, String procDate, AdminAuth auth) {
		try {
			Util.checkDateYYYYMMDD(procDate);

			// JobDefinition 읽기. 파라미터, 선행조건 포함됨.
			JobDefinition jobdef = jobDefinitionManager.getJobDefinitionDeep(jobId);
			userManager.checkOperationPermission(jobdef.getJobGroupId(), jobId, "activateAndLockJob", auth); // 권한 체크.
			
			// JobInstance 생성 - activation.
			// 여기서는 LOCK 하지 않고 activate 시켜놓기만하고 scheduler 가 실행하도록 내버려준다.
			sqlMapClient.startTransaction();
			JobInstance jobins = activator.activateAndLock(jobdef, procDate, null, auth);
			sqlMapClient.commitTransaction();
			
			Util.logInfo(log, MSG.get("main.jobctl.activatelock", auth, jobins.getJobInstanceId(), procDate)); // [{0}]에서 처리일({2}) 로 인스턴스({1})를 생성 후 Lock 합니다
			return jobins.getJobInstanceId();
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.activatelock.error", e, auth, jobId); // [{0}]에서 {1}의 인스턴스를 생성하는 중에 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
	}

	/**
	 * Job Instance 를 LOCK 함. LOCK 상태에서는 STATE 변경이 안됨.
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public boolean lockJob(String jobInstanceId, AdminAuth auth) {
		try {
			userManager.checkOperationPermission(
				jobInstanceManager.getJobGroupId(jobInstanceId), 
				jobInstanceIdMap.getJobId(jobInstanceId), "lock", auth); // 권한 체크.
			Util.logInfo(log, MSG.get("main.jobctl.lock", auth, jobInstanceId)); // [{0}]에서 인스턴스({1})를 Lock 합니다
			if (jobInstanceManager.lockJob(jobInstanceId, auth.getOperatorId(), auth.getOperatorIp())) {
				repeatManager.removeScheduledTask(jobInstanceId); // Repeat Timer 에서도 삭제한다.
				return true;
			}else {
				return false;
			}
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.lock.error", e, auth, jobInstanceId); // [{0}]에서 {1} 인스턴스를 Lock 하는 중에 에러가 발생하였습니다
		}
	}

	/**
	 * Job Instance 를 UNLOCK 함. LOCK 상태에서는 STATE 변경이 안됨.
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public boolean unlockJob(String jobInstanceId, AdminAuth auth) {
		try {
			userManager.checkOperationPermission(
				jobInstanceManager.getJobGroupId(jobInstanceId), 
				jobInstanceIdMap.getJobId(jobInstanceId), "unlock", auth); // 권한 체크.
			Util.logInfo(log, MSG.get("main.jobctl.unlock", auth, jobInstanceId)); // [{0}]에서 인스턴스({1})를 Unlock 합니다
			boolean b = jobInstanceManager.unlockJob(jobInstanceId, auth.getOperatorId());
			jobStarter.askToStart(jobInstanceId);
			return b;
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.unlock.error", e, auth, jobInstanceId); // [{0}]에서 {1} 인스턴스를 Unlock 하는 중에 에러가 발생하였습니다
		}
	}
	
	/**
	 * 상태에 상관없이 (이미 RUNNING, SUSPENDED 상태인것은 제외), 조건에 상관없이 해당 Job 을 즉시 실행시킴. 
	 * 이것을 실행시키면 trigger 도 돌아가고, 후행 Job 에도 영향을 미친다.
	 * @param jobInstanceId
	 * @param auth
	 * @return JobExecutionId or null if fail
	 */
	public String forceRunJob(String jobInstanceId, AdminAuth auth) {
		Util.logInfo(log, MSG.get("main.jobctl.forcerun", auth, jobInstanceId)); // [{0}]에서 인스턴스({1})를 강제실행 (forcerun) 합니다
		
		SchedulerUtil.checkStartedWithNoRun(); // startedWithNorun 모드인지 체크함.

		JobInstance jobins = null;
		try {
			jobins = jobInstanceManager.getJobInstanceDeep(jobInstanceId);
			
			// 운영 권한 체크
			userManager.checkOperationPermission(jobins.getJobGroupId(), jobInstanceIdMap.getJobId(jobInstanceId), "forceRunJob", auth); // 권한 체크.
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
		}
		
		if (JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {
			throw logAndMakeSchedulerException("main.jobctl.state.error", (Throwable)null, jobInstanceId, jobins.getJobStateText(), "Forcerun"); // {1} 상태에서는 {2}를 할 수 없습니다 ({0})
		}

		// JobExecution 생성
		JobExecution je = new JobExecution();
		je.setJobId(            jobins.getJobId());
		je.setJobInstanceId(    jobins.getJobInstanceId());
		je.setProcDate(         jobins.getProcDate());
		je.setBaseDate(         jobins.getBaseDate());
		je.setRunCount(         jobins.getRunCount()+1);
		je.setJobExecutionId(   jobExecutionIdMaker.makeJobExecutionId(jobins.getJobInstanceId()));
//		je.setAgentNode(        jobins.getAgentNode());  // 에이전트 이중화를 위해 여기서 에이전트 세팅을 하지않고 orderJobExecutionToAgent 안에서 한다.
		je.setComponentName(    jobins.getComponentName());
		je.setJobType(          jobins.getJobType());
		je.setInParameters(     jobins.getInParameters());
		je.setLogLevel(         jobins.getLogLevel());
		je.setOperatorType(     "USR");
		je.setOperatorId(       auth.getOperatorId());
		je.setOperatorIp(       auth.getOperatorIp());
		je.setDescription(      jobins.getDescription());
		je.setJobGroupId(       jobins.getJobGroupId());
		
		jobins.setLastJobExeId( je.getJobExecutionId());
		
		// Job 실행
		try {
			// force run 에서는 병렬제한그룹 체크를 하지 않으므로 , parallelgroup 을 null 로 준다. 2012-12-13
			if (!jobInstanceManager.setJobStateForStart(jobins.getJobInstanceId(), System.currentTimeMillis(), 
					MSG.get("main.jobctl.state.forcerun", auth.toString()), jobins.getJobState(), null, 0, jobins.getLastModifyTime())) {
				throw new SchedulerException("main.jobctl.state.inconsistent.error", auth, jobInstanceId); // [{0}]에서 인스턴스({1}) 컨트롤 중 상태 불일치 현상이 발생하였습니다
			}
		} catch (Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.forcerun.error", e, auth, jobInstanceId); // [{0}]에서 {1}를 강제실행 하는 중에 에러가 발생하였습니다
		}

		orderJobExecutionToAgent(jobins, je);
		return je.getJobExecutionId();
	}
	
	/**
	 * END 상태의 JOB 을 WAIT 상태로 변경
	 * @param jobInstanceId
	 * @param auth
	 */
	public void reRunJob(String jobInstanceId, AdminAuth auth) {

		JobInstance jobins   = null;
		try {
			jobins   = jobInstanceManager.getJobInstance(jobInstanceId);
			userManager.checkOperationPermission(jobins.getJobGroupId(), jobInstanceIdMap.getJobId(jobInstanceId), "reRunJob", auth); // 권한 체크.
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
		}
		
		if (JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {
			throw logAndMakeSchedulerException("main.jobctl.state.error", (Throwable)null, jobInstanceId, jobins.getJobStateText(), "Rerun"); // {1} 상태에서는 {2}를 할 수 없습니다 ({0})
		}
		String beforeJobState = jobins.getJobState();
		try {
			jobins.setJobState(JobInstance.JOB_STATE_WAIT);
			jobins.setJobStateReason(MSG.get("main.jobctl.state.rerun", auth));
			if (jobInstanceManager.setJobStateWithCheck(jobins, beforeJobState)) {
				jobStarter.askToStart(jobins);
				Util.logInfo(log, MSG.get("main.jobctl.rerun", auth, jobInstanceId)); // [{0}]에서 인스턴스({1})를 다시실행 (rerun) 합니다
			}else {
				throw logAndMakeSchedulerException("main.jobctl.state.inconsistent.error", auth, (Throwable)null, jobInstanceId); // [{0}]에서 인스턴스({1}) 컨트롤 중 상태 불일치 현상이 발생하였습니다
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.rerun.error", e, auth, jobInstanceId); // [{0}]에서 {1}를 다시실행 하는 중에 에러가 발생하였습니다
		}
	}
	
	/**
	 * RUNNING SUSPENDED 상태의 Job 을 stop 시킴
	 * @param jobInstanceId
	 * @param auth
	 */
	public void stopJob(String jobInstanceId, AdminAuth auth) {
		JobInstance jobins   = null;
		try {
			jobins   = jobInstanceManager.getJobInstance(jobInstanceId);
			userManager.checkOperationPermission(jobins.getJobGroupId(), jobInstanceIdMap.getJobId(jobInstanceId), "stopJob", auth); // 권한 체크.
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
		}
		
		if (!JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) &&
			!JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {
			throw logAndMakeSchedulerException("main.jobctl.state.error", (Throwable)null, jobInstanceId, jobins.getJobStateText(), "Stop"); // {1} 상태에서는 {2}를 할 수 없습니다 ({0})
		}

		String lastRunAgentId = null; // 현재 실행중인 Agent ID. 이중화를 고려한 조치. since 3.9
		try {
		    // JobInstance 의 agentNode 값에는 멀티정보가 들어있을 수 있으므로
		    // JobExecutionManager를 통해 최종 실행 agent id를 찾아온다.
            lastRunAgentId = jobExecutionManager.getJobExecutionAgentNode(jobins.getLastJobExeId()); // 마지막으로 실행했던 agent id
			IAgentClient agentClient = getAgentClient(lastRunAgentId);
			int jobExeState = agentClient.getJobExeState(jobins.getLastJobExeId());
			if (jobExeState > 0 && jobExeState < JobExecution.STATE_UNKNOWN) { // JobExe가 해당 에어전트에 존재하는 상태.
				Util.logInfo(log, MSG.get("main.jobctl.stop", auth, jobins.getLastJobExeId())); // [{0}]에서 {1}를 강제종료 시도 합니다
				agentClient.stop(jobins.getLastJobExeId());
			}else { 
				/*
				 * JobExe 가 에이전트에 없는 경우.
				 * 1. 이미 끝난 직후
				 * 2. peer 의 internal 에이전트에서 돌고 있는 경우
				 */
				if (agentInfoManager.isInternalAgent(lastRunAgentId)) {
					int state = peerClient.getInternalAgentJobExeState(lastRunAgentId, jobins.getLastJobExeId());
					if (state > 0 && state < 99) { // peer 의 internal 에서 수행중
						Util.logInfo(log, MSG.get("main.peer.request.jobctl", jobins.getLastJobExeId(), "Stop"));
						peerClient.stopInternalJob(lastRunAgentId, jobins.getLastJobExeId());
					}
				}else {
					if (jobExecutionManager.getJobExecutionState(jobins.getLastJobExeId()) ==  JobExecution.STATE_INIT && 
						Util.getDiffSecond(jobins.getLastStartTime().substring(8,14), Util.getCurrentHHMMSS()) > 180) {
						/* 
						 * Ins의 상태는 "R" 이지만, Exe 은 INIT 인 것은 아닌 경우,
						 * start 된지 3분이상이 지난 경우, ControlerMain 에서 실행을 위해 DB 업데이트를 마치고 agentClient.start() 호출전에 스케줄러가 다운된 경우
						 * ==>Ghost 상태로 변경하여 User 로 하여금 뭔가 조치를 취할 수 있게 해준다. 
						 */
						runningJobStateMonitor.changeToGhost(jobins.getJobInstanceId(), "");
						Util.logInfo(log, MSG.get("main.jobctl.stop", auth, jobins.getLastJobExeId())+" -> changed to Ghost.");
					}
				}
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.stop.error", e, auth, jobInstanceId); // [{0}]에서 {1}를 강제 종료(stop)하는 중에 에러가 발생하였습니다
		}

	}
	
	/**
	 * RUNNING 상태의 Job을 일시정지시킴
	 * @param jobInstanceId
	 * @param auth
	 */
	public void suspendJob(String jobInstanceId, AdminAuth auth) {
		JobInstance jobins   = null;
		try {
			jobins   = jobInstanceManager.getJobInstance(jobInstanceId);
			userManager.checkOperationPermission(jobins.getJobGroupId(), jobInstanceIdMap.getJobId(jobInstanceId), "suspendJob", auth); // 권한 체크.
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
		}
		
		if (!JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState())) {
			throw logAndMakeSchedulerException("main.jobctl.state.error", (Throwable)null, jobInstanceId, jobins.getJobStateText(), "Suspend"); // {1} 상태에서는 {2}를 할 수 없습니다 ({0})
		}

		String lastRunAgentId = null; // 현재 실행중인 Agent ID. 이중화를 고려한 조치. since 3.9
		try {
		    // JobInstance 의 agentNode 값에는 멀티정보가 들어있을 수 있으므로
		    // JobExecutionManager를 통해 최종 실행 agent id를 찾아온다.
		    lastRunAgentId = jobExecutionManager.getJobExecutionAgentNode(jobins.getLastJobExeId()); // 마지막으로 실행했던 agent id
		    IAgentClient agentClient = getAgentClient(lastRunAgentId);
			int jobExeState = agentClient.getJobExeState(jobins.getLastJobExeId());
			if (jobExeState > 0 && jobExeState < JobExecution.STATE_UNKNOWN) { // JobExe가 해당 에어전트에 존재하는 상태.
				Util.logInfo(log, MSG.get("main.jobctl.suspend", auth, jobins.getLastJobExeId())); // [{0}]에서 {1}를 일시정지(suspend) 시도 합니다
				agentClient.suspend(jobins.getLastJobExeId()); // 여기에 에러나면 rollback된다.
			}else {
				/*
				 * JobExe 가 에이전트에 없는 경우.
				 * 1. 이미 끝난 직후
				 * 2. peer 의 internal 에이전트에서 돌고 있는 경우
				 */
				if (agentInfoManager.isInternalAgent(lastRunAgentId)) {
					int state = peerClient.getInternalAgentJobExeState(lastRunAgentId, jobins.getLastJobExeId());
					if (state > 0 && state < 99) { // peer 의 internal 에서 수행중
						Util.logInfo(log, MSG.get("main.peer.request.jobctl", jobins.getLastJobExeId(), "Suspend"));
						peerClient.suspendInternalJob(lastRunAgentId, jobins.getLastJobExeId());
					}
				}else {
					// 이도 저도 아닌 경우, 그냥 리턴한다. 이미 End 된 상태일 가능성이 많다.
				}
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.suspend.error", e, auth, jobInstanceId); // [{0}]에서 {1}를 일지정지(suspend)하는 중에 에러가 발생하였습니다
		}
	}

	/**
	 * SUSPENDED 상태의 Job을 계속실행시킴
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void resumeJob(String jobInstanceId, AdminAuth auth) {
		JobInstance jobins   = null;
		try {
			jobins   = jobInstanceManager.getJobInstance(jobInstanceId);
			userManager.checkOperationPermission(jobins.getJobGroupId(), jobInstanceIdMap.getJobId(jobInstanceId), "resumeJob", auth); // 권한 체크.
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
		}
		
		if (!JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {	
			throw logAndMakeSchedulerException("main.jobctl.state.error", (Throwable)null, jobInstanceId, jobins.getJobStateText(), "Resume"); // {1} 상태에서는 {2}를 할 수 없습니다 ({0})
		}
		
        String lastRunAgentId = null; // 현재 실행중인 Agent ID. 이중화를 고려한 조치. since 3.9
        try {
            // JobInstance 의 agentNode 값에는 멀티정보가 들어있을 수 있으므로
            // JobExecutionManager를 통해 최종 실행 agent id를 찾아온다.
            lastRunAgentId = jobExecutionManager.getJobExecutionAgentNode(jobins.getLastJobExeId()); // 마지막으로 실행했던 agent id
            IAgentClient agentClient = getAgentClient(lastRunAgentId);
			int jobExeState = agentClient.getJobExeState(jobins.getLastJobExeId());
			if (jobExeState > 0 && jobExeState < JobExecution.STATE_UNKNOWN) { // JobExe가 해당 에어전트에 존재하는 상태.
				Util.logInfo(log, MSG.get("main.jobctl.resume", auth, jobins.getLastJobExeId())); // [{0}]에서 {1}를 계속실행(resume) 시도 합니다
				agentClient.resume(jobins.getLastJobExeId());
			}else { 
				/*
				 * JobExe 가 에이전트에 없는 경우.
				 * 1. 이미 끝난 직후
				 * 2. peer 의 internal 에이전트에서 돌고 있는 경우
				 */
				if (agentInfoManager.isInternalAgent(lastRunAgentId)) {
					int state = peerClient.getInternalAgentJobExeState(lastRunAgentId, jobins.getLastJobExeId());
					if (state > 0 && state < 99) { // peer 의 internal 에서 수행중
						Util.logInfo(log, MSG.get("main.peer.request.jobctl", jobins.getLastJobExeId(), "Resume"));
						peerClient.resumeInternalJob(lastRunAgentId, jobins.getLastJobExeId());
					}
				}else {
					// 이도 저도 아닌 경우, 그냥 리턴한다. 이미 End 된 상태일 가능성이 많다.
				}
			}
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.resume.error", e, auth, jobInstanceId); // [{0}]에서 {1}를 계속실행(resume)하는 중에 에러가 발생하였습니다
		}
	}
	
	/**
	 * Confirm 이 필요한 Job 에게 confirm 을 줌
	 * @param jobInstanceId
	 * @param auth
	 */
	public void confirmJob(String jobInstanceId, AdminAuth auth) {
		JobInstance jobins   = null;
		try {
			jobins   = jobInstanceManager.getJobInstance(jobInstanceId);
			userManager.checkOperationPermission(jobins.getJobGroupId(), jobInstanceIdMap.getJobId(jobInstanceId), "confirmJob", auth); // 권한 체크.
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
		}
		
		if (!"Y".equals(jobins.getConfirmNeedYN())) {	
			throw logAndMakeSchedulerException("main.jobctl.state.error", (Throwable)null, jobInstanceId, "Confirm Need=N", "Confirm"); // {1} 상태에서는 {2}를 할 수 없습니다 ({0})
		}
		
		try {
			jobInstanceManager.setConfirmed(jobInstanceId, auth.getOperatorId(), auth.getOperatorIp());
			Util.logInfo(log, MSG.get("main.jobctl.confirm", auth, jobInstanceId)); // [{0}]에서 {1}를 실행승인(confirm) 하였습니다
			jobStarter.askToStart(jobins);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.confirm.error", e, auth, jobInstanceId); // [{0}]에서 {1}를 실행승인(confirm)하는 중에 에러가 발생하였습니다
		}
	}
	
	/**
	 * ENDED_FAIL 인 Job을 ENDED_OK고 강제 상태 변경후, 후행 Job 들이 동작하도록 함
	 * 후행 Job, Trigger 를 기동시킴. 
	 * @param jobInstanceId
	 * @param auth
	 * @return
	 */
	public void forceEndOk(String jobInstanceId, AdminAuth auth) {
		JobInstance jobins   = null;
		try {
			jobins   = jobInstanceManager.getJobInstanceDeep(jobInstanceId);
			userManager.checkOperationPermission(jobins.getJobGroupId(), jobInstanceIdMap.getJobId(jobInstanceId), "forceEndOk", auth); // 권한 체크.
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
		}
		
		if (!JobInstance.JOB_STATE_ENDED_FAIL.equals(jobins.getJobState()) && 
			!JobInstance.JOB_STATE_WAIT.equals(jobins.getJobState()) && 
			!JobInstance.JOB_STATE_SLEEP_RPT.equals(jobins.getJobState()) && 
			!JobInstance.JOB_STATE_EXPIRED.equals(jobins.getJobState()) &&
			!JobInstance.JOB_STATE_INIT.equals(jobins.getJobState()) &&
			!JobInstance.JOB_STATE_GHOST.equals(jobins.getJobState())) {
			throw logAndMakeSchedulerException("main.jobctl.state.error", (Throwable)null, jobInstanceId, jobins.getJobStateText(), "ForceEndOk"); // {1} 상태에서는 {2}를 할 수 없습니다 ({0})
		}
		
		try {
			sqlMapClient.startTransaction();
			if (!jobInstanceManager.setJobStateForEnd(jobInstanceId, true, MSG.get("main.jobctl.state.forceendok", auth), 0, true, jobins.getJobState(), jobins.getLastJobExeId())) {
				throw new SchedulerException("main.jobctl.state.inconsistent.error", auth, jobInstanceId); // [{0}]에서 인스턴스({1}) 컨트롤 중 상태 불일치 현상이 발생하였습니다
			}
			Util.logInfo(log, MSG.get("main.jobctl.forceendok", auth, jobInstanceId)); // [{0}]에서 인스턴스({1})를 강제 정상종료 (force endok) 처리합니다

			// 상태변경으로 인해 돌아야하는 후행 놈들을 돌린다.
			jobRunResultProcessor.awakePreJobWaitingInstances(jobins.getJobId(), jobins.getProcDate());

			// 4. Trigger Job 처리.
			// 4.0 부터는 parent Job 의 결과에 따른 멀티 분기 처리 고도화. 2016.7.26 
			List<PostJobTrigger> triggerList = TriggerProcessor.selectTrigger(jobins.getJobInstanceId(), jobins.getJobId(), jobins.getTriggerList(), true, new Properties());
			List<JobInstance> triggeredJobInsList = new ArrayList<JobInstance>();
			for (PostJobTrigger trigger : triggerList) {
				for (int i=1; i<=trigger.getJobInstanceCount(); i++) {
					JobInstance newJobIns = jobRunResultProcessor.activateTriggerJob(jobins.getJobInstanceId(), jobins.getLastJobExeId(), trigger.getTriggerJobId(), jobins.getProcDate(), trigger.getJobInstanceCount(), i);
					if (newJobIns != null) {
						triggeredJobInsList.add(newJobIns);
					}
				}
			}
					
			sqlMapClient.commitTransaction();
			
			// commit 후에 기동한다.
			jobStarter.askToStart(triggeredJobInsList);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.forceendok.error", e, auth, jobInstanceId); // [{0}]에서 {1}를 강제정상종료 (force endok) 하는 중에 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception ignore) {
			}
		}
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
		JobInstance jobins   = null;
		try {
			jobins   = jobInstanceManager.getJobInstance(jobInstanceId);
			userManager.checkOperationPermission(jobins.getJobGroupId(), jobInstanceIdMap.getJobId(jobInstanceId), "forceChangeToGhost", auth); // 권한 체크.
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.error", e, jobInstanceId); // {0}의 Job 정보 조회 중 에러가 발생하였습니다
		}
		
		if (!JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) && 
			!JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {
			throw logAndMakeSchedulerException("main.jobctl.state.error", (Throwable)null, jobInstanceId, jobins.getJobStateText(), "forceChangeToGhost"); // {1} 상태에서는 {2}를 할 수 없습니다 ({0})
		}
		
		try {
			if (!jobInstanceManager.setJobStateWithCheck(jobInstanceId, jobins.getJobState(), JobInstance.JOB_STATE_GHOST, auth+" change to Ghost")) {
				throw new SchedulerException("main.jobctl.state.inconsistent.error", auth, jobInstanceId); // [{0}]에서 인스턴스({1}) 컨트롤 중 상태 불일치 현상이 발생하였습니다
			}
			Util.logInfo(log, MSG.get("main.jobctl.changetoghost", auth, jobInstanceId)); // [{0}]에서 {1}를 Ghost로 변경합니다

			// running list 에서 빼냄.
			JobExecution jobexe = jobExecutionManager.getJobExecution(jobins.getLastJobExeId());
			runningJobStateMonitor.removeRunningJobExecution(jobexe);
			
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.forceendok.error", e, auth, jobInstanceId); // [{0}]에서 {1}를 강제정상종료 (force endok) 하는 중에 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception ignore) {
			}
		}
	}
	
	// ###################################################################################################################
	//   여기부터는 Job Instace 작업
	// ###################################################################################################################
	
	
	/**
	 * Job Instance 정보 변경.
	 * 변경가능 항목 : 
	 *       TIME_FROM, TIME_UNTIL, REPEAT_YN, REPEAT_INTVAL, REPEAT_INTVAL_GB, REPEAT_MAX_OK, REPEAT_IF_ERROR, REPEAT_EXACT_EXP
	 * 	     CONFIRM_NEED_YN, PARALLEL_GROUP, AGENT_NODE, TRIGGER_JOB_ID, JOB_TYPE, JOB_DESC, COMPONENT_NAME, LOG_LEVEL
	 *       [PRO_JOB_CONDITIONS], [PARAMETERS]
	 * @param jobInstance
	 * @param auth
	 * @return 변경된 JobInstance
	 */
	public JobInstance modifyJobInstance(JobInstance jobins, AdminAuth auth) {
		try {
			userManager.checkOperationPermission(jobins.getJobGroupId(), jobins.getJobId(), "modifyJobInstance", auth); // 권한 체크.
			sqlMapClient.startTransaction();
			jobInstanceManager.updateJobInstance(jobins);
			sqlMapClient.commitTransaction();
			
			Util.logInfo(log, MSG.get("main.jobins.modify", auth, jobins)); // [{0}]에서 Job 인스턴스를 변경합니다. {1}

			// update된 내용으로 다시 읽어서 리턴한다.
			return jobInstanceManager.getJobInstanceDeep(jobins.getJobInstanceId());
		}catch (SchedulerException e) {
			throw e;
		}catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobins.crud.error", auth, e, jobins.getJobInstanceId(), 2); // Job 인스턴스 {0} {2#변경} 중 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
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
	public JobInstance modifyJobInstanceParameters(String jobInstanceId, Map newParams, AdminAuth auth) {
		JobInstance jobins = null;
		try {
			jobins = jobInstanceManager.getJobInstance(jobInstanceId);
			userManager.checkOperationPermission(jobins.getJobGroupId(), jobInstanceIdMap.getJobId(jobInstanceId), "modifyJobInstanceParameters", auth); // 권한 체크.
			sqlMapClient.startTransaction();
			if (jobins != null) {
				jobInstanceManager.deleteParameter(jobins);
				jobins.setInParameters(newParams);
				jobInstanceManager.insertParameter(jobins);
				sqlMapClient.commitTransaction();
			}
			
			Util.logInfo(log, MSG.get("main.jobins.modify", auth, jobins)); // [{0}]에서 Job 인스턴스를 변경합니다 {1}
			// update된 내용으로 다시 읽어서 리턴한다.
			return jobInstanceManager.getJobInstanceDeep(jobins.getJobInstanceId());
		}catch(SchedulerException e) {
			throw e;
		}catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobins.crud.error", auth, e, jobins.getJobInstanceId(), 2); // Job 인스턴스 {0} {2#변경} 중 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
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
		userManager.checkAdminPermission(auth); // 권한 체크.
		try {
			AgentInfo info = agentInfoManager.getAgentInfo(newAgentId);
			if (info == null) {
				throw new SchedulerException("main.agent.wrong.agentid", newAgentId); // 존재하지 않는 에이전트ID.
			}
			
			boolean b = jobInstanceManager.updateAgentId(jobInstanceId, newAgentId);
			if (b) {
				Util.logInfo(log, MSG.get("main.jobins.modify.agent", auth, jobInstanceId, newAgentId)); // [{0}]에서 인스턴스({1})의 에이전트 ID 를 {2}로 변경합니다
			}else {
				throw new SchedulerException("main.jobins.modify.agent.error.jobstate", JobInstance.getJobStateText(jobInstanceManager.getJobState(jobInstanceId))); // {0} 상태 중에는 에이전트 변경 불가합니다
			}
			return b;
		}catch (Exception e) {
			throw logAndMakeSchedulerException("main.jobins.crud.error", auth, e, jobInstanceId, 2); // Job 인스턴스 {0} {2#변경} 중 에러가 발생하였습니다
		}
	}

	
	/**
	 * Job Definition 추가. 비상용 API
	 * @return 건수
	 */
	public int addJobDefinition(JobDefinition jobdef, AdminAuth auth) {
		try {
			userManager.checkOperationPermission(jobdef.getJobGroupId(), jobdef.getJobId(), "addJobDefinition", auth); // 권한 체크.
			sqlMapClient.startTransaction();
			int c = jobDefinitionManager.insertJobDefinition(jobdef);
			sqlMapClient.commitTransaction();
			
			Util.logInfo(log, MSG.get("main.jobdef.add", auth, jobdef.getJobId(), jobdef.toString())); // [{0}]에서 신규 Job({1})을 생성합니다 {2} 
			return c;
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobdef.crud.error", auth, e, jobdef.getJobId(), 0); // Job {0} 의 등록정보 {1,choice,0#생성|1#조회|2#변경|3#삭제} 중 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
	}

	/**
	 * Job Definition 정보 직접 수정. 비상용 API
	 * @return 변경된 Job Definition
	 */
	public JobDefinition modifyJobDefinition(JobDefinition jobdef, AdminAuth auth) {
		try {
			userManager.checkOperationPermission(jobdef.getJobGroupId(), jobdef.getJobId(), "modifyJobDefinition", auth); // 권한 체크.
			sqlMapClient.startTransaction();
			jobDefinitionManager.updateJobDefinition(jobdef);
			sqlMapClient.commitTransaction();
			
			Util.logInfo(log, MSG.get("main.jobdef.modify", auth, jobdef.getJobId(), jobdef.toString())); // [{0}]에서 Job 등록정보({1})를 변경합니다 {2}
			return jobDefinitionManager.getJobDefinitionDeep(jobdef.getJobId());
		}catch (SchedulerException e) {
			throw e;
		}catch (SQLException e) {
			throw logAndMakeSchedulerException("main.jobdef.crud.error", auth, e, jobdef.getJobId(), 2); // Job 등록정보 {0} {2#변경} 중 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
	}
	
	/**
	 * JobDefinition 삭제. 비상용 API
	 * @param jobId
	 * @param auth
	 * @return 성공여부
	 */
	public boolean deleteJobDefinition(String jobId, AdminAuth auth) {
		try {
			userManager.checkOperationPermission(
				jobDefinitionManager.getJobGroupId(jobId), 
				jobId, "deleteJobDefinition", auth); // 권한 체크.
			sqlMapClient.startTransaction();
			int cnt = jobDefinitionManager.deleteJobDefinition(jobId);
			sqlMapClient.commitTransaction();
			Util.logInfo(log, MSG.get("main.jobdef.delete", auth, jobId)); // [{0}]에서 Job 등록정보({1})를 삭제합니다.
			return cnt > 0;
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobdef.crud.error", auth, e, jobId, 3); // Job 등록정보 {0} {3#삭제} 중 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
	}

	/**
	 * Calendar 리스트 조회
	 * @return
	 */
	public Map getCalendarList() {
		return scheduleCalendar.listCalendarIdNames();
	}
	
	/** 
	 * Calendar의 일자 조회
	 * @param calendarId
	 * @return
	 */
	public List getCalendarDayList(String calendarId) {
		return scheduleCalendar.getYyyymmddList(calendarId);
	}
	
	/** 
	 * Calendar 메모리 reload 
	 */
	public void reloadCalendar() {
		scheduleCalendar.reload();
		peerClient.refreshMemoryCache("RELOAD_CALENDAR", null);
	}
	
	/**
	 * 해당 일자에 Activate 될 Job Id 목록을 리스트함.
	 * @param yyyymmdd
	 * @return list of Job ID 
	 */
	public List getJobListWillBeActivated(String yyyymmdd) {
		Util.checkDateYYYYMMDD(yyyymmdd);

		List jobIdList = new LinkedList();
		List<JobDefinition> jobdefAll = null;

		try {
			jobdefAll = jobDefinitionManager.getJobDefinitionsByQuery(""); // 조건이 없으므로 full select
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.job.dbselect.query.error", e, "ALL"); // Job 검색 중 에러가 발생하였습니다
		}

		// Job 하나하나 activation 체크.
		for (JobDefinition jobdef : jobdefAll) {
			try {
				if (dayScheduler.isScheduledDay(jobdef, CalendarUtil.convYYYYMMDDToCalendar(yyyymmdd))) {
					jobIdList.add(jobdef.getJobId());
				}
			}catch(Exception e) {
				Util.logError(log, MSG.get("main.daysch.plan.error", jobdef.getJobId()), e); // {0} 의 스케줄 조회 중 에러가 발생하였습니다
			}
		}
		return jobIdList;
	}
	
	/**
	 * 특정 Job 의 해당월에 Activate 될 리스트 리턴
	 * @return list of yyyymmdd 
	 */
	public List getDayListWillBeActivated(String jobId, String yyyymm) {
		Util.checkDateYYYYMMDD(yyyymm+"01");
		try {
			JobDefinition jobdef = jobDefinitionManager.getJobDefinition(jobId);
			return dayScheduler.getScheduledDayList(jobdef, yyyymm);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.daysch.plan.error", e, jobId); // {0} 의 스케줄 조회 중 에러가 발생하였습니다
		}
	}

	/**
	 * 특정 Job 의 해당월에 Activate 될 리스트 리턴. 아직 등록되지 않은 Job 으로 스케줄 시뮬레이션 할때 사용됨. 
	 * @return list of yyyymmdd 
	 */
	public List getDayListWillBeActivated(JobDefinition jobdef, String yyyymm) {
		Util.checkDateYYYYMMDD(yyyymm+"01");
		try {
			return dayScheduler.getScheduledDayList(jobdef, yyyymm);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.daysch.plan.error", e, "Temp Job"); // {0} 의 스케줄 조회 중 에러가 발생하였습니다
		}
	}
	
	/**
	 * 특정월의 기준일 계산 결과.
	 * @param jobId
	 * @param yyyymm
	 * @return
	 */
	public Map getBaseDateMonthlyMap(String jobId, String yyyymm) {
		Util.checkDateYYYYMMDD(yyyymm+"01");
		try {
			JobDefinition jobdef = jobDefinitionManager.getJobDefinition(jobId);
			return activator.calcBaseDateMonthly(jobdef, yyyymm);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.daysch.calc.basedate.error", e, jobId, yyyymm); // {0} 의 {1} 월의 기준일 계산 중 에러가 발생하였습니다
		}
	}

	/**
	 * 특정월의 기준일 계산 결과.
	 * @param triggerJobId
	 * @param yyyymm
	 * @return
	 */
	public Map getBaseDateMonthlyMap(JobDefinition jobdef, String yyyymm) {
		Util.checkDateYYYYMMDD(yyyymm+"01");
		try {
			return activator.calcBaseDateMonthly(jobdef, yyyymm);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.daysch.calc.basedate.error", e, jobdef.getJobId(), yyyymm); // {0} 의 {1} 월의 기준일 계산 중 에러가 발생하였습니다
		}
	}
	
	/**
	 * JobDefinition 변경/신규/삭제를 위한 요청 등록.
	 * @param jobdef
	 * @param auth
	 * @return 요청번호
	 */
	public String addJobDefinitionStg(JobDefinitionStg jobdef, AdminAuth auth) {
		try {
			jobdef.setReqNo(jobDefinitionStgManager.newReqNo());
			if ("add".equals(jobdef.getReqType()) && jobDefinitionManager.getJobDefinition(jobdef.getJobId()) != null) {
				throw new SchedulerException("main.jobdefstg.jobid.dup", jobdef.getJobId()); // {0} 는 이미 존재하는 Job 입니다
			}
			
			// license 체크
			if("add".equals(jobdef.getReqType())){
				int jobDefinitionCount = jobDefinitionManager.getJobDefinitionsCount();
				int maxJobDefinitionCount = licenseManager.getMaxJobDefinitionCount();
				
				if(maxJobDefinitionCount != 0 && jobDefinitionCount >= maxJobDefinitionCount){
					throw logAndMakeSchedulerException("main.jobdef.maxcount.error", null, jobDefinitionCount, maxJobDefinitionCount);
				}
			}
			
			sqlMapClient.startTransaction();
			int c = jobDefinitionStgManager.insertJobDefinitionStg(jobdef);
			sqlMapClient.commitTransaction();
			Util.logInfo(log, MSG.get("main.jobdefstg.add", auth, jobdef.getJobId(), jobdef.getReqType())); // [{0}]에서 Job({1})을 {2} 요청을 하였습니다 
			return c>0 ? jobdef.getReqNo() : null;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobdefstg.add.error", auth, e, jobdef.getJobId()); // Job {0} 요청 중 에러가 발생하였습니다. 이미 요청 진행 중이거나, 동일 Job Id 의 중복 요청일 수 있습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
	}

	/**
	 * JobDefinition 변경/신규/삭제 요청 내역 조회
	 * @param reqNo
	 * @param jobId
	 * @return
	 */
	public JobDefinitionStg getJobDefinitionStg(String reqNo, String jobId) {
		try {
			return jobDefinitionStgManager.getJobDefinitionStgDeep(reqNo, jobId);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobdefstg.dbselect.error", e, reqNo, jobId); // 요청정보 {0}, Job Id {1} 의 요청 정보 조회 중 에러가 발생하였습니다
		}
	}

	/**
	 * JobDefinition 변경/신규/삭제 요청목록 조회
	 */
	public List<JobDefinitionStg> getJobDefinitionStgList(String queryCondition, boolean deep) {
		try {
			return jobDefinitionStgManager.getJobDefinitionStgsByQuery(queryCondition);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobdefstg.dbselect.query.error", e, queryCondition); // 요청 정보 검색 중 에러가 발생하였습니다
		}
	}
	
	public void getJobDefinitionStgListWithRH(String queryCondition, Object rowHandler) {
		try {
			jobDefinitionStgManager.getJobDefinitionStgsByQueryWithRH(queryCondition, rowHandler);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobdefstg.dbselect.query.error", e, queryCondition); // 요청 정보 검색 중 에러가 발생하였습니다
		}
	}

	public void approveJobDefinitionStgToJobDefinition(String reqNo, String jobId, String reqARReason, AdminAuth auth) {
		boolean actionOk=false;
		JobDefinitionStg jobdef = null;
		
		try {
			User user = userManager.getUser(auth.getOperatorId()); 
			jobdef = jobDefinitionStgManager.getJobDefinitionStgDeep(reqNo, jobId);
			userManager.checkOperationPermission(jobdef.getJobGroupId(), jobId, "approveJobDefinitionStgToJobDefinition", auth); // 권한 체크.
			jobdef.setReqOperatorId(user.getId());
			jobdef.setReqOperatorName(user.getName());
			jobdef.setReqOperatorIp(auth.getOperatorIp());
			if (!"Q".equals(jobdef.getReqState())) { // 상태가 요청이 아닌 경우는 에러. 
				throw new SchedulerException("main.jobdefstg.state.not.q", reqNo); // {0}은 요청 상태가 아닙니다
			}
			
			// license 체크
			if("add".equalsIgnoreCase(jobdef.getReqType())){
				int jobDefinitionCount = jobDefinitionManager.getJobDefinitionsCount();
				int maxJobDefinitionCount = licenseManager.getMaxJobDefinitionCount();
				if(maxJobDefinitionCount != 0 && jobDefinitionCount >= maxJobDefinitionCount){
					throw logAndMakeSchedulerException("main.jobdef.maxcount.error", null, jobDefinitionCount, maxJobDefinitionCount);
				}
			}
			
			sqlMapClient.startTransaction();
			if ("add".equalsIgnoreCase(jobdef.getReqType())) {
				jobdef.setCreateTime(Util.getCurrentYYYYMMDDHHMMSS()); // 승인일이 등록일.
				actionOk = jobDefinitionManager.insertJobDefinition(jobdef) > 0;
				
			}else if ("edit".equalsIgnoreCase(jobdef.getReqType())) {
				actionOk = jobDefinitionManager.updateJobDefinition(jobdef) > 0;
				
			}else if ("delete".equalsIgnoreCase(jobdef.getReqType())) {
				actionOk = jobDefinitionManager.deleteJobDefinition(jobdef.getJobId()) > 0;
			}
			
			if (actionOk) {
				jobdef.setReqState("A"+Util.getCurrentYYYYMMDDHHMMSS());
				jobdef.setReqARReason(reqARReason);
				jobDefinitionStgManager.updateJobDefinitionStgReqInfo(jobdef);
			}
			
			sqlMapClient.commitTransaction();
			Util.logInfo(log, MSG.get("main.jobdefstg.approve", auth, jobdef.getReqNo(), jobdef.getJobId())); // [{0}] 에서 요청정보({0}), Job Id({1}) 의 요청을 승인했습니다
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobdefstg.approve.error", auth, e, jobdef.getReqNo(), jobdef.getJobId()); // 요청번호({0}), Job Id({1})의 승인 중 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
	}
	
	public void rejectJobDefinitionStgToJobDefinition(String reqNo, String jobId, String reqARReason, AdminAuth auth) {
		JobDefinitionStg jobdef = null;
		try {
			User user = userManager.getUser(auth.getOperatorId()); 
			jobdef = jobDefinitionStgManager.getJobDefinitionStg(reqNo, jobId);
			userManager.checkOperationPermission(jobdef.getJobGroupId(), jobId, "rejectJobDefinitionStgToJobDefinition", auth); // 권한 체크.
			if (!"Q".equals(jobdef.getReqState())) { // 상태가 요청이 아닌 경우는 에러. 
				throw new SchedulerException("main.jobdefstg.state.not.q", reqNo); // {0}은 요청 상태가 아닙니다
			}
			jobdef.setReqState("R"+Util.getCurrentYYYYMMDDHHMMSS());
			jobdef.setReqARReason(reqARReason);
			jobdef.setReqOperatorId(user.getId());
			jobdef.setReqOperatorName(user.getName());
			jobdef.setReqOperatorIp(auth.getOperatorIp());

			sqlMapClient.startTransaction();
			jobDefinitionStgManager.updateJobDefinitionStgReqInfo(jobdef);
			sqlMapClient.commitTransaction();
			Util.logInfo(log, MSG.get("main.jobdefstg.reject", auth, jobdef.getReqNo(), jobdef.getJobId())); // [{0}] 에서 요청정보({0}), Job Id({1}) 의 요청을 반려하였습니다.
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobdefstg.reject.error", auth, e, jobdef.getReqNo(), jobdef.getJobId()); // 요청번호({0}), Job Id({1})의 반려 중 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
	}		
	
	/**
	 * 등록요청 취소
	 * @param reqNo
	 * @param auth
	 * @return 성공
	 */
	public boolean deleteJobDefinitionStg(String reqNo, AdminAuth auth) {
		try {
			sqlMapClient.startTransaction();
			int cnt = jobDefinitionStgManager.deleteJobDefinitionStg(reqNo);
			sqlMapClient.commitTransaction();
			Util.logInfo(log, MSG.get("main.jobdefstg.delete", auth, reqNo)); // [{0}] 에서 요청번호({1}) 의 요청을 취소합니다
			return cnt > 0;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobdefstg.delete.error", auth, e, reqNo); // 요청번호({0}) 의 요청을 취소하는 중 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception e) {
				e.printStackTrace(); // and ignore
			}
		}
	}


	/**
	 * Job Definition 정보 검증. (스케줄정보, 파라미터 정보등)
	 * @param jobdef
	 */
	public List validateJobDefinition(JobDefinition jobdef) {
		if (jobInfoValidator == null) {
			return null; // validator 가 설정되어있지 않으면 그냥 null 리턴함.
		}else {
			return jobInfoValidator.validate(jobdef);
		}
	}		
	
	/**
	 * Job Instance 정보 검증. (스케줄정보, 파라미터 정보등)
	 * @param jobins
	 */
	public List validateJobInstance(JobInstance jobins) {
		if (jobInfoValidator == null) {
			return null; // validator 가 설정되어있지 않으면 그냥 null 리턴함.
		}else {
			return jobInfoValidator.validate(jobins);
		}
	}		
	
	public List<String> getJobTypeUsingList() {
		return jobTypeManager.getJobTypeUsingList();
	}
	
	public List<String> getLogLevelUsingList() {
		return customConfig.getLogLevelUsingList();
	}

	/* ------- AgentInfo 관리 ---------- */
	
	//**************************************************************************************
	//********  파라미터 관리 
	//**************************************************************************************

	//**********************************************************************
	//****  Global Parameter
	//**********************************************************************
	
	public void addGlobalParameter(String paramName, String paramValue, AdminAuth auth) {
		userManager.checkAdminPermission(auth); // 권한 체크.
		try {
			parameterManager.insertParameter(paramName, paramValue);
			Util.logInfo(log, MSG.get("main.global.param.admin", auth, paramName+"="+paramValue, 0)); // [{0}] 에서 전역변수 ({1})를 {2,choice,0#추가|1#조회|2#변경|3#삭제} 했습니다
			parameterManager.reloadGlobalParameters();
			peerClient.refreshMemoryCache("GLOBAL_PARAMETER", null);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.global.param.admin.error", auth, e, paramName+"="+paramValue, 0); // 전역파라미터 ({0})를 {1,choice,0#추가|1#조회|2#변경|3#삭제} 중 에러가 발생했습니다
		}
	}

	public void modifyGlobalParameter(String paramName, String paramValue, AdminAuth auth) {
		userManager.checkAdminPermission(auth); // 권한 체크.
		try {
			parameterManager.updateParameter(paramName, paramValue);
			Util.logInfo(log, MSG.get("main.global.param.admin", auth, paramName+"="+paramValue, 2)); // [{0}] 에서 전역변수 ({1})를 {2,choice,0#추가|1#조회|2#변경|3#삭제} 했습니다
			parameterManager.reloadGlobalParameters();
			peerClient.refreshMemoryCache("GLOBAL_PARAMETER", null);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.global.param.admin.error", auth, e, paramName+"="+paramValue, 2); // 전역파라미터 ({0})를 {1,choice,0#추가|1#조회|2#변경|3#삭제} 중 에러가 발생했습니다
		}
	}

	public void deleteGlobalParameter(String paramName, AdminAuth auth) {
		userManager.checkAdminPermission(auth); // 권한 체크.
		try {
			parameterManager.deleteParameter(paramName);
			Util.logInfo(log, MSG.get("main.global.param.admin", auth, paramName, 3)); // [{0}] 에서 전역변수 ({1})를 {2,choice,0#추가|1#조회|2#변경|3#삭제} 했습니다
			parameterManager.reloadGlobalParameters();
			peerClient.refreshMemoryCache("GLOBAL_PARAMETER", null);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.global.param.admin.error", auth, e, paramName, 3); // 전역파라미터 ({0})를 {1,choice,0#추가|1#조회|2#변경|3#삭제} 중 에러가 발생했습니다
		}
	}

	public Map getGlobalParameters() {
		return parameterManager.getGlobalParameters();
	}
	
	/// 글로벌 파라미터 db reload
	public void reloadGlobalParameter(AdminAuth auth) {
		try {
			userManager.checkAdminPermission(auth); // 권한 체크.
			parameterManager.reloadGlobalParameters();
			peerClient.refreshMemoryCache("GLOBAL_PARAMETER", null);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.global.param.admin.error", auth, e, "ALL", 1); // 전역파라미터 ({0})를 {1,choice,0#추가|1#조회|2#변경|3#삭제} 중 에러가 발생했습니다
		}
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
		JobExecution jobexe = new JobExecution();
		jobexe.setInParameters(paramMap);
		jobexe.setProcDate(Util.getCurrentYYYYMMDD());
		
		jobdef.setBaseDateCalId(baseDateCalId);
		jobdef.setBaseDateLogic(baseDateLogic);
		
		jobexe.setBaseDate     (activator.calcBaseDate(jobdef, jobexe.getProcDate()));
		jobexe.setJobId        (jobdef.getJobId());
		jobexe.setJobGroupId   (jobdef.getJobGroupId());
		jobexe.setDescription  (jobdef.getDescription());
		jobexe.setJobType      (jobdef.getJobType());
		jobexe.setComponentName(jobdef.getComponentName());
		jobexe.setAgentNode    (jobdef.getAgentNodeMaster());
		
		Map         agentSystemEnv  = null;
		Properties  agentSystemProp = null;
		
		try {
			IAgentClient agentClient = agentInfoManager.getAgentClient(agentId);
			agentSystemEnv  = agentClient.getSystemEnv();
			agentSystemProp = agentClient.getSystemProperties();
		}catch(Exception ignore) {
			agentSystemEnv  = new HashMap();
			agentSystemProp = new Properties();
		}
		parameterManager.setupJobParameter(jobexe, agentSystemProp, agentSystemEnv);
		return jobexe.getInParameters();
	}
	
	//**********************************************************************
	//****  Parallel Group 
	//**********************************************************************
	public void addParallelGroup(ParallelGroup pg, AdminAuth auth) {
		userManager.checkAdminPermission(auth); // 권한 체크.
		try {
			parallelRunningCounter.addParallelGroup(pg, auth);
			Util.logInfo(log, MSG.get("main.parallel.group.admin", auth, pg, 0)); // [{0}] 에서 병렬제한그룹 ({1})을 {2,choice,0#추가|1#조회|2#변경|3#삭제} 했습니다
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.parallel.group.admin.error", auth, e, pg, 0); // 병렬제한그룹 ({0})을 {1,choice,0#추가|1#조회|2#변경|3#삭제} 중 에러가 발생했습니다
		}
	}

	public void modifyParallelGroup(ParallelGroup pg, AdminAuth auth) {
		userManager.checkAdminPermission(auth); // 권한 체크.
		try {
			parallelRunningCounter.modifyParallelGroup(pg, auth);
			Util.logInfo(log, MSG.get("main.parallel.group.admin", auth, pg, 2)); // [{0}] 에서 병렬제한그룹 ({1})을 {2,choice,0#추가|1#조회|2#변경|3#삭제} 했습니다
			jobRunResultProcessor.awakeParallelWaitingInstances(pg.getGroupName());
			peerClient.refreshMemoryCache("PARALLEL_GROUP", pg.getGroupName());
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.parallel.group.admin.error", auth, e, pg, 2); // 병렬제한그룹 ({0})을 {1,choice,0#추가|1#조회|2#변경|3#삭제} 중 에러가 발생했습니다
		}
	}

	public void deleteParallelGroup(String groupName, AdminAuth auth) {
		userManager.checkAdminPermission(auth); // 권한 체크.
		try {
			parallelRunningCounter.deleteParallelGroup(groupName, auth);
			Util.logInfo(log, MSG.get("main.parallel.group.admin", auth, groupName, 3)); // [{0}] 에서 병렬제한그룹 ({1})을 {2,choice,0#추가|1#조회|2#변경|3#삭제} 했습니다
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.parallel.group.admin.error", auth, e, groupName, 3); // 병렬제한그룹 ({0})을 {1,choice,0#추가|1#조회|2#변경|3#삭제} 중 에러가 발생했습니다
		}
	}

	public List<ParallelGroup> getAllParallelGroups() {
		try {
			return parallelRunningCounter.getAllParallelGroupsList();
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.parallel.group.admin.error", e, "ALL", 1); // 병렬제한그룹 ({0})을 {1,choice,0#추가|1#조회|2#변경|3#삭제} 중 에러가 발생했습니다
		}
	}

	public ParallelGroup getParallelGroup(String groupName) {
		try {
			return parallelRunningCounter.getParallelGroup(groupName);
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.parallel.group.admin.error", e, groupName, 1); // 병렬제한그룹 ({0})을 {1,choice,0#추가|1#조회|2#변경|3#삭제} 중 에러가 발생했습니다
		}
	}
	
	/**
	 * 로그 레벨 변경. JobInstance 의 로그레벨을 변경하면 현재 수행중인 Job Execution 에 영향을 주지만, JobDefinition 에는 주지 않는다.
	 * @param jobInstanceId
	 * @param logLevel
	 * @param auth
	 * @return
	 */
	public boolean setJobInstanceLogLevel(String jobInstanceId, String logLevel, AdminAuth auth) {
		try {
			userManager.checkOperationPermission(
				jobInstanceManager.getJobGroupId(jobInstanceId), 
				jobInstanceIdMap.getJobId(jobInstanceId), "setJobInstanceLogLevel", auth); // 권한 체크.
			sqlMapClient.startTransaction();
			int cnt = jobInstanceManager.updateJobInstanceLogLevel(jobInstanceId, logLevel);
			
			// agent 의 Job LogLevel Update.
			JobExecution jobexe = runningJobStateMonitor.getRunningJobExecutionByJobInsId(jobInstanceId);
			if (jobexe != null) { // 현재 실행중인 경우
				IAgentClient agent = getAgentClient(jobexe.getAgentNode());
				boolean ok = agent.setJobExecutionLogLevel(jobexe.getJobExecutionId(), logLevel);
				if (!ok) {
					// 로그레벨 변경 실패. (Job 이 End 됐거나, peer 의 internal 에서 실행중이거나)
					if (agentInfoManager.isInternalAgent(jobexe.getAgentNode())) { // internal 인 경우는 peer 에도 set 해본다. false 리턴하더라도 그냥 정상 리턴해라.
						peerClient.setInternalJobExecutionLogLevel(jobexe.getAgentNode(), jobexe.getJobExecutionId(), logLevel);
					}
				}
			}
			sqlMapClient.commitTransaction();
			Util.logInfo(log, MSG.get("main.jobctl.change.loglevel", auth, jobInstanceId, logLevel)); // [{0}] 에서 {1} 의 로그 레벨을 {2} 로 변경합니다
			return cnt == 1;
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.change.loglevel.error", auth, e, jobInstanceId, 3); // {0} 의 로그 레벨을 {2} 로 변경하는 중 에러가 발생하였습니다
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception ignore){
			}
		}
	}
	
	/**
	 * 로그 레벨 변경. JobDefinition 의 로그레벨을 변경하면, 그후 activation 되는 JobInstance에 영향을 준다.
	 * @param jobId
	 * @param logLevel
	 * @param auth
	 * @return
	 */
	public boolean setJobDefinitionLogLevel(String jobId, String logLevel, AdminAuth auth) {
		try {
			userManager.checkOperationPermission(
				jobDefinitionManager.getJobGroupId(jobId), 
				jobId, "setJobInstanceLogLevel", auth); // 권한 체크.
			Util.logInfo(log, MSG.get("main.jobctl.change.loglevel", auth, jobId, logLevel)); // [{0}] 에서 {1} 의 로그 레벨을 {2} 로 변경합니다
			return jobDefinitionManager.updateJobDefinitionLogLevel(jobId, logLevel) == 1;
		}catch(SchedulerException e) {
			throw e;
		}catch(Exception e) {
			throw logAndMakeSchedulerException("main.jobctl.change.loglevel.error", auth, e, jobId, 3); // {0} 의 로그 레벨을 {2} 로 변경하는 중 에러가 발생하였습니다
		}
	}

	/**
	 * EXACT 방식의 반복일 경우 정규표현식을 해석하여 실행 시각을 예측함
	 * From, Until 도 고려하여 계산함
	 * @param jobdef
	 * @return
	 */
	public List<String> getTimePlanForExactRepeat(JobDefinition jobdef) {
		return repeatManager.getTimePlanForExactRepeat(jobdef);
	}

	/**
	 * EXACT 방식의 반복일 경우 정규표현식을 해석하여 실행 시각을 예측함
	 * From, Until 도 고려하여 계산함
	 * @param jobins
	 * @return
	 */
	public List<String> getTimePlanForExactRepeat(JobInstance jobins) {
		return repeatManager.getTimePlanForExactRepeat(jobins);
	}
	
	/**
	 * shutdown 을 위해 주요 실행 모듈/스레드를 close한다.
	 * @param auth
	 */
	public void shutdown(String id, String password, String ip) {
		User user = userManager.login(id, password, ip);
		if (user == null) {
			throw new SchedulerException("main.user.login.fail", id, ip);
		}
		userManager.checkAdminPermission(user); // 권한 체크.
		Util.logInfo(log, MSG.get("main.shutdown.process.started", id, ip)); // !!!! [{0}/{1}] 에서 셧다운을 시도합니다 !!!!

		Util.logInfo(log, MSG.get("main.shutdown.stop.module", "TimeScheduler")); // {0} 를 정지합니다.
		do {
			timeScheduler.destroy();
			Util.sleep(1000);
		}while(timeScheduler.isAlive());

		Util.logInfo(log, MSG.get("main.shutdown.stop.module", "JobRunResultProcessor")); // {0} 를 정지합니다.
		jobRunResultProcessor.setClosed(true);

		Util.logInfo(log, MSG.get("main.shutdown.stop.module", "CommandQueue")); // {0} 를 정지합니다.
		jobStarter.setQueueClosed(true);
		do {
			Util.sleep(1000);
		}while(jobStarter.getDecisionQueueSize() > 0 || jobStarter.getRunQueueSize() > 0);

//		Util.logInfo(log, MSG.get("main.shutdown.stop.module", "DailyActivator")); // {0} 를 정지합니다.
//		do {
//			dailyActivator.destroy();
//			Util.sleep(1000);
//		}while(dailyActivator.isAlive());
		
		Util.logInfo(log, MSG.get("main.shutdown.stop.module", "Components")); // {0} 를 정지합니다.
		// 서버 컴포넌트들 destory()
		try {
			BeanRegistry.destroy();
		}catch(Throwable e) {
			e.printStackTrace();
		}
		
		Util.logInfo(log, MSG.get("main.shutdown.finished")); // 셧다운 완료되었습니다
		new Thread(new Runnable() {
			public void run() {
				Util.sleep(2000);
				System.exit(0);
			}
		}).start();
		// 정상 리턴 후에 System.exit 하도록 한다.
	}
	
	/**
	 * 시스템 설정 값을 조회함.
	 * 미리 정의된 값들만 조회 가능함. (예, "DAILY_ACTIVATION_TIME")
	 * @param key
	 * @return
	 */
	public String getSystemConfigValue(String key) {
		if ("DAILY_ACTIVATION_TIME".equalsIgnoreCase(key)) {
			return dailyActivator.getActivationTime();
		}
		return null;
	}
	
	/**
	 * LicenseManager 의 정보로 위반 사항이 없는지 체크 
	 * @return Map<String, Integer> 객체를 리턴함.
	 */
	public boolean checkLicenseAndCurrentInfo(){
		return licenseManager.checkValidUsingCache();
	}

	/**
	 * LicenseManager에 저장된 agentCount, maxAgentCount, jobDefinitionCount, maxJobDefinitionCount 추출 
	 * @return Map<String, Integer> 객체를 리턴함.
	 */
	public Map<String, Integer> getLicenseAndCurrentInfo(){
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		// Licen의 Agent, JobDefinition 정보 추출
		int maxAgentCount = licenseManager.getMaxAgentCount();
		int maxJobDefinitionCount = licenseManager.getMaxJobDefinitionCount();
		int agentCount = licenseManager.getAgentCountInCache();
		int jobDefinitionCount = licenseManager.getJobDefinitionCountInCache();
		
		map.put("maxAgentCount", maxAgentCount);
		map.put("maxJobDefinitionCount", maxJobDefinitionCount);
		map.put("agentCount", agentCount);
		map.put("jobDefinitionCount", jobDefinitionCount);
		
		return map;
	}
}

