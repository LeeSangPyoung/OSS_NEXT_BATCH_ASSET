<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	ViewFilter getViewFilter(javax.servlet.http.HttpServletRequest request) {
		// Http request parameter 로 부터 ViewFilter 객체 생성.
		ViewFilter vf = new ViewFilter();
		BeanMaker.makeFromHttpParameter(request.getParameterMap(), vf);
		return vf;
	}
%><%
	String cmd        = nvl(request.getParameter("cmd"), "");
	String returnUrl  = request.getParameter("returnurl");
    String vfid       = request.getParameter("id");

    String errorMsg = null;
	try {
		ControllerAdminLocal admin = getControllerAdmin();
		AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));
		if ("add".equals(cmd)) {
		    ViewFilter vf = getViewFilter(request);
		    boolean result = admin.addViewFilter(vf, auth);
		}else if ("modify_viewfilter".equals(cmd)) {
		    ViewFilter vf = getViewFilter(request);
		    boolean result = admin.modifyViewFilterNoJobList(vf, auth);
		}else if ("remove_jobid".equals(cmd)) { /* Job ID만 삭제 */
		    String[] jobidList = request.getParameterValues("chkjobid1");
		    if (jobidList != null) {
    		    boolean result = admin.modifyViewFilterDelJobList(toInt(vfid, -1), Arrays.asList(jobidList), auth);
    		}
		}else if ("add_jobid".equals(cmd)) { /* Job ID 만 추가 */
		    String[] jobidList = request.getParameterValues("chkjobid2");
		    if (jobidList != null) {
		        boolean result = admin.modifyViewFilterAddJobList(toInt(vfid, -1), Arrays.asList(jobidList), auth);
		    }
		}else if ("remove_viewfilter".equals(cmd)) {
		    boolean result = admin.removeViewFilter(toInt(vfid, -1), auth);
		}
	}catch(Exception e) {
		getMainLog().error("action_viewfilter.jsp", e);
        //putMsg(session, "▶▶ "+e.getMessage());
        throw new ServletException(e);
	}

    if (returnUrl != null) {
    	response.sendRedirect(returnUrl);
    }else {
    	response.sendRedirect("form_viewfilter.jsp?id="+vfid);
    }
%>

