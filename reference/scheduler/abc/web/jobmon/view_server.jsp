<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<jsp:include page="top.jsp" flush="true"/>
<%!
    String printWarnColor(Map<String, Long> mem) {
	
		int totalPer = (int)(mem.get("HEAP_COMMITTED") * 100l / mem.get("HEAP_MAX"));
		int usedPer  = (int)(mem.get("HEAP_USED")      * 100l / mem.get("HEAP_COMMITTED"));

		if (totalPer >= 80) {
	        if (usedPer > 80) {
	            return "#FF0000";
	        }else if (usedPer > 70) {
	            return "#FF5555";
	        }else if (usedPer > 60) {
	            return "#FFAAAA";
	        }
        }
        return "";
    }
%>
<%
    String cmd         = request.getParameter("cmd");
    String idFor       = request.getParameter("id_for");
    String orderby     = nvl( request.getParameter("orderby"),  "getId");
    String orderdir    = nvl( request.getParameter("orderdir"), "ASC");
	boolean autoReload = Util.isBlank(cmd) ? Util.toBoolean(request.getParameter("autoreload"), true) : false ;
	
%>
<script>
	$a.page(function() {
	    // 초기화 함수
	    this.init = function(id, param) {
	    	$("[id^='tableList']").css({'table-layout':'auto'});
	    }
	});	


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
	
	function checkAll() {
	    var chk = document.form1.chkagentid;
	    var v = document.form1.chkall.checked;
	    
	    if (typeof chk =="undefined") return false;
	    
	    if (chk.length == null) { /* 하나일때 */
	        chk.checked = v ;
	    }else {
	        for (var i=0; i<chk.length; i++ ) {
	            chk[i].checked = v ;
	        }
	    }
	}

	function openEditFormAgentInfo(agentid) {
   		var left = (window.screen.width  / 2) - ((400 / 2) + 10);
   	    var top  = (window.screen.height / 2) - ((400 / 2) + 50);
   		window.open("form_server.jsp?agentid="+agentid, 'form_server_'+agentid.replace(/-/g, ''), 'width=830,height=400,left='+left+',top='+top+',scrollbars=no, resizable=1').focus();
   	}

    function removeAgentInfo() {
		if (getCheckedCount(document.form1.chkagentid) == 0) {
			alert("Not checked");
			return;
		}
	    if (confirm("<%=Label.get("common.remove.confirm.msg")%>")) {
	        document.form1.cmd.value="remove_agent";
	        document.form1.submit();
	    }
    }

    function doCloseOrOpen(obj, agentid) {
        var msg;
        if (obj.value=="false"){
            msg = '<%=Label.get("agent.jobrun.enable.msg")%>';
            document.form1.isclosed.value="false";
        }else if (obj.value=="true"){
            msg = '<%=Label.get("agent.jobrun.disable.msg")%>';
            document.form1.isclosed.value="true";
        }
        if (confirm(msg)) {
            document.form1.cmd.value="close_or_open";
            document.form1.id_for.value=agentid;
            document.form1.submit();
        }
    }

    function openRunningJobExeWin(agentid) {
        window.open("view_server_joblist.jsp?agentid="+agentid, 'sever_joblist_'+agentid, 'width=810,height=500,scrollbars=1').focus();
    }

    function openRunningThreadWin(agentid) {
        window.open("view_server_threadlist.jsp?agentid="+agentid, 'sever_threadlist_'+agentid, 'width=1000,height=400,scrollbars=1').focus();
    }
    
    function openSystemPropertiesWin(agentid) {
        window.open("view_server_sysproperties.jsp?agentid="+agentid, 'sever_prop_'+agentid, 'width=820,height=500,scrollbars=1').focus();
    }

    function openConfigFiles(agentid) {
        window.open("view_server_configfiles.jsp?agentid="+agentid, 'sever_configfiles_'+agentid, 'width=1000,height=500,scrollbars=1').focus();
    }

    function orderby(orderbyCol) {
    	var orderdir;
        if ('<%=orderby%>' == orderbyCol) {
            if ('<%=orderdir%>' == 'ASC') {
                orderdir = 'DESC';
            }else {
                orderdir = 'ASC';
            }
        }else {
            orderdir = 'ASC';
        }
        window.location.href = 'view_server.jsp?orderby='+orderbyCol+'&orderdir='+orderdir;
    }
