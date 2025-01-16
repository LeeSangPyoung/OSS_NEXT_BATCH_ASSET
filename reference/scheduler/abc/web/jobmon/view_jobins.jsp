<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    String returnUrl      = null; // encode 된놈.   GET 용
    String returnUrlPlain = null; // encode 안된놈. POST 용
    if (request.getQueryString()==null) {
        returnUrl = request.getRequestURI();
        returnUrlPlain = request.getRequestURI();
    }else {
        returnUrl = request.getRequestURI()+"?"+URLEncoder.encode(request.getQueryString());
        returnUrlPlain = request.getRequestURI()+"?"+request.getQueryString();
    }

    ControllerAdminLocal  admin = getControllerAdmin();

    // 조회 조건
    String lastretcode    = request.getParameter("lastretcode");
    String jobstate       = request.getParameter("jobstate");
    String jobinstanceid  = nvl(request.getParameter("jobinstanceid")).trim();
    String jobgroup       = nvl(request.getParameter("jobgroup")).trim();
    String jobdesc        = nvl(request.getParameter("jobdesc")).trim();
    String component      = nvl(request.getParameter("component")).trim();
    String jobtype        = nvl(request.getParameter("jobtype")).trim();
    String agentid        = nvl(request.getParameter("agentid")).trim();
    String prejobid       = nvl(request.getParameter("prejobid")).trim();
    String searchdatetype = nvl(request.getParameter("searchdatetype")).trim();
    String searchdatefrom = nvl(request.getParameter("searchdatefrom")).trim();
    String searchdateto   = nvl(request.getParameter("searchdateto")).trim();
    String owner          = request.getParameter("owner");
    String viewfilter     = request.getParameter("viewfilter");
    String jobfilter      = (jobfilter=nvl(request.getParameter("jobfilter"))).length() == 0 ? null : jobfilter;
    String orderby        = nvl(request.getParameter("orderby"), "LAST_MODIFY_TIME");
    String orderdir       = nvl(request.getParameter("orderdir"), "DESC"); // ASC, DESC
    int currPageNo        = Util.toInt(request.getParameter("currpageno"), 1);
    String autoReloadInterval = request.getParameter("autoreload_interval");
    String userJobInsPageSize = nvl(request.getParameter("jobins_page_size"), jobInsPageSize+"");
    boolean doQuery       = true;

    if (Util.isBlank(viewfilter) && Util.isBlank(jobinstanceid) && Util.isBlank(jobgroup) && Util.isBlank(jobtype) && 
        Util.isBlank(agentid) && Util.isBlank(owner) && Util.isBlank(jobdesc))  {
        // 맨처음, 메뉴클릭시 조건. 이런 경우는 검색조건 입력하라고 표시한다.
        doQuery = false;
        jobinstanceid = "%";
        searchdatetype  = "activationDate"; /* 기본 조회일 조건은 생성일 */

        String todayDate;
        if (Util.getCurrentHHMMSS().compareTo(admin.getSystemConfigValue("DAILY_ACTIVATION_TIME")+"00") >=0) {
            /* daily activation 이후 */ 
            todayDate = Util.getCurrentYYYYMMDD();
        }else {
            /* daily activation 이전 */
            todayDate = Util.getYesterdayYYYYMMDD();
        }

        if (isAdmin(request)) {
            searchdatefrom = Util.getDateAdd(todayDate, -2); /* admin 은 3일치를 모니터링함. 3일전 job flow 가 오늘까지 실행될 수 있으므로 */
            searchdateto   = Util.getCurrentYYYYMMDD();
        }else {
            searchdatefrom = todayDate; /* 일반사용자는 기본값으로 당일자만 모니터링함. 조회성능 고려 */
            searchdateto   = Util.getCurrentYYYYMMDD();
        }
    }
    
    List<String> agentIdList = admin.getAgentIdList();

