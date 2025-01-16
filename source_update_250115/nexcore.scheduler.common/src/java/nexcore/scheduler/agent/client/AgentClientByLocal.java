package nexcore.scheduler.agent.client;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.IAgentService;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobExecutionSimple;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.ByteArray;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Local call 방식으로 internal agent 로 접속하는 client</li>
 * <li>작성일 : 2012. 4. 5.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class AgentClientByLocal implements IAgentClient {
	private String            agentId;
	private IAgentService     agentService;
	
	public AgentClientByLocal() {
	}
	
	public void init() {
	}

	public void destroy() {
	}

	public IAgentService getAgentService() {
		return agentService;
	}

	public void setAgentService(IAgentService agentService) {
		this.agentService = agentService;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	private RuntimeException checkNetworkAndMakeException(Exception causeException) {
		if (!"OK".equals(isAlive())) {
			return new SchedulerException("main.agent.agentclient.connect.error", causeException, agentId);
		}else {
			return Util.toRuntimeException(causeException);
		}
	}

	public void start(JobExecution je) {
		try {
			agentService.start(je);
		}catch (Exception e) {
			throw checkNetworkAndMakeException(e);
		}
	}

	public void stop(String jobExecutionId) {
	    try {
		    agentService.stop(jobExecutionId);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public void suspend(String jobExecutionId) {
	    try {
		    agentService.suspend(jobExecutionId);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public void resume(String jobExecutionId) {
	    try {
		    agentService.resume(jobExecutionId);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public int getJobExeState(String jobExecutionId) {
	    try {
	    	return agentService.getJobExeState(jobExecutionId);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public JobExecution getJobExecution(String jobExecutionId) {
	    try {
	    	return agentService.getJobExecution(jobExecutionId);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public Properties getSystemProperties() {
	    try {
	    	return agentService.getSystemProperties();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public Map getSystemEnv() {
	    try {
	    	return agentService.getSystemEnv();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public String getSystemId() {
	    try {
	    	return agentService.getSystemId();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public long getBootTime() {
	    try {
	    	return agentService.getBootTime();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	/**
	 * JobInstanceId 로 인스턴스가 실행중인가?
	 * @param jobInstanceId
	 * @return
	 */
	public boolean isRunningByJobInstanceId(String jobInstanceId) {
	    try {
	    	return agentService.isRunningByJobInstanceId(jobInstanceId);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public String getLogFilename(JobLogFilenameInfo info) {
	    try {
	    	return agentService.getLogFilename(info);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public boolean isFileExist(String filename) {
	    try {
	    	return agentService.isFileExist(filename);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public long getLogFileLength(String filename) {
	    try {
	    	return agentService.getLogFileLength(filename);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public String getJobLogFileEncoding() {
	    try {
	        return agentService.getJobLogFileEncoding();
	    }catch (Exception e) {
	        throw checkNetworkAndMakeException(e);
	    }
	}
	
	public String getSubLogFilename(JobLogFilenameInfo info) {
	    try {
	    	return agentService.getSubLogFilename(info);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public ByteArray readLogFile(String logFilename, int offset, int length) {
	    try {
	    	return agentService.readLogFile(logFilename, offset, length);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public String isAlive() {
	    try {
	    	return agentService.isAlive();
    	}catch (Exception e) {
    		return "FAIL:"+e.toString();
    	}
	}
	
	public List<JobExecution> getRunningJobExecutions() {
	    try {
	    	return agentService.getRunningJobExecutions();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public Map<String, JobExecutionSimple>  getRunningJobExecutionSimpleMap() {
	    try {
	    	return agentService.getRunningJobExecutionSimpleMap();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public int getRunningJobExecutionsCount() {
	    try {
	    	return agentService.getRunningJobExecutionsCount();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public Map getJobExecutionThreadStackTrace(String jobExecutionId) {
	    try {
	    	return agentService.getJobExecutionThreadStackTrace(jobExecutionId);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public Map<String, StackTraceElement[]> getAllThreadStackTrace() {
	    try {
	    	return agentService.getAllThreadStackTrace();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public int getAllThreadCount() {
	    try {
	    	return agentService.getAllThreadCount();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public Map getJVMMonitoringInfo() {
	    try {
	    	return agentService.getJVMMonitoringInfo();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public String getJobExecutionLogLevel(String jobExecutionId) {
	    try {
	    	return agentService.getJobExecutionLogLevel(jobExecutionId);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public boolean setJobExecutionLogLevel(String jobExecutionId, String logLevel) {
	    try {
	    	return agentService.setJobExecutionLogLevel(jobExecutionId, logLevel);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	
	public boolean isClosed() {
	    try {
	    	return agentService.isClosed();
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public void closeAgent(AdminAuth auth) {
	    try {
	    	agentService.closeAgent(auth);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	public void openAgent(AdminAuth auth) {
	    try {
	    	agentService.openAgent(auth);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}

	public void shutdown(AdminAuth auth) {
	    try {
    		agentService.shutdown(auth);
    	}catch (Exception e) {
    		throw checkNetworkAndMakeException(e);
    	}
	}
	
	/**
	 * beans/, properties/ 디렉토리 하위의 파일들의 파일명 리스트를 리턴한다. 
	 * @return
	 */
	public List<String> getConfigFilenames() {
		try {
			return agentService.getConfigFilenames();
		}catch (Exception e) {
			throw checkNetworkAndMakeException(e);
		}
	}
}
