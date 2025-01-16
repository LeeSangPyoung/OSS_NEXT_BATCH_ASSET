package nexcore.scheduler.diagram;

import java.io.IOException;
import java.io.Writer;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : SVG 방식으로 line, rect 그리기</li>
 * <li>작성일 : 2012. 3. 14.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// VML 은 fillcolor, stroke color 등을 CSS 로 할 수 없으므로 여기서는 style class 쓰지 않고 
// drawing 완료후 자바 스크립트로 color 다시 설정한다. 
public class GraphicsMakerVMLImpl extends GraphicsMaker {
	
	GraphicsMakerVMLImpl(Writer out) {
		this.out        = out;
	}

	void drawMainBegin() throws IOException {
		out.write("<div style=\"position:absolute;left:0;top:0;width:"+totalWidth+";height:"+totalHeight+"\">");
		out.write("<v:group style=\"position:absolute;left:0;top:0;width:"+totalWidth+";height:"+totalHeight+"\" coordorigin='0,0' coordsize="+totalWidth+","+totalHeight+" >");
	}
	
	void drawMainEnd() throws IOException {
		out.write("</v:group>");
		out.write("</v:div>");
	}
	
	/**
	 * Node area 시작
	 * @throws IOException
	 */
	void drawNodeAreaBegin() throws IOException {
	}
	
	/**
	 * Node area 끝
	 * @throws IOException
	 */
	void drawNodeAreaEnd() throws IOException {
	}


	/**
	 * 라인 area 시작
	 * @throws IOException
	 */
	void drawLineAreaBegin() throws IOException {
	}
	
	/**
	 * 라인 area 끝
	 * @throws IOException
	 */
	void drawLineAreaEnd() throws IOException {
	}

