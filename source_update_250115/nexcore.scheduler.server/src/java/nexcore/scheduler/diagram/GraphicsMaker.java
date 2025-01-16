package nexcore.scheduler.diagram;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : JobFlowModel, JobDefinitions 정보를 이용하여 svg, vml 태그를 print 함</li>
 * <li>작성일 : 2012. 3. 14.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public abstract class GraphicsMaker {
	protected Writer              out;
	
	protected int                 nodeWidth  = 200;   // Job Node 의 넓이
	protected int                 nodeHeight = 80;    // Job Node 의 높이
	
	protected int                 nodeWidthSpace  = 40;    // Job Node 간 가로 간격
	protected int                 nodeHeightSpace = 60;    // Job Node 간 높이 간격

	protected int                 innerRectWidth  ; // node box 안에 Job ID box, Desc box 등과 같은 내부 박스 크기
	protected int                 innerRectHeight ;
	
	protected int                 totalWidth;
	protected int                 totalHeight;
	
	public static GraphicsMaker getInstance(String type, Writer out) {
		if ("SVG".equalsIgnoreCase(type)) {
			return new GraphicsMakerSVGImpl(out);
		}else if ("VML".equalsIgnoreCase(type)) {
			return new GraphicsMakerVMLImpl(out);
		}
		return null;
	}
	
	public int getNodeWidth() {
		return nodeWidth;
	}

	public void setNodeWidth(int nodeWidth) {
		this.nodeWidth = nodeWidth;
	}

	public int getNodeHeight() {
		return nodeHeight;
	}

	public void setNodeHeight(int nodeHeight) {
		this.nodeHeight = nodeHeight;
	}
	
	public int getNodeWidthSpace() {
		return nodeWidthSpace;
	}

	public void setNodeWidthSpace(int nodeWidthSpace) {
		this.nodeWidthSpace = nodeWidthSpace;
	}

	public int getNodeHeightSpace() {
		return nodeHeightSpace;
	}

	public void setNodeHeightSpace(int nodeHeightSpace) {
		this.nodeHeightSpace = nodeHeightSpace;
	}

	public int getTotalWidth() {
		return totalWidth;
	}

	public void setTotalWidth(int totalWidth) {
		this.totalWidth = totalWidth;
	}

	public int getTotalHeight() {
		return totalHeight;
	}

	public void setTotalHeight(int totalHeight) {
		this.totalHeight = totalHeight;
	}

	/**
	 * X 좌표 기준 바로 옆인지?
	 * @param node1
	 * @param node2
	 * @return
	 */
	protected boolean isNeighborByX(JobNode node1, JobNode node2) {
		return Math.abs(node1.getX() - node2.getX()) == 1; 
	}

	/**
	 * Y 좌표 기준 바로 옆인지?
	 * @param node1
	 * @param node2
	 * @return
	 */
	protected boolean isNeighborByY(JobNode node1, JobNode node2) {
		return Math.abs(node1.getY() - node2.getY()) == 1; 
	}

	/**
	 * 진행률 Percentage
	 * @param progress
	 * @return
	 */
	protected String toProgressText(long[] progress) {
		if (progress == null) {
			return "N/A";
		}else if (progress[0] == 0) {
			return progress[1] + " ";
        }else {
        	return String.valueOf(progress[1] * 100 / progress[0]) +"% ("+progress[1]+"/"+progress[0]+")";
		}
	}
	
	/**
	 * 진행률 Precentage
	 * @param progress
	 * @return
	 */
	protected int getProgressPrecentage(long[] progress) {
		if (progress == null) {
			return -1;
		}else if (progress[0] == 0) {
            return -1;
        }else {
			return (int)(progress[1] * 100 / progress[0]);
		}
	}

	/**
	 * 
	 * @param startNode
	 * @param endNode
	 * @param outSeq out 순번
	 * @param inSeq int 순번
	 * @param isDash true if trigger, false otherwise
	 * @throws IOException
	 */
	private void drawLine(JobNode startNode, JobNode endNode, int outSeq, int inSeq, boolean isDash) throws IOException {
		
		int startX = startNode.getCoordX() + nodeWidth / (startNode.getOutCount()+1) * outSeq;
		int startY = startNode.getCoordY() + nodeHeight;
		int endX   = endNode.getCoordX()   + nodeWidth / (endNode.getInCount()+1) * inSeq;
		int endY   = endNode.getCoordY();
		
		drawLine(startX, startY, endX, endY, isDash);
	}

	/**
	 * JobDefinition diagram 그리기.
	 * @param jobdefList
	 * @param model
	 * @throws IOException
	 */
	public void drawJobDefMain(List<JobDefinition> jobdefList, JobFlowModel model) throws IOException {
		this.totalHeight = (model.getMaxY()+1)*(nodeHeight+nodeHeightSpace);
		this.totalWidth  = (model.getMaxX()+1)*(nodeWidth +nodeWidthSpace);
		
		this.innerRectWidth  = nodeWidth  - 4;
		this.innerRectHeight = nodeHeight / 4;
		
		drawMainBegin();

		// 실제 그래픽상의 좌표 설정
		for (JobNode node : model.getAllJobNodeList()) {
			node.setCoordX(node.getX() * (nodeWidth +nodeWidthSpace) - nodeWidth);
			node.setCoordY(node.getY() * (nodeHeight+nodeHeightSpace) - nodeHeight);
		}
		
		// Draw main
		HashMap<String, JobDefinition> jobdefMap = new HashMap();
		for (JobDefinition jobdef : jobdefList) {
			jobdefMap.put(jobdef.getJobId(), jobdef);
		}
		
		drawNodeAreaBegin();
		
		JobDefinition emptyJobDef = new JobDefinition();
			
		for (List<JobNode> level : model.getAllLevels()) {
			for (JobNode node : level) {
				JobDefinition jobdef = jobdefMap.get(node.getId());
				
				// draw node
				if (jobdef == null) {
					emptyJobDef.setJobId(node.getId());
					drawJobDefNode(node.getCoordX(), node.getCoordY(), emptyJobDef, node.isVirtual());
				}else {
					drawJobDefNode(node.getCoordX(), node.getCoordY(), jobdef, node.isVirtual());
				}
			}
		}
		drawNodeAreaEnd();

		int outSeq = 0;
		// line 을 맨 나중에 그림. post 로만 그림 (pre 로는 그리지 않음)
		drawLineAreaBegin();
		for (JobNode node : model.getAllJobNodeList()) {
			outSeq = 0;
			for (JobNodeRelation postNodeRel : node.getPostList()) {
				JobNode postNode = postNodeRel.getNodeTo();
				drawLine(node, postNode, ++outSeq, postNode.getPreList().indexOf(postNodeRel)+1, postNodeRel.getType() == JobNodeRelation.TYPE_TRIGGER);
			}
		}
		
		drawLineAreaEnd();
		drawMainEnd();
	}
	

	/**
	 * JobDefinition diagram 그리기.
	 * @param jobinsList
	 * @param model
	 * @throws IOException
	 */
	public void drawJobInsMain(List<JobInstance> jobinsList, JobFlowModel model) throws IOException {
		this.totalHeight = (model.getMaxY()+1)*(nodeHeight+nodeHeightSpace);
		this.totalWidth  = (model.getMaxX()+1)*(nodeWidth +nodeWidthSpace);

		this.innerRectWidth  = nodeWidth  - 4;
		this.innerRectHeight = nodeHeight / 4;
		
		drawMainBegin();

		// 실제 그래픽상의 좌표 설정
		for (JobNode node : model.getAllJobNodeList()) {
			node.setCoordX(node.getX() * (nodeWidth +nodeWidthSpace) - nodeWidth);
			node.setCoordY(node.getY() * (nodeHeight+nodeHeightSpace) - nodeHeight);
		}
		
		// Draw main
		HashMap<String, JobInstance> jobinsMap = new HashMap();
		for (JobInstance jobins : jobinsList) {
			jobinsMap.put(jobins.getJobInstanceId(), jobins);
		}
		
		drawNodeAreaBegin();
		
		JobInstance emptyJobIns = new JobInstance();
		
		for (List<JobNode> level : model.getAllLevels()) {
			for (JobNode node : level) {
				JobInstance jobins = jobinsMap.get(node.getId());
				
				// draw node
				if (jobins == null) {
					emptyJobIns.setJobInstanceId(node.getId());
					drawJobInsNode(node.getCoordX(), node.getCoordY(), jobins, node.isVirtual());
				}else {
					drawJobInsNode(node.getCoordX(), node.getCoordY(), jobins, node.isVirtual());  
				}
			}
		}
		drawNodeAreaEnd();

		int outSeq = 0;
		// line 을 맨 나중에 그림. post 로만 그림 (pre 로는 그리지 않음)
		drawLineAreaBegin();
		for (JobNode node : model.getAllJobNodeList()) {
			outSeq = 0;
			for (JobNodeRelation postNodeRel : node.getPostList()) {
				JobNode postNode = postNodeRel.getNodeTo();
				drawLine(node, postNode, ++outSeq, postNode.getPreList().indexOf(postNodeRel)+1, postNodeRel.getType() == JobNodeRelation.TYPE_TRIGGER);
			}
		}
		
		drawLineAreaEnd();
		drawMainEnd();
	}
	
	/*==================================================================*/
	/*======   요소 drawing 메소드                            ==========*/
	/*==================================================================*/
	
	/**
	 * 메인 그리기 시작. px 단위의 width, height 표시
	 */
	abstract void drawMainBegin() throws IOException;

	/**
	 * 메인 그리기 종료.
	 * @throws IOException
	 */
	abstract void drawMainEnd() throws IOException;

	/**
	 * Node area 시작
	 * @throws IOException
	 */
	abstract void drawNodeAreaBegin() throws IOException;

	/**
	 * Node area 끝
	 * @throws IOException
	 */
	abstract void drawNodeAreaEnd() throws IOException;

	/**
	 * 라인 area 시작
	 * @throws IOException
	 */
	abstract void drawLineAreaBegin() throws IOException ;

	/**
	 * 라인 area 끝
	 * @throws IOException
	 */
	abstract void drawLineAreaEnd() throws IOException ;

	/**
	 * 화살표 그리기.
	 * @param endX 끝점 X
	 * @param endY 끝점 Y
	 */
	abstract void drawArrow(int endX, int endY) throws IOException ;

	
	
	/**
	 * 베이지안 곡선 그리기 위한 point 2 개의 x,y 좌표 + 도착점 x,y 를 구함
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 * @throws IOException
	 * @return
	 */
	protected String getCurvePoint(int x, int y, int x2, int y2) throws IOException {

		if (y > y2) { // 아래에서 위로 갈때는 곡선 처리한다. 
			int cx=0,cy=0,cx2=0,cy2=0;

			cy  = y  + 80;
			cy2 = y2 - 80;
	
			if (x < x2) {
				cx  = x+10;
				cx2 = x2-10;
			}else if (x > x2) {
				cx  = x-10;
				cx2 = x2+10;
			}else {
				cx  = x+80;
				cx2 = x-80;
			}
			
			return " C "+cx+" "+cy+" "+cx2+" "+cy2+" "+x2+" "+(y2-10);
		}else {
			return " L "+x2+" "+(y2-10);
		}
	}

	/**
	 * 노드간 선후행 직선 라인을 그림.
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 * @param isDash
	 * @throws IOException
	 */
	abstract void drawLine(int x, int y, int x2, int y2, boolean isDash) throws IOException ;
	
	/**
	 * Job Definition 노드 하나 그리기. 
	 * @param x
	 * @param y
	 * @param jobdef
	 * @throws IOException
	 */
	abstract void drawJobDefNode(int x, int y, JobDefinition jobdef, boolean isVirtual) throws IOException;

	/**
	 * Job Instance 노드 하나를 그림
	 * @param x
	 * @param y
	 * @param jobins
	 * @param isVirtual
	 * @throws IOException
	 */
	abstract void drawJobInsNode(int x, int y, JobInstance jobins, boolean isVirtual) throws IOException;


}
