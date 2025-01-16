package nexcore.scheduler.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.entity.PreJobCondition;
import nexcore.scheduler.msg.Label;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  JobDefiniton 을 엑셀로 변환 </li>
 * <li>작성일 : 2012. 1. 26.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class ExcelFromJobDefinition {
	private HSSFCellStyle titleStyle;
	private HSSFCellStyle headerStyle;
	private HSSFCellStyle bodyStyle;
	private HSSFCellStyle bodyStyleLeftAlign;

	private HSSFSheet mainSheet = null;
	private HSSFSheet preJobSheet = null;
	private HSSFSheet postJobTriggerSheet = null;
	private HSSFSheet parameterSheet = null;
	private HSSFSheet hiddenSheet = null;

	private String NAMED_CELL_NAME_AGENT_ID = "agent_id";
	private String SHEET_NAME_HIDDEN = "hidden";
	private String SHEET_NAME_MAIN = "JobDefinition-main";
	private String SHEET_NAME_PREJOB = "PreJobCondition";
	private String SHEET_NAME_POSTJOB = "PostJobTrigger";
	private String SHEET_NAME_PARAMETERS = "Parameters";
	
	public ExcelFromJobDefinition() {
	}
	
	private HSSFCell createCell(HSSFRow row, int index, String value, HSSFCellStyle style) {
        HSSFCell cell = row.createCell(index);
        cell.setCellValue(new HSSFRichTextString(value));
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);

        cell.setCellStyle(style);
        return cell;
	}
	
	private void initCellStyle(HSSFWorkbook workbook) {
		// TITLE
        titleStyle = workbook.createCellStyle();
        titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        titleStyle.setWrapText(false);

        HSSFFont font1 = workbook.createFont(); //폰트 객체 생성
        font1.setFontHeightInPoints((short) 15); //폰트 크기
        font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); //폰트 굵게
        titleStyle.setFont(font1);

        // HEADER
        headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        headerStyle.setWrapText(false);

        HSSFFont font2 = workbook.createFont(); //폰트 객체 생성
        font2.setFontHeightInPoints((short) 12); //폰트 크기
        headerStyle.setFont(font2);

        headerStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        headerStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        headerStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        headerStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        headerStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
        headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        
        // BODY
        bodyStyle = workbook.createCellStyle();
        bodyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        bodyStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        bodyStyle.setWrapText(false);

        HSSFFont font3 = workbook.createFont(); //폰트 객체 생성
        font3.setFontHeightInPoints((short) 10); //폰트 크기
        bodyStyle.setFont(font3);

        bodyStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        bodyStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        bodyStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        bodyStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);

        // BODY LEFT ALIGN
        bodyStyleLeftAlign = workbook.createCellStyle();
        bodyStyleLeftAlign.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        bodyStyleLeftAlign.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        bodyStyleLeftAlign.setWrapText(false);
        
        HSSFFont font4 = workbook.createFont(); //폰트 객체 생성
        font4.setFontHeightInPoints((short) 10); //폰트 크기
        bodyStyleLeftAlign.setFont(font4);
        
        bodyStyleLeftAlign.setBorderTop(HSSFCellStyle.BORDER_THIN);
        bodyStyleLeftAlign.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        bodyStyleLeftAlign.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        bodyStyleLeftAlign.setBorderRight(HSSFCellStyle.BORDER_THIN);
    }
	
	// ###################################### HIDDEN SHEET #####################################
	private void generateHiddenSheet(HSSFWorkbook workbook, List<String> agentIdList) {
		int rowIndex = 0;
		int columnIndex = 0;
		for (String agentId : agentIdList) {
			HSSFRow row = hiddenSheet.createRow(rowIndex++);
			createCell(row, columnIndex, agentId, null);
		}

		Name namedCell = workbook.createName();
		namedCell.setNameName(NAMED_CELL_NAME_AGENT_ID);
		namedCell.setRefersToFormula(SHEET_NAME_HIDDEN + "!$A$1:$A$" + rowIndex);
		int sheetIndex = workbook.getSheetIndex(hiddenSheet);
		workbook.setSheetHidden(sheetIndex, true);
	}
	
    // ###################################### MAIN SHEET #######################################
    private void generateMainSheet(List<JobDefinition> jobdefList, List<String> jobTypeUseList, List<String> logLevelUseList, Map calendarMap, long current, String localDatetimePattern) {

        // Title
        HSSFRow titleRow = mainSheet.createRow(0);
        createCell(titleRow, 0, Label.get("jobdef") + " (" + FastDateFormat.getInstance(localDatetimePattern).format(current) + ")", titleStyle);
        
        mainSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10)); //가로병합
        
        int rowIndex = 0;
        int columnIndex = 0;

        rowIndex++;
        rowIndex++;

        HSSFRow headerRow = mainSheet.createRow(rowIndex++);
        createCell(headerRow, columnIndex++, "#",                     headerStyle);
        createCell(headerRow, columnIndex++, "JOB_ID",                headerStyle);
        createCell(headerRow, columnIndex++, "JOB_GROUP_ID",          headerStyle);
        createCell(headerRow, columnIndex++, "OWNER",                 headerStyle);
        createCell(headerRow, columnIndex++, "JOB_DESC",              headerStyle);
        createCell(headerRow, columnIndex++, "TIME_FROM",             headerStyle);
        createCell(headerRow, columnIndex++, "TIME_UNTIL",            headerStyle);
        createCell(headerRow, columnIndex++, "REPEAT_YN",             headerStyle);
        createCell(headerRow, columnIndex++, "REPEAT_INTVAL",         headerStyle);
        createCell(headerRow, columnIndex++, "REPEAT_INTVAL_GB",      headerStyle);
        createCell(headerRow, columnIndex++, "REPEAT_IF_ERROR",       headerStyle);
        createCell(headerRow, columnIndex++, "REPEAT_MAX_OK",         headerStyle);
        createCell(headerRow, columnIndex++, "REPEAT_EXACT_EXP",      headerStyle);
        createCell(headerRow, columnIndex++, "CONFIRM_NEED_YN",       headerStyle);
        createCell(headerRow, columnIndex++, "PARALLEL_GROUP",        headerStyle);
        createCell(headerRow, columnIndex++, "JOB_TYPE",              headerStyle);
        createCell(headerRow, columnIndex++, "AGENT_NODE",            headerStyle);
        createCell(headerRow, columnIndex++, "AGENT_NODE2",           headerStyle);
        createCell(headerRow, columnIndex++, "COMPONENT_NAME",        headerStyle);
        createCell(headerRow, columnIndex++, "SCHEDULE_TYPE",         headerStyle);
        createCell(headerRow, columnIndex++, "DAYS_IN_MONTH",         headerStyle);
        createCell(headerRow, columnIndex++, "MONTHS",                headerStyle);
        createCell(headerRow, columnIndex++, "DAYS_OF_WEEK",          headerStyle);
        createCell(headerRow, columnIndex++, "WEEKDAY_MONTHDAY_TYPE", headerStyle);
        createCell(headerRow, columnIndex++, "CALENDAR_ID",           headerStyle);
        createCell(headerRow, columnIndex++, "CALENDAR_EXP",          headerStyle);
        createCell(headerRow, columnIndex++, "DAY_SCHEDULE_TYPE",     headerStyle);
        createCell(headerRow, columnIndex++, "BEFORE_AFTER_EXP",      headerStyle);
        createCell(headerRow, columnIndex++, "SHIFT_EXP",             headerStyle);
        createCell(headerRow, columnIndex++, "SHIFT_EXP2",            headerStyle);
        createCell(headerRow, columnIndex++, "FIXED_DAYS",            headerStyle);
        createCell(headerRow, columnIndex++, "EXTRA_SCHEDULE",        headerStyle);
        createCell(headerRow, columnIndex++, "BASE_DATE_CAL_ID",      headerStyle);
        createCell(headerRow, columnIndex++, "BASE_DATE_LOGIC",       headerStyle);
        createCell(headerRow, columnIndex++, "LOG_LEVEL",             headerStyle);
        createCell(headerRow, columnIndex++, "CREATE_TIME",           headerStyle);
        createCell(headerRow, columnIndex++, "LAST_MODIFY_TIME",      headerStyle);

        // Body
        int seq = 0;

        for (JobDefinition jobdef : jobdefList) {
            seq++;
            columnIndex = 0;
            
            HSSFRow row = mainSheet.createRow(rowIndex++);
            
            createCell(row, columnIndex++, Integer.toString(seq),                       bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getJobId()),                 bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getJobGroupId()),            bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getOwner()),                 bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getDescription()),           bodyStyleLeftAlign);
            createCell(row, columnIndex++, Util.nvl(jobdef.getTimeFrom()),              bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getTimeUntil()),             bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getRepeatYN()),              bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getRepeatIntval()),          bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getRepeatIntvalGb()),        bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getRepeatIfError()),         bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getRepeatMaxOk()),           bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getRepeatExactExp()),        bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getConfirmNeedYN()),         bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getParallelGroup()),         bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getJobType()),               bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getAgentNodeMaster()),       bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getAgentNodeSlave()),        bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getComponentName()),         bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getScheduleType()),          bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getDaysInMonth()),           bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getMonths()),                bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getDaysOfWeek()),            bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getWeekdayMonthdayType()),   bodyStyle);
            createCell(row, columnIndex++, Util.isBlank(jobdef.getCalendarId()) ? "" : "["+jobdef.getCalendarId()+"] "+calendarMap.get(jobdef.getCalendarId()), bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getCalendarExps()),          bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getDayOfMonthScheduleType()),bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getBeforeAfterExp()),        bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getShiftExp()),              bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getShiftExp2()),             bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getFixedDays()),             bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getExtraSchedule()),         bodyStyle);
            createCell(row, columnIndex++, Util.isBlank(jobdef.getBaseDateCalId()) ? "" : "["+jobdef.getBaseDateCalId()+"] "+calendarMap.get(jobdef.getBaseDateCalId()), bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getBaseDateLogic()),         bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobdef.getLogLevel()),              bodyStyle);
            createCell(row, columnIndex++, FastDateFormat.getInstance(localDatetimePattern).format(Util.parseYYYYMMDDHHMMSS(jobdef.getCreateTime())), bodyStyle);
            createCell(row, columnIndex++, FastDateFormat.getInstance(localDatetimePattern).format(DateUtil.getTimestampLong(jobdef.getLastModifyTime())), bodyStyle);
        }
        
        // Auto Resize
        for (int i = 0; i < columnIndex; i++) { // [0] 컬럼은 autoresize 하지 않는다.
            mainSheet.autoSizeColumn(i);
        }

        // REPEAT_YN
        CellRangeAddressList addressListRepeatYN    = new CellRangeAddressList(3, rowIndex+100, 7,7);
        DVConstraint         dvConstraintRepeatYN   = DVConstraint.createExplicitListConstraint(new String[]{"Y", "N"});
        DataValidation       dataValidationRepeatYN = new HSSFDataValidation (addressListRepeatYN, dvConstraintRepeatYN);
        dataValidationRepeatYN.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationRepeatYN);
        
        // REPEAT_YN
        CellRangeAddressList addressListRepeatGB    = new CellRangeAddressList(3, rowIndex+100, 9,9);
        DVConstraint         dvConstraintRepeatGB   = DVConstraint.createExplicitListConstraint(new String[]{"START", "END", "EXACT"});
        DataValidation       dataValidationRepeatGB = new HSSFDataValidation (addressListRepeatGB, dvConstraintRepeatGB);
        dataValidationRepeatGB.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationRepeatGB);

        // REPEAT_ERROR
        CellRangeAddressList addressListRepeatErr    = new CellRangeAddressList(3, rowIndex+100, 10,10);
        DVConstraint         dvConstraintRepeatErr   = DVConstraint.createExplicitListConstraint(new String[]{"STOP", "IGNORE"});
        DataValidation       dataValidationRepeatErr = new HSSFDataValidation (addressListRepeatErr, dvConstraintRepeatErr);
        dataValidationRepeatErr.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationRepeatErr);

        // CONFIRM_YN
        CellRangeAddressList addressListConfirmYN    = new CellRangeAddressList(3, rowIndex+100, 13,13);
        DVConstraint         dvConstraintConfirmYN   = DVConstraint.createExplicitListConstraint(new String[]{"Y", "N"});
        DataValidation       dataValidationConfirmYN = new HSSFDataValidation (addressListConfirmYN, dvConstraintConfirmYN);
        dataValidationConfirmYN.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationConfirmYN);

        // JOB_TYPE
        CellRangeAddressList addressListJobType    = new CellRangeAddressList(3, rowIndex+100, 15,15);
        DVConstraint         dvConstraintJobType   = DVConstraint.createExplicitListConstraint(jobTypeUseList.toArray(new String[jobTypeUseList.size()]));
        DataValidation       dataValidationJobType = new HSSFDataValidation (addressListJobType, dvConstraintJobType);
        dataValidationJobType.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationJobType);

        // AGENT_NODE
        DVConstraint dvConstraintAgentNode = DVConstraint.createFormulaListConstraint(NAMED_CELL_NAME_AGENT_ID);
        CellRangeAddressList addressListAgentNode = new CellRangeAddressList(3, rowIndex + 100, 16, 16);
        DataValidation dataValidationAgentNode = new HSSFDataValidation(addressListAgentNode, dvConstraintAgentNode);
        dataValidationAgentNode.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationAgentNode);

        // AGENT_NODE2
        CellRangeAddressList addressListAgentNode2 = new CellRangeAddressList(3, rowIndex + 100, 17, 17);
        DataValidation dataValidationAgentNode2 = new HSSFDataValidation(addressListAgentNode2, dvConstraintAgentNode);
        dataValidationAgentNode2.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationAgentNode2); 
    
        // SCHEDULE_TYPE
        CellRangeAddressList addressListScheduleType    = new CellRangeAddressList(3, rowIndex+100, 19,19);
        DVConstraint         dvConstraintScheduleType   = DVConstraint.createExplicitListConstraint(new String[]{"EXPRESSION", "FIXED"});
        DataValidation       dataValidationScheduleType = new HSSFDataValidation (addressListScheduleType, dvConstraintScheduleType);
        dataValidationScheduleType.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationScheduleType);
    
        // WEEKDAY_MONTHDAY_TYPE
        CellRangeAddressList addressListAndOr    = new CellRangeAddressList(3, rowIndex+100, 23,23);
        DVConstraint         dvConstraintAndOr   = DVConstraint.createExplicitListConstraint(new String[]{"AND", "OR"});
        DataValidation       dataValidationAndOr = new HSSFDataValidation (addressListAndOr, dvConstraintAndOr);
        dataValidationAndOr.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationAndOr);
    
        // CALENDAR_ID
        CellRangeAddressList addressListCalId    = new CellRangeAddressList(3, rowIndex+100, 24,24);
        String[] calString = new String[calendarMap.size()];
        int calIdx = 0;
        for (Object calId : calendarMap.keySet()) {
            calString[calIdx++] = "["+calId+"] "+ calendarMap.get(calId);
        }
        DVConstraint         dvConstraintCalId   = DVConstraint.createExplicitListConstraint(calString);
        DataValidation       dataValidationCalId = new HSSFDataValidation (addressListCalId, dvConstraintCalId);
        dataValidationCalId.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationCalId);
    
        // DAY_SCHEDULE_TYPE
        CellRangeAddressList addressListDayScheduleType    = new CellRangeAddressList(3, rowIndex+100, 26,26);
        DVConstraint         dvConstraintDayScheduleType   = DVConstraint.createExplicitListConstraint(new String[]{"NUMBER", "CALENDAR"});
        DataValidation       dataValidationDayScheduleType = new HSSFDataValidation (addressListDayScheduleType, dvConstraintDayScheduleType);
        dataValidationDayScheduleType.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationDayScheduleType);
    
        // BASEDATE_CAL_ID
        CellRangeAddressList addressListBaseCalId    = new CellRangeAddressList(3, rowIndex+100, 32,32);
        DataValidation       dataValidationBaseCalId = new HSSFDataValidation (addressListBaseCalId, dvConstraintCalId);
        dataValidationBaseCalId.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationBaseCalId);
    
        // LOG_LEVEL
        CellRangeAddressList addressListLogLevel    = new CellRangeAddressList(3, rowIndex+100, 34,34);
        DVConstraint         dvConstraintLogLevel   = DVConstraint.createExplicitListConstraint(logLevelUseList.toArray(new String[logLevelUseList.size()]));
        DataValidation       dataValidationLogLevel = new HSSFDataValidation (addressListLogLevel, dvConstraintLogLevel);
        dataValidationLogLevel.setSuppressDropDownArrow(false);
        mainSheet.addValidationData(dataValidationLogLevel);

    }
    
    // ###################################### Pre Job Condition Sheet #######################################
    private void generatePreJobSheet(List<JobDefinition> jobdefList, long current) {

        // Title
        HSSFRow titleRow = preJobSheet.createRow(0);
        createCell(titleRow, 0, Label.get("jobdef") + " Pre Job Conditions ", titleStyle);
        
        preJobSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4)); //가로병합
        
        int rowIndex = 0;
        int columnIndex = 0;

        rowIndex++;
        rowIndex++;

        HSSFRow headerRow = preJobSheet.createRow(rowIndex++);
        createCell(headerRow, columnIndex++, "JOB_ID",     headerStyle);
        createCell(headerRow, columnIndex++, "SEQ",        headerStyle);
        createCell(headerRow, columnIndex++, "PRE_JOB_ID", headerStyle);
        createCell(headerRow, columnIndex++, "OK_FAIL",    headerStyle);
        createCell(headerRow, columnIndex++, "AND_OR",     headerStyle);

        // Body
        for (JobDefinition jobdef : jobdefList) {
            int seq=0;

            for (PreJobCondition prejob : jobdef.getPreJobConditions()) {
	            columnIndex = 0;
	            HSSFRow row = preJobSheet.createRow(rowIndex++);
	            
	            createCell(row, columnIndex++, Util.nvl(jobdef.getJobId()),    bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(++seq),                bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(prejob.getPreJobId()), bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(prejob.getOkFail()),   bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(prejob.getAndOr()),    bodyStyle);
            }
        }
        
        // Auto Resize
        for (int i = 0; i < columnIndex; i++) { // [0] 컬럼은 autoresize 하지 않는다.
            preJobSheet.autoSizeColumn(i);
        }

        // OK_FAIL
        CellRangeAddressList addressListOkFail    = new CellRangeAddressList(3, rowIndex+1000, 3, 3);
        DVConstraint         dvConstraintOkFail   = DVConstraint.createExplicitListConstraint(new String[]{"OK", "FAIL", "OKFAIL", "INSEXIST", "INSNONE", "OK_OR_INSNONE", "FAIL_OR_INSNONE", "OKFAIL_OR_INSNONE", "ALLINS_OK", "ALLINS_FAIL", "ALLINS_OKFAIL"});
        DataValidation       dataValidationOkFail = new HSSFDataValidation (addressListOkFail, dvConstraintOkFail);
        dataValidationOkFail.setSuppressDropDownArrow(false);
        preJobSheet.addValidationData(dataValidationOkFail);

        // AND_OR
        CellRangeAddressList addressListAndOr    = new CellRangeAddressList(3, rowIndex+1000, 4, 4);
        DVConstraint         dvConstraintAndOr   = DVConstraint.createExplicitListConstraint(new String[]{"AND", "OR"});
        DataValidation       dataValidationAndOr = new HSSFDataValidation (addressListAndOr, dvConstraintAndOr);
        dataValidationAndOr.setSuppressDropDownArrow(false);
        preJobSheet.addValidationData(dataValidationAndOr);

    }

    // ###################################### Post Job Trigger Sheet #######################################
    private void generatePostJobTriggerSheet(List<JobDefinition> jobdefList, long current) {

        // Title
        HSSFRow titleRow = postJobTriggerSheet.createRow(0);
        createCell(titleRow, 0, Label.get("jobdef") + " Post Job Triggers ", titleStyle);
        
        postJobTriggerSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7)); //가로병합
        
        int rowIndex = 0;
        int columnIndex = 0;

        rowIndex++;
        rowIndex++;

        HSSFRow headerRow = postJobTriggerSheet.createRow(rowIndex++);
        createCell(headerRow, columnIndex++, "JOB_ID",          headerStyle);
        createCell(headerRow, columnIndex++, "SEQ",             headerStyle);
        createCell(headerRow, columnIndex++, "WHEN",            headerStyle);
        createCell(headerRow, columnIndex++, "CHECK_VALUE1",    headerStyle);
        createCell(headerRow, columnIndex++, "CHECK_VALUE2",    headerStyle);
        createCell(headerRow, columnIndex++, "CHECK_VALUE3",    headerStyle);
        createCell(headerRow, columnIndex++, "TRIGGER_JOB_ID",  headerStyle);
        createCell(headerRow, columnIndex++, "INSTANCE_COUNT",  headerStyle);

        // Body
        for (JobDefinition jobdef : jobdefList) {
            int seq=0;

            for (PostJobTrigger trigger : jobdef.getTriggerList()) {
	            columnIndex = 0;
	            HSSFRow row = postJobTriggerSheet.createRow(rowIndex++);
	            
	            createCell(row, columnIndex++, Util.nvl(jobdef.getJobId()),             bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(++seq),                         bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(trigger.getWhen()),             bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(trigger.getCheckValue1()),      bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(trigger.getCheckValue2()),      bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(trigger.getCheckValue3()),      bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(trigger.getTriggerJobId()),     bodyStyle);
	            createCell(row, columnIndex++, Util.nvl(trigger.getJobInstanceCount()), bodyStyle);
            }
        }
        
        // Auto Resize
        for (int i = 0; i < columnIndex; i++) { // [0] 컬럼은 autoresize 하지 않는다.
            postJobTriggerSheet.autoSizeColumn(i);
        }

        // END, ENDOK, ENDFAIL, RETVAL
        CellRangeAddressList addressListWhen    = new CellRangeAddressList(3, rowIndex+1000, 2, 2);
        DVConstraint         dvConstraintWhen   = DVConstraint.createExplicitListConstraint(new String[]{"END", "ENDOK", "ENDFAIL", "RETVAL"});
        DataValidation       dataValidationWhen = new HSSFDataValidation (addressListWhen, dvConstraintWhen);
        dataValidationWhen.setSuppressDropDownArrow(false);
        postJobTriggerSheet.addValidationData(dataValidationWhen);

    }

    // ###################################### Parameter Sheet #######################################
    private void generateParameterSheet(List<JobDefinition> jobdefList, long current) {

        // Title
        HSSFRow titleRow = parameterSheet.createRow(0);
        createCell(titleRow, 0, Label.get("jobdef") + " Parameters ", titleStyle);

        parameterSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3)); //가로병합
        
        int rowIndex = 0;
        int columnIndex = 0;

        rowIndex++;
        rowIndex++;

        HSSFRow headerRow = parameterSheet.createRow(rowIndex++);
        createCell(headerRow, columnIndex++, "JOB_ID",      headerStyle);
        createCell(headerRow, columnIndex++, "SEQ",         headerStyle);
        createCell(headerRow, columnIndex++, "PARAM_NAME",  headerStyle);
        createCell(headerRow, columnIndex++, "PARAM_VALUE", headerStyle);

        // Body
        for (JobDefinition jobdef : jobdefList) {
            int seq=0;

            for (Map.Entry param : jobdef.getInParameters().entrySet()) {
                columnIndex = 0;
                HSSFRow row = parameterSheet.createRow(rowIndex++);
                
                createCell(row, columnIndex++, Util.nvl(jobdef.getJobId()), bodyStyle);
                createCell(row, columnIndex++, Util.nvl(++seq),             bodyStyle);
                createCell(row, columnIndex++, Util.nvl(param.getKey()),    bodyStyle);
                createCell(row, columnIndex++, Util.nvl(param.getValue()),  bodyStyle);
            }
        }
        
        // Auto Resize
        for (int i = 0; i < columnIndex; i++) { // [0] 컬럼은 autoresize 하지 않는다.
            parameterSheet.autoSizeColumn(i);
        }

    }

    /**
     * List Of JobDefinition 객체를 엑셀 Workbook 객체로 변환
     * @param workbook
     * @param jobdefList
     * @param agentIdList
     * @param jobTypeUseList
     * @param logLevelUseList
     * @param calendarMap
     * @param current
     * @param localDatetimePattern
     */
	public void toExcel(HSSFWorkbook workbook, List<JobDefinition> jobdefList, List<String> agentIdList, List<String> jobTypeUseList, List<String> logLevelUseList, Map calendarMap, long current, String localDatetimePattern) {
		// 폰트, style 초기화.
		initCellStyle(workbook);
		
		// Sheet 생성
		makeSheet(workbook);
		
		// HIDDEN sheet
		generateHiddenSheet(workbook, agentIdList);
		
        // MAIN sheet
		generateMainSheet(jobdefList, jobTypeUseList, logLevelUseList, calendarMap, current, localDatetimePattern);
        
        // 선행 job sheet
		generatePreJobSheet(jobdefList, current);

        // 후행 job sheet
		generatePostJobTriggerSheet(jobdefList, current);

        // 파라미터 sheet
		generateParameterSheet(jobdefList, current);

	}

    /**
     * Sheet 생성
     * @param workbook
     */
	private void makeSheet(HSSFWorkbook workbook) {
		mainSheet = workbook.createSheet(SHEET_NAME_MAIN);
		preJobSheet = workbook.createSheet(SHEET_NAME_PREJOB);
		postJobTriggerSheet = workbook.createSheet(SHEET_NAME_POSTJOB);
		parameterSheet = workbook.createSheet(SHEET_NAME_PARAMETERS);
		hiddenSheet = workbook.createSheet(SHEET_NAME_HIDDEN);
	}	
}
