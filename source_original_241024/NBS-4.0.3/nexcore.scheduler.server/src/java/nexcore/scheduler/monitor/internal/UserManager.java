package nexcore.scheduler.monitor.internal;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.User;
import nexcore.scheduler.entity.UserAuth;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.MessageDigestUtil;
import nexcore.scheduler.util.Util;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 사용자 관리 모듈. 관리자/운영자/일반사용자로 구분됨 </li>
 * <li>작성일 : 2011. 3. 9.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class UserManager {
	protected SqlMapClient                sqlMapClient;

	public void init() {
	}
	
	public void destroy() {
	}
	
	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}
	
	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public User getUser(String userid) throws SQLException {
		return (User)sqlMapClient.queryForObject("nbs.monitor.selectUser", userid);
	}
	
	public List<User> getUserByQuery(String queryCondition, String orderBy) throws SQLException {
		Map map = new HashMap();
		map.put("queryCondition", queryCondition);
		map.put("orderBy",        orderBy);
		return (List)sqlMapClient.queryForList("nbs.monitor.selectUserByQuery", map);
	}

	public boolean addUser(User user) throws SQLException {
		long current = System.currentTimeMillis();
		user.setCreateTime(DateUtil.getTimestampString(current));
		user.setLastModifyTime(DateUtil.getTimestampString(current));
		boolean b = sqlMapClient.update("nbs.monitor.insertUser", user) > 0;
		addUserAuth(user);
		return b;
	}
	
	public void addUserAuth(User user) throws SQLException {
		sqlMapClient.startBatch();
		for (UserAuth userAuth : user.getAuthList()) {
			sqlMapClient.update("nbs.monitor.insertUserAuth", userAuth);
		}
		sqlMapClient.executeBatch();
	}
	
	public boolean modifyUser(User user) throws SQLException {
		user.setLastModifyTime(DateUtil.getCurrentTimestampString());
		boolean b = sqlMapClient.update("nbs.monitor.updateUser", user) > 0;
		removeUserAuth(user.getId());
		addUserAuth(user);
		return b;
	}
	
	
	public boolean removeUser(String userId) throws SQLException {
		boolean b = sqlMapClient.delete("nbs.monitor.deleteUser", userId) > 0;
		removeUserAuth(userId);
		return b;
	}

	public void removeUserAuth(String userId) throws SQLException {
		sqlMapClient.delete("nbs.monitor.deleteUserAuth", userId);
	}
	
	public boolean modifyUserPassword(User user) throws SQLException {
		user.setLastModifyTime(DateUtil.getCurrentTimestampString());
		return sqlMapClient.update("nbs.monitor.updatePassword", user) > 0;
	}

	// ==================== 권한 체크 관련 메소드 ========================
	
	public void checkOperationPermission(String jobGroupId, String jobId, String action, AdminAuth auth) {
		try {
			User user = getUser(auth.getOperatorId());
			if (user==null) {
				throw new SchedulerException("main.permission.unknown.user", auth);
			}
			checkOperationPermission(jobGroupId, jobId, action, user);
		}catch (SQLException e) {
			throw new SchedulerException("main.user.admin.error", e, 1, auth.getOperatorId()); // 사용자 {} 중 에러가 발생하였습니다.
		}
	}

	public void checkOperationPermission(String jobGroupId, String jobId, String action, User user) {
		if (!user.isActive()) {
			throw new SchedulerException("main.permission.error.inactive.state", user.getId());
		}
		if (Util.isBlank(jobId)) {
			throw new SchedulerException("main.permission.error.wrong.jobid");
		}
		if (!user.isOperator()) {
			throw new SchedulerException("main.permission.error.not.operator", user.getId());
		}
		if (!isAllowedForOperation(jobGroupId, jobId, user)) {
			throw new SchedulerException("main.permission.error.not.allowed.jobid", user.getId(), jobId, action);
		}
	}
	
	/**
	 * 운영 권한이 허용되는 Job, JobGroup 인지?
	 * @param jobGroupId
	 * @param jobId
	 * @param user
	 * @return true 허용, false 불허
	 */
	public boolean isAllowedForOperation(String jobGroupId, String jobId, User user) {
		if (user.isOperator()) {
			boolean matchByJobID = false;
			if (!Util.isBlank(user.getOperateJobIdExp())) { // JOB ID 패턴으로 체크.
				matchByJobID = jobId.matches(user.getOperateJobIdExp());
				if (matchByJobID) {
					return true;
				}
			}
			
			// 사용자 별 운영자용 Job 그룹 리스트.
			boolean matchByJobGroup = false;
			List<String> authListForOperation = user.getAuthList("OPER_JOBGROUP");
			for (String jobGroup : authListForOperation) {
				if (jobGroup.equals(jobGroupId)) {
					matchByJobGroup = true;
					break;
				}
			}
			
			return matchByJobID || matchByJobGroup;
		}else {
			return false;
		}
	}
	
	public void checkAdminPermission(AdminAuth auth) {
		try {
			User user = getUser(auth.getOperatorId());
			if (user==null) {
				throw new SchedulerException("main.permission.unknown.user", auth);
			}
			checkAdminPermission(user);
		}catch (SQLException e) {
			throw new SchedulerException("main.user.admin.error", e, 1, auth.getOperatorId()); // 사용자 {} 중 에러가 발생하였습니다.
		}
	}
	
	public void checkAdminPermission(User user) {
		if (!user.isActive()) {
			throw new SchedulerException("main.permission.error.inactive.state", user.getId());
		}
		if (!user.isAdmin()) {
			throw new SchedulerException("main.permission.error.not.admin", user.getId());
		}
	}

	public User login(String id, String password, String ip) {
		if (Util.isBlank(password)) {
			throw new SchedulerException("main.login.password.empty"); // 입력된 비밀번호가 비어있습니다
		}
		
		try {
			User user = getUser(id);
			if (user==null) {
				throw new SchedulerException("main.login.id.notfound", id); // {0}는 없는 계정입니다
			}else if (!user.isActive()) {
				throw new SchedulerException("main.login.inactive", id); // {0}는 없는 비활성 계정입니다
			}

			String encodedPassword = null;
			if (Util.nvl(user.getPassword()).length() > 30) {
				// SHA256
				encodedPassword = MessageDigestUtil.encode(password);
				
			}else {
				// MD5
				encodedPassword = MessageDigestUtil.encodeMD5(password);
			}
			
			if (encodedPassword.equals(user.getPassword())) {
				user.setPassword(null);
				return user;
			}else {
				throw new SchedulerException("main.login.wrong.password"); // 비밀번호가 틀렸습니다
			}
		} catch (SQLException e) {
			throw new SchedulerException("main.user.admin.error", e, 1, id);
		}
	}
}