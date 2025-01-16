<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
	int     notifyid = toInt(request.getParameter("notifyid"), 0);
    boolean isEditMode = notifyid > 0;

    ControllerAdminLocal admin = getControllerAdmin();

    JobNotify                notify            = null;
    if (isEditMode) {
    	notify = admin.getJobNotify(notifyid);
    }else {
    	notify = new JobNotify();
    	notify.setWhen("EF"); /* 기본값은 End Fail */
    	notify.setReceivers(null); /* 내부 List 객체 만들기용 초기화 */
    }

    Map<Integer, JobNotifyReceiver> allReceiversMap = admin.getAllJobNotifyReceiversMap();
    
    List<JobNotifyReceiver>         selectedReceivers    = new ArrayList();
    for (int receiverId : notify.getReceiverList()) {
    	JobNotifyReceiver receiver = allReceiversMap.remove(receiverId);
    	if ( receiver != null) {
    		selectedReceivers.add(receiver);
    	}
    }
    
    List<JobNotifyReceiver>         unselectedReceivers  = new ArrayList<JobNotifyReceiver>(allReceiversMap.values());
    
%>
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<jsp:include page="display_msg.jsp" flush="true"/>
<title><%=Label.get("notify.title") %></title>
<script src="./script/app/include-lib.js"></script>
<script>
	function checkAll() {
	    var chk = document.form1.chkreceiverid;
	    var v = document.form1.chkall.checked;
	    if (chk.length == null) { /* 하나일때 */
	        chk.checked = v ;
	    }else {
	        for (var i=0; i<chk.length; i++ ) {
	            chk[i].checked = v ;
	        }
	    }
	}

    function checkSubmit() {
    	if (document.form1.jobIdExpression.value.trim() == '') {
    		alert('<%=Label.get("common.required.field.missing", Label.get("notify.jobid.pattern"))%>');
    		return false;
    	}
    }
    
    function changeWhen() {
    	var radioValue = document.form1.when.value;
    	
    	
    	document.getElementById("LongRun").style.display = 'none';
    	
    	if(radioValue == "LONGRUN")
    		document.getElementById("LongRun").style.display = '';
    	
    }
</script> 
</head>
<body onload="displayMsg();">

<%-- <font size="5">
<%=Label.get("notify.title") %> <%=isEditMode ? Label.get("common.edit") : Label.get("common.new") %>
</font> --%>

<center>
<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=Label.get("notify.title") %> <%=isEditMode ? Label.get("common.edit") : Label.get("common.new") %>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="팝업창 닫기" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
<form name="form1" action="action_notify.jsp" method="POST" onsubmit="return checkSubmit();">
<input type="hidden" name="cmd" value="<%=isEditMode ? "modify" : "add"%>_notify">
<input type="hidden" name="id" value="<%=notify.getId()%>">
<!-- <table border="1" style = "border-collapse:collapse" bordercolor = "#a0a0a0" cellpadding="1" width="90%"> -->
<table class="Table njf-table__typea Margin-bottom-10" >
    <colgroup>
        <col width="22%">
        <col width="78%">
    </colgroup>
<tr>
    <th class="required"><%=Label.get("notify.jobid.pattern")%></th>
    <td><textarea class="Textarea Width-100" name="jobIdExpression" rows=3 ><%=nvl(notify.getJobIdExpression())%></textarea></td>
</tr>
<tr>
    <th><%=Label.get("common.desc")%></th>
    <td><input class="Textinput Width-100" type="text" name="desc" value="<%=conv(notify.getDesc())%>"></td>
