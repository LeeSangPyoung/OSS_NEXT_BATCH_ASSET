<%@page language="java" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
    /* Job 등록 정보 조회 기본. table 방식 조회 화면 */
    String returnUrl      = null; 
    String returnUrlPlain = null;
    if (request.getQueryString()==null) {
        returnUrl = request.getRequestURI();
        returnUrlPlain = request.getRequestURI();
    }else {
        returnUrl = request.getRequestURI()+"?"+URLEncoder.encode(request.getQueryString());
        returnUrlPlain = request.getRequestURI()+"?"+request.getQueryString();
    }
%>
<jsp:include page="common_query_jobdef.jsp" />
<%
    ControllerAdminLocal admin = getControllerAdmin();
    List<String> agentIdList = admin.getAgentIdList();
    
    String orderby        = nvl(request.getParameter("orderby"),  "jobId");
    String orderdir       = nvl(request.getParameter("orderdir"), "ASC");     // ASC, DESC
    
    List<JobDefinition> jobdefList = (List<JobDefinition>)request.getAttribute("jobdef_query_result");
%>
<script>
	$a.page(function() {
	    // 초기화 함수
	    this.init = function(id, param) {
	    	$("[id^='tableList']").css({'table-layout':'auto'});
	    }
	});

    function openJobDefinitionWin(jobid) {
        window.open("view_jobdef_dtl.jsp?jobid="+jobid, "jobdef_"+jobid.replace(/-/g, ''), 'width=820,height=800,scrollbars=1').focus();
    }

    function checkAll() {
        var chk = document.form2.chkjobid;
        var v = document.form2.chkall.checked;
        
        if (typeof chk =="undefined") return false;
        
        if (chk.length == null) { 
            chk.checked = v ;
        }else {
            for (i=0; i<chk.length; i++ ) {
                chk[i].checked = v ;
            }
        }
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
    
    function getCheckedJobIdList() {
        var chk = document.form2.chkjobid;
        var jobidlist = '';
        if (chk.length == null) {
            if (chk.checked) {
                jobidlist = jobidlist + chk.value ;
            }
        }else { 
            var checkedCount = 0;
            for (i=0; i<chk.length; i++ ) {
                if (chk[i].checked) {
                    checkedCount++;
                    if (checkedCount <= 15) {
                        jobidlist = jobidlist + '\r['+chk[i].value+']';
                    }
                }
            }

            if (checkedCount > 15) {
                jobidlist = jobidlist + "...";
            }

            if (checkedCount > 0) {
	            jobidlist = jobidlist + "\r(Total : "+checkedCount+")";
            }
        }
        return jobidlist;
    }

    function requestJobDefDelete() {
    	if (getCheckedCount(document.form2.chkjobid) == 0) {
    		alert("Not checked");
    	}else {
	        document.form2.cmd.value="request_delete_multi";
	        var jobidlist = getCheckedJobIdList();
	        if (jobidlist.length > 0) {
	            if (confirm("<%=Label.get("common.remove.confirm.msg")%>"+jobidlist)) {
	                document.form2.submit();
	            }
	        }
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
        document.form1.action="view_jobdef.jsp";
        document.form1.submit();
    }
<%
    if (isAdmin(request) || isOperator(request)) { /* 아래 기능들은 admin,operator 만 사용함 */
%>
    function activateMultiJob() {
    	if (getCheckedCount(document.form2.chkjobid) == 0) {
    		alert("Not checked");
    	}else {
	        document.form2.cmd.value="activate_multi";
	        var jobidlist = getCheckedJobIdList();
	        if (jobidlist.length > 0) {
	            if (confirm("<%=Label.get("jobdef.activate.confirm.msg")%>\n(PROC_DATE : "+document.form2.activate_multi_procdate.value+") "+jobidlist)) {
	                document.form2.submit();
	            }
	        }
    	}
    }
    
    function activateAndRunMultiJob() {
    	if (getCheckedCount(document.form2.chkjobid) == 0) {
    		alert("Not checked");
    	}else {
	        document.form2.cmd.value="activate_run_multi";
	        var jobidlist = getCheckedJobIdList();
	        if (jobidlist.length > 0) {
	            if (confirm("<%=Label.get("jobdef.activaterun.confirm.msg")%>\n(PROC_DATE : "+document.form2.activate_multi_procdate.value+") "+jobidlist)) {
	                document.form2.submit();
	            }
	        }
    	}
    }

    function migrateJobTo() {
    	if (getCheckedCount(document.form2.chkjobid) == 0) {
    		alert("Not checked");
    	}else {
	        if (document.form2.mig_to_agent.value == '') {
	            alert("<%=Label.get("view_jobdef.agent.required.for.migration")%>");
	            return;
	        }
	    
	        document.form2.cmd.value="migrate";
	        var jobidlist = getCheckedJobIdList();
	        if (jobidlist.length > 0) {
	        	var message = "<%=Label.get("view_jobdef.migration.confirm.msg")%>\n(To : "+document.form2.mig_to_server.value+" / "+document.form2.mig_to_agent.value;
				if (document.form2.mig_to_agent2.value != '') 
					message += "," + document.form2.mig_to_agent2.value;
				message += ") "+jobidlist;
	            if (confirm(message)) {
	                document.form2.submit();
	            }
	        }
    	}
    }
<%
    }
%>
</script>
	<form name="form2" method="post" action="action_jobdef.jsp" style="margin-top:0">
	<input name="cmd" type="hidden" value="">
	<input name="returnurl" type="hidden" value="<%=returnUrlPlain%>">
	<table class="Table Width-100 Margin-bottom-10" id="tableList">	
<!-- 	<colgroup>
		<col style="width:3%"/>
		<col style="width:3%"/>
		<col style="width:5%"/>
		<col style="width:5%"/>
		<col style="width:5%"/>
		<col style="width:7%"/>
		<col style="width:4%"/>
		<col style="width:3%"/>
		<col style="width:3%"/>
		<col style="width:3%"/>
		<col style="width:5%"/>
		<col style="width:5%"/>
		<col style="width:5%"/>
		<col style="width:5%"/>
		<col style="width:5%"/>
		<col style="width:5%"/>
		<col style="width:5%"/>
		<col style="width:7%"/>
		<col style="width:7%"/>
		<col style="width:5%"/>
		<col style="width:7%"/>
	</colgroup> -->
	<thead>
		<tr>
			<th style="width:3%; padding:2px;"><input class="Checkbox" type="checkbox" id="chkall" name="chkall" onclick="checkAll()"></th>
			<th style="padding:2px;"></th>
			<th style="padding:2px;"><a href="javascript:orderby('jobGroupId');"><%=Label.get("job.jobgroup")%><%=printSortMark(orderby, orderdir, "jobGroupId")%></a></th>
			<th style="padding:2px;"><a href="javascript:orderby('jobId');"><%=Label.get("job.jobid")%><%=printSortMark(orderby, orderdir, "jobId")%></a></th>
			<th style="padding:2px;"><a href="javascript:orderby('owner');"><%=Label.get("job.owner")%><%=printSortMark(orderby, orderdir, "owner")%></a></th>
			<th style="padding:2px;"><a href="javascript:orderby('description');"><%=Label.get("job.desc")%><%=printSortMark(orderby, orderdir, "description")%></a></th>
			<th style="padding:2px;"><a href="javascript:orderby('auto');">Auto<%=printSortMark(orderby, orderdir, "auto")%></a></th>
			<th style="padding:2px;"><%=Label.get("job.months")%></th>
			<th style="padding:2px;"><%=Label.get("job.days")%></th>
			<th style="padding:2px;"><%=Label.get("job.weekday")%></th>
			<th style="padding:2px;"><a href="javascript:orderby('timeFrom');"><%=Label.get("job.time.from")%><%=printSortMark(orderby, orderdir, "timeFrom")%></a></th>
			<th style="padding:2px;"><%=Label.get("job.time.until")%></th>
			<th style="padding:2px;"><%=Label.get("job.repeat")%></th>
			<th style="padding:2px;"><%=Label.get("job.confirm")%></th>
			<th style="padding:2px;"><a href="javascript:orderby('parallelGroup');"><%=Label.get("job.parallel.group.short")%><%=printSortMark(orderby, orderdir, "parallelGroup")%></a></th>
			<th style="padding:2px;"><a href="javascript:orderby('jobType');"><%=Label.get("job.jobtype.short")%><%=printSortMark(orderby, orderdir, "jobType")%></a></th>
			<th style="padding:2px;"><a href="javascript:orderby('agentNode');"><%=Label.get("common.server")%><%=printSortMark(orderby, orderdir, "agentNode")%></a></th>
			<th style="padding:2px;"><a href="javascript:orderby('componentName');"><%=Label.get("job.component")%><%=printSortMark(orderby, orderdir, "componentName")%></a></th>
			<th style="padding:2px;"><%=Label.get("common.log")%></th>
			<th style="padding:2px;"><a href="javascript:orderby('lastModifyTime');"><%=Label.get("job.lastmodifytime.short")%><%=printSortMark(orderby, orderdir, "lastModifyTime")%></a></th>
		</tr>
	</thead>
	<tbody>
	<%
	    boolean colorFlip=true;
	    int i=0;
	    for (JobDefinition jobdef : jobdefList) {
	        i++;
	        colorFlip = !colorFlip;
	%>
	
		<tr id="TR_<%=jobdef.getJobId()%>" <%=printTrFlip(colorFlip)%> >
			<td style="padding:0px;"><input type="checkbox" class="Checkbox" id="chkjobid" name="chkjobid" value="<%=jobdef.getJobId()%>"></td>
			<td><%=i%></td>
			<td><%=nvl(jobdef.getJobGroupId())%></td>
			<td onMouseOver="this.style.cursor='pointer';this.style.backgroundColor='#AAAAFF';" 
			    onMouseOut =<%=(i%2==0) ? "this.style.backgroundColor='#f7f7f7';" : "this.style.backgroundColor='#ffffff';" %>
			    onclick="openJobDefinitionWin('<%=jobdef.getJobId()%>');">
			    <b><%=nvl(jobdef.getJobId())%></b>
			</td>
			<td><%=conv(jobdef.getOwner())%></td>
			<td class="Text-left">
				<b><%=getAppCode(jobdef.getJobId())%></b> <%=getShortDescription(jobdef.getDescription())%>
			</td>
			<td><%=JobUtil.isScheduledForDailyActivation(jobdef) ? "O" : "-"%></td>
			<td><%=nvl(jobdef.getMonths())%></td>
			<td><%=
			"NUMBER".equals(jobdef.getDayOfMonthScheduleType()) ? "N:"+nvl(jobdef.getDaysInMonth()) : 
			"CALENDAR".equals(jobdef.getDayOfMonthScheduleType()) ? "C:"+nvl(jobdef.getCalendarId()) : "N/A" %></td>
			<td title="<%=nvl(jobdef.getDaysOfWeek())%>"><%=Util.isBlank(jobdef.getDaysOfWeek()) ? "" : "O"%></td>
			<td><%=nvl(jobdef.getTimeFrom())%></td>
			<td><%=nvl(jobdef.getTimeUntil())%></td>
			<td><%="Y".equals(jobdef.getRepeatYN()) ? ("EXACT".equals(jobdef.getRepeatIntvalGb()) ? shortenRight(jobdef.getRepeatExactExp(),15) : secondsToTime(jobdef.getRepeatIntval())) : "-"%></td>
			<td><%=getYNSign(jobdef.getConfirmNeedYN())%></td>
			<td><%=nvl(jobdef.getParallelGroup())%></td>
			<td><%=getJobTypeText(jobdef.getJobType())%></td>
			<td><%=nvl(jobdef.getAgentNode()).replaceAll("\\/", "<br>")%></td>
			<td><%=getShortComponentName(jobdef.getJobType(), jobdef.getComponentName())%></td>
			<td><%=nvl(jobdef.getLogLevel())%></td>
			<td><%=toDatetimeString(DateUtil.getTimestamp(jobdef.getLastModifyTime()), true)%></td>
		</tr>
	<%    
	    }
	%>
	</tbody>
	</table>
	
	<%
	    if (jobdefList.size() > 0) {
	        if (isAdmin(request) || isOperator(request)) {   
	%>
<%-- 
	<table class="Width-100">
		<tr>
		    <td class="Text-right">
		        PROC_DATE : <input type="text" class="Textinput Width-15 Margin-right-10" name="activate_multi_procdate" value="<%=Util.getCurrentYYYYMMDD()%>" maxlength="8">
		        Lock : <input type="checkbox" class="Checkbox" name="activate_multi_lock_yn" value="1" checked>
		        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.activate")%>" onclick="activateMultiJob();">
		        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.activaterun")%>" onclick="activateAndRunMultiJob();" >
		    </td>
		    <td class="Text-center">
		        TO:
		        <select class="Select Width-30 Margin-right-10" name="mig_to_server">
					<%=printMigrateServerList()%>
		        </select>
		        Agent:
		        <select class="Select Width-15" name="mig_to_agent">
					<%=printMigrateAgentList()%>
		        </select>
		        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.migration")%>" onclick="migrateJobTo();">
		    </td>
		<%
		        }
		%>
		    <td class="Text-left">
		        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.req.delete")%>" onclick="requestJobDefDelete();">
		    </td>
		</tr>
	</table> --%>
	<table class="Width-100">
		<tr>
		    <td style="text-align:center">
		        PROC_DATE : <input type="text" class="Textinput Width-10 Margin-right-10" name="activate_multi_procdate" value="<%=Util.getCurrentYYYYMMDD()%>" maxlength="8">
		        Lock : <input type="checkbox" class="Checkbox" name="activate_multi_lock_yn" value="1" checked>
		        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.activate")%>" onclick="activateMultiJob();">
		        <input type="button" class="Button Margin-right-20" value="<%=Label.get("jobdef.btn.activaterun")%>" onclick="activateAndRunMultiJob();" >
		        
		        TO :
		        <select class="Select Width-10 Margin-right-10" name="mig_to_server" style="font-size:11px;">
					<%=printMigrateServerList()%>
		        </select>
		        <%=Label.get("job.agent.master")%> : 
		        <select class="Select Width-5" name="mig_to_agent">
					<%=printMigrateAgentList()%>
		        </select>
				<%=Label.get("job.agent.slave")%>(<%=Label.get("job.agent.slave.hint")%>) :
				<select class="Select Width-5" name="mig_to_agent2">
					<%=printMigrateAgentList()%>
		        </select>
		        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.migration")%>" onclick="migrateJobTo();">
				<%
				        }
				%>		 
				<input type="button" class="Button" value="<%=Label.get("jobdef.btn.req.delete")%>"  onclick="requestJobDefDelete();">		       		        
		    </td>
		</tr>
	</table>		
	<%
	    }else {
	%>
	
	<b><font color="blue"><%=Label.get("common.input.search.condition")%></font></b>
	<br><br>
	<%=Label.get("viewfilter.guide.msg")%> <A href="javascript:openViewFilterMgrWin();">[<%=Label.get("viewfilter.add")%>]</a>
	<%
	    }
	%>
	</form>
