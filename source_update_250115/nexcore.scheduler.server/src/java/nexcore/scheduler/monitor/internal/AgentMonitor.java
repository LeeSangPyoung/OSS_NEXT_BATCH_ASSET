package nexcore.scheduler.monitor.internal;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.client.IAgentClient;
import nexcore.scheduler.controller.ha.IPeerClient;
import nexcore.scheduler.core.IMonitorDisplayable;
import nexcore.scheduler.entity.AgentInfo;
import nexcore.scheduler.entity.JobExecutionSimple;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 에이전트의 JobExecution 의 상태 (상태, 진행률) 의 값을 주기적으로 모니터링하는 데몬. 
 *              스케줄러의 여러 모듈에서 각자 모니터링 목적으로 에이전트를 호출하는 부하를 줄이기 위해서
 *              여기서만 에이전트의 JobExe list 를 조회하고 쌓아 놓으면 
 *              다른 모듈에서는 이 값으로 에이전트 호출을 대신한다. 
 * </li>
 * <li>작성일 : 2012. 11. 19.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

/*
 * - 수초마다 에이전트를 폴링하여 progress, state 값을 읽어 온다.
 * - RunningJobStateMonitor, JobProgressStatuaManaget 등 필요로 하는 곳에 정보를 서비스 한다. 
 * 
 * - 2012.12.04 Job 모니터링 뿐만 아니라 에이전트 자체도 모니터링을 한다. 이름을 AgentJobMonitor 에서 AgentMonitor 로 변경함.
 */
public class AgentMonitor implements Runnable, IMonitorDisplayable {
	private boolean                         enable;
	private AgentInfoManager                agentInfoManager;
	private JobProgressStatusManager        jobProgressStatusManager;
	private int                             agentPollingIntervalInSec      = 2;     // 에이전트 폴링 인터벌. 기본 2초
	private int                             agentPollingThreadPoolCoreSize = 5;     // 에이전트 폴링 스레드 core 개수
	private int                             agentPollingThreadPoolMaxSize  = 30;    // 에이전트 폴링 스레드 max 개수
	private IPeerClient                     peerClient;

	/*
	 * 에이전트별 JobExecution list 를 담은. 에이전트의 JobExecutionBoard 의 값을 여기에 mirroring 한다고 보면됨.
	 * Map<String, JobExecutionSimple> 은 agent 에서 한번 만들어서 수신받으면 절대 modify 를 하지 않는다.
	 */
	private Map<String, Map<String, JobExecutionSimple>> jobExePool;     // <AgentId, Map<jobexeid, JobExeSimple>>
	private Map<String, Boolean>                         closedMap;      // <AgentId, isClosed>
	private Map<String, String>                          agentAliveMsg;  // <AgentId, isAlive() 결과 메세지>
	
	private ThreadPoolExecutor              pollingWorkerThreadPool;
	private Map<String, Runnable>           pollingWorkerMap;         // <agentId, thread>
	
	private Thread                          thisThread;
	private Log                             log;

	public AgentMonitor() {
	}
	
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public AgentInfoManager getAgentInfoManager() {
		return agentInfoManager;
	}

	public void setAgentInfoManager(AgentInfoManager agentInfoManager) {
		this.agentInfoManager = agentInfoManager;
	}

	public JobProgressStatusManager getJobProgressStatusManager() {
		return jobProgressStatusManager;
	}

	public void setJobProgressStatusManager(JobProgressStatusManager jobProgressStatusManager) {
		this.jobProgressStatusManager = jobProgressStatusManager;
	}

	public int getAgentPollingIntervalInSec() {
		return agentPollingIntervalInSec;
	}

	public void setAgentPollingIntervalInSec(int agentPollingIntervalInSec) {
		this.agentPollingIntervalInSec = agentPollingIntervalInSec;
	}

	public int getAgentPollingThreadPoolCoreSize() {
		return agentPollingThreadPoolCoreSize;
	}

	public void setAgentPollingThreadPoolCoreSize(int agentPollingThreadPoolCoreSize) {
		this.agentPollingThreadPoolCoreSize = agentPollingThreadPoolCoreSize;
	}

	public int getAgentPollingThreadPoolMaxSize() {
		return agentPollingThreadPoolMaxSize;
	}

	public void setAgentPollingThreadPoolMaxSize(int agentPollingThreadPoolMaxSize) {
		this.agentPollingThreadPoolMaxSize = agentPollingThreadPoolMaxSize;
	}

	public IPeerClient getPeerClient() {
		return peerClient;
	}

	public void setPeerClient(IPeerClient peerClient) {
		this.peerClient = peerClient;
	}

