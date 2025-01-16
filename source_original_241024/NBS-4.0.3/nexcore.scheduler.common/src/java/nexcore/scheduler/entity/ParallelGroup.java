package nexcore.scheduler.entity;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 병렬 그룹 설정 및 현재 실행 개수 </li>
 * <li>작성일 : 2011. 1. 28.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ParallelGroup implements Serializable {
	private static final long serialVersionUID = 4329279550840757574L;
	
	private String    groupName;
	private String    groupDesc;
	private int       maxLimit;
	private int       currentRunning;
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getGroupDesc() {
		return groupDesc;
	}
	public void setGroupDesc(String groupDesc) {
		this.groupDesc = groupDesc;
	}
	public int getMaxLimit() {
		return maxLimit;
	}
	public void setMaxLimit(int maxLimit) {
		this.maxLimit = maxLimit;
	}
	public int getCurrentRunning() {
		return currentRunning;
	}
	public void setCurrentRunning(int currentRunning) {
		this.currentRunning = currentRunning;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
