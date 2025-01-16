package nexcore.scheduler.agent.runner;

import java.lang.reflect.InvocationTargetException;

import nexcore.scheduler.agent.JobContext;

/**
 * <ul>
 * <li>업무 그룹명 : nexcore-scheduler-4</li>
 * <li>서브 업무명 : nexcore.scheduler.agent.runner</li>
 * <li>설  명 : 에이전트의 JobContext 객체로 부터 배치 프로그램의 BatchContext 로 변환하거나 연결하는 역할을 함</li>
 * <li>작성일 : 2016. 2. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IBatchContextAdapter {

	/**
	 * BatchContext 객체를 만든다
	 * 의존성 제거를 위해 reflection 방식으로 한다.
	 * @param jobContext
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	Object convertJobContextToBatchContext(JobContext jobContext)
		throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException ;

	/**
	 * 프로그램 실행 완료후 BatchContext 의 returnCode, returnValues 들을 JobContext 로 복사한다.
	 * @param batchContext 배치 컨텍스트 객체
	 * @param jobContext
	 */
	void copyBatchContextReturnValuesToJobContext(Object batchContext, JobContext jobContext) ;
	
	
	/**
	 * 강제종료 버튼을 누를 경우 호출되며 여기서 BatchContext 에 flag 전달한다. 
	 * @param jobContext
	 */
	void transferStopForceEvent(JobContext jobContext);

	/**
	 * 일시정지 버튼을 누를 경우 호출되며 여기서 BatchContext 에 flag 전달한다. 
	 * @param jobContext
	 */
	void transferSuspendEvent(JobContext jobContext);

	/**
	 * 계속실행 버튼을 누를 경우 호출되며 여기서 BatchContext 에 flag 전달한다. 
	 * @param jobContext
	 */
	void transferResumeEvent(JobContext jobContext);

	/**
	 * 일시정지/계속실행 버튼을 누를때 이 메소드를 통해 배치 프로그램을 상태를 BatchContext.getStatus() 또는 POJO.getStatus() 로 읽어온다.  
	 * @param jobContext
	 * @return true if suspended or false
	 */
	boolean isSuspendedStatus(JobContext jobContext);

}