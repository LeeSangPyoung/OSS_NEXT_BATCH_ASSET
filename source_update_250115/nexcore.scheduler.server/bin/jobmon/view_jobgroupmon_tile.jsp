<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	String printRunCount(int runTotalCount, int endOkCount, int endFailCount) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("<a title='");
		sb.append(Label.get("jobins.succcount")+" : "+endOkCount+", ");
		sb.append(Label.get("jobins.failcount")+" : "+endFailCount+", ");
		sb.append(Label.get("jobins.runcount") +" : "+runTotalCount);
		sb.append("'>");

		if (endFailCount > 0) {
			sb.append(endOkCount + " / <font color=red><b>"+ endFailCount +"</b></font> / "+runTotalCount);
		}else {
			sb.append(endOkCount + " / 0 / "+runTotalCount);
		}
		sb.append("</a>");
		return sb.toString();
	}

	String printJobStateText(String state) {
		return state == null ? "&nbsp;" : "<font class='jobstate_font_"+state+"'>"+JobInstance.getJobStateText(state)+"</font>";
	}
%>
<jsp:include page="top.jsp" flush="true"/>
<jsp:include page="common_query_jobgroupmon.jsp"/>
<%
    if (!checkLogin(request, response)) return;

	ControllerAdminLocal  admin = getControllerAdmin();
	
	// 조회 조건
    String searchdatetype     = nvl(request.getParameter("searchdatetype")).trim();
    String searchdatefrom     = nvl(request.getParameter("searchdatefrom")).trim();
    String searchdateto       = nvl(request.getParameter("searchdateto")).trim();

    String jobgroupid         = request.getParameter("jobgroupid");
    String jobgroupname       = request.getParameter("jobgroupname");
	String autoReloadInterval = request.getParameter("autoreload_interval");
	boolean failOnly          = Util.toBoolean(request.getParameter("failonly"), false); /* fail 건만 조회함 */
    final String jobfilter    = request.getParameter("jobfilter");

	if (Util.isBlank(searchdatetype)) {
		searchdatetype = "activationDate";
       	String todayDate;
    	if (Util.getCurrentHHMMSS().compareTo(admin.getSystemConfigValue("DAILY_ACTIVATION_TIME")+"00") >=0) {
    		/* daily activation 이후 */ 
        	todayDate = Util.getCurrentYYYYMMDD();
    	}else {
    		/* daily activation 이전 */
    		todayDate = Util.getYesterdayYYYYMMDD();
    	}

		searchdatefrom = todayDate; /* jobgroup 모니터링은 사용자에 상관없이 당일 조회만한다.*/
		searchdateto   = Util.getCurrentYYYYMMDD();
	}
	
	Map<String, String>           jobFilterCodeList     = getJobFilterCodeList(request);
	List<JobGroup>                jobGroupTreeList      = (List<JobGroup>)request.getAttribute("jobGroupTreeList");
    Map<String, JobGroupRunStats> jobGroupRunStatsMap   = (Map<String, JobGroupRunStats>)request.getAttribute("jobGroupRunStatsMap");
    Map<String, JobGroupRunStats> naJobGroupRunStatsMap = (Map<String, JobGroupRunStats>)request.getAttribute("naJobGroupRunStatsMap");
