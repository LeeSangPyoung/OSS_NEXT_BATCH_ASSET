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
<title>Job Instances Flow Diagram </title>
<link rel="stylesheet" href="jobflow_svg.css" type="text/css" ></link>
<script>
	function node_mouse_over(evt) { 
		evt.target.style.opacity='0.2'; 
		evt.target.style.cursor='pointer'; 
	}  
	function node_mouse_out(evt) { 
		evt.target.style.opacity='0'; 
	}   
	function node_onclick(jobinstanceid) { 
		window.open('view_jobins_dtl.jsp?jobinstanceid='+jobinstanceid, 'jobins_'+jobinstanceid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus(); 
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
<title>Job Instances Flow Diagram </title>
<link rel="stylesheet" href="jobflow_vml.css" type="text/css" ></link>
<style> v\:* { behavior: url(#default#VML); }</style>
<script>
	var lineColor         = "#8899ff";
    
	var baseStrokeColor   = "#9f9f9f";
	var jobidFillColor    = "#efefef";
	var jobidLockedColor  = "#FFCCAA";
    var descFillColor     = "#ffffff";
	
	var prgsBgColor       = "#dadada";
	var prgsRunningColor  = "#efaf50";
	var prgsEndOkColor    = "#9f9fff";
	var prgsEndFailColor  = "#ff0000";

	var stateEnum = {
		"I" : 0,
		"W" : 1,
		"O" : 2,
		"F" : 3,
		"R" : 4,
		"P" : 5,
		"S" : 6,
		"G" : 7,
		"X" : 8 };

	var stateColors = new Array(
		"#efefef",
		"#efefef",
		"#52e222",
		"#d42515",
		"#ffff00",
		"#aaaa00",
		"#e9e9a9",
		"#F08080",
		"#006400" );

    function node_mouse_over(evt) {
        evt.style.cursor='pointer';
        var rectNode = evt.getElementsByTagName('rect').item(0);
        rectNode.strokecolor='#aa5566';
    } 
    function node_mouse_out(evt) {
        var rectNode = evt.getElementsByTagName('rect').item(0);
        rectNode.strokecolor=baseStrokeColor;
    }  
    function node_onclick(jobinstanceid) {
        window.open('view_jobins_dtl.jsp?jobinstanceid='+jobinstanceid, 'jobins_'+jobinstanceid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
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
</script>
</head>

<%  }   %>

<jsp:include page="common_query_jobins.jsp" />
<body>
<%
	int nodeWidth       = toInt(request.getParameter("nodewidth"),       defaultDiagramNodeWidth);
	int nodeHeight      = toInt(request.getParameter("nodeheight"),      defaultDiagramNodeHeight);
	int nodeWidthSpace  = toInt(request.getParameter("nodewidthspace"),  defaultDiagramNodeWidthSpace);
	int nodeHeightSpace = toInt(request.getParameter("nodeheightspace"), defaultDiagramNodeHeightSpace);
	
	String autoReloadInterval = request.getParameter("autoreload_interval");

	/* JobInstance Diagram 조회 */
    List<JobInstance> jobinsList = (List<JobInstance>)request.getAttribute("jobins_query_result");

	JobFlowAnalyzer analyzer = new JobFlowAnalyzer();
	analyzer.doAnalyzeByJobInstance(jobinsList);
	JobFlowModel model = analyzer.getModel();

	GraphicsMaker gm = GraphicsMaker.getInstance(isSVG ? "SVG" : "VML", out);
	gm.setNodeWidth(       nodeWidth);
	gm.setNodeHeight(      nodeHeight);
	gm.setNodeWidthSpace(  nodeWidthSpace);
	gm.setNodeHeightSpace( nodeHeightSpace);

	gm.drawJobInsMain(jobinsList, model);
%>

<script>
    var firstContact = 1;
    var ongoing      = 0;
    var jobinsCount  = 0;
	var nodeWidth    = <%=nodeWidth%>;
	var innerWidth   = nodeWidth-4;
	var lastModifyTime = 0;

    function pollServer() {
        if (ongoing == 0) {
            ongoing = 1;
            var httpRequest;

            if (window.XMLHttpRequest) {
                httpRequest = new XMLHttpRequest();
            } else if (window.ActiveXObject) {
                try {
                    httpRequest = new ActiveXObject("Msxml2.XMLHTTP");
                } catch (e) {
                    try {
                        httpRequest = new ActiveXObject("Microsoft.XMLHTTP");
                    }catch (e) {}
                }
            }
            
            firstContact = 0;
            httpRequest.onreadystatechange = function() { 
                if (this.readyState == 4) {
                    if (this.status == 200) {
                        var resDoc = this.responseXML;
        /* TODO
                        document.getElementById("current_datetime").innerHTML = "<B>"+
                            resDoc.getElementsByTagName("Current")[0].firstChild.nodeValue+"</B>";
        */
		                lastModifyTime = resDoc.getElementsByTagName("CurrentTimeMs")[0].firstChild.nodeValue;

						/* JobInstance Node */
                        var jobinsList = resDoc.getElementsByTagName("JobInstance");
                        for (var i=0; i<jobinsList.length; i++) {
                            if (document.getElementById(jobinsList[i].getAttribute("jobInstanceId")) == null) {
                                window.location.reload(true);
                            }else {
                                processJobInsNodeFor<%=isSVG ? "SVG" : "VML"%>(jobinsList[i]);
                            }
                        }

						/* Progress Update */
						var runningJobInsList = resDoc.getElementsByTagName("RunningJobInstance");
						for (var i=0; i<runningJobInsList.length; i++) {
							if (document.getElementById(runningJobInsList[i].getAttribute("jobInstanceId")) != null) {
								updateProgressFor<%=isSVG ? "SVG" : "VML"%>(runningJobInsList[i]);
							}
						}

                    }
                }
                ongoing = 0;
            };
            httpRequest.open('GET', 'svc_jobins_xml.jsp?<%=nvl(request.getQueryString(), "a=a")%>&lastmodifytimefrom='+lastModifyTime, true);
            httpRequest.send('');
            httpRequest= null;
        }
    }
    
    function processJobInsNodeForSVG(jobinsElem) {
        var jobinsid  = jobinsElem.getAttribute("jobInstanceId");
        var jobState  = jobinsElem.getAttribute("jobState");
		var jobStateText  = jobinsElem.getAttribute("jobStateText");
		var jobStateReason  = jobinsElem.getAttribute("jobStateReason");
		var prgsTotal = jobinsElem.getAttribute("progressTotal");
		var prgsCurr  = jobinsElem.getAttribute("progressCurr");
		var lockedBy  = jobinsElem.getAttribute("lockedBy");
        
		/* main bg color*/
		var bgRect = document.getElementById(jobinsid+"_bg");
		bgRect.setAttribute("class", "ins_node ins_node_bg_"+jobState);

		/* locked color */
		var jobIdRect = document.getElementById(jobinsid+"_jobid");
		if (lockedBy != "" && lockedBy.length > 0) {
			jobIdRect.setAttribute("class", "ins_node_jobid_locked");
		}else {
			jobIdRect.setAttribute("class", "ins_node_jobid");
		}
		
		/* tooltip title */
		var title = document.getElementById(jobinsid+"_title");
		title.textContent = "["+jobStateText+"] "+jobStateReason;

		var progress = prgsCurr+"/"+prgsTotal;

		/* progress */
		var prgsBarBg  = document.getElementById(jobinsid+"_prgs_bar_bg");
		var prgsBar    = document.getElementById(jobinsid+"_prgs_bar");
		var prgsTxt    = document.getElementById(jobinsid+"_prgs_txt");

		var percentage = 0;
		if (prgsTotal == "0" || prgsTotal == "") {
			if (prgsCurr == "0" || prgsCurr == "") {
				prgsTxt.textContent = "";
			}else {
				prgsTxt.textContent = progress; /* without percentage */
			}
			return; /* do not change progress bar */
		}else {
			percentage = Math.ceil(prgsCurr * 100 / prgsTotal);
			prgsTxt.textContent = progress + " ("+ percentage +"%)";
		}
		
		/* progress bar */
		if (prgsBarBg.getAttribute("width") == 0 || prgsBarBg.getAttribute("width") == "0") {
			prgsBarBg.setAttribute("width", innerWidth);
		}
		
		if (jobState == "R") {
			prgsBar.setAttribute("class", "ins_node_prgs_running");
		}else if (jobState == "O") {
			prgsBar.setAttribute("class", "ins_node_prgs_endok");
		}else if (jobState == "F") {
			prgsBar.setAttribute("class", "ins_node_prgs_endfail");
		}
		
		prgsBar.setAttribute("width", innerWidth * percentage / 100 - 4);
    }
	
    function updateProgressForSVG(runningJobElem) {
        var jobinsid  = runningJobElem.getAttribute("jobInstanceId");
		var prgsTotal = runningJobElem.getAttribute("progressTotal");
		var prgsCurr  = runningJobElem.getAttribute("progressCurr");
        
		var progress = prgsCurr+"/"+prgsTotal;

		/* progress */
		var prgsBar    = document.getElementById(jobinsid+"_prgs_bar");
		var prgsTxt    = document.getElementById(jobinsid+"_prgs_txt");

		var percentage = 0;
		if (prgsTotal == "0" || prgsTotal == "") {
			if (prgsCurr == "0" || prgsCurr == "") {
				prgsTxt.textContent = "";
			}else {
				prgsTxt.textContent = progress; /* without percentage */
			}
			return; /* do not change progress bar */
		}else {
			percentage = Math.ceil(prgsCurr * 100 / prgsTotal);
			prgsTxt.textContent = progress + " ("+ percentage +"%)";
		}
		
		/* progress bar */
		prgsBar.setAttribute("width", innerWidth * percentage / 100 - 4);
    }

	function setupSVG() {
	}

    function processJobInsNodeForVML(jobinsElem) {
        var jobinsid  = jobinsElem.getAttribute("jobInstanceId");
        var jobState  = jobinsElem.getAttribute("jobState");
		var jobStateText  = jobinsElem.getAttribute("jobStateText");
		var jobStateReason  = jobinsElem.getAttribute("jobStateReason");
		var prgsTotal = jobinsElem.getAttribute("progressTotal");
		var prgsCurr  = jobinsElem.getAttribute("progressCurr");
		var lockedBy  = jobinsElem.getAttribute("lockedBy");
        
		/* main bg color */
		var node = document.getElementById(jobinsid);
		var rectList = node.getElementsByTagName('rect'); 
		for (i=0; i<rectList.length; i++) { 
			var gb = rectList[i].getAttribute('gb'); 
			if (gb == 'base') { 
				rectList[i].setAttribute('fillcolor', stateColors[stateEnum[jobState]]); 
			} 
		}

		/* locked color */
		var jobIdRect = document.getElementById(jobinsid+"_jobid");
		if (lockedBy != "" && lockedBy.length > 0) {
			jobIdRect.setAttribute("fillcolor", jobidLockedColor);
		}else {
			jobIdRect.setAttribute("fillcolor", jobidFillColor);
		}
		
		/* tooltip title */
		var baseGroup = document.getElementById(jobinsid);
		baseGroup.setAttribute("title", "["+jobStateText+"] "+jobStateReason);

		var progress = prgsCurr+"/"+prgsTotal;

		/* progress */
		var prgsBarBg  = document.getElementById(jobinsid+"_prgs_bar_bg");
		var prgsBar    = document.getElementById(jobinsid+"_prgs_bar");
		var prgsTxt    = document.getElementById(jobinsid+"_prgs_txt");

		var percentage = 0;
		if (prgsTotal == "0" || prgsTotal == "") {
			if (prgsCurr == "0" || prgsCurr == "") {
				prgsTxt.innerHTML= "";
			}else {
				prgsTxt.innerHTML= progress; /* without percentage */
			}
			return; /* do not change progress bar */
		}else {
			percentage = Math.ceil(prgsCurr * 100 / prgsTotal);
			prgsTxt.innerHTML= progress + " ("+ percentage +"%)";
		}
		
		/* progress bar */
		if (prgsBarBg.style.width == 0 || prgsBarBg.style.width == "0" || prgsBarBg.style.width == "0px") {
			prgsBarBg.style.width = innerWidth;
			prgsBarBg.setAttribute("fillcolor", prgsBgColor);
		}

		if (jobState == "R") {
			prgsBar.setAttribute("fillcolor", prgsRunningColor);
		}else if (jobState == "O") { 
			prgsBar.setAttribute("fillcolor", prgsEndOkColor);
		}else if (jobState == "F") {
			prgsBar.setAttribute("fillcolor", prgsEndFailColor);
		}
		
		prgsBar.style.width = innerWidth * percentage / 100 - 4;
	}
	
    function updateProgressForVML(runningJobElem) {
        var jobinsid  = runningJobElem.getAttribute("jobInstanceId");
		var prgsTotal = runningJobElem.getAttribute("progressTotal");
		var prgsCurr  = runningJobElem.getAttribute("progressCurr");
        
		var progress = prgsCurr+"/"+prgsTotal;

		/* progress */
		var prgsBar    = document.getElementById(jobinsid+"_prgs_bar");
		var prgsTxt    = document.getElementById(jobinsid+"_prgs_txt");

		var percentage = 0;
		if (prgsTotal == "0" || prgsTotal == "") {
			if (prgsCurr == "0" || prgsCurr == "") {
				prgsTxt.innerHTML= "";
			}else {
				prgsTxt.innerHTML= progress; /* without percentage */
			}
			return; /* do not change progress bar */
		}else {
			percentage = Math.ceil(prgsCurr * 100 / prgsTotal);
			prgsTxt.innerHTML= progress + " ("+ percentage +"%)";
		}
		
		/* progress bar */
		prgsBar.style.width = innerWidth * percentage / 100 - 4;
    }

	
	function setupVML() {
        var rectList = document.getElementsByTagName('rect'); 
		for (i=0; i<rectList.length; i++) { 
            var gb = rectList[i].getAttribute('gb'); 
            if (gb == 'base') { 
                rectList[i].setAttribute('fillcolor',     stateColors[stateEnum.I]); 
                rectList[i].setAttribute('strokecolor',   baseStrokeColor);
                rectList[i].setAttribute('strokeweight',  "2");
            }else if (gb == 'jobid') {
                rectList[i].setAttribute('fillcolor',     jobidFillColor);
                rectList[i].setAttribute('strokecolor',   '#ffffff');
                rectList[i].setAttribute('strokeweight',  "0");
            }else if (gb == 'desc') { 
                rectList[i].setAttribute('fillcolor',     descFillColor); 
                rectList[i].setAttribute('strokecolor',   '#ffffff');
                rectList[i].setAttribute('strokeweight',  "0");
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

    setup<%=isSVG ?  "SVG" : "VML"%>();
    pollServer();
    
    if ('<%=autoReloadInterval%>' != '0') {
        setInterval('pollServer()', <%=autoReloadInterval%> * 1000);
    }

</script>	
</body>
</html>
