package nexcore.scheduler.util;

import nexcore.scheduler.exception.SchedulerException;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 9999 </li>
 * <li>설  명 : 00000  </li>
 * <li>작성일 : 2011. 3. 29.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class SchedulerUtil {

	/**
	 * with norun 모드로 실행되었는지 체크해서 Job 실행안되도록 막는다.
	 */
	public static void checkStartedWithNoRun() {
		if (Boolean.parseBoolean(System.getProperty("NC_SCHEDULER_NORUN"))) { // startupWithNoRun 으로 실행한 경우 Job 실행은 되지 않도록 한다.
			throw new SchedulerException("main.started.with.norun");
		}
	}
}
