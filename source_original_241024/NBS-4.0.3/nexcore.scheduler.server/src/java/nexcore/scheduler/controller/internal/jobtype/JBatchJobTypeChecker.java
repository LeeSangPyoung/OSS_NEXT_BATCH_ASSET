package nexcore.scheduler.controller.internal.jobtype;

import java.util.LinkedList;
import java.util.List;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.JobType;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  EJB 타입의 Job Type Manager</li>
 * <li>작성일 : 2012. 1. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JBatchJobTypeChecker extends AbsJobTypeChecker {

	public String getTypeName() {
		return JobType.JOB_TYPE_JBATCH;
	}

	public List<String> validate(JobDefinition jobdef) {
		List<String> checkResult = new LinkedList<String>();
		validateDefault(jobdef, checkResult);
		
		checkComponentName(jobdef.getComponentName(), checkResult);
		
		return checkResult;
	}
	
	public List<String> validate(JobInstance jobins) {
		List<String> checkResult = new LinkedList<String>();
		validateDefault(jobins, checkResult);

		checkComponentName(jobins.getComponentName(), checkResult);
		
		return checkResult;
	}

}
