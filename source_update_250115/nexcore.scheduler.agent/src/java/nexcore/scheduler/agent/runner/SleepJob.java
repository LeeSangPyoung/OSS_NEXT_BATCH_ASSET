/**
 * 
 */
package nexcore.scheduler.agent.runner;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2012. 12. 3.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class SleepJob {
	
	private Log log;
	private JobExecution jobExecution;
	private IJobRunnerCallBack callback;
	
	public void onSuspend() {
		jobExecution.setState(JobExecution.STATE_SUSPENDED);
		
		log.info("### SUSPENDED ###");
	}
	
	public void onResume() {
		jobExecution.setState(JobExecution.STATE_RUNNING);
		synchronized (jobExecution) {
			jobExecution.notify();
		}
		
		log.info("### RESUMED ###");
		
		callback.callBackJobResume(jobExecution);
	}
	
	public void onStop() {
		jobExecution.setState(JobExecution.STATE_ENDED);
		if(jobExecution.getState() == JobExecution.STATE_SUSPENDED)
			synchronized (jobExecution) {
				jobExecution.notify();
			}
		
		log.info("### ENDED ###");
	}
	
    private void init(JobContext context){
    	log = context.getLogger();
    	jobExecution = context.getJobExecution();
    	callback = context.getJobRunnerCallBack();
    }
    
    private void pause(){
    	try {
    		synchronized (jobExecution) {
    			callback.callBackJobSuspend(jobExecution);
    			
    			jobExecution.wait(6000);
    		}
    	}catch (InterruptedException e) {
    		log.info("Interrupted");
    	}
    }
    
	public void execute(JobContext context) {
		init(context);
		
		String sleepTimeStr = context.getInParameter("SLEEP_TIME");
		long   sleepTime    = 0l;
		if (Util.isBlank(sleepTimeStr)) {
			throw new AgentException("com.job.parameter.required", "SLEEP_TIME");
		}
		
		try {
			sleepTime = Long.parseLong(sleepTimeStr);
		}catch(Exception e) {
			throw new AgentException("com.job.wrong.parameter", "SLEEP_TIME", sleepTimeStr);
		}
			
		log.info(MSG.get("agent.sleeptype.begin.sleep", sleepTime));
		
		context.setProgressTotal(sleepTime);
		
		long accumulatedTime = 0l;
		long timeBefore = System.currentTimeMillis();
		long timeAfter  = 0;
		long timeDiff   = 0;
		long sleepTimeMillis = 1000;
		while(accumulatedTime < sleepTime) {
			while(jobExecution.getState() == JobExecution.STATE_SUSPENDED)
				pause();
			
			if(jobExecution.getState() == JobExecution.STATE_ENDED)
				break;
			
			Util.sleep(sleepTimeMillis);
			timeAfter = System.currentTimeMillis();
			timeDiff = timeAfter - timeBefore;
			if (log.isDebugEnabled()) {
				log.debug("Time diff : "+timeDiff);
			}
			
			if (timeDiff > 1000 && timeDiff < 2000) {
				timeDiff = timeDiff - 1000;
				// suspend 없는 일반적인 경우. 시간 계산해서 정확한 sleep time 을 계산한다.
				sleepTimeMillis = 1000 - timeDiff; // 다음번 sleep 에서는 오차만큼 빼서 정확성을 기한다.
			}else {
				sleepTimeMillis = 1000;
			}
			timeBefore = timeAfter;
			if (log.isDebugEnabled()) {
				log.debug("Sleep time : "+sleepTimeMillis);
			}
			accumulatedTime += 1;
			context.setProgressCurrent(accumulatedTime);
		}
	}

}
