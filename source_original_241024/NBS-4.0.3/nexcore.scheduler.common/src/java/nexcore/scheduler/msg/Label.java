package nexcore.scheduler.msg;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어 </li>
 * <li>설  명 : 배치 스케줄러에서 사용하는 라벨 메세지 Util 클래스.  </li>
 * <li>작성일 : 2011. 3. 18.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class Label {
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
		ResourceBundle bundle = ResourceBundle.getBundle("nexcore.scheduler.msg.Label", locale);
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
		ResourceBundle bundle = ResourceBundle.getBundle("nexcore.scheduler.msg.Label", locale);
		try {
			String msgText = bundle.getString(msgName);
			return MessageFormat.format(msgText, params);
		}catch(MissingResourceException e) {
			return "UnknownMessage('"+msgName+"',"+Arrays.asList(params)+")";
		}
	}
	
	public static void main(String[] args) {
		System.out.println(MessageFormat.format("1111 {1}\t{0}", new Object[]{null, 10}));
		System.out.println(MessageFormat.format("1111 {1}\t{0}", new Object[]{null, 1000000}));
		System.out.println(MessageFormat.format("1111 {1}\t{0}", null, 1000001));
	}
}

