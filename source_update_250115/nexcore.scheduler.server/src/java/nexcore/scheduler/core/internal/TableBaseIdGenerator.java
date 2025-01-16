package nexcore.scheduler.core.internal;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import nexcore.scheduler.util.Util;

import com.ibatis.sqlmap.client.SqlMapClient;



/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 다중 서버 환경에서 중복되지 않은 ID 생성을 위해 DB 테이블을 이용한다. </li>
 * <li>작성일 : 2012. 10. 10.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class TableBaseIdGenerator {
	private String         idType;
	private SqlMapClient   sqlMapClient;
	private int            retryCount = 1000; // update 실패시 시도할 최대 retryCount. 기본값 : 1000

/*
 * Id 생성기는 주 트랜잭션과 분리된 별도의 트랜잭션으로 동작하도록 한다.
 * 그리고 autocommit 으로 동작시킨다.
 * 그렇게 하지 않을 경우 만일 한 트랜잭션에 여러 타입의 ID 를 생성하면 dead-lock 이 발생할 수 있으므로 위험하다.
 * 주 트랜잭션이 rollback 되면 결번이 발생하는 문제는 있지만, dead-lock 을 피하는 것이 우선이다.  
 */
	
	class Entry {
		String keyName;
		int    lastSeq;
		String lastModifyUser;
		String lastModifyTime;
	}
	
	public TableBaseIdGenerator(String idType, SqlMapClient sqlMapClient) {
		this.idType         = idType;
		this.sqlMapClient   = sqlMapClient;
	}
	
	public TableBaseIdGenerator(String idType, SqlMapClient sqlMapClient, int retryCount) {
		this.idType         = idType;
		this.sqlMapClient   = sqlMapClient;
		this.retryCount     = retryCount;
	}
	
	public String getIdType() {
		return idType;
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public int getRetryCount() {
		return retryCount;
	}
	
	/**
	 * 테이블에서 현재 SEQ 값 조회함
	 * @param key
	 * @return
	 * @throws SQLException
	 */
	private Entry select(String key) throws SQLException {
		Map sqlin = new HashMap();
		sqlin.put("idType",   idType);
		sqlin.put("keyName",  key);
		
		Map result = (Map)sqlMapClient.queryForObject("nbs.scheduler.selectLastSeq", sqlin);
		
		Entry entry = null;
		if (result != null) {
			entry = new Entry();
			entry.keyName         = (String)result.get("KEY_NAME");
			entry.lastSeq         = Integer.parseInt(String.valueOf(result.get("LAST_SEQ")));
			entry.lastModifyUser  = (String)result.get("LAST_MODIFY_USER");
			entry.lastModifyTime  = (String)result.get("LAST_MODIFY_TIME");
		}
		
		return entry;
	}

	/**
	 * 새로운 SEQ 값으로 UPDATE 함.
	 * 동시에 여러 스레드 혹은 여러 노드에서 채번을 시도할 경우 동시성 문제 해결을 위해
	 * UPDATE 시에 여러 값들은 조건으로 필터링하여 정확히 한번만 update 가 되도록 함.
	 * 
	 * @param key
	 * @param newLastSeq
	 * @param newLastModifyUser
	 * @param newLastModifyTime
	 * @param oldEntry
	 * @return true if success, false if fail
	 * @throws SQLException
	 */
	private boolean update(String key, int newLastSeq, String newLastModifyUser, String newLastModifyTime, Entry oldEntry) throws SQLException {
		Map sqlin = new HashMap();
		sqlin.put("idType",             idType);
		sqlin.put("keyName",            key);
		sqlin.put("newLastSeq",         newLastSeq);
		sqlin.put("newLastModifyUser",  newLastModifyUser);
		sqlin.put("newLastModifyTime",  newLastModifyTime);
		sqlin.put("oldLastSeq",         oldEntry.lastSeq);
		sqlin.put("oldLastModifyUser",  oldEntry.lastModifyUser);
		sqlin.put("oldLastModifyTime",  oldEntry.lastModifyTime);
		
		int result = sqlMapClient.update("nbs.scheduler.updateLastSeq", sqlin);
		
		return result > 0;
	}
	
	/**
	 * 해당 KEY 에 해당하는 ROW 가 없을 경우 INSERT 수행함.
	 * 
	 * INSERT 도중 다른 놈이 먼저 INSERT 할 경우 PK Violation 에러가 발생한다.
	 * 
	 * @param key
	 * @param lastSeq
	 * @param lastModifyUser
	 * @param lastModifyTime
	 * @return true  if success, false if pk violation
	 * @throws SQLException
	 */
	private boolean insert(String key, int lastSeq, String lastModifyUser, String lastModifyTime) throws SQLException {

		Map sqlin = new HashMap();
		sqlin.put("idType",          idType);
		sqlin.put("keyName",         key);
		sqlin.put("lastSeq",         lastSeq);
		sqlin.put("lastModifyUser",  lastModifyUser);
		sqlin.put("lastModifyTime",  lastModifyTime);
		try {
			int result = sqlMapClient.update("nbs.scheduler.insertLastSeq", sqlin);
			return result > 0;
		}catch(SQLException e) {
			Entry entry = select(key);
			if (entry != null) {
				// insert 실패한 후 select 를 해보니 이미 들어와 있는 경우, PK DUP 이다. 다른 놈이 먼저 INSERT 한 경우
				return false;
			}
			// PK DUP 이 아닌데 에러가 발생한 경우, 뭔가 DB 장애일 수 있다.
			throw e;
		}
	}
	
	/**
	 * 새로운 SEQ 하나를 생성한다.
	 * @param key
	 * @return
	 */
	public int getNextSeq(String key) throws SQLException {
		String lastModifyUser = System.getProperty("NEXCORE_ID")+"_"+Thread.currentThread().toString();
		String lastModifyTime = Util.getCurrentYYYYMMDDHHMMSSMS();
		
		int newSeq = 0;

		// update 실패로 재 획득을 시도할때에 최대 100번만 하고 그래도 안되면 에러낸다.
		for (int i=0; i<retryCount; i++) {
			Entry entry = select(key);
			if (entry == null) {
				if (insert(key, 1, lastModifyUser, lastModifyTime)) { // 처음 ID 획득시 insert 성공하면 그 값으로 리턴한다
					return 1;
				}
			}else {
				newSeq = entry.lastSeq + 1;
				
				// update 성공시 newSeq 값으로 리턴, 실패시 다시 한바퀴 돌린다.
				if (update(key, newSeq, lastModifyUser, lastModifyTime, entry)) {  
					return newSeq;
				}
			}
		}
		throw new RuntimeException("getNextSeq fail. ("+key+", try:"+retryCount+")");
	}
	
}

