package nexcore.scheduler.util;

import java.io.IOException;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

// 로그인시 사용됨.
// 구버전은 MD5 로 했으나, 4.0 부터는 SHA256 으로 한다.
public class MessageDigestUtil {

	/**
	 * 특정 문자열을 MD5로 암호화 하여 리턴
	 * 구버전 호환을 위해 유지.
	 * @param value
	 *            암호화할 대상 문자열
	 * @return 암호화된 문자열의 Base64 인코딩된 string. (length=24정도)
	 * @throws IOException
	 */
	public static String encodeMD5(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(value.getBytes());
			byte[] raw = md.digest();
			
			return new String(Base64.encodeBase64(raw, false));
		} catch (Exception e) {
			throw new RuntimeException("Exception occurred in processing md5 encoding", e);
		}
	}

	/**
	 * 특정 문자열을 SHA256 으로 암호화 하여 리턴
	 * 구버전 호환을 위해 유지.
	 * @param value
	 *            암호화할 대상 문자열
	 * @return 암호화된 문자열의 Base64 인코딩된 string. (length=44정도)
	 * @throws IOException
	 */
	public static String encode(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(value.getBytes());
			byte[] raw = md.digest();
			
			return new String(Base64.encodeBase64(raw, false));
		} catch (Exception e) {
			throw new RuntimeException("Exception occurred in processing md5 encoding", e);
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage : MessageDigestUtil [MD5/SHA256] [value]");
			return ;
		}
		
		if ("MD5".equals(args[0])) {
			System.out.println(encodeMD5(args[1]));
		}else {
			System.out.println(encode(args[1]));
		}

	}
}
