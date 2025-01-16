package nexcore.scheduler.monitor.internal;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import nexcore.scheduler.controller.internal.JobExecutionManager;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 진행 상태를 관리하는 모듈.  </li>
 * <li>작성일 : 2011. 12. 15.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// 1. 스레드로 동작하면서 2초마다 각 에이전트의 진행 상태 값을 조회해온다.
// 2. 완료된 Job 의 최종 JOB EXE 의 진행 건수를 DB 에서 읽어와 메모리에 캐쉬한다.
public class JobProgressStatusManager {
	private boolean                   enable;
	private JobExecutionManager       jobExecutionManager;
	private int                       cacheSize = 10000;       // 메모리에 담아둘 캐쉬 size. 동시 수행 Job 개수가 10000개 이상일 경우는 이값을 늘린다.

	private Map<String, long[]>       jobExeProgressMap;       // <JobExeId, progress>. 에이전트로 부터 조회한 진행 값을 메모리에 담아둠.
	private Log                       log;
	
	public JobProgressStatusManager() {
	}
	
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public JobExecutionManager getJobExecutionManager() {
		return jobExecutionManager;
	}

	public void setJobExecutionManager(JobExecutionManager jobExecutionManager) {
		this.jobExecutionManager = jobExecutionManager;
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}
	
	public void init() {
		jobExeProgressMap = new LinkedHashMap<String, long[]>() {
			private static final long serialVersionUID = 1L; // COMPILE WARN 안보이게 하기 위해
			protected boolean removeEldestEntry(java.util.Map.Entry<String, long[]> eldest) {
				return size() > cacheSize;
			}
		};

		log = LogManager.getSchedulerLog();

		Util.logServerInitConsole("JobProgressStatusManager");
	}
	
	public void destroy() {
		enable = false;
	}

	// ============================================================================================
	// ============================================================================================

	/**
	 * 진행률 조회
	 * @param jobexeId
	 * @return
	 */
	public long[] getJobExeProgress(String jobexeId) {
		if (Util.isBlank(jobexeId)) return null;
		
		// 먼저 메모리에서 검색해본다.
		long[] prgs = jobExeProgressMap.get(jobexeId);
		
		if (prgs == null) {
			try {
				// 메모리에 없으면 DB 조회한다.
				JobExecution jobexe = jobExecutionManager.getJobExecution(jobexeId);
				if (jobexe != null && jobexe.getState() != 0) { // 뭔가 에이전트로 부터 callback 이 온 상태
					prgs = new long[]{jobexe.getProgressTotal(), jobexe.getProgressCurrent()};
				}
				
				if (prgs != null) {
					synchronized(jobExeProgressMap) {
						jobExeProgressMap.put(jobexeId, prgs);
					}
				}
			}catch(SQLException e) {
				Util.logError(log, "jobExecutionManager.getJobExecutionProgress("+jobexeId+") fail", e); // 달리 방법이 없다 에러로그 찍고, null 리턴하고 끝냄.
			}
		}
		
		return prgs;
	}
	
	/**
	 * case 1. Job 이 종료되는 경우 callback 되어 온 JobExecution 값으로 최종 update 한다. 
	 *         이렇게 하지 않으면 gather thread 와 Job End 간 시간 차이로 인해, End 상태이지만 
	 *         progress 는 아직 완료전 상태로 보여지게 되는 경우가 있다.<br>
	 *         
	 * case 2. AgentJobMonitor 에서 pooling 결과값으로 이 메소드 호출하여 여기에도 update 한다. 
	 * 
	 * @param jobexeId
	 */
	public void updateRunningJobExecution(String jobExeId, long prgsTotal, long prgsCurrent) {
		long[] prgs = jobExeProgressMap.get(jobExeId);
		if (prgs != null) {
			prgs[0] = prgsTotal;
			prgs[1] = prgsCurrent;
		}else {
			synchronized(jobExeProgressMap) {
				jobExeProgressMap.put(jobExeId, new long[]{prgsTotal, prgsCurrent});
			}
		}
	}
	
	/**
	 * case 1. Job 이 종료되는 경우 callback 되어 온 JobExecution 값으로 최종 update 한다. 
	 *         이렇게 하지 않으면 gather thread 와 Job End 간 시간 차이로 인해, End 상태이지만 
	 *         progress 는 아직 완료전 상태로 보여지게 되는 경우가 있다.<br>
	 *         
	 * case 2. AgentJobMonitor 에서 pooling 결과값으로 이 메소드 호출하여 여기에도 update 한다. 
	 * 
	 * @param jobexeId
	 */
	public void updateRunningJobExecution(JobExecution jobexe) {
		updateRunningJobExecution(jobexe.getJobExecutionId(), jobexe.getProgressTotal(), jobexe.getProgressCurrent());
	}
	
}
