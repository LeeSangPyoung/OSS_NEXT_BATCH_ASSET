<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
    // 두 값이 다르면 색깔 표시
	String printDiffColor(Object o1, Object o2) {
	    boolean diff;
	    if (o1 instanceof String || o2 instanceof String) {
	        diff = nvl(o1).equals(nvl(o2));
	    }else {
	        diff = o1 == null ? o2 == null : o1.equals(o2);
	    }
	    
	    return diff ? "" : "style='background:#FFBBDD'";
	}

%>
<%
	String reqno = request.getParameter("reqno");
	String jobid = request.getParameter("jobid");

	ControllerAdminLocal admin = getControllerAdmin();
	JobDefinition    oldjobdef = new JobDefinition();
	JobDefinitionStg jobdefstg = admin.getJobDefinitionStg(reqno, jobid);

	if ("edit".equals(jobdefstg.getReqType())) { // 변경 요청일 경우는 이전것과 비교하여 보여준다.
    	try {
    	    oldjobdef = admin.getJobDefinition(jobid);
    	}catch(Exception e) {
			e.printStackTrace();
    	}
    }
%>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" /> --> 
<script src="./script/app/include-lib.js"></script>
<title>Job Definition Form</title>
<script type="text/javascript">
	$a.page(function() {
	    // 초기화 함수
	    this.init = function(id, param) {
	    	$("[id^='tableList']").css({'table-layout':'auto'});
	    }
	});

    function openJobDefinitionWin(jobid) {
        window.open("view_jobdef_dtl.jsp?jobid="+jobid, "jobdef_"+jobid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }

	function check_submit() {
		document.form1.lastPreJobIndex.value = lastPreJobSeq;
		document.form1.lastParamIndex.value  = lastParamSeq;
	}

</script> 

</head>
<body>
<center>

<div class="header-wrap">
	<div class="header">
		<div class="header-title">
			<%=Label.get("job.req.detail.title")%>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="팝업창 닫기" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">

<%-- <font size="5">
<%=Label.get("job.req.detail.title")%>
</font>
<br><br> --%>

<table class="Table njf-table__typea Width-100 Margin-bottom-10" >
    <colgroup>
        <col width="22%">
        <col>
    </colgroup>
<thead>
<tr>
	<th class="Width-5">#</th>
	<th><%=Label.get("job.req.diff.before")%></th>
	<th><%=Label.get("job.req.diff.after")%></th>
</tr>
</thead>
<tbody>
<tr>
	<th><%=Label.get("job.jobid")%></th>
	<td><b><%=nvl(oldjobdef.getJobId())%></b></td>
	<td <%=printDiffColor(oldjobdef.getJobId(),jobdefstg.getJobId())%>><b><%=nvl(jobdefstg.getJobId())%></b></td>
</tr>
<tr>
	<th><%=Label.get("job.jobgroup")%></th>
	<td><%=nvl(oldjobdef.getJobGroupId())%></td>
	<td <%=printDiffColor(oldjobdef.getJobGroupId(),jobdefstg.getJobGroupId())%>><%=nvl(jobdefstg.getJobGroupId())%></td>
</tr>
<tr>
	<th><%=Label.get("job.owner")%></th>
	<td><%=conv(oldjobdef.getOwner())%></td>
	<td <%=printDiffColor(oldjobdef.getOwner(),jobdefstg.getOwner())%>><%=conv(jobdefstg.getOwner())%></td>
</tr>
<tr>
	<th><%=Label.get("job.desc")%></th>
	<td><%=conv(oldjobdef.getDescription())%></td>
	<td <%=printDiffColor(oldjobdef.getDescription(),jobdefstg.getDescription())%>><%=conv(jobdefstg.getDescription())%></td>
</tr>
<tr>
	<th><%=Label.get("job.time")%></th>
	<td><%=nvl(oldjobdef.getTimeFrom())%> ~ <%=nvl(oldjobdef.getTimeUntil())%></td>
	<td <%=printDiffColor(nvl(oldjobdef.getTimeFrom())+"-"+nvl(oldjobdef.getTimeUntil()),nvl(jobdefstg.getTimeFrom())+"-"+nvl(jobdefstg.getTimeUntil()))%>><%=nvl(jobdefstg.getTimeFrom())%> ~ <%=nvl(jobdefstg.getTimeUntil())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat")%></th>
	<td><%=nvl(oldjobdef.getRepeatYN())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatYN(),jobdefstg.getRepeatYN())%>><%=nvl(jobdefstg.getRepeatYN())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat.intval")%></th>
	<td><%=nvl(oldjobdef.getRepeatIntval())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatIntval(),jobdefstg.getRepeatIntval())%>><%=nvl(jobdefstg.getRepeatIntval())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat.intval.gb")%></th>
	<td><%=nvl(oldjobdef.getRepeatIntvalGb())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatIntvalGb(),jobdefstg.getRepeatIntvalGb())%>><%=nvl(jobdefstg.getRepeatIntvalGb())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat.if.error")%></th>
	<td><%=nvl(oldjobdef.getRepeatIfError())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatIfError(),jobdefstg.getRepeatIfError())%>><%=nvl(jobdefstg.getRepeatIfError())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat.maxok")%></th>
	<td><%=nvl(oldjobdef.getRepeatMaxOk())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatMaxOk(),jobdefstg.getRepeatMaxOk())%>><%=nvl(jobdefstg.getRepeatMaxOk())%></td>
