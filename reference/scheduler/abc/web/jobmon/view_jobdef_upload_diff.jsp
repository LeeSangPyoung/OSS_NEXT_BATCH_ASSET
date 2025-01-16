<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
    // µÎ °ªÀÌ ´Ù¸£¸é »ö±ò Ç¥½Ã
    String printDiffColor(Object o1, Object o2) {
		boolean diff;
		if (o1 instanceof String || o2 instanceof String) {
			diff = nvl(o1).equals(nvl(o2));
		}else {
			diff = o1 == null ? o2 == null : o1.equals(o2);
		}
		
		return diff ? "" : "style='background:#FFBBDD;'";
    }
%>
<%
	String uploadJobdefFilename = request.getParameter("upload_jobdef_filename");
	File   jobdefUploadFile     = new File(System.getProperty("NEXCORE_HOME")+"/tmp", uploadJobdefFilename);
	
	if (jobdefUploadFile == null) {
		throw new RuntimeException("File not uploaded");
	}
	
	JobDefinition newjobdef = JobDefinitionUtil.readFromFile(jobdefUploadFile);
	
	ControllerAdminLocal admin = getControllerAdmin();
	AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));
	JobDefinition    oldjobdef = new JobDefinition();

	try {
		oldjobdef = admin.getJobDefinition(newjobdef.getJobId());
	}catch(Exception e) {
		/* In Add mode, old JobDefinition not exists */
    }

	Map calendarMap = admin.getCalendarList();
%>
<html>
<head>
<script src="./script/app/include-lib.js"></script>
<title>Job Definition Diff</title>
<script type="text/javascript">
    function openJobDefinitionWin(jobid) {
        window.open("view_jobdef_dtl.jsp?jobid="+jobid, "jobdef_"+jobid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }
</script> 

</head>
<body>
<center>

<div class="header-wrap">
	<div class="header">
		<div class="header-title">
			JobDefinition Upload Diff
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">

<table class="Table njf-table__typea Margin-bottom-10" >
    <colgroup>
        <col width="22%">
        <col>
    </colgroup>
<thead>
<tr>
	<th>#</th>
	<th><%=Label.get("job.req.diff.before")%></th>
	<th><%=Label.get("job.req.diff.after")%></th>
</tr>
</thead>
<tbody>
<tr>
	<th><%=Label.get("job.jobid")%></th>
	<td><b><%=nvl(oldjobdef.getJobId())%></b></td>
	<td <%=printDiffColor(oldjobdef.getJobId(),newjobdef.getJobId())%>><b><%=nvl(newjobdef.getJobId())%></b></td>
</tr>
<tr>
	<th><%=Label.get("job.jobgroup")%></th>
	<td><%=nvl(oldjobdef.getJobGroupId())%></td>
	<td <%=printDiffColor(oldjobdef.getJobGroupId(),newjobdef.getJobGroupId())%>><%=nvl(newjobdef.getJobGroupId())%></td>
</tr>
<tr>
	<th><%=Label.get("job.owner")%></th>
	<td><%=nvl(oldjobdef.getOwner())%></td>
	<td <%=printDiffColor(oldjobdef.getOwner(),newjobdef.getOwner())%>><%=nvl(newjobdef.getOwner())%></td>
</tr>
<tr>
	<th><%=Label.get("job.desc")%></th>
	<td><%=nvl(oldjobdef.getDescription())%></td>
	<td <%=printDiffColor(oldjobdef.getDescription(),newjobdef.getDescription())%>><%=nvl(newjobdef.getDescription())%></td>
</tr>
<tr>
	<th><%=Label.get("job.time")%></th>
	<td><%=nvl(oldjobdef.getTimeFrom())%> ~ <%=nvl(oldjobdef.getTimeUntil())%></td>
	<td <%=printDiffColor(nvl(oldjobdef.getTimeFrom())+"-"+nvl(oldjobdef.getTimeUntil()),nvl(newjobdef.getTimeFrom())+"-"+nvl(newjobdef.getTimeUntil()))%>><%=nvl(newjobdef.getTimeFrom())%> ~ <%=nvl(newjobdef.getTimeUntil())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat")%></th>
	<td><%=nvl(oldjobdef.getRepeatYN())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatYN(),newjobdef.getRepeatYN())%>><%=nvl(newjobdef.getRepeatYN())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat.intval")%></th>
	<td><%=nvl(oldjobdef.getRepeatIntval())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatIntval(),newjobdef.getRepeatIntval())%>><%=nvl(newjobdef.getRepeatIntval())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat.intval.gb")%></th>
	<td><%=nvl(oldjobdef.getRepeatIntvalGb())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatIntvalGb(),newjobdef.getRepeatIntvalGb())%>><%=nvl(newjobdef.getRepeatIntvalGb())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat.if.error")%></th>
	<td><%=nvl(oldjobdef.getRepeatIfError())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatIfError(),newjobdef.getRepeatIfError())%>><%=nvl(newjobdef.getRepeatIfError())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat.maxok")%></th>
	<td><%=nvl(oldjobdef.getRepeatMaxOk())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatMaxOk(),newjobdef.getRepeatMaxOk())%>><%=nvl(newjobdef.getRepeatMaxOk())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat.exact.exp")%></th>
	<td><%=nvl(oldjobdef.getRepeatExactExp())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatExactExp(),newjobdef.getRepeatExactExp())%>><%=nvl(newjobdef.getRepeatExactExp())%></td>
