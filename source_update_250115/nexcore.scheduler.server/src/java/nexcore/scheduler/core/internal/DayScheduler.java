package nexcore.scheduler.core.internal;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import nexcore.scheduler.core.IDayScheduler;
import nexcore.scheduler.core.IScheduleCalendar;
import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 해당 Job 이 오늘 activate 대상인지 체크하는 로직 </li>
 * <li>작성일 : 2010. 5. 12.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
/*
 * <ul>
 * <li>BIZ. Group : BATCH</li>
 * <li>Sub Group : SCHEDULE</li>
 * <li>Date : 2009. 07. 09</li>
 * <li>Author : Jeong Ho Cheol</li>
 * <li>Description : 작업일인지 여부를 계산하는 클래스. </li>
 * </ul>
 * 
 * @author NEXCORE Part.
 */
public class DayScheduler implements IDayScheduler {
	// java.util.Calendar 가 아니고, 영업일/개장일/휴일 등등 "달력" 기능을 의미한다.
	private IScheduleCalendar scheduleCalendar;

	public IScheduleCalendar getScheduleCalendar() {
		return scheduleCalendar;
	}

	public void setScheduleCalendar(IScheduleCalendar calendar) {
		this.scheduleCalendar = calendar;
	}

	public void init() {
		Util.logServerInitConsole("DayScheduler", "("+scheduleCalendar.listCalendarIdNames()+")");
	}
	public void destroy() {
	}

