<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	String printTrBgcolor(String jobState, boolean colorFlip) {
	    if (JobInstance.JOB_STATE_RUNNING.equals(jobState) || JobInstance.JOB_STATE_SUSPENDED.equals(jobState)) {
	        return "bgcolor='#FFFFBB'";
	    }else {
	        return printTrFlip(colorFlip);
	    }
	}
	
    String printTrHandOutBgcolor(String jobState, boolean colorFlip) {
	    if (JobInstance.JOB_STATE_RUNNING.equals(jobState) || JobInstance.JOB_STATE_SUSPENDED.equals(jobState)) {
	        return "'#FFFFBB'";
	    }else {
	        return printFlipBgcolor(colorFlip);
	    }
    }
    
    String printTrHandOverBgcolor(String jobState, boolean colorFlip) {
	    if (JobInstance.JOB_STATE_RUNNING.equals(jobState) || JobInstance.JOB_STATE_SUSPENDED.equals(jobState)) {
	        return "'#FFFF77'";
	    }else {
	        return "'#AAAAFF'";
	    }
    }
%><jsp:include page="common_query_jobins.jsp"/><%
    String returnUrl      = null; // encode 된놈.   GET 용
    String returnUrlPlain = null; // encode 안된놈. POST 용
    if (request.getQueryString()==null) {
        returnUrl = request.getRequestURI();
        returnUrlPlain = request.getRequestURI();
    }else {
        returnUrl = request.getRequestURI()+"?"+URLEncoder.encode(request.getQueryString());
        returnUrlPlain = request.getRequestURI()+"?"+request.getQueryString();
    }

    // 조회 조건
    String lastretcode    = request.getParameter("lastretcode");
    String jobstate       = request.getParameter("jobstate");
    String procdate       = request.getParameter("procdate");
    String jobinstanceid  = nvl(request.getParameter("jobinstanceid")).trim();
    String jobgroup       = nvl(request.getParameter("jobgroup")).trim();
    String jobdesc        = nvl(request.getParameter("jobdesc")).trim();
    String jobtype        = nvl(request.getParameter("jobtype")).trim();
    String agentid        = nvl(request.getParameter("agentid")).trim();
    String prejobid       = nvl(request.getParameter("prejobid")).trim();
    String triggerjobid   = nvl(request.getParameter("triggerjobid")).trim();
    String searchdate     = request.getParameter("searchdate");
    String owner          = request.getParameter("owner");
	boolean autoReload    = Util.toBoolean(request.getParameter("autoreload"), false);
    String viewfilter     = request.getParameter("viewfilter");
    String jobfilter      = (jobfilter=nvl(request.getParameter("jobfilter"))).length() == 0 ? null : jobfilter;
    String orderby        = nvl(request.getParameter("orderby"), "LAST_MODIFY_TIME");
    String orderdir       = nvl(request.getParameter("orderdir"), "DESC"); // ASC, DESC
    int currPageNo        = Util.toInt(request.getParameter("currpageno"), 1);
    boolean doQuery       = true;

    searchdate = Util.isBlank(searchdate) && Util.isBlank(procdate) && Util.isBlank(jobinstanceid) ? Util.getCurrentYYYYMMDD() : searchdate;

    if (Util.isBlank(viewfilter) && Util.isBlank(jobinstanceid) && Util.isBlank(jobgroup) && Util.isBlank(jobtype) && 
        Util.isBlank(agentid) && Util.isBlank(owner) && Util.isBlank(jobdesc))  {
        // 이런 경우는 검색조건 입력하라고 표시한다.
        doQuery = false;
    }
    
    ControllerAdminLocal  admin         = getControllerAdmin();
    List<JobInstance>     jobinsList    = (List<JobInstance>)request.getAttribute("jobins_query_result");
    Integer               totalCount    = (Integer)request.getAttribute("jobins_query_total");
    List<String>          agentIdList   = admin.getAgentIdList();

    WebPagingNavigator pn = new WebPagingNavigator();
    if (doQuery) {
	    pn.setTotalItemCount(totalCount);
	    pn.setCurrPageNo(currPageNo);
	    pn.setItemSizePerPage(jobInsPageSize);
	    pn.calculate();
    }
