package nexcore.scheduler.controller.internal.param;

import java.util.Arrays;

import nexcore.scheduler.exception.SchedulerException;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Space 문자 리턴 </li>
 * <li>작성일 : 2010. 6. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionSpace implements ParameterFunction {

	public String getName() {
		return "SPACE";
	}

	public String evaluate(String[] operands, ParameterContext paramContext) {
		if (operands.length == 0) {
			return " ";
		}else if (operands.length == 1) {
			char[] b = new char[Integer.parseInt(operands[0])];
			Arrays.fill(b, ' ');
			return new String(b);
		}else {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
	}
	
	public static void main(String[] args) {
		FunctionSpace f = new FunctionSpace();
		System.out.println(f.evaluate(new String[] {"10"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"3"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"8"}, new ParameterContext()));
	}
}
