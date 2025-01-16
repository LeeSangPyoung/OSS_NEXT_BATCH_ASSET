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
    
    boolean closeThisWin = false; /* 현재 window close 할 것인지?. 변경폼은 close, 신규 폼은 close 안함 */
	String  returnUrl    = "view_setting.jsp?suburl="+suburl;  /* closeThisWin=false 일 경우는 sendRedirect 할 URL을 지정해주어야한다. */


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
		    returnUrl = "form_setting_notify.jsp?doreload=yes"; /* 신규인 경우는 form 창 유지하고 parent 만 reload 함 */
	    }else if ("add_notify_receiver".equals(cmd)) {
    		JobNotifyReceiver receiver = getJobNotifyReceiver(request);
		    admin.addJobNotifyReceiver(receiver, auth);
		    putMsg(session, Label.get("common.add.ok"));
		    returnUrl = "form_setting_notify_receiver.jsp?doreload=yes"; /* 신규인 경우는 form 창 유지하고 parent 만 reload 함 */
	    }else if ("modify_notify".equals(cmd)) {
    		JobNotify notify = getJobNotify(request);
    		String[] receiverIdList = request.getParameterValues("chkreceiverid");
		    Set receiverIdSet = receiverIdList == null ? new TreeSet() : new TreeSet(Arrays.asList(Util.stringArrayToIntArray(receiverIdList)));
		    notify.setReceivers(Util.toString(new ArrayList(receiverIdSet), ","));
		    admin.modifyJobNotify(notify, auth);
		    putMsg(session, Label.get("common.edit.ok"));
		    /* 변경은 form 창 close 하고 parent reload 함 */
		    closeThisWin = true;
	    }else if ("modify_notify_receiver".equals(cmd)) {
    		JobNotifyReceiver receiver = getJobNotifyReceiver(request);
		    admin.modifyJobNotifyReceiver(receiver, auth);
		    putMsg(session, Label.get("common.edit.ok"));
		    /* 변경은 form 창 close 하고 parent reload 함 */
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
        //putMsg(session, "▶▶ "+e.getMessage());
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
