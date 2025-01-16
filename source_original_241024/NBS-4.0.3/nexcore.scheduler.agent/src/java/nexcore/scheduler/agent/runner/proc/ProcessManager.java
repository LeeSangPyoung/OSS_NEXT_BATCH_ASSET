package nexcore.scheduler.agent.runner.proc;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.log.LogManager;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 </li>
 * <li>설  명 : 프로세스 관리. kill을 위한 부가 기능도 포함 </li>
 * <li>작성일 : 2010. 10. 20.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ProcessManager {
	// Process 객체 관리
	private Map<String, Process> processMap = new ConcurrentHashMap<String, Process>();   // <JOB_EXE_ID, Process>
	
	// 프로세스 kill을 위한 helper 클래스
	private IProcessHelper processHelper;
	
	private Log            log;
	
	public ProcessManager() {
	}
	
	public void init() {
	    
	}
	
	public void destroy() {
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
		log = LogManager.getAgentLog();
		log.info("[ProcessManager] ProcessHelper "+processHelper+" selected");
	}

	public void put(String jobExecutionId, Process proc) {
		processMap.put(jobExecutionId, proc);
	}
	
	public Process get(String jobExecutionId) {
		return processMap.get(jobExecutionId);
	}
	
	public Process remove(String jobExecutionId) {
		return processMap.remove(jobExecutionId);
	}
	
	
	/**
	 * 프로세스 실행을 위한 runner sh (또는 cmd) 파일 생성.
	 * @param context
	 * @return runner sh 의 파일명 (./batch/runner/ncbatch_[JOB_EXE_ID].sh)
	 */
	public String createRunnerShell(JobContext context) throws IOException {
		return processHelper.createRunnerShell(context);
	}
	
	/**
	 * runner 파일을 실행
	 * @param runnerFile
	 * @param context
	 * @return Process 객체
	 * @throws IOException
	 */
	public Process execProcess(String runnerFile, JobContext context) throws IOException {
		Process proc = processHelper.execProcess(runnerFile, context);
		put(context.getJobExecution().getJobExecutionId(), proc);
		return proc;
	}

	/**
	 * Job Exe Id로 프로세스를 찾아내서 kill 함. 
	 * 하위 프로세스 까지 kill 함.
	 * @param jobExeId
	 * @param log
	 * @return 성공시 true, 실패시 false
	 */
	public boolean killProcess(String jobExeId, Log log) {
		return processHelper.killProcess(jobExeId, log);
	}
	
}
