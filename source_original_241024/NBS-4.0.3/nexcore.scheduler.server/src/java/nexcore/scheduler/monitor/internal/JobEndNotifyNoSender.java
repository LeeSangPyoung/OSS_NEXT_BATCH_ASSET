package nexcore.scheduler.monitor.internal;

import java.util.List;

import nexcore.scheduler.entity.JobNotifySendInfo;
import nexcore.scheduler.monitor.IJobEndNotifySender;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어 </li>
 * <li>설  명 : Job End 통지 대상 중 sender 가 설정안됐을때 강제로 실행되는 dummy </li>
 * <li>작성일 : 2011. 4. 7.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobEndNotifyNoSender implements IJobEndNotifySender {

	/**
	 * 아무일 하지 않고 상태만 "X"로 변경한다.
	 */
	public int doSend(List<JobNotifySendInfo> sendList) {
		if (sendList != null) {
			for (JobNotifySendInfo jobNotifySendInfo : sendList) {
				jobNotifySendInfo.setSendState("X");
			}
		}
		return 0;
	}
	
	public boolean isEnable() {
		return true;
	}
}
