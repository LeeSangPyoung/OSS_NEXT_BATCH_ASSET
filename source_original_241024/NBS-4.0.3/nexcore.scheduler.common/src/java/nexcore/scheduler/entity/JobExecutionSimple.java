package nexcore.scheduler.entity;

import java.io.Serializable;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치코어</li>
 * <li>설  명 : 에이전트로 부터 JobExecution 실시간 정보를 조회할때 사용될 통신 VO. 
 *              에이전트에서 변경가능한 정보들만 주로 포함됨 (상탱, 진행률)</li>
 * <li>작성일 : 2012.11.19</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobExecutionSimple implements Serializable {
	public static final long serialVersionUID = -4507290912260896662L;
	
	private String    		jobExecutionId;
	private int       		state;
	private long            progressTotal;
	private long            progressCurrent;
	
	public JobExecutionSimple() {
	}

	public String getJobExecutionId() {
		return jobExecutionId;
	}

	public void setJobExecutionId(String jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getProgressTotal() {
		return progressTotal;
	}

	public void setProgressTotal(long progressTotal) {
		this.progressTotal = progressTotal;
	}

	public long getProgressCurrent() {
		return progressCurrent;
	}

	public void setProgressCurrent(long progressCurrent) {
		this.progressCurrent = progressCurrent;
	}

	public boolean equals(Object obj) {
		return jobExecutionId.equals(((JobExecutionSimple)obj).getJobExecutionId());
	}
	
}

