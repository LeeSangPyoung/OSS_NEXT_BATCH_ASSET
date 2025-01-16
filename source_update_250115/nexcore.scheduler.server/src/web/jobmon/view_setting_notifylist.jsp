<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@page import="java.sql.Timestamp"%>
<%@include file= "common.jsp" %>

<script>

<%/* ================== JobNotifyList 관련 함수 ======================*/%>
    
    function checkAll() {
        var chk = document.form2.chkreceiver;
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
    
    function doQuery() {
        document.form1.list_method.value='search';
        document.form1.action="view_setting.jsp";
        document.form1.cmd.value="notifylist";
        document.form1.cntPerPage.value=document.form2.cntPerPage.options[document.form2.cntPerPage.selectedIndex].value;
        document.form1.submit();    	
    }
    
    function changePage(currentPage, cntPerPage, notifyCnt, firstSeq, lastSeq) {
    	document.form1.list_method.value='page';
    	document.form1.action="view_setting.jsp";
    	document.form1.cmd.value="notifylist";
    	document.form1.cntPerPage.value=cntPerPage;
    	document.form1.currentPage.value=currentPage;
    	document.form1.notifyCnt.value=notifyCnt;
    	document.form1.firstSeq.value=firstSeq;
    	document.form1.lastSeq.value=lastSeq;
    	document.form1.submit();
    }
    
    function preScreen(currentPage, cntPerPage, notifyCnt, preSeq) {
    	document.form1.list_method.value='preScreen';
    	document.form1.action="view_setting.jsp";
    	document.form1.cmd.value="notifylist";
    	document.form1.cntPerPage.value=cntPerPage;
    	document.form1.currentPage.value=currentPage;
    	document.form1.notifyCnt.value=notifyCnt;
    	document.form1.preSeq.value=preSeq;
    	document.form1.submit();
    }
    
    function postScreen(currentPage, cntPerPage, notifyCnt, postSeq) {
    	document.form1.list_method.value='postScreen';
    	document.form1.action="view_setting.jsp";
    	document.form1.cmd.value="notifylist";
    	document.form1.cntPerPage.value=cntPerPage;
    	document.form1.currentPage.value=currentPage;
    	document.form1.notifyCnt.value=notifyCnt;
    	document.form1.postSeq.value=postSeq;
    	document.form1.submit();
    }
    
    function lastScreen(currentPage, cntPerPage, notifyCnt, lastScreenCnt) {
    	document.form1.list_method.value='lastScreen';
    	document.form1.action="view_setting.jsp";
    	document.form1.cmd.value="notifylist";
    	document.form1.cntPerPage.value=cntPerPage;
    	document.form1.currentPage.value=currentPage;
    	document.form1.notifyCnt.value=notifyCnt;
    	document.form1.lastScreenCnt.value=lastScreenCnt;
    	document.form1.submit();
    }

</script>


<%
	String listMethod = request.getParameter("list_method");
	String cmd = request.getParameter("cmd");

	int notifyCnt = 0;
	String searchdate = Util.isBlank(request.getParameter("searchdate")) ? Util.getCurrentYYYYMMDD()	: request.getParameter("searchdate");
	List<JobNotifySendInfo> jobNotifyList = null;
	String jobId = null;
	String jobDesc = null;
	String receiverName = null;
	String recvType = null;
	String recvPoint = null;

	final int NUMBER_OF_PAGE = 10;                  // 한 스크린에 보여줄 페이지의 갯수
	int cntPerPage = 10;                            // 한 페이지에 보여줄 Record의 갯수
	int cntPerScreen = cntPerPage * NUMBER_OF_PAGE; // 한 스크린에 보여줄 Record의 갯수
	int currentPage = 1;                            // 현재 스크린에서의 페이지 순서

	if (listMethod != null) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date = sdf.parse(searchdate);
		Timestamp firstTime = new Timestamp(date.getTime());
		Timestamp lastTime = new Timestamp(date.getTime());
		lastTime.setHours(23);
		lastTime.setMinutes(59);
		lastTime.setSeconds(59);
		lastTime.setNanos(999999999);
		
		jobId = nvl(request.getParameter("jobid")).trim();
		jobDesc = nvl(request.getParameter("jobdesc")).trim();
		receiverName = nvl(request.getParameter("receivername")).trim();
		recvType = nvl(request.getParameter("recvtype")).trim();
		recvPoint = nvl(request.getParameter("recvpoint")).trim();
		cntPerPage = Integer.parseInt(request.getParameter("cntPerPage"));
		cntPerScreen = cntPerPage * NUMBER_OF_PAGE;
		
		Map queryParamMap = new HashMap();
		ControllerAdminLocal admin = getControllerAdmin();

		queryParamMap.put("firstTime", DateUtil.getTimestampString(firstTime));
		queryParamMap.put("lastTime", DateUtil.getTimestampString(lastTime));
		queryParamMap.put("jobId", jobId);
		queryParamMap.put("jobDesc", jobDesc);
		queryParamMap.put("receiverName", receiverName);
		queryParamMap.put("recvType", recvType);
		queryParamMap.put("recvPoint", recvPoint);
		queryParamMap.put("cntPerScreen", cntPerScreen);
		
		if ("search".equals(listMethod)) {			
			currentPage = 1;
			queryParamMap.put("orderBy", "ASC");
			notifyCnt = admin.getJobNotifySendListCount(queryParamMap);
			jobNotifyList = admin.getJobNotifySendList(queryParamMap);
		} else if ("page".equals(listMethod)) {			
			currentPage = Integer.parseInt(request.getParameter("currentPage"));
			String firstSeq = request.getParameter("firstSeq");
			String lastSeq = request.getParameter("lastSeq");
			queryParamMap.put("firstSeq", firstSeq);
			queryParamMap.put("lastSeq", lastSeq);
			queryParamMap.put("orderBy", "ASC");			
			notifyCnt = Integer.parseInt(request.getParameter("notifyCnt"));
			jobNotifyList = admin.getJobNotifySendList(queryParamMap);			
		} else if ("preScreen".equals(listMethod)) {
			currentPage = Integer.parseInt(request.getParameter("currentPage"));
			String preSeq = request.getParameter("preSeq");
			queryParamMap.put("preSeq", preSeq);
			queryParamMap.put("orderBy", "DESC");
			notifyCnt = Integer.parseInt(request.getParameter("notifyCnt"));
			jobNotifyList = admin.getJobNotifySendList(queryParamMap);
		} else if ("postScreen".equals(listMethod)) {
			currentPage = Integer.parseInt(request.getParameter("currentPage"));			
			String postSeq = request.getParameter("postSeq");
			queryParamMap.put("postSeq", postSeq);
			queryParamMap.put("orderBy", "ASC");			
			notifyCnt = Integer.parseInt(request.getParameter("notifyCnt"));
			jobNotifyList = admin.getJobNotifySendList(queryParamMap);
		} else if ("lastScreen".equals(listMethod)) {
			currentPage = Integer.parseInt(request.getParameter("currentPage"));
			queryParamMap.put("orderBy", "DESC");
			queryParamMap.put("cntPerScreen", request.getParameter("lastScreenCnt"));
			notifyCnt = Integer.parseInt(request.getParameter("notifyCnt"));
			jobNotifyList = admin.getJobNotifySendList(queryParamMap);
		}
	}
