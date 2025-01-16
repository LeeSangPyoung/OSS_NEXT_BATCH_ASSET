package nexcore.scheduler.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 사용자 / 관리자 / 운영자</li>
 * <li>작성일 : 2011. 3. 9.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class User implements Serializable {
	private static final long serialVersionUID = 3432667694319150592L;
	
	private String          id;               //  
	private String          password;         // 비번. MD5 암호화 
	private String          name;             // 이름.
	private String          desc;             // 설명. 
	private String          team1;            // 소속 팀. 예) 응용개발 2팀
	private String          team2;            // 소속 서브 팀. 예) 수신
	private String          email;
	private String          phone;    
	private boolean         isAdmin;          // ADMIN 여부. ADMIN 은 설정 및, 계정 생성을 할 수 있음    
	private boolean         isOperator;       // OPERATOR 여부. OPERATOR는 operateJobIdExp 에 등록된 job 들의 Job 정보 변경, activation, control(star/stop 등등) 을 할 수 있음  
	private boolean         isActive;         // 게정 유효 여부    
	private String          operateJobIdExp;  // operator 인 경우. operate할 Job 들의 패턴. (정규표현식) 
	private long            createTime;       // 생성일
	private long            lastModifyTime;   // 최종 변경일
	private List<UserAuth>  authList;         // 권한 리스트.
	
	private Map<String, List<String>> authListMap; // 권한 리스트. <AuthKind, List<TargetObject>>  

	/*
	 * operateJobIdExp 값이 null 인 경우, OPER_JOBGROUP Auth속성으로 대체한다.
	 * 
	 */
	
	public User() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getTeam1() {
		return team1;
	}

	public void setTeam1(String team1) {
		this.team1 = team1;
	}

	public String getTeam2() {
		return team2;
	}

	public void setTeam2(String team2) {
		this.team2 = team2;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
	public String getAdminYN() {
		return Util.toString(isAdmin);
	}

	public void setAdminYN(String isAdmin) {
		this.isAdmin = Util.toBoolean(isAdmin);
		
	}

	public boolean isOperator() {
		return isOperator;
	}

	public void setOperator(boolean isOperator) {
		this.isOperator = isOperator;
	}
	
	public String getOperatorYN() {
		return Util.toString(isOperator);
	}

	public void setOperatorYN(String isOperator) {
		this.isOperator = Util.toBoolean(isOperator);
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public String getActiveYN() {
		return Util.toString(isActive);
	}

	public void setActiveYN(String isActive) {
		this.isActive = Util.toBoolean(isActive);
	}

	public String getOperateJobIdExp() {
		return operateJobIdExp;
	}

	public void setOperateJobIdExp(String operateJobIdExp) {
		this.operateJobIdExp = operateJobIdExp;
	}

	public List<UserAuth> getAuthList() {
		return authList;
	}
	
	public void setAuthList(List<UserAuth> authList) {
		this.authList = authList;
	}

	/**
	 * 권한 종류별 targetObject List 를 리턴함
	 * @param authKind
	 * @return List of targetObject, empty list if not exist (not null).
	 */
	public List<String> getAuthList(String authKind) {
		if (authListMap == null || authListMap.size() < 1) {
			setupAuthListMap();
		}
		List<String> list = authListMap.get(authKind);
		return list == null ? Collections.EMPTY_LIST : list;
	}

	private void setupAuthListMap() {
		authListMap = new HashMap();
		if (this.authList == null) {
			return;
		}
		
		for (UserAuth userAuth : authList) {
			List list = authListMap.get(userAuth.getAuthKind());
			if (list == null) {
				list = new ArrayList();
				authListMap.put(userAuth.getAuthKind(), list);
			}
			list.add(userAuth.getTargetObject());
		}
	}
	/*
	public Timestamp getCreateTime() {
		return createTime==0 ? null : DateUtil.getTimestamp(createTime);
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime==null ? 0 : createTime.getTime();
	}

	public Timestamp getLastModifyTime() {
		return lastModifyTime == 0 ? null : DateUtil.getTimestamp(lastModifyTime);
	}

	public void setLastModifyTime(Timestamp lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : lastModifyTime.getTime();
	}
	 */
	
	public String getCreateTime() {
		return createTime==0 ? null : DateUtil.getTimestampString(createTime);
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime==null ? 0 : DateUtil.getTimestampLong(createTime);
	}

	public String getLastModifyTime() {
		return lastModifyTime == 0 ? null : DateUtil.getTimestampString(lastModifyTime);
	}

	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : DateUtil.getTimestampLong(lastModifyTime);
	}
	
	public String toString() { 
		// 비밀번호는 안 찍음
		return super.toString()+"[id="+id+",name="+name+",desc="+desc+",operateJobIdExp="+operateJobIdExp+",userAuth="+authList+"]";
	}

}
