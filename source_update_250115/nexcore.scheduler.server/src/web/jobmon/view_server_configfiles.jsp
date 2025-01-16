<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    String agentId = request.getParameter("agentid");

    ControllerAdminLocal admin = getControllerAdmin();
    List<String> filenames = admin.getAgentConfigFiles(agentId);
%>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" /> -->
<title><%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.configfiles")%></title>
<script src="./script/app/include-lib.js"></script>
</head>
<body>
<center>

<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.configfiles")%>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<%-- <br>
<font size="3"><b><%=Label.get("job.agent")%> [<%=agentId%>] <%=Label.get("agent.configfiles")%></b></font> --%>

<div class="popup-content-wrap">
<table class="Width-100">
<tr>
	<td colspan="100%" style="font-size:12px; text-align:right"><b><%=toDatetimeString(new java.util.Date(), false) %></b></td>
</tr>
</table>

<%
	for (String filename : filenames) {
%>
<table class="Table njf-table__typea Width-100 Margin-bottom-10">
<thead>
<tr>
	<th class="Text-left"><%= filename %></th>
</tr>
</thead>
<tbody>
<tr>
	<td class="Text-left"><pre>
<%
		ByteArray content = null;
		int offset=0;
		
		do { 
			content = admin.readAgentFile(agentId, filename, offset, 4096);
			if (content != null) {
				printTextWithLTGT(content.getByteArray(), content.getOffset(), content.getLength(), "utf8", out);
				offset += content.getLength();
			}
		}while(content != null);
%></pre>
	</td>
</tr>
</tbody>
</table>

<%
	}
%>

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


