package nexcore.scheduler.entity;

import nexcore.scheduler.exception.SchedulerException;



/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : Job 인스턴스의 상태에 따라 취할 수 있는 action 리스트를 리턴하는 모듈 </li>
 * <li>설  명 : 00000  </li>
 * <li>작성일 : 2011. 3. 28.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobControlActionHelper {
	public static final String ACTION_FORCERUN   = "forcerun";
	public static final String ACTION_RERUN      = "rerun";
	public static final String ACTION_STOP       = "stop";
	public static final String ACTION_SUSPEND    = "suspend";
	public static final String ACTION_RESUME     = "resume";
	public static final String ACTION_FORCEENDOK = "forceendok";
	public static final String ACTION_TOGHOST    = "toghost";

	/**
	 * 
	 * @param jobState 현재 Job 인스턴스의 상태
	 * @return &lt; action (forcerun | rerun | stop | suspend | resume | forceendok), &gt; 
	 */
	public static String[] getPossibleActionList(String jobState, String jobType) {
		if (jobState == null) {
			throw new SchedulerException("main.job.state.error", jobState);
		}else if (jobState.equals(JobInstance.JOB_STATE_INIT)) {
			return new String[] {ACTION_FORCERUN, ACTION_FORCEENDOK};
		}else if (jobState.equals(JobInstance.JOB_STATE_WAIT)) {
			return new String[] {ACTION_FORCERUN, ACTION_FORCEENDOK};
			
		}else if (jobState.equals(JobInstance.JOB_STATE_ENDED_OK)) {
			return new String[] {ACTION_FORCERUN, ACTION_RERUN};
			
		}else if (jobState.equals(JobInstance.JOB_STATE_ENDED_FAIL)) {
			return new String[] {ACTION_FORCERUN, ACTION_RERUN, ACTION_FORCEENDOK};
			
		}else if (jobState.equals(JobInstance.JOB_STATE_RUNNING)) {
			if (JobType.JOB_TYPE_DUMMY.equalsIgnoreCase(jobType)   || 
				JobType.JOB_TYPE_PROC.equalsIgnoreCase(jobType)) {
				return new String[] {ACTION_STOP, ACTION_TOGHOST}; // DUMMY, PROC, JAVARUN 타입은 suspend 안됨.
			}else if (JobType.JOB_TYPE_DBPROC.equalsIgnoreCase(jobType) || 
		              JobType.JOB_TYPE_QUARTZJOB.equalsIgnoreCase(jobType)) { // EJB Start Only 타입, DB Procedure 타입, Quartz 타입일 경우는 running job 에 대해 아무런 조작을 할 수 없다.
				return new String[] {ACTION_TOGHOST};
			}else {
				return new String[] {ACTION_STOP, ACTION_SUSPEND, ACTION_TOGHOST};
			}
			
		}else if (jobState.equals(JobInstance.JOB_STATE_SUSPENDED)) {
			return new String[] {ACTION_STOP, ACTION_RESUME, ACTION_TOGHOST};
			
		}else if (jobState.equals(JobInstance.JOB_STATE_SLEEP_RPT)) {
			return new String[] {ACTION_FORCERUN, ACTION_FORCEENDOK};
			
		}else if (jobState.equals(JobInstance.JOB_STATE_EXPIRED)) {
			return new String[] {ACTION_FORCERUN, ACTION_RERUN, ACTION_FORCEENDOK};
			
		}else if (jobState.equals(JobInstance.JOB_STATE_GHOST)) {
			return new String[] {ACTION_FORCERUN, ACTION_RERUN, ACTION_FORCEENDOK};
			
		}else {
			throw new SchedulerException("main.job.state.error", jobState);
		}
	}
}

