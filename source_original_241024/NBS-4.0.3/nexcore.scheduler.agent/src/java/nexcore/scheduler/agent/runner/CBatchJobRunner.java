package nexcore.scheduler.agent.runner;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.nsc.NSCIntegrator;
import nexcore.scheduler.agent.runner.proc.ProcJobRunner;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : C 프레임워크 기반 배치 APP를 실행시킨다. NSC 와의 연동 작업을 한다.</li>
 * <li>작성일 : 2012. 09. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

/*
 *  파라미터 : 
 *  	HOME_PATH : 프로세스 실행의 홈디렉토리.
 *  	ENV_###   : 프로세스의 환경변수중에 ### 로 설정됨.
 *      ARGn      : n 은 숫자. 프로그램에 n 번째 아규먼트로 지정됨.
 *   
 */
public class CBatchJobRunner extends ProcJobRunner {
	private NSCIntegrator       nscIntegrator;
	private String              runCommand ;                // 프로세스 기동을 위한 run_batch.sh 와 같은 쉘명.
	private int                 programNameArgIndex = 2;    // 프로그램명(COMPONENT_NAME)을 설정하는 ARG 번호는?  기본값은 2. 혹시 ARG2 말고 다른 argument 로 프로그램명을 받아야할 필요가 있는 경우를 대비하여

	public CBatchJobRunner() {
	}
	
	public void init() {
	}
	
	public void destroy() {
	}

	public NSCIntegrator getNscIntegrator() {
		return nscIntegrator;
	}

	public void setNscIntegrator(NSCIntegrator nscIntegrator) {
		this.nscIntegrator = nscIntegrator;
	}

	public String getRunCommand() {
		return runCommand;
	}

	public void setRunCommand(String runCommand) {
		this.runCommand = runCommand;
	}

	public int getProgramNameArgIndex() {
		return programNameArgIndex;
	}

	public void setProgramNameArgIndex(int programNameArgIndex) {
		this.programNameArgIndex = programNameArgIndex;
	}

	public void start(final JobExecution je, final JobContext context, final IJobRunnerCallBack jobRunnerCallBack) {
		if (Util.isBlank(runCommand)) {
			throw new AgentException("com.error.occurred.below", "CBatchJobRunner.runCommand attribute is null");
		}
		/*
		 * ARG1 : 배치 종류 (일반 : N, 데몬 : ...)
		 * ARG2 : COMPONENT_NAME
		 */
		je.getInParameters().put("ARG"+programNameArgIndex, context.getInParameter("COMPONENT_NAME"));
		je.setComponentName(runCommand);
		super.start(je, context, jobRunnerCallBack);
	}
	
	public void suspend(String jobExecutionId) {
		nscIntegrator.suspend(jobExecutionId);
	}
	
	public void resume(String jobExecutionId) {
		nscIntegrator.resume(jobExecutionId);
	}

	public void stop(String jobExecutionId) {
		super.stop(jobExecutionId);
	}

	/**
	 * 프로세스가 종료된후 callback 하기 전에 이 메소드가 호출된다.
	 * 프로세스 종료후 최종 진행률 값을 조회하여 세팅한다.
	 */
	public void afterProcessExit(JobExecution jobexe) {
		long[] progress = nscIntegrator.getJobProgress(jobexe.getJobExecutionId());
		if (progress == null) return;
		jobexe.setProgressTotal  (progress[0]);
		jobexe.setProgressCurrent(progress[1]);
		nscIntegrator.deleteMemoryBlock(jobexe.getJobExecutionId()); // NSC 메모리도 삭제한다.
		
	}
}
