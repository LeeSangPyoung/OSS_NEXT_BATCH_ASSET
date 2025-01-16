<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    String jspUrlQueryString = request.getQueryString();
    String returnUrl      = null; // encode µÈ³ð.   GET ¿ë 
    String returnUrlPlain = null; // encode ¾ÈµÈ³ð. POST ¿ë 
    if (request.getQueryString()==null) {
        returnUrl      = request.getRequestURI();
        returnUrlPlain = request.getRequestURI();
    }else {
        returnUrl      = request.getRequestURI()+"?"+URLEncoder.encode(request.getQueryString());
        returnUrlPlain = request.getRequestURI()+"?"+request.getQueryString();
    }
%>
<html>
<head>
<title><%=Label.get("viewfilter")%></title>
<script src="./script/app/include-lib.js"></script>
<script>
    function openViewFilterFormWin(vfid) {
        window.open("form_viewfilter.jsp?id="+vfid, '', 'width=900,height=650,scrollbars=1').focus();
    }
    
    function removeViewFilter(vfid, vfname) {
        if (confirm("["+vfname+"] <%=Label.get("common.remove.confirm.msg")%>")) {
            document.form1.cmd.value="remove_viewfilter";
            document.form1.id.value=vfid;
            document.form1.submit();
            opener.location.reload();
        }
    }
    
    function addNewViewFilter() {
    	if (checkSubmit()) {
            if (confirm("<%=Label.get("common.add.confirm.msg")%>")) {
                document.form1.cmd.value="add";
   	            document.form1.submit();
   	         	opener.location.reload();
            }
        }
    }
    
    function checkSubmit() {
    	if (document.form1.name.value == '') {
    		alert('<%=Label.get("common.required.field.missing", "Name")%>');
    		return false;
    	}
    	if (document.form1.team.value == '') {
            alert('<%=Label.get("common.required.field.missing", "Team")%>');
            return false;
    	}
    	if (document.form1.owner.value == '') {
            alert('<%=Label.get("common.required.field.missing", "Owner")%>');
            return false;
    	}
    	return true;
    }

</script>
</head>
<body>
<center>
	<div class="header-wrap">
		<div class="header">
			<div class="header-title">
				<%=Label.get("viewfilter")%>
			</div>
			<div class="header-close-button">
		    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
			</div>
		</div>
	</div>
	
	<div class="popup-content-wrap">
		<br><br>
		<%=Label.get("viewfilter.help")%>
		<br>
		<br>
		<form name="form1" action="action_viewfilter.jsp" method="post" >
		<input type="hidden" name="returnurl" value="<%=returnUrlPlain%>">
		<input type="hidden" name="cmd"       value="">
		<input type="hidden" name="id"      value="">
		
		<table class="Table">
		<colgroup>
			<col style="width:18%"/>
			<col style="width:10%"/>
			<col style="width:10%"/>
			<col style="width:20%"/>
			<col style="width:7%"/>
			<col style="width:10%"/>
			<col style="width:8%"/>
			<col style="width:8%"/>
		</colgroup>
		<thead>
			<tr>
			    <th><%=Label.get("viewfilter.name")%></th>
			    <th><%=Label.get("viewfilter.team")%></th>
			    <th><%=Label.get("viewfilter.owner")%></th>
			    <th><%=Label.get("viewfilter.desc")%></th>
			    <th><%=Label.get("viewfilter.jobcount")%></th>
			    <th><%=Label.get("common.createtime")%></th>
			    <th><%=Label.get("common.edit")%></th>
			    <th><%=Label.get("common.delete")%></th>
			</tr>
		</thead>
		<tbody>
			<tr>
			<%
			    ControllerAdminLocal admin = getControllerAdmin();
			    List<ViewFilter> viewfilters = admin.getViewFiltersByQuery("", "ORDER BY VF_TEAM, VF_OWNER, VF_DESC");
			    int i=0;
			    for (ViewFilter vf : viewfilters) {
			%>
			<tr>
			    <td><%=conv(vf.getName())%></td>
			    <td><%=conv(vf.getTeam())%></td>
			    <td><%=conv(vf.getOwner())%></td>
			    <td><%=conv(vf.getDescription())%></td>
			    <td><%=vf.getJobCount()%></td>
			    <td><%=toDatetimeString(DateUtil.getTimestamp(vf.getLastModifyTime()), true)%></td>
			    <td><input type="button" class="Button" value="<%=Label.get("common.btn.edit")  %>" onclick="openViewFilterFormWin('<%=vf.getId()%>');"></td>
			    <td><input type="button" class="Button" value="<%=Label.get("common.btn.delete")%>" onclick="removeViewFilter('<%=vf.getId()%>', '<%=vf.getName()%>');"></td>
			</tr>
			<%
			    }
			%>
			<tr>
			    <td><input type="text" class="Textinput Width-100" name="name" value="" size="20"></td>
			    <td><input type="text" class="Textinput Width-100" name="team" value="" size="8"></td>
			    <td><input type="text" class="Textinput Width-100" name="owner" value="" size="7"></td>
			    <td><input type="text" class="Textinput Width-100" name="description" value="" size="30"></td>
			    <td/>
			    <td/>
			    <td colspan="2"><input type="button" class="Button" value="<%=Label.get("common.btn.add")%>" onclick="addNewViewFilter();"></td>
			</tr>
		</tbody>
		</table>
		<br><br>
		</form>
		<br>
		<input type="button" class="Button" value="<%=Label.get("common.btn.close")%>" onclick="window.close();" style="width:80px; height:35px">
		<br><br>
		</div>
	</div>
</center>
</body>
</html>




