<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth            auth  = new AdminAuth(getUserId(request), getUserIp(request));
    
    String cmd       = request.getParameter("cmd");
    boolean editMode = "form_modify_user".equals(cmd);
    String orderby   = nvl( request.getParameter("orderby"),  "getId");
    String orderdir  = nvl( request.getParameter("orderdir"), "ASC");
    
    String userid    = nvl( request.getParameter("userid"));
    String username  = nvl( request.getParameter("username"));
    String userdesc  = nvl( request.getParameter("userdesc"));
    String userteam1 = nvl( request.getParameter("userteam1"));
    String userteam2 = nvl( request.getParameter("userteam2"));

    List<User> users = admin.getAllUsers();

    Map m = new HashMap();
    m.put("getId",    userid);
    m.put("getName",  username);
    m.put("getDesc",  userdesc);
    m.put("getTeam1", userteam1);
    m.put("getTeam2", userteam2);
    
    eliminateListByFilter(users, m);
    Collections.sort(users, getComparator(orderby, "ASC".equals(orderdir)));
%>

<script>
<% /* ================== User 관련 함수 ======================*/ %>
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
		var chk = document.form1.chkuserid;
	    var v = document.form1.chkall.checked;
	    
	    if (typeof chk =="undefined") return false;
	    
	    if (chk.length == null) { /* 하나일때 */
	        chk.checked = v ;
	    }else {
	        for (i=0; i<chk.length; i++ ) {
	            chk[i].checked = v ;
	        }
	    }
	}
	
    function openEditFormUser(userid) {
    	var left = (window.screen.width  / 2) - ((600 / 2) + 10);
        var top  = (window.screen.height / 2) - ((600 / 2) + 50);
        window.open("form_setting_user.jsp?userid="+userid, "user_"+userid, 'width=820,height=600,left='+left+',top='+top+',scrollbars=1').focus();
    }
    
    function openAddNewFormUser() {
    	var left = (window.screen.width  / 2) - ((600 / 2) + 10);
        var top  = (window.screen.height / 2) - ((600 / 2) + 50);
    	window.open("form_setting_user.jsp", "user_new", 'width=820,height=600,left='+left+',top='+top+',scrollbars=1').focus();
    }
    
    function removeUser(userid) {
		if (getCheckedCount(document.form1.chkuserid) == 0) {
			alert("Not checked");
			return;
		}

        if (confirm("<%=Label.get("common.remove.confirm.msg")%>")) {
            document.form1.cmd.value="delete_user";
            document.form1.submit();
        }
    }
    
    function orderby(orderbyCol) {
        if ('<%=orderby%>' == orderbyCol) {
            if ('<%=orderdir%>' == 'ASC') {
                document.form0.orderdir.value = 'DESC';
            }else {
            	document.form0.orderdir.value = 'ASC';
            }
        }else {
        	document.form0.orderdir.value = 'ASC';
        }
        document.form0.orderby.value = orderbyCol;
        document.form0.submit();
    }

</script>

<%-- <br>
<table border="0" width="100%">
<tr>
    <td colspan="100%" align="left"><font size="3"><b><img src="images/icon_user.png"/> <%=Label.get("user")%></b></font></td>
</tr>
</table> --%>

<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("user")%></div>
</div>

<form name="form0" action="view_setting.jsp" method="GET">
<input type="hidden" name="suburl" value="user">
<input type="hidden" name="orderby" value="<%=orderby%>">
<input type="hidden" name="orderdir" value="<%=orderdir%>">

<table class="Table Width-100 njf-table__typea Margin-bottom-10">
<tr>
    <th style="width:7%; padding:2px"><%=Label.get("user.id")%></th>
    <td style="width:15%;"><input class="Textinput Width-100" type="text" name="userid" value="<%=conv(userid)%>"></td>
    
    <th style="width:5%; padding:2px"><%=Label.get("user.name")%></th>
    <td style="width:15%;"><input class="Textinput Width-100" type="text" name="username" value="<%=conv(username)%>"></td>
    
    <th style="width:5%; padding:2px"><%=Label.get("user.desc")%></th>
    <td style="width:20%;"><input class="Textinput Width-100" type="text" name="userdesc" value="<%=conv(userdesc)%>"></td>
    
    <th style="width:4%; padding:2px"><%=Label.get("user.team1")%></th>
    <td style="width:10%;"><input class="Textinput Width-100" type="text" name="userteam1" value="<%=conv(userteam1)%>"></td>
    
    <th style="width:4%; padding:2px"><%=Label.get("user.team2")%></th>
    <td style="width:10%;"><input class="Textinput Width-100" type="text" name="userteam2" value="<%=conv(userteam2)%>"></td>
    
    <td style="width:5%; text-align:center; padding:2px"><input class="Button" type="submit" value="<%=Label.get("common.btn.query")%>"></td>
