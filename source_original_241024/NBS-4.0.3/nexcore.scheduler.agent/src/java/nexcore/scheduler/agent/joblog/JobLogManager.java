package nexcore.scheduler.agent.joblog;

import nexcore.scheduler.agent.JobContext;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 로그 관리자, 여기에서 Logger 객체를 얻는다. </li>
 * <li>작성일 : 2010. 5. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobLogManager {
	private ILoggerFactory factory;
	
    public JobLogManager() {
    }
	
	public void init() {
	}
	
	public void destroy() {
	}

	public ILoggerFactory getFactory() {
        return factory;
    }

    public void setFactory(ILoggerFactory factory) {
        this.factory = factory;
    }

	public String getBaseDir() {
        return factory.getBaseDir();
    }

	public String getEncoding() {
	    return factory.getEncoding();
	}
	
    public ILogger getLog(JobContext context) {
		ILogger logger = (ILogger)context.getLogger();
		if (logger == null || !logger.isValid()) {
			if (logger != null) {
				logger.close();  // invalid 할 경우. close 하고 다시시작. 
			}
			logger = factory.createLogger(context);
			context.setLog(logger);
		}
		return logger;
	}
	
}
