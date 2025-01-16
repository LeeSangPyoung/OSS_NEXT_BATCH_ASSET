package nexcore.scheduler.agent.joblog;

import java.io.File;

import org.apache.commons.logging.Log;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 로거 인터페이스 </li>
 * <li>작성일 : 2010. 5. 26.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface ILogger extends Log {
	public boolean isValid();
	
	public boolean isFatalEnabled();
	public boolean isErrorEnabled();
	public boolean isWarnEnabled();
	public boolean isInfoEnabled();
	public boolean isDebugEnabled();
	public boolean isTraceEnabled();

	public void fatal(Object log);
	public void fatal(Object log, Throwable t);
	public void error(Object log);
	public void error(Object log, Throwable t);
	public void warn(Object log);
	public void warn(Object log, Throwable t);
	public void info(Object log);
	public void info(Object log, Throwable t);
	public void debug(Object log);
	public void debug(Object log, Throwable t);
	public void trace(Object log);
	public void trace(Object log, Throwable t);
	
	// FileLogger 인 경우 디렉토리 get.
	public File getDirectory();
	
	// 로그 파일명 또는 테이블명.
	public String getFilename();
	
	// 로그 파일 인코딩
	public String getEncoding();
	
	/**
	 * 로그레벨객체
	 * @return
	 */
	public LogLevel getLogLevel();
	
	/**
	 * 로그레벨명
	 * @return
	 */
	public String getLogLevelByName();
	
	/**
	 * @param logLevel
	 */
	public void setLogLevel(LogLevel logLevel);
	
	/**
	 * @param logLevelName 로그레벨 명 
	 */
	public void setLogLevelByName(String logLevelName);
	
	public void close();
}