	/**
	 * 당일이 스케쥴 day인지 체크하는 메인 로직. 대체일 적용되지 않은 체크.
	 * @param jobdef
	 * @param today
	 * @return
	 */
	private boolean _isScheduledDay(JobDefinition jobdef, Calendar today) {
		boolean scheduleYn = false;
		
		// ■■■■■■ 1. 익전일처리. 스케쥴링 판단에 넣을 당일값 구함.
		Calendar theDay = getTheDay(jobdef, today);
		
		// ■■■■■■ 2. 스케쥴링 방식에 따라 체크로직 분기
		if (JobDefinition.SCHEDULE_TYPE_FIXED.equalsIgnoreCase(jobdef.getScheduleType())) {
			// YYYYMMDD 기반의 고정일자 방식. theDay 가 고정일자 집합의 부분인지 체크함.
			String theDayYYYYMMDD = String.valueOf(CalendarUtil.convCalendarToYYYYMMDD(theDay));
			
			scheduleYn = false;
			if (jobdef.getFixedDayList().contains(theDayYYYYMMDD)) { // wild 카드 표현식 중에 하나라도 만족시키면 중단하고 true 임.
				scheduleYn = true;
			}else {
				// 설정된 fixedDayList 의 한 entry 를 하나의 정규 표현식으로 보고 체크함. 예) 201201../2012..(05|06)
				
				// 정규표현식 설정시 일자 부분에 LD라고 표현하면 그달의 말일을 의미함
				boolean isLastDayOfMonth = 
					theDay.get(Calendar.DAY_OF_MONTH) == theDay.getActualMaximum(Calendar.DAY_OF_MONTH); // 말일인가?
				
				for (Object _fixedDay : jobdef.getFixedDayList()) {
					String fixedDay = (String)_fixedDay;
					if (theDayYYYYMMDD.matches(fixedDay)) {
						scheduleYn = true;
						break;
					}else{
						if (isLastDayOfMonth) { // theDay 가 말일(LD)일 경우, 말일이 fixedday 에 표현되어있는지 체크. 
							if ((theDayYYYYMMDD.substring(0,6)+"LD").matches(fixedDay)) {
								scheduleYn = true;
								break;
							}
						}
					}
				}

// 아래 와일드카드 방식은 위의 정규표현식 방식으로 변경됨. 2012-01-18
				// YYYYMMDD 중에 * 를 와일드카드로 하여 fixed 를 표현할 수 있게함. *0101/*0320 ==> 매년 1월1일 and 매년3월 20일
				// *MMDD, YYYY*DD, YYYYMM*, **DD, YYYY**, *MM*, *** 의 7가지 와일드카드 표현식을 포함함
//				List<String> wildcardedTheDayList = new ArrayList<String>(7);
//				wildcardedTheDayList.add("*"+theDayYYYYMMDD.substring(4)); 				// *MMDD
//				wildcardedTheDayList.add(theDayYYYYMMDD.substring(0,3)+"*"+theDayYYYYMMDD.substring(6)); // YYYY*DD
//				wildcardedTheDayList.add(theDayYYYYMMDD.substring(0,6)+"*"); 			// YYYYMM*
//				wildcardedTheDayList.add("**"+theDayYYYYMMDD.substring(6)); 			// **DD
//				wildcardedTheDayList.add(theDayYYYYMMDD.substring(0,4)+"**"); 			// YYYY**
//				wildcardedTheDayList.add("*"+theDayYYYYMMDD.substring(4,6)+"*");		// *MM*
//				wildcardedTheDayList.add("***"); 										// ***
//	
//				for (String wildcardedTheDay : wildcardedTheDayList) {
//					if (jobdef.getFixedDayList().contains(wildcardedTheDay)) { // wild 카드 표현식 중에 하나라도 만족시키면 중단하고 true 임.
//						scheduleYn = true;
//						break;
//					}
//				}
			}
		}else if (JobDefinition.SCHEDULE_TYPE_EXPRESSION.equalsIgnoreCase(jobdef.getScheduleType())) {
			// 월,일,달력,요일 기반의 expression 표현 방식
			// ■■■■■■ 1. 월 체크. 달력(calendar)을 지정하는 방식이라고 해도 해당월이 지정되어있지 않으면 안돈다.  
			if (!isScheduledMonth(jobdef, theDay)) {
				scheduleYn = false;
			}else {
				if (JobDefinition.WEEKDAY_MONTHDAY_TYPE_OR.equalsIgnoreCase(jobdef.getWeekdayMonthdayType())) {
					// ■■■■■■ (2. 일 체크. calendar 도 여기서 체크)  OR  (3. 요일 체크)
					// 일 스케쥴과 요일 스케쥴중 하나라도 만족하면 실행한다.
					if (isScheduledDayOfMonth(jobdef, theDay) || isScheduledDayOfWeek(jobdef, theDay)) {
						scheduleYn = true;
					}else {
						scheduleYn = false;
					}
				}else if(JobDefinition.WEEKDAY_MONTHDAY_TYPE_AND.equalsIgnoreCase(jobdef.getWeekdayMonthdayType())) {
					// ■■■■■■ (2. 일 체크. calendar 도 여기서 체크)  AND  (3. 요일 체크)
					// 일 스케쥴과 요일 스케쥴이 모두 만족해야 실행한다.
					if (isScheduledDayOfMonth(jobdef, theDay) && isScheduledDayOfWeek(jobdef, theDay)) {
						scheduleYn = true;
					}else {
						scheduleYn = false;
					}
				}else {
					throw new SchedulerException("main.daysch.wrong.weekday_monthday.type", jobdef.getWeekdayMonthdayType()); // {0} 는 잘못된 일자 요일 연산값입니다
				}
			}
		}else {
			throw new SchedulerException("main.daysch.wrong.schedule.type", jobdef.getScheduleType()); // {0} 는 잘못된 스케줄 방식입니다.
		}
		
		// 다시한번 점검한다. SK증권에서는 금요무처리를 아래 메소드에서 체크한다
		return afterDecision(jobdef, today, theDay, scheduleCalendar, scheduleYn);
	}
	
