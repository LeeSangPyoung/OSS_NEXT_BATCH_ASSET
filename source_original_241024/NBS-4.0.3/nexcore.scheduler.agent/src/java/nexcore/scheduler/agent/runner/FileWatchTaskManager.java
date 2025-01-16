package nexcore.scheduler.agent.runner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 </li>
 * <li>설  명 : 00000  </li>
 * <li>작성일 : 2010. 10. 21.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FileWatchTaskManager {
	private Map<String, FileWatchTimerTask> processMap = new ConcurrentHashMap<String, FileWatchTimerTask>();
	
	public void put(String jobExecutionId, FileWatchTimerTask task) {
		processMap.put(jobExecutionId, task);
	}
	
	public FileWatchTimerTask get(String jobExecutionId) {
		return processMap.get(jobExecutionId);
	}
	
	public FileWatchTimerTask remove(String jobExecutionId) {
		return processMap.remove(jobExecutionId);
	}
	
	
}
