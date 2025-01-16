package nexcore.scheduler.controller.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import nexcore.scheduler.entity.IControllerService;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.NRMIClientSocketFactory;
import nexcore.scheduler.util.Util;

/**
 *  
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Agent 에서 controller 와 통신할 때 사용하는 client 모듈.
 * 				Job  callback, Job 상태 조회, Job 결과 조회 등의 기능을 함</li>
 * <li>작성일 : 2010. 8. 30.</li>
 * <li>작성일 : 2012.11. 15. 이중화지원</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ControllerClientByRmi implements IControllerClient {
	private boolean                   enabled;
	private String                    schedulerAddress;
	
	private List<String>              schedulerAddressList;
	private List<IControllerService>  controllerServiceAliveList = new ArrayList(); // 접속 가능 리스트
	private List<IControllerService>  controllerServiceDeadList  = new ArrayList(); // 접속 불가 리스트
	
	private Map<String, IControllerService> controllerServiceMap = new HashMap(); // <schedulerid, ICS>

	private long                      lastDeadListCheckTime;
	private Log                       log;
	
	public void init() {
		log = LogManager.getAgentLog();
		if (enabled) {
			connect();
		}else {
			log.info("ControllerClient disabled");
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

	public String getSchedulerAddress() {
		return schedulerAddress;
	}

	public void setSchedulerAddress(String schedulerAddress) {
		this.schedulerAddress = schedulerAddress;
		schedulerAddressList  = Util.toList(schedulerAddress, ",");
		if (schedulerAddressList.size() > 2) {
			throw new RuntimeException("Scheduler list exceeded ("+schedulerAddressList.size()+").");
		}
	}

	private void connect() {
		int i = 0;
		for (String address : schedulerAddressList) {
			i++;
			if (Util.isBlank(address)) {
				Util.logInfo(log, "[ControllerClient] No scheduler");
				return;
			}
			Util.logInfo(log, "[ControllerClient] scheduler connection : "+address);
			String schedulerIp   = address.substring(0, address.indexOf(":"));
			int    schedulerPort = Integer.parseInt(address.substring(address.indexOf(":")+1));
			
			Util.logInfo(log, "[ControllerClient] ("+i+") Creating RMI Proxy to scheduler ["+schedulerIp+":"+schedulerPort+"].");
			System.out.println("Controller ("+i+") : ["+schedulerIp+":"+schedulerPort+"]");
			
			// RMI Proxy 생성
			RmiProxyFactoryBean rmiProxyFactory = new RmiProxyFactoryBean();
			rmiProxyFactory.setServiceUrl("rmi://"+schedulerIp+":"+schedulerPort+"/BatchController");
			rmiProxyFactory.setServiceInterface(IControllerService.class);
			rmiProxyFactory.setRefreshStubOnConnectFailure(true);
			rmiProxyFactory.setLookupStubOnStartup(false);
			rmiProxyFactory.setRegistryClientSocketFactory(new NRMIClientSocketFactory(5000, 30000));
			rmiProxyFactory.afterPropertiesSet();
			
			// Set Service Object
			IControllerService controllerService = (IControllerService)rmiProxyFactory.getObject();
			
			controllerServiceAliveList.add(controllerService);
		}
	}
	
	/**
	 * round-robin 하며 IControllerService 를 번갈아서 리턴함.
	 * isAlive() 로 체크하여 장애가 발생하지 않은 스케줄러쪽으로 리턴함.
	 * 장애가 발생한 노드로는 1분동안 round-robin 하지 않는다.
	 * 1분 후에 다시 isAlive() 체크한다.  
	 *
	 * (2013.7.22)
	 * 유효 Connection 을 가져올때 affinitiy 를 주어 두 스케줄러 중 선호되는 놈을 먼저 가져오도록 한다.
	 * Job launch 한 스케줄러로 우선 callback 하기 위한 조치.
	 *  
	 * <br>
	 * <br>에이전트의 멀티 스레드 환경에서 호출되므로 이 메소드 자체를 synchronized 건다. 대량 트랜잭션이 아니므로 이렇게 해서 성능에 큰 문제가 없다.
	 * @param affinitySchedulerId 우선 접속할 scheduler id
	 * @return valid IControllerService
	 */
	private synchronized IControllerService getValidControllerService(String affinitySchedulerId) {
		if (Util.isBlank(affinitySchedulerId)) {
			return getValidControllerService();
		}
		
		IControllerService service = controllerServiceMap.get(affinitySchedulerId);
		if (service == null) { 
			/* 
			 * 해당 스케줄러에 아직 한번도 정상 접속을 못한 상태.
			 * check dead 하여 살아나는지 다시 확인한다. 만약 checkDeadlist 한 시각이 1분이 안됐다면, 이 메소드 안에서 
			 * 알아서 check 하지 않고 skip할 것이다.
			 */
			checkDeadList();

			service = controllerServiceMap.get(affinitySchedulerId);
			if (service == null) {
				/* 
				 * checkdead list 후에도 null이면, 해당 스케줄러는 현재 down 상태임.
				 * alive 한 것 중 임의의 하나를 선택하여 리턴한다.  
				 */
				return getValidControllerService();
			}else { // dead check list 후 map 에 들어온 것은 정상 상태인 것이다.
				return service;
			}
		}else {
			if (isControllerAlive(service)) {
				return service;
			}else {
				// map 에는 들어있지만 정상이 아닌 상태. valid 한 것을 다시 받아온다.
				return getValidControllerService();
			}
		}
	}

	/**
	 * round-robin 하며 IControllerService 를 번갈아서 리턴함.
	 * isAlive() 로 체크하여 장애가 발생하지 않은 스케줄러쪽으로 리턴함.
	 * 장애가 발생한 노드로는 1분동안 round-robin 하지 않는다.
	 * 1분 후에 다시 isAlive() 체크한다.  
	 *
	 * <br>
	 * <br>에이전트의 멀티 스레드 환경에서 호출되므로 이 메소드 자체를 synchronized 건다. 대량 트랜잭션이 아니므로 이렇게 해서 성능에 큰 문제가 없다.
	 * @return valid IControllerService
	 */
	private synchronized IControllerService getValidControllerService() {
		if (log.isDebugEnabled()) {
			log.debug("[ControllerClient] controllerService alive list : "+controllerServiceAliveList.size()+", dead list : "+controllerServiceDeadList.size());
		}
		IControllerService selected = null;
		// alive list 에서 
		int len = controllerServiceAliveList.size();
		for (int i=0; i<len; i++) {
			IControllerService service = controllerServiceAliveList.remove(0);  // 맨 앞에 것을 꺼낸다.
			
			String schedulerId = getControllerSystemId(service);
			if (schedulerId != null) { // null 이 아니면 alive 상태임을 의미함.
				// 사용한것은 맨 뒤로 보낸다.
				controllerServiceAliveList.add(service);
				controllerServiceMap.put(schedulerId, service);
				selected = service;
				break;
			}else {
				// 장애난것은 dead list 로 보낸다.
				controllerServiceDeadList.add(service);
			}
		}
		
		// ## dead-list 에 있는 것들을 health check 하여, 살아난 것들은 다시 alive-list 로 넣는다.
		if (selected == null) {
			selected = checkDeadList();
		}
		
		// ## dead list 까지 체크했는데로 null 이면 두 스케줄러 모두 down 상태이다.
		if (selected == null) { 
			Util.logError(log, "[ControllerClient] All Controller connection fail. "+schedulerAddressList);
			try {
				if (controllerServiceDeadList.size() > 0) {
					controllerServiceDeadList.get(0).isAlive();
				}else if (controllerServiceAliveList.size() > 0) {
					controllerServiceAliveList.get(0).isAlive();
				}else {
					Util.logError(log, "[ControllerClient] Scheduler connection configuration abnormal. scheduler addresses = "+schedulerAddressList); // alive 도 dead 도 없다. 이해할 수 없는 상황이다.
				}
			}catch(Exception e) {
				Util.logError(log, "[ControllerClient] All Controller connection fail. ", e); // 모든 스케줄러 통신 에러인 상황이므로 stack print 한다.
			}
			
			throw new AgentException("agent.scheduler.network.error");
		}
		return selected;
	}

	/**
	 * dead list 에서 체크하여 살아있는 것을 alivelist 로 옮기고,
	 * 살아난놈 중 맨처음 살아있는 놈을 리턴한다.
	 * @return 살아난놈 중 맨처음 것, null if 직전 체크 이후로 1분이 안지났을 경우
	 */
	private IControllerService checkDeadList() {
		// dead-list 에 있는 것들을 health check 하여, 살아난 것들은 다시 alive-list 로 넣는다.
		long curr = System.currentTimeMillis();
		if (curr - lastDeadListCheckTime < 60 * 1000) {
			/*
			 * 직전 체크후 1분이 안지났을 경우는 그냥 null 리턴한다.
			 * 네트워크 장애, 다운등의 상황에서는 dead check 할때 몇초 정도 지연되는 현상이 있다.
			 * 1분의 간격을 두지 않을 경우, 스케줄러 중 하나가 장애 상태에서는 dead check 하느라고 전체가 지연되는 현상이 발생할 수 있다. 
			 */
			return null;
		}

		lastDeadListCheckTime = curr;
		IControllerService retval = null;
		int len2 = controllerServiceDeadList.size();
		for (int i=0; i<len2; i++) {
			IControllerService service = controllerServiceDeadList.remove(0); // 맨 앞에 것을 꺼낸다.
			
			String schedulerId = getControllerSystemId(service);
			if (schedulerId != null) { // null 이 아니면 alive 상태임을 의미함. 장애 복구된 것을 다시 alive list 로 넣는다.
				controllerServiceAliveList.add(service);
				controllerServiceMap.put(schedulerId, service);
				if (retval == null) { // 맨 처음 만난 것을 우선 선택한다.
					retval = service;
				}
			}else { // 여전히 dead 상태
				controllerServiceDeadList.add(service);
			}
		}
		return retval;
	}
	
	/**
	 * 컨트롤로(스케줄러) 의 NEXCORE_ID 값을 조회한다.
	 * @param service
	 * @return 해당 스케줄러의 NEXCORE_ID 값을 리턴하며, 통신에러일 경우는 null 리턴.
	 */
	private String getControllerSystemId(IControllerService service) {
		try {
			String systemId = service.getSystemId();
			if (log.isDebugEnabled()) {
				log.debug("[ControllerClient] check server . NEXCORE_ID="+systemId);
			}
			
			return systemId;
		}catch(Throwable e) {
			if (log.isDebugEnabled()) {
				log.debug("[ControllerClient] check server connection error.", e);
			}
			return null; // 통신에러일 경우 null 을 리턴한다.
		}
	}
	
	private boolean isControllerAlive(IControllerService service) {
		try {
			return service.isAlive();
		}catch(Throwable e) {
			return false;
		}
	}
	
	/**
	 * 스케줄러 중 하나만이라도 살아있으면 alive 로 본다.
	 */
	public boolean isAlive() {
		try {
			return getValidControllerService().isAlive();
		}catch(Throwable e) {
			return false;
		}
	}
	
	public String getSystemId() {
		IControllerService service = getValidControllerService();
		
		return service.getSystemId();
	}

	// Agent 에서 Job 수행 결과를 controller 로 전송함.
	public boolean callBackJobEnd(JobExecution je) {
		/*
		 * 2013.07.24. #21352. callbackJobEnd() 는 우선적으로 start 한 스케줄러로 callback한다.
		 * 반복 작업일 경우 두 스케줄러가 수초 정도 차이가 발생하게 되면, 정확한 interval 계산이 되지 않을채로 반복이 되는 현상이 발생한다.
		 * 따라서 우선 start 한 스케줄러로 (한 스케줄러로) 몰아서 실행이 되게 한다. 
		 */
		return getValidControllerService(je.getInParameters().get("SCHEDULER_ID")).callBackJobEnd(je);
	}
	
	public boolean callBackJobSuspend(JobExecution je) {
		return getValidControllerService().callBackJobSuspend(je);
	}
	
	public boolean callBackJobResume(JobExecution je) {
		return getValidControllerService().callBackJobResume(je);
	}

	//######################################################################
	//############   아래 메소드 들은 온라인 서버에서 사용됨. 실제로 사용되지는 않음
	//######################################################################
	
	// 온디멘드 배치를 호출함.
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp, byte[] onlineCtxData) {
		return getValidControllerService().invokeOnDemandJob(jobId, inParam, callerId, callerIp, onlineCtxData);
	}
	// 온디멘드 배치를 호출함.
	public String invokeOnDemandJob(String jobId, Map<String, String> inParam, String callerId, String callerIp) {
		return getValidControllerService().invokeOnDemandJob(jobId, inParam, callerId, callerIp);
	}
	
	public int getJobExecutionState(String jobExecutionId) {
		return getValidControllerService().getJobExecutionState(jobExecutionId);
	}
	
	public int getJobExecutionReturnCode(String jobExecutionId) {
		return getValidControllerService().getJobExecutionReturnCode(jobExecutionId);
	}

	public Properties getJobExecutionReturnValues(String jobExecutionId) {
		return getValidControllerService().getJobExecutionReturnValues(jobExecutionId);
	}
}
