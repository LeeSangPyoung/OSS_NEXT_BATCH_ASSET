/**
 * 
 */
package nexcore.scheduler.agent.runner.proc;

import nexcore.scheduler.agent.JobContext;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2013. 7. 1.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public interface IProcJobExitHandler {

	/**
	 * 프로세스 타입 Job 종료후 처리할 로직을 여기서함.
	 * 기본 ProcJobRunner 의 종료시에도 호출하며,
	 * 에이전트 restart 시에 이전 프로세스 복구후 종료 시에도 여기서함.
	 * 
	 * 1) useParameterFile 속성이 true 이면 OUT 파라미터 파일을 읽어 JobExe 에 set.
	 * 
	 * 2) JobRunner 의 afterProcessExit() 호출
	 * 
	 * 3) runner 파일 (ncbatch_jobexeid.sh/.cmd) 삭제
	 * 
	 * 4) callBackJobEnd() 호출
	 * 
	 * 5) jeproc/*.je 파일 삭제
	 * 
	 * 6) -exit.log 파일 삭제
	 *  
	 * @param context
	 * @param jobRunner
	 * @param runnerFile
	 */
	public void handleProcessExit(JobContext context, ProcJobRunner jobRunner, String runnerFile);

}
