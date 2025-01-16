package nexcore.scheduler.agent.runner;

import org.apache.commons.logging.Log;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : QuartJob 을 수동으로 trigger 하는 JobType.</li>
 * <li>작성일 : 2015. 10. 20.</li>
 * <li>작성자 : 정호철</li>
 * <li>since  : 3.8.0</li>
 * </ul>
 */
public class QuartzJobRunner extends AbsJobRunner {
	private Log                  log;
	private Scheduler            scheduler;
	
	public QuartzJobRunner() {
	}
	
	public void init() {
		log = LogManager.getAgentLog();
        try {
            scheduler.addGlobalJobListener(new JobListener() {
                public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
                    if ("NEXCORE".equals(context.getTrigger().getGroup())) {
                        // NEXCORE 가 fire 한 Job임.
                        String jobExeId = context.getTrigger().getName();
                        JobContext   jobContext = agentMain.getJobExecutionBoard().getJobContext(jobExeId);
                        JobExecution jobexe     = jobContext.getJobExecution();
                        
                        if (jobException == null) {
                            log.info("Quartz "+context+" executed without errors.");
                            jobContext.getLogger().info("Quartz "+context+" executed without errors.");
                        }else {
                            log.error("Quartz "+context+" failed.", jobException);
                            jobContext.getLogger().error("Quartz "+context+" failed", jobException);
                        }
                        end(jobexe, jobContext, jobException == null ? 0 : 80, jobException == null ? null : jobException.getMessage());
                    }
                }
                
                public void jobToBeExecuted(JobExecutionContext context) {
                }
                
                public void jobExecutionVetoed(JobExecutionContext context) {
                    if ("NEXCORE".equals(context.getTrigger().getGroup())) {
                        String jobExeId = context.getTrigger().getName();
                        JobContext   jobContext = agentMain.getJobExecutionBoard().getJobContext(jobExeId);
                        JobExecution jobexe     = jobContext.getJobExecution();
                        log.info("Quartz "+context+" executed without errors.");
                        jobContext.getLogger().info("Quartz "+context+" executed without errors.");

                        end(jobexe, jobContext, 81, "vetoed");
                    }
                }
                
                public String getName() {
                    return "NexcoreQuartzJobListener";
                }
            });
        } catch (SchedulerException e) {
            throw new AgentException("com.error.occurred.while", e, "Quartz JobListener creation");
        }            

	}
	
	public void destroy() {
	}

	public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void start(JobExecution je, JobContext context, IJobRunnerCallBack jobRunnerCallBack) {
        ILogger log = getAgentMain().getJobLogManager().getLog(context);

        try {
            // 선처리 실행
            doJobExePreProcessors(context);

            logJobStart(context);

            String inJobGroup = context.getInParameter("JOB_GROUP");
            String inJobName  = context.getInParameter("JOB_NAME");
            log.info("QuartzJob JOB_GROUP="+inJobGroup);
            log.info("QuartzJob JOB_NAME ="+inJobName);
            
            SimpleTrigger trigger = new SimpleTrigger(je.getJobExecutionId(), "NEXCORE");
            trigger.setName(je.getJobExecutionId()); // End 이벤트 발생시 구분하기 위해 JobExecutionId로 트리거 생성
            trigger.setJobName(inJobName);
            trigger.setJobGroup(inJobGroup);
            log.info("Quartz Job : "+inJobGroup+"."+inJobName+" will be triggered.");
            scheduler.scheduleJob(trigger);
        }catch (Throwable e) {
            String msg = MSG.get("agent.fail.start.job", je.getJobExecutionId());
            Util.logError(log, msg, e);
            end(je, context, 3, msg+"/"+e.toString());
        }
	}	
    
    /**
     * Job 종료 처리. 
     * @param je
     * @param context
     * @param returnCode
     * @param errorMsg
     * @param jobRunnerCallBack
     */
    public void end(JobExecution je, JobContext context, int returnCode, String errorMsg) {
        je.setEndTime(System.currentTimeMillis());
        je.setReturnCode(returnCode);
        je.setErrorMsg(errorMsg);
        je.setReturnValues(context.getReturnValues());
        je.setState(JobExecution.STATE_ENDED);
        try {
            logJobEnd(context);
        }catch(Throwable e) {
            Util.logError(log, "logJobEnd() fail/"+je.getJobExecutionId(), e); 
        }
        
        try {
            agentMain.getJobRunnerCallBack().callBackJobEnd(je);
        }finally {
            getJobExecutionBoard().remove(je.getJobExecutionId());
            
            // 후처리 실행
            doJobExePostProcessors(context, null);
        }
    }
    
    public void suspend(String jobExecutionId) {
        throw new AgentException("agent.proctype.unsupported.operation", "suspend");
    }
    
    public void resume(String jobExecutionId) {
        throw new AgentException("agent.proctype.unsupported.operation", "resume");
    }

    public void stop(String jobExecutionId) {
        throw new AgentException("agent.proctype.unsupported.operation", "stop");
    }
}
