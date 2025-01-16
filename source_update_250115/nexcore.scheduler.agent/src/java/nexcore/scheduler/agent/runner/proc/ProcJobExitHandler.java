package nexcore.scheduler.agent.runner.proc;

import java.io.File;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.internal.AgentConstants;
import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 쉘실행, CBATCH, JAVARUN 타입의 Job 에서 프로세스 종료 이후의 일을 처리함.</li>
 * <li>작성일 : 2013. 6. 29.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class ProcJobExitHandler implements IProcJobExitHandler {
	private IJobRunnerCallBack      jobRunnerCallBack;
	private boolean                 deleteRunnerFile = true;
	
	public ProcJobExitHandler() {
	}
	
	public void init() {
	}
	
	public void destroy() {
	}
	
	public IJobRunnerCallBack getJobRunnerCallBack() {
		return jobRunnerCallBack;
	}

	public void setJobRunnerCallBack(IJobRunnerCallBack jobRunnerCallBack) {
		this.jobRunnerCallBack = jobRunnerCallBack;
	}

	public boolean isDeleteRunnerFile() {
		return deleteRunnerFile;
	}

	public void setDeleteRunnerFile(boolean deleteRunnerFile) {
		this.deleteRunnerFile = deleteRunnerFile;
	}

	/**
	 * 프로세스 타입 Job 종료후 처리할 로직을 여기서함.
	 * 기본 ProcJobRunner 의 종료시에도 호출하며,
	 * 에이전트 restart 시에 이전 프로세스 복구후 종료 시에도 여기서함.
	 * 
	 * 1) useParameterFile 속성이 true 이면 OUT 파라미터 파일을 읽어 JobExe 에 set.
	 * 
	 * 2) JobRunner 의 afterProcessExit() 호출
	 * 
	 * 3) runner 파일 (ncbatch_jobexeid.sh/.cmd) 삭제
	 * 
	 * 4) callBackJobEnd() 호출
	 * 
	 * 5) jeproc/*.je 파일 삭제
	 * 
	 * 6) -exit.log 파일 삭제
	 *  
	 * @param jobexe
	 * @param runnerFile
	 */
	public void handleProcessExit(JobContext context, ProcJobRunner jobRunner, String runnerFile) {
		JobExecution jobexe       = context.getJobExecution();
	    ILogger      log          = context.getLogger();
		
		/* 
		 * out parameter 파일이 만들어졌다면 그 파일안의 returncode, errormsg, return value 가 우선한다. 
		 * 그 값들로 update 한다.
		 */
		if (jobRunner.isUseParameterFile()) {
			jobRunner.readOutParameterFile(context, log);
		}
		
		try {
			// 프로세스 종료 후처리 로직 돌린다.
			Util.logInfo(log, "Call afterProcessExit()");
			jobRunner.afterProcessExit(context);
		}catch(Throwable e) {
			Util.logError(log, "afterProcessExit() error. ignore", e); // 로그만 찍고 다음 계속한다.
		}
		
		// ncbatch_jobexeid.sh 파일 삭제.
		if (runnerFile != null && deleteRunnerFile) {
			try {
				boolean b = new File(runnerFile).delete();
				Util.logInfo(log, runnerFile+" deleted. result="+b);
			}catch(Throwable e) {
				// 로그찍고, 무시한다.
				Util.logError(log, runnerFile+" delete fail.", e);
			}
		}
		
		// callback.
		jobRunnerCallBack.callBackJobEnd(jobexe);
		
		// jeproc/*.je 파일 삭제. callback 이 정상적으로 된 후에 삭제한다.
		File jobexeObjFile = new File(Util.getHomeDirectory()+AgentConstants.JOBEXEPROC_FILE_DIRECTORY+"/"+jobexe.getJobExecutionId()+".je");
		try {
			boolean b = jobexeObjFile.delete();
			Util.logInfo(log, jobexeObjFile+" deleted. result="+b);
		}catch(Throwable e) {
			// 로그찍고, 무시한다.
			Util.logError(log, jobexeObjFile.getAbsolutePath()+" delete fail.", e);
		}
		
		// -exit.log 파일 삭제. callback 이 정상적이면 -exit.log 파일도 필요없다.
		File exitLogFile = new File(Util.getHomeDirectory()+AgentConstants.RUNNER_FILE_DIRECTORY+"/"+jobexe.getJobExecutionId()+"-exit.log");
		try {
			boolean b = exitLogFile.delete();
			Util.logInfo(log, exitLogFile+" deleted. result="+b);
		}catch(Throwable e) {
			// 로그찍고, 무시한다.
			Util.logError(log, exitLogFile.getAbsolutePath()+" delete fail.", e);
		}
	}			
}
