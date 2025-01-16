<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    String agentId = request.getParameter("agentid");

    ControllerAdminLocal admin = getControllerAdmin();
    Properties prop = admin.getAgentSystemProperties(agentId);
    Map        env  = admin.getAgentSystemEnv(agentId);
    request.setAttribute("prop", new TreeMap(prop));
    request.setAttribute("env",  new TreeMap(env));
%>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" /> -->
<title><%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.system.properties")%></title>
<script src="./script/app/include-lib.js"></script>
</head>
<body>
<center>

<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.system.properties")%>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<%-- 
<br>
<font size="3"><b><%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.system.properties")%></b></font> --%>

<div class="popup-content-wrap">

<table class="Width-100">
<tr>
	<td colspan="100%" style="font-size:12px; text-align:right"><b><%=toDatetimeString(new java.util.Date(), false) %></b></td>
</tr>
</table>

<table class="Table njf-table__typea Width-100 Margin-bottom-10">
<thead>
<tr>
	<th class="Width-30">Property Key</th>
	<th>Property Value</th>
</tr>
</thead>
<tbody>
<c:forEach var="entry" items="${prop}">
<tr>
	<th style="text-align:left; padding:2px; word-wrap:break-word;">${entry.key}</th>
	<td class="Text-left" style="padding:2px; word-wrap:break-word;">${entry.value}</td>
</tr>
</c:forEach>
</tbody>
</table>

<table class="Table njf-table__typea Width-100 Margin-bottom-10">
<thead>
<tr>
	<th class="Width-30">Env Key</th>
	<th>Env Value</th>
</tr>
</thead>
<tbody>
<c:forEach var="entry" items="${env}">
<tr>
	<th style="text-align:left; padding:2px; word-wrap:break-word;">${entry.key}</th>
	<td class="Text-left" style="padding:2px; word-wrap:break-word;">${entry.value}</td>
</tr>
</c:forEach>
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


