<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
	String     agentid = nvl(request.getParameter("agentid"));
    boolean isEditMode = !Util.isBlank(agentid);

    AgentInfo agentInfo = null;
    ControllerAdminLocal admin = getControllerAdmin();
    if (isEditMode) {
    	agentInfo = admin.getAgentInfo(agentid);
    }else {
    	agentInfo = new AgentInfo();
    	agentInfo.setInUse(true);
    	agentInfo.setRunMode("S");
    }
%>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" /> --> 
<jsp:include page="display_msg.jsp" flush="true"/>
<title><%=Label.get("agent")%></title>
<script src="./script/app/include-lib.js"></script>
<script>
	function addNewAgentInfo() {
	    if (document.form1.id.value != '') {
	        if (confirm("<%=Label.get("common.add.confirm.msg")%>")) {
	            document.form1.cmd.value="add";
	            document.form1.submit();
	        }
	    }
	}
	
	function checkSubmit() {
    	if (document.form1.id.value.trim() == '') {
    		alert('<%=Label.get("common.required.field.missing", Label.get("agent.id"))%>');
    		return false;
    	}
    	if (document.form1.name.value.trim() == '') {
            alert('<%=Label.get("common.required.field.missing", Label.get("agent.name"))%>');
            return false;
    	}
    	if (document.form1.desc.value.trim() == '') {
            alert('<%=Label.get("common.required.field.missing", Label.get("agent.desc"))%>');
            return false;
    	}
    	if (document.form1.ip.value.trim() == '') {
            alert('<%=Label.get("common.required.field.missing", Label.get("agent.ip"))%>');
            return false;
    	}
    	if (document.form1.port.value.trim() == '') {
            alert('<%=Label.get("common.required.field.missing", Label.get("agent.port"))%>');
            return false;
    	}
    }
</script> 
</head>
<body onload="displayMsg();">

<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=isEditMode ? Label.get("agent.edit.popup.title") : Label.get("agent.add.popup.title") %>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<%-- <font size="5">
<%=Label.get("job.agent")%> <%=isEditMode ? Label.get("common.edit") : Label.get("common.new") %>
</font>--%>

<form name="form1" action="action_server.jsp" method="POST" onsubmit="return checkSubmit();">
<input type="hidden" name="cmd" value="<%=isEditMode ? "modify" : "add"%>_agent">
<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
<table class="Table njf-table__typea Margin-bottom-10" >
    <colgroup>
        <col width="22%">
        <col>
    </colgroup>
<tr>
    <th class="required"><%=Label.get("agent.id")%></th>
    <td><input class="Textinput Width-100" type="text" name="id" value="<%=conv(agentInfo.getId())%>"></td>
</tr>
<tr>
    <th class="required"><%=Label.get("agent.name")%></th>
    <td><input class="Textinput Width-100" type="text" name="name" value="<%=conv(agentInfo.getName())%>"></td>
</tr>
<tr>
    <th class="required"><%=Label.get("agent.desc")%></th>
    <td><input class="Textinput Width-100" type="text" name="desc" value="<%=conv(agentInfo.getDesc())%>"></td>
</tr>
<tr>
    <th class="required"><%=Label.get("agent.ip")%></th>
    <td><input class="Textinput Width-100" type="text" name="ip" value="<%=conv(agentInfo.getIp())%>"></td>
</tr>
<tr>
    <th class="required"><%=Label.get("agent.port")%></th>
    <td><input class="Textinput Width-100" type="text" name="port" value="<%=agentInfo.getPort()%>"></td>
</tr>
<tr>
    <th><%=Label.get("agent.runmode")%></th>
    <td><%=printRadioOptionAlx("runMode", "S", agentInfo.getRunMode(), Label.get("agent.jobrun.mode.standalone"))%>
        <%=printRadioOptionAlx("runMode", "W", agentInfo.getRunMode(), Label.get("agent.jobrun.mode.was"))%></td>
</tr>
<tr>
    <th><%=Label.get("agent.inuse.yn")%></th>
    <td><%=printRadioOptionAlx("inUse", "true",  String.valueOf(agentInfo.isInUse()), Label.get("agent.inuse.true"))%>
        <%=printRadioOptionAlx("inUse", "false", String.valueOf(agentInfo.isInUse()), Label.get("agent.inuse.false"))%></td>
</tr>
<!-- 
<tr>
    <td class="tabletitle"><%=Label.get("agent.directory")%></td>
    <td><input type="text" name="baseDirectory" style="width:90%" value="<%=conv(agentInfo.getBaseDirectory())%>" size="30"></td>
</tr>
<tr>
    <td class="tabletitle"><%=Label.get("agent.osuser")%></td>
    <td><input type="text" name="osUserId" value="<%=conv(agentInfo.getOsUserId())%>" size="10"></td>
</tr>
<tr>
    <td class="tabletitle"><%=Label.get("agent.run.command")%></td>
    <td><input type="text" name="startCmd" value="<%=conv(agentInfo.getStartCmd())%>" size="10"></td>
</tr>
<tr>
    <td class="tabletitle"><%=Label.get("agent.run.type")%></td>
    <td><select name="remoteStartType">
        <%=printSelectOption("",       "",      "")%>
        <%=printSelectOption("telnet", "Telnet",agentInfo.getRemoteStartType())%>
        <%=printSelectOption("ssh",    "SSH",   agentInfo.getRemoteStartType())%>
        <%=printSelectOption("local",  "Local", agentInfo.getRemoteStartType())%></select></td>
</tr>
<tr>
    <td class="tabletitle"><%=Label.get("agent.job.max.limit")%></td>
    <td><input type="text" name="maxRunningJob" value="<%=agentInfo.getMaxRunningJob()%>" size="3"></td>
</tr>
 -->
<%
	if (isEditMode) {
%>
<tr>
    <th><%=Label.get("common.lastmodifytime")%></th>
    <td><input class="Textinput Width-100" type="text" name="" value="<%=toDatetimeString(DateUtil.getTimestamp(agentInfo.getLastModifyTime()), false)%>" readonly></td>
</tr>
<%
	}
%>
</table>

<table class="Width-100 Margin-bottom-10">
	<tr>
		<td class="Text-center">
			<input class="Button" type="submit" value="<%=isEditMode ? Label.get("common.btn.edit") : Label.get("common.btn.add")%>" style="width:80px; height:35px">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()" style="width:80px; height:35px">
		</td>
	</tr>
</table>
</div>
</form>

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
