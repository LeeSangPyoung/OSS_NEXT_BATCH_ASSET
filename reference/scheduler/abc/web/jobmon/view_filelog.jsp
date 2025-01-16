<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<script src="./script/app/include-lib.js"></script> 
<title><%=Label.get("filelog")%></title>
<script>
	function openFileLogDownWin(jobinstanceid, filetype) {
	    location.href = "view_filelog_down.jsp?jobinstanceid="+jobinstanceid+"&filetype="+filetype;
	}
	function openFileLogWin(jobinstanceid, filetype, tail) {
	    location.href = "view_filelog.jsp?jobinstanceid="+jobinstanceid+"&filetype="+filetype+"&tail="+tail;
	}
</script>
</head>
<body>
<center>
<div class="popup-content-wrap">
<%
	ControllerAdminLocal admin = getControllerAdmin();

	String  jobinstanceid = request.getParameter("jobinstanceid");
	String  fileType      = nvl(request.getParameter("filetype"), "joblog"); /* [joblog, stdout, sublog] */
	boolean tail          = Util.toBoolean(request.getParameter("tail"), false);
	JobLogFileLocation fileloc = null;
	
	if ("joblog".equals(fileType)) {
		fileloc = admin.getJobLogFileLocation(jobinstanceid);
	}else if ("stdout".equals(fileType)) {
		fileloc = admin.getStdoutJobLogFileLocation(jobinstanceid);
	}else if ("sublog".equals(fileType)) {
		fileloc = admin.getSubJobLogFileLocation(jobinstanceid);
	}else {
		throw new IllegalArgumentException("filetype is "+fileType);
	}
	
	int offset = tail ? Math.max((int)(fileloc.getLength()- 8192), 0) : 0;

%>
<%-- <font size="5">
<%=Label.get("filelog")%><%=tail? " (tail)" : "" %> [<%=jobinstanceid %>] [Agent:<%=fileloc.getAgentId()%>] 
</font> --%>
    <div class="popup-content-title__wrap">
		<div class="content-title"><%=Label.get("filelog")%><%=tail? " (tail)" : "" %> [<%=jobinstanceid %>] [Agent:<%=fileloc.getAgentId()%>]</div>
	</div>
	<div class="content-info" style="font-size: 14px;">
		<%=toDatetimeString(new java.util.Date(), false) %>
	</div>
	
	<table class="Table njf-table__typea" >
		<tr align="center" bgcolor="#DDDDFF">
			<td align="left">
				<b><%=fileloc.getFilename()%> <%=tail ? "(offset:"+offset+")" : "" %></b>
				<input type="button" class="Button"  value="<%=Label.get("common.btn.download")%>" onclick="openFileLogDownWin('<%=jobinstanceid%>', '<%=fileType%>');">
		<%
			if (tail) {
		%>
				<input type="button" class="Button"  value="Full" onclick="openFileLogWin('<%=jobinstanceid%>', '<%=fileType%>', '0');">
		<%
			}else {
		%>
				<input type="button" class="Button"  value="Tail" onclick="openFileLogWin('<%=jobinstanceid%>', '<%=fileType%>', '1');">
		<%
			}
		%>
			</td>
		</tr>
		<tr>
			<td align="left">
				<pre style="white-space: pre-wrap; font-family: monospace;"><%
			ByteArray log = null;
		
			String agentJnuEncoding = null;
			if ("stdout".equals(fileType)) { /* stdout 로그는 시스템 charset 에 따라 ebcdic 으로 생성될 수 도 있다. */
				agentJnuEncoding = admin.getAgentSystemProperties( fileloc.getAgentId() ).getProperty("sun.jnu.encoding");
			}
			
			do { 
				log = admin.readLogFile(jobinstanceid, fileloc, offset, 4096);
				if (log != null) {
				    if (agentJnuEncoding != null) {
						printTextWithLTGT(log.getByteArray(), log.getOffset(), log.getLength(), agentJnuEncoding, out);
				    }else {
				        printTextWithLTGT(log.getByteArray(), log.getOffset(), log.getLength(), fileloc.getEncoding(), out);
				    }
					offset += log.getLength();
				}
				
				if (!tail && offset > 1024 * 1024) { /* 최대 1M 까지만 view 하고 그 이상은 다운로드 하도록 함.  */
				    throw new RuntimeException(Label.get("filelog.toobig.help"));
				}
			}while(log != null);
		%></pre>
			</td>
		</tr>
	</table>
<br><br>
<input type="button" name="" class="Button" value="<%=Label.get("common.btn.refresh")%>" onclick="location.reload();">
&nbsp;&nbsp;
<input type="button" name="" class="Button" value="<%=Label.get("common.btn.close")%>" onclick="window.close();">
<br><br>
</div>
</center>
</body>
</html>
