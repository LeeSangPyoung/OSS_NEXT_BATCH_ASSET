package nexcore.scheduler.controller.internal.jobtype;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.JobType;
import nexcore.scheduler.msg.MSG;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  Quartz Job Type Checker</li>
 * <li>작성일 : 2015. 10. 20.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class RestAPIJobTypeChecker extends AbsJobTypeChecker {

	public String getTypeName() {
		return JobType.JOB_TYPE_RESTAPI;
	}

	public List<String> validate(JobDefinition jobdef) {
		List<String> checkResult = new LinkedList<String>();
		validateDefault(jobdef, checkResult);
		
		checkComponentName(jobdef.getComponentName(), checkResult);
		
		checkParameter(jobdef.getInParameters(), checkResult);
		
		return checkResult;
	}

	public List<String> validate(JobInstance jobins) {
		List<String> checkResult = new LinkedList<String>();
		validateDefault(jobins, checkResult);
		
		checkComponentName(jobins.getComponentName(), checkResult);
		
		checkParameter(jobins.getInParameters(), checkResult);
		
		return checkResult;
	}
	
	private void checkParameter(Map parameters, List<String> checkResult) {
//		if (!parameters.containsKey("REST_BASE_URI")) {
//			checkResult.add("[RestAPI Job]:"+MSG.get("main.jobinfo.check.missing.param", "RestAPI", "REST_BASE_URI")); // RestAPI Job 은 'REST_BASE_URI' 파라미터 값이 필수입니다.");
//		}
	}

}
