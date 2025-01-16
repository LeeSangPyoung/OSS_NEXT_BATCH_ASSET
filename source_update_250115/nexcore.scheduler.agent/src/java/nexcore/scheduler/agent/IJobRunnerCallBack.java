package nexcore.scheduler.agent;

import nexcore.scheduler.entity.JobExecution;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : JobRunner 가 Job 수행을 마치고 났을때 callback 함 </li>
 * <li>작성일 : 2010. 4. 22.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IJobRunnerCallBack {
	
	/**
	 * Job 수행완료된 후에 이게 불림.
	 * @param jobexe
	 * @return true if success, false if error
	 */
	public boolean callBackJobEnd(JobExecution jobexe);
	
	/**
	 * Job suspend 된 후 이게 불림
	 * @param jobexe
	 */
	public void callBackJobSuspend(JobExecution jobexe);

	/**
	 * Job resume 된 후 이게 불림
	 * @param jobexe
	 */
	public void callBackJobResume(JobExecution jobexe);

	/**
	 * callback 오류로 인해 스케줄로은 아직 running 상태이지만, JobExecutionBoard 에서는 사라진 상태인 경우
	 * 스케줄러에서는 Ghost 처리를 해버리게됨.
	 * 
	 * 이때 바로 ghost 가 되지 않게 하기 위해, JobExecutionBoard 에서 못찾으면 jestore 디렉토리에서 파일을 한번더 찾아본다.
	 *
	 * @param jobExeId
	 * @return
	 */
	public JobExecution getJobExecutionFromFile(String jobExeId);
	
}
