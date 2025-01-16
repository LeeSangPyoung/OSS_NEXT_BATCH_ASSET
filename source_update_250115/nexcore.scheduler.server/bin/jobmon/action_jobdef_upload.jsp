<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	int reqNoCount=0;
%>

<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<script src="./script/app/include-lib.js"></script>
<title>Job Definition Upload</title> 
<script>


</script>
</head>
<body>
<center>

<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=Label.get("form_jobdef_upload.title")%>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="팝업창 닫기" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
<table class="Table Width-100 Margin-bottom-10" >
	<thead>
	<tr>
		<th>#</td>
		<th><%=Label.get("job.jobid")%></th>
		<th><%=Label.get("job.owner")%></th>
		<th><%=Label.get("job.desc")%></th>
		<th><%=Label.get("job.jobtype.short")%></th>
		<th><%=Label.get("job.component")%></th>
		<th><%=Label.get("job.agent")%></th>
		<th><%=Label.get("common.btn.add")%><br><%=Label.get("common.btn.edit")%></th>
	</tr>
	</thead>
	<tbody>
<%
	String   cmd            = nvl(  request.getParameter("cmd"), "");
	String   returnUrl      =       request.getParameter("returnurl");
	
	int      jobdefCount    = toInt(request.getParameter("jobdef_count"), 0);
	String   jdfDir         =       request.getParameter("jdf_dir");

	/* Checked job only can be add or edit */
	Set<String> checkedJobIdList = new HashSet(Arrays.asList(request.getParameterValues("chk_jobid")));

	ControllerAdminLocal admin = getControllerAdmin();
	AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));
	
	List<String> okReqNoList = new LinkedList(); /* ReqNo list of success */
	
	try {
		for (int i=1; i<=jobdefCount; i++) {
			String jobid                 =        request.getParameter("jobdef_"+i+"_jobid");

			if (!checkedJobIdList.contains(jobid)) {
				continue; /* If not checked, do not process. */
			}

			String filename              =        request.getParameter("jobdef_"+i+"_filename");
			long   fileTimestamp         = toLong(request.getParameter("jobdef_"+i+"_file_timestamp"), 0);
			String mode                  =        request.getParameter("jobdef_"+i+"_mode");
			long   oldJobLastModifyTime  = toLong(request.getParameter("jobdef_"+i+"_old_lastmodifytime"), 0);
			
			File   newjobdefFile = new File(filename);
			JobDefinition newjobdef = JobDefinitionUtil.readFromFile(newjobdefFile);
			
			if ("add".equals(mode)) {
				newjobdef.setCreateTime(Util.getCurrentYYYYMMDDHHMMSS());
			}else if ("edit".equals(mode)) {
				JobDefinition oldjobdef = admin.getJobDefinition(newjobdef.getJobId());
				newjobdef.setCreateTime(oldjobdef.getCreateTime());
			}
			
			JobDefinitionStg newjobdefStg = new JobDefinitionStg(newjobdef);
			
			/* 요청 부가 정보 세팅 */
			newjobdefStg.setReqUserIp(request.getRemoteAddr());
			newjobdefStg.setReqUserName(getUserId(request));
			newjobdefStg.setReqTime(Util.getCurrentYYYYMMDDHHMMSS());
			newjobdefStg.setReqState("Q");
			newjobdefStg.setReqType(mode);
			
			admin.addJobDefinitionStg(newjobdefStg, auth);
			
			okReqNoList.add(newjobdefStg.getReqNo()); /* If exception occurred, all request should be cancel */
%>
	<tr>
		<td><%=i%></td>
		<td><%=nvl(newjobdefStg.getJobId())%></a></td>
		<td><%=nvl(newjobdefStg.getOwner())%></td>
		<td class="Text-left"><%=getShortDescription(newjobdefStg.getDescription())%></td>
		<td><%=nvl(newjobdefStg.getJobType())%></td>
		<td><%=getShortComponentName(newjobdefStg.getJobType(), newjobdefStg.getComponentName())%></td>
		<td><%=nvl(newjobdefStg.getAgentNode())%></td>
		<td><%=Label.get("common.btn."+mode)%></td>
<%
		}
	}catch(Exception e) {
		getMainLog().error("action_jobdef_upload.jsp", e);
		
		/* Cancel part of request list. 에러발생시 그 시점까지 요청된 것들 모두 삭제함. */
		for (String reqno : okReqNoList) {
			try {
				admin.deleteJobDefinitionStg(reqno, auth);
			}catch(Throwable ignore) {}
		}
		throw new ServletException(e);
	}finally {
		if (!Util.isBlank(jdfDir)) {
			/* delete temporary jdf file */
			File jdfDirFile = new File(jdfDir);
			File[] files = jdfDirFile.listFiles();
			for (File jdfFile : files) {
				jdfFile.delete();
			}
			jdfDirFile.delete();
		}
	}
%>
	</tbody>
</table>
<table class="Width-100 Margin-bottom-10">
	<tr style="height:30px">
		<td style="font-size:12px; text-align:center">
		<%=Label.get("action_jobdef_upload.success_msg")%>
		</td>
	</tr>
	<tr>
		<td class="Text-center">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close();" style="width:80px; height:35px">
		</td>
	</tr>
</table>
</div>
</center>

</body>
</html>