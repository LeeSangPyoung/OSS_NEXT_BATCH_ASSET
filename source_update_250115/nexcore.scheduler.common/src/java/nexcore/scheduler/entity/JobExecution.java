package nexcore.scheduler.entity;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

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
public class JobExecution implements Serializable {
	private static final long serialVersionUID = -1173800910653959607L;
	
	public static final int STATE_INIT              = 1;  // 초기화됨
	public static final int STATE_RUNNING           = 2;  // 실행중
//	public static final int STATE_BEFORE_SUSPENDED  = 3;  // 일시정지 대기중 (일시정지 명령직후)
	public static final int STATE_SUSPENDED         = 4;  // 일시정지됨
//	public static final int STATE_AFTER_SUSPENDED   = 5;  // 재실행 대기중 (재실행 명령직후)
//	public static final int STATE_BEFORE_ENDED      = 6;  // 종료 대기상태
	public static final int STATE_ENDED             = 7;  // 종료됨
	public static final int STATE_UNKNOWN           = 99; // 알수없는 상태

	private String    		jobId;
	private String    		jobInstanceId;
	private String     		jobExecutionId;
	
	private String    		jobType;          // EJB, POJO, PROCESS
	private String    		agentNode;        // job 실행되는 node 명;
	
	private String    		componentName;    // Ejb: classname, Pojo:classname, process:command 

	private String    		procDate;         // 처리일. YYYYMMDD 
	private String    		baseDate;         // 기준일. YYYYMMDD
	
	private int       		runCount;         // 실행회수
	private String          procId;           // 해당 JobExecution 이 실행되는 JVM 의 PID. singlejvm 에서만 유효함. v3.7 이후 사용되며, NBS_RUN_LOG 테이블에 기록됨.
	private String          hostname;         // 배치 실행되는 서버의 hostname
	private String          osUser;           // 배치 실행되는 JVM 의 username
	
	private long      		startTime;        // Job start time
	private long      		endTime;          // Job end time
	
	private int       		state;
	private int       		returnCode = -1;  // 초기값은 MIN 값으로함.

	private String          errorMsg;         // 에러발생시 error msg.
	
	private Map             inParameters = new LinkedHashMap<String, String>();  // LinkedHashMap<String, String>
	private Properties      returnValues = new Properties();
	
	// 진행률 정보를 담는 객체. JobContext 와 transparent 하도록 하기 위해 primitive 에서 object 타입으로 변경한다.
    private long[]          progressValues = new long[2]; // [0]:progressTotal. [1]:progressCurrent

	private String          operatorId;
	private String          operatorIp;
	private String          operatorType;     // 조작자 타입. OND | SCH | USR . EXT_SCH 는 4.0 부터는 사용하지 않는다. 
	
	private byte[]          optionalData;     // 온디맨드 호출의 경우 IOnlineContext가 byte[] 로 변환되어 여기 담긴다.
	private boolean         isOnDemand;       // 스케줄러를 호출하는 온디맨드 배치인지? 
	
	private String          logLevel;         // scheduler 에서 order 할때 설정하는 값.

	/*
	 * Timestamp 타입으로 할 경우 scheduler와 agent가 사용하는 DB 종류가 다를 경우 ClassNotFoundException이 발생할 수 있다. 
	 * Timestamp는 각 DBMS 별 다른 클래스를 사용하기 때문에 -- 2011-12-07 --
	 * 애초에 varchar 타입으로 하는게 좋았을 것 같다.
	 */
	private long            lastModifyTime;    
	
	private String          description;      // NBS_JOB_EXE 테이블에는 저장되지 않지만, AGENT 단에 로그를 위해 invoke 당시에만 set 되는 값.
	private String          jobGroupId;       // NBS_JOB_EXE 테이블에는 저장되지 않지만, 내장 파라미터 세팅을 위해 invoke 당시에만 set 되는 값.
	
	public JobExecution() {
	}

    public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobInstanceId() {
		return jobInstanceId;
	}

	public void setJobInstanceId(String jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}

	public String getJobExecutionId() {
		return jobExecutionId;
	}