	public void init() {
		jobExePool               = new ConcurrentHashMap();
		closedMap                = new ConcurrentHashMap();
		agentAliveMsg            = new ConcurrentHashMap();
		
		pollingWorkerThreadPool  = new ThreadPoolExecutor(agentPollingThreadPoolCoreSize, agentPollingThreadPoolMaxSize, 20, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		pollingWorkerMap         = new ConcurrentHashMap<String, Runnable>();  //<agentid, task>
		
		log = LogManager.getSchedulerLog();
		thisThread = new Thread(this, "AgentMonitor");
		thisThread.setDaemon(true);
		thisThread.start();

		Util.logServerInitConsole("AgentMonitor");
	}
	
	public void destroy() {
		enable = false;
	}

	// ============================================================================================
	// ============================================================================================

	private int     memoryCleanTimeCount;  // 에이전트가 삭제된 경우에는 AgentInfoManager 에서 알 수 없으므로, 별도로 clean 해주어야한다. 매번하는 것은 부하걸릴 수 있으므로 100번에 한번 정도 clean 체크한다.
	private boolean memoryCleanTimeFlag;

	/**
	 * 에이전트에 주기적으로 통신해서 최신 상태,진행률 정보를 조회해온다.
	 */
	public void run() {
		long errorCount = 0;
		while(!Thread.interrupted() && enable) {
			try {
				// 에이전트로 부터 진행률 정보를 얻어서 map 에 넣음.
				gatherJobExecutionInfoFromAgent();
				
				memoryCleanTimeFlag = ++memoryCleanTimeCount % 100 == 0; // 100 번에 한번 clean 한다.
				if (memoryCleanTimeFlag) {
					cleanPoolMemory();
					memoryCleanTimeCount = 0;
				}
				if (errorCount > 0) {
					Util.logInfo(log, "JobProgressStatusManager recovered from error");
					errorCount = 0;
				}
			}catch(Throwable e) {
				errorCount ++;
				Util.logError(log, "JobProgressStatusManager fail" , e);
			}finally {
				if (errorCount >= 10) {
					// 10 번 연속 에러가 나는 경우는 장시간 (1분 정도) sleep 한다. DB 가 다운되면 이런 현상이 발생한다. 너무 많은 로그가 남는 것을 방지하기 위한 조치
					Util.sleep(60000, true);
				}else {
					Util.sleep(agentPollingIntervalInSec * 1000, true);
				}
			}
		}
	}

	/**
	 * 에이전트의 실행중인 JobExecution 의 실시간 상태, 진행률 정보를 모아서 pool 에 담는다.
	 */
	private void gatherJobExecutionInfoFromAgent() throws SQLException {
		// 에이전트를 호출하도록 스레드 풀에 작업의뢰한다.
		for (final AgentInfo agentInfo : agentInfoManager.getCachedAgentInfos()) {
			if (!agentInfo.isInUse()) {
				continue;
			}

			if (pollingWorkerMap.get(agentInfo.getId()) == null) {
				Runnable task = new Runnable() {
					/*
					 * 에이전트 접속이 원할하지 않을 경우, 그 에이전트 때문에 다른 에이전트 정보 조회가 영향을 받을 수 있으므로 아래와 같이
					 * 별도 스레드를 띄워 조회하도록 한다. 
					 */
					private String agentId = agentInfo.getId();
					
					public void run() {
						Thread.currentThread().setName("AgentMonitorWorker-"+agentId);
						
						try {
							_run();
						}catch(Throwable ignore) { // 에러 무시하고 조용히 죽는다.
						}finally {
							pollingWorkerMap.remove(agentId);
							Thread.currentThread().setName("AgentMonitorWorker-Pooled");
						}
					}
					
					private void _run() {
						IAgentClient agentClient = agentInfoManager.getAgentClient(agentId);
						
						String aliveMsg = agentClient.isAlive();
						agentAliveMsg.put(agentId, aliveMsg);
						
						closedMap.put(agentId, agentClient.isClosed());
						
						Map<String, JobExecutionSimple> jobexeList = agentClient.getRunningJobExecutionSimpleMap();
						if (jobexeList == null) return;

						// 에이전트로 부터 받은 JobExecution 진행 정보를 pool 에 넣는다.
						jobExePool.put(agentId, jobexeList);

						// JobProgressStatusManager 에 진행률 update 도 한다.
						for (JobExecutionSimple jobexe : jobexeList.values()) {
							jobProgressStatusManager.updateRunningJobExecution(jobexe.getJobExecutionId(), jobexe.getProgressTotal(), jobexe.getProgressCurrent());
						}
						
						// 내장 에이전트일 경우는, peer 의 내장 에이전트에서도 돌고 있을 수 있으므로 거기서도 조회해온다.
						// peer 의 내장 에이전트의 Job 인 경우는 jobExePool 에는 넣지 않고, progressManager 에만 update 한다.
						// jobExePool 에 넣게 되면 동일 agentId 로 나의 internal 과 peer 의 internal 이렇게 두개의 에이전트가 존재하게 되므로 List<JobExeSimple> 관리가 어려워진다.  
						if (peerClient.isPeerExist() && agentInfo.isInternal()) {
							Map<String, JobExecutionSimple> peerInternalJobexeList = peerClient.getInternalJobExecutionSimpleMap(agentId);
							if (peerInternalJobexeList != null && peerInternalJobexeList.size() > 0) {
								for (JobExecutionSimple jobexe : peerInternalJobexeList.values()) {
									jobProgressStatusManager.updateRunningJobExecution(jobexe.getJobExecutionId(), jobexe.getProgressTotal(), jobexe.getProgressCurrent());
								}
							}
						}
					}
				};
				pollingWorkerMap.put(agentInfo.getId(), task);
				pollingWorkerThreadPool.execute(task); // 스레드풀에 작업의뢰.
			}else {
				/*
				 * 이미 동일 agent 에 대해 polling 이 진행중이다. 네트워크 문제이던 다른 문제로 인해 응답이 늦게 오고 있는 경우이므로
				 * 다시 getRunningJobExecutions() 하지는 않는다.
				 */
			}				
		}
	}

	/**
	 * pool 메모리 중에 에이전트가 삭제된 후에도 계속 남아있는 데이타들을 여기서 삭제한다.
	 * 가끔씩만 한다.
	 */
	private void cleanPoolMemory() {
		if (log.isDebugEnabled()) {
			log.debug("AgentMonitor.cleanPoolMemory() started.");
		}
		Iterator<String> iter = jobExePool.keySet().iterator();
		while(iter.hasNext()) {
			String agentId = iter.next();

			try {
				AgentInfo ai = agentInfoManager.getAgentInfo(agentId);
				// 삭제되거나 사용중지된 에이전트이므로 pool 에서 지운다.
				if (ai == null || !ai.isInUse()) {
					iter.remove();
					if (log.isDebugEnabled()) {
						log.debug("AgentMonitor.cleanPoolMemory() '"+agentId+"' deleted");
					}
				}
			}catch(Throwable e) {
				Util.logError(log, "AgentMonitor.cleanPoolMemory() fail", e);
			}
		}
	}
	
	/**
	 * 해당 Agent 에서 수신받은 active list of JobExecutions.
	 * 
	 * @param agentId
	 * @return 해당 에이전트의 JobExecution 의 Map. <b>Map 구조는 절대로 modify 하면 안됨 (put, remove 금지)</b>
	 */
	public Map<String, JobExecutionSimple> getJobExecutions(String agentId) {
		return jobExePool.get(agentId);
	}

	/**
	 * Pool 을 형성하고 있는 메모리 중에서 Agent Id list 를 리턴함.
	 * @return
	 */
	public Collection<String> getAgentIdList() {
		return jobExePool.keySet();
	}
	
	/**
	 * 해당 에이전트가 차단상태인지 확인.
	 * 차단상태일 경우는 Job 이 실행되지 않고 Wait 함.
	 * @param agentId
	 * @return
	 */
	public boolean isAgentClosed(String agentId) {
		Boolean b = closedMap.get(agentId);
		return b == null ? true : b;
	}
	
	/**
	 * 해당 에이전트의 alive 여부 확인. 
	 * @param agentId
	 * @return isAlive() 의 결과 메세지, null if notexist
	 */
	public String getAgentAliveMsg(String agentId) {
		return agentAliveMsg.get(agentId);
	}
	
	/**
	 * 에이전트 전체의 alive 메세지 리턴
	 * @return
	 */
	public Map<String, String> getAgentAliveMsgList() {
		Map m = new LinkedHashMap();
		for (String agentId : getAgentIdList()) {
			m.put(agentId, agentAliveMsg.get(agentId));
		}
		
		return m;
	}
	
	/**
	 * 에이전트가 사용중으로 설정돼있는지?
	 * @param agentId
	 * @return
	 */
	public boolean isAgentInUse(String agentId) {
		try {
			return agentInfoManager.isInUseAgent(agentId);
		}catch(Exception e) {
			throw Util.toRuntimeException(e);
		}
	}
	
	public String getDisplayName() {
		return "AgentMonitor";
	}

	public String getDisplayString() {
		int poolSize = 0;
		for (Map m : jobExePool.values()) {
			poolSize += m.size();
		}
		
		return "Worker : "+pollingWorkerMap.size()+", Pool : "+poolSize;
	}

}
