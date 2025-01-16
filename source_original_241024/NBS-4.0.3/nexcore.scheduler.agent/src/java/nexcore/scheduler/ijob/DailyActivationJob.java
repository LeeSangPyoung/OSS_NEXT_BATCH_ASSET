 /**
 * 
 */
package nexcore.scheduler.ijob;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : DailyActivation 을 Job 으로 수행하는 내장 Job. </li>
 * <li>작성일 : 2012. 12. 3.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class DailyActivationJob {

	
	public DailyActivationJob() {
	}
	
	public void execute(JobContext context) {
		Log    log        = context.getLogger();
		
		String targetDate = context.getInParameter("TARGET_DATE");
		
		if (targetDate == null) {
			targetDate = context.getInParameter("DATE");
			Util.logWarn(log, MSG.get("main.dailyact.targetdate.notexist", targetDate));
		}
		
		try {
			// DailyActivator 를 호출하여 일일 activation 작업을 수행한다.
			// 컴파일 의존성 제거를 위해 reflection 으로 한다.
			Class dailyActivator = Class.forName("nexcore.scheduler.core.internal.DailyActivator");
			Method getInstanceMethod = dailyActivator.getDeclaredMethod("getInstance", new Class[0]);
			Object dailyActivatorObj = getInstanceMethod.invoke(null, new Object[0]);
			
			Method doDailyActivationProcessMethod = dailyActivator.getDeclaredMethod("doDailyActivationProcess", new Class[]{String.class, org.apache.commons.logging.Log.class});
			Object retval = doDailyActivationProcessMethod.invoke(dailyActivatorObj, new Object[]{targetDate, log});

			context.setReturnValue("JOBINSTANCE_CNT", String.valueOf(retval));
			context.setReturnValue("TARGET_DATE",     targetDate);
		}catch(Exception e) {
			throw Util.toRuntimeException(e);
		}
	}
	
}
