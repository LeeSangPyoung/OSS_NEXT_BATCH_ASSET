<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth            auth  = new AdminAuth(getUserId(request), getUserIp(request));
    String orderby      = nvl( request.getParameter("orderby"),  "getName");
    String orderdir     = nvl( request.getParameter("orderdir"), "ASC");

    String name         = nvl( request.getParameter("name"));
    String desc         = nvl( request.getParameter("desc"));
    String emailaddr    = nvl( request.getParameter("emailaddr"));
    String smsnum       = nvl( request.getParameter("smsnum"));
    String terminalid   = nvl( request.getParameter("terminalid"));
    String messengerid  = nvl( request.getParameter("messengerid"));
    String dev1point    = nvl( request.getParameter("dev1point"));
    String dev2point    = nvl( request.getParameter("dev2point"));
    String dev3point    = nvl( request.getParameter("dev3point"));
    
    List<JobNotifyReceiver> jobNotifyReceivers = admin.getAllJobNotifyReceivers();

    Map m = new HashMap();
    m.put("getName",         name);
    m.put("getDesc",         desc);
    m.put("getEmailAddr",    emailaddr);
    m.put("getSmsNum",       smsnum);
    m.put("getTerminalId",   terminalid);
    m.put("getMessengerId",  messengerid);
    m.put("getDev1Point",    dev1point);
    m.put("getDev2Point",    dev2point);
    m.put("getDev3Point",    dev3point);
    
    eliminateListByFilter(jobNotifyReceivers, m);
    Collections.sort(jobNotifyReceivers, getComparator(orderby, "ASC".equals(orderdir)));
%>
<script>
<% /* ================== JobNotifyReceiver 관련 함수 ======================*/ %>
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
	    var chk = document.form1.chkreceiverid;
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

	function openEditFormJobNotifyReceiver(receiverid) {
    	/* var left = (window.screen.width  / 2) - ((450 / 2) + 10);
        var top  = (window.screen.height / 2) - ((300 / 2) + 50);
    	window.open("form_setting_notify_receiver.jsp?receiverid="+receiverid, 'form_notify_receiver_'+receiverid, 'width=820,height=300,left='+left+',top='+top+').focus(); */
    	window.open("form_setting_notify_receiver.jsp?receiverid="+receiverid, 'form_notify_receiver_'+receiverid, 'width=810,height=350,scrollbars=1').focus();
    }
    
    function removeJobNotifyReceiver() {
    	if (getCheckedCount(document.form1.chkreceiverid) == 0) {
			alert("Not checked");
			return;
		}
        if (confirm("<%=Label.get("common.remove.confirm.msg")%>")) {
            document.form1.cmd.value="remove_notify_receiver";
            document.form1.submit();
        }
    }

    function orderby(orderbyCol) {
    	var orderdir;
        if ('<%=orderby%>' == orderbyCol) {
            if ('<%=orderdir%>' == 'ASC') {
                orderdir = 'DESC';
            }else {
                orderdir = 'ASC';
            }
        }else {
            orderdir = 'ASC';
        }
        window.location.href = 'view_setting.jsp?suburl=notifyreceiver&orderby='+orderbyCol+'&orderdir='+orderdir;
    }

</script>

<%-- <br>
<table border="0" width="100%">
<tr>
    <td colspan="100%" align="left"><font size=3><b><img src="images/icon_setting.png"/> <%=Label.get("notify.receiver")%></b></font></td>
</tr>
</table> --%>

<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("notify.receiver")%></div>
</div>

<form name="form0" action="view_setting.jsp" method="GET">
<input type="hidden" name="suburl" value="notifyreceiver">
<input type="hidden" name="orderby" value="<%=orderby%>">
<input type="hidden" name="orderdir" value="<%=orderdir%>">

<table class="Table Width-100 njf-table__typea Margin-bottom-10">
<tr>
    <th style="width:5%; padding:2px"><%=Label.get("common.name")%></th>
    <td style="width:15%;"><input class="Textinput Width-100" type="text" name="name" value="<%=conv(name)%>"></td>
    
    <th style="width:5%; padding:2px"><%=Label.get("common.desc")%></th>
    <td style="width:20%;"><input class="Textinput Width-100" type="text" name="desc" value="<%=conv(desc)%>"></td>
    
    <th style="width:5%; padding:2px">Email</th>
    <td style="width:20%;"><input class="Textinput Width-100" type="text" name="emailaddr" value="<%=conv(emailaddr)%>"></td>
    
    <th style="width:5%; padding:2px">SMS</th>
    <td style="width:20%;"><input class="Textinput Width-100" type="text" name="smsnum" value="<%=conv(smsnum)%>"></td>
    
