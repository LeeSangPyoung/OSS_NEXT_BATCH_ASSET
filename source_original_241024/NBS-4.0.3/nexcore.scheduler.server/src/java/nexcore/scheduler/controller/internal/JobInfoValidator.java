package nexcore.scheduler.controller.internal;

import java.util.ArrayList;
import java.util.List;

import nexcore.scheduler.controller.IJobInfoValidator;
import nexcore.scheduler.controller.internal.jobtype.IJobTypeChecker;
import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.JobType;
import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 등록 신청시 Job 등록 정보의 값을 validation 하는 클래스. 각 프로젝트의 Naming Rule 에 따라 validation 한다.</li>
 * <li>작성일 : 2010. 10. 18.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobInfoValidator implements IJobInfoValidator {
	
	private JobTypeManager         jobTypeManager;
	
	public JobInfoValidator() {
	}

	public void init() {
	}
	
	public void destroy() {
	}

	public JobTypeManager getJobTypeManager() {
		return jobTypeManager;
	}

	public void setJobTypeManager(JobTypeManager jobTypeManager) {
		this.jobTypeManager = jobTypeManager;
	}

	/**
	 * Job 등록 정보를 validation 한다.
	 * @param jobdef
	 * @return [validation항목,validation fail 사유] 의 Map.
	 */
	public List<String> validate(JobDefinition jobdef) {
		if (jobTypeManager == null) {
			// 기존의 jobTypeManager 설정 안되어 있는 경우도 에러없이 돌아가도록 하기 위해 기존 로직을 돌림.
			return validateOldversion(jobdef);
		}else {
			// 애초에 여기에 validation 로직이 구현되어있던 것을 JobTypeManager 로 분리해냄.
			IJobTypeChecker checker = jobTypeManager.getJobTypeChecker(jobdef.getJobType());
			List<String> checkResult = checker.validate(jobdef);
			return checkResult;
		}			
	}
	
	public List<String> validate(JobInstance jobins) {
		IJobTypeChecker checker = jobTypeManager.getJobTypeChecker(jobins.getJobType());
		List<String> checkResult = checker.validate(jobins);
	
		return checkResult;
	}
	
	
	/**
	 * 구버전 validator. 파라미터 체크시 jobTypeManager 가 없는 경우 이 로직이 수행됨
	 * @param jobdef
	 * @return [validation항목,validation fail 사유] 의 Map.
	 */
	private List validateOldversion(JobDefinition  jobdef) {
		// Job 정보 validation
		List list = new ArrayList();
		
		// 1. Trigger Job Id 체크. 무한루프 방지
		if (jobdef.getTriggerList() != null) {
			for (PostJobTrigger trigger : jobdef.getTriggerList()) {
				if (jobdef.getJobId().equals(trigger.getTriggerJobId()) ) {
					list.add("[Trigger Job Id]:"+MSG.get("main.jobinfo.check.cannot.trigger.self")); // "[Trigger Job Id]:자기 자신을 Trigger Job으로 설정할 수 없습니다."
				}
			}
		}
		
		// 2. Sleep Job 파라미터 체크.
		if (JobType.JOB_TYPE_SLEEP.equals(jobdef.getJobType()) && !jobdef.getInParameters().containsKey("SLEEP_TIME")) {
			list.add("[Sleep Job]:"+MSG.get("main.jobinfo.check.missing.param", "Sleep", "SLEEP_TIME")); //Sleep Job 은 'SLEEP_TIME' 파라미터가 필수입니다. (초단위)");
		}
		
		// 3. Base Date Calendar 로직이 not null 이면 BaseDateLogic 도 not null 이어야한다.
		if (!Util.isBlank(jobdef.getBaseDateCalId())) {
			if (Util.isBlank(jobdef.getBaseDateLogic())) {
				list.add("[Base Date]:"+MSG.get("main.jobinfo.check.basedate.logic.missing")); // Base Date 연산 로직이 잘못 되었습니다.
			}
		}
		
		// 4. RepeatJob Until 타임 지정
		if ("Y".equals(jobdef.getRepeatYN()) && Util.isBlank(jobdef.getTimeUntil())) {
			list.add("[Repeat Job]:"+MSG.get("main.jobinfo.check.missing.attribute", "Repeat", "Time Until")); // Repeat Job 은 Time Until 속성이 필수입니다.
		}
		if ("Y".equals(jobdef.getRepeatYN()) && !"EXACT".equals(jobdef.getRepeatIntvalGb()) && jobdef.getRepeatIntval() == 0) {
			list.add("[Repeat Job]:"+MSG.get("main.jobinfo.check.wrong.attribute", "Repeat Interval")); //Repeat Interval 속성값이 잘못 되었습니다
		}


		// 5-1. FileWatch
		if (JobType.JOB_TYPE_FILEWATCH.equals(jobdef.getJobType()) && !jobdef.getInParameters().containsKey("DIRECTORY")) {
			list.add("[File Watch Job]:"+MSG.get("main.jobinfo.check.missing.param", "FileWatch", "DIRECTORY")); //File Watch Job 은 'DIRECTORY' 파라미터 값이 필수입니다.");
		}

		// 5-2. FileWatch
		if (JobType.JOB_TYPE_FILEWATCH.equals(jobdef.getJobType()) && !jobdef.getInParameters().containsKey("FILENAME")) {
			list.add("[File Watch Job]:"+MSG.get("main.jobinfo.check.missing.param", "FileWatch", "FILENAME")); //File Watch Job 은 'FILENAME' 파라미터 값이 필수입니다.");
		}

		// 6. CENTERCUT 
		if (JobType.JOB_TYPE_CENTERCUT.equals(jobdef.getJobType()) && !jobdef.getInParameters().containsKey("CC_ID")) {
			list.add("[Center Cut Job]:"+MSG.get("main.jobinfo.check.missing.param", "CenterCut", "CC_ID")); // Center Cut Job 은 'CC_ID' 파라미터 값이 필수입니다.");
		}
//		if (JobType.JOB_TYPE_CENTERCUT.equals(jobdef.getJobType()) && !jobdef.getInParameters().containsKey("CCIN_TABLE_NAME")) {
//			list.add("[Center Cut Job]:"+MSG.get("main.jobinfo.check.missing.param", "CenterCut", "CCIN_TABLE_NAME")); // Center Cut Job 은 'CCIN_TABLE_NAME' 파라미터 값이 필수입니다.");
//		}
//		2015.9.1. CCIN_TABLE_NAME 은 센터컷등록정보 테이블에서 읽으므로 필수에서 제외한다
		
		// 7. 프로그램
		if ((JobType.JOB_TYPE_JBATCH.equals(jobdef.getJobType()) || JobType.JOB_TYPE_POJO.equals(jobdef.getJobType()) || JobType.JOB_TYPE_PROC.equals(jobdef.getJobType())) &&
			Util.isBlank(jobdef.getComponentName()))	{
			list.add("[Component]:"+MSG.get("main.jobinfo.check.wrong.attribute", "Program")); // 프로그램명 또는 클래스명이 필요합니다.");
		}
		return list;
	}

}