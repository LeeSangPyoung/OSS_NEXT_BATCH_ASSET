/**
 * 
 */
package nexcore.scheduler.agent.nsc;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.logging.Log;

import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.PaddableDataOutputStream;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : NSC 와의 통신 프로토콜을 구현한 클래스 </li>
 * <li>작성일 : 2012. 9. 12.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class NSCChannel {
	private String                        hostname;
	private int                           port;
	private int                           timeout = 30000;
	private boolean                       doLogData;
	
	private Socket                        socket;
	private DataInputStream               in;
	private PaddableDataOutputStream      out;
	
	private byte[]                        readBuffer = new byte[1024];
	
	private ByteArrayOutputStream         logOutStream;
	
	private Log  log;
	
	public NSCChannel() {
		log = LogManager.getLog("nsc");
		if (log.isDebugEnabled()) {
			Util.logDebug(log, "[NSC] "+this+" created.");
		}
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isDoLogData() {
		return doLogData;
	}

	public void setDoLogData(boolean doLogData) {
		this.doLogData = doLogData;
	}

	public void connect() throws IOException {
		socket = new Socket(hostname, port);
		socket.setSoTimeout(timeout);

		if (log.isDebugEnabled()) {
			Util.logDebug(log, "[NSC] "+this+" connected. socket="+socket);
		}
		
		logOutStream = new ByteArrayOutputStream(256);
		if (doLogData) {
			in  = new DataInputStream          (new TeeInputStream (socket.getInputStream(),  logOutStream));
			out = new PaddableDataOutputStream (new TeeOutputStream(socket.getOutputStream(), logOutStream));
		}else {
			in  = new DataInputStream          (socket.getInputStream());
			out = new PaddableDataOutputStream (socket.getOutputStream());
		}
	}
	
	public void close() {
		try { in.close();     }catch(Exception ignore) {}
		try { out.close();    }catch(Exception ignore) {}
		try { socket.close(); }catch(Exception ignore) {}
	}
	
	/**
	 * 1000 전문 송신. 본문 : job_exe_id char(50)
	 * @param header
	 * @param jobExeId JobExecutionId or "all" for all JobExecutions
	 * @throws IOException
	 */
	public void sendTR1000(NSCHeader header, String jobExeId) throws IOException {
		try {
			// 전문 종류 설정
			header.setTrKind("1000");
			
			// 전체 길이 설정
			header.setTotalLenInt(4+1+1+4 + 50);
			
			// 헤더 송신
			writeHeader(header);
			
			// 본문 송신. char(50)
			out.writeStringWithRPadding(jobExeId, 50, (byte)' ');
			out.flush();
			logAndResetLogStream("SEND-TR1000");
		}catch(IOException e) {
			logAndResetLogStream("SEND-TR1000 [ERROR]");
			throw e;
		}
	}
	
	/**
	 * 1000 전문 수신.
	 * 
	 * @param header 이 객체에 수신부 헤더의 정보가 담긴다. 
	 * @return 본문부 Job 상태 정보가 List 형태로 수신되어 리턴된다.
	 * @throws IOException
	 */
	public List<NSCJobStatus> recvTR1000(NSCHeader header) throws IOException {
		// 헤더 수신.
		readHeader(header);
		
		List<NSCJobStatus> list = new ArrayList<NSCJobStatus>(header.getArrayCountInt());
		
		// 본문 리스트 수신.
		if (header.getArrayCountInt() > 0) {
			for (int i=0; i<header.getArrayCountInt(); i++) {
				list.add(readJobStatus());
			}
		}else {
			// array count 가 0 이지만 total len 에 garbage 가 포함되서 들어왔다면 일단 그것을 읽어서 소비해주어야 다음 통신이 꼬이지 않는다. 
			if (header.getTotalLenInt() > 10) {
				readNextBytes(header.getTotalLenInt() - 10); // 읽어서 버린다.
			}
		}

		logAndResetLogStream("RECV-TR1000 ("+list.size()+")");
		return list;
	}
	
	/**
	 * 1001 전문 송신. 본문 : jobid char(100), jobinstanceid char(100)
	 * @param header
	 * @param componentName
	 * @param jobInsId
	 * @throws IOException
	 */
	public void sendTR1001(NSCHeader header, String componentName, String jobInsId) throws IOException {
		// 전문 종류 설정
		header.setTrKind("1001");
		
		// 전체 길이 설정
		header.setTotalLenInt(4+1+1+4 + 100 + 100);
		
		// 헤더 송신
		writeHeader(header);
		
		// 본문 송신. jobid char(50), job_instance_id char(50)
		out.writeStringWithRPadding(componentName,  100, (byte)' ');
		out.writeStringWithRPadding(jobInsId,       100, (byte)' ');
		out.flush();
		
		logAndResetLogStream("SEND-TR1001");
	}
	
	/**
	 * 1001 전문 수신.
	 * 
	 * @param header 이 객체에 수신부 헤더의 정보가 담긴다. 
	 * @return 로그 파일명 (절대 경로)가 500 바이트에 담겨 수신된다.
	 * @throws IOException
	 */
	public String recvTR1001(NSCHeader header) throws IOException {
		// 헤더 수신.
		readHeader(header);
		
		// 본문 수신
		String logFilename = readNextBytes(500).trim();
		
		logAndResetLogStream("RECV-TR1001");

		return logFilename;
	}

	/**
	 * 2000 전문 송신. 본문 : job_exe_id char(50), pstat (프로세스상태정보) char(1)
	 * @param header
	 * @param jobExeId
	 * @param pstat 프로세스 상태 플래그 [0:대기, 1:동작, 2:중지전, 3:중지, 4:중지후, 5:정상종료, 6:강제종료, 7:종료확정]
	 * @throws IOException
	 */
	public void sendTR2000(NSCHeader header, String jobExeId, String pstat) throws IOException {
		// 전문 종류 설정
		header.setTrKind("2000");
		
		// 전체 길이 설정
		header.setTotalLenInt(4+1+1+4 + 50+1);
		
		// 헤더 송신
		writeHeader(header);
		
		// 본문 송신. char(51)
		out.writeStringWithRPadding(jobExeId, 50, (byte)' ');
		out.writeStringWithRPadding(pstat,    1,  (byte)' ');
		out.flush();

		logAndResetLogStream("SEND-TR2000");
	}
	
	/**
	 * 2000 전문 수신.
	 * 
	 * @param header 이 객체에 수신부 헤더의 정보가 담긴다. 
	 * @return Job 상태 정보 하나가 수신되어 리턴된다.
	 * @throws IOException
	 */
	public NSCJobStatus recvTR2000(NSCHeader header) throws IOException {
		// 헤더 수신.
		readHeader(header);
		
		// 본문 수신.
		NSCJobStatus jobStatus = readJobStatus();

		logAndResetLogStream("RECV-TR2000");
		return jobStatus;
	}
	
	/**
	 * 9999 전문 송신. PING 송신
	 * @param header
	 * @throws IOException
	 */
	public void sendTR9999(NSCHeader header) throws IOException {
		// 전문 종류 설정
		header.setTrKind("9999");
		
		// 전체 길이 설정
		header.setTotalLenInt(4+1+1+4);
		
		// 헤더 송신
		writeHeader(header);
		
		out.flush();

		logAndResetLogStream("SEND-TR9999");
	}

	/**
	 * 9999 전문 수신. PING 수신
	 * @param header
	 * @throws IOException
	 */
	public void recvTR9999(NSCHeader header) throws IOException {
		// 헤더 수신.
		readHeader(header);
		
		logAndResetLogStream("RECV-TR9999");
	}

	//======================== 유틸리티 메소드 ==========================
	
	private void writeHeader(NSCHeader header) throws IOException {
		out.writeStringWithLPadding(header.getTotalLen(),    8, (byte)'0');
		out.writeStringWithLPadding(header.getTrKind(),      4, (byte)'0');
		out.writeStringWithLPadding(header.getRsFlag(),      1, (byte)'0');
		out.writeStringWithLPadding(header.getClientType(),  1, (byte)'0');
		out.writeStringWithLPadding(header.getArrayCount(),  4, (byte)'0');
	}
	
	private String readNextBytes(int length) throws IOException {
		in.readFully(readBuffer, 0, length);
//log.debug("RECV readNextBytes("+length+"):"+new String(readBuffer, 0, length)+"<");
		return new String(readBuffer, 0, length);
	}
	
	private void readHeader(NSCHeader header) throws IOException {
		header.setTotalLen   (readNextBytes(8).trim());
		header.setTrKind     (readNextBytes(4));
		header.setRsFlag     (readNextBytes(1));
		header.setClientType (readNextBytes(1));
		header.setArrayCount (readNextBytes(4).trim());
	}
	
	private NSCJobStatus readJobStatus() throws IOException {
		NSCJobStatus jobStatus = new NSCJobStatus();
		
		jobStatus.setJobExeId        (readNextBytes(50).trim());
		jobStatus.setStartTime       (readNextBytes(14));
		jobStatus.setEndTime         (readNextBytes(14));
		jobStatus.setType            (readNextBytes(1));
		jobStatus.setMemStatus       (readNextBytes(1));
		jobStatus.setProcStatus      (readNextBytes(1));
		jobStatus.setProgressTotal   (in.readLong());
		jobStatus.setProgressCurrent (in.readLong());
		jobStatus.setEtc             (readNextBytes(31));

		return jobStatus;
	}

	/**
	 * 
	 * @param message
	 */
	private void logAndResetLogStream(String title) {
		if (doLogData) {
			try {
				byte[] logdata = logOutStream.toByteArray();
				log.info("["+this.socket.getLocalPort()+"] ========== "+title+" BEGIN ("+logdata.length+") ==========");
				log.info("["+this.socket.getLocalPort()+"] "+new String(logdata)+"<");
				log.info("["+this.socket.getLocalPort()+"] ========== "+title+" END   ("+logdata.length+") ==========");
			} finally {
				logOutStream.reset();
			}
		}
	}
}
