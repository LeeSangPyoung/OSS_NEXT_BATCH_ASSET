package nexcore.scheduler.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import nexcore.scheduler.util.DateUtil;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 9999 </li>
 * <li>설  명 : Job 통지 정보 VO </li>
 * <li>작성일 : 2011. 4. 7.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobNotifySendInfo implements Serializable {
	private static final long serialVersionUID = -2020303596704591443L;
	
	private  int          seqNo; 
	private  String       jobId;
	private  String       jobInstanceId;
	private  String       jobExecutionId;
	private  String       jobDesc;
	private  String       agentNode;
	private  String       jobGroupId;
	private  String       owner;
	private  String       ownerTel;
	//private  Timestamp    startTime;
	//private  Timestamp    endTime;
	private  long         startTime;
	private  long         endTime;
	private  String       operatorId;
	private  String       operatorName;
	private  String       operatorIp;
	private  int          returnCode;
	private  String       errorMsg;
	private  int          receiverId;
	private  String       receiverName;
	private  String       recvType;
	private  String       recvPoint;
	//private  Timestamp    createTime;
	private  long         createTime;
	private  String       sendState;
	//private  Timestamp    sendTime;  
	private  long         sendTime;
	private  int          tryCount;
	
	private  int          notifyId;
	private  String       checkValue1;
	private  String       checkValue2;
	private  String       checkValue3;
	
	private  String       procSystemId;
	
	public int getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
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
	public String getJobDesc() {
		return jobDesc;
	}
	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}
	public String getAgentNode() {
		return agentNode;
	}
	public void setAgentNode(String agentNode) {
		this.agentNode = agentNode;
	}
	public String getJobGroupId() {
		return jobGroupId;
	}
	public void setJobGroupId(String jobGroupId) {
		this.jobGroupId = jobGroupId;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getOwnerTel() {
		return ownerTel;
	}
	public void setOwnerTel(String ownerTel) {
		this.ownerTel = ownerTel;
	}
	/*
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public Timestamp getEndTime() {
		return endTime;
	}
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}
	*/
	public String getStartTime() {
		return DateUtil.getTimestampString(startTime);
	}
	public void setStartTime(String startTime) {
		this.startTime = DateUtil.getTimestampLong(startTime);
	}
	public String getEndTime() {
		return DateUtil.getTimestampString(endTime);
	}
	public void setEndTime(String endTime) {
		this.endTime = DateUtil.getTimestampLong(endTime);
	}
	public String getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}
	public String getOperatorName() {
		return operatorName;
	}
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	public String getOperatorIp() {
		return operatorIp;
	}
	public void setOperatorIp(String operatorIp) {
		this.operatorIp = operatorIp;
	}
	public int getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public int getReceiverId() {
		return receiverId;
	}
	public void setReceiverId(int receiverId) {
		this.receiverId = receiverId;
	}
	public String getReceiverName() {
		return receiverName;
	}
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
	public String getRecvType() {
		return recvType;
	}
	public void setRecvType(String recvType) {
		this.recvType = recvType;
	}
	public String getRecvPoint() {
		return recvPoint;
	}
	public void setRecvPoint(String recvPoint) {
		this.recvPoint = recvPoint;
	}
	/*
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	*/
	public String getCreateTime() {
		return DateUtil.getTimestampString(createTime);
	}
	public void setCreateTime(String createTime) {
		this.createTime = DateUtil.getTimestampLong(createTime);
	}
	public String getSendState() {
		return sendState;
	}
	public void setSendState(String sendState) {
		this.sendState = sendState;
	}
	/*
	public Timestamp getSendTime() {
		return sendTime;
	}
	public void setSendTime(Timestamp sendTime) {
		this.sendTime = sendTime;
	}
	*/
	public String getSendTime() {
		return DateUtil.getTimestampString(sendTime);
	}
	public void setSendTime(String sendTime) {
		this.sendTime = DateUtil.getTimestampLong(sendTime);
	}
	public int getTryCount() {
		return tryCount;
	}
	public void setTryCount(int tryCount) {
		this.tryCount = tryCount;
	}
	public String getProcSystemId() {
		return procSystemId;
	}
	public int getNotifyId() {
		return notifyId;
	}
	public void setNotifyId(int notifyId) {
		this.notifyId = notifyId;
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
	public void setProcSystemId(String procSystemId) {
		this.procSystemId = procSystemId;
	}
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
