/*
 * Copyright (c) 2006 SK C&C. All rights reserved.
 * 
 * This software is the confidential and proprietary information of SK C&C. You
 * shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with SK
 * C&C.
 */

package nexcore.scheduler.log;

import org.apache.commons.logging.LogFactory;

/**
 * <ul>
 * <li>업무 그룹명 : 배치 스케줄러 </li>
 * <li>서브 업무명 : 로거</li>
 * <li>설  명 : 배치 스케줄러 내부 로깅을 담당</li>
 * <li>작성일 : 2016. 4. 18.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class LogManager {

    public static final String SCHEDULER_LOG  = "scheduler";
    public static final String AGENT_LOG      = "scheduler_agent";

    public static org.apache.commons.logging.Log getSchedulerLog() {
    	return LogFactory.getLog(SCHEDULER_LOG);
    }
    
    public static org.apache.commons.logging.Log getAgentLog() {
    	return LogFactory.getLog(AGENT_LOG);
    }

    public static org.apache.commons.logging.Log getLog(String category) {
    	return LogFactory.getLog(category);
    }
    
}