	/**
	 * 대체일 체크. 오늘이 schedule day가 아니라도, 다른날의 대체일로 오늘이 지정될 수 있다. 처음 호출시는 2차 대체일을 먼저 풀고. 제귀호출될때 1차 대체일을 적용한다.
	 * @param jobdef
	 * @param theday
	 * @param shift
	 * @param shiftAgain
	 * 
	 */
	private boolean isShiftedScheduledDay(JobDefinition jobdef, Calendar theday, ShiftExp shift, ShiftExp shiftAgain) {
		// 대체일 문법은 confirm calendar id, shift calendar id, shift days 형태의 3개의 정수값으로 구성됨
		// 3,1,-1  ==> 당일이 휴일(3)이면 영업일(1) 기준으로 하루 빼기   (-1)
		// 3,1,1   ==> 당일이 휴일(3)이면 영업일(1) 기준으로 하루 더하기 ( 1)

//		System.out.println(new Date(theday.getTime().getTime())+"  -- "+shift+"  -- "+shiftAgain);
		if (shift == null || !shift.isValid()) { // shift 가 없으면 그날을 가지고 scheduledday 체크한다.
			return _isScheduledDay(jobdef, theday);
		}
		
		if (!scheduleCalendar.contains(shift.ifCalId, theday) && (shiftAgain == null || !shiftAgain.isValid())) { 
			// shift 할 필요가 없는 경우이면 여기서 scheduledDay 체크함. shiftAgain 이 있으면 여기서 체크하면 안되고 다음번 재귀호출에서 체크한다  
			if( _isScheduledDay(jobdef, theday) ) {
				return true;
			} else {
				// 밑으로 내려가서 theday 가 다른날의 shifted day 인지 체크.
			}
		}
		
		// ------------- 메인 로직 --------------
		if (scheduleCalendar.contains(shift.shiftCalId, theday)) {
			// 일단 오늘이 shift calendar day 이어야 해야지 체크라도 해볼 필요가 있다.
			if (shift.shiftDays < 0) { // 전일로 shift. "3,1,-1" 인 경우 
				// today + 1 일부터 하루씩 증가시키면서 앞으로의 날 중에서 대체일로 오늘(theday)을 지정한 것이 있는지 확인.
				boolean todayPassFlag = false; // 아래에서 checkday를 증가시킬때, 정지시키기 위한 flag로 사용.
				Calendar checkDay = CalendarUtil.getNextDay(theday); // 내일부터 시작해서...
				for (int i=0; i<100; i++) { // 무한루프 빠지는 것을 방지하기 위해 최대 100일 정도만 검사한다.
					// 1. shift 한 날이 오늘인지 확인해서 오늘이라면
					// 2. checkday 가 휴일인지 확인 (confirm calendar id 에 속한 날인지 확인) 하고 
					// 3. confirm calendar id 에 속한날이 맞다면, checkDay 가 schedule day 인지 확인해서, 맞다면 true 리턴
					if (CalendarUtil.equalsByDay(theday, scheduleCalendar.getNextDayOfCalendar(shift.shiftCalId, checkDay, shift.shiftDays))) { // 1. 
						todayPassFlag = true;
						if (scheduleCalendar.contains(shift.ifCalId, checkDay)) {  // 2.
							if (isShiftedScheduledDay(jobdef, checkDay, shiftAgain, null)) { // 1차 대체일로 다시 체크한다.
								return true;
							}
						}
					}else {
						if (todayPassFlag) { // shift 한 날이 이미 theday 를 지나는 경우이므로 여기서 멈추게 하라. 
							return false;
						}
					}
					checkDay = CalendarUtil.getNextDay(checkDay);
				}
			}else if (shift.shiftDays > 0) { // 익일로 shift. "3,1,1" 인 경우
				// today -1 일부터 하루씩 감소시키면서 이전날 중에서 대체일로 오늘을 지정한것이 있는지 확인.
				boolean todayPassFlag = false; // 아래에서 checkday를 감소시킬때, 정지시키기 위한 flag로 사용.
				Calendar checkDay = CalendarUtil.getPrevDay(theday); // 어제부터 시작해서...
				for (int i=0; i<100; i++) { // 무한루프 빠지는 것을 방지하기 위해 최대 100일 정도만 검사한다.
					// 1. shift 한 날이 오늘인지 확인해서 오늘이라면
					// 2. checkday 가 휴일인지 확인 (confirm calendar id 에 속한 날인지 확인) 하고 
					// 3. confirm calendar id 에 속한날이 맞다면, checkDay 가 schedule day 인지 확인해서, 맞다면 true 리턴
					if (CalendarUtil.equalsByDay(theday, scheduleCalendar.getNextDayOfCalendar(shift.shiftCalId, checkDay, shift.shiftDays))) { // 1. 
						todayPassFlag = true;
						if (scheduleCalendar.contains(shift.ifCalId, checkDay)) {  // 2.
							if (isShiftedScheduledDay(jobdef, checkDay, shiftAgain, null)) {  // 1차 대체일로 다시 체크한다.
								return true;
							}
						}
					}else {
						if (todayPassFlag) { // shift 한 날이 이미 theday 를 지나는 경우이므로 여기서 멈추게 하라. 
							return false;
						}
					}
					checkDay = CalendarUtil.getPrevDay(checkDay);
				}
			}else {
				return false; // shift days 가 0 이면 shift 하지 않고 "무처리"라고 인식함
			}
		}
		return false;
	}
	
