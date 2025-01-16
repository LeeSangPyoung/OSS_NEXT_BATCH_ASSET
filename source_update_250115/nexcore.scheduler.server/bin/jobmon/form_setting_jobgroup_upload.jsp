<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%@page import="org.apache.commons.fileupload.*"%>
<%@page import="org.apache.commons.fileupload.disk.*"%>
<%@page import="org.apache.commons.fileupload.servlet.*"%>

<html>
<head>
<!-- <link rel="stylesheet" href="common.css" type="text/css" />  -->
<title><%=Label.get("jobgroup.upload")%></title> 
<script src="./script/app/include-lib.js"></script>
<script>

    function doSubmit() {
   		document.form2.submit();
    }

</script>
</head>
<body>

<center>
<div class="header-wrap Margin-bottom-5">
	<div class="header">
		<div class="header-title">
			<%=Label.get("jobgroup.upload")%>
		</div>
		<div class="header-close-button">
	    	<span><a href="#" onclick="window.close();"><img alt="ÆË¾÷Ã¢ ´Ý±â" src="images/pop_close.png"></a></span>
		</div>
	</div>
</div>

<!-- <br>
<font size="3"><b>Job Group Upload</b></font>-->
 
<div class="popup-content-wrap Margin-top-20 Margin-bottom-10">
<%
	String queryString = request.getQueryString();
	if (queryString == null) { /* at first */
%>
<form name="form1" action="form_setting_jobgroup_upload.jsp?step=2" enctype="multipart/form-data" method="POST">

<table class="Table njf-table__typea Margin-bottom-10" >
<tr>
	<td><input type="file" style="width:100%" name="uploadfile"></td>
</tr>
</table>

<table class="Width-100">
	<tr>
		<td class="Text-center">
			<input class="Button" type="submit" value="<%=Label.get("common.btn.submit")%>">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()">
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
		ExcelToFromJobGroup  exceltool      = new ExcelToFromJobGroup();
		List<JobGroup>       jobGroupList   = Collections.EMPTY_LIST;
		ObjectOutputStream   fout           = null;
		File                 tempFile       = null;
		try {
			xlsIn = fileitem.getInputStream();
			exceltool.parseExcel(xlsIn);
			jobGroupList = exceltool.getJobGroupList();
			
			tempFile = new File(Util.getHomeDirectory()+"/tmp/jobgroup-upload-"+getUserId(request)+"_"+Util.getCurrentYYYYMMDDHHMMSSMS()+".tmp");
			fout = new ObjectOutputStream(new FileOutputStream(tempFile));
			fout.writeObject(jobGroupList);
		}finally {
			try { fout.close();  }catch(Exception ignore) {}
			try { xlsIn.close(); }catch(Exception ignore) {}
		}
		
		ControllerAdminLocal admin = getControllerAdmin();
		AdminAuth auth      = new AdminAuth(getUserId(request), getUserIp(request));
		List<JobGroupAttrDef> jobGroupAttrDefs = admin.getJobGroupAttrDefsByQuery("", " ORDER BY DISPLAY_ORDER ");
%>
<label style="font-size:14px"><b>Upload file : [<%=filename%>]</b></label>
<form name="form2" action="action_setting.jsp" method="POST">
<input type="hidden" name="cmd" value="upload_jobgroup">
<input type="hidden" name="filename" value="<%=tempFile.getAbsolutePath() %>">

<table class="Table Margin-bottom-10 Margin-top-5">
<thead>
<tr>
    <th style="width:3%;"></th>
    <th style="width:40%;"><%=Label.get("jobgroup.id")%></th>
    <th><%=Label.get("jobgroup.name")%></th>
    <th><%=Label.get("jobgroup.desc")%></th>
<%
    for (JobGroupAttrDef attrDef : jobGroupAttrDefs) {
%>
    <th><a title="[<%=conv(attrDef.getId())%>] <%=conv(attrDef.getDesc())%>"><%=conv(attrDef.getName())%></a></th>
<%
    }
%>    
</tr>
</thead>
<tbody>
<%
	int i=0;
    for (JobGroup jobgroup : jobGroupList) {
%>
<tr>
    <td style="padding:0px;"><%= (++i)%></td>
    <td class="Text-left"><b><%=conv(jobgroup.getId())%></b></td>
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

<table class="Width-100">
	<tr>
		<td class="Text-center">
			<input class="Button" type="submit" value="<%=Label.get("common.btn.submit")%>" onclick="doSubmit()">
			<input class="Button" type="button" value="<%=Label.get("common.btn.close")%>" onclick="window.close()">
		</td>
	</tr>
</table>
</form>
<%
	}
%>

</div>
</center>
</body>
</html>
