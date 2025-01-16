/**
 * 
 */
package nexcore.scheduler.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import nexcore.scheduler.entity.JobDefinition;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2012. 1. 31.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class JobDefinitionUtil {

	public static JobDefinition readFromFile(File f) {
		ObjectInputStream oin    = null;
		JobDefinition     jobdef = null;
		try {
			oin = new ObjectInputStream(new FileInputStream(f));
			jobdef = (JobDefinition)oin.readObject();
		} catch (Exception e) {
			throw Util.toRuntimeException(e);
		} finally {
			try { oin.close(); }catch(Exception ee) {}
		}
		return jobdef;
	}
	
	/**
	 * JobDefinition 객체를 파일로 저장한다.
	 * @param jobdef
	 * @return
	 */
	public static void writeToFile(JobDefinition jobdef, File file) {
		ObjectOutputStream oo = null;
		
		try {
			oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			oo.writeObject(jobdef);
		} catch (Exception e) {
			throw Util.toRuntimeException(e);
		} finally {
			try { oo.close(); }catch(Exception e) {}
		}
	}

    // 두 값이 다르면 색깔 표시
    private static boolean _isEquals(Object o1, Object o2) {
		boolean diff;
		if (o1 instanceof String || o2 instanceof String) {
			diff = Util.nvl(o1).equals(Util.nvl(o2));
		}else {
			diff = o1 == null ? o2 == null : o1.equals(o2);
		}
		
		return diff;
    }

    /**
     * 
     * @param jobdef1
     * @param jobdef2
     * @return
     */
	public static boolean isEquals(JobDefinition jobdef1, JobDefinition jobdef2) {
		if (!_isEquals(jobdef1.getJobId(),                     	      jobdef2.getJobId())) {
		    return false;
		}else if (!_isEquals(jobdef1.getJobGroupId(),                 jobdef2.getJobGroupId())) {
		    return false;
		}else if (!_isEquals(jobdef1.getOwner(),                      jobdef2.getOwner())) {
		    return false;
		}else if (!_isEquals(jobdef1.getDescription(),                jobdef2.getDescription())) {
		    return false;
		}else if (!_isEquals(jobdef1.getTimeFrom(),                   jobdef2.getTimeFrom())) {
		    return false;
		}else if (!_isEquals(jobdef1.getTimeUntil(),                  jobdef2.getTimeUntil())) {
		    return false;
		}else if (!_isEquals(jobdef1.getRepeatYN(),                   jobdef2.getRepeatYN())) {
		    return false;
		}else if (!_isEquals(jobdef1.getRepeatIntval(),               jobdef2.getRepeatIntval())) {
		    return false;
		}else if (!_isEquals(jobdef1.getRepeatIntvalGb(),             jobdef2.getRepeatIntvalGb())) {
		    return false;
		}else if (!_isEquals(jobdef1.getRepeatIfError(),              jobdef2.getRepeatIfError())) {
		    return false;
		}else if (!_isEquals(jobdef1.getRepeatMaxOk(),                jobdef2.getRepeatMaxOk())) {
		    return false;
		}else if (!_isEquals(jobdef1.getRepeatExactExp(),             jobdef2.getRepeatExactExp())) {
		    return false;
		}else if (!_isEquals(jobdef1.getConfirmNeedYN(),              jobdef2.getConfirmNeedYN())) {
		    return false;
		}else if (!_isEquals(jobdef1.getParallelGroup(),              jobdef2.getParallelGroup())) {
		    return false;
		}else if (!_isEquals(jobdef1.getJobType(),                    jobdef2.getJobType())) {
		    return false;
		}else if (!_isEquals(jobdef1.getAgentNode(),                  jobdef2.getAgentNode())) {
		    return false;
		}else if (!_isEquals(jobdef1.getComponentName(),              jobdef2.getComponentName())) {
		    return false;
		}else if (!_isEquals(jobdef1.getTriggerList(),                jobdef2.getTriggerList())) {
		    return false;
		}else if (!_isEquals(jobdef1.getScheduleType(),               jobdef2.getScheduleType())) {
		    return false;
		}else if (!_isEquals(jobdef1.getMonths(),                     jobdef2.getMonths())) {
		    return false;
		}else if (!_isEquals(jobdef1.getDayOfMonthScheduleType(),     jobdef2.getDayOfMonthScheduleType())) {
		    return false;
		}else if (!_isEquals(jobdef1.getDaysInMonth(),                jobdef2.getDaysInMonth())) {
		    return false;
		}else if (!_isEquals(jobdef1.getCalendarId(),                 jobdef2.getCalendarId())) {
		    return false;
		}else if (!_isEquals(jobdef1.getCalendarExps(),               jobdef2.getCalendarExps())) {
		    return false;
		}else if (!_isEquals(jobdef1.getWeekdayMonthdayType(),        jobdef2.getWeekdayMonthdayType())) {
		    return false;
		}else if (!_isEquals(jobdef1.getDaysOfWeek(),                 jobdef2.getDaysOfWeek())) {
		    return false;
		}else if (!_isEquals(jobdef1.getBeforeAfterExp(),             jobdef2.getBeforeAfterExp())) {
		    return false;
		}else if (!_isEquals(jobdef1.getShiftExp(),                   jobdef2.getShiftExp())) {
		    return false;
		}else if (!_isEquals(jobdef1.getShiftExp2(),                  jobdef2.getShiftExp2())) {
		    return false;
		}else if (!_isEquals(jobdef1.getFixedDays(),                  jobdef2.getFixedDays())) {
		    return false;
		}else if (!_isEquals(jobdef1.isReverse(),                     jobdef2.isReverse())) {
		    return false;
		}else if (!_isEquals(jobdef1.getBaseDateCalId(),              jobdef2.getBaseDateCalId())) {
		    return false;
		}else if (!_isEquals(jobdef1.getBaseDateLogic(),              jobdef2.getBaseDateLogic())) {
		    return false;
		}else if (!_isEquals(jobdef1.getPreJobConditions(),           jobdef2.getPreJobConditions())) {
		    return false;
		}else if (!_isEquals(jobdef1.getInParameters(),               jobdef2.getInParameters())) {
		    return false;
		}
		return true;
	}
	
}
