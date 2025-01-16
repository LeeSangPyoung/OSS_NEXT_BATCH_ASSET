package nexcore.scheduler.controller.internal.jobtype;

import java.util.List;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

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

public abstract class AbsJobTypeChecker implements IJobTypeChecker {
	
	/**
	 * 공통 validation
	 * @param jobdef
	 * @return
	 */
	protected void _validateTrigger(String jobId, List<PostJobTrigger> triggerList, List<String> checkResult) {
		// 1. Trigger Job Id 체크. 무한루프 방지
		if (triggerList != null) {
			for (PostJobTrigger trigger : triggerList) {
				if (Util.equals(trigger.getTriggerJobId(), jobId)) {
					checkResult.add("[Trigger Job Id]:"+MSG.get("main.jobinfo.check.cannot.trigger.self")); // "[Trigger Job Id]:자기 자신을 Trigger Job으로 설정할 수 없습니다."
				}
			}
		}
	}
	
	/**
	 * 기준일 로직 체크
	 * @param baseDateCalId
	 * @param baseDateLogic
	 * @param checkResult
	 */
	protected void _validateBaseDate(String baseDateCalId, String baseDateLogic, List<String> checkResult) {
		// 3. Base Date Calendar 로직이 not null 이면 BaseDateLogic 도 not null 이어야한다.
		if (!Util.isBlank(baseDateCalId)) {
			if (Util.isBlank(baseDateLogic)) {
				checkResult.add("[Base Date]:"+MSG.get("main.jobinfo.check.basedate.logic.missing")); // Base Date 연산 로직이 잘못 되었습니다.
			}
		}
	}
	
	/**
	 * 반복타입일 경우 Interval 값이나 TimeUntil 값 존재여부 체크 
	 * @param repeatYN
	 * @param repeatIntvalGb
	 * @param repeatIntval
	 * @param timeUntil
	 * @param checkResult
	 */
	protected void _validateRepeatUntil(String repeatYN, String repeatIntvalGb, int repeatIntval, String timeUntil, List<String> checkResult) {
		
		// 4. RepeatJob Until 타임 지정
		if ("Y".equals(repeatYN) && Util.isBlank(timeUntil)) {
			checkResult.add("[Repeat Job]:"+MSG.get("main.jobinfo.check.missing.attribute", "Repeat", "Time Until")); // Repeat Job 은 Time Until 속성이 필수입니다.
		}
		if ("Y".equals(repeatYN) && !"EXACT".equals(repeatIntvalGb) && repeatIntval == 0) {
			checkResult.add("[Repeat Job]:"+MSG.get("main.jobinfo.check.wrong.attribute", "Repeat Interval")); //Repeat Interval 속성값이 잘못 되었습니다
		}
	}

	protected void validateDefault(JobDefinition jobdef, List<String> checkResult) {
		_validateTrigger(jobdef.getJobId(), jobdef.getTriggerList(), checkResult);
		_validateBaseDate(jobdef.getBaseDateCalId(), jobdef.getBaseDateLogic(), checkResult);
		_validateRepeatUntil(jobdef.getRepeatYN(), jobdef.getRepeatIntvalGb(), jobdef.getRepeatIntval(), jobdef.getTimeUntil(), checkResult);
	}

	/**
	 * 공통 validation
	 * @param jobins
	 * @return
	 */
	protected void validateDefault(JobInstance jobins, List<String> checkResult) {
		_validateTrigger(jobins.getJobId(), jobins.getTriggerList(), checkResult);
		_validateRepeatUntil(jobins.getRepeatYN(), jobins.getRepeatIntvalGb(), jobins.getRepeatIntval(), jobins.getTimeUntil(), checkResult);
		// Job Instance 는 기준일 로직을 검증할 필요없다. (이미 상수값이므로)
	}
	
	/**
	 * EJB, POJO 타입인 경우 프로그램 클래스명 체크.
	 * @param componentName
	 * @return
	 */
	protected void checkComponentName(String componentName, List<String> checkResult) {
		if (Util.isBlank(componentName)) {
			checkResult.add("[Component]:"+MSG.get("main.jobinfo.check.wrong.attribute", "Program")); // 프로그램명 또는 클래스명이 필요합니다.
		}
	}
	
}
