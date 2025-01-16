<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	String printErrorCountFont(int endFailCount) {
		if (endFailCount > 0) {
			return "<font color=red>"+ endFailCount +"</font>";
		}else {
			return "0";
		}
	}

	String printJobStateText(String state) {
		return state == null ? "" : "<font class='jobstate_font_"+state+"'>"+JobInstance.getJobStateText(state)+"</font>";
	}

%>
<%
    if (!checkLogin(request, response)) return; 

	long current = System.currentTimeMillis();
	String currentDateTime = formatDatetime(current, "yyyyMMdd_HHmmss");
	
	response.setContentType("application/vnd.ms-excel; charset=euc-kr");
	response.setHeader("Content-Disposition", "attachment;filename=jobgroupmon-" + currentDateTime + ".xls");
%>
<html>
<head>
	<meta http-equiv=Content-Type content="text/html">
	<meta name=ProgId content=Excel.Sheet>
</head>
<jsp:include page="common_query_jobgroupmon.jsp"/>
<%
	// 조회 조건
	boolean showNoInsGroup    = Util.toBoolean(request.getParameter("shownoinsgroup"), false); /* 인스턴스없는 그룹 포함? */
	boolean failOnly          = Util.toBoolean(request.getParameter("failonly"), false); /* fail 건만 조회함 */
	
	Map<String, String>           jobFilterCodeList     = getJobFilterCodeList(request);
	List<JobGroup>                jobGroupTreeList      = (List<JobGroup>)request.getAttribute("jobGroupTreeList");
    Map<String, JobGroupRunStats> jobGroupRunStatsMap   = (Map<String, JobGroupRunStats>)request.getAttribute("jobGroupRunStatsMap");
    Map<String, JobGroupRunStats> naJobGroupRunStatsMap = (Map<String, JobGroupRunStats>)request.getAttribute("naJobGroupRunStatsMap");
