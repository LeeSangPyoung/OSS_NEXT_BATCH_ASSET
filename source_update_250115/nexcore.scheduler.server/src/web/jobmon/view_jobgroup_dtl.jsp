<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    ControllerAdminLocal admin = getControllerAdmin();
	String jobgroupid = request.getParameter("jobgroupid");
    JobGroup jobgroup = admin.getJobGroup(jobgroupid);
    if (jobgroup == null) {
    	throw new RuntimeException ("JobGroup '"+jobgroupid+"' not found");
    }
    List<JobGroupAttrDef> jobGroupAttrDefs = admin.getJobGroupAttrDefsByQuery("", " ORDER BY DISPLAY_ORDER ");
%>
<html>
<head>
<script src="./script/app/include-lib.js"></script>
<jsp:include page="display_msg.jsp" flush="true"/>
<title>Job Group </title>
</head>
<body onload="displayMsg();">
<center>

<div class="header-wrap">
	<div class="header">
		<div class="header-title">
			Job Group
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">

<table class="Table njf-table__typea Width-100 Margin-bottom-10" >
<colgroup>
    <col width="22%">
    <col>
</colgroup>
<tr>
    <th><%=Label.get("jobgroup")%> ID</th>
    <td><%=conv(jobgroup.getId()) %></td>
</tr>
<tr>
    <th><%=Label.get("common.name")%></th>
    <td><%=conv(jobgroup.getName()) %></td>
</tr>
<tr>
    <th><%=Label.get("common.desc")%></th>
    <td><textarea class="Textarea Width-100" name="desc" rows="3" readonly><%=conv(jobgroup.getDesc()) %></textarea></td>
</tr>
<tr>
    <th><%=Label.get("common.parent")%> <%=Label.get("jobgroup")%> ID</th>
    <td><%=jobgroup.getParentId() %></td>
</tr>
<%
    for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
    	String attrValue = jobgroup.getAttribute(attrDef.getId());
%>
<tr>
    <th><a title="[<%=conv(attrDef.getId())%>] <%=conv(attrDef.getDesc())%>"><%=conv(attrDef.getName())%></a></th>
<%      if ("TEXTAREA".equals(attrDef.getValueType())) {   %>
    <td><textarea class="Textarea Width-100" name="attr_<%=conv(attrDef.getId())%>" rows="<%=attrDef.getDisplayLine()%>"><%=conv(attrValue)%></textarea></td>
<%      }else { %>
    <td><%=conv(attrValue)%></td>
<%      }                                                  %>    
</tr>
<%  }                                                      %>    
<tr>
    <th><%=Label.get("common.creator")%></th>
    <td><%=jobgroup.getCreatorId() %></td>
</tr>
<tr>
    <th><%=Label.get("common.createtime")%></th>
    <td><%=toDatetimeString(DateUtil.getTimestamp(jobgroup.getCreateTime()), false)%></td>
</tr>

<tr>
    <th><%=Label.get("common.lastmodifytime")%></th>
    <td><%=toDatetimeString(DateUtil.getTimestamp(jobgroup.getLastModifyTime()), false)%></td>
</tr>
</table>

<table class="Width-100 Margin-bottom-10">
	<tr>
		<td class="Text-center">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()" style="width:80px; height:35px">
		</td>
	</tr>
</table>

</div>
</center>
</body>
</html>
