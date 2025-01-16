<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	AgentInfo getAgentInfo(javax.servlet.http.HttpServletRequest request) {
		// Http request parameter �� ���� AgentInfo ��ü ����.
		AgentInfo ai = new AgentInfo();
		BeanMaker.makeFromHttpParameter(request.getParameterMap(), ai);
		return ai;
	}
%><%
	String  cmd          = nvl(request.getParameter("cmd"), "");
    boolean closeThisWin = false; /* ���� window close �� ������?. �������� close, �ű� ���� close ���� */
	String  returnUrl    = "view_server.jsp";  /* closeThisWin=false �� ���� sendRedirect �� URL�� �������־���Ѵ�. */

    String errorMsg = null;
	try {
		ControllerAdminLocal admin = getControllerAdmin();
		AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));
		if ("add_agent".equals(cmd)) {
		    AgentInfo agentInfo = getAgentInfo(request);
		    boolean result = admin.addAgentInfo(agentInfo, auth);
		    putMsg(session, result ? Label.get("common.add.ok") : Label.get("common.add.fail"));
		    returnUrl = "form_server.jsp?doreload=yes"; /* �ű��� ���� form â �����ϰ� parent �� reload �� */
		}else if ("modify_agent".equals(cmd)) {
		    AgentInfo agentInfo = getAgentInfo(request);
		    boolean result = admin.modifyAgentInfo(agentInfo, auth);
		    putMsg(session, result ? Label.get("common.edit.ok") : Label.get("common.edit.fail"));
		    closeThisWin = true;
		}else if ("remove_agent".equals(cmd)) {
			String[] agentIdList = request.getParameterValues("chkagentid");
			int succ = 0, fail = 0;
		    for (String agentId : agentIdList==null ? new String[0] : agentIdList) {
	        	if (admin.removeAgentInfo(agentId, auth)) {
	        		succ ++;
	        	}else {
	        		fail ++;
	        	}
		    }
		    putMsg(session, Label.get("common.complete.success.fail", Label.get("common.btn.delete"), succ, fail));
		}else if ("close_or_open".equals(cmd)) {
		    String isClosed = request.getParameter("isclosed");
		    String agentId  = request.getParameter("id_for");
		    if ("true".equals(isClosed)) {
                admin.closeOrOpenAgent(agentId, true, auth);
		    }else if ("false".equals(isClosed)) {
                admin.closeOrOpenAgent(agentId, false, auth);
		    }
	    }
	}catch(Exception e) {
		getMainLog().error("action_notify.jsp", e);
        //putMsg(session, "���� "+e.getMessage());
        throw new ServletException(e);
	}
	
	if (closeThisWin) {
%>
<html>
<head>
<jsp:include page="display_msg.jsp" flush="true"/>
</head>
<body onload="displayMsg();opener.window.location.reload(true);window.close();">
</body>
</html>
<%
	}else {
    	response.sendRedirect(returnUrl);
	}
%>

