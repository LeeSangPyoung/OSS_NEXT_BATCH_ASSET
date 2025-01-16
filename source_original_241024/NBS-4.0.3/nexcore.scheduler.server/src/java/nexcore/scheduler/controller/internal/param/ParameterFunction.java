package nexcore.scheduler.controller.internal.param;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 파라미터 해석시 ! 로 시작하는 함수 처리용 interface. 모든 함수 클래스는 이 interface를 상속받는다. </li>
 * <li>작성일 : 2010. 6. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface ParameterFunction {

	/**
	 * 함수이름. 예) SUBSTRING, DATEADD, MONTHADD, YEARADD, EOMDATE, BOMDATE, SHIFTCAL
	 * @return
	 */
	String getName();
	
	
	/**
	 * 함수를 해석하여 최종 String으로 리턴함.
	 * 
	 * @param operands 괄호 안의 String 값들. 
	 * 예) !SUBSTRING(20101010 0 4) 인 경우  ["20101010", "0", "4"] 가 전달됨.
	 * 
	 * @param paramContext 이 값을 해석하는 문맥의 값들. agent 의 system properties, system env map 들이 여기에 들어간다.
	 * @return
	 */
	String evaluate(String[] operands, ParameterContext paramContext);
}
