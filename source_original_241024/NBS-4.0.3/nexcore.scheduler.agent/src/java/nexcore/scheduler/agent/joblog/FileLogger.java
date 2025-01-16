
package nexcore.scheduler.agent.joblog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : File Logger. 파일로거는 DB 로거와 달리 여기에서 직접 파일에 write한다. </li>
 * <li>작성일 : 2010. 5. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// 2016.3.8 FileLogger 로 단순화 한다. 
public class FileLogger implements ILogger, Serializable  {
	private static final long serialVersionUID = -7601029435808942518L;
	
	private File          logFile;
	private PrintWriter   out;
	private int           bufferSize = 1024;  // default 모드
	private String        encoding;           // 인코딩 지정 가능토록함
	private boolean       isValid;
	private LogLevel      logLevel;

	private Object        writeLock = new Object(); // 멀티 스레드에서 하나의 로거로 write 할 수 있으므로 동기화 필요함.
	
	private Set<String>   logMethodNames = new HashSet(Arrays.asList(new String[]{"debug", "info", "error", "warn", "fatal", "trace"}));
	
	/*
	 * 로그중 파일명을 출력하지 않도록 하는 패키지명 prefix list. 
	 * 프레임워크 자체 클래스는 파일명 출력 필요없다. 2014.11.27 정호철
	 * 기본 값으로는 "nexcore." 
	 * 예로 대구은행의 경우는 "inexpia.fke." 를 설정파일에 추가로 설정함 
	 */
	private List<String>  packageNamePrefixListForNotPrintFilename = Arrays.asList(new String[]{"nexcore."});
	
	private boolean       printFilenameAndLineNumber = true;
	
	public FileLogger(File file) {
		this.logFile = file;
	}
	
