package nexcore.scheduler.agent.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.runner.IBatchContextAdapter;
import nexcore.scheduler.controller.client.IControllerClient;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : JobExecution 이 suspend, resume, ended 등의 상태 변경이 있을때 스케줄러로 callback 하는 데몬. </li>
 * <li>작성일 : 2010. 12. 29.</li>
 * <li>변경일 : 2016. 2. 15.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// 구버전에서의 이름 JobEndCallbackRecovery 
// 2013.07.18 원래의 목적은 callback 시 에러난것들에 대한 retry 용이었으나, 정상적인 경우도 모두 이 쓰레드에 의해 callback 되도록 한다.
// 2016.02.15 구버전에서는 JobEndCallbackRecovery 였으나, EndCallback 뿐만 아니라 suspend/resume callback 도 하도록 기능 추가함 
public class JobStateCallbackSender implements Runnable {
	private boolean                         enabled = true;   // 기본값은 true 이지만, singlejvm 을 위해 에이전트만 실행되는 환경에서는 false로 한다. 
	private JobExecutionBoard               jobExecutionBoard;
	private IJobRunnerCallBack              jobRunnerCallBack;
	private IControllerClient               controllerClient;
	
	/**
	 * run() 안에서 sleep 하는 interval. 
	 * 스케줄러 연결 에러일때는 10초. 그외는 2초로 한다.
	 */
	private long                            sleepTimeForNormal = 2000;                    // 정상 상황에서의 sleepTime. 2초
	private long                            sleepTimeForSchedulerConnectionError = 10000; // 스케줄러 에러 상황에서의 sleepTime. 10초

	/**
	 * suspend 상태의 JobExecution 들.
	 */
	private Map<String, JobExecution>       suspendedJobExecutionMap = new ConcurrentHashMap<String, JobExecution>();
	
	private boolean                         destroyed;
	private Thread                          thisThread;
	private Log                             log;
	
	/**
	 * JobEndCallback 을 위해 마지막으로 디렉토리 polling한 시각.
	 * 디렉토리 polling 은 파일시스템 부하를 주므로 너무 자주 하지 않고 30초마다 한다.
	 */
	private long                            lastJobEndCallbackDirPollingTime = 0;
	
	/**
	 * 스케줄러 연결이 에러 상황인지 (스케줄러 다운) 
	 * 이 경우 sleep interval 을 늘려 잠시 쉬도록 한다. (과도한 로그 방지 목적)
	 */
	private boolean                         schedulerConnectionError; 
	
	
	public JobStateCallbackSender() {
	}
	
	public void init() {
		log = LogManager.getAgentLog();
		if (enabled) {
			thisThread = new Thread(this, "JobStateCallbackSender");
			thisThread.setDaemon(true);
			thisThread.start();
			Util.logInfo(log, "[JobStateCallbackSender] service started.");
		}else {
			Util.logInfo(log, "[JobStateCallbackSender] disabled.");
		}
	}
	
