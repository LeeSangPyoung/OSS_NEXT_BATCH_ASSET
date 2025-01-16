<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
	String jobexeid = request.getParameter("jobexecutionid");
%>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<script src="./script/app/include-lib.js"></script> 
<title>Job Execution (<%=jobexeid%>)</title>
<script>
    function openJobDefinitionWin(jobid) {
        window.open("view_jobdef_dtl.jsp?jobid="+jobid, 'jobdef_'+jobid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }

    function openJobInstanceWin(jobinsid) {
        window.open("view_jobins_dtl.jsp?jobinstanceid="+jobinsid, 'jobins_'+jobinsid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }
</script>
</head>
<body>
<center>
<%
	ControllerAdminLocal admin = getControllerAdmin();
	JobExecution jobexe = admin.getJobExecution(jobexeid);
%>

<br>
<div id="container2" class="popup-content-wrap">
	<table class="Table njf-table__typea" >
	    <colgroup>
	        <col width="22%">
	        <col>
	    </colgroup>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.jobid")%></th>
			<td>&nbsp;<a href="javascript:openJobDefinitionWin('<%=nvl(jobexe.getJobId())%>');"><b><%=nvl(jobexe.getJobId())%></b></a></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.jobinsid")%></th>
			<td>&nbsp;<a href="javascript:openJobInstanceWin('<%=nvl(jobexe.getJobInstanceId())%>');"><b><%=nvl(jobexe.getJobInstanceId())%></b></a></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.jobexeid")%></th>
			<td>&nbsp;<b><%=nvl(jobexe.getJobExecutionId())%></b></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.jobtype")%></th>
			<td>&nbsp;<b><%=nvl(jobexe.getJobType())%></b></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("jobexe.operator")%></th>
		    <td>&nbsp;<%=jobexe.getOperatorId()%>(<%=jobexe.getOperatorIp()%>)</td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("jobexe.runcount")%></th>
			<td>&nbsp;<%=nvl(jobexe.getRunCount())%></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.agent")%></th>
			<td>&nbsp;<%=nvl(jobexe.getAgentNode())%></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("jobexe.starttime")%></th>
		    <td>&nbsp;<%=toDatetimeString(jobexe.getStartTime(), false)%></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("jobexe.endtime")%></th>
		    <td>&nbsp;<%=toDatetimeString(jobexe.getEndTime(), false)%></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("jobexe.run.elaptime")%></th>
		    <td>&nbsp;<%=(double)(jobexe.getEndTime()==0?0:(jobexe.getEndTime()-jobexe.getStartTime()))/1000.0%> <%=Label.get("common.second")%></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("jobexe.progress.count")%></th>
		    <td>&nbsp;<%=jobexe.getProgressCurrent()+"/"+jobexe.getProgressTotal()%></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("jobexe.returncode")%></th>
		    <td>&nbsp;<%=jobexe.getReturnCode()%></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("jobexe.errmsg")%></th>
		    <td>&nbsp;<%=conv(jobexe.getErrorMsg())%></td>
		</tr>
		<tr>
		    <th><span class="ico_bull"></span><%=Label.get("job.param")%></th>
		    <td>
		        <table class="Table njf-table__typea Width-100">
					<thead>
					<tr>
						<th><%=Label.get("job.param.name")%></th>
						<th><%=Label.get("job.param.value")%></th>
					</tr>
					</thead>
					<tbody>					
		<%
			for (Map.Entry<String, String> param : jobexe.getInParameters().entrySet()) {
		%>
		            <tr>
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
		    <th><span class="ico_bull"></span><%=Label.get("jobexe.returnvalue")%><br>(<%=Label.get("jobexe.returnvalue.hint")%>)</th>
		    <td>
				<table class="Table njf-table__typea Width-100">
					<thead>
					<tr>
						<th><%=Label.get("job.rval.name")%></th>
						<th><%=Label.get("job.rval.value")%></th>
					</tr>
					</thead>
					<tbody>					
		<%
		    for (Map.Entry rval : jobexe.getReturnValues().entrySet()) {
		%>
					<tr>
						<td><%=conv((String)rval.getKey())%></td>
						<td><%=conv((String)rval.getValue())%></td>
					</tr>
		<% 
		    }
		%>
					</tbody>
				</table>
		    </td>
		</tr>
	</table>
<br>
<input type="button" class="Button" value="<%=Label.get("common.btn.close")%>" onclick="window.close();">
</div>
</center>
<br>
<br>
</body>
</html>
