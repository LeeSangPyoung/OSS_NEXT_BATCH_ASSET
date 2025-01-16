/**
 * 
 */
package nexcore.scheduler.agent.runner.proc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.IJobRunner;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.internal.AgentConstants;
import nexcore.scheduler.agent.internal.JobExecutionBoard;
import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.agent.joblog.JobLogManager;
import nexcore.scheduler.agent.prepost.IPrePostProcessorManager;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 에이전트 startup 시에 Proc, CBATCH, JAVARUN 타입의 Job 들이 이전에 실행되서 돌고 있는지 확인하여 복구함 </li>
 * <li>작성일 : 2013. 6. 21.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class ProcessRecoveryOnStart implements Runnable {

	private JobExecutionBoard               jobExecutionBoard;    
	private IProcessHelper                  processHelper;
	private IProcJobExitHandler             procJobExitHandler;
	private Map<String, IJobRunner>         jobRunnerMap; 
	private JobLogManager                   jobLogManager;
	private String                          enabled;             /* 로컬 개발환경과 같이 쉘실행 타입을 실행하지 않는 경우를 위해 disable 가능하게 함. 기본값 : true */             
	
	private List<JobContext>                rescuedJobExecutions; // 에이전트 restart 이전에 이미 실행되고 있던 프로세스들의 BatchContext list
	
    private IPrePostProcessorManager        prePostProcessorManager; // 복구후에 종료시에도 후처리기 돌려줘야한다. 3.8.0 부터

	
	private Thread                          thisThread;
	private Log                             log;
	private boolean                         destroyed;
	
	public ProcessRecoveryOnStart() {
	}

	public void init() {
		log = LogManager.getAgentLog();
		if ("false".equalsIgnoreCase(enabled) || "no".equalsIgnoreCase(enabled) || "off".equalsIgnoreCase(enabled)) { 
			/* 명시적으로 false 로 지정될때만 disable 하고, 그외는 true로 간주한다. */
			Util.logInfo(log, "[ProcessRecoveryOnStart] disabled.");
		}else {
			rescuedJobExecutions = new ArrayList<JobContext>();
	
			// 현재 프로세스 실행중인것 찾아서 복구.
			findAllNCBatchProcessesAndRescue();
			
			// 에이전트 다운중에 끝났놈 찾아서 복구.
			findExitLogFilesAndRescue();
			
			thisThread = new Thread(this, "ProcessRecoveryOnStart");
			thisThread.setDaemon(true);
			thisThread.start();
		}
	}
	
	public void destroy() {
		destroyed = true;
	}
	
	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public IProcessHelper getProcessHelper() {
		return processHelper;
	}

	public void setProcessHelperMap(Map<String, IProcessHelper> map) {
		String osName = System.getProperty("os.name").toLowerCase();
		
		if (osName.indexOf("windows") > -1) {
			this.processHelper = map.get("windows");
		}else {
			this.processHelper = map.get("unix");
		}
		
		if (processHelper == null) {
			throw new IllegalArgumentException("ProcessHelper is null");
		}
	}

	public Map<String, IJobRunner> getJobRunnerMap() {
		return jobRunnerMap;
	}

	public void setJobRunnerMap(Map<String, IJobRunner> jobRunnerMap) {
		this.jobRunnerMap = jobRunnerMap;
	}

	private ProcJobRunner getJobRunner(String jobType) {
		ProcJobRunner jr = (ProcJobRunner)jobRunnerMap.get(jobType);
		if (jr == null) {
			throw new AgentException("agent.jobtype.error", jobType);
		}else {
			return jr;
		}
	}
	
	public JobLogManager getJobLogManager() {
		return jobLogManager;
	}

	public void setJobLogManager(JobLogManager batchLogManager) {
		this.jobLogManager = batchLogManager;
	}

	public List<JobContext> getRescuedJobExecutions() {
		return rescuedJobExecutions;
	}

	public void setRescuedJobExecutions(List<JobContext> rescuedJobExecutions) {
		this.rescuedJobExecutions = rescuedJobExecutions;
	}

	public JobExecutionBoard getJobExecutionBoard() {
		return jobExecutionBoard;
	}

	public void setJobExecutionBoard(JobExecutionBoard jobExecutionBoard) {
		this.jobExecutionBoard = jobExecutionBoard;
	}

	public IProcJobExitHandler getProcJobExitHandler() {
		return procJobExitHandler;
	}

	public void setProcJobExitHandler(IProcJobExitHandler procJobExitHandler) {
		this.procJobExitHandler = procJobExitHandler;
	}

	public IPrePostProcessorManager getPrePostProcessorManager() {
        return prePostProcessorManager;
    }

    public void setPrePostProcessorManager(IPrePostProcessorManager prePostProcessorManager) {
        this.prePostProcessorManager = prePostProcessorManager;
    }

    /**
	 * startup 시에 ps -ef 를 수행하여 (ncbatch_*** 찾는다) 이전에 기동된 프로세스들이 있는지 확인하고, 
	 * JobExeId 를 extract 한 후, JobExecution 객체를 jeproc 디렉토리에서 읽어 JobExecutionBoard,  rescuedJobExecutions 에 put 한다.
	 */
	private void findAllNCBatchProcessesAndRescue() {
		Util.logInfo(log, "[ProcessRecoveryOnStart] findAllNCBatchProcessesAndRescue() started.");
		List<String> psOut = processHelper.listProcesses("ncbatch_", log);
		for (String s : psOut) {
			Util.logDebug(log, "[ProcessRecoveryOnStart] "+s);
			if (Util.isBlank(s)) continue;
			
			int beginIdx = s.indexOf("ncbatch_");
			int endIdx   = s.lastIndexOf(".");
			
			String fileExt = s.substring(endIdx+1);
			
			if (fileExt.startsWith("sh") || fileExt.startsWith("cmd")) {  /* 확장자가 .sh 또는 .cmd 인 것을 찾는다. */
				String jobExeId = s.substring(beginIdx + 8, endIdx);
				Util.logDebug(log, "[ProcessRecoveryOnStart] JobExeId : "+jobExeId);
				
				// JobExecutionID 로 jeproc 디렉토리에서 je 파일 읽어 JobExecution 객체 만든다.
				File dir = new File(Util.getHomeDirectory()+AgentConstants.JOBEXEPROC_FILE_DIRECTORY);
				File f   = new File(dir, jobExeId+".je");
				if (f.exists()) { // je 파일이 있다는 것은 미처리(정상종료 & callback) 됐다는 뜻
					try {
						JobExecution jobexe = (JobExecution)Util.readObjectFromFile(f);
						
						JobContext context = new JobContext();
						context.setJobExecution(jobexe);
						ILogger joblog = jobLogManager.getLog(context);
						Util.logInfo(joblog, "[ProcessRecoveryOnStart] "+jobExeId+" rescued. (still running)");
		
						// JobExecutionBoard 에 추가
						jobExecutionBoard.add(context);
		
						// RescueList 에 추가
						rescuedJobExecutions.add(context);
	
						Util.logInfo(log, "[ProcessRecoveryOnStart] JobExeId : "+jobExeId+" rescued. (still running)");
					}catch (Exception e) {
						// jeproc 디렉토리의 je 파일 읽다가 에러나는 경우. 파일이 뭔가 깨졌다...비정상 상황
						Util.logWarn(log, "[ProcessRecoveryOnStart] Process exists ["+s+"]. But Reading jeproc file ("+f.getName()+") fail. ", e);
					}
					
				}else {
					// ps 한 결과 프로세스는 살아있는데 이에 해당하는 JobExecution 파일이 없다. 이건 뭔가 잘못된 상황
					Util.logWarn(log, "[ProcessRecoveryOnStart] Process exists ["+s+"]. But No jeproc file exist ["+f+"]. ");
				}
			}
		}
		Util.logInfo(log, "[ProcessRecoveryOnStart] findAllNCBatchProcessesAndRescue() ended.");
	}

	/**
	 * batch/runner 디렉토리 scan 하여 #####-exit.log 파일 찾음. 
	 * 이 파일의 JobExeId 도 RescuedJobExecutionBoard 에 put.
	 * 이 경우는 에이전트가 다운되어있는 동안에 배치 프로그램이 종료된 경우임.
	 */
	private void findExitLogFilesAndRescue() {
		Util.logInfo(log, "[ProcessRecoveryOnStart] findExitLogFilesAndRescue() started.");
		
		File dir = new File(Util.getHomeDirectory()+AgentConstants.RUNNER_FILE_DIRECTORY);
		File[] filesInRunnerDir = dir.listFiles();
		if (filesInRunnerDir == null) return;
		
		for (File exitLogFile : filesInRunnerDir) {
			String filename = exitLogFile.getName();
			if (filename.endsWith("-exit.log")) {  /* ####-exit.log 파일을 찾는다. */
				int idx = filename.lastIndexOf("-exit.log");
				String jobexeid = filename.substring(0, idx);
				
				// JobExecutionID 로 jeproc 디렉토리에서 je 파일 읽어 JobExecution 객체 만든다.
				File jobexefile   = new File(Util.getHomeDirectory()+AgentConstants.JOBEXEPROC_FILE_DIRECTORY+"/"+jobexeid+".je");
				if (jobexefile.exists()) { // je 파일이 있다는 것은 미처리(정상종료 & callback) 됐다는 뜻
					try {
						JobExecution jobexe = (JobExecution)Util.readObjectFromFile(jobexefile);
	
						JobContext context = new JobContext();
						context.setJobExecution(jobexe);
						ILogger joblog = jobLogManager.getLog(context);
						Util.logInfo(joblog, "[ProcessRecoveryOnStart] "+jobexeid+" rescued. (already ended)");
		
						// JobExecutionBoard 에 추가
						jobExecutionBoard.add(context);
	
						// RescueList 에 추가
						rescuedJobExecutions.add(context);
						Util.logInfo(log, "[ProcessRecoveryOnStart] JobExeId : "+jobexeid+" rescued. (already ended)");
					}catch(Exception e) {
						// jeproc 파일 읽다게 에러난 경우. 복구 대상에서 제외한다.
						Util.logWarn(log, "[ProcessRecoveryOnStart] '-exit.log' file exists ["+exitLogFile.getName()+"].  But Reading jeproc file ("+jobexefile.getName()+") fail. ignore ", e);
					}
				}else {
					// -exit.log 파일은 있는데 jeproc 디렉토리에는 파일이 없는 경우, 잘못된 상황이거나 오래전 쓰레기 파일이 남아있는 경우.
					Util.logWarn(log, "[ProcessRecoveryOnStart] '-exit.log' file exists ["+exitLogFile.getName()+"]. But No jeproc file exist ["+jobexefile+"].");
				}
			}
		}
		
		Util.logInfo(log, "[ProcessRecoveryOnStart] findExitLogFilesAndRescue() ended.");
	}
	
	/**
	 * runner/~~-exit.log 파일 객체
	 * @param jobexeid
	 * @return
	 */
	private File getExitLogFile(String jobexeid) {
		return new File(Util.getHomeDirectory()+AgentConstants.RUNNER_FILE_DIRECTORY+"/"+jobexeid+"-exit.log");
	}
	
	/**
	 * runner/~~-exit.log 파일을 읽어 return code 값을 읽는다.
	 * @param jobexeid
	 * @return
	 */
	private int getReturnCodeFromExitLogFile(File exitLogFile) throws IOException {
		BufferedReader in = null;
		
		try {
			in = new BufferedReader(new FileReader(exitLogFile));
			String line = null;
			while((line = in.readLine()) != null) {
				try {
					int intVal = Integer.parseInt(line.trim());
					return intVal; // 파싱 정상이면 리턴.
				}catch(Exception e) {
					throw Util.toRuntimeException(e);
				}
			}
		}finally {
			try { 
				in.close(); 
			}catch(Exception e) {
				e.printStackTrace(); // ignore
			}
		}
		// 서버 전체가 shutdown 되면 -exit.log 파일이 0 바이트가 되는 경우가 있을 수 있다. 이 경우는 어차피 에러 상황이므로 특수 에러코드 (98) 리턴한다.
		Util.logInfo(log, "[ProcessRecoveryOnStart] "+exitLogFile+" file is abnormal. set return code to 98");
		return 98;
	}
	
	public void run() {
		Util.logInfo(log, "[ProcessRecoveryOnStart] monitoring started.");
		
		while(!destroyed) {
			Iterator<JobContext> iter = rescuedJobExecutions.iterator();
			while(iter.hasNext()) {
				JobContext context = iter.next();
				JobExecution jobexe = context.getJobExecution();

				try {
					// 리스트에서 꺼내 exit 여부를 확인한다.
					File exitLogFile = getExitLogFile(jobexe.getJobExecutionId());
					if (exitLogFile.exists()) { // 프로세스 종료되어 exit.log 파일 생성됐다.
						/* 종료한지 1초 이상이 흐른 경우만 JobEnd 처리한다. 아직 exit.log 파일이 만들어지고 있는 중일 수 있으므로 */
						long exitLogFileTime = exitLogFile.lastModified();
						if (exitLogFileTime + 1000 < System.currentTimeMillis()) {
							int exitCode = getReturnCodeFromExitLogFile(exitLogFile);
							jobexe.setReturnCode(exitCode);
							jobexe.setEndTime(exitLogFileTime);
							jobexe.setState(JobExecution.STATE_ENDED);

							ILogger log = context.getLogger();
							Util.logInfo(log, "[ProcessRecoveryOnStart] "+jobexe.getJobExecutionId()+" exit detected. exit code="+exitCode);
							
							ProcJobRunner jobRunner  = getJobRunner(jobexe.getJobType());
							String        runnerFile = processHelper.getRunnerShellFilename(jobexe.getJobExecutionId());
							procJobExitHandler.handleProcessExit(context, jobRunner, runnerFile);
							
							jobRunner.logJobEnd(context); 
	
							jobExecutionBoard.remove(jobexe.getJobExecutionId());
							iter.remove();
							
					         // JobExecution 후처리기 실행.
							if (prePostProcessorManager!=null) {
							    prePostProcessorManager.doJobExePostProcessors(context, null);
							}
						}
					}
				}catch(Throwable e) {
					Util.logError(log, "[ProcessRecoveryOnStart] rescue fail. "+jobexe.getJobExecutionId(), e);
				}
			}
			
			if (rescuedJobExecutions.size() == 0) break; // 다 끝나면 이 쓰레드는 종료한다.
			Util.sleep(10*1000); // 10 초에 한번씩 한다.
		}
		
		Util.logInfo(log, "[ProcessRecoveryOnStart] all processes ended. This thread exit.");
	}
}
