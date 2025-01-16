/* 
 * Copyright (c) 2006-2010 SK C&C Co., Ltd. All rights reserved. 
 * 
 * This software is the confidential and proprietary information of SK C&C. 
 * You shall not disclose such confidential information and shall use it 
 * only in accordance with the terms of the license agreement you entered into 
 * with SK C&C. 
 */ 

package nexcore.scheduler.agent.runner;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.IJobClassLoadManager;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : </li>
 * <li>설  명 : 에이전트에서 실행할 Job 클래스를 loading 하고 관리한다.</li>
 * <li>작성일 : 2010. 4. 1.</li>
 * <li>         2016. 1. 15.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
// 배치 Class 는 pooling 하지 않고 실행시마다 로드한다.
public class JobClassLoadManager implements IJobClassLoadManager {
    private String  classBaseDirectory;
    private Map<String, Object> batchObjectPool = new HashMap<String, Object>(); 
    private Log log;
    
    public void init() {
    	log = LogManager.getAgentLog();
        if (!Util.isBlank(classBaseDirectory)) {
            classBaseDirectory = classBaseDirectory.trim();
            log.info("[JobClassLoadManager] job class base directory : "+classBaseDirectory);
        }else {
            // 3.8 부터는 classBaseDirectory 미설정시 상위(기본) 클래스로더로 부터 로딩한다. 2015-10-19.
            log.info("[JobClassLoadManager] job class base directory is null. Using default ClassLoader !!! ");
        }
    }

    public void destroy() {
    }
    
    
    // 배치 클래스를 로드해서 init() 메소드를 호출함
    public Object loadAndInitClasses(String fullClassName ) {
        Class  jobClass  = null;
        Object jobClassObject = null;

        try {
            if (Util.isBlank(classBaseDirectory)) { // 이 경우 기본 클래스로더로 부터 클래스를 읽어 온다.
                jobClass  = Class.forName(fullClassName);
            }else {
                jobClass  = Class.forName(fullClassName, false, new LocalFileClassLoader(classBaseDirectory.split(",")));
            }
            jobClassObject = jobClass.newInstance();

            batchObjectPool.put(fullClassName, jobClassObject);
        }catch(Exception e) {
            Util.logError(log, MSG.get("agent.fail.load.component", fullClassName), e);
            throw new AgentException("agent.fail.load.component", e, fullClassName);
        }
        return jobClassObject;
    }

    public Object[] listAllClasses() {
        Object[] retval = null;
        synchronized(batchObjectPool) { // 이렇게 해야지 concurrentmodificationexception 이 발생하지 않는다.
        	retval = (Object[])batchObjectPool.values().toArray(new Object[0]);
        }
        
        return retval;
    }

	public String getClassBaseDirectory() {
		return classBaseDirectory;
	}

	public void setClassBaseDirectory(String classBaseDirectory) {
		this.classBaseDirectory = classBaseDirectory;
	}

}
