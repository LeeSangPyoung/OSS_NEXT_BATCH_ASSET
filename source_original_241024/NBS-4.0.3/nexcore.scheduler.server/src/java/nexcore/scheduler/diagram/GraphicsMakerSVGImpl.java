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

public class GraphicsMakerSVGImpl extends GraphicsMaker {

	GraphicsMakerSVGImpl(Writer out) {
		this.out        = out;
	}

	void drawMainBegin() throws IOException {
		out.write("<svg width="+totalWidth+" height="+totalHeight+" >");
		
	}
	
	void drawMainEnd() throws IOException {
		out.write("</svg>");
	}
	
	/**
	 * Node area 시작
	 * @throws IOException
	 */
	void drawNodeAreaBegin() throws IOException {
		out.write("<g onmouseover=\"node_mouse_over(evt);\" onmouseout=\"node_mouse_out(evt);\">");
	}
	
	/**
	 * Node area 끝
	 * @throws IOException
	 */
	void drawNodeAreaEnd() throws IOException {
		out.write("</g>");
	}


	/**
	 * 라인 area 시작
	 * @throws IOException
	 */
	void drawLineAreaBegin() throws IOException {
		out.write("<g onmouseover=\"line_mouse_over(evt);\" onmouseout=\"line_mouse_out(evt);\">");
	}
	
	/**
	 * 라인 area 끝
	 * @throws IOException
	 */
	void drawLineAreaEnd() throws IOException {
		out.write("</g>");
	}

	/**
	 * 화살표 그리기.
	 * @param endX 끝점 X
	 * @param endY 끝점 Y
	 * @param styleClass
	 */
	void drawArrow(int endX, int endY) throws IOException {
		out.write("<path");
		out.write(" d=\"M "+endX+" "+endY);
		out.write(" l -3 -7 l 3 3 l 3 -3 Z\" ");
		out.write(" />"); // 상태 좌표이므로 소문자 l 이용
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
		out.write("<g stroke-width=\"2\" class=line>");

		out.write("<path");
		out.write(" d=\"M "+x+" "+y+" L "+x+" "+(y+10));
		out.write(getCurvePoint(x, y, x2, y2));
		out.write(" L "+x2+" "+y2+ "\"");
		out.write(" fill=none ");
		if (isDash) {
			out.write(" stroke-dasharray=5");
		}
		out.write(" />");
		drawArrow(x2, y2);
		out.write("</g>");
	}
	
	private void drawRect(int x, int y, int width, int height, String styleClass, String extra) throws IOException {
		out.write("<rect ");
		out.write(" x="+x);
		out.write(" y="+y);
		out.write(" width="+width);
		out.write(" height="+height);
		if (styleClass != null) {
			out.write(" class=\""+styleClass+"\"");
		}
		if (extra != null) {
			out.write(" "+extra);
		}
		out.write(" />");
	}
	
	private void drawText(int x, int y, String text, boolean wrapCdata, String styleClass, String extra) throws IOException {
		out.write("<text ");
		out.write(" x="+x);
		out.write(" y="+y);
		
		if (styleClass != null) {
			out.write(" class=\""+styleClass+"\"");
		}
		if (extra != null) {
			out.write(" "+extra);
		}
		out.write(" >");
		if (wrapCdata) {
			out.write("<![CDATA[");
			out.write(Util.nvl(text));
			out.write("]]>");
		}else {
			out.write(Util.nvl(text));
		}
		out.write("</text>");
	}
	
