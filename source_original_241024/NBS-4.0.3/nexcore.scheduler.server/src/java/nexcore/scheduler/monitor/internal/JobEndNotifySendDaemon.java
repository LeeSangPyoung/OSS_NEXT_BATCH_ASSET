package nexcore.scheduler.monitor.internal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.ibatis.sqlmap.client.SqlMapClient;

import nexcore.scheduler.entity.JobNotifySendInfo;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.monitor.IJobEndNotifySender;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 9999 </li>
 * <li>설  명 : Job 종료 Notify 를 실제로 보내기 위해 NBS_NOTIFY_SEND_lIST 테이블을 폴링하여 WORKER들을 호출하는 놈</li>
 * <li>작성일 : 2011. 4. 7.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobEndNotifySendDaemon implements Runnable {
	private boolean      	                   enable;
	private SqlMapClient 	                   sqlMapClient;
	private int                                maxRetryCount    = 3;  // 에러시 최대 재실행 횟수. 기본값 3회
	private int                                retryIntervalMin = 1;  // 에러시 최대 재실행 인터벌 (분). 기본값 2분 
	private int                                maxRetryHour     = 1;  // 에러시 최대 재실행 구간 (시간). 기본값 1시간 
	private Map<String, IJobEndNotifySender>   senderMap;

	private JobEndNotifyNoSender               jobEndNotifyNoSender; // 아무일 안하고 상태만 X 로 바꿔주는 dummy sender.
	private Thread       	                   thisThread;
	private Log       		                   log;
	private boolean                            destroyed;
	
	private boolean                            autoDeleteOldNotify;    // 오래된 Notify List 를 자동으로 삭제할 것인지?
	private String                             keepPeriod    = "M1";   // [Dn] : n일 지난것 삭제, [Mn] : n월 지난것 삭제. 기본값:M1
	private String                             executionTime = "2100"; // HHMM 타입으로 실행 시각 지정. 기본값:21시
	
	private Calendar                           executionCal;		// 오래된 Notify List 자동 삭제하는 시간 (해당 시간이 되면 삭제루틴 동작)
	private boolean                            deleteOldNotify;     // true : delete, false : Skip
	
	public void init() {
		log = LogManager.getSchedulerLog();
		jobEndNotifyNoSender = new JobEndNotifyNoSender();
		thisThread = new Thread(this, "JobEndNotifySendDaemon");
		thisThread.setDaemon(true);
		thisThread.start();
		deleteOldNotify = true;
		
		if (autoDeleteOldNotify) {
			executionCal = Calendar.getInstance();
			executionCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(executionTime.substring(0,2)));
			executionCal.set(Calendar.MINUTE,      Integer.parseInt(executionTime.substring(2,4)));
			executionCal.set(Calendar.SECOND,      0);
		}
		Util.logServerInitConsole("JobEndNotifySendDaemon", String.valueOf(senderMap)+", Auto Del:("+autoDeleteOldNotify+","+keepPeriod+","+executionTime+")");
	}
	
	public void destroy() {
		destroyed = true;
		thisThread.interrupt();
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	public int getRetryIntervalMin() {
		return retryIntervalMin;
	}

	public void setRetryIntervalMin(int retryIntervalMin) {
		this.retryIntervalMin = retryIntervalMin;
	}

	public int getMaxRetryHour() {
		return maxRetryHour;
	}

	public void setMaxRetryHour(int maxRetryHour) {
		this.maxRetryHour = maxRetryHour;
	}

	public Map<String, IJobEndNotifySender> getSenderMap() {
		return senderMap;
	}
	
	public void setSenderMap(Map<String, IJobEndNotifySender> senderMap) {
		this.senderMap = senderMap;
	}
	
	public boolean isAutoDeleteOldNotify() {
		return autoDeleteOldNotify;
	}

	public void setAutoDeleteOldNotify(boolean autoDeleteOldNotify) {
		this.autoDeleteOldNotify = autoDeleteOldNotify;
	}

	public String getKeepPeriod() {
		return keepPeriod;
	}
	public void setKeepPeriod(String keepPeriod) {
		this.keepPeriod = keepPeriod;
	}
	public String getExecutionTime() {
		return executionTime;
	}
	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
	}
	
	
	// =========================================================================
	
	public void run() {
		while(!Thread.interrupted() && enable) {
			try {
				int cnt = _run();
				if (cnt == 0) {
					Util.sleep(10000); // 10 초 마다 폴링한다.
				}
				
				autoDeleteNotifySendList();
				
			}catch (Throwable e) {
				if (!destroyed) { // destroyed 에서는 조용히 넘어감.
					Util.logError(log, MSG.get("main.jobnotify.sender.error"), e);
				}
				Util.sleep(10*1000); // DB 조회시 에러 발생하면 10초 쉬었다가 다시 한다.
			}
		}
	}
	public int _run() throws Exception {
		
		/*
		 * 통지 대상 내역 select
		 * 조회시에 처음것(I), 에러난것 (F) 을 조회한다.
		 * 이전 실행에서 R 로 잡아 놓고 서버 다운된 경우에는 R 로 계속 남아있으므로 이것도 다시 읽어 재처리 한다.
		 * 내가 R 로 해놓은 것을 peer가 재처리 대상으로 오인하는 것을 방지하기 위해 조건에 systemId 도 넣는다. 
		 */
		// 
		Map in = new HashMap();
		in.put("fromCreateTime", DateUtil.getTimestampString(System.currentTimeMillis() - 3600000 * maxRetryHour));     // 최대 이시간 만큼만 재시도 함.
		in.put("retryBaseTime",  DateUtil.getTimestampString(System.currentTimeMillis() - 60000   * retryIntervalMin)); // 최소 이시간 간격을 두고 재시도함
		in.put("maxTryCount",    maxRetryCount);
		in.put("procSystemId",   Util.getSystemId());  
		List<JobNotifySendInfo> sendAllList = (List<JobNotifySendInfo>)sqlMapClient.queryForList("nbs.monitor.selectJobNotifySendList", in);
		
		if (log.isDebugEnabled()) {
			Util.logDebug(log, "[JobEndNotifySendDaemon] sendList1 : "+sendAllList.toString());
		}
		// 매체별 그룹화
		Map<String, List<JobNotifySendInfo>> sendListMapByDev = new HashMap<String, List<JobNotifySendInfo>>();
		for (JobNotifySendInfo sendInfo : sendAllList) {
			/*
			 * 먼저 R 상태로 잡아놓고, 정상적으로 잡히는 것만 처리 대상으로 삼는다.
			 * peer 와 동시에 잡기를 시도할 경우 peer 에서 먼저 update 성공하면 나는 0 건 리턴되며, 이때는 처리 대상에서 제외시킨다.
			 */
			sendInfo.setProcSystemId(Util.getSystemId());
			/*
			 *  현재 이미 R 로 되어있는 것은 이전 실행때 R 로 해놓고 제대로 완료를 못한 상태에서 down 된 경우일 수 있다.
			 *  이 경우는 update 시도 하지 말고 그냥 list에 포함시켜라. 
			 */
			if (!"R".equals(sendInfo.getSendState())) {
				sendInfo.setSendState("R");
				int cnt = sqlMapClient.update("nbs.monitor.updateJobNotifySendListForStart", sendInfo);
				if (cnt == 0) {
					// peer 에서 먼저 잡은것 같다. 나는 그냥 건너뛴다.
					continue;
				}
			}
			
			List<JobNotifySendInfo> list = sendListMapByDev.get(sendInfo.getRecvType());
			if (list == null) {
				list = new ArrayList(sendAllList.size());
				sendListMapByDev.put(sendInfo.getRecvType(), list);
			}
			list.add(sendInfo);
		}
		
		// 매체별로 한번씩 호출함
		int procCnt = 0;
		for (String devType : sendListMapByDev.keySet()) {
			List<JobNotifySendInfo> sendListByDev = sendListMapByDev.get(devType);
			try {
				if (sendListByDev != null) {
					IJobEndNotifySender sender = senderMap.get(devType);
					if (sender != null && sender.isEnable()) {
						procCnt += sender.doSend(sendListByDev);
						for (JobNotifySendInfo jobNotifySendInfo : sendListByDev) {
							if ("R".equals(jobNotifySendInfo.getSendState())) {
								jobNotifySendInfo.setSendState("F"); // 호출을 했는데도 상태가 "R" 로 그대로 있으면 에러의 가능성이 있으므로 "F"로 변경함.
							}
						}
					}else {
						jobEndNotifyNoSender.doSend(sendListByDev);
					}

					// 처리 결과 update.
					try {
						sqlMapClient.startTransaction();
						sqlMapClient.startBatch();

						for (JobNotifySendInfo info : sendListByDev) {
							sqlMapClient.update("nbs.monitor.updateJobNotifySendListForEnd", info);
						}

						sqlMapClient.executeBatch();
						sqlMapClient.commitTransaction();
					}finally {
						sqlMapClient.endTransaction();
					}
				}
			}catch (Throwable e) {
				Util.logError(log, MSG.get("main.jobnotify.sender.error"), e); // 에러 나도 로그 찍고 다음 매체 돌린다.
			}
		}
		
		return procCnt;
	}

	private void autoDeleteNotifySendList () {
		if (autoDeleteOldNotify) { // 오래된 Notify List 자동 삭제 기능을 사용할 경우에만 스레드를 생성한다.
			if (keepPeriod==null || !keepPeriod.matches("[D|M][0-9]+")) {
				throw new AgentException("main.jobnotify.oldnotify.cleaner.keepperiod.value.error", keepPeriod);
			}

			Calendar current = Calendar.getInstance();

			if (current.get(Calendar.HOUR_OF_DAY) != executionCal.get(Calendar.HOUR_OF_DAY) 
					|| current.get(Calendar.MINUTE) != executionCal.get(Calendar.MINUTE)) {
				// 시각이 아직 안되었거나 시각이 지난 경우 (10초 간격으로 폴링 하기 때문에, 분 단위로 체크한다.)
				deleteOldNotify = true;
				return;
			} else {
				// 실행시간이 된 경우
				if(deleteOldNotify == true) { // 실행시간이 되어 최초로 삭제 루틴이 수행되는 경우
					// delete
					final long deleteBaseDate = getDeleteBaseDate();
					log.info(MSG.get("main.jobnotify.oldnotify.cleaner.start", Util.getYYYYMMDD(deleteBaseDate))); //[OldNotifyCleaner] {0} 이전에 생성된 통지내역들을 삭제 시작합니다

					// Job Notify Thread 에 영향을 주지 않기 위해서 삭제를 하는 새로운 Thread 를 시작한다.
					new Thread(new Runnable() {
						public void run() {
							try {
								try {
									sqlMapClient.startTransaction();
									int cnt = sqlMapClient.delete("nbs.monitor.deleteJobOldNotifySendList", DateUtil.getTimestampString(deleteBaseDate));
									sqlMapClient.commitTransaction();
									log.info(MSG.get("main.jobnotify.oldnotify.cleaner.delete.ok", "NBS_NOTIFY_SEND_LIST", cnt)); //[OldNotifyCleaner] {0} 테이블에서 {1} 건이 삭제되었습니다
								} finally {
									sqlMapClient.endTransaction();
								}
							} catch (Exception e) {
								Util.logError(log, MSG.get("main.jobnotify.oldnotify.cleaner.error"), e); // 에러 나도 로그 찍는다.
							} finally {
								Calendar tmpCal = Calendar.getInstance();
								tmpCal.add(Calendar.DATE, 1);
								tmpCal.set(Calendar.HOUR_OF_DAY, executionCal.get(Calendar.HOUR_OF_DAY));
								tmpCal.set(Calendar.MINUTE,      executionCal.get(Calendar.MINUTE));
								tmpCal.set(Calendar.SECOND,      0);
								
								log.info(MSG.get("main.jobnotify.oldnotify.cleaner.next.time", tmpCal.getTime()));  // [OldNotifyCleaner] 다음 Clean 작업 시각은  [{0,date,full} {0,time,full}] 입니다
							}
						}
					}).start();

					deleteOldNotify = false;
				} else {
					// 실행시간이 되었어도, 한번 삭제가 되면 아무 작업도 하지 않고 SKIP 한다.
					// 폴링을 통해서 시간이 흐르기만 기다린다.
					// 삭제하고 나서 sleep으로 시간을 기다릴 수 있지만
					// 그렇게 되면 해당 시간동안 Notify 작업도 일어나지 않기에 이렇게 한다.
					return;
				}
			}
		}
	}

	/**
	 * 삭제 대상 기준일 계산.
	 * Mn : n 개월 이전것 삭제, Dn : n 일 이전것 삭제
	 * M0, D0, D1 은 에러
	 * @return
	 */
	private long getDeleteBaseDate() {
		Calendar deleteBaseDateCal = Calendar.getInstance();

		if (keepPeriod.startsWith("M")) {
			int month = Integer.parseInt( keepPeriod.substring(1) );
			if (month < 1) { // M0 는 에러를 내야함.
				throw new AgentException("agent.logger.oldfile.cleaner.keepperiod.value.error", keepPeriod);
			}
			deleteBaseDateCal.add(Calendar.MONTH, month * -1);
		}else if (keepPeriod.startsWith("D")) {
			int day   = Integer.parseInt( keepPeriod.substring(1) );
			if (day < 2) {   // 최소 2일 이상은 보관해야함.
				throw new AgentException("agent.logger.oldfile.cleaner.keepperiod.value.error", keepPeriod);
			}
			deleteBaseDateCal.add(Calendar.DATE,  day * -1);
		}else {
			throw new AgentException("agent.logger.oldfile.cleaner.keepperiod.value.error", keepPeriod);
		}

		return deleteBaseDateCal.getTime().getTime();
	}
}

