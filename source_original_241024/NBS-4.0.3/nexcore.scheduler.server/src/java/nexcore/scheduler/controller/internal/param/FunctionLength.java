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
public class FunctionLength implements ParameterFunction {

	public String getName() {
		return "LENGTH";
	}

	public String evaluate(String[] operands, ParameterContext paramContext) {
		if (operands.length != 1) {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}else {
			return String.valueOf(operands[0].length());
		}
	}
	
	public static void main(String[] args) {
		FunctionLength f = new FunctionLength();
		System.out.println(f.evaluate(new String[] {"abcdef"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100331"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"홍길동"}, new ParameterContext()));
		
	}
}
