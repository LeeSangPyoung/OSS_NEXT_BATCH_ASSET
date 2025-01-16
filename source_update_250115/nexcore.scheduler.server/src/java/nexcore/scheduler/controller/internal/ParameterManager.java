package nexcore.scheduler.controller.internal;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.controller.internal.param.FunctionBoMDate;
import nexcore.scheduler.controller.internal.param.FunctionDateAdd;
import nexcore.scheduler.controller.internal.param.FunctionEoMDate;
import nexcore.scheduler.controller.internal.param.FunctionGetProperty;
import nexcore.scheduler.controller.internal.param.FunctionGetenv;
import nexcore.scheduler.controller.internal.param.FunctionIndexOf;
import nexcore.scheduler.controller.internal.param.FunctionLastIndexOf;
import nexcore.scheduler.controller.internal.param.FunctionLength;
import nexcore.scheduler.controller.internal.param.FunctionMonthAdd;
import nexcore.scheduler.controller.internal.param.FunctionNumAdd;
import nexcore.scheduler.controller.internal.param.FunctionNumDiv;
import nexcore.scheduler.controller.internal.param.FunctionNumMultiply;
import nexcore.scheduler.controller.internal.param.FunctionSpace;
import nexcore.scheduler.controller.internal.param.FunctionSubstring;
import nexcore.scheduler.controller.internal.param.FunctionToLower;
import nexcore.scheduler.controller.internal.param.FunctionToUpper;
import nexcore.scheduler.controller.internal.param.FunctionYearAdd;
import nexcore.scheduler.controller.internal.param.ParameterContext;
import nexcore.scheduler.controller.internal.param.ParameterFunction;
import nexcore.scheduler.core.IScheduleCalendar;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.Util;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job start시 등록된 파라미터를 상수 값으로 해석한 후 exec 한다. </li>
 * <li>작성일 : 2010. 6. 23.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ParameterManager {
	private SqlMapClient            sqlMapClient;
	private IScheduleCalendar       scheduleCalendar;
	
	// --------
	private Map<String, String>             globalParameters;
	private Map<String, ParameterFunction>  functions;

	/* 
	 * -- 내장 파라미터 --
	 * 
	 * | TIME           | HHMMSS           | 현재시각
	 * | DATE           | YYYYMMDD         | 현재날짜
	 * | DATETIME       | YYYYMMDDHHMMSS   | 현재날짜시각
	 * | PROC_DATE      | YYYYMMDD         | 처리일 (ACTIVATION 시점의 DATE)
	 * | BASE_DATE      | YYYYMMDD         | 기준일
	 * | RUN_COUNT      | 숫자형           | 실행횟수
	 * | NEXCORE_HOME   | YYYYMMDD         | 에이전트 홈디렉토리
	 * | AGENT_NODE     | 문자형           | 에이전트 ID. SYSTEM_ID 와 다르게 스케줄러에 등록된 ID
	 * | SYSTEM_ID      | 문자형           | 에이전트 시스템ID. NEXCORE_ID 또는 system.id
	 * | NEXCORE_ID     | 문자형           | 에이전트 시스템ID. NEXCORE_ID 또는 system.id
	 * | SCHEDULER_ID   | 문자형           | 스케줄러 시스템ID. system.id
	 * | JOB_ID         | 문자형           | Job Definition Id
	 * | JOB_DESC       | 문자형           | Job 설명
	 * | JOB_INS_ID     | 문자형           | Job Instance Id
	 * | JOB_EXE_ID     | 문자형           | Job Execution Id
	 * | JOBGROUP_ID    | 문자형           | Job Group Id
	 * | JOBGROUP_NAME  | 문자형           | Job Group Name
	 * | LOG_LEVEL      | 문자형           | 로그레벨 (INFO, DEBUG, ...)
	 * | OPER_ID        | 문자형           | 조작자 ID
	 * | OPER_IP        | 문자형           | 조작자 IP
	 * | OPER_TYPE      | 문자형           | SCH:스케줄러, USR:사용자, OND:온디멘드
	 * | COMPONENT_NAME | 문자형           | 배치 클래스 또는 쉘명
	 * 
	 */
	
	public ParameterManager() {
		globalParameters = new LinkedHashMap();
		functions        = new HashMap();
		// 아래 작업을 init 에서 하면 로컬 개발환경에서 제대로 안돌아간다. (기본함수)
		loadFunction(new FunctionBoMDate());
		loadFunction(new FunctionDateAdd());
		loadFunction(new FunctionEoMDate());
		loadFunction(new FunctionIndexOf());
		loadFunction(new FunctionLastIndexOf());
		loadFunction(new FunctionLength());
		loadFunction(new FunctionMonthAdd());
		loadFunction(new FunctionNumAdd());
		loadFunction(new FunctionNumDiv());
		loadFunction(new FunctionNumMultiply());
		loadFunction(new FunctionSpace());
		loadFunction(new FunctionSubstring());
		loadFunction(new FunctionYearAdd());
		loadFunction(new FunctionGetenv());
		loadFunction(new FunctionGetProperty());
		loadFunction(new FunctionToLower());
		loadFunction(new FunctionToUpper());
	}
	
	public void init() {
		try {
			reloadGlobalParameters();
			Util.logServerInitConsole("ParameterManager");
		}catch(Exception e) {
			throw new SchedulerException("main.global.param.load.error", e);
		}
	}
	
	public void destroy() {
	}
	
	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public IScheduleCalendar getScheduleCalendar() {
		return scheduleCalendar;
	}

	public void setScheduleCalendar(IScheduleCalendar scheduleCalendar) {
		this.scheduleCalendar = scheduleCalendar;
	}
	
	// 기본 함수 이외에 프로젝트에서 추가할 수 있는 함수 반영
	public void setAdditionalFunctions(List<ParameterFunction> addtionalFunctions) {
		for (ParameterFunction function : addtionalFunctions) {
			loadFunction(function);
		}
	}

	private void loadFunction(ParameterFunction func) {
		functions.put(func.getName(), func);
	}
	
	/**
	 * DB 에서 글로벌 파라미터를 로드한다.
	 */
	public void reloadGlobalParameters() throws SQLException {
		Map<String, String> prop = new LinkedHashMap<String, String>();
		List<Map<String, String>> paramList = sqlMapClient.queryForList("nbs.scheduler.selectGlobalParam");
		
		for (Map<String, String> row : paramList) {
			String paramName  = row.get("PARAM_NAME");
			String paramValue = row.get("PARAM_VALUE");
			prop.put(paramName, paramValue);
		}
		globalParameters = prop;
	}

	/**
	 * 글로벌 파라미터를 변경한다. DB 변경, 메모리 변경. (Batch Admin 에서 변경수행)
	 * @param name
	 * @param value
	 */
	public int updateParameter(String name, String value) throws SQLException {
		globalParameters.put(name, value);
		Map data = new HashMap();
		data.put("paramName",  name);
		data.put("paramValue", value);
		
		return sqlMapClient.update("nbs.scheduler.updateGlobalParam", data);
	}
	
	/**
	 * 글로벌 파라미터를 삭제한다.
	 * @param name
	 * @param value
	 */
	public int deleteParameter(String name) throws SQLException {
		globalParameters.remove(name);
		Map data = new HashMap();
		data.put("paramName",  name);
		
		return sqlMapClient.delete("nbs.scheduler.deleteGlobalParam", data);
	}

	/**
	 * 글로벌 파라미터를 추가한다.
	 * @param name
	 * @param value
	 */
	public int insertParameter(String name, String value) throws SQLException {
		globalParameters.put(name, value);
		Map data = new HashMap();
		data.put("paramName",  name);
		data.put("paramValue", value);
		
		return sqlMapClient.delete("nbs.scheduler.insertGlobalParam", data);
	}

	/**
	 * 글로벌 파라미터를 읽는다.추가한다.
	 * @param name
	 * @param value
	 */
	public Map getGlobalParameters() {
		return globalParameters;
	}


	//*******************************************************************************************
	
	/**
	 * 파라미터를 설정함. 내장 파라미터, 글로벌 파라미터, Job 파라미터 순서로 설정함
	 * @param jobins
	 * @param jobexe
	 */
	private void setupInternalParameter(JobExecution jobexe, Properties agentSystemProperties) {
		String today  = Util.getCurrentYYYYMMDDHHMMSS();
		
		Map<String, String>  jp = jobexe.getInParameters();
		jp.put("TIME",          today.substring(8));
		jp.put("DATE",          today.substring(0,8));
		jp.put("DATETIME",      today);
		jp.put("PROC_DATE",     jobexe.getProcDate());
		jp.put("BASE_DATE",     jobexe.getBaseDate());
		jp.put("RUN_COUNT",     String.valueOf(jobexe.getRunCount()));
		jp.put("NEXCORE_HOME",  agentSystemProperties.getProperty("NEXCORE_HOME"));
		jp.put("AGENT_NODE",    jobexe.getAgentNode());
		jp.put("SYSTEM_ID",     Util.nvlBlank(agentSystemProperties.getProperty("NEXCORE_ID"), agentSystemProperties.getProperty("system.id")));
		jp.put("NEXCORE_ID",    Util.nvlBlank(agentSystemProperties.getProperty("NEXCORE_ID"), agentSystemProperties.getProperty("system.id"))); // NJF 7.0 에서는 NEXCORE_ID 로 함
		jp.put("SCHEDULER_ID",  Util.getSystemId());
		jp.put("JOB_ID",        jobexe.getJobId());
		jp.put("JOB_INS_ID",    jobexe.getJobInstanceId());
		jp.put("JOB_EXE_ID",    jobexe.getJobExecutionId());
		
		/*
		 * 2012-12-26 추가. Job 인스턴스의 로그 레벨을 내장 파라미터 (LOG_LEVEL) 로 전달한다.
		 * 만일 온디멘드 배치 실행시 이미 파라미터에 LOG_LEVEL 이 설정되어왔다면 그 값을 우선으로 한다.
		 * 온디멘드에 로그 레벨이 설정되어 넘어왔다면 그 값으로 JobExecution 의 로그 레벨을 변경한다 
		 */
		if (jp.containsKey("LOG_LEVEL")) { // 온디멘드인 호출에서 파라미터로 로그레벨이 이미 설정된 경우
			String logLevel = jp.get("LOG_LEVEL");
			jobexe.setLogLevel(logLevel);
		}else {
			jp.put("LOG_LEVEL", Util.nvl(jobexe.getLogLevel(), ""));
		}
		
		/*
		 * 2012-12-26 추가. Operator ID, Operator IP 를 내장 파라미터로 전달한다.
		 */
		jp.put("OPER_ID", jobexe.getOperatorId());
		jp.put("OPER_IP", jobexe.getOperatorIp());
		
		/*
		 * 2013-01-22 추가. 
		 */
		jp.put("JOB_DESC",      Util.nvl(jobexe.getDescription(),   ""));
		jp.put("JOBGROUP_ID",   Util.nvl(jobexe.getJobGroupId(),    ""));
		jp.put("COMPONENT_NAME",Util.nvl(jobexe.getComponentName(), ""));
		jp.put("OPER_TYPE",     Util.nvl(jobexe.getOperatorType(),  ""));

//		jp.put("JOBGROUP_NAME", Util.nvl(jobexe.getJobGroupName(),  ""));  // TODO  구현해야함

		/*
		 * 2016-02-29 추가
		 */
		jp.put("JOB_TYPE",      Util.nvl(jobexe.getJobType(),       ""));

		/*
		 * C 프레임워크 같은 경우 NEXCORE_HOME 을 C 프레임워크 디렉토리로 잡는 바람에 에이전트 홈디렉토리와 혼동이 될 수 있다. 
		 * 그외 프로세스 기반의 Job 들이 혹시라도 NEXCORE_HOME을 사용할 수 있으므로 
		 * 겹치는 것을 방지하기 위해 AGENT_HOME을 따로 둔다.
		 * 2012-02-06 추가
		 */
		jp.put("AGENT_HOME",    agentSystemProperties.getProperty("NEXCORE_HOME"));   
	}

	/**
	 * 설정된 파라미터 식을 해석하여 상수값으로 변환.
	 * @param jobins
	 * @param jobexe
	 * @param paramValue
	 * @return
	 */
	private String interpretParamValue(Map<String, String> jobParam, String paramName, String paramValue, ParameterContext paramContext) {
		/*
		 * 치환 값은 ${..} 형태로 함.  ${DATE}${TIME} ==> "20100624200400" 을 의미함
		 * 함수는 ${!..} 형태로함.     ${!SUBSTRING ${DATE} 0 4} ==> "2010" 을 의미함
		 * 참조는 REF 함수를 이용함    
		 *     ${!REF JXYZ0001.O.RESULT1} ==> 
		 *           JobId 가 JXYZ0001 인 Job 의 Output 값 중에서 RESULT1 이라는 키에 해당하는 값을 참조함
		 *     ${!REF JXYZ0001.I.MYPARAM} ==> 
		 *           JobId 가 JXYZ0001 인 Job 의 Input 파라미터 중에서 MYPARAM 이라는 키에 해당하는 값을 참조함
		 */

		if (paramValue == null) {
			return null;
		}
		
		int len = paramValue.length();
		int startIdx = -1;
		for (int i=0; i<len; i++) {
			if (paramValue.startsWith("${", i)) {
				startIdx = i;
			}else if (startIdx > -1 && paramValue.startsWith("}", i)) {
				String expToken = paramValue.substring(startIdx+2, i).trim();
				String expValue = expToken.startsWith("!") ?
						evaluateParamFunctions(jobParam, expToken, paramContext) : // 함수 처리
						jobParam.get(expToken); // 변수 대체

				// 무한 recursion 을 방지하기 위해, expValue 안에 paramName이 존재하는 지 체크함 
				if (paramValue != null && paramValue.indexOf("${"+paramName+"}") > -1) {
					throw new SchedulerException("main.param.endless.recursion.detected", paramName);
				}
				return interpretParamValue(jobParam, paramName, 
						paramValue.substring(0, startIdx) + expValue + (i==len-1 ? "" : paramValue.substring(i+1)), 
						paramContext);
			}
		}
		return paramValue;
	}

	/**
	 * 함수 해석
	 * @param jobParam
	 * @param expression
	 * @return
	 */
	private String evaluateParamFunctions(Map jobParam, String expression, ParameterContext paramContext) {
		// ${!SUBSTRING ABCDEFG 0 4}  => ABCD
		int idx1 = expression.indexOf(' ');
		String funcName   = idx1 == -1 ? expression.substring(1) : expression.substring(1, idx1).trim();
		String operandstr = idx1 == -1 ? "" : expression.substring(idx1+1).trim();
		String[] operands = operandstr.length()==0 ? new String[0] : operandstr.split(" +");
		
		ParameterFunction func = functions.get(funcName);
		if (func == null) {
			throw new SchedulerException("main.param.unknown.function", expression);
		}
		return func.evaluate(operands, paramContext);
	}
	
	/**
	 * JobExecution 실행 직전에 파라미터를 상수 값으로 해석해줌.
	 * Job Instance 에 설정된 파라미터를 해석해서 Job Execution 의 파라미터로 set 함. 
	 * @param je
	 */
	public void setupJobParameter(JobExecution jobexe) {
		setupJobParameter(jobexe, new Properties(), Collections.emptyMap());	
	}

	/**
	 * JobExecution 실행 직전에 파라미터를 상수 값으로 해석해줌.
	 * Job Instance 에 설정된 파라미터를 해석해서 Job Execution 의 파라미터로 set 함. 
	 * @param je
	 * @param agentSystemProperties agent node 의 JVM 파라미터 값들
	 * @param agentSystemEnv agenot node 의 환경변수 값들
	 */
	public void setupJobParameter(JobExecution jobexe, Properties agentSystemProperties, Map agentSystemEnv) {
		// 0. 개별 파리미터만 세팅되어서 여기로 들어옴.
		Map<String, String> exeParam     = jobexe.getInParameters();

		// 아래에서 overwrite 용으로 사용됨.
		Map<String, String> jobParamOnly = new LinkedHashMap(exeParam);

		// 파라미터 해석을 위한 context 
		ParameterContext paramContext = new ParameterContext();
		paramContext.setAgentSystemProperties(agentSystemProperties);
		paramContext.setAgentSystemEnv       (agentSystemEnv);
		paramContext.setParameters           (exeParam);
		
		// 1. 내장 파라미터 설정
		setupInternalParameter(jobexe, agentSystemProperties);
		
		// 2. 글로벌 파라미터 설정
		exeParam.putAll(globalParameters);
		
		// 3. 개별 Job 파라미터로 다시 overwrite. 동일이름의 파라미터가 존재할 경우는 '개별>전역>내장' 순으로 우선순위가 있음
		exeParam.putAll(jobParamOnly);
		
		// 4. 파라미터 변수, 함수식 해석
		int interpretCount;
		do {
			interpretCount = 0;
			for (Map.Entry<String, String> entry : exeParam.entrySet()) {
				String paramName  = entry.getKey();
				String paramValue = entry.getValue() == null ? null : String.valueOf(entry.getValue()); // 형변환 에러 방지
				try {
					String interpretedValue = interpretParamValue(exeParam, paramName, paramValue, paramContext);
					if (paramValue != null && !paramValue.equals(interpretedValue)) {
						interpretCount++; // 변수/함수 해석 건수를 리턴함. 이 값이 0이 될때까지 계속 해석함
					}
					exeParam.put(paramName, interpretedValue);
				}catch(Exception e) {
					throw new SchedulerException("main.param.interpret.error", e, paramValue);
				}
			}
		}while(interpretCount != 0);
	}
	
	public static void main(String[] args) {
		ParameterManager pm = new ParameterManager();
		Map jp = new LinkedHashMap();
		jp.put("DATE", Util.getCurrentYYYYMMDD());
		jp.put("TIME", "183005");
		jp.put("AA", "SOLOMON");
		
		String s;
		s = "${DATE}${TIME} . $${AA}00";
		System.out.println("표현:"+s);
		System.out.println("결과:"+pm.interpretParamValue(jp, "P1", s, new ParameterContext()));
		System.out.println();
		
		s = "${!DATEADD ${DATE} ${!LENGTH 10}}";
		System.out.println("표현:"+s);
		System.out.println("결과:"+pm.interpretParamValue(jp, "P1", s, new ParameterContext()));
		System.out.println();

		s = "-${AA}|${!SUBSTRING ${DATE} 0  4}";
		System.out.println("표현:"+s);
		System.out.println("결과:"+pm.interpretParamValue(jp, "P1", s, new ParameterContext()));
		System.out.println();
		
		s = "/app/date/${DATE}${!SPACE ${!LENGTH MMM}}.log";
		System.out.println("표현:"+s);
		System.out.println("결과:"+pm.interpretParamValue(jp, "P1", s, new ParameterContext()));
		System.out.println();

		s = "${DATE}.${!EOMDATE ${DATE}}";
		System.out.println("표현:"+s);
		System.out.println("결과:"+pm.interpretParamValue(jp, "P1", s, new ParameterContext()));
		System.out.println();
		
		s = "${!EOMDATE ${!MONTHADD ${DATE} -1}}";
		System.out.println("표현:"+s);
		System.out.println("결과:"+pm.interpretParamValue(jp, "P1", s, new ParameterContext()));
		System.out.println();
		
		s = "S.${TIME}.${!SUBSTRING ${TIME} ${!NUMADD ${!LENGTH ${TIME}} -2} ${!LENGTH ${TIME}}}";
		System.out.println("표현:"+s);
		System.out.println("결과:"+pm.interpretParamValue(jp, "P1", s, new ParameterContext()));
		System.out.println();
		
		jp.put("JOB_ID", "VAT21001");
		s = "${!GETRETVAL VAT200 TARGET_RANGE_${!SUBSTRING ${JOB_ID} 6 8}}";
		System.out.println("표현:"+s);
		System.out.println("결과:"+pm.interpretParamValue(jp, "P1", s, new ParameterContext()));
		System.out.println();
		
		Map p = new LinkedHashMap();
		p.put("BANK_CODE", "010");
		p.put("BCV", "0");
		p.put("MYVAR2", "${DATA_ROOT}---${MYVAR1}");
		p.put("MYVAR1", "S.${TIME}.${!SUBSTRING ${TIME} ${!NUMADD ${!LENGTH ${TIME}} -2} ${!LENGTH ${TIME}}}");

		p.put("DATE", Util.getCurrentYYYYMMDD());
		p.put("TIME", "183005");

		p.put("APP_CODE",   "${!LOWER ${!SUBSTRING ${JOB_ID} 1 4}}"); 
		p.put("DATA_ROOT",  "/dat/${APP_CODE}");
		p.put("NAS_ROOT",   "/nas/${APP_CODE}"); 
		p.put("STAGE_CODE", "D");

		JobExecution exe = new JobExecution();
		exe.setInParameters(p);
		exe.setJobId("BXYZ001001");
		
		System.out.println("B-"+p);
		pm.setupJobParameter(exe, System.getProperties(), System.getenv());
		System.out.println("E-"+exe.getInParameters());
	}
	

}
