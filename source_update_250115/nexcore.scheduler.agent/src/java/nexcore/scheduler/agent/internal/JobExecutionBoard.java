package nexcore.scheduler.agent.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.runner.BatchContextAdapterForPojo;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobType;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : JobExecution 정보를 관리함. <br>
 *              실행 중인 Execution 들에 대해, suspend, resume, stop 을 위한 용도임. <br>
 *              정상 또는 에러 종료된 Job 들의 정보는 여기에 담지 않음.
 * </li>
 * <li>작성일 : 2010. 4. 22.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// Controller 의 JobExecutionManager 와 혼동을 피하기 위해 이름을 Board로함.
// BatchContext 도 관리하기  위해 JobExecution 을 담지 않고 BatchContext를 담는다.
public class JobExecutionBoard {
	/**
	 * 실행중인 JobExecution
	 * <execution id, jobexecution> 
	 */
	private static Map<String, JobExecution>  runningJobExecutions = new ConcurrentHashMap();
	
	/**
	 * 실행중인 JobExecution. JobInstanceId 기준. 동일 JobInstance 에 대해 중복 실행 방지용.
	 * <instance id, jobexecution>
	 */
	private static Map<String, JobExecution>  runningJobExecutionsByJobInsId = new ConcurrentHashMap();
	
	/**
	 * 실행중인 JobExecution 의 BatchContext 객체. 없을 수도 있다. singlejvm의 경우는 runningJobExecutions 에만 있고 여기에는 없다.
	 * <execution id, batchcontext>
	 */
	private static Map<String, JobContext> runningJobContext  = new ConcurrentHashMap(); 
	
	/**
	 * JobExecutionBoard 의 변경이 발생한 시점.
	 * JobStateCallbackSender 에서 변경유무 체크할때 사용한다.
	 */
	private long   lastModifiedTime;
	
	public JobExecutionBoard() {
	}

    public void init() {
    }
    
    public void destroy() {
    }
    
	public void clear() {
		runningJobExecutions.clear();
	}
	
	public void add(JobContext context) {
		runningJobContext.put(context.getJobExecution().getJobExecutionId(), context);
		runningJobExecutions.put(context.getJobExecution().getJobExecutionId(), context.getJobExecution());
		runningJobExecutionsByJobInsId.put(context.getJobExecution().getJobInstanceId(), context.getJobExecution());
		lastModifiedTime = System.currentTimeMillis();
	}

	public void add(JobExecution jobexe) {
		runningJobExecutions.put(jobexe.getJobExecutionId(), jobexe);
		runningJobExecutionsByJobInsId.put(jobexe.getJobInstanceId(), jobexe);
		lastModifiedTime = System.currentTimeMillis();
	}

	public void remove(String jobExecutionId) {
		if (jobExecutionId == null) return;
		JobExecution removed = runningJobExecutions.remove(jobExecutionId);
		runningJobContext.remove(jobExecutionId);
		if (removed != null) {
			try {
				runningJobExecutionsByJobInsId.remove(removed.getJobInstanceId());
			}catch(Exception e) {
				// Null 에러 무시.
			}
		}
		lastModifiedTime = System.currentTimeMillis();
	}

	private boolean isPojoType(JobContext context) {
		return context != null && JobType.JOB_TYPE_POJO.equals(context.getJobExecution().getJobType());
	}

	private boolean isPojoType(JobExecution jobexe) {
		return jobexe != null && JobType.JOB_TYPE_POJO.equals(jobexe.getJobType());
	}
	
	public JobExecution getJobExecution(String jobExecutionId) {
		JobContext context = runningJobContext.get(jobExecutionId);
		if (context == null) {
			return runningJobExecutions.get(jobExecutionId);
		}else {
			if (isPojoType(context)) {
				((BatchContextAdapterForPojo) context.getBatchContextAdapter()).fillProgressStatusForPojo(context);
			}
		}
		return context == null ? null : context.getJobExecution();
	}

	/**
	 * Job Instance Id 로 JobExecution 을 찾아온다. 하나의 인스턴스는 하나의 Execution 만 존재할수 있으므로 단건 리턴한다. 
	 * @param jobInsId
	 * @return
	 */
	public JobExecution getJobExecutionByJobInsId(String jobInsId) {
		JobExecution jobexe = runningJobExecutionsByJobInsId.get(jobInsId);
		if (jobexe == null) {
			return null;
		}
		
		JobContext context = runningJobContext.get(jobexe.getJobExecutionId());
		if (context == null) {
			return jobexe;
		}
		
		if (isPojoType(context)) {
			((BatchContextAdapterForPojo) context.getBatchContextAdapter()).fillProgressStatusForPojo(context);
		}
		return context == null ? null : context.getJobExecution();
	}
	
	/**
	 * Job ID 를 기준으로 모든 JobExecution list를 리턴함.
	 * 중복 실행 방지 체크에서 사용될 수 있다.
	 * 
	 * @since 3.9 
	 * @param jobId
	 * @return
	 */
	public List<JobExecution> getJobExecutionsListByJobId(String jobId) {
	    List<JobExecution> list = new ArrayList();
	    for (JobExecution jobexe : runningJobExecutions.values()) {
	        if (Util.equals(jobexe.getJobId(), jobId)) {
	            list.add(jobexe);
	        }
	    }
	    return list;
	}
	
	public JobContext getJobContext(String jobExecutionId) {
		return runningJobContext.get(jobExecutionId);
	}

	public JobContext getJobContextByJobInsId(String jobInsId) {
		JobExecution jobexe = runningJobExecutionsByJobInsId.get(jobInsId);
		if (jobexe == null) {
			return null;
		}else {
			return runningJobContext.get(jobexe.getJobExecutionId());
		}
	}

	public int size() {
		return runningJobExecutions.size();
	}
	
	/**
	 * 스케줄러에서 진행률 정보 조회, JobExecution 목록 조회시 호출된다.
	 * 따라서 POJO 인 경우는 진행률 정보도 채워서 리턴해줘야한다. 
	 * @return
	 */
	public List<JobExecution> getJobExecutionsList() {
		List<JobExecution> list = new ArrayList<JobExecution>((int)(runningJobExecutions.size() * 1.2));
		
		for (JobContext jobContext : runningJobContext.values()) {
			JobExecution jobexe = jobContext.getJobExecution();
			if (isPojoType(jobexe)) {
				JobContext context = runningJobContext.get(jobexe.getJobExecutionId());
				if (context != null) {
					((BatchContextAdapterForPojo) context.getBatchContextAdapter()).fillProgressStatusForPojo(context);
				}
			}
			list.add(jobexe);
		}
		return list;
	}
	
	public int getJobExecutionCount() {
	    return runningJobExecutions.size();
	}
	
	public Set<String> getJobExecutionsIdList() {
		return runningJobExecutions.keySet();
	}
	
	public boolean containsJobExecution(String jobExeId) {
		return runningJobExecutions.containsKey(jobExeId);
	}
	
	public long getLastModifiedTime() {
		return lastModifiedTime;
	}
}
