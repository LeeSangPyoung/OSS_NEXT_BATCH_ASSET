<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
	String jobinstanceid = request.getParameter("jobinstanceid");

	ControllerAdminLocal admin = getControllerAdmin();
	AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));
    admin.lockJob(jobinstanceid, auth);

	JobInstance jobins = admin.getJobInstance(jobinstanceid);
    if (jobins==null) {
        throw new SchedulerException("Job Instace ID ["+jobinstanceid+"] Not Found");
    }
    List<String> agentIdNameList = admin.getAgentIdNameList();
%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<script src="./script/app/include-lib.js"></script>
<jsp:include page="display_msg.jsp" flush="true"/>
<script>
	var lastPreJobSeq=<%=jobins.getPreJobConditions().size()+1%>;
	function addPreJobRow() {
		var preJobTable = document.getElementById("prejob_table").getElementsByTagName("TBODY")[0];

		var newIndex = lastPreJobSeq++;

		var row = document.createElement("tr");
		row.setAttribute("align", "center");
		row.setAttribute("id", "prejob_"+newIndex);
		var col1= document.createElement("td");
		col1.innerHTML = "<input type=\"text\" class=\"Textinput Width-100\" id=\"preJobId_"+newIndex+"\" name=\"preJobId_"+newIndex+"\" value=\"\" autocomplete=\"off\">";
		var col2= document.createElement("td");
		col2.innerHTML = "<select class='Select' name='okFail_"+newIndex+"'>"+
	                     "<option value=OK><%=PreJobCondition.getOkFailText("OK")%></option>"+
	                     "<option value=FAIL><%=PreJobCondition.getOkFailText("FAIL")%></option>"+
	                     "<option value=OKFAIL><%=PreJobCondition.getOkFailText("OKFAIL")%></option>"+
	                     "<option value=INSEXIST><%=PreJobCondition.getOkFailText("INSEXIST")%></option>"+
	                     "<option value=INSNONE><%=PreJobCondition.getOkFailText("INSNONE")%></option>"+
	                     "<option value=OK_OR_INSNONE><%=PreJobCondition.getOkFailText("OK_OR_INSNONE")%></option>"+
	                     "<option value=FAIL_OR_INSNONE><%=PreJobCondition.getOkFailText("FAIL_OR_INSNONE")%></option>"+
	                     "<option value=OKFAIL_OR_INSNONE><%=PreJobCondition.getOkFailText("OKFAIL_OR_INSNONE")%></option>"+
	                     "<option value=ALLINS_OK><%=PreJobCondition.getOkFailText("ALLINS_OK")%></option>"+
	                     "<option value=ALLINS_FAIL><%=PreJobCondition.getOkFailText("ALLINS_FAIL")%></option>"+
	                     "<option value=ALLINS_OKFAIL><%=PreJobCondition.getOkFailText("ALLINS_OKFAIL")%></option>"+
	                     "</select>";
		var col3= document.createElement("td");
		col3.innerHTML = "<select class='Select' name=\"andOr_"+newIndex+"\"><option value=\"AND\">AND</option><option value=\"OR\">OR</option></select>";
		var col4= document.createElement("td");
		col4.style.cssText = "text-align:center;";
		col4.innerHTML = "<span class=\"Icon Minus-sign\" onclick=\"delPreJobRow('prejob_"+newIndex+"');\" style=\"vertical-align:middle;cursor:hand;\"></span>";
		row.appendChild(col1);
		row.appendChild(col2);
		row.appendChild(col3);
		row.appendChild(col4);
		preJobTable.appendChild(row);

		//Job 선택박스 초기화
		$("#preJobId_"+newIndex).smartAutoComplete({source: joblist.list1, maxResults: 5, delay: 200 } );
	}

	function delPreJobRow(trid) {
		var preJobTable = document.getElementById("prejob_table").getElementsByTagName("TBODY")[0];
		var delTr       = document.getElementById(trid);

		preJobTable.removeChild(delTr);
	}

	var lastTriggerJobSeq=<%=jobins.getTriggerList().size()+1%>;
	function addTriggerJobRow() {
		var preTriggerTable = document.getElementById("triggerjob_table").getElementsByTagName("TBODY")[0];

		var newIndex = lastTriggerJobSeq++;

		var row = document.createElement("tr");
		row.setAttribute("align", "center");
		row.setAttribute("id", "triggerjob_"+newIndex);
		var col1= document.createElement("td");
		col1.innerHTML = "<select class=\"Select Width-40\" id='triggerOkFail_"+newIndex+"' name='triggerOkFail_"+newIndex+"'>"+
						"<option value=END>END</option>"+
						"<option value=ENDOK>ENDOK</option>"+
						"<option value=ENDFAIL>ENDFAIL</option>"+
						"<option value=RETVAL>RETVAL</option>"+
        				"</select> "+
        				"<input type=\"text\" hidden class=\"Textinput Width-25\" id=\"triggerChkValue1_"+newIndex+"\" name=\"triggerChkValue1_"+newIndex+"\" value=\"\" placeholder=\"KEY\"> "+
        				"<input type=\"text\" hidden class=\"Textinput Width-25\" id=\"triggerChkValue2_"+newIndex+"\" name=\"triggerChkValue2_"+newIndex+"\" value=\"\" placeholder=\"VALUE\">";
		var col2= document.createElement("td");
		col2.innerHTML = "<input type=\"text\" class=\"Textinput Width-100\" id=\"triggerJobId_"+newIndex+"\" name=\"triggerJobId_"+newIndex+"\" value=\"\" autocomplete=\"off\"></td>";
		var col3= document.createElement("td");
		col3.innerHTML = "<input type=\"text\" class=\"Textinput Width-100\" id=\"triggerCount_"+newIndex+"\" name=\"triggerCount_"+newIndex+"\" value=\"1\" onkeypress='return event.charCode >= 48 && event.charCode <= 57'></td>";
		var col4= document.createElement("td");
		col4.style.cssText = "text-align:center;";
		col4.innerHTML = "<span class=\"Icon Minus-sign\" onclick=\"delTriggerJobRow('triggerjob_"+newIndex+"');\" style=\"vertical-align:middle;cursor:hand;\"></span>";
		row.appendChild(col1);
		row.appendChild(col2);
		row.appendChild(col3);
		row.appendChild(col4);
		preTriggerTable.appendChild(row);

		//Job 선택박스 초기화
		setTiggerOkFailChange();
		$("#triggerJobId_"+newIndex).smartAutoComplete({source: joblist.list1, maxResults: 5, delay: 200 } );
	}

	function delTriggerJobRow(trid) {
		var triggerJobTable = document.getElementById("triggerjob_table").getElementsByTagName("TBODY")[0];
		var delTr       = document.getElementById(trid);

		triggerJobTable.removeChild(delTr);
	}
	
	var lastParamSeq=<%=jobins.getInParameters().size()+1%>;
	function addParamRow() {
		var paramTable = document.getElementById("param_table").getElementsByTagName("TBODY")[0];

		var newIndex = lastParamSeq++;

		var row = document.createElement("tr");
		row.setAttribute("align", "center");
		row.setAttribute("id", "param_"+newIndex);
		var col1= document.createElement("td");
		col1.innerHTML = "<input type=\"text\" class=\"Textinput Width-100\" name=\"paramName_"+newIndex+"\" value=\"\" size=\"20\"></td>";
		var col2= document.createElement("td");
		col2.innerHTML = "<input type=\"text\" class=\"Textinput Width-100\" name=\"paramValue_"+newIndex+"\" value=\"\" size=\"30\"></td>";
		var col3= document.createElement("td");
		col3.style.cssText = "text-align:center;";
		col3.innerHTML = "<span class=\"Icon Minus-sign\" onclick=\"delParamRow('param_"+newIndex+"');\" style=\"vertical-align:middle;cursor:hand;\"></span>";
		row.appendChild(col1);
		row.appendChild(col2);
		row.appendChild(col3);
		paramTable.appendChild(row);

	}

	function delParamRow(trid) {
		var paramTable = document.getElementById("param_table").getElementsByTagName("TBODY")[0];
		var delTr       = document.getElementById(trid);

		paramTable.removeChild(delTr);
	}

	function changeRepeatIntvalGb() {
		if (document.form1.repeatIntvalGb.value == "EXACT") {
			document.getElementById('repeat_exact_div').style.display = '';
		}else {
			document.getElementById('repeat_exact_div').style.display = 'none';
		}
	}
	
	function changeRepeatYN() {
		if (document.form1.repeatYN.value == "Y") {
			alert("<%=Label.get("form_jobins.alert.timeuntil.required.for.repeat")%>");
		}
	}
	

	function do_submit() {
	    if (confirm('<%=Label.get("form_jobins.edit.confirm.msg")%>')) {
    		document.form1.lastPreJobIndex.value = lastPreJobSeq;
    		document.form1.lastTriggerJobIndex.value = lastTriggerJobSeq;
    		document.form1.lastParamIndex.value  = lastParamSeq;
            document.form1.action="action_jobins.jsp";
            document.form1.target='';
            document.form1.submit();
        }
	}
	
    function openRepeatExactPlanWin() {
        window.open("", "exactplanwin", 'width=900,height=550,scrollbars=1').focus();
        document.form1.action="view_repeat_exact_plan.jsp";
        document.form1.target='exactplanwin';
        document.form1.submit();
    }
    
    function openJobGroupSelectWin(targetElemName) {
        window.open("popup_jobgroup.jsp?target_name="+targetElemName, 'jobgroupselect', 'width=700,height=400,scrollbars=1').focus();
    }
    
    function check_submit() {
		if (document.form1.jobGroupId.value.trim() == '') {
    		alert('<%=Label.get("common.required.field.missing", Label.get("job.jobgroup"))%>');
    		return false;
    	}
		else if (document.form1.jobType.value.trim() == '') {
    		alert('<%=Label.get("common.required.field.missing", Label.get("job.jobtype"))%>');
    		return false;
    	}
		else if (document.form1.agentNode.value.trim() == '') {
    		alert('<%=Label.get("common.required.field.missing", Label.get("job.agent"))%>');
    		return false;
    	}
        
		document.form1.lastPreJobIndex.value = lastPreJobSeq;
		document.form1.lastTriggerJobIndex.value = lastTriggerJobSeq;
		document.form1.lastParamIndex.value  = lastParamSeq;
        document.form1.action="action_jobdef.jsp";
        document.form1.target='';
	}

	function time_helpmsg(time) {
		var helpmsg = '';
		if (time != null && time.length == 4) {
			var activationDate = "<%=jobins.getActivationTime()%>".substring(0,8);
			var activationTime = "<%=jobins.getActivationTime()%>".substring(8,12);

			var activationDateObj = new Date();
			activationDateObj.setFullYear(activationDate.substring(0,4));
			activationDateObj.setMonth   (activationDate.substring(4,6)-1);
			activationDateObj.setDate    (activationDate.substring(6,8));
			activationDateObj.setHours   (activationTime.substring(0,2));
			activationDateObj.setMinutes (activationTime.substring(2,4));
			activationDateObj.setSeconds (00);
			
			if (time <= activationTime) {
				helpmsg = activationDateObj.toLocaleString()+" (<a title='Activation Time'><b>AT</b></a>)";
			}else {
				var days = parseInt(time / 2400);
				var hhmm = time - 2400 * days;
				
				activationDateObj.setDate   (activationDateObj.getDate() + days);
				activationDateObj.setHours  (parseInt(hhmm/100));
				activationDateObj.setMinutes(hhmm - parseInt(hhmm/100) * 100);
				
				helpmsg = activationDateObj.toLocaleString();
			}
		}
		return helpmsg;
	}

	function onfocusout_timefrom_help() {
		document.getElementById("timefrom_help").innerHTML="<font color=blue>("+time_helpmsg(document.form1.timeFrom.value)+")</font>";
	}
	
	function onfocusout_timeuntil_help() {
		document.getElementById("timeuntil_help").innerHTML="<font color=blue>("+time_helpmsg(document.form1.timeUntil.value)+")</font>";
	}
	
	/*------------------------------------------------------------------
	  job 선택시 Autocomplete 기능 추가 
	  ------------------------------------------------------------------*/
	var joblist;
	$a.page(function() {
	    // 초기화 함수
	    this.init = function(id, param) {
	    	
	    	//$(".af-table-wrapper").css({'overflow': 'inherit'});
	    	
	    	getJobList(); //joblist autocomplete
	    	
	    	setTiggerOkFailChange(); //add selectbox event
	    }
	});
	
	function getJobList() {
		$a.request.setup({
		    url : function(id, param) {
		        return id;
		    },
		    method:'get'
		});
		
		$a.request('common_query_jobdef.jsp', {
		    data: {"_IS_AJAX_":"true"},
		    success: function(res) {
		    	joblist = res;
		    	
		    	//파라미터,선행잡,Trigger 입력항목 추가
		    	addParamRow();
		    	addParamRow();
		    	addPreJobRow();
		    	addPreJobRow();
		    	addTriggerJobRow();
		    	addTriggerJobRow();
		    	
		    	//수정페이지 일때 아래내용 설정
		    	<%
				for (int i=1; i<=jobins.getPreJobConditions().size(); i++) {
				%>
					$("#preJobId_"+<%=i%>).smartAutoComplete({source: joblist.list1, maxResults: 5, delay: 200 } );
				<%}%>
				
				<%
				for (int i=1; i<=jobins.getTriggerList().size(); i++) {
				%>
					$("#triggerJobId_"+<%=i%>).smartAutoComplete({source: joblist.list1, maxResults: 5, delay: 200 } );
				<%}%>
		    },
		    fail: function(res) {
		    	alert('fail');
		    },
		    error: function(errObject) {
		    	alert("error!!\n\nerrObject :: \n\n" + JSON.stringify(errObject));
		    }
		});
	}
	
	function setTiggerOkFailChange() {
		$("[id^=triggerOkFail_]").change(function() {
			var idx = this.id.substring(this.id.indexOf("_")+1);
			if($(this).val() == "RETVAL") {
				$("#triggerChkValue1_"+idx).show();
				$("#triggerChkValue2_"+idx).show();
			} else {
				$("#triggerChkValue1_"+idx).hide();
				$("#triggerChkValue2_"+idx).hide();
			}
		})
	}
