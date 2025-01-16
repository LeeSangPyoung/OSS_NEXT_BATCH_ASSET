package nexcore.scheduler.agent.runner;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.entity.JobExecution;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : DB Stored Procedure 호출 전용 Job Runner. </li>
 * <li>작성일 : 2013. 10. 19.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

/*
 * DBProcedure 인 경우는 component 명을 "nexcore.scheduler.agent.runner.DBProcedureJob" 라고 고정세팅하고
 * 나머지는 PojoJobRunner 와 동일하게 동작한다.
 */
public class DBProcedureJobRunner extends PojoJobRunner {

	public DBProcedureJobRunner() {
	}

	public void init() {
	}
	
	public void destroy() {
	}
	
	public void start(final JobExecution je, final JobContext context, final IJobRunnerCallBack jobRunnerCallBack) {
		je.setComponentName(DBProcedureJob.class.getName());
		super.start(je, context, jobRunnerCallBack);
	}
}