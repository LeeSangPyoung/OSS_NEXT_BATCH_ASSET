package nexcore.scheduler.core.internal;

import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.ibatis.sqlmap.client.SqlMapClient;

import nexcore.scheduler.controller.ha.IPeerClient;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.SchedulerUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Instance 중에서 시각 조건에 따라 invoke 대기함. </li>
 * <li>작성일 : 2010. 5. 3.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// 스레드로 동작하면서 1 분에 한번씩 Job Instance 들을 scan 하면서 launch 시킴. 
public class TimeScheduler implements Runnable {
	private boolean                  enable = true; // false 일 경우 스케줄러 돌지 않고 헛바퀴 돈다.
	private JobInstanceManager       jobInstanceManager;
	private JobStarter               jobStarter;
	private ParallelJobWaitingPool   parallelJobWaitingPool;
	private PreJobWaitingPool        preJobWaitingPool;
	private JobRunConditionChecker   jobRunConditionChecker;
	private DailyActivator           dailyActivator;  
	private SqlMapClient             sqlMapClient;
	private IPeerClient              peerClient;
	
	private Thread thisThread;
	private Log    log;
	
	private boolean destroyed;
	
	public void init() {
		log = LogManager.getSchedulerLog();
		
		thisThread = new Thread(this, "TimeScheduler");
		thisThread.setDaemon(true);
		thisThread.start();
		Util.logServerInitConsole("TimeScheduler", "("+String.valueOf(enable)+")");
	}
	
