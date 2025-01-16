package nexcore.scheduler.entity;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * JobDefinition 등록/변경/삭제를 위한 staging 객체
 * @author 정호철
 *
 */
public class JobDefinitionStg extends JobDefinition {
	private static final long serialVersionUID = 1858382653762727080L;
	
	private String reqNo;           // 요청번호
	private String reqUserName;     // 요청자 ID
	private String reqUserIp;       // 요청자 IP
	private String reqTime;         // 요청시각. YYYYMMDDHHMMSS
	private String reqType;         // 요청타입. add, edit, delete
	private String reqComment;      // 요청코멘트
	private String reqState;        // 요청상태. "Q":요청, "A(YYYYMMDDHHMMSS)":승인, "R(YYYYMMDDHHMMSS)":반려
	private String reqARReason;     // 승인/반려 사유.
	private String reqOperatorId;   // 승인자 ID
	private String reqOperatorName; // 승인자 이름
	private String reqOperatorIp;   // 승인자 IP
	
	public JobDefinitionStg() {
	}
	
	public JobDefinitionStg(JobDefinition jobdef) {
		
		setAgentNode(                  jobdef.getAgentNode());
		setBaseDateCalId(              jobdef.getBaseDateCalId());
		setBaseDateLogic(              jobdef.getBaseDateLogic());
		setBeforeAfterExp(             jobdef.getBeforeAfterExp());
		setCalendarExps(               jobdef.getCalendarExps());
		setCalendarId(                 jobdef.getCalendarId());
		setComponentName(              jobdef.getComponentName());
		setConfirmNeedYN(              jobdef.getConfirmNeedYN());
		setCreateTime(                 jobdef.getCreateTime());
		setDayOfMonthScheduleType(     jobdef.getDayOfMonthScheduleType());
		setDaysInMonth(                jobdef.getDaysInMonth());
		setDaysOfWeek(                 jobdef.getDaysOfWeek());
		setDescription(                jobdef.getDescription());
		setExtraSchedule(              jobdef.getExtraSchedule());
		setFixedDays(                  jobdef.getFixedDays());
		setJobGroupId(                 jobdef.getJobGroupId());
		setJobId(                      jobdef.getJobId());
		setJobType(                    jobdef.getJobType());
		setLogLevel(                   jobdef.getLogLevel());
		setMonths(                     jobdef.getMonths());
		setOwner(                      jobdef.getOwner());
		setParallelGroup(              jobdef.getParallelGroup());
		setRepeatExactExp(             jobdef.getRepeatExactExp());
		setRepeatIfError(              jobdef.getRepeatIfError());
		setRepeatIntval(               jobdef.getRepeatIntval());
		setRepeatIntvalGb(             jobdef.getRepeatIntvalGb());
		setRepeatMaxOk(                jobdef.getRepeatMaxOk());
		setRepeatYN(                   jobdef.getRepeatYN());
//		setReverse(                    jobdef.isReverse()); // 이 정보는 setExtraSchedule 에서 한다.
		setScheduleType(               jobdef.getScheduleType());
		setShiftExp(                   jobdef.getShiftExp());
		setShiftExp2(                  jobdef.getShiftExp2());
		setTimeFrom(                   jobdef.getTimeFrom());
		setTimeUntil(                  jobdef.getTimeUntil());
		setTriggerList(                jobdef.getTriggerList());
		setWeekdayMonthdayType(        jobdef.getWeekdayMonthdayType());
		
		setPreJobConditions(           jobdef.getPreJobConditions());
		setInParameters(               jobdef.getInParameters());
	}

	public String getReqNo() {
		return reqNo;
	}

	public void setReqNo(String reqNo) {
		this.reqNo = reqNo;
	}

	public String getReqUserName() {
		return reqUserName;
	}

	public void setReqUserName(String reqUserName) {
		this.reqUserName = reqUserName;
	}

	public String getReqUserIp() {
		return reqUserIp;
	}

	public void setReqUserIp(String reqUserIp) {
		this.reqUserIp = reqUserIp;
	}

	public String getReqTime() {
		return reqTime;
	}

	public void setReqTime(String reqTime) {
		this.reqTime = reqTime;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getReqComment() {
		return reqComment;
	}

	public void setReqComment(String reqComment) {
		this.reqComment = reqComment;
	}

	public String getReqState() {
		return reqState;
	}

	public void setReqState(String reqState) {
		this.reqState = reqState;
	}

	public String getReqARReason() {
		return reqARReason;
	}

	public void setReqARReason(String reqARReason) {
		this.reqARReason = reqARReason;
	}
	
	public String getReqOperatorId() {
		return reqOperatorId;
	}

	public void setReqOperatorId(String reqOperatorId) {
		this.reqOperatorId = reqOperatorId;
	}

	public String getReqOperatorName() {
		return reqOperatorName;
	}

	public void setReqOperatorName(String reqOperatorName) {
		this.reqOperatorName = reqOperatorName;
	}

	public String getReqOperatorIp() {
		return reqOperatorIp;
	}

	public void setReqOperatorIp(String reqOperatorIp) {
		this.reqOperatorIp = reqOperatorIp;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
