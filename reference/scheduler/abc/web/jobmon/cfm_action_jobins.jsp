<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@ include file= "common.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
	String    cmd          = nvl(request.getParameter("cmd"), "");
	String[]  chkjobinsid  = request.getParameterValues("chkjobinsid");
	List      jobinsidList = chkjobinsid == null ? Collections.EMPTY_LIST : Arrays.asList(chkjobinsid);
%>
<html>
<head>
<script src="./script/app/include-lib.js"></script>
<title>Job Instance Action</title>
<jsp:include page="display_msg.jsp" flush="true"/>
<script>

	$a.page(function() {
	    // 초기화 함수
	    this.init = function(id, param) {
	    	$(".Table").css({'table-layout':'auto'});
	    }
	});

	function check_submit() {
<%	
	if ("stop".equals(cmd)) {
%>
		return confirm('<%=Label.get("view_jobins.stop.job.warning.alert")%>');
<%
	}
%>
	}
</script>
</head>
<body onload="displayMsg();">
<center>
<div style="width: 90%;">
<br><br>
<form name="form1" action="action_jobins.jsp" method="POST">
<input type="hidden" name="cmd" value="<%=cmd%>_multi">
<input type="hidden" name="jobinsid_list" value="<%=Util.toString(jobinsidList)%>">
<b><%=Label.get("jobctl.action.desc."+cmd, Label.get("common.jobcount", jobinsidList.size()))%></b>
<br><br><br>
<%
	if ("changeagent".equals(cmd)) {   /* 에이전트 변경시에는 에이전트 ID, 이름 목록이 나열됨. */
	    ControllerAdminLocal  admin = getControllerAdmin();
		List<AgentInfo> agentInfoList = admin.getAllAgentInfos();
		request.setAttribute("agentInfoList", agentInfoList);
%>
	<table class="Table njf-table__typec">
		<thead>
		<tr>
			<th>#</th>
			<th>Agent ID</th>
			<th>Name</th>
			<th>Desc</th>
			<th>IP:Port</th>
		</tr>
		</thead>
		<c:forEach var="agentInfo" items="${agentInfoList}">
		<tr align="center">
			<td><input type="radio" name="toagentid" value="${agentInfo.id }"></td>
			<td>${agentInfo.id }</td>
			<td>${agentInfo.name }</td>
			<td>${agentInfo.desc }</td>
			<td>${agentInfo.ip }:${agentInfo.port }</td>
		</tr>
		</c:forEach>
	</table>
<br><br><br>
<%
	}

	if (isRequirePasswordForJobAction(request)) {
%>
<b><%=Label.get("user.password")%></b>
<input type="password" name="password" sizw="13">
<br><br><br>
<%
	}
%>
<input type="submit" class="Button Large"  value="<%=Label.get("common.btn.submit")%>" onclick="return check_submit();">&nbsp;&nbsp;
<input type="button" class="Button Large"  value="<%=Label.get("common.btn.cancel")%>" onclick="window.close();">
</form>
</div>
</center>
</body>
</html>


