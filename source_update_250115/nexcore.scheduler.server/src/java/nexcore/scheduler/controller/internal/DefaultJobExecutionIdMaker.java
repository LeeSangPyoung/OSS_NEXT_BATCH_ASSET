package nexcore.scheduler.controller.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import nexcore.scheduler.controller.IJobExecutionIdMaker;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.Util;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 기본 Job Execution Id 생성기. JOB INSTANCE ID + SEQ(6). </li>
 * <li>작성일 : 2010. 5. 13.</li>
 * <li>변경일 : 2011. 1. 08.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

/*
 * DB로부터 최종 Job Execution 번호를 읽어와 1을 증가시켜 리턴하며 메모리에 로드하여 관리한다.
 * 
 * Map<String:jobInstanceId, AtomicInteger:lastSeq>
 * 
 */
public class DefaultJobExecutionIdMaker implements IJobExecutionIdMaker {
	private int                         cacheSize = 10000; // 담아둘 최대 메모리 크기. 이게 다 차면 오랜된 것 부터 삭제한다.
	private SqlMapClient                sqlMapClient;
	
	private Map<String, AtomicInteger>  lastSeqNumberMap;  // <String:jobId+YYYYMMDD, AtomicInteger:lastSeq>

	public void init() {
		lastSeqNumberMap = new LinkedHashMap<String, AtomicInteger>() {
			private static final long serialVersionUID = 1L;
			protected boolean removeEldestEntry(java.util.Map.Entry<String, AtomicInteger> eldest) {
				return size() > cacheSize;
			}
		};
	}
	
	public void destroy() {
	}

	public int getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public String makeJobExecutionId(String jobInstanceId) {
		int    newSeq = 0;
		String key = jobInstanceId;
		AtomicInteger lastSeq = lastSeqNumberMap.get(key); // LinedHashMap의 insert-ordering 이므로 여기는 synchronized 걸 필요없다. 
		if (lastSeq == null) {
			synchronized (lastSeqNumberMap) {
				lastSeq = lastSeqNumberMap.get(key);
				if (lastSeq == null) {
					lastSeq = new AtomicInteger( getLastJobExecutionIdSeq(jobInstanceId) );
					
					lastSeqNumberMap.put(key, lastSeq);
				}
			}
		}
		newSeq = lastSeq.addAndGet(1);
		String newJiid = jobInstanceId + String.format("%06d", newSeq);
		return newJiid;
	}

	/**
	 * NBS_JOB_EXE 테이블로부터 최종 Job Execution Id 의 최종 일련번호를 읽는다.
	 * @param jobId
	 * @param procDate
	 * @return
	 */
	private int getLastJobExecutionIdSeq(String jobInstanceId) {
		try {
			// NBS_JOB_INS 테이블 LAST_JOB_EXE_ID 가 최종정보. 처음이라 이 값이 NULL인 경우는 EXE 테이블도 
			String maxJobExecutionId = (String)sqlMapClient.queryForObject("nbs.scheduler.selectLastJobExeIdOfJobInstance", jobInstanceId);
			if (Util.isBlank(maxJobExecutionId)) {
				maxJobExecutionId = (String)sqlMapClient.queryForObject("nbs.controller.selectLastExecutionId", jobInstanceId);
			}
			
			if (Util.isBlank(maxJobExecutionId)) {
				return 0;
			}else {
				return Integer.parseInt(maxJobExecutionId.substring(maxJobExecutionId.length()-6));
			}
			
		}catch (Exception e) {
			throw new SchedulerException("main.get.max.jobexeid.error", e, jobInstanceId);
		}
	}
	
	public String getMonitoringString() {
		return "JobExeIdMaker cache    : "+lastSeqNumberMap.size();
	}
}

