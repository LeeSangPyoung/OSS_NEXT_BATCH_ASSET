package nexcore.scheduler.agent.startup;

import java.net.InetAddress;

import nexcore.framework.supports.EncryptionUtils;
import nexcore.scheduler.agent.client.AgentClientByRmi;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.IAgentService;
import nexcore.scheduler.util.NRMIClientSocketFactory;
import nexcore.scheduler.util.Util;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;


/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Non-WAS 배치 Agent 를 shutdown 시키는 메인 메소드</li>
 * <li>작성일 : 2010. 10. 27.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class StopMain {

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: StopMain [ip] [port] [admin id] [admin password]");
			return;
		}
		// RMI Proxy 생성
		// nexcore-bat-scheduler.xml 파일에서 설정으로 해야하지만, 동적으로 생성하기 위해서 이렇게 한다.
		RmiProxyFactoryBean rmiProxyFactory = new RmiProxyFactoryBean();
		rmiProxyFactory.setServiceUrl("rmi://"+args[0]+":"+args[1]+"/BatchAgent");
		rmiProxyFactory.setServiceInterface(IAgentService.class);
		rmiProxyFactory.setRefreshStubOnConnectFailure(true);
		rmiProxyFactory.setLookupStubOnStartup(false);
		rmiProxyFactory.setRegistryClientSocketFactory(new NRMIClientSocketFactory(5000, 30000));
		rmiProxyFactory.afterPropertiesSet();
		
		// AgentClient object 생성
		AgentClientByRmi agentClient = new AgentClientByRmi();
		agentClient.setAgentService((IAgentService)rmiProxyFactory.getObject());
		agentClient.init();
		
        String encPassword = args[3];
        String decPassword = args[3];
//        if (!Util.isBlank(encPassword) && (encPassword.contains("{DES}") || encPassword.contains("{AES}"))) {
//            decPassword =  EncryptionUtils.decode(encPassword);
//        }
        if (!Util.isBlank(encPassword)) {
            decPassword =  EncryptionUtils.decode(encPassword);
        }

        try {
			agentClient.shutdown(new AdminAuth(args[2], InetAddress.getLocalHost().getHostAddress(), decPassword));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
