<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<html>
<head>
<script src="./script/app/include-lib.js"></script>
<title><%=Label.get("job.dayschedule.simulation.view")%></title>
</head>
<body>
<center>
<%
	ControllerAdminLocal admin = getControllerAdmin();
	String jobId  = request.getParameter("jobid");
	String yyyy   = nvl(request.getParameter("yyyy"), Util.getCurrentYYYYMMDD().substring(0,4));
	String mm     = nvl(request.getParameter("mm"),   Util.getCurrentYYYYMMDD().substring(4,6));
	
	if (mm.length() == 1) {
	    mm = "0"+mm;
	}
	
	Set planDayList = new HashSet();
	Map baseDateMap = new HashMap();
	try {
    	if (!Util.isBlank(jobId)) {
            planDayList = new HashSet(admin.getDayListWillBeActivated(jobId, yyyy+mm));
            baseDateMap = admin.getBaseDateMonthlyMap(jobId, yyyy+mm);
        }
    }catch(Exception e) {
        throw new ServletException(Label.get("schedule.plan.range.exceed"));
    }
%>
	<div class="popup-content-wrap Margin-bottom-10">
		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Util.isBlank(jobId)?"": Label.get("schedule.plan.title1", jobId, yyyy, mm)%></div>
		</div>
		<form action="view_schedule_plan.jsp" method="get">
		
		<div class="Margin-top-5 Text-left">
			<input type="hidden" name="jobid" value="<%=nvl(jobId)%>">
			<select class="Select Margin-right-5" name="yyyy">
			<%
				int currYYYY = Util.toInt(Util.getCurrentYYYYMMDD().substring(0,4));
				out.println(printSelectOption(String.valueOf(currYYYY-1), yyyy));
				out.println(printSelectOption(String.valueOf(currYYYY),   yyyy));
				out.println(printSelectOption(String.valueOf(currYYYY+1), yyyy));
			%>
			</select><label style="font-size:13px;"><%=Label.get("common.year")%><label> 
		
			<input class="Button Margin-left-5 Margin-right-20" type="submit" name="submit" value="<%=Label.get("common.btn.query")%>">
		<%
		    for (int i=1; i<=12; i++) {
		%>
		    <a href="view_schedule_plan.jsp?jobid=<%=jobId%>&yyyy=<%=yyyy%>&mm=<%=i%>" style="font-size:13px;"> [<%=i%>]</a>&nbsp;&nbsp;
		<%
		    }
		%>
		</div>
		
		<table class="Width-100 Margin-bottom-10">
			<tr style="vertical-align:top;">
				<td>
					<table class="Table non-border Width-100" style="height:140px">		
						<colgroup>
							<col style="width:2%"/>
							<col style="width:11%"/>
							<col style="width:2%"/>
							<col style="width:13%"/>
							<col style="width:2%"/>
							<col style="width:13%"/>
							<col style="width:2%"/>
							<col style="width:13%"/>
							<col style="width:2%"/>
							<col style="width:13%"/>
							<col style="width:2%"/>
							<col style="width:13%"/>
							<col style="width:2%"/>
							<col style="width:11%"/>
						</colgroup>	
							<tr style="height:20px; border-bottom:1px solid #e2e4e7; background:#f7f7f7;">
							    <td colspan=2 style="padding:0;"><font color="#ff0000"><%=Label.get("common.sunday")%></font>&nbsp;(<%=Label.get("common.basedate")%>)</td>
							    <td colspan=2 style="padding:0;"><%=Label.get("common.monday")%></td>
							    <td colspan=2 style="padding:0;"><%=Label.get("common.tuesday")%></td>
							    <td colspan=2 style="padding:0;"><%=Label.get("common.wednesday")%></td>
							    <td colspan=2 style="padding:0;"><%=Label.get("common.thursday")%></td>
							    <td colspan=2 style="padding:0;"><%=Label.get("common.friday")%></td>
							    <td colspan=2 style="padding:0;"><font color="#0000ff"><%=Label.get("common.saturday")%></font></th>
							</tr>
							<tr style="border-top:initial; background:initial;">
							<%
							    Calendar cal = CalendarUtil.convYYYYMMDDToCalendar(yyyy+mm+"01");
							    int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
							    
							    int colspanSize = (firstDayOfWeek - 1) * 2;
							    if (colspanSize > 0) {
							        out.println("<td colspan="+colspanSize+" style='padding:0;'/>");
							    }
							    int lastDayOfMonth = cal.getMaximum(Calendar.DAY_OF_MONTH);
							    for (int i=1; ; i++) {
							        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
							        int date = CalendarUtil.convCalendarToYYYYMMDD(cal);
							        boolean runday = planDayList.contains(String.valueOf(date));
							
							        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
							            out.println("</tr>");
							            out.println("	<tr style='border-top:initial; background:initial;'>");
							        }
							        if (runday) {
							            out.println("<td style='padding:0; background-color:#88ff55; text-align:right'><b>"+dayOfMonth+"</b></td><td style='padding:0; background-color:#88ff55;'>("+baseDateMap.get(date)+")</td>");
							        }else {
							            out.println("<td style='padding:0; text-align:right'><b>"+dayOfMonth+"</b></td><td style='padding:0;'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
							        }
							        cal.add(Calendar.DATE, 1);
							        if (cal.get(Calendar.DAY_OF_MONTH)==1) {// ¿ù¸».
							            break;
							        }
							        
							    }
							%>
							</tr>
					</table>
				</td>
			</tr>
		</table>					
		<input type="button" class="Button" value="<%=Label.get("common.btn.close")%>" onclick="window.close();">
		</form>
	</div>
</center>
</body>
</html>
