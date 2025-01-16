package nexcore.scheduler.agent.joblog;

import java.io.File;
import java.io.IOException;
import java.util.List;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : nexcore-scheduler-4</li>
 * <li>서브 업무명 : nexcore.scheduler.agent.joblog</li>
 * <li>설  명 : Job 로거 factory. 로그 파일 </li>
 * <li>작성일 : 2016. 1. 15.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FileLoggerFactory implements ILoggerFactory {
	private String  		baseDir;
	private boolean 		separatePerDay;    // 일자별로 별도 디렉토리에 로그할 것인지?
	private boolean 		separatePerJobId;  // Job Id 별로 별도 디렉토리에 로그할 것인지?
	private String			encoding;          // 파일 인코딩 설정
	private String          defaultLogLevel;   // 기본 로그레벨
	private String          filenamePattern;   // 로그 파일 파일명 패턴. 예)  bat-${SHELL_NAME}.${PROC_DATE}.${JOB_ID}-${CLASS_NAME}-${THREAD_NO}
	private List<String>    packageNamePrefixListForNotPrintFilename; // 로그에 파일명+라인번호를 표시하지 않을 예외 클래스들 (프레임워크 클래스등) 
	private boolean         printFilenameAndLineNumber;
	
	public FileLoggerFactory () {
	}

    public void init() {
		if (encoding == null) {
			encoding = System.getProperty("file.encoding");
		}
		
		File baseDirFile = new File(baseDir);
		if (!baseDirFile.exists()) {
		    baseDirFile.mkdirs();
		}
		
		try {
            baseDir = baseDirFile.getCanonicalPath();
        } catch (IOException e) {
            throw Util.toRuntimeException(e);
        }
	}

	public void destroy() {
	}

    public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public boolean isSeparatePerDay() {
		return separatePerDay;
	}

	public void setSeparatePerDay(boolean separatePerDay) {
		this.separatePerDay = separatePerDay;
	}

	public boolean isSeparatePerJobId() {
		return separatePerJobId;
	}

	public void setSeparatePerJobId(boolean separatePerJobId) {
		this.separatePerJobId = separatePerJobId;
	}

    public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

    public String getDefaultLogLevel() {
		return defaultLogLevel;
	}

	public void setDefaultLogLevel(String defaultLogLevel) {
		this.defaultLogLevel = defaultLogLevel;
	}

	public String getFilenamePattern() {
        return filenamePattern;
    }

    public void setFilenamePattern(String filenamePattern) {
        this.filenamePattern = filenamePattern;
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

	// =============================================================
	
	public File getLogFile(JobLogFilenameInfo info) {
		return getLogFile(info.getProcDate(), info.getJobId(), info.getJobInstanceId());
	}
	
	private File getLogFile(String procDate, String jobId, String jobInstanceId) {
		String path = baseDir + "/" + 
		(separatePerDay ? procDate+"/" : "") +
		(separatePerJobId ? jobId+"/" : "");

		String filename = null;
		if (Util.isBlank(filenamePattern)) {
		    filename = jobInstanceId+".log";
		}else {
		    // filenamePattern 지정시 다음의 변수들을 사용할 수 있다.
		    // ${PROC_DATE}, ${JOB_ID}, ${JOB_INS_ID}, ${SHELL_NAME}, ${JVM_PID}
		    filename = filenamePattern;
			filename = filename.replaceAll("\\$\\{SHELL_NAME\\}", System.getProperty("SHELL_NAME"));
			filename = filename.replaceAll("\\$\\{JVM_PID\\}",    System.getProperty("JVM_PID"));
			filename = filename.replaceAll("\\$\\{STARTTIME\\}",  System.getProperty("STARTTIME"));
			filename = filename.replaceAll("\\$\\{JOB_ID\\}",     jobId);
			filename = filename.replaceAll("\\$\\{JOB_INS_ID\\}", jobInstanceId);
			filename = filename.replaceAll("\\$\\{PROC_DATE\\}",  procDate);
			filename = filename.replaceAll("\\$\\{DATE\\}",       Util.getCurrentYYYYMMDD());
		}
		File file = new File(path, filename);
		return file;
	}

	public ILogger createLogger(JobContext context) {
		try {
			File file = getLogFile(context.getJobExecution().getProcDate(), context.getJobExecution().getJobId(), context.getJobExecution().getJobInstanceId());
			file.getParentFile().mkdirs();
			FileLogger fl = new FileLogger(file);
			fl.setEncoding(encoding);

			if (Util.isBlank(context.getJobExecution().getLogLevel())) { // Job 에 로그레벨 미지정시에는 기본 레벨
				fl.setLogLevelByName(defaultLogLevel); // default loglevel
				context.getJobExecution().setLogLevel(defaultLogLevel);
			}else {
				fl.setLogLevelByName(context.getJobExecution().getLogLevel()); // 로그레벨 조정.
			}
			
			if (packageNamePrefixListForNotPrintFilename != null && packageNamePrefixListForNotPrintFilename.size() > 0) {
			    fl.setPackageNamePrefixListForNotPrintFilename(packageNamePrefixListForNotPrintFilename);
			}
			fl.setPrintFilenameAndLineNumber(printFilenameAndLineNumber);
			fl.init();
			
			return fl;
		}catch(Exception e) {
			throw new AgentException("agent.fail.init.logger", e);
		}
	}
}
