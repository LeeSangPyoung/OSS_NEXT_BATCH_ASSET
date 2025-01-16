package nexcore.scheduler.controller.admin;

import nexcore.scheduler.entity.IControllerService;
import nexcore.scheduler.entity.IMonitorService;
import nexcore.scheduler.util.NRMIClientSocketFactory;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 *  
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : controller 와 통신하는 client 모듈. RMI</li>
 * <li>작성일 : 2010. 8. 30.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ControllerAdmin extends ControllerAdmin00 {
//	private String                controllerUrl; // rmi://localhost:8124/BatchController
	private String                controllerIp;
	private String                controllerPort;
	
	private RmiProxyFactoryBean   rmiProxyFactoryBean1;
	private RmiProxyFactoryBean   rmiProxyFactoryBean2;
	
	public ControllerAdmin(String ip, String port) {
		this.controllerIp   = ip;
		this.controllerPort = port;
		init();
	}
	
	public void init() {
		rmiProxyFactoryBean1 = new RmiProxyFactoryBean();
		rmiProxyFactoryBean1.setLookupStubOnStartup(false);
		rmiProxyFactoryBean1.setRefreshStubOnConnectFailure(true);
		rmiProxyFactoryBean1.setServiceInterface(IControllerService.class);
		rmiProxyFactoryBean1.setServiceUrl("rmi://"+controllerIp+":"+controllerPort+"/BatchController");
		rmiProxyFactoryBean1.setRegistryClientSocketFactory(new NRMIClientSocketFactory(5000, 30000));
		rmiProxyFactoryBean1.afterPropertiesSet();
		controllerService = (IControllerService)rmiProxyFactoryBean1.getObject();
		
		rmiProxyFactoryBean2 = new RmiProxyFactoryBean();
		rmiProxyFactoryBean2.setLookupStubOnStartup(false);
		rmiProxyFactoryBean2.setRefreshStubOnConnectFailure(true);
		rmiProxyFactoryBean2.setServiceInterface(IMonitorService.class);
		rmiProxyFactoryBean2.setServiceUrl("rmi://"+controllerIp+":"+controllerPort+"/BatchMonitor");
		rmiProxyFactoryBean2.setRegistryClientSocketFactory(new NRMIClientSocketFactory(5000, 30000));
		rmiProxyFactoryBean2.afterPropertiesSet();
		monitorService    = (IMonitorService)   rmiProxyFactoryBean2.getObject();
	}
	
	public void destroy() {
	}
	
}
