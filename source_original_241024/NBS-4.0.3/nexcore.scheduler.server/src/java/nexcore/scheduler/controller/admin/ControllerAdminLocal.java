package nexcore.scheduler.controller.admin;

import nexcore.scheduler.entity.IControllerService;
import nexcore.scheduler.entity.IMonitorService;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.ioc.BeanRegistry;

/**
 *  
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 같은 JVM 내에서는 RMI 통신하지 않고 직접 local 호출한다.</li>
 * <li>작성일 : 2010. 8. 30.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ControllerAdminLocal extends ControllerAdmin00 {
	public ControllerAdminLocal() {
		init();
	}
	
	public void init() {
		try {
			controllerService = (IControllerService)BeanRegistry.lookup("nbs.controller.ControllerService");
			monitorService    = (IMonitorService)   BeanRegistry.lookup("nbs.monitor.MonitorService");
		}catch(Exception e) {
			throw new SchedulerException("com.error.occurred.while", e, "Init Service" );
		}
	}
	
	public void destroy() {
	}
	
}
