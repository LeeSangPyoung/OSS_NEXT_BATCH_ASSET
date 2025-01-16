<%@ include file= "common_functions.jsp"
%><%@ page language="java" contentType="application/x-msdownload; charset=UTF-8" pageEncoding="euc-kr"
%><%@ page import="org.apache.poi.hssf.usermodel.*"
%><%@ page import="org.apache.poi.hssf.util.*"
%><%@ page import="org.apache.poi.ss.util.CellRangeAddressList"
%><%@ page import="org.apache.poi.ss.usermodel.DataValidation"
%><%
	ControllerAdminLocal admin = getControllerAdmin();
    AdminAuth auth      = new AdminAuth(getUserId(request), getUserIp(request));
    String cmd          = request.getParameter("cmd");

    List<JobGroupAttrDef> jobGroupAttrDefs = admin.getJobGroupAttrDefsByQuery("", " ORDER BY DISPLAY_ORDER ");
    List<JobGroup>        jobGroupList     = admin.getAllJobGroups();
    List<JobGroup>        jobGroupTreeList = admin.analyzeToJobGroupsTreeList(jobGroupList);
    
    HSSFWorkbook workbook = null;
    boolean exceptionOccurd = false;
    try {
        long current = System.currentTimeMillis();
        String currentDateTime = toDatetimeString(current, false).replaceAll("/|:", "-").replaceAll(" ", "_");
        
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=jobgroup-" + currentDateTime + ".xls");

        workbook = new HSSFWorkbook();

        ExcelToFromJobGroup excel = new ExcelToFromJobGroup();
        excel.toExcel(workbook, jobGroupTreeList, jobGroupAttrDefs, current, localDatetimePattern);
    } catch (Exception e) {
        exceptionOccurd = true;
        throw e;
    } finally {
        OutputStream ostream = response.getOutputStream();
        workbook.write(ostream);
        ostream.flush();
        if (!exceptionOccurd) {
            ostream.close();
        }
    }

%>