<!-- 
    <td bgcolor="#efefef"><b><%=Label.get("notify.terminal")%></b></td>
    <td><input type="text" name="terminalid" value="<%=conv(terminalid)%>" style="width: 90%"></td>

    <td bgcolor="#efefef"><b><%=Label.get("notify.messenger")%></b></td>
    <td><input type="text" name="messengerid" value="<%=conv(messengerid)%>" style="width: 90%"></td>

    <td bgcolor="#efefef"><b>Device 1</b></td>
    <td><input type="text" name="dev1point" value="<%=conv(dev1point)%>" style="width: 90%"></td>

    <td bgcolor="#efefef"><b>Device 2</b></td>
    <td><input type="text" name="dev2point" value="<%=conv(dev2point)%>" style="width: 90%"></td>

    <td bgcolor="#efefef"><b>Device 3</b></td>
    <td><input type="text" name="dev3point" value="<%=conv(dev3point)%>" style="width: 90%"></td>
-->
    
    <td style="width:5%; text-align:center; padding:2px"><input class="Button" type="submit" value="<%=Label.get("common.btn.query")%>"></td>
</tr>
</table>
</form>
<form name="form1" action="action_notify.jsp" method="POST">
<input type="hidden" name="suburl" value="notifyreceiver">
<input type="hidden" name="cmd" value="">

<table class="Table Width-100 Margin-bottom-10">
<thead>
<tr>
    <th style="width:3%"><input class="Checkbox" type="checkbox" id="chkall" name="chkall" onclick="checkAll();"></th>  
    <th style="width:3%">#</th>  
    <th style="width:10%"><a href="javascript:orderby('getName');"><%=Label.get("common.name")%><%=printSortMark(orderby, orderdir, "getName")%></a></th>
    <th style="width:20%"><a href="javascript:orderby('getDesc');"><%=Label.get("common.desc")%><%=printSortMark(orderby, orderdir, "getDesc")%></a></th>
    <th><%=Label.get("notify.email")%></th>
    <th><%=Label.get("notify.sms")%></th>
<!--
    <td><%=Label.get("notify.terminal")%></td>
    <td><%=Label.get("notify.messenger")%></td>
    <td>Device 1</td>
    <td>Device 2</td>
    <td>Device 3</td>
-->
    <th><%=Label.get("common.lastmodifytime")%></th>
</tr>
</thead>
<tbody>
<%
    int i=0;
    for (JobNotifyReceiver receiver : jobNotifyReceivers) {
%>
<tr>
    <td style="padding:0px;"><input class="Checkbox" type="checkbox" id="chkreceiverid" name="chkreceiverid" value="<%=receiver.getId()%>"></td>
    <td><%=(++i) %></td>
    <td onMouseOver="this.style.cursor='pointer';this.style.backgroundColor='#AAAAFF';"
    	onMouseOut =<%=(i%2==0) ? "this.style.backgroundColor='#f7f7f7';" : "this.style.backgroundColor='#ffffff';" %>
    	onclick="javascript:openEditFormJobNotifyReceiver('<%=receiver.getId()%>');"><b><%=conv(receiver.getName())%></b></td>
    <td><%=conv(receiver.getDesc())%></td>
    <td class="Text-left"><%=receiver.isRecvByEmail()    ? "[<B><font color='#0000FF'>"+Label.get("notify.true")+"</font></B>]" : ""%> <%=conv(receiver.getEmailAddr())%></td>
    <td class="Text-left"><%=receiver.isRecvBySms()      ? "[<B><font color='#0000FF'>"+Label.get("notify.true")+"</font></B>]" : ""%> <%=conv(receiver.getSmsNum())%></td>
<!-- 
    <td align="left"><%=receiver.isRecvByTerminal() ? "[<B><font color='#0000FF'>"+Label.get("notify.true")+"</font></B>]" : ""%> <%=conv(receiver.getTerminalId())%></td>
    <td align="left"><%=receiver.isRecvByMessenger()? "[<B><font color='#0000FF'>"+Label.get("notify.true")+"</font></B>]" : ""%> <%=conv(receiver.getMessengerId())%></td>
    <td align="left"><%=receiver.isRecvByDev1()     ? "[<B><font color='#0000FF'>"+Label.get("notify.true")+"</font></B>]" : ""%> <%=conv(receiver.getDev1Point())%></td>
    <td align="left"><%=receiver.isRecvByDev2()     ? "[<B><font color='#0000FF'>"+Label.get("notify.true")+"</font></B>]" : ""%> <%=conv(receiver.getDev2Point())%></td>
    <td align="left"><%=receiver.isRecvByDev3()     ? "[<B><font color='#0000FF'>"+Label.get("notify.true")+"</font></B>]" : ""%> <%=conv(receiver.getDev3Point())%></td>
-->
    <td><%=toDatetimeString(DateUtil.getTimestamp(receiver.getLastModifyTime()), true)%></td>
</tr>
<%
    }
%>
</tbody>
</table>

<input class="Button" type="button" value="<%=Label.get("common.btn.add")%>" onclick="openEditFormJobNotifyReceiver('0');">
<input class="Button" type="button" value="<%=Label.get("common.btn.delete")%>" onclick="removeJobNotifyReceiver();">

</form>
