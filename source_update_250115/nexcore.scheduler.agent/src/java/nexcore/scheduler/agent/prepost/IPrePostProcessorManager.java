/**
 * 
 */
package nexcore.scheduler.agent.prepost;

import nexcore.scheduler.agent.JobContext;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 배치에이전트에서 Job 실행 전후에 호출되는 선후처리기 들 관리. singlejvm 에서의 선후처리와 호환되지 않으며, Jvm PrePost 는 없다. </li>
 * <li>작성일 : 2015. 8. 31.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public interface IPrePostProcessorManager {

	/**
	 * JobExecution 마다 application 실행 전에 호출됨 
	 * @param context
	 */
	public void doJobExePreProcessors(JobContext context);

	/**
	 * JobExecution 마다 application 실행 후에 호출됨 
	 * @param context
	 * @param e
	 */
	public void doJobExePostProcessors(JobContext context, Throwable e);

}