%>
<%-- <br>
<table border="0" width="100%">
<tr>
    <td colspan="100%" align="left"><font size=3><b><img src="images/icon_setting.png"/> <%=Label.get("notify.view.list")%></b></font></td>
</tr>
</table> --%>

<div class="content-title__wrap">
	<div class="content-title"><%=Label.get("notify.view.list")%></div>
</div>

<form name="form1" action="view_setting.jsp" method="get">
<input type="hidden" name="suburl" value="notifylist">
<input type="hidden" name="list_method" value="">
<input type="hidden" name="cmd" value="">
<input type="hidden" name="notifyCnt" value="">
<input type="hidden" name="cntPerPage" value="">
<input type="hidden" name="currentPage" value="">
<input type="hidden" name="firstSeq" value="">
<input type="hidden" name="lastSeq" value="">
<input type="hidden" name="preSeq" value="">
<input type="hidden" name="postSeq" value="">
<input type="hidden" name="lastScreenCnt" value="">

<table class="Table njf-table__typea Margin-bottom-10">
<tr>
    <th style="width:5%; padding:2px"><%=Label.get("common.searchdate")%></th>
    <td style="width:9%; padding:2px""><input class="Textinput Width-100" type="text" name="searchdate" value="<%=searchdate%>" maxlength=8></td>
        
    <th style="width:4%; padding:2px"><%=Label.get("job.jobid")%></th>
    <td style="width:9%; padding:2px"><input class="Textinput Width-100" type="text" name="jobid" value="<%=nvl(jobId)%>"></td>

    <th style="width:4%; padding:2px"><%=Label.get("job.desc")%></th>
    <td style="width:13%; padding:2px"><input class="Textinput Width-100" type="text" name="jobdesc" value="<%=conv(jobDesc)%>"></td>
    
    <th style="width:7%; padding:2px"><%=Label.get("notify.receiver.name")%></th>
    <td style="width:10%; padding:2px"><input class="Textinput Width-100" type="text" name="receivername" value="<%=conv(receiverName)%>"></td>
    
	<th style="width:6%; padding:2px"><%=Label.get("notify.type")%></th>
	<td style="width:12%; padding:2px">
		<select class="Select Width-100" name="recvtype">
			<%=printSelectOption("", "", nvl(recvType))%>
            <%=printSelectOption("EMAIL", "EMAIL", nvl(recvType))%>
            <%=printSelectOption("SMS", "SMS", nvl(recvType))%>
            <%=printSelectOption("TERMINAL", "TERMINAL", nvl(recvType))%>
            <%=printSelectOption("MESSENGER", "MESSENGER", nvl(recvType))%>
            <%=printSelectOption("DEV1", "DEV1", nvl(recvType))%>
            <%=printSelectOption("DEV2", "DEV2", nvl(recvType))%>
            <%=printSelectOption("DEV3", "DEV3", nvl(recvType))%>
       </select>
	</td>

	<th style="width:6%; padding:2px"><%=Label.get("notify.point")%></th>
	<td style="width:10%; padding:2px"><input class="Textinput Width-100" type="text" name="recvpoint" value="<%=nvl(recvPoint)%>"></td>
	
	<td style="width:5%; text-align:center; padding:2px"><input class="Button" type="submit" value="<%=Label.get("common.btn.query")%>" onclick="doQuery();"></td>
