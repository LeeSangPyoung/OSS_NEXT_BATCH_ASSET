package nexcore.scheduler.core.internal;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.ibatis.sqlmap.client.SqlMapClient;

import nexcore.scheduler.core.IMonitorDisplayable;
import nexcore.scheduler.core.IScheduleCalendar;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  매일 정해진 시각에 당일자 인스턴스를 생성하는 데몬</li>
 * <li>작성일 :  </li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class DailyActivator implements /* Runnable, */ IMonitorDisplayable {
	private static DailyActivator instance; // DailyActivatorJob 에서 이 객체를 호출하기 위해 singleton 으로 만든다.
	
	private boolean              enable;
	private JobDefinitionManager jobDefinitionManager;
	private DayScheduler         dayScheduler;
	private IScheduleCalendar    scheduleCalendar;
	private Activator            activator;
	private String               activationTime;    // HHMM 타입으로 activation 타입을 지정함.
	private SqlMapClient         sqlMapClient;
	
//	private Thread    thisThread;
	private Log       schedulerLog;
//	private boolean   shouldStop;
	
	private String    systemId;
	
	public DailyActivator() {
	}

	public static DailyActivator getInstance() {
		return instance;
	}
	
	public void init() {
		instance     = this;
		schedulerLog = LogManager.getSchedulerLog();
		systemId     = System.getProperty("NEXCORE_ID");
		
		checkLogTableConstraint();
		
//		thisThread = new Thread(this, "DailyActivator");
//		thisThread.setDaemon(true);
//		thisThread.start();
		Util.logServerInitConsole("DailyActivator", "("+activationTime+","+enable+")");
	}
	
	public void destroy() {
//		shouldStop = true;
//		thisThread.interrupt();
	}
	
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public JobDefinitionManager getJobDefinitionManager() {
		return jobDefinitionManager;
	}

	public void setJobDefinitionManager(JobDefinitionManager jobDefinitionManager) {
		this.jobDefinitionManager = jobDefinitionManager;
	}

	public DayScheduler getDayScheduler() {
		return dayScheduler;
	}

	public void setDayScheduler(DayScheduler dayScheduler) {
		this.dayScheduler = dayScheduler;
	}

	public IScheduleCalendar getScheduleCalendar() {
		return scheduleCalendar;
	}

	public void setScheduleCalendar(IScheduleCalendar scheduleCalendar) {
		this.scheduleCalendar = scheduleCalendar;
	}

	public String getActivationTime() {
		return activationTime;
	}
	public void setActivationTime(String activationTime) {
		this.activationTime = activationTime;
	}
	public Activator getActivator() {
		return activator;
	}
	public void setActivator(Activator activator) {
		this.activator = activator;
	}
	
	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

//  TimeScheduler 쓰레드에서 수행하는 것으로 변경. 2013.2.18
//	public void run() {
//		while(!Thread.interrupted() && !shouldStop) {
//			try {
//				sleep();
//	
//				if (shouldStop) {
//					break;
//				}
//				
//				if (!enable) { // disable 상태에선 pass 시킨다.
//					Util.logInfo(schedulerLog, MSG.get("main.dailyact.disabled"));
//					Util.sleep(100 * 1000); // 100 초 쉬고 continue 한다. 이렇게 하는 이유는 분(minute)이 바뀔때까지 쉬기 위해.
//					continue;
//				}
//	
//				// 위 sleep() 에서 비정상적으로 먼저 깨어날 경우를 대비하기 위해 지금이 activation 시간이 맞는지 다시한번 확인
//				String currHHMM = Util.getCurrentYYYYMMDDHHMMSS().substring(8, 12);
//				if (!currHHMM.equals(activationTime)) {
//					Util.logWarn(schedulerLog, "Current time ("+currHHMM+"), activation time ("+activationTime+") different");
//					continue;
//				}
//				
//				String procDate = Util.getCurrentYYYYMMDD();
//	
//				doDailyActivationProcess(procDate, schedulerLog);
//				
//				// ■ 다음 분으로 넘어가기를 기다리기 위해 60초 쉰다.
//				Util.sleep(60 * 1000);
//			}catch(Throwable e) {
//				Util.logError(schedulerLog, MSG.get("main.dailyact.activation.error"), e); // 인스턴스 생성 작업 중 에러가 발생하였습니다.
//				try {
//					Util.sleep(20 * 1000); // 위에서 비정상적인 에러로 인해 무한 루프에 빠지는 것을 방지하기 위해 20초 sleep한다.
//				}catch(Exception ee) {
//					if (shouldStop) {
//						break; // shouldStop 으로 인해 Exception 이 발생한것이라면 printStackTrace 없이 조용히 죽는다.
//					}else {
//						ee.printStackTrace();
//						break;
//					}
//				}
//			}
//		}
//	}
	
//	private void sleep_old() {
//		// activation 해야하는 시각을 계산한다
//		Calendar activationCal = Calendar.getInstance();
//		activationCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(activationTime.substring(0,2)));
//		activationCal.set(Calendar.MINUTE,      Integer.parseInt(activationTime.substring(2,4)));
//		activationCal.set(Calendar.SECOND,      0);
//			
//		Calendar current = Calendar.getInstance();
//		if (current.compareTo(activationCal) > 0) {
//			// 이미 activation 시각이 지난 경우 다음날 activation 시각으로 다시 계산
//			activationCal.add(Calendar.DATE, 1);
//		}
//		Util.logInfo(log, MSG.get("main.dailyact.next.activation.time", activationCal.getTime()));  // 다음 인스턴스 생성시각은 {0} 입니다 
//
//		// activation 시각까지 sleep 한다. 한번 sleep 하지 않고 50초마다 계속 깨어나 다시 시간을 체크한다.
//		while(true) {
//			current = Calendar.getInstance();
//			long sleepTime = activationCal.getTime().getTime() - current.getTime().getTime();
//			Util.logDebug(log, "[DailyActivator] sleep time 1 : "+sleepTime);
//
//			if (sleepTime < 0) {
//				// loop 도는 중에 시스템 일자가 변경되어 activationCal 일자보다 앞서는 경우, activationCal을 변경해야한다.
//				// 여기서는 그냥 return 하고 위 run() 에서 에러 로그후 다시 sleep 하도록 유도한다.
//				return;
//			}
//			
//			// 한번에 최대 50 초 까지만 sleep 하고 다시 계산한다.
//			sleepTime = Math.min(sleepTime, 50 * 1000);  
//			Util.logDebug(log, "[DailyActivator] sleep time 2 : "+sleepTime);
//			try {
//				Thread.sleep(sleepTime);
//			} catch (Exception e) {
//				if (!shouldStop) {
//					Util.logInfo(log, "[DailyActivator] "+this+" interrupted.", e);
//				}
//			}
//
//			// 위 sleep() 깨어난 시각이 설정된 activationTime 과 일치하는지 체크하고 일치할 경우 리턴한다.
//			String currHHMM = Util.getCurrentYYYYMMDDHHMMSS().substring(8, 12);
//			if (currHHMM.equals(activationTime)) {
//				if (log.isDebugEnabled()) {
//					Util.logDebug(log, "[DailyActivator] Curr:"+currHHMM+",activationTime:"+activationTime+" match !!");
//				}
//				break;
//			}else {
//				if (log.isDebugEnabled()) {
//					Util.logDebug(log, "[DailyActivator] Curr:"+currHHMM+",activationTime:"+activationTime+" not match");
//				}
//			}
//			
//		}
//	}

//  TimeScheduler 쓰레드에서 수행하는 것으로 변경. 2013.2.18
//	/**
//	 * 다음 Activation 시각 까지 sleep 한다.
//	 * 
//	 */
//	private void sleep() {
//		// activation 해야하는 시각을 계산한다
//		Calendar activationCal = Calendar.getInstance();
//		activationCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(activationTime.substring(0,2)));
//		activationCal.set(Calendar.MINUTE,      Integer.parseInt(activationTime.substring(2,4)));
//		activationCal.set(Calendar.SECOND,      0);
//			
//		Calendar current = Calendar.getInstance();
//		if (current.compareTo(activationCal) > 0) {
//			// 이미 activation 시각이 지난 경우 다음날 activation 시각으로 다시 계산
//			activationCal.add(Calendar.DATE, 1);
//		}
//		Util.logInfo(schedulerLog, MSG.get("main.dailyact.next.activation.time", activationCal.getTime()));  // 다음 인스턴스 생성시각은 {0} 입니다 
//
//		/*
//		 *  activation 시각까지 sleep 한다. 한번 sleep 하지 않고 50초마다 계속 깨어나 다시 시간을 계산 한다.
//		 *  한번만 sleep 할 경우 sleep 하는 중에 시스템 시각이 변경될 경우, 정상적인 처리가 안되는 문제가 있으므로 (윤초, NTP 등)
//		 *  중간중간 계속 awake 하여 계속 sleep 시각을 계산한다.
//		 */
//		while(true) {
//			String currHHMMSS = Util.getCurrentYYYYMMDDHHMMSS().substring(8, 14);
//			String currHHMM   = currHHMMSS.substring(0,4);
//			if (activationTime.equals(currHHMM)) {
//				if (schedulerLog.isDebugEnabled()) {
//					Util.logDebug(schedulerLog, "[DailyActivator] Curr:"+currHHMM+",activationTime:"+activationTime+" match !!");
//				}
//				break;
//			}
//			
//			long sleepTime = 0;
//			
//			/*
//			 *  activationTime 1 분전인 경우 초단위까지 정확한 sleep 시간을 계산한다.
//			 *  0000 일 경우에만 2359 와 0000 은 1분의 차이가 난다는 예외를 처리하며, 그 외의 경우는 예외처리할 필요가 없다 
//			 *  예) 0001 은 0000과 1분의 차이가 나므로 24시 처리가 필요없다.
//			 */
//			long diffSecond = Util.getDiffSecond(currHHMMSS, activationTime.equals("0000") ? "240000" : activationTime+"00"); 
//			if (0 <= diffSecond && diffSecond <= 60) { 
//
//				activationCal = Calendar.getInstance();
//				activationCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(activationTime.substring(0,2)));
//				activationCal.set(Calendar.MINUTE,      Integer.parseInt(activationTime.substring(2,4)));
//				activationCal.set(Calendar.SECOND,      0);
//				
//				current = Calendar.getInstance();
//				if (current.compareTo(activationCal) > 0) { // 다음날로 계산.
//					activationCal.add(Calendar.DATE, 1);
//				}
//				
//				sleepTime = activationCal.getTimeInMillis() - current.getTimeInMillis();
//				if (schedulerLog.isDebugEnabled()) 
//					Util.logDebug(schedulerLog, "[DailyActivator] activationTime : "+activationTime+", diff second : "+diffSecond+", sleep time exact : "+sleepTime);
//			}else {
//				sleepTime = 50000; // 1분 이상 남은 경우, 또는 이미 지나간 경우 (음수)는 50초씩 sleep 한다.
//				if (schedulerLog.isDebugEnabled())
//					Util.logDebug(schedulerLog, "[DailyActivator] activationTime : "+activationTime+", diff second : "+diffSecond+", sleep time 50s : "+sleepTime);
//			}
//
//			try {
//				Thread.sleep(sleepTime);
//			} catch (Exception e) {
//				if (shouldStop) {
//					return; // shutdown 진행중.
//				}else {
//					Util.logInfo(schedulerLog, "[DailyActivator] "+this+" interrupted.", e);
//				}
//			}
//		}
//	}

	/**
	 * Activation 로그를 Insert 함. 
	 * Activation 수행 직전에 insert 하고, Activation 이 끝나고 나면 결과 걸수를 Update 한다.
	 * 
	 * @param procDate
	 * @return
	 */
	private int insertActivationLog(String procDate) throws SQLException {
		/*
		 * 오동작으로 인해 Daily Activation 이 중복 실행이 되면 매우 큰 장애 상황이 되므로,
		 * 중복 실행이 되지 않도록 여러가지 장치를 해야한다.
		 * 
		 * doDailyActivationProcess() 를 하기 전에 먼저 이렇게 로그테이블에 Insert 를 하고, 
		 * 나중에 결과 건수를 Update 하는 방식으로 한다. 
		 * 
		 * PROC_DATE 가 PK 이므로 두번 이상 INSERT 되지 않는다.
		 * INSERT 실패시 dailyActivation 하지 않도록 한다.
		 */
		
		// 로그 insert 한다.
		Map param = new HashMap();
		
		param.put("systimestamp",  DateUtil.getCurrentTimestampString());
		param.put("procDate",      procDate);
		param.put("systemId",      systemId);
		param.put("jobInsCount",   -1);   // 먼저 -1 로 Insert 한다. 
		param.put("jobInsIdList",  "");
		
		return sqlMapClient.update("nbs.scheduler.insertActivationLog", param);
	}
	
	/**
	 * 당일자 Activation 결과를 update 한다.
	 * 위에서 먼저 Insert 되어있는 row 에 activation 결과를 update한다. 
	 * @param procDate
	 * @return
	 */
	private int updateActivationLog(String procDate, List jobInsIdList) throws SQLException {
		Map param = new HashMap();
		
		param.put("systimestamp",  DateUtil.getCurrentTimestampString());
		param.put("procDate",      procDate);
		param.put("systemId",      systemId);
		param.put("jobInsCount",   jobInsIdList.size());
		param.put("jobInsIdList",  Util.toString(jobInsIdList, ","));
		
		return sqlMapClient.update("nbs.scheduler.updateActivationLog", param);
	}

	private int deleteActivationLog(String procDate) throws SQLException {
		Map param = new HashMap();
		
		param.put("procDate",      procDate);
		
		return sqlMapClient.update("nbs.scheduler.deleteActivationLog", param);
	}

	private boolean selectActivationLogExist(String procDate) throws SQLException {
		String s = (String)sqlMapClient.queryForObject("nbs.scheduler.selectActivationLogExist", procDate);
		return "TRUE".equals(s);
	}
	
	/**
	 * Activation Log 테이블에 PK 가 제대로 걸려있는지 검증한다. 
	 * DailyActivation 이 중복되면 매우 큰 장애이므로, start 시점에 PK DUP 에러가 나는지 안나는지 검사해본다.
	 * 
	 * @throws SQLException
	 */
	private void checkLogTableConstraint() {
		/*
		 * 00000000 일자 로그를 지우고, 동일한 값으로 2번 insert 해본다.
		 */
		try {
			deleteActivationLog("00000000"); // 먼저 00000000 일자 로그를 지우고
		}catch(Exception e) {
			// 이 경우는 DB 에 뭔가 문제가 있다.
			throw new SchedulerException("com.error.occurred.while", e, "checking activation log table");
		}
		
		try {
			insertActivationLog("00000000");
		}catch(SQLException e) { // 두 노드가 동시에 start 될때는 여기서도 에러날 수 있다. 그냥 무시한다.
		}
		
		try {
			insertActivationLog("00000000");
			// 두번 정상 insert 된다는 것은 PK 가 안걸렸다는 뜻.
			throw new SchedulerException("main.dailyact.logtable.no.pk");
		}catch(SQLException e) {
			// PK DUP 에러일 경우는 정상.
			Util.logInfo(schedulerLog, "[DailyActivator] Activation Log table primary key ok.");
		}finally {
			try {
				deleteActivationLog("00000000"); // 먼저 00000000 일자 로그를 지운다.
			}catch(Exception e) {
			}
		}
	}
	
	/**
	 * JobDefinition 들로부터 스케줄 확인하여 JobInstance 만듬.
	 * @param procDate 생성할 JobInstance 의 ProcDate
	 * @param log scheduler log 또는 Job log
	 * @return 생성된 JobInstance 들의 Job Ins ID 리스트.
	 */
	private List<String> checkScheduleAndMakeJobInstances(String procDate, Log log) throws SQLException {
		List jobInstanceIdList = new LinkedList();
		List<JobDefinition> jobdefAll = null;
		
		jobdefAll = jobDefinitionManager.getJobDefinitionsByQuery(""); // 조건이 없으므로 full select
		
		// Job 하나하나 activation 체크.
		for (JobDefinition jobdef : jobdefAll) {
			try {
				if (dayScheduler.isScheduledDay(jobdef)) {
					jobdef = jobDefinitionManager.getJobDefinitionDeep(jobdef.getJobId()); // 파라미터와 선행조건을 포함해서 Deep으로 다시 읽는다.
					try {
						// 오늘 수행되어야할 Job 이므로 activate 함. 중간에 하나 실패해도 다른건 계속 생성해야하므로 건건이 TX 처리한다.
						sqlMapClient.startTransaction();
						JobInstance jobins = activator.activate(jobdef, procDate, null, new AdminAuth("DailyActivator", InetAddress.getLocalHost().getHostAddress()));
						jobInstanceIdList.add(jobins.getJobInstanceId());
						sqlMapClient.commitTransaction();
					}finally {
						sqlMapClient.endTransaction();
					}
				}
			}catch(Throwable e) {
				Util.logError(log, MSG.get("main.dailyact.activation.one.error", jobdef.getJobId()), e); // Job [] 인스턴스 생성 중에 에러가 발생하였습니다
			}
		}

		return jobInstanceIdList;
	}

	/**
	 * 일일 Activation 메인 로직.
	 * 스스로 스레드로 돌기도 하고, Job 에 의해 호출되기도 한다.
	 * @param procDate 처리일
	 * @param log scheduler log 또는 Job log
	 * @return 생성된 JobInstance 개수.
	 */
	public int doDailyActivationProcess(String procDate, Log log) {
		try {
			// ★ 인스턴스 만들기 시작한다.
			Util.logInfo(log, MSG.get("main.dailyact.begin.activation", procDate)); // PROC_DATE ({0}) 의 일간 인스턴스 생성 작업을 시작합니다
			if (schedulerLog != log) {
				Util.logInfo(schedulerLog, MSG.get("main.dailyact.begin.activation", procDate)); // PROC_DATE ({0}) 의 일간 인스턴스 생성 작업을 시작합니다
			}

			// ■ NBS_ACTIVATION_LOG 테이블에 이미 수행 여부 체크한다.
			if (selectActivationLogExist(procDate)) {
				// activation log 테이블에 동일 proc_date 로 내역이 있다. 
				Util.logInfo(log, MSG.get("main.dailyact.precheck.false")+" LOG EXIST"); // 오늘은 이미 자동 인스턴스 생성 작업을 수행하였습니다.
				return -1;
			}
			
			// ■ NBS_ACTIVATION_LOG 테이블에 로깅한다. 먼저 Insert 를 한 후에 다음 처리한다. (중복 생성 방지를 위해)
			try {
				insertActivationLog(procDate);
			}catch(Throwable e) {
				// 이미 peer node 에서 먼저 insert 했다면 insert fail 이 난다. 이럴 경우에 나는 일하지 않고 쉰다. (내일을 기약)
				Util.logInfo(log, MSG.get("main.dailyact.precheck.false")); // 오늘은 이미 자동 인스턴스 생성 작업을 수행하였습니다.
				return -1;
			}

			// ■ calendar 먼저 reload 한다.
			scheduleCalendar.reload();
			
			// ■ 오늘자 인스턴스를 만든다.
			List<String> jobInsIdList = checkScheduleAndMakeJobInstances(procDate, log);
			if (schedulerLog != log) {
				// Job 방식으로 DailyActivation 을 수행할때만 이렇게 로그를 쌓는다.
				// 데몬 방식에서는 이미 scheduler.log 에 Activator 에서 로그를 쌓고 있다.
				int i=0;
				int dlength = String.valueOf(jobInsIdList.size()).length();
				for (String jobInsId : jobInsIdList) {
					Util.logInfo(log, String.format("(%"+dlength+"d) %s created.", (++i), jobInsId));
				}
			}

			// ■ NBS_ACTIVATION_LOG 테이블에 DailyActivation 결과 로깅한다.
			int cnt = updateActivationLog(procDate, jobInsIdList);
			if (cnt != 1) {
				// 위에서 먼저 insert 한 것을 update 하는 것이므로 1 이 아닐 수 없다.
				log.warn("[DailyActivator] updateActivationLog result : "+cnt);
			}
			
			Util.logInfo(log, MSG.get("main.dailyact.end.activation", jobInsIdList.size())); // 인스턴스 생성 작업이 끝났습니다. 총 {0} 개의 인스턴스가 생성되었습니다.
			if (schedulerLog != log) {
				Util.logInfo(schedulerLog, MSG.get("main.dailyact.end.activation", jobInsIdList.size())); // 인스턴스 생성 작업이 끝났습니다. 총 {0} 개의 인스턴스가 생성되었습니다.
			}
			
			return jobInsIdList.size();
		}catch(Throwable e) {
			throw new SchedulerException("main.dailyact.activation.error", e); // 인스턴스 생성 작업 중 에러가 발생하였습니다.
		}
	}

//	public boolean isAlive() {
//		return thisThread.isAlive();
//	}

	public String getDisplayName() {
		return "DailyActivator";
	}
	
	public String getDisplayString() {
		return "["+enable+"] " +activationTime;
	}
}
