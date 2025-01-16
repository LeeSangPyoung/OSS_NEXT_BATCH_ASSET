/**
 * 
 */
package nexcore.scheduler.agent.nsc;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.internal.AgentMain;
import nexcore.scheduler.agent.internal.JobExecutionBoard;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2012. 9. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class NSCIntegrator implements Runnable {
	
	private INSCClientFactory   nscClientFactory;
	private long                pollingInterval = 2000; // 기본값은 2초.
	private boolean             enabled;
	private AgentMain           agentMain;
	
	private JobExecutionBoard   jobExecutionBoard;
	private IJobRunnerCallBack  jobRunnerCallBack;
	private Thread              thisThread;
	
	private Log                 log;
	
	public NSCIntegrator() {
	}

	public void init() {
		log = LogManager.getLog("nsc");
		if (enabled) {
			jobExecutionBoard = agentMain.getJobExecutionBoard();
			thisThread = new Thread(this, "NSCIntegrator");
			thisThread.setDaemon(true);
			thisThread.start();
			Util.logServerInitConsole("NSCIntegrator", "(Interval:"+pollingInterval+",enable:"+enabled+")");
		}
	}
	
	public void destroy() {
		enabled = false;
		thisThread.interrupt();
	}
	
	public INSCClientFactory getNscClientFactory() {
		return nscClientFactory;
	}

	public void setNscClientFactory(INSCClientFactory nscClientFactory) {
		this.nscClientFactory = nscClientFactory;
	}

	public AgentMain getAgentMain() {
		return agentMain;
	}

	public void setAgentMain(AgentMain agentMain) {
		this.agentMain = agentMain;
	}

	public IJobRunnerCallBack getJobRunnerCallBack() {
		return jobRunnerCallBack;
	}

	public void setJobRunnerCallBack(IJobRunnerCallBack jobRunnerCallBack) {
		this.jobRunnerCallBack = jobRunnerCallBack;
	}

	public long getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(long pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * 건별로 진행률 값을 읽음. 주로 Process Exit 시점에 호출됨.
	 * @param jobExecutionId
	 * @return
	 * @throws IOException
	 */
	public long[] getJobProgress(String jobExecutionId) {
		INSCClient client = null;
		try {
			client = nscClientFactory.getClient();
			return client.getJobProgress(jobExecutionId);
		}catch(Exception e) {
			Util.logError(log, "NSC Communication error. (getJobProgress)", e);
			return null;
		}finally {
			if (client != null) {
				nscClientFactory.returnClient(client);
			}
		}
	}
	
	/**
	 * 건별로 진행률 값을 읽음. 주로 Process Exit 시점에 호출됨.
	 * @param componentName
	 * @param jobInstanceId
	 * @return 로그 파일명 절대경로
	 * @throws IOException
	 */
	public String getCBatchLogFilename(String componentName, String jobInstanceId) {
		INSCClient client = null;
		try {
			client = nscClientFactory.getClient();
			return client.getLogFilename(componentName, jobInstanceId);
		}catch(Exception e) {
			Util.logError(log, "NSC Communication error. (getCBatchLogFile)", e);
			return null;
		}finally {
			if (client != null) {
				nscClientFactory.returnClient(client);
			}
		}
	}
	
	/**
	 * 일시정지 (정지)
	 * @param jobExecutionId
	 */
	public void suspend(String jobExecutionId) {
		INSCClient client = null;
		try {
			client = nscClientFactory.getClient();
			String result = client.suspend(jobExecutionId);
			if (!"OK".equals(result)) {
				throw new Exception("NSC Protocol error");
			}
		}catch(Exception e) {
			Util.logError(log, "NSC Communication error. (suspend)", e);
			throw Util.toRuntimeException(e);
		}finally {
			if (client != null) {
				nscClientFactory.returnClient(client);
			}
		}
	}
	
	/**
	 * 다시실행 (재개)
	 * @param jobExecutionId
	 */
	public void resume(String jobExecutionId) {
		INSCClient client = null;
		try {
			client = nscClientFactory.getClient();
			String result = client.resume(jobExecutionId);
			if (!"OK".equals(result)) {
				throw new Exception("NSC Protocol error");
			}
		}catch(Exception e) {
			Util.logError(log, "NSC Communication error. (suspend)", e);
			throw Util.toRuntimeException(e);
		}finally {
			if (client != null) {
				nscClientFactory.returnClient(client);
			}
		}
	}

	/**
	 * delete Memory (종료확정, 메모리 삭제)
	 * @param jobExecutionId
	 */
	public void deleteMemoryBlock(String jobExecutionId) {
		INSCClient client = null;
		try {
			client = nscClientFactory.getClient();
			client.deleteMemoryBlock(jobExecutionId);
		}catch(Exception e) {
			Util.logError(log, "NSC Communication error. (deleteMemory)", e);
			throw Util.toRuntimeException(e);
		}finally {
			if (client != null) {
				nscClientFactory.returnClient(client);
			}
		}
	}

	/**
	 * 주기적으로 (수초) NSC 에 접속하여 전체 Job Progress 값을 읽어와 JobExecutionBoard 에 progress 값을 set 한다.
	 */
	private void _run() {
		INSCClient client = null;
		try {
			client = nscClientFactory.getClient();
			
			// NSC 로 부터 전체 Job Progress 수신 받는다.
			List<NSCJobStatus> allJobProgressStatus = client.getAllJobProcessStatus();
			
			for (NSCJobStatus status : allJobProgressStatus) {
				JobContext context = jobExecutionBoard.getJobContext(status.getJobExeId());
				if (context == null) {
					client.deleteMemoryBlock(status.getJobExeId());
					continue; // 종료되어 Board 에서 사라진 경우. NSC 메모리에서 삭제한다.
				}
				
				// 진행률을 설정한다.
				context.setProgressTotal  (status.getProgressTotal());
				context.setProgressCurrent(status.getProgressCurrent());
				
				// 상태를 체크해서 Board의 상태와 NSC 의 상태가 다르면 NSC 의 상태로 update 하고, callback 한다.
				JobExecution jobexe = context.getJobExecution();
				
				if (NSCJobStatus.PROC_STATE_RUNNING.equals(status.getProcStatus())) { // 1:동작
					if (jobexe.getState() == JobExecution.STATE_SUSPENDED) { // 
						// suspended 상태에서 running 상태로 변경됐음 (재개)
						jobRunnerCallBack.callBackJobResume(jobexe);
						jobexe.setState(JobExecution.STATE_RUNNING); // JobExecution 의 상태 변경
						Util.logInfo(log, "callBackJobResume("+jobexe.getJobExecutionId()+")");
					}
				}else if (NSCJobStatus.PROC_STATE_SUSPENDED.equals(status.getProcStatus())) { // 3:중지
					if (jobexe.getState() == JobExecution.STATE_RUNNING) { // 
						// Running 상태에서 Suspended 상태로 변경됐음 (중지)
						jobRunnerCallBack.callBackJobSuspend(jobexe);
						jobexe.setState(JobExecution.STATE_SUSPENDED); // JobExecution 의 상태 변경
						Util.logInfo(log, "callBackJobSuspend("+jobexe.getJobExecutionId()+")");
					}
				}
			}
			
		}catch(Throwable e) {
			Util.logError(log, this+" NSC Communication error. (getAllJobProgress)", e);
			// 에러나도 다음 polling 때 다시 처리하도록 조용히 넘어간다.
		}finally {
			if (client != null) {
				nscClientFactory.returnClient(client);
			}
		}
	}
	
	public void run() {
		// 데몬으로 돌면서 NSC 와 통신하여 현재 progress 값을 조회함.
		while(!Thread.interrupted() && enabled) {
			if (jobExecutionBoard != null) { // 아직 AgentMain 에서 set 하기 전에는 null 이다.
				_run();
			}
			
			try {
				Thread.sleep(pollingInterval);
			}catch(InterruptedException e) {
				if (enabled) { // 정상 shutdown 이 아닌 경우임.
					Util.logError(log, this+" interrupted", e);
				}
				break;
			}
		}
		
	}
	
	
	

}
