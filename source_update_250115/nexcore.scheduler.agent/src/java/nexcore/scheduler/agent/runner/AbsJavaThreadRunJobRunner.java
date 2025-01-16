package nexcore.scheduler.agent.runner;

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
 * <li>설  명 : Java Thread 를 생성하여 배치 프로그램을 실행시키는 추상 Job Runner. [JBATCH, POJO, CENTERCUT, SLEEP] 이 모두 이 방식으로 동작한다. </li>
 * <li>작성일 : 2010. 10. 27.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public abstract class AbsJavaThreadRunJobRunner extends AbsJobRunner {
	/*
	 * JobContext 와 BatchContext 간 변환 및 이벤트의 i/f 를 위한 Adapter.
	 * 배치 프로그램의 종류 (NBF기반인지 POJO 인지) 에 따라 다르므로 JobRunner 마다 하나씩 갖는다.
	 */
	protected IBatchContextAdapter    batchContextAdapter; 
	
	/**
	 * 기본 에러코드. 배치 프로그램에서 BatchContext.setReturnCode() 형태로 에러코드 정의하지 않은 채로 Exception 발생시 
	 * 여기의 기본 에러코드로 JobContext.setReturnCode() 를 한다.
	 * 기본 에러코드의 기본값을 9 이다. 
	 */
	protected int                     defaultErrorCode = 9; // 배치 프로그램에서 에러코드를 세팅하지 않았을 경우에 부여되는 기본 에러코드.

	public AbsJavaThreadRunJobRunner() {
	}

	public IBatchContextAdapter getBatchContextAdapter() {
		return batchContextAdapter;
	}

	public void setBatchContextAdapter(IBatchContextAdapter batchContextAdapter) {
		this.batchContextAdapter = batchContextAdapter;
	}

	public int getDefaultErrorCode() {
		return defaultErrorCode;
	}

	public void setDefaultErrorCode(int defaultErrorCode) {
		this.defaultErrorCode = defaultErrorCode;
	}

	/**
	 * 배치 클래스 호출 메인
	 * JBATCH, POJO 가 다르다.
	 * @param jobContext
	 * @throws Exception
	 */
    abstract void invokeBatchMain(JobContext jobContext) throws Exception;

	/**
	 * Job 실행 본체를 Runnable 객체로 만들어서 리턴한다.
	 * @param je
	 * @param jobRunnerCallBack
	 * @return
	 */
	protected Runnable makeJobRunningMain(final JobExecution je, final JobContext context, final IJobRunnerCallBack jobRunnerCallBack) {
		return new Runnable() {
			public void run() {
				int       returnCode = 0; // 0 이면 정상 종료 그외 숫자는 에러
				String    errorMsg   = null;
				Throwable throwable  = null;
				try {
				    // ■■■ 선처리 실행
				    doJobExePreProcessors(context);
				    
				    // ■■■ Job Log start
					logJobStart(context);

					// ■■■ 배치 프로그램 호출.
					invokeBatchMain(context);
					
					// 정상 종료된 경우는 무조건 returnCode = 0 
					returnCode = 0;
				}catch (Throwable e) {
				    throwable = e;
				    // return code 세팅
				    if (context.getReturnCode() == 0) { 
						// 에러 상황임에도 0 으로 되어있는 것은 afterExecute() 에서 에러 발생한 것이거나 뭔가 이상한 경우이므로, 기본 에러코드 세팅한다.
						returnCode = defaultErrorCode;
					}else {
						returnCode = context.getReturnCode(); // 배치 프로그램안에서 set 한 returnCode 
					}
				    
				    // base cause exception 을 찾아 에러메세지에 set 한다.
					Throwable cause = Util.getCauseException(e);
				    errorMsg = Util.fitLength(cause.getMessage(), 1000); // NBS_JOB_EXE 테이블에 INSERT 해야하므로 너무 길지 않도록 자른다. 
					context.getLogger().error(errorMsg, e);
					if ("localhost".equals(Util.getSystemId()) && context.getLogger().isDebugEnabled()) {
						e.printStackTrace(); // 로컬 테스트중 디버그 모드에서는 console 에도 exception 뿌린다.
					}
					
				}finally {
					je.setEndTime(System.currentTimeMillis());
					je.setReturnCode(returnCode);
					je.setErrorMsg(errorMsg);
					je.setState(JobExecution.STATE_ENDED);
					
					// ■■■ Job Log end
					logJobEnd(context);
					
					jobRunnerCallBack.callBackJobEnd(je);
					getJobExecutionBoard().remove(je.getJobExecutionId());
					
					// ■■■ 후처리 실행
					doJobExePostProcessors(context, throwable);
				}
			}
		};
	}
	
	/**
	 * 스레드를 생성하여 Job 을 실행시킨다. 스레드의 run() 은 위 makeRunnable() 메소드에서 한다. 
	 */
	public void start(final JobExecution je, final JobContext context, final IJobRunnerCallBack jobRunnerCallBack) {
		getJobRunThreadManager().newThreadAndStart(context, makeJobRunningMain(je, context, jobRunnerCallBack));
	}
	
	/**
	 * 실행중인 배치실행쓰레드를 일시정지 시킨다. 
	 * 여기서는 flag 를 on 으로 하고 배치 쓰레드 안에서 스스로 wait() 한다.
	 * resume/stop 을 할때까지는 계속 wait() 한다.
	 */
	public void suspend(String jobExecutionId) {
		JobContext   jobContext = getJobExecutionBoard().getJobContext(jobExecutionId);
		
		// 배치 프로그램이 강제종료 flag 값을 읽을 수 있도록 BatchContext 에 전달한다.
		batchContextAdapter.transferSuspendEvent(jobContext);
	}
	
	/**
	 * 실행중에 일시정지된(suspended상태된) 배치쓰레드를 resume 한다.
	 * 여기서는 상태만 체크하고 배치실행쓰레드 스스로 깨어나야한다.
	 * 1분이상 깨어나지 못하면 에러를 낸다. ( 그러나 그 후에 깨어날 수 도 있다. )
	 */
	public void resume(String jobExecutionId) {
		JobContext context = getJobExecutionBoard().getJobContext(jobExecutionId);
		if (context == null) {
			throw new AgentException("agent.jobexecution.notfound", jobExecutionId);
		}
		
		// POJO 방식인 경우는 onResume 메소드를 호출한다. onResume 메소드 안에서 notify 를 할 것이므로 여기서는 하지 않는다.
		
		// resume 이벤트를 전달하여 suspended flag 를 off 시키도록 한다
		batchContextAdapter.transferResumeEvent(context);
		long beginTime = System.currentTimeMillis();
		long endTime   = 0;
		while(batchContextAdapter.isSuspendedStatus(context)) {
			if (((endTime=System.currentTimeMillis()) - beginTime) > 30 * 1000) { 
				// 30초 이상 시도해도 상태가 안바뀔 경우는 일단 여기서는 Exception 낸다.
				context.getLogger().warn("["+jobExecutionId+"] Resume check timeout. "+(endTime-beginTime)+" ms");
				throw new AgentException("agent.fail.resume.job", jobExecutionId);
			}
			
			// 배치 프로그램이 suspended 상태 (wait() 하고 있는중) 에 있으면 깨어나도록 하기 위해 notify() 한다. 
			synchronized (context.getBatchContext()) {
				context.getBatchContext().notify();	
			}
			Util.sleep(1000, true);
		}
		context.getJobExecution().setState(JobExecution.STATE_RUNNING);
	}

	/**
	 * 배치 실행 쓰레드 강제 종료.
	 * 여기서는 flag 만 on 하고, 배치 쓰레드에서 스스로 run() 을 빠져나오도록 해야한다.
	 */
	public void stop(String jobExecutionId) {
		JobExecution jobexe     = getJobExecutionBoard().getJobExecution(jobExecutionId);
		if (jobexe == null) {
			throw new AgentException("agent.jobexecution.notfound", jobExecutionId);
		}

		JobContext   jobContext = getJobExecutionBoard().getJobContext(jobExecutionId);

		// 배치 프로그램이 강제종료 flag 값을 읽을 수 있도록 BatchContext 에 전달한다.
		batchContextAdapter.transferStopForceEvent(jobContext);
		
		// 배치 실행 쓰레드의 상태를 체크하며 종료 여부를 판단한다.
		Thread runthread = getJobRunThreadManager().getThread(jobExecutionId);
		if (runthread == null) {
			throw new IllegalStateException(MSG.get("agent.jobexecution.notfound", jobExecutionId));
		}
		
		// TODO  Statement 들을 cancel() 한다.
		
		// 1분 정도 stop 체크한다.
		for (int i=1; i<=30; i++) {
			synchronized (jobContext.getBatchContext()) { // 일시정지 하고 있는 경우 그놈들 깨운다.
				jobContext.getBatchContext().notify();
			}
			runthread.interrupt();
			log.info(MSG.get("agent.thread.check.stop.state", runthread.getName(), i, 30)); 
			if (!runthread.isAlive()) {
				break; // 스레드 정지됨.
			}
			Util.sleep(2000);
		}
		
		if (runthread.isAlive()) {
			throw new AgentException ("agent.thread.stop.fail", runthread.getName());
		}

		// 스레드가 죽었으면 
		jobexe.setState(JobExecution.STATE_ENDED);
	}
}
