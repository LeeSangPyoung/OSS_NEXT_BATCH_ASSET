package nexcore.scheduler.agent.internal;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치코어</li>
 * <li>설  명 : Job 수행중인 스레드 관리</li>
 * <li>작성일 : 2010.4.02</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobRunThreadManager {
	private Map<String, Thread> threads = new ConcurrentHashMap();
	
	private Log log;
	
	public JobRunThreadManager() {
		log = LogManager.getAgentLog();
	}
	
	public Thread newThreadAndStart(JobContext context, Runnable runnable) {
		String jobExecutionId = context.getJobExecution().getJobExecutionId();
		
		Thread t = new Thread(runnable, "JobRunThread-"+jobExecutionId);

		// 스레드 우선순위 설정.
		String threadPriority = context.getInParameter("THREAD_PRIORITY");
		if (!Util.isBlank(threadPriority)) {
			try {
				t.setPriority(Util.toInt(threadPriority, Thread.NORM_PRIORITY));
			}catch(Exception ignore) {
			}
		}
		
		threads.put(jobExecutionId, t);
		t.start();
		
		// 스레드 하나 만들때마다 다른 스레드 한번씩 검사해봐서 isAlive 아닌놈은 remove 한다.
		try {
			Iterator iter = threads.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry<String, Thread> entry = (Map.Entry)iter.next();
				/*
				 * 2013.09.26. Java1.5 의 ConcurrentHashMap 의 버그로 인해 iterate 중에 다른 쓰레드에서 remove가 일어나면 entry.getValue() 가 null이 리턴될 수 있다. 
				 * svn : 21562. 한화증권에서 발생
				 */
				Thread tt = entry.getValue();   
				if (entry != null && tt != null && !tt.isAlive()) {
					// 스레드가 이미 죽은 놈이면 여기 threadsMap 에서 꺼낸다.
					iter.remove();
				}
			}
		}catch(Exception e) {
			// 이과정에서는 에러가 나도 무방하므로 로그 찍고 pass 한다.
			Util.logWarn(log, e.toString(), e);
		}
		
		return t;
	}
	
	public Thread getThread(String jobExecutionId) {
		return (Thread)threads.get(jobExecutionId);
	}
	
	public int size() {
		return threads.size();
	}
}
