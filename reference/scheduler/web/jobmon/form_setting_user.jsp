<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth            auth  = new AdminAuth(getUserId(request), getUserIp(request));
    boolean isEditMode = !Util.isBlank(request.getParameter("userid"));
    User   user;
    if (isEditMode) {
        user      = admin.getUser(request.getParameter("userid"));
    }else {
        user      = new User();
    }
%>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<jsp:include page="display_msg.jsp" flush="true"/>
<title>User</title>
<script src="./script/app/include-lib.js"></script>
<script>
<% /* ================== User °ü·Ã ÇÔ¼ö ======================*/ %>
	function checkSubmit() {
		if (document.form1.id.value.trim() == '') {
			alert('<%=Label.get("common.required.field.missing", Label.get("user.id"))%>');
			return false;
		}
		else if(document.form1.cmd.value == 'add_user' && document.form1.password.value.trim() == '') {
			alert('<%=Label.get("common.required.field.missing", Label.get("user.password"))%>');
			return false;
		}
		else if(document.form1.cmd.value == 'add_user' && document.form1.password2.value.trim() == '') {
			alert('<%=Label.get("common.required.field.missing", Label.get("user.password"))%>');
			return false;
		}
		else if (document.form1.password.value != document.form1.password2.value) {
			alert("<%=Label.get("action_user.password.no.match")%>");
			return false;
		}
		else if(document.form1.name.value.trim() == '') {
			alert('<%=Label.get("common.required.field.missing", Label.get("user.name"))%>');
			return false;
		}
		
		if (document.form1.cmd.value == "modify_user") {
	        if (confirm("<%=Label.get("common.modify.confirm.msg")%>"))
	        	return true;
		}else if (document.form1.cmd.value == "add_user") {
			if (confirm("<%=Label.get("common.add.confirm.msg")%>"))
				return true;
	    }
		
		return false;
	}
    
    function openJobGroupSelectWin(targetElemName, before_value) {
        window.open("popup_jobgroup_multi.jsp?target_name="+targetElemName+"&before_value="+before_value, 'jobgroupselect', 'width=700,height=400,scrollbars=1').focus();
    }
</script>
</head>
<body onload="displayMsg();">

