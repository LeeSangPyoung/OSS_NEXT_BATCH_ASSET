package nexcore.scheduler.monitor.internal;

import java.util.ArrayList;
import java.util.List;

import nexcore.scheduler.entity.JobGroup;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 트리 모델을 위한 Node 객체 </li>
 * <li>작성일 : 2013. 1. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobGroupNode {
	private JobGroup value;
	private List<JobGroupNode>   childNodeList = new ArrayList();
	private JobGroupNode         nextSibling;
	
	public JobGroupNode(JobGroup value) {
		this.value  = value;
	}		
	
	public JobGroup getJobGroup() {
		return value;
	}
	
	public List<JobGroupNode> getChildNodeList() {
		return childNodeList;
	}
	
	public void setChildNodeList(List<JobGroupNode> childNodeList) {
		this.childNodeList = childNodeList;
	}

	public JobGroupNode getFirstChildNode() {
		if (childNodeList.size() == 0) {
			return null;
		}else {
			return childNodeList.get(0);
		}
	}

	public JobGroupNode getLastChildNode() {
		if (childNodeList.size() == 0) {
			return null;
		}else {
			return childNodeList.get(childNodeList.size() - 1); // 마지막
		}
	}

	public void addChildNode(JobGroupNode node) {
		this.childNodeList.add(node);
	}
	
	public JobGroupNode getNextSibling() {
		return nextSibling;
	}
	
	public void setNextSibling(JobGroupNode nextSibling) {
		this.nextSibling = nextSibling;
	}

	public String toString() {
		return "(JobGroupNode:"+value+")";
	}
	
}
