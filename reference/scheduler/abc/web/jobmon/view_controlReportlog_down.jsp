<%@page import="java.io.OutputStream"%><%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"
%><%@page import="java.util.*"
%><%@page import="java.text.*"
%><%@page import="nexcore.scheduler.entity.*"
%><%@page import="nexcore.scheduler.controller.admin.*"
%><%@page import="nexcore.scheduler.util.*"
%><%
    if (session.getAttribute("user")==null) {
        response.sendRedirect("login.jsp");
        return;
    }

    ControllerAdminLocal admin = new ControllerAdminLocal();

    String jobinstanceid = request.getParameter("jobinstanceid");
    String className     = request.getParameter("className");
    
    JobLogFileLocation fileloc = admin.getJobLogFileLocation(jobinstanceid);
    String reportFileName = fileloc.getFilename().replaceFirst(jobinstanceid + ".log", request.getParameter("className") + ".rst"); /* 컨트롤레포트 로그 파일 */
    fileloc.setFilename(reportFileName);

    response.setContentType("application/x-msdownload"); 
    response.setHeader("Content-Disposition", "attachment;filename=" + className + ".log;");
    OutputStream output = response.getOutputStream();

    ByteArray log = null;
    int offset = 0;
    
    do { 
        log = admin.readLogFile(jobinstanceid, fileloc, offset, 4096);
        if (log != null) {
            output.write(log.getByteArray(), 0, log.getLength());
            offset += log.getLength();
        }
    }while(log != null);
    output.close();
%>