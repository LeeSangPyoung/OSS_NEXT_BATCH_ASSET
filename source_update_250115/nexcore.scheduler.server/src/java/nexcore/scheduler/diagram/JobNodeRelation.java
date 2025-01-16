package nexcore.scheduler.diagram;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 노드간 관계 (라인의 속성)를 나타냄. 선행/후행/parent/child</li>
 * <li>작성일 : 2012. 4. 20.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobNodeRelation {
	public static final int TYPE_PREPOST = 1;
	public static final int TYPE_TRIGGER = 2;
	
	private JobNode        nodeFrom;   /* 선행 또는 parent    */
	private JobNode        nodeTo;     /* 후행 또는 child     */
	private int            type;       /* 1:선후행, 2:trigger */
	
	public JobNodeRelation(JobNode from, JobNode to, int type) {
		this.nodeFrom = from;
		this.nodeTo   = to;
		this.type     = type;
	}

	public JobNode getNodeFrom() {
		return nodeFrom;
	}

	public void setNodeFrom(JobNode nodeFrom) {
		this.nodeFrom = nodeFrom;
	}

	public JobNode getNodeTo() {
		return nodeTo;
	}

	public void setNodeTo(JobNode nodeTo) {
		this.nodeTo = nodeTo;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
