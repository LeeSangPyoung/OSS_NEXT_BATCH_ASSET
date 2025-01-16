package nexcore.scheduler.monitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 그룹별 실행 통계 정보를 담는 VO  </li>
 * <li>작성일 : 2013. 2. 20.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobGroupRunStats {
	private String           jobGroupId;
	private int              jobInsCount;
	private String           minTimeFrom;
	private String           maxTimeUntil;
	private int              runTotalCount;
	private int              endOkCount;
	private int              endFailCount;
	private String           state;
	private int              stateTemp;
	private long             progressTotal;
	private long             progressCurrent;
	private String           lastStartTime;  // 동일 그룹내 최종 실행 인스턴스의 start time
	private String           lastEndTime;    // 동일 그룹내 최종 실행 인스턴스의 end time
	
	private Set<String>      jobDefIdSet = new HashSet();  // <JobId>
	
	/* 
	 * Gubun 구분 값으로 동일 그룹 내에서 Gubun 별 통계도 계산함. 즉 Gubun 별 실행 건수의 합계는 위 필드 값과 동일하게 된다.
	 * Gubun 의 예는 현대스위스,솔로몬저축은행 등의 은행코드가 된다. 
	 */
	private Map<String, JobGroupRunStats> statsByFilterCode;   // <filter code, JGRS>

	/*
	 * 각 상태별 우선순위를 정해 그룹내 대표 상태를 설정하도록 함
	 */
	private static Map<String, Integer> statePriority = new HashMap();
	static {
		statePriority.put(JobInstance.JOB_STATE_INIT,         1);
		statePriority.put(JobInstance.JOB_STATE_EXPIRED,      2);
		statePriority.put(JobInstance.JOB_STATE_ENDED_OK,     3);
		statePriority.put(JobInstance.JOB_STATE_ENDED_FAIL,   4);
		statePriority.put(JobInstance.JOB_STATE_WAIT,         5);
		statePriority.put(JobInstance.JOB_STATE_SLEEP_RPT,    6);
		statePriority.put(JobInstance.JOB_STATE_GHOST,        7);
		statePriority.put(JobInstance.JOB_STATE_SUSPENDED,    8);
		statePriority.put(JobInstance.JOB_STATE_RUNNING,      9);
	}

	public JobGroupRunStats(String jobGroupId) {
		this.jobGroupId = jobGroupId;
	}
	
	public String getJobGroupId() {
		return jobGroupId;
	}

	public int getJobDefCount() {
		return jobDefIdSet.size();
	}

	public int getJobInsCount() {
		return jobInsCount;
	}

	public String getMinTimeFrom() {
		return minTimeFrom;
	}

	public String getMaxTimeUntil() {
		return maxTimeUntil;
	}

	public int getRunTotalCount() {
		return runTotalCount;
	}

	public int getEndOkCount() {
		return endOkCount;
	}

	public int getEndFailCount() {
		return endFailCount;
	}

	/**
	 * 그룹의 대표 상태.
	 * 각 상태별 우선순위가 있음
	 * @return
	 */
	public String getState() {
		return state;
	}
	
	public long getProgressTotal() {
		return progressTotal;
	}

	public long getProgressCurrent() {
		return progressCurrent;
	}

	public double getProgressPercentage() {
		return progressTotal == 0 ? 0 : Math.ceil( progressCurrent * 100 / progressTotal );
	}
	
	public String getLastStartTime() {
		return lastStartTime;
	}

	public String getLastEndTime() {
		return lastEndTime;
	}

	/**
	 * JobFilter 값을 기준으로 sub 정보 조회 
	 * @param filterCode job filter 코드값 (예, 1은행,2은행,3은행,4은행 의 코드값들)
	 * @return
	 */
	public JobGroupRunStats getStatsByJobFilter(String filterCode) {
		return statsByFilterCode == null ? null : statsByFilterCode.get(filterCode);
	}

	/**
	 * Job 인스턴스 정보로 JobGroup 통계에 sum 한다.
	 * @param jobins
	 * @param jobFilterCode
	 * @param progressTotal
	 * @param progressCurrent
	 */
	public void sumToThis(JobInstance jobins, String jobFilterCode, long progressTotal, long progressCurrent) {
		jobInsCount ++;
		
		if (!Util.isBlank(jobins.getTimeFrom()) && 
			(minTimeFrom == null || minTimeFrom.compareTo(jobins.getTimeFrom()) > 0)) {
			minTimeFrom = jobins.getTimeFrom();
		}
		
		if (!Util.isBlank(jobins.getTimeUntil()) && 
			(maxTimeUntil == null || maxTimeUntil.compareTo(jobins.getTimeUntil()) < 0)) {
			maxTimeUntil = jobins.getTimeUntil();
		}
		
		runTotalCount += jobins.getRunCount();
		endOkCount    += jobins.getEndOkCount();
		
		/*
		 * 전체 실행 건수에서 정상 건수를 빼면 에러건수가 되지만
		 * Running, Suspended 상태에서는 아직 end 되지 않았으므로 ok/fail 산정이 불가하다 
		 * 따라서 Running, Suspended 상태에서는 endFailCount 에서 하나 뺀다 (minus).
		 */
		int endFailCountTemp = jobins.getRunCount() - jobins.getEndOkCount();
		if (JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || 
			JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {
			endFailCountTemp --;
		}
		endFailCount  += endFailCountTemp;

		/* 대표 상태 설정 */
		if (statePriority.get(jobins.getJobState()) > stateTemp) {
			state     = jobins.getJobState();
			stateTemp = statePriority.get(jobins.getJobState());
		}
		
		/* 시작 시각 기준으로 최종 실행 시각으로 설정 */
		if (!Util.isBlank(jobins.getLastStartTime()) && 
			(lastStartTime == null || lastStartTime.compareTo(jobins.getLastStartTime()) < 0)) {
			lastStartTime = jobins.getLastStartTime();
			lastEndTime   = jobins.getLastEndTime();
		}

		/* 진행률 집계 */
		this.progressTotal   += progressTotal;
		this.progressCurrent += progressCurrent;
		
		/* job filter (gubun code) 로도 취합한다. */
		if (!Util.isBlank(jobFilterCode)) {
			if (statsByFilterCode == null) {
				statsByFilterCode = new HashMap();
			}
			JobGroupRunStats stats = statsByFilterCode.get(jobFilterCode);
			if (stats == null) {
				stats = new JobGroupRunStats(jobins.getJobGroupId());
				statsByFilterCode.put(jobFilterCode, stats);
			}
			stats.sumToThis(jobins, null, progressTotal, progressCurrent);
		}
	}

	/**
	 * Job ID 는 인스턴스 생성과 상관 없이 count 되어야하므로, 별도의 절차를 통해 집계한다. 
	 * @param jobid
	 * @param jobGroupId
	 * @param jobFilterCode
	 */
	public void addJobDefId(String jobid, String jobGroupId, String jobFilterCode) {
		jobDefIdSet.add(jobid);
		
		/* job filter (gubun code) 로도 취합한다. */
		if (!Util.isBlank(jobFilterCode)) {
			if (statsByFilterCode == null) {
				statsByFilterCode = new HashMap();
			}
			JobGroupRunStats stats = statsByFilterCode.get(jobFilterCode);
			if (stats == null) {
				stats = new JobGroupRunStats(jobGroupId);
				statsByFilterCode.put(jobFilterCode, stats);
			}
			stats.addJobDefId(jobid, jobGroupId, null);
		}
	}
}
