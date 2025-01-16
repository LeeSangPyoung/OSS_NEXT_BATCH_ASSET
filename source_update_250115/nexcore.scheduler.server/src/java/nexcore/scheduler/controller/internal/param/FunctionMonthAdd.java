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
 * <li>설  명 : 월 계산 함수. <BR>
 * 	            !MONTHADD(20100530 1) ==> 20110630, <BR>
 *              !MONTHADD(201005 2)   ==> 201207, <BR>
 * <li>작성일 : 2010. 6. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionMonthAdd implements ParameterFunction {

	public String getName() {
		return "MONTHADD";
	}

	private int calc(String yyyymmdd, int i) {
		Calendar cal = CalendarUtil.convYYYYMMDDToCalendar(yyyymmdd);
		cal.add(Calendar.MONTH, i);
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
		}else {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
	}
	
	public static void main(String[] args) {
		FunctionMonthAdd f = new FunctionMonthAdd();
		System.out.println(f.evaluate(new String[] {"20100331", "-1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100330", "-1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100329", "-1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100328", "-1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100327", "-1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100331", "6"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"201012",   "1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"200910",   "-1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100405", "-2"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100105", "-2"}, new ParameterContext()));
		
	}
}
