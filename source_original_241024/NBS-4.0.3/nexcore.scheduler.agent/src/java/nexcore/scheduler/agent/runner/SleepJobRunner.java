package nexcore.scheduler.agent.runner;

import java.lang.reflect.Method;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.AgentException;
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
 * 		SLEEP_TIME     : SLEEP 시간 (초)
 *      
 */
public class SleepJobRunner extends PojoJobRunner {
	
	public SleepJobRunner() {
	}
	
	public void start(final JobExecution je, final JobContext context, final IJobRunnerCallBack jobRunnerCallBack) {
		je.setComponentName(SleepJob.class.getName());
		super.start(je, context, jobRunnerCallBack);
	}
	
	/**
	 * 실행중인 배치실행쓰레드를 일시정지 시킨다. 
	 * 여기서는 flag 를 on 으로 하고 배치 쓰레드 안에서 스스로 wait() 한다.
	 * resume/stop 을 할때까지는 계속 wait() 한다.
	 */
	public void suspend(String jobExecutionId) {
		JobContext jobContext = getJobExecutionBoard().getJobContext(jobExecutionId);
		try {
			invokeMethod(jobContext, "onSuspend");
		}catch(Throwable e) {
			jobContext.getLogger().error("suspend error", e);
		}
	}
	
	/**
	 * 실행중에 일시정지된(suspended상태된) 배치쓰레드를 resume 한다.
	 * 여기서는 상태만 체크하고 배치실행쓰레드 스스로 깨어나야한다.
	 * 1분이상 깨어나지 못하면 에러를 낸다. ( 그러나 그 후에 깨어날 수 도 있다. )
	 */
	public void resume(String jobExecutionId) {
		JobContext jobContext = getJobExecutionBoard().getJobContext(jobExecutionId);
		try {
			invokeMethod(jobContext, "onResume");
		}catch(Throwable e) {
			jobContext.getLogger().error("resume error", e);
		}
	}

	/**
	 * 배치 실행 쓰레드 강제 종료.
	 * 여기서는 flag 만 on 하고, 배치 쓰레드에서 스스로 run() 을 빠져나오도록 해야한다.
	 */
	public void stop(String jobExecutionId) {
		JobContext jobContext = getJobExecutionBoard().getJobContext(jobExecutionId);
		
		try {
			invokeMethod(jobContext, "onStop");
		}catch(Throwable e) {
			jobContext.getLogger().error("stop error", e);
		}
	}
	
	private void checkJobExecution(String jobExecutionId){
		JobExecution jobexe     = getJobExecutionBoard().getJobExecution(jobExecutionId);
		if (jobexe == null) {
			throw new AgentException("agent.jobexecution.notfound", jobExecutionId);
		}
	}
	
	private void invokeMethod(JobContext jobContext, String methodName)  throws Throwable {
		Class  sleepJobClass  = SleepJob.class;
		log.info(sleepJobClass.toString());
    	Method method = sleepJobClass.getMethod(methodName);
    	log.info(method.toString());
    	method.invoke(jobContext.getBatchObject());
	}
}
