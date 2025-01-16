package nexcore.scheduler.core;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * <ul>
 * <li>BIZ. Group  : </li>
 * <li>Sub  Group  : </li>
 * <li>Date        : 2009. 7. 13.</li>
 * <li>Author      : Jeong Ho Cheol</li>
 * <li>Description : 스케쥴러에서 사용하는 달력. {@link IScheduleDayChecker} 에서 당일이 수행일인지 아닌지를 체크하기 위해서 달력을 사용함.
 *                   달력 기능은 프로젝트마다 요건이 다를 수 있으므로 customize 해야하는 부분임.
 *                   SK 증권에서는 업무 테이블 중에서 기준일 테이블을 조회하여 매일 정해진 시간에 달력 데이타를 로드하도록 함.</li>
 * </ul>
 *
 * @author NEXCORE Tech Part.
 */
/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Calendar. 프로젝트마다 calendar customize 시에 이 인터페이스를 상속받아야함.</li>
 * <li>작성일 : 2010. 5. 11.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */ // SK증권 배치 scheduler 모듈의 reuse.
public interface IScheduleCalendar {
	/**
	 * calendar의 특정 한달의 YYYYMMDD 리스트 구하기
	 * @param calendarId
	 * @param yyyymm
	 * @return
	 */
	public List/* <Integer> */ getMonthlyYyyymmddList(String calendarId, String yyyymm);
	
	/**
	 * calendar의 특정 한달의 YYYYMMDD 리스트 구하기
	 * @param calendarId
	 * @param year
	 * @param month
	 * @return
	 */
	public List/* <Integer> */ getMonthlyYyyymmddList(String calendarId, int year, int month);

	/**
	 * calendar의 특정 한달의 YYYYMMDD 리스트 구하기
	 * @param calendarId
	 * @param cal
	 * @return
	 */
	public List/* <Integer> */ getMonthlyYyyymmddList(String calendarId, Calendar cal);
	
	/**
	 * calendar 기준으로 n 번 익전일 구하기
	 * @param calendarId
	 * @param today
	 * @param n
	 * @return
	 */
	public Calendar getNextDayOfCalendar(String calendarId, Calendar today, int n);
	
	/**
	 * Calendar 전체 리스트. 
	 * @param calendarId
	 * @return
	 */
	public List getYyyymmddList(String calendarId);
	
	/**
	 * date 가 calendar에 포함되어있는지?
	 * @param calendarId
	 * @param date
	 * @return
	 */
	public boolean contains(String calendarId, Calendar date);
	
	/**
	 * calendar 목록을 리스트한다.
	 * @return
	 */
	public Map listCalendarIdNames();

	/**
	 * Calendar 정보를 DB에서 다시 로드함.
	 */
	public void reload();
}