</script>
<!-- <link rel="stylesheet" href="common.css" type="text/css" /> -->
<script src="./script/app/include-lib.js"></script> 
<title><%=Label.get("jobins")%> (<%=jobinstanceid%>)</title>
</head>
<body onload="displayMsg();changeRepeatIntvalGb();">
<center>

<form name="form1" action="action_jobins.jsp" method="post" onsubmit="return check_submit();">
<input type="hidden" name="cmd" value="modify_jobins">
<input type="hidden" name="lastPreJobIndex">
<input type="hidden" name="lastTriggerJobIndex">
<input type="hidden" name="lastParamIndex">
<input type="hidden" name="jobId" value="<%=jobins.getJobId()%>">
<input type="hidden" name="jobInstanceId" value="<%=jobins.getJobInstanceId()%>">
<%-- <table border="2" style = "border-collapse:collapse" bordercolor = "#000000" width="90%">
<tr>
    <td align="center" bgcolor="#DDDDFF" class=logo_title><%=Label.get("jobins")%> [<%=nvl(jobins.getJobInstanceId())%>] <%=Label.get("common.edit")%></td>
</tr>
</table> --%>

	<div class="header-wrap">
		<div class="header">
			<div class="header-title">
				<%=Label.get("jobins")%> [<%=nvl(jobins.getJobInstanceId())%>] <%=Label.get("common.edit")%>
			</div>
			<div class="header-close-button">
		    	<span><a href="#" onclick="window.close();"><img alt="팝업창 닫기" src="images/pop_close.png"></a></span>
			</div>
		</div>
	</div>
	
