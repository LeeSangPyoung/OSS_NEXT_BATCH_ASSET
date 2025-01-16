package nexcore.scheduler.agent.runner.proc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.internal.AgentConstants;
import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.agent.runner.AbsJobRunner;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

import org.apache.commons.logging.Log;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 외부 명령어를 실행시킴. 쉘/command 등 non-java program 들을 exec 함.</li>
 * <li>작성일 : 2010. 4. 22.</li>
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
public class ProcJobRunner extends AbsJobRunner {
	private ProcessManager          processManager;
	/*
	 * 2013-01-22. v3.6.1 부터
	 * PROC 타입 계열의 Job 인 경우 in/out 파라미터를 파일로 생성함. (기본값=true)
	 * 이 기능을 사용하지 않으려면 이 속성 (useParameterFile) 을 false로 한다.
	 */
	private boolean                 useParameterFile = true;
	
	/*
	 * 2013-07-01 v3.6.3 부터
	 * 프로세스 종료이후의 처리 과정은 별도의 handler 에서 처리함.
	 * 에이전트 restart 시에 이전 프로세스 복구시에도 이 로직을 동일하게 처리하기 위해 별도 클래스로 분리함. 
	 */
	private IProcJobExitHandler     procJobExitHandler;
	
	/**
	 * 파라미터 파일을 read/writer 할때 사용하는 인코딩. 한글이 값으로 들어 있는 경우 encoding 설정이 필요함.
	 */
	private String                  parameterFileEncoding;

	
	public ProcJobRunner() {
	}
	
	public void init() {
	}
	
	public void destroy() {
	}

	public ProcessManager getProcessManager() {
		return processManager;
	}

	public void setProcessManager(ProcessManager processManager) {
		this.processManager = processManager;
	}
	
	public boolean isUseParameterFile() {
		return useParameterFile;
	}

	public void setUseParameterFile(boolean useParameterFile) {
		this.useParameterFile = useParameterFile;
	}
	
	public IProcJobExitHandler getProcJobExitHandler() {
		return procJobExitHandler;
	}

	public void setProcJobExitHandler(IProcJobExitHandler procJobExitHandler) {
		this.procJobExitHandler = procJobExitHandler;
	}

	public String getParameterFileEncoding() {
        return parameterFileEncoding;
    }

    public void setParameterFileEncoding(String parameterFileEncoding) {
        this.parameterFileEncoding = parameterFileEncoding;
    }

    public void start(final JobExecution je, final JobContext context, final IJobRunnerCallBack jobRunnerCallBack) {
		getJobRunThreadManager().newThreadAndStart(context,
			new Runnable() {
				public void run() {
					int          returnCode   = 0; // 0 이면 정상 종료 그외 숫자는 에러
					ILogger      logger       = getAgentMain().getJobLogManager().getLog(context);
					String       errorMsg     = null;
					Process      proc         = null;
					long         procEndTime  = 0;
					
					String runnerFile    = null;
					Throwable throwable  = null;
					try {
					    // 선처리 실행
	                    doJobExePreProcessors(context);

						logJobStart(context);
						
						// ■ jeproc 디렉토리에 JobExecution 파일 생성.
						writeJobExeObjFileForStart(je, logger);
						
						// ■ IN 파라미터 파일 생성.
						if (useParameterFile) {
							writeInParameterFile(je, logger);
						}
						
						// ■ 프로세스 기동.
						runnerFile = processManager.createRunnerShell(context);
						proc = processManager.execProcess(runnerFile, context);

						Util.logInfo(logger, "Process waitFor() ");
						returnCode     = proc.waitFor();
						procEndTime    = System.currentTimeMillis();
						
						Util.logInfo(logger, "Process exit. return="+returnCode);
						
					}catch (Throwable e) {
					    throwable = e;
						returnCode = -1; // TODO 에러코드 정리. 
						Throwable cause = Util.getCauseException(e);
						errorMsg = Util.fitLength(cause.getMessage(), 1000);
						Util.logError(logger, cause.toString(), e);
					}finally {
						try {
							if (proc != null) proc.destroy();
						}catch(Exception ignore) {
						}finally {
							je.setEndTime(procEndTime);
							je.setReturnCode(returnCode);
							je.setErrorMsg(errorMsg);
							je.setReturnValues(context.getReturnValues());
							je.setState(JobExecution.STATE_ENDED);
							
							procJobExitHandler.handleProcessExit(context, ProcJobRunner.this, runnerFile);
							
							/*
							 * handleProcessExit 에서 OUT 파라미터를 이용하여 RETURN_CODE, RETURN VALUES 들이 SET 될 수 있으므로,
							 * 먼저 handleProcessExit() 를 호출하고 lobJobEnd 를 한다.
							 */
							logJobEnd(context); 

							getJobExecutionBoard().remove(je.getJobExecutionId());
							processManager.remove(je.getJobExecutionId());
							
			                 // 후처리 실행
		                    doJobExePostProcessors(context, throwable);
						}
					}
				}
			});
	}
	
