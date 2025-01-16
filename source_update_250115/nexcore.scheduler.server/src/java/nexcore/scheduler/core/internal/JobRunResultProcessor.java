package nexcore.scheduler.core.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

import com.ibatis.sqlmap.client.SqlMapClient;

import nexcore.scheduler.controller.IJobEndPostProcessor;
import nexcore.scheduler.controller.ha.IPeerClient;
import nexcore.scheduler.controller.internal.JobExecutionManager;
import nexcore.scheduler.controller.internal.RunningJobStateMonitor;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.monitor.internal.JobNotifyManager;
import nexcore.scheduler.monitor.internal.JobProgressStatusManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 실행 완료시 (정상종료, 에러종료) 그 후 처리를 담당함 </li>
 * <li>작성일 : 2010. 5. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
/*
 * 1. Job Instance Manager 를 이용해서 Job State 변경함
 * 
 * 2. Parallel Running Counter 에서 하나 빼기
 * 
 * 3. Pre job waiting board 에서 wait 하고 있는 Job 에게 notify 함
 * 
 * 4. Parallel waiting board 에서 wait 하고 있는 Job 에게 notify 함. 
 * 
 * 5. If Repeat 처리.
 */

public class JobRunResultProcessor {
	private JobInstanceManager       jobInstanceManager;
	private JobExecutionManager      jobExecutionManager;
	private ParallelRunningCounter   parallelRunningCounter;
	private PreJobWaitingPool        preJobWaitingPool;
	private ParallelJobWaitingPool   parallelJobWaitingPool;
	private JobStarter               jobStarter;
	private RepeatManager            repeatManager;
	private RunningJobStateMonitor   runningJobStateMonitor;
	private Activator                activator;
	private JobNotifyManager         jobNotifyManager;
	private IJobEndPostProcessor     jobEndPostProcessor;
	private JobProgressStatusManager jobProgressStatusManager;
	private SqlMapClient             sqlMapClient;
	private IPeerClient              peerClient;
	private boolean                  closed = false;
	
	private Log                      log;
	
	public void init() {
		log = LogManager.getSchedulerLog();
		Util.logServerInitConsole("JobRunResultProcessor");
	}
	
