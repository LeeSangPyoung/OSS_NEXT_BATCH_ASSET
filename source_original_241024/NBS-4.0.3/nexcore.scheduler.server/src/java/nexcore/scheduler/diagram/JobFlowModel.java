package nexcore.scheduler.diagram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Flow 모델 메인.</li>
 * <li>작성일 : 2012. 3. 6.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobFlowModel {
	
	// Node 전체 Pool
	private Map<String, JobNode> jobNodePool = new HashMap<String, JobNode>();
	
	// List of Level(Layer). Level is List of JobNode
	private List<List<JobNode>>  levelList   = new ArrayList<List<JobNode>>();  

	private int maxX = Integer.MIN_VALUE;
	private int maxY = Integer.MIN_VALUE;

	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;

	// Job Instance 분석 모델에 필요한 데이타.
	private Map<String, List<JobNode>> jobInsPool  = new HashMap<String, List<JobNode>>(); /* PROC_DATE+JOBID, List of JobNode */

	public JobFlowModel() {
	}
	
	JobNode createJobNode(String id) {
		return createJobNode(id, false);
	}

	JobNode createJobNode(String id, boolean virtual) {
		JobNode node = new JobNode(id, this);
		node.setVirtual(virtual);
		jobNodePool.put(id, node);
		return node;
	}

	void addJobInstanceInfo(String procDate, String jobId, JobNode node) {
		String key = jobId + procDate;
		List<JobNode> jobNodeList = jobInsPool.get(key);
		if (jobNodeList == null) {
			jobNodeList = new LinkedList<JobNode>();
			jobInsPool.put(key, jobNodeList);
		}
		jobNodeList.add(node);
	}
	
	public JobNode getJobNode(String id) {
		return jobNodePool.get(id);
	}
	
	public List<JobNode> getJobNodeByInstanceInfo(String jobId, String procDate) {
		return jobInsPool.get(jobId+procDate);
	}
	
	public List<JobNode> getAllJobNodeList() {
		return new ArrayList<JobNode>(jobNodePool.values());
	}
	
	public List<JobNode> getLevel(int index) {
		for (; levelList.size() < index; levelList.add(new ArrayList())); // 각 Level 의 List 초기화
		
		return levelList.get(index-1);
	}
	
	public List<List<JobNode>> getAllLevels() {
		return levelList;
	}

	public int getMaxX() {
		return maxX;
	}
	
	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}
	
	public int getMinX() {
		return minX;
	}

	public void setMinX(int minX) {
		this.minX = minX;
	}

	public int getMinY() {
		return minY;
	}

	public void setMinY(int minY) {
		this.minY = minY;
	}

	/**
	 * x 
	 * @param x
	 */
	void setMinMaxXAdjust(int x) {
		if (x > -1) {
			this.maxX = Math.max(x, this.maxX);
			this.minX = Math.min(x, this.minX);
		}
	}

	void setMinMaxYAdjust(int y) {
		if (y > -1) {
			this.maxY = Math.max(y, this.maxY);
			this.minY = Math.min(y, this.minY);
		}
	}
}
