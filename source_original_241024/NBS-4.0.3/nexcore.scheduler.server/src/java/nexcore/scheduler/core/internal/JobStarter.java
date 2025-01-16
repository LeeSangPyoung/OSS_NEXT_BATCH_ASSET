package nexcore.scheduler.core.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;

import nexcore.scheduler.controller.internal.ControllerMain;
import nexcore.scheduler.core.IMonitorDisplayable;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.SchedulerUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job start를 위해 조건을 판단하고, 실행조건을 만족시키면 launch 하는놈</li>
 * <li>작성일 : 2010. 5. 19.</li>
 * <li>개정일 : 2010. 12. 15.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobStarter implements IMonitorDisplayable {
	private JobInstanceManager           jobInstanceManager;
	private ControllerMain               controllerMain;
	private boolean                      queueClosed;                 // 가동중인가?
	private JobRunConditionChecker       jobRunConditionChecker;
	private ParallelRunningCounter       parallelRunningCounter;
	private int                          runQueueThreadCount = 1;     // runQueue를 보고 있는 스레드 개수.
	
	private BlockingQueue<String>        decisionQueue;               // 실행할지 말지 판단하도록 대기하는 큐. consumer 는 1 개 스레드
	private BlockingQueue<JobInstance>   runQueue;                    // 실행하도록 판단된 건들이 실제 실행될때까지 대기하는 큐. consumer는 n 개 스레드
	private Map<String, Long>            decisionQueueCache;          // DecisionQueue 에 들어있는 Job Ins Id 들의 Map. 불필요하게 중복되게 Queue 에 넣을 필요가 없으므로. (decision 성능 향상). PUT 하는 그 시각을 Long 값으로 넣는다.
	private Map<String, JobInstance>     runQueueCache;               // RunQueue 에 들어있는 Job Ins Id map. 
	
	private DecisionQueueThread          decisionQueueThread;
	private Map<Integer, RunQueueThread> runQueueThreadMap;
	private boolean                      destroyed;                   // destroy 되었는가? concumer 들 죽이기 위해
	
	private Log                          log;
	
	// 스케줄러 자신의 시스템 정보. 로그용 operatorid, operatorip
	private AdminAuth                    systemAdminAuth = AdminAuth.getAdminAuthSchedulerItself();
	
	class DecisionQueueThread extends Thread {
		public DecisionQueueThread() {
			super("JobStarter.DecisionQueueConsumer");
		}
		
		public void run() {
			// decisionQueue 를 보면서 하나씩 꺼내 실행 조건을 만족시켰는지 체크하여, 상태변경후 runQueue로 보냄.
			while(!destroyed) {
				String      jobinsId;
				long        putTime = 0;  // put 시점의 시각을 decision 의 current 시각으로 삼는다. EXACT 방식에서는 이게 매우 중요함.
				try {
					jobinsId = decisionQueue.take();
					if (jobinsId != null) { // DecisionQueue Cache 에서도 삭제한다.
						putTime = decisionQueueCache.remove(jobinsId);
					}
				} catch (InterruptedException e) {
					continue;
				}
				
				try {
					SchedulerUtil.checkStartedWithNoRun(); // Job 실행 시킬 수 있는 상황인지 부터 체크.
				}catch(Exception e) {
					Util.logInfo(log, "[DecisionQueueThread] "+e.getMessage());
					continue;
				}
				
				if (log.isDebugEnabled() && jobRunConditionChecker.isLogDecisionResult()) {
					log.debug("▶▶ DecisionQueue check start. ["+jobinsId+"]");
				}
				try {
					/*
					 * 큐에 있는 중에 간혹 같은 jobinsid로 먼저 enqueue 된 놈이 실행상태로 들어갈 수 있다.
					 * 중복 실행 방지를 위해 다시 한번 read를 한다. (no-deep)
					 * 
					 * 실행 조건 판단에는 파라미터가 필요없다. 
					 * PreJobCondition 은 선행조건 체크 단계에서 다시 load 하므로 여기서는 하지 않는다.
					 * 한화증권현장에서 과도한 쿼리 실행이 발생했다. (2013-06-18) 
					 */
					JobInstance jobins = jobInstanceManager.getJobInstance(jobinsId); 
					if (jobRunConditionChecker.doCheck(jobins, putTime)) {
						// 실행조건을 만족 시킨 경우임. 타 Job의 실행 조건에 영향을 줄만한 상태 변경을 여기서 함.
						// 이제 실행할 것이므로 파라미터 로드한다.
						jobInstanceManager.loadParameters(jobins);
						
						// Job State를 변경하고 run큐로 넣고 실행 대기한다.
						// 여기서 job state를 Running으로 변경해야지 중복실행되는것을 방지할 수 있다.
						
						/*
						 * 2013-01-02.
						 * 병렬제한그룹이 설정된 경우에는 current running 을 체크하는 부분에서 serial 하게 쿼리할 필요가 있다.
						 * 그렇지 않으면 이중화 환경에서는 max + 1 개 만큼 실행될 가능성이 있다.
						 * 
						 * 두 노드가 동시에 setJobStateForStart를 호출하고 그 쿼리 내에서 공교롭게도 select count(*) 를 하게 되는 경우는
						 * 쿼리시에는 max-1 이 되는 경우도 발생한다.
						 * 이런 경우는 이 두 노드가 동시에 동일 PG 의 JobIns 에 대해 update 를 하게 되므로 max+1 만큼 실행되는 경우가 발생할 수 있다.
						 * 
						 * 따라서 병렬제한그룹이 설정된 경우는 켜져있는 경우는 autocommit 으로 하지 않고 수동으로 트랜잭션을 걸어서
						 * (for update 이용) 
						 * 
						 * 동일 PG 에 대해서는 동시에 select count(*) current running 이 일어나지 않도록 한다.
						 * 아래에서 setJobStateForStart() 가 이중화 환경에서도 serial 하게 일어나기 위해 아래와 같이 복잡하게 한다. 
						 */

						boolean jobStateUpdateOkFlag = false;
						try {
							int pgMax = 0;
							if (!Util.isBlank(jobins.getParallelGroup())) { 
								controllerMain.getSqlMapClient().startTransaction(); // 병렬제한그룹이 존재할 경우 트랜잭션을 건다.
								pgMax = parallelRunningCounter.getParallelGroupMaxWithLock(jobins.getParallelGroup());
							}
							
							jobStateUpdateOkFlag = jobInstanceManager.setJobStateForStart(jobins.getJobInstanceId(), System.currentTimeMillis(), "Before job execution", jobins.getJobState(), jobins.getParallelGroup(), pgMax, jobins.getLastModifyTime());
						} catch (Throwable e) {
							Util.logError(log, MSG.get("main.jobstarter.decisionqueue.error", jobinsId)+"-1", e); // {0}의 기동 판단 중 에러가 발생하였습니다
						}finally {
							if (!Util.isBlank(jobins.getParallelGroup())) {
								try {
									controllerMain.getSqlMapClient().commitTransaction(); // commit
								}catch(Exception e) {
									Util.logError(log, "[DecisionQueueThread] commitTransaction() error", e);
								}
								try {
									controllerMain.getSqlMapClient().endTransaction();    // 병렬제한그룹이 존재할 경우 트랜잭션을 걸었던 것을 종료한다.
								}catch(Exception e) {
									Util.logError(log, "[DecisionQueueThread] endTransaction() error", e);
								}
							}
							
							if (jobStateUpdateOkFlag) {
								// runQueue로 넣어서 실행 대기 상태로 바꾼다.
								runQueueCache.put(jobins.getJobInstanceId(), jobins);
								runQueue.put(jobins);
							}else {
								// 여기서 doCheck()를 만족시킨 직후에 , forceRun을 누군가 하면..이와 같은 문제가 발생할 수 있다.
								// 이중화 환경에서 doCheck() 만족후에 다는 노드에서 먼저 setJobStateForStart() 를 호출하면 이렇게 될 수 있다.
								// 에러가 아니고 정상적인 상황임. 
								String newJobState = jobInstanceManager.getJobState(jobins.getJobInstanceId());
								if (JobInstance.JOB_STATE_RUNNING.equals(newJobState)) {
									// peer 에서 먼저 Running 시킨 경우이다. 정상적인 상황이다.
									Util.logInfo(log, "[DecisionQueueThread] ("+jobins.getJobInstanceId()+") started at peer.");
								}else {
									// 이 경우는 병렬제한그룹 때문에 wait 해야하는 상황일 수 있다.
									// 만일 병렬제한 그룹때문에 R 상태 변경이 실패한 것이라면, 
									// parallelJobWaitingPool 에도 등록해주어야 한다. 아래 checkParallelMax 안에서 이 일이 일어난다. 2012-12-13 
									jobRunConditionChecker.checkParallelMax(jobins);
								}
							}
						}
					}
				} catch (Throwable e) {
					Util.logError(log, MSG.get("main.jobstarter.decisionqueue.error", jobinsId)+"-2", e); // {0}의 기동 판단 중 에러가 발생하였습니다
				}
			}
			
		}
	}
	
	class RunQueueThread extends Thread {
		private int seq;
		
		public RunQueueThread(int seq) {
			super("JobStarter.RunQueueConsumer-"+seq);
		}
		
		public int getSeq() {
			return seq;
		}

		public void setSeq(int seq) {
			this.seq = seq;
		}

		public void run() {
			// runQueue를 보면서 실제 실행을 수행함.
			while(!destroyed) {
				JobInstance jobins;
				try {
					jobins = runQueue.take();
					runQueueCache.remove(jobins.getJobInstanceId());
				} catch (InterruptedException e) {
					continue;
				}

				/*
				 * TimeScheduler 에서 Job 실행시에는 병렬제한그룹 기능으로 인해 updateStateForStart() 에서 R 상태변경 실패를 할 수 있으므로,
				 * Decision 과정에서 NEW JOB EXE ID를 생성하지 않고, 여기 RunQueueThread 에서 생성한다.
				 * 어차피 DB에는 지금 "-" 값이 들어있게 된다. (Decision 후 RunQueue 에서 꺼내질때까지) 
				 */
				
				String jobExeId = controllerMain.getJobExecutionIdMaker().makeJobExecutionId(jobins.getJobInstanceId());
				jobins.setLastJobExeId(jobExeId);

				// JobExecution 을 생성하여 Job 실행함.
				JobExecution je = new JobExecution();
				try {
					// operator id, ip 정보 획득
					je.setJobId(            jobins.getJobId());
					je.setJobInstanceId(    jobins.getJobInstanceId());
					je.setProcDate(         jobins.getProcDate());
					je.setBaseDate(         jobins.getBaseDate());
					je.setRunCount(         jobins.getRunCount()+1);
					je.setJobExecutionId(   jobins.getLastJobExeId());
//					je.setAgentNode(        jobins.getAgentNode()); // 에이전트 이중화를 위해 여기서 에이전트 세팅을 하지않고 orderJobExecutionToAgent 안에서 한다. since 3.9
					je.setComponentName(    jobins.getComponentName());
					je.setJobType(          jobins.getJobType());
					je.setInParameters(     jobins.getInParameters());
					je.setLogLevel(         jobins.getLogLevel());
					je.setOperatorType(     "SCH");
					je.setOperatorId(       systemAdminAuth.getOperatorId());
					je.setOperatorIp(       systemAdminAuth.getOperatorIp());
					je.setDescription(      jobins.getDescription());
					je.setJobGroupId(       jobins.getJobGroupId());

					controllerMain.orderJobExecutionToAgent(jobins, je);
				}catch(Throwable e) {
					Util.logError(log, MSG.get("main.jobstarter.runqueue.error", je.getJobExecutionId()), e); // RunQueue 에서 {0} 를 기동하는 중 에러가 발생하였습니다
				}
			}
		}
	}
	
	public void init() {
		log = LogManager.getSchedulerLog();
		
		queueClosed         = false;
		decisionQueue       = new LinkedBlockingQueue<String>();   // decison 용 큐는 무한대로 둔다. 
		runQueue            = new LinkedBlockingQueue<JobInstance>(10); // 실행 큐는 최대 10개 정도만 받는다. 그이상 쌓이면 뭔가 문제가 있다.
		
		decisionQueueCache  = new ConcurrentHashMap();
		runQueueCache       = new ConcurrentHashMap();
		
		// decisionQueue consumer 생성
		decisionQueueThread = new DecisionQueueThread();
		decisionQueueThread.start();

		// runQueue consumer 생성. 멀티
		runQueueThreadMap = new HashMap<Integer, RunQueueThread>();
		for (int i=1; i<=runQueueThreadCount; i++) {
			RunQueueThread runQueueThread = new RunQueueThread(i);
			runQueueThreadMap.put(i, runQueueThread);
			runQueueThread.start();
		}
		Util.logServerInitConsole("JobStarter", "(RQ:"+runQueueThreadCount+")");
	}
	
	public void destroy() {
		destroyed = true;
		decisionQueueThread.interrupt();
		
		for (RunQueueThread t : runQueueThreadMap.values()) {
			t.interrupt();
		}
	}
	
	
	public JobInstanceManager getJobInstanceManager() {
		return jobInstanceManager;
	}

	public void setJobInstanceManager(JobInstanceManager jobInstanceManager) {
		this.jobInstanceManager = jobInstanceManager;
	}

	public ControllerMain getControllerMain() {
		return controllerMain;
	}

	public void setControllerMain(ControllerMain controllerMain) {
		this.controllerMain = controllerMain;
	}

	public JobRunConditionChecker getJobRunConditionChecker() {
		return jobRunConditionChecker;
	}

	public void setJobRunConditionChecker(JobRunConditionChecker jobRunConditionChecker) {
		this.jobRunConditionChecker = jobRunConditionChecker;
	}

	public ParallelRunningCounter getParallelRunningCounter() {
		return parallelRunningCounter;
	}

	public void setParallelRunningCounter(ParallelRunningCounter parallelRunningCounter) {
		this.parallelRunningCounter = parallelRunningCounter;
	}

	public int getRunQueueThreadCount() {
		return runQueueThreadCount;
	}

	public void setRunQueueThreadCount(int runQueueThreadCount) {
		this.runQueueThreadCount = runQueueThreadCount;
	}

	public boolean isQueueClosed() {
		return queueClosed;
	}

	public void setQueueClosed(boolean queueClosed) {
		this.queueClosed = queueClosed;
	}

	
	/**
	 * 이 JobInstance 가 지금 실행될 조건인지 판단하도록 의뢰함.
	 */
	public boolean askToStart(List<JobInstance> jobinsList) {
		if (jobinsList == null) {
			return true;
		}
		
		for (JobInstance jobins : jobinsList) {
			if (!askToStart(jobins.getJobInstanceId())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 이 JobInstance 가 지금 실행될 조건인지 판단하도록 의뢰함.
	 * @param jobins
	 */
	public boolean askToStart(JobInstance jobins) {
		return askToStart(jobins.getJobInstanceId());
	}
	
	/**
	 * 이 JobInstance 가 지금 실행될 조건인지 판단하도록 의뢰함.
	 * @param jobins
	 */
	public boolean askToStart(String jobinsId) {
		if (queueClosed) { // 큐를 닫은 경우.
			return false;
		}
		try {
			if (decisionQueueCache.put(jobinsId, new Long(System.currentTimeMillis()))==null) { // 이미 큐에 대기중인 Job Ins ID를 다시 큐에 넣을 필요없다.
				decisionQueue.put(jobinsId);
			}
			return true;
		}catch(InterruptedException e){
			return false; // 이 경우는 shutdown 과정에서 일어날수 있겠다. 
		}
	}
	
	public int getDecisionQueueSize() {
		return decisionQueue.size();
	}

	public int getRunQueueSize() {
		return runQueue.size();
	}
	
	/**
	 * JobInstance 가 runQueue 에서 대기중인가? 
	 * Ghost 여부 체크시에 사용된다.
	 * @param jobInstanceId
	 * @return
	 */
	public boolean existsInRunQueue(String jobInstanceId) {
		return runQueueCache.containsKey(jobInstanceId);
	}
	
	public String getDisplayName() {
		return "Main Queue";
	}
	
	public String getDisplayString() {
		return "Decision Q : "+decisionQueue.size()+", Run Q : "+runQueue.size()+", Run Q thread : "+runQueueThreadMap.size();
	}


}
