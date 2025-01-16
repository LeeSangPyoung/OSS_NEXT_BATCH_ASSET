package nexcore.scheduler.controller.internal;

import java.util.Arrays;
import java.util.List;

import nexcore.scheduler.core.IMonitorDisplayable;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 설정값 들을 담고 있는 Bean </li>
 * <li>작성일 : 2012. 12. 26.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class CustomConfig implements IMonitorDisplayable {
	/*
	 * 로그 레벨 리스트 설정. 사이트 마다 폼에 나오는 로그 레벨 리스트를 다르게 할 수 있다.
	 * 기본값은 FATAL,ERROR,WARN,INFO,DEBUG,TRACE 임.
	 */
	private String                        logLevelUsing; 
	private List<String>                  logLevelUseList; // 이 사이트에서 사용중인 로그 레벨.
	
	private int                           jobAliveDay;             // Job 인스턴스가 몇일 동안 Alive 한 후 expire 되어야하는지를 설정함. 미지정시 2일이 적용됨.
	
	private static CustomConfig instance;  // 어디서든 쉽게 사용하기 위해 이렇게 singleton 으로 처리한다.

	public CustomConfig() {
		instance = this;
	}
	
	public void init() {
	}
	
	public void destroy() {
	}

	public static CustomConfig getInstance() {
		return instance;
	}
	
	public String getLogLevelUsing() {
		return logLevelUsing;
	}

	public void setLogLevelUsing(String jobTypeUseListString) {
		this.logLevelUsing = jobTypeUseListString;
		if (!Util.isBlank(this.logLevelUsing)) {
			if (this.logLevelUsing.indexOf("${") < 0) { // 설정값이 제대로 되어있는 경우에만...
				logLevelUseList = Arrays.asList(this.logLevelUsing.split(","));
			}
		}
		
		if (logLevelUseList == null) {
			this.logLevelUsing = "FATAL,ERROR,WARN,INFO,DEBUG,TRACE";
			
			logLevelUseList = Arrays.asList(this.logLevelUsing.split(","));
		}
	}

	public List<String> getLogLevelUsingList() {
		return logLevelUseList;
	}

	public int getJobAliveDay() {
		return jobAliveDay;
	}

	public void setJobAliveDay(int jobAliveDay) {
		this.jobAliveDay = jobAliveDay;
	}
	
	public String getDisplayName() {
		return "Config";
	}
	
	public String getDisplayString() {
		return "JobAliveDay : "+jobAliveDay;
	}
}