</tr>
<tr>
	<th><%=Label.get("job.repeat.exact.exp")%></th>
	<td><%=nvl(oldjobdef.getRepeatExactExp())%></td>
	<td <%=printDiffColor(oldjobdef.getRepeatExactExp(),jobdefstg.getRepeatExactExp())%>><%=nvl(jobdefstg.getRepeatExactExp())%></td>
</tr>
<tr>
	<th><%=Label.get("job.confirm.need.yn")%></th>
	<td><%=nvl(oldjobdef.getConfirmNeedYN())%></td>
	<td <%=printDiffColor(oldjobdef.getConfirmNeedYN(),jobdefstg.getConfirmNeedYN())%>><%=nvl(jobdefstg.getConfirmNeedYN())%></td>
</tr>
<tr>
	<th><%=Label.get("job.parallel.group")%></th>
	<td><%=nvl(oldjobdef.getParallelGroup())%></td>
	<td <%=printDiffColor(oldjobdef.getParallelGroup(),jobdefstg.getParallelGroup())%>><%=nvl(jobdefstg.getParallelGroup())%></td>
</tr>
<tr>
	<th><%=Label.get("job.jobtype")%></th>
	<td><%=nvl(oldjobdef.getJobType())%></td>
	<td <%=printDiffColor(oldjobdef.getJobType(),jobdefstg.getJobType())%>><%=nvl(jobdefstg.getJobType())%></td>
</tr>
<tr>
	<th><%=Label.get("job.agent")%></th>
	<td><%=nvl(oldjobdef.getAgentNode())%></td>
	<td <%=printDiffColor(oldjobdef.getAgentNode(),jobdefstg.getAgentNode())%>><%=nvl(jobdefstg.getAgentNode())%></td>
