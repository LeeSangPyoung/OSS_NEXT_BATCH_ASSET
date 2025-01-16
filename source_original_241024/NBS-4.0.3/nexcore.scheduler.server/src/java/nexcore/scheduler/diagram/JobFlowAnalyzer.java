package nexcore.scheduler.diagram;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.entity.PreJobCondition;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 9999 </li>
 * <li>설  명 : Job 흐름을 메모리에 구성하는 분석기 </li>
 * <li>작성일 : 2011. 4. 19.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobFlowAnalyzer {
	
	private JobFlowModel model = new JobFlowModel();
	
	public JobFlowAnalyzer() {
	}
	
	public JobFlowModel getModel() {
		return model;
	}

	/**
	 * JobDefinition 들의 관계 모델 분석함
	 * @param jobdefList
	 */
	public void doAnalyzeByJobDefinition(List<JobDefinition> jobdefList) {
		if (jobdefList.size() < 1) {
			return;
		}
		
		// Node 구성
		for (JobDefinition jobdef : jobdefList) {
			model.createJobNode(jobdef.getJobId());
		}
		
		for (JobDefinition jobdef : jobdefList) {
			JobNode currNode = model.getJobNode(jobdef.getJobId());
			
			// 선행 Job 연결
			for (PreJobCondition prejob : jobdef.getPreJobConditions()) {
				JobNode preJobNode = model.getJobNode(prejob.getPreJobId());
				if (preJobNode == null) {
					// 선행 Job 이 검색 대상에 없는 경우이므로 virtual node 생성
					preJobNode = model.createJobNode(prejob.getPreJobId());
					preJobNode.setVirtual(true);
				}
				JobNodeRelation relation = new JobNodeRelation(preJobNode, currNode, JobNodeRelation.TYPE_PREPOST);
				currNode.addToPre(relation);
				preJobNode.addToPost(relation);
			}
			
			// Trigger Job 연결.
			for (PostJobTrigger trigger : jobdef.getTriggerList()) {
				JobNode triggerJobNode = model.getJobNode(trigger.getTriggerJobId());
				if (triggerJobNode == null) {
					triggerJobNode = model.createJobNode(trigger.getTriggerJobId());
					triggerJobNode.setVirtual(true);
				}
				JobNodeRelation relation = new JobNodeRelation(currNode, triggerJobNode, JobNodeRelation.TYPE_TRIGGER);
				currNode.addToPost(relation);
				triggerJobNode.addToPre(relation);
			}
		}

		// Y 좌표 계산.
		doArrangeY();
		
		// X 좌표 계산.
		doArrangeX();

	}
	
	/**
	 * JobInstance 들의 관계 모델 분석함
	 * @param jobinsList
	 */
	public void doAnalyzeByJobInstance(List<JobInstance> jobinsList) {
		if (jobinsList.size() < 1) {
			return;
		}
		
		// Pool 구성
		for (JobInstance jobins : jobinsList) {
			// JobNode 는 Job Instance 당 하나씩 생성되고
			JobNode node = model.createJobNode(jobins.getJobInstanceId());
			
			// JobId + ProcDate 와 Job Node 리스트의 매핑을 따로 관리한다. 
			model.addJobInstanceInfo(jobins.getProcDate(), jobins.getJobId(), node);
		}
		
		// 선후행/Trigger 연결
		for (JobInstance jobins : jobinsList) {
			JobNode currNode = model.getJobNode(jobins.getJobInstanceId());

			// 선후행 연결. 선행 Job ID + PROC_DATE 에 해당하는 Instance 들을 찾아서 연결한다.
			for (PreJobCondition prejob : jobins.getPreJobConditions()) {
				List<JobNode> preJobNodeList = model.getJobNodeByInstanceInfo(prejob.getPreJobId(), jobins.getProcDate());
				if (preJobNodeList != null) {
					for (JobNode preJobNode : preJobNodeList) {
						JobNodeRelation relation = new JobNodeRelation(preJobNode, currNode, JobNodeRelation.TYPE_PREPOST);
						currNode.addToPre(relation);
						preJobNode.addToPost(relation);
					}
				}				
			}
			
			// Trigger 연결. 
			// activator 값을 가지고 이 Job Instance 가 trigger 로 activation 됐는지 판단한다.
			if (Util.nvl(jobins.getActivator()).length() > (8+4+6)) {
				// 8 + 4 + 6  = PROC_DATE(8) + INS_SEQ(4) + EXE_SEQ(6)
				// jobins.getActivator().length() > (8+4+6) ==> jobins.getActivator() 가 JobExecutionID 일 가능성이 높음. parent 검색한다.
				
				String  jobInstanceId = jobins.getActivator().substring(0, jobins.getActivator().length() - 6);
				
				JobNode parentNode    = model.getJobNode(jobInstanceId);
				if (parentNode != null) { // Parent Node 가 존재한다면 그 parent 와 관계설정함.
					JobNodeRelation relation = new JobNodeRelation(parentNode, currNode, JobNodeRelation.TYPE_TRIGGER);
					parentNode.addToPost(relation);
					currNode.addToPre(relation);
				}
			}
		}

		// Y 좌표 계산.
		doArrangeY();
		
		// X 좌표 계산.
		doArrangeX();

	}

	/**
	 * 선행/부모 Job 들의 Y 값의 최대값 구함. 
	 * 나의 Y 의 위치 = 부모의 최대값의 Y + 1  
	 * @param node
	 * @return
	 */
	private int getPreNodeMaxY(JobNode node) {
		int max = 0;
		for (JobNodeRelation preNodeRelation : node.getPreList()) {
			JobNode preNode = preNodeRelation.getNodeFrom();
			if (preNode.getY() == -1) { // 아직 Y 좌표 미설정. recursion 함
				setLocationY(preNode);
				if (preNode.getY() == -1) { // recursion 을 했는데도 여전히 -1 인것은 무한 recursion 으로 인해 skip 된 것임. 그냥 pass 함
					continue;
				}
			}
			max = Math.max(preNode.getY(), max);
		}

		return max;
	}
	
	/**
	 * Y 위치를 설정함.
	 * 상위(선행,부모) 의 Y + 1 로 나의 Y를 설정함
	 * @param node
	 */
	private void setLocationY(JobNode node) {
		if (node.isNowSettingY() || node.getY() > -1) {
			/*
			 * 1) 이미 Y 설정 중에 다시 이 지점으로 온 것이므로 무한 recursion 이 발생하게 된다.
			 *    이런 경우는 이 node 는 skip 하고 나머지 node 들로 max 값을 갖게 한다.
			 * 
			 * 2) 이미 Y 설정이 된 경우는 다시 하지 말고 리턴한다. 
			 */
			return;
		}else {
			node.setNowSettingY(true); // To avoid infinite recursion. use flag
			node.setY(getPreNodeMaxY(node)+1);
			node.setNowSettingY(false);
		}
	}
	
	/**
	 * (메인)
	 * Y 좌표 계산하여 위치시킴 
	 */
	private void doArrangeY() {
		for (JobNode node : model.getAllJobNodeList()) {
			setLocationY(node);
			model.getLevel(node.getY()).add(node); // level list 에 넣는다.
		}
	}

	// ===============================================================================
	
	/**
	 * Level 리스트 중에 가장 큰 것들의 인덱스 값을 구함
	 * @return 가장 노드가 많은 Level 의 index 값. (1부터)
	 */
	private int getLargestLevelY() {
		int i        = 0;
		int max      = 0;
		int maxIndex = 0;
		
		for (List<JobNode> list : model.getAllLevels()) {
			i++;
			if (list.size() > max) {
				max = list.size();
				maxIndex = i; 
			}
		}
		return maxIndex;
	}

	/**
	 * 한 레벨의 JobNode를 Job ID로 소트함.
	 * @param list
	 */
	private void sortTheLevel(List<JobNode> list) {
		Collections.sort(list, new Comparator<JobNode>() {
			public int compare(JobNode o1, JobNode o2) {
				/* 
				 * 선행/부모 Job이 동일한 것들끼리 먼저 묶고
				 * 그 안에서는 Job ID 로 sort 한다.
				 */
				if (o1.getMinPreParentNode() != null) {
					if (o2.getMinPreParentNode() != null) {
						int compare = o1.getMinPreParentNode().getId().compareTo(o2.getMinPreParentNode().getId());
						return compare == 0 ? o1.getId().compareTo(o2.getId()) : compare; // 선행이 동일할 경우 jobid 로 sort
					}else {
						return 1; // 선행이 없는 것들은 오른쪽으로 보냄.
					}
				}else {
					if (o2.getMinPreParentNode() == null) {
						return o1.getId().compareTo(o2.getId()); // 둘다 선행이 없으면, jobid 로 sort
					}else {
						return -1; // 선행이 없는 것들은 오른쪽으로 보냄.
					}
				}
			}
		});
	}

	/**
	 * 기준 Level (Largest) 의 X 좌표를 설정함
	 */
	private void setLocationXBaseLevel(List<JobNode> level) {
		int i=level.size(); // 최값을 0으로 하지 않고 전체 shift 를 고려하여 화면 중앙으로 한다.
		for (JobNode node : level) { 
			node.setX(++i); // 이미 sort 가 되었으므로 기준 leve은 1부터 차례로 X 좌표를 매긴다.
		}
	}
	
	
	/**
	 * 설정된 X 좌표를 기준으로 정렬 후 중복된 위치의 X 좌표를 중복되지 않도록 펼친다.
	 * @param level
	 * @return x 좌표가 설정되지 않아서 건너뛴 노드의 개수
	 */
	private int sortAndSpreadOneLevel(List<JobNode> level) {
		// X 좌표를 가지고 sort 한다.
		Collections.sort(level, new Comparator<JobNode>() {
			public int compare(JobNode o1, JobNode o2) {
				int ret = o1.getX() - o2.getX();
				return ret == 0 ? o1.getId().compareTo(o2.getId()) : ret;
			}
		});
		
		// x 가 -1 이라서 pass 된 개수
		int xNotYetCount = 0;
		
		// X 좌표가 중복된 것들을 겹치지 않게 펴고, X 좌표의 적절한 위치를 설정한다.
		// X 좌표가 중복된 것을 블럭으로 보고, 먼저 블럭을 찾아서 begin, end 로 위치를 정해 펼치는 작업을 한다. 
		int levelSize = level.size();
		int currMaxPosX = 1;
		for (int i=0; i<levelSize; ) {
			if (level.get(i).getX() == -1) { /* x 좌표가 아직 설정되지 않은 것들은 건너뛴다. */
				i++;
				xNotYetCount++;
				continue;
			}
			
			int blockBeginIdx = i; // 중복시작 인덱스
			int blockEndIdx   = levelSize-1; // 중복끝   인덱스
			
			for (int j=i+1; j<levelSize; j++) {
				if (level.get(blockBeginIdx).getX() != level.get(j).getX()) {
					blockEndIdx = j-1;
					break; // X 좌표가 다르므로 group 종료
				}
			}

			// 블럭 하나를 처리한다. 블럭 시작 노드의 X 좌표를 계산한 후, 그 블럭의 X 좌표를 ++ 하며 set 한다.
			int blockSize = (blockEndIdx - blockBeginIdx + 1);
			currMaxPosX   = Math.max(level.get(blockBeginIdx).getX() - blockSize / 2, currMaxPosX);

			for (int k=blockBeginIdx; k<=blockEndIdx; k++) {
				level.get(k).setX(currMaxPosX++);
			}

			i = blockEndIdx + 1;
		}
		
		return xNotYetCount;
	}
	
	/**
	 * X 의 위치를 설정함
	 * 기준 level 의 위쪽 level 들 처리
	 * 후행/child 들의 X 좌표를 기준으로 중간에 위치시킴
	 * @param node
	 * @return x 좌표가 설정되지 않아서 건너뛴 노드의 개수
	 */
	private int setLocationX1(List<JobNode> level) {
		int sumOfPostNodeX   = 0;
		int countOfPostNode  = 0;
		for (JobNode node : level) { 
			sumOfPostNodeX   = 0;
			countOfPostNode  = 0;
			for (JobNodeRelation postNodeRel : node.getPostList()) {
				JobNode postNode = postNodeRel.getNodeTo();
				if (postNode.getX() > 0) { /* 하위 노드중 좌표가 설정된 것들만 대상으로 함 */
					sumOfPostNodeX += postNode.getX();
					countOfPostNode ++;
				}
			}
			
			// 중간 값으로 X 좌표 설정
			if (countOfPostNode > 0) {
				node.setX(sumOfPostNodeX / countOfPostNode);
			}
		}

		return sortAndSpreadOneLevel(level);
	}
	
	/**
	 * X 의 위치를 설정함
	 * 기준 level 의 아래쪽 level 들 처리
	 * 선행/parent 들의 X 좌표를 기준으로 중간에 위치시킴
	 * @param node
	 * @return x 좌표가 설정되지 않아서 건너뛴 노드의 개수
	 */
	private int setLocationX2(List<JobNode> level) {
		int sumOfPreNodeX   = 0;
		int countOfPreNode  = 0;
		for (JobNode node : level) { 
			sumOfPreNodeX   = 0;
			countOfPreNode  = 0;
			for (JobNodeRelation beforeNodeRel : node.getPreList()) {
				JobNode beforeNode = beforeNodeRel.getNodeFrom();
				if (beforeNode.getX() > 0) {
					sumOfPreNodeX += beforeNode.getX();
					countOfPreNode ++;
				}
			}

			// 중간 값으로 X 좌표 설정
			if (countOfPreNode > 0) {
				node.setX(sumOfPreNodeX / countOfPreNode);
			}
		}

		return sortAndSpreadOneLevel(level);
	}

	/**
	 * X 좌표 설정 완료후
	 * child, post Node 의 위치에 따라 JobNode 의 child, post list를 sort함.
	 * child, post 로 나가는 선이 교차되어 보이지 않도록 하기 위함.
	 * @param list
	 */
	private void sortPostList() {
		for (JobNode node : model.getAllJobNodeList()) {
			if (node.getPostList().size() > 1) {
				Collections.sort(node.getPostList(), new Comparator<JobNodeRelation>() {
					public int compare(JobNodeRelation o1, JobNodeRelation o2) {
						return o1.getNodeTo().getX() - o2.getNodeTo().getX();
					}
				});
			}
		}
	}

	/**
	 * (메인)
	 * X 좌표 계산하여 위치시킴 
	 */
	private void doArrangeX() {
		
		// Largest Level 찾아 기준 Level로 함.
		int largestLevelIdx = getLargestLevelY();
		List<JobNode> largestLevel = model.getLevel(largestLevelIdx);
		
		// largest level 을 찾아 JOB ID 로 sort.
		sortTheLevel(largestLevel );
		
		// 기준 level 의 Node 들의 X 좌표 설정
		setLocationXBaseLevel(largestLevel);
		
		int notYetCount;   // x 좌표 미설정 노드 개수
		int loopCount = 0;
		do {
			notYetCount = 0;
			// 기준 level 의 위쪽 것들은 후행/child 들의 X 좌표를 기준으로 중간에 위치시킴
			for (int i=largestLevelIdx-1; i>=1; i--) {
				notYetCount += setLocationX1(model.getLevel(i));
			}
	
			int levelSize = model.getAllLevels().size();
			// 기준 level 의 아래쪽 것들은 선행/parent 들의 X 좌표를 기준으로 중간에 위치시킴
			for (int i=largestLevelIdx+1; i<=levelSize; i++) {
				notYetCount += setLocationX2(model.getLevel(i));
			}
			if (++loopCount > 5 && notYetCount > 0) {
				/*
				 * 5 회 이상 반복되면 무한루프의 위험이 있으므로 여기서 강제 처리한다.
				 * 강제로 X 좌표는 제일 왼쪽으로 붙인 후 한바퀴 더 돌린다.
				 */
				for (JobNode node : model.getAllJobNodeList()) {
					if (node.getX() == -1) {
						node.setX(model.getMinX());
					}
				}
			}
		}while(notYetCount > 0);
		
		// 왼쪽에 불필요한 공백을 잘라낸다.
		int modelMinX = model.getMinX();
		int toLeft    = Math.max(modelMinX - 1, 0); // 왼쪽으로 이동할 길이. 

		if (toLeft > 0) {
			for (JobNode node : model.getAllJobNodeList()) {
//System.out.print(node+" ");
				node.setX(node.getX() - toLeft);
//System.out.println(" -> "+node);
			}
			model.setMaxX(model.getMaxX()-toLeft);
		}
		
		// child, post 라인이 교차되는 것을 방지하기 위해 보정함
		sortPostList();
	}
}


