package nexcore.scheduler.monitor.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import nexcore.scheduler.entity.User;
import nexcore.scheduler.entity.UserAuth;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 사용자 테이블 관리 모듈. NBS_USER_AUTH 테이블이 없는 경우를 위해 Auth 정보를 파일로 관리. </li>
 * <li>작성일 : 2013. 2. 15.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class UserManagerAuthByFile extends UserManager {

	/* 
	 * 2013-02-15
	 * 사내 통합 IT 시스템 배치 스케줄러 업그레이드에서 AUTH 테이블을 생성을 할 수 없는 상황이라 임시로 이렇게 파일을 이용하도록 하드코딩한다.
	 * 추후에 추가 업그레이드가 필요할 경우는 AUTH 테이블을 생성하고 정식으로 돌아가도록 한다.
	 *  
	 * 사내 통합 IT 시스템에서만 사용할 것이므로 오라클에서만 동작하면된다. 
	 * IBATIS 에는 때를 묻히지 않기 위해 JDBC 로 하드코딩한다.  
	 */

	public UserManagerAuthByFile() {
	}
	
	private void closeConnection(Connection conn, Statement stmt, ResultSet rs) {
		if (conn != null) {
			try { conn.close(); }catch(Exception e) {e.printStackTrace();}
		}
		if (stmt != null) {
			try { stmt.close(); }catch(Exception e) {e.printStackTrace();}
		}
		if (rs != null) {
			try { rs.close(); }catch(Exception e) {e.printStackTrace();}
		}
	}
	
	private User getUserFromResultSet(ResultSet rs) throws SQLException {
		User u = new User();
		u.setId(               rs.getString("USER_ID"));
		u.setPassword(         rs.getString("USER_PASSWD"));
		u.setName(             rs.getString("USER_NAME"));
		u.setDesc(             rs.getString("USER_DESC"));
		u.setTeam1(            rs.getString("TEAM1"));
		u.setTeam2(            rs.getString("TEAM2"));
		u.setEmail(            rs.getString("EMAIL"));
		u.setPhone(            rs.getString("PHONE"));
		u.setAdmin(            rs.getBoolean("IS_ADMIN"));
		u.setOperator(         rs.getBoolean("IS_OPERATOR"));
		u.setActive(           rs.getBoolean("IS_ACTIVE"));
		u.setOperateJobIdExp(  rs.getString("OPER_JOB_ID_EXP"));
		u.setCreateTime(       rs.getString("CREATE_TIME"));
		u.setLastModifyTime(   rs.getString("LAST_MODIFY_TIME"));
		return u;
	}
	
	public User getUser(String userid) throws SQLException {
		Connection        conn  = null;
		PreparedStatement pstmt = null;
		ResultSet         rs    = null;
		try {
			conn = sqlMapClient.getDataSource().getConnection();
			pstmt = conn.prepareStatement("SELECT * FROM NBS_USER WHERE USER_ID=?");
			pstmt.setString(1, userid);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				User u = getUserFromResultSet(rs);
				readUserAuth(u);
				return u;
			}else {
				return null;
			}
		}finally {
			closeConnection(conn, pstmt, rs);
		}
	}
	
	public List<User> getUserByQuery(String queryCondition, String orderBy) throws SQLException {
		Connection        conn  = null;
		PreparedStatement pstmt = null;
		ResultSet         rs    = null;
		List<User>        list  = new ArrayList();
		try {
			conn = sqlMapClient.getDataSource().getConnection();
			pstmt = conn.prepareStatement("SELECT * FROM NBS_USER "+queryCondition+" "+orderBy);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				User u = getUserFromResultSet(rs);
				readUserAuth(u);
				list.add(u);
			}
		}finally {
			closeConnection(conn, pstmt, rs);
		}
		return list;
	}

	private File getUserAuthFile(String userid) {
		return new File(Util.getHomeDirectory()+"/res/userauth/", userid+".auth");
	}
	
	/**
	 * $NEXCORE_HOME/res/useauth/userid.auth 파일을 읽어 권한 리스트를 로드한다. 
	 * @param user
	 * @throws SQLException
	 */
	private void readUserAuth(User user) {
		File f = getUserAuthFile(user.getId());
		if (!f.exists()) return;
		
		BufferedReader in = null;
		String line = null;
		try {
			in = new BufferedReader(new FileReader(f));
			List authList = new ArrayList();
			while((line = in.readLine()) != null) {
				int index = line.indexOf(",");
				if (index < 0) continue;
				String kind      = line.substring(0, index);
				String targetObj = line.substring(index+1);
				UserAuth ua = new UserAuth();
				ua.setUserId(user.getId());
				ua.setAuthKind(kind);
				ua.setTargetObject(targetObj);
				authList.add(ua);
			}
			user.setAuthList(authList);
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				in.close();
			} catch (Exception e2) {
			}
		}
	}

	/**
	 * NBS_USER_AUTH 테이블에 하지 않고
	 * $NEXCORE_HOME/res/useauth/userid.auth 파일에 권한 리스트를 저장한다. 
	 * @param user
	 * @throws SQLException
	 */
	public void addUserAuth(User user) throws SQLException {
		File f = getUserAuthFile(user.getId());
		
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(f));
			for (UserAuth userAuth : user.getAuthList()) {
				out.println(userAuth.getAuthKind()+","+userAuth.getTargetObject());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				out.close();
			} catch (Exception e2) {
			}
		}
	}
	
	public void removeUserAuth(String userId) throws SQLException {
		getUserAuthFile(userId).delete();
	}
}