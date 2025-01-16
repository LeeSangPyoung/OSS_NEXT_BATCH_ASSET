package nexcore.scheduler.monitor.internal;

import java.io.File;
import java.io.FileFilter;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.ibatis.sqlmap.client.SqlMapClient;

import nexcore.scheduler.core.IMonitorDisplayable;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 오래된 Job Instnace, Job Execution 정보를 삭제함 </li>
 * <li>설  명 : 00000  </li>
 * <li>작성일 : 2011. 3. 30.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */ // activation 일자를 기준으로 체크함
public class OldJobCleaner implements Runnable, IMonitorDisplayable {
	private boolean      	enable;
	private String       	keepPeriod;       // [Dn] : n일 지난것 삭제, [Wn] : n주 지난것 삭제, [Mn] : n월 지난것 삭제
	private String       	executionTime;    // HHMM 타입으로 실행 시각 지정.
	private SqlMapClient 	sqlMapClient;

	private Thread       	thisThread;
	private Log       		log;
	
	public void init() {
		if (enable) {
			if (keepPeriod==null || !keepPeriod.matches("[D|M][0-9]+")) {
				throw new SchedulerException("main.oldjobcleaner.keepperiod.value.error", keepPeriod);
			}
		}
		
		log = LogManager.getSchedulerLog();
		thisThread = new Thread(this, "OldJobCleaner");
		thisThread.setDaemon(true);
		thisThread.start();
		Util.logServerInitConsole("OldJobCleaner", "("+enable+","+keepPeriod+","+executionTime+")");
	}
	
