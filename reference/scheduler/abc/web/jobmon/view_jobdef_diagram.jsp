<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common_functions.jsp" %>
<%@page import="nexcore.scheduler.diagram.JobFlowAnalyzer"%>
<%@page import="nexcore.scheduler.diagram.JobFlowModel"%>
<%@page import="nexcore.scheduler.diagram.GraphicsMaker"%>

<%
    /* 브라우저에 따른 SVG, VML 선택 */
    boolean isSVG = true;

    String userAgent = request.getHeader("User-agent");
	int    msieIdx   = userAgent.indexOf("MSIE");
    if (userAgent != null && msieIdx > -1) {
		String msieVer = userAgent.substring(msieIdx+5, msieIdx+10);
		if (msieVer.startsWith("5") || msieVer.startsWith("6") || msieVer.startsWith("7") || msieVer.startsWith("8") || msieVer.startsWith("9")) {
			// MSIE 5,6,7,8,9 에서는 VML 로함.
			isSVG = false;
		}
    }
%>

<%  if (isSVG) {  /* SVG */ %>

<html>
<head>
<title>Job Definitions Flow Diagram </title>
<link rel="stylesheet" href="jobflow_svg.css" type="text/css" ></link>
<script>
	function node_mouse_over(evt) { 
		evt.target.style.opacity='0.2'; 
		evt.target.style.cursor='pointer'; 
	}  
	function node_mouse_out(evt) { 
		evt.target.style.opacity='0'; 
	}   
	function node_onclick(jobid) { 
		window.open('view_jobdef_dtl.jsp?jobid='+jobid, 'jobdef_'+jobid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus(); 
	}   
	function line_mouse_over(evt) {  
		evt.target.parentElement.setAttribute('class', 'line_mouseover');  
	}  
	function line_mouse_out(evt) {  
		evt.target.parentElement.setAttribute('class', 'line');  
	} 
</script>
</head>

<%  }else {  /* VML */ %>

<html xmlns:v="urn:schemas-microsoft-com:vml">
<head>
<title>Job Definitions Flow Diagram </title>
<link rel="stylesheet" href="jobflow_vml.css" type="text/css" ></link>
<style> v\:* { behavior: url(#default#VML); }</style>
<script>
    var baseFillColor     = '#dfdfdf';
    var baseStrokeColor   = '#9f9f9f';
    var baseStrokeWeight  = '2';
    var jobidFillColor    = '#cdcdfd';
    var jobidStrokeWeight = '0';
    var descFillColor     = '#efefef';
    var descStrokeWeight  = '0';
    var lineColor         = '#8899ff';
    function node_mouse_over(evt) {
        evt.style.cursor='pointer';
        var rectNode = evt.getElementsByTagName('rect').item(0);
        rectNode.strokecolor='#aa5566';
    } 
    function node_mouse_out(evt) {
        var rectNode = evt.getElementsByTagName('rect').item(0);
        rectNode.strokecolor=baseStrokeColor;
    }  
    function node_onclick(jobid) {
        window.open('view_jobdef_dtl.jsp?jobid='+jobid, 'jobdef_'+jobid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }  
    function line_mouse_over(evt) { 
        evt.style.cursor='pointer';
        var strokeNode = evt.getElementsByTagName('stroke').item(0);
        strokeNode.color='#ff0000';
        strokeNode.weight=3;
        strokeNode.opacity=0.5;
        strokeNode.endarrowwidth='narrow';
    } 
    function line_mouse_out(evt) { 
        var strokeNode = evt.getElementsByTagName('stroke').item(0);
        strokeNode.color=lineColor;
        strokeNode.weight=2;
        strokeNode.opacity=1;
        strokeNode.endarrowwidth='wide';
    } 
    function set_color() { 
        var rectList = document.getElementsByTagName('rect'); 
        for (i=0; i<rectList.length; i++) { 
            var gb = rectList[i].getAttribute('gb'); 
            if (gb == 'base') { 
                rectList[i].setAttribute('fillcolor',     baseFillColor); 
                rectList[i].setAttribute('strokecolor',   baseStrokeColor);
                rectList[i].setAttribute('strokeweight',  baseStrokeWeight);
            }else if (gb == 'jobid') { 
                rectList[i].setAttribute('fillcolor',     jobidFillColor);
                rectList[i].setAttribute('strokecolor',   '#ffffff');
                rectList[i].setAttribute('strokeweight',  '0');
            }else if (gb == 'desc') { 
                rectList[i].setAttribute('fillcolor',     descFillColor); 
                rectList[i].setAttribute('strokecolor',   '#ffffff');
                rectList[i].setAttribute('strokeweight',  '0');
            } 
        }
        
        var shapeList = document.getElementsByTagName('shape');
        for (i=0; i<shapeList.length; i++) {
            if (shapeList[i].gb == 'line') {
                var strokeNode = shapeList[i].getElementsByTagName('stroke').item(0);
                strokeNode.color = lineColor;
                strokeNode.weight = '2';
            }
        }
    } 
</script>
</head>

<%  }   %>

<jsp:include page="common_query_jobdef.jsp" />
<body>
<%
	int nodeWidth       = toInt(request.getParameter("nodewidth"),       defaultDiagramNodeWidth);
	int nodeHeight      = toInt(request.getParameter("nodeheight"),      defaultDiagramNodeHeight);
	int nodeWidthSpace  = toInt(request.getParameter("nodewidthspace"),  defaultDiagramNodeWidthSpace);
	int nodeHeightSpace = toInt(request.getParameter("nodeheightspace"), defaultDiagramNodeHeightSpace);

    /* JobDefinition Diagram 조회 */
    List<JobDefinition> jobdefList = (List<JobDefinition>)request.getAttribute("jobdef_query_result");

	JobFlowAnalyzer analyzer = new JobFlowAnalyzer();
	analyzer.doAnalyzeByJobDefinition(jobdefList);
	JobFlowModel model = analyzer.getModel();

	GraphicsMaker gm = GraphicsMaker.getInstance(isSVG ? "SVG" : "VML", out);
	gm.setNodeWidth(       nodeWidth);
	gm.setNodeHeight(      nodeHeight);
	gm.setNodeWidthSpace(  nodeWidthSpace);
	gm.setNodeHeightSpace( nodeHeightSpace);

	gm.drawJobDefMain(jobdefList, model);
%>

<%  if (!isSVG) { /* VML 인 경우 노드 색 설정 */ %> 
<script>set_color();</script>
<%  } %>

</body>
</html>