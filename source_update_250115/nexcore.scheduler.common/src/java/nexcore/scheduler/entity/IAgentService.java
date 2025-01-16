package nexcore.scheduler.entity;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.util.ByteArray;


/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Agent 서비스. RMI interface </li>
 * <li>작성일 : 2010. 8. 30.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IAgentService {
	/* ---- Job control --------*/
	public void start(JobExecution je);
	
	public void stop(String jobExecutionId) ;
	public void suspend(String jobExecutionId) ;
	public void resume(String jobExecutionId) ;
	
	/* ----- Job Monitoring ------*/
	public JobExecution   getJobExecution(String jobExecutionId) ;
	public int            getJobExeState(String jobExecutionId) ;
	public List<JobExecution>              getRunningJobExecutions();
	public Map<String, JobExecutionSimple> getRunningJobExecutionSimpleMap();
	public int            getRunningJobExecutionsCount();
	public Map            getJobExecutionThreadStackTrace(String jobExecutionId); //{"THREAD":스레드toString(), "STACKTRACE":t.getStackTrace()}
	public String         getJobExecutionLogLevel(String jobExecutionId);
	public boolean        setJobExecutionLogLevel(String jobExecutionId, String logLevel);
	
	/**
	 * JobInstanceId 로 인스턴스가 실행중인가?
	 * @param jobInstanceId
	 * @return
	 */
	public boolean isRunningByJobInstanceId(String jobInstanceId);
	
	public String         getLogFilename(JobLogFilenameInfo info);
	public boolean        isFileExist(String filename);
	public long           getLogFileLength(String filename);
	public String         getJobLogFileEncoding();	
	
	/**
	 * 파일 읽기. 
	 * @param logFilename
	 * @param offset
	 * @param length
	 * @return 파일 내용 ByteArray. null if reached to eof.
	 */
	public ByteArray      readLogFile(String logFilename, int offset, int length);
	public String         getSubLogFilename(JobLogFilenameInfo info);   // Job 로그 이외에, CBatch 의 로그 파일. 

	
	/* ------ Agent control & monitoring ------ */
	public Map<String, StackTraceElement[]> getAllThreadStackTrace();
	public int          getAllThreadCount();
	public String       isAlive();
	public Properties   getSystemProperties();
	public Map          getSystemEnv();
	public String       getSystemId();
	public Map          getJVMMonitoringInfo();
	public long         getBootTime();
	public boolean      isClosed();
	public void         closeAgent(AdminAuth auth);
	public void         openAgent(AdminAuth auth);
	public void         shutdown(AdminAuth auth);

	/**
	 * beans/, properties/ 디렉토리 하위의 파일들의 파일명 리스트를 리턴한다. 
	 * @return
	 */
	public List<String> getConfigFilenames();
	
}
