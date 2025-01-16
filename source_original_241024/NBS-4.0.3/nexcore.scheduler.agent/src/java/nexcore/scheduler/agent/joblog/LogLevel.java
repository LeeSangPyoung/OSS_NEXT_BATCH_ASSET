package nexcore.scheduler.agent.joblog;

import java.io.Serializable;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 로그레벨 </li>
 * <li>작성일 : 2010. 5. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class LogLevel implements Serializable {
	private static final long serialVersionUID = 7389009421372712868L;

	private int level ;
	
	public final static int LEVEL_OFF   = Integer.MAX_VALUE;
	public final static int LEVEL_FATAL = 500;
	public final static int LEVEL_ERROR = 400;
	public final static int LEVEL_WARN  = 300;
	public final static int LEVEL_INFO  = 200;
	public final static int LEVEL_DEBUG = 100;
	public final static int LEVEL_TRACE =  50;
	public final static int LEVEL_ALL   = Integer.MIN_VALUE;
	
	public LogLevel() {
		this.level = LEVEL_ALL;
	}
	
	public LogLevel(int logLevel) {
		this.level = logLevel;
	}

	public LogLevel(String logLevelString) {
		this.level = getLevelFromString(logLevelString);
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public void setLevel(int newLevel) {
		this.level = newLevel;
	}
	
	public boolean isEnabled(int lvl) {
		return this.level <= lvl;
	}
	
	public static String getLevelString(int level) {
		switch(level) {
			case LEVEL_FATAL : return "FATAL";
			case LEVEL_ERROR : return "ERROR";
			case LEVEL_WARN  : return "WARN";
			case LEVEL_INFO  : return "INFO";
			case LEVEL_DEBUG : return "DEBUG";
			case LEVEL_TRACE : return "TRACE";
		}
		return "N/A";
	}
	
	public static int getLevelFromString(String levelString) {
		if ("FATAL".equalsIgnoreCase(levelString)) {
			return LEVEL_FATAL;
		}else if ("ERROR".equalsIgnoreCase(levelString)) {
			return LEVEL_ERROR;
		}else if ("WARN".equalsIgnoreCase(levelString)) {
			return LEVEL_WARN;
		}else if ("INFO".equalsIgnoreCase(levelString)) {
			return LEVEL_INFO;
		}else if ("DEBUG".equalsIgnoreCase(levelString)) {
			return LEVEL_DEBUG;
		}else if ("TRACE".equalsIgnoreCase(levelString)) {
			return LEVEL_TRACE;
		}
		return LEVEL_ALL;
	}

	public String getThisLevelString() {
		return getLevelString(level);
	}
	
	public String toString() {
		return "[LogLevel:"+getThisLevelString();
	}
}