</tr>
<tr>
    <th><%=Label.get("notify.when")%></th>
    <td>
        <% if(notify.getWhen() != null && notify.getWhen().equals("EO")) {%>
        	<label><input class='Radio Margin-right-5' type='radio' name='when' value='EO' checked onchange="changeWhen();">End OK</label>
        <%} else {%>
        	<label><input class='Radio Margin-right-5' type='radio' name='when' value='EO' onchange="changeWhen();">End OK</label>
        <%} %>
        <% if(notify.getWhen() == null || notify.getWhen().equals("EF")) {%>
        	<label><input class='Radio Margin-right-5' type='radio' name='when' value='EF' checked onchange="changeWhen();">End Fail</label>
        <%} else {%>
        	<label><input class='Radio Margin-right-5' type='radio' name='when' value='EF' onchange="changeWhen();">End Fail</label>
        <%} %>
        <% if(notify.getWhen() != null && notify.getWhen().equals("LONGRUN")) {%>
        	<label><input class='Radio Margin-right-5' type='radio' name='when' value='LONGRUN' checked onchange="changeWhen();">LONGRUN</label>
        <%} else {%>
        	<label><input class='Radio Margin-right-5' type='radio' name='when' value='LONGRUN' onchange="changeWhen();">LONGRUN</label>
        <%} %>
    </td>
</tr>
<tr id="LongRun">
	<th>
		Long Run
	</th>
	<td>
		<table class="Table njf-table__typea">
		<tr>
		    <th><%=Label.get("notify.time")%>(<%=Label.get("common.minute")%>)</th>
		    <td><input class="Textinput Width-100" type="text" name="checkValue1" value="<%=notify.getCheckValue1()!=null?notify.getCheckValue1():""%>"/></td>
		    <th><%=Label.get("notify.interval")%>(<%=Label.get("common.minute")%>)</th>
		    <td><input class="Textinput Width-100" type="text" name="checkValue2" value="<%=notify.getCheckValue2()!=null?notify.getCheckValue2():""%>"/></td>
		    <th><%=Label.get("notify.count")%>(<%=Label.get("common.count.unit")%>)</th>
		    <td><input class="Textinput Width-100" type="text" name="checkValue3" value="<%=notify.getCheckValue3()!=null?notify.getCheckValue3():""%>"/></td>
		</tr>
		</table>
	</td>
</tr>
<tr>
    <th><%=Label.get("notify.receiver")%></th>
    <td>
		<table class="Table Width-100">
		<thead>
		<tr>
		    <th class="Width-5"><input class="Checkbox" type="checkbox" id="chkall" name="chkall" onclick="checkAll();"></th>
		    <th><%=Label.get("common.name")%></th>
		    <th><%=Label.get("common.desc")%></th>
		    <th><%=Label.get("notify.point") %></th>
		</tr>
		</thead>
		<tbody>
<%
	Collections.sort(selectedReceivers, getComparator("getName", true));
	for (JobNotifyReceiver receiver : selectedReceivers) { /* 이미 설정된 수신자 먼저 display */
%>
		<tr>
		    <td style="text-align:center; padding:0px"><input class="Checkbox" type="checkbox" id="chkreceiverid"  name="chkreceiverid" value="<%=receiver.getId()%>" checked></td>
		    <td><%=nvl(receiver.getName())%></td>
		    <td><%=nvl(receiver.getDesc())%></td>
		    <td class="Text-left"><%=printJobNotifyReceiveInfo(receiver) %></td>
		</tr>
<%
	}

	Collections.sort(unselectedReceivers, getComparator("getName", true));
	for (JobNotifyReceiver receiver : unselectedReceivers) { /* 나머지 아직 선택 안된 수신자 display */
%>
		<tr>
		    <td style="text-align:center; padding:0px"><input class="Checkbox" type="checkbox" id="chkreceiverid" name="chkreceiverid" value="<%=receiver.getId()%>"></td>
		    <td><%=nvl(receiver.getName())%></td>
		    <td><%=nvl(receiver.getDesc())%></td>
		    <td class="Text-left"><%=printJobNotifyReceiveInfo(receiver) %></td>
		</tr>
<%
	}
%>
		</tbody>
		</table>
    </td>
</tr>
<%
	if (isEditMode) {
%>
<tr>
    <th><%=Label.get("common.lastmodifytime")%></th>
    <td><input class="Textinput Width-100" type="text" name="" value="<%=toDatetimeString(DateUtil.getTimestamp(notify.getLastModifyTime()), false)%>" readonly></td>
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
