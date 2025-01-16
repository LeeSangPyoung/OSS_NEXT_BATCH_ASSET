package nexcore.scheduler.diagram;

import java.util.LinkedList;
import java.util.List;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Flow Model 에서 Job 노드 하나를 나타내는 객체. </li>
 * <li>작성일 : 2012. 3. 6.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobNode {
	private String         id;                     // Job ID 또는 Job InstanceID
	private JobFlowModel   ownerModel;
	private boolean        isVirtual = false;      // 검색 결과에는 없지만, 선행 또는 trigger 로 지정된 Node 를 점선으로 표시해줌. virtual Node 는 점선으로 표시함.
	
	private List<JobNodeRelation>  preList   = new LinkedList<JobNodeRelation>(); // 선행/부모 Job 리스트
	private List<JobNodeRelation>  postList  = new LinkedList<JobNodeRelation>(); // 후행/자식 Job 리스트
	
	private JobNode        minPreParentNode; // 선행,부모 Job 중에 Job ID 기준으로 가장 빠른 놈

	/*
	 * x, y 는 격좌 상의 논리적인 위치를 의치하고
	 * coordX, coorY 는 그래픽으로 표현하기 위한 픽셀 단위의 위치를 의미함.
	 * 비율값을 가지고 * 연산으로 계산할 수 있지만, 매번 계산하는 부하를 줄이고자 한번 계산된 값을 보존함
	 */
	
	private int x = -1; // default value is -1. This means does not set. 
	private int y = -1; // default value is -1. This means does not set.
	
	private int coordX; // 변환된 그래픽 좌표 X.
	private int coordY; // 변환된 그래픽 좌표 Y.
	
	
	private boolean nowSettingY; // 지금 Y 좌표를 설정중인지? 무한 recursion 방지를 위해 flag 를 둠.
	
	public JobNode(String id, JobFlowModel model) {
		this.id         = id;
		this.ownerModel = model;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public JobFlowModel getOwnerModel() {
		return ownerModel;
	}
	
	public boolean isVirtual() {
		return isVirtual;
	}

	public void setVirtual(boolean isVirtual) {
		this.isVirtual = isVirtual;
	}

	public List<JobNodeRelation> getPreList() {
		return preList;
	}

	public List<JobNodeRelation> getPostList() {
		return postList;
	}

	/** 
	 * 나의 선행 Job 으로 추가
	 * @param job
	 */
	public void addToPre(JobNodeRelation relation) {
		preList.add(relation);
		if (minPreParentNode != null) {
			if (minPreParentNode.getId().compareTo(relation.getNodeFrom().getId()) > 0) {
				minPreParentNode = relation.getNodeFrom();
			}
		}else {
			minPreParentNode = relation.getNodeFrom();
		}
	}
	
	public void addToPost(JobNodeRelation relation) {
		postList.add(relation);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
		this.ownerModel.setMinMaxXAdjust(x);
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
		this.ownerModel.setMinMaxYAdjust(y);
	}

	/**
	 * Y 좌표 계산중 infinite recursion 을 방지하기 위해 지금 setting 중인지를 체크함 
	 * @return
	 */
	public boolean isNowSettingY() {
		return nowSettingY;
	}

	public void setNowSettingY(boolean nowSettingY) {
		this.nowSettingY = nowSettingY;
	}

	public JobNode getMinPreParentNode() {
		return minPreParentNode;
	}

	/**
	 * 그래피컬 X 좌표 설정
	 * @param coordX the coordX to set
	 */
	public void setCoordX(int coordX) {
		this.coordX = coordX;
	}
	
	/**
	 * 그래피컬 Y 좌표 설정
	 * @param coordY the coordY to set
	 */
	public void setCoordY(int coordY) {
		this.coordY = coordY;
	}
	
	/**
	 * 그래피컬 X,Y 좌표 설정
	 * @param coordX
	 * @param coordY
	 */
	public void setCoordXY(int coordX, int coordY) {
		this.coordX = coordX;
		this.coordY = coordY;
	}
	
	/**
	 * @return the coordX
	 */
	public int getCoordX() {
		return coordX;
	}
	
	/**
	 * @return the coordY
	 */
	public int getCoordY() {
		return coordY;
	}
	
	public int getInCount() {
		return preList.size();
	}
	
	public int getOutCount() {
		return postList.size();
	}
	
	public String toString() {
		return "["+id+":"+x+","+y+"]";
	}
}
