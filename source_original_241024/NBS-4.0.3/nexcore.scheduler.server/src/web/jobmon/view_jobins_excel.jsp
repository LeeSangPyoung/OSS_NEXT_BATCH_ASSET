<%@ include file= "common_functions.jsp"
%><%@ page language="java" contentType="application/x-msdownload; charset=UTF-8" pageEncoding="euc-kr"
%><%!
	class JobInsWriter implements RowHandler {
		JspWriter          out;
		ControllerAdmin00  admin; 
		int                i;
	
		JobInsWriter(JspWriter _out, ControllerAdmin00 _admin) {
			this.out = _out;
			this.admin = _admin;
		}

		public void handleRow(Object row) {
			try {
				JobInstance jobins = (JobInstance)row;
	
				long[] progress = admin.getJobProgress(jobins.getLastJobExeId());
				
				out.println("<tr align=center>");
				out.println("<td>"+(++i)+"</td>");
				out.println("<td>"+nvl(jobins.getJobGroupId())+"</td>");
				out.println("<td>"+nvl(jobins.getJobInstanceId())+"</td>");
				out.println("<td>"+nvl(jobins.getJobId())+"</td>");
				out.println("<td><b>"+getAppCode(jobins.getJobId())+"</b></td>");
				out.println("<td align=left>"+nvl(jobins.getDescription())+"</td>");
				out.println("<td>"+jobins.getEndOkCount()+"</td>");
				out.println("<td>"+jobins.getRunCount()+"</td>");
				out.println("<td><b><font color='"+getStateColor(jobins.getJobState())+"'>"+nvl(jobins.getJobStateText())+"</font></b></td>");
				out.println("<td>"+(jobins.isLocked()?"Locked":"")+"</td>");
				out.println("<td>"+nvl(jobins.getTimeFrom())+"</td>");
				out.println("<td>"+nvl(jobins.getTimeUntil())+"</td>");
				out.println("<td>"+("Y".equals(jobins.getRepeatYN()) ? ("EXACT".equals(jobins.getRepeatIntvalGb()) ? jobins.getRepeatExactExp() : jobins.getRepeatIntval()) : "-")+"</td>");
				out.println("<td>"+("Y".equals(jobins.getConfirmNeedYN()) ? (Util.isBlank(jobins.getConfirmed()) ? "<font color=red>-/<b>Y</b></font>" : "<font color=blue><b>£°</b>/<b>Y</b></font>") : "")+"</td>");
				out.println("<td>"+nvl(getJobTypeText(jobins.getJobType()))+"</td>");
				out.println("<td>"+nvl(jobins.getAgentNode())+"</td>");
				out.println("<td>"+nvl(jobins.getComponentName())+"</td>");
				out.println("<td>"+toDatetimeString(nvl(jobins.getLastStartTime()), false)+"</td>");
				out.println("<td>"+toDatetimeString(nvl(jobins.getLastEndTime()), false)+"</td>");
				
				if (progress != null) {
					out.println("<td>"+progress[1]+"</td>");
					out.println("<td>"+progress[0]+"</td>");
					out.println("<td>"+(progress[0] == 0 ? "-" : Math.ceil(progress[1] * 100 / progress[0])+"%") + "</td>");
				}else {
					out.println("<td></td>");
					out.println("<td></td>");
					out.println("<td></td>");
				}
				
				out.println("<td>"+toRunTimeString(jobins.getJobState(), jobins.getLastStartTime(), jobins.getLastEndTime(), false)+"</td>");
				out.println("<td><b>"+nvl(jobins.getProcDate())+"</b></td>");
				out.println("<td>"+nvl(jobins.getBaseDate())+"</td>");
				out.println("<td>"+toDatetimeString(DateUtil.getTimestamp(jobins.getLastModifyTime()), false)+"</td>");
				out.println("</tr>");
			}catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
%><%
    if (!checkLogin(request, response)) return; 

	long current = System.currentTimeMillis();
	String currentDateTime = formatDatetime(current, "yyyyMMdd_HHmmss");
	
	response.setContentType("application/vnd.ms-excel; charset=euc-kr");
	response.setHeader("Content-Disposition", "attachment;filename=jobins-" + currentDateTime + ".xls");
%>
<html>
<head>
	<meta http-equiv=Content-Type content="text/html">
	<meta name=ProgId content=Excel.Sheet>
</head>
<body>
<table border=0>
<tr>
	<td colspan="24" align=center><font size="4"><b><%=Label.get("jobins")%> (<%=toDatetimeString(current, false)%>)</b></font></td>
</tr>
</table>
<br>
<table border=1>
<tr align="center" bgcolor="#DDDDFF">
<td>#</td>
<td><%=Label.get("job.jobgroup")%></td>
<td><%=Label.get("job.jobinsid")%></td>
<td><%=Label.get("job.jobid")%></td>
<td><%=Label.get("common.search.gubun")%></td>
<td><%=Label.get("job.desc")%></td>
<td><%=Label.get("jobins.endokcount")%></td>
<td><%=Label.get("jobins.runcount")%></td>
<td><%=Label.get("jobins.state")%></td>
<td><%=Label.get("jobins.lock")%></td>
<td><%=Label.get("job.time.from")%></td>
<td><%=Label.get("job.time.until")%></td>
<td><%=Label.get("job.repeat")%></td>
<td><%=Label.get("job.confirm")%></td>
<td><%=Label.get("job.jobtype")%></td>
<td><%=Label.get("job.agent")%></td>
<td><%=Label.get("job.component")%></td>
<td><%=Label.get("jobins.last.starttime")%></td>
<td><%=Label.get("jobins.last.endtime")%></td>
<td colspan=3><%=Label.get("common.progress")%></td>
<td><%=Label.get("jobins.run.elaptime")%></td>
<td><%=Label.get("common.procdate") + "(PROC)"%></td>
<td><%=Label.get("common.basedate") + "(BASE)"%></td>
<td><%=Label.get("job.lastmodifytime.short")%></td>
</tr>
<%
    JobInsWriter rowHandler = new JobInsWriter(out, getControllerAdmin());
	request.setAttribute("rowHandler", rowHandler);
%>
<jsp:include page="common_query_jobins.jsp"/>
</table>
</body>
</html>