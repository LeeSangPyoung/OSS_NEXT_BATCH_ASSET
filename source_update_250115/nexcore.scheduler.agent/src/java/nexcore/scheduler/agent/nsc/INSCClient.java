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
 * <li>설  명 : NSC 서버와 통신하는 Socket client </li>
 * <li>작성일 : 2012. 9. 12.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public interface INSCClient {

	/**
	 * Job 상태를 얻어온다.
	 * @param jobExecutionId 
	 * @return 로그파일명 (full path)
	 */
	public NSCJobStatus getJobProcessStatus(String jobExecutionId) throws IOException;
	
	/**
	 * 로그 파일명을 얻어온다.
	 * @param jobId 
	 * @param jobInsId 
	 * @return 로그파일명 (full path)
	 */
	public String getLogFilename(String jobId, String jobInsId) throws IOException;
	
	/**
	 * 진행률 조회
	 * @param jobExecutionId
	 * @return [0]:total count, [1]:current count
	 */
	public long[] getJobProgress(String jobExecutionId) throws IOException;
	
	/**
	 * 모든 Job 의 상태 조회
	 * @return List of NSCJobStatus
	 */
	public List<NSCJobStatus> getAllJobProcessStatus() throws IOException;

	/**
	 * 정지
	 * @param jobExecutionId
	 * @return 결과메세지
	 */
	public String suspend(String jobExecutionId) throws IOException;
	
	/**
	 * 재개 
	 * @param jobExecutionId
	 * @return 결과메세지
	 */
	public String resume(String jobExecutionId) throws IOException;
	
	/**
	 * Job 종료 처리가 정상으로 완료되어 NSC 의 상태 정보 메모리를 삭제함.
	 * @param jobExecutionId
	 */
	public void deleteMemoryBlock(String jobExecutionId) throws IOException;
	
	/**
	 * 통신 체크
	 */
	public void check() throws IOException;
	
	/**
	 * 통신종료
	 */
	public void close() throws IOException;
}