	/**
	 * 당일이 실행되어야하는날인지 체크
	 * 
	 * @param job
	 *            JobDefinition
	 * @return true if today is the scheduled day; false otherwise
	 */
	public boolean isScheduledDay(JobDefinition jobdef) {
		return isScheduledDay(jobdef, Calendar.getInstance());
	}

	/**
	 * 당일이 실행되어야하는날인지 체크
	 */
	public boolean isScheduledDay(JobDefinition jobdef, Calendar today) {
		ShiftExp shift1 = new ShiftExp(jobdef.getShiftExp());
		ShiftExp shift2 = new ShiftExp(jobdef.getShiftExp2());
		
		boolean result;
		if (shift2.isValid()) {
			result = isShiftedScheduledDay(jobdef, today, shift2, shift1); // 2차 대체일을 먼저 적용한후 1차 대체일을 적용함.
		}else {
			result = isShiftedScheduledDay(jobdef, today, shift1, null); // 2차 대체일이 없으므로 1차 대체일을 적용함.
		}
		
		if (jobdef.isReverse()) {
			result = !result;
		}
		
		return result;
	}
	

	/**
	 * 프로젝트에서 customize 하기 위해 스케쥴링 로직을 추가할때는 이 메소드를 상속 받아 구현하도록 한다.
	 * @param batchsc        : this job
	 * @param today          : 스케줄링 체크하는 당일. 
	 * @param theDay         : 익전일처리를 반영한 당일
	 * @param calendar       : 달력
	 * @param currentDecisition : 위 isScheduledDay() 의 결과.
	 * @return
	 */
	protected boolean afterDecision(JobDefinition jobdef, Calendar today, Calendar theDay, IScheduleCalendar calendar, boolean currentDecisition) {
		return currentDecisition;
	}

	// 익전일처리. 익전일처리 표현식을 보고 스케쥴해야할 당일을 구함.
	private Calendar getTheDay(JobDefinition jobdef, Calendar date) {
		/* ■■■■■■ 1. 익전일처리.
		 *  익전일 표현식으로 스케쥴 체크 당일 값을 구해낸다.
		 *  "A1" 으로 설정시 theDate = date - 1. ==> 어제가 스케쥴 날인지 판단하여 true 면 오늘 실행.
		 *  "A2" 으로 설정시 theDate = date - 2. ==> 그제가 스케쥴 날인지 판단하여 true 면 오늘 실행.
		 *  "B1" 으로 설정시 theDate = date + 1. ==> 내일이 스케쥴 날인지 판단하여 true 면 오늘 실행.
		 *  "B2" 으로 설정시 theDate = date + 2. ==> 모래가 스케쥴 날인지 판단하여 true 면 오늘 실행.
		 *  ...
		 */
		Calendar theDay = date;
		if (jobdef.getBeforeAfterExp() != null && jobdef.getBeforeAfterExp().trim().length() > 1) {
			if (jobdef.getBeforeAfterExp().charAt(0) == 'A') { // 익일처리. 스케쥴 판단일 theDate 는 -1 한다. ( -n 한 날이 스케쥴 날이면 오늘 돌아간다. )
				try {
					int afterDay = Integer.parseInt(jobdef.getBeforeAfterExp().substring(1));
					theDay = (Calendar)date.clone();
					theDay.add(Calendar.DATE, -1 * afterDay);
				}catch(NumberFormatException e) {
					throw new SchedulerException("main.daysch.wrong.before_after.exp", jobdef.getBeforeAfterExp()); // {0}는 잘못된 익전일 표현식입니다.
				}
			}else if (jobdef.getBeforeAfterExp().charAt(0) == 'B') { // 전일처리. 스케쥴 판단일 theDate 는 +1 한다. ( +n 한 날이 스케쥴 날이면 오늘 돌아간다. )
				try {
					int beforeDay = Integer.parseInt(jobdef.getBeforeAfterExp().substring(1));
					theDay = (Calendar)date.clone();
					theDay.add(Calendar.DATE, beforeDay);
				}catch(NumberFormatException e) {
					throw new SchedulerException("main.daysch.wrong.before_after.exp", jobdef.getBeforeAfterExp()); // {0}는 잘못된 익전일 표현식입니다.
				}
			}else {
				throw new SchedulerException("main.daysch.wrong.before_after.exp", jobdef.getBeforeAfterExp()); // {0}는 잘못된 익전일 표현식입니다.
			}
		}
		return theDay;
	}

