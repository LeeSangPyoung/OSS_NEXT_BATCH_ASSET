<%@ include file= "common_functions.jsp"
%><%@ page language="java" contentType="application/x-msdownload; charset=UTF-8" pageEncoding="euc-kr"
%><%@ page import="org.apache.poi.hssf.usermodel.*"
%><%@ page import="org.apache.poi.hssf.util.*"
%><%@ page import="org.apache.poi.ss.util.CellRangeAddressList"
%><%@ page import="org.apache.poi.ss.usermodel.DataValidation"
%><jsp:include page="common_query_jobdef.jsp" /><%
    if (!checkLogin(request, response)) return; 

    List<JobDefinition> jobdefList = (List<JobDefinition>)request.getAttribute("jobdef_query_result");

    ControllerAdminLocal admin = getControllerAdmin();

    HSSFWorkbook workbook = null;
    boolean exceptionOccurd = false;
    try {
        long current = System.currentTimeMillis();
        String currentDateTime = toDatetimeString(current, false).replaceAll("/|:", "-").replaceAll(" ", "_");
        
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=jobdef-" + currentDateTime + ".xls");

        workbook = new HSSFWorkbook();

        List<String> agentIdList      = admin.getAgentIdList();
        List<String> jobTypeUseList   = admin.getJobTypeUsingList();
        List<String> logLevelUseList  = admin.getLogLevelUsingList();
        Map          calendarMap      = admin.getCalendarList();

        ExcelFromJobDefinition excel = new ExcelFromJobDefinition();
        excel.toExcel(workbook, jobdefList, agentIdList, jobTypeUseList, logLevelUseList, calendarMap, current, localDatetimePattern);
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

