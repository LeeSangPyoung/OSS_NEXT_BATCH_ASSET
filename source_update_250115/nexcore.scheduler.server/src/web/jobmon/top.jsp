<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@ include file= "common.jsp" %>
<%@ page session="true" %>
<%@ page import="nexcore.scheduler.core.VERSION"%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<!-- <link rel="stylesheet" href="common.css" type="text/css" /> --> 
<script src="./script/app/include-lib.js"></script>
	
<title>[<%=getServerName()%>]<%=Label.get("common.title")%> </title>
<script type="text/javascript">
	var tid;
	var _switch = true;
	
	$a.page(function() {
	    // 초기화 함수
	    this.init = function(id, param) {
	    	if($a.session('menu_open') == 'true') {
				setLeftOpenClick();
			} else {
				setLeftCloseClick();
			}
	    	
	    	// alopex ui 에서 fixed 를 기본 세팅을 하고 있어 이를 'auto' 로 변경함
	    	$(".Table").css({'table-layout':'auto'});
	    }
	});
	
	function licenseNotify(){
		var license = document.getElementById("licenseNotify");
		
		if(license){
			if(_switch) {
				license.style.color = "yellow";
				license.style.backgroundColor = "red";
			} else {
				license.style.color = "red";
				license.style.backgroundColor = "yellow";
			}
			_switch = !_switch;
			
			clearTimeout(tid);
			tid = setTimeout(licenseNotify, 1000);
		}
	}
	
	function goInitPage() {
		location.href = "view_jobins.jsp";
		
		//초기메뉴 선택
		$a.session('menu_no1', 1); //leftmenu open 
		$a.session('menu_no2', 7); //leftmenu close
	}

    function showhide(obj) {
        var elm = document.getElementById(obj);
        if ( elm.style.display != 'none' ) {
            elm.style.display = 'none';
        } else {
            elm.style.display = '';
        }
	}
	
	function displayMsg() {
<%
        List<String> mymsg = (List<String>)session.getAttribute("MY_MESSAGE");
        if (mymsg != null) {
            for (String msg : mymsg) {
                out.println("alert(\""+msg.replaceAll("\"", "\\\\\"").replaceAll("\n", "_")+"\");");
            }
            session.removeAttribute("MY_MESSAGE");
        }
%>
	}
	
	function logout() {
    	if (confirm('<%=Label.get("top.logout.confirm.msg")%>')) {
    		location.href = "login.jsp?cmd=logout&user_id=<%=getUserId(request)%>&login_time=<%=getLoginTime(request)%>";
    	}
	}

	function formEditPassword(userid) {
    	window.open("form_password.jsp?user_id="+userid, '', 'width=810,height=270,scrollbars=0').focus();
	}


</script> 
</head>
<body class="left-bg__full" onload="displayMsg(); tid = setTimeout(licenseNotify, 1000);">
<%
    User user = getUser(request);
	ControllerAdminLocal admin = getControllerAdmin();
	boolean checkValid = admin.checkLicenseAndCurrentInfo();
	Map<String, Integer> map = admin.getLicenseAndCurrentInfo();
	int maxAgentCount         = (Integer)map.get("maxAgentCount");
	int maxJobDefinitionCount = (Integer)map.get("maxJobDefinitionCount");
	int agentCount            = (Integer)map.get("agentCount");
	int jobDefinitionCount    = (Integer)map.get("jobDefinitionCount");
%>

	<div class="wrap">
	<% if(!checkValid) { %>
		<div id="licenseNotify" style="color: red; background-color: yellow;">
			<h1 style="text-align: center;"><%=Label.get("top.license.validate", agentCount, maxAgentCount==0?"Unlimited":maxAgentCount, jobDefinitionCount, maxJobDefinitionCount==0?"Unlimited":maxJobDefinitionCount) %></h1>
		</div>
	<% } %>
		<!-- top -->
		<div class="header-wrap">
			<div class="header">
