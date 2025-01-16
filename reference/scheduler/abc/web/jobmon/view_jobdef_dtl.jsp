<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	String getRealDateTimeFor25H(String time, String activationTimeHHMM) {
		if (Util.isBlank(time)) return "";
		
		if (time.compareTo(activationTimeHHMM) <= 0) {
			return activationTimeHHMM.substring(0,2)+":"+activationTimeHHMM.substring(2,4) + " (<a title='Activation Time'><b>AT</b></a>)";
		}
		
		int timeInt = Integer.parseInt(time);
		
		int    days   = timeInt / 2400;
		String hhmm   = String.format("%04d", timeInt - 2400 * days);
		
		return (days > 0 ? Label.get("common.daysafter", days)+" " : "") + 
			hhmm.substring(0,2) + ":" + hhmm.substring(2,4);
	}

%>
<%
	String jobid = request.getParameter("jobid");

	ControllerAdminLocal admin = getControllerAdmin();
	JobDefinition jobdef = admin.getJobDefinition(jobid);
    if (jobdef==null) {
        throw new SchedulerException("JobID ["+jobid+"] Not Found");
    }
    
	Map  calendarMap = admin.getCalendarList();

	String dailyActivationTime = admin.getSystemConfigValue("DAILY_ACTIVATION_TIME");
	
	AgentInfo agentInfo = null;
	AgentInfo agentInfo2 = null;
	
	if(!Util.isBlank(jobdef.getAgentNodeSlave())){
		
		agentInfo = admin.getAgentInfo(jobdef.getAgentNodeMaster());
		agentInfo2 = admin.getAgentInfo(jobdef.getAgentNodeSlave());	
		
	}else{
		agentInfo = admin.getAgentInfo(jobdef.getAgentNodeMaster());	
	}
	
    
    