%>
<jsp:include page="top.jsp" flush="true"/>
<%	if (autoReload) { %>
<meta http-equiv="refresh" content="10" />
<% 	} %>

<script>
    var maxPageNo = <%=pn.getMaxPageNo()%>;
    
    function checkAll() {
        var chk = document.form2.chkjobinsid;
        var v = document.form2.chkall.checked;
        if (chk.length == null) { /* 하나일때 */
            chk.checked = v ;
        }else {
            for (var i=0; i<chk.length; i++ ) {
                chk[i].checked = v ;
            }
        }
    }

    function openJobInstanceWin(jobid) {
        window.open("view_jobins_dtl.jsp?jobinstanceid="+jobid, "jobins_"+jobid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }

    function openViewFilterMgrWin() {
        window.open("view_viewfilter_list.jsp", 'viewfilter_mgr', 'width=850,height=400,scrollbars=1').focus();
    }

    function doExcelDownload() {
        document.form1.action="view_jobins_excel.jsp";
        document.form1.submit();
    }

    function openDiagramWin() {
        window.open('', 'jobins_diagram_win', 'width=1200,height=800,scrollbars=1').focus();
        document.form1.action="view_jobins_diagram.jsp";
		document.form1.deep_query.value='true';
		document.form1.target="jobins_diagram_win";
        document.form1.submit();
		document.form1.target='';
		document.form1.deep_query.value='';
    }

    function openMultiFileLogWin() {
        if (confirm('<%=Label.get("view_jobins.multi.logview.confirm.msg")%>')) {
            window.open('', 'multilogwin', 'width=850,height=500,scrollbars=1').focus();
            document.form2.action="view_filelog_multi.jsp";
            document.form2.target="multilogwin";
            document.form2.submit();
        }
    }
    
    function openExeWin(jobinstanceid) {
        window.open("view_jobexe.jsp?jobinstanceid="+jobinstanceid, 'jobexe_'+jobinstanceid.replace(/-/g, ''), 'width=1150,height=300,scrollbars=1').focus();
    }

    function doMultiAction(jobAction, desc) {
        if (confirm(desc)) {
            if (jobAction == 'stop') {
                alert('<%=Label.get("view_jobins.stop.job.warning.alert")%>');
            }
            document.form2.action="action_jobins.jsp";
            document.form2.cmd.value=jobAction+"_multi";
            document.form2.submit();
        }
    }
    
    function checkPageNo() {
    	var currPageNoVal = document.form1.currpageno.value;
    	
    	if (currPageNoVal < 1) {
    		currPageNoVal = 1;
        }else if (maxPageNo > 0 && currPageNoVal > maxPageNo){
        	currPageNoVal = maxPageNo;
        }
        document.form1.currpageno.value=currPageNoVal;
    }

    function doQuery() {
        checkPageNo();
        document.form1.action="view_jobins_noajax.jsp";
        document.form1.submit();
    }

    function orderby(orderbyCol) {
        document.form1.orderby.value=orderbyCol;
        
        if ('<%=orderby%>' == orderbyCol) {
            if ('<%=orderdir%>' == 'ASC') {
                document.form1.orderdir.value='DESC';
            }else {
                document.form1.orderdir.value='ASC';
            }
        }else {
            document.form1.orderdir.value='ASC';
        }
        doQuery();
    }
    
    function gopage(pageno) {
    	document.form1.currpageno.value=pageno;
    	doQuery();
    }
    

</script>

<center>

<br>
<form name="form1" action="view_jobins_noajax.jsp" method="get" style="margin-bottom:0">
<table border="1" style = "border-collapse:collapse" bordercolor = "#a0a0a0" width="95%">
<tr align="center">
    <td bgcolor="#efefef">
        <b><A href="javascript:openViewFilterMgrWin();"><%=Label.get("viewfilter")%></a></b>
    </td>
    <td>
        <select name="viewfilter">
            <option value=""></option>
            <%=printViewFilterSelect(admin, viewfilter)%>
        </select>
    </td>
    <td bgcolor="#efefef">
        <b><%=Label.get("job.jobinsid.short")%></b>
    </td>
    <td>
        <input type="text" name="jobinstanceid" value="<%=Util.isBlank(jobinstanceid) ? (isAdmin(request) ? "%" : "") : jobinstanceid%>" size=13>
    </td>
    <td bgcolor="#efefef">
        <b><%=Label.get("job.jobgroup")%></b>
    </td>
    <td>
        <input type="text" name="jobgroup" value="<%=nvl(jobgroup)%>" size=9>
    </td>
    <td bgcolor="#efefef">
       <b><%=Label.get("job.jobtype")%></b>
    </td>
    <td>
       <select name="jobtype">
            <%=printSelectOption("", "", jobtype)%>
            <%=printJobTypeSelectOptionList(admin, jobtype) %>
       </select>
    </td>
    <td bgcolor="#efefef">
       <b><%=Label.get("common.server")%></b>
    </td>
    <td>
        <select name="agentid">
            <%=printSelectOption("",       "",       agentid)%>
<%  for (String _agentId : agentIdList) {          %>
            <%=printSelectOption(_agentId, _agentId, agentid)%>
<%  }                                               %>
       </select>
    </td>
    <td bgcolor="#efefef">
        <b><%=Label.get("common.search.gubun")%></b>
    </td>
    <td>
        <select name="jobfilter">
            <%=printJobFilter(jobfilter, request)%>
        </select>
    </td>
    <td bgcolor="#efefef">
        <b><%=Label.get("job.desc")%></b>
    </td>
    <td>
        <input type="text" name="jobdesc" value="<%=conv(jobdesc)%>" size=12>
    </td>
    <td rowspan=2 align=center>
        <input type="submit" class="button gray big_01"  value="<%=Label.get("common.btn.query")%>" onclick="doQuery();">
    </td>
</tr>
<tr align="center">
    <td bgcolor="#efefef">
        <b><%=Label.get("jobins.state")%></b>
    </td>
    <td>
        <select name="jobstate">
            <option value=""><%=Label.get("common.etc.all")%></option>
            <%=printSelectOption("I", "Init",         jobstate)%>
            <%=printSelectOption("W", "Wait",         jobstate)%>
            <%=printSelectOption("O", "Ended OK",     jobstate)%>
            <%=printSelectOption("F", "Ended Fail",   jobstate)%>
            <%=printSelectOption("R", "Running",      jobstate)%>
            <%=printSelectOption("P", "Suspended",    jobstate)%>
            <%=printSelectOption("S", "Sleep Repeat", jobstate)%>
            <%=printSelectOption("X", "Expired",      jobstate)%>
            <%=printSelectOption("G", "Ghost",        jobstate)%>
        </select>
    </td>
    <td bgcolor="#efefef">
        <b><%=Label.get("job.prejob.id")%></b>
    </td>
    <td>
        <input type="text" name="prejobid" value="<%=nvl(prejobid)%>" size=13>
    </td>
    <td bgcolor="#efefef">
        <b><%=Label.get("job.trigger.job")%></b>
    </td>
    <td>
        <input type="text" name="triggerjobid" value="<%=nvl(triggerjobid)%>" size=13>
    </td>
    <td bgcolor="#efefef">
        <b><%=Label.get("jobins.last.exe")%></b>
    </td>
    <td>
        <select name="lastretcode">
            <option value=""><%=Label.get("common.etc.all")%></option>
            <%=printSelectOption("-1", Label.get("jobins.last.exe.not"),  lastretcode)%>
            <%=printSelectOption("0",  Label.get("jobins.exe.ok"),   lastretcode)%>
            <%=printSelectOption("1",  Label.get("jobins.exe.fail"), lastretcode)%>
        </select>
    </td>
    <td bgcolor="#efefef">
        <b><%=Label.get("common.procdate")%></b>
    </td>
    <td>
        <input type="text" name="procdate" value="<%=nvl(procdate)%>" maxlength=8 size=9>
    </td>
    <td bgcolor="#efefef">
        <b><%=Label.get("common.searchdate")%></b>
    </td>
    <td>
        <input type="text" name="searchdate" value="<%=nvl(searchdate)%>" maxlength=8 size=9>
    </td>
    <td bgcolor="#efefef">
        <b><%=Label.get("job.owner")%></b>
    </td>
    <td>
        <input type="text" name="owner" value="<%=conv(owner)%>" maxlength=20 size=9>
    </td>
</tr>
</table>
<input type="hidden" name="orderby"  value="<%=orderby%>">
<input type="hidden" name="orderdir" value="<%=orderdir%>">
<input type="hidden" name="deep_query" value="">
<br>

<table border="0" width="100%">
<tr>
	<td align="left" width="30%">
		<input type="button" class="button gray medium_01"  value="<%=Label.get("common.btn.download")%>" onclick="doExcelDownload();">
		<input type="button" class="button gray medium_01"  value="Diagram" onclick="openDiagramWin();">
	</td>
    <td align="center" width="40%" >
<%
    if (doQuery) {
%>      
        <img src="images/pag_pre2.png" title="First Page" onclick="javascript:gopage(1);" >
        <img src="images/pag_pre1.png" title="Prev Page" onclick="javascript:gopage(<%=currPageNo-1%>);" >
        <input name="currpageno" type="text" size="1" maxlength="10" value="<%=currPageNo%>" style="text-align:right">/<%=pn.getMaxPageNo()%>
        <img src="images/btn_searchO.png" onclick="javascript:doQuery()" >
        <img src="images/pag_next1.png" title="Next Page" onclick="javascript:gopage(<%=currPageNo+1%>);" >
        <img src="images/pag_next2.png" title="Last Page" onclick="javascript:gopage(<%=pn.getMaxPageNo()%>);" >
<%
    }
%>
    </td>
	<td align="right" width="30%">
	    <%=Label.get("common.btn.auto.refresh")%><input type="checkbox" name="autoreload" value="1" <%=autoReload?"checked":""%> >&nbsp;&nbsp;
	    <b><span id="current_datetime"><%=toDatetimeString(new java.util.Date(), false) %></span></b>
	</td>
</tr>
</table>
</form>

<form name="form2" action="action_jobins.jsp" method="post" style="margin-top:0">
<input type="hidden" name="cmd" value="">
<input type="hidden" name="returnurl" value="<%=returnUrlPlain%>">
<table id="jobins_table" border="1" style = "border-collapse:collapse" bordercolor = "#a0a0a0" width="100%">
<tr align="center" bgcolor="#DDDDFF">
<%      if (isAdmin(request) || isOperator(request)) {   %>
<td><input type="checkbox" name="chkall" onclick="checkAll();"></td>
<%      }                         %>
<td></td>
<td><a href="javascript:orderby('JOB_GROUP_ID');"><%=Label.get("job.jobgroup")%><%=printSortMark(orderby, orderdir, "JOB_GROUP_ID")%></a></td>
<td><a href="javascript:orderby('JOB_INSTANCE_ID');"><%=Label.get("job.jobinsid")%><%=printSortMark(orderby, orderdir, "JOB_INSTANCE_ID")%></a></td>
<td><a href="javascript:orderby('JOB_DESC');"><%=Label.get("job.desc")%><%=printSortMark(orderby, orderdir, "JOB_DESC")%></a></td>
<td><%=Label.get("jobins.succcount")%>/<%=Label.get("jobins.runcount")%></td>
<td nowrap><a href="javascript:orderby('JOB_STATE');"><%=Label.get("jobins.state")%><%=printSortMark(orderby, orderdir, "JOB_STATE")%></a></td>
<td><%=Label.get("jobins.lock")%></td>
<td><a href="javascript:orderby('TIME_FROM');"><%=Label.get("job.time.from")%><%=printSortMark(orderby, orderdir, "TIME_FROM")%></a></td>
<td><%=Label.get("job.repeat")%></td>
<td><%=Label.get("job.confirm")%></td>
<td><%=Label.get("job.jobtype.short")%></td>
<td><a href="javascript:orderby('AGENT_NODE');"><%=Label.get("common.server")%><%=printSortMark(orderby, orderdir, "AGENT_NODE")%></a></td>
<td><%=Label.get("job.component")%></td>
<td><%=Label.get("common.progress")%></td>
<td>%</td>
<td><a href="javascript:orderby('LAST_START_TIME');"><%=Label.get("jobins.last.starttime")%><%=printSortMark(orderby, orderdir, "LAST_START_TIME")%></a></td>
<td><a href="javascript:orderby('LAST_END_TIME');"><%=Label.get("jobins.last.endtime")%><%=printSortMark(orderby, orderdir, "LAST_END_TIME")%></a></td>
<td><%=Label.get("jobins.run.elaptime")%></td>
<td><a href="javascript:orderby('PROC_DATE');"><%=Label.get("common.procdate")%><br>(PROC)<%=printSortMark(orderby, orderdir, "PROC_DATE")%></a></td>
<td><a href="javascript:orderby('LAST_MODIFY_TIME');"><%=Label.get("job.lastmodifytime.short")%><%=printSortMark(orderby, orderdir, "LAST_MODIFY_TIME")%></a></td>
</tr>
<%
    boolean colorFlip=true;
    int i=pn.getStartItemNoForCurrPage();
	for (JobInstance jobins : jobinsList) {
		if (!filterJobList(jobins, jobfilter)) continue;
	    colorFlip = !colorFlip;
%>
<tr align="center" id="TR_<%=jobins.getJobInstanceId()%>"  <%=printTrBgcolor(jobins.getJobState(), colorFlip)%> >
<%      if (isAdmin(request) || isOperator(request)) {   %>
<td><input type="checkbox" name="chkjobinsid" value="<%=jobins.getJobInstanceId()%>"></td>
<%      }                         %>
<td><%=i%></td>
<td><%=nvl(jobins.getJobGroupId())%></td>
<td><%=nvl(jobins.getJobInstanceId())%></td>
<td align=left 
    onMouseOver="this.style.cursor='pointer';this.style.backgroundColor=<%=printTrHandOverBgcolor(jobins.getJobState(), colorFlip)%>;" 
    onMouseOut="this.style.backgroundColor=<%=printTrHandOutBgcolor(jobins.getJobState(), colorFlip)%>;" 
    onclick="openJobInstanceWin('<%=jobins.getJobInstanceId()%>');" ><b>&nbsp;<%=getAppCode(jobins.getJobId())%></b> <%=getShortDescription(jobins.getDescription())%></td>
<td><a href="javascript:openExeWin('<%=jobins.getJobInstanceId()%>')"><%=jobins.getEndOkCount()%>/<%=jobins.getRunCount()%></a></td>
<td><a title="<%=nvl(conv(jobins.getJobStateReason()), jobins.getJobStateText())%>"><b><font color="<%=getStateColor(jobins.getJobState())%>"><%=conv(jobins.getJobStateText())%></font></b></a></td>
<td><%=jobins.isLocked()?"▼":""%></td>
<td><%=nvl(jobins.getTimeFrom())%></td>
<td><%="Y".equals(jobins.getRepeatYN()) ? ("EXACT".equals(jobins.getRepeatIntvalGb()) ? Util.fitLength(jobins.getRepeatExactExp(),15) : jobins.getRepeatIntval()) : "-"%></td>
<td><%="Y".equals(jobins.getConfirmNeedYN()) ? (Util.isBlank(jobins.getConfirmed()) ? "<font color=red>-/<b>Y</b></font>" : "<font color=blue><b>０</b>/<b>Y</b></font>") : ""%></td>
<td nowrap><%=nvl(getJobTypeText(jobins.getJobType()))%></td>
<td><%=nvl(jobins.getAgentNode())%></td>
<td style="word-break:break-all"><%=conv(getShortComponentName(jobins.getJobType(),jobins.getComponentName()))%></td>
<td id="prgs_<%=jobins.getJobInstanceId()%>"><%=toProgressString(admin.getJobProgress(jobins.getLastJobExeId()))%></td>
<td id="prgs_per_<%=jobins.getJobInstanceId()%>" align=right><%=toProgressPercentage(admin.getJobProgress(jobins.getLastJobExeId()))%></td>
<td><%=toDatetimeString(nvl(jobins.getLastStartTime()))%></td>
<td><%=toDatetimeString(nvl(jobins.getLastEndTime()))%></td>
<td id="running_elaptime_<%=jobins.getJobInstanceId()%>" nowrap><%=toRunTimeString(jobins.getJobState(), jobins.getLastStartTime(), jobins.getLastEndTime(), true)%></td>
<td><b><%=nvl(jobins.getProcDate())%></b></td>
<td><%=toDatetimeString(DateUtil.getTimestamp(jobins.getLastModifyTime()), true)%></td>
</tr>
<%	
        i++;
	}
%>
</table>
<br>
<%
    if (!doQuery) {
%>
<b><font color="blue"><%=Label.get("common.input.search.condition")%></font></b>
<br><br>
<%=Label.get("viewfilter.guide.msg")%> <A href="javascript:openViewFilterMgrWin();">[<%=Label.get("viewfilter.add")%>]</a>

<%
    }else {
%>
<br>
<table border="0" width="50%">
<tr>
    <td align=center>
        <input type="button" value="<%=Label.get("jobctl.action.name.forcerun")%>"   class="button gray01 medium_04" onclick="doMultiAction('forcerun',   '<%=Label.get("jobctl.action.desc.forcerun",   Label.get("jobins.selected.job"))%>');">
        <input type="button" value="<%=Label.get("jobctl.action.name.rerun")%>"      class="button gray01 medium_04" onclick="doMultiAction('rerun',      '<%=Label.get("jobctl.action.desc.rerun",      Label.get("jobins.selected.job"))%>');">
        <input type="button" value="<%=Label.get("jobctl.action.name.stop")%>"       class="button gray01 medium_04" onclick="doMultiAction('stop',       '<%=Label.get("jobctl.action.desc.stop",       Label.get("jobins.selected.job"))%>');">
    </td>
</tr>
<tr>
    <td align=center>
        <input type="button" value="<%=Label.get("jobctl.action.name.suspend")%>"    class="button gray01 medium_04" onclick="doMultiAction('suspend',    '<%=Label.get("jobctl.action.desc.suspend",    Label.get("jobins.selected.job"))%>');">
        <input type="button" value="<%=Label.get("jobctl.action.name.resume")%>"     class="button gray01 medium_04" onclick="doMultiAction('resume',     '<%=Label.get("jobctl.action.desc.resume",     Label.get("jobins.selected.job"))%>');">
        <input type="button" value="<%=Label.get("jobctl.action.name.forceendok")%>" class="button gray01 medium_04" onclick="doMultiAction('forceendok', '<%=Label.get("jobctl.action.desc.forceendok", Label.get("jobins.selected.job"))%>');">
    </td>
</tr>
<tr>
    <td align=center>
        <input type="button" value="<%=Label.get("jobctl.action.name.lock")%>"       class="button gray01 medium_04" onclick="doMultiAction('lock',    '<%=Label.get("jobctl.action.desc.lock",    Label.get("jobins.selected.job"))%>');">
        <input type="button" value="<%=Label.get("jobctl.action.name.unlock")%>"     class="button gray01 medium_04" onclick="doMultiAction('unlock',  '<%=Label.get("jobctl.action.desc.unlock",  Label.get("jobins.selected.job"))%>');">
        <input type="button" value="<%=Label.get("jobctl.action.name.confirm")%>"    class="button gray01 medium_04" onclick="doMultiAction('confirm', '<%=Label.get("jobctl.action.desc.confirm", Label.get("jobins.selected.job"))%>');">
    </td>
</tr>
<tr>
    <td align=center>
        <input type="button" class="button gray01 medium_04" value="<%=Label.get("jobins.view.multilog")%>" onclick="openMultiFileLogWin();">
    </td>
</tr>
</table>
<%
    }
%>
</form>
</center>
<br><br>
</div>
</div>
<jsp:include page="bottom.jsp" flush="true"/>
</body>
</html>