</tr>
<tr>
	<th><%=Label.get("job.confirm.need.yn")%></th>
	<td><%=nvl(oldjobdef.getConfirmNeedYN())%></td>
	<td <%=printDiffColor(oldjobdef.getConfirmNeedYN(),newjobdef.getConfirmNeedYN())%>><%=nvl(newjobdef.getConfirmNeedYN())%></td>
</tr>
<tr>
	<th><%=Label.get("job.parallel.group")%></th>
	<td><%=nvl(oldjobdef.getParallelGroup())%></td>
	<td <%=printDiffColor(oldjobdef.getParallelGroup(),newjobdef.getParallelGroup())%>><%=nvl(newjobdef.getParallelGroup())%></td>
</tr>
<tr>
	<th><%=Label.get("job.jobtype")%></th>
	<td><%=nvl(oldjobdef.getJobType())%></td>
	<td <%=printDiffColor(oldjobdef.getJobType(),newjobdef.getJobType())%>><%=nvl(newjobdef.getJobType())%></td>
</tr>
<tr>
	<th><%=Label.get("job.agent")%></th>
	<td><%=nvl(oldjobdef.getAgentNode())%></td>
	<td <%=printDiffColor(oldjobdef.getAgentNode(),newjobdef.getAgentNode())%>><%=nvl(newjobdef.getAgentNode())%></td>
</tr>
<tr>
	<th><%=Label.get("job.component")%></th>
	<td><%=nvl(oldjobdef.getComponentName())%></td>
	<td <%=printDiffColor(oldjobdef.getComponentName(),newjobdef.getComponentName())%>><%=nvl(newjobdef.getComponentName())%></td>
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
	for (PostJobTrigger triggerJob : oldjobdef.getTriggerList()) {
	i++;
	
	String retVal = "";
	if("RETVAL".equals(triggerJob.getWhen())) {
		retVal = "[" + nvl(triggerJob.getCheckValue1()) + ":" + nvl(triggerJob.getCheckValue2()) + "]";
	}
%>
			<tr id="prejob_<%=i%>">
				<td> <%=nvl(triggerJob.getWhen())%> <%=retVal%> </td>
				<td><a href="javascript:openJobDefinitionWin('<%=triggerJob.getTriggerJobId()%>');"><%=triggerJob.getTriggerJobId()%></a></td>
				<td> <%=nvl(triggerJob.getJobInstanceCount())%> </td>
			</tr>
<%
	}
%>
			</tbody>
		</table>
	</td>
	<td <%=printDiffColor(oldjobdef.getTriggerList(),newjobdef.getTriggerList())%>>
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
	i=0;
	for (PostJobTrigger triggerJob : newjobdef.getTriggerList()) {
	i++;
	
	String retVal = "";
	if("RETVAL".equals(triggerJob.getWhen())) {
		retVal = "[" + nvl(triggerJob.getCheckValue1()) + ":" + nvl(triggerJob.getCheckValue2()) + "]";
	}
%>
			<tr id="prejob_<%=i%>">
				<td> <%=nvl(triggerJob.getWhen())%> <%=retVal%> </td>
				<td><a href="javascript:openJobDefinitionWin('<%=triggerJob.getTriggerJobId()%>');"><%=triggerJob.getTriggerJobId()%></a></td>
				<td> <%=nvl(triggerJob.getJobInstanceCount())%> </td>
			</tr>
<%
	}
