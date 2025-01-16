<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>

<script>
<% /* ============ JobGroup 속성 정의 관련 함수 ===================*/ %>
    function viewEditFormJobGroupAttrDef(attrId) {
        document.form1.action="view_setting.jsp";
        document.form1.cmd.value="form_modify_jobgroupattrdef";
        document.form1.attrid_for.value=attrId;
        document.form1.submit();
    }

    function modifyJobGroupAttrDef(attrId) {
    	var name = document.form1.name.value.trim();
    	if(name == null || name == "" || typeof name =="undefined") {
    		alert('<%=Label.get("common.required.field.missing", Label.get("jobgroup.attr.name"))%>');
    		document.form1.name.focus();
    		return false;
    	}
    	
        if (confirm("["+attrId+"] <%=Label.get("common.modify.confirm.msg")%>")) {
            document.form1.action="action_setting.jsp";
            document.form1.cmd.value="modify_jobgroupattrdef";
            document.form1.submit();
        }
    }

    function formAddNewJobGroupAttrDef() {
        document.form1.action="view_setting.jsp";
        document.form1.cmd.value="form_add_jobgroupattrdef";
        document.form1.submit();
    }

    function addJobGroupAttrDef() {
    	var id = document.form2.id.value.trim();    
    	var name = document.form2.name.value.trim();  
    	if(id == null || id == "" || typeof id =="undefined") {
    		alert('<%=Label.get("common.required.field.missing", Label.get("jobgroup.attr.id"))%>');
    		document.form2.id.focus();
    		return false;
    	}else if(name == null || name == "" || typeof name =="undefined") {
    		alert('<%=Label.get("common.required.field.missing", Label.get("jobgroup.attr.name"))%>');
    		document.form2.name.focus();
    		return false;
    	}
    	
        document.form2.action="action_setting.jsp";
        document.form2.cmd.value="add_jobgroupattrdef";
        document.form2.submit();
    }

    function removeJobGroupAttrDef(attrId) {
        if (confirm("["+attrId+"] <%=Label.get("common.remove.confirm.msg")%>")) {
            document.form1.action="action_setting.jsp";
            document.form1.cmd.value="delete_jobgroupattrdef";
            document.form1.attrid_for.value=attrId;
            document.form1.submit();
        }
    }
    
	$(document).ready(function () {
		$('#dialogId').css('display','');
		var title = "<%=Label.get("jobgroup.attr.add.popup.title")%>";
		layerPopOpen("addButton", "dialogId", title, "1000", "170", "50", "close", true, true, true, "fade", "200", false, false);
	});    

</script>

<center>

<%
    ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth auth      = new AdminAuth(getUserId(request), getUserIp(request));
    String cmd          = request.getParameter("cmd");
    String attrIdFor    = request.getParameter("attrid_for");

    List<JobGroupAttrDef> jobGroupAttrDefList = admin.getAllJobGroupAttrDefs();
    
    // ####################### JobGroup 속성 정의 #########################################
%>
<%-- <br>
<table border="0" width="100%">
<tr>
    <td colspan="100%" align="left"><font size=3><b><img src="images/icon_setting.png"/> <%=Label.get("jobgroup")+" "+Label.get("common.attribute")%></b></font></td>
</tr>
</table> --%>

<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("jobgroup")+" "+Label.get("common.attribute")%></div>
</div>

<form name="form1" action="action_setting.jsp" method="POST">
<input type="hidden" name="suburl"         value="jobgroupattrdef">
<input type="hidden" name="cmd"            value="">
<input type="hidden" name="attrid_for" value="<%=attrIdFor%>">

<table class="Table Margin-bottom-10 Width-100">
<thead>
<tr>
    <th style="padding:3px; width:8%"><%=Label.get("jobgroup.attr.id")%></th>
    <th style="padding:3px; width:8%"><%=Label.get("jobgroup.attr.name")%></th>
    <th style="padding:3px; width:20%"><%=Label.get("jobgroup.attr.desc")%></th>
    <th style="padding:3px; width:12%"><%=Label.get("jobgroup.attr.valuetype")%></th>
    <th style="padding:3px; width:10%"><%=Label.get("jobgroup.attr.validvalue")%></th>
    <th style="padding:3px; width:5%"><%=Label.get("jobgroup.attr.linecount")%></th>
    <th style="padding:3px; width:10%"><%=Label.get("jobgroup.attr.monitor")%></th>
    <th style="padding:3px; width:5%"><%=Label.get("jobgroup.attr.order")%></th>
    <th style="padding:3px; width:9%"><%=Label.get("common.lastmodifytime")%></th>
    <th></th>
    <th></th>
