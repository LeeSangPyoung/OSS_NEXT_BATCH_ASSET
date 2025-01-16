<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    int srcCnt = Integer.parseInt(request.getParameter("lastParamIndex"));
    Map<String, String> srcMap = new LinkedHashMap();
    String agentId = request.getParameter("agentNode");
	srcMap.put("JOB_ID", request.getParameter("jobId")); /* JOB_ID is internal param */
    ControllerAdminLocal admin = getControllerAdmin();
    
    for (int i=1; i<=srcCnt; i++) {
        String paramName = request.getParameter("paramName_"+i);
        if (Util.isBlank(paramName)) {
            continue; // ¾ø´Â ÀÎµ¦½º´Â ±×³É pass
        }
        if (srcMap.containsKey(paramName)){
            continue; // Áßº¹ ÀÎµ¦½º´Â ±×³É pass
        }
        String paramValue = request.getParameter("paramValue_"+i);            
        srcMap.put(paramName, paramValue);
    }
    
    String baseDateCalId = request.getParameter("baseDateCalId");
    String baseDateLogic = request.getParameter("baseDateLogic");
    
    JobDefinition jobdef = new JobDefinition();
    BeanMaker.makeFromHttpParameter(request.getParameterMap(), jobdef);
    
    Map<String, String> resultMap = admin.evaluateParameters(agentId, jobdef, srcMap, baseDateCalId, baseDateLogic);    
%>
<html>

<head>
<script src="./script/app/include-lib.js"></script>
<title><%=Label.get("form_jobdef.param.simulation")%></title>
</head>
<body>
<center>
	<div class="header-wrap">
		<div class="header">
			<div class="header-title">
				<%=Label.get("job.param")%>
			</div>
			<div class="header-close-button">
		    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
			</div>
		</div>
	</div>

	<div id="container2" class="popup-content-wrap">
		<form name="form1">
		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Label.get("job.param")%></div>
		</div>
		<table class="Table">
	    <colgroup>
	        <col width="30%"/>
	        <col width="70%"/>
	    </colgroup>
	    <thead>
	    	<tr>
                <th><%=Label.get("job.param.name")%></td>
                <th><%=Label.get("job.param.value")%></td>
            </tr>
	    </thead>
	    <tbody>
    	<%
			for (String myParamKey : srcMap.keySet()) {
				if ("JOB_ID".equals(myParamKey)) {
					continue;
				}
		%>	
			<tr>
			    <td><%=nvl(myParamKey)%></td>
			    <td><%=nvl(resultMap.get(myParamKey))%></td>
			</tr>
		<%
				resultMap.remove(myParamKey);
		    }
		%>
		</tbody>
		</table>
		</br>
		
		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Label.get("internal.param")%>(<%=Label.get("gparam")%>)</div>
		</div>
		<table class="Table">
	    <colgroup>
	        <col width="30%"/>
	        <col width="70%"/>
	    </colgroup>
	     <thead>
	    	<tr>
                <th><%=Label.get("job.param.name")%></td>
                <th><%=Label.get("job.param.value")%></td>
            </tr>
	    </thead>
	    <tbody>
	    <%
		    for (String paramKey : resultMap.keySet()) {
		%>
			<tr>
				<td><%=nvl(paramKey)%></td>
				<td><%=nvl(resultMap.get(paramKey))%></td>
			</tr>
		<%
		    }
		%>
	    </tbody>
		</table>
		</form>
		<br/>
		<input type="button" class="Button" value="<%=Label.get("common.btn.close")%>" onclick="window.close();">
		<br/><br/>
	</div>
</center>
</body>
</html>
