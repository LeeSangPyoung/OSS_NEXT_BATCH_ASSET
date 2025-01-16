package nexcore.scheduler.core;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 시스템 모니터 로그에 display 할때 이 인터페이스를 상속받아 한다.</li>
 * <li>작성일 : 2012. 11. 21.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IMonitorDisplayable {

	/**
	 * 모니터링 대상 서비스/컴포넌트 명
	 * @return
	 */
	public String getDisplayName();
	
	/**
	 * SystemMonitor 가 로깅할 정보 리턴. null 일 경우 출력 안함.
	 * @return
	 */
	public String getDisplayString();
	
}