package nexcore.scheduler.agent.runner;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.entity.JobExecution;

/**
 * <ul>
 * <li>업무 그룹명 : 배치 스케줄러</li>
 * <li>서브 업무명 : 에이전트</li>
 * <li>설  명 : 센터컷 CCRunner 는 배치 프로그램이므로 JBatchJobRunner 를 상속 받는다.</li>
 * <li>작성일 : 2016. 7. 5.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class CenterCutJobRunner extends JBatchJobRunner {
    private String ccRunnerClassName = "nexcore.framework.centercut.batch.CCRunner"; // 기본값

	public CenterCutJobRunner() {
	}

	public void init() {
		super.init();
	}
	
	public void destroy() {
		super.destroy();
	}
	
	public String getCcRunnerClassName() {
        return ccRunnerClassName;
    }

    public void setCcRunnerClassName(String ccRunnerClassName) {
        this.ccRunnerClassName = ccRunnerClassName;
    }

    public void start(final JobExecution je, final JobContext context, final IJobRunnerCallBack jobRunnerCallBack) {
	    // 센터컷 실행은 배치 프레임워크 영역에서 실행된다.
		context.getInParameters().put("PROGRAM_NAME", ccRunnerClassName);
		super.start(je, context, jobRunnerCallBack);
	}
}