	public void destroy() {
	}
	
	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}
	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
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
	
	// ===============================================================

	private void sleep() {
		Calendar current = Calendar.getInstance();
		Calendar executionCal = Calendar.getInstance();
		executionCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(executionTime.substring(0,2)));
		executionCal.set(Calendar.MINUTE,      Integer.parseInt(executionTime.substring(2,4)));
		executionCal.set(Calendar.SECOND,      0);
		
		long sleepTime = 0;
		if (current.compareTo(executionCal) < 0) {
			// 시각이 아직 안된 경우
			sleepTime = executionCal.getTime().getTime() - current.getTime().getTime();
		}else {
			// 이미 시각이 지난 경우 다음날 실행 시각으로 다시 계산
			executionCal.add(Calendar.DATE, 1);
			sleepTime = executionCal.getTime().getTime() - current.getTime().getTime();
		}
		try {
			log.info(MSG.get("main.oldjobcleaner.next.time", executionCal.getTime()));  // 다음 Cleanup 작업은 {0} 입니다 
			Thread.sleep(sleepTime);
		} catch (Exception e) {
			log.info("[OldJobCleaner] "+this+" interrupted.", e);
		}
	}
	
	/**
	 * 삭제 대상 기준일 계산.
	 * Mn : n 개월 이전것 삭제, Dn : n 일 이전것 삭제
	 * M0, D0, D1 은 에러
	 * @return
	 */
	private String getDeleteBaseDateYYYYMMDD() {
		Calendar deleteBaseDateCal = Calendar.getInstance();
		
		if (keepPeriod.startsWith("M")) {
			int month = Integer.parseInt( keepPeriod.substring(1) );
			if (month < 1) { // M0 는 에러를 내야함.
				throw new SchedulerException("main.oldjobcleaner.keepperiod.value.error", keepPeriod);
			}
			deleteBaseDateCal.add(Calendar.MONTH, month * -1);
		}else if (keepPeriod.startsWith("D")) {
			int day   = Integer.parseInt( keepPeriod.substring(1) );
			if (day < 2) {   // 최소 2일 이상은 보관해야함.
				throw new SchedulerException("main.oldjobcleaner.keepperiod.value.error", keepPeriod);
			}
			deleteBaseDateCal.add(Calendar.DATE,  day * -1);
		}else {
			throw new SchedulerException("main.oldjobcleaner.keepperiod.value.error", keepPeriod);
		}
		
		return Util.getYYYYMMDD(deleteBaseDateCal.getTime().getTime());
	}
	
	private List<String> getToBeDeletedJobIdList(String deleteBaseDate) throws SQLException {
		List jobIdList = sqlMapClient.queryForList("nbs.monitor.selectTargetJobInsIdList", deleteBaseDate);
		return jobIdList;
	}
	
	/**
	 * 일괄 JobDefinition 시에 생성됐던 temp 파일들을 삭제함
	 */
	private int deleteUploadTempFile() {
		final long deleteTime = System.currentTimeMillis() - 60 * 60 * 1000;
		
		File tmpDir = new File(System.getProperty("NEXCORE_HOME")+"/tmp");
		File[] uploadDirs = tmpDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return 
					pathname.isDirectory() && 
					pathname.lastModified() < deleteTime && 
					pathname.getName().startsWith("jobdef_upload");
			}
		});
		
		int cnt=0;
		// upload 한번에 uploadDir 하나씩 생성된다.
		for (File uploadDir : uploadDirs) {
			for (File uploadFile : uploadDir.listFiles()) {
				if (uploadFile.delete()) {
					cnt++;
				}
			}
			if (uploadDir.delete()) {
				cnt++;
			}
		}
		return cnt;
	}
	
	/**
	 * Clean 로그를 Insert 함. 
	 * Clean 수행 직전에 insert 하고, Clean 이 끝나고 나면 결과 건수를 Update 한다.
	 * 
	 * @param date 오늘
	 * @param deleteBaseDate 삭제 기준일
	 * @return
	 */
	private void insertCleanLog(String date, String deleteBaseDate) throws SQLException {
		Map param = new HashMap();
		
		String current = DateUtil.getCurrentTimestampString();
		
		param.put("runDate",         date);
		param.put("delBaseDate",     deleteBaseDate);
		param.put("systemId",        Util.getSystemId());
		param.put("cleanStartTime",  current);
		param.put("lastModifyTime",  current); 
		
		sqlMapClient.insert("nbs.monitor.insertCleanLog", param);
	}
	
	/**
	 * 당일자 Activation 결과를 update 한다.
	 * 위에서 먼저 Insert 되어있는 row 에 activation 결과를 update한다. 
	 * @param procDate
	 * @return
	 */
	private void updateCleanLog(String date, 
			int jobinsCnt, int jobinsObjCnt, int jobinsPreJobCnt, 
			int jobexeCnt, int jobexeObjCnt, int uploadTempFileCnt,
			int timeSchLogCnt, int idgenBaseCnt) throws SQLException {
		Map param = new HashMap();
		
		String current = DateUtil.getCurrentTimestampString();
		param.put("runDate",             date);
		param.put("jobInsCnt",           jobinsCnt);
		param.put("jobInsObjCnt",        jobinsObjCnt);
		param.put("jobInsPreJobCnt",     jobinsPreJobCnt);
		param.put("jobExeCnt",           jobexeCnt);
		param.put("jobExeObjCnt",        jobexeObjCnt);
		param.put("uploadTempFileCnt",   uploadTempFileCnt);
		param.put("timeSchLogCnt",       timeSchLogCnt);
		param.put("idGenBaseCnt",        idgenBaseCnt);
		param.put("cleanEndTime",        current);
		param.put("lastModifyTime",      current); 
		
		int cnt = sqlMapClient.update("nbs.monitor.updateCleanLog", param);
		if (cnt != 1) {
			// 위에서 먼저 insert 한 것을 update 하는 것이므로 1 이 아닐 수 없다.
			log.warn("[OldJobCleaner] updateCleanLog result : "+cnt);
		}
	}

	public void run() {
		while(!Thread.interrupted() && enable) {
			try {
				sleep();
	
				if (!enable) { // disable 상태에선 pass 시킨다.
					log.info(MSG.get("main.oldjobcleaner.disabled"));
					Util.sleep(100 * 1000); // 100 초 쉬고 continue 한다. 이렇게 하는 이유는 분(minute)이 바뀔때까지 쉬기 위해.
					continue;
				}
				
				String deleteBaseDate     = getDeleteBaseDateYYYYMMDD(); // 삭제 대상 일자 YYYYMMDD
				String deleteBaseDateTime = deleteBaseDate + "000000";   // 삭제 대상 시각 YYYYMMDD000000
				String today              = Util.getCurrentYYYYMMDD();
				
				try {
					insertCleanLog(today, deleteBaseDate);
				}catch (SQLException e) { // DUP 에러가 나면 일하지 않고 쉰다.
					log.info(MSG.get("main.oldjobcleaner.precheck.false")+"/"+e.getMessage());
					if (log.isDebugEnabled()) {
						log.debug("insertCleanLog fail", e);
					}
					Util.sleep(100 * 1000);
					continue;
				}
				
				log.info(MSG.get("main.oldjobcleaner.target.basedate", deleteBaseDateTime)); //[OldJobCleaner] {0} 이전에 생성된 인스턴스들을 삭제 시작합니다
				
				// 삭제 대상 일자 추출함
				List<String> jobIdList = getToBeDeletedJobIdList(deleteBaseDateTime);
				log.info(MSG.get("main.oldjobcleaner.target.jobid.list", jobIdList.toString())); //[OldJobCleaner] 삭제 대상 Job Id 리스트. {0}


				int delJobInsCnt = 0, delJobInsObjCnt = 0, delJobInsPreJobCnt = 0, delJobInsTriggerCnt = 0, delJobExeCnt = 0, delJobExeObjCnt = 0, delUploadTempFileCnt = 0,
				    delTimeSchLogCnt = 0, delIdGenBaseCnt = 0;
				
				try {
					// NBS_JOB_INS 테이블 삭제
					delJobInsCnt = sqlMapClient.delete("nbs.monitor.deleteOldJobIns", deleteBaseDateTime);
					log.info(MSG.get("main.oldjobcleaner.delete.ok", "NBS_JOB_INS",delJobInsCnt)); //[OldJobCleaner] {0} 테이블에서 {1} 건이 삭제되었습니다
				}catch (Exception e) {
					log.info("[OldJobCleaner] "+MSG.get("com.error.occurred.while", "Delete NBS_JOB_INS"), e);
				}
					
				try {
					// NBS_JOB_INS_OBJ_STORE 테이블 삭제
					delJobInsObjCnt = sqlMapClient.delete("nbs.monitor.deleteOldJobInsObjStore");
					log.info(MSG.get("main.oldjobcleaner.delete.ok", "NBS_JOB_INS_OBJ_STORE", delJobInsObjCnt)); //[OldJobCleaner] {0} 테이블에서 {1} 건이 삭제되었습니다
				}catch (Exception e) {
					log.info("[OldJobCleaner] "+MSG.get("com.error.occurred.while", "Delete NBS_JOB_INS_OBJ_STORE"), e);
				}
					
				try {
					// NBS_JOB_INS_PREJOB 테이블 삭제
					delJobInsPreJobCnt = sqlMapClient.delete("nbs.monitor.deleteOldJobInsPreJob");
					log.info(MSG.get("main.oldjobcleaner.delete.ok", "NBS_JOB_INS_PREJOB", delJobInsPreJobCnt)); //[OldJobCleaner] {0} 테이블에서 {1} 건이 삭제되었습니다
				}catch (Exception e) {
					log.info("[OldJobCleaner] "+MSG.get("com.error.occurred.while", "Delete NBS_JOB_INS_PREJOB"), e);
				}

				try {
					// NBS_JOB_INS_TRIGGER 테이블 삭제
					delJobInsTriggerCnt = sqlMapClient.delete("nbs.monitor.deleteOldJobInsTrigger");
					log.info(MSG.get("main.oldjobcleaner.delete.ok", "NBS_JOB_INS_TRIGGER", delJobInsTriggerCnt)); //[OldJobCleaner] {0} 테이블에서 {1} 건이 삭제되었습니다
				}catch (Exception e) {
					log.info("[OldJobCleaner] "+MSG.get("com.error.occurred.while", "Delete NBS_JOB_INS_TRIGGER"), e);
				}

				try {
					// NBS_JOB_EXE 테이블 삭제
					delJobExeCnt = sqlMapClient.delete("nbs.monitor.deleteOldJobExe");
					log.info(MSG.get("main.oldjobcleaner.delete.ok", "NBS_JOB_EXE", delJobExeCnt)); //[OldJobCleaner] {0} 테이블에서 {1} 건이 삭제되었습니다
				}catch (Exception e) {
					log.info("[OldJobCleaner] "+MSG.get("com.error.occurred.while", "Delete NBS_JOB_EXE"), e);
				}

				try {
					// NBS_JOB_EXE_OBJ_STORE 테이블 삭제
					delJobExeObjCnt = sqlMapClient.delete("nbs.monitor.deleteOldJobExeObjStore");
					log.info(MSG.get("main.oldjobcleaner.delete.ok", "NBS_JOB_EXE_OBJ_STORE", delJobExeObjCnt)); //[OldJobCleaner] {0} 테이블에서 {1} 건이 삭제되었습니다
				}catch (Exception e) {
					log.info("[OldJobCleaner] "+MSG.get("com.error.occurred.while", "Delete NBS_JOB_EXE_OBJ_STORE"), e);
				}
				
				try {
					// NBS_TIMESCH_LOG 테이블 삭제
					Map sqlin = new HashMap();
					sqlin.put("tsDate", deleteBaseDate);
					delTimeSchLogCnt = sqlMapClient.delete("nbs.monitor.deleteOldTimeSchedulerLog", sqlin);
					log.info(MSG.get("main.oldjobcleaner.delete.ok", "NBS_TIMESCH_LOG", delTimeSchLogCnt)); //[OldJobCleaner] {0} 테이블에서 {1} 건이 삭제되었습니다
				}catch (Exception e) {
					log.info("[OldJobCleaner] "+MSG.get("com.error.occurred.while", "Delete NBS_TIMESCH_LOG"), e);
				}

				try {
					// NBS_IDGEN_BASE 테이블 삭제
					Map sqlin = new HashMap();
					sqlin.put("deleteBaseDate", deleteBaseDate);
					delIdGenBaseCnt = sqlMapClient.delete("nbs.monitor.deleteOldIdGenBase", sqlin);
					log.info(MSG.get("main.oldjobcleaner.delete.ok", "NBS_IDGEN_BASE", delIdGenBaseCnt)); //[OldJobCleaner] {0} 테이블에서 {1} 건이 삭제되었습니다
				}catch (Exception e) {
					log.info("[OldJobCleaner] "+MSG.get("com.error.occurred.while", "Delete NBS_IDGEN_BASE"), e);
				}

				try {
					// upload temp 파일 삭제
					delUploadTempFileCnt = deleteUploadTempFile();
					log.info(MSG.get("main.oldjobcleaner.delete.ok", "Upload Temp Dir", delUploadTempFileCnt)); //[OldJobCleaner] {0} 테이블에서 {1} 건이 삭제되었습니다
				}catch (Exception e) {
					log.info("[OldJobCleaner] "+MSG.get("com.error.occurred.while", "Delete Upload Temp Dir"), e);
				}
				updateCleanLog(today, delJobInsCnt, delJobInsObjCnt, delJobInsPreJobCnt, delJobExeCnt, delJobExeObjCnt, delUploadTempFileCnt, delTimeSchLogCnt, delIdGenBaseCnt);
			}catch (Throwable e) {
				Util.logInfo(log, "[OldJobCleaner] "+MSG.get("com.error.occurred.while", "Delete"), e);
			}
		}	
	}
	
	public String getDisplayName() {
		return "OldJobCleaner";
	}
	
	public String getDisplayString() {
		return "["+enable+"] " +executionTime +", "+keepPeriod;
	}

}
