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
 * <li>설  명 : 일 계산 함수. <BR>
 * 	            !DATEADD(20100531 1)  ==> 20110601, <BR>
 * 	            !DATEADD(20100601 -1) ==> 20110531, <BR>
 * <li>작성일 : 2010. 6. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionDateAdd implements ParameterFunction {

	public String getName() {
		return "DATEADD";
	}

	private int calc(String yyyymmdd, int i) {
		Calendar cal = CalendarUtil.convYYYYMMDDToCalendar(yyyymmdd);
		cal.add(Calendar.DATE, i);
		return CalendarUtil.convCalendarToYYYYMMDD(cal);
	}
	
	public String evaluate(String[] operands, ParameterContext paramContext) {
		if (operands.length != 2) {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
		
		if (operands[0].length() == 8) { // YYYYMMDD 타입
			return String.valueOf(calc(operands[0], Integer.parseInt(operands[1])));
		}else {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
	}
	
	public static void main(String[] args) {
		FunctionDateAdd f = new FunctionDateAdd();
		System.out.println(f.evaluate(new String[] {"20100405", "1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100401", "-2"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100301", "-2"}, new ParameterContext()));
	}
}