	// 월체크. ALL 로 지정되어있으면 무조건 true, 그외는 해당월일때만 true
	private boolean isScheduledMonth(JobDefinition jobdef, Calendar theDay) {
		if (jobdef.getMonthList().contains(JobDefinition.ALL) || 
			jobdef.getMonthList().contains(String.valueOf(theDay.get(Calendar.MONTH)+1))) {
			return true;
		}else {
			return false;
		}
	}
	
	// 일체크. 숫자 지정 방식과 calendar 지정 방식으로 구분됨.
	private boolean isScheduledDayOfMonth(JobDefinition jobdef, Calendar theDay) {
		int theDayOfMonth        = theDay.get(Calendar.DAY_OF_MONTH);
		String theDateString     = String.valueOf(theDayOfMonth);
		boolean isLastDayOfMonth = theDayOfMonth == theDay.getActualMaximum(Calendar.DAY_OF_MONTH); // 말일인가?
		
		if (JobDefinition.DAY_SCHEDULE_TYPE_NUMBER.equalsIgnoreCase(jobdef.getDayOfMonthScheduleType())) {
			// 숫자 지정방식.
			if (jobdef.getDayListInMonth().contains(theDateString) ||   // 1,2,3,4,10,22,23  식의 숫자 지정
				jobdef.getDayListInMonth().contains(JobDefinition.ALL) ||  // ALL 지정
				(isLastDayOfMonth && jobdef.getDayListInMonth().contains(JobDefinition.LAST_DAY_Of_MONTH))) {  // LD 포함된 경우 월말일 여부 
					return true;
			}else {
				return false;
			}
		}else if (JobDefinition.DAY_SCHEDULE_TYPE_CALENDAR.equalsIgnoreCase(jobdef.getDayOfMonthScheduleType())) {
			// theDay 가 calendar 에 포함되어있지 않으면 false.
			if (!scheduleCalendar.contains(jobdef.getCalendarId(), theDay)) {
				return false;
			}

			// Calendar 방식.
			Integer theDayYyyymmdd   = new Integer(CalendarUtil.convCalendarToYYYYMMDD(theDay));

			// yyyymmdd 형태의 Integer 리스트.
			List yyyymmddList = scheduleCalendar.getMonthlyYyyymmddList(jobdef.getCalendarId(), theDay.get(Calendar.YEAR), theDay.get(Calendar.MONTH)+1);

			List calExpList = jobdef.getCalendarExpList();
			if (calExpList.size() == 0) {
				// calendar 표현식이 없는 경우는 theDay 가 calendar 에 포함일이므로 true
				return true;
			}else {
				// calendar 표현식이 있는 경우.
				// theDay 가 B 표현식으로 언제인지?, E 표현식으로 언제인지 계산.
				String theDayBeginExp = "B"+(yyyymmddList.indexOf(theDayYyyymmdd)+1); 
				String theDayEndExp   = "E"+(yyyymmddList.size() - yyyymmddList.indexOf(theDayYyyymmdd));
				// 표현식이 -Bn 또는 -En 인지 검사, Bn 들과 -Bn 은 같이 사용할 수 없다.
				// 첫번째 식이 -로 시작한다면 나머지 것들도 -로 시작해야한다.
				// 예) [B1,B2,B3,E1] => OK , [-B1,-B2,-E1] => OK, [B1,B2,-E1] => ERROR
				if (((String)calExpList.get(0)).charAt(0) == '-') {
					// - 표현식 모드 (exclusive). 오늘이 calExpList 에 포함되어있으면 false.
					if (calExpList.contains("-"+theDayBeginExp) || calExpList.contains("-"+theDayEndExp)) {
						return false;
					}else {
						return true;
					}
				}else {
					// + 표현식 모드 (inclusive). 오늘이 calExpList 에 포함되어있으면 true. 
					if (calExpList.contains(theDayBeginExp) || calExpList.contains(theDayEndExp)) {
						return true;
					}else {
						return false;
					}
				}
			}
		}else {
			throw new SchedulerException("main.daysch.wrong.day_schedule.type", jobdef.getDayOfMonthScheduleType()); // {0}는 잘못된 일 스케줄 타입입니다 (NUMBER, CALENDAR 중 하나)
		}
	}
	
