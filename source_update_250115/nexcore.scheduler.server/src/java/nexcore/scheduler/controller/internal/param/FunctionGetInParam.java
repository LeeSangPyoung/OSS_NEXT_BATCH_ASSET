package nexcore.scheduler.controller.internal.param;

import java.util.Arrays;

import org.apache.commons.logging.Log;

import nexcore.scheduler.core.internal.JobInstanceManager;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 의 입력 파라미터 조회. 동일 PROC_DATE 의 최종 인스턴스의 입력 파라미터를 조회함</li>
 * <li>작성일 : 2012. 4. 18.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionGetInParam implements ParameterFunction {
	private JobInstanceManager  jobInstanceManager;
	private Log                 log;
	
	public FunctionGetInParam() {
		log = LogManager.getSchedulerLog();
	}
	
	public JobInstanceManager getJobInstanceManager() {
		return jobInstanceManager;
	}

	public void setJobInstanceManager(JobInstanceManager jobInstanceManager) {
		this.jobInstanceManager = jobInstanceManager;
	}



	public String getName() {
		return "GETINPARAM";
	}

	public String evaluate(String[] operands, ParameterContext paramContext) {
		// ( GETINPARAM JOBID PARAM_NAME )
		
		if (operands.length != 2) {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}else {
			try {
				String lastJobInstanceId  = jobInstanceManager.getLastJobInstanceId(operands[0], (String)paramContext.getParameters().get("PROC_DATE"));
				if (log.isDebugEnabled()) {
					log.debug("JobId:"+operands[0]+", LastJobInstnaceId="+lastJobInstanceId+", PROC_DATE="+paramContext.getParameters().get("PROC_DATE"));
				}
				if (!Util.isBlank(lastJobInstanceId)) {
					JobInstance jobins = jobInstanceManager.getJobInstanceDeep(lastJobInstanceId);
					if (jobins != null) {
						return jobins.getInParameters().get(operands[1]);
					}
				}					
			}catch(Exception e) {
				throw new SchedulerException("main.param.evaluate.function.error", e, "GETINPARAM", Arrays.asList(operands).toString());
			}
		}
		return null;
	}
}
