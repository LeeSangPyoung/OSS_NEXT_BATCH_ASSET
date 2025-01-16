package nexcore.scheduler.controller.internal;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.client.IAgentClient;
import nexcore.scheduler.controller.ha.IPeerClient;
import nexcore.scheduler.core.IMonitorDisplayable;
import nexcore.scheduler.core.internal.JobInstanceManager;
import nexcore.scheduler.core.internal.JobStarter;
import nexcore.scheduler.core.internal.RepeatManager;
import nexcore.scheduler.entity.AgentInfo;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobExecutionSimple;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.monitor.internal.AgentInfoManager;
import nexcore.scheduler.monitor.internal.AgentMonitor;
import nexcore.scheduler.monitor.internal.JobNotifyManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 현재 Running 상태의 Job 들을 주기적으로 Agent 로 부터 체크하여 GHOST 상태인지 아닌지 확인함. </li>
 * <li>작성일 : 2010. 12. 29.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

/*
 * GHOST 상태 : Job 실행 중에 Agent 가 down 되며 JobEnd callback 도 못한 상태. 
 * Agent 가 restart 되도라도 이미 이전 Job 은 실패이지만 정상 Callback을 알 수 없는 상황이므로
 */
public class RunningJobStateMonitor implements Runnable, IMonitorDisplayable {
	private AgentInfoManager          agentInfoManager;
	private AgentMonitor              agentMonitor;
	private JobInstanceManager        jobInstanceManager;
	private JobExecutionManager       jobExecutionManager;
	private JobNotifyManager          jobNotifyManager;
	private RepeatManager             repeatManager;
	private JobStarter                jobStarter;
	private IPeerClient               peerClient;
	private long                      interval = 10000; // 10초 마다 running job들의 상태를 체크함.
	
	private boolean                   closed = false;
	private Map<String, String>       jobInsIdExeIdMap = new ConcurrentHashMap<String, String>(); /* Instance Id로 JobExecution을 찾기 위한 Map */
	private Map<String, JobExecution> runningJobExecutions = new ConcurrentHashMap<String, JobExecution>(); /* <JobExeID, JobExecution> */ 
	private Thread                    thisThread;
	private Log                       log;
	
	public void init() {
		log = LogManager.getSchedulerLog();
		
		thisThread = new Thread(this, "RunningJobStateMonitor");
		thisThread.setDaemon(true);
		thisThread.start();

		Util.logServerInitConsole("RunningJobStateMonitor");
	}
	
