<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    long currentTime = System.currentTimeMillis();

    String agentId = request.getParameter("agentid");

    ControllerAdminLocal admin = getControllerAdmin();
    List<JobExecution> jobexeList = admin.getAgentRunningJobExecutions(agentId);
    
    Collections.sort(jobexeList, new Comparator() {
        public int compare(Object o1, Object o2) { 
            JobExecution je1 = (JobExecution)o1;
            JobExecution je2 = (JobExecution)o2;

            return (int)(je1.getStartTime() - je2.getStartTime());
        }
    });

%>
<html>
<head>
<script src="./script/app/include-lib.js"></script>
<script>
    function openJobDefinitionWin(jobid) {
        window.open("view_jobdef_dtl.jsp?jobid="+jobid, 'jobid_'+jobid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }

    function openJobInstanceWin(jobinstanceid) {
        window.open("view_jobins_dtl.jsp?jobinstanceid="+jobinstanceid, "jobins_"+jobinstanceid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }

</script>

<!-- <link rel="stylesheet" href="common.css" type="text/css" /> -->
<title><%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.running.job")%> (<%=jobexeList.size()%>)</title>
<meta http-equiv="refresh" content="5" />
</head>
<body>

<center>
<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.running.job")%> (<%=jobexeList.size()%>)
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<%-- <br>
<font size="3"><b><%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.running.job")%> (<%=jobexeList.size()%>)</b></font> --%>

<div class="popup-content-wrap">

<table class="Width-100">
<tr>
	<td colspan="100%" style="font-size:12px; text-align:right"><b><%=toDatetimeString(currentTime, false) %></b></td>
</tr>
</table>

<table class="Table njf-table__typea Width-100 Margin-bottom-10">
<thead>
<tr>
    <th>#</th>
    <th><%=Label.get("job.jobid")%></th>
    <th><%=Label.get("job.jobexeid")%></th>
    <th><%=Label.get("job.desc")%></th>
    <th><%=Label.get("job.jobtype")%></th>
    <th><%=Label.get("job.component")%></th>
    <th><%=Label.get("common.procdate")%></th>
    <th><%=Label.get("common.progress")%></th>
    <th>%</th>
    <th><%=Label.get("jobexe.operator")%></th>
    <th><%=Label.get("jobexe.starttime")%></th>
    <th><%=Label.get("jobexe.run.elaptime")%></th>
</tr>
</thead>
<tbody>
<tr>
<%
	int cnt = 0;
    for (JobExecution jobexe : jobexeList) {
        cnt++;
        long second = (currentTime - jobexe.getStartTime())/1000l;
%>
<tr>
    <td><%=cnt%></td>
    <td><a href="javascript:openJobDefinitionWin('<%=jobexe.getJobId()%>');"><%=jobexe.getJobId()%></a></td>
    <td><a href="javascript:openJobInstanceWin('<%=jobexe.getJobInstanceId()%>');"><%=jobexe.getJobExecutionId()%></a></td>
    <td class="Text-left"><%=getAppCode(jobexe.getJobId())%>&nbsp;<%=getShortDescription(jobexe.getDescription())%></td>
    <td><%=getJobTypeText(jobexe.getJobType())%></td>
    <td><%="CENTERCUT".equals(jobexe.getJobType()) ? "" : nvl(jobexe.getComponentName())%></td>
    <td><%=jobexe.getProcDate()%></td>
    <td><%=jobexe.getProgressCurrent()%>/<%=jobexe.getProgressTotal()%></td>
    <td class="Text-right"><%=toProgressPercentage(new long[]{jobexe.getProgressTotal(), jobexe.getProgressCurrent()})%></td>
    <td><%=jobexe.getOperatorId()%>/<%=jobexe.getOperatorIp()%></td>
    <td><%=toDatetimeString(jobexe.getStartTime(), true)%></td>
    <td nowrap><%= second <= 60 ? second+Label.get("common.second") : 
				   second+Label.get("common.second")+"<BR>("+(double)((second*10/60)/10.0)+Label.get("common.minute")+")"%></td>
</tr>
<%
    }
%>
</tbody>
</table>

<table class="Width-100 Margin-bottom-10">
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


