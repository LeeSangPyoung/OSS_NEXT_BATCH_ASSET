<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>

<%
	ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth auth       = new AdminAuth(getUserId(request), getUserIp(request));
    String cmd           = request.getParameter("cmd");
    String orderby       = nvl( request.getParameter("orderby"),  "getId");
    final String orderdir= nvl( request.getParameter("orderdir"), "ASC");
 
    List<JobGroupAttrDef> jobGroupAttrDefs = admin.getJobGroupAttrDefsByQuery("", " ORDER BY DISPLAY_ORDER ");
    List<JobGroup>        jobGroupList     = admin.getAllJobGroups();
    
    if (orderby.startsWith("attr_")) { /* 가변 속성으로 sort */
    	final String attrId = orderby.substring("attr_".length());
    	final boolean ascending = "ASC".equals(orderdir);
    	Collections.sort(jobGroupList, new Comparator() {
   			public int compare(Object o1, Object o2) {
   				JobGroup g1 = (JobGroup)o1;
   				JobGroup g2 = (JobGroup)o2;
   				if (ascending) {
   					return nvl(g1.getAttribute(attrId)).compareTo(nvl(g2.getAttribute(attrId)));
   				}else {
   					return nvl(g2.getAttribute(attrId)).compareTo(nvl(g1.getAttribute(attrId)));
   				}
   			}
   		});
    }else { /* 기본 속성으로 sort */
	    Collections.sort(jobGroupList, getComparator(orderby, "ASC".equals(orderdir)));
    }
    
    List<JobGroup>        jobGroupTreeList = admin.analyzeToJobGroupsTreeList(jobGroupList);
%>

<script>
<% /* ============ JobGroup 속성 정의 관련 함수 ===================*/ %>
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
	    var chk = document.form1.chkjobgroupid;
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

	function openEditFormJobGroup(jobgroupid) {
	    window.open("form_setting_jobgroup.jsp?jobgroupid="+jobgroupid, "jobgroup_"+jobgroupid.replace(/-/g, ''), 'width=600,height=500,scrollbars=1').focus();
	}
	
	function openAddNewFormJobGroup(parentid) {
	    window.open("form_setting_jobgroup.jsp?parentid="+parentid, "jobgroup_new", 'width=810,height=500,scrollbars=1').focus();
	}

	function doExcelDownload() {
        document.form1.action="view_setting_jobgroup_excel.jsp";
        document.form1.target='';
        document.form1.submit();
        document.form1.action="action_setting.jsp";
	}

    function openExcelUploadForm() {
        window.open("form_setting_jobgroup_upload.jsp", 'upload', 'width=810,height=400,scrollbars=1').focus();
    }

    function removeJobGroup() {
		if (getCheckedCount(document.form1.chkjobgroupid) == 0) {
			alert("Not checked");
			return;
		}

        if (confirm("<%=Label.get("common.remove.confirm.msg")%>")) {
            document.form1.action="action_setting.jsp";
            document.form1.cmd.value="delete_jobgroup";
            document.form1.submit();
        }
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
        window.location.href = 'view_setting.jsp?suburl=jobgroup&orderby='+orderbyCol+'&orderdir='+orderdir;
    }


</script>

<%-- <br>
<table border="0" width="100%">
<tr>
    <td>
    	<span style="float:left">
    		<font size=3><b><img src="images/icon_setting.png"/> <%=Label.get("jobgroup")%></b></font>
    	</span>
    	<span style="float:right">
    		<input type="button" class="button gray medium"  value="<%=Label.get("common.btn.upload")%>" onclick="openExcelUploadForm();">
    		<input type="button" class="button gray medium"  value="<%=Label.get("common.btn.download")%>" onclick="doExcelDownload();">&nbsp;
    	</span>
    </td>
</tr>
</table> --%>

<%-- <div class="tbl_info">
    <span class="tit_Area"><%=Label.get("jobgroup")%></span>
    <span class="r_Area"><button class="m-btn white default" onclick="openExcelUploadForm();"><span><%=Label.get("common.btn.upload")%></span></button> <button class="m-btn white default" onclick="doExcelDownload();"><span><%=Label.get("common.btn.download")%></span></button></span>
</div> --%>

