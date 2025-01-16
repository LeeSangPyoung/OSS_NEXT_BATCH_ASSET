<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    ControllerAdminLocal admin = getControllerAdmin();
%>
<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("system")%></div>
</div>

<table class="Table njf-table__typea Width-100">
	<tr>
		<td class="Text-left">
			<b><%=toDatetimeString(System.currentTimeMillis(), false) %></b><br><br>
			<pre style="white-space: pre-wrap; font-family: monospace;"><%=admin.getSystemMonitorText()%></pre>
		</td>
	</tr>
</table>
