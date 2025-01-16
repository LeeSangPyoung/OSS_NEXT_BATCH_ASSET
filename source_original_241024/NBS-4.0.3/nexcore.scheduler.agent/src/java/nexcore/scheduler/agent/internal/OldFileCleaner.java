package nexcore.scheduler.agent.internal;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;

import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 에이전트의 로그파일, 파라미터 파일 등 의 파일들 중에 일정 기간이 지난 파일을 자동으로 삭제하는 데몬 </li>
 * <li>작성일 : 2013. 2. 14.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class OldFileCleaner implements Runnable {
	private boolean         enabled;
	private boolean         autoDeleteOldFile; // 오래된 파일을 자동으로 삭제할 것인지?
	private String       	keepPeriod;        // [Dn] : n일 지난것 삭제, [Mn] : n월 지난것 삭제
	private String       	executionTime;     // HHMM 타입으로 실행 시각 지정.
	private List<String>    targetLocations;   // 삭제 대상 디렉토리들. ${NEXCORE_HOME}/batch/log, ${NEXCORE_HOME}/batch/param 등.

	private Thread       	thisThread;
	private Log       		log;

	public OldFileCleaner () {
	}

	public void init() {
		log = LogManager.getAgentLog();
		if (autoDeleteOldFile && enabled) { // 오래된 파일 자동 삭제 기능을 사용할 경우에만 스레드를 생성한다.
			if (keepPeriod==null || !keepPeriod.matches("[D|M][0-9]+")) {
				throw new AgentException("agent.logger.oldfile.cleaner.keepperiod.value.error", keepPeriod);
			}
			
			thisThread = new Thread(this, "OldFileCleaner");
			thisThread.setDaemon(true);
			thisThread.start();
		}else {
			log.info("OldFileCleaner disabled");
		}

	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void destroy() {
	}

	public boolean isAutoDeleteOldFile() {
		return autoDeleteOldFile;
	}

	public void setAutoDeleteOldFile(boolean autoDeleteOldFile) {
		this.autoDeleteOldFile = autoDeleteOldFile;
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

	public List<String> getTargetLocations() {
		return targetLocations;
	}

	public void setTargetLocations(List<String> targetLocations) {
		this.targetLocations = targetLocations;
	}

	
	// =============================================================
	
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
			Util.logInfo(log, MSG.get("agent.logger.oldfile.cleaner.next.time", executionCal.getTime()));  // 다음 Cleanup 작업은 {0} 입니다 
			Thread.sleep(sleepTime);
		} catch (Exception e) {
			Util.logInfo(log, "[OldLogFileCleaner] "+this+" interrupted.", e);
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

	/**
	 * 디렉토리 내의 파일 삭제. deleteBaseDate 보다 오래된 것은 삭제한다.
	 * 하위 디렉토리도 삭제한다.
	 * 파일이 없는 디렉토리는 디렉토리도 삭제한다.
	 * @param deleteBaseDate
	 */
	public void deleteFiles(long deleteBaseDate) {

		for (String targetDir : targetLocations) {
			int totalDeleteCount = _deleteFiles(deleteBaseDate, new File(targetDir));
			Util.logInfo(log, MSG.get("agent.logger.oldfile.cleaner.end", targetDir, totalDeleteCount)); //[OldLogFileCleaner] {0} 디렉토리내 {1} 개의 로그파일들이 삭제 되었습니다
		}
	}
	
	private int _deleteFiles(long deleteBaseDate, File dir) {
		File[] files = dir.listFiles();
		if (files == null) {
			return 0;
		}
			
		int deleteCount = 0;
		for (File f : files) {
			if (f.isFile()) {
				if (f.lastModified() < deleteBaseDate) { // 기준일 이전 파일이면 삭제한다.
					f.delete();
					deleteCount ++;
					Util.logInfo(log, MSG.get("agent.logger.oldfile.cleaner.delete.ok", f.getName())); //[OldLogFileCleaner] {0} 파일이 삭제 되었습니다
				}
			}else {
				deleteCount += _deleteFiles(deleteBaseDate, f);
				f.delete(); // 디렉토리도 삭제한다. 만일 해당 디렉토리에 파일이 하나라도 남아있으면 delete 가 되지 않는다. 남겨둔다.
			}
		}
		return deleteCount;
	}
	
	public void run() {
		while(!Thread.interrupted()) {
			try {
				sleep();
				long deleteBaseDate = getDeleteBaseDate();
				Util.logInfo(log, MSG.get("agent.logger.oldfile.cleaner.start", Util.getYYYYMMDD(deleteBaseDate))); //[OldLogFileCleaner] {0} 이전에 생성된 Job 로그 파일 삭제를 시작합니다
				deleteFiles(deleteBaseDate);
			}catch (Throwable e) {
				Util.logError(log, "[OldLogFileCleaner] "+MSG.get("com.error.occurred.while", "Old log delete"), e);
			}finally {
				Util.sleep(10000); // 5 초 쉼.
			}
		}	
	}
}
