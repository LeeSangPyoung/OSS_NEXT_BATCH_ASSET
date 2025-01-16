package nexcore.scheduler.controller.internal.param;

import java.math.BigDecimal;
import java.util.Arrays;

import nexcore.scheduler.exception.SchedulerException;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 숫자 곱하기. <BR>
 * 	            !NUMMUL(1 2)    ==> 2, <BR>
 * 	            !NUMMUL(10 -5)  ==> -50, <BR>
 * <li>작성일 : 2010. 6. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionNumMultiply implements ParameterFunction {

	public String getName() {
		return "NUMMUL";
	}

	public String evaluate(String[] operands, ParameterContext paramContext) {
		if (operands.length != 2) {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}
		
		// 소수점 처리를 자연스럽게 하기 위해 BigDecimal을 사용한다.
		return String.valueOf(new BigDecimal(operands[0]).multiply(new BigDecimal(operands[1])));
	}
	
	public static void main(String[] args) {
		FunctionNumMultiply f = new FunctionNumMultiply();
		System.out.println(f.evaluate(new String[] {"405.1" , "1"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"1" , "11"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"223" , "3"}, new ParameterContext()));
		System.out.println(f.evaluate(new String[] {"12" ,   "-10"}, new ParameterContext()));
	}
}
