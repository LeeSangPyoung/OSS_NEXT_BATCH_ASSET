package nexcore.scheduler.core.internal;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.client.IAgentClient;
import nexcore.scheduler.controller.internal.CustomConfig;
import nexcore.scheduler.controller.internal.JobExecutionManager;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.ParallelGroup;
import nexcore.scheduler.entity.PreJobCondition;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.monitor.internal.AgentMonitor;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 실행 조건을 체크함. 현재 실행 조건들이 모두 만족하면 true를 리턴함 </li>
 * <li>작성일 : 2010. 5. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobRunConditionChecker {
	private JobInstanceManager       jobInstanceManager;
	private JobExecutionManager      jobExecutionManager;
	private ParallelRunningCounter   parallelRunningCounter;
	private ParallelJobWaitingPool   parallelJobWaitingPool;
	private PreJobWaitingPool        preJobWaitingPool;
	private RepeatManager            repeatManager;
	private AgentMonitor             agentMonitor;
	
	private String                   _doLogDecisionResult;
	private boolean                  logDecisionResult;
	
	private Log                      log;
	
	public void init() {
		log = LogManager.getSchedulerLog();
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

	public ParallelJobWaitingPool getParallelJobWaitingPool() {
		return parallelJobWaitingPool;
	}

	public void setParallelJobWaitingPool(ParallelJobWaitingPool parallelJobWaitingPool) {
		this.parallelJobWaitingPool = parallelJobWaitingPool;
	}

	public PreJobWaitingPool getPreJobWaitingPool() {
		return preJobWaitingPool;
	}

	public void setPreJobWaitingPool(PreJobWaitingPool preJobWaitingPool) {
		this.preJobWaitingPool = preJobWaitingPool;
	}
	
	public RepeatManager getRepeatManager() {
		return repeatManager;
	}

	public void setRepeatManager(RepeatManager repeatManager) {
		this.repeatManager = repeatManager;
	}

	public AgentMonitor getAgentMonitor() {
		return agentMonitor;
	}

	public void setAgentMonitor(AgentMonitor agentMonitor) {
		this.agentMonitor = agentMonitor;
	}

	public String getDoLogDecisionResult() {
		return _doLogDecisionResult;
	}

	public void setDoLogDecisionResult(String _logDecisionResult) {
		this._doLogDecisionResult = _logDecisionResult;
		if ("false".equalsIgnoreCase(_logDecisionResult)) {
			logDecisionResult = false;
		}else {
			logDecisionResult = true;
		}
	}
	
	public boolean isLogDecisionResult() {
		return logDecisionResult;
	}
	
	/**
	 * wait 상태 사유를 update함.
	 * @param jobins
	 * @param newStateReason
	 */
	private void updateWaitStateReason(JobInstance jobins, String newStateReason) throws SQLException {
		if (!Util.equalsIgnoreNull(jobins.getJobStateReason(), newStateReason)) { 
			// 사유가 달라졌을때만 update state 수행함. update state 가 꽤 부하를 줌.
			jobins.setJobState(JobInstance.JOB_STATE_WAIT);
			jobins.setJobStateReason(newStateReason); // Lock 상태입니다.
			/* 2013-10-05. Wait 사유를 업데이트 하는 것이므로 반드시 Init or Wait 상태인 것만 업데이트 한다. */
			if (!jobInstanceManager.setJobStateWithCheck(jobins, JobInstance.JOB_STATE_WAIT)) {
				jobInstanceManager.setJobStateWithCheck(jobins, JobInstance.JOB_STATE_INIT);
			}
		}
	}
	
	/**
	 * Lock 체크. UnLock 상태이어야만 실행가능.
	 * @param jobins
	 * @return
	 * @throws SQLException
	 */
	private boolean checkLock(JobInstance jobins) throws SQLException {
		if (jobins.isLocked()) {
			updateWaitStateReason(jobins, MSG.get("main.runcheck.locked")); 
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * Job 상태 체크. Init, Wait 인 경우만 scheduler 에 의해 start 가능함.
	 * @param jobins
	 * @return
	 */
	private boolean checkStateWaitOrInit(JobInstance jobins) {
		return Util.isBlank(jobins.getJobState()) ? false :
			jobins.getJobState().equals(JobInstance.JOB_STATE_INIT) ||
			jobins.getJobState().equals(JobInstance.JOB_STATE_WAIT);
	}

	/**
	 * 에이전트의 상태 체크. Closed , down, not inuse 상태에서는 Job 실행되지 않고 wait 로 머문다.
	 * @param jobins
	 * @return true if agent available, false if agent unavailable
	 */
	private boolean checkAgentState(JobInstance jobins) throws SQLException {
		// master agent 가 가용 상태인지 체크
		boolean isMasterAgentAvailable = false;
		if (!Util.isBlank(jobins.getAgentNodeMaster())) {
			isMasterAgentAvailable = 
					agentMonitor.isAgentInUse(jobins.getAgentNodeMaster()) && 
					"OK".equals(agentMonitor.getAgentAliveMsg(jobins.getAgentNodeMaster())) &&
					!agentMonitor.isAgentClosed(jobins.getAgentNodeMaster());
		}
		
		if (isMasterAgentAvailable) { // master 가 available하면 바로 리턴함. 대부분의 케이스
			return true;
		}
		
		// master 가 불가용 상태이므로 slave 도 체크한다. 
		boolean isSlaveAgentAvailable = false;   // slave 에이전트가 가용 상태인지?
		if (!Util.isBlank(jobins.getAgentNodeSlave())) {
			isSlaveAgentAvailable = 
					agentMonitor.isAgentInUse(jobins.getAgentNodeSlave()) && 
					"OK".equals(agentMonitor.getAgentAliveMsg(jobins.getAgentNodeSlave())) &&
					!agentMonitor.isAgentClosed(jobins.getAgentNodeSlave());
		}
		
		if (isSlaveAgentAvailable) {
			return true; // slave 가 available하면 바로 리턴함
		}
		
		// master , slave 모두 불가용 상태임. 이때는 master 에이전트의 상태에 따라 wait 사유를 설정한다.
		if (!agentMonitor.isAgentInUse(jobins.getAgentNodeMaster())) {
			updateWaitStateReason(jobins, MSG.get("main.runcheck.agentnotinuse", jobins.getAgentNodeMaster())); 
			return false;
		}else if (!"OK".equals(agentMonitor.getAgentAliveMsg(jobins.getAgentNodeMaster()))) {
			updateWaitStateReason(jobins, MSG.get("main.runcheck.agentdown", jobins.getAgentNodeMaster()));
			return false;
		}else if (agentMonitor.isAgentClosed(jobins.getAgentNodeMaster())) {
			updateWaitStateReason(jobins, MSG.get("main.runcheck.agentclosed", jobins.getAgentNodeMaster())); 
			return false;
		}else {
			return true; // 저 위에서는 불가용 상태였으나 여기서는 가용 상태인 경우 (거의 가능성이 없지만) true 로 한다.
		}
	}
	
	/**
	 * 에이전트의 상태 체크. Closed , down, not inuse 상태에서는 Job 실행되지 않고 wait 로 머문다.
	 * 실행이 결정된 Job 들을 대상으로 최종적으로 에이전트 상태 체크를 다시 한다.
	 * 이때는 AgentMonitor의 캐쉬데이타로 하지 않고 직접 AgentClient 에 통신하여 정보를 획득한다
	 * 
	 * 캐쉬 시점이 2초정도 소요되므로 "차단" 이 수행된 후에 일시적으로는 agentMonitor.isAgentClose() = false 될 수 도 있다
	 * 이런 gap 을 최소화하기 위한 조치임. 
	 *  
	 * @param jobins
	 * @return
	 */
	private boolean checkAgentStateAgain(JobInstance jobins) throws SQLException {
	    /*
         * slave가 가용 상태이면 master 가 불가용 상태일지라도 true를 리턴하여 slave 에서 실행되도록 한다.
         */
	    // master agent 가 가용 상태인지 체크
	    IAgentClient agentClientMaster = agentMonitor.getAgentInfoManager().getAgentClient(jobins.getAgentNodeMaster());
        boolean isMasterAgentAvailable = false;
        if (!Util.isBlank(jobins.getAgentNodeMaster())) {
            isMasterAgentAvailable = 
                agentMonitor.isAgentInUse(jobins.getAgentNodeMaster()) && 
                "OK".equals(agentClientMaster.isAlive()) &&
                !agentClientMaster.isClosed();
        }
        
        if (isMasterAgentAvailable) { // master 가 available하면 바로 리턴함. 대부분의 케이스
            return true;
        }
        
        // master 가 불가용 상태이므로 slave 도 체크한다. 
        IAgentClient agentClientSlave = agentMonitor.getAgentInfoManager().getAgentClient(jobins.getAgentNodeSlave());
        boolean isSlaveAgentAvailable = false;   // slave 에이전트가 가용 상태인지?
        if (!Util.isBlank(jobins.getAgentNodeSlave())) {
            isSlaveAgentAvailable = 
            agentMonitor.isAgentInUse(jobins.getAgentNodeSlave()) && 
            "OK".equals(agentClientSlave.isAlive()) &&
            !agentClientSlave.isClosed();
        }
        
        if (isSlaveAgentAvailable) {
            return true; // slave 가 available하면 바로 리턴함
        }

        // master , slave 모두 불가용 상태임. 이때는 master 에이전트의 상태에 따라 wait 사유를 설정한다.
		
		if (!agentMonitor.isAgentInUse(jobins.getAgentNodeMaster())) {
			updateWaitStateReason(jobins, MSG.get("main.runcheck.agentnotinuse", jobins.getAgentNodeMaster())); 
			return false;
		}else if (!"OK".equals(agentClientMaster.isAlive())) {
			updateWaitStateReason(jobins, MSG.get("main.runcheck.agentdown", jobins.getAgentNodeMaster()));
			return false;
		}else if (agentClientMaster.isClosed()) {
			updateWaitStateReason(jobins, MSG.get("main.runcheck.agentclosed", jobins.getAgentNodeMaster())); 
			return false;
		}else {
			return true;
		}
	}

	/**
	 * Job 실행시각 체크. From ~ Until 범위 내에 있을 경우 실행함.
	 * @param jobins
	 * @param today     오늘날짜 YYYYMMDD
	 * @param yesterday     어제일자 YYYYMMDD
	 * @param currentHHMM1  오늘용  현재시각 HHMM
	 * @param currentHHMM2  어제용  현재시각 HHMM + 2400 
	 * @return
	 */
	private boolean checkTimeFromUntil(JobInstance jobins, String today, String yesterday, String currentHHMM1, String currentHHMM2) throws SQLException {
		boolean expired           = false; 
		boolean betweenFromUntil  = false; // 현재 실행할 시각인가?

		CustomConfig config = CustomConfig.getInstance();
		
		if (config.getJobAliveDay() == 0) { // 미설정시 기존 로직 대로 2일간만 유효하게 동작한다.
			String jobInsFrom  = jobins.getTimeFrom()  == null || jobins.getTimeFrom().length() < 4  ? "0000" : jobins.getTimeFrom();
			String jobInsUntil = jobins.getTimeUntil() == null || jobins.getTimeUntil().length() < 4 ? 
					jobInsFrom.compareTo("2400") < 0 ? "2359" : "4759" : jobins.getTimeUntil(); // null 일 경우 from 시각을 보고 2359, 또는 4759로 설정
			
			if (today.equals(jobins.getActivationDate())) { // 당일 기준
				betweenFromUntil = jobInsFrom.compareTo(currentHHMM1) <= 0 && currentHHMM1.compareTo(jobInsUntil) <= 0;
				expired = currentHHMM1.compareTo(jobInsUntil) > 0; // until 시각이 지난건 expire 처리함.
			}else if (yesterday.equals(jobins.getActivationDate())) { // 전일 기준
				if (jobInsFrom.compareTo("2400") >= 0 || jobInsUntil.compareTo("2400") >= 0) { 
					betweenFromUntil = jobInsFrom.compareTo(currentHHMM2) <= 0 && currentHHMM2.compareTo(jobInsUntil) <= 0;
				}
				expired = currentHHMM2.compareTo(jobInsUntil) > 0; // until 시각이 지난건 expire 처리함.
			}else if (jobins.getActivationDate().compareTo(yesterday) < 0) {
				expired = true;  // 2일 이전에 activation 된 건들은 expire 함. (오늘, 어제 activation 된 것들만 유효함) 
			}
		}else {
			// config.getJobAliveDay() 가 0 이상으로 설정되어있다면, 새로운 로직이 동작한다. 2013-05-29. 한화증권 no-expire 요건 추가 구현. 2일 이상 장기간 wait 할 수 있게 한다.
			
			String jobInsFrom  = jobins.getTimeFrom()  == null || jobins.getTimeFrom().length() < 4  ? "0000" : jobins.getTimeFrom();
			String jobInsUntil = jobins.getTimeUntil() == null || jobins.getTimeUntil().length() < 4 ? (24 * config.getJobAliveDay() - 1) + "59" : jobins.getTimeUntil();
			
			int from  = Integer.parseInt(jobInsFrom);
			int until = Integer.parseInt(jobInsUntil);
			
			// 현재시각 HHMM 을 구한다. 어제 이전에 생성된 인스턴스일 경우는 24 * day 만큼 add 하여
			int currentHHMM = Integer.parseInt(currentHHMM1) + Util.getDiffDay(jobins.getActivationDate(), today) * 2400;
			
			betweenFromUntil = from <= currentHHMM && currentHHMM <= until;
			expired = currentHHMM > until;
		}
		
		if (expired) {
			if ("Y".equals(jobins.getRepeatYN()) && !Util.isBlank(jobins.getLastJobExeId())) {
				// RepeatJob 이 expire 된 경우는 무조건 expire 처리 하지 않고, 최종 실행 결과에 따라 EndOK/EndFail 로 상태 변경한다.
				// 실행 이력이 없는 경우는 expire 처리한다.
				int lastReturnCode = jobExecutionManager.getReturnCode(jobins.getLastJobExeId());
				jobins.setJobState(lastReturnCode == 0 ? JobInstance.JOB_STATE_ENDED_OK : JobInstance.JOB_STATE_ENDED_FAIL);
				jobins.setJobStateReason(MSG.get("main.repeat.expired")); // 반복 시각이 모두 지났습니다
			}else {
				jobins.setJobState(JobInstance.JOB_STATE_EXPIRED);
				jobins.setJobStateReason(MSG.get("main.runcheck.expired", jobins.getActivationDate())); // 인스턴스 유효일(2일)이 지났거나 Until 시각이 지났습니다. 인스턴스 생성시각:{0}
			}
			jobInstanceManager.setJobState(jobins);
			return false;
		}
		if (!betweenFromUntil) {
			// 아직 Until 지나지 않은 경우, 앞으로 실행될 여지가 있으므로 계속 wait.
			updateWaitStateReason(jobins, MSG.get("main.runcheck.time.outofrange")); // 지금은 From~Until 범위 밖에 있습니다
		}
		return betweenFromUntil;
	}
	
	private boolean checkMaxOk(JobInstance jobins) throws SQLException {
		// MAX OK 체크.
		if (jobins.getRepeatMaxOk() > 0 ) { // max_ok 조건이 있다.
			if (jobins.getEndOkCount() >= jobins.getRepeatMaxOk()) {
				updateWaitStateReason(jobins, MSG.get("main.runcheck.maxok.exceed", jobins.getRepeatMaxOk())); // Max OK ({0}) 를 초과하였습니다.
				return false;
			}
		}
		return true;  // 0 이면 체크하지 않음.
	}
	
	/**
	 * EXACT 방식의 반복 수행일 경우 현재시각이 그 시각이 맞는지 체크.
	 * 
	 * @param jobins
	 * @param currentTime DecisionQueue 에 PUT 된 시각
	 * @return
	 */
	private boolean checkRepeat(JobInstance jobins, long currentTime) throws SQLException {
		if ("Y".equals(jobins.getRepeatYN())) {
			if (JobInstance.REPEAT_GB_EXACT.equals(jobins.getRepeatIntvalGb())) {
				if (repeatManager.checkCurrentIsExactTime(jobins.getRepeatExactExp(), currentTime, jobins.getActivationDate())) {
					return true;
				}else {
					// 1초 이내의 오차는 허용하기 위해 1초씩 더하고 뺀 후에 다시 체크해본다.
					
					// 약 0.## 초 먼저 sleep 에서 깨어난 경우, true 로 간주한다.
					if (repeatManager.checkCurrentIsExactTime(jobins.getRepeatExactExp(), currentTime+1000, jobins.getActivationDate())) {
						return true;
					}
					
					// 약 0.## 초 늦게 sleep 에서 깨어난 경우, true 로 간주한다.
					if (repeatManager.checkCurrentIsExactTime(jobins.getRepeatExactExp(), currentTime-1000, jobins.getActivationDate())) {
						return true;
					}
					
					// 아직은 run 할때가 아니므로 RepeatTimer 에 sleep 건다.
					// returnCode 를 0 을 주는 이유는 맨 처음 실행때 (Wait->Sleep) 는 에러정지 상황이 아니므로 0.
					// 그이후에는 이미 이전 실행에서 IfErrorIgnore 되었다면 Sleep 하지 않고 End 되었을 것이므로 여기서는 0 으로 Ok처리한다.
					repeatManager.checkAndScheduleForRepeat(jobins.getJobInstanceId(), 0, 0, jobins.getJobState());
					log.info("EXACT Check Not ok. JobInsId="+jobins.getJobInstanceId()+" Exact="+jobins.getRepeatExactExp()+", Time="+new Date(currentTime));
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 승인여부 체크
	 * @param jobins
	 * @return
	 */
	private boolean checkConfirm(JobInstance jobins) throws SQLException {
		if ("Y".equals(jobins.getConfirmNeedYN())) {
			if (jobins.getConfirmed() != null && jobins.getConfirmed().length() > 10 ) { 
				// 승인이 날 경우 시각(14), ID, IP 가 여기에 박힌다. 적당히 10자 이상으로함
				return true;
			}else {
				updateWaitStateReason(jobins, MSG.get("main.runcheck.need.confirm")); // Confirm 대기중입니다
				return false;
			}
		}else {
			return true;
		}
	}
	
	/**
	 * 최대 동시 실행 수 제한 여부를 체크함. 
	 * 이 메소드는 체크 과정에서 호출하지 않고 DecisionQueueThread 에서 호출한다. 
	 * R 상태 변경이 실패 됐을때, 이 메소드를 호출하여 병렬제한 값을 확인하는 순서로 한다.
	 * @param jobins
	 * @return
	 */
	public boolean checkParallelMax(JobInstance jobins) throws SQLException {
		if (Util.isBlank(jobins.getParallelGroup())) {
			return true;
		}
		
		ParallelGroup pg = parallelRunningCounter.getParallelGroup(jobins.getParallelGroup());
		if (pg == null) {
			updateWaitStateReason(jobins, MSG.get("main.runcheck.max.parallel.limit.notfound", jobins.getParallelGroup())); // 병렬제한그룹 ({0}) 이 존재하지 않습니다
			return false;
		}else if (pg.getMaxLimit() <= pg.getCurrentRunning()) {
			updateWaitStateReason(jobins, MSG.get("main.runcheck.max.parallel.limit.exceed", jobins.getParallelGroup(), pg.getMaxLimit())); // 병렬제한그룹 ({0}) 의 최대값({1})을 초과하였습니다 
			parallelJobWaitingPool.add(jobins); // waiting pool 에 넣음.
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * Job 인스턴스의 End 상태를 정리함. 
	 * RepeatSleep 상태에서는 직전 Execution 의 성공/에러 여부에 따라 EndOK / EndFail 결정함.
	 * EXACT 방식에서 한번도 실행되지 않은 상태에서는 lastJobExeId 가 없으며, 그런 경우는 RepeatSleep 를 그대로 리턴한다
	 * @param jobInsState
	 * @param lastJobExeId
	 * @return RepeatSleep 를 고려한 End OK / Fail
	 */
	private String getJobInsEndOkState(String jobInsState, String lastJobExeId) throws SQLException {
		// 선행 Job 의 상태 체크.  
		
		if (JobInstance.JOB_STATE_SLEEP_RPT.equals(jobInsState) && !Util.isBlank(lastJobExeId)) {
			// 현재 상태가 반복대기 중일때는 선행 조건 만족 여부를 체크할때 최종 수행의 결과 값을 가지고 OK/Fail 여부를 결정한다.
			// EXACT 방식의 반복에서는 한번도 실행하지 않은 상태에서 SLEEP_RPT 상태가 될 수 있으므로 LastJobExeId 를 꼭 확인해야한다.

			// 최종 Ended Execution 의 return code 값을 이용하여 endok, endfail 처리를 함
			JobExecution jobexe = jobExecutionManager.getJobExecution(lastJobExeId);
			/*
			 * 최종 End 된 Job 의 OK/FAIL 을 보던 것을 최종 Execution 의 OK/FAIL 을 보는 것으로 변경함
			 * ==> rerun 할 경우, 선행 Job 이 running 이면 후행 Job 은 wait 하는 것이 적절해 보인다. 
			 * 그래야지만 선후행 흐름을 살리면서 재실행 할 수 있다. 
			 */

			if (jobexe!=null ) {
				if (jobexe.getState() == JobExecution.STATE_ENDED) {
					jobInsState = jobexe.getReturnCode()==0 ? JobInstance.JOB_STATE_ENDED_OK : JobInstance.JOB_STATE_ENDED_FAIL;
				}else {
					// 아직 END 상태가 아니므로 OK, FAIL 모두 해당없음
				}
			} // else -> JobExe가 없으므로 OK, FAIL 을 알 수 없다.
		}
		return jobInsState;
	}
	
	/**
	 * 선행 Job 조건 만족 여부 체크. 
	 * 하나의 Job Id 에 동일 PROC_DATE 의 JobInstance가 여러개 있을 경우, 그중 하나라도 만족하면 만족하는 것으로 본다.
	 * 하나의 JobInstance에 대해서 여러개의 JobExecution이 있을 경우 최종 EXE 의 종료 상태에 따라 결과 returnCode를 보고 ok/fail를 체크함
	 * 
	 * @param jobins
	 * @return
	 */
	private boolean checkPreJobCondition(JobInstance jobins) throws SQLException {
		List exp = new LinkedList();
		LinkedList<String> reason = new LinkedList<String>();
		
		jobInstanceManager.loadPreJobConditions(jobins);
		
		for (PreJobCondition preJobCond : jobins.getPreJobConditions()) {
			
			List<Map> preJobInsList = jobInstanceManager.getJobInstancesStateByJobId(preJobCond.getPreJobId(), jobins.getProcDate()); // 2013.7.28. 쿼리 효율을 위해 필요한 컬럼만 조회하는 방식으로 변경
			// 동일 PROC_DATE, JOB ID 에 대해서 JOB Instance ID 가 여러 개일 경우는 모든 Job Instance 에 대해서 다 check 하면서 하나라도 만족하면 돌린다.
			/*
			 * Keys of map(preJobIns) is {"JOB_INSTANCE_ID", "JOB_ID", "JOB_STATE", "LAST_JOB_EXE_ID"}
			 */

			boolean satisfyPreJobCondition = false;
			
			if (PreJobCondition.INS_EXIST.equals(preJobCond.getOkFail())) {
				satisfyPreJobCondition = preJobInsList.size() > 0;   // 존재 조건 만족 여부 체크
			}else if (PreJobCondition.INS_NONE.equals(preJobCond.getOkFail()) ||
					  PreJobCondition.END_OK_OR_INS_NONE.equals(preJobCond.getOkFail()) ||
					  PreJobCondition.END_FAIL_OR_INS_NONE.equals(preJobCond.getOkFail()) ||
					  PreJobCondition.END_OKFAIL_OR_INS_NONE.equals(preJobCond.getOkFail())) {
				satisfyPreJobCondition = preJobInsList.size() == 0;  // 미존재 조건 만족 여부 체크
			}
			
			if (!satisfyPreJobCondition) {
				if (preJobInsList.size() == 0) {
					reason.add(MSG.get("main.runcheck.prejobins.notexist", preJobCond.getPreJobId())); // 선행 Job {0} 의 인스턴스가 없습니다
				}else if (PreJobCondition.ALLINS_END_OK.equals(preJobCond.getOkFail()) ||
						PreJobCondition.ALLINS_END_FAIL.equals(preJobCond.getOkFail()) ||
						PreJobCondition.ALLINS_END_OKFAIL.equals(preJobCond.getOkFail())) {
					// 2013-06-03. 정호철. 다중 트리거로 구성된 환경에서 모든 CHILD 들이 다 끝나기를 기다리도록 선후행을 구성하기 위해. 인스턴스 전체의 END 상태를 체크함 
					LinkedList<String> reasonSub = new LinkedList<String>();
					for (Map<String, String> preJobIns : preJobInsList) {
						String preJobInsState = getJobInsEndOkState(preJobIns.get("JOB_STATE"), preJobIns.get("LAST_JOB_EXE_ID"));
						boolean oneInsCondOK = false;
						if (PreJobCondition.ALLINS_END_OK.equals(preJobCond.getOkFail())) {
							oneInsCondOK = JobInstance.JOB_STATE_ENDED_OK.equals(preJobInsState);
						}else if (PreJobCondition.ALLINS_END_FAIL.equals(preJobCond.getOkFail())) {
							oneInsCondOK = JobInstance.JOB_STATE_ENDED_FAIL.equals(preJobInsState);
						}else if (PreJobCondition.ALLINS_END_OKFAIL.equals(preJobCond.getOkFail())) {
							oneInsCondOK = JobInstance.JOB_STATE_ENDED_OK.equals(preJobInsState) || JobInstance.JOB_STATE_ENDED_FAIL.equals(preJobInsState);
						}
						
						if (!oneInsCondOK) {
							reasonSub.add(MSG.get("main.runcheck.prejobins.state", preJobIns.get("JOB_INSTANCE_ID"), JobInstance.getJobStateText(preJobInsState)));
						}
					}
					
					if (reasonSub.size() > 0) { // 한 인스턴스라도 조건에 만족되지 않는 경우가 있다. 
						reason.addAll(reasonSub);
						satisfyPreJobCondition = false;
					}else {
						satisfyPreJobCondition = true;
					}
				}else {
					LinkedList<String> reasonSub = new LinkedList<String>();
					for (Map<String, String> preJobIns : preJobInsList) {
						// 선행 Job 의 상태 체크.
						String preJobInsState = getJobInsEndOkState(preJobIns.get("JOB_STATE"), preJobIns.get("LAST_JOB_EXE_ID"));
						
						// OK 로 설정되어있으면 ENDED-OK 인가 확인, FAIL로 설정되어있으면 ENDED-FAIL 인가 확인.
						satisfyPreJobCondition = 
							PreJobCondition.END_OK.equals(preJobCond.getOkFail()) || PreJobCondition.END_OK_OR_INS_NONE.equals(preJobCond.getOkFail()) 
								? JobInstance.JOB_STATE_ENDED_OK.equals(preJobInsState) :
							PreJobCondition.END_FAIL.equals(preJobCond.getOkFail()) || PreJobCondition.END_FAIL_OR_INS_NONE.equals(preJobCond.getOkFail()) 
								? JobInstance.JOB_STATE_ENDED_FAIL.equals(preJobInsState) : 
							PreJobCondition.END_OKFAIL.equals(preJobCond.getOkFail()) || PreJobCondition.END_OKFAIL_OR_INS_NONE.equals(preJobCond.getOkFail()) 
								? JobInstance.JOB_STATE_ENDED_OK.equals(preJobInsState) || JobInstance.JOB_STATE_ENDED_FAIL.equals(preJobInsState) : false;
								
						if (satisfyPreJobCondition) {
							break;  // Instance 중 하나가 조건에 만족하면...이 조건은 만족한걸로 본다. 따라서..다중 Instance 는 조심해서 챙겨야함
						}else {
							reasonSub.add(MSG.get("main.runcheck.prejobins.state", preJobIns.get("JOB_INSTANCE_ID"), JobInstance.getJobStateText(preJobInsState))); // 선행 {0} 의 상태 {1}
						}
					}
					
					if (!satisfyPreJobCondition) {
						reason.addAll(reasonSub);
					}
				}
			}
			
			exp.add(satisfyPreJobCondition);
			exp.add(preJobCond.getAndOr()); // 선행 Job 들의 실행 완료 여부와 and/or 조건이 담긴 list 만듬.
		}
		
		boolean preJobConditionOk = evaluatePreJobConditionExpression(exp);
		if (!preJobConditionOk) {
			// 조건이 만족되지 않을 경우 reason 도 set 한다.
			updateWaitStateReason(jobins, reason==null ? MSG.get("main.runcheck.prejob.fail") : reason.toString()); // 선행 Job 체크 중 에러가 발생
			preJobWaitingPool.add(jobins);
		}
		return preJobConditionOk;
	}
	
	/**
	 * 선행 Job 조건 표현식을 계산함
	 * @param preJobsExpression
	 * @return
	 */
	private boolean evaluatePreJobConditionExpression (List preJobsExpression) {
		if (preJobsExpression.size() == 0) {
			return true; // 선행 Job 조건이 없는 경우는 무조건 true
		}
		if (preJobsExpression.size() < 3) { // 선행 Job 이 하나만인 경우.
			return ((Boolean)preJobsExpression.get(0)).booleanValue();
		}
		
		// 먼저 AND 연산 부터 수행함.
		for (int i=1; i<preJobsExpression.size()-1; i=i+2) {
			if ("AND".equals(preJobsExpression.get(i))) {
				boolean evalResult = ((Boolean)preJobsExpression.get(i-1)).booleanValue() && ((Boolean)preJobsExpression.get(i+1)).booleanValue();
				preJobsExpression.set(i-1, new Boolean(evalResult));  // eval 결과를 세팅하고
				preJobsExpression.remove(i); // eval 소스를 삭제함. AND operator                          
				preJobsExpression.remove(i); // eval 소스를 삭제함. operand
				i=i-2;
			}else if ("OR".equals(preJobsExpression.get(i))) {
				// skip
			}else {
				throw new SchedulerException("main.runcheck.prejob.andor.error", preJobsExpression.get(i));
			}
		}

		// OR 연산 수행
		boolean result = false;
		for (int i=0; i<preJobsExpression.size(); i=i+2) {
			result = result || ((Boolean)preJobsExpression.get(i)).booleanValue();
		}

		return result;
	}

	/**
	 * 지금 실행되어야하는 Job 인지 체크함.
	 * @param jobInstanceId
	 * @param currentTime DecisionQueue 에 PUT 된 시각.
	 * @return
	 */
	public boolean doCheck(JobInstance jobins, long currentTime) throws SQLException {
		/*
		 * Decision Queue 에 부하가 걸려서 약간 처리 시각이 지연되더라고 최초 PUT 시각을 기준으로 삼는다.
		 * EXACT 방식의 반복에서는 최초 PUT 시각이 Timer 에서 깨워준 그 시각이므로 매우 중요함
		 */
		String date1 = Util.getYYYYMMDD(currentTime);          // 오늘
		String date2 = Util.getYYYYMMDD(currentTime-86400000); // 어제
		
		String currentHHMM1 = Util.getHHMM(currentTime);        // 오늘 현재시각
		String currentHHMM2 = String.valueOf(Integer.parseInt(currentHHMM1) + 2400);         // 어제 기준 25시 표기

		return doCheck(jobins, date1, date2, currentTime, currentHHMM1, currentHHMM2);
	}

	/**
	 * 지금 실행되어야하는 Job 인지 체크함. 
	 * @param jobInstanceId 
	 * @param todayYYYYMMDD  오늘
	 * @param yesterdayYYYYMMDD  어제  
	 * @param currentTime DecisionQueue 에 put 된 시각
	 * @param currentTodayHHMM1 오늘기준 현재시각
	 * @param currentYesterdayHHMM2 어제기준 +24 시 현재시각
	 * @param doCheckState 상태체크도 할지? (true 이면, WAIT, INIT 이어야만함)
	 * 
	 * @return
	 */
	private boolean doCheck(JobInstance jobins, String todayYYYYMMDD, String yesterdayYYYYMMDD, long currentTime, String currentTodayHHMM1, String currentYesterdayHHMM2) throws SQLException {
		boolean satisfy = true;
		String checkPhase = null;
		try {
			// ■ 1.상태 체크. 이미 RUNNING, SUSPENDED 인 Instance 는 다시 Run 될 수 없다.
			if (JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {
				satisfy = false;
				checkPhase = "Check running";
			}
		
			// ■ 2.LOCK 체크. UNLOCK 상태인 것들만 start 할 수 있음.
			if (satisfy && !checkLock(jobins)) {
				satisfy = false;
				checkPhase = "Check lock";
			} 
			
			// ■ 3.상태 체크. Init, Wait 인 것들만 start 할 수 있음.
			if (satisfy && !checkStateWaitOrInit(jobins)) {
				satisfy = false;
				checkPhase = "Check state Wait/Init";
			}
			
			// ■ 4.Time 체크
			if (satisfy && !checkTimeFromUntil(jobins, todayYYYYMMDD, yesterdayYYYYMMDD, currentTodayHHMM1, currentYesterdayHHMM2)) {
				satisfy = false;
				checkPhase = "Check time";
			}
			
			// ■ 5.Confirm 체크
			if (satisfy && !checkConfirm(jobins)) {
				satisfy = false;
				checkPhase = "Check confirm";
			}
			
			// ■ 5-2.에이전트 상태 체크. 에이전트 차단 상태에서는 start 안됨.
			if (satisfy && !checkAgentState(jobins)) {
				satisfy = false;
				checkPhase = "Check agent";
			}
			
			// ■ 6.MaxOk 체크
			if (satisfy && !checkMaxOk(jobins)) {
				satisfy = false;
				checkPhase = "Check time";
			}
			
			// ■ 6.2. Repeat Interval, Exact Time 체크
			if (satisfy && !checkRepeat(jobins, currentTime)) {
				satisfy = false;
				checkPhase = "Check Repeat";
			}

			// ■ 7.선행 Job 조건 만족 여부 체크
			if (satisfy && !checkPreJobCondition(jobins)) {
				satisfy = false;
				checkPhase = "Check pre job";
			}
			
			// 2012-12-13. 병렬제한그룹 체크는 여기서 하지 않고 setJobStateForStart() 에서 update 쿼리로 한다.
			// ■ 8.parallel max 조건 만족 여부 체크.
//			if (satisfy && !checkParallelMax(jobins)) {
//				satisfy = false;
//				checkPhase = "Check parallel";
//			}
			
			// ■ 9.Agent 의 OK 여부, Closed 여부를 통신하여 다시 체크한다.
			if (satisfy && !checkAgentStateAgain(jobins)) {
				satisfy = false;
				checkPhase = "Check agent2";
			}
		}catch(Exception e) {
			Util.logError(log, MSG.get("main.runcheck.fail", jobins.getJobInstanceId()), e); // 인스턴스 실행 조건 체크 중 에러가 발생하였습니다
			satisfy = false;
		}
		
		if (satisfy) {
			if (log.isDebugEnabled()) {
				log.debug(MSG.get("main.runcheck.satisfy", jobins.getJobInstanceId())); // {0}는 실행 조건을 만족합니다. 곧 실행됩니다.
			}
			
		} else {
			if (log.isDebugEnabled() && logDecisionResult) {
				log.debug(MSG.get("main.runcheck.not.satisfy", jobins.getJobInstanceId(), checkPhase)); //{0}는 실행 조건({1})이 만족되지 않습니다. 대기합니다.
			}
		}
		
		return satisfy;
	}

	/**
	 * 지금 실행되어야하는 Job 인지 체크함.
	 * 파라미터로 넘어온 jobins 만 가지고 판단할 수 있는 항목만 판단하고 추가로 DB 쿼리는 하지 않음. 
	 * TimeScheduler 에서 DecisionQueue 에 넣기 전에 한번 걸르기 위해 사용함. 
	 * 
	 * @param jobInstanceId 
	 * @param todayYYYYMMDD  오늘
	 * @param yesterdayYYYYMMDD  어제  
	 * @param currentTodayHHMM1 오늘기준 현재시각
	 * @param currentYesterdayHHMM2 어제기준 +24 시 현재시각
	 * @param doCheckState 상태체크도 할지? (true 이면, WAIT, INIT 이어야만함)
	 * 
	 * @return
	 */
	public boolean doCheckSimple(JobInstance jobins, String todayYYYYMMDD, String yesterdayYYYYMMDD, String currentTodayHHMM1, String currentYesterdayHHMM2) {
		boolean satisfy = true;
		String checkPhase = null;
		try {
			// ■ 1.상태 체크. 이미 RUNNING, SUSPENDED 인 Instance 는 다시 Run 될 수 없다.
			if (JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {
				satisfy = false;
				checkPhase = "Check running";
			}
		
			// ■ 2.LOCK 체크. UNLOCK 상태인 것들만 start 할 수 있음.
			if (satisfy && !checkLock(jobins)) {
				satisfy = false;
				checkPhase = "Check lock";
			} 
			
			// ■ 3.상태 체크. Init, Wait 인 것들만 start 할 수 있음.
			if (satisfy && !checkStateWaitOrInit(jobins)) {
				satisfy = false;
				checkPhase = "Check state Wait/Init";
			}
			
			// ■ 4.Time 체크
			if (satisfy && !checkTimeFromUntil(jobins, todayYYYYMMDD, yesterdayYYYYMMDD, currentTodayHHMM1, currentYesterdayHHMM2)) {
				satisfy = false;
				checkPhase = "Check time";
			}
			
			// ■ 5.Confirm 체크
			if (satisfy && !checkConfirm(jobins)) {
				satisfy = false;
				checkPhase = "Check confirm";
			}
			
			// ■ 5-2.에이전트 상태 체크. 에이전트 차단 상태에서는 start 안됨.
			if (satisfy && !checkAgentState(jobins)) {
				satisfy = false;
				checkPhase = "Check agent";
			}
			
			// ■ 6.MaxOk 체크
			if (satisfy && !checkMaxOk(jobins)) {
				satisfy = false;
				checkPhase = "Check time";
			}
			
			// ■ 7.선행 Job 조건 만족 여부 체크
			// ■ 8.parallel max 조건 만족 여부 체크.
			// 선행, parallel max 조건 체크는 DB를 다시 select 해야하는 항목이므로 여기서 하지 않고 doCheck 하도록 함. 
			
		}catch(Exception e) {
			Util.logError(log, MSG.get("main.runcheck.fail", jobins.getJobInstanceId()), e); // 인스턴스 실행 조건 체크 중 에러가 발생하였습니다
			satisfy = false;
		}

		if (!satisfy) {
			if (log.isDebugEnabled() && logDecisionResult) {
				log.debug(MSG.get("main.runcheck.not.satisfy", jobins.getJobInstanceId(), checkPhase)); //{0}는 실행 조건({1})이 만족되지 않습니다. 대기합니다.
			}
		}
		
		return satisfy;
	}

	/**
	 * 지금 실행되어야하는 Job 인지 체크함 (상태값만 확인함).
	 * 이중화 환경 대비 다시한번 점검하기 위해 사용함 
	 * 
	 * @param jobins
	 * 
	 * @return true 실행가능, false 실행불가
	 */
	public boolean doCheckStateOnly(JobInstance jobins) {
		return !(JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState()));
	}
	
	public static void main(String[] args) {
		JobRunConditionChecker j = new JobRunConditionChecker();
		List l = new LinkedList();
		l.add(true);
		l.add("AND");
		l.add(true);
		l.add("AND");
		l.add(false);
		l.add("OR");
		l.add(true);
		l.add("AND");
		System.out.println(j.evaluatePreJobConditionExpression(l));
	}
}
