<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<html>
<head>
	<script src="./script/app/include-lib.js"></script> 
	<title>Job End Fail Alert</title>
</head>
<%
	String jobinsidList = request.getParameter("jobinsidlist");

	ControllerAdminLocal admin = getControllerAdmin();
%>
<script>
	$a.page(function() {
	    // 초기화 함수
	    this.init = function(id, param) {
	    	$(".Table").css({'table-layout':'auto'});
	    }
	});
	
    function openJobInstanceWin(jobinsid) {
        window.open("view_jobins_dtl.jsp?jobinstanceid="+jobinsid, 'jobins_'+jobinsid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }
</script>
<body>
<embed src="./sounds/endfail_alert.mp3" width="0" height="0"/>
<center>
<div>
	<table width="100%" height="100%" border="0">
		<tr height="20%">
			<td align="center" id="title_bg"><font color="red" size="5" id="title_text"><%=Label.get("job.endfail.alert") + " ("+toDatetimeString(System.currentTimeMillis(), false)+")"%></font></td>
		</tr>
		
		<tr height="60%">
		<td align="center">
			<table class="Table njf-table__typec">
				<thead>
				<tr>
					<th>#</th>
					<th><%=Label.get("job.jobinsid")%></th>
					<th><%=Label.get("job.owner")%></th>
					<th><%=Label.get("job.desc")%></th>
					<th><%=Label.get("jobins.last.starttime")%></th>
					<th><%=Label.get("jobins.last.endtime")%></th>
				</tr>
				</thead>
				<%
					String[] jobinsidArray = jobinsidList.split(",");
					int i=0;
					boolean flip = true;
					for (String jobinsid : jobinsidArray) {
						i++;
						flip = !flip;
						JobInstance   jobins = admin.getJobInstance(jobinsid);
					    JobDefinition jobdef = admin.getJobDefinition(jobins.getJobId());
					    List<JobNotifyReceiver> notifyReceivers = admin.getJobNotifyReceiverList(jobins.getJobId(), "EF");
				%>
				<tr align="center" <%=printTrFlip(flip) %>>
					<td rowspan="<%=notifyReceivers.size()+1%>"><%=i%></td>
					<td><a href="javascript:openJobInstanceWin('<%=nvl(jobins.getJobInstanceId())%>');"><b><%=splitJobInstanceId(nvl(jobins.getJobInstanceId()))%></b></a></td>
					<td><b><%=jobdef.getOwner()%></b></td>
					<td><b><font color="#0000FF"><%=jobins.getDescription()%></font></b></td>
					<td><%=toDatetimeString(jobins.getLastStartTime())%></td>
					<td><%=toDatetimeString(jobins.getLastEndTime())%></td>
				</tr>
				<%
						int receiverIdx=0;
						for (JobNotifyReceiver receiver : notifyReceivers) {
							receiverIdx++;
				%>
				<tr align="center" <%=printTrFlip(flip) %>>
				<%
							if (receiverIdx==1) {
				%>
					<td rowspan="<%=notifyReceivers.size()%>"><%=Label.get("notify.receiver") %></td>
				<%
							}
				%>
					<td><b><%=receiver.getName()%></b></td>
					<td colspan=3 align=left>
				        <%=receiver.isRecvByEmail()     ? "[Email : <b>"     + conv(receiver.getEmailAddr())   + "</b>]" : ""%>
				        <%=receiver.isRecvBySms()       ? "[SMS : <b>"       + conv(receiver.getSmsNum())      + "</b>]" : ""%>
				        <%=receiver.isRecvByTerminal()  ? "[Terminal : <b>"  + conv(receiver.getTerminalId())  + "</b>]" : ""%>
				        <%=receiver.isRecvByMessenger() ? "[Messenger : <b>" + conv(receiver.getMessengerId()) + "</b>]" : ""%>
				        <%=receiver.isRecvByDev1()      ? "[Dev1 : <b>"      + conv(receiver.getDev1Point())   + "</b>]" : ""%>
				        <%=receiver.isRecvByDev2()      ? "[Dev2 : <b>"      + conv(receiver.getDev2Point())   + "</b>]" : ""%>
				        <%=receiver.isRecvByDev3()      ? "[Dev3 : <b>"      + conv(receiver.getDev3Point())   + "</b>]" : ""%>
					</td>
				</tr>
				<%
						}
					}
				%>
			</table>
		</td>
		</tr>
		
		<tr height="20%" >
			<td align="center"><input type="button" class="Button" style="width: 80px; height: 35px" value="<%=Label.get("common.btn.close")%>" onclick="window.close();"></td>
		</tr>
	
	</table>
</div>
</center>

</body>

<script>
	window.onload = function ()
	{
		setInterval("setBgColor()",1000);
	}
	
	var currColorIdx = 0;
	
	function setBgColor()
	{
		var colors = new Array("red","white");
	
		document.getElementById("title_bg").bgColor = colors[currColorIdx];
		
		currColorIdx++;
		if (currColorIdx > colors.length -1) {
			currColorIdx = 0;
		}
		
		document.getElementById("title_text").color = colors[currColorIdx];
	
	}
</script>
</html>
