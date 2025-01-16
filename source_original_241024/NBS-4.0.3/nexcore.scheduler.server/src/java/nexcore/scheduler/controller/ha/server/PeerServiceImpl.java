package nexcore.scheduler.controller.ha.server;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.client.IAgentClient;
import nexcore.scheduler.controller.ha.IPeerService;
import nexcore.scheduler.controller.internal.ControllerMain;
import nexcore.scheduler.controller.internal.RunningJobStateMonitor;
import nexcore.scheduler.core.internal.JobRunResultProcessor;
import nexcore.scheduler.core.internal.JobStarter;
import nexcore.scheduler.core.internal.ParallelJobWaitingPool;
import nexcore.scheduler.core.internal.PreJobWaitingPool;
import nexcore.scheduler.core.internal.RepeatManager;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobExecutionSimple;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.monitor.internal.JobProgressStatusManager;
import nexcore.scheduler.monitor.internal.MonitorMain;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.ByteArray;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Peer 로부터 호출된 요청 처리를 위한 service </li>
 * <li>작성일 : 2012. 11. 5.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class PeerServiceImpl implements IPeerService {
	
	private ControllerMain            controllerMain;
	private MonitorMain               monitorMain;
	
	// =======================
	private JobStarter                jobStarter;
	private JobRunResultProcessor     jobRunResultProcessor;
	private RunningJobStateMonitor    runningJobStateMonitor;
	private JobProgressStatusManager  jobProgressStatusManager;
	private PreJobWaitingPool         preJobWaitingPool;
	private ParallelJobWaitingPool    parallelJobWaitingPool;
	private RepeatManager             repeatManager;

	private Log                       log;
	
	public void init() {
		log = LogManager.getSchedulerLog();
		Util.logServerInitConsole("PeerService");
		jobStarter                = controllerMain.getJobStarter();
		jobRunResultProcessor     = controllerMain.getJobRunResultProcessor();
		runningJobStateMonitor    = controllerMain.getRunningJobStateMonitor();
		jobProgressStatusManager  = controllerMain.getJobRunResultProcessor().getJobProgressStatusManager();
		repeatManager             = controllerMain.getRepeatManager();
	}

	public void destroy() {
	}
	
	public ControllerMain getControllerMain() {
		return controllerMain;
	}

	public void setControllerMain(ControllerMain controllerMain) {
		this.controllerMain = controllerMain;
	}

	public MonitorMain getMonitorMain() {
		return monitorMain;
	}

	public void setMonitorMain(MonitorMain monitorMain) {
		this.monitorMain = monitorMain;
	}
	
	public PreJobWaitingPool getPreJobWaitingPool() {
		return preJobWaitingPool;
	}
	
	public void setPreJobWaitingPool(PreJobWaitingPool preJobWaitingPool) {
		this.preJobWaitingPool = preJobWaitingPool;
	}

	public ParallelJobWaitingPool getParallelJobWaitingPool() {
		return parallelJobWaitingPool;
	}
	
	public void setParallelJobWaitingPool(ParallelJobWaitingPool parallelJobWaitingPool) {
		this.parallelJobWaitingPool = parallelJobWaitingPool;
	}
	
	// ######################################################################
	// ########                     REMOTE METHOD                 ###########
	// ######################################################################
	

	public boolean isAlive() {
		return true;
	}
	
	public long getSystemTime() {
		return System.currentTimeMillis();
	}

	public String getSystemId() {
		return Util.getSystemId();
	}

	public boolean askToStart(String jobInstanceId) {
		return jobStarter.askToStart(jobInstanceId);
	}

	/**
	 * peer 가 callBackJobEnd()를 받으면 update state 처리 후에 나에게도 callbackJobEnd() 를 하여
	 * 메모리상에 정리가 필요한 것들을 처리하게 한다.
	 * @param jobexe 
	 */
	public void callBackJobEnd(JobExecution jobexe) {
		// 1. awakePreJobWaitingInstances
		try {
			jobRunResultProcessor.awakePreJobWaitingInstances(jobexe.getJobId(), jobexe.getProcDate());
		}catch(Throwable e) { 
			Util.logError(log, "From peer awakePreJobWaitingInstances() fail. [JEXE_ID:"+jobexe.getJobExecutionId()+"]", e);
		}
		
		// 2. awakeParallelWaitingInstances
		try {
			jobRunResultProcessor.awakeParallelWaitingInstances(jobexe);
		}catch(Throwable e) { 
			Util.logError(log, "From peer awakePreJobWaitingInstances() fail. [JEXE_ID:"+jobexe.getJobExecutionId()+"]", e);
		}
		
		// 3. removeRunningJobExecution
		runningJobStateMonitor.removeRunningJobExecution(jobexe);
		
		// 4. updateJobProgress
		jobProgressStatusManager.updateRunningJobExecution(jobexe);
	}
	
	/**
	 * peer 에게 메모리 refresh 상황을 통지함.
	 * 전역 파라미터, 
	 * 병렬제한 max 값, 
	 * calendar reload, 
	 * AgentInfo, 
	 * JobNotify 등의 DB  값이 변경될 경우 peer 에게도 notify 함.
	 * 
	 * @param targetObject
	 * @param key
	 */
	public void refreshMemoryCache(String targetObject, String key) {
		
		if ("GLOBAL_PARAMETER".equals(targetObject)) {
			try {
				controllerMain.getParameterManager().reloadGlobalParameters();
			}catch(Exception e) {
				Util.logError(log, "refreshMemoryCache() fail."+targetObject+"/"+key, e);
			}
		}else if ("PARALLEL_GROUP".equals(targetObject)) {
			try {
				jobRunResultProcessor.awakeParallelWaitingInstances(key);
			}catch(Exception e) {
				Util.logError(log, "refreshMemoryCache() fail."+targetObject+"/"+key, e);
			}
		}else if ("RELOAD_CALENDAR".equals(targetObject)) {
			try {
				controllerMain.getScheduleCalendar().reload();
			}catch(Exception e) {
				Util.logError(log, "refreshMemoryCache() fail."+targetObject+"/"+key, e);
			}
		}else if ("NOTIFY".equals(targetObject)) {
			try {
				monitorMain.getJobNotifyManager().checkAndRefreshCache(true);
			}catch(Exception e) {
				Util.logError(log, "refreshMemoryCache() fail."+targetObject+"/"+key, e);
			}
		}else if ("AGENT_INFO".equals(targetObject)) {
			try {
				monitorMain.getAgentInfoManager().removeCache(key);
			}catch(Exception e) {
				Util.logError(log, "refreshMemoryCache() fail."+targetObject+"/"+key, e);
			}
		}

	}

	/**
	 * peer 에게 Repeat Timer 에 등록된 놈들인지 확인한다.
	 * @param jobinsid list
	 * @return repeat timer 에 등록된 Job Instance Id List
	 */
	public List<String> checkIfExistInRepeatTimer(List<String> jobinsidList) {
		if (jobinsidList == null) return null;
		List<String> returnList = null;
		for (String jobinsid : jobinsidList) {
			if (repeatManager.isScheduledForRepeatTimer(jobinsid)) {
				if (returnList == null) {
					returnList = new LinkedList<String>();
				}
				returnList.add(jobinsid);
			}
		}
		if (log.isDebugEnabled()) {
			Util.logDebug(log, "[PeerServiceImpl] checkIfExistInRepeatTimer() : "+returnList);
		}
		return returnList;
	}

	/**
	 * 해당 에이전트가 내장 에이전트인지 체크.
	 * 
	 * @param agentId
	 * @return IAgentClient
	 */
	private IAgentClient getIfInternalAgent(String agentId) {
		try {
			if (!controllerMain.getAgentInfoManager().isInternalAgent(agentId)) {
				throw new SchedulerException("main.agent.notinternal.error", agentId);
			}
			
			IAgentClient client =  controllerMain.getAgentInfoManager().getAgentClient(agentId);
			if (client == null) {
				throw new SchedulerException("main.agent.notinternal.error", agentId);
			}
			return client;
		}catch(Throwable e) {
			Util.logError(log, String.format("From peer checkIfInternalAgent(%s) fail", agentId), e);
			throw Util.toRuntimeException(e);
		}
	}
	
	/**
	 * 내장 에이전트의 Job Exe State 리턴
	 * @param agentId
	 * @param jobExeId
	 * @return -1 if peer down, other number if peer normal.
	 */
	public int getInternalAgentJobExeState(String agentId, String jobExeId) {
		IAgentClient agentClient = getIfInternalAgent(agentId);
		return agentClient.getJobExeState(jobExeId);
	}
	
	/**
	 * 내장 에이전트에 수행중인 JobExe 들의 Map 을 리턴 
	 * @param agentId 내장 에이전트 ID
	 * @return Map of &lt; jobExeId, JobExeSimple &gt;
	 */
	public Map<String, JobExecutionSimple> getInternalJobExecutionSimpleMap(String agentId) {
		IAgentClient agentClient = getIfInternalAgent(agentId);
		return agentClient.getRunningJobExecutionSimpleMap();
	}

	/**
	 * 내장 에이전트에 수행중인 JobExe 를 강제종료함.
	 * @param agentId
	 * @param jobExeId
	 */
	public void stopInternalJob(String agentId, String jobExeId) {
		IAgentClient agentClient = getIfInternalAgent(agentId);
		Util.logInfo(log, MSG.get("main.jobctl.stop", "Peer", jobExeId));
		agentClient.stop(jobExeId);
	}
	
	/**
	 * 내장 에이전트에 수행중인 JobExe 를 suspend함.
	 * @param agentId
	 * @param jobExeId
	 */
	public void suspendInternalJob(String agentId, String jobExeId) {
		IAgentClient agentClient = getIfInternalAgent(agentId);
		Util.logInfo(log, MSG.get("main.jobctl.suspend", "Peer", jobExeId));
		agentClient.suspend(jobExeId);
	}
	
	/**
	 * 내장 에이전트에 수행중인 JobExe 를 resume함.
	 * @param agentId
	 * @param jobExeId
	 */
	public void resumeInternalJob(String agentId, String jobExeId) {
		IAgentClient agentClient = getIfInternalAgent(agentId);
		Util.logInfo(log, MSG.get("main.jobctl.resume", "Peer", jobExeId));
		agentClient.resume(jobExeId);
	}
	
	/**
	 * 내장 에이전트에 수행중인 Job 의 스레드 stack trace 조회.
	 * @param agentId
	 * @param jobExecutionId
	 */
	public Map getInternalJobExecutionThreadStackTrace(String agentId, String jobExecutionId) {
		return getIfInternalAgent(agentId).getJobExecutionThreadStackTrace(jobExecutionId);
	}

	/**
	 * 내장 에이전트에 수행중인 Job 의 로그레벨 조회.
	 * @param agentId
	 * @param jobExecutionId
	 */
	public String getInternalJobExecutionLogLevel(String agentId, String jobExecutionId) {
		return getIfInternalAgent(agentId).getJobExecutionLogLevel(jobExecutionId);
	}

	/**
	 * 내장 에이전트에 수행중인 Job 의 로그레벨 변경.
	 * @param agentId
	 * @param jobExecutionId
	 */
	public boolean setInternalJobExecutionLogLevel(String agentId, String jobExecutionId, String logLevel) {
		return getIfInternalAgent(agentId).setJobExecutionLogLevel(jobExecutionId, logLevel);
	}


	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 로그 파일명 조회
	 * @param agentId
	 * @param info
	 */
	public String getInternalJobLogFilename(String agentId, JobLogFilenameInfo info) {
		return getIfInternalAgent(agentId).getLogFilename(info);
	}
	
	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 로그 파일 길이 조회
	 * @param agentId
	 * @param info
	 */
	public long getInternalJobLogFileLength(String agentId, String filename) {
		return getIfInternalAgent(agentId).getLogFileLength(filename);
	}
	
	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 SUB 로그 파일명 조회
	 * @param agentId
	 * @param info
	 */
	public String getInternalJobSubLogFilename(String agentId, JobLogFilenameInfo info) {
		return getIfInternalAgent(agentId).getSubLogFilename(info);
	}
	
	/**
	 * 파일이 존재하는지 (로그파일등) 확인함
	 * 내장에이전트의 Job Log 파일 read 용. 
	 * @param agentId
	 * @param filename
	 * @return
	 */
	public boolean isFileExist(String agentId, String filename) {
		if (filename==null) {
			return false;
		}else {
			return new File(filename).exists();
		}
	}
	
	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 로그 read.
	 * 
	 * @param agentId
	 * @param logFilename
	 * @param offset
	 * @param length
	 */
	public ByteArray readInternalJobLogFile(String agentId, String logFilename, int offset, int length) {
		return getIfInternalAgent(agentId).readLogFile(logFilename, offset, length);
	}
	
	/**
	 * peer 의 PreJobWaitingPool, ParallelWaitingPool 의 garbarge 를 정리한다.
	 * @param idListForWaitingPoolCleansing
	 */
	public void cleansingWaitingPool(Set<String> idListForWaitingPoolCleansing) {
		preJobWaitingPool.doCleansing(idListForWaitingPoolCleansing);
		parallelJobWaitingPool.doCleansing(idListForWaitingPoolCleansing);
	}

}