%>
<jsp:include page="top.jsp" flush="true"/>
<link rel="stylesheet" href="jobdisplay.css" type="text/css" />
<script>
    var maxPageNo = -1;

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
        window.open("view_jobins_dtl.jsp?jobinstanceid="+jobid, "jobins_"+jobid.replace(/-/g, ''), 'width=820,height=800,scrollbars=1').focus();
    }

    function openViewFilterMgrWin() {
        window.open("view_viewfilter_list.jsp", 'viewfilter_mgr', 'width=810,height=400,scrollbars=1').focus();
    }

    function doExcelDownload() {
        document.form1.action="view_jobins_excel.jsp";
        document.form1.target='';
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
            window.open('', 'multilogwin', 'width=850,height=500,scrollbars=1,resizable=1').focus();
            document.form2.action="view_filelog_multi.jsp";
            document.form2.target="multilogwin";
            document.form2.submit();
        }
    }
    
    function openExeWin(jobinstanceid) {
        window.open("view_jobexe.jsp?jobinstanceid="+jobinstanceid, 'jobexe_'+jobinstanceid.replace(/-/g, ''), 'width=1150,height=300,scrollbars=1,resizable=1').focus();
    }
    
    function openFileLogWin(jobinstanceid, filetype) {
        window.open("view_filelog.jsp?jobinstanceid="+jobinstanceid+"&filetype="+filetype+"&tail=1", 'filelog_'+filetype+'_'+jobinstanceid.replace(/-/g, ''), 'width=1000,height=600,scrollbars=1,resizable=1').focus();
    }

    function openJobGroupSelectWin(targetElemName) {
        window.open("popup_jobgroup.jsp?target_name="+targetElemName, 'jobgroupselect', 'width=700,height=400,scrollbars=1').focus();
    }

    function getCheckedCount(chk) {
        if (chk.length == null) { /* 하나일때 */
            return chk.checked ? 1 : 0;
        }else {
            var cnt = 0;
            for (var i=0; i<chk.length; i++) {
                cnt += chk[i].checked ? 1 : 0;
            }
            return cnt;
        }
    }
    
    function doMultiAction(jobAction, desc) {
        if (getCheckedCount(document.form2.chkjobinsid) == 0) {
            alert("Not checked");
        }else {
            var left = (window.screen.width  / 2) - ((500 / 2) + 10);
            var top  = (window.screen.height / 2) - ((200 / 2) + 50);
    
            window.open('', 'jobins_action', 'width=550,height=300,left='+left+',top='+top+',toolbar=no,menubar=no,scrollbars=1').focus();
            document.form2.action="cfm_action_jobins.jsp";
            document.form2.cmd.value=jobAction;
            document.form2.target='jobins_action';
            document.form2.submit();
        }
    }
    
    function checkPageNo() {
        if (document.form1.currpageno == null) {
            return;
        }
        var currPageNoVal = parseInt(document.form1.currpageno.value);
        if (currPageNoVal < 1) {
            currPageNoVal = 1;
        }else if (maxPageNo > 0 && currPageNoVal > maxPageNo){
            currPageNoVal = maxPageNo;
        }
        document.form1.currpageno.value = currPageNoVal;
    }

    function validateInputData() {
        var regex = /(\d{8})/;
        if (!document.form1.searchdatefrom.value.match(regex) || !document.form1.searchdateto.value.match(regex)) {
            alert("Date format error");
            return false;
        }else {
            return true;
        }
    }

    function doQuery() {
        checkPageNo();
        if (validateInputData()) {
            document.form1.action="view_jobins.jsp";
            document.form1.submit();
        }else {
            return false;
        }
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
<div class="content-wrap">
<div class="content-title__wrap">
    <div class="content-title"><%=Label.get("top.menu.jobins")%></div>
</div>
<form name="form1" action="view_jobins.jsp" method="get" style="margin-top:0;margin-bottom:0">
<input type="hidden" name="orderby"  value="<%=orderby%>">
<input type="hidden" name="orderdir" value="<%=orderdir%>">
<input type="hidden" name="deep_query" value="">
<table class="Table njf-table__typea Margin-bottom-10">
    <colgroup>
        <col style="width:4%">
        <col style="width:10%">
        <col style="width:7%">
        <col style="width:10%">
        <col style="width:5%">
        <col style="width:8%">
        <col style="width:5%">
        <col style="width:8%">
        <col style="width:6%">
        <col style="width:6%">
        <col style="width:3%">
        <col style="width:8%">    
        <col style="width:4%">
        <col style="width:10%">
        <col style="width:6%">
    </colgroup>
    <tbody>
        <tr>
            <th style="padding:1px">
                <b><A href="javascript:openViewFilterMgrWin();"><%=Label.get("viewfilter")%></a></b>
            </th>
            <td style="text-align:center; padding:1px">
                <select class="Select Width-100" name="viewfilter" style="padding:0px; font-size:11;">
                    <option value=""></option>
                    <%=printViewFilterSelect(admin, viewfilter)%>
                </select>
            </td>
            <th style="padding:1px">
                <b><%=Label.get("job.jobinsid.short")%></b>
            </th>
            <td style="text-align:center; padding:1px">
                <input class="Textinput Width-80" type="text" name="jobinstanceid" value="<%=nvl(jobinstanceid)%>">%
            </td>
            <th style="padding:1px">
                <b><a href="javascript:openJobGroupSelectWin('jobgroup');"><%=Label.get("job.jobgroup")%></a></b>
            </th>
            <td style="text-align:center; padding:1px">
                <input class="Textinput Width-80" type="text" name="jobgroup" value="<%=nvl(jobgroup)%>">%
            </td>
            <th style="padding:1px">
               <b><%=Label.get("job.jobtype")%></b>
            </th>
            <td style="text-align:center; padding:1px">
               <select class="Select Width-100" name="jobtype" style="padding:0px; font-size:11;">
                    <%=printSelectOption("", "", jobtype)%>
                    <%=printJobTypeSelectOptionList(admin, jobtype) %>
               </select>
            </td>
            <th style="padding:1px">
               <b><%=Label.get("common.server")%></b>
            </th>
            <td style="text-align:center; padding:1px">
                <select class="Select Width-100" name="agentid" style="padding:0px; font-size:11;">
                    <%=printSelectOption("",       "",       agentid)%>
        <%  for (String _agentId : agentIdList) {          %>
                    <%=printSelectOption(_agentId, _agentId, agentid)%>
        <%  }                                               %>
               </select>
            </td>
            <th style="padding:1px">
                <b><%=Label.get("common.search.gubun")%></b>
            </th>
            <td style="text-align:center; padding:1px">
                <select class="Select Width-100" name="jobfilter" style="padding:0px; font-size:11;">
                    <%=printJobFilter(jobfilter, request)%>
                </select>
            </td>
            <th style="padding:1px">
                <b><%=Label.get("job.desc")%></b>
            </th>
            <td style="text-align:center; padding:1px">
                %<input class="Textinput Width-70" type="text" name="jobdesc" value="<%=conv(jobdesc)%>">%
            </td>
            <td rowspan=2  style="text-align:center; padding:1px">
                <input type="submit" class="Button Width-100" style="line-height:62px; padding:0px" value="<%=Label.get("common.btn.query")%>" onclick="return doQuery();"><!-- <img alt="Search" src="images/search-icon.png"> -->
            </td>
        </tr>
        <tr>
            <th style="padding:1px">
                <b><%=Label.get("jobins.state")%></b>
            </th>
            <td style="text-align:center; padding:1px">
                <select class="Select Width-100" name="jobstate" style="padding:0px; font-size:11;">
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
            <th style="padding:1px">
                <b><%=Label.get("job.prejob.id")%></b>
            </th>
            <td style="text-align:center; padding:1px">
                <input class="Textinput Width-80" type="text" name="prejobid" value="<%=nvl(prejobid)%>">%
            </td>
            <th style="padding:1px">
                <b><%=Label.get("job.component")%></b>
            </th>
            <td style="text-align:center; padding:1px">
                <input class="Textinput Width-80" type="text" name="component" value="<%=nvl(component)%>">%
            </td>
            <th style="padding:1px">
                <b><%=Label.get("jobins.last.exe")%></b>
            </th>
            <td style="text-align:center; padding:1px">
                <select class="Select Width-100" name="lastretcode" style="padding:0px; font-size:11;">
                    <option value=""><%=Label.get("common.etc.all")%></option>
                    <%=printSelectOption("-1", Label.get("jobins.last.exe.not"),  lastretcode)%>
                    <%=printSelectOption("0",  Label.get("jobins.exe.ok"),   lastretcode)%>
                    <%=printSelectOption("1",  Label.get("jobins.exe.fail"), lastretcode)%>
                </select>
            </td>
            <th style="padding:1px">
                <select class="Select Width-100" name="searchdatetype" style="padding:0px; font-size:11;">
                    <%=printSelectOption("activationDate", Label.get("common.activationdate"),  searchdatetype)%>
                    <%=printSelectOption("procDate",       Label.get("common.procdate"),        searchdatetype)%>
                </select>
            </th>
            <td colspan=3 style="text-align:center; padding:1px">
                <input class="Textinput Width-45 Text-center" style="padding:0px; font-size:11;" type="text" name="searchdatefrom" value="<%=nvl(searchdatefrom)%>" maxlength=8>
                ~
                <input class="Textinput Width-45 Text-center" style="padding:0px; font-size:11;" type="text" name="searchdateto" value="<%=nvl(searchdateto)%>" maxlength=8>
            </td>
            <th style="padding:1px">
                <b><%=Label.get("job.owner")%></b>
            </th>
            <td style="text-align:center; padding:1px">
                %<input class="Textinput Width-70" type="text" name="owner" value="<%=conv(owner)%>" maxlength=20>%
            </td>
        </tr>
    </tbody>
</table>

<span id="error_bar"></span>
<table class="Table non-border">
    <tr>
    <%
        if (doQuery) {
    %>      
        <td align="left">
            <input type="button" class="Button" value="<%=Label.get("common.btn.download")%>"         onclick="doExcelDownload();">
            <input type="button" class="Button" value="<%=Label.get("jobins.view.multilog")%>"        onclick="openMultiFileLogWin();">
            <input type="button" class="Button" value="Diagram"                                       onclick="openDiagramWin();">
        </td>
        <td align="center">
            <img src="images/pag_pre2.png" title="First Page" onclick="javascript:gopage(1);" onMouseOver="this.style.cursor='pointer'">
            <img src="images/pag_pre1.png" title="Prev Page" onclick="javascript:gopage(<%=currPageNo-1%>);" onMouseOver="this.style.cursor='pointer'">
            <input name="currpageno" type="text" size="1" maxlength="10" value="<%=currPageNo%>" style="text-align:right"/>/<span id="maxPageNo1" style="display:inline-block;width:30px;">&nbsp;&nbsp;&nbsp;&nbsp;</span>
            <img src="images/btn_searchO.png" onclick="javascript:doQuery()" onMouseOver="this.style.cursor='pointer'">
            <img src="images/pag_next1.png" title="Next Page" onclick="javascript:gopage(<%=currPageNo+1%>);" onMouseOver="this.style.cursor='pointer'">
            <img src="images/pag_next2.png" title="Last Page" onclick="javascript:gopage(1000000);" onMouseOver="this.style.cursor='pointer'">
        </td>
    <%
        }
    %>
        <td align="right" width="450px">
            <%=Label.get("common.numberperpage")%>
            <select class="Select" name="jobins_page_size">
                <%=printSelectOption("100",    "100",   userJobInsPageSize)%>
                <%=printSelectOption("200",    "200",   userJobInsPageSize)%>
                <%=printSelectOption("300",    "300",   userJobInsPageSize)%>
                <%=printSelectOption("500",    "500",   userJobInsPageSize)%>
                <%=printSelectOption("1000",   "1000",  userJobInsPageSize)%>
                <%=printSelectOption("2000",   "2000",  userJobInsPageSize)%>
                <%=printSelectOption("3000",   "3000",  userJobInsPageSize)%>
                <%=printSelectOption("5000",   "5000",  userJobInsPageSize)%>
                <%=printSelectOption("10000",  "10000", userJobInsPageSize)%>
                <%=printSelectOption("30000",  "30000", userJobInsPageSize)%>
                <%=printSelectOption("50000",  "50000", userJobInsPageSize)%>
            </select>
            &nbsp;
            <%=Label.get("common.btn.auto.refresh")%>
            <select class="Select Margin-right-5" name="autoreload_interval">
                <%=printSelectOption("0",  "NO",                             autoReloadInterval)%>
    <%
        for (String jobinsViewRefreshInterval : jobinsViewRefreshIntervalList) {
            out.println(printSelectOption(jobinsViewRefreshInterval, jobinsViewRefreshInterval+Label.get("common.second"),  autoReloadInterval));
        }
    %>
            </select>
            <span style="font-size:12px; text-align:right;" id="current_datetime"><%=toDatetimeString(new java.util.Date(), false) %></span>
        </td>
    </tr>
</table>
</form>

<form name="form2" action="action_jobins.jsp" method="post" style="margin-top:0">
<input type="hidden" name="cmd" value="">
<table id="jobins_table" class="nbs-table__typea" width="100%">
    <thead>
    	<tr>
            <th style="padding:2px;"><label><input class="Checkbox" type="checkbox" id="chkall" name="chkall" onclick="checkAll()"></label></th>
            <th>p<%=currPageNo%></th>
            <th><a href="javascript:orderby('JOB_GROUP_ID');"><%=Label.get("job.jobgroup")%><%=printSortMark(orderby, orderdir, "JOB_GROUP_ID")%></a></th>
            <th><a href="javascript:orderby('JOB_INSTANCE_ID');"><%=Label.get("job.jobinsid")%><%=printSortMark(orderby, orderdir, "JOB_INSTANCE_ID")%></a></th>
            <th><a href="javascript:orderby('JOB_DESC');"><%=Label.get("job.desc")%><%=printSortMark(orderby, orderdir, "JOB_DESC")%></a></th>
            <th><%=Label.get("jobins.succcount")%>/<%=Label.get("jobins.runcount")%></th>
            <th>Log</th>
            <th nowrap><a href="javascript:orderby('JOB_STATE');"><%=Label.get("jobins.state")%><%=printSortMark(orderby, orderdir, "JOB_STATE")%></a></th>
            <th><%=Label.get("jobins.lock")%></th>
            <th><a href="javascript:orderby('TIME_FROM');"><%=Label.get("common.time")%><%=printSortMark(orderby, orderdir, "TIME_FROM")%></a></th>
            <th><%=Label.get("job.repeat")%></th>
            <th><%=Label.get("job.confirm")%></th>
            <th><%=Label.get("job.jobtype.short")%></th>
            <th><a href="javascript:orderby('AGENT_NODE');"><%=Label.get("common.server")%><%=printSortMark(orderby, orderdir, "AGENT_NODE")%></a></th>
            <th><a href="javascript:orderby('COMPONENT_NAME');"><%=Label.get("job.component")%><%=printSortMark(orderby, orderdir, "COMPONENT_NAME")%></a></th>
            <th><%=Label.get("common.progress")%></th>
            <th colspan="2">%</th>
            <th><a href="javascript:orderby('LAST_START_TIME');"><%=Label.get("jobins.last.starttime")%><%=printSortMark(orderby, orderdir, "LAST_START_TIME")%></a></th>
            <th><a href="javascript:orderby('LAST_END_TIME');"><%=Label.get("jobins.last.endtime")%><%=printSortMark(orderby, orderdir, "LAST_END_TIME")%></a></th>
            <th><%=Label.get("jobins.run.elaptime")%></th>
            <th><a href="javascript:orderby('PROC_DATE');"><%=Label.get("common.procdate")%><br>(PROC)<%=printSortMark(orderby, orderdir, "PROC_DATE")%></a></th>
            <th><a href="javascript:orderby('LAST_MODIFY_TIME');"><%=Label.get("job.lastmodifytime.short")%><%=printSortMark(orderby, orderdir, "LAST_MODIFY_TIME")%></a></th>
    	</tr>
    </thead>
    <tbody></tbody>
</table>
<%
    if (!doQuery) {
%>
<br>
<b><font color="blue"><%=Label.get("common.input.search.condition")%></font></b>
<br><br>
<%=Label.get("viewfilter.guide.msg")%> <a href="javascript:openViewFilterMgrWin();">[<%=Label.get("viewfilter.add")%>]</a>
</form>
</div>
</center>
<br><br>
</body>
<%
    }else {
%>
<br>
<table border="0">
<tr align=center>
    <td>
        <input type="button" class="Button" value="<%=Label.get("jobctl.action.name.lock")%>"     onclick="doMultiAction('lock',       '<%=Label.get("jobctl.action.desc.lock",       Label.get("jobins.selected.job"))%>');" title="Lock">
        <input type="button" class="Button"    value="<%=Label.get("jobctl.action.name.unlock")%>"   onclick="doMultiAction('unlock',     '<%=Label.get("jobctl.action.desc.unlock",     Label.get("jobins.selected.job"))%>');" title="Unlock">
    </td>
    <td>&nbsp;|&nbsp;</td>
    <td>     
        <input type="button" class="Button" value="<%=Label.get("jobctl.action.name.forcerun")%>" onclick="doMultiAction('forcerun',   '<%=Label.get("jobctl.action.desc.forcerun",   Label.get("jobins.selected.job"))%>');" title="Force Run">
        <input type="button" class="Button" value="<%=Label.get("jobctl.action.name.rerun")%>"    onclick="doMultiAction('rerun',      '<%=Label.get("jobctl.action.desc.rerun",      Label.get("jobins.selected.job"))%>');" title="Rerun">
        <input type="button" class="Button" value="<%=Label.get("jobctl.action.name.stop")%>"     onclick="doMultiAction('stop',       '<%=Label.get("jobctl.action.desc.stop",       Label.get("jobins.selected.job"))%>');" title="Stop">
    </td>
<%
		if (useMultiSuspendResumeButton) {
%>
    <td>&nbsp;|&nbsp;</td>
    <td>     
        <input type="button" class="Button" value="<%=Label.get("jobctl.action.name.suspend")%>"    onclick="doMultiAction('suspend',    '<%=Label.get("jobctl.action.desc.suspend",    Label.get("jobins.selected.job"))%>');" title="Suspend">
        <input type="button" class="Button" value="<%=Label.get("jobctl.action.name.resume")%>"     onclick="doMultiAction('resume',     '<%=Label.get("jobctl.action.desc.resume",     Label.get("jobins.selected.job"))%>');" title="Resume">
    </td>
<%
		}
%>
    <td>&nbsp;|&nbsp;</td>
    <td>     
        <input type="button" class="Button"    value="<%=Label.get("jobctl.action.name.forceendok")%>" onclick="doMultiAction('forceendok', '<%=Label.get("jobctl.action.desc.forceendok", Label.get("jobins.selected.job"))%>');" title="Force End OK">
        <input type="button" class="Button" value="<%=Label.get("jobctl.action.name.confirm")%>"    onclick="doMultiAction('confirm',    '<%=Label.get("jobctl.action.desc.confirm",    Label.get("jobins.selected.job"))%>');" title="Confirm">
        <input type="button" class="Button"    value="<%=Label.get("jobctl.action.name.toghost")%>"    onclick="doMultiAction('toghost',    '<%=Label.get("jobctl.action.desc.toghost",    Label.get("jobins.selected.job"))%>');" title="Force Ghost">
        <input type="button" class="Button"   value="<%=Label.get("jobctl.action.name.changeagent")%>" onclick="doMultiAction('changeagent','<%=Label.get("jobctl.action.desc.changeagent",Label.get("jobins.selected.job"))%>');" title="Change Agent">
    </td>
</tr>
</table>
</form>
</center>

</body>

<script>
    var ongoing         = 0;
    var lastModifyTime  = 0;
    var lastModifyYMD14;
    var doFullQuery     = true;
    var fullDataResult  = true;
    var endFailJobInsIds= new Array();

    function pollServer() {
        if (ongoing == 0) {
            ongoing = 1;
            
            var httpRequest;
            if (window.XMLHttpRequest) {
                httpRequest = new XMLHttpRequest();
            } else if (window.ActiveXObject) {
                try {
                    httpRequest = new ActiveXObject("Msxml2.XMLHTTP");
                } catch (e) {
                    try {
                        httpRequest = new ActiveXObject("Microsoft.XMLHTTP");
                    }catch (e) {}
                }
            }

            httpRequest.onreadystatechange = function() {
                if (this.readyState == 4) {
                    if (this.status == 200) {
                        var resDoc = this.responseXML;
                        
                        if (resDoc == null || resDoc.getElementsByTagName("JobInstanceQuery").length == 0) { 
                            return;
                        }

                        endFailJobInsIds = new Array(); /* initialize */

                        document.getElementById('error_bar').innerHTML = "";
                        document.getElementById("current_datetime").innerHTML = resDoc.getElementsByTagName("Current")[0].firstChild.nodeValue;
                        
                        var maxPageNoNodeList = resDoc.getElementsByTagName("MaxPageNo");
                        if (maxPageNoNodeList.length > 0) {
                            maxPageNo = parseInt(maxPageNoNodeList[0].firstChild.nodeValue);
                            document.getElementById("maxPageNo1").innerHTML = maxPageNo;
                            fullDataResult = true;
                        }else {
                            fullDataResult = false;
                        }

                        /* JobInstance Update */
                        var jobinsList = resDoc.getElementsByTagName("JobInstance");
                        var jobinsTable = document.getElementById("jobins_table").getElementsByTagName("tbody")[0];
                        if (fullDataResult) { /* full redraw */
                            var createTrCount = jobinsList.length - (jobinsTable.getElementsByTagName("TR").length);
                            for (var i=0; i<createTrCount; i++) {
                                jobinsTable.appendChild(createJobInsTr());
                            }
                            var jobinsTrList = jobinsTable.getElementsByTagName("TR");

                            for (var i=0; i<jobinsList.length; i++) {
                                updateJobInsTr(jobinsList[i], jobinsTrList[i]);
                            }
                        }else { /* update only */
                            for (var i=0; i<jobinsList.length; i++) {
                                var jobinsid = jobinsList[i].getAttribute("jobInstanceId");
                                var jobinsTr = document.getElementById("jobins_tr_"+jobinsid);
                                if (jobinsTr == null) {
                                    doFullQuery = true; <% /* 현재 화면에서 표현할 수 없는 Job 이면 전체 refresh한다. */ %>
                                }else {
                                    updateJobInsTr(jobinsList[i], jobinsTr);
                                }
                            }
                        }
                        /* Progress Update */
                        var runningJobInsList = resDoc.getElementsByTagName("RunningJobInstance");
                        for (var i=0; i<runningJobInsList.length; i++) {
                            if (document.getElementById("jobins_tr_"+runningJobInsList[i].getAttribute("jobInstanceId")) != null) {
                                updateJobInsTrProgress(runningJobInsList[i]);
                            }
                        }
                        
                        lastModifyTime  = resDoc.getElementsByTagName("CurrentTimeMs")[0].firstChild.nodeValue;
                        lastModifyYMD14 = resDoc.getElementsByTagName("CurrentYMD14")[0].firstChild.nodeValue;
                    }else {
                        document.getElementById('error_bar').innerHTML = "<font color=red><b>Scheduler Connection Error</b></font>";
                    }
<%
        if (useEndFailAlert(request)) {
%>
                    if (endFailJobInsIds.length > 0) {
                        var left = (window.screen.width  / 2) - ((600 / 2) + 10);
                        var top  = (window.screen.height / 2) - ((300 / 2) + 50);
                        window.open('endfail_alert.jsp?jobinsidlist='+endFailJobInsIds.toString(), '_blank', 'width=800,height=300,left='+left+',top='+top+',toolbar=0,menubar=0,scrollbars=1,resizable=1,status=0').focus();
                    }
<%
        }        
%>
                }
                ongoing = 0;
            };
            
            if (doFullQuery) {
                doFullQuery = false; <% /* 한번 Full Query 한 이후에는 변경분만 쿼리한다. */ %>
                httpRequest.open('GET', 'svc_jobins_xml.jsp?<%=nvl(request.getQueryString(), "a=a")%>', true);
            }else {
                httpRequest.open('GET', 'svc_jobins_xml.jsp?<%=nvl(request.getQueryString(), "a=a")%>&lastmodifytimefrom='+lastModifyTime, true);
            }
            httpRequest.send('');
            httpRequest = null;
        }
    }
        
    function getJobStateTrBgColor(jobState) {
        switch(jobState) {
            case "I" : return "#EEEEEE";
            case "W" : return "#EEEEEE";
            case "O" : return "#FFFFFE";
            case "F" : return "#FFBBBB";
            case "R" : return "#FFFF88";
            case "P" : return "#AAAA00";
            case "S" : return "#EEEEEE";
            case "G" : return "#60A0A0";
            case "X" : return "#99EEEE";
        }
        return "#000000";
    }

    function getJobStateTrHandOverBgColor(jobState) {
        switch(jobState) {
            case "I" : return "#BBBBBB";
            case "W" : return "#BBBBBB";
            case "O" : return "#BBBBFF";
            case "F" : return "#FF8888";
            case "R" : return "#FFFF00";
            case "P" : return "#888800";
            case "S" : return "#BBBBBB";
            case "G" : return "#609090";
            case "X" : return "#99FFFF";
        }
        return "#000000";
    }

    function getProgressCurrImage(jobState) {
        if (jobState == "R" || jobState == "P") {
            return "./images/prgs1_running.jpg";
        }else if (jobState == "O") {
            return "./images/prgs1_endok.jpg";
        }else if (jobState == "F") {
            return "./images/prgs1_endfail.jpg";
        }else {
            return "./images/prgs2.jpg";
        }
    }
    
    function getSplitJobInstanceId(jobinsid) {
        return jobinsid.substring(0, jobinsid.length-12) + "<br>" + jobinsid.substr(jobinsid.length-12, 12);
    }

    function getShortDescription(desc) {
        return shortenRight(desc, <%=descShortLimit%>);
    }
    
    function shortenMiddle(str, len) {
        if (str=='') {
            return "";
        }else if (str.length > len) {
            return "<a title='"+str+"'>"+str.substring(0, len/2 - 5)+"..."+str.substring(str.length-len/2)+"</a>";
        }else {
            return str;
        }
    }

    function shortenRight(str, len) {
        if (str=='') {
            return "";
        }else if (str.length > len) {
            return "<a title='"+str+"'>"+str.substring(0, len - 3)+"...</a>";
        }else {
            return str;
        }
    }

    function secondsToTime(s){
        if (s >= 3600) {
            var h  = Math.floor( s / ( 60 * 60 ) );
            s -= h * ( 60 * 60 );
            var m  = Math.floor( s / 60 );
            s -= m * 60;
            return (h==0?"":h+"h ") + (m==0?"":m+"m ") + (s==0?"":s+"s");
        }else if (s >= 60) {
            var m  = Math.floor( s / 60 );
            s -= m * 60;
            return (m==0?"":m+"m ") + (s==0?"":s+"s");
        }else {
            return s+"s";
        }
    }

    function createJobInsTr() {
        var row         = document.createElement("tr");        
        row.align = "center";    


        for (var i=0; i<23; i++) {
            var col = document.createElement("td");
            row.appendChild(col);
        }
        return row;
    }
    
    function updateJobInsTr(jobinsElem, jobinsTR) {
        if (jobinsTR == null) {
            return;
        }

        var jobinsid = jobinsElem.getAttribute("jobInstanceId");
        var jobState = jobinsElem.getAttribute("jobState");
        var jobType  = jobinsElem.getAttribute("jobType");

        if (jobState == "F") {
            var oldJobState = document.getElementById("jobstate_"+jobinsid);
            if (oldJobState != null && oldJobState.value != "F") {
                endFailJobInsIds.push(jobinsid);
            }
        }
        
        jobinsTR.id     ="jobins_tr_"+jobinsid;
        /* jobinsTR.bgColor=getJobStateTrBgColor(jobState); */
        jobinsTR.style.backgroundColor  = getJobStateTrBgColor(jobState);
        var tdList = jobinsTR.getElementsByTagName("td");

        if (jobinsElem.getAttribute("seqNo") != null) { /* full update only */
            tdList[0].innerHTML = "<label><input type='checkbox' id='chkjobinsid' name='chkjobinsid' value='"+jobinsid+"'></label>";
            tdList[1].innerHTML = jobinsElem.getAttribute("seqNo");
        }
        tdList[2].innerHTML = jobinsElem.getAttribute("jobGroupId");
        tdList[3].innerHTML = "<b>"+getSplitJobInstanceId(jobinsid)+"</b>";
        var createClickHandler = 
            function(jobinsID) {
                return function() {
                    openJobInstanceWin(jobinsID);
                };
            };
        tdList[3].onclick     = createClickHandler(jobinsid);
        var createMouseoverHandler = 
            function(bgcolor) {
                return function() {
                    this.style.cursor='pointer';
                    this.style.backgroundColor = bgcolor;
                };
            };
        tdList[3].onmouseover = createMouseoverHandler(getJobStateTrHandOverBgColor(jobState));
        var createMouseoutHandler = 
            function(bgcolor) {
                return function() {
                    this.style.backgroundColor = bgcolor;
                };
            };
        tdList[3].onmouseout = createMouseoutHandler(getJobStateTrBgColor(jobState)); 
        tdList[4].align = "left";
        tdList[4].innerHTML  = jobinsElem.getAttribute("appCode")+" "+getShortDescription(jobinsElem.getAttribute("description"));
        tdList[4].style.backgroundColor = getJobStateTrBgColor(jobState);

        tdList[5].innerHTML = "<a href=\"javascript:openExeWin('"+jobinsid+"')\">"+jobinsElem.getAttribute("endOkCount")+"/"+jobinsElem.getAttribute("runCount")+"</a>";
        tdList[6].innerHTML = "<a href=\"javascript:openFileLogWin('"+jobinsid+"', 'joblog')\" title=\"View Job Log\">Log</a>";
        
        if (jobType == "PROC" || jobType == "CBATCH") {
            tdList[6].innerHTML = tdList[6].innerHTML + "<br><a href=\"javascript:openFileLogWin('"+jobinsid+"', 'stdout')\" title=\"View Stdout Log\">Stdout</a>";
            if (<%=useSubLogForProcJobType%>) {
            	tdList[6].innerHTML = tdList[6].innerHTML + ", <a href=\"javascript:openFileLogWin('"+jobinsid+"', 'sublog')\" title=\"View Second Log\">Sub</a>";
	        }
        }
        
        tdList[7].innerHTML = 
            "<input type=hidden id=jobstate_"+jobinsid+" value="+jobState+">"+
            "<a title='"+jobinsElem.getAttribute("jobStateReason")+"'><b><font class='jobstate_font_"+jobState+"'>"+jobinsElem.getAttribute("jobStateText")+"</font></b></a>";

        if (jobinsElem.getAttribute("lockedBy") != '') {
            tdList[8].innerHTML = "▼";
        }else {
            tdList[8].innerHTML = "";
        }

        tdList[9].innerHTML = jobinsElem.getAttribute("timeFrom");

        if (jobinsElem.getAttribute("repeatYN") == "Y") {
            if (jobinsElem.getAttribute("repeatIntvalGb") == "EXACT") {
                tdList[10].innerHTML = shortenRight(jobinsElem.getAttribute("repeatExactExp"), 15);
            }else {
                tdList[10].innerHTML = secondsToTime(jobinsElem.getAttribute("repeatIntval"));
            }
        }else {
            tdList[10].innerHTML = "-";
        }

        if (jobinsElem.getAttribute("confirmNeedYN") == "Y") {
            if (jobinsElem.getAttribute("confirmed") == '') {
                tdList[11].innerHTML = "<font color=red>-/<b>Y</b></font>";
            }else {
                tdList[11].innerHTML = "<font color=blue><b>０</b>/<b>Y</b></font>";
            }
        }else {
            tdList[11].innerHTML = "";
        }

        tdList[12].innerHTML = jobinsElem.getAttribute("jobTypeText");
        tdList[13].innerHTML = jobinsElem.getAttribute("agentNode").replace("/", "<br>");
        tdList[14].innerHTML = "<b title='"+jobinsElem.getAttribute("componentNameFull")+"'>"+jobinsElem.getAttribute("componentName")+"</b>";
        tdList[18].innerHTML = jobinsElem.getAttribute("lastStartTime");
        tdList[19].innerHTML = jobinsElem.getAttribute("lastEndTime");
        tdList[20].innerHTML = jobinsElem.getAttribute("runningElapTime");
        tdList[20].noWrap = 1;
        tdList[21].innerHTML = "<b>"+jobinsElem.getAttribute("procDate")+"</B>";
        tdList[22].innerHTML = jobinsElem.getAttribute("lastModifyTime");
        
        /* progress */
        tdList[15].innerHTML = jobinsElem.getAttribute("progressCurr")+"/"+jobinsElem.getAttribute("progressTotal");

        /* progress percentage */
        var percentage = 0;
        if (jobinsElem.getAttribute("progressTotal") == 0) {
            percentage = "";
        }else {
            percentage = Math.ceil(jobinsElem.getAttribute("progressCurr") * 100 / jobinsElem.getAttribute("progressTotal"));
        }
        tdList[16].innerHTML = percentage;

        /* progres bar image */
        var jobStatePrgsImg  = getProgressCurrImage(jobState);
        var percentageWidth  = (percentage == "" ? 0 : percentage / 4)
        var percentageWidth2 = 25 - percentageWidth;
        tdList[17].innerHTML = 
            "<img id='prgs_img1_"+jobinsid+"' src='"+jobStatePrgsImg+"' height=10 width="+percentageWidth +"/>"+
            "<img id='prgs_img2_"+jobinsid+"' src='./images/prgs2.jpg'  height=10 width="+percentageWidth2+"/>";
    }

    function updateJobInsTrProgress(runningJobElem) {
        var jobinsid = runningJobElem.getAttribute("jobInstanceId");

        var jobinsTR = document.getElementById("jobins_tr_"+jobinsid);
        var tdList = jobinsTR.getElementsByTagName("td");
        
        tdList[20].innerHTML = runningJobElem.getAttribute("runningElapTime");
        
        /* progress */
        tdList[15].innerHTML = runningJobElem.getAttribute("progressCurr")+"/"+runningJobElem.getAttribute("progressTotal");

        /* progress percentage */
        var percentage = 0;
        if (runningJobElem.getAttribute("progressTotal") == 0) {
            percentage = "";
        }else {
            percentage = Math.ceil(runningJobElem.getAttribute("progressCurr") * 100 / runningJobElem.getAttribute("progressTotal"));
        }
        tdList[16].innerHTML = percentage;

        /* progres bar image */
        var jobStatePrgsImg  = getProgressCurrImage("R");
        var percentageWidth  = (percentage == "" ? 0 : percentage / 4)
        var percentageWidth2 = 25 - percentageWidth;

        document.getElementById("prgs_img1_"+jobinsid).width = percentageWidth;
        document.getElementById("prgs_img2_"+jobinsid).width = percentageWidth2;
    }

    pollServer();
    if (document.form1.autoreload_interval.value != '0') {
        setInterval('pollServer()', parseInt(document.form1.autoreload_interval.value) * 1000);
    }
</script> 
<%
    }
%>
</div>
</div>
<jsp:include page="bottom.jsp" flush="true"/>
</html>
