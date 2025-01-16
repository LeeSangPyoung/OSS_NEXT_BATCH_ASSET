package nexcore.scheduler.controller.client;

import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.entity.JobExecution;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Controller 와 통신하는 client. Agent 또는 bizunit 에서 (온디맨드 용) 필요로 하는 기능만 들어있다. </li>
 * <li>작성일 : 2010. 4. 22.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IControllerClient {

	public boolean isAlive();
	
	/**
	 * 스케줄러 NEXCORE_ID 값을 조회함.
	 * @return
	 */
	public String getSystemId();
	
	/**
	 * Job 실행 결과를 Controller 로 리턴함
	 * 
	 * @param je
	 */
	public boolean callBackJobEnd(JobExecution je);

	/**
	 * Job suspend 된 상황을 Controller 로 리턴함
	 * 
	 * @param je
	 */
	public boolean callBackJobSuspend(JobExecution je);
	
	/**
	 * Job resume 된 상황을 Controller 로 리턴함
	 * 
	 * @param je
	 */
	public boolean callBackJobResume(JobExecution je);
	
	
	/**
	 * 배치 Job 을 수행함. 
	 * NEXCORE 온라인 프레임워크의 온라인 프로그램에서 온디멘드 호출시에 사용됨.
	 * 온라인 서비스의 IOnlineContext 정보가 이용됨
	 * 
	 * @param jobId
	 * @param inParam
	 * @param callerId
	 * @param callerIp
	 * @param onlineCtxData
	 * @return jobExecutionId 리턴
	 */
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp, byte[] onlineCtxData);
	
	
	/**
	 * 배치 Job 을 수행함. 
	 * Non-NEXCORE 환경에서 프로그램적으로 배치를 호출할때 사용됨
	 * IOnlineContext 없이 호출자의 Audit 정보 (ID/IP) 만 넣음
	 * 
	 * @param jobId
	 * @param inParam
	 * @param callerId
	 * @param callerIp
	 * @return jobExecutionId 리턴
	 */
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp);

	/**
	 * Job 실행 상태 리턴.
	 * @param jobExecutionId
	 * @return
	 */
	public int getJobExecutionState(String jobExecutionId);
	
	/**
	 * 리턴코드 리턴
	 * @param jobExecutionId
	 * @return
	 */
	public int getJobExecutionReturnCode(String jobExecutionId);

	/**
	 * Job 실행 결과값 리턴.
	 * @param jobExecutionId
	 * @return
	 */
	public Properties getJobExecutionReturnValues(String jobExecutionId);
}