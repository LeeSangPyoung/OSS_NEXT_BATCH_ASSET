/**
 * 
 */
package nexcore.scheduler.agent.nsc;

import java.io.IOException;
import java.util.List;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : NSC 서버와 통신하여 C 배치 Job 의 상태 정보 등을 조회하고, 컨트톨한다. </li>
 * <li>작성일 : 2012. 9. 12.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class NSCClientImpl implements INSCClient {
	
	private NSCChannel channel;

	public NSCClientImpl(String hostname, int port, int timeout, boolean doLogData) throws IOException {
		channel = new NSCChannel();
		channel.setHostname(hostname);
		channel.setPort(port);
		channel.setTimeout(timeout);
		channel.setDoLogData(doLogData);
		channel.connect();
	}
	
	
	/**
	 * Job 프로세스 상태를 조회한다.
	 * @param jobExecutionId 
	 * @return 로그파일명 (full path)
	 */
	public NSCJobStatus getJobProcessStatus(String jobExecutionId) throws IOException {
		NSCHeader header = new NSCHeader();
		header.setRsFlag("0"); // 요청
		
		channel.sendTR1000(header, jobExecutionId);

		List<NSCJobStatus> jobStatusList = channel.recvTR1000(header); // 응답 헤더는 별 필요없으므로 요청 헤더 객체를 재활용한다.
		
		return jobStatusList.size() > 0 ? jobStatusList.get(0) : null;
	}
	
	/**
	 * 로그 파일명을 얻어온다.
	 * @param componentName 배치 프로그램명
	 * @param jobInsId 
	 * @return 로그파일명 (full path)
	 */
	public String getLogFilename(String componentName, String jobInsId) throws IOException {
		NSCHeader header = new NSCHeader();
		header.setRsFlag("0"); // 요청
		
		channel.sendTR1001(header, componentName, jobInsId);
		
		String logFilename = channel.recvTR1001(header);
		return logFilename;
	}
	
	/**
	 * 진행률 조회
	 * @param jobExecutionId
	 * @return [0]:total count, [1]:current count
	 */
	public long[] getJobProgress(String jobExecutionId) throws IOException {
		NSCJobStatus status = getJobProcessStatus(jobExecutionId);
		return status == null ? null : new long[]{status.getProgressTotal(), status.getProgressCurrent()};
	}
	
	/**
	 * 전체 Job 상태 조회
	 * @return 
	 */
	public List<NSCJobStatus> getAllJobProcessStatus() throws IOException {
		NSCHeader header = new NSCHeader();
		header.setRsFlag("0"); // 요청
		
		channel.sendTR1000(header, "all");

		List<NSCJobStatus> jobStatusList = channel.recvTR1000(header); // 응답 헤더는 별 필요없으므로 요청 헤더 객체를 재활용한다.
		
		return jobStatusList;
	}

	/**
	 * 정지.
	 * BEFORE_SUSPENDED 로 Proc Stat 를 전송함.
	 * @param jobExecutionId
	 * @return 결과메세지
	 */
	public String suspend(String jobExecutionId) throws IOException {
		NSCHeader header = new NSCHeader();
		header.setRsFlag("0"); // 요청
		
		channel.sendTR2000(header, jobExecutionId, NSCJobStatus.PROC_STATE_BEFORE_SUSPENDED); // 2:중지전
		
		NSCJobStatus status = channel.recvTR2000(header);
		if (jobExecutionId.equals(status.getJobExeId())) {
			return "OK";
		}else {
			return null;
		}
	}
	
	/**
	 * 재개 
	 * @param jobExecutionId
	 * @return 결과메세지
	 */
	public String resume(String jobExecutionId) throws IOException {
		NSCHeader header = new NSCHeader();
		header.setRsFlag("0"); // 요청
		
		channel.sendTR2000(header, jobExecutionId, NSCJobStatus.PROC_STATE_AFTER_SUSPENDED); // 4:중지후
		
		NSCJobStatus status = channel.recvTR2000(header);
		if (jobExecutionId.equals(status.getJobExeId())) {
			return "OK";
		}else {
			return null;
		}
	}
	
	/**
	 * Job 종료 처리가 정상으로 완료되어 NSC 의 상태 정보 메모리를 삭제함.
	 * @param jobExecutionId
	 */
	public void deleteMemoryBlock(String jobExecutionId) throws IOException {
		NSCHeader header = new NSCHeader();
		header.setRsFlag("0"); // 요청
		
		channel.sendTR2000(header, jobExecutionId, NSCJobStatus.PROC_STATE_EXIT_DEL_MEM); // 7:종료확정. 메모리 반납
		channel.recvTR2000(header);
	}

	/**
	 * 통신체크
	 */
	public void check() throws IOException {
		NSCHeader header = new NSCHeader();
		header.setRsFlag("0"); // 요청
		
		channel.sendTR9999(header);
		channel.recvTR9999(header);
	}
	
	/**
	 * 통신종료
	 */
	public void close() throws IOException {
		channel.close();
	}
}
