<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
	String uploadJobdefFilename = request.getParameter("upload_jobdef_filename");
	File   jobdefUploadFile     = new File(System.getProperty("NEXCORE_HOME")+"/tmp", uploadJobdefFilename);
	
	if (jobdefUploadFile == null) {
		throw new RuntimeException("File not uploaded");
	}
	
	JobDefinition jobdef = JobDefinitionUtil.readFromFile(jobdefUploadFile);

	ControllerAdminLocal admin = getControllerAdmin();
	Map  calendarMap = admin.getCalendarList();

%>
<html>
<head>
<jsp:include page="display_msg.jsp" flush="true"/>
<script src="./script/app/include-lib.js"></script>
<title>Job Definition Upload (<%=jobdef.getJobId()%>)</title>
</head>
<body onload="displayMsg();">
<center>
<%-- <table border="2" style = "border-collapse:collapse" bordercolor = "#000000" width="80%">
<tr>
    <td align="center" bgcolor="#DDFFDD" class="logo_title"><%=Label.get("jobdef")%> [<%=nvl(jobdef.getJobId())%>] Temporary</td>
</tr>
</table> --%>

<div class="header-wrap">
	<div class="header">
		<div class="header-title">
			<%=Label.get("jobdef")%> [<%=nvl(jobdef.getJobId())%>] Temporary
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<div class="popup-content-wrap Margin-bottom-10">

<div class="popup-content-title__wrap">
	<div class="content-title"><%=Label.get("job.program.info")%></div>
</div>

<%-- <br>
<table border="0" width="80%">
<tr><td colspan="100%" align="left"><font size="3"><b>¡á <%=Label.get("job.program.info")%></b></font></td></tr>
</table> --%>
<!-- <table border="2" style = "border-collapse:collapse" bordercolor = "#000000"  cellpadding="3" width="80%"> -->

<table class="Table njf-table__typea Width-100" >
    <colgroup>
        <col width="22%">
        <col>
    </colgroup>
<tr>
	<th><%=Label.get("job.jobid")%></th>
	<td><b><%=nvl(jobdef.getJobId())%></b></td>
</tr>
<tr>
	<th><%=Label.get("job.jobgroup")%></th>
	<td><%=nvl(jobdef.getJobGroupId())%></td>
</tr>
<tr>
	<th><%=Label.get("job.owner")%></th>
	<td><%=conv(jobdef.getOwner())%></td>
</tr>
<tr>
	<th><%=Label.get("job.desc")%></th>
	<td><font color="blue"><b>&nbsp;<%=getAppCode(jobdef.getJobId())%>&nbsp;&nbsp;<%=conv(jobdef.getDescription())%></b></font></td>
</tr>
<tr>
	<th><%=Label.get("job.jobtype")%></th>
	<td><%=getJobTypeText(jobdef.getJobType())+" ("+jobdef.getJobType()+")"%></td>
</tr>
<tr>
	<th><%=Label.get("job.agent")%></th>
	<td><%=jobdef.getAgentNode()%></td>
</tr>
<tr>
	<th><%=Label.get("job.component")%></th>
	<td><%=conv(jobdef.getComponentName())%></td>
</tr>
</table>
<%-- <br>
<table border="0" width="80%">
<tr><td colspan="100%" align="left"><font size="3"><b>¡á <%=Label.get("job.execution.condition")%></b></font></td></tr>
</table>
<table border="2" style = "border-collapse:collapse" bordercolor = "#000000"  cellpadding="3" width="80%"> --%>

<div class="popup-content-title__wrap">
	<div class="content-title"><%=Label.get("job.execution.condition")%></div>
</div>

<table class="Table njf-table__typea Width-100">
    <colgroup>
        <col width="22%">
        <col>
    </colgroup>
<tr>
	<th><%=Label.get("job.time")%></th>
	<td>[<%=nvl(jobdef.getTimeFrom())%>] ~ [<%=nvl(jobdef.getTimeUntil())%>]</td>
