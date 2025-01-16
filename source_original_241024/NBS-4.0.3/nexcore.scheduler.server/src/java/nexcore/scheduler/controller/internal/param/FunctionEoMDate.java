package nexcore.scheduler.controller.internal.param;

import java.util.Arrays;
import java.util.Calendar;

import nexcore.scheduler.core.internal.CalendarUtil;
import nexcore.scheduler.exception.SchedulerException;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 월말일자 구하기. 다음달1일에서 1일을 빼면 이달말일<BR>
 * 	            !BOMDATE(20100531)  ==> 20110531, <BR>
 * 	            !BOMDATE(201006)    ==> 20110630, <BR>
 * 	            !BOMDATE(20100709)  ==> 20110731, <BR>
 * <li>작성일 : 2010. 6. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionEoMDate implements ParameterFunction {

	public String getName() {
		return "EOMDATE";
	}

	private int calc(String yyyymmdd) {
		Calendar cal = CalendarUtil.convYYYYMMDDToCalendar(yyyymmdd);
		cal.add(Calendar.MONTH, 1);  // 다음달
		cal.set(Calendar.DATE,  1);  // 1일에서
		cal.add(Calendar.DATE,  -1); // 1일 빼기
		
		return CalendarUtil.convCalendarToYYYYMMDD(cal);
	}
	
	public String evaluate(String[] operands, ParameterContext paramContext) {
		if (operands.length != 1) {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
		
		if (operands[0].length() == 8) { // YYYYMMDD 타입
			return String.valueOf(calc(operands[0]));
		}else if (operands[0].length() == 6) { // YYYYMM 타입
			return String.valueOf(calc(operands[0]+"01"));
		}else {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
	}
	
	public static void main(String[] args) {
		FunctionEoMDate f = new FunctionEoMDate();
		System.out.println(f.evaluate(new String[] {"20100405"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100401"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100223"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"201012"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"201202"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"201102"}, new ParameterContext()));
	}
}
