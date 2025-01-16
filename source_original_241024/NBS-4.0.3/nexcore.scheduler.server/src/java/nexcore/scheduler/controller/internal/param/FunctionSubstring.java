package nexcore.scheduler.controller.internal.param;

import java.util.Arrays;

import nexcore.scheduler.exception.SchedulerException;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 문자열 SUBSTRING 함수. 인덱스 규칙은 자바의 substring 과 동일 </li>
 * <li>작성일 : 2010. 6. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionSubstring implements ParameterFunction {

	public String getName() {
		return "SUBSTRING";
	}

	public String evaluate(String[] operands, ParameterContext paramContext) {
		if (operands.length == 2) {
			return operands[0].substring(Integer.parseInt(operands[1]));
		}else if (operands.length == 3) {
			return operands[0].substring(Integer.parseInt(operands[1]), Integer.parseInt(operands[2]));
		}else {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
	}
	
	public static void main(String[] args) {
		FunctionSubstring f = new FunctionSubstring();
		System.out.println(f.evaluate(new String[] {"20100331", "2"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100331", "2", "5"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100331", "2", "2"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100331", "2"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100331", "3"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100331", "4"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"홍길동", "2"}, new ParameterContext()));
		
	}
}