</tr>
</table>
</form>

<%
	/* ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ */
%>
<%
	/* ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■ */
%>

<form name="form2" action="action_notify.jsp" method="POST">

<table class="Width-100">
<tr>
	<td class="Text-right">
		<select class="Select Width-5" name="cntPerPage" onchange="doQuery();">
			<%=printSelectOption( "10",  "10", Integer.toString(cntPerPage))%>
            <%=printSelectOption( "20",  "20", Integer.toString(cntPerPage))%>
            <%=printSelectOption( "30",  "30", Integer.toString(cntPerPage))%>
            <%=printSelectOption( "40",  "40", Integer.toString(cntPerPage))%>
            <%=printSelectOption( "50",  "50", Integer.toString(cntPerPage))%>
            <%=printSelectOption( "70",  "70", Integer.toString(cntPerPage))%>
            <%=printSelectOption("100", "100", Integer.toString(cntPerPage))%>
       </select>
	</td>
</tr>
</table>

<table class="Table Width-100 Margin-bottom-10">
<thead>
<tr>
    <th style="width:15%; padding:2px"><%=Label.get("job.jobexeid")%></th>
    <th style="width:6%; padding:2px"><%=Label.get("job.jobid")%></th>
    <th style="width:3%; padding:2px"><%=Label.get("job.desc")%></th>
    <th style="width:3%; padding:2px"><%=Label.get("common.server")%></th>
    <th style="width:6%; padding:2px"><%=Label.get("jobexe.returncode")%></th>
    <th style="width:7%; padding:2px"><%=Label.get("jobexe.errmsg")%></th>
    <th style="width:6%; padding:2px"><%=Label.get("notify.receiver.id")%></th>
    <th style="width:7%; padding:2px"><%=Label.get("notify.receiver.name")%></th>
    <th style="width:6%; padding:2px"><%=Label.get("notify.type")%></th>
    <th style="width:6%; padding:2px"><%=Label.get("notify.point")%></th>
    <th style="width:6%; padding:2px"><%=Label.get("common.createtime")%></th>
    <th style="width:8%; padding:2px"><%=Label.get("notify.send.state")%></th>
    <th style="width:8%; padding:2px"><%=Label.get("notify.sendtime")%></th>
    <th style="width:6%; padding:2px"><%=Label.get("notify.try.count")%></th>
