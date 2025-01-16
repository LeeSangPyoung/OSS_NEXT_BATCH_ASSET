<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	User getUserFromHttp(javax.servlet.http.HttpServletRequest request) {
		User user = new User();
		BeanMaker.makeFromHttpParameter(request.getParameterMap(), user);

		/* 권한 정보 set        */
		/* Job Group 조회 권한  */
		List<String> jobGroupListForView = Util.toList(request.getParameter("jobGroupListForView"));
		List<UserAuth> authList = new ArrayList();
		for (String authTargetObject : jobGroupListForView) {
			UserAuth userAuth = new UserAuth();
			userAuth.setUserId(user.getId());
			userAuth.setAuthKind("VIEW_JOBGROUP");
			userAuth.setTargetObject(authTargetObject);
			authList.add(userAuth);
		}
		
		/* Job Group 운영 권한  */
		List<String> jobGroupListForOper = Util.toList(request.getParameter("jobGroupListForOper"));
		for (String authTargetObject : jobGroupListForOper) {
			UserAuth userAuth = new UserAuth();
			userAuth.setUserId(user.getId());
			userAuth.setAuthKind("OPER_JOBGROUP");
			userAuth.setTargetObject(authTargetObject);
			authList.add(userAuth);
		}
		
		user.setAuthList(authList);

		return user;
	}
%>
<%
	String  cmd          = nvl(request.getParameter("cmd"), "");
	boolean closeThisWin = false; /* 현재 window close 할 것인지?. 변경폼은 close, 신규 폼은 close 안함 */
	String  returnUrl    = null;  /* closeThisWin=false 일 경우는 sendRedirect 할 URL을 지정해주어야한다. */

    String errorMsg = null;
	try {
		ControllerAdminLocal admin = getControllerAdmin();
		AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));
		if ("add_user".equals(cmd)) {
		    User user = getUserFromHttp(request);
		    String password2 = request.getParameter("password2");
		    if (!Util.equalsIgnoreNull(user.getPassword(), password2) || Util.isBlank(user.getPassword())) {
		        putMsg(session, Label.get("action_user.password.error"));
		        return;
		    }else {
    		    user.setPassword(MessageDigestUtil.encode(user.getPassword()));
    		    admin.addUser(user, auth);
    		}
		    putMsg(session, Label.get("common.add.ok"));
		    closeThisWin = false;
		    returnUrl = "form_setting_user.jsp?doreload=yes";
		}else if ("modify_user".equals(cmd)) {
		    User user = getUserFromHttp(request);
            User beforeUser = admin.getUser(user.getId());
   		    String password2 = request.getParameter("password2");
            if (!Util.equalsIgnoreNull(user.getPassword(), password2)) {
                putMsg(session, Label.get("action_user.password.error"));
                return;
            }else {
                if (!Util.isBlank(user.getPassword())) {
                    user.setPassword(MessageDigestUtil.encode(user.getPassword()));
                }
                admin.modifyUser(user, auth);
            }
            putMsg(session, Label.get("common.edit.ok"));
		    closeThisWin = true; /* 변경은 폼 close */
		}else if ("delete_user".equals(cmd)) {
		    String[] useridList = request.getParameterValues("chkuserid");
		    for (String userid : useridList) {
		    	admin.removeUser(userid, auth);
		    }
		    putMsg(session, Label.get("common.delete.ok"));
		    closeThisWin = false;
		    returnUrl = "view_setting.jsp?suburl=user";
	    }else if ("modify_password".equals(cmd)) {
	        String oldPassword  = request.getParameter("old_password");
	        String newPassword  = request.getParameter("new_password");
	        String newPassword2 = request.getParameter("new_password2");
	        
	        if (!Util.equalsIgnoreNull(newPassword, newPassword2) || Util.isBlank(newPassword) || Util.isBlank(oldPassword)) {
	            putMsg(session, Label.get("action_user.password.error"));
	            return;
	        }else {
	            User user = admin.getUser(getUserId(request));
	            if (user==null) {
    	            putMsg(session, Label.get("action_user.password.change.error"));
    	        }else {
    				String oldEncrypted = null;
    				if (Util.nvl(user.getPassword()).length() > 30) {
    					// SHA256
    					oldEncrypted = MessageDigestUtil.encode(oldPassword);
    				}else {
    					// MD5
    					oldEncrypted = MessageDigestUtil.encodeMD5(oldPassword);
    				}
    				
    	            if (oldEncrypted.equals(user.getPassword())) {
    	                user.setPassword(MessageDigestUtil.encode(newPassword));
    	                admin.modifyUserPassword(user, auth);
    	                putMsg(session, Label.get("action_user.password.change.complete"));
    	            }else {
        	            putMsg(session, Label.get("action_user.password.no.match"));
    	            }
                }
            }
		    closeThisWin = true;
		    returnUrl = "view_setting.jsp?suburl=user";
	    }
	}catch(Exception e) {
		getMainLog().error("action_user.jsp", e);
	    //putMsg(session, "▶▶ "+e);
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