</tr>
</table>
</form>

<form name="form1" action="action_user.jsp" method="POST">
<input type="hidden" name="suburl" value="user">
<input type="hidden" name="cmd" value="">

<table class="Table Width-100 Margin-bottom-10">
<thead>
<tr>
    <th style="width:3%; padding:2px;"><input class="Checkbox" type="checkbox" name="chkall" onclick="checkAll();"></th>  
    <th style="width:3%; padding:2px;">#</th>
    <th style="width:6%; padding:2px;"><a href="javascript:orderby('getId');"><%=Label.get("user.id")%><%=printSortMark(orderby, orderdir, "getId")%></a></th>
    <th style="width:8%; padding:2px;"><a href="javascript:orderby('getName');"><%=Label.get("user.name")%><%=printSortMark(orderby, orderdir, "getName")%></a></th>
    <th style="width:15%; padding:2px;"><a href="javascript:orderby('getDesc');"><%=Label.get("user.desc")%><%=printSortMark(orderby, orderdir, "getDesc")%></a></th>
    <th style="width:6%; padding:2px;"><a href="javascript:orderby('getTeam1');"><%=Label.get("user.team1")%><%=printSortMark(orderby, orderdir, "getTeam1")%></a></th>
    <th style="width:10%; padding:2px;"><a href="javascript:orderby('getTeam2');"><%=Label.get("user.team2")%><%=printSortMark(orderby, orderdir, "getTeam2")%></a></th>
    <th style="width:5%; padding:2px;"><%=Label.get("user.isactive")%></th>
    <th style="width:5%; padding:2px;"><%=Label.get("user.isadmin")%></th>
    <th style="width:5%; padding:2px;"><%=Label.get("user.isoperator")%></th>
    <th style="width:7%; padding:2px;"><%=Label.get("user.jobgroup.foroper")%></th>
    <th style="width:9%; padding:2px;"><%=Label.get("user.jobid.foroper.pattern")%></th>
    <th style="width:7%; padding:2px;"><%=Label.get("user.jobgroup.forview")%></th>
    <th style="width:12%; padding:2px;"><%=Label.get("common.lastmodifytime")%></th>
</tr>
</thead>
<tbody>
<%
	int i=0;
    for (User user : users) {
%>
<tr>
    <td style="padding:0px"><input class="Checkbox" type="checkbox" name="chkuserid" value="<%=user.getId()%>"></td>
    <td style="padding:2px"><%=(++i) %></td>
    <td onMouseOver="this.style.cursor='pointer';this.style.backgroundColor='#AAAAFF';"
    	onMouseOut =<%=(i%2==0) ? "this.style.backgroundColor='#f7f7f7';" : "this.style.backgroundColor='#ffffff';" %> 
    	onclick="javascript:openEditFormUser('<%=nvl(user.getId())%>');"><%=user.getId()%></td>
    <td style="padding:2px"><b><%=conv(user.getName())%></b></td>
    <td style="padding:2px"><%=conv(user.getDesc())%></td>
    <td style="padding:2px"><%=conv(user.getTeam1())%></td>
    <td style="padding:2px"><%=conv(user.getTeam2())%></td>
    <td style="padding:2px"><%=user.isActive() ? Label.get("user.active") : Label.get("user.inactive") %></td>
    <td style="padding:2px"><b><%=user.isAdmin() ? "Ⅴ" : ""%></b></td>
    <td style="padding:2px"><b><%=user.isOperator() ? "Ⅴ" : ""%></b></td>
    <td style="padding:2px"><%=shortenRight(Util.toString(user.getAuthList("OPER_JOBGROUP")), 20)%></td>
    <td style="padding:2px"><%=shortenRight(user.getOperateJobIdExp(), 30)%></td>
    <td style="padding:2px"><%=shortenRight(Util.toString(user.getAuthList("VIEW_JOBGROUP")), 20)%></td>
    <td style="padding:2px"><%=toDatetimeString(DateUtil.getTimestamp(user.getLastModifyTime()), true)%></td>
</tr>
<%
    }
%>
</tbody>
</table>

<input class="Button" type="button" value="<%=Label.get("common.btn.add")%>" onclick="openAddNewFormUser();">
<input class="Button" type="button" value="<%=Label.get("common.btn.delete")%>" onclick="removeUser();">

</form>
