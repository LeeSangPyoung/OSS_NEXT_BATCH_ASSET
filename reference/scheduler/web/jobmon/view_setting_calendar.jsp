<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
	String yyyy = Util.nvlBlank(request.getParameter("yyyy"), Util.getCurrentYYYYMMDD().substring(0,4));
	String calendarId = nvl(request.getParameter("calendarId"), "1");

	ControllerAdminLocal admin = getControllerAdmin();
	Map calendarMap = admin.getCalendarList();
	
	boolean isEmptyCalendar = calendarMap == null || calendarMap.size() < 1;

	List<Integer> calendarDayList = null;
	if(!isEmptyCalendar) {
		calendarDayList = admin.getCalendarDayList(calendarId);
	}
%>
<script>
$a.page(function() {
    // 초기화 함수
    this.init = function(id, param) {
    	$(".Table").css({'table-layout':'fixed'});
    }
});
    function reloadCalendar() {
        if (confirm("<%=Label.get("calendar.confirm.reload")%>")) {
            document.form1.action="action_setting.jsp";
            document.form1.cmd.value="reload_calendar";
            document.form1.submit();
        }
    }
</script>

<!-- <br>
<table border="0" width="100%">
	<tr>
	    <td colspan="100%" align="left"><font size=3><b><img src="images/icon_setting.png"/> Calendar</b></font></td>
	</tr>
</table> -->

<div class="content-title__wrap">
	<div class="content-title">Calendar</div>
</div>

<form name="form1" action="view_setting.jsp" method="get">
<input type="hidden" name="suburl" value="calendar">
<input type="hidden" name="cmd"    value="">
<%
	if(isEmptyCalendar) {
%>
	<table class="Width-100 Margin-bottom-5">
		<tr>
		    <td class="Text-left"><font size='2'><b><%= MSG.get("main.calendar.nodefine")%></b></font></td>
		</tr>
	</table>
<%
	}
	else {
%>
	<table class="Width-100 Margin-bottom-5">
		<tr>
		    <td class="Text-right">
                <input class="Button Margin-right-5" type="button" value="<%=Label.get("common.btn.reload")%>" onclick="reloadCalendar();" >
				<select class="Select Width-10 Margin-right-10" name="calendarId">
<%
					Iterator calIter = calendarMap.entrySet().iterator();
				    while(calIter.hasNext()) {
				    	Map.Entry entry = (Map.Entry)calIter.next();
				    	out.println(printSelectOption((String)entry.getKey(), "["+entry.getKey()+"] "+(String)entry.getValue(), calendarId));
				    }
%>
				</select>
				<select class="Select Width-10 Margin-right-5" name="yyyy">
<%
	int currYYYY = Util.toInt(Util.getCurrentYYYYMMDD().substring(0,4));
	out.println(printSelectOption(String.valueOf(currYYYY-1), yyyy));
	out.println(printSelectOption(String.valueOf(currYYYY),   yyyy));
	out.println(printSelectOption(String.valueOf(currYYYY+1), yyyy));
%>
				</select><label class="Font Style Font-small"><%=Label.get("common.year")%></label>
				<input class="Button" type="submit" value="<%=Label.get("common.btn.query")%>">
		    </td>
		</tr>
	</table>
<%
		out.println("<table class='Width-100'>");
		for(int cnt=0; cnt<12; cnt++) {
			if( cnt%3 == 0 ) out.println("<tr style='vertical-align:top;'>");
			out.println("<td>");
			out.println("<br>");
			out.println("<font size='2'><b><font color=blue>"+String.valueOf(cnt+1)+"-Month"+"</font></b></font>");
			out.println("<table class='Table non-border Width-100' style='height:140px'>");
			out.println("	<tr style='height:20px; border-bottom:1px solid #e2e4e7; background:#f7f7f7'>");
			out.println("		<td class='Text-right' style='padding:0px'><font color='#ff0000'>"+Label.get("common.sunday")+"</font></td>");
			out.println("		<td colspan='2' class='Text-right' style='padding:0px'>"+Label.get("common.monday")+"</td>");
			out.println("		<td colspan='2' class='Text-right' style='padding:0px'>"+Label.get("common.tuesday")+"</td>");
			out.println("		<td colspan='2' class='Text-right' style='padding:0px'>"+Label.get("common.wednesday")+"</td>");
			out.println("		<td colspan='2' class='Text-right' style='padding:0px'>"+Label.get("common.thursday")+"</td>");
			out.println("		<td colspan='2' class='Text-right' style='padding:0px'>"+Label.get("common.friday")+"</td>");
			out.println("		<td colspan='3' style='padding:0px'><font color='#0000ff'>"+Label.get("common.saturday")+"</font></td>");
			out.println("	</tr>");
			out.println("	<tr style='border-top:initial; background:initial;'>");
			
			int cntLine = 0;
			
			String mm = String.valueOf(cnt+1);
			mm = mm.length() == 1 ? "0"+mm : mm;
		    Calendar cal = CalendarUtil.convYYYYMMDDToCalendar(yyyy+mm+"01");
		    int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		    
		    int colspanSize = (firstDayOfWeek - 1) * 2;
		    if (colspanSize > 0) {
		        out.println("		<td colspan="+colspanSize+"/>");	        
		    }
	
		    int lastDayOfMonth = cal.getMaximum(Calendar.DAY_OF_MONTH);
		    for (int i=1; ; i++) {
		        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		        int date = CalendarUtil.convCalendarToYYYYMMDD(cal);
		        boolean runday = calendarDayList == null ? false : calendarDayList.contains(Integer.valueOf(date));
	
		        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
		            out.println("	</tr>");
		            out.println("	<tr style='border-top:initial; background:initial;'>");
		            cntLine++;
		        }
		        if (runday) {
		            out.println("		<td class='Text-right' style='padding:0px' bgcolor=#88ff55><b>"+dayOfMonth+"</b></td><td bgcolor=#88ff55 class='Text-left' style='padding:0px'>&nbsp;</td>");
		        }else {
		            out.println("		<td class='Text-right' style='padding:0px'><b>"+dayOfMonth+"</b></td><td>&nbsp;&nbsp;</td>");
		        }
		        cal.add(Calendar.DATE, 1);
		        if (cal.get(Calendar.DAY_OF_MONTH)==1) {// 월말.
		            break;
		        }
		    }
	
		    if(cntLine < 5) {
	            out.println("	</tr>");
	            out.println("	<tr style='border-top:initial; background:initial;'>");
		    	out.println("		<td class='Text-right' style='padding:0px'></td><td>&nbsp;&nbsp;</td>");
		    }
			out.println("	</tr>");
			out.println("</table>");	
		
			out.println("<td>");
			if( cnt%3 == 2 ) out.println("</tr>");
			else out.println("&nbsp;&nbsp;&nbsp;"); 
		}
		out.println("</table>");
	}
%>

</form>
