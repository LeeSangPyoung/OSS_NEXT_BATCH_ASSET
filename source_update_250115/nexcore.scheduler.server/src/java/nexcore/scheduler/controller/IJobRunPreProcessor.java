package nexcore.scheduler.controller;

import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobInstance;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 스케줄러 </li>
 * <li>설  명 : Job 이 기동되기 직전에 호출되는 전처리기 </li>
 * <li>작성일 : 2011. 11. 30.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IJobRunPreProcessor {
	
	/**
	 * Job 실행 직전에 전처리 로직 수행.<br>
	 * 이 메소드 안에서 RuntimeException 이 throw 되면 Job 은 launch 되지 않고 End Fail 됨 
	 * 
	 * @param jobins 실행하려는 JobInstance 정보
	 * @param jobexe 실행하려는 JobExecution 정보
	 * 
	 */
	public void doPreProcess(JobInstance jobins, JobExecution jobexe);
}
