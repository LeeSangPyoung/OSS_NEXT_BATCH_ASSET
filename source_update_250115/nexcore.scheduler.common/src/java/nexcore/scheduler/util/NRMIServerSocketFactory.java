/**
 * 
 */
package nexcore.scheduler.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2013. 7. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// read timeout 적용을 위한 custom RMIServerSocketFactory 
public class NRMIServerSocketFactory implements RMIServerSocketFactory {
	private int readTimeout = 30000;
	
	private ServerSocket serverSocket;
	
	public NRMIServerSocketFactory(int _readTimeout) {
		this.readTimeout = _readTimeout;
	}
	
	public ServerSocket createServerSocket(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(readTimeout);
		return serverSocket;
	}
}