<%-- 				<div class="header-logo">
					<h1><a href="view_jobins.jsp" class="logo"></a><span class="hidden">NEXCORE 배치스케줄러</span></h1>
					[Batch Admin (<%=getServerName()%>)] <font color=#ffffff><%= VERSION.toVersionString()%></font>
				</div>
				<!-- login info -->
				<div class="header-info">
					<ul class="header-topmenu"> 
						<li class="log_admin"><%=user.isAdmin()?"<font color='#ff7777'>["+Label.get("top.admin")+"]</font>":""%></li>
		                <li class="log_system"><%=user.isOperator()?"<font color='#7777ff'>["+Label.get("top.operator")+"]</font>":""%></li>
		                <li><%=user.getName()%>(<%=user.getId()%>)</li>
		                <li class="pl5"><span class="m-btn white small" onclick="javascript:formEditPassword('<%=user.getId()%>');"><%=Label.get("top.change.password")%></span></li>
		                <li><span class="m-btn white small" onclick="javascript:logout();"><%=Label.get("top.logout")%></span></li>
					</ul>
				</div> --%>
				
				<div class="header-logo">
					<a href="javascript:goInitPage()"><h1 class="logo"></h1></a>
					<h2 class="sublogo"><%=getServerName()%>(<%= getHostName()%>)</h2>
				</div>
				<!-- login info -->
				<div class="header-info">
					<ul class="header-topmenu">
						<li>
							<%=Label.get("user")%> : <%=user.getName()%> | 
							<%=Label.get("top.role")%> : <%= user.isAdmin() && user.isOperator() ? (Label.get("top.admin") + " & "+Label.get("top.operator")):  user.isAdmin() ? Label.get("top.admin") : user.isOperator() ? Label.get("top.operator") : Label.get("top.developer") %>
						</li>
						<li><a href="#0" class="btn-profile blind"><span class="Icon icon-profile"></span>profile menu</a></li>
					</ul>
				</div>			
			</div>
			<!-- //login info-->
		</div>
		<!-- //top -->
		<!-- profile wrap -->
 		<div class="profile-wrap" style="display:none">
			<dl class="user-info">
				<dt></dt>
				<dd></dd>
			</dl>
			<ul class="profile-menu">
<!-- 					<li><a href="#0">My Settings</a></li> -->
				<li><a href="view_setting.jsp?suburl=user"><%=Label.get("user")%></a></li>
				<li><a href="javascript:formEditPassword('<%=user.getId()%>');" style="cursor:hand"><%=Label.get("top.change.password")%></a></li>
				<li><a href="javascript:logout();"><%=Label.get("top.logout")%></a></li>
			</ul>
		</div>
		<!-- //profile wrap -->
		<!-- container -->
		<div class="container">
			<!-- left-wrap : menu open-->
			<div class="left-wrap">
				<div class="left-close"><a href="#0">close left menu</a></div>
				<!-- user info -->
				<div class="user-wrap">
