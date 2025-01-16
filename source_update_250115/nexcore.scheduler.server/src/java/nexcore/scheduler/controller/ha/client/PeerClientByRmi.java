/**
 * 
 */
package nexcore.scheduler.controller.ha.client;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import nexcore.scheduler.controller.ha.IPeerClient;
import nexcore.scheduler.controller.ha.IPeerService;
import nexcore.scheduler.core.internal.LicenseManager;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobExecutionSimple;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.ByteArray;
import nexcore.scheduler.util.NRMIClientSocketFactory;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : peer 노드에 요청 하는 client. </li>
 * <li>작성일 : 2012. 11. 5.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2012. 11. 5.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class PeerClientByRmi implements IPeerClient {
	private LicenseManager            licenseManager;
	private String                    peerAddress;

	private boolean                   isPeerExist;       // 이중화 환경인지 single 환경인지 ?
	private IPeerService              peerService;
	
	private Log                       log;
	
	public PeerClientByRmi() {
		log = LogManager.getSchedulerLog();
	}
	
	public void init() {
		if(!licenseManager.canHA() && !Util.isBlank(peerAddress)){
			Util.logErrorConsole(MSG.get("main.license.cant.ha", licenseManager.getLicenseEdition()));
			System.exit(2);
		}
			
		connect();
		checkPeerTime();
	}
	
	public void destroy() {
	}
	
	private void connect() {
		if (Util.isBlank(peerAddress)) {
			isPeerExist = false;
			Util.logInfo(log, "No peer scheduler");
			return;
		}
		
		isPeerExist = true;
		String peerIp   = peerAddress.substring(0, peerAddress.indexOf(":"));
		int    peerPort = Integer.parseInt(peerAddress.substring(peerAddress.indexOf(":")+1));
		
		Util.logInfo(log, "Creating RMI Proxy to peer scheduler ["+peerIp+":"+peerPort+"].");
		
		// RMI Proxy 생성
		RmiProxyFactoryBean rmiProxyFactory = new RmiProxyFactoryBean();
		rmiProxyFactory.setServiceUrl("rmi://"+peerIp+":"+peerPort+"/PeerService");
		rmiProxyFactory.setServiceInterface(IPeerService.class);
		rmiProxyFactory.setRefreshStubOnConnectFailure(true);
		rmiProxyFactory.setLookupStubOnStartup(false);
		rmiProxyFactory.setRegistryClientSocketFactory(new NRMIClientSocketFactory(5000, 30000));
		
		rmiProxyFactory.afterPropertiesSet();
		
		
		// Set Service Object
		peerService = (IPeerService)rmiProxyFactory.getObject();
		
		Util.logServerInitConsole("PeerClient", "Peer=("+peerIp+":"+peerPort+")");
	}
	
	
	public LicenseManager getLicenseManager() {
		return licenseManager;
	}

	public void setLicenseManager(LicenseManager licenseManager) {
		this.licenseManager = licenseManager;
	}

	public String getPeerAddress() {
		return peerAddress;
	}

	public void setPeerAddress(String peerAddress) {
		this.peerAddress = peerAddress;
	}
	
	
	/**
	 * Peer 의 시각을 체크하여 나의 시각과 1분이상 차이나면 에러내고 startup 을 정지시킨다.
	 */
	private void checkPeerTime() {
		if (peerService == null) return; // single server 에서는 체크하지 않는다.
		try {
			long peerTime = peerService.getSystemTime();
			long myTime   = System.currentTimeMillis();
			int diff = (int)(Math.abs(myTime - peerTime) / 1000);
			if (diff > 60) {
				System.out.println(MSG.get("main.peer.time.diff.toobig", 1, diff));
				System.exit(2);
			}
		}catch(Exception e) {
			Util.logDebug(log, e.getMessage(), e);
			// peer 가 아직 안떠있는 상황이면 그냥 pass 한다.
		}
	}

	// ######################################################################
	// ########                     REMOTE METHOD                 ###########
	// ######################################################################

	/**
	 * 이중화 환경여부 리턴
	 * @return true if dual server.
	 */
	public boolean isPeerExist() {
		return isPeerExist;
	}

	private long lastIsAliveFalseTime; // isAlive() 가 false 를 리턴했던 최종 시각.
	
	/**
	 * alive 여부를 통신해서 체크한후 리턴한다.
	 * 
	 * 바로 직전 콜에서 false 상황이었으면, 1초 이내 다시 물어볼때는 isAlive() 통신하지 않고 그냥 false 리턴한다.
	 * 직전 false 리턴 이후 1초가 지난 후에는 다시 isAlive() 통신해본다.
	 * 
	 * peer down 상황에서는 isAlive() 시에 몇 초 정도 delay 가 되는데, 뻔한 down 상황에서 계속해서 isAlive() 통신을 하면
	 * 호출하는 쪽에서 계속 밀리게 되므로 이렇게 1 초 이내에는 false 로 pass 시키도록 한다. 
	 * 
	 * @return alive 여부
	 */
	private boolean _isAlive() {
		long beginTime = 0;
		boolean alive = false;
		try {
			if (lastIsAliveFalseTime == 0) { // 직전에도 문제 없었으므로 isAlive() 해 본다. (잘될 것을 기대하고)
				alive = peerService.isAlive();
			}else {
				beginTime = System.currentTimeMillis(); // 직전에 문제가 있었을 경우만 시간 체크한다. 너무 잦은 get time 은 성능을 저하시킬 수 있기 때문에
				if (System.currentTimeMillis() - lastIsAliveFalseTime < 1000) {
					// 직전에 isAlive false 가 된 이루 1초 이내 다시 call 하는 경우는 isAlive() 호출하지 말고 그냥 false 리턴한다.
					return false;
				}else {
					// 직전에 isAlive false 가 된 이루 1초 이내 다시 call 하는 경우는 isAlive() 호출해본다. 그냥 false 리턴한다.
					alive = peerService.isAlive();
				}
			}
		}catch(Throwable e) {
			String timemsg = beginTime == 0 ? "" : "(time="+(System.currentTimeMillis() - beginTime)+"ms)";
//			if (log.isDebugEnabled()) {
//				Util.logError(log, "["+Thread.currentThread().getName()+"] Peer["+peerAddress+"] may down. "+timemsg+" "+e.getMessage(), e);
//			}else {
				Util.logError(log, "["+Thread.currentThread().getName()+"] Peer["+peerAddress+"] may down. "+timemsg+" "+e.getMessage());
//			}  // 에러 메세지 감소.
			
			alive = false;
		}
		
		if (alive) {
			lastIsAliveFalseTime = 0;
		}else {
			lastIsAliveFalseTime = System.currentTimeMillis();
		}
		return alive;

	}
	
	public boolean isAlive() {
		return peerService == null ? false : _isAlive();
	}
	
	public long getSystemTime() {
		return peerService.getSystemTime();
	}

	public String getSystemId() {
		return peerService.getSystemId();
	}

	public boolean askToStart(String jobInstanceId) {
		return peerService.askToStart(jobInstanceId);
	}

	public void callBackJobEnd(JobExecution jobexe) {
		try {
			if (isAlive()) {
				peerService.callBackJobEnd(jobexe);
			}
		}catch(Throwable e) { 
			// peer 작업에 에러나면 무시하고 넘어간다. 에러가 나도 메모리 동기화가 약간 느려지지만 Job 상태 처리는 오동작 되지 않는다.
			Util.logError(log, "[PeerClient] callBackJobEnd() fail. [JEXE_ID:"+jobexe.getJobExecutionId()+"]", e);
		}
	}
	
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
	public void refreshMemoryCache(String targetObject, String key) {
		try {
			if (isAlive()) {
				peerService.refreshMemoryCache(targetObject, key);
			}
		}catch(Throwable ee) {
			Util.logError(log, "[PeerClient] refreshMemoryCache() fail. "+targetObject+"/"+key, ee);
		}
	}

	/**
	 * peer 에게 Repeat Timer 에 등록된 놈들인지 확인한다.
	 * @param jobinsid list
	 * @return repeat timer 에 등록된 Job Instance Id List
	 */
	public List<String> checkIfExistInRepeatTimer(List<String> jobinsidList) {
		try {
			if (isAlive()) {
				return peerService.checkIfExistInRepeatTimer(jobinsidList);
			}
		}catch(Throwable ee) {
			Util.logError(log, "[PeerClient] isScheduledForRepeatTimer() fail. "+jobinsidList, ee);
		}
		return null;
	}

	/**
	 * 내장 에이전트의 Job Exe State 리턴
	 * @param agentId
	 * @param jobExeId
	 * @return -1 if peer down, other number if peer normal.
	 */
	public int getInternalAgentJobExeState(String agentId, String jobExeId) {
		try {
			if (isAlive()) {
				return peerService.getInternalAgentJobExeState(agentId, jobExeId);
			}else {
				return -1;
			}
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] getInternalAgentJobExeState(%s, %s) fail. ", agentId, jobExeId), ee);
			throw Util.toRuntimeException(ee);
		}
	}

	/**
	 * 내장 에이전트에 수행중인 JobExe 들의 Map 을 리턴 
	 * @param agentId 내장 에이전트 ID
	 * @return Map of &lt; jobExeId, JobExeSimple &gt;
	 */
	public Map<String, JobExecutionSimple>  getInternalJobExecutionSimpleMap(String agentId) {
		try {
			if (isAlive()) {
				return peerService.getInternalJobExecutionSimpleMap(agentId);
			}else {
				return null;
			}
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] getInternalJobExecutionSimpleMap(%s) fail. ", agentId), ee);
			throw Util.toRuntimeException(ee);
		}
	}
	
	/**
	 * 내장 에이전트에 수행중인 JobExe 를 강제종료함.
	 * @param agentId
	 * @param jobExeId
	 */
	public void stopInternalJob(String agentId, String jobExeId) {
		try {
			peerService.stopInternalJob(agentId, jobExeId);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] stopInternalJob(%s, %s) fail. ", agentId, jobExeId), ee);
			throw Util.toRuntimeException(ee);
		}
	}
	
	/**
	 * 내장 에이전트에 수행중인 JobExe 를 suspend함.
	 * @param agentId
	 * @param jobExeId
	 */
	public void suspendInternalJob(String agentId, String jobExeId) {
		try {
			peerService.suspendInternalJob(agentId, jobExeId);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] suspendInternalJob(%s, %s) fail. ", agentId, jobExeId), ee);
			throw Util.toRuntimeException(ee);
		}
	}
	
	/**
	 * 내장 에이전트에 수행중인 JobExe 를 resume함.
	 * @param agentId
	 * @param jobExeId
	 */
	public void resumeInternalJob(String agentId, String jobExeId) {
		try {
			peerService.resumeInternalJob(agentId, jobExeId);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] resumeInternalJob(%s, %s) fail. ", agentId, jobExeId), ee);
			throw Util.toRuntimeException(ee);
		}
	}

	/**
	 * 내장 에이전트에 수행중인 Job 의 스레드 stack trace 조회.
	 * @param jobExecutionId
	 */
	public Map getInternalJobExecutionThreadStackTrace(String agentId, String jobExecutionId) {
		try {
			return peerService.getInternalJobExecutionThreadStackTrace(agentId, jobExecutionId);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] getInternalJobExecutionThreadStackTrace(%s, %s) fail. ", agentId, jobExecutionId), ee);
			throw Util.toRuntimeException(ee);
		}
	}

	/**
	 * 내장 에이전트에 수행중인 Job 의 로그레벨 조회.
	 * @param jobExecutionId
	 */
	public String getInternalJobExecutionLogLevel(String agentId, String jobExecutionId) {
		try {
			return peerService.getInternalJobExecutionLogLevel(agentId, jobExecutionId);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] getInternalJobExecutionLogLevel(%s, %s) fail. ", agentId, jobExecutionId), ee);
			throw Util.toRuntimeException(ee);
		}
	}

	/**
	 * 내장 에이전트에 수행중인 Job 의 로그레벨 변경.
	 * @param jobExecutionId
	 */
	public boolean setInternalJobExecutionLogLevel(String agentId, String jobExecutionId, String logLevel) {
		try {
			return peerService.setInternalJobExecutionLogLevel(agentId, jobExecutionId, logLevel);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] setInternalJobExecutionLogLevel(%s, %s, %s) fail. ", agentId, jobExecutionId, logLevel), ee);
			throw Util.toRuntimeException(ee);
		}
	}

	
	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 로그 파일명 조회
	 * @param agentId
	 * @param info
	 */
	public String getInternalJobLogFilename(String agentId, JobLogFilenameInfo info) {
		try {
			return peerService.getInternalJobLogFilename(agentId, info);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] getInternalJobLogFilename(%s, %s) fail. ", agentId, info.getJobInstanceId()), ee);
			throw Util.toRuntimeException(ee);
		}
	}
	
	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 로그 파일 길이 조회
	 * @param agentId
	 * @param filename
	 */
	public long getInternalJobLogFileLength(String agentId, String filename) {
		try {
			return peerService.getInternalJobLogFileLength(agentId, filename);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] getInternalJobLogFileLength(%s, %s) fail. ", agentId, filename), ee);
			throw Util.toRuntimeException(ee);
		}
	}
	
	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 SUB 로그 파일명 조회
	 * @param agentId
	 * @param info
	 */
	public String getInternalJobSubLogFilename(String agentId, JobLogFilenameInfo info) {
		try {
			return peerService.getInternalJobSubLogFilename(agentId, info);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] getInternalJobSubLogFilename(%s, %s) fail. ", agentId, info.getJobInstanceId()), ee);
			throw Util.toRuntimeException(ee);
		}
	}
	
	/**
	 * 파일이 존재하는지 (로그파일등) 확인함
	 * 내장에이전트의 Job Log 파일 read 용. 
	 * @param agentId
	 * @param filename
	 * @return
	 */
	public boolean isFileExist(String agentId, String filename) {
		try {
			return peerService.isFileExist(agentId, filename);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] isFileExist(%s, %s) fail. ", agentId, filename), ee);
			throw Util.toRuntimeException(ee);
		}
	}
	
	/**
	 * 내장 에이전트에 수행중인 Job 이 생성한 로그 read.
	 * @param logFilename
	 * @param offset
	 * @param length
	 */
	public ByteArray readInternalJobLogFile(String agentId, String logFilename, int offset, int length) {
		try {
			return peerService.readInternalJobLogFile(agentId, logFilename, offset, length);
		}catch(Throwable ee) {
			Util.logError(log, String.format("[PeerClient] readInternalJobLogFile(%s, %s, %d, %d) fail. ", agentId, logFilename, offset, length), ee);
			throw Util.toRuntimeException(ee);
		}
	}

	public void cleansingWaitingPool(Set<String> idListForWaitingPoolCleansing) {
		try {
			peerService.cleansingWaitingPool(idListForWaitingPoolCleansing);
		}catch(Throwable ee) {
			Util.logError(log, "[PeerClient] cleansingWaitingPool() fail. ", ee);
			throw Util.toRuntimeException(ee);
		}
	}
}
