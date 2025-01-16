<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
    String printActivator(String activator) {
        if (activator==null) {
            return "";
        }else if (activator.indexOf(":") < 0 && activator.indexOf("/") < 0) { // trigger 로 인해 인스턴스 생성됨. link 도 출력 
            try {
                return "<A href=\"javascript:openJobInstanceWin('"+activator.substring(0, activator.length()-6)+"');\">"+activator+"</A>";
            }catch(Exception e) {
                return activator;
            }
        }else {
            return activator;
        }
    }

    String getRealDateTimeFor25H(String time, String activationDateTime) {
        if (Util.isBlank(time)) return "";
        
        String activationDate = activationDateTime.substring(0,8);
        String activationTime = activationDateTime.substring(8,12);
        
        if (time.compareTo(activationTime) <= 0) {
            return toDatetimeString(activationDateTime, false) + " (<a title='Activation Time'><b>AT</b></a>)";
        }
        
        int timeInt = Integer.parseInt(time);
        
        int    days   = timeInt / 2400;
        String hhmmss = String.format("%04d00", timeInt - 2400 * days);
        
           long realdate = Util.parseYYYYMMDDHHMMSS(activationDate + hhmmss ) + days * 86400000;
           return toDatetimeString(realdate, false);
    }
%>
<%
    String jobinstanceid = request.getParameter("jobinstanceid");

    ControllerAdminLocal admin = getControllerAdmin();
    JobInstance jobins = admin.getJobInstance(jobinstanceid);
    if (jobins==null) {
        throw new SchedulerException("Job Instace ID ["+jobinstanceid+"] Not Found");
    }
    
    /* 담당자 를 알아내기 위해 JobDefinition 를 조회함. */ 
    JobDefinition jobdef = admin.getJobDefinition(jobins.getJobId());
    String owner = jobdef==null ? "N/A" : jobdef.getOwner();
    
    String progressString          = null;
    int    progressPercentage      = 0;
    try {
        if (!Util.isBlank(jobins.getLastJobExeId())) {
            long[] progress = admin.getJobProgress(jobins.getLastJobExeId());
            if (progress != null) {
                progressString          = toProgressString(progress);
                progressPercentage      = progress[0]==0 ? 0 : (int)(progress[1] * 100 / progress[0]);
            }
        }
    }catch(Exception e) {
        progressString = e.getMessage();
    }
    
    boolean agentDown = 
        (JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) && 
        !"OK".equals(admin.getAgentCheck(jobins.getLastAgentNode()));
    
    AgentInfo agentInfo = null;
    AgentInfo agentInfo2 = null;
    
    if(!Util.isBlank(jobins.getAgentNodeSlave())){
        
        agentInfo = admin.getAgentInfo(jobins.getAgentNodeMaster());
        agentInfo2 = admin.getAgentInfo(jobins.getAgentNodeSlave());    
        
    }else{
        agentInfo = admin.getAgentInfo(jobins.getAgentNodeMaster());    
    }
    
    
