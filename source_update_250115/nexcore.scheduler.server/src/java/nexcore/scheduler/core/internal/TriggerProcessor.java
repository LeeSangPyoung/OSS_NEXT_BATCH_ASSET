package nexcore.scheduler.core.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;

import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 배치 스케줄러 </li>
 * <li>서브 업무명 : 트리거</li>
 * <li>설  명 : Job 종료후 트리거 처리 로직에 따라 분기함</li>
 * <li>작성일 : 2016. 7. 26.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class TriggerProcessor {
	
	private static Log log;

	/**
	 * Job 종료 상황에 따라 설정된 Target Trigger 들 중에서 해당되는 Trigger 를 찾아서 리턴한다.
	 * @param jobInsId ended Job Instance Id
	 * @param jobId parent JobId
	 * @param targetList parent 에 등록된 Trigger List
	 * @param isEndOk parent 의 종료 상태
	 * @param returnValues parent 의 종료값
	 * @return
	 */
	public static List<PostJobTrigger> selectTrigger(String jobInsId, String jobId, List<PostJobTrigger> targetList, boolean isEndOk, Properties returnValues) {
		if (log == null) {
			log = LogManager.getSchedulerLog();
		}
		
		if (targetList == null || targetList.size() == 0) { 
			return new ArrayList(0);  // caller 에서 NPE 발생 방지를 위해 empty list 리턴한다.
		}
		
		List<PostJobTrigger> selected = new ArrayList<PostJobTrigger>();
		
		for (PostJobTrigger trigger : targetList) {
			if (Util.equals(jobId, trigger.getTriggerJobId())) {
				// 자기 자신으로 trigger 하는 것은 금지.
				continue;
			}
			if (Util.equals(trigger.getWhen(), "ENDOK")) {
				if (isEndOk) {
					selected.add(trigger);
					log.info("[TriggerProcessor] ("+jobInsId+") endok. "+trigger+" selected.");
				}
			}else if (Util.equals(trigger.getWhen(), "ENDFAIL")) {
				if (!isEndOk) {
					selected.add(trigger);
					log.info("[TriggerProcessor] ("+jobInsId+") endfail. "+trigger+" selected.");
				}
			}else if (Util.equals(trigger.getWhen(), "END")) {
				selected.add(trigger);
				log.info("[TriggerProcessor] ("+jobInsId+") ended. "+trigger+" selected.");
			}else if (Util.equals(trigger.getWhen(), "RETVAL")) {
				// parent 의 return value 값에 다라 trigger 후행 Job 이 결정되는 경우
				// checkValue1 이 key 이며, checkValue2가 value 에 해당한다.
				if (returnValues != null && returnValues.size() > 0) { 
					if (Util.equals(returnValues.getProperty(Util.nvl(trigger.getCheckValue1())), trigger.getCheckValue2())) {
						selected.add(trigger);
						log.info("[TriggerProcessor] ("+jobInsId+") RETVAL("+trigger.getCheckValue1()+","+returnValues.getProperty(Util.nvl(trigger.getCheckValue1()))+") "+trigger+" selected.");
					}
				}
			}
		}

		return selected;
	}
}
