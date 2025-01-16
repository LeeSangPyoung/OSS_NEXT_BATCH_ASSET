package nexcore.scheduler.core.internal;

import java.sql.SQLException;
import java.util.List;

import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.ParallelGroup;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Parallel Job의 max running을 관리 </li>
 * <li>작성일 : 2010. 5. 7.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ParallelRunningCounter {
	private SqlMapClient  sqlMapClient;
	
	public ParallelRunningCounter() {
	}

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

	/**
	 * DB 에서 parallel 설정 값을 읽음.
	 * @param groupName
	 * @return
	 */
	public ParallelGroup getParallelGroup(String groupName) throws SQLException {
		return (ParallelGroup)sqlMapClient.queryForObject("nbs.scheduler.selectParallelGroup", groupName);
	}

	/**
	 * DB 에서 parallel 설정 값중 MAX 값을 읽되. FOR UPDATE 를 이용하여 LOCK 을 걸어서 읽는다.
	 * 이중화 환경에서 동시성문제를 해결할때 사용된다.
	 * @param groupName
	 * @return
	 */
	public int getParallelGroupMaxWithLock(String groupName) throws SQLException {
		Integer max = (Integer)sqlMapClient.queryForObject("nbs.scheduler.selectParallelGroupMaxWithLock", groupName);
		
		if (max == null) {
			return -1;
		}else {
			return max.intValue();
		}
	}
	
	/**
	 * 전체 병렬 그룹 조회
	 * @return
	 * @throws SQLException
	 */
	public List<ParallelGroup> getAllParallelGroupsList() throws SQLException {
		List<ParallelGroup> list = sqlMapClient.queryForList("nbs.scheduler.selectAllParallelGroup", null);
		return list;
	}

	/**
	 * 최대 실행 수 값을 변경
	 * @param groupName
	 * @param newMaxLimit
	 */
	public void addParallelGroup(ParallelGroup parallelGroup, AdminAuth auth) throws SQLException {
		int updateCnt = sqlMapClient.update("nbs.scheduler.insertParallelGroup", parallelGroup);
		if (updateCnt < 1) {
			// TODO warning 로그.
		}
	}

	/**
	 * 최대 실행 수 값을 변경
	 * @param groupName
	 * @param newMaxLimit
	 */
	public void modifyParallelGroup(ParallelGroup parallelGroup, AdminAuth auth) throws SQLException {
		int updateCnt = sqlMapClient.update("nbs.scheduler.updateParallelGroup", parallelGroup);
		if (updateCnt < 1) {
			// TODO warning 로그.
		}
	}
	
	/**
	 * 최대 실행 변수 제거
	 * @param groupName
	 * @param newMaxLimit
	 */
	public void deleteParallelGroup(String groupName, AdminAuth auth) throws SQLException {
		int updateCnt = sqlMapClient.update("nbs.scheduler.deleteParallelGroup", groupName);
		if (updateCnt < 1) {
			// TODO warning 로그.
		}
	}
}
