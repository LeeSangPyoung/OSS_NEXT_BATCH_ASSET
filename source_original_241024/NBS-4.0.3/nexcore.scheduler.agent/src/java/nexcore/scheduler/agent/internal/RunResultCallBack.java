package nexcore.scheduler.agent.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.controller.client.IControllerClient;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 실행 결과를 처리하는 클래스. Controller로 callback 함.</li>
 * <li>작성일 : 2010. 4. 21.</li>
 * <li>작성일 : 2013. 5. 09.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// 2013-05-09. 에이전트 Only 환경에서는 scheduler 로 callback 하지 않고 그냥 끝냄.
// 2013-07-17. callback 시에 callbackQueue (디렉토리) 를 이용하여 비동기로 처리한다. callback 에서 지연되면서 이중화 환경에서 오동작 하는 경우 발생. #21353

public class RunResultCallBack implements IJobRunnerCallBack {
	private IControllerClient           controllerClient;
	private int                         clientConcurrentMax = 5;  // 컨트롤러 동시 접속제한을 둔다. 기본값 5
	
	private Semaphore                   semaphore; // 컨트롤러 동시 접속제한을 위해 세마포어를 사용함.
	private Object                      lockForCallbackExecutor;  // 실제 callback 을 호출할 스레드를 notify 할 때 사용한 객체.
	private Log                         log;

	public void init() {
		log = LogManager.getAgentLog();
		semaphore = new Semaphore(clientConcurrentMax);
	}
	
	public void destroy() {
	}

	public IControllerClient getControllerClient() {
		return controllerClient;
	}

	public void setControllerClient(IControllerClient controllerClient) {
		this.controllerClient = controllerClient;
	}
	
	public int getClientConcurrentMax() {
		return clientConcurrentMax;
	}

	public void setClientConcurrentMax(int clientConcurrentMax) {
		this.clientConcurrentMax = clientConcurrentMax;
	}

	public boolean callBackJobEnd(JobExecution je) {
		// callback 시에 onlineCtx 에 NotSerializable 객체들이 포함되어있을 경우 에러 발생한다. 아예 OnlineContext는 callback 후에 사용되지 않으므로 여기서 버린다.
		je.setOptionalData(null);
		
		Util.logInfo(log, MSG.get("agent.jobend", je.getJobExecutionId(), je.getReturnCode()==0?" OK ":"FAIL", je.getDescription(), (je.getEndTime()-je.getStartTime())));

		// scheduler / controller 로 callback함.
		boolean callbackOk;
		try {
		    /*
		     * interrupted 상태를 clear 하기 위해 부름. 
		     * Running -> 일시정지 -> 강제종료 의 순서로 동작하는 경우
		     * stop 에서 해당 Job 스레드를 깨우기 위해 interrupt()를 호출하는데, sleep() 이나, wait() 상태가 아니었다면
		     * interrupt flag 가 여기까지 살아있다가 아래 semaphore.acquire() 에서 InterruptedException 을 발생 시킨다.
		     * 이렇게 되면 callBackJobEnd() 가 제대로 수행되지 않는다. 
		     * 이를 방지 하기 위해 아래 Thread.interupted() 를 한번 부른다. 이 메소드는 interrupt flag를 clear 시킨다.
		     */
		    Thread.interrupted();
		    
		    /*
		     * 2013.07.17. #21353
		     * 이중화 환경에서 callback 시에 스케줄러에서는 End 상태 업데이트 후에 peer 와 통신을 하는데, 이때 peer 에 장애사 발생하여
		     * 통신이 지연되는 경우에는 callback 호출 자체가 늦어지게 되서, 에이전트의 JobExecutionBoard 에서 remove하는 것이 늦어지게 된다.
		     * 이 와중에 만약에 스케줄러에서 다음번 반복 또는 강제 실행 등으로 해당 Job 실행을 하게 되면 "이미 같은 Job Instance가 실행 중입니다" 라는 에러가 나게 된다.
		     * 이를 개선하기 위해 
		     * callBackJobEnd() 메소드 호출시 jestore 디렉토리에 write 후 바로 리턴하고, 비동기로 별도의 쓰레드 (JobEndCallbackRecovery) 가 스케줄러로 callback 을 수행한다.   
		     */
		    writeToJEStore(je);
		    return true;
		    
//				semaphore.acquire();
//				callbackOk = controllerClient.callBackJobEnd(je);
		}catch(Throwable e) {
		    callbackOk = false;
		    Util.logError(log, "CallbackJobEnd Fail. ["+je.getJobExecutionId()+"]", e);
		}
		
		return callbackOk;
	}
	
