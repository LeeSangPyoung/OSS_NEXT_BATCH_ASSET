package nexcore.scheduler.entity;

import java.io.Serializable;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 사용자 권한 관리 (NBS_USER_AUTH)</li>
 * <li>작성일 : 2013. 2. 6.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class UserAuth implements Serializable {
	private static final long serialVersionUID = 4504514738568655500L;
	
	private String          userId;
	private String          authKind;
	private String          targetObject;
	
	public UserAuth() {
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAuthKind() {
		return authKind;
	}

	public void setAuthKind(String authKind) {
		this.authKind = authKind;
	}

	public String getTargetObject() {
		return targetObject;
	}

	public void setTargetObject(String targetObject) {
		this.targetObject = targetObject;
	}

	public String toString() { 
		return super.toString()+"[authKind="+authKind+",targetObject="+targetObject+"]";
	}

}