<%-- <font size="5">
<%=Label.get("user") %> <%=isEditMode ? Label.get("common.edit") : Label.get("common.new") %>
</font> --%>
<center>
<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=Label.get("user") %> <%=isEditMode ? Label.get("common.edit") : Label.get("common.new") %>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
<form name="form1" action="action_user.jsp" method="POST" onsubmit="return checkSubmit();">
<input type="hidden" name="suburl" value="user">
<input type="hidden" name="cmd" value="<%=isEditMode ? "modify" : "add"%>_user">
<!-- <table border="1" style = "border-collapse:collapse" bordercolor = "#a0a0a0" width="90%"> -->
<table class="Table njf-table__typea Margin-bottom-10" >
    <colgroup>
        <col width="22%">
        <col>
    </colgroup>
    <tr>
        <th class="required"><%=Label.get("user.id")%></th>
        <td><input class="Textinput Width-100" name="id" type="text" value="<%=nvl(user.getId())%>" <%=isEditMode ? "readonly" : "" %>></td>
    </tr>
    <tr>
        <th class="<%=isEditMode ? "" : "required"%>"><%=Label.get("user.password")%></th>
        <td><input class="Textinput Width-100" name="password" type="password" value=""></td>
    </tr>
    <tr>
        <th class="<%=isEditMode ? "" : "required"%>"><%=Label.get("user.password")%>(Confirm)</th>
        <td><input class="Textinput Width-100" name="password2" type="password" value=""></td>
    </tr>
    <tr>
        <th class="required"><%=Label.get("user.name")%></th>
        <td><input class="Textinput Width-100" name="name" type="text" value="<%=conv(user.getName())%>"></td>
    </tr>
    <tr>
        <th><%=Label.get("user.desc")%></th>
        <td><input class="Textinput Width-100" name="desc" type="text" value="<%=conv(user.getDesc())%>"></td>
    </tr>
    <tr>
        <th><%=Label.get("user.team1")%></th>
        <td><input class="Textinput Width-100" name="team1" type="text" value="<%=conv(user.getTeam1())%>"></td>
    </tr>
    <tr>
        <th><%=Label.get("user.team2")%></th>
        <td><input class="Textinput Width-100" name="team2" type="text" value="<%=conv(user.getTeam2())%>"></td>
    </tr>
    <tr>
        <th><%=Label.get("user.email")%></th>
        <td><input class="Textinput Width-100" name="email" type="text" value="<%=conv(user.getEmail())%>"></td>
    </tr>
    <tr>
        <th><%=Label.get("user.phone")%></th>
        <td><input class="Textinput Width-100" name="phone" type="text" value="<%=conv(user.getPhone())%>"></td>
    </tr>
    <tr>
        <th><%=Label.get("user.isactive")%></th>
        <td>
            <%=printCheckboxAlx("active", Label.get("user.active"), "true", String.valueOf(user.isActive())) %> 
        </td>
    </tr>
    <tr>
        <th><%=Label.get("user.admin.permission")%></th>
        <td>
            <%=printCheckboxAlx("admin", Label.get("user.grant"), "true", String.valueOf(user.isAdmin())) %> 
        </td>
    </tr>
    <tr>
        <th><%=Label.get("user.operator.permission")%></th>
        <td>
            <%=printCheckboxAlx("operator", Label.get("user.grant"), "true", String.valueOf(user.isOperator())) %> 
    </tr>
    <tr>
        <th><%=Label.get("user.jobgroup.foroper")%></th>
        <td>
            <input class="Textinput Margin-bottom-5" name="jobGroupListForOper" type="text" value="<%=nvl(Util.toString(user.getAuthList("OPER_JOBGROUP")))%>" style="width:80%;">
            <input class="Button Margin-bottom-5" type="button" value="<%=Label.get("common.select")%>" onclick="openJobGroupSelectWin('jobGroupListForOper', document.getElementsByName('jobGroupListForOper')[0].value);"><br>
            <%=Label.get("form_user.operjobgroup.help") %>
        </td>
    </tr>
    <tr>
        <th><%=Label.get("user.jobid.foroper.pattern")%></th>
        <td><input class="Textinput Width-100 Margin-bottom-5" name="operateJobIdExp" type="text" value="<%=nvl(user.getOperateJobIdExp())%>"><br>
        <%=Label.get("form_user.operjobgroup_jobid.help") %>
        </td>
    </tr>
    <tr>
        <th><%=Label.get("user.jobgroup.forview")%></th>
        <td>
            <input class="Textinput Margin-bottom-5" name="jobGroupListForView" type="text" value="<%=nvl(Util.toString(user.getAuthList("VIEW_JOBGROUP")))%>" style="width:80%;">
            <input class="Button Margin-bottom-5" type="button" value="<%=Label.get("common.select")%>" onclick="openJobGroupSelectWin('jobGroupListForView', document.getElementsByName('jobGroupListForView')[0].value);"><br>
            <%=Label.get("form_user.viewjobgroup.help") %>
        </td>
    </tr>
</table>

<table class="Width-100 Margin-bottom-10">
	<tr>
		<td class="Text-center">
			<input class="Button" type="submit" value="<%=isEditMode ? Label.get("common.btn.edit") : Label.get("common.btn.add")%>" style="width:80px; height:35px">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()" style="width:80px; height:35px">
		</td>
	</tr>
</table>

</form>
</div>
</center>
</body>
<%
    if (Util.toBoolean(request.getParameter("doreload"))) {
%>
<script>
    opener.location.reload();
</script>
<%
    }
%>
</html>
