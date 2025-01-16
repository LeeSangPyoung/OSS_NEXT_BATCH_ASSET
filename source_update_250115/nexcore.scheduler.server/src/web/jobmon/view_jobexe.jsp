<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<script src="./script/app/include-lib.js"></script>
<title><%=Label.get("jobexe")%></title>
<script type="text/javascript">
	$a.page(function() {
	    // ÃÊ±âÈ­ ÇÔ¼ö
	    this.init = function(id, param) {
	    	$("#tableList").css({'table-layout':'auto'});
	    	$(".Table th").css({'padding':'2px'});
	    	$(".Table td").css({'padding':'2px'});
	    }
	});

	function openJobExeDtlWin(jobexeid) {
	    window.open("view_jobexe_dtl.jsp?jobexecutionid="+jobexeid, '', 'width=650,height=700,scrollbars=1').focus();
	}
</script> 

</head>
<body>
<center>

	<div class="header-wrap Margin-bottom-5">
		<div class="header">
			<div class="header-title">
				<%=Label.get("jobexe")%>
			</div>
			<div class="header-close-button">
		    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
			</div>
		</div>
	</div>
	
<div class="popup-content-wrap">
<%-- <font size="5">
<%=Label.get("jobexe")%>
</font> --%>
<%
	String jobinstanceid = request.getParameter("jobinstanceid");

	ControllerAdminLocal admin = getControllerAdmin();
	List<JobExecution> jobexeList = admin.getJobExecutionListByJobInstanceId(jobinstanceid, false);
%>

	<table class="Width-100">
		<tr>
			<td colspan="100%" class="Text-right"><b><%=toDatetimeString(new java.util.Date(), false) %></b></td>
		</tr>
	</table>
	<table class="Table Width-100 Margin-bottom-10" id="tableList">
		<thead>
		<tr>
		<th nowrap><%=Label.get("job.jobexeid")%></th>
		<th nowrap><%=Label.get("jobexe.runcount")%></th>
		<th nowrap><%=Label.get("jobexe.state")%></th>
		<th nowrap><%=Label.get("job.agent")%></th>
		<th nowrap><%=Label.get("jobexe.starttime")%></th>
		<th nowrap><%=Label.get("jobexe.endtime")%></th>
		<th nowrap><%=Label.get("jobexe.run.elaptime")%>(<%=Label.get("common.second")%>)</th>
		<th nowrap><%=Label.get("common.progress")%></th>
		<th nowrap><%=Label.get("jobexe.returncode")%></th>
		<th nowrap><%=Label.get("jobexe.errmsg")%></th>
		<th nowrap><%=Label.get("jobexe.operator")%></th>
		<th nowrap><%=Label.get("jobexe.operator")%> IP</th>
		<th nowrap><%=Label.get("common.type")%></th>
		<th nowrap><%=Label.get("common.procdate")%></th>
		<th nowrap><%=Label.get("common.basedate")%></th>
		<th nowrap><%=Label.get("job.lastmodifytime")%></th>
		</tr>
		</thead>
		<tbody>
		<%
		    boolean colorFlip = true;
		    String  bgcolor   = null;
			for (JobExecution jobexe : jobexeList) {
			    colorFlip = !colorFlip;
			    bgcolor   = printTrFlip(colorFlip);
			    if (jobexe.getState() == JobExecution.STATE_RUNNING || jobexe.getState() == JobExecution.STATE_INIT ) {
			    	// running
			    	bgcolor = "bgcolor='#FFFF88'";
			    }else if (jobexe.getReturnCode() != 0) { /* if state is ended */
			    	// end fail
			    	bgcolor = "bgcolor='#FFBBBB'";
			    }
		%>
		<tr id="TR_<%=jobexe.getJobExecutionId()%>" <%=bgcolor%>>
		<td><b><a href="javascript:openJobExeDtlWin('<%=jobexe.getJobExecutionId()%>');"><%=nvl(jobexe.getJobExecutionId())%></a></b></td>
		<td><%=jobexe.getRunCount()%></td>
		<td><%=nvl(jobexe.getStateString())%></td>
		<td><%=nvl(jobexe.getAgentNode())%></td>
		<td><%=toDatetimeString(jobexe.getStartTime(), true)%></td>
		<td><%=toDatetimeString(jobexe.getEndTime(), true)%></td>
		<td><%=(jobexe.getEndTime()==0 || jobexe.getStartTime()==0) ? "" : ""+((double)(jobexe.getEndTime()-jobexe.getStartTime()))/1000.0%></td>
		<td><%=jobexe.getProgressCurrent()+"/"+jobexe.getProgressTotal()%></td>
		<td><%=jobexe.getReturnCode()%></td>
		<td><%=conv(jobexe.getErrorMsg())%></td>
		<td><%=conv(jobexe.getOperatorId())%></td>
		<td><%=conv(jobexe.getOperatorIp())%></td>
		<td><%=conv(jobexe.getOperatorType())%></td>
		<td><%=nvl(jobexe.getProcDate())%></td>
		<td><%=nvl(jobexe.getBaseDate())%></td>
		<td><%=toDatetimeString(DateUtil.getTimestamp(jobexe.getLastModifyTime()), true)%></td>
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