</script>

<%
    ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));
    
    List<AgentInfo> agentInfoList = admin.getAllAgentInfos();
    Collections.sort(agentInfoList, getComparator(orderby, "ASC".equals(orderdir)));

%>
<center>
<div class="content-wrap">

<div class="content-title__wrap">
	<div class="content-title">서버</div>
</div>

<form name="form1" action="action_server.jsp" method="post">
<table class="Width-100">
<tr>
    <td class="Text-right">
    	<label><%=Label.get("common.btn.auto.refresh")%></label>
    	<input class="Checkbox" type="checkbox" name="autoreload" value="1" <%=autoReload?"checked":""%>>
    	<label style="font-size:12px" class="Font Style Margin-left-10"><%=toDatetimeString(new java.util.Date(), false) %></label>
    </td>
</tr>
</table>
<input type="hidden" name="cmd" value="">
<input type="hidden" name="isclosed" value="">
<input type="hidden" name="id_for" value="">

<table id="tableList" class="Table Width-100 Margin-bottom-10" style="word-break: break-all;">
<thead>
<tr>
    <th style="width:2%; padding:1px;"><input class="Checkbox" type="checkbox" id="chkall" name="chkall" onclick="checkAll();"></th>
    <th style="width:2%; padding:1px;">#</th>
    <th style="width:7%; padding:1px;" nowrap><a href="javascript:orderby('getId');"><%=Label.get("agent.id")%><%=printSortMark(orderby, orderdir, "getId")%></a></th>
    <th style="width:8%; padding:1px;"><a href="javascript:orderby('getName');"><%=Label.get("agent.name")%><%=printSortMark(orderby, orderdir, "getName")%></a></th>
    <th style="width:10%; padding:1px;"><a href="javascript:orderby('getDesc');"><%=Label.get("agent.desc")%><%=printSortMark(orderby, orderdir, "getDesc")%></a></th>
    <th style="width:7%; padding:1px;"><a href="javascript:orderby('getIp');"><%=Label.get("agent.ip")%><%=printSortMark(orderby, orderdir, "getIp")%></a></th>
    <th style="width:5%; padding:1px; word-break:keep-all"><%=Label.get("agent.port")%></th>
    <!-- <th style="width:7%; padding:1px;"><%=Label.get("agent.runmode")%></th>  -->
    <th style="width:5%; padding:1px;"><%=Label.get("agent.inuse.yn")%></th>
    <th style="width:10%; padding:1px;"><%=Label.get("agent.directory")%></th>
    <th style="width:5%; padding:1px;"><%=Label.get("agent.osuser")%></th>
    <th style="width:6%; padding:1px;">OS</th>
    <th style="width:6%; padding:1px;">Java</th>
    <th style="width:6%; padding:1px; word-break:keep-all"><%=Label.get("job.agent")%> Ver</th>
<!--
    <th><%=Label.get("agent.run.command")%></th>
    <th><%=Label.get("agent.run.type")%></th>
    <th><%=Label.get("agent.job.max.limit")%></th>
 -->
    <th style="width:7%; padding:1px;"><%=Label.get("common.current.time")%></th>