</tr>
<%
boolean isExactRepeat    = "EXACT".equals(jobdef.getRepeatIntvalGb());
boolean isStartEndRepeat = "START".equals(jobdef.getRepeatIntvalGb()) || "END".equals(jobdef.getRepeatIntvalGb());
%>
<tr>
	<th><%=Label.get("job.repeat")%></th>
	<td <%="Y".equals(jobdef.getRepeatYN()) ? "style=backgrouond:#ffcccc" : ""%>>
		<table class="Table njf-table__typea Width-100">
	    <tr>
	        <th class="Width-15" rowspan="3"><b><%=jobdef.getRepeatYN()%></b></th>
        	<th><b><%=printCancelLine("["+Label.get("job.repeat.intval")+"]", !isStartEndRepeat)%></b></th>
			<td><%=printCancelLine(jobdef.getRepeatIntval()+Label.get("common.second"), !isStartEndRepeat)%></td>
            <th><b>[<%=Label.get("job.repeat.intval.gb")%>]</b></th>
        	<td><%=nvl(jobdef.getRepeatIntvalGb())%></td>
        </tr>
        <tr>
            <th><b>[<%=Label.get("job.repeat.if.error")%>]</b></th>
        	<td><%=nvl(jobdef.getRepeatIfError())%></td>
            <th><b>[<%=Label.get("job.repeat.maxok")%>]</b></th>
        	<td><%=jobdef.getRepeatMaxOk()%></td>
        </tr>
        <tr>
            <th><b><%=printCancelLine("["+Label.get("job.repeat.exact.exp")+"]", !isExactRepeat)%></b></th>
        	<td colspan="3"><%=printCancelLine(nvl(jobdef.getRepeatExactExp()), !isExactRepeat)%></td>
        </tr>
        </table>
    </td>
</tr>
<tr>
	<th><%=Label.get("job.confirm.need.yn")%></th>
	<td><%=jobdef.getConfirmNeedYN()%></td>
</tr>
<tr>
	<th><%=Label.get("job.parallel.group")%></th>
	<td><%=nvl(jobdef.getParallelGroup())%></td>
</tr>
<tr>
	<th><%=Label.get("job.trigger")%></th>
	<td>
		<table class="Table njf-table__typea Width-100" id="prejob_table">
			<thead>
			<tr>
				<th><%=Label.get("job.trigger.okfail")%></th>
				<th><%=Label.get("job.trigger.id")%></th>
				<th><%=Label.get("job.trigger.count")%></th>
			</tr>
			</thead>
			<tbody>					
	<%
		int i=0;
		for (PostJobTrigger triggerJob : jobdef.getTriggerList()) {
		i++;
		
		String retVal = "";
		if("RETVAL".equals(triggerJob.getWhen())) {
			retVal = "[" + nvl(triggerJob.getCheckValue1()) + ":" + nvl(triggerJob.getCheckValue2()) + "]";
		}
	%>
			<tr>
				<td><%=nvl(triggerJob.getWhen())%> <%=retVal%></td>
				<td><a href="javascript:openJobDefinitionWin('<%=nvl(triggerJob.getTriggerJobId())%>');"><b><%=nvl(triggerJob.getTriggerJobId())%></b></a></td>
				<td><%=nvl(triggerJob.getJobInstanceCount())%></td>
			</tr>
	<%
		}
	%>
		</tbody>
		</table>
	</td>
</tr>
<tr>
	<th><%=Label.get("job.prejob")%></th>
	<td>
		<table class="Table njf-table__typea Width-100" id="prejob_table">
			<tr>
				<th><%=Label.get("job.prejob.id")%></th>
				<th><%=Label.get("job.prejob.okfail")%></th>
				<th><%=Label.get("job.prejob.andor")%></th>
			</tr>
<%
	i=0;
	for (PreJobCondition preJob : jobdef.getPreJobConditions()) {
	i++;
%>
			<tr>
				<td><%=nvl(preJob.getPreJobId())%></td>
				<td><%=nvl(preJob.getOkFailText())%></td>
				<td><%=nvl(preJob.getAndOr())%></td>
			</tr>
<%
	}
%>
		</table>
	</td>
