package nexcore.scheduler.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Repository 에 등록되는 Job 등록 정보. 이 정보를 바탕으로 Job Instance 가 생성됨 </li>
 * <li>작성일 : 2010. 5. 11.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobDefinition implements Serializable {
	private static final long serialVersionUID = 3977687607556878890L;
	
	// 데몬 Job 의 에러시 수행 지시 상수
	public static final String REPEAT_IF_ERROR_STOP       = "STOP";
	public static final String REPEAT_IF_ERROR_IGNORE     = "IGNORE";
	
	public static final String Y                          = "Y";
	public static final String N                          = "N";
	
	// 스케줄 방식
	public static final String SCHEDULE_TYPE_FIXED        = "FIXED";
	public static final String SCHEDULE_TYPE_EXPRESSION   = "EXPRESSION";

	// 매일, 매월시 ALL 지정
	public static final String ALL                        = "ALL";

	// 월의 말일을 지정
	public static final String LAST_DAY_Of_MONTH          = "LD";

	// 일자 스케줄 방식
	public static final String DAY_SCHEDULE_TYPE_CALENDAR = "CALENDAR";
	public static final String DAY_SCHEDULE_TYPE_NUMBER   = "NUMBER";

	// 일, 요일 연산
	public static final String WEEKDAY_MONTHDAY_TYPE_AND  = "AND";
	public static final String WEEKDAY_MONTHDAY_TYPE_OR   = "OR";

	/* --------------- Job Definition 기본 정보 ------------- */
	private String       jobId;         
	private String       jobGroupId;  
	
	private String       owner;            /* 담당자. 사번 또는 이름                                             */
	private String       description;      /* 설명.                                                              */
	
	private String       timeFrom;
	private String       timeUntil;
	private String       repeatYN;         /* 반복작업 여부.                  ['N', 'Y':TRUE]                    */
	private int          repeatIntval;     /* 반복작업 INTERVAL. 초단위 입력.                                    */
	private String       repeatIntvalGb;   /* 반복작업 INTERVAL 구분. ['START':시작시각기준, 'END':종료시각기준] */
	private String       repeatIfError;    /* 반복작업시 에러발생.            ['STOP':정지, 'IGNORE':무시]       */
	private int          repeatMaxOk;      /* 반복작업시 최대 정상 종료 회수. 이 횟수를 초과하여 실행되지 않는다.*/
	private String       repeatExactExp;   /* 반복작업시 EXACT 방식일때의 HHMMSS 의 표현식. 정규표현식           */
	
	private String       confirmNeedYN;    /* 승인 필요여부.                  ['N', 'Y':TRUE]                    */
	private String       parallelGroup;    /* 동시 실행 제한용. 그룹명                                           */
	private String       jobType;          /* ['EJB', 'POJO', 'PROC', 'DUMMY', 'SLEEP', 'FILEWATCH' ]            */
	private String       agentNode;        /* agent 의 NEXCORE_ID                                                 */
	private String       componentName;    /* Ejb: classname, Pojo:classname, proc:command                       */
	
//  2016.7.26 string 이었던 trigger 를 PostJobTrigger 로 구조화함. 정호철
//	private String       triggerJobIds;    /* 후행으로 activate될 JOB ID List                                    */
//	private List<String> triggerJobIdList;
	private List<PostJobTrigger> triggerList  = new ArrayList<PostJobTrigger>();

	/* --------------- Job Schedule 정보 ------------- */

	private String       scheduleType;           /*                          ['FIXED', 'EXPRESSION']                          */
	private String       daysInMonth;            /* (EXP..) 일 스케줄.       ['ALL' | '1/2/3/LD/'...  ]                       */ 
	private List<String> dayListInMonth; 
	private String       months;                 /* (EXP..) 월 스케줄.       ['ALL' | '1/4/7/10/ ...  ]                       */
	private List<String> monthList;
	private String       daysOfWeek;             /* (EXP..) 요일 스케줄. ['W2D7/W2D6/...' | '2D5/..'] W2D2 2째주월요일, 2D2 2번째월요일  */
	private List<String> dayListOfWeek;
	private String       weekdayMonthdayType;    /* (EXP..) 일자와 요일 연산 ['OR', 'AND' ]                                   */
	private String       calendarId;             /* (EXP..) CALENDAR ID. ['0', '1', '2, ... ]                                 */
	private String       calendarExps;           /* (EXP..) Calendar 상세식. ["B1/B2", "E1/E2", "B1/E1", ... ]                */
	private List<String> calendarExpList;
	private String       dayOfMonthScheduleType; /* (EXP..) 숫자지정방식, 달력 지정방식 ['NUMBER', 'CALENDAR']                */
	private String       beforeAfterExp;         /* (공통) 익전일 처리 처리 지정 ['A1':익1일, 'B1':전1일]                     */
	private String       shiftExp;               /* (공통) 1차대체일 ['3,1,-1'] : 휴일(3) 일경우 영업일(1) 기준으로 하루전일(-1) */
	private String       shiftExp2;              /* (공통) 2차대체일 ['3,1,-1'] : 휴일(3) 일경우 영업일(1) 기준으로 하루전일(-1) */ /* 1차 대체일 후에 2차 대체일도 적용 */
	private String       fixedDays;              /* (FIXED) SCHEDULE_TYPE='FIXED' 일 경우. ['20100101/20100509/...']          */
	private List<String> fixedDayList;
	private String       extraSchedule;          /* 스케줄 요구사항 추가시 이 속성 값의 표현식을 이용함                       */ 
	private boolean      isReverse;              /* true 일 경우 위에서 설정한 스케줄에만 안돌아가도록 함. "REVERSE=Y"        */ 
	
	
	/* --------------- 기준일 처리 정보 ------------- */
	
	private String       baseDateCalId;          /* 기준일 처리를 위한 calendar 지정                                         */
	private String       baseDateLogic;          /* 기준일 처리를 위한 calendar 연산일 지정                                  */
	
	/* ---------------------------------------------- */

	private String       logLevel;               /* 기본 로그 레벨. null:default, FATAL,ERROR,WARN,INFO,DEBUG,TRACE          */
	private String       createTime;             /* Job Def 생성일자 시각. YYYYMMDDHHMMSS                                    */
//	private Timestamp    lastModifyTime;         /* 최종변경시각 */
	/*
	 * Timestamp 타입으로 할 경우 scheduler와 agent가 사용하는 DB 종류가 다를 경우 ClassNotFoundException이 발생할 수 있다. 
	 * Timestamp는 각 DBMS 별 다른 클래스를 사용하기 때문에 -- 2011-12-07 --
	 * 애초에 varchar 타입으로 하는게 좋았을 것 같다.
	 */
	private long         lastModifyTime;    

	
	private Map<String, String>   inParameters     = new LinkedHashMap<String, String>();
	private List<PreJobCondition> preJobConditions = new ArrayList<PreJobCondition>(); // <PreJobCondition> 선행 Job. NBS_JOB_DEF_PREJOB 에 등록된 조건이 List로 담김.
	
	public JobDefinition() {
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getJobGroupId() {
		return jobGroupId;
	}
	public void setJobGroupId(String jobGroupId) {
		this.jobGroupId = jobGroupId;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTimeFrom() {
		return timeFrom;
	}
	public void setTimeFrom(String timeFrom) {
		this.timeFrom = timeFrom;
	}
	public String getTimeUntil() {
		return timeUntil;
	}
	public void setTimeUntil(String timeUntil) {
		this.timeUntil = timeUntil;
	}
	public String getRepeatYN() {
		return repeatYN;
	}
	public void setRepeatYN(String repeatYN) {
		this.repeatYN = repeatYN;
	}
	public int getRepeatIntval() {
		return repeatIntval;
	}
	public void setRepeatIntval(int repeatIntval) {
		this.repeatIntval = repeatIntval;
	}
	public String getRepeatIntvalGb() {
		return repeatIntvalGb;
	}
	public void setRepeatIntvalGb(String repeatIntvalGb) {
		this.repeatIntvalGb = repeatIntvalGb;
	}
	public String getRepeatIfError() {
		return repeatIfError;
	}
	public void setRepeatIfError(String repeatIfError) {
		this.repeatIfError = repeatIfError;
	}
	public int getRepeatMaxOk() {
		return repeatMaxOk;
	}
	public void setRepeatMaxOk(int repeatMaxOk) {
		this.repeatMaxOk = repeatMaxOk;
	}
	public String getRepeatExactExp() {
		return repeatExactExp;
	}
	public void setRepeatExactExp(String repeatExactExp) {
		this.repeatExactExp = repeatExactExp;
	}
	public String getConfirmNeedYN() {
		return confirmNeedYN;
	}
	public void setConfirmNeedYN(String confirmNeedYN) {
		this.confirmNeedYN = confirmNeedYN;
	}
	public String getParallelGroup() {
		return parallelGroup;
	}
	public void setParallelGroup(String parallelGroup) {
		this.parallelGroup = parallelGroup;
	}
	public String getJobType() {
		return jobType;
	}
	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	public String getAgentNode() {
		return agentNode;
	}
	public void setAgentNode(String agentNode) {
		this.agentNode = agentNode;
	}
	
	/**
     * 에이전트 설정이 이중화 되어있을 경우 master node 값을 리턴한다.
     * @return master agent node id
     */
    public String getAgentNodeMaster() {
        if (agentNode != null) {
            if (agentNode.contains("/")) {
                return agentNode.substring(0, agentNode.indexOf("/"));
            }else {
                return agentNode;
            }
        }else {
            return null;
        }
    }
    
    /**
     * 에이전트 설정이 이중화 되어있을 경우 slave node 값을 리턴한다.
     * @return slave agent node id
     */
    public String getAgentNodeSlave() {
        if (agentNode != null) {
            if (agentNode.contains("/")) {
                return agentNode.substring(agentNode.indexOf("/")+1);
            }else {
                return null; // 
            }
        }else {
            return null;
        }
    }

	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
//	public String getTriggerJobIds() {
//		return triggerJobIds;
//	}
//	public void setTriggerJobIds(String triggerJobId) {
//		this.triggerJobIds = triggerJobId;
//		this.triggerJobIdList = Util.toList(triggerJobId);
//	}
//	public List<String> getTriggerJobIdList() {
//		return triggerJobIdList;
//	}

	public String getScheduleType() {
		return scheduleType;
	}
	public List<PostJobTrigger> getTriggerList() {
		return triggerList;
	}
	public void setTriggerList(List<PostJobTrigger> triggerList) {
		this.triggerList = triggerList;
	}
	public void setScheduleType(String scheduleType) {
		this.scheduleType = scheduleType;
	}
	public String getDaysInMonth() {
		return daysInMonth;
	}
	public void setDaysInMonth(String daysInMonth) {
		this.daysInMonth = daysInMonth;
	}
	public List getDayListInMonth() {
		// string 표현을 파싱하여 List 로 변환
		this.dayListInMonth= Util.toList(daysInMonth);
		for (int i=0; i<dayListInMonth.size(); i++) {
			if (this.dayListInMonth.get(i).startsWith("0")) { // 0 으로 시작하는 0이 아닌 숫자는 0을 떼어낸다.
				try {
					this.dayListInMonth.set(i, String.valueOf(Integer.parseInt(this.dayListInMonth.get(i))));
				}catch(Exception e) {}
			}
		}
		
		return dayListInMonth;
	}
	public String getMonths() {
		return months;
	}
	public void setMonths(String months) {
		this.months = months;
	}
	/*
	public static void main(String[] args) {
		JobDefinition jobdef = new JobDefinition ();
		jobdef.setMonths("1/2/03/4/5/01");
		System.out.println(jobdef.getMonths());
		
	}*/
	public List getMonthList() {
		// string 표현을 파싱하여 List 로 변환
		this.monthList = Util.toList(months);
		for (int i=0; i<monthList.size(); i++) {
			if (this.monthList.get(i).startsWith("0")) { // 0 으로 시작하는 0이 아닌 숫자는 0을 떼어낸다.
				try {
					this.monthList.set(i, String.valueOf(Integer.parseInt(this.monthList.get(i))));
				}catch(Exception e) {}
			}
		}

		return monthList;
	}
	public String getDaysOfWeek() {
		return daysOfWeek;
	}
	public void setDaysOfWeek(String daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
		// string 표현을 파싱하여 List 로 변환
		this.dayListOfWeek = Util.toList(daysOfWeek);
	}
	public List getDayListOfWeek() {
		return dayListOfWeek;
	}
	public String getWeekdayMonthdayType() {
		return weekdayMonthdayType;
	}
	public void setWeekdayMonthdayType(String weekdayMonthdayType) {
		this.weekdayMonthdayType = weekdayMonthdayType;
	}
	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}
	public String getCalendarExps() {
		return calendarExps;
	}
	public void setCalendarExps(String calendarExps) {
		this.calendarExps = calendarExps;
		// string 표현을 파싱하여 List 로 변환
		this.calendarExpList = Util.toList(calendarExps);
	}
	public List getCalendarExpList() {
		return calendarExpList;
	}
	public String getDayOfMonthScheduleType() {
		return dayOfMonthScheduleType;
	}
	public void setDayOfMonthScheduleType(String dayOfMonthScheduleType) {
		this.dayOfMonthScheduleType = dayOfMonthScheduleType;
	}
	public String getBeforeAfterExp() {
		return beforeAfterExp;
	}
	public void setBeforeAfterExp(String beforeAfterExp) {
		this.beforeAfterExp = beforeAfterExp;
	}
	public String getShiftExp() {
		return shiftExp;
	}
	public void setShiftExp(String shiftExp) {
		this.shiftExp = shiftExp;
	}
	public String getShiftExp2() {
		return shiftExp2;
	}
	public void setShiftExp2(String shiftExp2) {
		this.shiftExp2 = shiftExp2;
	}
	public String getFixedDays() {
		return fixedDays;
	}
	public void setFixedDays(String fixedDays) {
		this.fixedDays = fixedDays;
		// string 표현을 파싱하여 List 로 변환
		this.fixedDayList = Util.toList(fixedDays);
	}
	
	public String getExtraSchedule() {
		return extraSchedule;
	}
	public void setExtraSchedule(String extraSchedule) {
		this.extraSchedule = extraSchedule;
		
		/* 부가 스케줄정보 Parse & Set */
		if (!Util.isBlank(this.extraSchedule)) {
			String[] extras = extraSchedule.split(",");
			for (String extra : extras) {
				if (extra.startsWith("REVERSE")) {  /*    REVERSE 여부 설정  */
					String[] reverseExp = extra.split("=");
					isReverse = reverseExp.length == 2 && "Y".equals(reverseExp[1]);
				}
			}
		}
	}
	public boolean isReverse() {
		return isReverse;
	}
	public void setReverse(boolean isReverse) {
		this.isReverse = isReverse;
		
		if (this.isReverse) {
			if (this.extraSchedule == null) {
				this.extraSchedule = "REVERSE=Y"; // 현재는 extra scheduler 정보가 이거 하나밖에 
			}else {
				this.extraSchedule += ",REVERSE=Y";
			}
		}
	}
	public String getBaseDateCalId() {
		return baseDateCalId;
	}
	public void setBaseDateCalId(String baseDateCalId) {
		this.baseDateCalId = baseDateCalId;
	}
	public String getBaseDateLogic() {
		return baseDateLogic;
	}
	public void setBaseDateLogic(String baseDateLogic) {
		if (!Util.isBlank(baseDateLogic)) {
			baseDateLogic = baseDateLogic.replaceAll("\\+", "");
		}
			
		this.baseDateLogic = baseDateLogic;
	}
	public List getFixedDayList() {
		return fixedDayList;
	}
	public String getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createDateTime) {
		this.createTime = createDateTime;
	}
	public String getLastModifyTime() {
		return lastModifyTime==0 ? null : DateUtil.getTimestampString(lastModifyTime);
	}
	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = (lastModifyTime==null || lastModifyTime.equals("")) ? 0 : DateUtil.getTimestampLong(lastModifyTime);
	}
	public Map<String, String> getInParameters() {
		return inParameters = (inParameters==null ? new LinkedHashMap() : inParameters) ; // null 이면 empty 를 리턴한다.
	}
	public void setInParameters(Map _inParameters) {
		(this.inParameters = getInParameters()).putAll(_inParameters);
	}
	public List<PreJobCondition> getPreJobConditions() {
		return preJobConditions;
	}
	public void setPreJobConditions(List<PreJobCondition> preJobConditions) {
		this.preJobConditions = preJobConditions;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}

