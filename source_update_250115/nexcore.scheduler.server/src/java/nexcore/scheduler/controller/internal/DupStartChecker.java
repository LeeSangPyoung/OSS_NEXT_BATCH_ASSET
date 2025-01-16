package nexcore.scheduler.controller.internal;

import java.io.File;
import java.net.ServerSocket;

import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 스케줄러 서버가 동시에 여러가 수행중인지 체크하는 로직.</li>
 * <li>작성일 : 2011. 7. 18.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class DupStartChecker {
	private boolean  enable       = true;
	private int      registryPort;
	
	public DupStartChecker() {
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public int getRegistryPort() {
		return registryPort;
	}

	public void setRegistryPort(int registryPort) {
		this.registryPort = registryPort;
	}
	
	public void init() {
		// RMI 서버 포트 가용 여부 조사
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(registryPort);
            Util.logServerInitConsole("RMI", "(Registry Port:"+registryPort+")");
			// 포트 정상.
		}catch (Exception e) {
			// 포트 에러.
			Util.logErrorConsole(MSG.get("main.dup.server.socket.error", registryPort+""));
			System.exit(1);
		}finally {		
			 try {ss.close();}catch (Exception ignore) {}
		}

		// start.lock 파일 체크 여부와 상관없이 포트 체크는 먼저 해야하므로 enable 체크를 여기서 한다.
		if (!enable) { 
		    return;
		}
		// start.lock 파일 유무 검사
		File startLockFile = new File(System.getProperty("NEXCORE_HOME")+"/etc/start.lock");
		if (startLockFile.exists()) {
		    Util.logErrorConsole(MSG.get("main.dup.start.error"));
		    System.exit(1);
		}
		
		try {
			startLockFile.createNewFile();
		}catch(Exception e) {
			throw new SchedulerException("main.start.fail", e);
		}
	}

	public void destroy() {
	}
}
