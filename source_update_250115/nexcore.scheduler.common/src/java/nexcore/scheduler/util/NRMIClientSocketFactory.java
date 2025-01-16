/**
 * 
 */
package nexcore.scheduler.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2013. 7. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// socket connect timeout, read timeout 적용을 위한 custom RMIClientSocketFactory 
public class NRMIClientSocketFactory implements RMIClientSocketFactory {
	
	private int connectTimeout;
	private int readTimeout;
	
	public NRMIClientSocketFactory(int connectTimeout, int readTimeout) {
		this.connectTimeout = connectTimeout;
		this.readTimeout    = readTimeout;
		
		/**
		 * 생성자 값보다 JVM 프로퍼티 값이 우선한다. 프로퍼티로 설정되어있으면 그 값으로 엎어쓴다.
		 */
		checkSystemProperties(); 
	}

	/**
	 * JVM 프로퍼티에서 timeout 값 읽는다.
	 */
	private void checkSystemProperties() {
		String connectionTimeoutProp = System.getProperty("sun.rmi.transport.connectionTimeout"); // 이 값은 표준 속성이지만 잘 안먹는다.
		if (!Util.isBlank(connectionTimeoutProp)) {
			this.connectTimeout = Integer.parseInt(connectionTimeoutProp);
		}
	
		String readTimeoutProp = System.getProperty("sun.rmi.transport.tcp.readTimeout"); // 이 값은 표준 속성이지만 잘 안먹는다.
		if (!Util.isBlank(readTimeoutProp)) {
			this.readTimeout = Integer.parseInt(readTimeoutProp);
		}
	}
	
	public Socket createSocket(String host, int port) throws IOException {
        Socket s = new Socket();
        if (readTimeout > 0) {
        	s.setSoTimeout(readTimeout);
        }
        if (connectTimeout > 0) {
        	s.connect(new InetSocketAddress(host, port), connectTimeout);
        }else {
        	s.connect(new InetSocketAddress(host, port));
        }
        return s;
    }

}
