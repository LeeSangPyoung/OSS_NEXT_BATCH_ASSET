package nexcore.scheduler.agent.runner.proc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.internal.JobExecutionBoard;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 쉘실행 방식의 Job 들에서 UDP 로 Progress 정보를 보내면, 여기서 받아서 JobExecutionBoard 에 반영한다.</li>
 * <li>작성일 : 2016. 6. 24.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

// 로직을 간단히 처리하기 위해 UDP 수신 이벤트와, jeboard 디렉토리 scan을 이 쓰레드에서 single thread 로 처리한다.
public class ProcJobProgressUdpReceiver implements Runnable {
	private boolean                  enabled;
	private int                      port;
	private JobExecutionBoard        jobExecutionBoard;
	
	private Thread                   thisThread;
	private Log                      log;

	private DatagramSocket           socket;
	private byte[]                   recvBuffer  = new byte[100]; // 현재는 100 byte 만 필요함. 나중에 더 필요할 경우 이 값을 늘림
	private long                     lastErrorTime;
	private int                      errorCount;
	
	public void init() {
		log = LogManager.getAgentLog();
		
		if (enabled) {
			try {
				connectSocket();
				thisThread = new Thread(this, "ProcJobProgressUdpReceiver");
				thisThread.setDaemon(true);
				thisThread.start();
				Util.logInfo(log, "[ProcJobProgressUdpReceiver] service started.");
			}catch(Exception e) {
				throw Util.toRuntimeException(e);
			}
		}
	}

	public void destroy() {
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public JobExecutionBoard getJobExecutionBoard() {
		return jobExecutionBoard;
	}

	public void setJobExecutionBoard(JobExecutionBoard jobExecutionBoard) {
		this.jobExecutionBoard = jobExecutionBoard;
	}

	private void connectSocket() throws SocketException {
		if (socket != null) {
			try {
				socket.close(); // 이전 socket 이 살아있으면 close 하고 다시 맺는다.
			}catch(Exception e) {
				Util.logWarn(log, "[ProcJobProgressUdpReceiver] socket close error. (reconnect)", e);
			}
		}
		socket = new DatagramSocket(port);
		socket.setSoTimeout(60000);
		Util.logInfo(log, "[ProcJobProgressUdpReceiver] socket created. port="+socket.getLocalPort());
	}

	/**
	 * 패킷 수신.
	 * @return 데이타그램패킷 if receive, null if error or timeout
	 */
	private DatagramPacket doReceive() throws IOException {
		DatagramPacket packet = new DatagramPacket(recvBuffer, 0, recvBuffer.length);
		try {
			socket.receive(packet);
			return packet;
		}catch(Exception e) {
			return null; // timeout 나면 그냥 null 리턴한다.
		}
	}

	public void run() {
		while(enabled && !Thread.interrupted()) {
			try {
				DatagramPacket packet = doReceive();
				if (packet == null) continue;
				byte[] message = packet.getData();
				if (packet != null) {
					String command = new String(message, 0, 10).trim();
					
					if ("PROGRESS".equals(command)) {
						String jobExeId        = new String(message, 10, 50).trim();
						long   progressTotal   = Util.toLong(new String(message, 60, 20).trim());
						long   progressCurrent = Util.toLong(new String(message, 80, 20).trim());
						
						JobExecution jobexe = jobExecutionBoard.getJobExecution(jobExeId);
						if (jobexe != null) {
							jobexe.setProgressTotal  (progressTotal);
							jobexe.setProgressCurrent(progressCurrent);
						}
					}
				}
				// JobStart 상태인 것들은 ps 로 프로세스 살아있는지도 확인한다. 
			}catch(Throwable e) {
				Util.logError(log, "[ProcJobProgressUdpReceiver] packet receive fail.", e);
				/*
				 * 무한 에러에 빠지는 것을 방지 하기 위해 5초 이내에 10번 에러 발생할 경우 10초간 sleep 후 다시 receive 시도함.
				 */
				errorCount++;
				
				if (errorCount >= 10) {
					long curr = System.currentTimeMillis();
					if (curr - lastErrorTime < 5000) {// 5초 이내
						Util.logWarn(log, "[ProcJobProgressUdpReceiver] too many receive errors. will sleep 10 seconds.");
						Util.sleep(10000); // 10초 쉰다.
						errorCount = 0;
						lastErrorTime = System.currentTimeMillis();
					}
				}
			}
		}
	}
}


