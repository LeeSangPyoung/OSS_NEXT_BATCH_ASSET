<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	String printRunCount(int runTotalCount, int endOkCount, int endFailCount) {
		if (endFailCount > 0) {
			return endOkCount + " / <font color=red><b>"+ endFailCount +"</b></font> / "+runTotalCount;
		}else {
			return endOkCount + " / 0 / "+runTotalCount;
		}
	}

	String printJobStateText(String state) {
		return state == null ? "" : "<font class='jobstate_font_"+state+"'>"+JobInstance.getJobStateText(state)+"</font>";
	}

	String getProgressImage(String jobState) {
	    if ("O".equals(jobState)) {
	        return "./images/prgs1_endok.jpg";
	    }else if ("F".equals(jobState)) {
	        return "./images/prgs1_endfail.jpg";
	    }else if ("R".equals(jobState) || "P".equals(jobState)) {
	        return "./images/prgs1_running.jpg";
	    }else {
	    	return "./images/prgs2.jpg";
	    }
	}
	
	String printProgressImage(String state, double percentage) {
		if (percentage == 0) {
			return "";
		}else {
			double percentageWidth  = percentage / 4;
			double percentageWidth2 = 25 - percentageWidth;

			return 
				"<img src='"+getProgressImage(state)+"' height=10 width="+percentageWidth+"/>"+
				"<img src='./images/prgs2.jpg' height=10 width="+percentageWidth2 +"/>";
		}
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
    String jobfilter          = request.getParameter("jobfilter");
	String orderby            = nvl( request.getParameter("orderby"),  "getId");
    String orderdir           = nvl( request.getParameter("orderdir"), "ASC");
	
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
    
    List<JobGroupAttrDef>         jobGroupAttrDefs      = admin.getJobGroupAttrDefsByQuery("", " ORDER BY DISPLAY_ORDER ");
    
    for (Iterator iter = jobGroupAttrDefs.iterator(); iter.hasNext(); ) {
    	if (!((JobGroupAttrDef)iter.next()).isDisplayMonitor()) {
    		iter.remove(); /* display 할 필요없는 속성 메모리에서 제거 */
    	}
    }
%>
<link rel="stylesheet" href="jobdisplay.css" type="text/css" />
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

	function toggle_subtr(obj, groupid) {
		if (obj.src.lastIndexOf("plus") >= 0) {
			obj.src = "images/icon_minus.png";
		}else {
			obj.src = "images/icon_plus.png";
		}
        var filterLength = <%=jobFilterCodeList.size()%>;
        
        for (var i=1; i<=filterLength; i++) {
            var trlist = document.getElementById("subtr"+groupid+"_"+i);
            if (trlist != null) { 
				if (trlist.style.display == "none") {
					trlist.style.display = "table-row";
				}else {
					trlist.style.display = "none";
				}
        	}
		}
	}
	
	function gotoTileStyle() {
		document.form1.action="view_jobgroupmon_tile.jsp";
		doQuery();
	}

	function doExcelDownload() {
        document.form1.action="view_jobgroupmon_excel.jsp";
        document.form1.target='';
        document.form1.submit();
        document.form1.action="view_jobgroupmon.jsp";
    }

    function orderby(orderbyCol) {
    	var orderdir;
        if ('<%=orderby%>' == orderbyCol) {
            if ('<%=orderdir%>' == 'ASC') {
                orderdir = 'DESC';
            }else {
                orderdir = 'ASC';
            }
        }else {
            orderdir = 'ASC';
        }
        document.form1.orderby.value  = orderbyCol;
        document.form1.orderdir.value = orderdir;
        document.form1.submit();
    }
</script>

<center>
<div class="content-wrap">
<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("top.menu.jobgroupmon")%></div>
</div>

<form name="form1" action="view_jobgroupmon.jsp" method="get" style="margin-top:0;margin-bottom:0">
<input type="hidden" name="orderby" value=<%=nvl(orderby) %>>
<input type="hidden" name="orderdir" value=<%=nvl(orderdir) %>>
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

<span id="error_bar"></span>
	<table class="Width-100">
		<tbody>
		<tr>
			<td class="Text-left">
				<input type="button" class="Button" value="<%=Label.get("view_jobgroupmon.tilestyle")%>" onclick="gotoTileStyle();">
				<input type="button" class="Button" value="<%=Label.get("common.btn.download")%>" onclick="doExcelDownload();">
			</td>
			<td class="Text-right">
			    <label><%=Label.get("view_jobgroupmon.failonly")%></label>
			    <input class="Checkbox Margin-right-20" type="checkbox" name="failonly" value="1" <%=failOnly?"checked":""%>>
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
		</tbody>
	</table>
</form>
	<table class="Table Margin-bottom-10" id="tableList">
		<thead>
		<tr>
			<th style="width:3%; padding:2px;"></th>
			<th style="padding:2px;"><a href="javascript:orderby('getId');"><%=Label.get("jobgroup")%> ID<%=printSortMark(orderby, orderdir, "getId")%></a></th>
			<th style="padding:2px;"><a href="javascript:orderby('getName');"><%=Label.get("common.name")%><%=printSortMark(orderby, orderdir, "getName")%></a></th>
			<th style="padding:2px;"><a href="javascript:orderby('getDesc');"><%=Label.get("common.desc")%><%=printSortMark(orderby, orderdir, "getDesc")%></a></th>
			<%
			    for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
			%>
			<th style="padding:2px;"><a title="[<%=conv(attrDef.getId())%>] <%=conv(attrDef.getDesc())%>" href="javascript:orderby('attr_<%=conv(attrDef.getId())%>');"><%=conv(attrDef.getName())%><%=printSortMark(orderby, orderdir, "attr_"+conv(attrDef.getId()))%></a></th>
			<%
			    }
			%>
			
			<th style="padding:2px;"><%=Label.get("jobdef").replace(" ", "<br>")%></th>
			<th style="padding:2px;"><%=Label.get("jobins").replace(" ", "<br>")%></th>
			<th style="padding:2px;">From</th>
			<th style="padding:2px;">Until</th>
			<th style="padding:2px;"><%=Label.get("jobins.succcount") %>/<%=Label.get("jobins.failcount") %>/<%=Label.get("jobins.runcount") %></th>
			<th style="padding:2px;"><%=Label.get("common.state") %></th>
			<th style="padding:2px;"><%=Label.get("common.progress") %></th>
			<th style="padding:2px;" colspan=2>%</th>
			<th style="padding:2px;"><%=Label.get("jobins.last.starttime") %></th>
			<th style="padding:2px;"><%=Label.get("jobins.last.endtime") %></th>
		</tr>
		</thead>
		<tbody>
		<%
			int i=0;
		
		    for (JobGroup jobgroup : jobGroupTreeList) {
				JobGroupRunStats stats = jobGroupRunStatsMap.get(jobgroup.getId());
				if (failOnly && stats.getEndFailCount() == 0) { /* failonly 체크시에는 endfail 건이 없으면 출력하지 않는다. */
					continue;
				}
				
		%>
		
		<tr class='jobstate_bgcolor_<%=stats.getState()%>'>
			<td><%=(++i)%></td>
			<td class="Text-left"><%=printSpace(jobgroup.getDepth(), 4)%><img src="images/icon_tree_list_hide.png"><b><a href="javascript:openJobGroupWin('<%=conv(jobgroup.getId())%>');"><%=conv(jobgroup.getId())%></a></b>
			<%
					if (jobFilterCodeList != null && jobFilterCodeList.size() > 0) {
			%>
			<img src="images/icon_plus.png" style="cursor:pointer" onclick="javascript:toggle_subtr(this, '<%=jobgroup.getId()%>');">
			<%			
					}
			%>
			</td>
			<td class="Text-left"><%=getShortDescription(jobgroup.getName())%></td>
			<td class="Text-left"><%=getShortDescription(jobgroup.getDesc())%></td>
			<%
			    for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
			%>
			<td class="Text-left"><%=getShortDescription(jobgroup.getAttribute(attrDef.getId()))%></td>
			<%
			    }
			%>
			<td><a href="javascript:openJobDefListWin('<%=jobgroup.getId()%>', '<%=nvl(jobfilter)%>')"><b><%=stats.getJobDefCount() %></b></a></td>
			<td><a href="javascript:openJobInsListWin('<%=jobgroup.getId()%>', '<%=nvl(jobfilter)%>')"><b><%=stats.getJobInsCount() %></b></a></td>
			<td><%=nvl(stats.getMinTimeFrom()) %></td>
			<td><%=nvl(stats.getMaxTimeUntil()) %></td>
			<td><%=printRunCount(stats.getRunTotalCount(), stats.getEndOkCount(), stats.getEndFailCount()) %></td>
			<td><%=printJobStateText(stats.getState()) %></td>
			<td><%=stats.getProgressCurrent() %> / <%=stats.getProgressTotal() %></td>
			<td style="width:3px;"><%=(int)stats.getProgressPercentage()%></td>
			<td><%=printProgressImage(stats.getState(), stats.getProgressPercentage())%></td>
			<td><%=toDatetimeString(stats.getLastStartTime(), false) %></td>
			<td><%=toDatetimeString(stats.getLastEndTime(), false) %></td>
		</tr>
		<%
				if (jobFilterCodeList == null || jobFilterCodeList.size() == 0) {
					continue;
				}
		
				/* Job Filter 기준으로 sub tr 뿌리기. */
		        int ii=0;
				for (Map.Entry<String, String> entry : jobFilterCodeList.entrySet()) {
					String filterCode = entry.getKey();
					String filterName = entry.getValue();
					JobGroupRunStats stats2 = stats.getStatsByJobFilter(filterCode);
					if (stats2 == null) {
						stats2 = new JobGroupRunStats(stats.getJobGroupId());
					}
		%>
		<tr id="subtr<%=jobgroup.getId() %>_<%=(++ii)%>" style="display:none" class='jobstate_bgcolor_<%=stats.getState()%>'>
			<td></td>
			<td class="Text-center"><%=conv(filterCode)%></b></td>
			<td class="Text-left"><%="["+filterName+"] "+getShortDescription(jobgroup.getName())%></td>
			<td></td>
			<%
			    for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
			%>
			<td></td>
			<%
			    }
			%>
			<td><a href="javascript:openJobDefListWin('<%=jobgroup.getId()%>', '<%=nvl(filterCode)%>')"><b><%=stats2.getJobDefCount() %></b></a></td>
			<td><a href="javascript:openJobInsListWin('<%=jobgroup.getId()%>', '<%=nvl(filterCode)%>')"><b><%=stats2.getJobInsCount() %></b></a></td>
			<td><%=nvl(stats2.getMinTimeFrom()) %></td>
			<td><%=nvl(stats2.getMaxTimeUntil()) %></td>
			<td><%=printRunCount(stats2.getRunTotalCount(), stats2.getEndOkCount(), stats2.getEndFailCount()) %></td>
			<td><%=printJobStateText(stats2.getState()) %></td>
			<td><%=stats2.getProgressCurrent() %> / <%=stats2.getProgressTotal() %></td>
			<td style="width:3px;"><%=(int)stats2.getProgressPercentage()%></td>
			<td><%=printProgressImage(stats2.getState(), stats2.getProgressPercentage())%></td>
			<td><%=toDatetimeString(stats2.getLastStartTime(), false) %></td>
			<td><%=toDatetimeString(stats2.getLastEndTime(), false) %></td>
		</tr>
		<%
				}
		    }
		
		    /* 등록되지 않은 그룹들. 이거는 다시 고민한다. display 할지 말지. */
		    for (Map.Entry<String, JobGroupRunStats> entry : naJobGroupRunStatsMap.entrySet()) {
		    	JobGroupRunStats stats = entry.getValue();
				if (failOnly && stats.getEndFailCount() == 0) { /* failonly 체크시에는 endfail 건이 없으면 출력하지 않는다. */
					continue;
				}
		%>
		<tr class='jobstate_bgcolor_<%=stats.getState()%>'>
			<td><%=(++i)%></td>
			<td class="Text-left">&nbsp;&nbsp;<img src="images/icon_tree_list_hide.png"/> <b><%=conv(entry.getKey())%></b></td>
			<td class="Text-left">N/A</td>
			<td class="Text-left">N/A</td>
			<%
			    for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
			%>
			<td/>
			<%
			    }
			%>
			<td><a href="javascript:openJobDefListWin('<%=entry.getKey()%>', '<%=nvl(jobfilter)%>')"><b><%=stats.getJobDefCount() %></b></a></td>
			<td><a href="javascript:openJobInsListWin('<%=entry.getKey()%>', '<%=nvl(jobfilter)%>')"><b><%=stats.getJobInsCount() %></b></a></td>
			<td><%=nvl(stats.getMinTimeFrom()) %></td>
			<td><%=nvl(stats.getMaxTimeUntil()) %></td>
			<td><%=printRunCount(stats.getRunTotalCount(), stats.getEndOkCount(), stats.getEndFailCount()) %></td>
			<td><%=printJobStateText(stats.getState()) %></td>
			<td><%=stats.getProgressCurrent() %> / <%=stats.getProgressTotal() %></td>
			<td style="width:3px;"><%=(int)stats.getProgressPercentage()%></td>
			<td><%=printProgressImage(stats.getState(), stats.getProgressPercentage())%></td>
			<td><%=toDatetimeString(stats.getLastStartTime(), false) %></td>
			<td><%=toDatetimeString(stats.getLastEndTime(), false) %></td>
		</tr>
		
		<%
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
		</tbody>
	</table>

</div>
</center>
</div>
</div>
<jsp:include page="bottom.jsp" flush="true"/>
</body>
<script>
	$a.page(function() {
	    // 초기화 함수
	    this.init = function(id, param) {
	    	$("#tableList").css({'table-layout':'auto'});
	    }
	});
	
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
