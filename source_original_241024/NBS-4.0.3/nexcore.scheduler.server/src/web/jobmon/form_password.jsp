<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@ include file= "common.jsp" %>

<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<script src="./script/app/include-lib.js"></script>
<title><%=Label.get("form_password.change.password")%></title>
<jsp:include page="display_msg.jsp" flush="true"/>
</head>
<body onload="displayMsg();">
<center>

<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=Label.get("form_password.change.password")%>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>


<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
<form name="form1" action="action_user.jsp" method="POST">
<input type="hidden" name="cmd" value="modify_password">
<!-- <table border="1" style="border-collapse:collapse" bordercolor="#000000" cellpadding="2" width="100%" class=tabletitle> -->
<table class="Table njf-table__typea Margin-bottom-10" >
    <colgroup>
        <col width="22%">
        <col>
    </colgroup>
    <tr>
        <th><%=Label.get("form_password.old.password")%></th>
        <td><input class="Textinput Width-100" type="password" name="old_password"></td>
    </tr>
    <tr>
        <th><%=Label.get("form_password.new.password")%></th>
        <td><input class="Textinput Width-100" type="password" name="new_password"></td>
    </tr>
    <tr>
        <th><%=Label.get("form_password.new.password")%> (Confirm)</th>
        <td><input class="Textinput Width-100" type="password" name="new_password2"></td>
    </tr>
</table>

<table class="Width-100">
	<tr>
		<td class="Text-center">
			<input class="Button" type="submit" value="<%=Label.get("common.btn.edit")%>">&nbsp;&nbsp;
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close();">
		</td>
	</tr>
</table>

</form>
</div>
</center>
</body>
</html>