%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<jsp:include page="display_msg.jsp" flush="true"/>
<script>
    function openJobDefinitionWin(jobid) {
        window.open("view_jobdef_dtl.jsp?jobid="+jobid, "jobdef_"+jobid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }

    function openJobGroupWin(jobgroupid) {
        window.open("view_jobgroup_dtl.jsp?jobgroupid="+jobgroupid, 'jobgroup_'+jobgroupid.replace(/-/g, ''), 'width=600,height=400,scrollbars=1').focus();
    }

    function openSchedulePlanWin(jobid) {
        window.open("view_schedule_plan.jsp?jobid="+jobid, '', 'width=700,height=400,scrollbars=1').focus();
    }

    function openJobDefinitionFormWin(jobid, mode) {
        window.open("form_jobdef.jsp?jobid="+jobid+"&mode="+mode, '', 'width=820,height=800,scrollbars=1').focus();
    }

    function doActivate() {
        var procDate = document.form1.procdate.value;
        if (confirm("<%=Label.get("jobdef.activate.confirm.msg")%>\n(PROC_DATE : "+procDate+")")) {
		    document.form1.cmd.value="activate";
            document.form1.submit();
        }
    }
    
    function doActivateAndRun() {
        var procDate = document.form1.procdate.value;
        if (confirm("<%=Label.get("jobdef.activaterun.confirm.msg")%>\n(PROC_DATE : "+procDate+")")) {
		    document.form1.cmd.value="activate_run";
            document.form1.submit();
        }
    }

	function doChangeLogLevel() {
		if (confirm("<%=Label.get("jobdef.change.loglevel.confirm.msg")%>")) {
		    document.form1.cmd.value="change_loglevel";
            document.form1.submit();
		}
	}

    function goInstanceSearch(jobid) {
        window.open("view_jobins.jsp?jobinstanceid="+jobid, 'jobins', 'width=1500,height=400,scrollbars=1').focus();
    }

	function openRepeatExactPlanWin() {
        window.open("view_repeat_exact_plan.jsp?mode=view&jobid=<%=jobid%>", "exactplanwin", 'width=1100,height=550,scrollbars=1').focus();
    }

</script>
<script src="./script/app/include-lib.js"></script>
<title>Job Definition (<%=jobid%>)</title>
</head>
<body onload="displayMsg();">
<center>
	<div class="header-wrap">
		<div class="header">
			<div class="header-title">
				<%=Label.get("jobdef")%> [<%=nvl(jobdef.getJobId())%>]
			</div>
			<div class="header-close-button">
		    	<span><a href="#" onclick="window.close();"><img alt="팝업창 닫기" src="images/pop_close.png"></a></span>
			</div>
		</div>
	</div>

	<div class="popup-content-wrap Margin-bottom-10">
		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Label.get("job.program.info")%></div>
		</div>
		
		<table class="Table njf-table__typea Width-100" >
	    <colgroup>
	        <col width="20%">
	        <col>
	        <col width="18%">
	        <col>
	    </colgroup>
	    <tbody>
			<tr>
				<th><%=Label.get("job.jobid")%></th>
				<td><b><%=nvl(jobdef.getJobId())%></b></td>
				<th><%=Label.get("job.jobgroup")%></th>
				<td><a href="javascript:openJobGroupWin('<%=jobdef.getJobGroupId()%>');"><%=nvl(jobdef.getJobGroupId())%></a></td>
			</tr>
			<tr>
				<th><%=Label.get("job.owner")%></th>
				<td><%=conv(jobdef.getOwner())%></td>
				<th><%=Label.get("job.desc")%></th>
				<td><font color="blue"><b>&nbsp;<%=getAppCode(jobdef.getJobId())%>&nbsp;&nbsp;<%=conv(jobdef.getDescription())%></b></font></td>
			</tr>
			<tr>
				<th><%=Label.get("job.jobtype")%></th>
				<td><%=getJobTypeText(jobdef.getJobType())+" ("+jobdef.getJobType()+")"%></td>
				<th><%=Label.get("job.component")%></th>
				<td><%=conv(jobdef.getComponentName())%></td>
			</tr>
			<tr>
				<th><%=Label.get("job.agent")%></th>
				<%if(!Util.isBlank(jobdef.getAgentNodeSlave())){ %>
				<td colspan="3"><%=agentInfo == null ? jobdef.getAgentNode()+" (N/A)" : jobdef.getAgentNode()+" ("+agentInfo.getName()+"/"+agentInfo2.getName()+")"%></td>
				<%}else{ %>
				<td colspan="3"><%=agentInfo == null ? jobdef.getAgentNode()+" (N/A)" : jobdef.getAgentNode()+" ("+agentInfo.getName()+")"%></td>
				<%} %>
			</tr>
		<%
			if (isOperator(request) || isAdmin(request)) {
		%>
			<tr>
			    <th><%=Label.get("notify.receiver")%></th>
			    <td colspan="3">
			    	<table class="Table njf-table__typea Width-100">
			    	<tbody><tr></tr>
				<%
	
					/* 통지 수신자 표시 */
				    Map<String, List<JobNotifyReceiver>> notifyReceiversMap = new LinkedHashMap();
				    notifyReceiversMap.put("EO", admin.getJobNotifyReceiverList(jobdef.getJobId(), "EO"));
				    notifyReceiversMap.put("EF", admin.getJobNotifyReceiverList(jobdef.getJobId(), "EF"));
				    notifyReceiversMap.put("LONGRUN", admin.getJobNotifyReceiverList(jobdef.getJobId(), "LONGRUN"));
					for (String eventName : notifyReceiversMap.keySet()) {
						int receiverIdx=0;
						List<JobNotifyReceiver> receiverList = notifyReceiversMap.get(eventName);
						Collections.sort(receiverList, getComparator("getName", true));
						for (JobNotifyReceiver receiver : receiverList) {
						receiverIdx++;
				%>
						<tr>
				<%
					if (receiverIdx==1) {
				%>
							<th rowspan="<%=receiverList.size()%>"><%="EO".equals(eventName) ? "End OK" : "EF".equals(eventName) ? "End Fail" : "LONGRUN".equals(eventName) ? "LONG RUN" : ""%></th>
				<%
					}
				%>
							<td><b><%=receiver.getName()%></b></td>
							<td><%=printJobNotifyReceiveInfo(receiver) %></td>
						</tr>
				<%
						}
					}
				%>
					</tbody>
					</table>
				</td>
			</tr>
		<%
			}
		%>
			<tr>
				<th><%=Label.get("job.param")%></th>
				<td colspan="3">
					<table class="Table njf-table__typea Width-100" id="param_table">
					<thead>
						<tr>
							<th><%=Label.get("job.param.name")%></th>
							<th><%=Label.get("job.param.value")%></th>
						</tr>
					</thead>
					<tbody>
				<%
					int i=0;
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
					</tbody>
					</table>
				</td>
			</tr>
		</tbody>
		</table>

		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Label.get("job.execution.condition")%></div>
		</div>
	
		<table class="Table njf-table__typea Width-100" >
	    <colgroup>
	        <col width="20%">
	        <col>
	    </colgroup>
	    <tbody>
			<tr>
				<th><%=Label.get("job.time")%></th>
				<td>[<%=nvl(jobdef.getTimeFrom())%>] ~ [<%=nvl(jobdef.getTimeUntil())%>]
				    (<%=getRealDateTimeFor25H(jobdef.getTimeFrom(), dailyActivationTime) %>) ~ (<%=getRealDateTimeFor25H(jobdef.getTimeUntil(), dailyActivationTime) %>)
				</td>
			</tr>
			<%
				boolean isExactRepeat    = "EXACT".equals(jobdef.getRepeatIntvalGb());
				boolean isStartEndRepeat = "START".equals(jobdef.getRepeatIntvalGb()) || "END".equals(jobdef.getRepeatIntvalGb());
			%>
			<tr>
				<th><%=Label.get("job.repeat")%></th>
				<td <%="Y".equals(jobdef.getRepeatYN()) ? "style=backgrouond:#ffcccc" : ""%>>
					<table class="Table njf-table__typea Width-100">
					<tbody>
					    <tr>
					        <th class="Width-15" rowspan="3"><b><%=jobdef.getRepeatYN()%></b></th>
				        	<th><b><%=printCancelLine("["+Label.get("job.repeat.intval")+"]", !isStartEndRepeat)%></b></th>
							<td><%=printCancelLine(jobdef.getRepeatIntval()+Label.get("common.second")+" ("+secondsToTime(jobdef.getRepeatIntval())+")", !isStartEndRepeat)%></td>
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
				        	<td colspan="3"><%=printCancelLine(nvl(jobdef.getRepeatExactExp()), !isExactRepeat)%>
							<% if (isExactRepeat) { %>
								<input type="button" class="Button" value="Plan" onclick="openRepeatExactPlanWin();">
							<% } %>
							</td>
        				</tr>
        			</tbody>
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
					i=0;
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
					for (PreJobCondition preJob : jobdef.getPreJobConditions()) {
					i++;
				%>
						<tr>
							<td><a href="javascript:openJobDefinitionWin('<%=nvl(preJob.getPreJobId())%>');"><b><%=nvl(preJob.getPreJobId())%></b></a></td>
							<td><%=nvl(preJob.getOkFailText())%></td>
							<td><%=nvl(preJob.getAndOr())%></td>
						</tr>
				<%
					}
				%>
					</tbody>
					</table>
				</td>
			</tr>
		</tbody>
		</table>
		
		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Label.get("job.day.schedule")%></div>
		</div>

		<table class="Table njf-table__typea Width-100" >
	    <colgroup>
	        <col width="20%">
	        <col>
	    </colgroup>
	    <tbody>
			<tr>
				<th><%=Label.get("job.schedule.type")%></th>
				<td><%=jobdef.getScheduleType()%></td>
			</tr>
			<tr>
				<th><%=Label.get("job.months")%></th>
				<td><%=nvl(jobdef.getMonths())%></td>
			</tr>
			<tr>
				<th><%=Label.get("job.days")%></th>
				<td><%=jobdef.getDayOfMonthScheduleType()%> : 
				<%if("NUMBER".equals(jobdef.getDayOfMonthScheduleType())) {%>
						<%=nvl(jobdef.getDaysInMonth())%>
				<%} else {%>
						[<%=nvl(jobdef.getCalendarId())%>]<%=nvl(calendarMap.get(nvl(jobdef.getCalendarId())))%> (<%=nvl(jobdef.getCalendarExps())%>)
				<%}%>
				</td>
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
			<tr>
				<th><%=Label.get("job.dayschedule.simulation")%></th>
				<td><input type="button" class="Button" value="<%=Label.get("job.dayschedule.simulation.view")%>" onclick="openSchedulePlanWin('<%=jobdef.getJobId()%>');"></td>
			</tr>
		</tbody>
		</table>

		<br>
		<br>
		
		<table class="Table njf-table__typea Width-100 Margin-bottom-5" >
	    <colgroup>
	        <col width="20%">
	        <col>
	    </colgroup>
	   	<tbody>
			<tr>
				<th><%=Label.get("job.createtime")%></th>
				<td><%=nvl(toDatetimeString(jobdef.getCreateTime(), false))%></td>
			</tr>
			<tr>
				<th><%=Label.get("job.lastmodifytime")%></th>
				<td><%=jobdef.getLastModifyTime()==null ? "" : toDatetimeString(DateUtil.getTimestamp(jobdef.getLastModifyTime()), false)%></td>
			</tr>
		</tbody>
		</table>		
		
		<table class="Width-100 Margin-bottom-5" >
		<tbody>
			<tr>
			    <td class="Text-center">
			        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.req.edit")%>" onclick="openJobDefinitionFormWin('<%=nvl(jobdef.getJobId())%>', 'edit');">
			        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.req.copy")%>" onclick="openJobDefinitionFormWin('<%=nvl(jobdef.getJobId())%>', 'copy');">
			        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.req.delete")%>" onclick="openJobDefinitionFormWin('<%=nvl(jobdef.getJobId())%>', 'delete');">
			        <input type="button" class="Button" value="<%=Label.get("jobins")%> <%=Label.get("common.btn.query")%>" onclick="goInstanceSearch('<%=nvl(jobdef.getJobId())%>');">
			    </td>
			</tr>
		</tbody>
		</table>
		
	<%
	
	    if (isOperator(request) && admin.isAllowedForOperation(jobdef.getJobGroupId(), jobdef.getJobId(), getUser(request))) {
	%>
		<form name="form1" action="action_jobdef.jsp" method="post">
		<input type="hidden" name="cmd" value="">
		<input type="hidden" name="jobid" value="<%=nvl(jobdef.getJobId())%>">
		<input type="hidden" name="returnurl" value="view_jobdef_dtl.jsp?jobid=<%=nvl(jobdef.getJobId())%>">
		<table class="Table njf-table__typea Width-100 Margin-bottom-10" >
		<tbody>
			<tr>
			    <td class="Width-30" style="text-align:center">
			        <select class="Select Width-60" name="newlevel">
			            <option value=""></option>
					<%
					    List<String> logLevelList = admin.getLogLevelUsingList();
					    for(String logLevel : logLevelList ) {
					        out.println(printSelectOption(logLevel, jobdef.getLogLevel()));
					    }
					%>
        			</select>
       				<input type="button" class="Button" value="<%=Label.get("common.btn.change.loglevel")%>" onclick="doChangeLogLevel();">
    			</td>
			    <td style="text-align:center">
			        <b>PROC_DATE : </b><input type="text" class="Textinput" name="procdate"  value="<%=Util.getCurrentYYYYMMDD()%>" maxlength="8">&nbsp;&nbsp;
			        <b>Lock : </b> <input type="checkbox" class="Checkbox" name="activate_lock_yn" value="1" checked>&nbsp;&nbsp;
			        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.activate")%>" onclick="doActivate();">
			        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.activaterun")%>" onclick="doActivateAndRun();">
			    </td>
			</tr>
		</tbody>
		</table>
		</form>
	<%
	    }
	%>
	
<table class="Width-100">
	<tr>
		<td class="Text-center">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()" style="width:80px; height:35px">
		</td>
	</tr>
</table>	

</div>
</center>
</body>
</html>
