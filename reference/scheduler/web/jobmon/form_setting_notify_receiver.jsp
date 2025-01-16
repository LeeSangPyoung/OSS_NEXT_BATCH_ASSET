<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
	int     receiverid = toInt(request.getParameter("receiverid"), 0);
    boolean isEditMode = receiverid > 0;

    JobNotifyReceiver receiver = null;
    ControllerAdminLocal admin = getControllerAdmin();
    if (isEditMode) {
    	receiver = admin.getJobNotifyReceiver(receiverid);
    }else {
    	receiver = new JobNotifyReceiver();
    }
%>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<jsp:include page="display_msg.jsp" flush="true"/>
<title><%=Label.get("notify.receiver") %></title>
<script src="./script/app/include-lib.js"></script>
<script>
	function checkSubmit() {
		if (document.form1.name.value.trim() == '') {
	        alert('<%=Label.get("common.required.field.missing", Label.get("common.name"))%>');
	        document.form1.name.focus();
	        return false;
		}else if (document.form1.desc.value.trim() == '') {
	        alert('<%=Label.get("common.required.field.missing", Label.get("common.desc"))%>');
	        document.form1.desc.focus();
	        return false;
		}    	
	}
</script> 
</head>
<body onload="displayMsg();">
<%-- <font size="5">
<%=Label.get("notify.receiver") %> <%=isEditMode ? Label.get("common.edit") : Label.get("common.new") %>
</font>
<br><br> --%>

<center>
<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=Label.get("notify.receiver") %> <%=isEditMode ? Label.get("common.edit") : Label.get("common.new") %>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
<form name="form1" action="action_notify.jsp" method="POST" onsubmit="return checkSubmit();">
<input type="hidden" name="cmd" value="<%=isEditMode ? "modify" : "add"%>_notify_receiver">
<input type="hidden" name="id" value="<%=receiver.getId()%>">
<!-- <table border="1" style = "border-collapse:collapse" bordercolor = "#a0a0a0" cellpadding="1" width="90%"> -->
<table class="Table njf-table__typea Margin-bottom-10" >
    <colgroup>
        <col width="22%">
        <col>
    </colgroup>
<tr>
    <th class="required"><%=Label.get("common.name")%></th>
    <td><input class="Textinput Width-100" type="text" name="name" value="<%=conv(receiver.getName())%>"></td>
</tr>
<tr>
    <th class="required"><%=Label.get("common.desc")%></th>
    <td><input class="Textinput Width-100" type="text" name="desc" style="width:100%" value="<%=conv(receiver.getDesc())%>"></td>
</tr>
<tr>
    <th><%=Label.get("notify.email")%></th>
    <td><%=printRadioOptionAlx("recvByEmail", "true",  String.valueOf(receiver.isRecvByEmail()), Label.get("notify.true"))%>&nbsp;
        <%=printRadioOptionAlx("recvByEmail", "false", String.valueOf(receiver.isRecvByEmail()), Label.get("notify.false"))%>
    	<input class="Textinput Margin-left-10" style="width:77.4%" type="text" name="emailAddr" value="<%=conv(receiver.getEmailAddr())%>"></td>
</tr>
<tr>
    <th><%=Label.get("notify.sms")%></th>
    <td><%=printRadioOptionAlx("recvBySms", "true",  String.valueOf(receiver.isRecvBySms()), Label.get("notify.true"))%>&nbsp;
        <%=printRadioOptionAlx("recvBySms", "false", String.valueOf(receiver.isRecvBySms()), Label.get("notify.false"))%>
        <input class="Textinput Margin-left-10" style="width:77.4%" type="text" name="smsNum" value="<%=conv(receiver.getSmsNum())%>"></td>
</tr>
<!-- 
<tr>
    <td class="tabletitle"><%=Label.get("notify.terminal")%></td>
    <td><%=printRadioOptionAlx("recvByTerminal", "true",  String.valueOf(receiver.isRecvByTerminal()), Label.get("notify.true"))%>
        <%=printRadioOptionAlx("recvByTerminal", "false", String.valueOf(receiver.isRecvByTerminal()), Label.get("notify.false"))%>
        <input type="text" name="terminalId" style="width:50%" value="<%=conv(receiver.getTerminalId())%>" size="10"></td>
</tr>
<tr>
    <td class="tabletitle"><%=Label.get("notify.messenger")%></td>
    <td><%=printRadioOptionAlx("recvByMessenger", "true",  String.valueOf(receiver.isRecvByMessenger()), Label.get("notify.true"))%>
        <%=printRadioOptionAlx("recvByMessenger", "false", String.valueOf(receiver.isRecvByMessenger()), Label.get("notify.false"))%>
        <input type="text" name="messengerId" style="width:50%" value="<%=conv(receiver.getMessengerId())%>" size="10"></td>
</tr>
<tr>
    <td class="tabletitle">Device 1</td>
    <td><%=printRadioOptionAlx("recvByDev1", "true",  String.valueOf(receiver.isRecvByDev1()), Label.get("notify.true"))%>
        <%=printRadioOptionAlx("recvByDev1", "false", String.valueOf(receiver.isRecvByDev1()), Label.get("notify.false"))%>
        <input type="text" name="dev1Point" style="width:50%" value="<%=conv(receiver.getDev1Point())%>" size="10"></td>
</tr>
<tr>
    <td class="tabletitle">Device 2</td>
    <td><%=printRadioOptionAlx("recvByDev2", "true",  String.valueOf(receiver.isRecvByDev2()), Label.get("notify.true"))%>
        <%=printRadioOptionAlx("recvByDev2", "false", String.valueOf(receiver.isRecvByDev2()), Label.get("notify.false"))%>
        <input type="text" name="dev2Point" style="width:50%" value="<%=conv(receiver.getDev2Point())%>" size="10"></td>
</tr>
<tr>
    <td class="tabletitle">Device 3</td>
    <td><%=printRadioOptionAlx("recvByDev3", "true",  String.valueOf(receiver.isRecvByDev3()), Label.get("notify.true"))%>
        <%=printRadioOptionAlx("recvByDev3", "false", String.valueOf(receiver.isRecvByDev3()), Label.get("notify.false"))%>
        <input type="text" name="dev3Point" style="width:50%" value="<%=conv(receiver.getDev3Point())%>" size="10"></td>
</tr>
-->
<%
	if (isEditMode) {
%>
<tr>
    <th><%=Label.get("common.lastmodifytime")%></th>
    <td><input class="Textinput Width-100"  type="text" name="" value="<%=toDatetimeString(DateUtil.getTimestamp(receiver.getLastModifyTime()), false)%>" readonly></td>
</tr>
<%
	}
%>
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
    if (Util.toBoolean(request.getParameter("doreload"))) {
%>
<script>
    opener.location.reload();
</script>
<%
    }
%>
</html>
