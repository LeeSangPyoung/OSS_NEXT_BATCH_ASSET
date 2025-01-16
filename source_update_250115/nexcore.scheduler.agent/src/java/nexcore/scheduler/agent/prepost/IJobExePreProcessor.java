/**
 * 
 */
package nexcore.scheduler.agent.prepost;

import nexcore.scheduler.agent.JobContext;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 쓰레드(JobExecution) 실행 전에 호출되는 선처리기 </li>
 * <li>작성일 : 2014. 2. 27.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IJobExePreProcessor {

	/**
	 * 쓰레드(JobExecution) 실행 전에 호출되는 선처리기
	 * @param context
	 */
	public void doPreProcess(JobContext context);

}
