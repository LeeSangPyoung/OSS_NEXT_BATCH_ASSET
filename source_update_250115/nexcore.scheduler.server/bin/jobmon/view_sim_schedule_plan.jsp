<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<html>
<head>
<script src="./script/app/include-lib.js"></script>
<title><%=Label.get("job.dayschedule.simulation")%></title>
</head>
<body>
<center>
<%
	ControllerAdminLocal admin = getControllerAdmin();
    JobDefinition jobdef = new JobDefinition();
    BeanMaker.makeFromHttpParameter(request.getParameterMap(), jobdef);

    String yyyy = nvl(request.getParameter("yyyy"), Util.getCurrentYYYYMMDD().substring(0,4));
    String mm   = nvl(request.getParameter("mm"),   Util.getCurrentYYYYMMDD().substring(4,6));
    mm = mm.length() == 1 ? "0"+mm : mm;
    String yyyymm = yyyy+mm;

	Set planDayList = new HashSet();
	Map baseDateMap = new HashMap();
	try {
        planDayList = new HashSet(admin.getDayListWillBeActivated(jobdef, yyyymm));
        baseDateMap = admin.getBaseDateMonthlyMap(jobdef, yyyymm);
    }catch(Exception e) {
        throw new ServletException(Label.get("schedule.plan.range.exceed"), e);
    }
%>
	<div class="popup-content-wrap Margin-bottom-10">
		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Label.get("schedule.plan.title2", yyyy, mm)%></div>
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
						    <td colspan="2" style="padding:0;"><font color="#ff0000"><%=Label.get("common.sunday")%></font>&nbsp;(<%=Label.get("common.basedate")%>)</td>
						    <td colspan="2" style="padding:0;"><%=Label.get("common.monday")%></td>
						    <td colspan="2" style="padding:0;"><%=Label.get("common.tuesday")%></td>
						    <td colspan="2" style="padding:0;"><%=Label.get("common.wednesday")%></td>
						    <td colspan="2" style="padding:0;"><%=Label.get("common.thursday")%></td>
						    <td colspan="2" style="padding:0;"><%=Label.get("common.friday")%></td>
						    <td colspan="2" style="padding:0;"><font color="#0000ff"><%=Label.get("common.saturday")%></font></th>
						</tr>
						<tr style="border-top:initial; background:initial;">
				<%
				    Calendar cal = CalendarUtil.convYYYYMMDDToCalendar(yyyymm+"01");
				    int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				    
				    int colspanSize = (firstDayOfWeek - 1) * 2;
				    if (colspanSize > 0) {
				        out.println("<td colspan="+colspanSize+"/>");
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
	</div>

</center>
</body>
</html>