%>
			</tbody>
		</table>
	</td>
</tr>
<tr>
	<th><%=Label.get("job.schedule.type")%></th>
	<td><%=nvl(oldjobdef.getScheduleType())%> </td>
	<td <%=printDiffColor(oldjobdef.getScheduleType(),newjobdef.getScheduleType())%>><%=nvl(newjobdef.getScheduleType())%> </td>
</tr>
<tr>
	<th><%=Label.get("job.months")%></th>
	<td><%=nvl(oldjobdef.getMonths())%></td>
	<td <%=printDiffColor(oldjobdef.getMonths(),newjobdef.getMonths())%>><%=nvl(newjobdef.getMonths())%></td>
</tr>
<tr>
	<th><%=Label.get("job.dayofmonth.schedule.type")%></th>
	<td><%=nvl(oldjobdef.getDayOfMonthScheduleType())%> </td>
	<td <%=printDiffColor(oldjobdef.getDayOfMonthScheduleType(),newjobdef.getDayOfMonthScheduleType())%>><%=nvl(newjobdef.getDayOfMonthScheduleType())%> </td>
</tr>
<tr>
	<th><%=Label.get("job.days")%></th>
	<td><%=nvl(oldjobdef.getDaysInMonth())%></td>
	<td <%=printDiffColor(oldjobdef.getDaysInMonth(),newjobdef.getDaysInMonth())%>><%=nvl(newjobdef.getDaysInMonth())%></td>
</tr>
<tr>
	<th><%=Label.get("job.calendar")%></th>
	<td><%="["+nvl(oldjobdef.getCalendarId())+"]"+nvl(calendarMap.get(nvl(oldjobdef.getCalendarId())))%></td>
	<td <%=printDiffColor(oldjobdef.getCalendarId(),newjobdef.getCalendarId())%>><%="["+nvl(newjobdef.getCalendarId())+"]"+nvl(calendarMap.get(nvl(newjobdef.getCalendarId())))%></td>
</tr>
<tr>
	<th><%=Label.get("job.calendar.expression")%></th>
	<td><%=nvl(oldjobdef.getCalendarExps())%> </td>
	<td <%=printDiffColor(oldjobdef.getCalendarExps(),newjobdef.getCalendarExps())%>><%=nvl(newjobdef.getCalendarExps())%> </td>
</tr>
<tr>
	<th><%=Label.get("job.weekday_monthday.type")%></tdh>
	<td><%=nvl(oldjobdef.getWeekdayMonthdayType())%> </td>
	<td <%=printDiffColor(oldjobdef.getWeekdayMonthdayType(),newjobdef.getWeekdayMonthdayType())%>><%=nvl(newjobdef.getWeekdayMonthdayType())%> </td>
</tr>
<tr>
	<th><%=Label.get("job.weekday")%></th>
	<td><%=nvl(oldjobdef.getDaysOfWeek())%></td>
	<td <%=printDiffColor(oldjobdef.getDaysOfWeek(),newjobdef.getDaysOfWeek())%>><%=nvl(newjobdef.getDaysOfWeek())%></td>
</tr>
<tr>
	<th><%=Label.get("job.before_after.day")%></th>
	<td><%=nvl(oldjobdef.getBeforeAfterExp())%></td>
	<td <%=printDiffColor(oldjobdef.getBeforeAfterExp(),newjobdef.getBeforeAfterExp())%>><%=nvl(newjobdef.getBeforeAfterExp())%></td>
</tr>
<tr>
	<th><%=Label.get("job.shift1")%></th>
	<td><%=nvl(oldjobdef.getShiftExp())%></td>
	<td <%=printDiffColor(oldjobdef.getShiftExp(),newjobdef.getShiftExp())%>><%=nvl(newjobdef.getShiftExp())%></td>
</tr>
<tr>
	<th><%=Label.get("job.shift2")%></th>
	<td><%=nvl(oldjobdef.getShiftExp2())%></td>
	<td <%=printDiffColor(oldjobdef.getShiftExp2(),newjobdef.getShiftExp2())%>><%=nvl(newjobdef.getShiftExp2())%></td>
</tr>
<tr>
	<th><%=Label.get("job.fixed")%></th>
	<td><%=nvl(oldjobdef.getFixedDays())%></td>
	<td <%=printDiffColor(oldjobdef.getFixedDays(),newjobdef.getFixedDays())%>><%=nvl(newjobdef.getFixedDays())%></td>