	/**
	 * 화살표 그리기.
	 * @param endX 끝점 X
	 * @param endY 끝점 Y
	 */
	void drawArrow(int endX, int endY) throws IOException {
		// VML 에서는 arrow 를 직접 그리지 않고 stroke 의 속성으로 처리함 
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
	void drawLine(int x, int y, int x2, int y2, boolean isDash) throws IOException {
		out.write("<v:shape gb=line style='position:absolute;left:0;top:0;width:"+totalWidth+";height:"+totalHeight+"'");
		out.write(" onmouseover=\"line_mouse_over(this);\" onmouseout=\"line_mouse_out(this);\">");
		out.write("<v:path v=\"m "+x+","+y+" l "+x+","+(y+10));
		out.write(getCurvePoint(x, y, x2, y2));
		out.write(" l "+x2+","+y2+" e\" fillok=f arrowok=t />");
		
		out.write("<v:stroke endarrow=classic endarrowwidth=wide ");
		if (isDash) {
			out.write(" dashstyle=dash");
		}
		out.write(" />");
		out.write("</v:shape>");

	}
	
	private void drawGroupOpen(String id, int x, int y, int width, int height, String extra) throws IOException {
		out.write("<v:group id='"+id+"' ");
		out.write(" style='position:absolute;left:"+x+";top:"+y+";width:"+nodeWidth+";height:"+nodeHeight+"' ");
		out.write(" coordorigin='0,0' coordsize='"+width+","+height+"' ");
		if (extra != null) {
			out.write(" ");
			out.write(extra);
		}
		out.write(">");
	}

	private void drawGroupClose() throws IOException {
		out.write("</v:group>");
	}

	private void drawRect(int x, int y, int width, int height, String extra, boolean isDashLine) throws IOException {
		out.write("<v:rect ");
		out.write(" style='position:absolute;top:"+y+";left:"+x+";width:"+width+";height:"+height+"' ");
		if (extra != null) {
			out.write(" ");
			out.write(extra);
		}
		out.write(" >");
		if (isDashLine) {
			out.write("<v:stroke dashstyle=dash />");
		}
		out.write("</v:rect>");
	}
	
	private void drawRoundRect(double arcSize, int x, int y, int width, int height, String extra) throws IOException {
		out.write("<v:roundrect ");
		out.write(" arcSize=\""+arcSize+"\"");
		
		out.write(" style='position:absolute;top:"+y+";left:"+x+";width:"+width+";height:"+height+"' ");
		if (extra != null) {
			out.write(" ");
			out.write(extra);
		}
		out.write(" >");
		out.write("<v:stroke on=false />");
		out.write("</v:roundrect>");
	}
	
	private void drawText(int x, int y, String styleClass, String text, String extra) throws IOException {
		out.write("<v:textBox");
		out.write(" style='position:absolute;top:"+y+";left:"+x+"'");
		if (styleClass != null) {
			out.write(" class="+styleClass);
		}
		if (extra != null) {
			out.write(" ");
			out.write(extra);
		}
		
		out.write(" >");
		out.write(Util.nvl(text));
		out.write("</v:textBox>");
	}
	
	private void drawText(int x, int y, int width, int height, String styleClass, String text, String extra) throws IOException {
		out.write("<v:textBox");
		out.write(" style='position:absolute;top:"+y+";left:"+x+";width:"+width+";height:"+height+"'");
		if (styleClass != null) {
			out.write(" class="+styleClass);
		}
		if (extra != null) {
			out.write(" ");
			out.write(extra);
		}
		
		out.write(" >");
		out.write(Util.nvl(text));
		out.write("</v:textBox>");
	}
	
	
	void drawJobDefNode(int x, int y, JobDefinition jobdef, boolean isVirtual) throws IOException {
		drawGroupOpen(jobdef.getJobId(), x, y, nodeWidth, nodeHeight, "onmouseover=\"node_mouse_over(this);\" onmouseout=\"node_mouse_out(this);\" onclick=\"node_onclick('"+jobdef.getJobId()+"');\"");
		
		drawRect(0, 0,  nodeWidth, nodeHeight, " gb=base", isVirtual);  /* main box */

		drawRect(2, 2,  innerRectWidth, innerRectHeight, " gb=jobid", false);  /* id   box */
		drawRect(2, 20, innerRectWidth, innerRectHeight, " gb=desc",  false);  /* desc box */

		drawText(4, 4,                       "font_jobid",  jobdef.getJobId(), null);                         // job id
		drawText(4, 6,  innerRectWidth, 20,  "font1_right", jobdef.getOwner(), null);                         // owner. right align
		drawText(4, 24,                      "font1",       Util.left(jobdef.getDescription(),   20), null);  // desc
		drawText(4, 44,                      "font1",       jobdef.getJobType(), null);                       // jobtype
		drawText(4, 44, innerRectWidth, 20,  "font1_right", Util.left(jobdef.getComponentName(), 20), null);  // componentName
		drawText(4, 64,                      "font1",       jobdef.getAgentNode(), null);                     // agent
		drawText(4, 64, innerRectWidth, 20,  "font1_right", jobdef.getTimeFrom(), null);                      // timefrom

		drawGroupClose();
	}

	void drawJobInsNode(int x, int y, JobInstance jobins, boolean isVirtual) throws IOException {
		drawGroupOpen(jobins.getJobInstanceId(), x, y, nodeWidth, nodeHeight, "onmouseover=\"node_mouse_over(this);\" onmouseout=\"node_mouse_out(this);\" onclick=\"node_onclick('"+jobins.getJobInstanceId()+"');\"" );
		
		drawRect(0, 0,  nodeWidth, nodeHeight, " gb=base id=\""+jobins.getJobInstanceId()+"_bg\"", isVirtual);  /* main box */

		drawRect(2, 2,  innerRectWidth, innerRectHeight, " gb=jobid id=\""+jobins.getJobInstanceId()+"_jobid\"",     false);   /* id   box      */
		drawRect(2, 20, innerRectWidth, innerRectHeight, " gb=desc",      false);   /* desc box      */
		drawRoundRect(0.2, 2, 40, 0,    innerRectHeight, " gb=prgs_bg   id=\""+jobins.getJobInstanceId()+"_prgs_bar_bg\"");   /* progress bg   */
		drawRoundRect(0.2, 4, 42, 0,    innerRectHeight-4, " gb=prgs_curr id=\""+jobins.getJobInstanceId()+"_prgs_bar\"");    /* progress curr */

		drawText(4, 4,                         "font_jobinsid",  jobins.getJobInstanceId(), null);                       // job id
		drawText(4, 24,                        "font1",          Util.left(jobins.getDescription(), 20), null);          // desc
		drawText(4, 44, innerRectWidth,   20,  "font1_center",   "" , "id=\""+jobins.getJobInstanceId()+"_prgs_txt\"");  // progress
		drawText(4, 64,                        "font1",          jobins.getJobType(), null);                             // jobtype
		drawText(4, 64, innerRectWidth,   20,  "font1_right",    Util.left(jobins.getComponentName(), 20), null);           // componentName

		drawGroupClose();
		
	}
}
