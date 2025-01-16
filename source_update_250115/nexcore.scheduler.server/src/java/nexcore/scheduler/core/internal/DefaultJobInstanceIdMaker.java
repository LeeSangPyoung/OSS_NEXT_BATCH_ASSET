package nexcore.scheduler.core.internal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import nexcore.scheduler.core.IJobInstanceIdMaker;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.Util;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 기본 Job Instance Id 생성기. JOBID + PROC_DATE(YYYYMMDD) + SEQ(4). </li>
 * <li>작성일 : 2010. 5. 13.</li>
 * <li>변경일 : 2011. 1. 08.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

/*
 * DB로부터 최종 JobInstance 번호를 읽어와 1을 증가시켜 리턴하며 메모리에 로드하여 관리한다.
 * 
 * Map<String:jobId+YYYYMMDD, AtomicInteger:lastSeq>
 * 
 * 이렇게 메모리에서 관리하는 이유은 채번후 Insert commit 되기 전에 다른 세션 (예, 온디맨드배치, 수동인스턴스 생성) 에서
 * 채번을 할 경우 동일한 번호가 생성 될 수 있다.
 * 
 * 이런 문제를 방지하기 위해 메모리에서 중앙 관리한다. 
 */
public class DefaultJobInstanceIdMaker implements IJobInstanceIdMaker {
	private int                         cacheSize = 10000; // 담아둘 최대 메모리 크기. 이게 다 차면 오랜된것 부터 삭제한다.
	private SqlMapClient                sqlMapClient;
	
	private Map<String, AtomicInteger>  lastSeqNumberMap;  // <String:jobId+YYYYMMDD, AtomicInteger:lastSeq>

	public void init() {
		lastSeqNumberMap = new LinkedHashMap<String, AtomicInteger>() {
			private static final long serialVersionUID = 1L; // COMPILE WARN 안보이게 하기 위해
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

	public String makeJobInstanceId(String jobId, String procDate) {
		int    newSeq = 0;
		String key = jobId + procDate;
		AtomicInteger lastSeq = lastSeqNumberMap.get(key); // LinedHashMap의 insert-ordering 이므로 여기는 synchronized 걸 필요없다. 
		if (lastSeq == null) {
			synchronized (lastSeqNumberMap) {
				lastSeq = lastSeqNumberMap.get(key);
				if (lastSeq == null) {
					lastSeq = new AtomicInteger( getLastJobInstanceIdSeq(jobId, procDate) );
					
					lastSeqNumberMap.put(key, lastSeq);
				}
			}
		}
		newSeq = lastSeq.addAndGet(1);
		String newJiid = jobId + procDate + String.format("%04d", newSeq);
		return newJiid;
	}

	/**
	 * NBS_JOB_INS 테이블로부터 최종 JOB Instance Id 의 최종 일련번호를 읽는다.
	 * @param jobId
	 * @param procDate
	 * @return
	 */
	private int getLastJobInstanceIdSeq(String jobId, String procDate) {
		try {
			Map param = new HashMap();
			param.put("jobId",    jobId);
			param.put("procDate", procDate);
			
			String maxJobInstanceId = (String)sqlMapClient.queryForObject("nbs.scheduler.selectLastJobInstanceId", param);
			
			if (Util.isBlank(maxJobInstanceId)) {
				return 0;
			}else {
				return Integer.parseInt(maxJobInstanceId.substring(jobId.length() + procDate.length()));
			}
			
		}catch (Exception e) {
			throw new SchedulerException("main.jobins.maxid.error", e, jobId, procDate);
		}
	}
	
	public String getMonitoringString() {
		return "JobInsIdMaker cache    : "+lastSeqNumberMap.size();
	}
}