</tr>
<tr>
	<th><%=Label.get("job.component")%></th>
	<td><%=conv(oldjobdef.getComponentName())%></td>
	<td <%=printDiffColor(oldjobdef.getComponentName(),jobdefstg.getComponentName())%>><%=conv(jobdefstg.getComponentName())%></td>
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
	<td <%=printDiffColor(oldjobdef.getTriggerList(),jobdefstg.getTriggerList())%>>
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
	for (PostJobTrigger triggerJob : jobdefstg.getTriggerList()) {
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
	<td <%=printDiffColor(oldjobdef.getScheduleType(),jobdefstg.getScheduleType())%>><%=nvl(jobdefstg.getScheduleType())%> </td>
</tr>
<tr>
	<th><%=Label.get("job.months")%></th>
	<td><%=nvl(oldjobdef.getMonths())%></td>
	<td <%=printDiffColor(oldjobdef.getMonths(),jobdefstg.getMonths())%>><%=nvl(jobdefstg.getMonths())%></td>
</tr>
<tr>
	<th><%=Label.get("job.dayofmonth.schedule.type")%></th>
	<td><%=nvl(oldjobdef.getDayOfMonthScheduleType())%> </td>
	<td <%=printDiffColor(oldjobdef.getDayOfMonthScheduleType(),jobdefstg.getDayOfMonthScheduleType())%>><%=nvl(jobdefstg.getDayOfMonthScheduleType())%> </td>
</tr>
<tr>
	<th><%=Label.get("job.days")%></th>
	<td><%=nvl(oldjobdef.getDaysInMonth())%></td>
	<td <%=printDiffColor(oldjobdef.getDaysInMonth(),jobdefstg.getDaysInMonth())%>><%=nvl(jobdefstg.getDaysInMonth())%></td>
</tr>
<tr>
	<th><%=Label.get("job.calendar")%></th>
	<td><%=nvl(oldjobdef.getCalendarId()) %></td>
	<td <%=printDiffColor(oldjobdef.getCalendarId(),jobdefstg.getCalendarId())%>><%=nvl(jobdefstg.getCalendarId()) %></td>
</tr>
<tr>
	<th><%=Label.get("job.calendar.expression")%></th>
	<td><%=nvl(oldjobdef.getCalendarExps())%> </td>
	<td <%=printDiffColor(oldjobdef.getCalendarExps(),jobdefstg.getCalendarExps())%>><%=nvl(jobdefstg.getCalendarExps())%> </td>
</tr>
<tr>
	<th><%=Label.get("job.weekday_monthday.type")%></th>
	<td><%=nvl(oldjobdef.getWeekdayMonthdayType())%> </td>
	<td <%=printDiffColor(oldjobdef.getWeekdayMonthdayType(),jobdefstg.getWeekdayMonthdayType())%>><%=nvl(jobdefstg.getWeekdayMonthdayType())%> </td>
</tr>
<tr>
	<th><%=Label.get("job.weekday")%></th>
	<td><%=nvl(oldjobdef.getDaysOfWeek())%></td>
	<td <%=printDiffColor(oldjobdef.getDaysOfWeek(),jobdefstg.getDaysOfWeek())%>><%=nvl(jobdefstg.getDaysOfWeek())%></td>
</tr>
<tr>
	<th><%=Label.get("job.before_after.day")%></th>
	<td><%=nvl(oldjobdef.getBeforeAfterExp())%></td>
	<td <%=printDiffColor(oldjobdef.getBeforeAfterExp(),jobdefstg.getBeforeAfterExp())%>><%=nvl(jobdefstg.getBeforeAfterExp())%></td>
</tr>
<tr>
	<th><%=Label.get("job.shift1")%></th>
	<td><%=nvl(oldjobdef.getShiftExp())%></td>
	<td <%=printDiffColor(oldjobdef.getShiftExp(),jobdefstg.getShiftExp())%>><%=nvl(jobdefstg.getShiftExp())%></td>
</tr>
<tr>
	<th><%=Label.get("job.shift2")%></th>
	<td><%=nvl(oldjobdef.getShiftExp2())%></td>
	<td <%=printDiffColor(oldjobdef.getShiftExp2(),jobdefstg.getShiftExp2())%>><%=nvl(jobdefstg.getShiftExp2())%></td>
</tr>
<tr>
	<th><%=Label.get("job.fixed")%></th>
	<td><%=nvl(oldjobdef.getFixedDays())%></td>
	<td <%=printDiffColor(oldjobdef.getFixedDays(),jobdefstg.getFixedDays())%>><%=nvl(jobdefstg.getFixedDays())%></td>
</tr>
<tr>
    <th><%=Label.get("job.reverse")%></th>
    <td><%=nvl(oldjobdef.isReverse()?"Y":"N")%></td>
    <td <%=printDiffColor(oldjobdef.isReverse(),jobdefstg.isReverse())%>><%=nvl(jobdefstg.isReverse()?"Y":"N")%></td>
</tr>
<tr>
	<th><%=Label.get("job.basedate.calendar")%></th>
	<td><%=nvl(oldjobdef.getBaseDateCalId())%></td>
	<td <%=printDiffColor(oldjobdef.getBaseDateCalId(),jobdefstg.getBaseDateCalId())%>><%=nvl(jobdefstg.getBaseDateCalId())%></td>
</tr>
<tr>
	<th><%=Label.get("job.basedate.logic")%></th>
	<td><%=nvl(oldjobdef.getBaseDateLogic())%></td>
	<td <%=printDiffColor(oldjobdef.getBaseDateLogic(),jobdefstg.getBaseDateLogic())%>><%=nvl(jobdefstg.getBaseDateLogic())%></td>
</tr>
<tr>
	<th><%=Label.get("job.prejob")%></th>
	<td>
		<table class="Table njf-table__typea Width-100" id="prejob_table">
			<thead>
			<tr>
				<th><%=Label.get("job.prejob.id")%></th>
				<th><%=Label.get("job.prejob.okfail")%></th>
				<th><%=Label.get("job.prejob.andor")%></th>
			</tr>
			</thead>
			<tbody>
<%
	i=0;
	for (PreJobCondition preJob : oldjobdef.getPreJobConditions()) {
	i++;
%>
			<tr id="prejob_<%=i%>">
				<td><a href="javascript:openJobDefinitionWin('<%=preJob.getPreJobId()%>');"><%=preJob.getPreJobId()%></a></td>
				<td> <%=preJob.getOkFailText()%> </td>
				<td> <%=preJob.getAndOr()%> </td>
			</tr>
<%
	}
%>
			</tbody>
		</table>
	</td>
	<td <%=printDiffColor(oldjobdef.getPreJobConditions(),jobdefstg.getPreJobConditions())%>>
		<table class="Table njf-table__typea Width-100" id="prejob_table">
			<thead>
			<tr>
				<th><%=Label.get("job.prejob.id")%></th>
				<th><%=Label.get("job.prejob.okfail")%></th>
				<th><%=Label.get("job.prejob.andor")%></th>
			</tr>
			</thead>
			<tbody>
<%
	i=0;
	for (PreJobCondition preJob : jobdefstg.getPreJobConditions()) {
	i++;
%>
			<tr id="prejob_<%=i%>">
				<td><a href="javascript:openJobDefinitionWin('<%=preJob.getPreJobId()%>');"><%=preJob.getPreJobId()%></a></td>
				<td> <%=preJob.getOkFailText()%> </td>
				<td> <%=preJob.getAndOr()%> </td>
			</tr>
<%
	}
%>
			</tbody>
		</table>
	</td>
</tr>
<tr>
	<th><%=Label.get("job.param")%></th>
	<td>
		<table class="Table njf-table__typea Width-100" id="param_table">
			<thead>
			<tr>
				<th><%=Label.get("job.param.name")%></th>
				<th><%=Label.get("job.param.value")%></th>
			</tr>
			</thead>
			<tbody>
<%
	i=0;
	for (Map.Entry<String, String> param : oldjobdef.getInParameters().entrySet()) {
	i++;
%>
			<tr id="param_<%=i%>">
				<td><%=conv(param.getKey())%></td>
				<td><%=conv(param.getValue())%></td>
			</tr>
<%
	}
%>
			</tbody>
		</table>
	</td>
	<td <%=printDiffColor(oldjobdef.getInParameters().toString(), jobdefstg.getInParameters().toString())%>>
		<table class="Table njf-table__typea Width-100" id="param_table">
			<thead>
			<tr>
				<th><%=Label.get("job.param.name")%></th>
				<th><%=Label.get("job.param.value")%></th>
			</tr>
			</thead>
			<tbody>
<%
	i=0;
	for (Map.Entry<String, String> param : jobdefstg.getInParameters().entrySet()) {
	i++;
%>
			<tr id="param_<%=i%>">
				<td><%=conv(param.getKey())%></td>
				<td><%=conv(param.getValue())%></td>
			</tr>
<%
	}
%>
			</tbody>
		</table>
	</td>
</tr>

<tr>
	<th><%=Label.get("job.createtime")%></th>
	<td><%=toDatetimeString(oldjobdef.getCreateTime(), false)%></td>
	<td><%=toDatetimeString(jobdefstg.getCreateTime(), false)%></td>
</tr>
<tr>
	<th><%=Label.get("job.lastmodifytime")%></th>
	<td><%=toDatetimeString(DateUtil.getTimestamp(oldjobdef.getLastModifyTime()), false)%></td>
	<td><%=toDatetimeString(DateUtil.getTimestamp(jobdefstg.getLastModifyTime()), false)%></td>
</tr>
</tbody>
</table>


<table class="Table Width-100 Margin-bottom-10" id="tableList">
    <colgroup>
        <col width="22%">
        <col>
    </colgroup>
    <thead>
	<tr>
		<th class="Width-10"><%=Label.get("job.req.user")%>ID</th>
		<th class="Width-10"><%=Label.get("job.req.userip")%></th>
		<th class="Width-10"><%=Label.get("job.req.type")%></th>
		<th class="Width-10"><%=Label.get("job.req.no")%></th>
		<th><%=Label.get("job.req.comment")%></th>
	</tr>
	</thead>
	<tbody>
	<tr id="param_<%=i%>">
		<td> <%=conv(jobdefstg.getReqUserName())%></td>
		<td> <%=nvl(jobdefstg.getReqUserIp())%></td>
		<td> <%=nvl(jobdefstg.getReqType())%> </td>
		<td> <%=nvl(jobdefstg.getReqNo())%> </td>
		<td> <%=conv(jobdefstg.getReqComment())%></td>
	</tr>
	</tbody>
</table>

<%
	if (isOperator(request)) {
		String reqState = 
			nvl(jobdefstg.getReqState()).startsWith("A") ? "A":
			nvl(jobdefstg.getReqState()).startsWith("R") ? "R": "";
		boolean approveRejectDone = jobdefstg.getReqState()!=null && !"Q".equals(jobdefstg.getReqState());  // 이미 승인/반려 완료됐는지? 

%>

<form name="form1" method="post" action="action_jobdef.jsp" onsubmit="check_submit();">
	<input type="hidden" name="reqno"  value="<%=reqno%>">
	<input type="hidden" name="jobid"  value="<%=jobid%>">
	<input type="hidden" name="cmd"    value="admin_approve_reject">
	
	<table class="Table njf-table__typea Width-100 Margin-bottom-10" >
    <colgroup>
        <col width="22%">
        <col>
<%
        if (!approveRejectDone) {
%>
		<tr>
			<th class="Width-30"><b><%=Label.get("job.req.action.approve")%>/<%=Label.get("job.req.action.reject")%></b></th>
			<td>
				<select class="Select Width-30" name="reqState">
					<%=printSelectOption("",  "",     reqState)%>
					<%=printSelectOption("A", Label.get("job.req.action.approve"), reqState)%>
					<%=printSelectOption("R", Label.get("job.req.action.reject"),  reqState)%>
				</select>
				<input class="Textinput Width-65" type="text" name="reqARReason" value="<%=conv(jobdefstg.getReqARReason())%>" <%=approveRejectDone?"readonly":""%>>
			</td>
		</tr>
<%
        }else {
%>
		<thead>
		<tr>
			<th><%=Label.get("job.req.approve.yn")%></th>
			<th><%=Label.get("job.req.approve.time")%></th>
			<th><%=Label.get("job.req.approver")%></th>
			<th><%=Label.get("job.req.approver.id")%></th>
			<th><%=Label.get("job.req.approver.action.ip")%></th>
			<th class="Width-30"><%=Label.get("job.req.appr_rej.cause")%></th>
		</tr>
		</thead>
		<tbody>
		<tr>
			<td class="Text-center"><b><%="A".equals(reqState) ? Label.get("job.req.state.approved") : "R".equals(reqState) ? Label.get("job.req.state.rejected") : "N/A"%></b></td>
			<td class="Text-center"><%=toDatetimeString(jobdefstg.getReqState().substring(1), false)%></td>
			<td class="Text-center"><b><%=conv(jobdefstg.getReqOperatorName())%></b></td>
			<td class="Text-center"><%=nvl(jobdefstg.getReqOperatorId())%></td>
			<td class="Text-center"><%=nvl(jobdefstg.getReqOperatorIp())%></td>
			<td class="Text-center"><%=conv(jobdefstg.getReqARReason())%></td>
		</tr>
		</tbody>
<%
        }
%>
	</table>

<%
		if (!approveRejectDone) {
%>
<table class="Width-100">
	<tr>
		<td class="Text-center">
			<input class="Button" type="button" value="<%=Label.get("job.req.action.approve")%>" onclick="document.form1.reqState.value='A';document.form1.submit();" style="width:80px; height:35px">
			<input class="Button" type="button" value="<%=Label.get("job.req.action.reject") %>" onclick="document.form1.reqState.value='R';document.form1.submit();" style="width:80px; height:35px">
		</td>
	</tr>
</table>

<%
		}
%>
</form>
<%
	}
%>

</div>


</body>

</html>
