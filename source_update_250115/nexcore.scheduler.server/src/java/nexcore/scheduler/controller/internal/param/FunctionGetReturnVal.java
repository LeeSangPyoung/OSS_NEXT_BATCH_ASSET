package nexcore.scheduler.controller.internal.param;

import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.logging.Log;

import nexcore.scheduler.controller.internal.JobExecutionManager;
import nexcore.scheduler.core.internal.JobInstanceManager;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 의 리턴값 조회 함수. NBS_JOB_EXE_RETVAL 테이블을 조회하여 최종 Job Execution 의 리턴값을 조회하는 함수</li>
 * <li>작성일 : 2010. 10. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionGetReturnVal implements ParameterFunction {
	private JobExecutionManager jobExecutionManager;
	private JobInstanceManager  jobInstanceManager;
	private Log                 log;
	
	public FunctionGetReturnVal() {
		log = LogManager.getSchedulerLog();
	}
	
	public JobExecutionManager getJobExecutionManager() {
		return jobExecutionManager;
	}

	public void setJobExecutionManager(JobExecutionManager jobExecutionManager) {
		this.jobExecutionManager = jobExecutionManager;
	}

	public JobInstanceManager getJobInstanceManager() {
		return jobInstanceManager;
	}

	public void setJobInstanceManager(JobInstanceManager jobInstanceManager) {
		this.jobInstanceManager = jobInstanceManager;
	}



	public String getName() {
		return "GETRETVAL";
	}

	public String evaluate(String[] operands, ParameterContext paramContext) {
		// ( GETRETVAL JOBID RETVALNAME )
		
		if (operands.length != 2) {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}else {
			try {
				String lastJobInstanceId  = jobInstanceManager.getLastJobInstanceId(operands[0], (String)paramContext.getParameters().get("PROC_DATE"));
				if (log.isDebugEnabled()) {
					log.debug("JobId:"+operands[0]+", LastJobInstnaceId="+lastJobInstanceId+", PROC_DATE="+paramContext.getParameters().get("PROC_DATE"));
				}
				if (!Util.isBlank(lastJobInstanceId)) {
					String lastJobExecutionId = jobExecutionManager.getLastEndedJobExecutionId(lastJobInstanceId);
					if (log.isDebugEnabled()) {
						log.debug("JobInstnaceId="+lastJobInstanceId+", JobExecutionId="+lastJobExecutionId);
					}
					if (!Util.isBlank(lastJobExecutionId)) {
						Properties returnValues = jobExecutionManager.getReturnValues(lastJobExecutionId);
						if (returnValues != null) {
							return Util.nvl(returnValues.getProperty(operands[1]));
						}
					}
				}
			}catch(Exception e) {
				throw new SchedulerException("main.param.evaluate.function.error", e, "GETRETVAL", Arrays.asList(operands).toString());
			}
		}
		return "";
	}
}
