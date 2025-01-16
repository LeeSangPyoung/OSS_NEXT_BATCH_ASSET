package nexcore.scheduler.controller.internal;

import java.util.List;

import org.apache.commons.logging.Log;

import nexcore.scheduler.controller.IJobRunPreProcessor;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Run 전처리 로직을 돌림. 여러개를 설정할 수 있도록 Multiple로 설정가능하게 하는 놈.</li>
 * <li>작성일 : 2011. 11. 30.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class MultipleJobRunPreProcessor implements IJobRunPreProcessor {
	private List<IJobRunPreProcessor> processors;
	
	private Log                        log;
	
	public void init() {
		log = LogManager.getSchedulerLog();
		Util.logServerInitConsole("JobRunPreProcessor", String.valueOf(processors));
	}
	
	public void destroy() {
	}
	
	public List<IJobRunPreProcessor> getProcessors() {
		return processors;
	}

	public void setProcessors(List<IJobRunPreProcessor> processors) {
		this.processors = processors;
	}

	public void doPreProcess(JobInstance jobins, JobExecution jobexe) {
		if (processors != null) {
			for (IJobRunPreProcessor proc : processors) {
				try {
					proc.doPreProcess(jobins, jobexe);
				}catch(Throwable e) {
					Util.logError(log, MSG.get("main.endproc.preproc.error", jobexe.getJobExecutionId()), e);
					throw new SchedulerException("main.endproc.preproc.error", jobexe.getJobExecutionId(), e); // 전처리 실행 중 에러가 발생하였습니다
				}
			}
		}
	}
}
