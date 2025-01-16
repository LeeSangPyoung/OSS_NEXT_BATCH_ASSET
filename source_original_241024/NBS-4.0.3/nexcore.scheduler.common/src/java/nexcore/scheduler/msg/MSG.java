package nexcore.scheduler.msg;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.exception.SchedulerException;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어 </li>
 * <li>설  명 : 배치 스케줄러에서 사용하는 메시지 Util 클래스.  </li>
 * <li>작성일 : 2011. 3. 12.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class MSG {
	private static final String PROPERTY_NAME_MSG_LOCALE = "scheduler.msg.locale"; 

	private static Locale defaultLocale;
	static {
		if (System.getProperty(PROPERTY_NAME_MSG_LOCALE) != null) {
			String localeStr = System.getProperty(PROPERTY_NAME_MSG_LOCALE);
			
			String[] s = localeStr.split("_");
			if (s.length == 3) {
				defaultLocale = new Locale(s[0], s[1], s[2]);
			}else if (s.length == 2) {
				defaultLocale = new Locale(s[0], s[1]);
			}else if (s.length == 1) {
				defaultLocale = new Locale(s[0]);
			}
		}else {
			defaultLocale = Locale.getDefault();
		}
	}

	public static String get(String msgName) {
		return get(msgName, defaultLocale);
	}

	public static String get(String msgName, Locale locale) {
		ResourceBundle bundle = ResourceBundle.getBundle("nexcore.scheduler.msg.Message", locale);
		try {
			return bundle.getString(msgName);
		}catch(MissingResourceException e) {
			return "UnknownMessage('"+msgName+"')";
		}
	}

	public static String get(String msgName, Object ... params) {
		return get(msgName, defaultLocale, params);
	}

	public static String get(String msgName, Locale locale, Object... params) {
		ResourceBundle bundle = ResourceBundle.getBundle("nexcore.scheduler.msg.Message", locale);
		try {
			String msgText = bundle.getString(msgName);
			return MessageFormat.format(msgText, params);
		}catch(MissingResourceException e) {
			return "UnknownMessage('"+msgName+"',"+Arrays.asList(params)+")";
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(MessageFormat.format("1111 {1}\t{0}", new Object[]{null, 10}));
		System.out.println(MessageFormat.format("1111 {1}\t{0}", new Object[]{null, 1000000}));
		System.out.println(MessageFormat.format("1111 {1}\t{0}", null, 1000001));
		System.out.println(MessageFormat.format("1111 {1}\t{0}", null, AdminAuth.getAdminAuthSchedulerItself()));
		
		System.out.println(MessageFormat.format("{0,choice,0#추가|1#삭제|2#변경|3#조회} 중 에러가 발생했습니다.", 0));
		System.out.println(MessageFormat.format("{0,choice,0#추가|1#삭제|2#변경|3#조회} 중 에러가 발생했습니다.", 1));
		System.out.println(MessageFormat.format("{0,choice,0#추가|1#삭제|2#변경|4#조회} 중 에러가 발생했습니다.", 2));
		System.out.println(MessageFormat.format("--{0,number,#}--", 20110105));
		System.out.println(MessageFormat.format("''{0}'' 는 필수입니다.", "20110105"));
		
		throw new SchedulerException("AAAAA", null, 1,2,3);
	}
}