%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<script src="./script/app/include-lib.js"></script>
<jsp:include page="display_msg.jsp" flush="true"/>
<script>
    function openJobDefinitionWin(jobid) {
        window.open("view_jobdef_dtl.jsp?jobid="+jobid, 'jobdef_'+jobid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }

    function openJobGroupWin(jobgroupid) {
        window.open("view_jobgroup_dtl.jsp?jobgroupid="+jobgroupid, 'jobgroup_'+jobgroupid.replace(/-/g, ''), 'width=600,height=400,scrollbars=1').focus();
    }

    function openJobInstanceWin(jobinsid) {
        window.open("view_jobins_dtl.jsp?jobinstanceid="+jobinsid, 'jobins_'+jobinsid.replace(/-/g, ''), 'width=800,height=800,scrollbars=1').focus();
    }

    function openJobInstanceFormWin(jobinsid) {
        if (confirm('<%=Label.get("view_jobins_dtl.edit.lock.confirm.msg")%>')) {
            window.open("form_jobins.jsp?jobinstanceid="+jobinsid, 'jobins_form_'+jobinsid.replace(/-/g, ''), 'width=820,height=800,scrollbars=1').focus();        
        }
    }

    function openThreadDumpWin(jobinstanceid) {
        window.open("view_threaddump.jsp?jobinstanceid="+jobinstanceid, 'thread_'+jobinstanceid.replace(/-/g, ''), 'width=800,height=600,scrollbars=1,resizable=1').focus();
    }

    function openExeWin(jobinstanceid) {
        window.open("view_jobexe.jsp?jobinstanceid="+jobinstanceid, 'jobexe_'+jobinstanceid.replace(/-/g, ''), 'width=1150,height=300,scrollbars=1,resizable=1').focus();
    }

    function openFileLogWin(jobinstanceid, filetype) {
        var logtail = document.getElementById("logtail");
        var tail = "";
        if (logtail.checked) {
            tail = "1";
        }
        
        window.open("view_filelog.jsp?jobinstanceid="+jobinstanceid+"&filetype="+filetype+"&tail="+tail, 'filelog_'+filetype+'_'+jobinstanceid.replace(/-/g, ''), 'width=1000,height=600,scrollbars=1,resizable=1').focus();
    }

    function openControlReportLogWin(jobinstanceid, className) {
        window.open("view_controlReportlog.jsp?jobinstanceid="+jobinstanceid + "&className=" + className, 'ctrlrpt_'+jobinstanceid.replace(/-/g, ''), 'width=1000,height=600,scrollbars=1,resizable=1').focus();
    }       
    
    function openControlReportLogDownWin(jobinstanceid, className) {
        location.href = "view_controlReportlog_down.jsp?jobinstanceid="+jobinstanceid + "&className=" + className;
    }    

    function openRepeatExactPlanWin() {
        window.open("view_repeat_exact_plan.jsp?mode=view&jobinstanceid=<%=jobinstanceid%>", 'exactplanwin', 'width=1100,height=550,scrollbars=1,resizable=1').focus();
    }

    function doChangeLogLevel() {
        if (confirm("<%=Label.get("view_jobins_dtl.change.loglevel.confirm.msg")%>")) {
            document.form1.cmd.value="change_loglevel";
            document.form1.submit();
        }
    }

    function doAction(jobAction, actionDesc, jobInstanceId) {
        if (confirm(actionDesc)) {
            if (jobAction == 'stop') {
                alert('<%=Label.get("view_jobins.stop.job.warning.alert")%>');
            }
            document.form1.cmd.value=jobAction;
            document.form1.submit();
        }
    }
</script>
<!-- <link rel="stylesheet" href="common.css" type="text/css" /> -->
<script src="./script/app/include-lib.js"></script> 
<title><%=Label.get("jobins")%> (<%=jobinstanceid%>)</title>
</head>
<body onload="displayMsg();">
<center>

<form name="form1" action="action_jobins.jsp" method="post">
<input type="hidden" name="cmd" value="">
<input type="hidden" name="jobinstanceid" value="<%=nvl(jobins.getJobInstanceId())%>">
<input type="hidden" name="returnurl" value="view_jobins_dtl.jsp?jobinstanceid=<%=nvl(jobins.getJobInstanceId())%>">

<%-- <table border="2" style = "border-collapse:collapse" bordercolor = "#000000" width="90%">
<tr>
    <td align="center" bgcolor="#FFDDFF" class="logo_title"><%=Label.get("jobins")%> [<%=nvl(jobins.getJobInstanceId())%>]</td> 
</tr>
</table> --%>

    <div class="header-wrap">
        <div class="header">
            <div class="header-title">
                <%=Label.get("jobins")%> [<%=nvl(jobins.getJobInstanceId())%>]
            </div>
            <div class="header-close-button">
                <span><a href="#" onclick="window.close();"><img alt="팝업창 닫기" src="images/pop_close.png"></a></span>
            </div>
        </div>
    </div>

<div id="container2" class="popup-content-wrap">
<%-- <table border="0" width="90%">
<tr><td colspan="100%" align="left"><font size=3><b>■ <%=Label.get("job.program.info")%></b></font></td>
<td colspan="100%" align="right">
    <input type="button" name="" class="Button" value="<%=Label.get("common.btn.refresh")%>" onclick="location.reload();">&nbsp;
    <input type="button" name="" class="Button" value="<%=Label.get("jobins.btn.edit")%>" onclick="openJobInstanceFormWin('<%=jobins.getJobInstanceId()%>');">
</td>
</tr>
</table> --%>

        <div class="gr-btn__wrap">
                <input type="button" class="Button" value="<%=Label.get("common.btn.refresh")%>" onclick="location.reload();"> <input type="button" class="Button" value="<%=Label.get("jobins.btn.edit")%>" onclick="openJobInstanceFormWin('<%=jobins.getJobInstanceId()%>');">
        </div>
        <div class="popup-content-title__wrap">
            <div class="content-title"><%=Label.get("job.program.info")%></div>
        </div>

<!-- <table border="2" style = "border-collapse:collapse" bordercolor = "#000000"  cellpadding="3" width="90%"> -->
        <table class="Table njf-table__typea" >
        <colgroup>
            <col width="22%">
            <col>
        </colgroup>
        <tbody>            
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.jobid")%></th>
                <td><b><a href="javascript:openJobDefinitionWin('<%=jobins.getJobId()%>');"><%=nvl(jobins.getJobId())%></a></b></td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.jobinsid")%></th>
                <td><b><%=nvl(jobins.getJobInstanceId())%></b></td>
            </tr>
            
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.jobgroup")%></th>
                <td><a href="javascript:openJobGroupWin('<%=jobins.getJobGroupId()%>');"><%=nvl(jobins.getJobGroupId())%></a></td>
            </tr>
            
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.owner")%></th>
                <td><%=nvl(owner)%></td>
            </tr>
            
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.desc")%></th>
                <td><font color="blue"><b><%=getAppCode(jobins.getJobId())%><%=conv(jobins.getDescription())%></b></font></td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.jobtype")%></th>
                <td><%=getJobTypeText(jobins.getJobType())+" ("+jobins.getJobType()+")"%></td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.agent")%></th>
                <%if(!Util.isBlank(jobins.getAgentNodeSlave())){ %>
                <td><%=agentInfo == null ? jobins.getAgentNode()+" (N/A)" : jobins.getAgentNode()+" ("+agentInfo.getName()+"/"+agentInfo2.getName()+")"%></td>
                <%}else{ %>
                <td><%=agentInfo == null ? jobins.getAgentNode()+" (N/A)" : jobins.getAgentNode()+" ("+agentInfo.getName()+")"%></td>
                <%} %>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.component")%></th>
                <td><%=conv(jobins.getComponentName())%></td>
            </tr>
            <%
                if (isOperator(request) || isAdmin(request)) {
            %>
            <tr>
                <th><%=Label.get("notify.receiver")%></th>
                <td>
                	<table class="Table njf-table__typea Width-100">
                	<tbody><tr></tr>
            <%
                    /* 통지 수신자 표시 */
                    Map<String, List<JobNotifyReceiver>> notifyReceiversMap = new LinkedHashMap();
                    notifyReceiversMap.put("EO", admin.getJobNotifyReceiverList(jobins.getJobId(), "EO"));
                    notifyReceiversMap.put("EF", admin.getJobNotifyReceiverList(jobins.getJobId(), "EF"));
                    notifyReceiversMap.put("LONGRUN", admin.getJobNotifyReceiverList(jobdef.getJobId(), "LONGRUN"));
                    for (String eventName : notifyReceiversMap.keySet()) {
                        int receiverIdx=0;
                        List<JobNotifyReceiver> receiverList = notifyReceiversMap.get(eventName);
                        Collections.sort(receiverList, getComparator("getName", true));
                        for (JobNotifyReceiver receiver : receiverList) {
                            receiverIdx++;
            %>
                        <tr>
            <%
                        if (receiverIdx==1) {
            %>
                            <th rowspan="<%=receiverList.size()%>"><%="EO".equals(eventName) ? "End OK" : "EF".equals(eventName) ? "End Fail" : "LONGRUN".equals(eventName) ? "LONG RUN" : ""%></th>
            <%
                        }
            %>
                            <td><b><%=receiver.getName()%></b></td>
							<td><%=printJobNotifyReceiveInfo(receiver) %></td>
						</tr>
            <%
                        }
                    }
            %>
					</tbody>
                    </table>
                </td>
            </tr>
            <%
                }
            %>
        </tbody>
        </table>

<%-- <table border="0" width="90%">
<tr><td colspan="100%" align="left"><font size=3><b>■ <%=Label.get("jobins.run.state.info")%></b></font></td></tr>
</table> --%>

        <div class="popup-content-title__wrap">
            <div class="content-title"><%=Label.get("jobins.run.state.info")%></div>
        </div>

<!-- <table border="2" style = "border-collapse:collapse;" bordercolor = "#000000"  cellpadding="3" width="90%"> -->
        <table class="Table njf-table__typea" >
        <colgroup>
            <col width="22%">
            <col>
        </colgroup>
        <tbody>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("jobins.run.current.state")%></th>
                <td>
                    <table class="Table non-border"><tr>
                        <td style="text-align:left"   width="25%"><b><font color="<%=getStateColor(jobins.getJobState())%>"><%=nvl(jobins.getJobStateText())%><%=agentDown?"<br>(Agent Fail)":"" %></font></b></td>
                        <td style="text-align:center" width="25%"><b><%= !Util.isBlank(jobins.getLastAgentNode()) ? "("+jobins.getLastAgentNode()+")" : "" %></b></td>
                        <td style="text-align:right" width="50%" id="current_time"></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("common.jobctl")%></th>
                <td bgcolor="#FFFF99">
            <%
                String[] actionList = JobControlActionHelper.getPossibleActionList(jobins.getJobState(), jobins.getJobType());
                for (String action : actionList) {
            %>
                    <input type="button" class="Button" value="<%=Label.get("jobctl.action.name."+action)%>" title="<%=action %>"
                           onclick="doAction('<%=action%>', '<%=Label.get("jobctl.action.desc."+action, jobins.getJobInstanceId())%>', '<%=jobins.getJobInstanceId()%>');">
            <%
                }

                /* Thread 버튼 표시. JBATCH|POJO 이면서 실행중일때는 스레드 조회 가능함. */
                if ((JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) &&
                    (JobType.JOB_TYPE_JBATCH.equals(jobins.getJobType()) || 
                     JobType.JOB_TYPE_POJO.equals(jobins.getJobType()))) { 
            %>
                    <input type="button" class="Button"  value="Thread" onclick="openThreadDumpWin('<%=jobins.getJobInstanceId()%>');">
            <%
                }
            %>
                        </td>
            
                </td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("jobins.state.cause")%></th>
                <td><b><font color="blue"><%=conv(jobins.getJobStateReason())%></font></b></td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("jobins.progress.status")%></th>
                <td align=left>
                    <table class="Table non-border"><tr>
                        <td align="left" id="prgs" width="50%"><%="<b>"+nvl(progressString) + " (" + progressPercentage +"%)</b>"%></td>
                        <td align="right" ><img id="prgs_img1" src="./images/prgs2.jpg" height="10"><img id="prgs_img2" src="./images/prgs2.jpg" height="10"></td>
                    </tr></table></td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("jobins.lock.yn")%></th>
                <td bgcolor="<%=Util.isBlank(jobins.getLockedBy()) ? "#FFFFFF" : "#FFCCAA"%>">
                    <span style="float:left;">
                        <%=Util.isBlank(jobins.getLockedBy()) ?"<B><font color=blue>(Not Locked)</font></b>" : "<b><font color=red>(Locked)</font></b> "+jobins.getLockedBy()%>
                    </span>
                    <span style="float:right;">
            <%
                String lockUnlock = Util.isBlank(jobins.getLockedBy()) ? "lock" : "unlock";
            %>
                        <input type="button" class="Button" value="<%=Label.get("jobctl.action.name."+lockUnlock)%>"  
                               onclick="doAction('<%=lockUnlock%>', '&nbsp;<%=Label.get("jobctl.action.desc."+lockUnlock, jobins.getJobInstanceId())%>&nbsp;', '<%=jobins.getJobInstanceId()%>');">
                    </span>
                </td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("jobins.confirm.yn")%></th>
                <td bgcolor="<%="Y".equals(jobins.getConfirmNeedYN()) && Util.isBlank(jobins.getConfirmed()) ? "#FFCCAA" : "#FFFFFF"%>">
                    <span style="float:left;">
                        <%="N".equals(jobins.getConfirmNeedYN()) ? "<b>("+Label.get("jobins.confirm.no.need")+")</b>" : 
                           Util.isBlank(jobins.getConfirmed()) ? 
                            "<B><font color=red>("+Label.get("jobins.not.confirmed")+")</font></b>" : 
                            "<b><font color=blue>("+Label.get("jobins.confirmed")+")</font></b> "+jobins.getConfirmed()%></span>
                    <span style="float:right;">
            <%
                if ("Y".equals(jobins.getConfirmNeedYN()) && Util.isBlank(jobins.getConfirmed())) {
            %>
                    <input type="button" class="Button" value="<%=Label.get("jobctl.action.name.confirm")%>"  
                           onclick="doAction('confirm', '<%=Label.get("jobctl.action.desc.confirm", jobins.getJobInstanceId())%>', '<%=jobins.getJobInstanceId()%>');"><br>
            <%
                }
            %>
                    </span>
                </td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("jobins.activator")%></th>
                <td>[<%=printActivator(jobins.getActivator())%>]&nbsp;&nbsp;&nbsp;<b>[<%=toDatetimeString(nvl(jobins.getActivationTime()), false)%>]</b></td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("common.procdate")%></th>
                <td>[<b>PROC_DATE(<%=Label.get("common.procdate")%>)</b> : <b><font color="blue"><%=jobins.getProcDate()%></font></b>]</td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("jobins.last.exe.time")%></th>
                <td><span style="float:left;">
                    [<%=toDatetimeString(nvl(jobins.getLastStartTime()), false)%>] ~ [<%=toDatetimeString(nvl(jobins.getLastEndTime()), false)%>]
                    </span>
                    <span style="float:right;" id="running_elaptime">
                    </span>
                </td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("jobins.exe.list")%></th>
                <td><span style="float:left;">
                    [<b><%=Label.get("jobins.exe.ok")%> : <%=jobins.getEndOkCount()%></b> / <b><%=Label.get("common.etc.all")%> : <%=jobins.getRunCount()%></b>]
                    </span>
                    <span style="float:right;">
                    <input type="button" class="Button"  value="<%=Label.get("jobins.view.exe.list")%>" onclick="openExeWin('<%=jobins.getJobInstanceId()%>');">
                    </span>
                </td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("common.log")%></th>
                <td><span style="float:left;">
                        <select name="newlevel" class="Select">
                            <option value="">&lt;DEFAULT&gt;</option>
            <%
                List<String> logLevelList = admin.getLogLevelUsingList();
                for(String logLevel : logLevelList ) {
                    out.println(printSelectOption(logLevel, jobins.getLogLevel()));
                }
            %>
                        </select>
                        <input type="button" class="Button"  value="<%=Label.get("common.btn.change.loglevel")%>" onclick="doChangeLogLevel();">
                    </span>
                    <span style="float:right;">
                          <label><input id="logtail" type="checkbox" value="1" name="tail" checked>Tail</label>&nbsp;      
                        <input type="button" class="Button" value="<%=Label.get("common.log")%>" onclick="openFileLogWin('<%=jobins.getJobInstanceId()%>', 'joblog');">
            <%
                if (JobType.JOB_TYPE_PROC.equals(jobins.getJobType()) || JobType.JOB_TYPE_CBATCH.equals(jobins.getJobType())) { /* PROC, CBATCH, JAVARUN 타입인 경우 stdout 조회 버튼 표시 */
            %>
                        <input type="button" class="Button" value="Stdout <%=Label.get("common.log")%>" onclick="openFileLogWin('<%=jobins.getJobInstanceId()%>', 'stdout');">
                        <input type="button" class="Button" value="Sub <%=Label.get("common.log")%>" onclick="openFileLogWin('<%=jobins.getJobInstanceId()%>', 'sublog');">
            <%    
                }
            %>
                    </span>
                </td>
            </tr>
            <%
                String className = getClassNameOnly(jobins);
                if (getClassNameOnly(jobins) != null) { /* 프로그램 명이 NULL 이 아닌 경우 컨트롤 리포트 조회 버튼 표시 */          
            %>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("ctrlreport")%></th>
                <td><span style="float:right;">
                        <input type="button" class="Button" value="<%=Label.get("ctrlreport.view")%>" onclick="openControlReportLogWin('<%=jobins.getJobInstanceId()%>', '<%=className %>');">&nbsp;
                        <input type="button" class="Button" value="<%=Label.get("ctrlreport.down")%>" onclick="openControlReportLogDownWin('<%=jobins.getJobInstanceId()%>', '<%=className %>');">
                    </span>
                </td>
            </tr>
            <%
                }
            %>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.lastmodifytime")%></th>
                <td>[<%=toDatetimeString(DateUtil.getTimestamp(jobins.getLastModifyTime()), false)%>]</td>
            </tr>
        </tbody>
        </table>

<%-- <table border="0" width="90%">
<tr><td colspan="100%" align="left"><font size=3><b>■ <%=Label.get("job.execution.condition")%></b></font></td></tr>
</table> --%>

        <div class="popup-content-title__wrap">
            <div class="content-title"><%=Label.get("job.execution.condition")%></div>
        </div>

<!-- <table border="2" style = "border-collapse:collapse" bordercolor = "#000000"  cellpadding="3" width="90%"> -->
        <table class="Table njf-table__typea" >
        <colgroup>
            <col width="22%">
            <col>
        </colgroup>
        <tbody>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.time")%></th>
                <td><b><font color='<%=jobins.getTimeFrom()!=null ? "#FF0000" : "#000000"%>'>[<%=nvl(jobins.getTimeFrom())%>] ∼ [<%=nvl(jobins.getTimeUntil())%>]</font></b> (<%=getRealDateTimeFor25H(jobins.getTimeFrom(), jobins.getActivationTime()) %>) ~ (<%=getRealDateTimeFor25H(jobins.getTimeUntil(), jobins.getActivationTime()) %>)</td>
            </tr>
            <%
            boolean isExactRepeat    = "EXACT".equals(jobins.getRepeatIntvalGb());
            boolean isStartEndRepeat = "START".equals(jobins.getRepeatIntvalGb()) || "END".equals(jobins.getRepeatIntvalGb());
            %>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.repeat")%></th>
                <td <%="Y".equals(jobins.getRepeatYN()) ? "bgcolor=#ffcccc" : ""%>>
                    <table class="Table non-border">
                        <tr>
                            <td rowspan=3 valign="top"><%=jobins.getRepeatYN()%><br></td>
                            <td><b><%=printCancelLine("["+Label.get("job.repeat.intval")+"]", !isStartEndRepeat)%></b></td>
                            <td><%=printCancelLine(jobins.getRepeatIntval()+Label.get("common.second")+" ("+secondsToTime(jobins.getRepeatIntval())+")", !isStartEndRepeat)%></td>
                            <td><b>[<%=Label.get("job.repeat.intval.gb")%>]</b></td>
                            <td><%=nvl(jobins.getRepeatIntvalGb())%></td>
                        </tr>
                        <tr>
                            <td><b>[<%=Label.get("job.repeat.if.error")%>]</b></td>
                            <td><%=nvl(jobins.getRepeatIfError())%></td>
                            <td><b>[<%=Label.get("job.repeat.maxok")%>]</b></td>
                            <td><%=jobins.getRepeatMaxOk()%></td>
                        </tr>
                        <tr>
                            <td><b><%=printCancelLine("["+Label.get("job.repeat.exact.exp")+"]", !isExactRepeat)%></b></td>
                            <td colspan=3><%=printCancelLine(nvl(jobins.getRepeatExactExp()), !isExactRepeat)%>
                <% if (isExactRepeat) { %>
                <input type="button" value="Plan" class="Button" onclick="openRepeatExactPlanWin();">
                <% } %>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.confirm.need.yn")%></th>
                <td><%=jobins.getConfirmNeedYN()%></td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.parallel.group")%></th>
                <td><%=nvl(jobins.getParallelGroup())%></td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.trigger")%></th>
                <td>
                    <table id="triggerjob_table" class="Table non-border">
                        <tr class="tabletitle">
                            <td style="background-color:#f1f1f4;">#</td>
                            <td style="background-color:#f1f1f4;"><%=Label.get("job.trigger.okfail")%></td>
                            <td style="background-color:#f1f1f4;"><%=Label.get("job.trigger.id")%></td>
                            <td style="background-color:#f1f1f4;"><%=Label.get("job.trigger.count")%></td>
                        </tr>
            <%
                int i=0;
	            for (PostJobTrigger triggerJob : jobins.getTriggerList()) {
					i++;
            
					String retVal = "";
					if("RETVAL".equals(triggerJob.getWhen())) {
						retVal = "[" + nvl(triggerJob.getCheckValue1()) + ":" + nvl(triggerJob.getCheckValue2()) + "]";
					}
            %>
                        <tr align="center">
                            <td><%=i%></td>
                            <td><%=nvl(triggerJob.getWhen())%> <%=retVal%></td>
                            <td><a href="javascript:openJobDefinitionWin('<%=nvl(triggerJob.getTriggerJobId())%>');"><b><%=nvl(triggerJob.getTriggerJobId())%></b></a></td>
                            <td><%=nvl(triggerJob.getJobInstanceCount())%></td>
                        </tr>
            <%
                }
            %>
                    </table>
                </td>
            </tr>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.prejob")%></th>
                <td>
                    <table id="prejob_table" class="Table non-border">
                        <tr class="tabletitle">
                            <td style="background-color:#f1f1f4;">#</td>
                            <td style="background-color:#f1f1f4;"><%=Label.get("job.prejob.id")%></td>
                            <td style="background-color:#f1f1f4;"><%=Label.get("job.prejob.today.ins")%></td>
                            <td style="background-color:#f1f1f4;"><%=Label.get("job.prejob.okfail")%></td>
                            <td style="background-color:#f1f1f4;"><%=Label.get("job.prejob.andor")%></td>
                        </tr>
            <%
                i=0;
                for (PreJobCondition preJob : jobins.getPreJobConditions()) {
                    i++;
            
                    List<JobInstance> preJobInsList = admin.getJobInstanceList(
                        "WHERE JOB_INSTANCE_ID LIKE '"+preJob.getPreJobId()+jobins.getProcDate()+"%'", 
                        "ORDER BY JOB_INSTANCE_ID ", false);
            %>
                        <tr align="center">
                            <td><%=i%></td>
                            <td><a href="javascript:openJobDefinitionWin('<%=nvl(preJob.getPreJobId())%>');"><b><%=nvl(preJob.getPreJobId())%></b></a></td>
                            <td><table width="100%">
            <%      for (JobInstance preJobIns : preJobInsList) {  %>
                                <tr align=center>
                                    <td><a href="javascript:openJobInstanceWin('<%=preJobIns.getJobInstanceId()%>');" title="<%=preJobIns.getDescription()%>"><b><%=preJobIns.getJobInstanceId().substring(preJobIns.getJobInstanceId().length()-4)%></b></a></td>
                                    <td><b><font color='<%=getStateColor(preJobIns.getJobState())%>'><%=preJobIns.getJobStateText()%></font></b></td>
                                </tr>
            <%      }  %>
                                </table>
                            </td>
                            <td><%=nvl(preJob.getOkFailText())%></td>
                            <td><%=nvl(preJob.getAndOr())%></td>
                        </tr>
            <%
                }
            %>
                    </table>
                </td>
            </tr>
        </tbody>
        </table>

<%-- <table border="0" width="90%">
<tr><td colspan="100%" align="left"><font size=3><b>■ <%=Label.get("job.param")%></b></font></td></tr>
</table> --%>

        <div class="popup-content-title__wrap">
            <div class="content-title"><%=Label.get("job.param")%></div>
        </div>

        <table class="Table njf-table__typea Margin-bottom-10" >
        <colgroup>
            <col width="22%">
            <col>
        </colgroup>
        <tbody>
            <tr>
                <th><span class="ico_bull"></span><%=Label.get("job.param")%></th>
                <td>
                    <table id="param_table" class="Table non-border">
                        <tr class="tabletitle">
                            <td width="40%" bgcolor = "#f1f1f4"><%=Label.get("job.param.name")%></td>
                            <td bgcolor = "#f1f1f4"><%=Label.get("job.param.value")%></td>
                        </tr>
            <%
                i=0;
                for (Map.Entry<String, String> param : jobins.getInParameters().entrySet()) {
                    i++;
            %>
                        <tr align="center" id="param_<%=i%>">
                            <td><%=conv(param.getKey())%></td>
                            <td><%=conv(param.getValue())%></td>
                        </tr>
            <%
                }
            %>
                    </table>
                </td>
            </tr>
        </tbody>
        </table>

        <table class="Width-100 Margin-bottom-10">
            <tr>
                <td class="Text-center">
                    <input class="Button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()" style="width:80px; height:35px">
                </td>
            </tr>
        </table>
</div>
</form>
<br><br>
</div>
</center>
</body>
<script>
    var beforeStatus = '<%=jobins.getJobState()%>';
    var ongoing      = 0;

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
                        var resText     = this.responseText;
                        var statusPrgs  = resText.split("^");
                        var jobinsid    = statusPrgs[1];
                        var jobState    = statusPrgs[2];
                        var prgsTotal   = statusPrgs[3];
                        var prgsCurr    = statusPrgs[4];
                        var currentDt   = statusPrgs[5];
                        var runningTime = statusPrgs[6];

                        if (beforeStatus != jobState) { /* refresh page if job status changed */
                            location.reload();
                            return;
                        }
                        
                        /* progress percentage part */
                        var prgsTd = document.getElementById("prgs");
                        var percentage = 0;
                        if (prgsTotal == 0) {
                            percentage = "";
                        }else {
                            percentage = Math.ceil(prgsCurr * 100 / prgsTotal);
                        }
                        prgsTd.innerHTML = "<b>"+prgsCurr+"/"+prgsTotal+" ("+percentage+"%)</b>";
                        
                        /* progress image part */
                        var prgsImg1 = document.getElementById("prgs_img1");
                        var prgsImg2 = document.getElementById("prgs_img2");

                        prgsImg1.width = percentage * 2;
                        prgsImg2.width = 200 - percentage * 2;

                        if (jobState == 'O') {
                            prgsImg1.src = "./images/prgs1_endok.jpg";
                        }else if (jobState == 'F') {
                            prgsImg1.src = "./images/prgs1_endfail.jpg";
                        }else if (jobState == 'R' || jobState == "P") {
                            prgsImg1.src = "./images/prgs1_running.jpg";
                        }
                        
                        /* current time */
                        var currentTimeTd = document.getElementById("current_time");
                        currentTimeTd.innerHTML = "(<%=Label.get("common.current.time")%> : <b>"+currentDt+"</b>)";

                        /* running elap time */
                        var runningElaptimeTd = document.getElementById("running_elaptime");
                        runningElaptimeTd.innerHTML = "[<b>"+runningTime+"</b>]";
                    }
                }
            };
            httpRequest.open('GET', 'svc_jobins_prgs.jsp?jobinstanceid=<%=jobinstanceid%>', true);
            httpRequest.send('');
            httpRequest = null;
        }
        ongoing = 0;
    }
    
    pollServer();
<%
    if (JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {
%>  
    setInterval('pollServer()',2000);
<%
    }else { /* if not running, setInterval to 10 sec. In order to reduce server load. */
%>
    setInterval('pollServer()',10000);
<%
    }
%>
</script> 
</html>