	public void setJobExecutionId(String jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
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

	public void setAgentNode(String agent) {
		this.agentNode = agent;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

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

	public int getRunCount() {
		return runCount;
	}

	public void setRunCount(int runCount) {
		this.runCount = runCount;
	}

	public String getProcId() {
		return procId;
	}

	public void setProcId(String procId) {
		this.procId = procId;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getOsUser() {
		return osUser;
	}

	public void setOsUser(String osUser) {
		this.osUser = osUser;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	/*
	public Timestamp getStartTimeTS() {
		return startTime==0 ? null : DateUtil.getTimestamp(startTime);
	}

	public void setStartTimeTS(Timestamp startTime) {
		this.startTime = startTime == null ? 0l : startTime.getTime();
	}
	 */
	
	public String getStartTimeTS() {
		return startTime==0 ? null : DateUtil.getTimestampString(startTime);
	}

	public void setStartTimeTS(String startTime) {
		this.startTime = startTime == null ? 0l : DateUtil.getTimestampLong(startTime);
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	/*
	public Timestamp getEndTimeTS() {
		return endTime == 0 ? null : DateUtil.getTimestamp( endTime );
	}

	public void setEndTimeTS(Timestamp endTime) {
		this.endTime = endTime == null ? 0l : endTime.getTime();
	}
	*/
	public String getEndTimeTS() {
		return endTime == 0 ? null : DateUtil.getTimestampString( endTime );
	}

	public void setEndTimeTS(String endTime) {
		this.endTime = endTime == null ? 0l : DateUtil.getTimestampLong(endTime);
	}
	
	public long getRunTime() {
		return endTime - startTime;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public int getState() {
		return state;
	}
	
	public String getStateString() {
		return JobExecutionStateHelper.getStateString(state);
	}

	/**
	 * 상태 변경. <br>
	 * Job실행쓰레드, JobStateCallbackSender 쓰레드에 의해 동시적으로 호출될 수 있으므로 synchronized 로 동기화 처리한다. 
	 * @param state
	 */
	public synchronized void setState(int state) {
		this.state = state;
	}

	/**
	 * 상태 변경. <br>
	 * Job실행쓰레드, JobStateCallbackSender 쓰레드에 의해 동시적으로 호출될 수 있으므로 synchronized 로 동기화 처리한다. <br>4
	 * 이전상태와 비교하여 일치하는 경우만 새 상태로 update 한다.  
	 * @param beforeState 이전 상태 
	 * @param newState 새 상태
	 */
	public synchronized boolean setStateWithCheck(int beforeState, int newState) {
		if (this.state == beforeState) {
			this.state = newState;
			return true;
		}else {
			return false;
		}
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public Map<String, String> getInParameters() {
		return inParameters = (inParameters==null ? new LinkedHashMap() : inParameters) ; // null 이면 empty 를 리턴한다.
	}

	public void setInParameters(Map _inParameter) {
		(this.inParameters = getInParameters()).putAll(_inParameter);
	}

	public String getReturnValue(String key) {
		return returnValues.getProperty(key);
	}

	public Properties getReturnValues() {
		return returnValues;
	}

	public void setReturnValue(String key, String value) {
		returnValues.setProperty(key, value);
	}

	public void setReturnValues(Properties _returnValues) {
		returnValues = _returnValues;
	}

	private void initProgressValues() {
		progressValues = new long[2];
	}
	
    public long getProgressTotal() {
    	if (progressValues == null) initProgressValues();
        return progressValues == null ? 0 : progressValues[0];
    }

    public void setProgressTotal(long progressTotal) {
    	if (progressValues == null) initProgressValues();
        progressValues[0] = progressTotal;
    }

    public long getProgressCurrent() {
    	if (progressValues == null) initProgressValues();
        return progressValues[1];
    }

    public void setProgressCurrent(long progressCurrent) {
    	if (progressValues == null) initProgressValues();
        progressValues[1] = progressCurrent;
    }
    
    public long[] getProgressValues() {
    	if (progressValues == null) initProgressValues();
        return progressValues; // long[] 객체를 JobExecution, JobContext, BatchContext 에서 공유재사용 하기 위함이므로 ref 를 그대로 리턴해야한다.
    }
    
	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getOperatorType() {
		return operatorType;
	}

	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;  
	}
	
	public String getOperatorIp() {
		return operatorIp;
	}

	public void setOperatorIp(String operatorIp) {
		this.operatorIp = operatorIp;
	}
	
	public byte[] getOptionalData() {
		return optionalData;
	}

	public void setOptionalData(byte[] optionalData) {
		this.optionalData = optionalData;
	}

	public boolean isOnDemand() {
		return isOnDemand;
	}

	public void setOnDemand(boolean isOnDemand) {
		this.isOnDemand = isOnDemand;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
	/*
	public Timestamp getLastModifyTime() {
		return lastModifyTime==0 ? null : DateUtil.getTimestamp(lastModifyTime);
	}

	public void setLastModifyTime(Timestamp lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : lastModifyTime.getTime();
	}
	*/
	public String getLastModifyTime() {
		return lastModifyTime==0 ? null : DateUtil.getTimestampString(lastModifyTime);
	}

	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : DateUtil.getTimestampLong(lastModifyTime);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getJobGroupId() {
		return jobGroupId;
	}

	public void setJobGroupId(String jobGroupId) {
		this.jobGroupId = jobGroupId;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}