	void drawJobDefNode(int x, int y, JobDefinition jobdef, boolean isVirtual) throws IOException {
		out.write("<g id=\""+jobdef.getJobId()+"\" transform=translate("+x+","+y+")>");
		
		if (isVirtual) {
			drawRect(0, 0,  nodeWidth,      nodeHeight,      "def_node_virtual", null); /* job node virtual main */
		}else {
			drawRect(0, 0,  nodeWidth,      nodeHeight,      "def_node",       null); /* job node main */
		}
		drawRect(2, 2,  innerRectWidth, innerRectHeight, "def_node_jobid", null); /* id box        */
		drawRect(2, 20, innerRectWidth, innerRectHeight, "def_node_desc",  null); /* desc box      */
		
		int textVBase = 15;
		drawText(4,              textVBase,      jobdef.getJobId(),                        false, "font_jobid",  null);
		drawText(innerRectWidth, textVBase,      jobdef.getOwner(),                        true,  "font1_right", null);
		drawText(4,              textVBase + 20, Util.left(jobdef.getDescription(), 20),   true,  "font1",       "textLength="+(innerRectWidth-5));
		drawText(4,              textVBase + 40, jobdef.getJobType(),                      false, "font1",       null);
		drawText(innerRectWidth, textVBase + 40, Util.left(jobdef.getComponentName(), 20), false, "font1_right", null);
		drawText(4,              textVBase + 60, jobdef.getAgentNode(),                    false, "font1",       null);
		drawText(innerRectWidth, textVBase + 60, jobdef.getTimeFrom(),                     false, "font1_right", null);
		
		drawRect(0, 0, nodeWidth, nodeHeight, "def_node_outer",  " onclick=\"node_onclick('"+jobdef.getJobId()+"');\""); /* outer box      */
		
		out.write("</g>");

	}

	void drawJobInsNode(int x, int y, JobInstance jobins, boolean isVirtual) throws IOException {
		out.write("<g id=\""+jobins.getJobInstanceId()+"\" transform=translate("+x+","+y+")>");
		
		if (isVirtual) {
			drawRect(0, 0,  nodeWidth,      nodeHeight,      "ins_node_virtual", null); /* job node virtual main */
		}else {
			drawRect(0, 0,  nodeWidth,      nodeHeight,      "ins_node ins_node_bg_"+jobins.getJobState(), "id=\""+jobins.getJobInstanceId()+"_bg\""); /* job node main box */
		}
		
		drawRect(2, 2,  innerRectWidth, innerRectHeight, "ins_node_jobid",     "id=\""+jobins.getJobInstanceId()+"_jobid\""); /* id box           */
		drawRect(2, 20, innerRectWidth, innerRectHeight, "ins_node_desc ",     null); /* desc box         */
		drawRect(2, 40, 0,              innerRectHeight, "ins_node_prgs_bg",   "rx=5 id=\""+jobins.getJobInstanceId()+"_prgs_bar_bg\""); /* progress bg box  */
		drawRect(4, 42, 0,              innerRectHeight-4, "ins_node_prgs_bg", "rx=5 id=\""+jobins.getJobInstanceId()+"_prgs_bar\""); /* progress fg box. 여기서는 0 으로 하고 ajax 쪽에서 width 조정한다. */ 
		
		int textVBase = 15;
		drawText(4,                textVBase,      jobins.getJobInstanceId(),                false, "font_jobinsid", null);
		drawText(4,                textVBase + 20, Util.left(jobins.getDescription(), 20),   true,  "font1",         "textLength="+(innerRectWidth-5));
		drawText(innerRectWidth/2, textVBase + 40, "",                                       true,  "font1_middle",  "id=\""+jobins.getJobInstanceId()+"_prgs_txt\"");
		drawText(4,                textVBase + 60, jobins.getJobType(),                      false, "font1",         null);
		drawText(innerRectWidth,   textVBase + 60, Util.left(jobins.getComponentName(), 20), false, "font1_right",   null);
		
		drawRect(0, 0, nodeWidth, nodeHeight, "ins_node_outer",  " onclick=\"node_onclick('"+jobins.getJobInstanceId()+"');\""); /* outer box      */
		out.write("<title id=\""+jobins.getJobInstanceId()+"_title\"></title>");
		out.write("</g>");

	}

	
}
