package nexcore.scheduler.startup;

import java.net.InetAddress;

import nexcore.framework.supports.EncryptionUtils;
import nexcore.scheduler.controller.admin.ControllerAdmin;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 배치 스케줄러를 Shutdown 시키는 메인 메소드</li>
 * <li>작성일 : 2010. 10. 05.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class StopMain {

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: StopMain [controller ip] [controller port] [admin id] [admin password]");
			return;
		}
		
		String encPassword = args[3];
		String decPassword = args[3];
//		if (!Util.isBlank(encPassword) && (encPassword.contains("{DES}") || encPassword.contains("{AES}"))) {
		if (!Util.isBlank(encPassword)) {
		    decPassword =  EncryptionUtils.decode(encPassword);
		}
		
		ControllerAdmin admin = new ControllerAdmin(args[0], args[1]);
		
		admin.shutdown(args[2], decPassword, InetAddress.getLocalHost().getHostAddress());
	}
}
