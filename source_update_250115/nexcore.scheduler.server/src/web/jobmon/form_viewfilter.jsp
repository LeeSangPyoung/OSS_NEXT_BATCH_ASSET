<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>

<html>
<head>
<title><%=Label.get("viewfilter.edit")%></title>
<script src="./script/app/include-lib.js"></script>
<script>
    function checkAll1() {
        var chk = document.form1.chkjobid1;
        var v = document.form1.chkall1.checked;
        
        if (typeof chk =="undefined") return false;
        
        if (chk.length == null) { /* 하나일때 */
            chk.checked = v ;
        }else {
            for (i=0; i<chk.length; i++ ) {
                chk[i].checked = v ;
            }
        }
    }

    function checkAll2() {
        var chk = document.form1.chkjobid2;
        var v = document.form1.chkall2.checked;
        
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
    
    function modifyViewFilter() {
        if (confirm("<%=Label.get("common.modify.confirm.msg")%>")) {
            document.form1.cmd.value="modify_viewfilter";
            document.form1.submit();
            opener.location.reload();
        }
    }
    
    function searchJobList() {
        document.form1.action="form_viewfilter.jsp";
        document.form1.submit();
    }
    
    function addJobListToFilter() {
		if (getCheckedCount(document.form1.chkjobid2) == 0) {
			alert("Not checked");
			return;
		}
		
        document.form1.cmd.value="add_jobid";
        document.form1.submit();
        opener.location.reload();
    }

    function removeJobListFromFilter() {
		if (getCheckedCount(document.form1.chkjobid1) == 0) {
			alert("Not checked");
			return;
		}
		
        document.form1.cmd.value="remove_jobid";
        document.form1.submit();
        opener.location.reload();
    }

</script>
</head>
<body>
<center>

	<div class="header-wrap">
		<div class="header">
			<div class="header-title">
				<%=Label.get("viewfilter.edit")%>
			</div>
			<div class="header-close-button">
		    	<span><a href="#" onclick="window.close();"><img alt="팝업창 닫기" src="images/pop_close.png"></a></span>
			</div>
		</div>
	</div>

<div class="popup-content-wrap">
<%
    int vfid = toInt(request.getParameter("id"),-1);

    ControllerAdminLocal admin = getControllerAdmin();
    ViewFilter          vf         = admin.getViewFilterDeep(vfid);
    List<JobDefinition> jobdefList = admin.getJobDefinitionsByViewFilter(vfid);
%>
<br>
<form name="form1" action="action_viewfilter.jsp" method="POST">
<input type="hidden" name="cmd" value="">
<input type="hidden" name="id" value="<%=vfid%>">
	<table class="Table">
		<colgroup>
			<col style="width:20%"/>
			<col style="width:10%"/>
			<col style="width:10%"/>
			<col style="width:20%"/>
			<col style="width:7%"/>
			<col style="width:10%"/>
			<col style="width:7%"/>
		</colgroup>
		<thead>
		<tr>
		    <th><%=Label.get("viewfilter.name")%></th>
		    <th><%=Label.get("viewfilter.team")%></th>
		    <th><%=Label.get("viewfilter.owner")%></th>
		    <th><%=Label.get("viewfilter.desc")%></th>
		    <th><%=Label.get("viewfilter.jobcount")%></th>
		    <th><%=Label.get("common.createtime")%></th>
		    <th/>
		</tr>
		</thead>
		<tr>
		    <td><input type="text" class="Textinput Width-100" name="name" value="<%=conv(vf.getName())%>" size="25"></td>
		    <td><input type="text" class="Textinput Width-100" name="team" value="<%=conv(vf.getTeam())%>" size="8%"></td>
		    <td><input type="text" class="Textinput Width-100" name="owner" value="<%=conv(vf.getOwner())%>" size="7"></td>
		    <td><input type="text" class="Textinput Width-100" name="description" value="<%=conv(vf.getDescription())%>" size="25"></td>
		    <td><%=vf.getJobCount()%></td>
		    <td><%=vf.getLastModifyTime()!=null?toDatetimeString(DateUtil.getTimestamp(vf.getLastModifyTime()), true):""%></td>
		    <td><input type="button" class="Button" value="<%=Label.get("common.btn.edit")%>" onclick="modifyViewFilter();"></td>
		</tr>
	</table>
<%-- <br><br>
<font size="3"><b><%=Label.get("viewfilter.joblist")%></b></font>
<br><br> --%>
<div class="popup-content-title__wrap">
	<div class="content-title"><%=Label.get("viewfilter.joblist")%></div>
</div>
	<table class="Table">
		<colgroup>
			<col style="width:7%"/>
			<col style="width:15%"/>
			<col style="width:15%"/>
			<col style="width:15%"/>
			<col style="width:10%"/>
			<col style="width:15%"/>
			<col style="width:15%"/>
			<col style="width:10%"/>
		</colgroup>
		<thead>
		<tr>
		    <th><%=Label.get("common.delete")%><input type="checkbox" name="chkall1" onclick="checkAll1();"></th>
		    <th><%=Label.get("job.jobid")%></th>
		    <th><%=Label.get("job.jobgroup")%></th>
		    <th><%=Label.get("job.jobtype")%></th>
		    <th><%=Label.get("job.owner")%></th>
		    <th><%=Label.get("job.component")%></th>
		    <th><%=Label.get("job.desc")%></th>
		    <th><%=Label.get("job.createtime")%></th>
		</tr>
		</thead>
		<%
		    Set<String> filterJobIdList     = new TreeSet(vf.getJobIdList()); /* 등록된 JobId 리스트. */
		    Set<String> filterJobIdListCopy = new TreeSet(vf.getJobIdList()); /* 등록된 JobId 리스트. 뒤에서 사용됨 */
		    for (JobDefinition jobdef : jobdefList) {
		        filterJobIdList.remove(jobdef.getJobId());
		%>
		<tr>
		    <td><input type="checkbox" name="chkjobid1" value="<%=jobdef.getJobId()%>"></td>
		    <td><%=jobdef.getJobId()%></td>
		    <td><%=jobdef.getJobGroupId()%></td>
		    <td><%=getJobTypeText(jobdef.getJobType())%></td>
		    <td><%=conv(jobdef.getOwner())%></td>
		    <td><%=conv(jobdef.getComponentName())%></td>
		    <td align="left"><%=conv(jobdef.getDescription())%></td>
		    <td><%=toDatetimeString(nvl(jobdef.getCreateTime()))%></td>
		</tr>
		<%
		    }
		    /* 등록되지 않은 Job 중에 필터에 있는 Job  */
		    for (String jobid : filterJobIdList) {
		%>
		<tr align="center">
		    <td><input type="checkbox" name="chkjobid1" value="<%=jobid%>"></td>
		    <td><%=jobid%></td>
		    <td colspan="6"><< <%=Label.get("viewfilter.undefined.job.list")%> >> </td>
		</tr>
		<%
		    }
		%>
		<tr align="center">
		    <td colspan="2"><input type="button" class="Button" value="<%=Label.get("viewfilter.remove.from.filter")%>" onclick="removeJobListFromFilter()"></td>
		    <td colspan="6"></td>
		</tr>
	</table>
<br><br>

<%
    /* 필터에 등록할 Job 추가.  */
    String jobid     = nvl(request.getParameter("jobid"));
    String jobgroup  = nvl(request.getParameter("jobgroup"));
    String jobowner  = nvl(request.getParameter("jobowner"));
    String jobdesc   = nvl(request.getParameter("jobdesc"));
%>
<b><font color="blue"><%=Label.get("viewfilter.select.job.help")%></font></b><br><br>

	<table class="Table njf-table__typea Margin-bottom-10">
		<colgroup>
			<col style="width:10%"/>
			<col style="width:15%"/>
			<col style="width:10%"/>
			<col style="width:15%"/>
			<col style="width:10%"/>
			<col style="width:15%"/>
			<col style="width:10%"/>
			<col style="width:15%"/>
			<col style="width:10%"/>
		</colgroup>
		<tbody>
		<tr>
		    <th><%=Label.get("job.jobid")%></th>
		    <td><input type="text" class="Textinput Width-100" name="jobid" value="<%=jobid%>" size="10"></td>
		    <th><%=Label.get("job.jobgroup")%></th>
		    <td><input type="text" class="Textinput Width-100" name="jobgroup" value="<%=jobgroup%>" size="10"></td>
		    <th><%=Label.get("job.owner")%></th>
		    <td><input type="text" class="Textinput Width-100" name="jobowner" value="<%=jobowner%>" size="8"></td>
		    <th><%=Label.get("job.desc")%></th>
		    <td><input type="text" class="Textinput Width-100" name="jobdesc" value="<%=jobdesc%>" size="10"></td>
		    <td><input type="submit" class="Button" value="<%=Label.get("common.btn.query")%>" onclick="searchJobList();"></td>
		</tr>
		</tbody>
	</table>
	
	<table class="Table">
		<%
		    List<JobDefinition> searchJobdefList = null;
		    StringBuilder searchJobQuery = new StringBuilder(100);
		    if (!Util.isBlank(jobid)) {
		        searchJobQuery.append("JOB_ID LIKE '"+jobid+"%' ");
		    }
		    if (!Util.isBlank(jobgroup)) {
		        if (searchJobQuery.length() > 0) searchJobQuery.append(" AND ");
		        searchJobQuery.append("JOB_GROUP_ID LIKE '%"+jobgroup+"%' ");
		    }
		    if (!Util.isBlank(jobowner)) {
		        if (searchJobQuery.length() > 0) searchJobQuery.append(" AND ");
		        searchJobQuery.append("OWNER LIKE '%"+jobowner+"%' ");
		    }
		    if (!Util.isBlank(jobdesc)) {
		        if (searchJobQuery.length() > 0) searchJobQuery.append(" AND ");
		        searchJobQuery.append("JOB_DESC LIKE '%"+jobdesc+"%' ");
		    }
		    if (searchJobQuery.length() > 0) {
		        searchJobdefList = admin.getJobDefinitionList("WHERE "+searchJobQuery.toString());
		    }else {
		        searchJobdefList = new ArrayList();
		    }
		
		    Collections.sort(searchJobdefList, new Comparator () {
		        public int compare(Object o1, Object o2) {
		            return ((JobDefinition)o1).getJobId().compareTo(((JobDefinition)o2).getJobId());
		        }
		    });
		%>
		<colgroup>
			<col style="width:7%"/>
			<col style="width:15%"/>
			<col style="width:15%"/>
			<col style="width:15%"/>
			<col style="width:10%"/>
			<col style="width:15%"/>
			<col style="width:15%"/>
			<col style="width:10%"/>
		</colgroup>
		<thead>
		<tr>
		    <th><%=Label.get("common.btn.add")%><input type="checkbox" name="chkall2" onclick="checkAll2();"></th>
		    <th><%=Label.get("job.jobid")%></th>
		    <th><%=Label.get("job.jobgroup")%></th>
		    <th><%=Label.get("job.jobtype")%></th>
		    <th><%=Label.get("job.owner")%></th>
		    <th><%=Label.get("job.component")%></th>
		    <th><%=Label.get("job.desc")%></th>
		    <th><%=Label.get("job.createtime")%></th>
		</tr>
		</thead>
		<%
		    for (JobDefinition jobdef : searchJobdefList) {
		        if (filterJobIdListCopy.contains(jobdef.getJobId())) {
		            continue;
		        }
		%>
		<tr>
		    <td><input type="checkbox" name="chkjobid2" value="<%=jobdef.getJobId()%>"></td>
		    <td><%=jobdef.getJobId()%></td>
		    <td><%=jobdef.getJobGroupId()%></td>
		    <td><%=getJobTypeText(jobdef.getJobType())%></td>
		    <td><%=conv(jobdef.getOwner())%></td>
		    <td><%=conv(jobdef.getComponentName())%></td>
		    <td align="left"><%=conv(jobdef.getDescription())%></td>
		    <td><%=toDatetimeString(nvl(jobdef.getCreateTime()))%></td>
		</tr>
		<%
		    }
		%>
		<tr>
		    <td colspan="2"><input type="button" class="button" value="<%=Label.get("viewfilter.add.to.filter")%>" onclick="addJobListToFilter();"></td>
		    <td colspan="6"></td>
		</tr>
	</table>
</form>
<br>
<input type="button" class="Button Large" value="<%=Label.get("common.btn.close")%>" onclick="window.close();">
<br><br>
</div>
</center>

</body>
</html>


