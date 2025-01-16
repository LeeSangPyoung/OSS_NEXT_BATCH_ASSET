<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<html>
<head>
<script src="./script/app/include-lib.js"></script>
<title><%=Label.get("job.repeat.timeplan.win")%></title>
</head>
<body>
<center>

<div class="popup-content-wrap Margin-bottom-10">

<div class="popup-content-title__wrap">
	<div class="content-title"><%=Label.get("job.repeat.timeplan.win")%></div>
</div>


<%
	ControllerAdminLocal admin = getControllerAdmin();
	List<String> exactTimePlanList = null;
	
	String mode = request.getParameter("mode");
	
	if ("view".equals(mode)) { /* view mode */
		String jobId         = request.getParameter("jobid");
		String jobInstanceId = request.getParameter("jobinstanceid");

		if (jobId != null) {
			exactTimePlanList = admin.getTimePlanForExactRepeat(admin.getJobDefinition(jobId));
		}else if (jobInstanceId != null) {
			exactTimePlanList = admin.getTimePlanForExactRepeat(admin.getJobInstanceSimple(jobInstanceId));
		}
	} else { /* form mode */
		JobDefinition jobdef = new JobDefinition();
		BeanMaker.makeFromHttpParameter(request.getParameterMap(), jobdef);
	
		exactTimePlanList = admin.getTimePlanForExactRepeat(jobdef);
	}
	
	/* group by HHMM */
	Map<String, List> hhmmssListMap = new HashMap();
	for (String hhmmss : exactTimePlanList) {
		String hhmm = hhmmss.substring(0,4);
		
		List hhmmssList = hhmmssListMap.get(hhmm);
		if (hhmmssList == null) {
			hhmmssList = new LinkedList();
			hhmmssListMap.put(hhmm, hhmmssList);
		}
		hhmmssList.add(hhmmss);
	}
	
%>
<%-- <br>
<font size="3"><b><font color=blue><%=Label.get("job.repeat.timeplan.win")%></font></b></font>
<br><br> --%>
<table class="Table njf-table__typea Width-100 Margin-bottom-10">
<tr>
	<th style="width:3%; font-size:10px; padding:1px; height:20px;">H,M</th>
<%
	for (int i=0; i<60; i++) { /* minute title */
%>
	<th style="font-size:10px; padding:1px; height:20px;"><%=i<10 ? "0"+i : ""+i%></th>
	
<%	} %>
</tr>

<%	
	for (int i=0; i<=23; i++) { /* hour */
%>
<tr>
	<th style="font-size:10px; padding:1px; height:15px;"><%=i%></th>
<%
		for (int j=0; j<=59; j++) {
			String hhmm       = (i<10 ? "0"+i : ""+i) + (j<10 ? "0"+j : ""+j);
			List<String> hhmmssList = hhmmssListMap.get(hhmm);
			
			if (hhmmssList == null) {
				out.println("<td style=\"font-size:10px; padding:1px; height:15px;\"></td>");
			} else {
				out.print("<td bgcolor='#66FF66'>");
				out.print("<a title=\"");
				for (String hhmmss : hhmmssList) {
					out.print("[");
					out.print(hhmmss.substring(0,2)+":");
					out.print(hhmmss.substring(2,4)+":");
					out.print(hhmmss.substring(4,6));
					out.print("]");
				}
				
				out.print("\">"+hhmmssList.size());
				out.println("</td>");
			}
%>
<%		} %>
</tr>
<%	} %>

</tr>
</table>
<br>
<%
	for (String hhmmss : exactTimePlanList) {
		out.print(hhmmss.substring(0,2)+":");
		out.print(hhmmss.substring(2,4)+":");
		out.print(hhmmss.substring(4,6));
		out.println("<br>");
	
	}
%>

<table class="Width-100">
	<tr>
		<td class="Text-center">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()">
		</td>
	</tr>
</table>

</div>
</center>
</body>
</html>