	private boolean isScheduledDayOfWeek(JobDefinition jobdef, Calendar theDay) {
		// W째주D요일. 예) 2009/07/13  => W3D2
		String theDateDayString1 = "W"+theDay.get(Calendar.WEEK_OF_MONTH)+"D"+theDay.get(Calendar.DAY_OF_WEEK);
		
		// 번째D요일.  예) 2009/07/13  => 2D2
		String theDateDayString2 = theDay.get(Calendar.DAY_OF_WEEK_IN_MONTH)+"D"+theDay.get(Calendar.DAY_OF_WEEK);

		// 매주D요일.  예) 2009/07/13  => AD2
		String theDateDayString3 = "WAD"+theDay.get(Calendar.DAY_OF_WEEK);
		String theDateDayString4 = "AD"+theDay.get(Calendar.DAY_OF_WEEK);

		// 오늘이 위 두 표현방식으로 W3D2, 2D2 (예) 로 표현되는데, -- WAD2, AD2 도 가능 (2010-10-19 추가)
		// 이 Job 은 이 요일에 수행되도록 스케줄 등록되었는지 체크함.
		if (jobdef.getDayListOfWeek().contains(theDateDayString1) || 
			jobdef.getDayListOfWeek().contains(theDateDayString2) || 
			jobdef.getDayListOfWeek().contains(theDateDayString3) || 
			jobdef.getDayListOfWeek().contains(theDateDayString4)) {
			return true;
		}else {
			// 마지막주 체크로직도 돌린다.
			
			// WLD3 : 마지막주 화요일, LD3:마지막 화요일
			// 마지막주 의 D요일.
			String lastWeekString1 = (theDay.getActualMaximum(Calendar.WEEK_OF_MONTH) == theDay.get(Calendar.WEEK_OF_MONTH)) 
				? "WLD" + theDay.get(Calendar.DAY_OF_WEEK) : "NOTMATCH";
			
			// 다음주 같은 요일이 아직도 이번달이면 오늘은 마지막 ~요일이 아니다.
			int thisMonth = theDay.get(Calendar.MONTH);
			theDay.add(Calendar.DATE, 7);  // 다음 주 같은 요일
			int nextWeekMonth = theDay.get(Calendar.MONTH);
			theDay.add(Calendar.DATE, -7); // 다시 원래대로

			// 마지막 n 요일인가?
			String lastWeekString2 = (thisMonth != nextWeekMonth) ? "LD" + theDay.get(Calendar.DAY_OF_WEEK) : "NOTMATCH";

			if (jobdef.getDayListOfWeek().contains(lastWeekString1) || 
				jobdef.getDayListOfWeek().contains(lastWeekString2)) {
				return true;
			}else {
				return false;
			}
		}
	}
	
	/**
	 * 월 기준으로 실행되어야할 일자를 리턴함.
	 * @param jobdef
	 * @param yyyymm
	 * @return
	 */
	public List getScheduledDayList(JobDefinition jobdef, String yyyymm) {
		Calendar cal = CalendarUtil.convYYYYMMDDToCalendar(Integer.parseInt(yyyymm+"01"));
		int month1 = cal.get(Calendar.MONTH); // 시작일의 월.
		List result = new LinkedList();
		while(true) {
			if (isScheduledDay(jobdef, cal)) {
				result.add(String.valueOf(CalendarUtil.convCalendarToYYYYMMDD(cal)));
			}
			
			cal.add(Calendar.DATE, 1);
			
			if (cal.get(Calendar.MONTH) != month1) { // 내일이 다음달이면 끝.  
				break;
			}
		}
		return result;
	}

	
	class ShiftExp {
		String   ifCalId;
		String   shiftCalId;
		int      shiftDays;

		public ShiftExp(String shiftExpString) {
			try {
				String[] shiftExp = shiftExpString.split(",");
				ifCalId	        = shiftExp[0];
				shiftCalId		= shiftExp[1];
				shiftDays		= Integer.parseInt(shiftExp[2]);
			}catch(Exception e) {
			}
		}
		
		boolean isValid() {
			return ifCalId != null && shiftCalId != null && shiftDays != 0;
		}
		
		public String toString() {
			return ifCalId+","+shiftCalId+","+shiftDays;
		}
	}
}
