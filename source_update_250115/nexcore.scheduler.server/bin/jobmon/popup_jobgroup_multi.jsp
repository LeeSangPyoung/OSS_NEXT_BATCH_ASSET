<%@page import="org.springframework.util.StringUtils"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
	ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth auth      = new AdminAuth(getUserId(request), getUserIp(request));
    
    String elementName   = request.getParameter("target_name");
    String beforeValue   = request.getParameter("before_value");
	String orderby       = nvl( request.getParameter("orderby"),  "getId");
	final String orderdir= nvl( request.getParameter("orderdir"), "ASC");

	Set beforeSelectedJobGroupSet = new HashSet(Util.toList(beforeValue, "/"));
	
	List<JobGroupAttrDef> jobGroupAttrDefs = admin.getJobGroupAttrDefsByQuery("", " ORDER BY DISPLAY_ORDER ");

	Map queryParamMap = new HashMap();
    User user = getUser(request);
    if (!user.isAdmin()) { /* 해당 사용자의 View 권한의 그룹만 조회 */
    	queryParamMap.put("authorizedJobGroupIdViewList", user.getAuthList("VIEW_JOBGROUP"));
    }
    List<JobGroup>        jobGroupList     = admin.getJobGroupsByDynamicQuery(queryParamMap);
	
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
<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" /> -->
<title><%=Label.get("jobgroup")%></title>
<script src="./script/app/include-lib.js"></script>
<script>
<% /* ============ JobGroup 속성 정의 관련 함수 ===================*/ %>
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
    
    function selectAndSubmitJobGroup() {
    	var chk = document.form1.chkjobgroupid;
    	var jobgroupList = new String();
        if (chk.length == null) { /* 하나일때 */
            if (chk.checked) {
                jobgroupList = jobgroupList + chk.value + "/";
            }
        }else {
            for (var i=0; i<chk.length; i++ ) {
                if (chk[i].checked) {
                	jobgroupList = jobgroupList + chk[i].value + "/";
                }
            }
        }
       	opener.document.getElementsByName("<%=elementName%>")[0].value=jobgroupList;
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
        window.location.href = 'popup_jobgroup_multi.jsp?target_name=<%=elementName%>&before_value=<%=beforeValue%>&orderby='+orderbyCol+'&orderdir='+orderdir;
    }
</script>

<center>
<%-- <br>
<table border="0" width="100%">
<tr>
    <td colspan="100%" align="left"><font size=3><b><img src="images/icon_setting.png"/> <%=Label.get("jobgroup")%> <%=Label.get("common.select")%></b></font></td>
</tr>
</table> --%>


<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
	<div class="popup-content-title__wrap Margin-bottom-5">
		<div class="content-title">
			<%=Label.get("jobgroup")%> <%=Label.get("common.select")%>
		</div>
	</div>

<form name="form1" action="action_setting.jsp" method="POST">
<input type="hidden" name="suburl"         value="jobgroup">

<table class="Table Width-100 Margin-bottom-10" >
<thead>
<tr>
    <th class="Width-5"><input class="Checkbox" type="checkbox" id="chkall" name="chkall" onclick="checkAll();"></th>
    <th><a href="javascript:orderby('getId');"><%=Label.get("jobgroup")%> ID<%=printSortMark(orderby, orderdir, "getId")%></a></th>
    <th><a href="javascript:orderby('getName');"><%=Label.get("common.name")%><%=printSortMark(orderby, orderdir, "getName")%></a></th>
    <th><a href="javascript:orderby('getDesc');"><%=Label.get("common.desc")%><%=printSortMark(orderby, orderdir, "getDesc")%></a></th>
<%
    for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
%>
    <th>
    	<a title="[<%=conv(attrDef.getId())%>] <%=conv(attrDef.getDesc())%>" href="javascript:orderby('attr_<%=conv(attrDef.getId())%>');"><%=conv(attrDef.getName())%><%=printSortMark(orderby, orderdir, "attr_"+conv(attrDef.getId()))%></a>
    </th>
<%
    }
%>    
</tr>
</thead>
<tbody>
<%
    for (JobGroup jobgroup : jobGroupTreeList) {
    	boolean checked = beforeSelectedJobGroupSet.contains(jobgroup.getId());
%>
<tr>
    <td style="text-align:center; padding:0" <%=checked ? "bgcolor='#ffff99'" : ""%>><input class="Checkbox" type="checkbox" name="chkjobgroupid" value="<%=jobgroup.getId()%>" <%= checked  ? "checked" : "" %>></td>
    <td class="Text-left Margin-right-10"  <%=checked ? "bgcolor='#ffff99'" : ""%>><%=printSpace(jobgroup.getDepth(), 4)%><img src="images/icon_tree_list_hide.png"/> <b><%=conv(jobgroup.getId())%></b></td>
    <td class="Text-left"  <%=checked ? "bgcolor='#ffff99'" : ""%>><%=getShortDescription(jobgroup.getName())%></td>
    <td class="Text-left"  <%=checked ? "bgcolor='#ffff99'" : ""%>><%=getShortDescription(jobgroup.getDesc())%></td>
<%
        for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
%>
    <td><%=conv(jobgroup.getAttribute(attrDef.getId()))%></td>
<%
        }
%>    
</tr>
<%
    }
%>
</tbody>
</table>

<table class="Width-100">
	<tr>
		<td class="Text-center">
			<input class="Button" type="button" value="<%=Label.get("common.btn.submit")%>" onclick="selectAndSubmitJobGroup();window.close()">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()">
		</td>
	</tr>
</table>

</form>
</div>
</center>

</head>
</html>
