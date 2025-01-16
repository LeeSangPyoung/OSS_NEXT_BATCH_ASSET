package nexcore.scheduler.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nexcore.scheduler.msg.MSG;

public class Util {

	/**
	 * Job 스케줄 (일/요일/월) 정보 구분자
	 */
	private static final String DELIM_SCHED = "/";

	/**
	 * 주어진 List의 모든 객체를 하나의 문자열 (Deliminator 포함)로 변환한다.
	 * 
	 * @param list
	 *            데이터 리스트. List&lt;?&gt;
	 * @return Deliminator를 포함한 문자열
	 */
	public static String toString(List list) {
		return toString(list, DELIM_SCHED);
	}

	/**
	 * 주어진 List의 모든 객체를 하나의 문자열 (Deliminator 포함)로 변환한다.
	 * 
	 * @param list
	 *            데이터 리스트. List&lt;?&gt;
	 * @param delimeter
	 * @return Deliminator를 포함한 문자열
	 */
	public static String toString(List list, String delimeter) {
		if (list == null || list.size() == 0)
			return null;

		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			if (i != 0) {
				sb.append(delimeter);
			}
			sb.append(list.get(i));
		}

		return sb.toString();

	}
	
	/**
     * Boolean -> String 변환
     * @param b
     * @return true 일 경우 "Y" 리턴
     * @return false 일 경우 "N" 리턴
     */
    public static String toString(boolean b) {
    	if(b)
    		return "Y";
    				
    	return "N";
    }

	/**
	 * Deliminator를 포함한 문자열을 List로 변환한다.
	 * 
	 * @param str
	 *            Deliminator를 포함한 문자열
	 * @return 변환된 List
	 */
	public static List toList(String str) {
		return toList(str, DELIM_SCHED);
	}

	public static List<Integer> toListAsInt(String str) {
		return toListAsInt(str, DELIM_SCHED);
	}
	/**
	 * Deliminator를 포함한 문자열을 List로 변환한다.
	 * 
	 * @param str
	 *            Deliminator를 포함한 문자열
	 * @param delimeter
	 * @return 변환된 List
	 */
	public static List<String> toList(String str, String delimeter) {

		final List<String> list = new ArrayList(); // list = List<String>

		if (str == null)
			return list;

		str = str.trim();

		if (str.length() == 0)
			return list;

		final StringTokenizer st = new StringTokenizer(str, delimeter);
		while (st.hasMoreTokens()) {
			list.add(st.nextToken());
		}

		return list;
	}
	
	/**
	 * Deliminator를 포함한 문자열을 List로 변환한다.
	 * List 의 각 객체는 Integer 타입으로 변환한다.
	 * 
	 * @param str
	 *            Deliminator를 포함한 문자열
	 * @param delimeter
	 * @return 변환된 List
	 */
	public static List<Integer> toListAsInt(String str, String delimeter) {

		final List<Integer> list = new ArrayList(); // list = List<String>

		if (str == null)
			return list;

		str = str.trim();

		if (str.length() == 0)
			return list;

		final StringTokenizer st = new StringTokenizer(str, delimeter);
		while (st.hasMoreTokens()) {
			list.add(Integer.parseInt(st.nextToken()));
		}

		return list;
	}

	public static Integer[] stringArrayToIntArray(String[] src) {
		if (src == null) {
			return null;
		}
		Integer[] ret = new Integer[src.length];
		for (int i=0; i<ret.length; i++) {
			ret[i] = Integer.parseInt(src[i]);
		}
		return ret;
	}
	
	/**
	 * 오늘 날짜를 YYYYMMDD 로 리턴함
	 * @return
	 */
	public static String getCurrentYYYYMMDD() {
		return getYYYYMMDD(System.currentTimeMillis());
	}

	/**
	 * 현재시각을 YYYYMMDDHHMMSS 로 리턴함
	 * @return
	 */
	public static String getCurrentYYYYMMDDHHMMSS() {
		return getYYYYMMDDHHMMSS(System.currentTimeMillis());
	}

	/**
	 * 현재시각을 YYYYMMDDHHMMSSSSS 로 리턴함
	 * @return
	 */
	public static String getCurrentYYYYMMDDHHMMSSMS() {
		return getYYYYMMDDHHMMSSMS(System.currentTimeMillis());
	}

	/**
	 * 현재시각을 HHMMSS 로 리턴함
	 * @return
	 */
	public static String getCurrentHHMMSS() {
		return getHHMMSS(System.currentTimeMillis());
	}

	/**
	 * 어제 날짜를 YYYYMMDD 로 리턴함
	 * @return
	 */
	public static String getYesterdayYYYYMMDD() {
		return getYYYYMMDD(System.currentTimeMillis() -  86400000);
	}

	/**
	 * 어제시각을 YYYYMMDDHHMMSS 로 리턴함
	 * @return
	 */
	public static String getYesterdayYYYYMMDDHHMMSS() {
		return getYYYYMMDDHHMMSS(System.currentTimeMillis() -  86400000);
	}

	/**
	 * 내일 날짜를 YYYYMMDD 로 리턴함
	 * @return
	 */
	public static String getTomorrowYYYYMMDD() {
		return getYYYYMMDD(System.currentTimeMillis() -  86400000);
	}

	/**
	 * 내일시각을 YYYYMMDDHHMMSS 로 리턴함
	 * @return
	 */
	public static String getTomorrowYYYYMMDDHHMMSS() {
		return getYYYYMMDDHHMMSS(System.currentTimeMillis() -  86400000);
	}

	/**
	 * 기준일자에서 n일 add, minus 함
	 * @param date 기준일
	 * @param addDay 연산할일
	 * @return
	 */
	public static String getDateAdd(String date, int addDay) {
		// 오차 방지를 위해 00시 10분 00초를 기준으로 계산한다.
		return getYYYYMMDD(parseYYYYMMDDHHMMSS(date+"001000") + addDay * 86400000); 
	}
	
	public static String getYYYYMMDDHHMMSSMS(long time) {
		return FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(time);
	}
	public static String getYYYYMMDDHHMMSS(long time) {
		return FastDateFormat.getInstance("yyyyMMddHHmmss").format(time);
	}
	public static String getYYYYMMDD(long time) {
		return FastDateFormat.getInstance("yyyyMMdd").format(time);
	}
	public static String getHHMMSS(long time) {
		return FastDateFormat.getInstance("HHmmss").format(time);
	}
	public static String getHHMM(long time) {
		return FastDateFormat.getInstance("HHmm").format(time);
	}
	public static String formatTime(String format, long time) {
		return FastDateFormat.getInstance(format).format(time);
	}
	
	/**
	 * localized 형식의 패터으로 변환한 datetime 리턴.
	 * @param time
	 * @return
	 */
	public static String getDatetimeLocalizedText(long time) {
		String localDatetimeFormat = System.getProperty("NC_LOCAL_DATETIME_FORMAT");
		if (isBlank(localDatetimeFormat)) {
			localDatetimeFormat = "yyyy-MM-dd HH:mm:ss";
		}
		
		return FastDateFormat.getInstance(localDatetimeFormat).format(time);
	}
	
	/**
	 * localized 형식의 패터으로 변환한 date 리턴.
	 * @param time
	 * @return
	 */
	public static String getDateLocalizedText(long time) {
		String localDateFormat = System.getProperty("NC_LOCAL_DATE_FORMAT");
		if (isBlank(localDateFormat)) {
			localDateFormat = "yyyy-MM-dd";
		}
		
		return FastDateFormat.getInstance(localDateFormat).format(time);
	}

	public static long parseYYYYMMDDHHMMSS(String datetime)  {
		return parseDateString("yyyyMMddHHmmss", datetime);
	}
	
	public static long parseDateString(String format, String datetime)  {
		if (Util.isBlank(datetime)) {
			return 0;
		}
		try {
			return new SimpleDateFormat(format).parse(datetime).getTime();
		} catch (ParseException e) {
			throw new RuntimeException("Illegal dateformat ["+datetime+"]", e);
		}
	}
	
	/**
	 * 두 날짜 사이의 차이를 일 단위로 리턴함.
	 * @param yyyymmdd1
	 * @param yyyymmdd2
	 * @return
	 */
	public static int getDiffDay(String yyyymmdd1, String yyyymmdd2) {
		long date1 = parseYYYYMMDDHHMMSS(yyyymmdd1+"000001");
		long date2 = parseYYYYMMDDHHMMSS(yyyymmdd2+"000001");
		
		return (int)((date2 - date1) / 1000 / 60 / 60 / 24);
	}
	
	/**
	 * 두 시각 사이의 차이를 초단위로 리턴함 (hhmmss2 - hhmmss1)
	 * @param hhmmss1
	 * @param hhmmss2
	 * @return
	 */
	public static long getDiffSecond(String hhmmss1, String hhmmss2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		
		cal1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hhmmss1.substring(0,2)));
		cal1.set(Calendar.MINUTE,      Integer.parseInt(hhmmss1.substring(2,4)));
		cal1.set(Calendar.SECOND,      Integer.parseInt(hhmmss1.substring(4,6)));
		cal1.set(Calendar.MILLISECOND, 0);
		
		cal2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hhmmss2.substring(0,2)));
		cal2.set(Calendar.MINUTE,      Integer.parseInt(hhmmss2.substring(2,4)));
		cal2.set(Calendar.SECOND,      Integer.parseInt(hhmmss2.substring(4,6)));
		cal2.set(Calendar.MILLISECOND, 0);
		
		return (cal2.getTimeInMillis() - cal1.getTimeInMillis()) / 1000; 
	}
	
	/**
	 * 해당월의 말일 구하기
	 * @param yyyymm (년월 4자리)
	 * @return
	 */
	public static String getLastDayOfMonth(String yyyymm) {
		long month01date = parseDateString("yyyyMMdd", yyyymm+"01");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(month01date);
		cal.add(Calendar.MONTH, 1);  // 다음달
		cal.set(Calendar.DATE,  1);  // 1일에서
		cal.add(Calendar.DATE,  -1); // 1일 빼기
		return getYYYYMMDD(cal.getTimeInMillis());
	}
	
	/**
	 * 밀리초를 *h*m*s*ms 형태로 변환한다.
	 * @param s
	 * @return
	 */
    public static String milliSecondsToTime(long millisecond, boolean printMilliSecond){
    	String result;
    	long s = millisecond/1000;
    	if (s >= 3600) {
            int h  = (int)Math.floor( s / ( 60l * 60l) );
            s -= h * ( 60l * 60l );
            int m  = (int)Math.floor( s / 60l );
            s -= m * 60;
            result = (h==0?"":h+"h ") + (m==0?"":m+"m ") + (s==0?"":s+"s ") ;
    	}else if (s >= 60) {
            int m  = (int)Math.floor( s / 60l );
            s -= m * 60;
            result = (m==0?"":m+"m ") + (s==0?"":s+"s ");
    	}else {
    		result = s+"s ";
    	}
    	
    	if (printMilliSecond) {
    		int ms = (int)(millisecond % 1000l);
    		result += (ms==0?"":ms+"ms");
    	}
    	return result.trim();
    }

    /**
     * 길이만큼 (byte단위) value string 을 잘라낸다.
     * SQL 파라미터로 사용할때 value 가 컬럼길이를 초과하지 않도록 하기 위해 쓰인다. 
     *
     * @param value
     * @param length
     * @return
     */
    public static String fitLength(String value, int length) {
        if (value == null) {
            return value;
        }
        
        if (value.length() * 3 < length) {
            return value;
        }else {
            byte[] tmp = value.getBytes();
            if (tmp.length <= length) {
                return value;
            }else {
           		return new String(tmp, 0, length);
            }
        }
    }
    
    /**
     * byte 수가 아닌 char 수로 substring 한다. NullPointerException 이 안나도록 처리한다.
     * @param value
     * @param length
     */
    public static String left(String value, int length) {
        if (value == null) {
            return value;
        }
        
        if (value.length() < length) {
        	return value;
        }else {
        	return value.substring(0, length-1);
        }
    }
    
	public static String[] EMPTY_STRING_ARRAY = new String[0];
	
	public static void sleep(long time) {
		sleep(time, false);
	}
	
	public static void sleep(long time, boolean ignoreInterrupedException) {
		try {
			Thread.sleep(time);
		}catch(InterruptedException e) {
			if (ignoreInterrupedException) {
				return;
			}else {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * InvocationTargetException, EJBException 발생시 base cause Exception 을 리턴한다. 
	 * @param e
	 * @return
	 */
	public static Throwable getCauseException(Throwable e) {
		Throwable cause = e;
		while(true) {
			if (cause instanceof InvocationTargetException) {
				cause = cause.getCause();
			}else if (cause.getClass().getName().equals("javax.ejb.EJBException")) { // EJBException 가 없다는 컴파일 에러방지를 위해 이렇게 함
				if (cause.getCause() != null) {
					cause = cause.getCause();
				}else {
					try {
						Method getCausedByExceptionMethod = cause.getClass().getMethod("getCausedByException", new Class[0]);
						Exception causedBy = (Exception)getCausedByExceptionMethod.invoke(cause, new Object[0]);
						if (causedBy == null) {  // == if (((javax.ejb.EJBException)cause).getCausedByException() == null) {
							return cause;
						}else {
							cause = causedBy; 
						}
					}catch (Exception ex) { // 무시함
						return cause; 
					}
				}
			}else {
				return cause;
			}
		}
	}

	/**
	 * 일자가 YYYYMMDD 형태인지 체크함.
	 * @param date
	 * @return
	 */
	public static void checkDateYYYYMMDD(String date) {
		boolean check = false;
		
		try {
			if (date != null && date.length() == 8) {
				int y = Integer.parseInt(date.substring(0, 4));
				int m = Integer.parseInt(date.substring(4, 6));
				int d = Integer.parseInt(date.substring(6, 8));
				if (1 <= m && m <= 12) {
					if (d >= 1) {
						if (m==2) {
							// 28,29일 체크
							if (((y % 4 == 0) && (y % 100 !=0)) || (y % 400 == 0)) {
								check = d<=29;
							}else {
								check = d<=28;
							}
						}else if (m==1 || m==3 || m==5 || m==7 || m==8 || m==10 || m==12) {
							// 큰달 체크.
							check = d<=31;
						}else {
							// 작은달 체크
							check = d<=30;
						}
					}
				}
			}
		}catch(Exception e) {
			check = false;
		}
		if (!check) {
			throw new IllegalArgumentException("Date format error. ["+date+"]");
		}
	}
	
	/**
	 * Throwable 을 받아서 RuntimeException 으로 cast 하거나 wrap 하여 리턴함.
	 * @param e
	 * @return
	 */
	public static RuntimeException toRuntimeException(Throwable e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException)e;
		}else {
			return new RuntimeException(e);
		}
	}
	
	public static void logErrorConsole(String msg) {
		LogFactory.getLog("console").error(msg);
	}

	public static void logErrorConsole(String msg, Throwable e) {
		LogFactory.getLog("console").error(msg, e);
	}

	public static void logInfoConsole(String msg) {
		LogFactory.getLog("console").info(msg);
	}
	
	public static void logServerInitConsole(Object ... params) {
		if (params !=null) {
			if (params.length == 1) {
				logInfoConsole(MSG.get("main.comp.init.ok", params[0], "")); // 파라미터가 하나일때는 {1} 이 출력되지 않게 하기 위함.
			}else {
				logInfoConsole(MSG.get("main.comp.init.ok", params));
			}
		}
	}
	
	public static byte[] objectToBytes(Serializable obj) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream(256);
		ObjectOutputStream    oout = null;
		try {
			oout = new ObjectOutputStream(bout);
			oout.writeObject(obj);
		}catch(IOException e) {
			throw Util.toRuntimeException(e);
		}finally {
			try { oout.close(); }catch(Exception e) {}
		}
		
		return bout.toByteArray();
	}
	
	public static Serializable bytesToObject(byte[] bytes) {
		Serializable obj = null;
		ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		ObjectInputStream oin = null;
		try {
			oin = new ObjectInputStream(bin);
			obj = (Serializable)oin.readObject();
		}catch(Exception e) {
			throw Util.toRuntimeException(e);
		}finally {
			try {oin.close();}catch(Exception e) {}
		}

		return obj;
	}
	
	static String localIp;
	
	public static String getLocalIp() {
		try {
			return localIp != null ? localIp : (localIp = InetAddress.getLocalHost().getHostAddress());
		}catch(Exception e) {
			throw Util.toRuntimeException(e);
		}
	}
	
	public static boolean isBlank(String s) {
		return org.apache.commons.lang.StringUtils.isBlank(s);
	}

	public static String nvl(Object s) {
        return s==null ? "" : String.valueOf(s);
    }

	public static String nvl(Object s, String defaultValue) {
        return s==null ? defaultValue : String.valueOf(s);
    }

	public static String nvlBlank(String s, String replaceValue) {
        return isBlank(s) ? replaceValue : s;
    }

	public static String trimIfNotNull(Object s) {
		return s==null ? (String) s : ((String)s).trim();
	}
	
	public static int toInt(String s) {
		return toInt(s, 0);
	}
	
    public static int toInt(String s, int defaultValue) {
		return isBlank(s) ? defaultValue : Integer.parseInt(s.trim());
	}

    public static long toLong(String s) {
		return toLong(s, 0);
	}

    public static long toLong(String s, long defaultValue) {
		return isBlank(s) ? defaultValue : Long.parseLong(s.trim());
	}

    public static double toDouble(String s) {
    	return toDouble(s, 0);
    }
    
    public static double toDouble(String s, double defaultValue) {
		return isBlank(s) ? defaultValue : Double.parseDouble(s.trim());
	}

    /**
     * String -> Boolean 변환
     * @param s
     * @return "Y" 일 경우 true 리턴
     * @return "N" 일 경우 false 리턴
     */
    public static boolean toBoolean(String s) {
    	return 
        		"1".equals(s) || 
        		"Y".equalsIgnoreCase(s)    || "YES".equalsIgnoreCase(s) || 
        		"TRUE".equalsIgnoreCase(s) || "T".equalsIgnoreCase(s) || 
        		"ON".equalsIgnoreCase(s);
    }
	
    /**
     * String -> Boolean 변환
     * @param s
     * @param defaultValue
     * @return "1", "Y", "YES", "TRUE", "T", "ON" 일 경우 true 리턴
     */
    public static boolean toBoolean(String s, boolean defaultValue) {
    	return isBlank(s) ? defaultValue : toBoolean(s); 
    }

    public static void logInfo(Log log, String msg) {
		logInfo(log, msg, null);
	}
	
	public static void logInfo(Log log, String msg, Throwable t) {
		try {
			if (t==null) {
				log.info(msg);
			}else {
				log.info(msg, t);
			}
		}catch (Throwable ignore) { // 로그 찍다가 에러나는 경우를 무시하기 위함
		}
	}

	public static void logWarn(Log log, String msg) {
		logWarn(log, msg, null);
	}
	
	public static void logWarn(Log log, String msg, Throwable t) {
		try {
			if (t==null) {
				log.warn(msg);
			}else {
				log.warn(msg, t);
			}
		}catch (Throwable ignore) { // 로그 찍다가 에러나는 경우를 무시하기 위함
		}
	}

	public static void logError(Log log, String msg) {
		logError(log, msg, null);
	}

	public static void logError(Log log, String msg, Throwable t) {
		try {
			if (t==null) {
				log.error(msg);
			}else {
				log.error(msg, t);
			}
		}catch (Throwable ignore) {// 로그 찍다가 에러나는 경우를 무시하기 위함
		}
	}

	public static void logDebug(Log log, String msg) {
		logDebug(log, msg, null);
	}
	
	public static void logDebug(Log log, String msg, Throwable t) {
		try {
			if (t==null) {
				log.debug(msg);
			}else {
				log.debug(msg, t);
			}
		}catch (Throwable ignore) {// 로그 찍다가 에러나는 경우를 무시하기 위함
		}
	}
	
    /** 
     * null 체크와 함께 equals 비교. 
     * equals() 를 직접 쓸때 NullPointerException 방지하는 목적
     * @param a
     * @param b
     * @return
     */ // 정호철 - 2010-09-09 -
    public static boolean equals(String a, String b) {
        if (a == null) {
            return b == null;
        }else {
            return a.equals(b);
        }
    }

    /** 
     * null 체크와 함께 equalsIgnoreCase 비교. 
     * equalsIgnoreCase() 를 직접 쓸때 NullPointerException 방지하는 목적
     * @param a
     * @param b
     * @return
     */ // 정호철 - 2010-09-09 -
    public static boolean equalsIgnoreCase(String a, String b) {
        if (a == null) {
            return b == null;
        }else {
            return a.equalsIgnoreCase(b);
        }
    }

    /** 
     * null 체크와 함께 equals 비교. "" 와 null을 같은 것으로 취급 
     * equals() 를 직접 쓸때 NullPointerException 방지하는 목적
     * @param a
     * @param b
     * @return
     */ // 정호철 - 2010-09-09 -
    public static boolean equalsIgnoreNull(String a, String b) {
        if (isBlank(a)) {
            return isBlank(b);
        }else {
            return a.equals(b);
        }
    }

    /** 
     * null 체크와 함께 equalsIgnoreCase 비교. "" 와 null을 같은 것으로 취급 
     * equalsIgnoreCase() 를 직접 쓸때 NullPointerException 방지하는 목적
     * @param a
     * @param b
     * @return
     */ // 정호철 - 2010-09-09 -
    public static boolean equalsIgnoreCaseIgnoreNull(String a, String b) {
        if (isBlank(a)) {
            return isBlank(b);
        }else {
            return a.equalsIgnoreCase(b);
        }
    }
    
    /**
     * -DNEXCORE_ID 설정값 리턴
     * 또는 -DNEXCORE_ID 
     * @return
     */
    public static String getSystemId() {
    	return Util.nvlBlank(System.getProperty("NEXCORE_ID"), System.getProperty("NEXCORE_ID"));
    }

    public static String getHomeDirectory() {
    	return System.getProperty("NEXCORE_HOME");
    }
    
   /**
    * searchdate 를 기준으로 YYYYMMDD 형태의 from ~ to between 일자 array 를 리턴한다.
    * 예) 2012      => [20120101,20121231] <br>
    *     201       => [20100101,20191231] <br>
    *     201210    => [20121001,20121031] <br>
    *     20120     => [20120101,20120931] <br>
    *     2012021   => [20120210,20120219] <br>
    *     2012022   => [20120221,20120229] <br>
    *     2013022   => [20130221,20130228] <br>
    *     20131025  => [20131025,20131025] <br>
    * @param date
    * @return
    */
    public static String[] getBetweenDate(String date) {
		String searchDateFrom;
		String searchDateTo;

		if (date.length() <= 4) {
			searchDateFrom = (date+"0000").substring(0,4);
			searchDateTo   = (date+"9999").substring(0,4);
			searchDateFrom = searchDateFrom + "0101";
			searchDateTo   = searchDateTo   + "1231";
		}else if (date.length() <= 6) {
			if (date.charAt(4) == '0') { /* 월이 0으로 시작하면 */
				searchDateFrom = (date+"1").substring(0,6) ;  /* 월이 0으로 시작하면 최소 01월 */
				searchDateTo   = (date+"9").substring(0,6) ;  /* 월이 0으로 시작하면 최대 09월 */	
			}else if (date.charAt(4) == '1') { /* 월이 1로 시작하면 */
				searchDateFrom = (date+"0").substring(0,6) ;  /* 월이 1로 시작하면 최소 10월 */
				searchDateTo   = (date+"2").substring(0,6) ;  /* 월이 1로 시작하면 최대 12월 */	
			}else { /* 월이 2 이상이면 */
				throw new IllegalArgumentException("Wrong date format ["+date+"]");
			}
			searchDateFrom = searchDateFrom + "01";
			searchDateTo   = getLastDayOfMonth(searchDateTo);
		}else if (date.length() < 8) {  /* 7자리이면 */
			if (date.charAt(6) == '0') { /* 일이 0으로 시작하면 */
				searchDateFrom = (date+"1").substring(0,8) ;  /* 일이 0으로 시작하면 최소 01일 */
				searchDateTo   = (date+"9").substring(0,8) ;  /* 일은 무조건 9로 끝나도록 한 후에 다시 교정한다. */
			}else if (date.charAt(6) > '3') { /* 일이 3 보다 크면 에러 */
				throw new IllegalArgumentException("Wrong date format ["+date+"]");
			}else { /* 일이 1,2,3 으로 시작하면 */
				searchDateFrom = (date+"0").substring(0,8) ;  /* 일이 1로 시작하면 최소 10일 */
				searchDateTo   = (date+"9").substring(0,8) ;  /* 일은 무조건 9로 끝나도록 한 후에 다시 교정한다. */
			
				String eom = getLastDayOfMonth(searchDateTo.substring(0,6)); // end of month
				if (searchDateTo.compareTo(eom) > 0) {
					searchDateTo = eom; // 그달 말일보다 큰 값이 나오면 그달 말일로 교정한다.
				}
			}
		}else if (date.length() == 8) {
			searchDateFrom = date;
			searchDateTo   = date;
		}else { /* 8 자리를 초과하면 */
			searchDateFrom = date.substring(0,8);
			searchDateTo   = date.substring(0,8);
		}

		return new String[]{searchDateFrom, searchDateTo};
    }
    
    /**
     * 파일에서 객체 읽기 
     * @param file
     * @return
     */
    public static Object readObjectFromFile(File file) {
    	ObjectInputStream in = null;
    	try {
    		in = new ObjectInputStream(new FileInputStream(file));
    		return in.readObject();
    	}catch (Exception e) {
    		throw toRuntimeException(e);
    	}finally {
    		if (in!=null) {
    			try { 
    				in.close();
    			}catch(Exception e) {
    				e.printStackTrace();
    			}
    		}
    	}
    }

    /**
     * 파일에서 객체 읽기 
     * @param file
     * @param obj
     * @return
     */
    public static void writeObjectToFile(File file, Object obj) {
    	ObjectOutputStream out = null;
    	try {
    		out = new ObjectOutputStream(new FileOutputStream(file));
    		out.writeObject(obj);
    	}catch (Exception e) {
    		throw toRuntimeException(e);
    	}finally {
    		if (out!=null) {
    			try { 
    				out.close();
    			}catch(Exception e) {
    				e.printStackTrace();
    			}
    		}
    	}
    }
    
    
    public static void sortFiles(File[] files) {
        if (files != null) {
            Arrays.sort(files, (Comparator) new FileComparator());
        }
    }

    public static void sortFiles(List/*<File>*/ files) {
        if (files != null) {
            Collections.sort(files, (Comparator) new FileComparator());
        }
    }

    /**
     * 파일의 timestamp 를 기준으로 sort함.
     *
     * @param files
     */ // 정호철. 2010.01-29
    public static void sortFilesByTimestamp(File[] files) {
        if (files != null) {
            Arrays.sort(files, new FileComparatorByTimestamp());
        }
    }

    /**
     * 파일의 timestamp 를 기준으로 sort함.
     *
     * @param files
     */ // 정호철. 2010.01-29
    public static void sortFilesByTimestamp(List/*<File>*/ files) {
        if (files != null) {
            Collections.sort(files, new FileComparatorByTimestamp());
        }
    }

    private static class FileComparator implements Comparator {
        
        private Collator c = Collator.getInstance();

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object arg1, Object arg2) {
            
            File f1 = (File) arg1;
            File f2 = (File) arg2;
            
            if (f1 == f2) {
                return 0;
            }
            if (f1.isDirectory() && f2.isFile()) {
                return -1;
            }
            if (f1.isFile() && f2.isDirectory()) {
                return 1;
            }
            return c.compare(f1.getName(), f2.getName());            
        }
    }

    private static class FileComparatorByTimestamp implements Comparator {
        private Map timestampMap = new HashMap(); 
        // sort 알고리즘 중에 File.lastModified() 메소드를 많이 부르게되면 과다한 IO가 일어나 속도가 많이 떨어진다.
        // 이를 방지하기 위해 한번 lastModified() 한 값을 캐쉬해서 사용한다.
        public int compare(Object arg0, Object arg1) {
            File f0 = (File)arg0;
            File f1 = (File)arg1;
            Long timestamp0 = (Long)timestampMap.get(f0.getPath());
            if (timestamp0 == null) {
                timestamp0 = new Long(f0.lastModified());
                timestampMap.put(f0.getPath(), timestamp0);
            }
            
            Long timestamp1 = (Long)timestampMap.get(f1.getPath());
            if (timestamp1 == null) {
                timestamp1 = new Long(f1.lastModified());
                timestampMap.put(f1.getPath(), timestamp1);
            }
            return timestamp0.compareTo(timestamp1);
        }
    }
    
    public static final String getJvmPID() {
		RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
		String pidString = rmxb.getName(); // pid@hostname 형태로 리턴됨. 1.5 이상에서만 사용 가능.
		int idx = pidString.indexOf("@");
		if (idx < 0) {
			throw new RuntimeException("Fail to get pid. RuntimeMXBean.getName()="+pidString);
		}
		String pid = pidString.substring(0, idx);
		return pid;
    }
    
    public static final String getHostname() {
    	RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
    	String pidString = rmxb.getName(); // pid@hostname 형태로 리턴됨. 1.5 이상에서만 사용 가능.
    	int idx = pidString.indexOf("@");
    	if (idx < 0) {
    		throw new RuntimeException("Fail to get hostname. RuntimeMXBean.getName()="+pidString);
    	}
    	String pid = pidString.substring(idx+1);
    	return pid;
    }
    
    public static String toStringIgnoreNull(Object obj, String nullValue) {
        return obj==null ? nullValue : obj.toString();
    }

    public static String toStringIgnoreNull(Object obj) {
        return toStringIgnoreNull(obj, "null");
    }
    
    private static String hostName;
    private static String hostAddress;
    
	public final static String getHostName() {
		loadInetAddress();
		return hostName;
	}

	public final static String getHostAddress() {
		loadInetAddress();
		return hostAddress;
	}

	private final static void loadInetAddress() {
		if(hostName == null || hostAddress == null) {
			try {
				InetAddress ia = InetAddress.getLocalHost();
				if(ia != null) {
					hostName    = ia.getHostName();
					hostAddress = ia.getHostAddress();
				}
			} catch (UnknownHostException e) {
			}
		}
	}
}