</tr>
</thead>
<tbody>
<%
	int pageAllCnt = (notifyCnt % cntPerPage == 0) ? notifyCnt / cntPerPage : (notifyCnt / cntPerPage) + 1;
	int screenNum = (currentPage - 1) / NUMBER_OF_PAGE;
	int screenCnt = (pageAllCnt % NUMBER_OF_PAGE == 0) ? pageAllCnt / NUMBER_OF_PAGE : (pageAllCnt / NUMBER_OF_PAGE) + 1;

	if (jobNotifyList != null) {
		int i = ((currentPage - 1) * cntPerPage) - (cntPerScreen * screenNum);
		int isReverse = 1;
		
		if ("preScreen".equals(listMethod) || "lastScreen".equals(listMethod)) {
			i = jobNotifyList.size() - 1;
			isReverse = -1;
		}
		
		for (int j = 0; i < jobNotifyList.size() && i >= 0 && j < cntPerPage; i = i+(1*isReverse), j++) {
%>
<tr>
    <td><%=jobNotifyList.get(i).getJobExecutionId()%></td>
    <td><%=jobNotifyList.get(i).getJobId()%></td>
    <td><%=jobNotifyList.get(i).getJobDesc()%></td>
    <td><%=jobNotifyList.get(i).getAgentNode()%></td>
    <td><%=jobNotifyList.get(i).getReturnCode()%></td>
    <td><%=jobNotifyList.get(i).getErrorMsg()%></td>
    <td><%=jobNotifyList.get(i).getReceiverId()%></td>
    <td><%=jobNotifyList.get(i).getReceiverName()%></td>
    <td><%=jobNotifyList.get(i).getRecvType()%></td>
    <td><%=jobNotifyList.get(i).getRecvPoint()%></td>
    <td><%=DateUtil.getTimestamp(jobNotifyList.get(i).getCreateTime()).toString()%></td>
    <td><%=jobNotifyList.get(i).getSendState()%></td>
    <td><%=jobNotifyList.get(i).getSendTime() != null ? DateUtil.getTimestamp(jobNotifyList.get(i).getSendTime()).toString() : ""%></td>
    <td><%=jobNotifyList.get(i).getTryCount()%></td>
</tr>
<%
		}
	}
%>
	</tbody>
</table>

<%
	// 페이징 처리
	if (notifyCnt > 0) {
%>
<table>
<tr>
	
<%
	int firstSeq = jobNotifyList.get(0).getSeqNo();
	int lastSeq = jobNotifyList.get(jobNotifyList.size()-1).getSeqNo();
	int preSeq = jobNotifyList.get(0).getSeqNo();
	int postSeq = jobNotifyList.get(jobNotifyList.size()-1).getSeqNo();

	if ("preScreen".equals(listMethod) || "lastScreen".equals(listMethod)) {
		firstSeq = jobNotifyList.get(jobNotifyList.size()-1).getSeqNo();
		lastSeq = jobNotifyList.get(0).getSeqNo();
		preSeq = jobNotifyList.get(jobNotifyList.size()-1).getSeqNo();
		postSeq = jobNotifyList.get(0).getSeqNo();
	}
%>	

	<td width="20" title="First 10 Page"><b>
<%	// 처음 screen 이면 First 10 Page 를 비활성화 한다. %>
<%  if (screenNum == 0) { %>
		<img src="images/pag_pre2.png">
<%  } else { %>
		<img src="images/pag_pre2.png" onclick="javascript:doQuery();">
<%  } %>
	</b></td>

	<td width="20" title="Previous 10 Page"><b>
<%	// 처음 screen 이면 Previous 10 Page 를 비활성화 한다. %>
<%	if (screenNum == 0) { %>
		<img src="images/pag_pre1.png">
<%	} else {
		currentPage = (screenNum - 1) * NUMBER_OF_PAGE + 1;	%>
		<img src="images/pag_pre1.png" onclick="javascript:preScreen('<%=currentPage%>','<%=cntPerPage%>','<%=notifyCnt%>','<%=preSeq%>');">
<%	} %>
	</b></td>

<%  for (int i = screenNum * NUMBER_OF_PAGE + 1, j = 0; i <= pageAllCnt	&& j < NUMBER_OF_PAGE; i++, j++) { %>
		<td width="10"><b><a href="javascript:changePage('<%=i%>','<%=cntPerPage%>','<%=notifyCnt%>','<%=firstSeq%>','<%=lastSeq%>');"><%=i%></a></b></td>
<%	} %>

	<td width="20" title="Next 10 Page"><b>	
<%	// 마지막 screen 이면 Next 10 Page 를 비활성화 한다. %>	
<%	if (screenNum + 1 == screenCnt) { %>
		<img src="images/pag_next1.png">
<%	} else { 
		currentPage = (screenNum + 1) * NUMBER_OF_PAGE + 1; %>
		<img src="images/pag_next1.png" onclick="javascript:postScreen('<%=currentPage%>','<%=cntPerPage%>','<%=notifyCnt%>','<%=postSeq%>');">
<%  } %>
	</b></td>
	
	<td width="20" title="Next 10 Page"><b>	
<%	// 마지막 screen 이면 Next 10 Page 를 비활성화 한다. %>	
<%	if (screenNum + 1 == screenCnt) { %>
		<img src="images/pag_next2.png">
<%	} else { 
		currentPage = (screenCnt - 1) * NUMBER_OF_PAGE + 1;
		int lastScreenCnt = notifyCnt - ((screenCnt - 1) * cntPerScreen);%>
		<img src="images/pag_next2.png" onclick="javascript:lastScreen('<%=currentPage%>','<%=cntPerPage%>','<%=notifyCnt%>','<%=lastScreenCnt%>');">
<%  } %>
	</b></td>
	
</tr>
</table>
<%	} %>
</form>