	public void destroy() {
		destroyed = true;
		thisThread.interrupt();
		File startLockFile = new File(System.getProperty("NEXCORE_HOME")+"/etc/start.lock");
		startLockFile.delete();
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public JobInstanceManager getJobInstanceManager() {
		return jobInstanceManager;
	}

	public void setJobInstanceManager(JobInstanceManager jobInstanceManager) {
		this.jobInstanceManager = jobInstanceManager;
	}

	public JobStarter getJobStarter() {
		return jobStarter;
	}

	public void setJobStarter(JobStarter jobStarter) {
		this.jobStarter = jobStarter;
	}

	public ParallelJobWaitingPool getParallelJobWaitingPool() {
		return parallelJobWaitingPool;
	}

	public void setParallelJobWaitingPool(ParallelJobWaitingPool parallelJobWaitingPool) {
		this.parallelJobWaitingPool = parallelJobWaitingPool;
	}

	public PreJobWaitingPool getPreJobWaitingPool() {
		return preJobWaitingPool;
	}

	public void setPreJobWaitingPool(PreJobWaitingPool preJobWaitingPool) {
		this.preJobWaitingPool = preJobWaitingPool;
	}

	public JobRunConditionChecker getJobRunConditionChecker() {
		return jobRunConditionChecker;
	}

	public void setJobRunConditionChecker(JobRunConditionChecker jobRunConditionChecker) {
		this.jobRunConditionChecker = jobRunConditionChecker;
	}
	
	public DailyActivator getDailyActivator() {
		return dailyActivator;
	}

	public void setDailyActivator(DailyActivator dailyActivator) {
		this.dailyActivator = dailyActivator;
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public IPeerClient getPeerClient() {
		return peerClient;
	}

	public void setPeerClient(IPeerClient peerClient) {
		this.peerClient = peerClient;
	}
	
	
	

	public void run() {
		while(!destroyed) {
			try {
				SchedulerUtil.checkStartedWithNoRun();
			}catch(Exception e) {
				// norun 이면 메세지 찍고 건너뛴다.
				Util.logInfo(log, "[TimeScheduler] "+e.getMessage());
				Util.sleep(60*1000); // 100 초 쉬었다가 다시 체크해본다. 스케줄러 reboot 없이 RUN 모드로 동작 가능하도록 하기위함. 
				continue;
			}
			
			try {
				_run();
			}catch (Throwable e) {
				try {
					Util.logError(log, MSG.get("main.timesch.fatal.error"), e); // 타임 스케줄러 실행 중 심각한 에러가 발생하였습니다. 잠시 sleep 후 계속 실행합니다
					// OOME 같은 비상 에러 나도 계속 돌아야한다.
					Util.sleep(30*1000); // 30 초 쉬었다가 다시 돌리도록 한다.
				}catch(Throwable ee) {
					ee.printStackTrace();
					// 에러 무시하고 계속 돌게 한다.
				}
			}
		}
	}
	
	private void _run() {
		while(!destroyed) {
			sleep(); // next minute 00 초까지 sleep
			
			long   currentTime = System.currentTimeMillis();
			
			if (!enable) {
				log.info(MSG.get("main.timesch.disabled")); // 타임 스케줄러가 비활성화 되었습니다 
				continue; // enable=false 일 경우는 timescheduler 가 동작하지 않고 그냥 헛바퀴 돈다.
			}
			
			/* 
			 * 이중화 기능 보강. 2012-11-01.
			 * 이중화를 위해 복수 노드에서 동시에 TimeScheduler 가 돌아가는 것을 방지하기 위해 로그 테이블을 활용함.
			 * 로그 insert 가 성공한 경우에만 아래 메인 로직 수행됨
			 * 로그 insert 실패는 동시간 대에 다른 노드에서 먼저 아래 로직을 수행했다는 상황이 된다.
			 * 이런 경우는 그냥 skip 한다.
			 */
			boolean insertOk = insertTimeSchedulerLog(currentTime);
			if (!insertOk) {
			    if (peerClient.isPeerExist()) {
			        Util.logDebug(log, "[TimeScheduler] TimeScheduler log insert fail. skip");
			    }else {
			        /*
			         * 이중화 환경이 아니면서 insert 실패하는 것은 또다른 스케줄러가 동일한 DB를 보고 있을 수 있다는 뜻이다.
			         * 중대 오류 상황이다. 에러 메세지 로깅하고 console 에도 뿌린다.
			         */
			        Util.logErrorConsole("[TimeScheduler] "+MSG.get("main.dup.timeschlog.detected.error"));
			        Util.logError(log, "[TimeScheduler] "+MSG.get("main.dup.timeschlog.detected.error"));
			    }
			    
				continue;
			}
			
			/*
			 * DailyActivator 쓰레드와 통합. 2013-02-17.
			 * 메인 로직 시작하기 전에 DailyActivation 시각인지 체크해서 지금이 그 시각이면 DailyActivation 을 먼저 한후에 메인 로직 시작한다.
			 * 기존 방식은 DailyActivator, TimeScheduler 가 별개로 수행되었음. DailyActivator 로 인해 00:01 에 인스턴스 생성후에는
			 * 01분대는 이미 TimeScheduler 가 돌아간 이후가 되므로 02 분까지 최초 실행을 기다려야한다. 
			 * checkImmediately() 를 호출하더라도, 이중화 이후에는 TimeSchedulerLog 방식을 취하고 있으므로 이미 01 분대에 로그가 생성되어있다면 01분대에는
			 * 절대로 TimeScheduler 의 메인로직이 다시 실행되지 않는다. 
			 * 
			 * 이런 문제를 해결하기 위해 TimeScheduler 스레드에서 DailyActivation 도 수행하도록 수정한다.
			 * TimeScheduler 에서 현재 시각이 activationTime 이라면 DailyActivation 을 수행한 후에 TimeScheduler 의 메인 로직을 수행한다.
			 * 
			 * 이중화 환경에서, DailyActivation 수행중에 peer 가 TimeScheduler 를 수행하게 되는 문제를 해결하기 위해 
			 * DailyActivation 도 inserTimeSchedulerLog() 성공 이후에 한다. 
			 * 
			 * activation 수행 진행 중에 peer 에서 TimeScheduler 가 중간에 실행되게 되면, 
			 * 선행 조건 INS_NOTEXIST 가 만족하는 오동작이 발생할 수도 있기 때문에
			 * 항상 DailyActivation 이 모두 완료된 후에 TimeScheduler 가 돌도록 해야한다. 
			 */
			if (dailyActivator.isEnable()) {
				if (Util.getHHMM(currentTime).equals(dailyActivator.getActivationTime())) {
					dailyActivator.doDailyActivationProcess(Util.getCurrentYYYYMMDD(), log);
				}else if (Util.getHHMM(currentTime - 60*1000).equals(dailyActivator.getActivationTime())) {
					/*
					 * activationTime + 1분 후에도 dailyActivation 시도한다.
					 * activationTime 직전에 시스템의 시각이 변경되는 경우, 
					 * 그로 인해 activationTime 이 skip 되어 activation 이 수행이 안 될 수 있으므로 
					 * 그 다음 1분에도 다시 dailyActivation 수행해본다. 
					 */
					dailyActivator.doDailyActivationProcess(Util.getCurrentYYYYMMDD(), log);
				}
			}
			
			Util.logInfo(log, MSG.get("main.timesch.begin")); // 타입 스케줄러 체크 시작합니다
			
//			String today     = Util.getYYYYMMDD(currentTime);            // 오늘
//			String yesterday = Util.getYYYYMMDD(currentTime-86400000);   // 어제
//			
			// 1. 어제 오늘 activation 된 건들에 대해서만 CONDITION CHECK 한다.
			// 2. 상태가 Init, Wait 인 것들에 대해서 실행 여부 체크한다.
			List<JobInstance> jobInsList = null;
			try {
				// 여기서는 param, prejob 정보를 체크하지 않으므로 deep query 필요없다. DecisionQueueConsumer 가 다시 Deep query 할 것이므로 여기서는 하지 않는다.
				jobInsList = jobInstanceManager.getJobInstancesByQuery(  
//						"WHERE (ACTIVATION_TIME like '"+today+"%' OR ACTIVATION_TIME like '"+yesterday+"%') " + // 2012/11/13. 오래된 것을 Expire 처리되므로 activation 날짜로 다시한번 거르지 않도록 한다.
						"WHERE JOB_STATE IN ('I', 'W')" +
						"  AND LOCKED_BY IS NULL ", "");
			}catch (SQLException e) {
				Util.logError(log, MSG.get("main.jobins.getlist.error"), e); 
				continue; // DB 에러나면 그냥 쉰다.
			}
			
//			if (log.isDebugEnabled()) {
//				// RUN CHECK 대상 디버그 로그.
//				StringBuilder sb = new StringBuilder(128);
//				sb.append("[");
//				for (JobInstance jobIns : jobInsList) {
//					sb.append(jobIns.getJobInstanceId());
//					sb.append(",");
//				}
//				if (sb.length() > 1) {
//					sb.deleteCharAt(sb.length()-1); // 맨마지막 , 떼어내기.
//				}
//				sb.append("]");
//			}
			
			// doCheckSimple 에서 사용할 날짜 값.
			String date1 = Util.getYYYYMMDD(currentTime);          // 오늘
			String date2 = Util.getYYYYMMDD(currentTime-86400000); // 어제
			
			String currentHHMM1 = Util.getHHMM(currentTime);        // 오늘 현재시각
			String currentHHMM2 = String.valueOf(Integer.parseInt(currentHHMM1) + 2400);         // 어제 기준 25시 표기
			
			boolean isPeerAlive = peerClient.isAlive(); // peer 가 동작중이면, 분산한다 
			boolean peerFlip = true;
			
			Set<String> idListForPreJobPoolCleansing = new HashSet(jobInsList.size());;  // 2013.10.02. PreJobWaitPool cleansing 로직 개선. #21614
			for (JobInstance jobins : jobInsList) {
				// 여기서, 시각, Confirm, Lock, MaxOK 등등 jobins 객체 만으로 메모리에서 한번 체크할 수 있는 항목들은 여기서 체크해서 걸러낸다.
				// 일단 ask를 하고 나면 DecisionQueueConsumer가 JobInstance를 다시 DB에서 읽으므로 
				if (jobRunConditionChecker.doCheckSimple(jobins, date1, date2, currentHHMM1, currentHHMM2)) {
					idListForPreJobPoolCleansing.add(jobins.getJobId()+"_"+jobins.getProcDate());
					if (isPeerAlive) {
						peerFlip = !peerFlip;
						if (peerFlip) {
							try {
								peerClient.askToStart(jobins.getJobInstanceId()); // 절반은 peer 에게 보내서 그쪽에서 decision 이 되도록 한다.
								continue;
							}catch(Throwable e) { // 에러가 발생하면 peer로 전송하지 말고 내 node 에게 나머지 모두 decision 시킨다.
								Util.logError(log, "peerClient.askToStart() fail. "+jobins.getJobInstanceId(), e);
								isPeerAlive = false;
							}
						}
					}
					jobStarter.askToStart(jobins);
				}
			}
			Util.logInfo(log, MSG.get("main.timesch.end", jobInsList.size())); // 타입 스케줄러 체크 종료합니다
			
			try {
				preJobWaitingPool.doCleansing(idListForPreJobPoolCleansing);
				parallelJobWaitingPool.doCleansing(idListForPreJobPoolCleansing);
				if (isPeerAlive) {
					peerClient.cleansingWaitingPool(idListForPreJobPoolCleansing);
				}
			}catch(Throwable e) {
				Util.logWarn(log, "[TimeScheduler] WaitPool doCleansing()", e); 
				// 무시하고 계속 돌린다.
			}
		}
	}
	
	/**
	 * next minute's 00 초 까지 sleep 한다.
	 */
	private void sleep() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 1);
		cal.set(Calendar.SECOND, 0);
		
		long sleepTime = cal.getTime().getTime() - System.currentTimeMillis();

		Util.sleep(sleepTime, true);
	}
	
