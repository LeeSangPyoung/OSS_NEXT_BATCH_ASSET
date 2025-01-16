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
 * <li>설  명 :  Sleep 타입의 Job Type Checker</li>
 * <li>작성일 : 2012. 1. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class CenterCutJobTypeChecker extends AbsJobTypeChecker {

	public String getTypeName() {
		return JobType.JOB_TYPE_CENTERCUT;
	}

	public List<String> validate(JobDefinition jobdef) {
		List<String> checkResult = new LinkedList<String>();
		validateDefault(jobdef, checkResult);
		
		checkParameter(jobdef.getInParameters(), checkResult);
		
		return checkResult;
	}

	public List<String> validate(JobInstance jobins) {
		List<String> checkResult = new LinkedList<String>();
		validateDefault(jobins, checkResult);
		
		checkParameter(jobins.getInParameters(), checkResult);
		
		return checkResult;
	}
	
	private void checkParameter(Map parameters, List<String> checkResult) {
		if (!parameters.containsKey("CC_ID")) {
			checkResult.add("[Center Cut Job]:"+MSG.get("main.jobinfo.check.missing.param", "CenterCut", "CC_ID")); // Center Cut Job 은 'CC_ID' 파라미터 값이 필수입니다.");
		}
//		if (!parameters.containsKey("CCIN_TABLE_NAME")) {
//			checkResult.add("[Center Cut Job]:"+MSG.get("main.jobinfo.check.missing.param", "CenterCut", "CCIN_TABLE_NAME")); // Center Cut Job 은 'CCIN_TABLE_NAME' 파라미터 값이 필수입니다.");
//		}
//      2015.9.1. CCIN_TABLE_NAME 은 센터컷등록정보 테이블에서 읽으므로 필수에서 제외한다

	}

}