<%--					<button class="Button Onlyicon btn-lng" onclick="javascript:logout();"><span class="Icon icon-lng"></span><%=Label.get("top.logout")%></button> --%>
				</div>
				<!-- //user info -->
				<!-- left menu -->
				<div class="leftmenu-wrap">
					<ul class="leftmenu">
						<li>
							<a href="view_jobdef.jsp"><span class="Icon left-icon1"></span><%=Label.get("top.menu.jobdef")%></a>
						</li>
						<li>
							<a href="view_jobins.jsp"><span class="Icon left-icon2"></span><%=Label.get("top.menu.jobins")%></a>
						</li>
						<li>
							<a href="view_jobgroupmon.jsp"><span class="Icon left-icon3"></span><%=Label.get("top.menu.jobgroupmon")%></a>
						</li>
						<li>
							<a href="view_jobdefstg.jsp"><span class="Icon left-icon4"></span><%=Label.get("top.menu.jobdefstg")%></a>
						</li>
						<li>
							<a href="view_server.jsp"><span class="Icon left-icon5"></span><%=Label.get("top.menu.server")%></a>
						</li>
						<li>
							<a href="#0"><span class="Icon left-icon6"></span><%=Label.get("top.menu.setting")%></a>
							<ul style="display : none;">
								<li><a href="view_setting.jsp?suburl=gparam"><%=Label.get("gparam")%></a></li>
								<li><a href="view_setting.jsp?suburl=pgroup"><%=Label.get("pgroup")%></a></li>
								<li><a href="view_setting.jsp?suburl=calendar">Calendar</a></li>
								<li><a href="view_setting.jsp?suburl=sysmon">System</a></li>
								<li><a href="view_setting.jsp?suburl=user"><%=Label.get("user")%></a></li>
								<li><a href="view_setting.jsp?suburl=notify"><%=Label.get("notify.title")%></a></li>
								<li><a href="view_setting.jsp?suburl=notifyreceiver"><%=Label.get("notify.receiver")%></a></li>
								<li><a href="view_setting.jsp?suburl=notifylist"><%=Label.get("notify.view.title")%></a></li>
								<li><a href="view_setting.jsp?suburl=jobgroupattrdef"><%=Label.get("jobgroup")%> <%=Label.get("common.attribute")%></a></li>
								<li><a href="view_setting.jsp?suburl=jobgroup"><%=Label.get("jobgroup")%></a></li>
							</ul>
						</li>
					</ul>
				</div>
				<!-- //left menu -->
			</div>
			<!-- //left-wrap-->
			<!-- left-wrap : menu close-->
			<div class="left-wrap__close">
				<div class="left-open"><a href="#0">open left menu</a></div>
				<!-- user info -->
				<div class="user-wrap">
<%--					<button class="Button Onlyicon btn-logout" onclick="javascript:logout();"><span class="Icon icon-logout" data-position="top"></span></button> --%>
				</div>
				<!-- //user info -->
				<!-- left menu -->
				<!-- left menu close 시 메인메뉴를 제외한 나머지 서브 메뉴는 display: none 처리 -->
				<div class="leftmenu-wrap">
					<ul class="leftmenu">
						<li>
							<a href="view_jobdef.jsp"><span class="Icon left-icon1"></span><%=Label.get("top.menu.jobdef")%></a>
						</li>
						<li>
							<a href="view_jobins.jsp"><span class="Icon left-icon2"></span><%=Label.get("top.menu.jobins")%></a>
						</li>
						<li>
							<a href="view_jobgroupmon.jsp"><span class="Icon left-icon3"></span><%=Label.get("top.menu.jobgroupmon")%></a>
						</li>
						<li>
							<a href="view_jobdefstg.jsp"><span class="Icon left-icon4"></span><%=Label.get("top.menu.jobdefstg")%></a>
						</li>
						<li>
							<a href="view_server.jsp"><span class="Icon left-icon5"></span><%=Label.get("top.menu.server")%></a>
						</li>
						<li>
							<a href="#0"><span class="Icon left-icon6"></span><%=Label.get("top.menu.setting")%></a>
							<ul style="display : none;">
								<li><a href="view_setting.jsp?suburl=gparam"><%=Label.get("gparam")%></a></li>
								<li><a href="view_setting.jsp?suburl=pgroup"><%=Label.get("pgroup")%></a></li>
								<li><a href="view_setting.jsp?suburl=calendar">Calendar</a></li>
								<li><a href="view_setting.jsp?suburl=sysmon">System</a></li>
								<li><a href="view_setting.jsp?suburl=user"><%=Label.get("user")%></a></li>
								<li><a href="view_setting.jsp?suburl=notify"><%=Label.get("notify.title")%></a></li>
								<li><a href="view_setting.jsp?suburl=notifyreceiver"><%=Label.get("notify.receiver")%></a></li>
								<li><a href="view_setting.jsp?suburl=notifylist"><%=Label.get("notify.view.title")%></a></li>
								<li><a href="view_setting.jsp?suburl=jobgroupattrdef"><%=Label.get("jobgroup")%> <%=Label.get("common.attribute")%></a></li>
								<li><a href="view_setting.jsp?suburl=jobgroup"><%=Label.get("jobgroup")%></a></li>
							</ul>
						</li>
					</ul>
				</div>
				<!-- //left menu -->
			</div>
			<!-- //left-wrap-->
<!-- 		</div>
	</div> -->