	public void destroy() {
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

	public ParallelRunningCounter getParallelRunningCounter() {
		return parallelRunningCounter;
	}

	public void setParallelRunningCounter(ParallelRunningCounter parallelRunningCounter) {
		this.parallelRunningCounter = parallelRunningCounter;
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

	public JobStarter getJobStarter() {
		return jobStarter;
	}

	public void setJobStarter(JobStarter jobStarter) {
		this.jobStarter = jobStarter;
	}

	public RepeatManager getRepeatManager() {
		return repeatManager;
	}

	public void setRepeatManager(RepeatManager repeatManager) {
		this.repeatManager = repeatManager;
	}

	public RunningJobStateMonitor getRunningJobStateMonitor() {
		return runningJobStateMonitor;
	}

	public void setRunningJobStateMonitor(RunningJobStateMonitor runningJobStateMonitor) {
		this.runningJobStateMonitor = runningJobStateMonitor;
	}

	public Activator getActivator() {
		return activator;
	}

	public void setActivator(Activator activator) {
		this.activator = activator;
	}

	public JobNotifyManager getJobNotifyManager() {
		return jobNotifyManager;
	}

	public void setJobNotifyManager(JobNotifyManager jobNotifyManager) {
		this.jobNotifyManager = jobNotifyManager;
	}

	public IJobEndPostProcessor getJobEndPostProcessor() {
		return jobEndPostProcessor;
	}

	public void setJobEndPostProcessor(IJobEndPostProcessor jobEndPostProcessor) {
		this.jobEndPostProcessor = jobEndPostProcessor;
	}

	public JobProgressStatusManager getJobProgressStatusManager() {
		return jobProgressStatusManager;
	}

	public void setJobProgressStatusManager(JobProgressStatusManager jobProgressStatusManager) {
		this.jobProgressStatusManager = jobProgressStatusManager;
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public IPeerClient getPeerClient() {
		return peerClient;
	}

	public void setPeerClient(IPeerClient peerClient) {
		this.peerClient = peerClient;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	//====================================================================================
	//====    Agent Call back 처리. 
	//====================================================================================
	/**
	 * Agent로 부터 수신되는 JobEnd 신호를 받아 End 처리함.
	 */
	public boolean callBackJobEnd(JobExecution jobexe) {
		log.info(MSG.get("main.endproc.callbackend", jobexe.getJobExecutionId(), jobexe.getAgentNode(), jobexe.getReturnCode())); // {1}에서 실행된 {0} 이 종료됩니다. 리턴코드:{2} 
		if (log.isDebugEnabled()) {
			log.debug("Job end callback detail="+jobexe);
		}
		
		if (closed) {
			Util.logError(log, MSG.get("main.endproc.error.because.closed"));  // 스케줄러가 CLOSE 상태이므로 콜백 처리를 수행할 수 없습니다
			return false;
		}
		try {
			if (updateJobStateForEnd(jobexe)) {
				doJobEndPostProcess(jobexe);
				
				if (peerClient.isPeerExist()) {
					peerClient.callBackJobEnd(jobexe); // peer 에게도 callback 해서 메모리 정리하게 한다.
				}
				
				try {
					if (jobEndPostProcessor != null) {
						jobEndPostProcessor.doPostProcess(jobexe);      // Job 종료후 후처리 모듈이 설정되어 있으면 돌려라. 
					}
				}catch(Throwable ignore) { // 후처리 중 End 처리를 false 시키면 안됨. 로그 남기고 버린다.
					Util.logError(log, MSG.get("main.endproc.postproc.error", jobexe.getJobExecutionId()), ignore); // 후처리 실행 중 에러가 발생하였습니다
				}

				try {
					if (jobNotifyManager!=null) {
						jobNotifyManager.processJobEndCallback(jobexe); // Job 종료후 통지 로직을 수행함.
					}
				}catch(Throwable ignore) { // 통지 중 에러 발생했다고 해서 End 처리를 false 시키면 안됨. 로그 남기고 버린다.
					Util.logError(log, MSG.get("main.endproc.notify.error", jobexe.getJobExecutionId()), ignore); // Job 통지 수행중 에러가 발생하였습니다
				}
				return true;
			}else {
				if (jobExecutionManager.getJobExecutionState(jobexe.getJobExecutionId()) == JobExecution.STATE_ENDED) {
					// 이미 callbackJobEnd 정상처리됐는데 다시 callback호출된 경우. Agent 에서는 여러번 callback할 수 있으므로 이런 현상 발생할 수 있음.
					/*
					 * 2013-08-12. Ghost 전환후 callback 이 들어온 경우. 이런 경우는 JobExe 테이블만 update 하고, 그 후속 처리를 하지 않는다.
					 * Ghost 로 강제 전환된 경우는 뭔가 비상상황이므로 정상적인 후속 처리를 하지 않는다.
					 */
					return true;
				} // else 이면 밑에서 return false 한다.
			}
		}catch (Throwable e) {
			Util.logError(log, MSG.get("main.endproc.callbackend.error", jobexe.getJobExecutionId()), e); // {0} 의 종료 콜백 처리 중 에러가 발생하였습니다.
		}
		return false; // 에러가 나면 다시 callback 하도록 함.
	}
	
	public boolean callBackJobSuspend(JobExecution jobexe) {
		log.info(MSG.get("main.endproc.callbacksuspend", jobexe.getJobExecutionId(), jobexe.getAgentNode())); // {1}에서 실행된 {0} 이 일시정지됩니다 
		if (log.isDebugEnabled()) {
			log.debug("Job suspended callback detail="+jobexe);
		}

		if (closed) {
			Util.logError(log, MSG.get("main.endproc.error.because.closed"));  // 스케줄러가 CLOSE 상태이므로 콜백 처리를 수행할 수 없습니다
			return false;
		}
		try {
		    boolean updateStateOk = jobInstanceManager.setJobStateWithCheck(jobexe.getJobInstanceId(), JobInstance.JOB_STATE_RUNNING, JobInstance.JOB_STATE_SUSPENDED, "Suspended");
		    if (!updateStateOk) {
			    log.warn("Job suspended callback. Job status change fail. Before status is not RUNNING. "+jobexe.getJobInstanceId());
			}
			return true;
		}catch (Exception e) {
			Util.logError(log, MSG.get("main.endproc.callbacksuspend.error", jobexe.getJobExecutionId()), e); // {0} 의 일시정지 콜백 처리 중 에러가 발생하였습니다.
			return false;
		}
	}
	

	public boolean callBackJobResume(JobExecution jobexe) {
		log.info(MSG.get("main.endproc.callbackresume", jobexe.getJobExecutionId(), jobexe.getAgentNode())); // {1}에서 일시정지 중인 {0} 을 계속 실행합니다 
		if (log.isDebugEnabled()) {
			log.debug("Job resumed callback detail="+jobexe);
		}

		if (closed) {
			Util.logError(log, MSG.get("main.endproc.error.because.closed"));  // 스케줄러가 CLOSE 상태이므로 콜백 처리를 수행할 수 없습니다
			return false;
		}
		try {
		    boolean updateStateOk = jobInstanceManager.setJobStateWithCheck(jobexe.getJobInstanceId(), JobInstance.JOB_STATE_SUSPENDED, JobInstance.JOB_STATE_RUNNING, "Resumed");
            if (!updateStateOk) {
                log.warn("Job suspended callback. Job status change fail. Before status is not SUSPENDED. "+jobexe.getJobInstanceId());
            }
			return true;
		}catch (Exception e) {
			Util.logError(log, MSG.get("main.endproc.callbackresume.error", jobexe.getJobExecutionId()), e); // {0} 의 계속실행 콜백 처리 중 에러가 발생하였습니다.
			return false;
		}
	}

	//====================================================================================
	//====    Agent Call back 처리. 
	//====================================================================================

	/**
	 * Job End 상태 처리를 위해 JobExe, JobIns 테이블 상태값 변경, 병렬처리 값 변경등등 update함.
	 * @return true if 정상 처리.
	 */
	private boolean updateJobStateForEnd(JobExecution jobexe) {
		try {
			sqlMapClient.startTransaction();
			
			int returnCode = jobexe.getReturnCode();
			
			// 1. JobExecution 테이블 update.
			jobexe.setState(JobExecution.STATE_ENDED); // 에이전트에서 이미 이렇게 세트되어 넘어오지만, 확실하게 처리하기 위해 한번 더 세트한다. suspend 감지 쓰레드에서 꼬임 방지목적
			boolean result = jobExecutionManager.updateJobExecutionForJobEnd(jobexe);
			if (!result) {
				// 이미 한번 callback 처리된 경우 이상황이 됨. 
				log.info(MSG.get("main.endproc.callbackend.maybe.already.done", jobexe.getJobExecutionId())); // {0}은 이미 종료 콜백 처리가 완료된 상태일 수 있습니다
				return false;
			}
			
			// PostJobTrigger 를 읽어야하므로 DEEP 으로 읽는다. 2016.7.26
			JobInstance jobins = jobInstanceManager.getJobInstanceDeep(jobexe.getJobInstanceId());
			
			// 2. JobInstance 테이블 update
			/*
			 * endTime을 에이전트 시각으로 하지 않고 스케줄러 시각으로 한다. 
			 * 만약 장애로 인해 오랜시간 후에 복구가 되면 그 복구된 시점의 시각으로 end 시간이 찍히는 문제가 있긴하지만. 
			 * Job Instance 의 start,end 시각은 스케줄러 시각으로 한다는 원칙이 정당하다.
			 * 
			 * 이런 경우 JobExecution 의 start, end 는 에이전트의 실제 시각으로 찍히므로 이 시각을 대안으로 사용하도록 한다.  
			 */
			if (JobInstance.JOB_STATE_GHOST.equals(jobins.getJobState()) ||
				!(JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) || /* 2013-10-07. 실행중에서 임의로 상태 조작을 한 경우이므로 그상태를 보존한다. */
				!jobInstanceManager.setJobStateForEnd(jobexe.getJobInstanceId(), returnCode==0, returnCode==0 ? null : "Return Code="+returnCode+"/"+jobexe.getErrorMsg(), System.currentTimeMillis(), false, null, jobexe.getJobExecutionId())) {
				/*
				 * 2013-08-12. #21417
				 * GHOST 상태인 경우는 JobExe 테이블만 update 하고 후속 처리는 하지 않는다.
				 * Ghost 상태로 전환되는 경우는, 
				 *    1) 에이전트 장애 & reboot 으로 인해 에이전트 JVM 에서 돌던 Job 들이 날라갔을 경우.
				 *       에이전트 reboot 후 스케줄러와 상태 체크 과정에서 자동으로 ghost 처리된다. 이 경우는 정상 callbackJobEnd() 가 들어올 가능성이 없다.
				 *    2) 에이전트 장애 (또는 네트워크 장애) 로 인해 수동(강제)으로 Running 상태를 Ghost 로 전환한 경우,
				 *       (강제 Ghost 처리후 백업 에이전트에서 실행 시킬 목적)
				 *       이 경우는 나중에 에이전트와의 네트워크가 정상화 되어 정상 callbackJobEnd() 가 들어올 가능성이 있다.
				 *       이 경우는 정상으로 들어온 callbackJobEnd() 의 JobExe로 JobExe 테이블에만 Update 하고 후속 조치는 취하지 않는다.
				 *       이미 백업 에이전트로 Running 을 준비하고 있을 수 있기 때문에.
				 *       
				 *       이미 백업 에이전트로 실행하기 위해 Running 으로 변경된 경우라면 LastJobExeId 가 이전 실행의 값과 다른값이므로 setJobStateForEnd() = false 가 된다. 
				 */
				/*
				 * 2013-08-07. #21417
				 * JobInstance 상태 변경 에러. running 중에 강제로 Ghost 로 수동 변경했을 가능성이 크다. (에이전트 장애로 인해)
				 * 이 경우는 아래의 3,4번 처리흐름을 하지 않고 JobExecution 테이블 update 만 commit 하고 끝낸다.
				 */
				Util.logError(log, MSG.get("main.endproc.lastjobexeid.error", jobexe.getJobExecutionId())); // {0} 의 종료 콜백 처리 전에 수동으로 상태가 변경됐습니다
				
				// 6. DB commit
				sqlMapClient.commitTransaction();
				return false;
			}
				
			// 3. Running Job State Monitor 에서 빼내기.
			runningJobStateMonitor.removeRunningJobExecution(jobexe);
			
			// 3.2 JobProgressStatus 의 progress 값 업데이트.
			jobProgressStatusManager.updateRunningJobExecution(jobexe);

			// 4. Trigger Job 처리.
			// 4.0 부터는 parent Job 의 결과에 따른 멀티 분기 처리 고도화. 2016.7.26 
			List<PostJobTrigger> triggerList = TriggerProcessor.selectTrigger(jobexe.getJobInstanceId(), jobexe.getJobId(), jobins.getTriggerList(), returnCode==0, jobexe.getReturnValues());
			List<JobInstance> triggeredJobInsList = new ArrayList<JobInstance>();
			for (PostJobTrigger trigger : triggerList) {
				for (int i=1; i<=trigger.getJobInstanceCount(); i++) {
					JobInstance newJobIns = activateTriggerJob(jobins.getJobInstanceId(), jobexe.getJobExecutionId(), trigger.getTriggerJobId(), jobexe.getProcDate(), trigger.getJobInstanceCount(), i);
					if (newJobIns != null) {
						triggeredJobInsList.add(newJobIns);
					}
				}
			}

			// 6. DB commit
			sqlMapClient.commitTransaction();
			
			// 7. commit 후에 trigger로 생성된 JobInstance 들을 ask 한다.
			jobStarter.askToStart(triggeredJobInsList);
			return true;
		}catch (Exception e) {
			Util.logError(log, MSG.get("main.endproc.callbackend.updatestate.error", jobexe.getJobExecutionId()), e); // {0}의 종료 콜백 처리 중 인스턴스 상태 변경 중 에러가 발생하였습니다
			return false;
		}finally {
			try {
				sqlMapClient.endTransaction();
			}catch(Exception ignore) {
			}
		}
	}
	
	/**
	 * Job End 시 후처리로 실행되는 로직. 
	 * 후행Job들 깨움, 병렬대기Job들 깨움, 반복처리, 트리거 실행
	 * @param jobexe
	 */
	private void doJobEndPostProcess(JobExecution jobexe) throws SQLException {
		// 1. Pre job waiting pool 에서 wait 하고 있는 Job 들을 다시 check 하여 실행할 놈은 실행하도록 함
		awakePreJobWaitingInstances(jobexe.getJobId(), jobexe.getProcDate());
		
		// 2. Parallel waiting pool 에서 wait 하고 있는 Job 들을 다시 check 하여 실행할 놈은 실행하도록 함
		awakeParallelWaitingInstances(jobexe);
		
		// 3. Repeat 처리. 
		// 에이전트와 스케줄러가 다른 서버에 있을때 시스템 시각이 다를 수 있다.
		// JobExecution의 getStartTime() 은 에이전트의 시각이며
		// 여기서는 스케줄러의 시각이 필요하다. 이를 위해 Job Instance 의 getLastStartTime 이 필요하다.
		JobInstance jobins = jobInstanceManager.getJobInstance(jobexe.getJobInstanceId()); // 2013.7.29 #21394. getJobInstanceNoParams 에서 getJobInstance로 변경. 불필요하게 PREJOB 테이블 select 를 하지 않기 위해.
		if (JobInstance.Y.equalsIgnoreCase(jobins.getRepeatYN())) { // 반복=Y 인 경우만 아래 메소드로 들어간다.
			repeatManager.checkAndScheduleForRepeat(jobexe.getJobInstanceId(), Util.parseYYYYMMDDHHMMSS(jobins.getLastStartTime()), jobexe.getReturnCode(), 
				jobexe.getReturnCode()==0 ? JobInstance.JOB_STATE_ENDED_OK : JobInstance.JOB_STATE_ENDED_FAIL);
		}
		
	}

	/**
	 * 선행 Job 종료 대기중인 후행 Job 들을 깨워 실행되도록 한다. 
	 * @param endedJobId
	 * @param procDate
	 * 
	 * @param returnCode
	 */
	public void awakePreJobWaitingInstances(String endedJobId, String procDate) throws SQLException {
		String[] waitingJobIds = preJobWaitingPool.getWaitingJobIdFor(endedJobId, procDate);
		
		for (String waitingJobId : waitingJobIds) {
			// 동일 Job Id 로 여러개의 Instance 가 존재할때 이들 모두를 실행시킨다.
			List<Map> jobInstances = jobInstanceManager.getJobInstancesStateByJobId(waitingJobId, procDate);
			
			// {"JOB_INSTANCE_ID", "JOB_ID", "JOB_STATE", "LAST_JOB_EXE_ID"}
			
			for (Map jobins : jobInstances) {
				jobStarter.askToStart((String)jobins.get("JOB_INSTANCE_ID"));
			}
		}
	}
	
	/**
	 * Parallel wait 대기중인 Job 들을 깨워 실행되도록 한다. 
	 * @param jobexe
	 * @param returnCode
	 */
	public void awakeParallelWaitingInstances(JobExecution jobexe) throws SQLException {
		JobInstance jobins = jobInstanceManager.getJobInstance(jobexe.getJobInstanceId());
		awakeParallelWaitingInstances(jobins.getParallelGroup());
	}

	/**
	 * Parallel wait 대기중인 Job 들을 깨워 실행되도록 한다. 
	 * @param pgName
	 * @param returnCode
	 */
	public void awakeParallelWaitingInstances(String pgName) throws SQLException {
		String[] waitingJobInstanceIds = parallelJobWaitingPool.getWaitingJobIdFor(pgName);
		
		for (String waitingJobInsId : waitingJobInstanceIds) {
			jobStarter.askToStart(waitingJobInsId);
		}
	}

	/**
	 * Trigger Job 으로 지징된 Job 을 Activate 시킴. 하나의 인스턴스는 하나의 상태만 가질 수 있으므로, trigger 마다 activate 하는 것이 맞다. 
	 * 온디맨드 배치와 비슷한 구조.
	 * 
	 * @param parentJobInstanceId
	 * @param parentJobExecutionId
	 * @param parentParams
	 * @param triggerJobId
	 * @param procDate
	 * @param totalChildCount 만들어야할 전체 child 개수
	 * @param childSeq 생성되는 child 들의 번호 1 ~ n
	 * @return
	 * @throws SQLException
	 */
	public JobInstance activateTriggerJob(String parentJobInstanceId, String parentJobExecutionId, String triggerJobId, String procDate, int totalChildCount, int childSeq) throws SQLException {
		try {
			// parent job 의 파라미터에 "PARENT." 라고 달아서 복사하여 child job 한테 전달한다.
			Map<String, String> newChildParams = new LinkedHashMap();
			Map<String, String> parentInParams = new LinkedHashMap();

			// parent 의 Execution.
			JobExecution parentJobExe = jobExecutionManager.getJobExecutionDeep(parentJobExecutionId);
			
			if (parentJobExe != null) {
				parentInParams = parentJobExe.getInParameters();
			}else {
				// parent Job 이 한번도 execution 된적 없는 경우. 인스턴스 파라미터를 상속한다.
				JobInstance parentJobIns = jobInstanceManager.getJobInstanceDeep(parentJobInstanceId);
				parentInParams = parentJobIns.getInParameters();
			}
				
			// parent 의 INPUT 파라미터를 PARENT. 붙여서 상속
			for (Map.Entry entry : parentInParams.entrySet()) {
				if (!Util.nvl(entry.getKey()).startsWith("PARENT.")) {
					// PARENT.PARENT.*** 와 같이 3대 이상으로는 파라미터 상속이 되지 않도록 한다. 
					newChildParams.put("PARENT."+entry.getKey(), String.valueOf(entry.getValue()));
				}
			}

			// parent 의 retval을 PARENT.OUT. 붙여서 상속. (PARENT.OUT.)
			if (parentJobExe != null) { 
				Properties retval = parentJobExe.getReturnValues();
				if (retval != null) {
					for (Map.Entry entry : retval.entrySet()) {
						newChildParams.put("PARENT.OUT."+entry.getKey(), String.valueOf(entry.getValue()));
					}
				}
			}
			
			newChildParams.put("TRIGGER_CHILD_NO",    String.valueOf(childSeq));        // 동일 Job 의 쌍둥이를 child 로 생성할때 그 순서를 매김.
			newChildParams.put("TRIGGER_CHILD_COUNT", String.valueOf(totalChildCount)); // CHILD 전체 개수.
			
			JobInstance jobins = activator.activate(triggerJobId, procDate, newChildParams, new AdminAuth(parentJobExecutionId));
			log.info(MSG.get("main.endproc.trigger.ok", parentJobExecutionId, jobins.getJobInstanceId())); // {0}의 트리거로 인스턴스({1})가 생성되었습니다
			return jobins;
		}catch(Throwable e) {
			Util.logError(log, MSG.get("main.endproc.trigger.error", parentJobExecutionId, triggerJobId), e); //{0} 의 트리거로 {1} 의 인스턴스를 생성 중 에러가 발생하였습니다
			// 하나 에러나도 그냥 무시하고 다음꺼 처리한다.
		}
		return null;
	}
}
