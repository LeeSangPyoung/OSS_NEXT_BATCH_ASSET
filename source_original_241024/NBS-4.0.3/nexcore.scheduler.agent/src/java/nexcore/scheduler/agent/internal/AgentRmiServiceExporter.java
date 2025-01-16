package nexcore.scheduler.agent.internal;

import java.net.ServerSocket;
import java.rmi.RemoteException;

import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.util.Util;

import org.springframework.remoting.rmi.RmiServiceExporter;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어 </li>
 * <li>설  명 : online was 에서는 Agent RMI 를 export 하지 않기 위해 기본, RmiServiceExporter를 wrap 한다. </li>
 * <li>작성일 : 2010. 10. 22.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class AgentRmiServiceExporter extends RmiServiceExporter {
	private int     rmiPort; 
	private boolean enable  = true;
	private boolean checkPortDup = false; // port 사용 여부를 먼저 체크할지 말지? WAS의 RMI registry 를 공유해서 사용하는 경우는 dup 체크를 하면안됨. 
	
	public void init() {
	}

	public void destroy() {
		if (enable) {
			try {
				super.destroy();
			} catch (RemoteException e) {
				throw Util.toRuntimeException(e);
			}
		}
	}
	
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
	public boolean isCheckPortDup() {
		return checkPortDup;
	}

	public void setCheckPortDup(boolean checkPortDup) {
		this.checkPortDup = checkPortDup;
	}

	public void setRegistryPort(int registryPort) {
		this.rmiPort = registryPort;
		super.setRegistryPort(registryPort);
	}
	
	public void afterPropertiesSet() throws RemoteException {
		if (enable) {
			try {
				if (checkPortDup) {
					ServerSocket ss = null;
					try {
						ss = new ServerSocket(this.rmiPort);
					}catch(Exception e) {
						throw new AgentException("agent.server.port.error", e); 
					}finally {
						try {
							ss.close(); 
						}catch(Exception ee) {}
					}
				}
		        Util.logServerInitConsole("RMI", "(Registry Port:"+rmiPort+")");
				try {
				    // RMI 포트 번호를 환경 변수에 설정
				    System.setProperty("NC_BATAGENT_RMI_PORT", String.valueOf(rmiPort));
				} catch (Exception e) {
				    System.out.println("System.setProperty(\"NC_BATAGENT_RMI_PORT\", \""+rmiPort+"\") fail.");
				}
				super.afterPropertiesSet();
			}catch(Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
}