	public void callBackJobSuspend(JobExecution je) {
		Util.logInfo(log, MSG.get("agent.jobsuspended", je.getJobExecutionId(), je.getDescription()));
		
		try {
			semaphore.acquire();
			controllerClient.callBackJobSuspend(je);
		}catch(InterruptedException e) {
			Util.logError(log, "CallbackJobSuspend Fail. ["+je.getJobExecutionId()+"]", e);
		}finally {
			semaphore.release();
		}
	}
	
	public void callBackJobResume(JobExecution je) {
		Util.logInfo(log, MSG.get("agent.jobresumed", je.getJobExecutionId(), je.getDescription()));
		
		try {
			semaphore.acquire();
			controllerClient.callBackJobResume(je);
		}catch(Throwable e) {
			Util.logError(log, "CallbackJobResume Fail. ["+je.getJobExecutionId()+"]", e);
		}finally {
			semaphore.release();
		}
	}	

	/**
	 * callback 오류로 인해 스케줄로은 아직 running 상태이지만, JobExecutionBoard 에서는 사라진 상태인 경우
	 * 스케줄러에서는 Ghost 처리를 해버리게됨.
	 * 
	 * 이때 바로 ghost 가 되지 않게 하기 위해, JobExecutionBoard 에서 못찾으면 jestore 디렉토리에서 파일을 한번더 찾아본다.
	 *
	 * @param jobExeId
	 * @return
	 */
	public JobExecution getJobExecutionFromFile(String jobExeId) {
		File jeFile = new File(Util.getHomeDirectory() + AgentConstants.END_JOBEXE_STORE_DIRECTORY +"/"+jobExeId+".je");
		ObjectInputStream in = null;
		JobExecution      je = null;
		try {
			in = new ObjectInputStream(new FileInputStream(jeFile));
			je = (JobExecution)in.readObject();
		}catch(Throwable e) {
			if (!jeFile.exists()) { /* 파일이 없으면 null 리턴 */
				return null;
			}else {
				Util.logError(log, "getJobExecutionFromRecoveryFile("+jobExeId+") error", e);
			}
		}finally {
			try { in.close(); }catch(Exception ignore) {}
		}
		return je;
	}

	/**
	 * 먼저 jestore 디렉토리에 파일로 write 한다.
	 * 메모리 큐에 put (offer) 하되, full 상태이면 그냥 무시하고 skip 한다.
	 * 한번 full 이 나고 나면
	 * @param je
	 */
	private void writeToJEStore(JobExecution je) {
		// write 중에 읽어 가는 것을 방지 하기 위해 write 중에는 .tmp 를 붙인다.
		File f  = new File(Util.getHomeDirectory() + AgentConstants.END_JOBEXE_STORE_DIRECTORY, je.getJobExecutionId()+".je.tmp");
		File f2 = new File(Util.getHomeDirectory() + AgentConstants.END_JOBEXE_STORE_DIRECTORY, je.getJobExecutionId()+".je");
		if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
		try {
			if (log.isDebugEnabled()) {
				Util.logDebug(log, "[RunResultCallback] Write to jestore. ["+je.getJobExecutionId()+"]");
			}
			Util.writeObjectToFile(f, je);
			
			boolean ok = false;
			for (int i=0; i<100; i++) {
				ok = f.renameTo(f2); // write 완료되면 .tmp를 땐다.
				if (!ok) {
					Util.logWarn(log, "[RunResultCallback] rename fail. "+je.getJobExecutionId());
					if (i==99) {
						throw new RuntimeException("[RunResultCallback] rename fail. "+f.getName()+"->"+f2.getName());
					}
					Util.sleep(100);
				}else {
					break;
				}
			}
		}catch(Throwable e) {
			Util.logError(log, MSG.get("agent.jobend.callback.filewrite.error", f.getName(), je.toString()), e);
		}
		
		// JobEndCallbackRecovery 쓰레드를 깨운다.
		if (lockForCallbackExecutor != null) { // null 인 경우는 boot 중에 아직 쓰레드 초기화 가 안된상황. 그냥 넘긴다.
			synchronized(lockForCallbackExecutor) {
				lockForCallbackExecutor.notify();
			}
		}
	}
}
