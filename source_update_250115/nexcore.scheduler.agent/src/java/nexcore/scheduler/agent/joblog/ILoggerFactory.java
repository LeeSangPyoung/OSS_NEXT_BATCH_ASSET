package nexcore.scheduler.agent.joblog;

import java.io.File;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.entity.JobLogFilenameInfo;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Logger를 만드는 놈 </li>
 * <li>작성일 : 2010. 5. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface ILoggerFactory {

	public ILogger createLogger(JobContext context);

//    public abstract File getLogFile(String procDate, String jobId, String jobInstanceId);
    public abstract File getLogFile(JobLogFilenameInfo info);

    public abstract String getFilenamePattern();

    public abstract String getEncoding();

    public abstract String getBaseDir();

    public abstract void init();

}