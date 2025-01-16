<%@page language="java" contentType="text/html; charset=UTF-8"%>
<%@include file= "common.jsp" %>
<%
	String jobinstanceid = request.getParameter("jobinstanceid");

	ControllerAdminLocal admin = getControllerAdmin();
	JobInstance jobins = null;
	try {
		jobins = admin.getJobInstanceSimple(jobinstanceid);
	}catch(Exception e){
	}
    if (jobins==null) {
        out.println("BLANK");
		return;
    }
	
	long[] progress = admin.getJobProgress(jobins.getLastJobExeId());
    
	out.print("!");
	out.print("^");
	out.print(jobins.getJobInstanceId());
	out.print("^");
	out.print(jobins.getJobState());
    out.print("^");
	out.print(progress==null ? 0 : progress[0]);
	out.print("^");
	out.print(progress==null ? 0 : progress[1]);
	out.print("^");
	out.print(toDatetimeString(System.currentTimeMillis(), false));  /* current time  */
	out.print("^");
	out.print(toRunTimeString(jobins.getJobState(), jobins.getLastStartTime(), jobins.getLastEndTime(), false));  /* if running, elap time */
	out.print("^");
	
%>
