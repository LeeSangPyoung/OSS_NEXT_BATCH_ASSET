<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<jsp:include page="top.jsp" flush="true"/>
<%
    String jspUrlQueryString = request.getQueryString();
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

<%
    /* Job 등록정보 조회의 상단 검색 조건만 표시하는 페이지 */
    String jobid        = nvl(request.getParameter("jobid")).trim();
    String jobgroup     = nvl(request.getParameter("jobgroup")).trim();
    String owner        = nvl(request.getParameter("owner")).trim();
    String jobtype      = nvl(request.getParameter("jobtype")).trim();
    String agentid      = nvl(request.getParameter("agentid")).trim();
    String component    = nvl(request.getParameter("component")).trim();
    String desc         = nvl(request.getParameter("desc")).trim();
    String prejobid     = nvl(request.getParameter("prejobid")).trim();
    String triggerjobid = nvl(request.getParameter("triggerjobid")).trim();
    String viewfilter   = nvl(request.getParameter("viewfilter"));
    String jobfilter    = (jobfilter=nvl(request.getParameter("jobfilter"))).length() == 0 ? null : jobfilter;
    String planDate     = nvl(request.getParameter("plandate"));

    ControllerAdminLocal admin = getControllerAdmin();
    List<String> agentIdList = admin.getAgentIdList();
    
    String orderby        = nvl(request.getParameter("orderby"),  "jobId");
    String orderdir       = nvl(request.getParameter("orderdir"), "ASC");     // ASC, DESC
%>

<script>
    function clearQueryCondition() {
        document.form1.jobid.value='';
        document.form1.jobgroup.value='';
        document.form1.owner.value='';
        document.form1.desc.value='';
        document.form1.jobtype.value='';
        document.form1.agentid.value='';
    }
    
    function doQuery() {
        document.form1.action="view_jobdef.jsp";
		document.form1.target='_self';
        document.form1.submit();
    }

    function doExcelDownload() {
        document.form1.action="view_jobdef_excel.jsp";
		document.form1.deep_query.value='true';
        document.form1.submit();
		document.form1.target='';
		document.form1.deep_query.value='';
    }

    function openDiagramWin() {
        window.open('', 'jobdef_diagram_win', 'width=1200,height=800,scrollbars=1').focus();
        document.form1.action="view_jobdef_diagram.jsp";
		document.form1.deep_query.value='true';
		document.form1.target="jobdef_diagram_win";
        document.form1.submit();
		document.form1.target='';
		document.form1.deep_query.value='';
    }
	
    function openJobDefinitionFormWin(jobid, mode) {
        window.open("form_jobdef.jsp?jobid="+jobid+"&mode="+mode, '', 'width=820,height=800,scrollbars=1').focus();
    }

    function openJobDefinitionUploadFormWin(jobid) {
        window.open("form_jobdef_upload.jsp", 'upload', 'width=810,height=500,scrollbars=1').focus();
    }

    function openViewFilterMgrWin() {
        window.open("view_viewfilter_list.jsp", 'viewfiltermgr', 'width=810,height=400,scrollbars=1').focus();
    }
    
    function openJobGroupSelectWin(targetElemName) {
        window.open("popup_jobgroup.jsp?target_name="+targetElemName, 'jobgroupselect', 'width=700,height=400,scrollbars=1').focus();
    }
    

