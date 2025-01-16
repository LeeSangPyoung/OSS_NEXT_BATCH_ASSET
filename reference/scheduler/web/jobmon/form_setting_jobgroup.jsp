<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    boolean isEditMode = !Util.isBlank(request.getParameter("jobgroupid"));

    JobGroup jobgroup = null;
    ControllerAdminLocal admin = getControllerAdmin();
    if (isEditMode) {
    	jobgroup = admin.getJobGroup(request.getParameter("jobgroupid"));
    }else {
    	jobgroup = new JobGroup();
    	jobgroup.setParentId(request.getParameter("parentid"));
    	jobgroup.setCreatorId(getUserId(request));
    	jobgroup.setOwnerId(getUserId(request));
    }
    List<JobGroupAttrDef> jobGroupAttrDefs = admin.getJobGroupAttrDefsByQuery("", " ORDER BY DISPLAY_ORDER ");
%>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<jsp:include page="display_msg.jsp" flush="true"/>
<title>Job Group </title>
<script src="./script/app/include-lib.js"></script>
<script>
    function openJobGroupSelectWin(targetElemName) {
        window.open("popup_jobgroup.jsp?target_name="+targetElemName, 'jobgroupselect', 'width=700,height=400,scrollbars=1').focus();
    }

    function checkSubmit() {
    	if (document.form1.id.value.trim() == '') {
    		alert('<%=Label.get("common.required.field.missing", Label.get("jobgroup") + " ID")%>');
    		return false;
    	}
    	else if (document.form1.name.value.trim() == '') {
            alert('<%=Label.get("common.required.field.missing", Label.get("common.name"))%>');
            return false;
    	}
		else if (document.form1.parentId.value.trim() == '') {
            alert('<%=Label.get("common.required.field.missing", Label.get("common.parent") + Label.get("jobgroup") + " ID")%>');
            return false;
		}
		else if (document.form1.creatorId.value.trim() == '') {
            alert('<%=Label.get("common.required.field.missing", Label.get("common.creator"))%>');
            return false;
    	}
		
		return true;
    }
    
</script> 
</head>
<body onload="displayMsg();">

<center>
<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			Job Group <%=isEditMode ? Label.get("common.edit") : Label.get("common.new") %>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="팝업창 닫기" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<%-- <font size="5">
Job Group <%=isEditMode ? Label.get("common.edit") : Label.get("common.new") %>
</font> --%>

<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
<form name="form1" method="post" action="action_setting.jsp" onsubmit="return checkSubmit();">
<input type="hidden" name="cmd" value="<%=isEditMode ? "modify" : "add"%>_jobgroup">
<input type="hidden" name="ownerId" value="<%=jobgroup.getOwnerId() %>" > <% /* 담당자 기능은 추가 구현될때 활성화 한다. 당분간은 creator 로 set 한다. */ %>

<table class="Table njf-table__typea Margin-bottom-10" >
    <colgroup>
        <col width="22%">
        <col>
    </colgroup>
<tr>
    <th class="required"><%=Label.get("jobgroup")%> ID</th>
    <td><input class="Textinput Width-100" type="text" name="id" value="<%=conv(jobgroup.getId()) %>" <%=isEditMode ? "readonly" : ""%> ></td>
</tr>
<tr>
    <th class="required"><%=Label.get("common.name")%></th>
    <td><input class="Textinput Width-100" type="text" name="name" value="<%=conv(jobgroup.getName()) %>"></td>
</tr>
<tr>
    <th><%=Label.get("common.desc")%></th>
    <td><textarea class="Textarea Width-100" name="desc" rows="3"><%=conv(jobgroup.getDesc()) %></textarea></td>
</tr>
<tr>
    <th class="required"><%=Label.get("common.parent")%> <%=Label.get("jobgroup")%> ID</th>
    <td><input class="Textinput Width-85 Margin-right-5" type="text" name="parentId" value="<%=jobgroup.getParentId() %>">
        <input class="Button" type="button" value="<%=Label.get("common.select")%>" onclick="openJobGroupSelectWin('parentId');"> 
    </td>
</tr>
<%
    for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
    	String attrValue = jobgroup.getAttribute(attrDef.getId());
%>
<tr>
    <th><a title="[<%=conv(attrDef.getId())%>] <%=conv(attrDef.getDesc())%>"><%=conv(attrDef.getName())%></a></th>
<%      if ("TEXTAREA".equals(attrDef.getValueType())) {   %>
    <td><textarea class="Textarea Width-100" name="attr_<%=conv(attrDef.getId())%>" rows="<%=attrDef.getDisplayLine()%>"><%=conv(attrValue)%></textarea></td>
<%      }else if ("LIST".equals(attrDef.getValueType())) { %>
    <td>
        <select class="Select Width-100" name="attr_<%=conv(attrDef.getId())%>">
<%
           	String valueList = nvl(attrDef.getValueCheck());
           	String[] values = valueList.split("\\|");
           	for (String value : values) {
           		out.println(printSelectOption(value, attrValue));
           	}
%>
        </select>
    </td>
<%      }else if ("TEXT".equals(attrDef.getValueType())) { %>
    <td><input class="Textinput Width-100" type="text" name="attr_<%=conv(attrDef.getId())%>" value="<%=conv(attrValue)%>"></td>
<%      }                                                  %>    
</tr>
<%  }                                                      %>    
<tr>
    <th class="required"><%=Label.get("common.creator")%></th>
    <td><input class="Textinput Width-100" type="text" name="creatorId" value="<%=jobgroup.getCreatorId() %>" readonly></td>
</tr>
<tr>
    <th><%=Label.get("common.createtime")%></th>
    <td><input class="Textinput Width-100" type="text" name="" value="<%=jobgroup.getCreateTime()==null ? "" : toDatetimeString(DateUtil.getTimestamp(jobgroup.getCreateTime()), false)%>" readonly></td>
</tr>

<tr>
    <th><%=Label.get("common.lastmodifytime")%></th>
    <td><input class="Textinput Width-100" type="text" name="" value="<%=jobgroup.getLastModifyTime()==null ? "" : toDatetimeString(DateUtil.getTimestamp(jobgroup.getLastModifyTime()), false)%>" readonly></td>
</tr>
</table>

<table class="Width-100">
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
    if ("yes".equals(request.getParameter("doreload"))) {
%>
<script>
    opener.location.reload();
</script>
<%
    }
%>
</html>