	public void destroy() {
		closed = true;
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

	public void setAgentMonitor(AgentMonitor agentJobMonitor) {
		this.agentMonitor = agentJobMonitor;
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

	public JobNotifyManager getJobNotifyManager() {
		return jobNotifyManager;
	}

	public void setJobNotifyManager(JobNotifyManager jobNotifyManager) {
		this.jobNotifyManager = jobNotifyManager;
	}

	public RepeatManager getRepeatManager() {
		return repeatManager;
	}

	public void setRepeatManager(RepeatManager repeatManager) {
		this.repeatManager = repeatManager;
	}

	public JobStarter getJobStarter() {
		return jobStarter;
	}

	public void setJobStarter(JobStarter jobStarter) {
		this.jobStarter = jobStarter;
	}

	public IPeerClient getPeerClient() {
		return peerClient;
	}

	public void setPeerClient(IPeerClient peerClient) {
		this.peerClient = peerClient;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}


	// ############################################################################################
	// ############################################################################################

	// --------------------------------------------------------------------------
	
	public void addRunningJobExecution(JobExecution jobexe) {
		runningJobExecutions.put(jobexe.getJobExecutionId(), jobexe);
		jobInsIdExeIdMap.put(jobexe.getJobInstanceId(), jobexe.getJobExecutionId());
	}
	
	public int getRunningJobExecutionsCount() {
		return runningJobExecutions.size();
	}
	
	public Map<String, JobExecution> getRunningJobExecutionsMap() {
		return new HashMap(runningJobExecutions);
	}
	
	public Collection<JobExecution> getRunningJobExecutions() {
		return runningJobExecutions.values();
	}
	
	public JobExecution getRunningJobExecution(String jobExecutionId) {
		return runningJobExecutions.get(jobExecutionId);
	}
	
	public JobExecution getRunningJobExecutionByJobInsId(String jobInstanceId) {
		String jobExeId = jobInsIdExeIdMap.get(jobInstanceId);
		return jobExeId == null ? null : runningJobExecutions.get(jobExeId);
	}

	public void removeRunningJobExecution(JobExecution jobexe) {
		runningJobExecutions.remove(jobexe.getJobExecutionId());
		if (Util.equalsIgnoreNull(jobexe.getJobExecutionId(), jobInsIdExeIdMap.get(jobexe.getJobInstanceId()))) {
			// jobInsIdExeIdMap 에 value 로 들어있는 값과 비교해서 같을때만 삭제한다.
			// #21417. 에에전트 장애시 강제로 ghost 처리후 다른 에이전트에서 강제실행하면 이런 현상이 발생할 수 있다. 
			jobInsIdExeIdMap.remove(jobexe.getJobInstanceId());
			if (log.isDebugEnabled()) {
				Util.logDebug(log, "[RunningJobStateMonitor] remove ("+jobexe.getJobInstanceId()+"/"+jobexe.getJobExecutionId()+")");
			}
		}else {
			/* 
			 * #21417. 에에전트 장애시 강제로 ghost 처리후 다른 에이전트에서 강제실행하면 이런 현상이 발생할 수 있다.
			 * peer 에서 start 되고 RunningJobStateMonitor 가 queryRunningJobInstance() 를 통해 runningJobExecutions 를 다시 정리하기 전에 먼저 end 되면
			 * 이런 현상 발생할 수 있다.
			 */
			if (log.isDebugEnabled()) {
				Util.logDebug(log, "[RunningJobStateMonitor] remove (JobExeOnly:"+jobexe.getJobExecutionId()+")");
			}
		}
	}
	
	public void removeAll() {
		runningJobExecutions.clear();
		jobInsIdExeIdMap.clear();
	}
	
	/**
	 * NBS_JOB_INS 테이블에서 (S, R, P) 상태인 것들을 조회한다.
	 */
	private void queryRunningJobInstancesFromDB() {
		// #####################  NBS_JOB_INS 테이블 조회 (R,P,S) 상태.
		List<JobInstance> jobInsList = null;
		try {
			jobInsList = jobInstanceManager.getJobInstancesByQuery("WHERE JOB_STATE IN ('R', 'P', 'S') ", ""); 
		}catch (SQLException e) {
			throw new SchedulerException("main.runmon.select.jobins.error", e);
		}
	
		if (jobInsList == null) {
			return;
		}
		
		Map<String, JobInstance> peerRepeatCheckList = new HashMap();
		for (JobInstance jobins : jobInsList ) {
			if (JobInstance.JOB_STATE_SLEEP_RPT.equals(jobins.getJobState())) {
				// Repeat time 등록 여부 체크
				
				if (!repeatManager.isScheduledForRepeatTimer(jobins.getJobInstanceId())) {
					if (DateUtil.getTimestampLong(jobins.getLastModifyTime()) < System.currentTimeMillis() - 70000) {
						// 2013.09.02 DB의 상태는 "S" 이지만 아직 Timer 에 넣기 직전일 수 있으므로 DB 상태가 "S" 로 변경된 최종 시각이 1분 이상 흐른 경우에만, 아래 복구 로직을 돌린다.
						// peer 간 오차 허용치 (60초) 를 고려하여 70초로 함.
						if (peerClient.isPeerExist()) { // 이중화 환경일 경우
							// 나의 Repeat timer 에는 없으므로, peer 에게 물어봐야한다. 일단 list 에 넣어놓고 한꺼번에 물어보자
							peerRepeatCheckList.put(jobins.getJobInstanceId(), jobins);
						}else { // 단일 환경일 경우, repeatTimer 에 체크&등록한다.
							try {
								/*
								 * 2013.09.05
								 * timer 에 복구 하기 전에 DB 상태 한번더 검색해본다. 저 위에서 select 하던 그 모습 그대로인지?
								 * 복구대상이 아니라 단순 시간차 문제일 수 있으므로 한번 더 체크한다.
								 * 위에서 쿼리순간에는 "S" 이 었으나
								 * 그 순간 다른 쓰레드에서 timer 에서 깨어나고 바로 ask 를 수행하여 "W" 상태로 바꾸고 timer 에서는 remove 하는 동작이 먼저 수행되는 경우가 있을수 있다.
								 * 이런 경우는 timer 에 없기 때문에 여기까지 들어올 수 있다.
								 * 이를 보완하기 위해 Job 상태가 "S" 인지 다시 확인하고 LastModifyTime 확인하여 이전과 다르다면 그냥 pass한다.
								 */
								JobInstance jobinsNew = jobInstanceManager.getJobInstance(jobins.getJobInstanceId());
								if (jobinsNew.getLastModifyTime().equals(jobins.getLastModifyTime()) && 	JobInstance.JOB_STATE_SLEEP_RPT.equals(jobinsNew.getJobState())) {
									Util.logInfo(log, "[RunningJobMonitor] RepeatJob recovered. ["+jobins.getJobInstanceId()+"]"); 
									repeatManager.checkAndScheduleForRepeat(jobins.getJobInstanceId(), 
											Util.parseYYYYMMDDHHMMSS(jobins.getLastStartTime()), 0, JobInstance.JOB_STATE_SLEEP_RPT);
								}else {
									Util.logDebug(log, 
										"[RunningJobMonitor] RepeatJob recovery canceled_1. (lastModifyTime changed) "+
										jobins.getLastModifyTime()    + " >  " +
										jobinsNew.getLastModifyTime() + " ["+jobins.getJobInstanceId()+"]");
								}
								
							}catch(Throwable e) {
								Util.logError(log, "[RunningJobMonitor] RepeatJob recovered fail. ["+jobins.getJobInstanceId()+"]", e); 
							}
						}
					}else {
						// 2013.09.02 DB 상태는 "S" 이고, Timer 에도 없지만, 최종변경시각 (즉 "S" 상태가 된지) 1분이 안된 경우이므로 좀더 기다려본다.
						Util.logDebug(log, 
								"[RunningJobMonitor] RepeatJob recovery candidate. lastModifyTime="+
								jobins.getLastModifyTime()+". ["+jobins.getJobInstanceId()+"]");
					}
				}
				
			}else if (JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {
				try {
				
					// running list 에 추가함.
					if ("-".equals(jobins.getLastJobExeId())) {
						/* 
						 * Running 준비 상태. RunQueue 를 확인하여 실행 대기중인지 확인하고
						 * RunQueue 에 없으면 last start 시각을 체크하여 3분 이상 이 상태가 지속된다면
						 * RunQueue 에 들어있는 중에 스케줄러 서버가 down 되거나 기타 장애가 발생한 상황이다.
						 * 이럴때는 상태를 Wait 로 바꿔서 다시 정상 실행되도록 유도한다. 
						 */
						if (!jobStarter.existsInRunQueue(jobins.getJobInstanceId()) &&
							Util.parseYYYYMMDDHHMMSS(jobins.getLastStartTime()) + 180000 < System.currentTimeMillis()) { // 3 분 이상 흘렀다면. Wait 상태로 변경하여 다시실행하도록 유도한다.
								jobInstanceManager.setJobStateWithCheck(jobins.getJobInstanceId(), jobins.getJobState(), JobInstance.JOB_STATE_WAIT, "Retry");
							Util.logInfo(log, String.format("(%s) LastJobExeId is '-' for a long time, change to Wait state", jobins.getJobInstanceId()));
						}
						continue;
						
					}
					if (!runningJobExecutions.containsKey(jobins.getLastJobExeId())) {
						/*
						 * 1. peer 에서 실행중이거나, 
						 * 2. reboot 되는 과정에서 처음 running list 를 만드는 경우.
						 * 
						 * runnlig list 에 넣는다.
						 */
						JobExecution jobexe = jobExecutionManager.getJobExecution(jobins.getLastJobExeId());
						if (jobexe != null && jobexe.getState() == JobExecution.STATE_RUNNING) { // 아직 실행중
							addRunningJobExecution(jobexe);
						}else {
							/*
							 * iterate 중에 종료되서 EXE 테이블은 ENDED 로 변경된 상황이거나
							 * ControllerMain 에서 orderJobExecutionToAgent() 메소드에서 JobIns, JobExe 테이블은 update/insert 후에
							 * 아직 AgentClient.start() 호출까지 못 온 상태.
							 * ==> 조용히 넘어감. 
							 */
							continue;
						}
					}
				
					// 정상 실행 중인 것 중에 하루 이상 수행중인놈이 있는지 확인. LastModifyTime 을 당일로 바꾸어주자. 오늘자 조회 화면에 나오게 하기 위해
					/* 2013.08.30 조회일 기준으로 조회하지 않고 생성일,처리일로 조회방식을 변경하였으므로 이 로직은 필요없다. 3.6.3  
					if (runningJobExecutions.containsKey(jobins.getLastJobExeId())) {
						if ((System.currentTimeMillis() - jobins.getLastModifyTime().getTime()) > 86400000l) {
							Util.logError(log, MSG.get("main.runmon.update.lastmodifytime.over24", jobins.getJobInstanceId()));
							jobInstanceManager.updateJobLastModifyTime(jobins.getJobInstanceId(), System.currentTimeMillis());
						}
					}
					*/
				}catch(Throwable e) {
					Util.logError(log, "[RunningJobMonitor] queryRunningJobInstancesFromDB fail. ["+jobins.getJobInstanceId()+"]", e); 
				}
			}
		}

		/*
		 * "S" 이며 나의 Repeat timer에는 없는 놈들을 여기서 한꺼번에 peer 에게 물어본다.
		 * peer 통신이 비정상이라 delay 가 걸릴 경우 건건이 통신하면 delay 가 너무 오래 걸리므로 
		 * 이렇게 묶어서 한번만 통신한다.
		 */
		if (peerRepeatCheckList.size() > 0) {
			List<String> existListInPeer = peerClient.checkIfExistInRepeatTimer(new LinkedList(peerRepeatCheckList.keySet()));
			if (existListInPeer != null) {
				// peer 에 이미 Repeat timer 에 등록돼있는건 내 timer 에 중복 등록하지 않는다.
				for (String jobinsId : existListInPeer) {
					peerRepeatCheckList.remove(jobinsId);
				}
			}
				
			// peer 가 down 상태이면 전부 나의 repeat time 에 넣는다.
			for (JobInstance jobins : peerRepeatCheckList.values()) {
				try {
					/*
					 * 2013.09.05
					 * timer 에 복구 하기 전에 DB 상태 한번더 검색해본다. 저 위에서 select 하던 그 모습 그대로인지?
					 * 복구대상이 아니라 단순 시간차 문제일 수 있으므로 한번 더 체크한다.
					 * 위에서 쿼리순간에는 "S" 이 었으나
					 * 그 순간 다른 쓰레드에서 timer 에서 깨어나고 바로 ask 를 수행하여 "W" 상태로 바꾸고 timer 에서는 remove 하는 동작이 먼저 수행되는 경우가 있을수 있다.
					 * 이런 경우는 timer 에 없기 때문에 여기까지 들어올 수 있다.
					 * 이를 보완하기 위해 Job 상태가 "S" 인지 다시 확인하고 LastModifyTime 확인하여 이전과 다르다면 그냥 pass한다.
					 */
					JobInstance jobinsNew = jobInstanceManager.getJobInstance(jobins.getJobInstanceId());
					if (jobinsNew.getLastModifyTime().equals(jobins.getLastModifyTime()) && JobInstance.JOB_STATE_SLEEP_RPT.equals(jobinsNew.getJobState())) {
						Util.logInfo(log, "[RunningJobMonitor] Peer's RepeatJob scheduled to me. ["+jobins.getJobInstanceId()+"]"); 
						repeatManager.checkAndScheduleForRepeat(jobins.getJobInstanceId(), 
								Util.parseYYYYMMDDHHMMSS(jobins.getLastStartTime()), 0, JobInstance.JOB_STATE_SLEEP_RPT);
					}else {
						Util.logDebug(log, 
							"[RunningJobMonitor] RepeatJob recovery canceled_2. (lastModifyTime changed) "+
							jobins.getLastModifyTime()    + " >  " +
							jobinsNew.getLastModifyTime() + " ["+jobins.getJobInstanceId()+"]");
					}
				}catch(Throwable e) {
					Util.logError(log, "[RunningJobMonitor] Peer's RepeatJob reschedule fail. ["+jobins.getJobInstanceId()+"]", e); 
				}
			}
		}
	}

	/**
	 * Ghost 여부 체크하여 Ghost 상태 처리함.
	 */
	private void doCheckGhost() {
		for (JobExecution jobexe : runningJobExecutions.values()) {
			try {
				int agentJobExeState = getJobExecutionState(jobexe.getJobExecutionId(), jobexe.getJobInstanceId(), jobexe.getAgentNode());
				if (agentJobExeState == -1) {
					continue; // 통신에러시에는 그냥 skip 하고 다음에 다시한다.
				}else if (agentJobExeState == JobExecution.STATE_UNKNOWN) { 
					// 에이전트에 미존재. (이미 완료, 에이전트 장애 발생후 reboot 됨. ghost 대상 )
					// JobExe 테이블에 미존재하는 경우는 여기서는 발생하지 않음. 위에서 다 걸려짐.
					int jobexeStatFromDB = jobExecutionManager.getJobExecutionState(jobexe.getJobExecutionId());
					if (jobexeStatFromDB == JobExecution.STATE_ENDED) { // ●●●● : 이미 완료된 상태.
						// peer node 에서 callbackJobEnd 처리한 경우, 또는 위 iterate 중에 callbackJobEnd 된 경우. 
						// 정상 적인 경우이므로 running list 에서만 삭제하고 끝냄. 
						Util.logInfo(log, MSG.get("main.runmon.ended.remove.runlist", jobexe.getJobInstanceId()+"/"+jobexe.getJobExecutionId())); // [실행모니터] {0} 는 종료 상태이므로 실행 리스트에서 삭제합니다
						removeRunningJobExecution(jobexe);
					}else { // ●●●● : 에이전트에서 실행된 후 장애 발생한 후 reboot 된 상태. GHOST 대상
						/*
						 * 2013.08.27
						 * ControllerMain.orderJobExecutionToAgent() 안에서
						 * runningJobStateMonitor.addRunningJobExecution(jobexe); 직후에
						 * agent.start() 에서 약간의 시간이 걸리는데 그 와중에 doCheckGhost() 가 먼저 돌면 Ghost 상태가 될 가능성이 있다.
						 * 이런 경우는 Ghost 가 된 이후에 정상적인 실행이 이루어 지고 정상적인 callback 이 이루어 지더라도 Ghost 로 계속 남게 된다.
						 * 이런 문제를 해결하기 위해 start 이후 1분 지난 것들에 대해서만 Ghost 판정을 하게 한다.
						 */
						if (jobexe.getStartTime() < System.currentTimeMillis() - 70000) { // 위 comment 와 같은 이유로 start 한지 1 분 이상 된 경우만 Ghost 판정한다. 2013-08-27. 
							// peer 간 시간차이 허용치 (60초) 를 고려하여 60초에서 70초로 변경한다. 
							Util.logInfo(log, "[RunningJobStateMonitor] change to ghost. ["+jobexe.getJobInstanceId()+"/"+jobexe.getJobExecutionId()+"]");
							changeToGhost(jobexe.getJobInstanceId(), MSG.get("main.jobcause.jobexe.missing"));
							removeRunningJobExecution(jobexe);
						}
					}
				}else {
					// 정상.
				}
			}catch(Throwable e) {
				Util.logError(log, "[RunningJobStateMonitor] Check ghost state fail.", e); 
			}
		}
	}
	
	/**
	 * Long Run Job 처리
	 */
	private void doCheckLongRun() {
		try {
			jobNotifyManager.processJobLongRun(runningJobExecutions);
		} catch (Exception e) {
			Util.logError(log, "[RunningJobStateMonitor] Failed to check Long Run Job.", e); 
		}
	}

	/**
	 * JobExecution 상태 리턴.
	 * AgentMonitor, 에이전트, Peer 에서 차례로 조회한다.
	 * 
	 * @param jobExeId
	 * @param jobInsId
	 * @param agentId
	 * @return -1 if communication error, 99 (UNKONWN) if not exist, others if jobexe exists
	 */
	private int getJobExecutionState(String jobExeId, String jobInsId, String agentId) throws SQLException {
		// 1.●●● AgentMonitor 에서 먼저 찾아본다. (메모리 캐쉬)
		Map<String, JobExecutionSimple> jobexeMap = agentMonitor.getJobExecutions(agentId);
		if (jobexeMap != null) {
			JobExecutionSimple jes = jobexeMap.get(jobExeId);
			if (jes != null) {  // 없다. 에이전트 메모리에는 없다. (정상으로 끝났거나, ghost 상황이거나)
				return jes.getState();
			}
		}
		
		// 2.●●● AgentMonitor 에 없을 경우 에에전트에 직접 호출해서 찾아봄.
		try {
			// ghost 가능성이 있는 상태. 에이전트에 직접 통신해본다.
			IAgentClient client = agentInfoManager.getAgentClient(agentId);
			if (client != null) {
				int state = client.getJobExeState(jobExeId);

				// 3.●●● 에이전트에도 없을 경우 internal 인지 확인하여 Peer 의 Internal 에 있는건지 물어봄
				if (peerClient.isPeerExist() && state == JobExecution.STATE_UNKNOWN) {
					AgentInfo agent = agentInfoManager.getAgentInfo(agentId);
					if (agent != null && agent.isInternal()) {
						return peerClient.getInternalAgentJobExeState(agentId, jobExeId);
					}
				}else {
					return state;
				}
			}
		}catch(Throwable e) {
			// 통신에러시에는 ghost 로 판정하기에는 이르다. -1 리턴하여 보류한다. 통신이 복구된 후에 정확하게 ghost 판정을 다시 한다.
			Util.logError(log, String.format("[RunningJobStateMonitor] getJobExecutionState(%s, %s) ", jobExeId, agentId) + e.toString()); // 통신에러 로그가 너무 많이 남지 않게 하기 위해 printStackTrace() 는 하지 않도록 한다.
			jobInstanceManager.setJobStateWithCheck(jobInsId, JobInstance.JOB_STATE_RUNNING, JobInstance.JOB_STATE_RUNNING,e.getMessage());
			return -1; 
		}

		
		// 이런 경우는 ghost 판정 대상이 된다.
		return JobExecution.STATE_UNKNOWN;
	}

	/**
	 * Ghost 상태로 변경함.
	 * 
	 * @param jobInsId
	 * @param newStateReason
	 * @throws SQLException
	 */
	public void changeToGhost(String jobInsId, String newStateReason) throws SQLException {
		if (!jobInstanceManager.setJobStateWithCheck(jobInsId, JobInstance.JOB_STATE_RUNNING, JobInstance.JOB_STATE_GHOST, newStateReason)) {
			if (!jobInstanceManager.setJobStateWithCheck(jobInsId, JobInstance.JOB_STATE_SUSPENDED, JobInstance.JOB_STATE_GHOST, newStateReason)) {
				Util.logError(log, MSG.get("main.runmon.set.ghost.error.state.inconsistent", jobInsId));
				return;
			}
		}
		
		// 반복이며 REPEAT_IF_ERROR_IGNORE 일 경우는 Ghost 로 변경 직후 바로 Wait 로 하여 다시 돌도록 한다. 
		JobInstance jobins = jobInstanceManager.getJobInstance(jobInsId);
		if ("Y".equals(jobins.getRepeatYN()) && JobInstance.REPEAT_IF_ERROR_IGNORE.equals(jobins.getRepeatIfError())) {
			if (jobInstanceManager.setJobStateWithCheck(jobInsId, JobInstance.JOB_STATE_GHOST, JobInstance.JOB_STATE_WAIT, "Ignore Error for Repeat")) {
				Util.logInfo(log, "Change to Wait for repeat ["+ jobInsId+"]");
			}
		}
	}
	
	public void run() {
		long errorCount = 0;
		
		// 1분마다 Long Run Job 체크를 위한 변수 선언
		long lastLongRunCheckTime = System.currentTimeMillis();
		long currentTime;
		while(!closed && !Thread.interrupted()) {
			try {
				queryRunningJobInstancesFromDB();
				doCheckGhost();
				
				// 최근 체크 시간이 현재 시간과 1분 이상 차이가 날 경우
				currentTime = System.currentTimeMillis();
				if(currentTime - lastLongRunCheckTime >= 60000){
					doCheckLongRun();
					lastLongRunCheckTime = currentTime;
				}
				
				if (errorCount > 0) {
					// 장애 상황에서 복구된 케이스. 로그 찍어라.
					errorCount = 0;
					Util.logInfo(log, MSG.get("main.runmon.work.error.recovered"));
				}
			}catch(Throwable e) {
				errorCount++;
				Util.logError(log, MSG.get("main.runmon.work.error"), e);
			}finally {
				if (errorCount >= 10) {
					// 10 번 연속 에러가 나는 경우는 장시간 (1분정도) sleep 한다. DB 가 다운되면 이런 현상이 발생한다. 너무 많은 로그가 남는 것을 방지하기 위한 조치
					Util.sleep(60000);
				}else {
					Util.sleep(interval);
				}
			}
		}
	}
	
	public String getDisplayName() {
		return "RunningJobMonitor";
	}
	
	public String getDisplayString() {
		return runningJobExecutions.size() + " : " + runningJobExecutions.keySet();
	}
}