	public void init() throws IOException {
		this.out     = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), encoding), bufferSize));
		this.isValid = true;
	}
	
	protected void finalize() throws Throwable {
		try {
			close();
		}catch(Exception ignore) {
			ignore.printStackTrace(); // 별다른 방법없음
		}
	}
	
	public File getLogFile() {
		return logFile;
	}
	
	public void setLogFile(File logFile) {
		this.logFile = logFile;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public List<String> getPackageNamePrefixListForNotPrintFilename() {
		return packageNamePrefixListForNotPrintFilename;
	}
	
	public void setPackageNamePrefixListForNotPrintFilename(List<String> packageNamePrefixListForNotPrintFilename) {
		this.packageNamePrefixListForNotPrintFilename = packageNamePrefixListForNotPrintFilename;
	}
	
	public boolean isPrintFilenameAndLineNumber() {
		return printFilenameAndLineNumber;
	}
	
	public void setPrintFilenameAndLineNumber(boolean printFilenameAndLineNumber) {
		this.printFilenameAndLineNumber = printFilenameAndLineNumber;
	}
	
	public PrintWriter getOut() {
		return out;
	}

	public File getDirectory() {
		return logFile !=null ? logFile.getParentFile() : null;
	}
	
	public String getFilename() {
		return logFile !=null ? logFile.getAbsolutePath() : null;
		
	}

	// ===========================================================================================================
	
	
	public void debug(Object log) {
		if (isDebugEnabled()) {
			writeLog(LogLevel.LEVEL_DEBUG, findSourceLocation(), log, null);
		}
	}
	
	public void debug(Object log, Throwable t) {
		if (isDebugEnabled()) {
			writeLog(LogLevel.LEVEL_DEBUG, findSourceLocation(), log, t);
		}
	}
	
	public void error(Object log) {
		if (isErrorEnabled()) {
			writeLog(LogLevel.LEVEL_ERROR, findSourceLocation(), log, null);
		}
	}
	
	public void error(Object log, Throwable t) {
		if (isErrorEnabled()) {
			writeLog(LogLevel.LEVEL_ERROR, findSourceLocation(), log, t);
		}
	}
	
	public void fatal(Object log) {
		if (isFatalEnabled()) {
			writeLog(LogLevel.LEVEL_FATAL, findSourceLocation(), log, null);
		}
	}
	
	public void fatal(Object log, Throwable t) {
		if (isFatalEnabled()) {
			writeLog(LogLevel.LEVEL_FATAL, findSourceLocation(), log, t);
		}
	}
	
	public void info(Object log) {
		if (isInfoEnabled()) {
			writeLog(LogLevel.LEVEL_INFO, findSourceLocation(), log, null);
		}
	}
	
	public void info(Object log, Throwable t) {
		if (isInfoEnabled()) {
			writeLog(LogLevel.LEVEL_INFO, findSourceLocation(), log, t);
		}
	}
	
	public void warn(Object log) {
		if (isWarnEnabled()) {
			writeLog(LogLevel.LEVEL_WARN, findSourceLocation(), log, null);
		}
	}
	
	public void warn(Object log, Throwable t) {
		if (isWarnEnabled()) {
			writeLog(LogLevel.LEVEL_WARN, findSourceLocation(), log, t);
		}
	}
	
	public void trace(Object log) {
		if (isTraceEnabled()) {
			writeLog(LogLevel.LEVEL_TRACE, findSourceLocation(), log, null);
		}
	}
	
	public void trace(Object log, Throwable t) {
		if (isTraceEnabled()) {
			writeLog(LogLevel.LEVEL_TRACE, findSourceLocation(), log, t);
		}
	}
	
	/**
	 * [파일명 + 라인번호] 를 로그에 포함할지 말지 체크함
	 *  
	 * @param className
	 * @return true : not print, false : print
	 */
	private boolean checkIfNotPrintFilename(String className) {
		for (String packageNamePrefixForNotPrintFilename : packageNamePrefixListForNotPrintFilename) {
			if (className.startsWith(packageNamePrefixForNotPrintFilename)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 소스의 파일명:라인번호 를 찾는다.
	 * @return
	 */
	protected String findSourceLocation() {
		StackTraceElement[] steList = Thread.currentThread().getStackTrace();
		
		for (int i=0; i<steList.length; i++) {
			StackTraceElement ste = steList[i];
			if (FileLogger.class.getName().equals(ste.getClassName()) && logMethodNames.contains(ste.getMethodName()) && !checkIfNotPrintFilename(steList[i+1].getClassName())) {
				return steList[i+1].getFileName()+":"+steList[i+1].getLineNumber();
			}
		}
		
		return null;
	}
	
	public boolean isDebugEnabled() {
		return logLevel.isEnabled(LogLevel.LEVEL_DEBUG);
	}
	
	public boolean isErrorEnabled() {
		return logLevel.isEnabled(LogLevel.LEVEL_ERROR);
	}
	
	public boolean isFatalEnabled() {
		return logLevel.isEnabled(LogLevel.LEVEL_FATAL);
	}
	
	public boolean isInfoEnabled() {
		return logLevel.isEnabled(LogLevel.LEVEL_INFO);
	}
	
	public boolean isTraceEnabled() {
		return logLevel.isEnabled(LogLevel.LEVEL_TRACE);
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	public boolean isWarnEnabled() {
		return logLevel.isEnabled(LogLevel.LEVEL_WARN);
	}
	
	public LogLevel getLogLevel() {
		return logLevel;
	}
	
	public String getLogLevelByName() {
		return logLevel.getThisLevelString();
	}
	
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	
	public void setLogLevelByName(String logLevelName) {
		this.logLevel = new LogLevel(logLevelName);
	}

	private void writeLog(int level, String sourceLocation, Object log, Throwable t) {
		// 1. 시각 찍기
		FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");
		String datetime = fdf.format(System.currentTimeMillis());
		String levelString = LogLevel.getLevelString(level);
		
		synchronized(writeLock) {
			out.print("[");
			out.print(datetime);
			out.print("]");
			
			// 2. 레벨 찍기
			out.print("[");
			out.print(levelString.charAt(0)); // 2014.09.05. 로그 간소화를 위해 레벨은 한글자만 찍는다. 온라인과 맞춘다.
			out.print("]");
			
			// 3. 소스 위치 찍기 (debug 일때만 && printFilenameAndLineNumber == true 일때만)
			if (sourceLocation != null && printFilenameAndLineNumber) { 
				out.print("[");
				out.print(sourceLocation);
				out.print("]");
			}
			
			// 4. 내용 찍기.
			out.print(" ");
			out.print(String.valueOf(log));
			
			// 5. Exception 찍기
			if (t != null) {
				out.println();
				t.printStackTrace(out);
			}else {
				out.println();
			}
		}
		flush();
	}
	
	public void flush() {
		out.flush();
	}
	
	public void close() {
		if (out != null) {
			out.close();
		}
		isValid = false;
	}
	
	
	// 테스트 메소드
	public static void main(String[] args) throws Exception {
		FileLogger fl = new FileLogger(new File("c:/temp/out.txt"));
		fl.setEncoding("ms949");
		fl.init();
		LogLevel lvl = new LogLevel("debug");
		fl.setLogLevel(lvl);
		fl.debug("** debug log ** ");
		fl.debug("** debug log 2 ** ", new Exception("FORCE"));
		fl.info("mmmmmmmm", new Exception("1"));
		fl.info("aaaaaaa", new Exception("2"));
		fl.flush();
	}
	
}