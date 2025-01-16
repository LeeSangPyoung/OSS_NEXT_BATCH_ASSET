package nexcore.scheduler.core.internal;

import nexcore.scheduler.core.IJobInstanceIdMaker;
import nexcore.scheduler.exception.SchedulerException;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 테이블 기반 Job Instance Id 생성기. JOBID + PROC_DATE(YYYYMMDD) + SEQ(4). 멀티노드 환경에서 중복 방지를 위해 테이블을 이용한다.</li>
 * <li>작성일 : 2012. 10. 11.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class TableBaseJobInstanceIdMaker implements IJobInstanceIdMaker {
	private SqlMapClient            sqlMapClient;
	private int                     retryCount;

	private TableBaseIdGenerator    tableBaseIdGenerator;
	
	public void init() {
		if (retryCount==0) {
			tableBaseIdGenerator = new TableBaseIdGenerator("JI", sqlMapClient);
		}else {
			tableBaseIdGenerator = new TableBaseIdGenerator("JI", sqlMapClient, retryCount);
		}
	}
	
	public void destroy() {
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}
	
	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public String makeJobInstanceId(String jobId, String procDate) {
		try {
			int newSeq = tableBaseIdGenerator.getNextSeq(jobId+procDate);
			if (newSeq > 9999) {
				throw new SchedulerException("main.jobins.maxid.exceed.error", jobId, procDate);
			}
			return jobId + procDate + String.format("%04d", newSeq);
			
		}catch(Exception e) {
			throw new SchedulerException("main.jobins.maxid.error", e, jobId, procDate);
		}
	}

	public String getMonitoringString() {
		return null; // Memory 기반 구현체만 메모리 정보를 로깅하고, 테이블 기반인 것은 로깅필요없다.
	}
}