%>
<style>
.jobstate_font_I { font-weight:bold; color:#000000; }
.jobstate_font_W { font-weight:bold; color:#000000; }
.jobstate_font_O { font-weight:bold; color:#0000CD; }
.jobstate_font_F { font-weight:bold; color:#EB0000; }
.jobstate_font_R { font-weight:bold; color:#DD8200; }
.jobstate_font_P { font-weight:bold; color:#52E222; }
.jobstate_font_S { font-weight:bold; color:#8B4513; }
.jobstate_font_G { font-weight:bold; color:#008080; }
.jobstate_font_X { font-weight:bold; color:#239999; }

.jobstate_bgcolor_I_hover, .jobstate_bgcolor_I { background-color:#EEEEEE;}
.jobstate_bgcolor_W_hover, .jobstate_bgcolor_W { background-color:#EEEEEE;}
.jobstate_bgcolor_O_hover, .jobstate_bgcolor_O { background-color:#FFFFFE;}
.jobstate_bgcolor_F_hover, .jobstate_bgcolor_F { background-color:#FFBBBB;}
.jobstate_bgcolor_R_hover, .jobstate_bgcolor_R { background-color:#FFFF88;}
.jobstate_bgcolor_P_hover, .jobstate_bgcolor_P { background-color:#AAAA00;}
.jobstate_bgcolor_S_hover, .jobstate_bgcolor_S { background-color:#EEEEEE;}
.jobstate_bgcolor_G_hover, .jobstate_bgcolor_G { background-color:#60A0A0;}
.jobstate_bgcolor_X_hover, .jobstate_bgcolor_X { background-color:#99EEEE;}
</style>
<br>
<table>
<tr>
	<td colspan="17" align=center><font size="4"><b><%=Label.get("job.jobgroup")%> (<%=toDatetimeString(current, false)%>)</b></font></td>
</tr>
</table>
<br>
<table border="1" style = "border-collapse:collapse" bordercolor = "#a0a0a0" >
<tr bgcolor="#DDDDFF" align="center">
<td></td>
<td><%=Label.get("jobgroup")%> ID</td>
<td><%=Label.get("common.name")%></td>
<td><%=Label.get("common.desc")%></td>
<td><%=Label.get("jobdef")%></td>
<td><%=Label.get("jobins")%></td>
<td>From</td>
<td>Until</td>
<td><%=Label.get("jobins.succcount") %></td>
<td><%=Label.get("jobins.failcount") %></td>
<td><%=Label.get("jobins.runcount") %></td>
<td><%=Label.get("common.state") %></td>
<td><%=Label.get("common.progress") %> Curr</td>
<td><%=Label.get("common.progress") %> Total</td>
<td>%</td>
<td><%=Label.get("jobins.last.starttime") %></td>
<td><%=Label.get("jobins.last.endtime") %></td>
</tr>
<%
	int i=0;

    for (JobGroup jobgroup : jobGroupTreeList) {
		JobGroupRunStats stats = jobGroupRunStatsMap.get(jobgroup.getId());
		if (failOnly && stats.getEndFailCount() == 0) { /* failonly 체크시에는 endfail 건이 없으면 출력하지 않는다. */
			continue;
		}
		
		if (!showNoInsGroup && stats.getJobInsCount() == 0) { /* instanceonly 체크시에는 instance 가 없으면 출력하지 않는다. */
			continue;
		}
%>
<tr align="center" class='jobstate_bgcolor_<%=stats.getState()%>'>
<td><%=(++i)%></td>
<td align="left"><b><%=conv(jobgroup.getId())%></b></td>
<td align='left'><%=jobgroup.getName()%></td>
<td align='left'><%=jobgroup.getDesc()%></td>
<td><b><%=stats.getJobDefCount() %></b></td>
<td><b><%=stats.getJobInsCount() %></b></td>
<td><%=nvl(stats.getMinTimeFrom()) %></td>
<td><%=nvl(stats.getMaxTimeUntil()) %></td>
<td><%=stats.getEndOkCount() %></td>
<td><%=printErrorCountFont(stats.getEndFailCount()) %></td>
<td><%=stats.getRunTotalCount() %></td>
<td><%=printJobStateText(stats.getState()) %></td>
<td><%=stats.getProgressCurrent() %></td>
<td><%=stats.getProgressTotal() %></td>
<td><%=(int)stats.getProgressPercentage()%></td>
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
<tr align="center" class='jobstate_bgcolor_<%=stats.getState()%>'>
<td></td>
<td align="center"><%=conv(filterCode)%></b></td>
<td align='left'><%="["+filterName+"] "+jobgroup.getName()%></td>
<td></td>
<td><b><%=stats2.getJobDefCount() %></b></td>
<td><b><%=stats2.getJobInsCount() %></b></td>
<td><%=nvl(stats2.getMinTimeFrom()) %></td>
<td><%=nvl(stats2.getMaxTimeUntil()) %></td>
<td><%=stats2.getEndOkCount() %></td>
<td><%=printErrorCountFont(stats2.getEndFailCount()) %></td>
<td><%=stats2.getRunTotalCount() %></td>
<td><%=printJobStateText(stats2.getState()) %></td>
<td><%=stats2.getProgressCurrent() %></td>
<td><%=stats2.getProgressTotal() %></td>
<td><%=(int)stats2.getProgressPercentage()%></td>
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
<tr align="center" class='jobstate_bgcolor_<%=stats.getState()%>'>
<td><%=(++i)%></td>
<td align="left"><b><%=conv(entry.getKey())%></b></td>
<td align='left'>N/A</td>
<td align='left'>N/A</td>
<td><b><%=stats.getJobDefCount() %></b></td>
<td><b><%=stats.getJobInsCount() %></b></td>
<td><%=nvl(stats.getMinTimeFrom()) %></td>
<td><%=nvl(stats.getMaxTimeUntil()) %></td>
<td><%=stats.getEndOkCount() %></td>
<td><%=printErrorCountFont(stats.getEndFailCount()) %></td>
<td><%=stats.getRunTotalCount() %></td>
<td><%=printJobStateText(stats.getState()) %></td>
<td><%=stats.getProgressCurrent() %></td>
<td><%=stats.getProgressTotal() %></td>
<td><%=(int)stats.getProgressPercentage()%></td>
<td><%=toDatetimeString(stats.getLastStartTime(), false) %></td>
<td><%=toDatetimeString(stats.getLastEndTime(), false) %></td>
</tr>
<%
    }
%>
</table>
</center>
</body>
</html>
