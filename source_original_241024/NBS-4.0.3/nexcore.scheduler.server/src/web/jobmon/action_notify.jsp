<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
    JobNotify getJobNotify(javax.servlet.http.HttpServletRequest request) {
		JobNotify notify = new JobNotify();
		BeanMaker.makeFromHttpParameter(request.getParameterMap(), notify);
		return notify;
	}

    JobNotifyReceiver getJobNotifyReceiver(javax.servlet.http.HttpServletRequest request) {
		JobNotifyReceiver receiver = new JobNotifyReceiver();
		BeanMaker.makeFromHttpParameter(request.getParameterMap(), receiver);
		return receiver;
	}
%>
<%

	String cmd           = nvl(request.getParameter("cmd"), "");
    String suburl        = nvl(request.getParameter("suburl"), "");
    
    boolean closeThisWin = false; /* ���� window close �� ������?. �������� close, �ű� ���� close ���� */
	String  returnUrl    = "view_setting.jsp?suburl="+suburl;  /* closeThisWin=false �� ���� sendRedirect �� URL�� �������־���Ѵ�. */


    String errorMsg = null;
	try {
		ControllerAdminLocal admin = getControllerAdmin();
		AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));
		if ("add_notify".equals(cmd)) {
    		JobNotify notify = getJobNotify(request);
    		String[] receiverIdList = request.getParameterValues("chkreceiverid");
		    Set receiverIdSet = receiverIdList == null ? new TreeSet() : new TreeSet(Arrays.asList(Util.stringArrayToIntArray(receiverIdList)));
		    notify.setReceivers(Util.toString(new ArrayList(receiverIdSet), ","));
		    admin.addJobNotify(notify, auth);
		    putMsg(session, Label.get("common.add.ok"));
		    returnUrl = "form_setting_notify.jsp?doreload=yes"; /* �ű��� ���� form â �����ϰ� parent �� reload �� */
	    }else if ("add_notify_receiver".equals(cmd)) {
    		JobNotifyReceiver receiver = getJobNotifyReceiver(request);
		    admin.addJobNotifyReceiver(receiver, auth);
		    putMsg(session, Label.get("common.add.ok"));
		    returnUrl = "form_setting_notify_receiver.jsp?doreload=yes"; /* �ű��� ���� form â �����ϰ� parent �� reload �� */
	    }else if ("modify_notify".equals(cmd)) {
    		JobNotify notify = getJobNotify(request);
    		String[] receiverIdList = request.getParameterValues("chkreceiverid");
		    Set receiverIdSet = receiverIdList == null ? new TreeSet() : new TreeSet(Arrays.asList(Util.stringArrayToIntArray(receiverIdList)));
		    notify.setReceivers(Util.toString(new ArrayList(receiverIdSet), ","));
		    admin.modifyJobNotify(notify, auth);
		    putMsg(session, Label.get("common.edit.ok"));
		    /* ������ form â close �ϰ� parent reload �� */
		    closeThisWin = true;
	    }else if ("modify_notify_receiver".equals(cmd)) {
    		JobNotifyReceiver receiver = getJobNotifyReceiver(request);
		    admin.modifyJobNotifyReceiver(receiver, auth);
		    putMsg(session, Label.get("common.edit.ok"));
		    /* ������ form â close �ϰ� parent reload �� */
		    closeThisWin = true;
	    }else if ("remove_notify".equals(cmd)) {
	        String[] notifyIdList = request.getParameterValues("chknotifyid");
		    for (String notifyId : notifyIdList==null ? new String[0] : notifyIdList) {
	        	admin.removeJobNotify(toInt(notifyId, -1), auth);
		    }
		    putMsg(session, Label.get("common.delete.ok"));
	    }else if ("remove_notify_receiver".equals(cmd)) {
        	String[] receiverIdList    = request.getParameterValues("chkreceiverid");
        	for (String receiverId : receiverIdList==null ? new String[0] : receiverIdList) {
      			admin.removeJobNotifyReceiver(toInt(receiverId, -1), auth); 
        	}
		    putMsg(session, Label.get("common.delete.ok"));
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
