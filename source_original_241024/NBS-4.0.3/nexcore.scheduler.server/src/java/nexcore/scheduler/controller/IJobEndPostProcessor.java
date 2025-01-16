package nexcore.scheduler.controller;

import nexcore.scheduler.entity.JobExecution;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 9999 </li>
 * <li>설  명 : Job 이 종료된 후에 뭔가 후처리 작업을 돌릴 필요가 있을 경우 여기서 구현한다.  </li>
 * <li>작성일 : 2011. 4. 15.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IJobEndPostProcessor {
	
	/**
	 * 후처리 로직 수행
	 * @param jobexe
	 * @return true 정상 종료, false 에러 종료 
	 */
	public boolean doPostProcess(JobExecution jobexe);
}
