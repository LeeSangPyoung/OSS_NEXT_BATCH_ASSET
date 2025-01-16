package nexcore.scheduler.agent.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import nexcore.scheduler.agent.VERSION;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.IAgentService;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobExecutionSimple;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.util.ByteArray;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Agent RMI service. </li>
 * <li>작성일 : 2010. 8. 30.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class AgentServiceImpl implements IAgentService {
	private transient AgentMain agentMain;
	

	public void init() {
	}
	
	public void destroy() {
	}

	public AgentMain getAgentMain() {
		return agentMain;
	}

	public void setAgentMain(AgentMain agentMain) {
		this.agentMain = agentMain;
	}
	
	public void start(JobExecution je) {
		agentMain.start(je);
	}

	public void stop(String jobExecutionId) {
		agentMain.stop(jobExecutionId);
	}
	
	public void suspend(String jobExecutionId) {
		agentMain.suspend(jobExecutionId);
	}
	
	public void resume(String jobExecutionId) {
		agentMain.resume(jobExecutionId);
	}
	
	public JobExecution getJobExecution(String jobExecutionId) {
		return agentMain.getJobExecution(jobExecutionId);
	}

	public int getJobExeState(String jobExecutionId) {
		return agentMain.getJobExeState(jobExecutionId);
	}
	
	public List<JobExecution>  getRunningJobExecutions() {
		return agentMain.getRunningJobExecutions();
	}
	
	public Map<String, JobExecutionSimple>  getRunningJobExecutionSimpleMap() {
		return agentMain.getRunningJobExecutionSimpleMap();
	}
	
	public int getRunningJobExecutionsCount() {
		return agentMain.getRunningJobExecutionsCount();
	}
	
	public int getRunningJobThreadsCount() {
		return agentMain.getRunningJobThreadsCount();
	}
	
	public Properties getSystemProperties() {
		//return System.getProperties(); // agent JVM의 프러퍼티를 조회하여 파라미터를 설정하기 위함.
		
		// java.io.WriteAbortedException: writing aborted; java.io.NotSerializableException: java.lang.ProcessEnvironment$StringEnvironment
		// 예외를 해결하기 위해 Properties를 생성하여 반환
		Properties props = new Properties();
		props.putAll(System.getProperties());
		
		return props;
	}
	
	public Map getSystemEnv() {
		//return System.getenv(); // agent JVM의 프러퍼티를 조회하여 파라미터를 설정하기 위함.

		// java.io.WriteAbortedException: writing aborted; java.io.NotSerializableException: java.lang.ProcessEnvironment$StringEnvironment
		// 예외를 해결하기 위해 Map를 생성하여 반환
		Map<String, String> map = new HashMap<String, String>(System.getenv());
		
		return map;
	}

	public String getSystemId() {
		return Util.getSystemId();
	}

	public long getBootTime() {
		return agentMain.getBootTime();
	}
	
	/**
	 * 모든 스레드 dump
	 * @return
	 */
	public Map<String, StackTraceElement[]> getAllThreadStackTrace() {
		Map<String, StackTraceElement[]> retmap = new TreeMap<String, StackTraceElement[]>();
		Map<Thread, StackTraceElement[]> dump   = Thread.getAllStackTraces();

		for (Map.Entry<Thread, StackTraceElement[]> entry : dump.entrySet()) {
            Thread t = entry.getKey();
            StackTraceElement[] steArray = entry.getValue();

            retmap.put("name="+t.getName()+",id="+t.getId()+",priority="+t.getPriority()+",state="+t.getState(), steArray);
        }
		return retmap;
	}
	
	public int getAllThreadCount() {
		ThreadMXBean threadmx = ManagementFactory.getThreadMXBean();
		return threadmx.getThreadCount();
	}
	
	/**
	 * JobExecution 의 스레드 dump
	 * @param jobExecutionId
	 * @return
	 */
	public Map getJobExecutionThreadStackTrace(String jobExecutionId) {
		return agentMain.getJobExecutionThreadStackTrace(jobExecutionId);
	}
	
	/**
	 * JobInstanceId 로 인스턴스가 실행중인가?
	 * @param jobInstanceId
	 * @return
	 */
	public boolean isRunningByJobInstanceId(String jobInstanceId) {
		return agentMain.isRunningByJobInstanceId(jobInstanceId);
	}

	/**
	 * Job 로그 파일명 조회. 
	 * @return 절대경로 로그 파일명
	 */
	public String getLogFilename(JobLogFilenameInfo info) {
		return agentMain.getLogFilename(info);
	}
	
	public boolean isFileExist(String filename) {
		if (filename==null) {
			return false;
		}else {
			return new File(filename).exists();
		}
	}
	
	/**
	 * Job 로그 파일 길이 조회. 부분범위로 파일 읽이 위해 전체 길이 필요함.
	 * @return 로그 파일 길이 (byte 수)
	 */
	public long getLogFileLength(String filename) {
		if (filename==null) {
			return -1;
		}else {
			return new File(filename).length();
		}
	}
	
	public String getJobLogFileEncoding() {
        return agentMain.getJobLogFileEncoding();
	}
	

	
	/**
	 * 파일 읽기. 
	 * @param logFilename
	 * @param offset
	 * @param length
	 * @return 파일 내용 ByteArray. null if reached to eof.
	 */
	public ByteArray readLogFile(String logFilename, int offset, int length) {
		return agentMain.readLogFile(logFilename, offset, length);
	}
	
	/**
	 * 메인 Job 로그 파일이 아닌, 각 배치 APP 에서 기록한 로그 파일명
	 * @return sub 로그 파일 절대경로
	 */
	public String getSubLogFilename(JobLogFilenameInfo info) {
		return agentMain.getSubLogFilename(info);
	}
	
	/**
	 * JVM 메모리 정보와 Properties 를 활용한 os, dir, version 등의 정보를 리턴한다.
	 */
	public Map getJVMMonitoringInfo() {
		Map map = new HashMap();
        String javaVersion = System.getProperty("java.specification.version");
        if (javaVersion.compareTo("1.5") >= 0) {
    		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
//    		System.out.println("heap      > "+mem.getHeapMemoryUsage());
//    		System.out.println("non heap  > "+mem.getNonHeapMemoryUsage());

    		MemoryUsage heapMem  = mem.getHeapMemoryUsage();
    		MemoryUsage nonHeapMem = mem.getNonHeapMemoryUsage();
    		map.put("HEAP_INIT",          heapMem.getInit());
    		map.put("HEAP_USED",          heapMem.getUsed());
    		map.put("HEAP_COMMITTED",     heapMem.getCommitted());
    		map.put("HEAP_MAX",           heapMem.getMax());
    		map.put("NONHEAP_INIT",       nonHeapMem.getInit());
    		map.put("NONHEAP_USED",       nonHeapMem.getUsed());
    		map.put("NONHEAP_COMMITTED",  nonHeapMem.getCommitted());
    		map.put("NONHEAP_MAX",        nonHeapMem.getMax());
	            
        }else {
        	Runtime rt = Runtime.getRuntime();
        	long total = rt.totalMemory();
        	long free  = rt.freeMemory();
        	long max   = rt.maxMemory();
    		map.put("HEAP_INIT",          -1);
    		map.put("HEAP_USED",          total-free);
    		map.put("HEAP_COMMITTED",     total);
    		map.put("HEAP_MAX",           max);
	    }
        
        map.put("NC_BATAGENT_VERSION", VERSION.getImplementationVersion());
        map.put("user.dir",            System.getProperty("user.dir"));
        map.put("user.name",           System.getProperty("user.name"));
        map.put("os.name",             System.getProperty("os.name"));
        map.put("java.version",        System.getProperty("java.version"));
        map.put("current_time_ms",     System.currentTimeMillis());
        
        return map;
	}
	
	public String getJobExecutionLogLevel(String jobExecutionId) {
		return agentMain.getJobExecutionLogLevel(jobExecutionId);
	}

	public boolean setJobExecutionLogLevel(String jobExecutionId, String logLevel) {
		return agentMain.setJobExecutionLogLevel(jobExecutionId, logLevel);
	}

	public String isAlive() {
		return !agentMain.isDestroyed() ? "OK" : "FAIL:Destroyed";
	}

	/**
	 * Job 실행안되도록 close
	 */
	public void closeAgent(AdminAuth auth) {
		agentMain.closeAgent(auth);
	}

	/**
	 * Job 실행되도록 open
	 */
	public void openAgent(AdminAuth auth) {
		agentMain.openAgent(auth);
	}
	
	public boolean isClosed() {
		return agentMain.isClosed();
	}
	
	public void shutdown(AdminAuth auth) {
		agentMain.shutdown(auth);
	}
	
	/**
	 * beans/, properties/ 디렉토리 하위의 파일들의 파일명 리스트를 리턴한다. 
	 * @return
	 */
	public List<String> getConfigFilenames() {
		List filenames = new ArrayList();

		try {
			URL xmlDir = this.getClass().getClassLoader().getResource("beans");
			if (xmlDir == null) {
			    // 설정파일이 beans 에 없는 경우는 그냥 empty 리턴함
			    return Collections.EMPTY_LIST;
			}
			File xmlDirFile = new File(xmlDir.toURI().getPath());
			File[] files1 = xmlDirFile.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.indexOf("nexcore") > -1;
				}
			});
			if (files1 != null) {
				for (File f : files1) {
					filenames.add(f.getCanonicalPath());
				}
			}
			
			URL propDir = this.getClass().getClassLoader().getResource("properties");
			if (propDir == null) {
			    // 설정파일이 properties 에 없는 경우는 그냥 empty 리턴함
			    return Collections.EMPTY_LIST;
			}
			File propDirFile = new File(propDir.toURI().getPath());
			File[] files2 = propDirFile.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.indexOf("nexcore") > -1;
				}
			});
			if (files2 != null) {
				for (File f : files2) {
					filenames.add(f.getCanonicalPath());
				}
			}
		}catch(Exception e) {
			throw Util.toRuntimeException(e);
		}
		return filenames;
	}
}