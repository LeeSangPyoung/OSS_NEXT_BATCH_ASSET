/**
 * 
 */
package nexcore.scheduler.agent.nsc;

import java.io.IOException;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2012. 9. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public interface INSCClientFactory {

	public INSCClient getClient() throws IOException; 
	
	public void returnClient(INSCClient client);

}