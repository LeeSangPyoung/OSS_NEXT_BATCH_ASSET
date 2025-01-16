<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@page errorPage="error_page.jsp" contentType="text/html; charset=UTF-8" %>
<%@include file= "common_functions.jsp" %>
<%!
	boolean filterJobList(JobInstance jobins, String filterBy) {
		return filterJobList(jobins.getJobId(), filterBy);
	}

	boolean filterJobList(JobDefinition jobdef, String filterBy) {
		return filterJobList(jobdef.getJobId(), filterBy);
	}

%><% 
	if (!checkLogin(request, response)) return;
	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	response.setHeader("Pragma", "no-cache");
	response.setDateHeader("Expires", 0);
%>