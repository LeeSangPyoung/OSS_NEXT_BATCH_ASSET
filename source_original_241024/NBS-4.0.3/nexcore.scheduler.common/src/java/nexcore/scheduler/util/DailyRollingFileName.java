/* 
* Copyright (c) 2016 SK C&C Co., Ltd. All rights reserved. 
* 
* This software is the confidential and proprietary information of SK C&C. 
* You shall not disclose such confidential information and shall use it 
* only in accordance with the terms of the license agreement you entered into with SK C&C. 
*/
package nexcore.scheduler.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class DailyRollingFileName {

	private static enum PERIOD_TYPE {
		MINUTE, HOUR, HALF_DAY, DAY, MONTH, YEAR
	};
	
	private String fileNamePattern; // yyyyMMdd/'xxxx-'yyyyMMdd'.log'
	
	private PERIOD_TYPE m_period = PERIOD_TYPE.DAY; // 파일 생성 주기.
//	private File m_path; // 로그 파일이 저장될 디렉토리의 절대경로. 내부에서 사용하는 값.
	private Calendar m_calendar; // 시각을 다루기 위해 사용하는 {@link Calendar} 객체.
	private long m_tomorrow = 0L; // 다음 파일 생성을 할 시각 (로컬 시스템 타임으로 1970/01/01 부터
	private String m_filename;

	public DailyRollingFileName(String fileNamePattern) {
		setFileNamePattern(fileNamePattern);
		this.m_calendar = Calendar.getInstance();
	}
	
	/**
	 * @return the fileNamePattern
	 */
	public String getFileNamePattern() {
		return this.fileNamePattern;
	}
	
	public String getFileName() {
		return m_filename;
	}

	/**
	 * @param fileNamePattern
	 *            the fileNamePattern to set
	 */
	protected void setFileNamePattern(String fileNamePattern) {
		this.fileNamePattern = fileNamePattern;

		String stripedPattern = fileNamePattern.replaceAll("'[^']*'", "");
		if (stripedPattern.indexOf("m") > -1) {
			m_period = PERIOD_TYPE.MINUTE;
			return;
		} else if (stripedPattern.indexOf("H") > -1) {
			m_period = PERIOD_TYPE.HOUR;
			return;
		} else if (stripedPattern.indexOf("a") > -1
				&& stripedPattern.indexOf("h") > -1) {
			m_period = PERIOD_TYPE.HALF_DAY;
			return;
		} else if (stripedPattern.indexOf("d") > -1) {
			m_period = PERIOD_TYPE.DAY;
			return;
		} else if (stripedPattern.indexOf("M") > -1) {
			m_period = PERIOD_TYPE.MONTH;
			return;
		} else {
			m_period = PERIOD_TYPE.YEAR;
			return;
		}
	}
	
	public boolean rollover(long timeMillis) {
		long n = timeMillis;
		if (n >= m_tomorrow) {
			// 2020.05.07 rolling 대상인 시점 로그파일 변경 처리를 synchronized 하도록 변경.
			synchronized (m_calendar) {
				if (n >= m_tomorrow) {
					m_calendar.setTime(new Date(n)); // set Calendar to current time
					String filename = getRollingFileName(m_calendar); // convert to string "yyyy-mm-dd"
					tomorrow(m_calendar, m_period); // set the Calendar to the start of tomorrow
					m_tomorrow = m_calendar.getTime().getTime(); // time in milliseconds when tomorrow starts
					m_filename = filename;
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 주어진 Calendar 객체를 미리 정해놓은 'FileNamePattern'에 맞게 포매팅한다.
	 * 
	 * @param calendar
	 *            a Calendar containing the date to format.
	 * 
	 * @return a String in the form "yyyy-yy-dd".
	 */
	protected String getRollingFileName(Calendar calendar) {
		DateFormat df = new SimpleDateFormat(fileNamePattern);
		String dateString = df.format(calendar.getTime());
		return dateString;
	}
	
	/**
	 * 파일이름을 변경할 "다음 시점"(="tomorrow")을 정한다. "다음 시점"은 periodType 값에 따라 정해진다.
	 * 
	 * "다음 시점"은 1분 후, 1시간 후, 1일 후 등이 될 것인데, Usually "THE NEXT TIME" will be one
	 * minute later, or one hour, or so, but at the first time it would not be
	 * like those. so we should reset the smaller ones. (that is, for the
	 * "NEXT HOUR" we should reset MINUTE)
	 * 
	 * @param calendar
	 * @param periodType
	 */
	protected void tomorrow(Calendar calendar, PERIOD_TYPE periodType) {
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);

		/*
		 * "다음 시점"은 일반적으로는 "지금으로부터 1분 후", "지금으로부터 1일 후" 등이 될 것인데, 최초 로깅 시점은 다를 수
		 * 있다. 이를테면 최초 로깅 시점이 17시 46분이고 "다음 시점"은 18시 00분이다. 이를 맞추기 위해 "더 작은"
		 * 단위들은 리셋해 준다.
		 */
		if (periodType == PERIOD_TYPE.DAY) {
			day++;
			hour = 0;
			min = 0;
		} else if (periodType == PERIOD_TYPE.HALF_DAY) {
			hour += 12;
			min = 0;
		} else if (periodType == PERIOD_TYPE.HOUR) {
			hour++;
			min = 0;
		} else if (periodType == PERIOD_TYPE.MINUTE) {
			min++;
		} else if (periodType == PERIOD_TYPE.MONTH) {
			month++;
			day = 1;
			hour = 0;
			min = 0;
		} else if (periodType == PERIOD_TYPE.YEAR) {
			year++;
			month = 0;
			day = 1;
			hour = 0;
			min = 0;
		} else {
			day++;
			hour = 0;
			min = 0;
		}

		// calendar.clear();
		calendar.set(year, month, day, hour, min, 0);
	}
	
}
