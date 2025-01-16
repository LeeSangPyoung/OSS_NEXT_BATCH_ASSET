package nexcore.scheduler.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nexcore.scheduler.entity.JobGroup;
import nexcore.scheduler.entity.JobGroupAttrDef;
import nexcore.scheduler.msg.Label;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  JobGroup 으로 부터 엑셀 (xls) 파일 생성 </li>
 * <li>작성일 : 2013. 3. 12.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class ExcelToFromJobGroup {
	
	
	// #####################################################################################
	// ############################     아래는 JobGroup -> Excel  ##########################
	// #####################################################################################

	private static final Set<String>        mainColNameSet   = new HashSet(Arrays.asList(new String[]{
		"#", "GROUP_ID", "GROUP_NAME", "GROUP_DESC", "PARENT_ID", "CREATOR_ID", "OWNER_ID", "CREATE_TIME", "LAST_MODIFY_TIME"}));
	
	private Sheet mainSheet;
	
	private List<JobGroup> jobGroupList = new LinkedList<JobGroup>();
	
	public ExcelToFromJobGroup() {
	}
	
	/**
	 * 현재 ROW가 header row 인지 체크하여, header row 일 경우 cell 의 index 정보를 Map으로 만들어 리턴함.
	 * @param colNameSet 체크할 header 의 컬럼명 set.
	 * @param row 현재 row
	 * @return
	 */
	private Map<String, Short> checkAndReturnColumnIndexMap(Set<String> colNameSet, Row row) {
		Set<String> cellValueSet = new HashSet(); 
		for (Cell cell : row) {
			if (!Util.isBlank(getCellValue(cell))) {
				cellValueSet.add(getCellValue(cell).toUpperCase());
			}
		}
		
		if (cellValueSet.containsAll(colNameSet)) {
			// 이 row 가 header row 임. 여기서는 header 의 각 컬럼의 index 정보를 찾는다.
			Map<String, Short> colIndexMap = new HashMap(); // <COL NAME, COL INDEX>
			short minColIx = row.getFirstCellNum();
			short maxColIx = row.getLastCellNum();
			for(short colIx=minColIx; colIx<maxColIx; colIx++) {
				Cell cell = row.getCell(colIx);
				if(cell == null) {
					continue;
				}else { /* 가변 속성 처리를 위해 모든 컬럼에 대해 인덱스 정보를 담는다 */
					colIndexMap.put(getCellValue(cell), colIx);
				}
			}
			return colIndexMap;
		}
		return null;
		
	}
	
	private String getCellValue(Cell cell) {
		if (cell == null) {
			return null;
		}else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			double val = cell.getNumericCellValue();
			// if you input '3' into cell, getNumericCellValue() return '3.0'.    

			if (val - (int)val > 0) {
				return String.valueOf(val);
			}else {
				return String.valueOf((int)val); // 소수점 제거.
			}
		}else {
			return Util.trimIfNotNull(cell.getStringCellValue());
		}
	}
	
	
	public List<JobGroup> getJobGroupList() {
		return jobGroupList;
	}
	
	/**
	 * main sheet 를 파싱하여 JobDefinition 객체를 만든다.
	 */
	private void loadMain() throws IOException {
		Map<String, Short> colIndexMap = null; // <COL NAME, COL INDEX>
		Set<String> jobGroupIdList = new LinkedHashSet<String>();
		
		for (Row row : mainSheet) {
			if (colIndexMap == null) { // 아직 header 를 못찾은 경우  
				colIndexMap = checkAndReturnColumnIndexMap(mainColNameSet, row);
			}else {
				String jobGroupId = getCellValue(row.getCell(colIndexMap.get("GROUP_ID")));
				if (Util.isBlank(jobGroupId)) continue;
				
				if (!jobGroupIdList.add(jobGroupId)) { // dup check
					throw new RuntimeException("Duplicated Job Group ID ["+jobGroupId+"]");
				}
				JobGroup jobgroup = new JobGroup();
				jobgroup.setId(jobGroupId);
				jobgroup.setName(          getCellValue(row.getCell(colIndexMap.get("GROUP_NAME"))));
				jobgroup.setDesc(          getCellValue(row.getCell(colIndexMap.get("GROUP_DESC"))));
				jobgroup.setParentId(      getCellValue(row.getCell(colIndexMap.get("PARENT_ID"))));
				jobgroup.setCreatorId(     getCellValue(row.getCell(colIndexMap.get("CREATOR_ID"))));
				jobgroup.setOwnerId(       getCellValue(row.getCell(colIndexMap.get("OWNER_ID"))));

				/*
				 * A_ 가 붙은 컬럼을 추가 속성으로 인식한다.
				 */
				for (String colName : colIndexMap.keySet()) {
					if (!Util.isBlank(colName) && colName.startsWith("A_")) {
						jobgroup.setAttribute(colName.substring(2), getCellValue(row.getCell(colIndexMap.get(colName))));
					}
				}
				jobGroupList.add(jobgroup);
			}
		}
	}
	
	public void parseExcel(InputStream xlsIn) {
		try {
			Workbook wb = new HSSFWorkbook(xlsIn);
			mainSheet   = wb.getSheet("JobDefinition-main");
			if (mainSheet==null) {
				throw new Exception("JobDefinition-main sheet not found");
			}
			loadMain();
		}catch(Exception e) {
			throw Util.toRuntimeException(e);
		}

	}
	

	// #####################################################################################
	// ############################     아래는 JobGroup -> Excel  ##########################
	// #####################################################################################
	
	private HSSFCellStyle titleStyle;
	private HSSFCellStyle headerStyle;
	private HSSFCellStyle bodyStyle;
	private HSSFCellStyle bodyStyleLeftAlign;
	
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

    // ###################################### MAIN SHEET #######################################
    private void createMainSheet(HSSFWorkbook workbook, List<JobGroup> jobGroupList, List<JobGroupAttrDef> jobGroupAttrDefList, long current, String localDatetimePattern) {
        HSSFSheet sheet = workbook.createSheet("JobDefinition-main");

        // Title
        HSSFRow titleRow = sheet.createRow(0);
        createCell(titleRow, 0, Label.get("job.jobgroup") + " (" + FastDateFormat.getInstance(localDatetimePattern).format(current) + ")", titleStyle);
        
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7)); //가로병합
        
        int rowIndex = 0;
        int columnIndex = 0;

        rowIndex++;
        rowIndex++;

        HSSFRow headerRow = sheet.createRow(rowIndex++);
        createCell(headerRow, columnIndex++, "#",                     headerStyle);
        createCell(headerRow, columnIndex++, "GROUP_ID",              headerStyle);
        createCell(headerRow, columnIndex++, "GROUP_NAME",            headerStyle);
        createCell(headerRow, columnIndex++, "GROUP_DESC",            headerStyle);
        createCell(headerRow, columnIndex++, "PARENT_ID",             headerStyle);
        
        for (JobGroupAttrDef attrDef : jobGroupAttrDefList) {
        	createCell(headerRow, columnIndex++, "A_"+attrDef.getId(),     headerStyle);
        }
        
        createCell(headerRow, columnIndex++, "CREATOR_ID",            headerStyle);
        createCell(headerRow, columnIndex++, "OWNER_ID",              headerStyle);
        createCell(headerRow, columnIndex++, "CREATE_TIME",           headerStyle);
        createCell(headerRow, columnIndex++, "LAST_MODIFY_TIME",      headerStyle);
        
        // Body
        int seq = 0;

        for (JobGroup jobGroup : jobGroupList) {
            seq++;
            columnIndex = 0;
            
            HSSFRow row = sheet.createRow(rowIndex++);
            
            createCell(row, columnIndex++, Integer.toString(seq),                        bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobGroup.getId()),                   bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobGroup.getName()),                 bodyStyleLeftAlign);
            createCell(row, columnIndex++, Util.nvl(jobGroup.getDesc()),                 bodyStyleLeftAlign);
            createCell(row, columnIndex++, Util.nvl(jobGroup.getParentId()),             bodyStyle);
            
            for (JobGroupAttrDef attrDef : jobGroupAttrDefList) {
            	/* 추가 속성은 A_ 를 PREFIX 로 붙인다 */
            	createCell(row, columnIndex++, Util.nvl(jobGroup.getAttribute(attrDef.getId())),  bodyStyle);
            }
            
            createCell(row, columnIndex++, Util.nvl(jobGroup.getCreatorId()),            bodyStyle);
            createCell(row, columnIndex++, Util.nvl(jobGroup.getOwnerId()),              bodyStyle);
            createCell(row, columnIndex++, FastDateFormat.getInstance(localDatetimePattern).format(DateUtil.getTimestampLong(jobGroup.getCreateTime())), bodyStyle);
            createCell(row, columnIndex++, FastDateFormat.getInstance(localDatetimePattern).format(DateUtil.getTimestampLong(jobGroup.getLastModifyTime())), bodyStyle);
        }
        
        // Auto Resize
        for (int i = 0; i < columnIndex; i++) { // [0] 컬럼은 autoresize 하지 않는다.
            sheet.autoSizeColumn(i);
        }
    }
    
	public void toExcel(HSSFWorkbook workbook, List<JobGroup> jobGroupList, List<JobGroupAttrDef> jobGroupAttrDefList, long current, String localDatetimePattern) {
		// 폰트, style 초기화.
		initCellStyle(workbook);
		
        // MAIN sheet 생성
        createMainSheet(workbook, jobGroupList, jobGroupAttrDefList, current, localDatetimePattern);
	}

}
