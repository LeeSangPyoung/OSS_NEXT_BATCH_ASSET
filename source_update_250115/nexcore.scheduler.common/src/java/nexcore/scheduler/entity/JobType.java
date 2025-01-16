package nexcore.scheduler.entity;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Type 상수 클래스 </li>
 * <li>작성일 : 2010. 10. 29.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobType {
	// Job 타입 상수
//	public static final String JOB_TYPE_EJB        = "EJB";
	public static final String JOB_TYPE_JBATCH     = "JBATCH";     // NBF7 기반의 배치 Job. 과거의 EJB 타입의 이름을 변경함 (NBF7 에서 EJB 를 걷어냈음)
	public static final String JOB_TYPE_POJO       = "POJO";
	public static final String JOB_TYPE_PROC       = "PROC";
	public static final String JOB_TYPE_DUMMY      = "DUMMY";
	public static final String JOB_TYPE_SLEEP      = "SLEEP";
	public static final String JOB_TYPE_FILEWATCH  = "FILEWATCH";
	public static final String JOB_TYPE_CENTERCUT  = "CENTERCUT";
//	public static final String JOB_TYPE_EJBSO      = "EJBSO";      // Start Only. 강제종료불가. (수출입은행 요구사항)      
	public static final String JOB_TYPE_CBATCH     = "CBATCH";     // NEXCORE C Framework 기반 배치.      
//	public static final String JOB_TYPE_JAVARUN    = "JAVARUN";    // 배치 스케줄러 2.0 버전의 JVM 실행 방식으로 실행하는 Job type.  
	public static final String JOB_TYPE_DBPROC     = "DBPROC";     // 3.7 버전 부터, DB PROCEDURE 호출 타입의 Job 추가됨
	public static final String JOB_TYPE_QUARTZJOB  = "QUARTZ";     // 3.8 버전 부터, QuartzJob 을 수동으로 trigger 함
	public static final String JOB_TYPE_RESTAPI    = "RESTAPI";     // 4.1 버전 부터, RestAPI 방식
      
}
