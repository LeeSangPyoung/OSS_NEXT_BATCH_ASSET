/* 
 * Copyright (c) 2015. 10. 19. SK C&C Co., Ltd. All rights reserved.
 * 
 * This software is the confidential and proprietary information of SK C&C.
 * You shall not disclose such confidential information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with SK C&C.
 */
package nexcore.scheduler.agent.startup;

import nexcore.scheduler.ioc.BeanRegistry;
import nexcore.scheduler.log.LogManager;

/**
 * <ul>
 * <li>업무 그룹명 : nexcore-scheduler-4</li>
 * <li>서브 업무명 : 에이전트</li>
 * <li>설  명 : 셧다운시 호출되는 Helper </li>
 * <li>작성일 : 2015. 10. 19.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ShutdownHelper {

    public static void destroy() {
        try {
            BeanRegistry.destroy();
        }catch(Throwable e) {
            LogManager.getAgentLog().warn(e.toString());
        }
    }
    
}
