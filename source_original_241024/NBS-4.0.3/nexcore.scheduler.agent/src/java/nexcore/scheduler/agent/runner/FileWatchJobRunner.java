package nexcore.scheduler.agent.runner;

import java.util.Timer;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 파일 생성 여부를 체크하는 Job. 후행 Job 으로 파일 처리하는 Job 을 돌리도록 함.</li>
 * <li>작성일 : 2010. 10, 21.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

/*
 * 파라미터 :
 * 		DIRECTORY      : POLLING 할 디렉토리
 *      FILENAME       : POLLING 할 파일명 (*, ? 사용가능)
 *      POLLING_INTVAL : POLLING 간격을 지정 (초단위). (미지정시 기본 10초)  
 *      POLLING_TIME   : POLLING 시간을 분 단위로 지정. 미지정시 당일 23:50분까지의 남은 분이 자동으로 설정됨
 *      
 * 결과값 :
 *      FILE_LIST  : 파라미터 FILENAME 패턴을 만족시키는 파일 목록. 예) a1.txt,a2.txt,a3.txt
 *      FILE_COUNT : FILE_LIST 의 파일 개수.
 *      
 * Time Until 시각이 반드시 지정되어있어야 함.
 */
public class FileWatchJobRunner extends AbsJobRunner {
	private static final long   DEFAULT_POLLING_INTERVAL_SEC = 60;      // 60 초. 
	
	private Timer timer = new Timer("FileWatch Timer");
	
	private FileWatchTaskManager fileWatchTaskManager;
	private Log                  log;
	
	public FileWatchJobRunner() {
	}
	
	public void init() {
		fileWatchTaskManager = new FileWatchTaskManager();
		log                  = LogManager.getAgentLog();
	}
	
	public void destroy() {
	}

	private long getTimerPeriod(JobContext context) {
		String pollingInterval = context.getInParameter("POLLING_INTVAL");
		return Util.isBlank( pollingInterval ) ? DEFAULT_POLLING_INTERVAL_SEC : Long.parseLong(pollingInterval);
	}
	
	public void start(JobExecution je, JobContext context, IJobRunnerCallBack jobRunnerCallBack) {
		/*
		 * FileWatcher Job 은 다른 Job 들과 다르게 스레드를 발생시키지 않고
		 * Timer 를 이용하여 주기적으로 체크하도록 한다.
		 */

		ILogger logger = getAgentMain().getJobLogManager().getLog(context);

		try {
            // 선처리 실행
            doJobExePreProcessors(context);

			FileWatchTimerTask task = new FileWatchTimerTask();
			logJobStart(context);
	
			task.setJobContext        (context);
			task.setFileWatchJobRunner(this);
			task.setJobRunnerCallBack (jobRunnerCallBack);
			task.init();
			fileWatchTaskManager.put(je.getJobExecutionId(), task);
			timer.schedule(task, 1000, getTimerPeriod(context)*1000);
		}catch (Throwable e) {
			String msg = MSG.get("agent.fail.start.job", je.getJobExecutionId());
			Util.logError(logger, msg, e);
			end(context, 3, msg+"/"+e.toString(), jobRunnerCallBack);
		}
	}	
	
	/**
	 * Job 종료 처리. 
	 * 1) 파일을 찾아서 정상리턴하거나
	 * 2) 설정이 잘못되어 아예 시작을 못하거나
	 * 3) 정해진 시간만큼 기다려도 파일이 안들어오거나.
	 * @param context
	 * @param returnCode
	 * @param errorMsg
	 * @param jobRunnerCallBack
	 */
	public void end(JobContext context, int returnCode, String errorMsg, IJobRunnerCallBack jobRunnerCallBack) {
		JobExecution jobexe = context.getJobExecution();
		jobexe.setEndTime(System.currentTimeMillis());
		jobexe.setReturnCode(returnCode);
		jobexe.setErrorMsg(errorMsg);
		jobexe.setReturnValues(context.getReturnValues());
		jobexe.setState(JobExecution.STATE_ENDED);
		try {
			logJobEnd(context);
		}catch(Throwable e) {
			Util.logError(log, "logJobEnd() fail/"+jobexe.getJobExecutionId(), e); // file system 이 full 나면 로그 남기다 에러남..그래도 end 처리는 해야함.
		}
		
		try {
			jobRunnerCallBack.callBackJobEnd(jobexe);
		}finally {
			getJobExecutionBoard().remove(jobexe.getJobExecutionId());
			fileWatchTaskManager.remove(jobexe.getJobExecutionId());
			
            // 후처리 실행
            doJobExePostProcessors(context, null);
		}
	}
	
	/**
	 * Task 를 cancel 한다.
	 */
	public void suspend(String jobExecutionId) {
		FileWatchTimerTask t = fileWatchTaskManager.get(jobExecutionId);
		if (t != null) {
			t.cancel();
			JobExecution jobexe = getJobExecutionBoard().getJobExecution(jobExecutionId);
			if (jobexe!=null) {
				jobexe.setState(JobExecution.STATE_SUSPENDED);
				t.getJobRunnerCallBack().callBackJobSuspend(jobexe);
			}
		}else {
			throw new AgentException("agent.fail.suspend.job", jobExecutionId);
		}
	}
	
	/**
	 * 다시 task를 schedule한다.
	 */
	public void resume(String jobExecutionId) {
		FileWatchTimerTask oldT = fileWatchTaskManager.get(jobExecutionId);
		if (oldT != null) {
			FileWatchTimerTask newT = oldT.copy();
			fileWatchTaskManager.put(jobExecutionId, newT);
			timer.schedule(newT, 1000, getTimerPeriod(newT.getJobContext()) * 1000);
			JobExecution jobexe = getJobExecutionBoard().getJobExecution(jobExecutionId);
			if (jobexe!=null) {
				jobexe.setState(JobExecution.STATE_RUNNING);
				oldT.getJobRunnerCallBack().callBackJobResume(jobexe);
			}
		}else {
			throw new AgentException("agent.fail.resume.job", jobExecutionId);
		}
	}

	/**
	 * Task를 cancel 하고, Job End 처리한다.
	 */
	public void stop(String jobExecutionId) {
		FileWatchTimerTask t = fileWatchTaskManager.get(jobExecutionId);
		
		
		if (t != null) {
			// stop 상태 flag를 세팅하고 실제 스레드 안에서 stop 체크하도록 함.
			t.getJobContext().setStopForced(true);
			
			t.cancel();
			end(t.getJobContext(), 1, MSG.get("com.stop.force"), t.getJobRunnerCallBack());
		}else {
			throw new AgentException("agent.fail.stop.job", jobExecutionId);
		}
	}
}