<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("jobgroup")%></div>
</div>

<table class="Width-100 Margin-bottom-5">
	<tr>
		<td class="Text-right">
			<input type="button" class="Button" value="<%=Label.get("common.btn.upload")%>" onclick="openExcelUploadForm();">
			<input type="button" class="Button" value="<%=Label.get("common.btn.download")%>" onclick="doExcelDownload();">
		</td>
	</tr>
</table>
<form name="form1" action="action_setting.jsp" method="POST"  style="margin-top:0;margin-bottom:0">
<input type="hidden" name="suburl"         value="jobgroup">
<input type="hidden" name="cmd"            value="">

<table class="Table Width-100 Margin-bottom-10">
<thead>
<tr>
    <th style="width:3%"><input class="Checkbox" type="checkbox" id="chkall" name="chkall" onclick="checkAll();"></th> 
    <th style="width:3%">#</th>
    <th style="width:20%"><a href="javascript:orderby('getId');"><%=Label.get("jobgroup.id")%><%=printSortMark(orderby, orderdir, "getId")%></a></th>
    <th style="width:5%"><a href="javascript:orderby('getName');"><%=Label.get("jobgroup.name")%><%=printSortMark(orderby, orderdir, "getName")%></a></th>
    <th style="width:5%"><a href="javascript:orderby('getDesc');"><%=Label.get("jobgroup.desc")%><%=printSortMark(orderby, orderdir, "getDesc")%></a></th>
<%
    for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
%>
    <th style="width:10%">
    	<a title="[<%=conv(attrDef.getId())%>] <%=conv(attrDef.getDesc())%>" href="javascript:orderby('attr_<%=conv(attrDef.getId())%>');"><%=conv(attrDef.getName())%><%=printSortMark(orderby, orderdir, "attr_"+conv(attrDef.getId()))%></a>
    </th>
<%
    }
%>    
    <th><%=Label.get("common.createtime")%></th>
    <th><%=Label.get("common.lastmodifytime")%></th>
    <th></th>
</tr>
</thead>
<tbody>
<%
	int i=0;
    for (JobGroup jobgroup : jobGroupTreeList) {
%>
<tr>
    <td style="padding:0px;"><input class="Checkbox" type="checkbox" id="chkjobgroupid" name="chkjobgroupid" value="<%=jobgroup.getId()%>"></td>
    <td><%= (++i)%></td>
    <td class="Text-left"
    	onMouseOver="this.style.cursor='pointer';this.style.backgroundColor='#AAAAFF';"
    	onMouseOut =<%=(i%2==0) ? "this.style.backgroundColor='#f7f7f7';" : "this.style.backgroundColor='#ffffff';" %>
    	onclick="javascript:openEditFormJobGroup('<%=jobgroup.getId()%>');">&nbsp;&nbsp;<%=printSpace(jobgroup.getDepth(), 4)%><img src="images/icon_tree_list_hide.png"/><b><%=conv(jobgroup.getId())%></b></td>
    <td class="Text-left"><%=getShortDescription(jobgroup.getName())%></td>
    <td class="Text-left"><%=getShortDescription(jobgroup.getDesc())%></td>
<%
        for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
%>
    <td><%=conv(jobgroup.getAttribute(attrDef.getId()))%></td>
<%
        }
%>    
    <td><%=toDatetimeString(DateUtil.getTimestamp(jobgroup.getCreateTime()), true)%></td>
    <td><%=toDatetimeString(DateUtil.getTimestamp(jobgroup.getLastModifyTime()), true)%></td>
    <td><input class="Button" type="button" value="<%=Label.get("jobgroup.addchild")%>" onclick="openAddNewFormJobGroup('<%=jobgroup.getId()%>');"></td>
</tr>
<%
    }
%>
</tbody>
</table>

<input class="Button" type="button" value="<%=Label.get("common.btn.add")%>" onclick="openAddNewFormJobGroup('ROOT');">
<input class="Button" type="button" value="<%=Label.get("common.btn.delete")%>" onclick="removeJobGroup('');">&nbsp;&nbsp;
<%=Label.get("jobgroup.deletechild")%><input class="Checkbox" type="checkbox" name="delete_recursively" value="true" >

</form>
