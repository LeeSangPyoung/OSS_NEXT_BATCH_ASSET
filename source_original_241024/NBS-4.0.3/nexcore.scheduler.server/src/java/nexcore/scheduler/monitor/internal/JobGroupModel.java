/**
 * 
 */
package nexcore.scheduler.monitor.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexcore.scheduler.entity.JobGroup;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : tree 구조를 표현하는 데이타모델 </li>
 * <li>작성일 : 2013. 1. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class JobGroupModel {
	/* Node 전체 Pool     */
	private Map<String, JobGroupNode> pool = new HashMap<String, JobGroupNode>();

	/* 최상위 그룹 리스트 */
	private List<JobGroupNode> baseNodeList = new ArrayList<JobGroupNode>();

	public static JobGroupModel createModel(List entryList) {
		JobGroupModel model = new JobGroupModel();
		
		// 먼저 Pool 에 넣는다.
		model.addToModel((List<JobGroup>)entryList);
		
		return model;
	}
	
	public void addToModel(List<JobGroup> entryList) {
		for (JobGroup entry : entryList) {
			JobGroupNode node = new JobGroupNode(entry);
			pool.put(entry.getId(), node);
		}

		for (JobGroup entry : entryList) {
			JobGroupNode node = pool.get(entry.getId());
			if ("ROOT".equals(entry.getParentId()) || entry.getParentId() == null) {
				baseNodeList.add(pool.get(entry.getId()));
			}else {
				JobGroupNode parent = pool.get(entry.getParentId());
				if (parent == null) {
					baseNodeList.add(node); // 부모가 없으면 base 에 붙인다.
				}else {
					JobGroupNode elderBrother = parent.getLastChildNode();
					parent.addChildNode(node);
					if (elderBrother != null) {
						elderBrother.setNextSibling(node);
					}
				}
			}
		}
	}
	
	public List<JobGroupNode> getBaseNodeList() {
		return baseNodeList;
	}
	
	public List<JobGroup> getChildNodeList(String id) {
		List<JobGroup> list = new ArrayList();
		
		JobGroupNode node = pool.get(id);
		for (JobGroupNode childNode : node.getChildNodeList()) {
			list.add(childNode.getJobGroup());
		}
		return list;
	}

	public List<JobGroup> getChildNodeListRecursively(String id) {
		List<JobGroup> list = new ArrayList();
		
		JobGroupNode node = pool.get(id);
		for (JobGroupNode childNode : node.getChildNodeList()) {
			list.add(childNode.getJobGroup());
			list.addAll(getChildNodeList(childNode.getJobGroup().getId()));
		}
		
		return list;
	}
	
	/**
	 * depth-first 순서로 tree를 탐색한다.
	 * JobGroup 의 depth 가 채워진다.
	 * @return depth-first 로 traverser 된 리스트
	 */
	public List<JobGroup> traverseDepthFirst() {
		List<JobGroup> returnList = new ArrayList(pool.size());
		
		int depth = 0;
		for (JobGroupNode node : baseNodeList) {
			node.getJobGroup().setDepth(depth);
			returnList.add(node.getJobGroup());
			_traverseDepthFirst(returnList, node, depth);
		}
		return returnList;
	}
	
	private void _traverseDepthFirst(List<JobGroup> returnList, JobGroupNode parent, int parentDepth) {
		List<JobGroupNode> childNodeList = parent.getChildNodeList();
		if (childNodeList.size() == 0) {
			return;
		}
		for (JobGroupNode child : childNodeList) {
			child.getJobGroup().setDepth(parentDepth + 1);
			returnList.add(child.getJobGroup());
			_traverseDepthFirst(returnList, child, parentDepth+1);
		}
	}
}


