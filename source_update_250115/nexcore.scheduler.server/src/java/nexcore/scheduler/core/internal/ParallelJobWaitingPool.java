package nexcore.scheduler.core.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import nexcore.scheduler.core.IMonitorDisplayable;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 병렬제한그룹 조건이 만족되지 않아 waiting 하고 있는 Job 들의 pool </li>
 * <li>작성일 : 2010. 5. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ParallelJobWaitingPool implements IMonitorDisplayable {
	private Map<String, Set<String>> waitingJobInstanceIdPool = new ConcurrentHashMap<String, Set<String>>(); // <String:groupName, Set<String:jobInstanceId>>. prejob 과는 달리 여기서는 InstanceId 임.

	private Log log;
	
	public void init() {
		log = LogManager.getSchedulerLog();
	}
	
	public void destroy() {
	}
	
	/**
	 * Parallel Max 를 초과하여 wait 해야하는 Job Instance 를 pool에 넣는다.
	 * @param jobins
	 */
	public void add(JobInstance jobins) {
		if (Util.isBlank(jobins.getParallelGroup())) return;
		
		try {
			Set<String> waitingJobInstanceIdSet = waitingJobInstanceIdPool.get(jobins.getParallelGroup());
			if (waitingJobInstanceIdSet == null) {
				synchronized (waitingJobInstanceIdPool) {
					if (waitingJobInstanceIdSet == null) {
						waitingJobInstanceIdPool.put(jobins.getParallelGroup(), waitingJobInstanceIdSet = new HashSet());
					}
				}
			}
			
			if (!waitingJobInstanceIdSet.contains(jobins.getJobInstanceId())) {
				synchronized(waitingJobInstanceIdSet) {
					waitingJobInstanceIdSet.add(jobins.getJobInstanceId());
				}
			}
		}catch(Exception e) {
			// 위의 복잡한 메모리 처리 중에 혹시 Exception 이 발생하더라도 중단되지 않고 로그 찍고 다음으로 넘어가도록 한다.
			Util.logError(log, "[ParallelJobWaitingPool] add("+jobins.getJobInstanceId()+") fail", e);  
		}
	}
	
	/**
	 * Parallel Max 의 제한으로 인해 대기중인 Job Instance Id 리스트 리턴
	 * @param endedJobId
	 * @return
	 */
	public String[] getWaitingJobIdFor(String groupName) {
		if (Util.isBlank(groupName)) {
			return Util.EMPTY_STRING_ARRAY;
		}
		Set<String> waitingJobInstanceIdSet = waitingJobInstanceIdPool.get(groupName);
		return waitingJobInstanceIdSet != null ? (String[])waitingJobInstanceIdSet.toArray(Util.EMPTY_STRING_ARRAY) : Util.EMPTY_STRING_ARRAY;
	}

	/**
	 * 메모리 상태를 cleansing 한다. 강제종료, 강제실행, 강제정상종료 등의 경우는 위 remove 메소드가 불리지 않으므로
	 * garbage 상태로 계속 남을 수 있다. 주기적으로 지워줘야한다.
	 * 
	 * 이 메소드는 TimeScheduler 에 의해 불리고 10분마다 작업한다.
	 * peer 의 PreJobWaitPool 도 같이 cleansing 작업 할 수 있게 호출한다. 
	 * @param waitingIdSet
	 */
	public void doCleansing(Set<String> waitingIdSet) {
		if (waitingIdSet == null) {
			return;
		}
		Util.logInfo(log, "[ParallelJobWaitingPool] Start cleansing. ID set : "+waitingIdSet.size());

		int removePGroupCount=0;
		int removeJobInsCount=0;
		
		try {
			Iterator iter = waitingJobInstanceIdPool.values().iterator();
			while(iter.hasNext()) {
				Set<String> waitingJobIdSet = (Set<String>)iter.next();
				
				// 후행 Job Set을 위의 waiting id set 에서 찾아 검사 후 없으면 remove 한다.
				synchronized(waitingJobIdSet) {
					Iterator<String> iter2 = waitingJobIdSet.iterator();
					while(iter2.hasNext()) {
						String jobInsId = iter2.next();
						if (!waitingIdSet.contains(jobInsId)) {
							iter2.remove();
							removeJobInsCount++;
						}
					}
					if (waitingJobIdSet.size() == 0) {
						synchronized(waitingJobInstanceIdPool) {
							iter.remove();
							removeJobInsCount++;
						}
					}
				}
			}
		}catch(Exception e) {
			// 위의 복잡한 메모리 처리 중에 혹시 Exception 이 발생하더라도 중단되지 않고 로그 찍고 다음으로 넘어가도록 한다.
			Util.logError(log, "[ParallelJobWaitingPool] doCleansing() fail", e);  
		}
		Util.logInfo(log, String.format("[ParallelJobWaitingPool] End cleansing. removeGroupCount=%d, removeJobInsCount=%d", removePGroupCount, removeJobInsCount));
	}
	
	public String toSizeString() {
		int groupCount         = 0;
		int waitingJobInsCount = 0;
		
		for (Set entry : waitingJobInstanceIdPool.values()) {
			groupCount  ++;
			waitingJobInsCount += entry == null ? 0 : entry.size(); // entry == null 일 가능성은 거의 없지만 ConcurrentHashMap 의 버그로 한번더 체크한다.
		}
		return "Group : "+groupCount+", Waiting Job : "+waitingJobInsCount;
	}
	
	public String getDisplayName() {
		return "ParallelJobWaitingPool";
	}
	
	public String getDisplayString() {
		return toSizeString();
	}
}
