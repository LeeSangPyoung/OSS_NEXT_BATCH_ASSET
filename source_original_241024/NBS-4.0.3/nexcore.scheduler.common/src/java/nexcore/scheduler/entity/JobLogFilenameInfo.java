package nexcore.scheduler.entity;

import java.io.Serializable;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 로그 파일 생성을 위해 정보를 담고 있는 객체. </li>
 * <li>작성일 : 2012. 11. 29.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobLogFilenameInfo implements Serializable {
	
	private static final long serialVersionUID = -7615322255166763924L;
	
	/*
	 * Job 로그 파일명은 JobId, ProcDate 등의 몇개의 정보를 조합하여 만드는데
	 * 확장성을 고려하여 여러개의 정보를 담을 수 있게 이렇게 VO 로 만든다.
	 * 
	 * 이 객체는 IAgentClient 에 통신 VO 객체로 활용되므로 조합에 필요한 항목이 늘어날때마다 
	 * IAgentClient api를 늘리는 것 보다는 이 VO 객체만 modify 하여 활용할 수 있도록 하기 위함이다.  
	 */
	
	private String jobId;
	private String jobGroupId;
	private String jobInstanceId;
	private String jobExecutionId;
	private String jobType;
	private String componentName;
	private String procDate;
	private String activationDate;
	private String activationTime;

	public JobLogFilenameInfo() {
	}
	
	public JobLogFilenameInfo(JobInstance jobins) {
		this.jobId           = jobins.getJobId();
		this.jobInstanceId   = jobins.getJobInstanceId();
		this.jobGroupId      = jobins.getJobGroupId();
		this.jobType         = jobins.getJobType();
		this.componentName   = jobins.getComponentName();
		this.procDate        = jobins.getProcDate();
		this.activationDate  = jobins.getActivationDate();
		this.activationTime  = jobins.getActivationTime();
	}
	
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getJobGroupId() {
		return jobGroupId;
	}
	public void setJobGroupId(String jobGroupId) {
		this.jobGroupId = jobGroupId;
	}
	public String getJobInstanceId() {
		return jobInstanceId;
	}
	public void setJobInstanceId(String jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}
	public String getJobExecutionId() {
        return jobExecutionId;
    }
    public void setJobExecutionId(String jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }
    public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	public String getProcDate() {
		return procDate;
	}
	public void setProcDate(String procDate) {
		this.procDate = procDate;
	}
	public String getActivationDate() {
		return activationDate;
	}
	public void setActivationDate(String activationDate) {
		this.activationDate = activationDate;
	}
	public String getActivationTime() {
		return activationTime;
	}
	public void setActivationTime(String activationTime) {
		this.activationTime = activationTime;
	}
	
}
