<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    String agentId = request.getParameter("agentid");

    ControllerAdminLocal admin = getControllerAdmin();
    Map<String, StackTraceElement[]> threads = admin.getAgentAllThreadStackTrace(agentId);
%>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" /> -->
<title><%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.thread")%> (<%=threads.size()%>)</title>
<script src="./script/app/include-lib.js"></script>
</head>

<body>
<center>
<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.thread")%> (<%=threads.size()%>)
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<%-- 
<br>
<font size="3"><b><%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.thread")%> (<%=threads.size()%>)</b></font> --%>

<div class="popup-content-wrap">
<table class="Width-100">
<tr>
	<td colspan="100%" style="font-size:12px; text-align:right;"><b><%=toDatetimeString(new java.util.Date(), false) %></b></td>
</tr>
</table>

<table class="Table njf-table__typea Margin-bottom-10" >
<%
    int i=0;
    for (Map.Entry entry : threads.entrySet()) {
%>
    <tr>
        <td><%=(++i)+" : "+entry.getKey()%><pre><%
        for (StackTraceElement ste : (StackTraceElement[])entry.getValue()) {
            out.println("\t"+ste);
        }
%></pre>
        </td>
    </tr>
<%
    }
%>
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