<div id="container2" class="popup-content-wrap">

	<br>
	<table border="0" width="100%">
		<tr><td colspan="100%" align="left"><%=Label.get("form_jobins.edit.msg")%></td></tr>
	</table>
	
	<%-- <table border="0" width="90%">
	<tr><td colspan="100%" align="left"><font size=3><b>■ <%=Label.get("job.program.info")%></b></font></td></tr>
	</table> --%>
	
    <div class="popup-content-title__wrap">
		<div class="content-title"><%=Label.get("job.program.info")%></div>
	</div>
	
    <table class="Table njf-table__typea" >
        <colgroup>
            <col width="22%">
            <col>
        </colgroup>
		<tr>
			<th class="required"><span class="ico_bull"></span><%=Label.get("job.jobid")%></th>
			<td><b><%=jobins.getJobId()%></b></td>
		</tr>
		<tr>
			<th class="required"><span class="ico_bull"></span><%=Label.get("job.jobinsid")%></th>
			<td><b><%=nvl(jobins.getJobInstanceId())%></b></td>
		</tr>
		<tr>
			<th class="required"><span class="ico_bull"></span><%=Label.get("job.jobgroup")%></th>
			<td><input type="text" class="TextInput" name="jobGroupId" value="<%=nvl(jobins.getJobGroupId())%>" style="width:85%">
		        <input type="button" class="Button" value="<%=Label.get("common.select")%>" onclick="openJobGroupSelectWin('jobGroupId');"> 
		    </td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.desc")%></th>
			<td><input type="text" class="TextInput" name="description" value="<%=conv(jobins.getDescription())%>" size="50" style="width:100%"></td>
		</tr>
		<tr>
			<th class="required"><span class="ico_bull"></span><%=Label.get("job.jobtype")%></th>
			<td>
		        <select class="Select" name="jobType">
		            <%=printJobTypeSelectOptionList(admin, jobins.getJobType()) %>
		        </select>
			</td>
		</tr>
		<tr>
			<th class="required"><span class="ico_bull"></span><%=Label.get("job.agent")%></th>
			<td>
		<%
		    if (JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) {
		        // 현재 agent 에서 실행 상태인 Job 은 agent 변경 불가.
		%>
		        <input type="text" class="TextInput" name="agentNode" value="<%=jobins.getAgentNode()%>" readonly size="10"> <%=Label.get("form_jobins.runningjob.cannot.change.agent")%>
		<%
		    }else {
		%>
				<%=Label.get("job.agent.master")%> :
			   	<select class="Select" name="agentNode">
		<%      
				for (String agentIdName : agentIdNameList) {
					String[] agentIdNameArray = agentIdName.split(",");
					out.println(printSelectOption(agentIdNameArray[0], agentIdNameArray[0]+" ("+agentIdNameArray[1]+")", jobins.getAgentNodeMaster()));
				}
		%>
		        </select>
		        <%=Label.get("job.agent.slave")%>(<%=Label.get("job.agent.slave.hint")%>) :
		       		<select class="Select" name="agentNode2">
		<%      
				out.println(printSelectOption("","N/A",""));
				if(agentIdNameList.size()>1){
					for (String agentIdName : agentIdNameList) {
						String[] agentIdNameArray = agentIdName.split(",");
						out.println(printSelectOption(agentIdNameArray[0], agentIdNameArray[0]+" ("+agentIdNameArray[1]+")", jobins.getAgentNodeSlave()));
					}	
				}
				
		%>
		        </select>
		        
		<%
		    }
		%>
		    </td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.component")%></th>
			<td><input type="text" class="TextInput" name="componentName" value="<%=conv(jobins.getComponentName())%>" size="50" style="width:100%"></td>
		</tr>
		<tr>
		    <th><span class="ico_bull"></span><%=Label.get("common.log")%></th>
		    <td>
		        <select class="Select" name="logLevel">
		            <option value=""></option>
		<%
		    List<String> logLevelList = admin.getLogLevelUsingList();
		    for(String logLevel : logLevelList ) {
		    	out.println(printSelectOption(logLevel, jobins.getLogLevel()));
		    }
		%>
		        </select>
		    </td>
		</tr>
	</table>
