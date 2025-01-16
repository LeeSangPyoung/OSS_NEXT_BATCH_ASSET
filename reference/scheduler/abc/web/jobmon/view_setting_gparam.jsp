<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<script>
<% /* ============ 글로벌 파라미터 관련 함수 ===================*/ %>
    function viewEditFormGlobalParam(gparamName) {
        document.form1.action="view_setting.jsp";
        document.form1.cmd.value="form_modify_gparam";
        document.form1.param_name_for.value=gparamName;
        document.form1.submit();
    }

    function modifyGlobalParam(gparamName) {
        if (confirm("["+gparamName+"] <%=Label.get("common.modify.confirm.msg")%>")) {
            document.form1.action="action_setting.jsp";
            document.form1.cmd.value="modify_global_param";
            document.form1.submit();
        }
    }

/*     function formAddNewGlobalParam() {
        document.form1.action="view_setting.jsp";
        document.form1.cmd.value="form_add_gparam";
        document.form1.submit();
    } */

    function addGlobalParam(gparamName) {
    	var param_name = document.form2.param_name.value.trim();    	
    	if(param_name == null || param_name == "" || typeof param_name =="undefined") {
    		alert('<%=Label.get("common.required.field.missing", Label.get("gparam.name"))%>');
    		document.form2.param_name.focus();
    		return false;
    	}
    	
        document.form2.action="action_setting.jsp";
        document.form2.cmd.value="add_global_param";        
        document.form2.submit();
    }

    function removeGlobalParam(gparamName) {
        if (confirm("["+gparamName+"] <%=Label.get("common.remove.confirm.msg")%>")) {
            document.form1.action="action_setting.jsp";
            document.form1.cmd.value="delete_global_param";
            document.form1.param_name_for.value=gparamName;
            document.form1.submit();
        }
    }
    
	$(document).ready(function () {
		$('#dialogId').css('display','');
		layerPopOpen("addButton", "dialogId", "<%=Label.get("gparam.add.btn")%>", "700", "170", "50", "close", true, true, true, "fade", "200", false, false);
	});
</script>

<%
    ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth auth      = new AdminAuth(getUserId(request), getUserIp(request));
    String cmd          = request.getParameter("cmd");
    String paramNameFor = request.getParameter("param_name_for");

	admin.reloadGlobalParameters(auth); // DB -> memory refresh
    Map<String, String> globalParameters = admin.getGlobalParameters();
    
    // ####################### 전역 파라미터 부분 #########################################
%>
<%-- <br>
<table border="0" width="100%">
<tr>
    <td colspan="100%" align="left"><font size=3><b><img src="images/icon_setting.png"/> <%=Label.get("gparam")%></b></font></td>
</tr>
</table> --%>

<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("gparam")%></div>
</div>

<form name="form1" action="action_setting.jsp" method="POST">
<input type="hidden" name="suburl"         value="gparam">
<input type="hidden" name="cmd"            value="">
<input type="hidden" name="param_name_for" value="<%=paramNameFor%>">

<table class="Table Width-100 Margin-bottom-10">
<thead>
<tr>
    <th width="20%"><%=Label.get("gparam.name")%></th>
    <th width="50%"><%=Label.get("gparam.value")%></th>
    <th width="15%"><%=Label.get("common.edit")%></th>
    <th width="15%"><%=Label.get("common.delete")%></th>
</tr>
</thead>
<tbody>
<%
	for (Map.Entry<String, String> gparam : globalParameters.entrySet()) {
        if ("form_modify_gparam".equals(cmd) && gparam.getKey().equals(paramNameFor)) {
            /* 파라미터 변경을 위한 입력폼 만들기  */
%>
<tr>
    <td><%=conv(gparam.getKey())%><input type="hidden" name="param_name" value="<%=conv(gparam.getKey())%>"></td>
    <td><input class="Textinput Width-100" type="text" name="param_value" value="<%=conv(gparam.getValue())%>"></td>
    <td><input class="Button" type="button" value="<%=Label.get("common.btn.submit")%>" onclick="modifyGlobalParam('<%=paramNameFor%>');"></td>
    <td></td>
</tr>
<%
        }else {
            /* 그냥 보기용  */
%>
<tr>
    <td><%=conv(gparam.getKey())%></td>
    <td><%=conv(gparam.getValue())%></td>
    <td><input class="Button" type="button" value="<%=Label.get("common.btn.edit")  %>" onclick="viewEditFormGlobalParam('<%=gparam.getKey()%>');"></td>
    <td><input class="Button" type="button" value="<%=Label.get("common.btn.delete")%>" onclick="removeGlobalParam('<%=gparam.getKey()%>');"></td>
</tr>
<%
    	}
    }
%>
</tbody>
</table>
</form>
<form name="form2" action="action_setting.jsp" method="POST">
<input type="hidden" name="suburl"         value="gparam">
<input type="hidden" name="cmd"            value="">
<input type="hidden" name="param_name_for" value="<%=paramNameFor%>">
<div>
	<input id="addButton"  class="Button" type="button" value="<%=Label.get("gparam.add.btn")%>">
	<div id="dialogId" class="Dialog" style="display:none">
	    <div class="Dialog-contents">
			<table class="Table Width-100">
			<thead>
			<tr>
			    <th width="20%" class="required"><%=Label.get("gparam.name")%></th>
			    <th width="50%"><%=Label.get("gparam.value")%></th>
			    <th width="30%"></th>
			</tr>
			</thead>
			<tbody>
			<tr>
			    <td><input class="Textinput Width-100" type="text" name="param_name" value=""></td>
			    <td><input class="Textinput Width-100" type="text" name="param_value" value=""></td>
			    <td><input class="Button" type="button" value="<%=Label.get("common.btn.add")%>" onclick="addGlobalParam();"></td>
			</tr>
			</tbody>
			</table>
		</div>
	</div>
</div>
</form>
