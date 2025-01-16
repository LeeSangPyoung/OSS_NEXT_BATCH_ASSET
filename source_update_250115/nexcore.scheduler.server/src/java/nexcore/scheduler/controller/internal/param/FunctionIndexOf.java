package nexcore.scheduler.controller.internal.param;

import java.util.Arrays;

import nexcore.scheduler.exception.SchedulerException;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 문자열 indexOf 함수. String.indexOf() 메소드와 동일</li>
 * <li>작성일 : 2010. 6. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionIndexOf implements ParameterFunction {

	public String getName() {
		return "INDEX";
	}

	public String evaluate(String[] operands, ParameterContext paramContext) {
		if (operands.length != 2) {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}else {
			return String.valueOf(operands[0].indexOf(operands[1]));
		}
	}
	
	public static void main(String[] args) {
		FunctionIndexOf f = new FunctionIndexOf();
		System.out.println(f.evaluate(new String[] {"abcdef", "def"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"20100331", "03"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"홍길동", "동"}, new ParameterContext()));
		
	}
}
