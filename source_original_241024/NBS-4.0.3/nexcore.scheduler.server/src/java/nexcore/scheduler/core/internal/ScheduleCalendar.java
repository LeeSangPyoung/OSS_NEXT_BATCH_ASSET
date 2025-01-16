/**
 * 
 */
package nexcore.scheduler.core.internal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;

import nexcore.scheduler.core.IScheduleCalendar;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;

/**
 * <ul>
 * <li>BIZ. Group  : </li>
 * <li>Sub  Group  : </li>
 * <li>Date        : 2009. 7. 13.</li>
 * <li>Author      : Jeong Ho Cheol</li>
 * <li>Description : 스케쥴링 calendar 의 구현체. 일반적인 기능은 여기에 구현되어있고 
 *                   각 프로젝트 특성에 한정된 기능들은 이 클래스를 상속받아 별도로 구현해야함</li>
 * </ul>
 *
 * @author NEXCORE Tech Part.
 */
/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 기본 Calendar 구성 클래스. 필요시 이 클래스를 상속받아 customize 함. </li>
 * <li>작성일 : 2010. 5. 11.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class ScheduleCalendar implements IScheduleCalendar {
	private String              sourceType;
	
	/* DB 방식일 경우 사용됨 */
	private DataSource          dataSource;
	private Map<String, String> calendarSql;
	
	/* File 방식일 경우 사용됨 */
	private Map<String, String> files;
	private int                 loadRangeYear = 3; // 메모리에 로드할 calendar 의 범위 (년단위). 기본값 3 = 오늘 기준 3년 (작년,올해,내년) 
	
	/* Map<String, CalendarElement>. calendar 전체의 YYYYMMDD 포함 */
	private Map<String, CalendarElement> calendars = new HashMap();
	
	private Log log;

	/* calendar 가 여러개 있을때 각 calendar 를 나타내는 구조체 */
	class CalendarElement {
		private String 					calendarId;
		private String 					calendarName;
		private List<Integer> 			yyyymmddList;
		private Map<String, Integer>	monthIndexMap; /* <String(yyyymm), Integer> */
		
		CalendarElement (String calendarId, String calendarName) {
			this.calendarId		= calendarId;
			this.calendarName	= calendarName;
			this.yyyymmddList	= new ArrayList();
			this.monthIndexMap	= new HashMap();
		}
		
		public String getCalendarId() {
			return calendarId;
		}
		public String getCalendarName() {
			return calendarName;
		}
		public List<Integer> getYyyymmddList() {
			return yyyymmddList;
		}
		private synchronized void setYyyymmddList(List<Integer> list) {
			Map newIndexMap = rebuildIndex(list);
			
			yyyymmddList  = list;
			monthIndexMap = newIndexMap;
		}
		public Map getMonthIndexMap() {
			return monthIndexMap;
		}
		/**
		 * List of yyyymmdd 중에서 해당월의 첫일의 인덱스를 찾는다. 
		 * @param yyyymm
		 * @return idx or -1 if not exist.
		 */
		public int getMonthIndex(String yyyymm) {
			Integer idx = (Integer)monthIndexMap.get(yyyymm);
			return idx == null ? -1 : idx.intValue();
		}
		// yyyymmddList 가 변경되고 난 후에 monthIndex를 rebuild 한다.
		private Map rebuildIndex(List yyyymmddList) {
			/*
			 *  yyyymmddList 를 역순으로 travese하면서 YYYYMM 부분만 잘라서 Map에 넣는다. 
			 *  이렇게 하면 자동으로 YYYYMM 의 맨 첫날의 index 가 Map 에 최종으로 남기 때문에 월(MM)의 변경을 체크할 필요가 없다.
			 *  자주 불리는 작업이 아니므로 속도에 신경쓰지 말고 이렇게 한다.
			 */

			// rebuild 중에 혹시라도 List 가 변경되면 안되므로
			ArrayList list = new ArrayList(yyyymmddList);
			Map indexMap = new HashMap();
			for (int i=list.size()-1; i>=0; i--) {
				Integer yyyymmdd = (Integer)list.get(i);
				int yyyymm = yyyymmdd.intValue() / 100;
				indexMap.put(String.valueOf(yyyymm), new Integer(i));
			}
			return indexMap;
		}
	}

	public ScheduleCalendar() {
	}

	public void init() {
		log = LogManager.getSchedulerLog();
		calendars = new HashMap();
		loadCalendarData();
	}
	
	public void destroy() {
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Map<String, String> getCalendarSql() {
		return calendarSql;
	}

	public void setCalendarSql(Map<String, String> calendarSql) {
		this.calendarSql = calendarSql;
	}

	public Map<String, String> getFiles() {
		return files;
	}

	public void setFiles(Map<String, String> files) {
		this.files = files;
	}

	public int getLoadRangeYear() {
		return loadRangeYear;
	}

	public void setLoadRangeYear(int loadRangeYear) {
		this.loadRangeYear = loadRangeYear;
	}

	/////////////////////////////////////////////////////////////

	private CalendarElement getCalendarElement(String calendarId) {
		CalendarElement ce = (CalendarElement)calendars.get(calendarId);
		if (ce == null) {
			throw new SchedulerException("main.calendar.notfound", calendarId); // {0} 은 등록되지 않은 Calendar 입니다
		}
		return ce;
	}
	
	/**
	 * yyyymm 월의 List of yyyymmdd  값을 리턴받는다. 
	 */
	public List getMonthlyYyyymmddList(String calendarId, String yyyymm) {
		CalendarElement ce = getCalendarElement(calendarId);
		int beginIdx = ce.getMonthIndex(yyyymm);      // 당월 첫일 idx.
		int endIdx   = ce.getYyyymmddList().size();   // 익월 첫일 idx. 기본값으로는 맨 마지막 idx. 
		if (beginIdx == -1) {
			return Collections.EMPTY_LIST;
		}

		// 당월 yyyymm int 값.
		int yyyymmInt  = Integer.parseInt(yyyymm);
		for (int i=beginIdx; i<ce.getYyyymmddList().size(); i++) {
			if (ce.getYyyymmddList().get(i) / 100 != yyyymmInt) { // 월이 바뀌었군.
				endIdx = i;
				break;
			}
		}
		
		// 혹시 이 List 를 사용하다가 변경을 할려고 할지도 모르니..안전하게 그냥 copy 해서 리턴한다.
		return new ArrayList(ce.getYyyymmddList().subList(beginIdx, endIdx));
	}

	public List getMonthlyYyyymmddList(String calendarId, int year, int month) {
		if (month<10) {
			return getMonthlyYyyymmddList(calendarId, String.valueOf(year)+"0"+String.valueOf(month));
		}else {
			return getMonthlyYyyymmddList(calendarId, String.valueOf(year)+String.valueOf(month));
		}
	}

	public List getMonthlyYyyymmddList(String calendarId, Calendar cal) {
		return getMonthlyYyyymmddList(calendarId, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1);
	}

	public List getYyyymmddList(String calendarId) {
		return getCalendarElement(calendarId).getYyyymmddList();
	}
	
	// calendar list 에서 월별로 월초일의 index를 따로 관리함. 그 index를 리턴함. 
	public int getMonthIndex(String calendarId, String yyyymm) {
		return getCalendarElement(calendarId).getMonthIndex(yyyymm);
	}

	// 해당일이 calendar 에 포함되어있는지?
	public boolean contains(String calendarId, Calendar date) {
		return getCalendarElement(calendarId).getYyyymmddList().contains(
			new Integer(CalendarUtil.convCalendarToYYYYMMDD(date)));
	}

	// subclass 에서 이 메소드를 이용하여 월별 달력 정보(days set)을 calendars 에 저장함
	protected void setCalendar(String calendarId, String calendarName, List<Integer> yyyymmddList) {
		// day 를 String 으로 하지 않고 Integer로 한 이유는 String 으로 할 경우 "01", "1" 은 다른 값이 되므로 
		// compare 할때마다 "0" 처리를 따로 해야한다. 이런 에러를 원천 차단하기 위해 int 로 한다.
		// int 로 할 경우 Collection 클래스들을 다루는데 불편함이 많기 때문에 Integer 로 한다.

		// #################################################
		CalendarElement ce = (CalendarElement)calendars.get(calendarId);
		if (ce == null) {
			ce = new CalendarElement(calendarId, calendarName);
			synchronized(calendars) {
				calendars.put(calendarId, ce);
			}
		}
		// 혹시 모를 중복 데이타 제거와 정렬을 목적으로 TreeSet 으로 한번 감싼후 list 에 넣는다.
		ce.setYyyymmddList(new ArrayList(new TreeSet(yyyymmddList)));

		// yyyymmddList 에는 [20010102,20010103,20010104,20010105,20010106,....] 등의 형태로 들어있다.
		
		try {
			log.info(MSG.get("main.calendar.loaded", calendarId, calendarName, yyyymmddList.get(0), yyyymmddList.get(yyyymmddList.size()-1))); // Calenadar [{0}]{1} 가 로드 됐습니다. ({2}~{3})
		}catch(IndexOutOfBoundsException e) {
			log.warn("Calendar ["+calendarId+":"+calendarName+"] loaded 0 days", e);
			log.info(MSG.get("main.calendar.loaded", calendarId, calendarName)); // Calenadar [{0}]{1} 가 로드 됐습니다. ({2}~{3})
		}
	}

	// 달력 기반으로 익일 구하기.
	// 익영업일 구하기. 익2영업일 구하기. 익개장일 구하기. 익2개장일 구하기 등등
	public Calendar getNextDayOfCalendar(String calendarId, Calendar today, int n) {
		if (n == 0) { // 양수이거나 음수이어야한다.
			return today; // 0 이면 그냥 today를 리턴한다.
		}

		// 오늘 YYYYMMDD
		int todayYMD      = CalendarUtil.convCalendarToYYYYMMDD(today);

		// 결과일.
		int targetYMD   = 0;

		// calendar 전체 리스트
		List yyyymmddList = getYyyymmddList(calendarId);
		
		if (n > 0) {// ## calendar 기준으로 "익n일" 구하는 로직
			// calendar 에서 today 보다 큰 날 중에서 가장 작은 날을 찾아서 n 만큼 index를 더한다.

			// calendar 전체 리스트 중에서 today 월의 첫 시작일 index
			int monthBeginIdx = getMonthIndex(calendarId, String.valueOf(CalendarUtil.convCalendarToYYYYMM(today)));

			for (int i=monthBeginIdx; i<yyyymmddList.size(); i++) {
				if (((Integer)yyyymmddList.get(i)).intValue() > todayYMD) {
					try {
						targetYMD = ((Integer)yyyymmddList.get(i-1+n)).intValue(); // 익n일 의 일자
						break;
					}catch(IndexOutOfBoundsException e) {
						// get 에서 index 범위를 넘어간 경우, 더이상 calendar 에 데이타가 없는 경우, 그냥 null리턴하도록한다.
						break;
					}
				}
			}
		}else { // ## calendar 기준으로 "전n일" 구하기 로직
			// calendar 에서 today 보다 작은 날 중에서 가장 큰날을 찾아서 n 만큼 index를 뺀다.

			// calendar 전체 리스트 중에서 today 다음달 시작일 index
			int nextMonthBeginIdx = getMonthIndex(calendarId, String.valueOf(CalendarUtil.getNextMonthYYYYMM(today)));
			// 월말을 구하기 어려우니 다음달초부터 시작해서 꺼꾸로 traverse 한다.
			for (int i=nextMonthBeginIdx; i>0; i--) {
				if (((Integer)yyyymmddList.get(i)).intValue() < todayYMD) {					
					try {
						targetYMD = ((Integer)yyyymmddList.get(i+1+n)).intValue(); // 전n일 의 일자. n이 이미 음수(-)이므로 -하면안되고 + 해야한다.  
						break;
					}catch(IndexOutOfBoundsException e) {
						// get 에서 index 범위를 넘어간 경우, 더이상 calendar 에 데이타가 없는 경우, 그냥 null리턴하도록한다.
						break;
					}
				}
			}
		}
		
		if (targetYMD > 0) {
			return CalendarUtil.convYYYYMMDDToCalendar(targetYMD);
		}else {
			return null;
		}
	}
	
	/* 
	 * key 순서를 위해 TreeMap 으로 리턴한다.
	 */
	public Map listCalendarIdNames() {
		Map result = new TreeMap();
		Iterator iter = calendars.keySet().iterator();
		while(iter.hasNext()) {
			String id = (String)iter.next();
			CalendarElement ce = (CalendarElement)calendars.get(id);
			result.put(id, ce.getCalendarName());
		}
		return result;
	}

	// 달력을 reload 함.
	public void reload() {
		loadCalendarData();
		log.info(MSG.get("main.calendar.reloaded")); // Calendar 정보를 DB에러 다시 로드합니다.
	}
	
	/**
	 * calendar 정보를 로드함.
	 * 프로젝트에서 필요시 이 메소드를 상속 받아 구현한다.
	 * 이 메소드 안에서 setCalendar() 메소드를 이용하여 calendar를 등록한다. 
	 */
	protected void loadCalendarData() {
		try {
			if ("file".equalsIgnoreCase(sourceType)) {
				loadCalendarDataFromFile();
			}else if ("db".equalsIgnoreCase(sourceType)) {
				loadCalendarDataFromDB();
			}
		}catch(Exception e) {
			throw new SchedulerException("main.calendar.load.error", e); // Calendar 정보를 DB에서 읽는 중 에러가 발생하였습니다 
		}
	}

	/**
	 * calendar 정보를 DB 에서 로드함. 
	 * 설정 파일에 설정한 SQL 로 쿼리를 수행하여 달력정보를 로드하며 setCalendar() 메소드로 calendar 등록한다.
	 */
	private void loadCalendarDataFromDB() throws SQLException {
		Connection conn = null;
		try {
			if(calendarSql != null) {
				conn = dataSource.getConnection();
				
				// 설정된 calendar select sql 로 부터 yyyymmdd 를 읽는다.
				for (Map.Entry<String, String> entry : calendarSql.entrySet()) {
					String[] ss = entry.getKey().split(":"); // calendar id : calendar name. ex) "0:매일", "1:영업일"
					String calendarId   = ss[0];
					String calendarName = ss[1];
					String sql          = entry.getValue();
					
					List yyyymmddList = selectYYYYMMDDListFromDB(conn, sql);
					setCalendar(calendarId, calendarName, yyyymmddList);
				}
			}
		}finally {
			try { conn.close(); }catch(Exception ignore) {}
		}
	}

	/**
	 * Default SqlManager 로 부터 SQL을 수행하여 리턴되는 ResultSet 의 첫번째 column 들로 구성된 List를 리턴함. 
	 * @param sql
	 * @throws SQLException
	 */
	protected List<Integer> selectYYYYMMDDListFromDB(Connection conn, String sql) throws SQLException {
		Statement  stmt = null;
		ResultSet  rs   = null;
		LinkedList list = new LinkedList();
		try {
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sql);
			while(rs.next()) {
				list.add(Integer.valueOf(rs.getString(1)));
			}
		}finally {
			try { rs.close(); }catch(Exception ignore) {}
			try { stmt.close(); }catch(Exception ignore) {}
		}
		return list;
	}
	
	/**
	 * calendar 정보를 DB 에서 로드함. 
	 * 설정 파일에 설정한 SQL 로 쿼리를 수행하여 달력정보를 로드하며 setCalendar() 메소드로 calendar 등록한다.
	 */
	private void loadCalendarDataFromFile() throws IOException {
		// 지정된 파일을 읽어 yyyymmdd 를 읽는다.
		if(files != null) {
			for (Map.Entry<String, String> entry : files.entrySet()) {
				String[] ss = entry.getKey().split(":"); // calendar id : calendar name. ex) "0:매일", "1:영업일"
				String calendarId   = ss[0];
				String calendarName = ss[1];
				String filename     = entry.getValue();
				
				List yyyymmddList = readYYYYMMDDListFromFile(filename);
				setCalendar(calendarId, calendarName, yyyymmddList);
			}
		}
	}

	/**
	 * 파일로 부터 YYYYMMDD 리스트를 읽는다.
	 * 
	 * - 파일 내용 전체를 read 하되. 오늘 기준 작년 ~ 내년까지의 내용만 메모리에 적재하고 그외는 버린다.
	 * - YYYYMMDD 형태가 아닌 잘못된 값은 버린다.
	 * - YYYYMMDD 뒤의 값은 코멘트로 간주하여 무시한다.
	 *   
	 * @param filename
	 * @return
	 */
	protected List readYYYYMMDDListFromFile(String filename) throws IOException {
		int currentYear = Calendar.getInstance().get(Calendar.YEAR); // 올해
		
		int fromDate = (currentYear - (int)(loadRangeYear/2)) * 10000 + 0101; // 작년  1월  1일
		int toDate   = (currentYear + (int)(loadRangeYear/2)) * 10000 + 1231; // 내년 12월 31일
		
		// 파일 읽기
		List<Integer>  dateList = new ArrayList(1000);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filename));
			String line = null;
			
			while((line = in.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#")) continue;  // 주석 무시
				if (line.length()==0) continue;      // 빈 라인 무시
				
				if (line.length() > 8) {             // 일자 옆에 주석이 있을 수 있으므로 앞 8자리까지만
					line = line.substring(0,8);
				}

				try {
					int date = Integer.parseInt(line);
					if (date >= fromDate && date <= toDate) {  // 범위내의 일자만 로드함
						dateList.add(date);
					}
				}catch(NumberFormatException e) {
					System.out.println("[WARN] Wrong date ("+line+")");
				}
			}
		}finally {
			try { in.close(); }catch(Exception ignore) {}
		}
		
		Collections.sort(dateList);
		return dateList;
	}
}
 