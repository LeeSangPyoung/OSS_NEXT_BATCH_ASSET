package nexcore.scheduler.controller.client;

import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.entity.IControllerService;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.util.Util;

/**
 *  
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Agent 에서 controller 와 통신할때 사용하는 client 모듈. 
 * 				Job  callback, Job 상태 조회, Job 결과 조회 등의 기능을 함
 *              스케줄러와 에이전트가 동일 JVM 에서 돌때 사용되는 Local call client
 * </li>
 * <li>작성일 : 2012. 4. 05.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ControllerClientByLocal implements IControllerClient {
	private IControllerService      controllerService;

	public void init() {
	}
	
	public void destroy() {
	}
	
	public IControllerService getControllerService() {
		return controllerService;
	}

	public void setControllerService(IControllerService controllerService) {
		this.controllerService = controllerService;
	}

	public boolean isAlive() {
		return controllerService.isAlive();
	}
	
	public String getSystemId() {
		return controllerService.getSystemId();
	}
	
	private RuntimeException checkNetworkAndMakeException(Exception causeException) {
		if (!"OK".equals(isAlive())) {
			return new AgentException("agent.scheduler.network.error", causeException);
		}else {
			return Util.toRuntimeException(causeException);
		}
	}
	
	// Agent 에서 Job 수행 결과를 controller 로 전송함.
	public boolean callBackJobEnd(JobExecution je) {
		try {
			return controllerService.callBackJobEnd(je);
		}catch(Exception e) {
			throw checkNetworkAndMakeException(e);
		}
	}
	
	public boolean callBackJobSuspend(JobExecution je) {
		try {
			return controllerService.callBackJobSuspend(je);
		}catch(Exception e) {
			throw checkNetworkAndMakeException(e);
		}
	}
	
	public boolean callBackJobResume(JobExecution je) {
		try {
			return controllerService.callBackJobResume(je);
		}catch(Exception e) {
			throw checkNetworkAndMakeException(e);
		}
	}

	// 온디멘드 배치를 호출함.
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp, byte[] onlineCtxData) {
		try {
			return controllerService.invokeOnDemandJob(jobId, inParam, callerId, callerIp, onlineCtxData);
		}catch(Exception e) {
			throw checkNetworkAndMakeException(e);
		}
	}
	// 온디멘드 배치를 호출함.
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp) {
		try {
			return controllerService.invokeOnDemandJob(jobId, inParam, callerId, callerIp);
		}catch(Exception e) {
			throw checkNetworkAndMakeException(e);
		}
	}
	
	public int getJobExecutionState(String jobExecutionId) {
		try {
			return controllerService.getJobExecutionState(jobExecutionId);
		}catch(Exception e) {
			throw checkNetworkAndMakeException(e);
		}
	}
	
	public int getJobExecutionReturnCode(String jobExecutionId) {
		try {
			return controllerService.getJobExecutionReturnCode(jobExecutionId);
		}catch(Exception e) {
			throw checkNetworkAndMakeException(e);
		}
	}

	public Properties getJobExecutionReturnValues(String jobExecutionId) {
		try {
			return controllerService.getJobExecutionReturnValues(jobExecutionId);
		}catch(Exception e) {
			throw checkNetworkAndMakeException(e);
		}
	}
}
