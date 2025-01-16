package nexcore.scheduler.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 통지 수신자 상세 정보 </li>
 * <li>작성일 : 2011. 02. 05.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobNotifyReceiver implements Serializable {
	private static final long serialVersionUID = 6898683641864638147L;
	
	private int        id;
	private String     name;
	private String     desc;
	private boolean    recvByEmail;
	private String     emailAddr;
	private boolean    recvBySms;
	private String     smsNum;
	private boolean    recvByMessenger;
	private String     messengerId;
	private boolean    recvByTerminal;
	private String     terminalId;
	private boolean    recvByDev1;
	private String     dev1Point;
	private boolean    recvByDev2;
	private String     dev2Point;
	private boolean    recvByDev3;
	private String     dev3Point;
	//private Timestamp  lastModifyTime;
	private long  lastModifyTime;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public boolean isRecvByEmail() {
		return recvByEmail;
	}
	public void setRecvByEmail(boolean recvByEmail) {
		this.recvByEmail = recvByEmail;
	}
	public String getRecvByEmailYN() {
		return Util.toString(recvByEmail);
	}
	public void setRecvByEmailYN(String recvByEmail) {
		this.recvByEmail = Util.toBoolean(recvByEmail);
	}
	public String getEmailAddr() {
		return emailAddr;
	}
	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}
	public boolean isRecvBySms() {
		return recvBySms;
	}
	public void setRecvBySms(boolean recvBySms) {
		this.recvBySms = recvBySms;
	}
	public String getRecvBySmsYN() {
		return Util.toString(recvBySms);
	}
	public void setRecvBySmsYN(String recvBySms) {
		this.recvBySms = Util.toBoolean(recvBySms);
	}
	public String getSmsNum() {
		return smsNum;
	}
	public void setSmsNum(String smsNum) {
		this.smsNum = smsNum;
	}
	public boolean isRecvByMessenger() {
		return recvByMessenger;
	}
	public void setRecvByMessenger(boolean recvByMessenger) {
		this.recvByMessenger = recvByMessenger;
	}
	public String getRecvByMessengerYN() {
		return Util.toString(recvByMessenger);
	}
	public void setRecvByMessengerYN(String recvByMessenger) {
		this.recvByMessenger = Util.toBoolean(recvByMessenger);
	}
	public String getMessengerId() {
		return messengerId;
	}
	public void setMessengerId(String messengerId) {
		this.messengerId = messengerId;
	}
	public boolean isRecvByTerminal() {
		return recvByTerminal;
	}
	public void setRecvByTerminal(boolean recvByTerminal) {
		this.recvByTerminal = recvByTerminal;
	}
	public String getRecvByTerminalYN() {
		return Util.toString(recvByTerminal);
	}
	public void setRecvByTerminalYN(String recvByTerminal) {
		this.recvByTerminal = Util.toBoolean(recvByTerminal);
	}
	public String getTerminalId() {
		return terminalId;
	}
	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}
	
	public boolean isRecvByDev1() {
		return recvByDev1;
	}
	public void setRecvByDev1(boolean recvByDev1) {
		this.recvByDev1 = recvByDev1;
	}
	public String getRecvByDev1YN() {
		return Util.toString(recvByDev1);
	}
	public void setRecvByDev1YN(String recvByDev1) {
		this.recvByDev1 = Util.toBoolean(recvByDev1);
	}
	public String getDev1Point() {
		return dev1Point;
	}
	public void setDev1Point(String dev1Point) {
		this.dev1Point = dev1Point;
	}
	public boolean isRecvByDev2() {
		return recvByDev2;
	}
	public void setRecvByDev2(boolean recvByDev2) {
		this.recvByDev2 = recvByDev2;
	}
	public String getRecvByDev2YN() {
		return Util.toString(recvByDev2);
	}
	public void setRecvByDev2YN(String recvByDev2) {
		this.recvByDev2 = Util.toBoolean(recvByDev2);
	}
	public String getDev2Point() {
		return dev2Point;
	}
	public void setDev2Point(String dev2Point) {
		this.dev2Point = dev2Point;
	}
	public boolean isRecvByDev3() {
		return recvByDev3;
	}
	public void setRecvByDev3(boolean recvByDev3) {
		this.recvByDev3 = recvByDev3;
	}
	public String getRecvByDev3YN() {
		return Util.toString(recvByDev3);
	}
	public void setRecvByDev3YN(String recvByDev3) {
		this.recvByDev3 = Util.toBoolean(recvByDev3);
	}
	public String getDev3Point() {
		return dev3Point;
	}
	public void setDev3Point(String dev3Point) {
		this.dev3Point = dev3Point;
	}
	/*
	public Timestamp getLastModifyTime() {
		return lastModifyTime;
	}
	public void setLastModifyTime(Timestamp lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}
	*/
	public String getLastModifyTime() {
		return DateUtil.getTimestampString(lastModifyTime);
	}
	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = DateUtil.getTimestampLong(lastModifyTime);
	}
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
