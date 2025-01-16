package nexcore.scheduler.controller.client;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.entity.IControllerService;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.util.NRMIClientSocketFactory;
import nexcore.scheduler.util.Util;

import org.apache.commons.logging.Log;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 배치 스케줄러 </li>
 * <li>서브 업무명 : 베이스 모듈</li>
 * <li>설  명 : 스케줄러에 connect 하여 Job 실행할 수 있는 client 모듈. </li>
 * <li>작성일 : 2011.  6. 9.</li>
 * <li>작성일 : 2012. 12. 6. 이중화 지원</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class BatchCaller {
	protected	String 				schedulerIp;
	protected	int					schedulerPort;
	protected	String 				schedulerIp2;
	protected	int					schedulerPort2;

	protected	String				localIp;
	
	protected	RmiProxyFactoryBean	rmiProxyFactoryBean;
	
	protected   List<String>              schedulerAddressList;
	protected   List<IControllerService>  controllerServiceAliveList = new ArrayList(); // 접속 가능 리스트
	protected   List<IControllerService>  controllerServiceDeadList  = new ArrayList(); // 접속 불가 리스트

	protected   long                      lastDeadListCheckTime;
	protected   Log                       log;
	
	public BatchCaller(String ip, int port) {
		setSchedulerIp   (ip);
		setSchedulerPort (port);
		connect();
	}

	public BatchCaller(String ip, int port, String ip2, int port2) {
		setSchedulerIp    (ip);
		setSchedulerPort  (port);
		setSchedulerIp2   (ip2);
		setSchedulerPort2 (port2);
		connect();
	}
	
	public String getSchedulerIp() {
		return schedulerIp;
	}

	public void setSchedulerIp(String schedulerIp) {
		this.schedulerIp = schedulerIp;
	}

	public int getSchedulerPort() {
		return schedulerPort;
	}

	public void setSchedulerPort(int schedulerPort) {
		this.schedulerPort = schedulerPort;
	}

	public String getSchedulerIp2() {
		return schedulerIp2;
	}

	public void setSchedulerIp2(String schedulerIp) {
		this.schedulerIp2 = schedulerIp;
	}

	public int getSchedulerPort2() {
		return schedulerPort2;
	}

	public void setSchedulerPort2(int schedulerPort) {
		this.schedulerPort2 = schedulerPort;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	private void connect() {
		if (schedulerAddressList == null) {
			schedulerAddressList = new ArrayList<String>();
			schedulerAddressList.add(schedulerIp + ":" + schedulerPort);
			if (!Util.isBlank(schedulerIp2)) {
				schedulerAddressList.add(schedulerIp2 + ":" + schedulerPort2);
			}
		}
		
		for (String address : schedulerAddressList) {
			if (Util.isBlank(address)) {
				logInfo("No peer scheduler");
				return;
			}
			logInfo("[BatchCaller] scheduler connection : "+address);
			String peerIp   = address.substring(0, address.indexOf(":"));
			int    peerPort = Integer.parseInt(address.substring(address.indexOf(":")+1));
			
			logInfo("[BatchCaller] Creating RMI Proxy to scheduler ["+peerIp+":"+peerPort+"].");
			
			// RMI Proxy 생성
			RmiProxyFactoryBean rmiProxyFactory = new RmiProxyFactoryBean();
			rmiProxyFactory.setServiceUrl("rmi://"+peerIp+":"+peerPort+"/BatchController");
			rmiProxyFactory.setServiceInterface(IControllerService.class);
			rmiProxyFactory.setRefreshStubOnConnectFailure(true);
			rmiProxyFactory.setLookupStubOnStartup(false);
			rmiProxyFactory.setRegistryClientSocketFactory(new NRMIClientSocketFactory(5000, 30000));
			rmiProxyFactory.afterPropertiesSet();
			
			// Set Service Object
			IControllerService controllerService = (IControllerService)rmiProxyFactory.getObject();
			
			controllerServiceAliveList.add(controllerService);
		}
		try {
			localIp = InetAddress.getLocalHost().getHostAddress();
		}catch(Exception e) {
			logError("[BatchCaller] Fail to lookup local IP.", e);
		}

	}

	private boolean isControllerAlive(IControllerService service) {
		try {
			return service.isAlive();
		}catch(Throwable e) {
			e.printStackTrace();
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
	protected synchronized IControllerService getValidControllerService() {
		IControllerService selected = null;
		// alive list 에서
		int len = controllerServiceAliveList.size();
		for (int i=0; i<len; i++) {
			IControllerService service = controllerServiceAliveList.remove(0);  // 맨 앞에 것을 꺼낸다.
			
			if (isControllerAlive(service)) {
				// 사용한것은 맨 뒤로 보낸다.
				controllerServiceAliveList.add(service);
				selected = service;
				break;
			}else {
				// 장애난것은 dead list 로 보낸다.
				controllerServiceDeadList.add(service);
			}
		}

		long curr = System.currentTimeMillis();
		if (curr - lastDeadListCheckTime >= 60 * 1000 || selected == null) {
			// dead list 를 1분마다 alive 여부 체크한다.
			// 위에서 하나도 select 되지 않으면 dead list 도 체크한다.
			lastDeadListCheckTime = curr;
			int len2 = controllerServiceDeadList.size();
			for (int i=0; i<len2; i++) {
				IControllerService service = controllerServiceDeadList.remove(0); // 맨 앞에 것을 꺼낸다.
				
				if (isControllerAlive(service)) { // 장애 복구된 것을 다시 alive list 로 넣는다.
					controllerServiceAliveList.add(service);
					if (selected == null) { // 맨 처음 만난 것을 우선 선택한다.
						selected = service;
					}
				}else { // 여전히 dead 상태
					controllerServiceDeadList.add(service);
				}
			}
		}

		if (selected == null) {
			logError("[BatchCaller] All Controller connection fail. "+schedulerAddressList);
			try {
				if (controllerServiceDeadList.size() > 0) {
					controllerServiceDeadList.get(0).isAlive();
				}else if (controllerServiceAliveList.size() > 0) {
					controllerServiceAliveList.get(0).isAlive();
				}else {
					logError("[BatchCaller] Scheduler connection configuration abnormal. scheduler addresses = "+schedulerAddressList); // alive 도 dead 도 없다. 이해할 수 없는 상황이다.
				}
			}catch(Exception e) {
				logError("[BatchCaller] All Controller connection fail. ", e); // 모든 스케줄러 통신 에러인 상황이므로 stack print 한다.
			}
			throw new RuntimeException("[BatchCaller] All Controller connection fail");
		}
		return selected;
	}
	
	
	/**
	 * 온디멘드 배치를 호출함
	 * @param jobId
	 * @param inParam
	 * @param onlineCtx
	 * @return Job Execution Id
	 */
	public String callBatchJob(String jobId, Map<String, String> inParam, String callerId) {
		return getValidControllerService().invokeOnDemandJob(jobId, inParam==null ? new HashMap<String, String>() : inParam, callerId, localIp);
	}

	/**
	 * 해당 JobExecution 이 끝날때 까지 기다림.
	 * @param jobExecutionId
	 * @param timeoutMillis 이시간 만큼 끝나지 않으면 에러 발생.
	 */
	public void waitJobEnd(String jobExecutionId, long timeoutMillis) {
		long waitBeginTime = System.currentTimeMillis();
		try {
			while(getValidControllerService().getJobExecutionState(jobExecutionId) != JobExecution.STATE_ENDED) {
				Thread.sleep(1000); // 1초 쉰다.
				if (System.currentTimeMillis() - waitBeginTime > timeoutMillis) {
					throw new RuntimeException("On-demand Batch timeout. "+jobExecutionId); // TODO 메세지 정리.
				}
			}
		}catch (Exception e) {
			if  (e instanceof RuntimeException) {
				throw (RuntimeException)e;
			}else {
				throw new RuntimeException("Error On-demand Batch. "+jobExecutionId, e); // TODO 메세지 조회.
			}
		}
	}
	
	/**
	 * 해당 JobExecution 의 결과 ReturnCode 조회.
	 * @param jobExecutionId
	 * @return
	 */
	public int getJobReturnCode(String jobExecutionId) {
		return getValidControllerService().getJobExecutionReturnCode(jobExecutionId);
	}
	
	/**
	 * 해당 JobExecution 의 Return Values 조회
	 * @param jobExecutionId
	 * @return
	 */
	public Properties getJobReturnValues(String jobExecutionId) {
		return getValidControllerService().getJobExecutionReturnValues(jobExecutionId);
	}

	/**
	 * JobInstance 의 상태 조회.
	 * 스케줄러에서 DB 의 상태값 조회하여 리턴한다.
	 * @param jobInstanceId
	 * @return "I":init, "W":wait, "O":EndOk, "F":EndFail, "R":Running, "P":Suspended, "S":SleepRepeat, "X":Expired, "G":Ghost 
	 */
	public String getJobInstanceState(String jobInstanceId) {
		return getValidControllerService().getJobInstanceSimple(jobInstanceId).getJobState();
	}

	/**
	 * JobExecution 의 상태 조회.
	 * 스케줄러에서 DB 의 상태값 조회하여 리턴한다.
	 * @param jobExecutionId
	 * @return JobExecution.STATE_### 상수값. (1~6 : 실행중, 7 : Ended, 99 : Unknown or NotExist) 
	 */
	public int getJobExecutionState(String jobExecutionId) {
		return getValidControllerService().getJobExecutionState(jobExecutionId);
	}

	protected void logInfo(String msg) {
		if (log == null) {
			System.out.println(msg);
		}else {
			log.info(msg);
		}
	}

	protected void logError(String msg) {
		if (log == null) {
			System.err.println(msg);
		}else {
			log.error(msg);
		}
	}
	
	protected void logError(String msg, Throwable t) {
		if (log == null) {
			System.err.print(msg);
			System.err.print(" >> ");
			t.printStackTrace();
		}else {
			log.error(msg, t);
		}
	}
}