<%-- <br>
<table border="0" width="90%">
<tr><td colspan="100%" align="left"><font size=3><b>■ <%=Label.get("job.execution.condition")%></b></font></td></tr>
</table> --%>

    <div class="popup-content-title__wrap">
		<div class="content-title"><%=Label.get("job.execution.condition")%></div>
	</div>

    <table class="Table njf-table__typea" >
        <colgroup>
            <col width="22%">
            <col>
        </colgroup>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.time")%></th>
			<td><table class="Table">
				<tr>
					<td width="40%" align=center>
						<input type="text" class="TextInput" name="timeFrom" value="<%=nvl(jobins.getTimeFrom())%>" maxlength=4 size=4 onblur="onfocusout_timefrom_help();" style="width:50%"> (HHMM) 
					</td>
					<td rowspan=2>~</td>
					<td width="40%" align=center>
						<input type="text" class="TextInput" name="timeUntil" value="<%=nvl(jobins.getTimeUntil())%>" maxlength=4 size=4 onblur="onfocusout_timeuntil_help();" style="width:50%"> (HHMM)
					</td>
				</tr>
				<tr>
					<td align=center><span id="timefrom_help"/></td>
					<td align=center><span id="timeuntil_help"/></td>
				</tr>
				</table>
			</td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.repeat")%></th>
			<td><table class="Table">
			    <tr>
			        <td rowspan=3 valign="top" width="10%">
		        	    <select class="Select" name="repeatYN" onchange="changeRepeatYN();"><%=printYNSelectOptions(jobins.getRepeatYN())%></select><br>
		        	</td>
		        	<td width="20%">
		        	    <b>[<%=Label.get("job.repeat.intval")%>]</b>
		        	</td>
		        	<td>
		                <input type="text" class="TextInput" name="repeatIntval" value="<%=jobins.getRepeatIntval()%>" size=4 maxlength=10 style="width:50%"><%=Label.get("common.second")%>
		            </td>
		            <td width="20%">
		                <b>[<%=Label.get("job.repeat.intval.gb")%>]</b>
		        	</td>
		        	<td>
		                <select class="Select" name="repeatIntvalGb" onchange="changeRepeatIntvalGb();">
		                    <%=printSelectOption("START", jobins.getRepeatIntvalGb())%>
		                    <%=printSelectOption("END",   jobins.getRepeatIntvalGb())%>
							<%=printSelectOption("EXACT", jobins.getRepeatIntvalGb())%>
		                </select><br>
		            </td>
		        </tr>
		        <tr>
		            <td>
		                <b>[<%=Label.get("job.repeat.if.error")%>]</b>
		        	</td>
		        	<td>
		                <select class="Select" name="repeatIfError">
		                    <%=printSelectOption("STOP",   jobins.getRepeatIfError())%>
		                    <%=printSelectOption("IGNORE", jobins.getRepeatIfError())%>
		                </select>
		            </td>
		            <td>
		        	    <b>[<%=Label.get("job.repeat.maxok")%>]</b>
		        	</td>
		        	<td>
		                <input type="text" class="TextInput" name="repeatMaxOk" value="<%=jobins.getRepeatMaxOk()%>" size=4 maxlength=10>
		            </td>
		        </tr>
		        <tr>
					<td colspan="4">
						<div id="repeat_exact_div">
							<table width="100%" border=0>
								<tr>
									<td>
										<b>[<%=Label.get("job.repeat.exact.exp")%>]</b>
									</td>
									<td colspan=3>
										<input type="text" class="TextInput" name="repeatExactExp" value="<%=nvl(jobins.getRepeatExactExp())%>" size=30>&nbsp;(HHmmss)<br>
										<%=Label.get("job.repeat.exact.hint")%>
									</td>
									<td valign=middle>
									    <input type="button" class="Button" value="Plan" onclick="openRepeatExactPlanWin();">
									</td>
								</tr>
							</table>
						</div>
					</td>
		        </tr>
		        </table>
		    </td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.confirm.need.yn")%></th>
			<td>&nbsp;<select class="Select" name="confirmNeedYN"><%=printYNSelectOptions(jobins.getConfirmNeedYN())%></select>&nbsp;</td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.parallel.group")%></th>
			<td><input type="text" class="TextInput" name="parallelGroup" value="<%=nvl(jobins.getParallelGroup())%>" style="width:100%"></td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.trigger")%></th>
			<td>
				<table class="Table njf-table__typea Width-100" id="triggerjob_table">
				<colgroup>
			        <col width="45%"/>
			        <col width="35%"/>
			        <col width="10%"/>
			        <col width="10%"/>
			    </colgroup>
			   	<thead>
					<tr>
						<th><%=Label.get("job.trigger.okfail")%></th>
						<th><%=Label.get("job.trigger.id")%></th>
						<th><%=Label.get("job.trigger.count")%></th>
						<th><span class="Icon Plus-sign" onclick="addTriggerJobRow();" style="vertical-align:middle;cursor:hand;"></span></th>
					</tr>
				</thead>
				<tbody>
		<%
			int i=0;
			for (PostJobTrigger triggerJob : jobins.getTriggerList()) {
			i++;
			
			Boolean isRetVal = "RETVAL".equals(triggerJob.getWhen()) ? true : false;
		%>
					<tr id="triggerjob_<%=i%>">
						<td>
							<select class="Select Width-40" id="triggerOkFail_<%=i%>" name="triggerOkFail_<%=i%>">
								<%=printSelectOption("END"		, 	triggerJob.getWhen())%>
								<%=printSelectOption("ENDOK"	, 	triggerJob.getWhen())%>
								<%=printSelectOption("ENDFAIL"	, 	triggerJob.getWhen())%>
								<%=printSelectOption("RETVAL"	, 	triggerJob.getWhen())%>
							</select>
							
							<input type="text" <%=isRetVal?"":"hidden"%> class="Textinput Width-25" id="triggerChkValue1_<%=i%>" name="triggerChkValue1_<%=i%>" value="<%=nvl(triggerJob.getCheckValue1())%>" placeholder="KEY">
							<input type="text" <%=isRetVal?"":"hidden"%> class="Textinput Width-25" id="triggerChkValue2_<%=i%>" name="triggerChkValue2_<%=i%>" value="<%=nvl(triggerJob.getCheckValue2())%>" placeholder="VALUE">
						</td>
						<td>
							<input type="text" class="Textinput Width-100" id="triggerJobId_<%=i%>" name="triggerJobId_<%=i%>" value="<%=nvl(triggerJob.getTriggerJobId())%>" autocomplete="off">
						</td>
						<td>
							<input type="text" class="Textinput Width-100" id="triggerCount_<%=i%>" name="triggerCount_<%=i%>" value="<%=nvl(triggerJob.getJobInstanceCount())%>" onkeypress='return event.charCode >= 48 && event.charCode <= 57'>
						</td>
						<td style="text-align:center"><span class="Icon Minus-sign" onclick="delTriggerJobRow('triggerjob_<%=i%>');" style="vertical-align:middle;cursor:hand;"></span></td>
					</tr>
		<%
			}
		%>
				</tbody>
				</table>
			</td>
		</tr>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.prejob")%></th>
			<td>
				<table id="prejob_table" class="Table">
					<tr>
						<th width="170px"><%=Label.get("job.prejob.id")%></td>
						<th width="210px"><%=Label.get("job.prejob.okfail")%></td>
						<th><%=Label.get("job.prejob.andor")%></td>
						<th width="50px"><span class="Icon Plus-sign" onclick="addPreJobRow();" style="vertical-align:middle;cursor:hand;"></span></th>
					</tr>
		<%
			i=0;
			for (PreJobCondition preJob : jobins.getPreJobConditions()) {
			i++;
		%>
					<tr align="center" id="prejob_<%=i%>">
						<td><input type="text" class="Textinput Width-100" id="preJobId_<%=i%>" name="preJobId_<%=i%>" value="<%=nvl(preJob.getPreJobId())%>" autocomplete="off"></td>
						<td>
							<select class="Select" name="okFail_<%=i%>">
								<%=printSelectOption("OK" ,               preJob.getOkFailText("OK") ,               preJob.getOkFail())%>
								<%=printSelectOption("FAIL",              preJob.getOkFailText("FAIL"),              preJob.getOkFail())%>
								<%=printSelectOption("OKFAIL",            preJob.getOkFailText("OKFAIL"),            preJob.getOkFail())%>
								<%=printSelectOption("INSEXIST",          preJob.getOkFailText("INSEXIST"),          preJob.getOkFail())%>
								<%=printSelectOption("INSNONE",           preJob.getOkFailText("INSNONE"),           preJob.getOkFail())%>
								<%=printSelectOption("OK_OR_INSNONE",     preJob.getOkFailText("OK_OR_INSNONE"),     preJob.getOkFail())%>
								<%=printSelectOption("FAIL_OR_INSNONE",   preJob.getOkFailText("FAIL_OR_INSNONE"),   preJob.getOkFail())%>
								<%=printSelectOption("OKFAIL_OR_INSNONE", preJob.getOkFailText("OKFAIL_OR_INSNONE"), preJob.getOkFail())%>
								<%=printSelectOption("ALLINS_OK",         preJob.getOkFailText("ALLINS_OK"),         preJob.getOkFail())%>
								<%=printSelectOption("ALLINS_FAIL",       preJob.getOkFailText("ALLINS_FAIL"),       preJob.getOkFail())%>
								<%=printSelectOption("ALLINS_OKFAIL",     preJob.getOkFailText("ALLINS_OKFAIL"),     preJob.getOkFail())%>
							</select>
						</td>
						<td>
							<select class="Select" name="andOr_<%=i%>">
								<%=printSelectOption("AND", preJob.getAndOr())%>
								<%=printSelectOption("OR",  preJob.getAndOr())%>
							</select>
						</td>
						<td style="text-align:center"><span class="Icon Minus-sign" onclick="delPreJobRow('prejob_<%=i%>');" style="vertical-align:middle;cursor:hand;"></span></td>
					</tr>
		<%
			}
		%>
				</table>
			</td>
		</tr>
	</table>
