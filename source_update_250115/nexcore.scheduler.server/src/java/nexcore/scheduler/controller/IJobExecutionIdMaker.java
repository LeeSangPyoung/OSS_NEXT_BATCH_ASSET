package nexcore.scheduler.controller;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : JobExecutionId 를 생성함. 프로젝트마다 커스터마이즈 될 수 있는 부분이나 기본적으로는 기본 로직을 사용함 </li>
 * <li>작성일 : 2010. 4. 28.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IJobExecutionIdMaker {
	/**
	 * Job Instance Id 에 일련번호 6자리를 붙여 Job Execution Id 를 만든다.
	 * @param jobInstanceId
	 * @return
	 */
	public String makeJobExecutionId(String jobInstanceId);
	
	/**
	 * SystemMonitor 가 로깅할 정보 리턴. null 일 경우 출력 안함.
	 * @return
	 */
	public String getMonitoringString();
	
}
