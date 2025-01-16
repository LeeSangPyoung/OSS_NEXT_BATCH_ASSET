package nexcore.scheduler.agent.internal;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.IJobClassLoadManager;
import nexcore.scheduler.agent.IJobRunner;
import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.joblog.ILoggerFactory;
import nexcore.scheduler.agent.joblog.JobLogManager;
import nexcore.scheduler.agent.joblog.LogLevel;
import nexcore.scheduler.agent.startup.ShutdownHelper;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobExecutionSimple;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.ByteArray;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치코어</li>
 * <li>설  명 : 배치 Agent</li>
 * <li>작성일 : 2010.03.30</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class AgentMain {
	private long                            bootTime;
	private boolean                         enabled; // 온라인 전용 WAS의 경우 Agent=false 로 하여 각종 데몬들이 기동되지 않게 한다.
	private IJobRunnerCallBack              jobRunnerCallBack;
	private JobExecutionBoard               jobExecutionBoard;
	private JobLogManager                   jobLogManager;
	private Map<String, IJobRunner>         jobRunnerMap;
	
	private JobRunThreadManager             jobRunThreadManager;
	private boolean                         destroyed = false;
	private boolean                         closed    = false;
	
	private Log                             log;

	private String                          procId;   // 에이전트 JVM 의 PID
	private String                          hostname; // 에이전트 호스트명
	private String                          osUser;   // 에이전트 os 유저명
	
	public AgentMain() {
		bootTime = System.currentTimeMillis();
	}
	
	public void init() {
		log = LogManager.getAgentLog();

		if (enabled) {
		    procId   = Util.getJvmPID();
		    hostname = Util.getHostname();
		    osUser   = System.getProperty("user.name");
		    
		    jobRunThreadManager = new JobRunThreadManager();

		    Util.logServerInitConsole("AgentMain");
		}else {
			Util.logInfo(log, "Batch Agent disabled");
			destroy();
		}
	}
	
	public void destroy() {
		destroyed = true;
	}
	
	public long getBootTime() {
		return bootTime;
	}
	
	public JobRunThreadManager getJobRunThreadManager() {
		return jobRunThreadManager;
	}
	
	public JobExecutionBoard getJobExecutionBoard() {
		return jobExecutionBoard;
	}

    public IJobRunnerCallBack getJobRunnerCallBack() {
		return jobRunnerCallBack;
	}

	public void setJobRunnerCallBack(IJobRunnerCallBack jobRunnerCallBack) {
		this.jobRunnerCallBack = jobRunnerCallBack;
	}

	public void setJobExecutionBoard(JobExecutionBoard jobExecutionBoard) {
        this.jobExecutionBoard = jobExecutionBoard;
    }

    public JobLogManager getJobLogManager() {
		return jobLogManager;
	}

	public void setJobLogManager(JobLogManager jobLogManager) {
		this.jobLogManager = jobLogManager;
	}
	
	// =====================================================

	public boolean isDestroyed() {
		return destroyed;
	}

	public boolean isClosed() {
		return closed;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Map<String, IJobRunner> getJobRunnerMap() {
		return jobRunnerMap;
	}

	public void setJobRunnerMap(Map<String, IJobRunner> jobRunnerMap) {
		this.jobRunnerMap = jobRunnerMap;
		for (IJobRunner jobRunner : jobRunnerMap.values()) {
			jobRunner.setAgentMain(this);
		}
	}

	// ##########################################################################33
	
    private IJobRunner getJobRunner(String jobType) {
		IJobRunner jr = jobRunnerMap.get(jobType);
		if (jr == null) {
			throw new AgentException("agent.jobtype.error", jobType);
		}else {
			return jr;
		}
	}
	
	public void start(JobExecution je) {
		if (closed) {
			throw new AgentException("agent.closed");
		}
		
		// 에이전트의 호스트명, OS유저명, 프로세스 ID 를 세팅한다.
        je.setProcId(procId);
        je.setHostname(hostname);
        je.setOsUser(osUser);
		
		if (jobExecutionBoard.getJobExecutionByJobInsId(je.getJobInstanceId()) != null) {
			// 동일 JobInstance 로 이미 돌고 있다면 에러낸다.
			String msg = MSG.get("agent.fail.run.job.same.instance.is.running", je.getJobExecutionId());
			Util.logError(log, msg);
			throw new AgentException("agent.fail.run.job.same.instance.is.running", je.getJobExecutionId());
		}

		je.setStartTime(System.currentTimeMillis());
		je.setState(JobExecution.STATE_RUNNING);
		
		JobContext context = new JobContext();
		context.setJobExecution(je);
		context.setJobRunnerCallBack(jobRunnerCallBack);

		jobExecutionBoard.add(context);
		try {
			Util.logInfo(log, MSG.get("agent.do.start.job", je.getJobExecutionId(), je.getDescription()));
			getJobRunner(je.getJobType()).start(je, context, jobRunnerCallBack);
		}catch(Throwable e) {
			jobExecutionBoard.remove(je.getJobExecutionId());
			Util.logError(log, MSG.get("agent.fail.start.job", je.getJobExecutionId()), e);
			throw new AgentException("agent.fail.start.job", e, je.getJobExecutionId());
		}
	}

	public void stop(String jobExecutionId) {
		JobContext context = jobExecutionBoard.getJobContext(jobExecutionId);
		if (context == null) {
			throw new AgentException("agent.jobexecution.notfound", jobExecutionId);
		}
		
		context.setStopForced(true);
		
		Util.logInfo(log, MSG.get("agent.do.stop.job", jobExecutionId));
		try {
			context.getLogger().info(MSG.get("agent.do.stop.job", jobExecutionId));
		}catch(Exception e) {
			// Job 로그 생성 실패한 경우 여기서 exception 이 발생하면서 stop 도 제대로 안되는 경우가 생긴다. 
			Util.logError(log, "Job stop log fail", e);
		}

		getJobRunner(context.getJobExecution().getJobType()).stop(jobExecutionId);
	}
	
	public void suspend(String jobExecutionId) {
	    JobContext context = jobExecutionBoard.getJobContext(jobExecutionId);
	    if (context == null) {
	        throw new AgentException("agent.jobexecution.notfound", jobExecutionId);
	    }
	    context.setSuspendForced(true);
	    
		Util.logInfo(log, MSG.get("agent.do.suspend.job", jobExecutionId));
		context.getLogger().info(MSG.get("agent.do.suspend.job", jobExecutionId));
		getJobRunner(context.getJobExecution().getJobType()).suspend(jobExecutionId);
	}
	
	public void resume(String jobExecutionId) {
	    JobContext context = jobExecutionBoard.getJobContext(jobExecutionId);
	    if (context == null) {
	        throw new AgentException("agent.jobexecution.notfound", jobExecutionId);
	    }
	    context.setSuspendForced(false);

	    Util.logInfo(log, MSG.get("agent.do.resume.job", jobExecutionId));
		context.getLogger().info(MSG.get("agent.do.resume.job", jobExecutionId));
		getJobRunner(context.getJobExecution().getJobType()).resume(jobExecutionId);
	}

	public int getJobExeState(String jobExecutionId) {
		JobExecution je = jobExecutionBoard.getJobExecution(jobExecutionId);
		if (je==null) {
			// End 되어서 jobExecutionBoard 에서는 이미 사라진 상태. je 파일도 한번 검색해본다.
			// ghost 판단 처리시에 호출됨. callback 이 늦어져서 ghost 로 판단되자마자 다시 end 로 변경되는 현상을 해결하기 위함.
			je = jobRunnerCallBack.getJobExecutionFromFile(jobExecutionId);
			if (je != null) {
				Util.logInfo(log, "getJobExeState("+jobExecutionId+"): get from file. status="+je.getState());
			}
		}
		return je==null? JobExecution.STATE_UNKNOWN : je.getState();
	}

	public List<JobExecution>  getRunningJobExecutions() {
		return jobExecutionBoard.getJobExecutionsList();
	}
	
	/**
	 * 컨트롤러에서 주기적으로 상태, 진행률 모니터링시 통신 부하를 줄이기 위해 JobExecution 으로 통신하지 않고
	 * JobExecutionSimple 로 통신한다.
	 * @return 
	 */
	public Map<String, JobExecutionSimple> getRunningJobExecutionSimpleMap() {
		List<JobExecution>              jobexeList = jobExecutionBoard.getJobExecutionsList();
		Map<String, JobExecutionSimple> returnVal  = new HashMap(jobexeList.size());
		
		for (JobExecution jobexe : jobexeList) {
			JobExecutionSimple jes = new JobExecutionSimple();
			jes.setJobExecutionId(  jobexe.getJobExecutionId());
			jes.setState(           jobexe.getState());
			jes.setProgressTotal(   jobexe.getProgressTotal());
			jes.setProgressCurrent( jobexe.getProgressCurrent());
			
			returnVal.put(jes.getJobExecutionId(), jes);
		}
		
		return returnVal;
	}

	public int getRunningJobExecutionsCount() {
		return jobExecutionBoard.size();
	}
	
	public int getRunningJobThreadsCount() {
		return jobRunThreadManager.size();
	}
	
	public JobExecution getJobExecution(String jobExecutionId) {
		return jobExecutionBoard.getJobExecution(jobExecutionId);
	}
	
	/**
	 * @param jobExecutionId
	 * @return {"THREAD":스레드toString(), "STACKTRACE":t.getStackTrace()}
	 */
	public Map getJobExecutionThreadStackTrace(String jobExecutionId) {
		Map retmap = new HashMap();
		Thread t = jobRunThreadManager.getThread(jobExecutionId);
		if (t == null) {
			return null;
		}
        retmap.put("THREAD",     "name="+t.getName()+",id="+t.getId()+",priority="+t.getPriority()+",state="+t.getState());
        retmap.put("STACKTRACE", t.getStackTrace());
		return retmap;
	}
	
	/**
	 * JobInstanceId 기준으로 이미 JobInstance 가 돌고 있는지? 
	 * @param jobInstanceId
	 * @return
	 */
	public boolean isRunningByJobInstanceId(String jobInstanceId) {
		return jobExecutionBoard.getJobExecutionByJobInsId(jobInstanceId) != null;
	}
	
	// ----------- admin method --------------
	/**
	 * remote 에서 로그 파일 찾기용 메소드
	 *  
	 * @param info
	 * @return String
	 */
	public String getLogFilename(JobLogFilenameInfo info) {
	    return jobLogManager.getFactory().getLogFile(info).getAbsolutePath();
	}

	/**
	 * 위와 동일하지만 JMX 를 위해 map 으로 필요한 값 받음. 
	 *  
	 * @param info
	 * @return String
	 */
	public String getLogFilename(String procDate, String jobId, String jobInstanceId, String jobExecutionId) {
	    JobLogFilenameInfo jobLogFilenameInfo = new JobLogFilenameInfo();
	    jobLogFilenameInfo.setProcDate(procDate);
	    jobLogFilenameInfo.setJobId(jobId);
	    jobLogFilenameInfo.setJobInstanceId(jobInstanceId);
	    jobLogFilenameInfo.setJobExecutionId(jobExecutionId);
	    return getLogFilename(jobLogFilenameInfo);
	}

	public String getJobLogFileEncoding() {
        ILoggerFactory logfactory = jobLogManager.getFactory();
        return logfactory.getEncoding();
	}

	private RandomAccessFile openFile(String filename) throws IOException {
		return new RandomAccessFile(filename, "r");
	}
	
	private void closeFile(RandomAccessFile file) {
		try {
			file.close();
		}catch(Exception ignore) {
		}
	}
	
	/**
	 * Agent에 저장된 로그 파일 내용을 읽음. 메모리 효율을 위해 부분부분 끊어 읽는다.
	 * @param logFilename
	 * @param offset
	 * @param length
	 * @return 파일 내용 ByteArray. null if reached to eof.
	 */
	private ByteArray _readLogFile(String logFilename, int offset, int length) {
		RandomAccessFile file = null;
		try {
			file = openFile(logFilename);
			int skipedLen = file.skipBytes(offset);
			if (skipedLen != offset) {
				throw new IOException("skip fail. "+skipedLen);
			}
			byte[] buffer = new byte[length];
			int readLen = file.read(buffer, 0, length);
			if (readLen == -1) {
				return null;
			}else {
				return new ByteArray(buffer, 0, readLen);
			}
		} catch (EOFException e) {
			return null; // 다 읽었을 경우는 null 리턴함.
		} catch (IOException e) {
			throw new AgentException("agent.fail.read.logfile", e, logFilename);
		} finally {
			closeFile(file);
		}
	}
	
    /**
     * Agent에 저장된 로그 파일 내용을 읽음.
     * 이 메소드를 이용하는 경우는 설정된 로그 디렉토리 하위의 파일인지 체크한다. 
     * 로그 파일이 아닌 다른 파일을 읽으려는 해킹을 차단하기 위한 조치 
     *  
     * @param logFilename
     * @param offset
     * @param length
     * @return 파일 내용 ByteArray. null if reached to eof.
     */
    public ByteArray readLogFile(String logFilename, int offset, int length) {
        try {
            String logFile = new File(logFilename).getCanonicalPath();
            if (!logFile.startsWith(jobLogManager.getBaseDir()) && 
                !logFile.contains(new File(Util.getHomeDirectory()).getCanonicalPath())) {
                // 로그디렉토리가 아닌 다른 디렉토리 조회 시도시에는 에러 발생
                // 설정파일 조회를 위해 에이전트 홈디렉토리 하위는 허용 
                throw new SecurityException("Can not read "+logFile);
            }
            return _readLogFile(logFilename, offset, length);
        } catch (IOException e) {
            throw new AgentException("agent.fail.read.logfile", e, logFilename);
        }
    }

	public Object getBatchJobMonitoringInfo(String jobExecutionId) {
		return jobExecutionBoard.getJobExecution(jobExecutionId);
	}
	
	public String getJobExecutionLogLevel(String jobExecutionId) {
		JobContext context = jobExecutionBoard.getJobContext(jobExecutionId);
		return context==null || context.getLogger()==null ? null : context.getLogger().getLogLevel().getThisLevelString();
	}

	public boolean setJobExecutionLogLevel(String jobExecutionId, String logLevel) {
	    JobContext context = jobExecutionBoard.getJobContext(jobExecutionId);
		
		if (context != null) {
			if (!Util.isBlank(logLevel)) {
			    context.getLogger().setLogLevel(new LogLevel(logLevel));
			}
			context.getJobExecution().setLogLevel(context.getLogger().getLogLevel().getThisLevelString());
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * sub 로그 파일명 조회
	 * @param info
	 * @return
	 */
	public String getSubLogFilename(JobLogFilenameInfo info) {
		File subLogFile = getJobRunner(info.getJobType()).getSubLogFile(info);
		return subLogFile == null ? null : subLogFile.getAbsolutePath();
	}

	/**
	 * sub 로그 파일 길이 조회
	 * @param info
	 * @return
	 */
	public long getSubLogFileLength(JobLogFilenameInfo info) {
		File subLogFile = getJobRunner(info.getJobType()).getSubLogFile(info);
		return subLogFile == null ? -1 : subLogFile.length();
	}

	/**
	 * Agent 가 Job 실행 (launch)을 못하도록 close함. 이미 실행중인놈들은 계속 실행 
	 * @param auth
	 */
	public void closeAgent(AdminAuth auth) {
		Util.logInfo(log, "!!! Agent closed from "+auth);
		closed = true;
	}
	
	/**
	 * close 된 agent를 다시 open함. 이미 실행중인놈들은 계속 실행 
	 * @param auth
	 */
	public void openAgent(AdminAuth auth) {
		Util.logInfo(log, "!!! Agent opened from "+auth);
		closed = false;
	}

	/**
	 * standalone 모드의 agent를 강제로 shutdown 함.
	 * @param auth
	 */
	public void shutdown(AdminAuth auth) {
		Util.logInfo(log, "!!! Agent shutdown process started by "+auth);
		closeAgent(auth);
		
		Util.logInfo(log, "Trying to shutdown agent components.");
		try {
			ShutdownHelper.destroy();
		}catch(Throwable e) {
			e.printStackTrace();
		}
		
		new Thread(new Runnable() {
			public void run() {
				Util.sleep(2000);
				System.exit(0);
			}
		}).start();
	}
}
