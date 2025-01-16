package nexcore.scheduler.agent;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.agent.runner.IBatchContextAdapter;
import nexcore.scheduler.entity.JobExecution;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : nexcore-scheduler-4</li>
 * <li>서브 업무명 : nexcore.scheduler.agent</li>
 * <li>설  명 : JobExecution 은 스케줄러가 생성해서 보내주는 객체이며, 에이전트 에서는 Job실행에 필요한 context 정보들을 JobContext 에 담아 관리함.
 *              3.* 버전의 IBatchContext 를 대체함. BatchContext 는 스케줄러,에이전트에서는 사용되지 않고 배치프레임워크 영역에서만 사용되도록 분리함.</li>
 * <li>작성일 : 2016. 1. 13.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobContext implements Serializable {
    private static final long serialVersionUID = -566852024075063942L;

    private Map          	        attributeMap = new ConcurrentHashMap(); // 이 정보가 CenterCut 에서는 모니터링 정보로 사용된다.
	private JobExecution            jobExecution;
	
	/**
	 * 실행할 배치 프로그램 객체. 클래스로딩하여 객체로 갖고 있는다.
	 */
	private Object                  batchObject;
	
	/**
	 * 배치 프레임워크에서는 JobContext 가 아닌 BatchContext 를 사용한다. 
	 * 여러 버전 BatchContext 클래스를 담을 수 있도록 하기 위해 Object 타입으로 하고 reflection 으로 처리한다.
	 */
	private Object                  batchContext; 
	
	private IJobRunnerCallBack      jobRunnerCallBack;
	private IBatchContextAdapter    batchContextAdapter;
	
	private boolean                 stopForced;       // 강제 종료 시도됐는지 여부. 상태로 관리하지 않고 flag로 관리함.
	private boolean                 suspendForced;    // 일시 정지 시도됐는지 여부. 상태로 관리하지 않고 flag로 관리함.

	private ILogger                 log;  // Job Logger

    public JobContext() {
    }
    
	public Object getBatchObject() {
		return batchObject;
	}

	public void setBatchObject(Object batchObject) {
		this.batchObject = batchObject;
	}

	public Object getBatchContext() {
        return batchContext;
    }

    public void setBatchContext(Object batchContext) {
        this.batchContext = batchContext;
    }

    public Object getAttribute(String key) {
		return attributeMap.get(key);
	}

	public String getInParameter(String name) {
		return (String)jobExecution.getInParameters().get(name);
	}

	public Map<String, String> getInParameters() {
		return jobExecution.getInParameters();
	}
	
	public JobExecution getJobExecution() {
		return jobExecution;
	}
	
	public String getOperatorId() {
	    return jobExecution.getOperatorId();
	}
	
	public String getOperatorIp() {
	    return jobExecution.getOperatorIp();
	}
	
	public String getOperatorType() {
	    return jobExecution.getOperatorType();
	}
	
	public void setAttribute(String key, Object value) {
		this.attributeMap.put(key, value);
	}

	public void setJobExecution(JobExecution je) {
		this.jobExecution = je;
	}

	public int getReturnCode() {
		return jobExecution.getReturnCode();
	}
	
	public void setReturnCode(int returnCode) {
		jobExecution.setReturnCode(returnCode);
	}
	
	public String getReturnValue(String key) {
		return jobExecution.getReturnValue(key);
	}

	public Properties getReturnValues() {
		return jobExecution.getReturnValues();
	}

	public void setReturnValue(String key, String value) {
		jobExecution.setReturnValue(key, value);
	}

	public void setReturnValues(Properties _returnValues) {
		jobExecution.setReturnValues(_returnValues);
	}

	public long getProgressTotal() {
		return jobExecution.getProgressTotal();
	}

	public void setProgressTotal(long progressTotal) {
		jobExecution.setProgressTotal(progressTotal);
	}

	public long getProgressCurrent() {
		return jobExecution.getProgressCurrent();
	}

	public void setProgressCurrent(long progressCurrent) {
		jobExecution.setProgressCurrent(progressCurrent);
	}

	public long[] getProgressValues() {
	    return jobExecution.getProgressValues();
	}
	
	public ILogger getLogger() {
		return log;
	}

	public void setLog(ILogger log) {
		this.log = log;
	}

	public IJobRunnerCallBack getJobRunnerCallBack() {
		return jobRunnerCallBack;
	}

	public void setJobRunnerCallBack(IJobRunnerCallBack jobRunnerCallBack) {
		this.jobRunnerCallBack = jobRunnerCallBack;
	}

	public IBatchContextAdapter getBatchContextAdapter() {
		return batchContextAdapter;
	}

	public void setBatchContextAdapter(IBatchContextAdapter batchContextAdapter) {
		this.batchContextAdapter = batchContextAdapter;
	}

	public boolean isStopForced() {
		return stopForced;
	}
	
	public void setStopForced(boolean stopForced) {
		this.stopForced = stopForced;
	}
	
	public boolean isSuspendForced() {
		return suspendForced;
	}
	
	public void setSuspendForced(boolean suspendForced) {
		this.suspendForced = suspendForced;
	}
}
