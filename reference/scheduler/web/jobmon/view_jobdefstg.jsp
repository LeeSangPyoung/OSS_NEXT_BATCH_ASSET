<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<jsp:include page="top.jsp" flush="true"/>
<%
    String returnUrl      = null; 
    String returnUrlPlain = null;
    if (request.getQueryString()==null) {
        returnUrl = request.getRequestURI();
        returnUrlPlain = request.getRequestURI();
    }else {
        returnUrl = request.getRequestURI()+"?"+URLEncoder.encode(request.getQueryString());
        returnUrlPlain = request.getRequestURI()+"?"+request.getQueryString();
    }

	final ControllerAdminLocal admin = getControllerAdmin();
	final User user = admin.getUser(getUserId(request)); /* 권한 변경사항을 바로 적용하기 위해 DB 에서 다시 읽는다 */
%>

<script>
	$a.page(function() {
	    // 초기화 함수
	    this.init = function(id, param) {
	    	$("[id^='tableList']").css({'table-layout':'auto'});
	    }
	});		

	function openJobDefinitionStgDtlWin(reqNo, jobid) {
		window.open("view_jobdefstg_dtl.jsp?reqno="+reqNo+"&jobid="+jobid, 'jobdefstg_dtl_'+reqNo, 'width=820,height=800,scrollbars=1').focus();
	}
	
    function openJobDefinitionStgDiffWin(reqNo, jobid) {
        window.open("view_jobdefstg_diff.jsp?reqno="+reqNo+"&jobid="+jobid, 'jobdefstg_diff'+reqNo, 'width=820,height=800,scrollbars=1').focus();
    }

    function openJobDefinitionWin(jobid) {
        window.open("view_jobdef_dtl.jsp?jobid="+jobid, 'jobdef_'+jobid.replace(/-/g, ''), 'width=820,height=800,scrollbars=1').focus();
    }
    
    function checkAll() {
        var chk = document.form2.chkreqno_jobid;
        var v = document.form2.chkall.checked;
        
        if (typeof chk =="undefined") return false;
        
        if (chk.length == null) { /* 하나일때 */
            chk.checked = v ;
        }else {
            for (i=0; i<chk.length; i++ ) {
                chk[i].checked = v ;
            }
        }
    }
    
    function getCheckedCount(chk) {
    	if (typeof chk =="undefined") return false;
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
    
    function getCheckedReqList() {
        var chk = document.form2.chkreqno_jobid;
        var reqlist = '';
        if (chk.length == null) {
            if (chk.checked) {
            	reqlist = reqlist + chk.value ;
            }
        }else { 
            var checkedCount = 0;
            for (i=0; i<chk.length; i++ ) {
                if (chk[i].checked) {
                    checkedCount++;
                    if (checkedCount <= 15) {
                    	reqlist = reqlist + '\r['+chk[i].value+']';
                    }
                }
            }

            if (checkedCount > 15) {
            	reqlist = reqlist + "...";
            }

            if (checkedCount > 0) {
            	reqlist = reqlist + "\r(Total : "+checkedCount+")";
            }
        }
        return reqlist;
    }
    function multiApproval() {
    	if (getCheckedCount(document.form2.chkreqno_jobid) == 0) {
    		alert("Not checked");
    	}else {
	    	var reqlist = getCheckedReqList();
	    	if (reqlist.length > 0) {
		        if (confirm("<%=Label.get("view_jobdefstg.multi.approve.confirm.msg")%> "+reqlist)) {
		            document.form2.cmd.value="admin_multi_approve";
		            document.form2.submit();
		        }
	    	}
    	}
    }

    function multiReject() {
    	if (getCheckedCount(document.form2.chkreqno_jobid) == 0) {
    		alert("Not checked");
    	}else {
	    	var reqlist = getCheckedReqList();
	    	if (reqlist.length > 0) {
		        if (confirm("<%=Label.get("view_jobdefstg.multi.reject.confirm.msg")%> "+reqlist)) {
		            document.form2.cmd.value="admin_multi_reject";
		            document.form2.submit();
		        }
	    	}
    	}
    }
    
    function validateInputData() {
    	var regex = /(\d{8})/;
    	if (!document.form1.reqTimeFrom.value.match(regex) || !document.form1.reqTimeTo.value.match(regex)) {
    		alert("Date format error");
    		return false;
    	}else {
    		return true;
    	}
    }

</script>

<center>

<div class="content-wrap">

<div class="content-title__wrap">
	<div class="content-title">Job 등록요청현황</div>
</div>

<%
	String reqUserName = request.getParameter("reqUserName"); /* 실제로는 user id 가 들어옴. name 에서 id 로 변경 */
	String jobId       = request.getParameter("jobId");
	String jobDesc     = request.getParameter("jobDesc");
	String reqState    = nvl(request.getParameter("reqState"), "%");
	String reqTimeFrom = Util.nvlBlank(request.getParameter("reqTimeFrom"), Util.getCurrentYYYYMMDD());
	String reqTimeTo   = Util.nvlBlank(request.getParameter("reqTimeTo"),   Util.getCurrentYYYYMMDD());

	StringBuilder query = new StringBuilder(100);

	if (!Util.isBlank(reqUserName)) {
		query.append("REQ_USERNAME LIKE '"+reqUserName+"%' ");
	}else if(!user.isAdmin() && !user.isOperator())  { /* 일반 사용자는 자기꺼만 보이게 한다. */
		query.append("REQ_USERNAME = '"+getUserId(request)+"' ");
	}
	if (!Util.isBlank(jobId)) {
        if (query.length() > 0) query.append(" AND ");
		query.append("JOB_ID LIKE '"+jobId+"%' ");
	}
	if (!Util.isBlank(jobDesc)) {
        if (query.length() > 0) query.append(" AND ");
		query.append("JOB_DESC LIKE '%"+jobDesc+"%' ");
	}
	
    if (query.length() > 0) query.append(" AND ");
	if ("%".equals(reqState) || "Q".equals(reqState)) { /* for all or request state */
		query.append(" ( REQ_STATE LIKE 'Q%' OR "); /* 일자와 상관없이 대기상태인것 보여줌 */
		query.append("   REQ_STATE LIKE '"+reqState+"%' AND REQ_TIME BETWEEN '"+reqTimeFrom+"000000' AND '"+reqTimeTo+"240000' ) ");
	}else {
		query.append("   REQ_STATE LIKE '"+reqState+"%' AND REQ_TIME BETWEEN '"+reqTimeFrom+"000000' AND '"+reqTimeTo+"240000' ");
	}

	String queryAll = null;
	if(query.length() > 0){
		queryAll = "WHERE "+query.toString();
		if(isSybaseDB() || isMSSQL()){ //사이베이스
			queryAll += " ORDER BY (case substring(REQ_STATE,1,1) when 'Q' then 1 else 2 end) ";
		} else {
			queryAll += " ORDER BY (case when substr(REQ_STATE, 1, 1) = 'Q' then 1 else 2 end) ";
		}
		queryAll += ", REQ_TIME DESC, REQ_NO DESC ";
	}
	else {
		queryAll = " ORDER BY REQ_TIME DESC, REQ_NO DESC ";
	}
%>

<form name="form1" action="view_jobdefstg.jsp" method="get" onsubmit="return validateInputData();">
<input type="hidden" name="cmd" value="">

<table class="Table njf-table__typea Margin-bottom-10">
<tr>
<%
    if (user.isAdmin() || user.isOperator()) {
%>
	<th style="width:7%; padding:2px"><%=Label.get("job.req.user")%> ID</th>
	<td style="width:10%; padding:2px"><input class="Textinput Width-100" type="text" name="reqUserName" value="<%=conv(reqUserName)%>"></td>
<%
    }
%>
	<th style="width:7%; padding:2px"><%=Label.get("job.jobid")%></th>
	<td style="width:12%; padding:2px"><input class="Textinput Width-100" type="text" name="jobId" value="<%=nvl(jobId)%>"></td>
	
	<th style="width:4%; padding:2px"><%=Label.get("job.desc")%></th>
	<td style="width:13%; padding:2px"><input class="Textinput Width-100" type="text" name="jobDesc" value="<%=nvl(jobDesc)%>"></td>
	
	<th style="width:7%; padding:2px"><%=Label.get("job.req.time")%></th>
	<td style="width:20%; padding:2px; text-align:center">
		<input class="Textinput Width-45 Margin-right-5" type="text" name="reqTimeFrom" value="<%=nvl(reqTimeFrom)%>" maxlength="8">~
		<input class="Textinput Width-45" type="text" name="reqTimeTo"   value="<%=nvl(reqTimeTo)%>" maxlength="8">
	</td>
	
	<th style="width:6%; padding:2px"><%=Label.get("job.req.state")%></th>
	<td style="width:9%; padding:2px">
    	<select class="Select Width-100" name="reqState">
            <%=printSelectOption("%",  Label.get("common.etc.all"),     reqState)%>
            <%=printSelectOption("Q", Label.get("job.req.state.requested"), reqState)%>
            <%=printSelectOption("A", Label.get("job.req.state.approved"),  reqState)%>
            <%=printSelectOption("R", Label.get("job.req.state.rejected"),  reqState)%>
        </select>
	</td>
	
	<td style="width:5%; padding:2px; text-align:center"><input class="Button" type="submit" value="<%=Label.get("common.btn.query")%>"></td>
</tr>
</table>
</form>
<form name="form2" action="action_jobdef.jsp" method="post">
<input type="hidden" name="cmd" value="">
<input type="hidden" name="returnurl" value="<%=returnUrlPlain%>">

<table class="Width-100">
<tr>
	<td colspan="100%" style="font-size:12px; text-align:right"><%=toDatetimeString(new java.util.Date(), false) %></td>
</tr>
</table>

<table class="Table Width-100 Margin-bottom-10" id="tableList">
<thead>
<tr>
<th style="width:3%; padding:2px;"><input class="Checkbox" type="checkbox" id="chkall" name="chkall" onclick="checkAll();"></th>
<th style="width:3%; padding:2px;" nowrap>#</th>
<th style="padding:2px;" nowrap><%=Label.get("job.req.no")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.req.username.short")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.req.userip")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.jobgroup")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.jobid")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.jobtype")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.desc")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.req.comment")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.req.type")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.req.state.approved")%>/<%=Label.get("job.req.state.rejected")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.req.approver")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.req.appr_rej.cause")%></th>
<th style="padding:2px;" nowrap><%=Label.get("job.req.time")%></th>
</tr>
</thead>
<tbody>
<%!
    void printTr(JobDefinitionStg jobdef, int i, JspWriter out) throws IOException {
		out.println("<tr>");
		out.println("<td style=\"padding:0px;\"><input class=\"Checkbox\" type=\"checkbox\" id=\"chkreqno_jobid\" name=\"chkreqno_jobid\" value=\""+jobdef.getReqNo()+"_"+jobdef.getJobId()+"\"></td>");
		out.println("<td style=\"padding:2px;\">"+i+"</td>");
		out.println("<td style=\"padding:2px;\"><b><a href=\"javascript:openJobDefinitionStgDtlWin('"+nvl(jobdef.getReqNo())+"', '"+nvl(jobdef.getJobId())+"');\">"+nvl(jobdef.getReqNo())+"</a></b></td>");
		out.println("<td style=\"padding:2px;\">"+conv(jobdef.getReqUserName())+"</td>");
		out.println("<td style=\"padding:2px;\">"+nvl(jobdef.getReqUserIp())+"</td>");
		out.println("<td style=\"padding:2px;\">"+nvl(jobdef.getJobGroupId())+"</td>");
		out.println("<td style=\"padding:2px;\"><a href=\"javascript:openJobDefinitionWin('"+jobdef.getJobId()+"');\" title=\"Master JobDefiniton\">"+nvl(jobdef.getJobId())+"</a></td>");
		out.println("<td style=\"padding:2px;\">"+getJobTypeText(jobdef.getJobType())+"</td>");
		out.println("<td style=\"padding:2px;\" class=\"Text-left\"><b>"+getAppCode(jobdef.getJobId())+"</b> "+getShortDescription(jobdef.getDescription())+"</td>");
		out.println("<td style=\"padding:2px;\">"+shortenRight(jobdef.getReqComment(), 20)+"</td>");
		out.println("<td style=\"padding:2px;\">"+(
			"add".equals(jobdef.getReqType())?Label.get("reqtype.add") : 
			"edit".equals(jobdef.getReqType())?Label.get("reqtype.edit") : 
			"delete".equals(jobdef.getReqType())?Label.get("reqtype.delete") : "N/A")+"</td>");
		if (jobdef.getReqState().startsWith("Q")) {
		    out.println("<td style=\"padding:2px;\"><A href=\"javascript:openJobDefinitionStgDiffWin('"+nvl(jobdef.getReqNo())+"', '"+nvl(jobdef.getJobId())+"');\"><font color=#009900><b>▶"+Label.get("job.req.state.requested")+"◀</b></font></a></td>");
		}else {
		    out.println("<td style=\"padding:2px;\">"+(
		    	jobdef.getReqState().startsWith("A")?"<font color=#0000FF>"+Label.get("job.req.state.approved")+"</font>":
				jobdef.getReqState().startsWith("R")?"<font color=#FF0000>"+Label.get("job.req.state.rejected")+"</font>":"N/A")+"</td>");
		}
		out.println("<td style=\"padding:2px;\">"+conv(jobdef.getReqOperatorName())+"("+nvl(jobdef.getReqOperatorId())+")/"+nvl(jobdef.getReqOperatorIp())+"</td>");
		out.println("<td style=\"padding:2px;\">"+conv(jobdef.getReqARReason())+"</td>");
		out.println("<td style=\"padding:2px;\">"+toDatetimeString(jobdef.getReqTime(), false)+"</td>");
		out.println("</tr>");
    }
