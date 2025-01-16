<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%@page import="org.apache.commons.fileupload.*"%>
<%@page import="org.apache.commons.fileupload.disk.*"%>
<%@page import="org.apache.commons.fileupload.servlet.*"%>

<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<script src="./script/app/include-lib.js"></script>
<title>Job Definition Upload</title> 
<script>

    function openDiffWin(upload_jobdef_filename) {
        window.open("view_jobdef_upload_diff.jsp?upload_jobdef_filename="+upload_jobdef_filename, 'upload_jobdef_diff', 'width=1000,height=500,scrollbars=1').focus();
    }

    function openUploadedJobDefinitionWin(upload_jobdef_filename) {
        window.open("view_jobdef_upload_dtl.jsp?upload_jobdef_filename="+upload_jobdef_filename, "upload_jobdef_dtl", 'width=820,height=800,scrollbars=1').focus();
    }
    
    function getCheckedCount(chk) {
    	if (chk.length == null) { /* ÇÏ³ªÀÏ¶§ */
    		return chk.checked ? 1 : 0;
    	}else {
    		var cnt = 0;
    		for (var i=0; i<chk.length; i++) {
    			cnt += chk[i].checked ? 1 : 0;
    		}
    		return cnt;
    	}
    }
    
    function doSubmitRequest() {
    	if (getCheckedCount(document.form2.chk_jobid) == 0) {
    		alert("Not checked");
    	}else {
    		document.form2.submit();
    	}
    }

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
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
<%-- <br>
<font size="3"><b><%=Label.get("form_jobdef_upload.title")%></b></font>
<br><br>
<br> --%>
<%
	String queryString = request.getQueryString();
	if (queryString == null) { /* at first */
%>
<form name="form1" action="form_jobdef_upload.jsp?step=2" enctype="multipart/form-data" method="POST">

<table class="Table njf-table__typea Margin-bottom-10" >
	<tr>
		<td><input type="file" style="width:100%" name="uploadfile"></td>
	</tr>
</table>

<table class="Width-100">
	<tr>
		<td class="Text-center">
			<input class="Button" type="submit" value="<%=Label.get("common.btn.submit")%>" style="width:80px; height:35px">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()" style="width:80px; height:35px">
		</td>
	</tr>
</table>

</form>
<%
	} else if (queryString.startsWith("step")) { /*  after upload */ 
	
		DiskFileItemFactory factory = new DiskFileItemFactory();

		factory.setSizeThreshold(512);
		factory.setRepository(new File(System.getProperty("NEXCORE_HOME")+"/tmp"));

		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(50 * 1024 * 1024); // 50 M

		List<FileItem> items = upload.parseRequest(request);

		if (items == null || items.size() == 0) {
			throw new RuntimeException("File not uploaded");
		}
		
		FileItem fileitem = items.get(0);
		String filename = fileitem.getName();
		long   filesize = fileitem.getSize();

		if (Util.isBlank(filename) || filesize == 0) {
			throw new RuntimeException("File not uploaded");
		}
		
		InputStream          xlsIn          = null;
		ExcelToJobDefinition exceltool      = new ExcelToJobDefinition();
		List<File>           jobdefFileList = Collections.EMPTY_LIST;
		try {
			xlsIn = fileitem.getInputStream();
			exceltool.parseExcel(xlsIn);
			jobdefFileList = exceltool.getJobdefFileList();
		}finally {
			try { xlsIn.close(); }catch(Exception ignore) {}
		}
%>

<label style="font-size:14px"><b>Upload file : [<%=filename%>]</b></label>
<script>
    function checkAll() {
        var chk = document.form2.chk_jobid;
        var v = document.form2.chkall.checked;
        
        if (typeof chk =="undefined") return false;
        
        if (chk.length == null) {
            chk.checked = v ;
        }else {
            for (i=0; i<chk.length; i++ ) {
                chk[i].checked = v ;
            }
        }
    }

</script>
<form name="form2" action="action_jobdef_upload.jsp" method="POST">
<input type="hidden" name="jobdef_count" value="<%=jobdefFileList.size()%>"/>
<input type="hidden" name="jdf_dir" value="<%=jobdefFileList.size()==0 ? "" : jobdefFileList.get(0).getParent()%>"/>

<table class="Table Margin-bottom-10 Margin-top-5">
	<thead>
	<tr>
		<th style="width:3px;  padding:2px;">#</th>
		<th style="width:10px; padding:2px;"><%=Label.get("job.jobid")%></th>
		<th style="width:14px; padding:2px;"><%=Label.get("job.jobgroup")%></th>
		<th style="width:8px;  padding:2px;"><%=Label.get("job.owner")%></th>
		<th style="width:7px;  padding:2px;"><%=Label.get("job.desc")%></th>
		<th style="width:10px; padding:2px;"><%=Label.get("job.jobtype.short")%></th>
		<th style="width:13px; padding:2px;"><%=Label.get("job.component")%></th>
		<th style="width:13px; padding:2px;"><%=Label.get("job.agent")%></th>
		<th style="width:7px;  padding:2px;"><%=Label.get("common.btn.add")%><br><%=Label.get("common.btn.edit")%></th>
		<th style="width:10px; padding:2px;"><%=Label.get("job.req.diff")%></th>
		<th style="width:3px;  padding:2px;"><input class="Checkbox" type="checkbox" id="chkall" name="chkall" onclick="checkAll();"></th>
	</tr>
	</thead>
	<tbody>
<%
		ControllerAdminLocal admin = getControllerAdmin();
		
		int seq = 0;
		for (File jobdefFile : jobdefFileList) {
			seq++;
			JobDefinition jobdef    = JobDefinitionUtil.readFromFile(jobdefFile);
			JobDefinition oldJobdef = null;
			try {
				oldJobdef = admin.getJobDefinition(jobdef.getJobId());
			}catch(Exception ignore) { /* not found */
			}
			
			String dirname = jobdefFile.getParentFile().getName();
			String fname   = jobdefFile.getName();
			
			boolean isAdd     = oldJobdef == null;
			boolean isChanged = isAdd ? true : !JobDefinitionUtil.isEquals(jobdef, oldJobdef);
%>
	<tr>
		<td><%=seq%></td>
		<td><a href="javascript:openUploadedJobDefinitionWin('<%=dirname+"/"+fname%>');"><b><%=nvl(jobdef.getJobId())%></b></a></td>
		<td><%=nvl(jobdef.getJobGroupId())%></td>
		<td><%=nvl(jobdef.getOwner())%></td>
		<td class="Text-left"><%=getShortDescription(jobdef.getDescription())%></td>
		<td><%=nvl(jobdef.getJobType())%></td>
		<td><%=getShortComponentName(jobdef.getJobType(), jobdef.getComponentName())%></td>
		<td><%=nvl(jobdef.getAgentNode())%></td>
		<td><%=isAdd ? Label.get("common.btn.add") : Label.get("common.btn.edit") %></td>
<%
			if (!isAdd && isChanged) { /* if changed */
%>
		<td><a href="javascript:openDiffWin('<%=dirname+"/"+fname%>');"><b><%=Label.get("job.req.diff")%></b></a></td>
<%
			} else {
%>
		<td></td>
<%
			}
%>
		<td style="padding:0px;"><input class="Checkbox" type="checkbox" id="chk_jobid" name="chk_jobid" value="<%=jobdef.getJobId()%>" <%=isAdd || isChanged ? "checked" : "" %>></td>
	</tr>
	<input type="hidden" name="jobdef_<%=seq%>_jobid"				value="<%=jobdef.getJobId()%>"/>
	<input type="hidden" name="jobdef_<%=seq%>_filename"			value="<%=jobdefFile.getAbsolutePath()%>"/>
	<input type="hidden" name="jobdef_<%=seq%>_file_timestamp"		value="<%=jobdefFile.lastModified()%>"/>
	<input type="hidden" name="jobdef_<%=seq%>_mode"				value="<%=isAdd ? "add" : "edit" %>"/>
	<input type="hidden" name="jobdef_<%=seq%>_old_lastmodifytime"	value="<%=isAdd ? 0 : DateUtil.getTimestampLong(oldJobdef.getLastModifyTime()) %>"/>
<%
		}
%>
	</tbody>
</table>

<table class="Width-100">
	<tr>
		<td class="Text-center">
			<input class="Button" type="button" value="<%=Label.get("common.btn.request")%>" onclick="doSubmitRequest()" style="width:80px; height:35px">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()" style="width:80px; height:35px">
		</td>
	</tr>
</table>
</form>
<%
	}
%>
</div>
</center>
<br>
</body>
</html>
