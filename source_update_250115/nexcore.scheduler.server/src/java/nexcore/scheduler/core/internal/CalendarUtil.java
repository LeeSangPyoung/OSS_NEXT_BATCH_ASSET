/**
 * 
 */
package nexcore.scheduler.core.internal;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * <ul>
 * <li>BIZ. Group  : </li>
 * <li>Sub  Group  : </li>
 * <li>Date        : 2009. 7. 16.</li>
 * <li>Author      : Jeong Ho Cheol</li>
 * <li>Description : </li>
 * </ul>
 *
 * @author NEXCORE Tech Part.
 */

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Calendar Util. </li>
 * <li>작성일 : 2010. 5. 11.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */ // SK증권 배치의 동일 모듈 reuse.
public class CalendarUtil {

	// 속도를 위해 SimpleDateFormat을 사용하지 않는다.
	public static int getNextMonthYYYYMM(Calendar cal) {
		if (cal.get(Calendar.MONTH) == Calendar.DECEMBER) {
			return (cal.get(Calendar.YEAR)+1) * 100 + 01; // 다음해 1월
		}else {
			return cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH)+1)+1; // 당해 
		}
	}

	public static String getNextMonthYYYYMM(String yyyymm) {
		if ("12".equals(yyyymm.substring(4,6))) {
			return String.valueOf(Integer.parseInt(yyyymm.substring(0,4))+1)+"01"; // 다음해 1월
		}else {
			int mm = Integer.parseInt(yyyymm.substring(4))+1;
			return yyyymm.substring(0,4)+ (mm < 10 ? "0"+mm : ""+mm); // 당해
		}
	}

	// 다음날 구하기.
	public static Calendar getNextDay(Calendar cal) {
		Calendar theDayNext = (Calendar)cal.clone();
		theDayNext.add(Calendar.DAY_OF_MONTH, 1);
		return theDayNext;
	}
	
	// 이전날 구하기.
	public static Calendar getPrevDay(Calendar cal) {
		Calendar theDayPrev = (Calendar)cal.clone();
		theDayPrev.add(Calendar.DAY_OF_MONTH, -1);
		return theDayPrev;
	}

	// 속도를 위해 SimpleDateFormat을 사용하지 않는다.
	public static int convCalendarToYYYYMM(Calendar cal) {
		return 
			cal.get(Calendar.YEAR) * 100 + 
			(cal.get(Calendar.MONTH)+1);
	}

	// 속도를 위해 SimpleDateFormat을 사용하지 않는다.
	public static int convCalendarToYYYYMMDD(Calendar cal) {
		return 
			cal.get(Calendar.YEAR) * 10000 + 
			(cal.get(Calendar.MONTH)+1) * 100 +
			cal.get(Calendar.DAY_OF_MONTH);
	}

	public static Calendar convYYYYMMDDToCalendar(int yyyymmdd) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR,         yyyymmdd/10000);
		cal.set(Calendar.MONTH,        (yyyymmdd-(yyyymmdd/10000*10000))/100 -1);
		cal.set(Calendar.DAY_OF_MONTH, (yyyymmdd-(yyyymmdd/100  * 100)));
		return cal;
	}

	public static Calendar convYYYYMMDDToCalendar(String yyyymmdd) {
		return convYYYYMMDDToCalendar(Integer.parseInt(yyyymmdd));
	}

	// from ~ to 까지의 매일의 날짜를 구함.
	public static List/* <Integer> */ getYyyymmddListByRange(Calendar from, Calendar to) {
		Calendar cal = (Calendar)from.clone();
		
		LinkedList ll = new LinkedList();
		while(true) {
			if (cal.after(to)) break;
			ll.add(new Integer(convCalendarToYYYYMMDD(cal)));
			cal.add(Calendar.DATE, 1);
		}
		return ll;
	}
	
	/**
	 * 시분초는 비교하지 않고 날짜만 비교해서 같은 날인지 체크
	 * @param cal1
	 * @param cal2
	 */
	public static boolean equalsByDay(Calendar cal1, Calendar cal2) {
		return 
			cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
			cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
			cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
	}
}
