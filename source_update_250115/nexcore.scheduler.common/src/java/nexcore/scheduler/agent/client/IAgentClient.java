package nexcore.scheduler.agent.client;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobExecutionSimple;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.util.ByteArray;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Agent 와 통신하는 클라이언트. Controller 에서 사용함 </li>
 * <li>작성일 : 2010. 4. 26.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IAgentClient {

	/* ----------------------- Job control method BEGIN -----------------------------*/
	public void start(JobExecution je) ;
	
	public void stop(String jobExecutionId) ;
	
	public void suspend(String jobExecutionId) ;
	
	public void resume(String jobExecutionId) ;
	
	public int getJobExeState(String jobExecutionId) ;
	
	public JobExecution getJobExecution(String jobExecutionId) ;
	
	/**
	 * JobInstanceId 로 인스턴스가 실행중인가?
	 * @param jobInstanceId
	 * @return
	 */
	public boolean isRunningByJobInstanceId(String jobInstanceId);

	public String getLogFilename(JobLogFilenameInfo info);
	
	public boolean isFileExist(String filename);
	
	public long getLogFileLength(String filename);
	
	public String getJobLogFileEncoding(); 
	
	public String getSubLogFilename(JobLogFilenameInfo info);
	
	public ByteArray readLogFile(String logFilename, int offset, int length);

	/* ----------------------- Job control method END   -----------------------------*/

	public String isAlive();
	
	public long getBootTime();

	public Properties getSystemProperties();
	
	public Map getSystemEnv();

	public String getSystemId();
	
	public List<JobExecution> getRunningJobExecutions();

	public Map<String, JobExecutionSimple>  getRunningJobExecutionSimpleMap();
	
	public int getRunningJobExecutionsCount();
	
	public Map getJobExecutionThreadStackTrace(String jobExecutionId);

	public Map<String, StackTraceElement[]> getAllThreadStackTrace();

	public int getAllThreadCount();
	
	public Map getJVMMonitoringInfo();
	
	public String getJobExecutionLogLevel(String jobExecutionId);

	public boolean setJobExecutionLogLevel(String jobExecutionId, String logLevel);
	
	/* ------ Agent control & monitoring ------ */
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