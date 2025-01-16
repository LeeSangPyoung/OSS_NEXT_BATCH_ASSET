package nexcore.scheduler.entity;

import java.io.Serializable;
import java.net.InetAddress;

import nexcore.scheduler.util.Util;

/**
 * Admin operation 시 사용될 로그인, 비밀번호 정보
 * @author 정호철
 *
 */
public class AdminAuth implements Serializable {
	private static final long serialVersionUID = -431811479788963491L;
	
	private String operatorId;
	private String operatorPasswd;
	private String operatorIp;
	
	private static String systemId;
	private static String systemIp;
	
	static {
		systemId = System.getProperty("NEXCORE_ID");
		try {
			systemIp = InetAddress.getLocalHost().getHostAddress();
		}catch (Exception e) {
			systemIp = "N/A";
		}
	}
	
	public static AdminAuth getAdminAuthSchedulerItself() {
		return new AdminAuth(systemId, systemIp);
	}
	
	public AdminAuth(String operatorId) {
		this.operatorId     = operatorId;
	}

	/**
	 * @param operatorId
	 * @param operatorIp
	 * @param password
	 */
	public AdminAuth(String operatorId, String operatorIp) {
		this.operatorId     = operatorId;
		this.operatorIp     = operatorIp;
	}

	/**
	 * @param operatorId
	 * @param operatorIp
	 * @param operatorPasswd
	 * @param password
	 */
	public AdminAuth(String operatorId, String operatorIp, String operatorPasswd) {
		this.operatorId     = operatorId;
		this.operatorIp     = operatorIp;
		this.operatorPasswd = operatorPasswd;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getOperatorIp() {
		return operatorIp;
	}

	public void setOperatorIp(String operatorIp) {
		this.operatorIp = operatorIp;
	}

	public String getOperatorPasswd() {
		return operatorPasswd;
	}

	public void setOperatorPasswd(String operatorPasswd) {
		this.operatorPasswd = operatorPasswd;
	}
	
	public String toString() {
		return Util.isBlank(operatorIp) ? 
			operatorId : 
			operatorId+"/"+operatorIp;
	}
}