	/**
	 * 프로세스가 exit 된 후에 callback 이 호출되기 전에 호출되는 후처리 메소드.
	 * CBatch 타입에서 이것을 상속 받아 사용한다.
	 * @param context 프로세스 종료코드가 JobExecution 의 returnCode 에 들어있다.
	 */
	public void afterProcessExit(JobContext context) {
	}
    
	public void suspend(String jobExecutionId) {
		throw new AgentException("agent.proctype.unsupported.operation", "suspend");
	}
	
	public void resume(String jobExecutionId) {
		throw new AgentException("agent.proctype.unsupported.operation", "resume");
	}

	public void stop(String jobExecutionId) {
		// EJB 타입과 달리 Process 를 kill (destroy) 하고 스레드는 stop 하지 않는다.
//		Process proc = processManager.get(jobExecutionId);
//		if (proc == null) {
//			throw new AgentException("agent.proctype.cannot.find.process.info", jobExecutionId);
//		}else {
// 2013.07.15 에이전트 reboot 후에는 rescue 된 JobExe 들은 processManager 체크할 필요없다. 

	        JobContext context = agentMain.getJobExecutionBoard().getJobContext(jobExecutionId);
			if (context != null) {
				context.setStopForced(true);
				context.getLogger().info(MSG.get("agent.proctype.process.force.stop"));
			}
			
			// destroy() 를 먼저하면 위에서 이 스레드가 removeJobExecution() 를 먼저할 수도 있으므로, 로깅먼저한다.
			Log ll = context.getLogger();
			processManager.killProcess(jobExecutionId, ll == null ? log : ll);
//		}
	}

	/**
	 * PROC, JAVARUN, CBATCH 타입의 실행시에는 JobExecution 객체를 jeproc 디렉토리에 저장한다.
	 * 에이전트 reboot 시에 이전 실행의 JobExecution 를 복구하기 위한 조치이며,
	 * 정상 종료시에 이 파일을 삭제한다.  
	 * @param jobexe
	 * @param logger
	 * @throws IOException
	 */
	protected File writeJobExeObjFileForStart(JobExecution jobexe, ILogger logger) throws IOException  {
		File dir = new File(Util.getHomeDirectory()+AgentConstants.JOBEXEPROC_FILE_DIRECTORY);
		File f   = new File(dir, jobexe.getJobExecutionId()+".je");

		// 디렉토리 생성
		if (!dir.exists()) {
			dir.mkdirs();
		}

		if (log.isDebugEnabled()) {
			Util.logDebug(log, "[ProcJobRunner] Write JobExecution obj file. ["+f.getCanonicalPath()+"]");
		}
		
		// 파일 write
		Util.writeObjectToFile(f, jobexe);
		
		return f;
	}
	
	/**
	 * IN 파라미터 파일을 생성함.
	 * 파일명 : $AGENT_HOME/batch/param/[PROC_DATE]/[JobExeId].in
	 * @param jobexe
	 * @param context
	 */
	protected void writeInParameterFile(JobExecution jobexe, ILogger logger) throws IOException  {
		File dir     = new File(Util.getHomeDirectory()+AgentConstants.PARAMETER_FILE_DIRECTORY+"/"+jobexe.getProcDate());
		File inFile  = new File(dir, jobexe.getJobExecutionId()+".in");
		File outFile = new File(dir, jobexe.getJobExecutionId()+".out");
		
		String inParameterFile  = inFile.getCanonicalPath();
		String outParameterFile = outFile.getCanonicalPath();
				
		jobexe.getInParameters().put("ENV_"+AgentConstants.NBS_PIN_FILE,  inParameterFile);
		jobexe.getInParameters().put("ENV_"+AgentConstants.NBS_POUT_FILE, outParameterFile);
		
		// 디렉토리 생성
		if (!dir.exists()) {
			dir.mkdirs();
		}

		if (log.isDebugEnabled()) {
			Util.logDebug(log, "[ProcJobRunner] ["+jobexe.getJobExecutionId()+"] Write Job In Parameter file. ["+inParameterFile+"]");
		}

		Util.logInfo(logger, "Write Job In Parameter file. ["+inParameterFile+"]");
		
		// 파일 생성
		PrintWriter fout = null;
		try {
			if (Util.isBlank(parameterFileEncoding)) {
				fout = new PrintWriter(new FileWriter(inFile)); // JVM 기본 인코딩 적용
			}else {
				fout = new PrintWriter(new OutputStreamWriter(new FileOutputStream(inFile), parameterFileEncoding)); // 수동 인코딩 적용
			}

			for (Map.Entry param : jobexe.getInParameters().entrySet()) {
				fout.print(param.getKey());
				fout.print("=");
				fout.println(param.getValue());
			}
		}finally {
			try { fout.close(); }catch(Exception ignore) {}
		}
		
		// 파일의 삭제는 에이전트 내의 별도 데몬에 의해 삭제되도록 한다. (로그, jestore, param 파일 삭제 데몬)
	}

