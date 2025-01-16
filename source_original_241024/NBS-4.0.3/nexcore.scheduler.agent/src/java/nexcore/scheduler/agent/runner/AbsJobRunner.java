package nexcore.scheduler.agent.runner;

import java.io.File;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.IJobRunner;
import nexcore.scheduler.agent.ISubLogFilenameResolver;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.internal.AgentMain;
import nexcore.scheduler.agent.internal.JobExecutionBoard;
import nexcore.scheduler.agent.internal.JobRunThreadManager;
import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.agent.prepost.IPrePostProcessorManager;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Runner </li>
 * <li>작성일 : 2010. 4. 22.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public abstract class AbsJobRunner implements IJobRunner {
	protected  ISubLogFilenameResolver  subLogFilenameResolver;
	protected  IPrePostProcessorManager prePostProcessorManager;
	
	protected  AgentMain    agentMain;
	protected  Log          log;
	
	public AbsJobRunner() {
		this.log       = LogManager.getAgentLog();
	}

	public ISubLogFilenameResolver getSubLogFilenameResolver() {
		return subLogFilenameResolver;
	}

	public void setSubLogFilenameResolver(ISubLogFilenameResolver subLogFilenameResolver) {
		this.subLogFilenameResolver = subLogFilenameResolver;
	}
	
	public IPrePostProcessorManager getPrePostProcessorManager() {
        return prePostProcessorManager;
    }

    public void setPrePostProcessorManager(IPrePostProcessorManager prePostProcessorManager) {
        this.prePostProcessorManager = prePostProcessorManager;
    }

    public AgentMain getAgentMain() {
		return agentMain;
	}

	public void setAgentMain(AgentMain agentMain) {
		this.agentMain = agentMain;
	}

	public JobExecutionBoard getJobExecutionBoard() {
		return agentMain.getJobExecutionBoard();
	}

	public JobRunThreadManager getJobRunThreadManager() {
		return agentMain.getJobRunThreadManager();
	}

	/**
	 * 로거 초기화
	 * initLogger
	 *  
	 * @param context
	 */
	protected void initLogger(JobContext context) {
	    agentMain.getJobLogManager().getLog(context);
	}
	
	// 배치 선처리 메소드
	public final void logJobStart(JobContext context) {
		try {
			ILogger logger = agentMain.getJobLogManager().getLog(context);
			logger.info("#################### STARTING BATCH JOB ####################");
			logger.info("### Job ID           : "+context.getJobExecution().getJobId());
			logger.info("### Job Instance ID  : "+context.getJobExecution().getJobInstanceId());
			logger.info("### Job Execution ID : "+context.getJobExecution().getJobExecutionId());
			logger.info("### Description      : "+context.getJobExecution().getDescription());
			logger.info("### Start time       : "+Util.getDatetimeLocalizedText(context.getJobExecution().getStartTime()));
			logger.info("### Job Type         : "+context.getJobExecution().getJobType());
			logger.info("### Agent Node       : "+context.getJobExecution().getAgentNode());
			logger.info("### Component name   : "+context.getJobExecution().getComponentName());
			logger.info("### Operator Type    : "+context.getJobExecution().getOperatorType());
			logger.info("### Operator ID      : "+context.getJobExecution().getOperatorId());
			logger.info("### Operator IP      : "+context.getJobExecution().getOperatorIp());
			logger.info("### PROC_DATE        : "+context.getJobExecution().getProcDate());
			logger.info("### Input Parameter  : "+context.getJobExecution().getInParameters());
			logger.info("### Thread priority  : "+Thread.currentThread().getPriority());
			logger.info("#################### STARTING BATCH JOB ####################");
		}catch(Throwable e) {
			// 로그파티션이 full 났을 경우는 여기서 에러를 throw 해야지 다음 진행이 안된다.
			Util.logError(log, context.getJobExecution().getJobExecutionId()+"/"+ MSG.get("agent.fail.write.joblog"), e);
			throw new AgentException("agent.fail.write.joblog", e);
		}
	}

	/**
	 * Job End 로그 기록.
	 * 이 메소드에서는 어떠한 경우도 throw 하지 않는다.
	 * 혹시 로그 기록중 에러 발생하는 경우 (file system full 등) Job End Fail 처리하지만
	 * 이 메소드가 throw 하지는 않든다.
	 * 이 메소드 후에 callback 이 정상적으로 되도록 하기 위함.
	 * @param context
	 */
	public final void logJobEnd(JobContext context) {
		ILogger logger = null;
		try {
			logger = agentMain.getJobLogManager().getLog(context);

			logger.info("#################### ENDING BATCH JOB ######################");
			logger.info("### Job ID           : "+context.getJobExecution().getJobId());
			logger.info("### Job Instance ID  : "+context.getJobExecution().getJobInstanceId());
			logger.info("### Job Execution ID : "+context.getJobExecution().getJobExecutionId());
			logger.info("### Description      : "+context.getJobExecution().getDescription());
			logger.info("### Start time       : "+Util.getDatetimeLocalizedText(context.getJobExecution().getStartTime()));
			logger.info("### End time         : "+Util.getDatetimeLocalizedText(context.getJobExecution().getEndTime()));
			logger.info("### Elap time        : "+((context.getJobExecution().getEndTime()-context.getJobExecution().getStartTime())/1000)+" s");
			logger.info("### Job Type         : "+context.getJobExecution().getJobType());
			logger.info("### Agent Node       : "+context.getJobExecution().getAgentNode());
			logger.info("### Component name   : "+context.getJobExecution().getComponentName());
			logger.info("### Operator Type    : "+context.getJobExecution().getOperatorType());
			logger.info("### Operator ID      : "+context.getJobExecution().getOperatorId());
			logger.info("### Operator IP      : "+context.getJobExecution().getOperatorIp());
			logger.info("### PROC_DATE        : "+context.getJobExecution().getProcDate());
			logger.info("### Input Parameter  : "+context.getJobExecution().getInParameters());
			logger.info("### Return Code      : "+context.getJobExecution().getReturnCode());
			logger.info("### Return Values    : "+context.getJobExecution().getReturnValues());
			logger.info("### Error Message    : "+context.getJobExecution().getErrorMsg());
			logger.info("### Progress Total   : "+context.getJobExecution().getProgressTotal());
			logger.info("### Progress Current : "+context.getJobExecution().getProgressCurrent());
			logger.info("#################### ENDING BATCH JOB ######################");
			logger.info("");
		}catch(Throwable e) {
			// 로그파티션이 full 났을 경우는 End Fail 처리한다. throw 는 하지 않는다. 뒤에서 callback메소드가 제대로 호출되야하므로.
			Util.logError(log, context.getJobExecution().getJobExecutionId()+"/"+ MSG.get("agent.fail.write.joblog"), e);
			int    srcResultCode = context.getJobExecution().getReturnCode();
			String srcErrorMsg   = context.getJobExecution().getErrorMsg();
			context.getJobExecution().setReturnCode(91);
			context.getJobExecution().setErrorMsg(MSG.get("agent.fail.write.joblog")+" / ResultCode="+srcResultCode+",ErrorMsg="+srcErrorMsg);
		}finally {
			try {
				logger.close();
			}catch(Exception ignore) {}
		}
	}
	
	/**
	 * 서브 로그 파일명을 조립한다 
	 * @param filenameInfo
	 * @return SUB 로그 파일 File 객체
	 */
	public File getSubLogFile(JobLogFilenameInfo filenameInfo) {
		if (subLogFilenameResolver != null) {
			return subLogFilenameResolver.getSubLogFile(filenameInfo);
		}else {
			return null;
		}
	}

	/**
	 * JobExecution 선처리기 실행
	 * 
	 * @param context
	 */
	protected void doJobExePreProcessors(JobContext context) {
	    if (prePostProcessorManager!= null) {
	        // JobExecution 선처리기 실행.
	        initLogger(context);
	        prePostProcessorManager.doJobExePreProcessors(context);
	    }
	}
	
	/**
	 * JobExecution 후처리기 실행
	 *  
	 * @param context
	 */
	protected void doJobExePostProcessors(JobContext context, Throwable e) {
	    if (prePostProcessorManager!= null) {
	        // JobExecution 후처리기 실행.
	        prePostProcessorManager.doJobExePostProcessors(context, e);
	    }
	}
}
