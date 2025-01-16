package nexcore.scheduler.controller.internal.jobtype;

import java.util.List;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  EJB 타입의 Job Type Manager</li>
 * <li>작성일 : 2012. 1. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
/*
 * 타입별 필수 파라미터 검증.
 * 스케줄 정보 체크 등등 
 * 기타 작업을 수행함 
 */
public interface IJobTypeChecker {

	/**
	 * Job 타입 명 리턴
	 */
	public String getTypeName();

	/**
	 * Job 타입별 스케줄정보, 필수 파라미터 정보 validation 수행
	 * @param jobdef
	 * @return list of in-validated message
	 */
	public List<String> validate(JobDefinition jobdef);

	/**
	 * Job 타입별 스케줄정보, 필수 파라미터 정보 validation 수행
	 * @param jobdef
	 * @return list of in-validated message
	 */
	public List<String> validate(JobInstance jobins);

}
