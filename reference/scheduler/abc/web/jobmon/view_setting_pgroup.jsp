<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>

<script>
<% /* ============ 병렬 그룹 설정 관련 함수 ===================*/ %>
    function viewEditFormParallelGroup(pgName) {
        document.form2.action="view_setting.jsp";
        document.form2.cmd.value="form_modify_parallel_group";
        document.form2.parallel_group_for.value=pgName;
        document.form2.submit();
    }

    function modifyParallelGroup(pgName) {
    	var groupDesc = document.form2.groupDesc.value.trim(); 
    	if(groupDesc == null || groupDesc == "" || typeof groupDesc =="undefined") {
    		alert('<%=Label.get("common.required.field.missing", Label.get("pgroup.desc"))%>');
    		document.form2.groupDesc.focus();
    		return false;
    	}      	
    	
        if (confirm("["+pgName+"] <%=Label.get("common.modify.confirm.msg")%>")) {
            document.form2.action="action_setting.jsp";
            document.form2.cmd.value="modify_parallel_group";
            document.form2.submit();
        }
    }

/*     function formAddNewParallelGroup() {
        document.form2.action="view_setting.jsp";
        document.form2.cmd.value="form_add_parallel_group";
        document.form2.submit();
    } */

    function addParallelGroup(pgName) {
    	var groupName = document.form3.groupName.value.trim(); 
    	var groupDesc = document.form3.groupDesc.value.trim(); 
    	if(groupName == null || groupName == "" || typeof groupName =="undefined") {
    		alert('<%=Label.get("common.required.field.missing", Label.get("pgroup.id"))%>');
    		document.form3.groupName.focus();
    		return false;
    	}else if(groupDesc == null || groupDesc == "" || typeof groupDesc =="undefined") {
    		alert('<%=Label.get("common.required.field.missing", Label.get("pgroup.desc"))%>');
    		document.form3.groupDesc.focus();
    		return false;
    	}   
    	
        document.form3.action="action_setting.jsp";
        document.form3.cmd.value="add_parallel_group";
        document.form3.submit();
    }

    function removeParallelGroup(pgName) {
        if (confirm("["+pgName+"] <%=Label.get("common.remove.confirm.msg")%>")) {
            document.form2.action="action_setting.jsp";
            document.form2.cmd.value="delete_parallel_group";
            document.form2.parallel_group_for.value=pgName;
            document.form2.submit();
        }
    }
    
	$(document).ready(function () {
		$('#dialogId').css('display','');
		layerPopOpen("addButton", "dialogId", "<%=Label.get("pgroup.add.btn")%>", "700", "170", "50", "close", true, true, true, "fade", "200", false, false);
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

<%
    String pgName = request.getParameter("parallel_group_for");

    List<ParallelGroup> parallelGroupList = admin.getAllParallelGroups();
    
    // ####################### 병렬 그룹 부분 #########################################
%>
<%-- <br>
<table border="0" width="100%">
<tr>
    <td colspan="100%" align="left"><font size=3><b><img src="images/icon_setting.png"/> <%=Label.get("pgroup")%></b></font></td>
</tr>
</table> --%>

<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("pgroup")%></div>
</div>

<form name="form2" action="action_setting.jsp" method="POST">
<input type="hidden" name="suburl"             value="pgroup">
<input type="hidden" name="cmd"                value="">
<input type="hidden" name="parallel_group_for" value="<%=pgName%>">

<table class="Table Width-100 Margin-bottom-10">
<thead>
<tr>
    <th width="20%"><%=Label.get("pgroup.id")%></th>
    <th width="50%"><%=Label.get("pgroup.desc")%></th>
    <th width="10%"><%=Label.get("pgroup.max")%></th>
    <th width="10%"><%=Label.get("pgroup.current")%></th>
    <th width="5%"><%=Label.get("common.btn.edit")%></th>
    <th width="5%"><%=Label.get("common.btn.delete")%></th>
</tr>
</thead>
<tbody>
<%
	for (ParallelGroup pg : parallelGroupList) {
        if ("form_modify_parallel_group".equals(cmd) && pg.getGroupName().equals(pgName)) {
            // 병렬 그룹 변경을 위한 입력폼 만들기
%>
<tr>
    <td><%=nvl(pg.getGroupName())%><input type="hidden" name="groupName" value="<%=nvl(pg.getGroupName())%>"></td>
    <td><input class="Textinput Width-100" type="text" name="groupDesc" value="<%=conv(pg.getGroupDesc())%>"></td>
    <td><input class="Textinput Width-100" data-keyfilter-rule="digits" type="text" name="maxLimit"  value="<%=nvl(pg.getMaxLimit())%>"></td>
    <td><%=nvl(pg.getCurrentRunning())%></td>
    <td><input class="Button" type="button" value="<%=Label.get("common.btn.submit")%>" onclick="modifyParallelGroup('<%=pgName%>');"></td>
    <td></td>
</tr>
<%
        }else {
            // 그냥 보기용
%>
<tr>
    <td><%=nvl(pg.getGroupName())%></td>
    <td><%=conv(pg.getGroupDesc())%></td>
    <td><%=nvl(pg.getMaxLimit())%></td>
    <td><%=nvl(pg.getCurrentRunning())%></td>
    <td><input class="Button" type="button" value="<%=Label.get("common.btn.edit")  %>" onclick="viewEditFormParallelGroup('<%=pg.getGroupName()%>');"></td>
    <td><input class="Button" type="button" value="<%=Label.get("common.btn.delete")%>" onclick="removeParallelGroup('<%=pg.getGroupName()%>');"></td>
</tr>
<%
    	}
    }
%>
</tbody>
</table>

</form>
<form name="form3" action="action_setting.jsp" method="POST">
<input type="hidden" name="suburl"             value="pgroup">
<input type="hidden" name="cmd"                value="">
<input type="hidden" name="parallel_group_for" value="<%=pgName%>">
<div>
	<input id="addButton"  class="Button" type="button" value="<%=Label.get("pgroup.add.btn")%>">
	<div id="dialogId" class="Dialog" style="display:none">
	    <div class="Dialog-contents">
			<table class="Table Width-100">
			<thead>
			<tr>
			    <th width="20%" class="required"><%=Label.get("pgroup.id")%></th>
			    <th width="50%" class="required"><%=Label.get("pgroup.desc")%></th>
			    <th width="20%"><%=Label.get("pgroup.max")%></th>
			    <th width="10%"></th>
			</tr>
			</thead>
			<tbody>
			<tr>
			    <td><input class="Textinput Width-100" type="text" name="groupName" value=""></td>
			    <td><input class="Textinput Width-100" type="text" name="groupDesc" value=""></td>
			    <td><input class="Textinput Width-100" data-keyfilter-rule="digits" type="text" name="maxLimit" value=""></td>
			    <td><input class="Button" type="button" value="<%=Label.get("common.btn.add")%>" onclick="addParallelGroup();"></td>
			</tr>
			</tbody>
			</table>
		</div>
	</div>
</div>

</form>

