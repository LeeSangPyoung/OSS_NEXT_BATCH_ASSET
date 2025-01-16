<%@page import="java.io.*"  
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
	
	String  jobinstanceid = request.getParameter("jobinstanceid");
	String  jobType       = request.getParameter("jobtype");
	String  fileType      = Util.nvl(request.getParameter("filetype"), "joblog"); /* [joblog, stdout, sublog] */
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
	
	String downFilename = null;
	if (fileloc.getFilename() != null) {
		int idx1 = Math.max(fileloc.getFilename().lastIndexOf("/"), fileloc.getFilename().lastIndexOf("\\"));
		if (idx1 == -1) {
			throw new IllegalArgumentException("Wrong filename. ["+fileloc.getFilename()+"]");
		}
		downFilename = fileloc.getFilename().substring(idx1+1);
	}

	response.setContentType("application/x-msdownload"); 
    response.setHeader("Content-Disposition", "attachment;filename=" + downFilename+";");
    response.setHeader("Content-Length", String.valueOf(fileloc.getLength()));
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