package nexcore.scheduler.monitor;

import java.util.List;

import nexcore.scheduler.entity.JobNotifySendInfo;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 9999 </li>
 * <li>설  명 : 실제로 통지 send 하는 클래스 </li>
 * <li>작성일 : 2011. 4. 7.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IJobEndNotifySender {

	/**
	 * 통지 전송 메소드. 
	 * ## 이 메소드 안에서 통지 전송 결과에 따라 'S', 'F'로 상태를 변경함.
	 * @param sendList
	 * @return
	 */
	public int doSend(List<JobNotifySendInfo> sendList);
	
	
	public boolean isEnable();
}