	public void destroy() {
		this.destroyed = true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public JobExecutionBoard getJobExecutionBoard() {
		return jobExecutionBoard;
	}

	public void setJobExecutionBoard(JobExecutionBoard jobExecutionBoard) {
		this.jobExecutionBoard = jobExecutionBoard;
	}

	public IJobRunnerCallBack getJobRunnerCallBack() {
		return jobRunnerCallBack;
	}

	public void setJobRunnerCallBack(IJobRunnerCallBack JobRunnerCallBack) {
		this.jobRunnerCallBack = JobRunnerCallBack;
	}

	public IControllerClient getControllerClient() {
		return controllerClient;
	}

	public void setControllerClient(IControllerClient controllerClient) {
		this.controllerClient = controllerClient;
	}

	public long getSleepTimeForNormal() {
		return sleepTimeForNormal;
	}

	public void setSleepTimeForNormal(long sleepTimeForNormal) {
		this.sleepTimeForNormal = sleepTimeForNormal;
	}

	public long getSleepTimeForSchedulerConnectionError() {
		return sleepTimeForSchedulerConnectionError;
	}

	public void setSleepTimeForSchedulerConnectionError(long sleepTimeForSchedulerConnectionError) {
		this.sleepTimeForSchedulerConnectionError = sleepTimeForSchedulerConnectionError;
	}

	public void run() {
		while(!destroyed) {
			long current = System.currentTimeMillis();
			/*
			 * batch/jestore 디렉토리의 *.je 파일을 scan 하여 JobEndCallback 을 한다. 
			 * 이 작업은 매번 디렉토리를 polling 하는 작업이므로 2초마다 하면 파일시스템에 부하를 준다. 
			 * 따라서 jobExecutionBoard.getLastModifiedTime() 로 건수가 있을때만 하거나, 30초가 지난 경우에만 한다.  
			 */
			if ((jobExecutionBoard.getLastModifiedTime() >= lastJobEndCallbackDirPollingTime) || (current - lastJobEndCallbackDirPollingTime > 30000)) {
				try {
					lastJobEndCallbackDirPollingTime = System.currentTimeMillis();
					for (int i=0; i<10 && (readJEFileAndDoCallbackJobEnd() > 0); i++) ; // 처리한게 하나라도 있으면 최대 10번 까지 반복 실행한다.
				}catch(Throwable e) {
					Util.logError(log, "[JobStateCallbackSender] CallbackJobEnd fail.", e); // Job 상태 콜백 중 에러입니다.
				}
			}
			
			try {
				// ■■■■ suspend, resume 상태 변경 감지하여 callbackSuspend, callbackResume 한다. 이 작업은 2초마다 돈다.
				checkStateAndCallbackSuspendResume();
			}catch(Throwable e) {
				Util.logError(log, "[JobStateCallbackSender] Callback suspend/resume fail.", e); // Job 상태 콜백 중 에러입니다.
			}

			if (schedulerConnectionError) {
				Util.logInfo(log, "[JobStateCallbackSender] sleep "+(sleepTimeForSchedulerConnectionError/1000)+"s. Scheduler connection error");
				Util.sleep(sleepTimeForSchedulerConnectionError); // 에러 상황에서는 과도한 에러로그 방지를 위해 10초 쉰다. 
			}else {
				Util.sleep(sleepTimeForNormal); // 정상 상황에서는 2초마다 sleep 한다.
			}
		}
	}
	
	/**
	 * jestore 의 *.je 파일을 읽어 callback jobend 수행함.
	 * @return callback 호출 성공한 건수
	 */
	private int readJEFileAndDoCallbackJobEnd() {
		File dir = new File(Util.getHomeDirectory() + AgentConstants.END_JOBEXE_STORE_DIRECTORY);
		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name != null && name.endsWith(".je");
			}}
		);
		
		Util.sortFilesByTimestamp(files); // timestamp 로 sort 함.
		int okCount = 0;
		for (int i=0; files!=null && i<files.length; i++) {
			boolean delete = false;
			JobExecution jobexe = null;
			try {
				jobexe = (JobExecution) Util.readObjectFromFile(files[i]);
			}catch(Exception e) {
				// *.je 파일 read 실패. 파일이 깨졌을 가능성이 있다. 다시 해봐야 계속 에러가 나므로 error 디렉토리로 옮긴다. 
				File errorDir = new File(Util.getHomeDirectory() + AgentConstants.END_JOBEXE_ERROR_DIRECTORY);
				files[i].renameTo(new File(errorDir, files[i].getName())); // batch/jestore_error 디렉토리로 옮긴다.
				continue; // 이 파일은 skip 한다.
			}
			try {
				if (controllerClient.callBackJobEnd(jobexe)) {
					Util.logInfo(log, "[JobStateCallbackSender] CallbackJobEnd("+jobexe.getJobExecutionId()+") ok");
					delete = true;
				}else {
					Util.logInfo(log, "[JobStateCallbackSender] CallbackJobEnd("+jobexe.getJobExecutionId()+") fail. will try again");
				}
			}catch(Throwable e) {
				schedulerConnectionError = true;
				Util.logError(log, "[JobStateCallbackSender] CallbackJobEnd fail. ("+files[i].getName()+")", e);
			}finally {
				if (delete) {
					// 정상 종료됐으므로 je 파일을 지운다.
					// 제대로 삭제되지 않는 경우 다시 callback 이 일어나서 트리거가 설정되어있을 경우 계속해서 트리거 인스턴스가 생성될 수 있다.
					// 이런 위험을 방지하기 위해 delete 실패시 여러번 시도한다.
					// delete() 시도 중에 RunResultCallback.getJobExecutionFromFile() 이 호출되면 이럴 수 있다.
					for (int k=1; k<=10 && files[i].exists() ; k++) {
						boolean deleteOk = files[i].delete();
						if (!deleteOk) {
							Util.logError(log, "[JobStateCallbackSender] ["+k+"] Fail to delete je file ["+files[i].getName()+"]. retry after 2 seconds");
							Util.sleep(2000); // 파일 삭제 오류시 뭔가 문제가 있다. 잠시 쉬었다 다시 해라.
						}else {
							if (log.isDebugEnabled()) {
								Util.logDebug(log, "[JobStateCallbackSender] ["+k+"] Delete je file ["+files[i].getName()+"] ok");
							}
							okCount ++;
						}
					}
					schedulerConnectionError = false; // 스케줄러 에러 없이 정상 상황임
				}
			}
		}
		return okCount;
	}

	/**
	 * Suspended 상태에 들어간 경우, Running 으로 빠져나온 경우를 감지하여 
	 * 스케줄러로 callbackSuspend, callbackResume 를 호출한다. 
	 * JobEndCallback 과 다르게 좀더 짧은 interval을 둔다. 
	 */
	private void checkStateAndCallbackSuspendResume() {
		if (jobExecutionBoard.getJobExecutionCount() == 0 && suspendedJobExecutionMap.size() == 0) {
			// 건수가 없을때는 건너뜀
			return;
		}
		/*
		 * JobExecutionBoard 의 것들과 suspendedJobExecutionMap 를 비교해서 
		 * running -> suspended, suspended -> running 등의 상태 변경을 감지하여 
		 * 스케줄러로 callback 호출한다.
		 */
		for (String jobExeId : jobExecutionBoard.getJobExecutionsIdList()) {
			try {
				JobContext   jobContext = jobExecutionBoard.getJobContext(jobExeId);
				JobExecution jobexe     = jobContext.getJobExecution();
				
				// JBATCH, POJO 타입인 경우에만 batchContextAdapter 가지고 상태 체크한다
				IBatchContextAdapter batchContextAdapter = jobContext.getBatchContextAdapter();
				
				if (batchContextAdapter == null) {
					continue; // JBATCH, POJO 타입이 아닌 JobExecution 이므로 skip 한다.
				}
				
				// ■■■■ 상태 체크.
				if (batchContextAdapter.isSuspendedStatus(jobContext)) { // 실제 배치 프로그램은 suspended 되었음 
					if (suspendedJobExecutionMap.containsKey(jobexe.getJobExecutionId())) {  // 이미 suspended 로 인지되었음
						// 프로그램 쓰레드에서 suspended 이고, 이미 suspended pool 에 들어있는 경우. => 변경없음
					}else {
						// 프로그램 쓰레드에서 suspended 이고, suspended pool 에는 없는 경우. => 지금 막 SUSPENDED 된 경우 callback
						if (jobexe.setStateWithCheck(JobExecution.STATE_RUNNING, JobExecution.STATE_SUSPENDED)) {
							// RUNNING -> SUSPENDED 가 성공한 경우만 callback 한다.
							jobContext.getJobRunnerCallBack().callBackJobSuspend(jobexe);
							suspendedJobExecutionMap.put(jobexe.getJobExecutionId(), jobexe);
							schedulerConnectionError = false; // callback 이 정상적으로 되었으므로 error flag 를 끈다.
						}
					}
				}else { // 배치 프로그램은 RUNNING 상태임
					if (suspendedJobExecutionMap.containsKey(jobexe.getJobExecutionId())) { // suspended pool 에 들어있음
						// 프로그램 쓰레드가 막 RESUME 되었고, 아직 suspended pool 에서는 빠지지 않은  상태
						if (jobexe.getState() == JobExecution.STATE_RUNNING) { // AbsBatchInvokeJobRunner 의 resume() 메소드에서 STATE_RUNNING 으로 변경한다.
							// RESUME 이 되어 지금 RUNNING 으로 수행중인 상황
							jobContext.getJobRunnerCallBack().callBackJobResume(jobexe);
							suspendedJobExecutionMap.remove(jobexe.getJobExecutionId());
							schedulerConnectionError = false; // callback 이 정상적으로 되었으므로 error flag 를 끈다.
						}else {
							// RESUME 이 되자마자 ENDED 로 끝나거나, SUSPEND 상태에서 강제종료로 끝내버린 상황. 이런 경우는 아래에서 다시 iterate 하면 걸러낸다.
						}
					}
				}
			}catch(Exception e) {
				// 한건 에러나더라도 나머지 계속 수행한다. 
				schedulerConnectionError = true;
				Util.logError(log, "[JobStateCallbackSender] Callback suspend/resume fail. JOB_EXE_ID:"+jobExeId, e);
			}
		}
	
		/*
		 * suspendedJobExecutionMap 에만 들어있고, jobExecutionBoard 에는 없는 경우는
		 * suspend 에서 resume 으로 깨어난 직후에 위의 callback을 하기 전에 먼저 Job 이 끝나버린 경우
		 * 조용히 여기서 지우고 끝낸다.  
		 */
		Iterator<String> iter = suspendedJobExecutionMap.keySet().iterator();
		while(iter.hasNext()) {
			String jobExeId = iter.next();
			if (!jobExecutionBoard.containsJobExecution(jobExeId)) {
				iter.remove();
			}
		}
	}
}
