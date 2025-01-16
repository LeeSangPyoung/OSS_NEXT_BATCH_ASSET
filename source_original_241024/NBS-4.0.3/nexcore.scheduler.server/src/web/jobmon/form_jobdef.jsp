<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
	String mode = nvl(request.getParameter("mode"), "");
	boolean isEditMode = "edit".equals(mode);

	String jobid = request.getParameter("jobid");
	JobDefinition jobdef = new JobDefinition();

	ControllerAdminLocal admin = getControllerAdmin();
	if ("edit".equals(mode) || "copy".equals(mode) || "delete".equals(mode)) {
		jobdef = admin.getJobDefinition(jobid);
	}
	Map  calendarMap = admin.getCalendarList();
    List<String> agentIdNameList = admin.getAgentIdNameList();
	
	String dailyActivationTime = admin.getSystemConfigValue("DAILY_ACTIVATION_TIME");
%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<script src="./script/app/include-lib.js"></script>
<jsp:include page="display_msg.jsp" flush="true"/>
<title>Job Definition Form</title>
<script type="text/javascript">
    function changeScheduleType() {
        if (document.form1.scheduleType.value == "FIXED") {
            document.getElementById('expression_type').style.display = 'none';
            document.getElementById('fixed_type').style.display = '';
        }else if (document.form1.scheduleType.value == "EXPRESSION") {
            document.getElementById('expression_type').style.display = '';
            document.getElementById('fixed_type').style.display = 'none';
        }
    }
	
	function changeRepeatYN() {
		if (document.form1.repeatYN.value == "Y") {
			alert("<%=Label.get("form_jobdef.alert.timeuntil.required.for.repeat")%>");
		}
	}
	
    function changeRepeatIntvalGb() {
        if (document.form1.repeatIntvalGb.value == "EXACT") {
            document.getElementById('repeat_exact_div').style.display = '';
            $("#repeatTh").attr("rowspan", "3");
        }else {
            document.getElementById('repeat_exact_div').style.display = 'none';
            $("#repeatTh").attr("rowspan", "2");
        }
    }
    
    function changeDayOfMonthScheduleType() {
    	if (document.form1.dayOfMonthScheduleType[0].checked == true) {
    		document.getElementById('schedule_type_number_div').style.display = '';
            document.getElementById('schedule_type_calendar_div').style.display = 'none';
    	}else if (document.form1.dayOfMonthScheduleType[1].checked == true) {
    		document.getElementById('schedule_type_number_div').style.display = 'none';
            document.getElementById('schedule_type_calendar_div').style.display = '';
        }else {
        	document.getElementById('schedule_type_number_div').style.display = '';
            document.getElementById('schedule_type_calendar_div').style.display = 'none';
        }
    }
	
	var lastPreJobSeq=<%=jobdef.getPreJobConditions().size()+1%>;
	function addPreJobRow() {
		var preJobTable = document.getElementById("prejob_table").getElementsByTagName("TBODY")[0];

		var newIndex = lastPreJobSeq++;

		var row = document.createElement("tr");
		row.setAttribute("align", "center");
		row.setAttribute("id", "prejob_"+newIndex);
		var col1= document.createElement("td");
		col1.innerHTML = "<input type=\"text\" class=\"Textinput Width-100\" id=\"preJobId_"+newIndex+"\" name=\"preJobId_"+newIndex+"\" value=\"\" autocomplete=\"off\"></td>";
		var col2= document.createElement("td");
		col2.innerHTML = "<select class=\"Select Width-100\" name='okFail_"+newIndex+"'>"+
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
		col3.innerHTML = "<select class=\"Select Width-100\" name=\"andOr_"+newIndex+"\"><option value=\"AND\">AND</option><option value=\"OR\">OR</option></select>";
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
	
	var lastTriggerJobSeq=<%=jobdef.getTriggerList().size()+1%>;
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

	var lastParamSeq=<%=jobdef.getInParameters().size()+1%>;
	function addParamRow() {
		var paramTable = document.getElementById("param_table").getElementsByTagName("TBODY")[0];

		var newIndex = lastParamSeq++;

		var row = document.createElement("tr");
		row.setAttribute("align", "center");
		row.setAttribute("id", "param_"+newIndex);
		var col1= document.createElement("td");
		col1.innerHTML = "<input type=\"text\" class=\"Textinput  Width-100 Text-center\" name=\"paramName_"+newIndex+"\" value=\"\" size=\"20\"></td>";
		var col2= document.createElement("td");
		col2.innerHTML = "<input type=\"text\" class=\"Textinput  Width-100 Text-center\" name=\"paramValue_"+newIndex+"\" value=\"\" size=\"30\"></td>";
		var col3= document.createElement("td");
		col3.style.cssText = "text-align:center;";
		col3.innerHTML = "<span class=\"Icon Minus-sign\" onclick=\"delParamRow('param_"+newIndex+"');\" style=\"vertical-align:middle;cursor:hand;\"></span>";
		var col4= document.createElement("td");
		col4.style.cssText = "text-align:center;";
		col4.innerHTML = "<a href=\"javascript:moveUpParamRow("+newIndex+");\">▲</a>&nbsp;<a href=\"javascript:moveDownParamRow("+newIndex+");\">▼</a>";
		row.appendChild(col1);
		row.appendChild(col2);
		row.appendChild(col3);
		row.appendChild(col4);
		paramTable.appendChild(row);
	}
	
	function delParamRow(trid) {
		var paramTable = document.getElementById("param_table").getElementsByTagName("TBODY")[0];
		var delTr       = document.getElementById(trid);

		paramTable.removeChild(delTr);
	}
	
	function moveParamRow(idx1, idx2) {
		if (idx1 == 0 || idx2 == 0) {
			return;    /* the first row. do not move */
		}
		
		var paramNameNodeList1 = document.getElementsByName("paramName_"+idx1);
		var paramNameNodeList2 = document.getElementsByName("paramName_"+idx2);
		
		if (paramNameNodeList1.length == 0 || paramNameNodeList2.length == 0) {
			return; /* maybe the last row. do not move */
		}
		
		var paramValueNodeList1 = document.getElementsByName("paramValue_"+idx1);
		var paramValueNodeList2 = document.getElementsByName("paramValue_"+idx2);
		
		var tempName  = paramNameNodeList1[0].value;
		var tempValue = paramValueNodeList1[0].value;
		
		paramNameNodeList1[0].value  = paramNameNodeList2[0].value;
		paramValueNodeList1[0].value = paramValueNodeList2[0].value;
		
		paramNameNodeList2[0].value  = tempName;
		paramValueNodeList2[0].value = tempValue;
	}

	function moveUpParamRow(idx) {
		moveParamRow(idx, idx-1);
	}
	
	function moveDownParamRow(idx) {
		moveParamRow(idx, idx+1);
	}
	
	function check_submit() {
		if (document.form1.reqUserName.value=='') {
            alert("<%=Label.get("form_jobdef.input_request_username")%>");
            return false;
        }
		else if (document.form1.jobId.value.trim() == '') {
    		alert('<%=Label.get("common.required.field.missing", Label.get("job.jobid"))%>');
    		return false;
    	}
		else if (document.form1.jobGroupId.value.trim() == '') {
    		alert('<%=Label.get("common.required.field.missing", Label.get("job.jobgroup"))%>');
    		return false;
    	}
		else if (document.form1.owner.value.trim() == '') {
    		alert('<%=Label.get("common.required.field.missing", Label.get("job.owner"))%>');
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

    function openScheduleSimulationWin() {
        window.open("", "simschedulewin", 'width=700,height=350,scrollbars=1').focus();
        document.form1.action="view_sim_schedule_plan.jsp";
        document.form1.target='simschedulewin';
        document.form1.submit();
    }
    
    function openParamExpWin() {
    	check_submit();
    	window.open("", "paramexpwin", 'width=820,height=550,scrollbars=1').focus();
    	document.form1.action="view_test_param_exp.jsp";
        document.form1.target='paramexpwin';
        document.form1.submit();
    }

    function openRepeatExactPlanWin() {
        window.open("", "exactplanwin", 'width=1100,height=550,scrollbars=1').focus();
        document.form1.action="view_repeat_exact_plan.jsp";
        document.form1.target='exactplanwin';
        document.form1.submit();
    }
    
    function openJobGroupSelectWin(targetElemName) {
        window.open("popup_jobgroup.jsp?target_name="+targetElemName, 'jobgroupselect', 'width=700,height=400,scrollbars=1').focus();
        /*
        $a.popup({
        	url: "popup_jobgroup.jsp?target_name="+targetElemName
        	,iframe: true
        })*/
    }
	
	function time_helpmsg(time) {
		var helpmsg = '';
		if (time != null && time.length == 4) {
			var activationTime = "<%=dailyActivationTime%>";
			
			if  (time <= activationTime) {
				helpmsg = activationTime.substring(0,2)+":"+activationTime.substring(2,4)+" (<a title='Activation Time'><b>AT</b></a>)";
			}else {
				var days = parseInt(time / 2400);
				var hhmm = time - 2400 * days;
				var hhmmStr = String(hhmm);
				if (hhmmStr.length == 1) {
					hhmmStr = "000"+hhmm;
				}else if (hhmmStr.length == 2) {
					hhmmStr = "00"+hhmm;
				}else if (hhmmStr.length == 3) {
					hhmmStr = "0"+hhmm;
				}
				
				if (days > 0) {
					helpmsg = days + " <%=Label.get("common.daysafter", "")%>";
				}
				
				helpmsg = helpmsg + hhmmStr.substring(0,2) + ":" + hhmmStr.substring(2,4);
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
	    	
	    	$(".af-table-wrapper").css({'overflow': 'inherit'});
	    	
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
		    	
		    	//파라미터,선행잡, Trigger 입력항목 추가
		    	addParamRow();
		    	addParamRow();
		    	addPreJobRow();
		    	addPreJobRow();
		    	addTriggerJobRow();
		    	addTriggerJobRow();
		    	
		    	//수정페이지 일때 아래내용 설정
		    	<%
				for (int i=1; i<=jobdef.getPreJobConditions().size(); i++) {
				%>
					$("#preJobId_"+<%=i%>).smartAutoComplete({source: joblist.list1, maxResults: 5, delay: 200 } );
				<%}%>
				
				<%
				for (int i=1; i<=jobdef.getTriggerList().size(); i++) {
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
</head>
<body onload="displayMsg();changeScheduleType();changeRepeatIntvalGb();changeDayOfMonthScheduleType();">
<center>
	<div class="header-wrap">
		<div class="header">
			<div class="header-title">
				Job Definition <%="add".equals(mode)||"copy".equals(mode)?Label.get("common.new"):"edit".equals(mode)?Label.get("common.edit"):"delete".equals(mode)?Label.get("common.delete"):"" %>
			</div>
			<div class="header-close-button">
		    	<span><a href="#" onclick="window.close();"><img alt="팝업창 닫기" src="images/pop_close.png"></a></span>
			</div>
		</div>
	</div>
	<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
		<form name="form1" method="post" action="action_jobdef.jsp" onsubmit="return check_submit();">
		<input type="hidden" name="lastPreJobIndex">
		<input type="hidden" name="lastTriggerJobIndex">
		<input type="hidden" name="lastParamIndex">
		<input type="hidden" name="cmd" value="<%=isEditMode?"request_edit":"request_add"%>">
		<input type="hidden" name="logLevel" value="<%=nvl(jobdef.getLogLevel())%>">
		
		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Label.get("job.program.info")%></div>
		</div>
		
		<table class="Table njf-table__typea" >
	    <colgroup>
	        <col width="15%">
	        <col>
	        <col width="15%">
	        <col>
	    </colgroup>
	    <tbody>
			<tr>
				<th class="required"><%=Label.get("job.jobid")%></th>
				<td><input type="text" <%="copy".equals(mode)? "class='Textinput Width-100 Margin-bottom-5'" : "class='Textinput Width-100'" %> name="jobId" value="<%=nvl(jobdef.getJobId())%>" <%=isEditMode?"readonly":""%>><%="copy".equals(mode)?"<font color=#ee0000><b>"+Label.get("form_jobdef.change.jobid")+"</b></font>":""%></td>
				<th class="required"><%=Label.get("job.jobgroup")%></th>
				<td style="padding:0;">
					<table class="Width-100">
					<colgroup>
						<col style="width:85%"/>
		       			<col style="width:15%"/>
	       			</colgroup>
					<tbody>
						<tr>
							<td style="border:0;"><input type="text" class="Textinput Width-100" name="jobGroupId" value="<%=nvl(jobdef.getJobGroupId())%>"></td>
							<td style="text-align:center; border:0;"><input type="button" class="Button" value="<%=Label.get("common.select")%>" onclick="openJobGroupSelectWin('jobGroupId');"> </td>
						</tr>
					</tbody>
					</table>
				</td>
			</tr>
			<tr>
				<th class="required"><%=Label.get("job.owner")%></th>
				<td><input type="text" class="Textinput Width-100" name="owner" value="<%=conv(jobdef.getOwner())%>"></td>
				<th><%=Label.get("job.desc")%></th>
				<td><input type="text" class="Textinput Width-100" name="description" value="<%=conv(jobdef.getDescription())%>"></td>
			</tr>
			<tr>
				<th class="required"><%=Label.get("job.jobtype")%></th>
				<td>
			        <select class="Select Width-100" id="jobType" name="jobType">
			            <%=printJobTypeSelectOptionList(admin, jobdef.getJobType()) %>
			        </select>
				</td>
				<th><%=Label.get("job.component")%></th>
				<td><input type="text" class="Textinput Width-100 Margin-bottom-5" name="componentName" value="<%=conv(jobdef.getComponentName())%>">
					<div class="Tooltip"><%=Label.get("form_jobdef.component.input.help")%></div>
				</td>
			</tr>
			<tr>
				<th class="required"><%=Label.get("job.agent")%></th>
				<td style="padding:0;" colspan="3">
					<table class="Width-100">
					<colgroup>
						<col style="width:10%"/>
		       			<col style="width:30%"/>
		       			<col style="width:25%"/>
		       			<col style="width:30%"/>
	       			</colgroup>
	       			<tbody>
	       				<tr>
	       					<td style="text-align:right; border:0">
	       						<%=Label.get("job.agent.master")%> : 
	       					</td>
	       					<td style="border:0">
	       						<select class="Select Width-100" name="agentNode">
	       						<%      
									for (String agentIdName : agentIdNameList) {
										String[] agentIdNameArray = agentIdName.split(",");
										out.println(printSelectOption(agentIdNameArray[0], agentIdNameArray[0]+" ("+agentIdNameArray[1]+")", jobdef.getAgentNodeMaster()));	
									}
								%>
								</select>
	       					</td>
							<td style="text-align:right; border:0">
								<%=Label.get("job.agent.slave")%>(<%=Label.get("job.agent.slave.hint")%>) :
							<td style="border:0">
								 <select class="Select Width-100" name="agentNode2">
								<%      
									out.println(printSelectOption("","N/A",""));
									if(agentIdNameList.size()>1){
										for (String agentIdName : agentIdNameList) {
											String[] agentIdNameArray = agentIdName.split(",");
											out.println(printSelectOption(agentIdNameArray[0], agentIdNameArray[0]+" ("+agentIdNameArray[1]+")", jobdef.getAgentNodeSlave()));
										}
									}
								%>
			        			</select>
							</td>
						</tr>
	       			</tbody>
	       			</table>
				</td>
			</tr>
			<tr>
				<th><%=Label.get("job.param")%><br><input type="button" class="Button Margin-top-5" value="<%=Label.get("form_jobdef.param.simulation")%>" onclick="openParamExpWin();"></th>
				<td colspan="3">
					<table class="Table njf-table__typea Width-100" id="param_table">
					<colgroup>
						<col style="width:40%"/>
						<col style="width:40%"/>
						<col style="width:10%"/>
						<col style="width:10%"/>
					</colgroup>
					<thead>
						<tr>
							<th><%=Label.get("job.param.name")%></th>
							<th><%=Label.get("job.param.value")%></th>
							<th><span class="Icon Plus-sign" onclick="addParamRow();" style="vertical-align:middle;cursor:hand;"></span></th>
							<th/>
						</tr>
					</thead>
					<tbody>
					<%
						int i=0;
						for (Map.Entry<String, String> param : jobdef.getInParameters().entrySet()) {
						i++;
					%>
						<tr id="param_<%=i%>">
							<td><input type="text" class="Textinput Width-100" name="paramName_<%=i%>" value="<%=conv(param.getKey())%>"></td>
							<td><input type="text" class="Textinput Width-100" name="paramValue_<%=i%>" value="<%=conv(param.getValue())%>"></td>
							<td style="text-align:center"><span class="Icon Minus-sign" onclick="delParamRow('param_<%=i%>');" style="vertical-align:middle;cursor:hand;"></span></td>
							<td><a href="javascript:moveUpParamRow(<%=i%>);">▲</a>&nbsp;<a href="javascript:moveDownParamRow(<%=i%>);">▼</a></td>
						</tr>
					<%
						}
					%>
					</tbody>
					</table>
				</td>
			</tr>
		</tbody>
		</table>

		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Label.get("job.execution.condition")%></div>
		</div>
		
		<table class="Table njf-table__typea" >
	    <colgroup>
	        <col width="15%">
	        <col>
	    </colgroup>
	    <tbody>
			<tr>
				<th><%=Label.get("job.time")%></th>
				<td>
					<input type="text" class="Textinput Width-20 Text-center" name="timeFrom" value="<%=nvl(jobdef.getTimeFrom())%>" maxlength=4 onblur="onfocusout_timefrom_help();">
					<b><label>(HHMM)</label><label id="timefrom_help"/></b> ~ 
					<input type="text" class="Textinput Width-20 Text-center" name="timeUntil" value="<%=nvl(jobdef.getTimeUntil())%>" maxlength=4 onblur="onfocusout_timeuntil_help();">
					<b><label>(HHMM)</label><label id="timeuntil_help"/></b>
				</td>
			</tr>
			<tr>
				<th><%=Label.get("job.repeat")%></th>
				<td>
					<table class="Table njf-table__typea Width-100">
					    <tr>
					        <th class="Width-15" rowspan="2" id="repeatTh">
				        	    <select class="Select Width-90" name="repeatYN" onchange="changeRepeatYN();"><%=printYNSelectOptions(jobdef.getRepeatYN())%></select><br>
				        	</th>
				        	<th>
				        	    [<%=Label.get("job.repeat.intval")%>]
				        	</th>
				        	<td>
				                <input type="text" class="Textinput Width-80 Text-center" name="repeatIntval" value="<%=jobdef.getRepeatIntval()%>" maxlength=10>&nbsp;<%=Label.get("common.second")%>
				            </td>
				            <th>
				                [<%=Label.get("job.repeat.intval.gb")%>]
				        	</th>
				        	<td>
				                <select class="Select Width-100" name="repeatIntvalGb" onchange="changeRepeatIntvalGb();">
				                    <%=printSelectOption("START", jobdef.getRepeatIntvalGb())%>
				                    <%=printSelectOption("END",   jobdef.getRepeatIntvalGb())%>
				                    <%=printSelectOption("EXACT", jobdef.getRepeatIntvalGb())%>
				                </select>
				            </td>
				        </tr>
				        <tr>
				            <th>
				                [<%=Label.get("job.repeat.if.error")%>]
				        	</th>
				        	<td>
				                <select class="Select Width-100" name="repeatIfError">
				                    <%=printSelectOption("STOP",   jobdef.getRepeatIfError())%>
				                    <%=printSelectOption("IGNORE", jobdef.getRepeatIfError())%>
				                </select>
				            </td>
				            <th>
				        	    [<%=Label.get("job.repeat.maxok")%>]
				        	</th>
				        	<td>
				                <input type="text" class="Textinput Width-100 Text-center" name="repeatMaxOk" value="<%=jobdef.getRepeatMaxOk()%>" maxlength=10>
				            </td>
				        </tr>
				        <tr id="repeat_exact_div" style="border-top:0;">
							<td style="padding:0;" colspan="4">
								<!-- <div id="repeat_exact_div"> -->
									<table class="Table njf-table__typea" style="border:0px;">
									<colgroup>
										<col style="width:24.7%"/>
										<col style="width:60.3%"/>
										<col style="width:15%"/>
									</colgroup>
									<tbody>
										<tr>
											<th>
												[<%=Label.get("job.repeat.exact.exp")%>]
											</th>
											<td>
												<input type="text" class="Textinput" name="repeatExactExp" value="<%=nvl(jobdef.getRepeatExactExp())%>">&nbsp;(HHmmss)<br>
												<%=Label.get("job.repeat.exact.hint")%>
											</td>
											<td style="border:0px;">
											    <input type="button" class="Button" value="Plan" onclick="openRepeatExactPlanWin();">
											</td>
										</tr>
									</tbody>
									</table>
								<!-- </div> -->
							</td>
				        </tr>
			        </table>
			    </td>
			</tr>
			<tr>
				<th><%=Label.get("job.confirm.need.yn")%></th>
				<td>
					<select class="Select Width-15" name="confirmNeedYN"
						><%=printYNSelectOptions(jobdef.getConfirmNeedYN())%>
					</select>
				</td>
			</tr>
			<tr>
				<th><%=Label.get("job.parallel.group")%></th>
				<td><input type="text" class="Textinput Width-100" name="parallelGroup" value="<%=nvl(jobdef.getParallelGroup())%>"></td>
			</tr>
			<tr>
				<th><%=Label.get("job.trigger")%></th>
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
				i=0;
				for (PostJobTrigger triggerJob : jobdef.getTriggerList()) {
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
				<th><%=Label.get("job.prejob")%></th>
				<td>
					<table class="Table njf-table__typea Width-100" id="prejob_table">
					<colgroup>
				        <col width="30%"/>
				        <col width="40%"/>
				        <col width="20%"/>
				        <col width="10%"/>
				    </colgroup>
				   	<thead>
						<tr>
							<th><%=Label.get("job.prejob.id")%></th>
							<th><%=Label.get("job.prejob.okfail")%></th>
							<th><%=Label.get("job.prejob.andor")%></th>
							<!-- <th><button class="Button" onclick="addPreJobRow();">＋</button></th> -->
							<th><span class="Icon Plus-sign" onclick="addPreJobRow();" style="vertical-align:middle;cursor:hand;"></span></th>
						</tr>
					</thead>
					<tbody>
			<%
				i=0;
				for (PreJobCondition preJob : jobdef.getPreJobConditions()) {
				i++;
			%>
						<tr id="prejob_<%=i%>">
							<td>
								<input type="text" class="Textinput Width-100" id="preJobId_<%=i%>" name="preJobId_<%=i%>" value="<%=nvl(preJob.getPreJobId())%>" autocomplete="off">
							</td>
							<td>
								<select class="Select Width-100" name="okFail_<%=i%>">
									<%=printSelectOption("OK" ,               preJob.getOkFailText("OK") ,               preJob.getOkFail())%>
									<%=printSelectOption("FAIL",              preJob.getOkFailText("FAIL"),              preJob.getOkFail())%>
									<%=printSelectOption("OKFAIL",            preJob.getOkFailText("OKFAIL"),            preJob.getOkFail())%>
									<%=printSelectOption("INSEXIST",          preJob.getOkFailText("INSEXIST"),          preJob.getOkFail())%>
									<%=printSelectOption("INSNONE",           preJob.getOkFailText("INSNONE"),           preJob.getOkFail())%>
									<%=printSelectOption("OK_OR_INSNONE",     preJob.getOkFailText("OK_OR_INSNONE"),     preJob.getOkFail())%>
									<%=printSelectOption("FAIL_OR_INSNONE",   preJob.getOkFailText("FAIL_OR_INSNONE"),   preJob.getOkFail())%>
									<%=printSelectOption("OKFAIL_OR_INSNONE", preJob.getOkFailText("OKFAIL_OR_INSNONE"), preJob.getOkFail())%>
									<%=printSelectOption("ALLINS_OK",         preJob.getOkFailText("ALLINS_OK"),          preJob.getOkFail())%>
									<%=printSelectOption("ALLINS_FAIL",       preJob.getOkFailText("ALLINS_FAIL"),        preJob.getOkFail())%>
									<%=printSelectOption("ALLINS_OKFAIL",     preJob.getOkFailText("ALLINS_OKFAIL"),      preJob.getOkFail())%>
								</select>
							</td>
							<td>
								<select class="Select Width-100" name="andOr_<%=i%>">
									<%=printSelectOption("AND", preJob.getAndOr())%>
									<%=printSelectOption("OR",  preJob.getAndOr())%>
								</select>
							</td>
							<%-- <td><button class="Button" onclick="delPreJobRow('prejob_<%=i%>');">－</button></td> --%>
							<td style="text-align:center"><span class="Icon Minus-sign" onclick="delPreJobRow('prejob_<%=i%>');" style="vertical-align:middle;cursor:hand;"></span></td>
						</tr>
			<%
				}
			%>
					</tbody>
					</table>
				</td>
			</tr>
		</tbody>
		</table>
		
		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Label.get("job.day.schedule")%></div>
		</div>
		
		<table class="Table njf-table__typea Width-100 Margin-bottom-0" >
	    <colgroup>
	        <col width="15%">
	        <col width="30%">
	        <col >
	    </colgroup>
	    <tbody>
			<tr>
				<th><%=Label.get("job.schedule.type")%></th>
				<td>
			        <select class="Select Width-100" name="scheduleType" onchange="changeScheduleType();">
			            <%=printSelectOption("EXPRESSION" , Label.get("job.schedule.expression"), jobdef.getScheduleType())%>
			            <%=printSelectOption("FIXED"      , Label.get("job.schedule.fixed"),      jobdef.getScheduleType())%>
			        </select>
			    </td>
			    <td>
					<select class="Select Margin-right-5" name="yyyy">
			<%
				int currYYYY = Util.toInt(Util.getCurrentYYYYMMDD().substring(0,4));
				out.println(printSelectOption(String.valueOf(currYYYY-1), String.valueOf(currYYYY)));
				out.println(printSelectOption(String.valueOf(currYYYY),   String.valueOf(currYYYY)));
				out.println(printSelectOption(String.valueOf(currYYYY+1), String.valueOf(currYYYY)));
			%>
					</select><%=Label.get("common.year")%>
					<select class="Select Margin-left-5 Margin-right-5" name="mm">		
			<%
				int currMM = Util.toInt(Util.getCurrentYYYYMMDD().substring(4,6));
				for (int mm=1; mm<=12; mm++) {
					out.println(printSelectOption(String.valueOf(mm), String.valueOf(currMM)));
				}
					
			%>
					</select><%=Label.get("common.month")%>
				    <input type="button" class="Button Margin-left-5" value="<%=Label.get("job.dayschedule.simulation.view")%>" onclick="openScheduleSimulationWin();">
				</td>
			</tr>
		</tbody>
		</table>
		
		<div id="expression_type" style="display: <%="FIXED".equals(jobdef.getScheduleType())?"none":""%>">
		    <table class="Table njf-table__typea" >
		    <colgroup>
		        <col width="15%">
		        <col>
		    </colgroup>
		    <tbody>
			    <tr>
			    	<th><%=Label.get("job.days")%></th>
			    	<td>
			    		<table class="Table non-border Width-100">
			    			<colgroup>
			    				<col style="width:200px"/>
			    				<col/>
			    			</colgroup>
			    			<tr>
			    				<td><input type="text" class="Textinput Width-60" name="months" value="<%=nvl(jobdef.getMonths())%>"><%=Label.get("job.months")%>&nbsp;&nbsp;
			    					<input type="button" class="Button" value="<%=Label.get("job.months.all")%>" onclick="document.form1.months.value='ALL'"></td>
			    				<td>
						    		<%=printRadioOptionAlx("dayOfMonthScheduleType' onchange='changeDayOfMonthScheduleType();", "NUMBER"     , "NUMBER", "NUMBER"   )%>
						    		<%=printRadioOptionAlx("dayOfMonthScheduleType' onchange='changeDayOfMonthScheduleType();", "CALENDAR"   , jobdef.getDayOfMonthScheduleType(), "CALENDAR" )%>
						    		
						            <div id="schedule_type_number_div" style="display: <%="NUMBER".equals(jobdef.getDayOfMonthScheduleType())?"":"none"%>">
						            	<input type="text" class="Textinput Width-90 Margin-top-5" name="daysInMonth" value="<%=nvl(jobdef.getDaysInMonth())%>"><%=Label.get("job.days")%>
						            </div>
						            <div id="schedule_type_calendar_div" style="display: <%="CALENDAR".equals(jobdef.getDayOfMonthScheduleType())?"":"none"%>">
						            	<select class="Select Margin-top-5" name="calendarId">
						    				<option value=""></option>
									    <%
									    	Iterator calIter = calendarMap.entrySet().iterator();
									    	while(calIter.hasNext()) {
									    		Map.Entry entry = (Map.Entry)calIter.next();
									            out.println(printSelectOption((String)entry.getKey(), "["+entry.getKey()+"] "+(String)entry.getValue(), jobdef.getCalendarId()));
									    	}
									    %>
						    			</select>&nbsp;  <%--<b>[<%=Label.get("job.calendar.expression")%>]</b> --%>
						        		<input type="text" class="Textinput Width-60 Margin-top-5" name="calendarExps" value="<%=nvl(jobdef.getCalendarExps())%>">
						        		<div class="Tooltip"><%=Label.get("form_jobdef.calendar.exp.input.help")%></div>
						        		<%=Label.get("job.days")%>
						            </div>
			    				</td>
			    			</tr>
			    		</table>
			    	</td>
			    </tr>
			    <tr>
			    	<th><%=Label.get("job.weekday")%></th>
			    	<td><%=Label.get("job.weekday_monthday.type")%>
			    		<select class="Select Width-10" name="weekdayMonthdayType">
			                <%=printSelectOption("OR" , jobdef.getWeekdayMonthdayType())%>
			                <%=printSelectOption("AND", jobdef.getWeekdayMonthdayType())%>
			            </select>
			            <input type="text" class="Textinput Width-60" name="daysOfWeek" value="<%=nvl(jobdef.getDaysOfWeek())%>"><%=Label.get("job.weekday")%>
			    		<div class="Tooltip"><%=Label.get("form_jobdef.weekday.input.help")%></div>
			    </tr>
			    <tr>
			    	<th><%=Label.get("job.schedule.pattern")%></th>
			    	<td><input type="button" class="Button" value="<%=Label.get("job.days.all")%>" onclick="document.form1.dayOfMonthScheduleType[0].checked='true';document.form1.daysInMonth.value='ALL';document.form1.calendarId.value='';document.form1.months.value='ALL';document.getElementById('schedule_type_number_div').style.display = '';document.getElementById('schedule_type_calendar_div').style.display = 'none'" >
			    	    <input type="button" class="Button" value="<%=Label.get("job.days.bizday")%>" onclick="document.form1.dayOfMonthScheduleType[1].checked='true';document.form1.daysInMonth.value='';document.form1.calendarId.value='1';document.form1.months.value='ALL';document.getElementById('schedule_type_number_div').style.display = 'none';document.getElementById('schedule_type_calendar_div').style.display = ''" >
			    	    <input type="button" class="Button" value="<%=Label.get("job.days.holiday")%>" onclick="document.form1.dayOfMonthScheduleType[1].checked='true';document.form1.daysInMonth.value='';document.form1.calendarId.value='3';document.form1.months.value='ALL';document.getElementById('schedule_type_number_div').style.display = 'none';document.getElementById('schedule_type_calendar_div').style.display = ''" >
			    	    <input type="button" class="Button" value="<%=Label.get("job.days.ondemand_manual")%>" onclick="document.form1.dayOfMonthScheduleType[0].checked='true';document.form1.daysInMonth.value='';document.form1.calendarId.value='';document.form1.months.value='';document.form1.daysOfWeek.value='';document.form1.fixedDays.value='';document.getElementById('schedule_type_number_div').style.display = '';document.getElementById('schedule_type_calendar_div').style.display = 'none'" >
			    	</td>
			    </tr>
			</tbody>
		    </table>
		</div>
		
		
		<div id="fixed_type" style="display: display: <%="FIXED".equals(jobdef.getScheduleType())?"":"none"%>">
		    <!-- <table border="2" style = "border-collapse:collapse" bordercolor = "#000000" width="95%"> -->
		    <table class="Table njf-table__typea" >
		    <colgroup>
		        <col width="15%">
		        <col>
		    </colgroup>
		    <tbody>
			    <tr>
			    	<th><%=Label.get("job.fixed")%></th>
			    	<td><input type="text" class="Textinput Width-100 Margin-bottom-5" name="fixedDays" value="<%=nvl(jobdef.getFixedDays())%>">
			    		<div class="Tooltip"><%=Label.get("form_jobdef.fixed.input.help")%></div>
			    	</td>
			    </tr>
			</tbody>
		    </table>
		</div>
		
		
		<!-- <table border="2" style = "border-collapse:collapse" bordercolor = "#000000" width="95%"> -->
		<table class="Table njf-table__typea  Margin-bottom-10" >
	    <colgroup>
	        <col width="15%">
	        <col>
	        <col width="15%">
	        <col>
	        <col width="15%">
	        <col>
	    </colgroup>
	    <tbody>
			<tr>
				<th><%=Label.get("job.before_after.day")%></th>
				<td><input type="text" class="Textinput Width-100" name="beforeAfterExp" value="<%=nvl(jobdef.getBeforeAfterExp())%>" >
					<div class="Tooltip"><%=Label.get("form_jobdef.before_after.day.input.help")%></div>
				</td>
				<th><%=Label.get("job.shift1")%></th>
				<td><input type="text" class="Textinput Width-100" name="shiftExp" value="<%=nvl(jobdef.getShiftExp())%>" >
					<div class="Tooltip"><%=Label.get("form_jobdef.shift.input.help")%></div>
				</td>
				<th><%=Label.get("job.shift2")%></th>
				<td><input type="text" class="Textinput Width-100" name="shiftExp2" value="<%=nvl(jobdef.getShiftExp2())%>" >
					<div class="Tooltip"><%=Label.get("form_jobdef.shift.input.help")%></div>
				</td>
			<tr>
			    <th><%=Label.get("job.reverse")%></th>
			    <td colspan="5"><input type="checkbox" class="Checkbox" name="reverse" value="true" <%=jobdef.isReverse() ? "checked" : ""%> ><%=Label.get("form_jobdef.reverse.input.help")%>
			    </td>
			</tr>
		</tbody>
		</table>
		
		<table class="Table njf-table__typea Margin-bottom-20" >
	    <colgroup>
	        <col width="15%">
	        <col>
	    </colgroup>
	    <tbody>
			<tr>
				<th><%=Label.get("job.createtime")%></th>
				<td><%=toDatetimeString(jobdef.getCreateTime(), false)%>
				    <input type="hidden" name="createTime" value="<%=nvl(jobdef.getCreateTime())%>">
				</td>
			</tr>
			<tr>
				<th><%=Label.get("job.lastmodifytime")%></th>
				<td><%=jobdef.getLastModifyTime() == null ? "" : toDatetimeString(DateUtil.getTimestamp(jobdef.getLastModifyTime()), false)%>
				    <input type="hidden" name="lastModifyTime" value="<%=jobdef.getLastModifyTime() == null ? "" : jobdef.getLastModifyTime()%>"  readonly>
				</td>
			</tr>
		</tbody>
		</table>

		<table class="Table Margin-bottom-10" >
		<thead>
			<tr>
				<th><%=Label.get("job.req.username")%></th>
				<th><%=Label.get("job.req.type")%></th>
		<%
			if (isOperator(request) || isAdmin(request)) {	
		%>
				<th><%=Label.get("job.req.auto.approve")%></th>
		<%
			}
		%>
				<th><%=Label.get("job.req.comment")%></th>
			</tr>
		</thead>
		<tbody>
			<tr id="param_<%=i%>">
				<td><input type="text" class="Textinput Width-100 Text-center" name="reqUserName" size=6 value="<%=getUserId(request)%>" readonly></td>
				<td><input type="text" class="Textinput Width-100 Text-center" name="reqTypeName"  readonly value="<%=
						    "add".equals(mode) || "copy".equals(mode)  ? Label.get("common.new"):
						    "edit".equals(mode)   ? Label.get("common.edit"):
						    "delete".equals(mode) ? Label.get("common.delete") : ""%>">
					<input type="hidden" name="reqType" value="<%="copy".equals(mode) ? "add" : mode%>">
				</td>
		<%
			if (isOperator(request) || isAdmin(request)) {	
		%> 
				<td><input type="checkbox" class="Checkbox" name="autoApproval" value="true"></td>
		<%
			}
		%>
				<td ><input type="text" class="Textinput Width-100" name="reqComment"></td>
			</tr>
		</table>
		
		<input type="submit" class="Button Large" value="<%=
		    "add".equals(mode)    ? Label.get("jobdef.btn.req.new"):
		    "edit".equals(mode)   ? Label.get("jobdef.btn.req.edit"):
		    "delete".equals(mode) ? Label.get("jobdef.btn.req.delete"):
		    "copy".equals(mode)   ? Label.get("jobdef.btn.req.copy"):Label.get("common.btn.submit")%>">
		</form>
	</div>
</center>
</body>
</html>