</tr>
</thead>
<tbody>
<%
	int i=0;
	for (AgentInfo agentInfo : agentInfoList) {
    	// 서버 실시간 모니터링 보기.
    	AgentMonitoringSummary summary = admin.getAgentMonitoringSummary(agentInfo.getId());
%>
<tr>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" rowspan="2"><input class="Checkbox" type="checkbox" id="chkagentid" name="chkagentid" value="<%=agentInfo.getId()%>"></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" rowspan="2"><%=(++i) %></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" rowspan="2" 
        onMouseOver="this.style.cursor='pointer';this.style.backgroundColor='#FFFF00';"
    	onMouseOut ="this.style.backgroundColor='<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>';" 
    	onclick="openEditFormAgentInfo('<%=agentInfo.getId()%>');"><b><%=nvl(agentInfo.getId())%></b></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" class="Text-left"><b><%=nvl(agentInfo.getName())%></b></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" class="Text-left"><%=nvl(agentInfo.getDesc())%></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" ><b><%=nvl(agentInfo.getIp())%></b></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" ><b><%=nvl(agentInfo.getPort())%></b></td>
    <!-- <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" ><%=("S".equals(agentInfo.getRunMode()) ? "Standalone" : "W".equals(agentInfo.getRunMode()) ? "WAS" : "")%></td> -->
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" ><b><%=agentInfo.isInUse() ? Label.get("agent.inuse.true") : Label.get("agent.inuse.false") %></b></td>
<%
		if (summary.getAgentConnectionError() == null) {
		    Map info = summary.getJvmMonitoringInfo();
%>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" class="Text-left"><%=nvl(info.get("user.dir"))%></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" ><%=nvl(info.get("user.name"))%></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" ><%=nvl(info.get("os.name"))%></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" ><%=nvl(info.get("java.version"))%></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" ><%=nvl(info.get("NC_BATAGENT_VERSION"))%></td>
    <td style="padding:1px;" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>" ><%=info.get("current_time_ms")==null ? "" : toDatetimeString((Long)info.get("current_time_ms"), true)%></td> 
<%
		}else {
%>
    <td colspan="6" bgcolor="<%=agentInfo.isInUse() ? "#fafaaa" : "#c0c0c0"%>"></td>
<%
		}
%>
</tr>
<tr>
    <td colspan="100%" style="padding: 0px 0px;">
        <table class="Table njf-table__typea Width-100" id="tableList">
<%
        if (summary.getAgentConnectionError() == null) {
            Map<String, Long> mem = summary.getJvmMonitoringInfo();
%>
            <tr>
                <td style="padding:1px; text-align:center"><%=Label.get("common.state")%><br><%=summary.getAlive()%></td>
                <td style="padding:1px; text-align:center"><%=Label.get("agent.boot.time")%><br><%=toDatetimeString(summary.getBootTime(), false)%></td>
                <td style="padding:1px; text-align:center" bgcolor="<%=summary.isClosed()?"#FF4444":"#88FF88"%>"><%=Label.get("agent.jobrun.mode")%><br>
                	<select class="Select Width-80 Margin-top-5" onchange="doCloseOrOpen(this, '<%=agentInfo.getId()%>');">
                        <%=printSelectOption("false", Label.get("agent.jobrun.mode.run"),   String.valueOf(summary.isClosed()))%>
                        <%=printSelectOption("true",  Label.get("agent.jobrun.mode.norun"), String.valueOf(summary.isClosed()))%></select>
                </td>
                <td style="padding:1px; text-align:center" bgcolor="<%=summary.getRunningJobCount()>0 ? "#FFFF00" : "#FFFFFF"%>"><a href="javascript:openRunningJobExeWin('<%=agentInfo.getId()%>');"><%=Label.get("agent.running.job")%><br><b><%=summary.getRunningJobCount()%></b></a></td>
                <td style="padding:1px; text-align:center"><a href="javascript:openRunningThreadWin('<%=agentInfo.getId()%>');"><%=Label.get("agent.thread")%><br><b><%=summary.getThreadCount()%></b></a></td>
                <td style="padding:1px; text-align:center"><a href="javascript:openSystemPropertiesWin('<%=agentInfo.getId()%>');"><%=Label.get("agent.system.properties")%></a></td>
                <td style="padding:1px; text-align:center"><a href="javascript:openConfigFiles('<%=agentInfo.getId()%>');"><%=Label.get("agent.configfiles")%></a></td>
                <td style="padding:1px;">
                    <table class="Table njf-table__typea Width-100" id="tableList">
                        <tr>
                            <th style="height: 15px; padding:1px; font-weight: normal;">Heap(M)</th>
                            <th style="height: 15px; padding:1px; font-weight: normal; vertical-align: top;" rowspan="2">Init</th>
                            <td style="height: 15px; padding:1px; text-align:right"><%=byteToMega(mem.get("HEAP_INIT"))%> </td>
                            <th style="height: 15px; padding:1px; font-weight: normal; vertical-align: top;" rowspan="2">Max</th>
                            <td style="height: 15px; padding:1px; text-align:right"><%=byteToMega(mem.get("HEAP_MAX"))%> </td>
                            <th style="height: 15px; padding:1px; font-weight: normal; vertical-align: top;" rowspan="2">Total</th>
                            <td style="height: 15px; padding:1px; text-align:right"><%=byteToMega(mem.get("HEAP_COMMITTED"))%> (<%=mem.get("HEAP_COMMITTED")*100l/mem.get("HEAP_MAX")%>%)</td>
                            <th style="height: 15px; padding:1px; font-weight: normal; vertical-align: top;" rowspan="2">Used</th>
                            <td style="height: 15px; padding:1px; text-align:right" bgcolor="<%=printWarnColor(mem)%>"><b><%=byteToMega(mem.get("HEAP_USED"))%> (<%=mem.get("HEAP_USED")*100l/mem.get("HEAP_COMMITTED")%>%)</b></td>
                        </tr>                        
<%
            if (mem.get("NONHEAP_INIT") > 0) {
%>
                        <tr>
                            <th style="height: 15px; padding:1px; font-weight: normal;">Non-Heap</th>
                            <td style="height: 15px; padding:1px; text-align:right"><%=byteToMega(mem.get("NONHEAP_INIT"))%></td>
                            <td style="height: 15px; padding:1px; text-align:right"><%=byteToMega(mem.get("NONHEAP_MAX"))%></td>
                            <td style="height: 15px; padding:1px; text-align:right"><%=byteToMega(mem.get("NONHEAP_COMMITTED"))%> (<%=mem.get("NONHEAP_COMMITTED")*100l/mem.get("NONHEAP_MAX")%>%)</td>
                            <td style="height: 15px; padding:1px; text-align:right"><b><%=byteToMega(mem.get("NONHEAP_USED"))%> (<%=mem.get("NONHEAP_USED")*100l/mem.get("NONHEAP_COMMITTED")%>%)</b></td>
                        </tr>
<%
            }
%>
						</tbody>
                    </table>
                </td>
            </tr>
<%
        }else {
        	int failMsgIdx = summary.getAgentConnectionError().indexOf("FAIL:");
        	String errorMsg, detailMsg;
        	if (failMsgIdx > -1) {
        	    errorMsg  = summary.getAgentConnectionError().substring(0, failMsgIdx);
                detailMsg = summary.getAgentConnectionError().substring(failMsgIdx);
        	}else {
                errorMsg  = summary.getAgentConnectionError();
                detailMsg = "";
        	}
%>
            <tr align="center">
                <td colspan="100%" style="padding: 0px 0px; text-align: center;"><font color="red"><a style="color: red;" title='<%=conv(detailMsg)%>'><%=Label.get("agent.connect.fail")%>:<%=errorMsg%></a></font></td>
            </tr>
<%
        }
%>
        </table>
    </td>
</tr>
<%
    }
	
    String reloadUrl = null; // encode 된놈.   GET 용
    if (request.getQueryString()==null) {
    	reloadUrl = request.getRequestURI();
    	reloadUrl += "?dummy=1";
    }else {
    	reloadUrl = request.getRequestURI()+"?"+request.getQueryString();
    	reloadUrl = reloadUrl.replaceAll("&pos_y.*", "");
    }
%>
</tbody>
</table>
</form>
<input class="Button" type="button" value="<%=Label.get("common.btn.add")%>" onclick="openEditFormAgentInfo('');">
<input class="Button" type="button" value="<%=Label.get("common.btn.delete")%>" onclick="removeAgentInfo();">

</div>

</center>
</div>
</div>
<jsp:include page="bottom.jsp" flush="true"/>	
</body>
<script>
	function checkReload() {
		if (!document.form1.autoreload.checked) {
			return;
		}else {
			var posY = document.body.scrollTop;
			window.location.href='<%=reloadUrl%>&pos_y='+posY;
		}
	}
<%
	String posY = nvl(request.getParameter("pos_y"));
	if (!Util.isBlank(posY)) {
		out.println("window.scrollTo(0, "+posY+")");
	}
%>
	setInterval('checkReload()', 10000);
</script>
</html>
