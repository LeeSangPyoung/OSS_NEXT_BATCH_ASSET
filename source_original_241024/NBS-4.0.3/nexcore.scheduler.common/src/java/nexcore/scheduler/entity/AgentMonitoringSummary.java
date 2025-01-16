package nexcore.scheduler.entity;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Agent 의 모니터링 정보를 담고 있는 객체</li>
 * <li>작성일 : 2011. 01. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class AgentMonitoringSummary implements Serializable {
	private static final long serialVersionUID = -8097332438888649234L;
	
	private String      agentId;
	private String      isAlive;                 // Agent와 연결이 가능한가? isAlive 메세지.
	private boolean     isClosed;                // Agent의 Job실행이 가능한 상태인가?
	private long        bootTime;                // boot time  
	private int         runningJobCount;         // 실행중 Job 수
	private int         threadCount;             // 실행중 스레드 수
	private Map         jvmMonitoringInfo;       // Agent의 JVM Heap 메모리 + 프로퍼티 등 기타 정보 정보. 
	private String      agentConnectionError;    // agent 의 정보 조회에 실패하면 여기에 에러메세지 담는다.

	public AgentMonitoringSummary() {
	}
	
	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	public String getAlive() {
		return isAlive;
	}
	public void setAlive(String isAlive) {
		this.isAlive = isAlive;
	}
	public boolean isClosed() {
		return isClosed;
	}
	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}
	public long getBootTime() {
		return bootTime;
	}
	public void setBootTime(long bootTime) {
		this.bootTime = bootTime;
	}
	public int getRunningJobCount() {
		return runningJobCount;
	}
	public void setRunningJobCount(int runningJobCount) {
		this.runningJobCount = runningJobCount;
	}
	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public Map getJvmMonitoringInfo() {
		return jvmMonitoringInfo;
	}
	public void setJvmMonitoringInfo(Map jvmMonInfo) {
		this.jvmMonitoringInfo = jvmMonInfo;
	}

	public String getAgentConnectionError() {
		return agentConnectionError;
	}

	public void setAgentConnectionError(String agentConnectionError) {
		this.agentConnectionError = agentConnectionError;
	}
	
	/* 
		memoryInfo 에 들어있는 메모리 정보 구조
		map.put("HEAP_INIT",          heapMem.getInit());
		map.put("HEAP_USED",          heapMem.getUsed());
		map.put("HEAP_COMMITTED",     heapMem.getCommitted());
		map.put("HEAP_MAX",           heapMem.getMax());
		map.put("NONHEAP_INIT",       nonHeapMem.getInit());
		map.put("NONHEAP_USED",       nonHeapMem.getUsed());
		map.put("NONHEAP_COMMITTED",  nonHeapMem.getCommitted());
		map.put("NONHEAP_MAX",        nonHeapMem.getMax());
	 */

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}

