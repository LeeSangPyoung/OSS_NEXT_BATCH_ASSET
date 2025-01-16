<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>

<script>
$a.page(function() {
    // 초기화 함수
    this.init = function(id, param) {
    	$("[id^='tdList']").css({'background':'#FFFF99'});    	
    }
});

<% /* ================== JobNotify 관련 함수 ======================*/ %>
	function getCheckedCount(chk) {
		if (chk.length == null) { /* 하나일때 */
			return chk.checked ? 1 : 0;
		}else {
			var cnt = 0;
			for (var i=0; i<chk.length; i++) {
				cnt += chk[i].checked ? 1 : 0;
			}
			return cnt;
		}
	}
	
	function checkAll() {
	    var chk = document.form1.chknotifyid;
	    var v = document.form1.chkall.checked;
	    
	    if (typeof chk =="undefined") return false;
	    
	    if (chk.length == null) { /* 하나일때 */
	        chk.checked = v ;
	    }else {
	        for (var i=0; i<chk.length; i++ ) {
	            chk[i].checked = v ;
	        }
	    }
	}
	
	function openEditFormJobNotify(notifyid) {
		var left = (window.screen.width  / 2) - ((700 / 2) + 10);
	    var top  = (window.screen.height / 2) - ((500 / 2) + 50);
		window.open("form_setting_notify.jsp?notifyid="+notifyid, 'form_notify_'+notifyid, 'width=810,height=500,left='+left+',top='+top+',scrollbars=1,resizable=1').focus();
		             
	}
	
	function removeJobNotify() {
		if (getCheckedCount(document.form1.chknotifyid) == 0) {
			alert("Not checked");
			return;
		}
	    if (confirm("<%=Label.get("common.remove.confirm.msg")%>")) {
	        document.form1.cmd.value="remove_notify";
	        document.form1.submit();
	    }
	}
</script>

<%
    ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth            auth  = new AdminAuth(getUserId(request), getUserIp(request));
    
    List<JobNotify>                 jobNotifies           = admin.getAllJobNotifies();
    Map<Integer, JobNotifyReceiver> jobNotifyReceiversMap = admin.getAllJobNotifyReceiversMap();
    
    String cmd         = request.getParameter("cmd");
%>
<%-- <br>
<table border="0" width="100%">
<tr>
    <td colspan="100%" align="left"><font size=3><b><img src="images/icon_setting.png"/> <%=Label.get("notify.title")%></b></font></td>
</tr>
</table> --%>

<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("notify.title")%></div>
</div>

<form name="form1" action="action_notify.jsp" method="get">
<input type="hidden" name="suburl" value="notify">
<input type="hidden" name="cmd" value="">

<table class="Table Width-100 Margin-bottom-10">
<thead>
<tr>
    <th style="width:3%;"><input class="Checkbox" type="checkbox" id="chkall" name="chkall" onclick="checkAll();"></th>
    <th style="width:3%;">#</th>
    <th style="width:30%"><%=Label.get("notify.jobid.pattern")%></th>
    <th><%=Label.get("common.desc")%></th>
    <th class="Width-10"><%=Label.get("notify.when")%></th>
    <th class="Width-10"><%=Label.get("common.lastmodifytime")%></th>
</tr>
</thead>
<tbody>
<%
    Collections.sort(jobNotifies, getComparator("getJobIdExpression", true));

	int i=0;
    for (JobNotify notify : jobNotifies) {
%>
<tr>
    <td rowspan=2  style="padding:0px;"><input class="Checkbox" type="checkbox" id="chknotifyid" name="chknotifyid" value="<%=notify.getId()%>"></td>
    <td rowspan=2 ><%=(++i) %></td>
    <td rowspan=2 bgcolor="#F5EDFF"
    	onMouseOver="this.style.cursor='pointer';this.style.backgroundColor='#AAAAFF';"
    	onMouseOut ="this.style.backgroundColor='#F5EDFF';" 
    	onclick="javascript:openEditFormJobNotify('<%=notify.getId()%>')">
    	<B><%=nvl(notify.getJobIdExpression()).replaceAll("\\|", "|<BR>")%></B></td>
    <td><b><%=conv(notify.getDesc())%></b></td>
    <td><b>
    	<% if("EO".equals(notify.getWhen())) {%>
    		<font color="blue">End OK</font>
    	<% } else if("EF".equals(notify.getWhen())) {%>
    		<font color="red">End Fail</font>
    	<% } else if("LONGRUN".equals(notify.getWhen())) {%>
    		<font color="#ff7f27">Long Run<br/>(<%=notify.getCheckValue1()%><%=Label.get("common.minute")%>, <%=notify.getCheckValue2()%><%=Label.get("common.minute")%>, <%=notify.getCheckValue3()%><%=Label.get("common.count.unit")%>)</font>
    	<% } else {%>
    		N/A
    	<% } %>
    </b></td>
    <td><%=toDatetimeString(DateUtil.getTimestamp(notify.getLastModifyTime()), true)%></td>
</tr>
<tr>
    <td colspan="3" style="word:no-break">
        <table class="Table Width-100 njf-table__typea">
<%
		List<JobNotifyReceiver> receivers = new ArrayList();
        for (int receiverId : notify.getReceiverList()) { // 상세 수신자 목록 리스트
            JobNotifyReceiver receiver = jobNotifyReceiversMap.get(receiverId);
        	if (receiver != null) {
        		receivers.add(receiver);
        	}
        }

        Collections.sort(receivers, getComparator("getName", true));

		int j=0;
        for (JobNotifyReceiver receiver : receivers) { // 상세 수신자 목록 리스트
%>
            <tr>
            	<td id="tdList" style="width:5%"><%=(++j)%></td>
                <td id="tdList" style="width:12%"><%=conv(receiver.getName())%></td>
                <td id="tdList" style="width:20%"><%=conv(receiver.getDesc())%></td>
                <td id="tdList" class="Text-left"><%=printJobNotifyReceiveInfo(receiver) %></td>
            </tr>
<%
        }
        if (j==0) {
%>
            <tr>
            	<td id="tdList" style="text-align:center">No Data</td>
            </tr>
<%        	
        }
%>
        </table>
    </td>
</tr>
<%
    }
%>
</tbody>
</table>

<input class="Button" type="button" value="<%=Label.get("common.btn.add")%>" onclick="openEditFormJobNotify('0');">
<input class="Button" type="button" value="<%=Label.get("common.btn.delete")%>" onclick="removeJobNotify();">
</form>