	public boolean isAlive() {
		return thisThread.isAlive();
	}
	
	/**
	 * 1분마다 TimeScheduler가 동작을 시작하기 직전에 먼저 로그를 insert 하여 정상일 경우만 본 작업을 수행하도록 한다.
	 * 이중화 환경에서 동시에 돌지못하도록 하기 위해 이런 방식을 이용한다.
	 * 동시간 대에 먼저 insert 한 놈만 본 작업을 수행할 수 있고 나중에 insert 하는 노드는 PK DUP 에러가 나서 본 작업을 수행하지 못하게 처리한다.
	 * 
	 * @param current
	 * @return
	 */
	private boolean insertTimeSchedulerLog(long current) {
		Map sqlin = new HashMap();
		sqlin.put("systemId",       Util.getSystemId());
		sqlin.put("lastModifyTime", DateUtil.getTimestampString(current));
		try {
			int cnt = sqlMapClient.update("nbs.scheduler.insertTimeSchedulerLog", sqlin);
			return cnt == 1; // 로그 insert 가 성공하면 true 리턴한다. 
		}catch(Exception e) {
			// 로그 insert 실패하면 false 리턴한다.
			if (!(e instanceof SQLException)) {
				Util.logError(log, "Insert TimeScheduler log fail.", e);
			}
			return false;
		}
	}
}
