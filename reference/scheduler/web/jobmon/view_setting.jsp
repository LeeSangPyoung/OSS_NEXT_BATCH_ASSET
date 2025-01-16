<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<jsp:include page="top.jsp" flush="true"/>
<%
    String suburl     = Util.nvl(request.getParameter("suburl"), "jobgroup");
    String includeUrl = "view_setting_"+suburl+".jsp";
%>

<center>
<div class="content-wrap">
	<jsp:include page="<%=includeUrl%>"/>
</div>
</center>
</div>
</div>
<jsp:include page="bottom.jsp" flush="true"/>	
</body>
</html>
