package nexcore.scheduler.controller.internal.param;

import java.util.Arrays;

import nexcore.scheduler.exception.SchedulerException;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 문자열 대문자화 함수 </li>
 * <li>작성일 : 2010. 11. 08.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionToUpper implements ParameterFunction {

	public String getName() {
		return "UPPER";
	}

	public String evaluate(String[] operands, ParameterContext paramContext) {
		if (operands.length == 1) {
			return operands[0].toUpperCase();
		}else {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
	}
	
	public static void main(String[] args) {
		FunctionToUpper f = new FunctionToUpper();
		System.out.println(f.evaluate(new String[] {"aaa"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"bbb"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"ccc"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"홍길동", "2"}, new ParameterContext()));
		
	}
}