%>
<%
	final JspWriter out2 = out;
    RowHandler rh = new RowHandler() {
    	int i = 0;
    	public void handleRow(Object row) {
    		JobDefinitionStg jobdef = (JobDefinitionStg)row;
    	    // 운영자인 경우는 자기 권한의 것들만 보여준다.
    	    // 사용자는 쿼리할때 자기것만 쿼리 된다.
    	    // 관리자는 다 보여준다.
    	    if (user.isOperator()) { // 운영자인 경우는 자기 권한의 것들만 보여준다.
    	    	if (!admin.isAllowedForOperation(jobdef.getJobGroupId(), jobdef.getJobId(), user)) {
    	    		// 운영 권한이 없더라도 내가 요청한 것이라면 display 한다.
    	    		if (!user.getId().equals(jobdef.getReqUserName())) { 
    	            	return;
    	    		}
    	        }
    	    }
    	    try {
    	    	printTr(jobdef, ++i, out2);
    	    }catch(IOException e) {
    	    	e.printStackTrace();
    	    }
    	}
    };
    
    /* 대량 조회시 OOME 를 방지하기 위해 RowHandler 로 한다. */
	admin.getJobDefinitionStgListWithRH(queryAll, rh);
%>
</tbody>
</table>

<%
    if (user.isOperator()) {
%>
        <input class="Button Margin-bottom-10" type="button" value="<%=Label.get("job.req.action.approve")%>" onclick="multiApproval();">
        <input class="Button Margin-bottom-10" type="button" value="<%=Label.get("job.req.action.reject")%>" onclick="multiReject();">     
<%
    }
%>
</form>
</div>
</center>
</div>
</div>
<jsp:include page="bottom.jsp" flush="true"/>
</body>
</html>
