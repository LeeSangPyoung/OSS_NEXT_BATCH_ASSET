package nexcore.scheduler.core;

import java.util.Calendar;
import java.util.List;

import nexcore.scheduler.entity.JobDefinition;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 스케줄된 일자인지 아닌지 체크함 </li>
 * <li>작성일 : 2010. 5. 13.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IDayScheduler {

	/**
	 * 당일이 실행되어야하는날인지 체크
	 * 
	 * @param job
	 *            JobDefinition
	 * @return true if today is the scheduled day; false otherwise
	 */
	public boolean isScheduledDay(JobDefinition jobdef);

	/**
	 * 당일이 실행되어야하는날인지 체크
	 */
	public boolean isScheduledDay(JobDefinition jobdef, Calendar today);

	
	/**
	 * 월 기준으로 실행되어야할 일자를 리턴함.
	 * @param jobdef
	 * @param yyyymm
	 * @return
	 */
	public List getScheduledDayList(JobDefinition jobdef, String yyyymm);

}