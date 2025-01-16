/* 
 * Copyright (c) 2016. 1. 20. SK C&C Co., Ltd. All rights reserved.
 * 
 * This software is the confidential and proprietary information of SK C&C.
 * You shall not disclose such confidential information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with SK C&C.
 */
package nexcore.scheduler.agent.runner;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.exception.AgentException;

/**
 * <ul>
 * <li>업무 그룹명 : nexcore-scheduler-4</li>
 * <li>서브 업무명 : nexcore.scheduler.agent.runner</li>
 * <li>설  명 : 에이전트의 JobContext 객체로 부터 배치 프레임워크 7.0 버전용 BatchContext 객체로 변환한후 연결하는 adapter </li>
 * <li>작성일 : 2016. 1. 20.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class BatchContextAdapterForNBF7 implements IBatchContextAdapter {

	public static final String BATCH_CONTEXT_INTERFACE_CLASS = "nexcore.framework.batch.IBatchContext";
	public static final String BATCH_CONTEXT_IMPLEMENT_CLASS = "nexcore.framework.batch.BatchContext";
	
	
	/**
	 * BatchContext 객체를 만든다
	 * 의존성 제거를 위해 reflection 방식으로 한다.
	 * @param jobContext
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
    public Object convertJobContextToBatchContext(JobContext context) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // 컴파일 의존성 제거를 위해 BatchContext 만들때 reflection 으로 만든다.
    	Class batchContextClass = Class.forName(BATCH_CONTEXT_IMPLEMENT_CLASS);
    	Object batchContext = batchContextClass.newInstance();
        
        Method setBatchObjectMethod           = null;
        Method setInParametersMethod          = null;
        Method setLogMethod                   = null;
        Method setProgressValuesMethod        = null;
        Method setSingleJvmMethod             = null;
        Method setJobIdMethod                 = null;
        Method setJobInstanceIdMethod         = null;
        Method setJobExecutionIdMethod        = null;
        Method setJobGroupIdMethod            = null;
        Method setProcDateMethod              = null;
        Method setBaseDateMethod              = null;
        Method setOnDemandMethod              = null;
        Method setAttributeMethod             = null;

        try {
        	setBatchObjectMethod            = batchContextClass.getMethod("setBatchObject",                 new Class[]{Object.class});
            setInParametersMethod           = batchContextClass.getMethod("setInParameters",                new Class[]{Map.class});
            setLogMethod                    = batchContextClass.getMethod("setLogger",                      new Class[]{Log.class});
            setProgressValuesMethod         = batchContextClass.getMethod("setProgressValues",              new Class[]{long[].class});
            setSingleJvmMethod              = batchContextClass.getMethod("setSingleJvm",                   new Class[]{Boolean.TYPE});
            setJobIdMethod                  = batchContextClass.getMethod("setJobId",                       new Class[]{String.class});
            setJobInstanceIdMethod          = batchContextClass.getMethod("setJobInstanceId",               new Class[]{String.class});
            setJobExecutionIdMethod         = batchContextClass.getMethod("setJobExecutionId",              new Class[]{String.class});
            setJobGroupIdMethod             = batchContextClass.getMethod("setJobGroupId",                  new Class[]{String.class});
            setProcDateMethod               = batchContextClass.getMethod("setProcDate",                    new Class[]{String.class});
            try {
            	setBaseDateMethod               = batchContextClass.getMethod("setBaseDate",                    new Class[]{String.class});
            }catch(Throwable e) {
            	// NJF 7.1 까지만 setBaseDate 존재함. 그 이후는 제거됨.
            }
            setOnDemandMethod               = batchContextClass.getMethod("setOnDemand",                    new Class[]{Boolean.TYPE});
            setAttributeMethod              = batchContextClass.getMethod("setAttribute",                   new Class[]{String.class, Object.class});
            
            setBatchObjectMethod        .invoke(batchContext, context.getBatchObject());
            setInParametersMethod       .invoke(batchContext, context.getInParameters());
            setLogMethod                .invoke(batchContext, context.getLogger());
            setProgressValuesMethod     .invoke(batchContext, context.getProgressValues());
            setSingleJvmMethod          .invoke(batchContext, false);
            setJobIdMethod              .invoke(batchContext, context.getJobExecution().getJobId());
            setJobInstanceIdMethod      .invoke(batchContext, context.getJobExecution().getJobInstanceId());
            setJobExecutionIdMethod     .invoke(batchContext, context.getJobExecution().getJobExecutionId());
            setJobGroupIdMethod         .invoke(batchContext, context.getJobExecution().getJobGroupId());
            setProcDateMethod           .invoke(batchContext, context.getJobExecution().getProcDate());
            if (setBaseDateMethod != null) {
            	setBaseDateMethod           .invoke(batchContext, context.getJobExecution().getBaseDate());
            }
            setOnDemandMethod           .invoke(batchContext, context.getJobExecution().isOnDemand());
            setAttributeMethod          .invoke(batchContext, new Object[]{"_LOG_FILENAME_", context.getLogger().getFilename()});
        } catch (Exception e) {
            context.getLogger().warn("BatchContext object creation fail.", e);
        }
        
        return batchContext;
    }
    
    /**
     * 배치 실행 후 BatchContext 의 리턴값들을 JobContext 에 넣는다.
     */
    public void copyBatchContextReturnValuesToJobContext(Object batchContext, JobContext jobContext) {
    	Class  batchContextClass = batchContext.getClass();
    	
    	Log log = jobContext.getLogger(); 
        
    	try {
        	Method getReturnCodeMethod = batchContextClass.getMethod("getReturnCode", new Class[0]);
        	jobContext.setReturnCode((Integer)getReturnCodeMethod.invoke(batchContext, new Object[0]));
        } catch (Exception e) {
        	log.warn("Fail to copy returnCode from BatchContext to JobContext", e);
        }
        try {
        	Method getReturnValuesMethod = batchContextClass.getMethod("getReturnValues", new Class[0]);
        	
        	Object returnValues = getReturnValuesMethod.invoke(batchContext, new Object[0]);
        	if (returnValues instanceof Map) {
        		Properties p = new Properties();
        		p.putAll((Map)returnValues);
        		jobContext.setReturnValues(p);
        	}else if (returnValues instanceof Properties) {
        		jobContext.setReturnValues((Properties)returnValues);
        	}else {
        		log.warn("Fail to copy returnValues from BatchContext to JobContext. Unsupported returnValues object. "+returnValues.getClass());
        	}
        } catch (Exception e) {
        	log.warn("Fail to copy returnValues from BatchContext to JobContext", e);
        }

    }
    
	/**
	 * 강제종료 버튼을 누를 경우 호출되며 여기서 BatchContext 에 flag 전달한다. 
	 * @param jobContext
	 */
	public void transferStopForceEvent(JobContext jobContext) {
		Object batchContext = jobContext.getBatchContext();
		try {
			// BatchContext.setStopForced(true) 호출
			Method setStopForcedMethod = batchContext.getClass().getMethod("setStopForced", new Class[]{Boolean.TYPE});
			setStopForcedMethod.invoke(batchContext, true);
			
		} catch (Exception e) {
			jobContext.getLogger().warn("Fail to transfer stop event to BatchContext. ", e);
			throw new AgentException("agent.fail.stop.job", e, jobContext.getJobExecution().getJobExecutionId());
		}
	}

	/**
	 * 일시정지 버튼을 누를 경우 호출되며 여기서 BatchContext 에 flag 전달한다. 
	 * @param jobContext
	 */
	public void transferSuspendEvent(JobContext jobContext) {
		Object batchContext = jobContext.getBatchContext();
		try {
			// BatchContext.setSuspendForced(true) 호출
			Method setSuspendForcedMethod = batchContext.getClass().getMethod("setSuspendForced", new Class[]{Boolean.TYPE});
			setSuspendForcedMethod.invoke(batchContext, true);
		} catch (Exception e) {
			jobContext.getLogger().warn("Fail to transfer suspend event to BatchContext. ", e);
			throw new AgentException("agent.fail.suspend.job", e, jobContext.getJobExecution().getJobExecutionId());
		}
	}

	/**
	 * 계속실행 버튼을 누를 경우 호출되며 여기서 BatchContext 에 flag 전달한다. 
	 * @param jobContext
	 */
	public void transferResumeEvent(JobContext jobContext) {
		Object batchContext = jobContext.getBatchContext();
		try {
			// BatchContext.setResumeForced(false) 호출
			Method setSuspendForcedMethod = batchContext.getClass().getMethod("setSuspendForced", new Class[]{Boolean.TYPE});
			setSuspendForcedMethod.invoke(batchContext, false);
		} catch (Exception e) {
			jobContext.getLogger().warn("Fail to transfer resume event to BatchContext. ", e);
			throw new AgentException("agent.fail.resume.job", e, jobContext.getJobExecution().getJobExecutionId());
		}
	}

	/**
	 * 배치 프로그램의 상태를 가져온다. BatchContext 또는 getStatus() 로 읽어온다.  
	 * @param jobContext
	 * @return true if suspended or false
	 */
	public boolean isSuspendedStatus(JobContext jobContext) {
		Object batchContext = jobContext.getBatchContext();
		try {
			// BatchContext.isSuspendedStatus() 호출
			Method isSuspendedStatusMethod = batchContext.getClass().getMethod("isSuspendedStatus", new Class[0]);
			Object retval = isSuspendedStatusMethod.invoke(batchContext, new Object[0]);
			return ((Boolean)retval).booleanValue();
		} catch (Exception e) {
			jobContext.getLogger().warn("Fail to get batch status from BatchContext. ", e);
			throw new AgentException("agent.fail.getstatus.job", e, jobContext.getJobExecution().getJobExecutionId()); // Job {0} 의 실행 상태를 알 수 없습니다
		}
	}
    
}
