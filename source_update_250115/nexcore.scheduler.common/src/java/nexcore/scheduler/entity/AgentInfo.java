package nexcore.scheduler.entity;

import java.io.Serializable;

import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Agent 의 설정정보를 담는 entity. Agent 의 동적 정보 (current running job, current thread, JVM Memory)는 여기 없다</li>
 * <li>작성일 : 2011. 1. 22.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class AgentInfo implements Serializable {
	private static final long serialVersionUID = -6244778347703222572L;
	
	private String       id;                 /* AGENT 의 NEXCORE_ID                               */
	private String       name;               /* AGENT 의 이름                                    */
	private String       desc;               /* AGENT 의 설명                                    */
	private String       ip;                 /* AGENT 의 통신 IP                                 */
	private int          port;               /* AGENT 의 통신 Port. Job 실행용                   */
	private String       runMode;            /* 구동 모드. 'S':Standalone, 'W':WAS based         */
	private boolean      inUse;              /* 사용중인 agent 인지?                             */
	private String       baseDirectory;      /* 설치 기준 디렉토리. $NEXCORE_HOME                */
	private String       osUserId;           /* 구동 OS 계정                                     */
	private String       osPasswd;           /* 구동 OS 비번. 암호화됨                           */
	private String       startCmd;           /* 기동 스크립트.  예) startup.sh                   */
	private String       remoteStartType;    /* 원격 START용 접속 URL. 'TELNET', 'SSH', 'LOCAL'  */
	private int          maxRunningJob;      /* 최대 동시 실행 Job 수. 미지정시 무제한           */
	private long         lastModifyTime;
	
	private boolean      internal;        /* @INTERNAL 에이전트인지?                          */
	
	public AgentInfo() {
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
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
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
		if ("@INTERNAL".equals(this.ip)) {
			this.internal = true;
		}
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getRunMode() {
		return runMode;
	}
	public void setRunMode(String runMode) {
		this.runMode = runMode;
	}
	public boolean isInUse() {
		return inUse;
	}
	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}
	public String getInUseYN() {
		return Util.toString(inUse);
	}
	public void setInUseYN(String inUse) {
		this.inUse = Util.toBoolean(inUse);
	}
	public String getBaseDirectory() {
		return baseDirectory;
	}
	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}
	public String getOsUserId() {
		return osUserId;
	}
	public void setOsUserId(String osUserId) {
		this.osUserId = osUserId;
	}
	public String getOsPasswd() {
		return osPasswd;
	}
	public void setOsPasswd(String osPasswd) {
		this.osPasswd = osPasswd;
	}
	public String getStartCmd() {
		return startCmd;
	}
	public void setStartCmd(String startCmd) {
		this.startCmd = startCmd;
	}
	public String getRemoteStartType() {
		return remoteStartType;
	}
	public void setRemoteStartType(String remoteStartType) {
		this.remoteStartType = remoteStartType;
	}
	public int getMaxRunningJob() {
		return maxRunningJob;
	}
	public void setMaxRunningJob(int  maxRunningJob) {
		this.maxRunningJob = maxRunningJob;
	}
	/*
	public Timestamp getLastModifyTime() {
		return lastModifyTime==0 ? null : DateUtil.getTimestamp(lastModifyTime);
	}
	public void setLastModifyTime(Timestamp lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : lastModifyTime.getTime();
	}
	*/
	public String getLastModifyTime() {
		return lastModifyTime==0 ? null : DateUtil.getTimestampString(lastModifyTime);
	}
	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : DateUtil.getTimestampLong(lastModifyTime);
	}
	public boolean isInternal() {
		return internal;
	}
	public String toString() {
		// 몇몇 정보는 숨기기 위해 이렇게 함.
		return super.toString()+"[id="+id+",name="+name+",desc="+desc+",ip="+ip+"]";
	}
}
