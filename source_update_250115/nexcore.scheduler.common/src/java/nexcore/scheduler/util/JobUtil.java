/**
 * 
 */
package nexcore.scheduler.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexcore.scheduler.entity.JobDefinition;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2012. 3. 30.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class JobUtil {
	private static Map<String, Comparator<JobDefinition>> jobdefComparator;

	/**
	 * JobDefinition 리스트를 정렬한다.
	 * @param list
	 */
	public static void sort(List<JobDefinition> list) {
		sort(list, "jobId");
	}
	
	/**
	 * JobDefinition 리스트를 정렬한다.
	 * @param list
	 * @param orderBy
	 */
	public static void sort(List<JobDefinition> list, String orderBy) {
		if (jobdefComparator == null) {
			Map<String, Comparator<JobDefinition>> tmpMap = new HashMap<String, Comparator<JobDefinition>>();
			
			tmpMap.put("jobId", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					return o1.getJobId().compareTo(o2.getJobId());
				}
			});
			tmpMap.put("jobGroupId", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					return o1.getJobGroupId().compareTo(o2.getJobGroupId());
				}
			});
			tmpMap.put("description", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					return Util.nvl(o1.getDescription()).compareTo(Util.nvl(o2.getDescription()));
				}
			});
			tmpMap.put("owner", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					return Util.nvl(o1.getOwner()).compareTo(Util.nvl(o2.getOwner()));
				}
			});
			tmpMap.put("timeFrom", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					return Util.nvl(o1.getTimeFrom()).compareTo(Util.nvl(o2.getTimeFrom()));
				}
			});
			tmpMap.put("parallelGroup", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					return Util.nvl(o1.getParallelGroup()).compareTo(Util.nvl(o2.getParallelGroup()));
				}
			});
			tmpMap.put("jobType", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					return o1.getJobType().compareTo(o2.getJobType());
				}
			});
			tmpMap.put("agentNode", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					return o1.getAgentNode().compareTo(o2.getAgentNode());
				}
			});
			tmpMap.put("componentName", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					return Util.nvl(o1.getComponentName()).compareTo(Util.nvl(o2.getComponentName()));
				}
			});
			tmpMap.put("lastModifyTime", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					return o1.getLastModifyTime().compareTo(o2.getLastModifyTime());
				}
			});
			tmpMap.put("auto", new Comparator<JobDefinition>() {
				public int compare(JobDefinition o1, JobDefinition o2) {
					int b1 = isScheduledForDailyActivation(o1) ? 1 : 0;
					int b2 = isScheduledForDailyActivation(o2) ? 1 : 0;
					return b1 - b2;
				}
			});
			
			jobdefComparator = tmpMap;
		}
		
		Comparator comparator = jobdefComparator.get(orderBy);
		if (comparator == null) {
			throw new IllegalArgumentException("Wrong orderBy ("+orderBy+") argument");
		}
		
		Collections.sort(list, comparator);
		
	}
	
	/**
	 * DailyActivator 에 의해 인스턴스 생성되도록 스케줄되어있는지?
	 * (자동 / 수동 여부 체크)
	 * @param jobdef
	 * @return
	 */
	public static boolean isScheduledForDailyActivation(JobDefinition jobdef) {
		if (JobDefinition.SCHEDULE_TYPE_EXPRESSION.equals(jobdef.getScheduleType())) {
			if (Util.isBlank(jobdef.getMonths())) {
				return false;  /* 월 설정안되어있으면 auto 실행 안됨*/
			}
			
			boolean dayScheduled = false;
			if (JobDefinition.DAY_SCHEDULE_TYPE_CALENDAR.equals(jobdef.getDayOfMonthScheduleType())) {
				dayScheduled = !Util.isBlank(jobdef.getCalendarId());
			}else if (JobDefinition.DAY_SCHEDULE_TYPE_NUMBER.equals(jobdef.getDayOfMonthScheduleType())) {
				dayScheduled = !Util.isBlank(jobdef.getDaysInMonth());
			}
			
			if (JobDefinition.WEEKDAY_MONTHDAY_TYPE_AND.equals(jobdef.getWeekdayMonthdayType())) {
				return dayScheduled && !Util.isBlank(jobdef.getDaysOfWeek());
			}else {
				return dayScheduled || !Util.isBlank(jobdef.getDaysOfWeek());
			}			
		}else if (JobDefinition.SCHEDULE_TYPE_FIXED.equals(jobdef.getScheduleType())) {
			return !Util.isBlank(jobdef.getFixedDays()); 
		}else {
			return false;
		}
	}
}
