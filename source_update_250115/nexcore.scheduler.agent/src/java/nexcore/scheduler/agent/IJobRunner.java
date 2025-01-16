package nexcore.scheduler.agent;

import java.io.File;

import nexcore.scheduler.agent.internal.AgentMain;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobLogFilenameInfo;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Runner  인터페이스. (refactoring)</li>
 * <li>작성일 : 2013. 1. 31.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IJobRunner {
	
	public AgentMain getAgentMain();
	public void setAgentMain(AgentMain agentMain);
	
	/**
	 * 메인 Job 로그 이외에 추가 로그가 필요한 경우에 사용됨
	 * 현재는 CBATCH 타입에서 배치 로그에 기록한 파일을 스케줄러 화면에서 읽어 올때 사용한다.
	 * @param filenameInfo
	 * @return
	 */
	public File getSubLogFile(JobLogFilenameInfo filenameInfo);
	
	public void start(JobExecution je, JobContext context, IJobRunnerCallBack jobRunnerCallBack);
	public void suspend(String jobExecutionId);
	public void resume(String jobExecutionId);
	public void stop(String jobExecutionId);
}
