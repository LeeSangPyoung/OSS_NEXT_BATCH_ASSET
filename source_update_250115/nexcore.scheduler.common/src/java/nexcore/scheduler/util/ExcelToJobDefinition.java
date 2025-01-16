package nexcore.scheduler.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.entity.PreJobCondition;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  엑셀(xls) 파일을 파싱하여 JobDefiniton 으로 변환 </li>
 * <li>작성일 : 2012. 1. 26.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class ExcelToJobDefinition {
    
    private static final Set<String>        preJobColNameSet = new HashSet(Arrays.asList(new String[]{
        "JOB_ID", "SEQ", "PRE_JOB_ID", "OK_FAIL", "AND_OR"}));
    
    private static final Set<String>        postJobTriggerColNameSet = new HashSet(Arrays.asList(new String[]{
        "JOB_ID", "SEQ", "WHEN", "CHECK_VALUE1", "CHECK_VALUE2", "CHECK_VALUE3", "TRIGGER_JOB_ID", "INSTANCE_COUNT"}));
    
    private static final Set<String>        paramColNameSet  = new HashSet(Arrays.asList(new String[]{
        "JOB_ID", "SEQ", "PARAM_NAME", "PARAM_VALUE"}));
    
    private static final Set<String>        mainColNameSet   = new HashSet(Arrays.asList(new String[]{
        "JOB_ID", "JOB_GROUP_ID", "OWNER", "JOB_DESC", "TIME_FROM", "TIME_UNTIL", 
        "REPEAT_YN", "REPEAT_INTVAL", "REPEAT_INTVAL_GB", "REPEAT_IF_ERROR", "REPEAT_MAX_OK", "REPEAT_EXACT_EXP", 
        "CONFIRM_NEED_YN", "PARALLEL_GROUP", "JOB_TYPE", "AGENT_NODE", "AGENT_NODE2", "COMPONENT_NAME", 
        "SCHEDULE_TYPE", "DAYS_IN_MONTH", "MONTHS", "DAYS_OF_WEEK", "WEEKDAY_MONTHDAY_TYPE", 
        "CALENDAR_ID", "CALENDAR_EXP", "DAY_SCHEDULE_TYPE", "BEFORE_AFTER_EXP", "SHIFT_EXP", "SHIFT_EXP2", "FIXED_DAYS", "EXTRA_SCHEDULE", 
        "BASE_DATE_CAL_ID", "BASE_DATE_LOGIC", "LOG_LEVEL"}));
    
    private Sheet mainSheet;
    private Sheet prejobSheet;
    private Sheet postTriggerSheet;
    private Sheet paramSheet;
    
    private Map<String, Map<String, String>>   parametersByJob     = new HashMap(); // <JOBID, <ParamName, ParamValue>>
    private Map<String, List<PreJobCondition>> preJobCondByJob     = new HashMap(); // <JOBID, List<PreJobCondition>>
    private Map<String, List<PostJobTrigger>>  postTriggerByJob    = new HashMap(); // <JOBID, List<PostTriggerJob>>

    private List<File> jobdefFileList = new LinkedList<File>();
    
    public ExcelToJobDefinition() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * 파싱된 JobDefinition 객체 파일 리스트
     * @return the jobdefFileList
     */
    public List<File> getJobdefFileList() {
        return jobdefFileList;
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
                }else {
                    if (colNameSet.contains(getCellValue(cell))) {
                        colIndexMap.put(getCellValue(cell), colIx);
                    }
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

    /**
     * 엑셀 파일에서 파싱한 JobDefinition 객체를 tmp 디렉토리에 파일로 저장한다.
     * @param jobdef
     * @return
     */
    private File writeJobDefinitionToTmpFile(File directory, JobDefinition jobdef) throws IOException {
        File file = new File(directory, jobdef.getJobId()+".jdf");
        JobDefinitionUtil.writeToFile(jobdef, file);
        return file;
    }

    /**
     * JobDefinition 객체를 저장할 temp 디렉토리 생성.
     * scheduler/tmp 밑에 임의의 디렉토리 생성 
     * @return
     */
    private File prepareTempDirectory() {
        for (int i=0; i<10; i++) {
            File tmpDirectory = new File(System.getProperty("NEXCORE_HOME")+"/tmp/jobdef_upload_"+System.currentTimeMillis());
            if (tmpDirectory.mkdirs()) {
                // 디렉토리가 정상적으로 생성되면 break.
                return tmpDirectory;
            }else {
                // 디렉토리가 이미 생성되어있는 경우이므로 잠시 쉬었다가 다시 만든다.
                Util.sleep(new Random().nextLong() % 1000); // 동시에 누군가가 upload 하는 것이므로 잠시 쉬었다가 다시 한다.
            }
        }
        
        // 10번 쉬어도 안될 경우 Exception 내고 다시 하라고 유도한다.
        throw new RuntimeException("Temp Directory Fail. ["+System.getProperty("NEXCORE_HOME")+"/tmp"+"]");
    }
    
    
    /**
     * PreJobCondition sheet 를 먼저 메모리에 Map 으로 올려둔다.
     */
    private void loadPreJobCondition() {
        Map<String, Short> colIndexMap = null; // <COL NAME, COL INDEX>
        
        for (Row row : prejobSheet) {
            if (colIndexMap == null) { // 아직 header 를 못찾은 경우  
                colIndexMap = checkAndReturnColumnIndexMap(preJobColNameSet, row);
            }else {
                String jobId    = getCellValue(row.getCell(colIndexMap.get("JOB_ID")));
                if (Util.isBlank(jobId)) continue;
                int    seq      = Util.toInt(getCellValue(row.getCell(colIndexMap.get("SEQ"))), 1);
                String preJobId = getCellValue(row.getCell(colIndexMap.get("PRE_JOB_ID")));
                String okFail   = getCellValue(row.getCell(colIndexMap.get("OK_FAIL")));
                String andOr    = getCellValue(row.getCell(colIndexMap.get("AND_OR")));
                
                PreJobCondition prejobcond = new PreJobConditionWithSeq(seq, preJobId, okFail, andOr);
                
                List<PreJobCondition> preJobCondList = preJobCondByJob.get(jobId);
                if (preJobCondList == null) {
                    preJobCondList = new ArrayList<PreJobCondition>();
                    preJobCondByJob.put(jobId, preJobCondList);
                }

                preJobCondList.add(prejobcond);
            }
        }
        
        // 메모리 로드 완료후에 SEQ 로 정렬
        for (List<PreJobCondition> preJobCondList : preJobCondByJob.values()) {
            Collections.sort(preJobCondList, new Comparator<PreJobCondition>() {
                public int compare(PreJobCondition o1, PreJobCondition o2) {
                    return ((PreJobConditionWithSeq)o1).seqNo - ((PreJobConditionWithSeq)o2).seqNo;
                }
            });
        }
        
        // 정렬 후에 다시 PreJobCondition 객체로 변경 (호환 문제)
        for (List<PreJobCondition> preJobCondList : preJobCondByJob.values()) {
            int len = preJobCondList.size();
            for (int i=0; i<len; i++) {
                preJobCondList.set(i, ((PreJobConditionWithSeq) preJobCondList.get(i)).toPreJobCondition());
            }
        }
    }

    /**
     * PostJobTrigger sheet 를 먼저 메모리에 Map 으로 올려둔다.
     */
    private void loadPostJobTrigger() {
        Map<String, Short> colIndexMap = null; // <COL NAME, COL INDEX>
        
        for (Row row : postTriggerSheet) {
            if (colIndexMap == null) { // 아직 header 를 못찾은 경우  
                colIndexMap = checkAndReturnColumnIndexMap(postJobTriggerColNameSet, row);
            }else {
                String jobId         = getCellValue(row.getCell(colIndexMap.get("JOB_ID")));
                if (Util.isBlank(jobId)) continue;
                int    seq           = Util.toInt(getCellValue(row.getCell(colIndexMap.get("SEQ"))), 1);
                String when          = getCellValue(row.getCell(colIndexMap.get("WHEN")));
                String checkValue1   = getCellValue(row.getCell(colIndexMap.get("CHECK_VALUE1")));
                String checkValue2   = getCellValue(row.getCell(colIndexMap.get("CHECK_VALUE2")));
                String checkValue3   = getCellValue(row.getCell(colIndexMap.get("CHECK_VALUE3")));
                String triggerJobId  = getCellValue(row.getCell(colIndexMap.get("TRIGGER_JOB_ID")));
                int    instanceCount = Util.toInt(getCellValue(row.getCell(colIndexMap.get("INSTANCE_COUNT"))), 1);
                
                PostJobTrigger trigger = new PostJobTriggerWithSeq(seq, when, checkValue1, checkValue2, checkValue3, triggerJobId, instanceCount);
                
                List<PostJobTrigger> postJobTriggerList = postTriggerByJob.get(jobId);
                if (postJobTriggerList == null) {
                    postJobTriggerList = new ArrayList<PostJobTrigger>();
                    postTriggerByJob.put(jobId, postJobTriggerList);
                }

                postJobTriggerList.add(trigger);
            }
        }
        
        // 메모리 로드 완료후에 SEQ 로 정렬
        for (List<PostJobTrigger> postJobTriggerList : postTriggerByJob.values()) {
            Collections.sort(postJobTriggerList, new Comparator<PostJobTrigger>() {
                public int compare(PostJobTrigger o1, PostJobTrigger o2) {
                    return ((PostJobTriggerWithSeq)o1).seqNo - ((PostJobTriggerWithSeq)o2).seqNo;
                }
            });
        }
        
        // 정렬 후에 다시 PreJobCondition 객체로 변경 (호환 문제)
        for (List<PostJobTrigger> postJobTriggerList : postTriggerByJob.values()) {
            int len = postJobTriggerList.size();
            for (int i=0; i<len; i++) {
                postJobTriggerList.set(i, ((PostJobTriggerWithSeq) postJobTriggerList.get(i)).toPostJobTrigger());
            }
        }
    }        

    /**
     * Parameteres sheet 를 먼저 메모리에 Map 으로 올려둔다.
     */
    private void loadParameters() {
        Map<String, Short> colIndexMap = null; // <COL NAME, COL INDEX>
        
        // SEQ 값으로 순서 정렬을 위해 List<ParameterWithSeq> 에 담은 후에 마지막에 sort 한 후 Map으로 변환한다. 
        Map<String, List<ParameterWithSeq>> paramMapTmp = new HashMap(); // <JOBID, List<ParameterWithSeq>>
        
        for (Row row : paramSheet) {
            if (colIndexMap == null) { // 아직 header 를 못찾은 경우  
                colIndexMap = checkAndReturnColumnIndexMap(paramColNameSet, row);
            }else {
                String jobId      = getCellValue(row.getCell(colIndexMap.get("JOB_ID")));
                if (Util.isBlank(jobId)) continue;
                int    seq        = Util.toInt(getCellValue(row.getCell(colIndexMap.get("SEQ"))), 1);
                String paramName  = getCellValue(row.getCell(colIndexMap.get("PARAM_NAME")));
                String paramValue = getCellValue(row.getCell(colIndexMap.get("PARAM_VALUE")));
                
                ParameterWithSeq param = new ParameterWithSeq(seq, paramName, paramValue);
                
                List<ParameterWithSeq> paramList = paramMapTmp.get(jobId);
                if (paramList == null) {
                    paramList = new ArrayList<ParameterWithSeq>();
                    paramMapTmp.put(jobId, paramList);
                }

                paramList.add(param);
            }
        }
        
        // 메모리 로드 완료후에 SEQ 로 정렬
        for (List<ParameterWithSeq> paramList : paramMapTmp.values()) {
            Collections.sort(paramList, new Comparator<ParameterWithSeq>() {
                public int compare(ParameterWithSeq o1, ParameterWithSeq o2) {
                    return o1.seqNo - o2.seqNo;
                }
            });
        }
        
        // sort 완료후엔 List<ParameterWithSeq> => LinkedHashMap 변환
        for (Map.Entry<String, List<ParameterWithSeq>> paramEntry : paramMapTmp.entrySet()) {
            String jobId = paramEntry.getKey();
            Map<String, String> paramMap = new LinkedHashMap<String, String>();
            for (ParameterWithSeq param : paramEntry.getValue()) {
                paramMap.put(param.name, param.value);
            }
            parametersByJob.put(jobId, paramMap);
        }
    }

    /**
     * main sheet 를 파싱하여 JobDefinition 객체를 만든다.
     */
    private void loadMain() throws IOException {
        Map<String, Short> colIndexMap = null; // <COL NAME, COL INDEX>
        File tempDirectory = prepareTempDirectory();
        
        Set<String> jobidList = new LinkedHashSet<String>();
        
        for (Row row : mainSheet) {
            if (colIndexMap == null) { // 아직 header 를 못찾은 경우  
                colIndexMap = checkAndReturnColumnIndexMap(mainColNameSet, row);
            }else {
                String jobId = getCellValue(row.getCell(colIndexMap.get("JOB_ID")));
                if (Util.isBlank(jobId)) continue;
                
                if (!jobidList.add(jobId)) { // dup check
                    throw new RuntimeException("Duplicated Job ID ["+jobId+"]");
                }
                
                JobDefinition jobdef = new JobDefinition();
                jobdef.setJobId(                    jobId);
                jobdef.setJobGroupId(               getCellValue(row.getCell(colIndexMap.get("JOB_GROUP_ID"))));
                jobdef.setOwner(                    getCellValue(row.getCell(colIndexMap.get("OWNER"))));
                jobdef.setDescription(              getCellValue(row.getCell(colIndexMap.get("JOB_DESC"))));
                jobdef.setTimeFrom(                 getCellValue(row.getCell(colIndexMap.get("TIME_FROM"))));
                jobdef.setTimeUntil(                getCellValue(row.getCell(colIndexMap.get("TIME_UNTIL"))));
                jobdef.setRepeatYN(                 getCellValue(row.getCell(colIndexMap.get("REPEAT_YN"))));
                jobdef.setRepeatIntval(             Util.toInt(getCellValue(row.getCell(colIndexMap.get("REPEAT_INTVAL")))));
                jobdef.setRepeatIntvalGb(           getCellValue(row.getCell(colIndexMap.get("REPEAT_INTVAL_GB"))));
                jobdef.setRepeatIfError(            getCellValue(row.getCell(colIndexMap.get("REPEAT_IF_ERROR"))));
                jobdef.setRepeatMaxOk(              Util.toInt(getCellValue(row.getCell(colIndexMap.get("REPEAT_MAX_OK")))));
                jobdef.setRepeatExactExp(           getCellValue(row.getCell(colIndexMap.get("REPEAT_EXACT_EXP"))));
                jobdef.setConfirmNeedYN(            getCellValue(row.getCell(colIndexMap.get("CONFIRM_NEED_YN"))));
                jobdef.setParallelGroup(            getCellValue(row.getCell(colIndexMap.get("PARALLEL_GROUP"))));
                jobdef.setJobType(                  getCellValue(row.getCell(colIndexMap.get("JOB_TYPE"))));
                
                String agentNode1                 = getCellValue(row.getCell(colIndexMap.get("AGENT_NODE")));
                if (colIndexMap.containsKey("AGENT_NODE2")) {
                    String agentNode2             = getCellValue(row.getCell(colIndexMap.get("AGENT_NODE2")));
                    if (Util.isBlank(agentNode2)) {
                        jobdef.setAgentNode(        agentNode1);
                    }else {
                        jobdef.setAgentNode(        agentNode1+"/"+agentNode2);
                    }
                }else {
                    jobdef.setAgentNode(            agentNode1);
                }

                jobdef.setComponentName(            getCellValue(row.getCell(colIndexMap.get("COMPONENT_NAME"))));
                jobdef.setScheduleType(             getCellValue(row.getCell(colIndexMap.get("SCHEDULE_TYPE"))));
                jobdef.setDaysInMonth(              getCellValue(row.getCell(colIndexMap.get("DAYS_IN_MONTH"))));
                jobdef.setMonths(                   getCellValue(row.getCell(colIndexMap.get("MONTHS"))));
                jobdef.setDaysOfWeek(               getCellValue(row.getCell(colIndexMap.get("DAYS_OF_WEEK"))));
                jobdef.setWeekdayMonthdayType(      getCellValue(row.getCell(colIndexMap.get("WEEKDAY_MONTHDAY_TYPE"))));
                String calIdTmp                   = getCellValue(row.getCell(colIndexMap.get("CALENDAR_ID")));
                jobdef.setCalendarExps(             getCellValue(row.getCell(colIndexMap.get("CALENDAR_EXP"))));
                jobdef.setDayOfMonthScheduleType(   getCellValue(row.getCell(colIndexMap.get("DAY_SCHEDULE_TYPE"))));
                jobdef.setBeforeAfterExp(           getCellValue(row.getCell(colIndexMap.get("BEFORE_AFTER_EXP"))));
                jobdef.setShiftExp(                 getCellValue(row.getCell(colIndexMap.get("SHIFT_EXP"))));
                jobdef.setShiftExp2(                getCellValue(row.getCell(colIndexMap.get("SHIFT_EXP2"))));
                jobdef.setFixedDays(                getCellValue(row.getCell(colIndexMap.get("FIXED_DAYS"))));
                jobdef.setExtraSchedule(            getCellValue(row.getCell(colIndexMap.get("EXTRA_SCHEDULE"))));
//                jobdef.setBaseDateCalId(            getCellValue(row.getCell(colIndexMap.get("BASE_DATE_CAL_ID"))));
                String baseDateCalId              = getCellValue(row.getCell(colIndexMap.get("BASE_DATE_CAL_ID")));
                jobdef.setBaseDateLogic(            getCellValue(row.getCell(colIndexMap.get("BASE_DATE_LOGIC"))));
                jobdef.setLogLevel(                 getCellValue(row.getCell(colIndexMap.get("LOG_LEVEL"))));

                // Calendar ID 를 다시 정리
                if (!Util.isBlank(calIdTmp) && calIdTmp.indexOf("[") > -1) {
                    jobdef.setCalendarId(calIdTmp.substring(calIdTmp.indexOf("[") + 1, calIdTmp.indexOf("]")).trim());
                }
                if (!Util.isBlank(baseDateCalId) && baseDateCalId.indexOf("[") > -1) {
                    jobdef.setBaseDateCalId(baseDateCalId.substring(baseDateCalId.indexOf("[") + 1, baseDateCalId.indexOf("]")).trim());
                }
                
                // 선행 Job, 파라미터 정보를 꺼내 JobDefinition 에 set 하고 난 후에는 기존 Map 에서 삭제한다. 불필요한 메모리 낭비를 막기 위해.
                if (parametersByJob.get(jobId) != null) {
                    jobdef.setInParameters(parametersByJob.remove(jobId));
                }
                
                if (preJobCondByJob.get(jobId) != null) {
                    jobdef.setPreJobConditions(preJobCondByJob.remove(jobId));
                }

                if (postTriggerByJob.get(jobId) != null) {
                	jobdef.setTriggerList(postTriggerByJob.remove(jobId));
                }
                
                /*
                 * 파싱에러가 한건도 없는 경우에만 upload 가 가능하도록 하기 위해 일단 전체를 파싱해야한다.
                 * 전체 파싱 결과를 계속 메모리에 담아두고 있으면 메모리 효율이 떨어지므로, 파싱완료된 JobDefinition 은 파일로 저장한후
                 * 나중에 다시 read 하여 사용한다.
                 */

                File jobdefFile = writeJobDefinitionToTmpFile(tempDirectory, jobdef);
                jobdefFileList.add(jobdefFile);
            }
        }
    }
    
    /**
     * 엑셀 파일을 파싱하여 JobDefinition 파일로 변환한다.
     * getJobDefFileList() 메소드로 변환된 JobDefinition 정보를 얻는다.
     * @param xslFile
     */
    public void parseExcel(File xlsFile) {
        InputStream fin = null;
        try {
            fin = new BufferedInputStream(new FileInputStream(xlsFile));
            parseExcel(fin);
        }catch(Exception e) {
            throw Util.toRuntimeException(e);
        }finally {
            try { fin.close(); }catch (Exception ignore) {}
        }
    }
    
    public void parseExcel(InputStream xlsIn) {
        try {
            Workbook wb = new HSSFWorkbook(xlsIn);
            mainSheet   = wb.getSheet("JobDefinition-main");
            if (mainSheet==null) {
                throw new Exception("JobDefinition-main sheet not found");
            }
            prejobSheet = wb.getSheet("PreJobCondition");
            if (prejobSheet==null) {
                throw new Exception("PreJobCondition sheet not found");
            }
            postTriggerSheet = wb.getSheet("PostJobTrigger");
            if (postTriggerSheet==null) {
            	throw new Exception("PostJobTrigger sheet not found");
            }
            paramSheet  = wb.getSheet("Parameters");
            if (paramSheet==null) {
                throw new Exception("Parameters sheet not found");
            }
            
            loadPreJobCondition();
            loadPostJobTrigger();
            loadParameters();
            loadMain();
        }catch(Exception e) {
            throw Util.toRuntimeException(e);
        }
    }

    /**
     * seq 번호로 순서 유지를 위해 기존의 PreJobCondition 클래스를 확장하여 seq를 담고 간다.
     * seq 값으로 sort 한다.
     */
    class PreJobConditionWithSeq extends PreJobCondition {
        public static final long serialVersionUID = -4996359257206859824L;
        
        int seqNo;

        public PreJobConditionWithSeq(int seq, String preJobId, String okFail, String andOr) {
            super(preJobId, okFail, andOr);
            this.seqNo = seq;
        }
        
        public PreJobCondition toPreJobCondition() {
            return new PreJobCondition(preJobId, okFail, andOr);
        }
    }

    /**
     * seq 번호로 순서 유지를 위해 기존의 PreJobCondition 클래스를 확장하여 seq를 담고 간다.
     * seq 값으로 sort 한다.
     */
    class PostJobTriggerWithSeq extends PostJobTrigger {
		private static final long serialVersionUID = -1417336219994718450L;
		int seqNo;

        public PostJobTriggerWithSeq(int seq, String when, String checkValue1, String checkValue2, String checkValue3, String jobId, int jobInstanceCount) {
        	super(when, checkValue1, checkValue2, checkValue3, jobId, jobInstanceCount);
            this.seqNo = seq;
        }
        
        public PostJobTrigger toPostJobTrigger() {
            return new PostJobTrigger(when, checkValue1, checkValue2, checkValue3, triggerJobId, jobInstanceCount);
        }
    }

    /**
     * seq 번호로 순서 유지를 위해 일단 ParameterWithSeq 클래스로 담았다가 
     * sort 후에 LinkedHashMap 에 넣는다.
     */
    class ParameterWithSeq {
        int seqNo;
        String name;
        String value;

        public ParameterWithSeq(int seq, String name, String value) {
            this.seqNo = seq;
            this.name  = name;
            this.value = value;
        }
    }
    
    public static void main(String[] args) {
        System.setProperty("NEXCORE_HOME", "c:/temp");
        ExcelToJobDefinition xls = new ExcelToJobDefinition();
        xls.parseExcel(new File("D:/Download/jobdef-2012-01-26_14-33-22.xls"));
        List<File> file = xls.getJobdefFileList();
        System.out.println(file);
    }
}

