package nexcore.scheduler.core.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;

import nexcore.scheduler.core.IMonitorDisplayable;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.PreJobCondition;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 선행 조건이 만족되지 않아 waiting 하고 있는 Job 들의 pool </li>
 * <li>작성일 : 2010. 5. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// 선후행 관계 관리는 Job Id 단위로 함. Job Instance Id 가 아님.
public class PreJobWaitingPool implements IMonitorDisplayable {
	/*
	 * Map<PreJobWaitingPoolKey, Set<String:postjobid>>. jobInstanceId 가 아님을 유의해야함
	 */
	private Map<PreJobWaitingPoolKey, Set<String>> waitingJobIdPool = new ConcurrentHashMap<PreJobWaitingPoolKey, Set<String>>(); 

	private Log log;
	
	public class PreJobWaitingPoolKey {
		String preJobId;
		String procDate;
		PreJobWaitingPoolKey(String preJobId, String procDate) {
			this.preJobId = preJobId;
			this.procDate = procDate;
		}
		
		public int hashCode() {
			return preJobId.hashCode()+procDate.hashCode();
		}
		public boolean equals(Object obj) {
			PreJobWaitingPoolKey key = (PreJobWaitingPoolKey)obj;
			return Util.equals(key.preJobId, this.preJobId) && Util.equals(key.procDate, this.procDate);
		}
		
		public String toString() {
			return "("+preJobId+":"+procDate+")";
		}
	}
	
	public PreJobWaitingPool() {
	}
	
	public void init() {
		log = LogManager.getSchedulerLog();
	}
	
	public void destroy() {
	}
	
	/**
	 * 선행 Job 의 종료를 기다려야하는 경우, 여기에 등록한다.
	 * @param jobins
	 */
	public void add(JobInstance jobins) {
		try {
			List<PreJobCondition> prejobConditions = jobins.getPreJobConditions();
			if (prejobConditions == null) {
				return;
			}
			for (PreJobCondition preJobCondition : prejobConditions) {
				// 선행 Job 이 아직 end 되지 않은 경우에.. wait pool 에 넣는다. 
				PreJobWaitingPoolKey key = new PreJobWaitingPoolKey(preJobCondition.getPreJobId(), jobins.getProcDate());
				Set<String> postJobIdSet = null;
				
				postJobIdSet = waitingJobIdPool.get(key);
				if (postJobIdSet == null) {
					synchronized(waitingJobIdPool) {
						postJobIdSet = waitingJobIdPool.get(key); 
						if (postJobIdSet == null) {
							waitingJobIdPool.put(key, postJobIdSet = new HashSet<String>());
						}
					}
				}
				
				if (!postJobIdSet.contains(jobins.getJobId())) {
					synchronized(postJobIdSet) {
						postJobIdSet.add(jobins.getJobId());
					}
				}
			}
		}catch(Exception e) {
			// 위의 복잡한 메모리 처리 중에 혹시 Exception 이 발생하더라도 중단되지 않고 로그 찍고 다음으로 넘어가도록 한다.
			Util.logError(log, "[PreJobWaitingPool] add("+jobins.getJobInstanceId()+") fail", e);  
		}
	}
	
	/**
	 * 이 Job 을 선행 Job 으로 하여 wait 하고 있는 Job ID 들을 리턴함.
	 * @param endedJobId
	 * @return
	 */
	public String[] getWaitingJobIdFor(String endedJobId, String procDate) {
		PreJobWaitingPoolKey key = new PreJobWaitingPoolKey(endedJobId, procDate);
		Set<String> postJobIdSet = waitingJobIdPool.get(key);
		if (postJobIdSet != null) {
			synchronized(postJobIdSet) { /* toArray 안에서 iterate 중에 다른 쓰레드에서 remove 가 일어날 수 있으므로 sync 한다. */
				return (String[])postJobIdSet.toArray(Util.EMPTY_STRING_ARRAY);
			}
		}else {
			return Util.EMPTY_STRING_ARRAY;
		}
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
		Util.logInfo(log, "[PreJobWaitingPool] Start cleansing. ID set : "+waitingIdSet.size());

		int removePreCount=0;
		int removePostCount=0;
		
		try {
			Iterator iter = waitingJobIdPool.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry<PreJobWaitingPoolKey, Set<String>> entry = (Map.Entry)iter.next();
				PreJobWaitingPoolKey key = entry.getKey();
				Set<String> postJobIdSet = entry.getValue();
				
				// 후행 Job Set을 위의 waiting id set 에서 찾아 검사 후 없으면 remove 한다.
				synchronized(postJobIdSet) {
					Iterator<String> iter2 = postJobIdSet.iterator();
					while(iter2.hasNext()) {
						String postJobId = iter2.next();
						if (!waitingIdSet.contains(postJobId+"_"+key.procDate)) {
							iter2.remove();
							removePostCount++;
						}
					}
					if (postJobIdSet.size() == 0) {
						synchronized(waitingJobIdPool) {
							iter.remove();
							removePreCount++;
						}
					}
				}
			}
		}catch(Exception e) {
			// 위의 복잡한 메모리 처리 중에 혹시 Exception 이 발생하더라도 중단되지 않고 로그 찍고 다음으로 넘어가도록 한다.
			Util.logError(log, "[PreJobWaitingPool] doCleansing() fail", e);  
		}
		Util.logInfo(log, String.format("[PreJobWaitingPool] End cleansing. removePreCount=%d, removePostCount=%d", removePreCount, removePostCount));
	}
	
	public String toSizeString() {
		int preJobCount  = 0;
		int postJobCount = 0;
		
		for (Set entry : waitingJobIdPool.values()) {
			preJobCount  ++;
			postJobCount += entry == null ? 0 : entry.size(); // entry == null 일 가능성은 거의 없지만 ConcurrentHashMap 의 버그로 한번더 체크한다.
		}
		return "PreJob : "+preJobCount+", PostJob : "+postJobCount;
	}
	
	public String getDisplayName() {
		return "PreJobWaitingPool";
	}
	public String getDisplayString() {
		return toSizeString();
	}
}
