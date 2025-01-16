package nexcore.scheduler.controller.internal.param;

import java.util.Arrays;
import java.util.Calendar;

import nexcore.scheduler.core.internal.CalendarUtil;
import nexcore.scheduler.exception.SchedulerException;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 연도 계산 함수. <BR>
 * 	            !YEARADD(20100530 1) ==> 20110530, <BR>
 *              !YEARADD(201005 2)   ==> 201205, <BR>
 *              !YEARADD(2010 -2)    ==> 2008 </li>
 * <li>작성일 : 2010. 6. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionYearAdd implements ParameterFunction {

	public String getName() {
		return "YEARADD";
	}

	private int calc(String yyyymmdd, int i) {
		Calendar cal = CalendarUtil.convYYYYMMDDToCalendar(yyyymmdd);
		cal.add(Calendar.YEAR, i);
		return CalendarUtil.convCalendarToYYYYMMDD(cal);
	}
	
	public String evaluate(String[] operands, ParameterContext paramContext) {
		if (operands.length != 2) {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
		
		if (operands[0].length() == 8) { // YYYYMMDD 타입
			return String.valueOf(calc(operands[0], Integer.parseInt(operands[1])));
		}else if (operands[0].length() == 6) { // YYYYMM 타입
			return String.valueOf(calc(operands[0]+"01", Integer.parseInt(operands[1]))/100);
		}else if (operands[0].length() == 4) { // YYYY 타입
			return String.valueOf(calc(operands[0]+"0101", Integer.parseInt(operands[1]))/10000);
		}else {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
	}
	
	public static void main(String[] args) {
		FunctionYearAdd f = new FunctionYearAdd();
		System.out.println(f.evaluate(new String[] {"20100405", "1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"201003",   "1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"2010",     "1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"200910",   "-1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100405", "-2"}, new ParameterContext()));
		
	}
}
