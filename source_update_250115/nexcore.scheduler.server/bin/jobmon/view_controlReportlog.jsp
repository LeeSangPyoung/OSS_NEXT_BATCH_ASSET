<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<html>
<head>
<link rel="stylesheet" href="common.css" type="text/css" /> 
<title><%=Label.get("ctrlrpt")%></title>
</head>
<body>
<center>
<div id="container2">
<div class="tbl_info">
    <span class="tit_Area"><%=Label.get("ctrlrpt")%></span>
</div>

<%-- <font size="5">
<%=Label.get("ctrlrpt")%>
</font> --%>
<%
    ControllerAdminLocal admin = getControllerAdmin();
    
    String jobinstanceid = request.getParameter("jobinstanceid");
    JobLogFileLocation fileloc = admin.getJobLogFileLocation(jobinstanceid);
    String reportFileName = fileloc.getFilename().replaceFirst(jobinstanceid + ".log", request.getParameter("className") + ".rst"); /* ��Ʈ�ѷ���Ʈ �α� ���� */
    fileloc.setFilename(reportFileName);
%>
<br>

<table border="0" style = "border-collapse:collapse" bordercolor = "#000000" width="100%">
<tr>
    <td colspan="100%" align="right"><b>&nbsp;&nbsp;<%=toDatetimeString(new java.util.Date(), false) %></b></td>
</tr>
</table>
<table border="1" style = "border-collapse:collapse" bordercolor = "#000000" cellpadding="2" width="100%">
<tr align="center" bgcolor="#DDDDFF">
    <td colspan="100%" align="left"><b><%=reportFileName%></b></td>
</tr>
<tr>
    <td align="left">
        <pre>
<%
    ByteArray log = null;
    int offset = 0;
    
    do { 
        log = admin.readLogFile(jobinstanceid, fileloc, offset, 1024);
        if (log != null) {
            printTextWithLTGT(log.getByteArray(), log.getOffset(), log.getLength(), out);
            offset += log.getLength(); 
        } 
        if (offset > 1024 * 1024) { /* �ִ� 1M ������ view �ϰ� �� �̻��� �ٿ�ε� �ϵ��� ��. */
            throw new RuntimeException(Label.get("filelog.toobig.help"));
        }        
    }while(log != null);
    
%></pre>
    </td>
</tr>
</table>
</div>
</center>
</body>
</html>
