package nexcore.scheduler.entity;

import java.lang.reflect.Field;
import java.util.Arrays;

public class JobExecutionStateHelper {
	public static String getStateString(int state) {
		Field[] fields = JobExecution.class.getFields();
		
		try {
			for (int i=0; i<fields.length; i++) {
				if (fields[i].getInt(null) == state && fields[i].getName().startsWith("STATE_")) {
					return  fields[i].getName().substring(6);
				}
			}
		}catch (Exception e) {
		}
		return "N/A";
	}
	
	public static String[] getPossibleAction (int state) {
		switch(state) {
		
		case JobExecution.STATE_INIT : 
			return new String[]{};
		case JobExecution.STATE_RUNNING :
			return new String[]{"SUSPEND", "STOP"};
		case JobExecution.STATE_ENDED : 
			return new String[]{};
		case JobExecution.STATE_SUSPENDED : 
			return new String[]{"RESUME", "STOP"};
		}
		return new String[]{};
	}
	
	public static void main(String[] args) {
		System.out.println(getStateString(JobExecution.STATE_SUSPENDED));
		System.out.println(getStateString(JobExecution.STATE_INIT));
		System.out.println(getStateString(JobExecution.STATE_RUNNING));
		System.out.println(getStateString(JobExecution.STATE_ENDED));
		System.out.println(Arrays.asList(getPossibleAction(JobExecution.STATE_SUSPENDED)));
		System.out.println(Arrays.asList(getPossibleAction(JobExecution.STATE_INIT)));
		System.out.println(Arrays.asList(getPossibleAction(JobExecution.STATE_RUNNING)));
		System.out.println(Arrays.asList(getPossibleAction(JobExecution.STATE_ENDED)));
		
	}
	

}