<%-- <table width="100%"  cellspacing="0" cellpadding="0" border="0">
	<tr height="75">
		<td valign="top">
			<!-- TOP 영역 시작 -->
			<table width="100%" border="0" height="70" cellpadding="0" cellspacing="0" style="font-size:12px;">
				<tr height="40">
					<td>
						<table width="100%" border="0" height="45" cellpadding="0" cellspacing="0" style="font-size:12px;">
							<tr height="40">
								<td align="center" valign="middle" width="200" ><img src='images/logo/NEXCORE.jpg' style="vertical-align:middle;"></td>
								<td align="left" valign="bottom" class="logo_title">[Batch Admin (<%=getServerName()%>)] <font color=#ffffff><%= VERSION.toVersionString()%></font> </td>
								<td>
									<table height="100%" width="100%">
										<tr>
											<td align="right" valign="bottom" class="logo_title_user">
												<%=user.isAdmin()?"<font color='#ff7777'>["+Label.get("top.admin")+"]</font>&nbsp;&nbsp;":""%><%=user.isOperator()?"<font color='#7777ff'>["+Label.get("top.operator")+"]</font>&nbsp;&nbsp; ":""%> <%=user.getName()%>(<%=user.getId()%>)
                                                &nbsp;&nbsp;<a href="javascript:formEditPassword('<%=user.getId()%>')">[<%=Label.get("top.change.password")%>]</a>
                                                &nbsp;&nbsp;<a href="javascript:logout()">[<%=Label.get("top.logout")%>]</a>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				
				<tr>
				

				
				</tr>
				
				
				<tr><td height="5" width="100%" style="none" nowrap></td></tr>
				<tr height="37" align="left">
					<td align="left">
						<table width="100%" height="37" border="0" cellpadding="0" cellspacing="0" style="font-size:12px;">
							<tr align="left">
								<td width="20"><img src="images/topmenu/top_menu_blue_left.gif" width="20" height="37"></td>
 <%
    Map<String, String> menus = new LinkedHashMap();
    menus.put("view_jobdef.jsp",      Label.get("top.menu.jobdef"));
    menus.put("view_jobins.jsp",      Label.get("top.menu.jobins"));
    menus.put("view_jobgroupmon.jsp", Label.get("top.menu.jobgroupmon"));
/*    menus.put("slm_cc_log.jsp",     Label.get("top.menu.cclog"));  */
    menus.put("view_jobdefstg.jsp",   Label.get("top.menu.jobdefstg"));
    
    if (isAdmin(request)) {
        menus.put("view_server.jsp",   Label.get("top.menu.server"));
        menus.put("view_setting.jsp",  Label.get("top.menu.setting"));
    }
	
    String pageURI = request.getRequestURI();
    for (Map.Entry<String, String> entry : menus.entrySet()) {
        if (pageURI.endsWith(entry.getKey())) { // 현재 메뉴
%> 
     <td width="150" background="images/topmenu/top_menu_blue_bg.gif">
        <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0" >
            <tr>
            <td background="images/topmenu/top_menu_blue_sel_left.gif" width="17"></td>
            <td background="images/topmenu/top_menu_blue_sel_bg.gif" align="center" class="top_menu_item_sel" onClick="document.location.href='<%=entry.getKey()%>';"><%=entry.getValue()%></td>
            <td background="images/topmenu/top_menu_blue_sel_right.gif" width="17"></td>
            </tr>
        </table>
    </td>
<%
        }else { // 현재 아닌 메뉴
%>
    <td width="150" background="images/topmenu/top_menu_blue_bg.gif" class="top_menu_item" align="center" onClick="document.location.href='<%=entry.getKey()%>';" nowrap>
    <%=entry.getValue()%>
    </td>
<%
        }
    }
%> 

								<td background="images/topmenu/top_menu_blue_bg.gif" >&nbsp;</td>
							</tr>								
						</table>
					</td>
				</tr>
				<tr><td height="2" width="100%" style="none" nowrap></td></tr>
			</table>
			<!-- TOP 영역 종료 -->
		</td>
	</tr>
</table> --%>
