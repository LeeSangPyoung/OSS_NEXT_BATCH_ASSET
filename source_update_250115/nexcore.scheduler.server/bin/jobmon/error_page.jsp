<%@page isErrorPage="true" contentType="text/html; charset=UTF-8"  pageEncoding="euc-kr"%>
<%@page import="nexcore.scheduler.util.*"%>
<%@page import="nexcore.scheduler.msg.*"%>
<html>
<!-- <link rel="stylesheet" href="common.css" type="text/css" /> -->
<jsp:include page="display_msg.jsp" flush="true"/>
	<body onload="displayMsg();">
	    <br>
	    
	    <div class="content-title__wrap">
			<div class="content-title"><%=Label.get("error_page.error.occured")%></div>
			<br>
		</div>

		<div class="Text-left"><%=exception.getMessage()%></div>
		<!--
		<%
		exception.printStackTrace(new java.io.PrintWriter(out));
		%>
		-->
	</body>
</html>
