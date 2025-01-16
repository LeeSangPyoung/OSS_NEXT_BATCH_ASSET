package nexcore.scheduler.core.internal;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import nexcore.scheduler.controller.internal.JobExecutionManager;
import nexcore.scheduler.core.IMonitorDisplayable;
import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Repeat 처리를 위해서 Timer 가 필요함. </li>
 * <li>작성일 : 2010. 6. 4.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

// java.util.Timer 를 사용하지 않고 ScheduledThreadPoolExecutor 를 사용함. 
public class RepeatManager implements IMonitorDisplayable {
	private JobInstanceManager            jobInstanceManager;
	private JobExecutionManager           jobExecutionManager;
	private JobStarter                    jobStarter;
	private int                           scheduledThreadPoolExecutorSize = 5;
	private Map<String, ScheduledFuture>  tasks = new ConcurrentHashMap<String, ScheduledFuture>(); /* <JobInsId, ..> */

	private Log   log;
	private ScheduledThreadPoolExecutor timer;

	public RepeatManager() {
	}
	
	public void init() {
		log   = LogManager.getSchedulerLog();
		timer = new ScheduledThreadPoolExecutor(scheduledThreadPoolExecutorSize); 
	}
	
	public void destroy() {
		timer.shutdownNow();
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

	public JobStarter getJobStarter() {
		return jobStarter;
	}

	public void setJobStarter(JobStarter jobStarter) {
		this.jobStarter = jobStarter;
	}

	public int getScheduledThreadPoolExecutorSize() {
		return scheduledThreadPoolExecutorSize;
	}

	public void setScheduledThreadPoolExecutorSize(int scheduledThreadPoolExecutorSize) {
		this.scheduledThreadPoolExecutorSize = scheduledThreadPoolExecutorSize;
	}
	
	public long getTaskCount() {
		return timer.getTaskCount();
	}
	
	public Set<String> getSleepingJobInstanceIdList() {
		return new TreeSet<String>(tasks.keySet());
	}

	/** 
	 * 타이머에서 깨어나서 ask 하는 main 로직
	 * @param jobins
	 * @return true 정상적으로 처리한 경우 (ask 하거나, 반복 중지한 경우) , false 에러로 인해 ask 하지 못한 경우
	 */
	private boolean checkAndAsk(JobInstance jobins) {
		String beforeJobState = jobins.getJobState();
		try {
			// 1. RepeatYN 다시 확인
			// 2. Until time 확인
			// 3. State 를 "W" 로 변경
			// 4. Check Run Condition and fire.
			
			if (!JobInstance.Y.equals(jobins.getRepeatYN())) {
				// sleep 하는 동안 repeat를 N 으로 끈 경우. 마지막 수행의 return code값을 읽어서 End OK, EndFail 처리를 한다.
				if ("-".equals(jobins.getLastJobExeId())) { // Running 준비 단계인 경우. 이미 running 이 진행중이므로 아무일 하지 않고 조용이 끝낸다.
					return true;
				}
				if (Util.isBlank(jobins.getLastJobExeId())) {
					// EXACT 방식에서 한번도 Run 되지 않고 Init->Wait->Sleep 상태로 들어간 경우에. sleep 중에 N 으로 바뀌면 이런 상태가된다.
					// Expire 상태로 변경한다.
					jobins.setJobState(JobInstance.JOB_STATE_EXPIRED);
					jobins.setJobStateReason(MSG.get("main.repeat.repeatyn.changed.to.n")); // 반복구분이 N 으로 변경되었습니다
				}else {
					int returnCode = jobExecutionManager.getReturnCode(jobins.getLastJobExeId());
					jobins.setJobState(returnCode==0 ? JobInstance.JOB_STATE_ENDED_OK : JobInstance.JOB_STATE_ENDED_FAIL);
					jobins.setJobStateReason(MSG.get("main.repeat.repeatyn.changed.to.n")); // 반복구분이 N 으로 변경되었습니다
				}
				jobInstanceManager.setJobStateWithCheck(jobins, beforeJobState);
			}else {
				// 반복 실행을 위해 WAIT 로 상태 변경후 ask.
				if (jobInstanceManager.setJobStateWithCheck(jobins.getJobInstanceId(), JobInstance.JOB_STATE_SLEEP_RPT, JobInstance.JOB_STATE_WAIT, MSG.get("main.repeat.wait"))) {
					/*
					 * SLEEP_RPT -> WAIT 로 정상 전이가 된 경우만 실행한다. SLEEP 중에 FORCE RUN 한 경우 동시에 SLEEP 할 수가 있다. 
					 * 이 경우에는 동시에 깨어날 수가 있으며, 그중에 한놈만 실행되도록 하기 위해 setJobStateWithCheck() 가 true인 경우만 ask한다.
					 */
					jobStarter.askToStart(jobins);
				}else {
					// 이 경우는 다른 sleep 중인 runnable (task) 가 이미 Job launch 한 경우임, 그냥 pass. sleep 중에 force run, rerun 하는 경우 이럴 수 있다. 
					Util.logWarn(log, MSG.get("main.repeat.state.changed.while.sleep", jobins.getJobInstanceId())); // {0}은 반복 Sleep 중에 강제로 상태가 변경되었습니다
				}
			}
		}catch(Throwable e) {
			Util.logError(log, MSG.get("main.repeat.check.repeat.error", jobins.getJobInstanceId()), e); // {0} 의 Repeat Job 반복 체크 중에 에러가 발생하였습니다
			return false;
			/*
			 * 여기서 DB 장애로 에러가 발생할 경우 Job 인스턴스 상태는 SLEEP_RPT 상태이면서, Timer 에서는 해제가 된 상태이므로
			 * 무한히 SLEEP_RPT 상태로 남게 된다. 
			 * 다시 Timer 에 등록하도록 해야한다.
			 */
		}finally {
			tasks.remove(jobins.getJobInstanceId()); 
		}
		return true;
	}
	
	/**
	 * sleep time 방식으로 Timer 에 등록함
	 * @param jobins
	 * @param sleepTime sleep 해야할 시간
	 * @param sleepCalcBaseTime sleepTime 을 계산한 기준시각.
	 */
	private void schedule(final JobInstance jobins, final long sleepTime, final long sleepCalcBaseTime) {
		// repeat 작업은 수초내로 repeat 해야하므로 TimeScheduler로는 불가하다.
		// Timer 를 이용해서 이렇게 별도로 수행해야한다.
		Runnable runnable = new Runnable() {
			private String oldJobInsLastJobExeId = jobins.getLastJobExeId();
			
			public void run() {
				JobInstance newJobIns = null;
				try {
					newJobIns = jobInstanceManager.getJobInstance(jobins.getJobInstanceId());
				}catch(Exception e) {
					Util.logError(log, "[RepeatManager] getJobInstance fail. ("+jobins.getJobInstanceId()+").");
					tasks.remove(jobins.getJobInstanceId());
					return; // DB 조회 에러가 나면 여기서 그냥 리턴하고 RunningJobStateMonitor 가 복구하기를 기대한다.
				}
				
				/* 
				 * 2013.07.11 이중화 환경에서 반복작업이 중복 실행되는 경우가 종종 발생하여 다음의 보완 로직을 적용함.
				 * >> sleep 들어가기 전의 jobins.getLastJobExeId() 시각과 sleep 에서 깨어난 후의 jobins.getLastJobExeId() 을 비교하여 같은 경우만 ask 함.
				 * >> 다르다는 것은 sleep 중에 peer node 에서 먼저 깨어나서 실행을 했을 가능성이 있다는 의미. peer 서버에서도 이미 반복으로 sleep timer 에서 돌고 있을 수 있다. 
				 */
				if (Util.equalsIgnoreNull(oldJobInsLastJobExeId, newJobIns.getLastJobExeId())) {
					checkAndAsk(newJobIns);
/* 20130711. 이런경우는 RunningJobStateMonitor 가 복구하게 될 것이므로 여기서는 그냥 pass 시킨다. */
//					if (checkAndAsk(newJobIns)) {
//						return; // 정상 ask.
//					}else {
//						// 장애 상황 발생. DB 장애와 같은 장애로 인해 ask 하지 못한 상황. 무조건 30 초 sleep 후 깨어나서 다시 시도해본다 
//						log.warn("[RepeatManager] Recover repeat job ("+jobins.getJobInstanceId()+") after 30 seconds.");
//						ScheduledFuture task = timer.schedule(this, 30000, TimeUnit.MILLISECONDS);
//						tasks.put(jobins.getJobInstanceId(), task);
//					}
				}else { // lastJobExeId 가 다르다 --> sleep 하는 중에 peer 가 실행했을 수 있다. 
					Util.logWarn(log, "[RepeatManager] While sleeping, another execution has ocurred at a peer.2 ("+jobins.getJobInstanceId()+"). ");
					tasks.remove(jobins.getJobInstanceId());
					return;
				}
			}
		};
		long current       = System.currentTimeMillis();
		long realSleepTime = sleepTime - (current - sleepCalcBaseTime);
		/* 
		 * 최초 sleepTime 을 계산한 후에 DB Update, 스레드 처리 등의 작업을 하는 동안 약간의 시간이 소요된다. 
		 * 이 시간만큼을 뺀 후 실제 sleep 을 해야지만 정확한 repeat 이 된다.
		 * 이 시간을 빼주지 않으면 반복이 되면 될 수록 delay 가 길어진다. 
		 */
		ScheduledFuture task = timer.schedule(runnable, realSleepTime, TimeUnit.MILLISECONDS);
		tasks.put(jobins.getJobInstanceId(), task);
		Util.logDebug(log, "[RepeatManager] put to sleeping task list.2 ("+jobins.getJobInstanceId()+"). ");
	}

	/** 
	 * EXACT 방식에서 현재 시각 기준으로 다음 실행까지의 sleep time 을 계산함.
	 * @param nextRunTime 다음 시각 (HHMMSS) 
	 * @param activationDate activation date. 24시 표현식을 위해 +24 를 할지 말지 결정하기 위한 용도로 사용
	 */
	private long calcSleepTimeForNextExactRun(String nextRunTime, String activationDate) {
		if (nextRunTime == null) {
			return -1;
		}
		int hh  = Integer.parseInt(nextRunTime.substring(0,2));
		int mm  = Integer.parseInt(nextRunTime.substring(2,4));
		int ss  = Integer.parseInt(nextRunTime.substring(4,6));
		
		String currentTime = Util.getCurrentYYYYMMDDHHMMSSMS(); 
		
		// 현재시각 시분초밀리초
		int hh2  = Integer.parseInt(currentTime.substring(8,10));
		int mm2  = Integer.parseInt(currentTime.substring(10,12));
		int ss2  = Integer.parseInt(currentTime.substring(12,14));
		int sss2 = Integer.parseInt(currentTime.substring(14,17));

		// 현재시각일자. 위의 ActivationDate 와 비교해서 이미 다음날로 넘어갔다면 +24h 를 해야함.
		String currentDate = currentTime.substring(0, 8);
		
		if (activationDate.compareTo(currentDate) < 0) {  // activation date 다음날 이므로 +24h 를 함.
			hh2 = hh2 + 24 * Util.getDiffDay(activationDate, currentDate);
		}
	
		long tobeRunTimeMs = ( hh *60*60 + mm *60 + ss  ) * 1000;         // run 해야할 시각의 밀리초 변환
		long currentTimeMs = ( hh2*60*60 + mm2*60 + ss2 ) * 1000 + sss2;  // 현재시각 밀리초 변환
		
		return tobeRunTimeMs - currentTimeMs;
	}
	
	/**
	 * Exact 방식의 반복일 경우는 Sleep time 을 계산해 등록하는 방식이 아니고, 지속적으로 깨어나서 다음번 시각을 계산해 다시 sleep 하는 방식이다. 
	 * @param jobins
	 * @param nextRunTime (hhmmss 타입)
	 */
	private void scheduleForExact(final JobInstance jobins, final String nextRunTime) {
		/* 
		 * EXACT 방식인 경우는 정확한 시각까지 sleep 해야하느므로 sleep time 을 계산하는 방식이 아니라 1분마다 sleep 에서 깨어나서 
		 * 나머지 sleep 시각을 다시 계산하는 방식으로 한다.
		 * 
		 * NTP 와 같은 시각 동기화 서비스가 돌아가게 되는 경우, 동기화 수행 이전에 계산해 놓은 시각과 실제 시스템 시각이 오차가 발생하는 문제가 생기기 때문에 이런 방식으로 한다. 
		 */
		Runnable runnable = new Runnable() {
			private String oldJobInsLastJobExeId = jobins.getLastJobExeId(); 
			
			public void run() {
				
				long sleepTime = calcSleepTimeForNextExactRun(nextRunTime, jobins.getActivationDate());
				if (sleepTime < 100) { // 0.1 초 미만인 경우 ask 함.
					JobInstance newJobIns = null;
					try {
						newJobIns = jobInstanceManager.getJobInstance(jobins.getJobInstanceId());
					}catch(Exception e) {
						Util.logError(log, "[RepeatManager] getJobInstance fail. ("+jobins.getJobInstanceId()+").");
						tasks.remove(jobins.getJobInstanceId());
						return; // DB 조회 에러가 나면 여기서 그냥 리턴하고 RunningJobStateMonitor 가 복구하기를 기대한다.
					}
					
					/* 
					 * 2013.07.11 이중화 환경에서 반복작업이 중복 실행되는 경우가 종종 발생하여 다음의 보완 로직을 적용함.
					 * >> sleep 들어가기 전의 jobins.getLastJobExeId() 시각과 sleep 에서 깨어난 후의 jobins.getLastJobExeId() 을 비교하여 같은 경우만 ask 함.
					 * >> 다르다는 것은 sleep 중에 peer node 에서 먼저 깨어나서 실행을 했을 가능성이 있다는 의미. peer 서버에서도 이미 반복으로 sleep timer 에서 돌고 있을 수 있다. 
					 */
					if (Util.equalsIgnoreNull(oldJobInsLastJobExeId, newJobIns.getLastJobExeId())) {
						checkAndAsk(newJobIns);
						
/* 20130711. 이런경우는 RunningJobStateMonitor 가 복구하게 될 것이므로 여기서는 그냥 pass 시킨다. */
//						if (checkAndAsk(newJobIns)) {
//							return; // 정상 ask.
//						}else {
//							// 장애 상황 발생. DB 장애와 같은 장애로 인해 ask 하지 못한 상황. 무조건 30 초 sleep 후 깨어나서 다시 시도해본다
//							log.warn("[RepeatManager] Recover repeat job ("+jobins.getJobInstanceId()+") after 30 seconds.");
//							ScheduledFuture task = timer.schedule(this, 30000, TimeUnit.MILLISECONDS);
//							tasks.put(jobins.getJobInstanceId(), task);
//						}
					}else { // lastJobExeId 가 다르다 --> sleep 하는 중에 peer 가 실행했을 수 있다. 
						Util.logWarn(log, "[RepeatManager] While sleeping, another execution has ocurred at a peer.1 ("+jobins.getJobInstanceId()+"). ");
						tasks.remove(jobins.getJobInstanceId());
						return;
					}
				}else { // 실행할 시각까지 0.1초 이상 남았다. 다시 timer 에 넣는다.
					ScheduledFuture task = timer.schedule(this, Math.min(sleepTime, 50000), TimeUnit.MILLISECONDS); // 최대 50 초까지만 sleep 하고 다시 계산하도록 한다.
					tasks.put(jobins.getJobInstanceId(), task);
				}
			}
		};

		ScheduledFuture task = timer.schedule(runnable, 1, TimeUnit.MILLISECONDS); // 일단 1 밀리 초 후 부터 제대로 계산해서 sleep 한다. 
		tasks.put(jobins.getJobInstanceId(), task);
		Util.logDebug(log, "[RepeatManager] put to sleeping task list.1 ("+jobins.getJobInstanceId()+"). ");
	}

	/**
	 * Repeat 를 위해 체크하고 계산하여 Timer 에 등록한다.
	 * 
	 * @param jobInstanceId
	 * @param startTime START 방식일 경우 interval 계산을 위해 직전의 start 값. 
	 * @param returnCode 직전 실행의 결과코드.
	 * @param beforeState 직전 상태. ( END_OK, END_FAIL, SLEEP, WAIT, INIT )
	 * @throws SQLException
	 */
	public void checkAndScheduleForRepeat(String jobInstanceId, long startTime, int returnCode, String beforeState) throws SQLException {
		JobInstance jobins = jobInstanceManager.getJobInstance(jobInstanceId);
		if (JobInstance.Y.equalsIgnoreCase(jobins.getRepeatYN())) {
			// 정상종료이거나, 에러발생시에라도 "REPEAT_IF_ERROR_IGNORE" 일 경우는 repeat 함.
			boolean doRepeat = returnCode == 0 ? true : JobInstance.REPEAT_IF_ERROR_IGNORE.equals(jobins.getRepeatIfError());
			
			// UNTIL 이 반드시 있어야함.
			if (jobins.getTimeUntil() == null || jobins.getTimeUntil().length() < 4 ) {
				jobins.setJobStateReason(MSG.get("main.repeat.until.required")); // RepeatJob은 Time Until 속성이 필수입니다
				jobInstanceManager.setJobStateWithCheck(jobins, jobins.getJobState());
				doRepeat = false;
			}
			
			// MAX OK 체크.
			if (doRepeat && jobins.getRepeatMaxOk() > 0 ) { // max_ok 조건이 있다.
				if (jobins.getEndOkCount() >= jobins.getRepeatMaxOk()) {
					jobins.setJobStateReason(MSG.get("main.repeat.maxok.exceed", jobins.getRepeatMaxOk())); // Max OK {0} 를 초과하여 반복할 수 없습니다
					jobInstanceManager.setJobStateWithCheck(jobins, jobins.getJobState());
					doRepeat = false;
				}
			}
			
			long current = System.currentTimeMillis();
			if (doRepeat) {
				// recovery 인 경우. shutdown 되어있는 동안 이미 sleep interval 을 지나게 된다면
				// 여기서 즉시로 실행하지 않고 다음 분 01 초에 돌아가도록 여기서는 그냥 wait 상태로만 변경하고 끝낸다.
//				if (recoveryMode && (startTime + (jobins.getRepeatIntval()*1000) - current) < 0) {
//					jobInstanceManager.setJobStateWithCheck(jobins.getJobInstanceId(), JobInstance.JOB_STATE_SLEEP_RPT, JobInstance.JOB_STATE_WAIT, MSG.get("main.repeat.wait.for.next.repeat")); // 반복 실행을 위해 WAIT 상태로 변경합니다
//					return;
//				}
// 이기능은 EXACT 방식의 추가로 인해 필요없어진 기능. -2011-10-19-
				
				String nextExactRunTime = null; // EXACT 방식일 경우 여기에 값이 채워짐.
				long   sleepTime        = 0; 
				
				/* REPEAT_GB_START 일지라도 Job 실행 시간이 internal 시간을 넘어가면 Job 실행 후에 Repeat 된다. repeat 로 인해 concurrent 하게 돌지는 않는다. */
				if (JobInstance.REPEAT_GB_START.equals(jobins.getRepeatIntvalGb())) {
					/*
					 *  누적되는 오차를 제거하기 위핸 millis 초 단위는 계산에 포함시키지 않는다.
					 *  이렇게 하지 않을 경우 awake 후에 생태변경(DB Update) + ask & decision 에 시간이 소요되면 그 만큼 (1초미만) 계속 누적되며 나중에는 분단위 오차가 발생한다.
					 */
					startTime = startTime/1000*1000; 
					sleepTime = Math.max(startTime + (jobins.getRepeatIntval()*1000) - current, 1000); // 최소 1초 이상은 interval을 둔다.
				}else if (JobInstance.REPEAT_GB_END.equals(jobins.getRepeatIntvalGb())) {
					sleepTime = Math.max(jobins.getRepeatIntval()*1000, 1000); // 최소 1초 이상은 interval을 둔다. 
					// END 기준일 때는 end 시간으로 측정하지 않고 현재시각으로 한다. 에러상황에서는 end time 이 0 이므로 오동작한다.
				}else if (JobInstance.REPEAT_GB_EXACT.equals(jobins.getRepeatIntvalGb())) {
					// EXACT 구분일때는 정규표현식에 정의된 시간을 계산하여 다음 run time을 계산.  (2011-10-17. ITS 요청으로 기능 추가)
					nextExactRunTime = getNextExactRunTime(jobins.getRepeatExactExp(), current, jobins.getActivationDate());
					sleepTime = calcSleepTimeForNextExactRun(nextExactRunTime, jobins.getActivationDate());
				}else {
					jobins.setJobStateReason(MSG.get("main.repeat.gubun.required")); // 반복구분이 지정되지 않았습니다
					jobInstanceManager.setJobStateWithCheck(jobins, jobins.getJobState());
				}
				boolean sleepOK = false;
				if (sleepTime > 0) {
					// 2012.05.22 - sleep 후의 시각이 until 을 지나가게 되면 굳이 sleep 할 필요 없이 여기서 end 처리한다.
					// sleep 후의 시각이 until 을 지나갈지말지 체크한다. 24시 이후의 시각도 체크한다. 
					String afterSleepString   = Util.getYYYYMMDDHHMMSS(current+sleepTime);
					String afterSleepDate     = afterSleepString.substring(0,8);
					int    afterSleepHHMM;
					
					// after sleep 시각이 다음날인지 체크하여 그럴 경우 +24h * day 한다.
					if (afterSleepDate.compareTo(jobins.getActivationDate()) > 0) {
//						afterSleepHHMM = String.valueOf(Util.toInt(afterSleepString.substring(8,12)) + 2400);
						// - 정호철. 2013.05.31 - 3일 이상 alive 할 수 있는 기능 추가로 인해 이부분 로직도 3일 이상 체크 가능하도록 수정 (Long-live 기능). 
						afterSleepHHMM = Util.toInt(afterSleepString.substring(8,12)) + 2400 * Util.getDiffDay(jobins.getActivationDate(), afterSleepDate);  
					}else {
						afterSleepHHMM = Util.toInt(afterSleepString.substring(8,12));
					}
					
					// after sleep time 이 time until 이후인지 체크
					if (afterSleepHHMM <= Util.toInt(jobins.getTimeUntil())) {
						// 아직 until 시각을 안지났으므로 sleep 한다.
						sleepOK = true;
						jobins.setJobState(JobInstance.JOB_STATE_SLEEP_RPT);
						jobins.setJobStateReason(MSG.get("main.repeat.sleep.for.repeat.state", Util.getDatetimeLocalizedText(current+sleepTime))); // 반복을 위해 {0} 까지 sleep 합니다
						if (jobInstanceManager.setJobStateWithCheck(jobins, beforeState)) { // 이전 상태로 체크해가며.
							log.info(MSG.get("main.repeat.sleep.for.repeat", jobInstanceId, sleepTime)); // {0} 이 {1} ms 동안 반복 sleep 합니다
							
							ScheduledFuture task = tasks.get(jobInstanceId);
							if (task != null) { // 이미 같은 JobInstanceId로 sleep 중. sleep 중에 누군가가 force run 한 경우임. 이전 sleep 을 죽인다.
								if (task.cancel(false)) { // 이전꺼가 잘 cancel 되면 새걸 sleep 시킨다.
									tasks.remove(jobInstanceId);
									if (nextExactRunTime != null) {
										scheduleForExact(jobins, nextExactRunTime); // EXACT 방식
									}else {
										schedule(jobins, sleepTime, current);
									}
									log.info(MSG.get("main.repeat.cancel.before.sleep", jobInstanceId)); // {0} 이 반복 Sleep 중에 다시 강제 실행되어 이전 Sleep 중인 태스크를 취소합니다 
								}
							}else { // 이전꺼가 없으므로 바로 sleep 들어간다.
								if (nextExactRunTime != null) {
									scheduleForExact(jobins, nextExactRunTime); // EXACT 방식
								}else {
									schedule(jobins, sleepTime, current);
								}
							}
						}else {
							// end ok 되자 마자 sleep 들어가기 직전에 force run 되는 희귀한 경우.
							// 이중화 환경에서 JobRunResultProcessor 와 RunningJobMonitor 가 동시에 같은 JobIns를 처리하면서 발생하는 경우. 에러아님.
							Util.logWarn(log, MSG.get("main.repeat.sleep.error.state.inconsistent", jobInstanceId)); // {0} 종료 직후 상태 불일치 현상으로 인해 반복 sleep 할 수 없습니다.	
						}
					}
				}
				if (!sleepOK) {
					/*
					 * 1. sleep 을 해야하지만 sleepTime 이 음수가 나왔다. 이런 경우는 EXACT 에서 실행 예정 시각이 모두 지나간 경우.
					 * 2. sleep 한 이후의 시각이 until 을 지난다. sleep 해봐야 어차피 다음번에 실행하지 못한다.
					 * 
					 * ==> 마지막 실행 결과에 따라 END 처리하며, 실행 내역이 없으면 Expire로 변경한다.
					 */
					if ("-".equals(jobins.getLastJobExeId())) { // Running 준비 단계인 경우. 이미 running 이 진행중이므로 아무일 하지 않고 조용이 끝낸다.
						return;
					}
					if (Util.isBlank(jobins.getLastJobExeId())) {
						// 한번도 실행 안된 상태. EXPIRE 처리
						jobins.setJobState(JobInstance.JOB_STATE_EXPIRED);
					}else {
						int lastReturnCode = jobExecutionManager.getReturnCode(jobins.getLastJobExeId());
						jobins.setJobState(lastReturnCode == 0 ? JobInstance.JOB_STATE_ENDED_OK : JobInstance.JOB_STATE_ENDED_FAIL);
					}
					
					jobins.setJobStateReason(MSG.get("main.repeat.expired")); // 반복 시각이 모두 지났습니다
					jobInstanceManager.setJobStateWithCheck(jobins, jobins.getJobState());
				}
			}
		}
	}
	
	/**
	 * EXACT 방식일 경우 정규 표현식으로 되어있는 다음번 실행할 시각을 찾아서 sleep time을 계산한다.
	 * 25 시 개념을 적용하기 위해 activation date 를 체크한다. 어제 activation 된 것이라면 currentMs 에 하루를 더한다.
	 * 중요) 성능을 위해 최소 5초 이상의 간격을 두고 반복한다.
	 * @param repeatExactExp
	 * @param currentMs
	 * @param activationDate
	 * @return sleep time in milliseconds. -1 if nomatch
	 */
//  2012-05-03. 시스템 시각 변경되면 오차가 발생하는 문제를 해결하기 위해 이 메소드를 삭제하고  getNextExactRunTime(), scheduleForExact(), calcSleepTimeForNextExactRun() 메소드 추가됨.
//	private long calcSleepTimeForNextExactRun삭제대상(String repeatExactExp, long currentMs, String activationDate) {
//		
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(new Date(currentMs));
//		String current = FastDateFormat.getInstance("yyyyMMddHHmmss").format(cal.getTime());
//
//		// 현재시각
//		int hh = Integer.parseInt(current.substring(8,10));
//		int mm = Integer.parseInt(current.substring(10,12));
//		int ss = Integer.parseInt(current.substring(12,14));
//		
//		/*
//		 * 현재시각(날짜) 가 activation date 보다 클 경우 이미 하루가 지난 경우이므로 시간 (hh) 에 24를 더해서 25시 표현으로 만들어야 한다.
//		 */
//		if (activationDate.compareTo(current.substring(0,8)) < 0) {
//			hh = hh + 24;
//		}
//		
//		// 정규효현식 처리.
//		Pattern ptn = Pattern.compile(repeatExactExp);
//
//		/*
//		 * 현재시각 부터 1초씩 증가하면서 표현식 (RE) 에 맞는 최단 시각이 언제인지 찾은 후
//		 * 그 시각까지 몇 ms 를 sleep 해야하는지 계산한다.
//		 */
//		for (int i=0; i<=86400; i++) {
//			ss++;
//			if (ss >= 60) {
//				ss = 0;
//				mm++;
//				if (mm >= 60) {
//					mm = 0;
//					hh++;
//					// 25 시 표현식을 적용하기 위해 if (hh>=24) 체크는 하지 않는다.
//				}
//			}
//			
//			String hhmmss = 
//				(hh < 10 ? "0"+hh : ""+hh) +
//				(mm < 10 ? "0"+mm : ""+mm) +
//				(ss < 10 ? "0"+ss : ""+ss);
//
//			if (ptn.matcher(hhmmss).matches()) {
//				// match 됐다. 이시각 (hhmmss) 까지 sleep 해야한다.
//				
//				// 오차를 줄이기 위해 다시 current 시각 측정하고 밀리초 까지 구한다.
//				String currentTime2 = FastDateFormat.getInstance("HHmmssSSS").format(new Date()); 
//				
//				// 현재시각 시분초밀리초
//				int hh2  = Integer.parseInt(currentTime2.substring(0,2));
//				int mm2  = Integer.parseInt(currentTime2.substring(2,4));
//				int ss2  = Integer.parseInt(currentTime2.substring(4,6));
//				int sss2 = Integer.parseInt(currentTime2.substring(6,9));
//				
//				if (hh >= 24) {  // 24시 이후이므로 hh2 도 24 더한다.
//					hh2 = hh2 + 24;
//				}
//			
//				long tobeRunTimeMs = ( hh *60*60 + mm *60 + ss  ) * 1000;         // run 해야할 시각의 밀리초 변환
//				long currentTimeMs = ( hh2*60*60 + mm2*60 + ss2 ) * 1000 + sss2;  // 현재시각 밀리초 변환
//				
//				long sleepTime = tobeRunTimeMs - currentTimeMs;
//				
//				if (sleepTime <= 0) { // 실행해야할 시간이 이미 지난 경우는 다음번 실행 시각을 계속 찾는다.
//					continue;
//				}
//				
//				// 최소 2초 이상은 SLEEP 하도록 한다. 1초의 오차를 허용하므로..최소 2초이상은 sleep 해야한다.
//				
//				return sleepTime < 2000 ? 2000 : sleepTime ; 
//			}
//		}
//		
//		return -1; // match 되는 time 이 없으므로 반복하지 않는다.
//	}
	
	/**
	 * EXACT 방식일 경우 정규 표현식으로 되어있는 다음번 실행할 시각을 찾아서 리턴한다. (HHMMSS 타입)
	 * 25 시 개념을 적용하기 위해 activation date 를 체크한다. 어제 activation 된 것이라면 currentMs 에 하루를 더한다.
	 * @param repeatExactExp
	 * @param currentMs
	 * @param activationDate
	 * @return next run time hhmmss (25시 표현 가능). null if nomatch
	 */
	private String getNextExactRunTime(String repeatExactExp, long currentMs, String activationDate) {
		
		String current = Util.getYYYYMMDDHHMMSS(currentMs);

		// 현재시각
		int hh = Integer.parseInt(current.substring(8,10));
		int mm = Integer.parseInt(current.substring(10,12));
		int ss = Integer.parseInt(current.substring(12,14));
		
		/*
		 * 현재시각(날짜) 가 activation date 보다 클 경우 이미 하루가 지난 경우이므로 시간 (hh) 에 24를 더해서 25시 표현으로 만들어야 한다.
		 */
		String currentDate = current.substring(0,8);
		if (activationDate.compareTo(currentDate) < 0) {
			hh = hh + 24  * Util.getDiffDay(activationDate, currentDate);
		}
		
		// 정규효현식 처리.
		Pattern ptn = Pattern.compile(repeatExactExp);

		/*
		 * 현재시각 부터 1초씩 증가하면서 표현식 (RE) 에 맞는 최단 시각이 언제인지 찾는다
		 */
		for (int i=0; i<=86400; i++) {
			ss++;
			if (ss >= 60) {
				ss = 0;
				mm++;
				if (mm >= 60) {
					mm = 0;
					hh++;
					// 25 시 표현식을 적용하기 위해 if (hh>=24) 체크는 하지 않는다.
				}
			}
			
			String hhmmss = 
				(hh < 10 ? "0"+hh : ""+hh) +
				(mm < 10 ? "0"+mm : ""+mm) +
				(ss < 10 ? "0"+ss : ""+ss);

			if (ptn.matcher(hhmmss).matches()) {
				// match 됐다. 이시각 (hhmmss) 을 리턴한다.
				return hhmmss;
			}
		}
		
		return null; // match 되는 time 이 없으면 null 을 리턴한다.
	}

	/**
	 * 현재시각이 EXACT 시각인지 체크. 
	 * 
	 * 앞의 calcSleepTimeForNextExactRun() 는 다음번 실행할 EXACT 시각을 체크하는 것이므로 일단 1초 더한후 체크 시작한다.
	 * 여기서는 현재 시각이 EXACT 에 해당하느냐 아니냐를 체크한다.
	 * 
	 * WAIT 상태에서 최초 실행들어갈때 또는 CONFIRM, UNLOCK, RERUN, RECOVERY 등의 컨트롤에서 
	 * EXACT 여부를 체크하여 sleep 하기 전 첫 번째 run 여부를 체크하기 위해 사용된다.
	 * @param repeatExactExp
	 * @param currentMs
	 * @param activationDate
	 * @return
	 */
	public boolean checkCurrentIsExactTime(String repeatExactExp, long currentMs, String activationDate) {
		
		String current = Util.getYYYYMMDDHHMMSS(currentMs);

		// 현재시각
		int hh = Integer.parseInt(current.substring(8,10));
		
		/*
		 * 현재시각(날짜) 가 activation date 보다 클 경우 이미 하루가 지난 경우이므로 시간 (hh) 에 24를 더해서 25시 표현으로 만들어야 한다.
		 */
		String currentDate = current.substring(0,8);
		if (activationDate.compareTo(currentDate) < 0) {
			hh = hh + 24  * Util.getDiffDay(activationDate, currentDate);
		}

		// 정규효현식 처리.
		Pattern ptn = Pattern.compile(repeatExactExp);

		String hhmmss =
			(hh < 10 ? "0"+hh : ""+hh) + current.substring(10,14);

		return ptn.matcher(hhmmss).matches();
	}

	/**
	 * EXACT 방식의 반복일 경우 정규표현식을 해석하여 실행 시각을 예측함. 
	 * From, Until 도 고려하여 계산함
	 * @param jobdef
	 * @return List of HHMMSS
	 */
	public List<String> getTimePlanForExactRepeat(JobDefinition jobdef) {
		if ("Y".equals(jobdef.getRepeatYN()) && "EXACT".equals(jobdef.getRepeatIntvalGb())) {
			return _getTimePlanForExactRepeat(jobdef.getTimeFrom(), jobdef.getTimeUntil(), jobdef.getRepeatExactExp());
		}else {
			return Collections.EMPTY_LIST; 
		}
	}
	
	/**
	 * EXACT 방식의 반복일 경우 정규표현식을 해석하여 실행 시각을 예측함. 
	 * From, Until 도 고려하여 계산함
	 * @param jobins
	 * @return List of HHMMSS
	 */
	public List<String> getTimePlanForExactRepeat(JobInstance jobins) {
		if ("Y".equals(jobins.getRepeatYN()) && "EXACT".equals(jobins.getRepeatIntvalGb())) {
			return _getTimePlanForExactRepeat(jobins.getTimeFrom(), jobins.getTimeUntil(), jobins.getRepeatExactExp());
		}else {
			return Collections.EMPTY_LIST; 
		}
	}

	private List<String> _getTimePlanForExactRepeat(String timeFromExp, String timeUntilExp, String repeatExactExp) {
		List<String> result = new LinkedList<String>();
		
		// 정규효현식 처리.
		Pattern ptn = Pattern.compile(repeatExactExp);

		String timeFrom  = Util.isBlank(timeFromExp)  ? "000000" : timeFromExp+"00";
		String timeUntil = Util.isBlank(timeUntilExp) ? "235959" : timeUntilExp+"59";
		
		// check기준 시각
		String theTime = timeFrom;
		
		// 현재시각
		int hh = Integer.parseInt(theTime.substring(0,2));
		int mm = Integer.parseInt(theTime.substring(2,4));
		int ss = Integer.parseInt(theTime.substring(4,6));

		/*
		 * 현재시각 부터 1초씩 증가하면서 표현식 (RE) 에 맞는 최단 시각이 언제인지 찾은 후
		 * 그 시각까지 몇 ms 를 sleep 해야하는지 계산한다.
		 */
		while(true) {
			if (ss >= 60) {
				ss = 0;
				mm++;
				if (mm >= 60) {
					mm = 0;
					hh++;
					// 25 시 표현식을 적용하기 위해 if (hh>=24) 체크는 하지 않는다.
				}
			}
			
			String hhmmss = 
				(hh < 10 ? "0"+hh : ""+hh) +
				(mm < 10 ? "0"+mm : ""+mm) +
				(ss < 10 ? "0"+ss : ""+ss);
			
			if (hhmmss.compareTo(timeUntil) > 0) {
				break;
			}

			if (ptn.matcher(hhmmss).matches()) {
				result.add(hhmmss);
			}
			ss++;
		}
		
		return result;
	}
	
	/**
	 * 해당 Job Instance 가 Repeat Timer 에 등록됐는지 아닌지?
	 * @param jobInsId
	 * @return
	 */
	public boolean isScheduledForRepeatTimer(String jobInsId) {
		return tasks.containsKey(jobInsId);
	}
	
	/**
	 * Repeat Job 을 lock 을 걸 경우는 timer 에서 제거한다.
	 * unlock 시에 다시 ask 된다.
	 * @param jobInsid
	 * @return
	 */
	public void removeScheduledTask(String jobInsid) {
		ScheduledFuture task = tasks.remove(jobInsid);
		if (task != null) {
			task.cancel(false);
		}
	}
	
	public String getDisplayName() {
		return "Sleep for repeat list";
	}
	
	public String getDisplayString() {
		return tasks.size() + " : " + tasks.keySet();
	}

}