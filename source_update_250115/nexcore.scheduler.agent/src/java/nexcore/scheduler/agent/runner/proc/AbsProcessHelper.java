/**
 * 
 */
package nexcore.scheduler.agent.runner.proc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.internal.AgentConstants;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Super of WindowsProcessHelper, UnixProcessHelper </li>
 * <li>작성일 : 2013. 1. 29.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public abstract class AbsProcessHelper implements IProcessHelper {

	public AbsProcessHelper() {
		super();
	}

	/**
	 * Job 파라미터 값으로 프로세스 Argument 를 구성함
	 * @param context
	 * @return List of argument
	 */
	protected List<String> makeArgumentList(JobContext context) {
		List<String> argList = new ArrayList(20);
		
		for (int i=1; ;i++) {
			String argValue = context.getInParameter("ARG"+i);
			if (argValue != null) {
				argList.add(argValue);
			}else {
				break;
			}
		}
		return argList;
	}

	/**
	 * Job 파라미터 값으로 프로세스 환경변수 를 구성하여 set 함.
	 * @param context
	 * @param defaultEnv 프로세스의 환경변수 Map
	 */
	protected void setEnvironment(JobContext context, Map defaultEnv) {
		// 파라미터중 ENV_ 로 시작하는 파라미터는 _ 다음 문자를 환경변수명으로 해서 환경변수에 세팅한다.
		// 예) ENV_MYVAR=${PROCDATE} 일 경우 MYVAR=20101021 이라는 값이 프로세스의 환경변수로 세팅됨.
		for (Map.Entry entry : context.getInParameters().entrySet()) {
			String paramName = (String)entry.getKey();
			if (paramName != null && paramName.startsWith("ENV_")) {
				String envName  = paramName.substring(paramName.indexOf('_')+1);
				String envValue = (String)entry.getValue();
				defaultEnv.put(envName, envValue);
			}
		}
		
		// NBS_JOB_ID, NBS_JOB_INS_ID, NBS_JOB_EXE_ID 가 환경변수로 들어간다. since 4.0
		defaultEnv.put(AgentConstants.NBS_JOB_ID,     context.getJobExecution().getJobId());
		defaultEnv.put(AgentConstants.NBS_JOB_INS_ID, context.getJobExecution().getJobInstanceId());
		defaultEnv.put(AgentConstants.NBS_JOB_EXE_ID, context.getJobExecution().getJobExecutionId());
		
		/*
		 * 로그레벨이 수동으로 변경되었다면 그 변경된 로그레벨 이 배치 FWK 의 배치 profile 보다는
		 * 더 중요하다는 판단이므로 그 변경된 로그레벨로 singlejvm 의 로그 레벨그 값을 배치 프로세스에게 전달한다.
		 * context.getJobExecution().getLogLevel() 에는 에이전트의 기본 로그레벨이 들어있으므로 
		 */
		if (!Util.isBlank(context.getInParameter("LOG_LEVEL"))) { 
			defaultEnv.put(AgentConstants.NBS_LOG_LEVEL, context.getInParameter("LOG_LEVEL"));
		}
	}

	/**
	 * redirect 할 stdout 파일명 리턴.
	 * @param context
	 * @return
	 */
	protected String getStdoutFile(JobContext context) {
		File logDirectory = context.getLogger().getDirectory();
		File stdoutLogFile = new File(logDirectory + "/" + context.getJobExecution().getJobInstanceId()+"-stdout.log");
		return stdoutLogFile.getAbsolutePath();
	}

	/**
	 * exit value 를 담아둘 파일명 리턴.
	 * nbs_agent/batch/runner/JobExeId-exit.log 형태로 저장
	 * @param context
	 * @return
	 */
	protected String getExitValueFile(JobContext context) {
		File f = new File(Util.getHomeDirectory() + AgentConstants.RUNNER_FILE_DIRECTORY + "/" + context.getJobExecution().getJobExecutionId()+"-exit.log");
		return f.getAbsolutePath();
	}

}