package nexcore.scheduler.core.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import nexcore.scheduler.core.IJobInstanceIdMaker;
import nexcore.scheduler.core.IScheduleCalendar;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobInstance;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Definition 으로 부터 Job Instance 를 생성함. </li>
 * <li>작성일 : 2010. 9. 10.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class Activator {
	private JobDefinitionManager  jobDefinitionManager;
	private IJobInstanceIdMaker   jobInstanceIdMaker;
	private JobInstanceManager    jobInstanceManager;
	private IScheduleCalendar     scheduleCalendar;
	
	private Log                   log;

	public void init() {
		log = LogManager.getSchedulerLog();
	}
	
	public void destroy() {
	}

	public JobDefinitionManager getJobDefinitionManager() {
		return jobDefinitionManager;
	}

	public void setJobDefinitionManager(JobDefinitionManager jobDefinitionManager) {
		this.jobDefinitionManager = jobDefinitionManager;
	}

	public IJobInstanceIdMaker getJobInstanceIdMaker() {
		return jobInstanceIdMaker;
	}

	public void setJobInstanceIdMaker(IJobInstanceIdMaker jobInstanceIdMaker) {
		this.jobInstanceIdMaker = jobInstanceIdMaker;
	}

	public JobInstanceManager getJobInstanceManager() {
		return jobInstanceManager;
	}

	public void setJobInstanceManager(JobInstanceManager jobInstanceManager) {
		this.jobInstanceManager = jobInstanceManager;
	}

	public IScheduleCalendar getScheduleCalendar() {
		return scheduleCalendar;
	}

	public void setScheduleCalendar(IScheduleCalendar scheduleCalendar) {
		this.scheduleCalendar = scheduleCalendar;
	}

	public JobInstance activate(String jobId, Map additionalParam, AdminAuth auth) throws SQLException {
		return activate(jobId, Util.getCurrentYYYYMMDD(), additionalParam, auth);
	}
	
	public JobInstance activate(String jobId, String procDate, Map additionalParam, AdminAuth auth) throws SQLException {
		JobDefinition jobdef = jobDefinitionManager.getJobDefinitionDeep(jobId);
		if (jobdef == null) {
			throw new SchedulerException("main.act.job.notfound.error", jobId, auth); // 존재하지 않는 Job 이므로 인스턴스 생성할 수 없습니다
		}
		return activate(jobdef, procDate, additionalParam, auth);
	}

	public JobInstance activate(JobDefinition jobdef, Map additionalParam, AdminAuth auth) throws SQLException {
		return activate(jobdef, Util.getCurrentYYYYMMDD(), additionalParam, auth);
	}

	public JobInstance activate(JobDefinition jobdef, String procDate, Map additionalParam, AdminAuth auth) throws SQLException {
		return _activate(jobdef, procDate, additionalParam, auth, false);
	}
	
	public JobInstance activateAndLock(String jobId, Map additionalParam, AdminAuth auth) throws SQLException {
		return activateAndLock(jobId, Util.getCurrentYYYYMMDD(), additionalParam, auth);
	}
	
	public JobInstance activateAndLock(String jobId, String procDate, Map additionalParam, AdminAuth auth) throws SQLException {
		JobDefinition jobdef = jobDefinitionManager.getJobDefinitionDeep(jobId);
		return activateAndLock(jobdef, procDate, additionalParam, auth);
	}
	
	public JobInstance activateAndLock(JobDefinition jobdef, Map additionalParam, AdminAuth auth) throws SQLException {
		return activateAndLock(jobdef, Util.getCurrentYYYYMMDD(), additionalParam, auth);
	}

	public JobInstance activateAndLock(JobDefinition jobdef, String procDate, Map additionalParam, AdminAuth auth) throws SQLException {
		return _activate(jobdef, procDate, additionalParam, auth, true);
	}

	/**
	 * 실제로 인스턴스를 생성하여 NBS_JOB_INS 테이블에 insert함.
	 * 
	 * @param jobdef
	 * @param procDate
	 * @param additionalParam
	 * @param auth
	 * @param lock
	 * @throws SQLException
	 */
	private JobInstance _activate(JobDefinition jobdef, String procDate, Map additionalParam, AdminAuth auth, boolean lock) throws SQLException {
		if (additionalParam != null) { // 등록된 기본 파라미터 이외에 추가 파라미터가 필요한 경우 추가한다.
			jobdef.getInParameters().putAll(additionalParam);
		}
		
		// Job Instnace Id 채번
		String newJobInstanceId = jobInstanceIdMaker.makeJobInstanceId(jobdef.getJobId(), procDate);
		
		Util.logInfo(log, MSG.get("main.act.activating", newJobInstanceId, procDate, auth)); // 인스턴스 생성합니다.

		// Job Instance 객체 생성
		JobInstance jobins = makeNewJobInstance(jobdef, procDate, newJobInstanceId, JobInstance.JOB_STATE_INIT, auth.toString());
		if (lock) {
			jobins.setLockedBy(auth.getOperatorId());
		}

		/// Job Instance 인서트.
		jobInstanceManager.insertJobInstance(jobins);
		return jobins;
	}
	
	/**
	 * base_date를 계산한다.
	 * @param jobdef
	 * @return
	 */
	public String calcBaseDate(JobDefinition jobdef, String procDate) {
		try {
			if (!Util.isBlank(jobdef.getBaseDateCalId())) {
				int baseDateLogic = Util.toInt(jobdef.getBaseDateLogic(), 0); // null, "" 이면 0
				Calendar baseDate = scheduleCalendar.getNextDayOfCalendar(jobdef.getBaseDateCalId(), CalendarUtil.convYYYYMMDDToCalendar(procDate), baseDateLogic);
				return String.valueOf(CalendarUtil.convCalendarToYYYYMMDD(baseDate));
			}else {
				return procDate; // base_date 달력이 설정되어있지 않으면 PROC_DATE 를 Base_Date로함
			}
		}catch(Exception e) {
			throw new SchedulerException("main.act.make.basedate.error", e, jobdef.getJobId(), jobdef.getCalendarId(), jobdef.getBaseDateLogic());
		}
	}
	
	/**
	 * 한달치 base_date를 계산한다.
	 * @param jobdef
	 * @return
	 */
	public Map calcBaseDateMonthly(JobDefinition jobdef, String procDateYYYYMM) {
		Map retval = new LinkedHashMap();
		Calendar cal = CalendarUtil.convYYYYMMDDToCalendar(Integer.parseInt(procDateYYYYMM+"01"));
		int month1 = cal.get(Calendar.MONTH); // 시작일의 월.
		while(true) {
			if (!Util.isBlank(jobdef.getBaseDateCalId())) {
				int baseDateLogic = Integer.parseInt(jobdef.getBaseDateLogic());
				Calendar baseDate = scheduleCalendar.getNextDayOfCalendar(
						jobdef.getBaseDateCalId(), cal, baseDateLogic);
				
				retval.put(CalendarUtil.convCalendarToYYYYMMDD(cal), CalendarUtil.convCalendarToYYYYMMDD(baseDate));
			}else {
				retval.put(CalendarUtil.convCalendarToYYYYMMDD(cal), CalendarUtil.convCalendarToYYYYMMDD(cal));
			}
			
			cal.add(Calendar.DATE, 1);
			
			if (cal.get(Calendar.MONTH) != month1) { // 내일이 다음달이면 끝.  
				break;
			}
		}
		return retval;
	}
	
	
	/**
	 * activation 을 위해 Job Instance 를 만듬.
	 * @param jobdef
	 */
    private JobInstance makeNewJobInstance(JobDefinition jobdef, String procDate, String newJobInstanceId, String jobState, String activator) {
    	JobInstance jobins = new JobInstance();
    	jobins.setProcDate        (procDate);
		jobins.setBaseDate        (calcBaseDate(jobdef, procDate));
		jobins.setJobId           (jobdef.getJobId());
		jobins.setJobGroupId      (jobdef.getJobGroupId());
		jobins.setJobInstanceId   (newJobInstanceId);
		jobins.setDescription     (jobdef.getDescription());
		jobins.setJobState        (jobState);

		jobins.setTimeFrom        (jobdef.getTimeFrom());
		jobins.setTimeUntil       (jobdef.getTimeUntil());
		jobins.setRepeatYN        (jobdef.getRepeatYN());
		jobins.setRepeatIntval    (jobdef.getRepeatIntval());
		jobins.setRepeatIntvalGb  (jobdef.getRepeatIntvalGb());
		jobins.setRepeatIfError   (jobdef.getRepeatIfError());
		jobins.setRepeatMaxOk     (jobdef.getRepeatMaxOk());
		jobins.setRepeatExactExp  (jobdef.getRepeatExactExp());

		jobins.setConfirmNeedYN   (jobdef.getConfirmNeedYN());
		jobins.setParallelGroup   (jobdef.getParallelGroup());
		jobins.setJobType         (jobdef.getJobType());
		jobins.setAgentNode       (jobdef.getAgentNode());
		jobins.setComponentName   (jobdef.getComponentName());
		jobins.setActivationTime  (Util.getCurrentYYYYMMDDHHMMSS());
		jobins.setActivator       (activator);
		jobins.setRunCount        (0);
		jobins.setInParameters    (jobdef.getInParameters());
		jobins.setLogLevel(       jobdef.getLogLevel());
		
		jobins.setPreJobConditions(new ArrayList(jobdef.getPreJobConditions()));
		jobins.setTriggerList     (new ArrayList(jobdef.getTriggerList()));
    	return jobins;
    }
}