</tr>
<tr>
    <th><%=Label.get("job.reverse")%></th>
    <td><%=nvl(oldjobdef.isReverse()?"Y":"N")%></td>
    <td <%=printDiffColor(oldjobdef.isReverse(),newjobdef.isReverse())%>><%=nvl(newjobdef.isReverse()?"Y":"N")%></td>
</tr>
<tr>
	<th><%=Label.get("job.basedate.calendar")%></th>
	<td><%="["+nvl(oldjobdef.getBaseDateCalId())+"]"+nvl(calendarMap.get(nvl(oldjobdef.getBaseDateCalId())))%></td>
	<td <%=printDiffColor(oldjobdef.getBaseDateCalId(),newjobdef.getBaseDateCalId())%>><%="["+nvl(newjobdef.getBaseDateCalId())+"]"+nvl(calendarMap.get(nvl(newjobdef.getBaseDateCalId())))%></td>
</tr>
<tr>
	<th><%=Label.get("job.basedate.logic")%></th>
	<td><%=nvl(oldjobdef.getBaseDateLogic())%></td>
	<td <%=printDiffColor(oldjobdef.getBaseDateLogic(),newjobdef.getBaseDateLogic())%>><%=nvl(newjobdef.getBaseDateLogic())%></td>
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
	for (PreJobCondition preJob : oldjobdef.getPreJobConditions()) {
	i++;
%>
			<tr id="prejob_<%=i%>">
				<td> <%=preJob.getPreJobId()%></td>
				<td> <%=preJob.getOkFailText()%> </td>
				<td> <%=preJob.getAndOr()%> </td>
			</tr>
<%
	}
%>
		</table>
	</td>
	<td <%=printDiffColor(oldjobdef.getPreJobConditions(),newjobdef.getPreJobConditions())%>>
		<table class="Table njf-table__typea Width-100" id="prejob_table">
			<tr>
				<th><%=Label.get("job.prejob.id")%></th>
				<th><%=Label.get("job.prejob.okfail")%></th>
				<th><%=Label.get("job.prejob.andor")%></th>
			</tr>
<%
	i=0;
	for (PreJobCondition preJob : newjobdef.getPreJobConditions()) {
	i++;
%>
			<tr id="prejob_<%=i%>">
				<td> <%=preJob.getPreJobId()%></td>
				<td> <%=preJob.getOkFailText()%> </td>
				<td> <%=preJob.getAndOr()%> </td>
			</tr>
<%
	}
%>
		</table>
	</td>
</tr>
<tr>
	<th><%=Label.get("job.param")%></th>
	<td>
		<table class="Table njf-table__typea Width-100" id="param_table">	
			<tr>
				<th><%=Label.get("job.param.name")%></th>
				<th><%=Label.get("job.param.value")%></th>
			</tr>
<%
	i=0;
	for (Map.Entry param : oldjobdef.getInParameters().entrySet()) {
	i++;
%>
			<tr id="param_<%=i%>">
				<td><%=param.getKey()%></td>
				<td><%=nvl(param.getValue())%></td>
			</tr>
<%
	}
%>
		</table>
	</td>
	<td <%=printDiffColor(oldjobdef.getInParameters(),newjobdef.getInParameters())%>>
		<table class="Table njf-table__typea Width-100" id="param_table">	
			<tr>
				<th><%=Label.get("job.param.name")%></th>
				<th><%=Label.get("job.param.value")%></th>
			</tr>
<%
	i=0;
	for (Map.Entry param : newjobdef.getInParameters().entrySet()) {
	i++;
%>
			<tr id="param_<%=i%>">
				<td><%=param.getKey()%></td>
				<td><%=nvl(param.getValue())%></td>
			</tr>
<%
	}
%>
		</table>
	</td>
</tr>

<tr>
	<th><%=Label.get("job.createtime")%></th>
	<td><%=toDatetimeString(oldjobdef.getCreateTime(), false)%></td>
	<td></td>
</tr>
<tr>
	<th><%=Label.get("job.lastmodifytime")%></th>
	<td><%=toDatetimeString(DateUtil.getTimestamp(oldjobdef.getLastModifyTime()), false)%></td>
	<td></td>
</tr>
</tbody>
</table>

<table class="Width-100">
	<tr>
		<td class="Text-center">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()">
		</td>
	</tr>
</table>
</div>
</center>
</body>

</html>
