<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@ include file= "common.jsp" %>
<%@page import="nexcore.scheduler.core.VERSION"%>
<%
    String cmd      = request.getParameter("cmd");
    String userid   = nvl(request.getParameter("user_id"));
    String password = request.getParameter("user_password");
    
    if ("login".equals(cmd)) {
        try {
            ControllerAdminLocal admin = getControllerAdmin();
            User user = admin.login(userid, password, getUserIp(request));
            if (user!=null) {
                session.setAttribute("user",      user);
                session.setAttribute("loginTime", new Long(System.currentTimeMillis()));
                response.sendRedirect("view_jobins.jsp");
            }else {
                putMsg(session, "Login Fail. ");
            }
        }catch(Exception e) {
            putMsg(session, e.getMessage());
        }
    }else if ("logout".equals(cmd)) {
        String ltime1 = request.getParameter("login_time");
        String ltime2 = String.valueOf(session.getAttribute("loginTime"));
        if (Util.equalsIgnoreNull(ltime1, ltime2)) { // 로그인한 그 사람이 맞는지 최소한의 검증.
            session.removeAttribute("user");
            session.removeAttribute("loginTime");
        }
        response.sendRedirect("view_jobins.jsp");
    }
%>

<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<link rel="SHORTCUT ICON" href="images/favicon.ico">
<link rel="stylesheet" href="common.css" type="text/css" />
<title>[<%=getServerName()%>]<%=Label.get("common.title")%> <%=Label.get("common.login")%></title>
<jsp:include page="display_msg.jsp" flush="true"/>
<script>
    function onload_function() {
        displayMsg();
        document.form1.user_id.focus();
    }
</script>
</head>

<body onload="onload_function();">
<form name="form1" action="login.jsp" method="post">
<input type="hidden" name="cmd" value="login">
<div id="contents" style="height:680px;">
    <div id="loginWrap">
    <div class="logbox">
        <ul class="logbox_in">
        	<li><span class="titbox"><%=Label.get("common.server")%></span><span class="txtbox"><%=getServerName()%></span></li>
            <li><span class="titbox"><%=Label.get("common.version")%></span><span class="txtbox"><%=VERSION.getImplementationVersion()%></span></li>
            <li><span class="titbox"><%=Label.get("user.id")%></span><input class="txt" value="" type="text" name="user_id" value="" ></li>
            <li><span class="titbox"><%=Label.get("user.password")%></span><input class="txt" type="password" name="user_password" ></li>
            <li><input type="submit" class="m-btn log login" value="<%=Label.get("common.login")%>"></li>
        </ul>
    </div>
    </div>
</div>
</form>







<%-- <form name="form1" action="login.jsp" method="post">
<input type="hidden" name="cmd" value="login">
<table width="100%" height="100%">
	<tr>
		<td valign="middle" align="center">
            <table width="500" border="0" cellpadding="0" cellspacing="0" align="center" class="login">
				<tr height="50">
					<td valign="middle">
						<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0" class="login-title" >
							<tr>
								<td width="200"><img src="images/logo/NEXCORE_signature_bg_blue.jpg" style="vertical-align:middle;"></td>
								<td valign="bottom"><%=Label.get("common.title")%></td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td width="1" style="background-color:gray;" nowrap></td>
				</tr>
				<tr>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td align="center">
						<table border="0">
							<tr>
								<th><%=Label.get("common.server")%> : </th>
								<td valign="middle"><b><%=getServerName()%></b></td>
							</tr>
							<tr>
                                <th><%=Label.get("common.version")%> : </th>
                                <td valign="middle"><b><%=VERSION.getImplementationVersion()%></b></td>
							</tr>
							<tr>
								<th><%=Label.get("user.id")%> : </th>
								<td valign="middle"><input class="txt" value="" type="text" name="user_id" value="" style="width:200; font-size:12px;"></td>
							</tr>
							<tr>
								<th><%=Label.get("user.password")%> : </th>
								<td valign="middle"><input class="txt" type="password" name="user_password" style="width:200; font-size:12px;"></td>
							</tr>
							<tr align="right">
								<td colspan="2" valign="middle"><input type="submit" class="button gray01 medium_02" value="<%=Label.get("common.login")%>"%></td>
								<td colspan="2" valign="middle"><input type="submit" class="m-btn white default" value="<%=Label.get("common.login")%>"%></td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td>&nbsp;</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</form> --%>
</body>
</html>
