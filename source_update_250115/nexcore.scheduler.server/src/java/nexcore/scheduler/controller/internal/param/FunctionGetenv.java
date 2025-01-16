package nexcore.scheduler.controller.internal.param;

import java.util.Arrays;

import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 환경변수 조회 함수. agent 의 System.getenv() 의 결과 값 중에서 조회 </li>
 * <li>작성일 : 2010. 6. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FunctionGetenv implements ParameterFunction {

	public String getName() {
		return "GETENV";
	}

	public String evaluate(String[] operands, ParameterContext paramContext) {
		if (operands.length != 1) {
			throw new SchedulerException("main.param.wrong.operands", Arrays.asList(operands).toString());
		}else {
			return Util.nvl((String)paramContext.getAgentSystemEnv().get(operands[0]), "");
		}
	}
}
