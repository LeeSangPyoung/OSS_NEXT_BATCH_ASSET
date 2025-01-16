<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<script src="./script/app/include-lib.js"></script>  
<title><%=Label.get("filelog.multi")%></title>
</head>
<body>
<center>
<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">

    <div class="popup-content-title__wrap">
		<div class="content-title"><%=Label.get("filelog.multi")%></div>
	</div>
	<div class="content-info">
		<b><%=toDatetimeString(new java.util.Date(), false) %></b>
	</div>

<%-- <font size="5">
<%=Label.get("filelog.multi")%>
</font> --%>
<%
	ControllerAdminLocal admin = getControllerAdmin();

    String[]     jobinsidList        = request.getParameterValues("chkjobinsid");
%>

	<table class="Table njf-table__typea Margin-bottom-10" >
		<%
		    for (int i=0; jobinsidList != null && i<jobinsidList.length; i++) {
		        String jobinsid = jobinsidList[i];
		
		        JobLogFileLocation fileloc = null;
		        try {
		        	fileloc = admin.getJobLogFileLocation(jobinsid);
		%>
		<tr align="center" bgcolor="#DDDDFF">
			<td colspan="100%" align="left"><b>[<%=i%>] [<%=jobinsid%>] <%=fileloc.getFilename()%></b></td>
		</tr>
		<%
		        }catch(Exception e) {
		%>
		<tr align="center" bgcolor="#DDDDFF">
			<td colspan="100%" align="left" bgcolor="#FFDDAA"><b>[<%=i%>] [<%=jobinsid%>] </b></td>
		</tr>
		<%
		            continue;
		        }
		%>
		<tr>
			<td align="left">
				<pre><%
				try {
		        	ByteArray log = null;
		        	int offset = 0;
		        	
		        	do { 
		        		log = admin.readLogFile(jobinsid, fileloc, offset, 4096);
		        		if (log != null) {
		        			printTextWithLTGT(log.getByteArray(), log.getOffset(), log.getLength(), out);
		        			offset += log.getLength();
		        		}
		        	}while(log != null);
		        }catch(Exception e) {
		%>
		    <font color="#FF1111"><%=e.toString()%></font>
		<%
		        }finally {
		%></pre>
			</td>
		</tr>
		<%
		        }
		    }
		%>
		<tr align="center" bgcolor="#DDDDFF">
			<td colspan="100%" align="left" bgcolor="#33FF22"><b>&nbsp;&nbsp;&nbsp;&nbsp;<%=Label.get("common.etc.end")%> (<%=jobinsidList==null ? 0 : jobinsidList.length%>)</B></td>
		</tr>
	</table>

		<table class="Width-100 Margin-bottom-10">
			<tr>
				<td class="Text-center">
					<input class="Button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()" style="width:80px; height:35px">
				</td>
			</tr>
		</table>

</div>
</center>
</body>
</html>