</script>

	<center>
		<div class="content-wrap">
			<div class="content-title__wrap">
				<div class="content-title"><%=Label.get("top.menu.jobdef")%></div>
			</div>
			<form name="form1" action="view_jobdef.jsp" method="get">
			<input type="hidden" name="deep_query" value="">
			<input type="hidden" name="orderby"  value="<%=orderby%>">
			<input type="hidden" name="orderdir" value="<%=orderdir%>">
			<table class="Table njf-table__typea Margin-bottom-10">
			<colgroup>
				<col style="width:4%"/>
				<col style="width:8%"/>
				<col style="width:5%"/>
				<col style="width:12%"/>
				<col style="width:5%"/>
				<col style="width:10%"/>
				<col style="width:5%"/>
				<col style="width:8%"/>
				<col style="width:5%"/>
				<col style="width:14%"/>
				<col style="width:4%"/>
				<col style="width:14%"/>
				<col style="width:6%"/>
			</colgroup>
			<tbody>
				<tr>
				    <th><a href="javascript:openViewFilterMgrWin();"><%=Label.get("viewfilter")%></a></th>
					<td style="text-align:center; padding:2px">
						<select class="Select Width-100" name="viewfilter">
				            <option value=""></option>
				            <%=printViewFilterSelect(admin, viewfilter)%>
				        </select>
				    </td>
					<th><%=Label.get("job.jobid")%></th>
					<td style="text-align:center; padding:2px"><input type="text" class="Textinput Width-80 Margin-right-5" name="jobid" value="<%=nvl(jobid)%>" >%</td>
					<th><a href="javascript:openJobGroupSelectWin('jobgroup');"><%=Label.get("job.jobgroup")%></a></th>
					<td style="text-align:center; padding:2px"><input type="text" class="Textinput Width-80 Margin-right-5" name="jobgroup" value="<%=nvl(jobgroup)%>" >%</td>
					<th><%=Label.get("common.search.gubun")%></th>
					<td style="text-align:center; padding:2px">
						<select class="Select Width-100" name="jobfilter">
				            <%=printJobFilter(jobfilter, request)%>
				        </select>
					</td>
					<th><%=Label.get("jobdef.plandate")%></th>
					<td style="text-align:center; padding:2px"><input type="text" class="Textinput Width-70" name="plandate" value="<%=conv(planDate)%>" maxlength="8"></td>
					<th><%=Label.get("job.desc")%></th>
					<td style="text-align:center; padding:2px">%<input type="text" class="Textinput Width-70 Margin-left-5 Margin-right-5" name="desc" value="<%=conv(desc)%>" >%</td>
					<td style="padding:2px" rowspan="2">
				        <input type="submit" class="Button Width-100" style="line-height:62px; padding:0px" value="<%=Label.get("common.btn.query")%>" onclick="return doQuery();"><!-- <img alt="Search" src="images/search-icon.png"> -->
				    </td>
				</tr>
				<tr>
				    <th><%=Label.get("common.server")%></th>
					<td style="text-align:center; padding:2px">
						<select class="Select Width-100" name="agentid">
				            <%=printSelectOption("",       "",       agentid)%>
				<%  for (String _agentId : agentIdList) {          %>
				            <%=printSelectOption(_agentId, _agentId, agentid)%>
				<%  }                                               %>
				        </select>
					</td>
					<th><%=Label.get("job.prejob")%></th>
					<td style="text-align:center; padding:2px"><input type="text" class="Textinput Width-80 Margin-right-5" name="prejobid" value="<%=nvl(prejobid)%>">%</td>
					<th><%=Label.get("job.trigger")%></th>
					<td style="text-align:center; padding:2px"><input type="text" class="Textinput Width-80 Margin-right-5" name="triggerjobid" value="<%=nvl(triggerjobid)%>">%</td>
					<th><%=Label.get("job.jobtype")%></th>
					<td style="text-align:center; padding:2px">
						<select class="Select Width-100" name="jobtype" style="font-size:11px;">
				            <%=printSelectOption("" ,"" ,jobtype)%>
				            <%=printJobTypeSelectOptionList(admin, jobtype) %>
				        </select>
				    </td>
					<th><%=Label.get("job.component")%></th>
					<td style="text-align:center; padding:2px">%<input type="text" class="Textinput Width-70 Margin-left-5 Margin-right-5" name="component" value="<%=conv(component)%>">%</td>
					<th><%=Label.get("job.owner")%></th>
					<td style="text-align:center; padding:2px">%<input type="text" class="Textinput Width-70 Margin-left-5 Margin-right-5" name="owner" value="<%=conv(owner)%>">%</td>
				</tr>
			</tbody>
			</table>
			
			<!-- top of table -->
			<table class="Width-100">
				<tr>
					<td class="Text-left">
						<input type="button" class="Button" value="<%=Label.get("common.btn.download")%>" onclick="doExcelDownload();">
						<input type="button" class="Button" value="diagram" onclick="openDiagramWin();">
						<input type="button" class="Button" value="<%=Label.get("jobdef.btn.new.jobdef.form")%>" onclick="openJobDefinitionFormWin('', 'add');">
				        <input type="button" class="Button" value="<%=Label.get("jobdef.btn.jobdef.excel.upload.form")%>" onclick="openJobDefinitionUploadFormWin();">
					</td>
				    <td style="font-size:12px; text-align:right; vertical-align:bottom;"><%=toDatetimeString(new java.util.Date(), false) %></td>
				</tr>
			</table>

			</form>
			
			<jsp:include page="view_jobdef_table.jsp"/>
			
		</div>
	</center>
</div>
</div>
<jsp:include page="bottom.jsp" flush="true"/>	
</body>
</html>

