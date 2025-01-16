package nexcore.scheduler.controller;

import java.util.List;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 등록 신청시 Job 등록 정보의 값을 validation 하는 클래스. 각 프로젝트의 Naming Rule 에 따라 validation 한다.</li>
 * <li>작성일 : 2010. 10. 18.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IJobInfoValidator {

	/**
	 * Job 등록 정보를 validation 한다.
	 * @param jobdef
	 * @return [validation fail 사유] 의 List.
	 */
	public List validate(JobDefinition  jobdef);
	
	/**
	 * Job 인스턴스 정보를 validation 한다.
	 * Job 인스턴스의 정보를 Admin 에서 수정할 경우에도 validation이 필요하다
	 * @param jobins
	 * @return [validation fail 사유] 의 List.
	 */
	public List validate(JobInstance  jobins);

}