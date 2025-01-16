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
 * <li>설  명 :  FileWatch 타입의 Job Type Checker</li>
 * <li>작성일 : 2012. 1. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class FileWatchJobTypeChecker extends AbsJobTypeChecker {

	public String getTypeName() {
		return JobType.JOB_TYPE_FILEWATCH;
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
		if (!parameters.containsKey("DIRECTORY")) {
			checkResult.add("[File Watch Job]:"+MSG.get("main.jobinfo.check.missing.param", "FileWatch", "DIRECTORY")); //File Watch Job 은 'DIRECTORY' 파라미터 값이 필수입니다.");
		}

		if (!parameters.containsKey("FILENAME")) {
			checkResult.add("[File Watch Job]:"+MSG.get("main.jobinfo.check.missing.param", "FileWatch", "FILENAME")); //File Watch Job 은 'FILENAME' 파라미터 값이 필수입니다.");
		}
	}

}