</tr>
</thead>
<tbody>
<%
	for (JobGroupAttrDef attrdef : jobGroupAttrDefList) {
		
        if ("form_modify_jobgroupattrdef".equals(cmd) && attrdef.getId().equals(attrIdFor)) {
            /* 변경폼용 */
%>
<tr>
    <td><input type="hidden" name="id" value="<%=attrdef.getId()%>"><%=attrdef.getId()%></td>
    <td><input class="Textinput Width-100" type="text" name="name" maxlength="100" value="<%=conv(attrdef.getName())%>"></td>
    <td><input class="Textinput Width-100" type="text" name="desc" maxlength="100" value="<%=conv(attrdef.getDesc())%>"></td>
    <td><select class="Select Width-100" name="valueType">
           <%=printSelectOption("TEXT",     attrdef.getValueType())%>
           <%=printSelectOption("TEXTAREA", attrdef.getValueType())%>
           <%=printSelectOption("LIST",     attrdef.getValueType())%>
        </select></td>
    <td><input class="Textinput Width-100" type="text" name="valueCheck" maxlength="300" value="<%=conv(attrdef.getValueCheck())%>"></td>
    <td><input class="Textinput Width-100" data-keyfilter-rule="digits" type="text" name="displayLine" maxlength="5" value="<%=attrdef.getDisplayLine()%>"></td>
    <td><input class="Checkbox" type="checkbox" name="displayMonitor" value="true" <%=attrdef.isDisplayMonitor() ? "checked" : ""%>></td>
    <td><input class="Textinput Width-100" data-keyfilter-rule="digits" type="text" name="displayOrder" maxlength="5" value="<%=attrdef.getDisplayOrder()%>"></td>
    <td></td>
    <td><input class="Button" type="button" value="<%=Label.get("common.btn.submit")%>" onclick="modifyJobGroupAttrDef('<%=attrIdFor%>');"></td>
    <td></td>
</tr>
<%
        }else {
            /* 보기용  */  
%>
<tr>
    <td><%=attrdef.getId()%></td>
    <td><%=conv(attrdef.getName())%></td>
    <td><%=conv(attrdef.getDesc())%></td>
    <td><%=conv(attrdef.getValueType())%></td>
    <td><%=conv(attrdef.getValueCheck())%></td>
    <td><%=attrdef.getDisplayLine()%></td>
    <td><%=attrdef.isDisplayMonitor() ? "O" : "x"%></td>
    <td><%=attrdef.getDisplayOrder()%></td>
    <td><%=toDatetimeString(DateUtil.getTimestamp(attrdef.getLastModifyTime()), true)%></td>
    <td><input class="Button" type="button" value="<%=Label.get("common.btn.edit")  %>" onclick="viewEditFormJobGroupAttrDef('<%=attrdef.getId()%>');"></td>
    <td><input class="Button" type="button" value="<%=Label.get("common.btn.delete")%>" onclick="removeJobGroupAttrDef('<%=attrdef.getId()%>');"></td>
</tr>
<%
    	}
    }
%>
</tbody>
</table>
</form>
<form name="form2" action="action_setting.jsp" method="POST">
<input type="hidden" name="suburl"         value="jobgroupattrdef">
<input type="hidden" name="cmd"            value="">
<input type="hidden" name="attrid_for" value="<%=attrIdFor%>">
<div>
	<input id="addButton"  class="Button" type="button" value="<%=Label.get("common.btn.add")%>">
	<div id="dialogId" class="Dialog" style="display:none">
	    <div class="Dialog-contents">
			<table class="Table Margin-bottom-10">
			<thead>
			<tr>
			    <th class="required" style="padding:3px; width:8%"><%=Label.get("jobgroup.attr.id")%></th>
			    <th class="required" style="padding:3px; width:8%"><%=Label.get("jobgroup.attr.name")%></th>
			    <th style="padding:3px; width:20%"><%=Label.get("jobgroup.attr.desc")%></th>
			    <th style="padding:3px; width:12%"><%=Label.get("jobgroup.attr.valuetype")%></th>
			    <th style="padding:3px; width:10%"><%=Label.get("jobgroup.attr.validvalue")%></th>
			    <th style="padding:3px; width:5%"><%=Label.get("jobgroup.attr.linecount")%></th>
			    <th style="padding:3px; width:10%"><%=Label.get("jobgroup.attr.monitor")%></th>
			    <th style="padding:3px; width:5%"><%=Label.get("jobgroup.attr.order")%></th> 
			    <th></th>
			</tr>
			</thead>
			<tbody>
			<tr>
			    <td><input class="Textinput Width-100" type="text" name="id" value=""></td>
			    <td><input class="Textinput Width-100" type="text" name="name" maxlength="100" value=""></td>
			    <td><input class="Textinput Width-100" type="text" name="desc" maxlength="100" value=""></td>
			    <td><select class="Select Width-100" name="valueType">
			           <%=printSelectOption("TEXT",     "")%>
			           <%=printSelectOption("TEXTAREA", "")%>
			           <%=printSelectOption("LIST",     "")%>
			        </select></td>
			    <td><input class="Textinput Width-100" type="text" name="valueCheck" maxlength="300" value=""></td>
			    <td><input class="Textinput Width-100" data-keyfilter-rule="digits" type="text" name="displayLine" maxlength="5" value=""></td>
			    <td><input class="Checkbox" type="checkbox" name="displayMonitor" value="true" ></td>
			    <td><input class="Textinput Width-100" data-keyfilter-rule="digits" type="text" name="displayOrder" maxlength="5" value=""></td>
			    <td><input class="Button" type="button" value="<%=Label.get("common.btn.add")%>" onclick="addJobGroupAttrDef();"></td>
			</tr>
			</tbody>
			</table>
		</div>
	</div>
</div>

</form>
</center>