	/**
	 * OUT 파라미터 파일을 읽어 errorCode, errorMessage, ReturnValue 들을 세트함.
	 * 파일이 없으면 skip함.
	 * 파일명 : $AGENT_HOME/batch/param/[PROC_DATE]/[JobExeId].out
	 * @param jobexe
	 * @param context
	 */
	protected void readOutParameterFile(JobContext context, ILogger logger) {
	    JobExecution jobexe = context.getJobExecution();
		File f = new File(Util.getHomeDirectory()+AgentConstants.PARAMETER_FILE_DIRECTORY+"/"+jobexe.getProcDate()+"/"+jobexe.getJobExecutionId()+".out");
		if (!f.exists()) { // out 파라미터 파일이 없음. skip.
			return;
		}
		BufferedReader fin = null;
		try {
			if (log.isDebugEnabled()) {
				Util.logDebug(log, "[ProcJobRunner] ["+jobexe.getJobExecutionId()+"] Read Job Out Parameter (return value) file. ["+f.getCanonicalPath()+"]");
			}

			Util.logInfo(logger, "Read Job Out Parameter (return value) file. ["+f.getCanonicalPath()+"]");
			
			if (Util.isBlank(parameterFileEncoding)) {
				fin = new BufferedReader(new FileReader(f)); // JVM 기본 인코딩 적용
			}else {
				fin = new BufferedReader(new InputStreamReader(new FileInputStream(f), parameterFileEncoding)); // 수동 인코딩 적용
			}

			/*
			 * 파일 레이아웃
			 * 1 라인 : RETURN_CODE=0
			 * 2 라인 : PROGRESS_CURRENT=19998
			 * 3 라인 : PROGRESS_TOTAL=20000
			 * 4 라인 : RETURN_MESSAGE=정상/에러 메세지
			 * 5 라인 : key1=value1
			 * 6 라인 : key2=value2.
			 * .... 계속
			 */
			// 에러코드, 처리건수 (진행률), 에러메세지,를 읽는다. 콤마(,)로 구분된다. 
			// 에러코드는 process 의 exit value 보다 이것이 우선한다.

			String line = null;
			while(true) {
				line = fin.readLine();
				if (line == null) {
					break;
				}
				line = line.trim();
				
				int idx = line.indexOf('=');
				if (idx < 0) { // '=' 가 없는 라인은 그냥 무시한다.
					continue;
				}
				
				String key  = line.substring(0, idx);
				String value = line.substring(idx+1);

				if ("RETURN_CODE".equalsIgnoreCase(key)) {
					try {
						jobexe.setReturnCode      (Integer.parseInt(value));
					}catch(NumberFormatException ignore) {} // 리턴코드가 int 가 아니면 set 하지 말고 무시한다.
				}else if ("PROGRESS_CURRENT".equalsIgnoreCase(key)) {
					try {
						jobexe.setProgressCurrent (Long.parseLong(value));
					}catch(NumberFormatException ignore) {} // 리턴코드가 long 이 아니면 set 하지 말고 무시한다.
				}else if ("PROGRESS_TOTAL".equalsIgnoreCase(key)) {
					try {
						jobexe.setProgressTotal   (Long.parseLong(value));
					}catch(NumberFormatException ignore) {} // 리턴코드가 long 이 아니면 set 하지 말고 무시한다.
				}else if ("RETURN_MESSAGE".equalsIgnoreCase(key)) {
					jobexe.setErrorMsg        (value);
				}else {
					jobexe.setReturnValue(key, value);
				}
			}
		}catch (Exception e) {
			Util.logError(logger, "Reading OutParameterFile fail. ["+f+"]", e);
			Util.logError(log, "[ProcJobRunner] Reading OutParameterFile fail. ["+f+"]", e);
		}finally {
			try { fin.close(); }catch(Exception ignore) {}
		}
	}

}
