package nexcore.scheduler.core.internal;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import nexcore.scheduler.entity.JobInstance;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Instance ID 로 부터 Job Id를 쉽게 가져올 수 있도록 메모리 매핑함.</li>
 * <li>작성일 : 2012. 3. 2.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// 나중에 JobInstance 정보를 메모리에 올리는 작업을 마치면 이 기능은 필요없어진다. 그때까지만 사용.
public class JobInstanceIdMap {
	private JobInstanceManager   jobInstanceManager;
	private int                  cacheSize = 1000; // 기본값 1000
	
	private Map<String, String>  jobIdMap;  // <String:jobId+YYYYMMDD, AtomicInteger:lastSeq>

	public void init() {
		jobIdMap = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L; // COMPILE WARN 안보이게 하기 위해
			protected boolean removeEldestEntry(java.util.Map.Entry<String, String> eldest) {
				return size() > cacheSize;
			}
		};
	}
	
	public void destroy() {
	}
	
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
	
	public int getCacheSize() {
		return cacheSize;
	}

	public JobInstanceManager getJobInstanceManager() {
		return jobInstanceManager;
	}
	
	public void setJobInstanceManager(JobInstanceManager jobInstanceManager) {
		this.jobInstanceManager = jobInstanceManager;
	}
	
	public String getJobId(String jobInstanceId) throws SQLException {
		String jobid = jobIdMap.get(jobInstanceId);

		if (jobid == null) {
			synchronized(jobIdMap) {
				jobid = jobIdMap.get(jobInstanceId);
				if (jobid == null) {
					JobInstance jobins = jobInstanceManager.getJobInstance(jobInstanceId);
					if (jobins != null) {
						jobid = jobins.getJobId();
						jobIdMap.put(jobInstanceId, jobid);
					}
				}
			}
		}
		return jobid;
	}

}
