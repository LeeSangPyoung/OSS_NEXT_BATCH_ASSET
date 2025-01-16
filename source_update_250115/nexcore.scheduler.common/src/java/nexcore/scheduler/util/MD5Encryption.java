package nexcore.scheduler.util;

import java.io.IOException;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

// 로그인시 사용됨. 구버전 호환을 위해 그대로 둠.
// command line 에서 사용될 수 있음
public class MD5Encryption {

	/**
	 * 특정 문자열을 MD5로 암호화 하여 리턴
	 * 
	 * @param value
	 *            암호화할 대상 문자열
	 * @return 암호화된 문자열
	 * @throws IOException
	 */
	public static String encode(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(value.getBytes());
			byte[] raw = md.digest();
//			BASE64Encoder b64e = new BASE64Encoder();
//			return b64e.encode(raw);
			
//			return Base64.encodeToString(raw, false);
			
			return new String(Base64.encodeBase64(raw, false));
		} catch (Exception e) {
			throw new RuntimeException("Exception occurred in processing md5 encoding", e);
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage : MD5Encryption [value]");
			return ;
		}
		
		System.out.println(encode(args[0]));

	}
}