<%-- <br>
<table border="0" width="90%">
<tr><td colspan="100%" align="left"><font size=3><b>■ <%=Label.get("job.param")%></b></font></td></tr>
</table> --%>

    <div class="popup-content-title__wrap">
		<div class="content-title"><%=Label.get("job.param")%></div>
	</div>

    <table class="Table njf-table__typea" >
        <colgroup>
            <col width="22%">
            <col>
        </colgroup>
		<tr>
			<th><span class="ico_bull"></span><%=Label.get("job.param")%></th>
			<td>
				<table id="param_table" class="Table">
					<tr>
						<th width="40%"><%=Label.get("job.param.name")%></td>
						<th><%=Label.get("job.param.value")%></td>
						<th width="10%"><span class="Icon Plus-sign" onclick="addParamRow();" style="vertical-align:middle;cursor:hand;"></span></th>
					</tr>
		<%
			i=0;
			for (Map.Entry<String, String> param : jobins.getInParameters().entrySet()) {
			i++;
		%>
					<tr align="center" id="param_<%=i%>">
						<td><input type="text" class="Textinput Width-100" name="paramName_<%=i%>" value="<%=conv(param.getKey())%>" size="20"></td>
						<td><input type="text" class="Textinput Width-100" name="paramValue_<%=i%>" value="<%=conv(param.getValue())%>" size="30"></td>
						<td style="text-align:center"><span class="Icon Minus-sign" onclick="delParamRow('param_<%=i%>');" style="vertical-align:middle;cursor:hand;"></span></td>
					</tr>
		<%
			}
		%>
				</table>
			</td>
		</tr>
	</table>
<br>
<input type="button" class="Button" value="<%=Label.get("common.btn.edit")%>" onclick="do_submit();">
</form>
<br><br>
</div>
</div>
</center>
</body>
</html>
