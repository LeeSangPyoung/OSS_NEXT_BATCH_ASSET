<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<html>
<head>
<script src="./script/app/include-lib.js"></script> 
<title><%=Label.get("thead.view")%></title>
</head>
<body>
<center>
<div class="header-wrap">
	<div class="header">
		<div class="header-title">
			<%=Label.get("thead.view")%>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>
<br>
<div class="popup-content-wrap">
	<div class="content-info" style="font-size: 14px;">
		<%=toDatetimeString(new java.util.Date(), false) %>
	</div>
	<table class="Table njf-table__typea">
		<tr>
			<td align="left">
				<pre><%
					ControllerAdminLocal admin = getControllerAdmin();
				
					String jobinstanceid = request.getParameter("jobinstanceid");
					Map map = admin.getJobExecutionThreadStackTrace(jobinstanceid);
				
				    if (map==null) {
				        throw new RuntimeException(Label.get("thead.running.job.only.msg"));
				    }
				
				    out.println("[THREAD] "+map.get("THREAD"));
				    out.println("");
				
				    StackTraceElement[] steArray = (StackTraceElement[])map.get("STACKTRACE");
				
				    for (StackTraceElement ste : steArray) {
				        out.println("\t"+ste);
				    }
				%></pre>
			</td>
		</tr>
	</table>
	<br><br>
	<input type="button" name="" class="Button" value="<%=Label.get("common.btn.close")%>" onclick="window.close();">
	<br><br>
</div>
</center>
</body>
</html>
