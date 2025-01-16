/**
 * 
 */
package nexcore.scheduler.agent.internal;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 에이전트에서 사용하는 상수값. (디렉토리, 코드 등) </li>
 * <li>작성일 : 2013. 6. 28.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public interface AgentConstants {

	public static final String RUNNER_FILE_DIRECTORY      = "/batch/runner";   // PROC 타입에서는 이 디렉토리에 runner .sh, .cmd 파일을 만들어 실행한다.
	public static final String PARAMETER_FILE_DIRECTORY   = "/batch/param";    // PROC 타입에서는 파라미터 값들을 이 디렉토리에 파일로 저장한다.
	public static final String JOBEXEPROC_FILE_DIRECTORY  = "/batch/jeproc";   // PROC 타입인 경우 start 시에 이 디렉토리에 je obj 파일 write하고, 종료시에 delete 한다.
	public static final String END_JOBEXE_STORE_DIRECTORY = "/batch/jestore";  // 종료시 여기에 저장하고, JobStateCallbackSender 쓰레드가 스케줄러로 callback 한다.
	public static final String END_JOBEXE_ERROR_DIRECTORY = "/batch/jestore_error";  // jestore 의 파일들을 처리하는 중, 파일 깨짐이 발견된 경우는 여기로 move 한다.
	
	public static final String NBS_PIN_FILE       = "NBS_PIN_FILE";  // 쉘실행 타임에서 입력 파라미터 파일명
	public static final String NBS_POUT_FILE      = "NBS_POUT_FILE"; // 쉘실행 타입에서 출력 파라미터 파일명

	public static final String NBS_JOB_ID         = "NBS_JOB_ID";      // JOB_ID, JOB_INS_ID, JOB_EXE_ID 는 환경변수로 전달된다.
	public static final String NBS_JOB_INS_ID     = "NBS_JOB_INS_ID";  
	public static final String NBS_JOB_EXE_ID     = "NBS_JOB_EXE_ID";  
	public static final String NBS_LOG_LEVEL      = "NBS_LOG_LEVEL";  
	
}
