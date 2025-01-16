/**
 * 
 */
package nexcore.scheduler.controller.ha;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2012. 11. 5.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public interface IPeerClient extends IPeerService {
	/**
	 * 이중화 환경여부 리턴
	 * @return true if dual server.
	 */
	public boolean isPeerExist();
	
	/**
	 * peer 주소 리턴
	 * @return
	 */
	public String getPeerAddress();

}
