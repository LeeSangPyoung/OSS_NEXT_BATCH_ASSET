package nexcore.scheduler.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import nexcore.scheduler.util.DateUtil;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치코어</li>
 * <li>설  명 : Batch 프로그램에서 사용할 Context 정보</li>
 * <li>작성일 : 2010.03.30</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobInstance implements Serializable, Comparable {
	private static final long serialVersionUID = 7065794652751611136L;
	
	public static final String JOB_STATE_INIT          = "I";
	public static final String JOB_STATE_WAIT          = "W";
	public static final String JOB_STATE_ENDED_OK      = "O";
	public static final String JOB_STATE_ENDED_FAIL    = "F";
	public static final String JOB_STATE_RUNNING       = "R";
	public static final String JOB_STATE_SUSPENDED     = "P"; // Pending.
	public static final String JOB_STATE_SLEEP_RPT     = "S"; // Sleep for Repeat.
	public static final String JOB_STATE_EXPIRED       = "X"; // activation_date 가 expire.
	public static final String JOB_STATE_GHOST         = "G"; // running 중에 agent 가 down 되어 Job 이 비정상 exit 된경우
	
	public static final String REPEAT_IF_ERROR_STOP    = "STOP";
	public static final String REPEAT_IF_ERROR_IGNORE  = "IGNORE";

	public static final String REPEAT_GB_START         = "START";
	public static final String REPEAT_GB_END           = "END";
	public static final String REPEAT_GB_EXACT         = "EXACT";

	public static final String Y                       = "Y";
	public static final String N                       = "N";
	
	private String       procDate;
	private String       baseDate;
	private String       jobId;
	private String       jobGroupId;
	private String       jobInstanceId;
	private String       desc;
	private String       jobState;         /* ['I':init, 'W':wait, 'O':ok, 'F':fail, 'R':running, 'S':suspended, 'L':Sleep for repeat ]   */
	private String       jobStateReason;   /* 현재 상태 사유. 어떤 조건에 의해 WAIT 하고 있는지를 기술           */
	private String       lastJobExeId;     /* 최종 수행 JobExecutionId                                           */
	private String       lockedBy;
	private String       timeFrom;
	private String       timeUntil;
	private String       repeatYN;         /* 반복작업 여부.                  ['N', 'Y':TRUE]                    */
	private int          repeatIntval;     /* 반복작업 INTERVAL. 초단위 입력.                                    */
	private String       repeatIntvalGb;   /* 반복작업 INTERVAL 구분. ['START':시작시각기준, 'END':종료시각기준] */
	private String       repeatIfError;    /* 반복작업시 에러발생.            ['STOP':정지, 'IGNORE':무시]       */
	private int          repeatMaxOk;      /* 반복작업시 최대 정상 종료 건수. 이횟수를 초과하지 않는다.          */
	private String       repeatExactExp;   /* 반복작업시 EXACT 방식일때의 HHMMSS 의 표현식. 정규표현식           */

	private String       confirmNeedYN;    /* 승인 필요여부.                  ['N', 'Y':TRUE]                    */
	private String       confirmed;        /* 승인 여부.                      [시각+승인자ID+승인자IP]           */
	private String       parallelGroup;    /* 동시 실행 제한용. 그룹명                                           */
	private String       jobType;          /* ['EJB', 'POJO', 'PROC', 'DUMMY', 'SLEEP', 'FILEWATCH' ]            */
	private String       agentNode;        /* agent 의 NEXCORE_ID                                                 */
	private String       lastAgentNode;    /* 에이전트 이중화 환경에서 현재 또는 마지막으로 수행한 에이전트 ID   */
	private String       componentName;    /* Ejb: classname, Pojo:classname, proc:command                       */

//  2016.7.26 string 이었던 trigger 를 PostJobTrigger 로 구조화함. 정호철
//	private String       triggerJobIds;    /* 후행으로 activate될 JOB ID List                                    */
//	private List<String> triggerJobIdList;
	private List<PostJobTrigger> triggerList  = new ArrayList<PostJobTrigger>();
	
	private String       activationTime;   /* instance 화 된 시각. YYYYMMDDHHMMSS                                */
	private String       activator;        /* instance 화한 주체                                                 */
	private int          runCount;         /* 실행 횟수.                                                         */
	private int          endOkCount;       /* 실행 후 정상종료 횟수.                                             */
	private String       logLevel;         /* 기본 로그 레벨. null:default, FATAL,ERROR,WARN,INFO,DEBUG,TRACE          */
	private String       lastStartTime;    /* last start 시각. YYYYMMDDHHMMSS                                    */
	private String       lastEndTime;      /* last end   시각. YYYYMMDDHHMMSS                                    */
//	private Timestamp    lastModifyTime;   /* 최종변경시각 */
	/*
	 * Timestamp 타입으로 할 경우 scheduler와 agent가 사용하는 DB 종류가 다를 경우 ClassNotFoundException이 발생할 수 있다. 
	 * Timestamp는 각 DBMS 별 다른 클래스를 사용하기 때문에 -- 2011-12-07 --
	 * 애초에 varchar 타입으로 하는게 좋았을 것 같다.
	 */
	private long         lastModifyTime;    


	private Map                   inParameters     = new LinkedHashMap<String, String>();
	private List<PreJobCondition> preJobConditions = new ArrayList<PreJobCondition>(); // <PreJobCondition> 선행 Job. NBS_JOB_INST_PREJOB 에 등록된 조건이 List로 담김.
	
	public String getProcDate() {
		return procDate;
	}
	public void setProcDate(String procDate) {
		this.procDate = procDate;
	}
	public String getBaseDate() {
		return baseDate;
	}
	public void setBaseDate(String baseDate) {
		this.baseDate = baseDate;
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
	public String getJobInstanceId() {
		return jobInstanceId;
	}
	public void setJobInstanceId(String jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}
	public String getDescription() {
		return desc;
	}
	public void setDescription(String jobDesc) {
		this.desc = jobDesc;
	}
	public String getJobState() {
		return jobState;
	}
	public static String getJobStateText(String jobState) {
		if (jobState.equals(JOB_STATE_INIT)) {
			return "Init";
		}else if (jobState.equals(JOB_STATE_WAIT)) {
			return "Wait";
		}else if (jobState.equals(JOB_STATE_ENDED_OK)) {
			return "End OK";
		}else if (jobState.equals(JOB_STATE_ENDED_FAIL)) {
			return "End Fail";
		}else if (jobState.equals(JOB_STATE_RUNNING)) {
			return "Running";
		}else if (jobState.equals(JOB_STATE_SUSPENDED)) {
			return "Suspended";
		}else if (jobState.equals(JOB_STATE_SLEEP_RPT)) {
			return "Repeat Sleep";
		}else if (jobState.equals(JOB_STATE_EXPIRED)) {
			return "Expired";
		}else if (jobState.equals(JOB_STATE_GHOST)) {
			return "Ghost";
		}else {
			return "N/A";
		}
	}
	public String getJobStateText() {
		return getJobStateText(jobState);
	}
	public void setJobState(String jobState) {
		this.jobState = jobState;
	}
	public String getJobStateReason() {
		return jobStateReason;
	}
	public void setJobStateReason(String jobStateReason) {
		this.jobStateReason = jobStateReason;
	}
	
	/**
	 * 최종 수행 JobExecutionID를 리턴함
	 * @return 미실행상태 이면 null 또는 "" 리턴,
	 * "R" 상태에서 Execution insert 전이라면 "-" 리턴,
	 * 그외 정상 상태에서는 JobExeId 리턴
	 */
	public String getLastJobExeId() {
		return lastJobExeId;
	}
	public void setLastJobExeId(String lastJobExeId) {
		this.lastJobExeId = lastJobExeId;
	}
	public String getLockedBy() {
		return lockedBy;
	}
	public boolean isLocked() {
		return lockedBy!=null && lockedBy.length()>0;
	}
	public void setLockedBy(String lockedby) {
		this.lockedBy = lockedby;
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
	public String getConfirmed() {
		return confirmed;
	}
	public void setConfirmed(String confirmed) {
		this.confirmed = confirmed;
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
	public void setAgentNode(String agentNode) {
		this.agentNode = agentNode;
	}
	public String getLastAgentNode() {
		return lastAgentNode;
	}
	public void setLastAgentNode(String lastAgentNode) {
		this.lastAgentNode = lastAgentNode;
	}
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
//	
//	public String getTriggerJobIds() {
//		return triggerJobIds;
//	}
//	public void setTriggerJobIds(String triggerJobIds) {
//		this.triggerJobIds = triggerJobIds;
//		this.triggerJobIdList = Util.toList(triggerJobIds);
//	}
//	public List<String> getTriggerJobIdList() {
//		return triggerJobIdList;
//	}
	
	public List<PostJobTrigger> getTriggerList() {
		return triggerList;
	}
	public void setTriggerList(List<PostJobTrigger> triggerList) {
		this.triggerList = triggerList;
	}
	public String getActivationTime() {
		return activationTime;
	}
	public String getActivationDate() {
		return activationTime!=null ? activationTime.substring(0, 8) : null;
	}
	public void setActivationTime(String activationTime) {
		this.activationTime = activationTime;
	}
	public String getActivator() {
		return activator;
	}
	public void setActivator(String activator) {
		this.activator = activator;
	}
	public int getRunCount() {
		return runCount;
	}
	public void setRunCount(int runCount) {
		this.runCount = runCount;
	}
	public int getEndOkCount() {
		return endOkCount;
	}
	public void setEndOkCount(int endOkCount) {
		this.endOkCount = endOkCount;
	}
	public String getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
	public String getLastStartTime() {
		return lastStartTime;
	}
	public void setLastStartTime(String lastStartTime) {
		this.lastStartTime = lastStartTime;
	}
	public String getLastEndTime() {
		return lastEndTime;
	}
	public void setLastEndTime(String lastEndTime) {
		this.lastEndTime = lastEndTime;
	}
	public String getLastModifyTime() {
		return lastModifyTime==0 ? null : DateUtil.getTimestampString(lastModifyTime);
	}
	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : DateUtil.getTimestampLong(lastModifyTime);
	}
	public Map<String, String> getInParameters() {
		return inParameters = (inParameters==null ? new LinkedHashMap() : inParameters) ; // null 이면 empty 를 리턴한다.
	}
	public void setInParameters(Map paramMap) {
		(this.inParameters = getInParameters()).putAll(paramMap);
	}
	public List<PreJobCondition> getPreJobConditions() {
		return preJobConditions;
	}
	public void setPreJobConditions(List<PreJobCondition> preJobConditions) {
		this.preJobConditions = preJobConditions;
	}
	public int compareTo(Object o) {
		return this.jobInstanceId.compareTo(((JobInstance)o).getJobInstanceId());
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}

