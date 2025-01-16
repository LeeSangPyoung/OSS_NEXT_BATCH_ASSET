/**
 * 
 */
package nexcore.scheduler.controller.ha;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobExecutionSimple;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.util.ByteArray;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2012. 11. 5.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public interface IPeerService {

	/**
	 * alive 여부 리턴
	 * @return true if alive
	 */
	public boolean isAlive();
	
	/**
	 * peer의 시스템 시각을 조회한다.
	 * 두 시스템간 시각을 비교하여 30초 이상 차이가 나면 경고 띄운다. 
	 * @return
	 */
	public long getSystemTime();
	
	/**
	 * peer의 NEXCORE_ID 값을 조회한다.
	 * 두 시스템간의 NEXCORE_ID 가 동일하면 에러를 내고 boot fail 시킨다.
	 * @return
	 */
	public String getSystemId();
	

	/**
	 * peer에게 Job 실행을 의뢰한다. (분산 효과)
	 * @param jobInstanceId
	 * @return
	 */
	public boolean askToStart(String jobInstanceId);

	/**
	 * Job End 되어 peer 에게 callBackJobEnd() 가 호출된 상황
	 * awakePreJobWaitingInstances,
	 * awakeParallelWaitingInstances,
	 * removeRunningJobExecution
	 * updateJobProgress
	 * 
	 * @param jobexe
	 */
	public void callBackJobEnd(JobExecution jobexe);
	
	/**
	 * peer 에게 메모리 refresh 상황을 통지함.
	 * 전역 파라미터, 
	 * 병렬제한 max 값, 
	 * calendar reload, 
	 * AgentInfo, 
	 * JobNotify, 
	 * JobNotifyReceiver 등의 DB  값이 변경될 경우 peer 에게도 notify 함.
	 * 
	 * @param targetObject
	 * @param key
	 */
	public void refreshMemoryCache(String targetObject, String key) ;
	
	/**
	 * peer 에게 Repeat Timer 에 등록된 놈들인지 확인한다.
	 * @param jobinsid list
	 * @return repeat timer 에 등록된 Job Instance Id List
	 */
	public List<String> checkIfExistInRepeatTimer(List<String> jobinsidList); 

	/**
	 * 내장 에이전트의 Job Exe 상태 코드 리턴
	 * @param agentId
	 * @param jobExeId
	 * @return -1 if peer down, other number if peer normal.
	 */
	public int getInternalAgentJobExeState(String agentId, String jobExeId);
	
	/**
	 * 내장 에이전트에 수행중인 JobExe 들의 Map 을 리턴
	 * @param agentId 내장 에이전트 ID 
	 * @return Map of &lt; jobExeId, JobExeSimple &gt;
	 */
	public Map<String, JobExecutionSimple>  getInternalJobExecutionSimpleMap(String agentId);
	
	/**
	 * 내장 에이전트에 수행중인 JobExe 를 강제종료함.
	 * @param agentId
	 * @param jobExeId
	 */
	public void stopInternalJob(String agentId, String jobExeId);

	/**
	 * 내장 에이전트에 수행중인 JobExe 를 suspend함.
	 * @param agentId
	 * @param jobExeId
	 */
	public void suspendInternalJob(String agentId, String jobExeId);
	

	/**
	 * 내장 에이전트에 수행중인 JobExe 를 resume함.
	 * @param agentId
	 * @param jobExeId
	 */
	public void resumeInternalJob(String agentId, String jobExeId);
	
	/**
	 * 내장 에이전트에 수행중인 Job 의 스레드 stack trace 조회.
	 * @param jobExecutionId
	 */
	public Map getInternalJobExecutionThreadStackTrace(String agentId, String jobExecutionId);

	/**
	 * 내장 에이전트에 수행중인 Job 의 로그레벨 조회.
	 * @param jobExecutionId
	 */
	public String getInternalJobExecutionLogLevel(String agentId, String jobExecutionId);

	/**
	 * 내장 에이전트에 수행중인 Job 의 로그레벨 변경.
	 * @param jobExecutionId
	 */
	public boolean setInternalJobExecutionLogLevel(String agentId, String jobExecutionId, String logLevel);


	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 로그 파일명 조회
	 * @param agentId
	 * @param info
	 */
	public String getInternalJobLogFilename(String agentId, JobLogFilenameInfo info);
	
	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 로그 파일 길이 조회
	 * @param agentId
	 * @param filename
	 */
	public long getInternalJobLogFileLength(String agentId, String filename);
	
	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 SUB 로그 파일명 조회
	 * @param agentId
	 * @param info
	 */
	public String getInternalJobSubLogFilename(String agentId, JobLogFilenameInfo info);
	
	/**
	 * 파일이 존재하는지 (로그파일등) 확인함
	 * 내장에이전트의 Job Log 파일 read 용. 
	 * @param agentId
	 * @param filename
	 * @return
	 */
	public boolean isFileExist(String agentId, String filename);
	
	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 로그 read.
	 * @param logFilename
	 * @param offset
	 * @param length
	 */
	public ByteArray readInternalJobLogFile(String agentId, String logFilename, int offset, int length);
	
	/**
	 * peer 의 PreJobWaitingPool, ParallelWaitingPool 의 garbarge 를 정리한다.
	 * @param idListForWaitingPoolCleansing
	 */
	public void cleansingWaitingPool(Set<String> idListForWaitingPoolCleansing);

}