%>
<link rel="stylesheet" href="jobdisplay.css" type="text/css" />
<style>
/* a[href] {font-weight: bold} */
.node { border-style:dotted ;border-color: #cdcdcd; border-width:1px } 
</style>
<script>
	function doQuery() {
	    document.form1.submit();
	}
	
    function openJobGroupSelectWin(targetElemName) {
        window.open("popup_jobgroup.jsp?target_name="+targetElemName, 'jobgroupselect', 'width=700,height=400,scrollbars=1').focus();
    }

    function openJobGroupWin(jobgroupid) {
        window.open("view_jobgroup_dtl.jsp?jobgroupid="+jobgroupid, 'jobgroup_'+jobgroupid.replace(/-/g, ''), 'width=600,height=400,scrollbars=1').focus();
    }

    function openJobDefListWin(jobgroupid, jobfilter) {
    	var width  = window.screen.width  - 200;
    	var height = window.screen.height - 200;
    	var left = (window.screen.width  / 2) - (width / 2);
        var top  = (window.screen.height / 2) - (height / 2 + 50);

        window.open("view_jobdef.jsp?list_method=search&jobgroup="+jobgroupid+"&jobfilter="+jobfilter, 'jobdeflist_'+jobgroupid.replace(/-/g, ''), 'width='+width+',height='+height+',left='+left+',top='+top+',scrollbars=1,resizable=1').focus();
    }

    function openJobInsListWin(jobgroupid, jobfilter) {
    	var width  = window.screen.width  - 200;
    	var height = window.screen.height - 200;
    	var left = (window.screen.width  / 2) - (width / 2);
        var top  = (window.screen.height / 2) - (height / 2 + 50);

       	window.open("view_jobins.jsp?jobgroup="+jobgroupid+"&jobfilter="+jobfilter+"&searchdatetype=<%=nvl(searchdatetype)%>&searchdatefrom=<%=nvl(searchdatefrom)%>&searchdateto=<%=nvl(searchdateto)%>", 'jobinslist_'+jobgroupid.replace(/-/g, ''), 'width='+width+',height='+height+',left='+left+',top='+top+',scrollbars=1,resizable=1').focus();
    }

    function onclickTD(event, jobgroupid, jobfilter) {
    	var origin = event.srcElement || event.target;
    	if (origin.getAttribute("href") != null) { /* href 우선 */
    		return;
    	}else {
    		openJobInsListWin(jobgroupid, jobfilter);
    	}        	
    }

	function gotoListStyle() {
		document.form1.action="view_jobgroupmon.jsp";
		doQuery();
	}

</script>

<center>
<div class="content-wrap">
<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("top.menu.jobgroupmon")%></div>
</div>

<form name="form1" action="view_jobgroupmon_tile.jsp" method="get" style="margin-top:0;margin-bottom:0">
	<table class="Table Width-100 njf-table__typea Margin-bottom-10">
		<tbody>
		<tr>
		    <th style="width:6%; padding:1px"><b><a href="javascript:openJobGroupSelectWin('jobgroupid');"><%=Label.get("job.jobgroup")%></a></b></th>
		    <td style="width:16%; text-align:center; padding:3px;"><input type="text" class="Textinput Width-85 Margin-right-5" name="jobgroupid" value="<%=nvl(jobgroupid)%>">%</td>
		    
		    <th style="width:8%; padding:1px"><b><%=Label.get("jobgroup")+" "+Label.get("common.name")%></b></th>
		    <td style="width:26%; text-align:center; padding:3px;">%<input type="text" class="Textinput Width-85 Margin-left-5 Margin-right-5" name="jobgroupname" value="<%=conv(jobgroupname)%>">%</td>
		    
		    <th style="width:3%; padding:1px"><b><%=Label.get("common.search.gubun")%></b></th>
		    <td style="width:8%; text-align:center; padding:3px;">
		        <select class="Select Width-100" name="jobfilter">
		            <%=printJobFilter(jobfilter, request)%>
		        </select>
		    </td>
		    
		    <th style="width:8%; padding:1px; text-align:center;">
		        <select class="Select" name="searchdatetype">
		        	<%=printSelectOption("activationDate", Label.get("common.activationdate"),  searchdatetype)%>
		        	<%=printSelectOption("procDate",       Label.get("common.procdate"),        searchdatetype)%>
		        </select>
		    </th>
		    <td style="width:20%; text-align:center; padding:3px;">
		        <input type="text" class="Textinput Width-45" name="searchdatefrom" value="<%=nvl(searchdatefrom)%>" maxlength=8>
		        ~
		        <input type="text" class="Textinput Width-45" name="searchdateto" value="<%=nvl(searchdateto)%>" maxlength=8>
		    </td>
		    <td style="text-align:center; padding:3px;">
		        <input type="submit" class="Button"  value="<%=Label.get("common.btn.query")%>" onclick="doQuery();">
		    </td>    
		</tr>
		</tbody>
	</table>
<br>
<span id="error_bar"></span>
	<table class="Width-100">
		<tr>
			<td class="Text-left">
				<input type="button" class="Button" value="<%=Label.get("view_jobgroupmon.liststyle")%>" onclick="gotoListStyle();">
			</td>
			<td class="Text-right">
			    <label><%=Label.get("view_jobgroupmon.failonly")%></label>
			    <input class="Checkbox Margin-right-20" type="checkbox" name="failonly" value="1" <%=failOnly?"checked":""%> >
			    <%=Label.get("common.btn.auto.refresh")%>
		        <select class="Select Margin-right-20" name="autoreload_interval">
		            <%=printSelectOption("0",  "NO",                             autoReloadInterval)%>
		<%
			for (String jobinsViewRefreshInterval : jobinsViewRefreshIntervalList) {
		        out.println(printSelectOption(jobinsViewRefreshInterval, jobinsViewRefreshInterval+Label.get("common.second"),  autoReloadInterval));
			}
		%>
		        </select>
			    <span style="font-size:12px; text-align:right;" id="current_datetime"><%=toDatetimeString(new java.util.Date(), false) %></span>
			</td>
		</tr>
	</table>
</form>

	<table class="Width-100">
		<%
			int i=0;
		    for (JobGroup jobgroup : jobGroupTreeList) {
				JobGroupRunStats stats = jobGroupRunStatsMap.get(jobgroup.getId());
				if (failOnly && stats.getEndFailCount() == 0) { /* failonly 체크시에는 endfail 건이 없으면 출력하지 않는다. */
					continue;
				}
		    	i++;
		%>
		<td class="jobstate_bgcolor_<%=stats.getState() %>_hover hand node" onclick="javascript:onclickTD(event, '<%=stats.getJobGroupId()%>', '');" >
		<table class="Table njf-table__typea Width-100">
			<thead>
			<tr><th colspan=2><a href="javascript:openJobGroupWin('<%=conv(stats.getJobGroupId())%>')"><%=stats.getJobGroupId() %></a></th></tr>
			<tr><th colspan=2 class="jobdesc_font"><%=shortenRight(jobgroup.getName(), 15) %></th></tr>
			</thead>
			<tbody>
			<tr>
				<td class="Text-left"><%=printJobStateText(stats.getState())%></td>
				<td class="Text-right"><%=printRunCount(stats.getRunTotalCount(), stats.getEndOkCount(), stats.getEndFailCount()) %></td>
			</tr>
			<tr>
				<td class="Text-left"><a title="Job Definitions" href="javascript:openJobDefListWin('<%=stats.getJobGroupId()%>', '')" ><%=stats.getJobDefCount() %></a>&nbsp; -> &nbsp;<a title="Job Instances" href="javascript:openJobInsListWin('<%=stats.getJobGroupId()%>', '')"><%=stats.getJobInsCount() %></a></td>
				<td class="Text-right"><a title="Time From=<%=nvl(stats.getMinTimeFrom()) %>, Until=<%=nvl(stats.getMaxTimeUntil()) %>"><%=nvl(stats.getMinTimeFrom()) %></a></td>
			</tr>
			</tbody>
		</table>
		</td>
		<%
		    	if (i%8 == 0) {
		    		out.println("</tr><tr>");
		    	}
			}
		
		    /* 등록되지 않은 그룹들. 이거는 다시 고민한다. display 할지 말지. */
		    for (Map.Entry<String, JobGroupRunStats> entry : naJobGroupRunStatsMap.entrySet()) {
		    	JobGroupRunStats stats = entry.getValue();
				if (failOnly && stats.getEndFailCount() == 0) { /* failonly 체크시에는 endfail 건이 없으면 출력하지 않는다. */
					continue;
				}
		    	i++;
		%>
		<td class="jobstate_bgcolor_<%=stats.getState() %>_hover hand" onclick="javascript:onclickTD(event, '<%=stats.getJobGroupId()%>', '');" >
		<table class="Table njf-table__typea Width-100">
			<thead>
			<tr><th colspan=2><%=stats.getJobGroupId() %></th></tr>
			<tr><th colspan=2 class="jobdesc_font">N/A</th></tr>
			</thead>
			<tbody>
			<tr>
				<td class="Text-left"><%=printJobStateText(stats.getState())%></td>
				<td align="right"><%=printRunCount(stats.getRunTotalCount(), stats.getEndOkCount(), stats.getEndFailCount()) %></td>
			</tr>
			<tr>
				<td class="Text-left"><a title="Job Definitions" href="javascript:openJobDefListWin('<%=stats.getJobGroupId()%>', '')" ><%=stats.getJobDefCount() %></a>&nbsp; -> &nbsp;<a title="Job Instances" href="javascript:openJobInsListWin('<%=stats.getJobGroupId()%>', '')"><%=stats.getJobInsCount() %></a></td>
				<td class="Text-right"><a title="Time From=<%=nvl(stats.getMinTimeFrom()) %>, Until=<%=nvl(stats.getMaxTimeUntil()) %>"><%=nvl(stats.getMinTimeFrom()) %></a></td>
			</tr>
			</tbody>
		</table>
		</td>
		<%
				if (i%8 == 0) {
					out.println("</tr><tr>");
				}
		    }
		    
		    String reloadUrl = null; // encode 된놈.   GET 용
		    if (request.getQueryString()==null) {
		    	reloadUrl = request.getRequestURI();
		    	reloadUrl += "?dummy=1";
		    }else {
		    	reloadUrl = request.getRequestURI()+"?"+request.getQueryString();
		    	reloadUrl = reloadUrl.replaceAll("&pos_y.*", "");
		    }
		%>
	</table>

</div>
</center>
</div>
</div>
<jsp:include page="bottom.jsp" flush="true"/>
</body>
<script>
	function checkReload() {
		var posY = document.body.scrollTop;
		window.location.href='<%=reloadUrl%>&pos_y='+posY;
	}
<%
	String posY = nvl(request.getParameter("pos_y"));
	if (!Util.isBlank(posY)) {
		out.println("window.scrollTo(0, "+posY+")");
	}
%>
	if (document.form1.autoreload_interval.value != '0') {
	    setInterval('checkReload()', parseInt(document.form1.autoreload_interval.value) * 1000);
	}
</script>
</html>
