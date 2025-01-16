/**
 * 
 */
package nexcore.scheduler.agent.prepost;

import java.util.List;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 배치에이전트에서 Job 실행 전후에 호출되는 선후처리기 들 관리. singlejvm 에서의 선후처리와 호환되지 않으며, Jvm PrePost 는 없다. </li>
 * <li>작성일 : 2015. 8. 31.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class DefaultPrePostProcessorManager implements IPrePostProcessorManager {

	private List<IJobExePreProcessor>    jobExePreProcessors;
	private List<IJobExePostProcessor>   jobExePostProcessors;
	
	private Log  log;
	
	public void init() {
		log = LogManager.getAgentLog();
	}

	public void destroy() {
	}
	
	public List<IJobExePreProcessor> getJobExePreProcessors() {
		return jobExePreProcessors;
	}
	
	public void setJobExePreProcessors(List<IJobExePreProcessor> jobExePreProcessors) {
		this.jobExePreProcessors = jobExePreProcessors;
	}
	
	public List<IJobExePostProcessor> getJobExePostProcessors() {
		return jobExePostProcessors;
	}
	
	public void setJobExePostProcessors(List<IJobExePostProcessor> jobExePostProcessors) {
		this.jobExePostProcessors = jobExePostProcessors;
	}

	/**
	 * 에이전트의 Job 실행 전 선처리 실행
	 * @see nexcore.scheduler.agent.prepost.IPrePostProcessorManager#doJobExePreProcessors(nexcore.scheduler.agent.JobContext)
	 */
	public void doJobExePreProcessors(JobContext context) {
		if (jobExePreProcessors !=null) {
			for (IJobExePreProcessor p : jobExePreProcessors) {
				try {
				    context.getLogger().info("Processing preprocessor. "+p);
					p.doPreProcess(context);
				}catch(Throwable e) {
					Util.logError(log, "Processing "+p, e);
					throw new AgentException("agent.fail.to.execute.preprocessor", e);
				}
			}
		}
	}

	/**
	 * 에이전트의 Job 실행 후 후처리 실행
	 * @see nexcore.scheduler.agent.prepost.IPrePostProcessorManager#doJobExePostProcessors(nexcore.scheduler.agent.JobContext, java.lang.Throwable)
	 */
	public void doJobExePostProcessors(JobContext context, Throwable e) {
		if (jobExePostProcessors !=null) {
			for (IJobExePostProcessor p : jobExePostProcessors) {
				try {
				    context.getLogger().info("Processing preprocessor. "+p);
					p.doPostProcess(context, e);
				}catch(Throwable ee) {
					Util.logError(log, "Processing "+p, ee);
					throw new AgentException("agent.fail.to.execute.postprocessor", e);
				}
			}
		}
	}
	
}
