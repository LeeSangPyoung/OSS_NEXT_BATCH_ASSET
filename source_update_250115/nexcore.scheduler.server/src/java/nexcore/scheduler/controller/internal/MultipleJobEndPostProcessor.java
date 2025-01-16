package nexcore.scheduler.controller.internal;

import java.util.List;

import org.apache.commons.logging.Log;

import nexcore.scheduler.controller.IJobEndPostProcessor;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job End 후처리 로직을 돌림. 여러개를 설정할 수 있도록 Multiple로 설정가능하게 하는 놈.</li>
 * <li>작성일 : 2011. 07. 18.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class MultipleJobEndPostProcessor implements IJobEndPostProcessor {
	private List<IJobEndPostProcessor> processors;
	
	private Log                        log;
	
	public void init() {
		log = LogManager.getSchedulerLog();
		Util.logServerInitConsole("JobEndPostProcessor", String.valueOf(processors));
	}
	
	public void destroy() {
	}
	
	public List<IJobEndPostProcessor> getProcessors() {
		return processors;
	}

	public void setProcessors(List<IJobEndPostProcessor> processors) {
		this.processors = processors;
	}

	public boolean doPostProcess(JobExecution jobexe) {
		int errcnt = 0; 
		if (processors != null) {
			for (IJobEndPostProcessor proc : processors) {
				try {
					proc.doPostProcess(jobexe);
				}catch(Throwable e) {
					errcnt ++;
					Util.logError(log, MSG.get("main.endproc.postproc.error", jobexe.getJobExecutionId()), e); // 후처리 실행 중 에러가 발생하였습니다
				}
			}
		}
		return errcnt == 0;
	}
}