</tr>
</table>
<%-- <br>
<table border="0" width="80%">
<tr><td colspan="100%" align="left"><font size="3"><b>¡á <%=Label.get("job.day.schedule")%></b></font></td></tr>
</table>
<table border="2" style = "border-collapse:collapse" bordercolor = "#000000"  cellpadding="3" width="80%"> --%>

<div class="popup-content-title__wrap">
	<div class="content-title"><%=Label.get("job.day.schedule")%></div>
</div>

<table class="Table njf-table__typea Width-100">
<colgroup><col width="22%"><col></colgroup>
<tr>
	<th><%=Label.get("job.schedule.type")%></th>
	<td><%=jobdef.getScheduleType()%></td>
</tr>
<tr>
	<th><%=Label.get("job.months")%></th>
	<td><%=nvl(jobdef.getMonths())%></td>
</tr>
<tr>
	<th><%=Label.get("job.dayofmonth.schedule.type")%></th>
	<td><%=jobdef.getDayOfMonthScheduleType()%></td>
</tr>
<tr>
	<th><%=Label.get("job.days")%></th>
	<td><%=nvl(jobdef.getDaysInMonth())%></td>
</tr>
<tr>
	<th><%=Label.get("job.calendar")%></th>
	<td>[<%=nvl(jobdef.getCalendarId())%>]<%=nvl(calendarMap.get(nvl(jobdef.getCalendarId())))%></td>
</tr>
<tr>
	<th><%=Label.get("job.weekday_monthday.type")%></th>
	<td><%=jobdef.getWeekdayMonthdayType()%></td>
</tr>
<tr>
	<th><%=Label.get("job.weekday")%></th>
	<td><%=nvl(jobdef.getDaysOfWeek())%></td>
</tr>
<tr>
	<th><%=Label.get("job.before_after.day")%></th>
	<td><%=nvl(jobdef.getBeforeAfterExp())%></td>
</tr>
<tr>
	<th><%=Label.get("job.shift1")%></th>
	<td><%=nvl(jobdef.getShiftExp())%></td>
</tr>
<tr>
	<th><%=Label.get("job.shift2")%></th>
	<td><%=nvl(jobdef.getShiftExp2())%></td>
</tr>
<tr>
	<th><%=Label.get("job.fixed")%></th>
	<td><%=nvl(jobdef.getFixedDays())%></td>
</tr>
<tr>
	<th><%=Label.get("job.reverse")%></th>
	<td><%=jobdef.isReverse() ? "Y" : "N"%></td>
</tr>
</table>
<%-- <br>
<table border="0" width="80%">
<tr><td colspan="100%" align="left"><font size="3"><b>¡á <%=Label.get("job.param")%></b></font></td></tr>
</table>
<table border="2" style = "border-collapse:collapse" bordercolor = "#000000" cellpadding="3" width="80%"> --%>

<div class="popup-content-title__wrap">
	<div class="content-title"><%=Label.get("job.param")%></div>
</div>

<table class="Table njf-table__typea Width-100 Margin-bottom-10">
<colgroup><col width="22%"><col></colgroup>
<tr>
	<th><%=Label.get("job.basedate")%></th>
	<td>Calendar:[<%=nvl(jobdef.getBaseDateCalId())%>]<%=nvl(calendarMap.get(nvl(jobdef.getBaseDateCalId())))%>, Logic:<%=nvl(jobdef.getBaseDateLogic())%></td>
</tr>
<tr>
	<th><%=Label.get("job.param")%></th>
	<td>
		<table class="Table njf-table__typea Width-100" id="param_table">
			<tr>
				<th class="Width-40"><%=Label.get("job.param.name")%></th>
				<th><%=Label.get("job.param.value")%></th>
			</tr>
<%
	i=0;
	for (Map.Entry<String, String> param : jobdef.getInParameters().entrySet()) {
	i++;
%>
			<tr id="param_<%=i%>">
				<td><%=conv(param.getKey())%></td>
				<td><%=conv(param.getValue())%></td>
			</tr>
<%
	}
%>
		</table>
	</td>
</tr>
</table>
<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close();" style="width:80px; height:35px;">
</div>
</center>
</body>
</html>
