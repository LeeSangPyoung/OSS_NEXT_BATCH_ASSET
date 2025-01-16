<%@page import="org.springframework.util.StringUtils"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%
	ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth auth      = new AdminAuth(getUserId(request), getUserIp(request));
    
    String elementName   = request.getParameter("target_name");
	String orderby       = nvl( request.getParameter("orderby"),  "getId");
	final String orderdir= nvl( request.getParameter("orderdir"), "ASC");
	
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
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<script src="./script/app/include-lib.js"></script>
<title><%=Label.get("jobgroup")%></title>
<script>
$a.page(function() {
    // 초기화 함수
    this.init = function(id, param) {
    	// alopex ui 에서 fixed 를 기본 세팅을 하고 있어 이를 'auto' 로 변경함
    	$(".Table").css({'table-layout':'auto'});
    }
});
<% /* ============ JobGroup 속성 정의 관련 함수 ===================*/ %>
    function selectJobGroup(jobgroupid) {
    	opener.document.getElementsByName("<%=elementName%>")[0].value=jobgroupid;
    	window.close();
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
        window.location.href = 'popup_jobgroup.jsp?target_name=<%=elementName%>&orderby='+orderbyCol+'&orderdir='+orderdir;
    }
</script>

<center>
	<div class="popup-content-wrap Margin-bottom-10">

		<div class="popup-content-title__wrap">
			<div class="content-title"><%=Label.get("jobgroup")%> <%=Label.get("common.select")%></div>
		</div>

		<form name="form1" action="action_setting.jsp" method="POST">
		<input type="hidden" name="suburl"         value="jobgroup">
		<table class="Table Width-100 Margin-bottom-10" >
<!-- 		<colgroup>
			<col style="width:60%" />
			<col style="width:20%" />
			<col style="width:20%" />
		</colgroup> -->
		<thead>
			<tr>
			    <th class="Width-60"><a href="javascript:orderby('getId');"><%=Label.get("jobgroup")%> ID<%=printSortMark(orderby, orderdir, "getId")%></a></th>
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
				int i=0;
			    boolean colorFlip=true;
			    for (JobGroup jobgroup : jobGroupTreeList) {
			    	colorFlip = !colorFlip;
			    	++i;
			%>
			<tr <%=printTrFlip(colorFlip)%> 
			    onMouseOver="this.style.cursor='pointer';this.style.backgroundColor='#AAAAFF';" 
			    onMouseOut =<%=(i%2==0) ? "this.style.backgroundColor='#f7f7f7';" : "this.style.backgroundColor='#ffffff';" %>
			    onclick="selectJobGroup('<%=jobgroup.getId()%>')">
			    <td class="Text-left">&nbsp;&nbsp;<%=printSpace(jobgroup.getDepth(), 4)%><img src="images/icon_tree_list_hide.png"/> <b><%=conv(jobgroup.getId())%></b></td>
			    <td class="Text-left"><%=getShortDescription(jobgroup.getName())%></td>
			    <td class="Text-left"><%=getShortDescription(jobgroup.getDesc())%></td>
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

		<table class="Width-100 Margin-bottom-10">
			<tr>
				<td class="Text-center">
					<input class="Button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()" style="width:80px; height:35px">
				</td>
			</tr>
		</table>		
		</form>
	</div>
</center>

</head>
</html>
