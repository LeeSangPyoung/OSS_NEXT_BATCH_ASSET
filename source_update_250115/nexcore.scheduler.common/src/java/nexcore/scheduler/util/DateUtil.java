package nexcore.scheduler.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;


/**
 * 날짜 관련 클래스
 *
 * @작성일	2016/04/22
 * @작성자	신윤호
 */
public class DateUtil {
	private static String dateFormat = "yyyyMMddHHmmssSSS";
	
	
	/**
     * 해당 시간의 Timestamp 객체 생성
     *
     * @param	timeMillis  
     * @return	Timestamp
     */
	public static Timestamp getTimestamp(long timeMillis){
		return new Timestamp(timeMillis);
	}
	
	/**
     * 해당 시간의 Timestamp 객체 생성
     *
     * @param	dateTime (yyyyMMddHHmmssSSS)  
     * @return	Timestamp
     */
	public static Timestamp getTimestamp(String dateTime){
		return new Timestamp(getTimestampLong(dateTime));
//		if(dateTime == null || dateTime.equals(""))
//			return null;
//		
//		StringBuffer buffer = new StringBuffer();
//		
//		buffer.append(dateTime.substring(0, 4)).append("-");
//		buffer.append(dateTime.substring(4, 6)).append("-");
//		buffer.append(dateTime.substring(6, 8)).append(" ");
//		buffer.append(dateTime.substring(8, 10)).append(":");
//		buffer.append(dateTime.substring(10, 12)).append(":");
//		buffer.append(dateTime.substring(12, 14)).append(".");
//		buffer.append(dateTime.substring(14, 17));
//		
//		return Timestamp.valueOf(buffer.toString());
	}
	
	/**
     * 현재 시간의 TimeStamp 객체 생성
     *
     * @param	none  
     * @return	TimeStamp
     */
	public static Timestamp getCurrentTimestamp(){
		return getTimestamp(System.currentTimeMillis());
	}
	
	/**
     * 해당 시간을 지정된 형태의  문자열로 변환
     *
     * @param	long  
     * @return	String
     */
	public static String getTimestampString(Timestamp timeStamp){
		return format(timeStamp, dateFormat);
	}
	
	/**
     * 해당 시간을 지정된 형태의  문자열로 변환
     *
     * @param	long  
     * @return	String
     */
	public static String getTimestampString(long timeMillis){
		return format(timeMillis, dateFormat);
	}
	
	/**
     * 현재 시간을 지정된 형태의  문자열로 변환
     *
     * @param	none  
     * @return	String
     */
	public static String getCurrentTimestampString(){
		return format(System.currentTimeMillis(), dateFormat);
	}
	
	/**
     * 문자열 시간을 long 으로 변환
     *
     * @param	String (yyyyMMddHHmmssSSS)  
     * @return	long
     */
	public static long getTimestampLong(String dateTime){
		if(dateTime==null || dateTime.equals(""))
			return 0;
		
		return Util.parseDateString("yyyyMMddHHmmssSSS", dateTime);
	}
	
	/**
     * Timestamp 시간을 long 으로 변환
     *
     * @param	Typestamp 
     * @return	long
     */
	public static long getTimestampLong(Timestamp timestamp){
		
		return timestamp.getTime();
	}
	
	/**
     * 현재 시간를 long 으로 변환
     *
     * @param	None
     * @return	long
     */
	public static long getCurrentTimestampLong(){
		
		return getCurrentTimestamp().getTime();
	}
	
	/**
     * 해당 시간을 특정 형태의  문자열로 변환
     *
     * @param	timeStamp  
     * @return	String
     */
	public static String format(Timestamp timeStamp, String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		return sdf.format(timeStamp);
	}
	
	/**
     * 해당 시간을 특정 형태의  문자열로 변환
     *
     * @param	long  
     * @return	String
     */
	public static String format(long timeMillis, String format){
		
		return format(getTimestamp(timeMillis), format);
	}
}
