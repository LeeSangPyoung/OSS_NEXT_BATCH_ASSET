package nexcore.scheduler.entity;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import nexcore.scheduler.util.Util;


/**
 * <ul>
 * <li>업무 그룹명 : 배치 스케줄러</li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 후행 Job Trigger 정보. NBS_JOB_DEF_TRIGGER </li>
 * <li>작성일 : 2016. 7. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class PostJobTrigger implements Serializable {
	public static final long serialVersionUID = 7494750509463654481L;

	protected String when;                /* 트리거 생성 조건. ENDOK, ENDFAIL, RETVAL 등  */
	protected String checkValue1;         /* condition 의 상세 값1. conditionWhen = RETVAL 일 경우 parent 의 리턴값의 KEY 값 */
	protected String checkValue2;         /* condition 의 상세 값2. conditionWhen = RETVAL 일 경우 parent 의 리턴값의 VALUE 값 */
	protected String checkValue3;         /* 여분 */
	protected String triggerJobId;        /* 트리거로 생성되는 Job ID */
	protected int    jobInstanceCount;    /* 트리거로 생성되는 Job 인스턴수 개수 */

	public PostJobTrigger() {
	}
	
	public PostJobTrigger(String when, String checkValue1, String checkValue2, String checkValue3, String triggerJobId, int jobInstanceCount) {
		this.when             = when;
		this.checkValue1      = checkValue1;
		this.checkValue2      = checkValue2;
		this.checkValue3      = checkValue3;
		this.triggerJobId     = triggerJobId;
		this.jobInstanceCount = jobInstanceCount;
	}

	public PostJobTrigger(Map<String, String> mapFromSQL) {
		this.when             = mapFromSQL.get("ACTIVATE_WHEN");
		this.checkValue1      = mapFromSQL.get("CHECK_VALUE1");
		this.checkValue2      = mapFromSQL.get("CHECK_VALUE2");
		this.checkValue3      = mapFromSQL.get("CHECK_VALUE3");
		this.triggerJobId     = mapFromSQL.get("TRIGGER_JOB_ID");
		this.jobInstanceCount = Util.toInt(String.valueOf(mapFromSQL.get("INSTANCE_COUNT")), 1);
	}

	public String getWhen() {
		return when;
	}

	public void setWhen(String when) {
		this.when = when;
	}

	public String getCheckValue1() {
		return checkValue1;
	}

	public void setCheckValue1(String checkValue1) {
		this.checkValue1 = checkValue1;
	}

	public String getCheckValue2() {
		return checkValue2;
	}

	public void setCheckValue2(String checkValue2) {
		this.checkValue2 = checkValue2;
	}

	public String getCheckValue3() {
		return checkValue3;
	}

	public void setCheckValue3(String checkValue3) {
		this.checkValue3 = checkValue3;
	}

	public String getTriggerJobId() {
		return triggerJobId;
	}

	public void setTriggerJobId(String jobId) {
		this.triggerJobId = jobId;
	}

	public int getJobInstanceCount() {
		return jobInstanceCount;
	}

	public void setJobInstanceCount(int jobInstanceCount) {
		this.jobInstanceCount = jobInstanceCount;
	}
	
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof PostJobTrigger) {
			PostJobTrigger pjt = (PostJobTrigger)obj;

			return Util.equalsIgnoreNull(this.when,  pjt.when)
				&& Util.equalsIgnoreNull(this.checkValue1,  pjt.checkValue1)
				&& Util.equalsIgnoreNull(this.checkValue2,  pjt.checkValue2)
				&& Util.equalsIgnoreNull(this.checkValue3,  pjt.checkValue3)
				&& Util.equalsIgnoreNull(this.triggerJobId,        pjt.triggerJobId)
				&& this.jobInstanceCount == pjt.jobInstanceCount;
		}else {
			return false;
		}
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}                                                                             
