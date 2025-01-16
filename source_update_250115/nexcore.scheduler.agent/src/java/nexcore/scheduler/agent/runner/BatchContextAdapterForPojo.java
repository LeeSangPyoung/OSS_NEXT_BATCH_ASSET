/* 
 * Copyright (c) 2016. 1. 20. SK C&C Co., Ltd. All rights reserved.
 * 
 * This software is the confidential and proprietary information of SK C&C.
 * You shall not disclose such confidential information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with SK C&C.
 */
package nexcore.scheduler.agent.runner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : nexcore-scheduler-4</li>
 * <li>서브 업무명 : nexcore.scheduler.agent.runner</li>
 * <li>설  명 : 에이전트의 JobContext 객체로 부터 배치 프레임워크 7.0 버전용 BatchContext 객체로 변환한후 연결하는 adapter </li>
 * <li>작성일 : 2016. 2. 16.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class BatchContextAdapterForPojo implements IBatchContextAdapter {
	/**
	 * JobContext 의 값을 POJO 클래스에 넣어준다.
	 * reflection 으로 set 메소드를 찾아 넣어준다.
	 * @param jobContext
	 */
	public Object convertJobContextToBatchContext(JobContext jobContext) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		// reflection 으로 POJO 클래스의 set 메소드를 찾아 BatchContext 에 해당하는 정보를 넘겨준다.
		Object batchObject = jobContext.getBatchObject();
    	Class  batchClass  = batchObject.getClass();

    	String mNameSetInParameters    = Util.nvlBlank(jobContext.getInParameter("METHOD_setInParameters"),   "setInParameters");            
    	String mNameSetOperatorId      = Util.nvlBlank(jobContext.getInParameter("METHOD_setOperatorId"),     "setOperatorId");     
    	String mNameSetOperatorIp      = Util.nvlBlank(jobContext.getInParameter("METHOD_setOperatorIp"),     "setOperatorIp");              
    	String mNameSetOperatorType    = Util.nvlBlank(jobContext.getInParameter("METHOD_setOperatorType"),   "setOperatorType");              
    	String mNameSetOnDemand        = Util.nvlBlank(jobContext.getInParameter("METHOD_setOnDemand"),       "setOnDemand");                
    	String mNameSetLog             = Util.nvlBlank(jobContext.getInParameter("METHOD_setLog"),            "setLog");            

    	try {
    		Method m = batchClass.getMethod(mNameSetInParameters, new Class[]{Map.class});
    		m.invoke(batchObject, new Object[]{jobContext.getInParameters()});
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 되므로 에러 무시한다.
    	try {
    		Method m = batchClass.getMethod(mNameSetOperatorId, new Class[]{String.class});
    		m.invoke(batchObject, new Object[]{jobContext.getOperatorId()});
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 되므로 에러 무시한다.
    	try {
    		Method m = batchClass.getMethod(mNameSetOperatorIp, new Class[]{String.class});
    		m.invoke(batchObject, new Object[]{jobContext.getOperatorIp()});
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 되므로 에러 무시한다.
    	try {
    		Method m = batchClass.getMethod(mNameSetOperatorType, new Class[]{String.class});
    		m.invoke(batchObject, new Object[]{jobContext.getOperatorType()});
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 되므로 에러 무시한다.
    	try {
    		Method m = batchClass.getMethod(mNameSetOnDemand, new Class[]{boolean.class});
    		m.invoke(batchObject, new Object[]{jobContext.getJobExecution().isOnDemand()});
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 되므로 에러 무시한다.
    	try {
    		Method m = batchClass.getMethod(mNameSetLog, new Class[]{Log.class});
    		m.invoke(batchObject, new Object[]{jobContext.getLogger()});
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 되므로 에러 무시한다.
    	
    	setupPojoMethod(jobContext);
    	
    	return null; // BatchContext 는 없으므로 null 리턴한다.
	}
	
    /**
     * BatchContext 에 해당하는 메소드들을 reflection 으로 미리 찾아 놓는다.
     * @param jobContext
     */
    private void setupPojoMethod(JobContext jobContext) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    	if (jobContext == null || jobContext.getBatchObject() == null) {
    		return;
    	}
    	
		Object batchObject = jobContext.getBatchObject();
    	Class  batchClass  = batchObject.getClass();

    	String mNameOnStopForced       = Util.nvlBlank(jobContext.getInParameter("METHOD_onStopForced"),      "onStopForced");      
    	String mNameOnSuspend          = Util.nvlBlank(jobContext.getInParameter("METHOD_onSuspend"),         "onSuspend");                  
    	String mNameOnResume           = Util.nvlBlank(jobContext.getInParameter("METHOD_onResume"),          "onResume");          
    	String mNameIsSuspended        = Util.nvlBlank(jobContext.getInParameter("METHOD_isSuspended"),       "isSuspended");          
    	String mNameGetProgressTotal   = Util.nvlBlank(jobContext.getInParameter("METHOD_getProgressTotal"),  "getProgressTotal");           
    	String mNameGetProgressCurrent = Util.nvlBlank(jobContext.getInParameter("METHOD_getProgressCurrent"),"getProgressCurrent");
    	
    	try {
    		jobContext.setAttribute("_MethodObj_onStopForced",       batchClass.getMethod(mNameOnStopForced, new Class[0]));
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 된다.
    	try {
    		jobContext.setAttribute("_MethodObj_onSuspend",          batchClass.getMethod(mNameOnSuspend, new Class[0]));
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 된다.
    	try {
    		jobContext.setAttribute("_MethodObj_onResume",           batchClass.getMethod(mNameOnResume, new Class[0]));
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 된다.
    	try {
    		jobContext.setAttribute("_MethodObj_isSuspended",        batchClass.getMethod(mNameIsSuspended, new Class[0]));
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 된다.
    	try {
    		jobContext.setAttribute("_MethodObj_getProgressTotal",   batchClass.getMethod(mNameGetProgressTotal, new Class[0]));
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 된다.
    	try {
    		jobContext.setAttribute("_MethodObj_getProgressCurrent", batchClass.getMethod(mNameGetProgressCurrent, new Class[0]));
    	}catch(NoSuchMethodException ignore) { } // 이 메소드는 없어도 된다.
	}
	
	/**
	 * POJO 실행 종료후 POJO 배치 실행 결과 값을 다시 JobContext 에 넣어준다.
	 * 메소드 reflection 으로 찾아온다.
	 */
	public void copyBatchContextReturnValuesToJobContext(Object batchContext, JobContext jobContext) {
		if (jobContext == null || jobContext.getBatchObject() == null) {
			// 이런 경우는 없을 것이지만, 
			return ;
		}
		
		Log log = jobContext.getLogger();
		
		Object batchObject = jobContext.getBatchObject();
		Class  batchClass  = batchObject.getClass();

    	String mNameGetReturnValues    = Util.nvlBlank(jobContext.getInParameter("METHOD_getReturnValues"),   "getReturnValues");            
    	String mNameGetReturnCode      = Util.nvlBlank(jobContext.getInParameter("METHOD_getReturnCode"),     "getReturnCode");            

		// 리턴 코드 세팅
    	try {
    		Method getReturnCodeMethod = batchClass.getMethod(mNameGetReturnCode, new Class[0]);
    		Integer returnCode = (Integer)getReturnCodeMethod.invoke(batchObject, new Object[0]);
    		jobContext.setReturnCode(returnCode);
    		log.info(batchObject+" returnCode="+returnCode);
    	}catch(Exception ignore) { // 이 메소드 없다고 에러를 낼 수 는 없다. 로깅하고 skip 한다.
    		log.warn(batchObject+" has no '"+mNameGetReturnCode+"' method.");
    	}

    	try {
    		Method getReturnValuesMethod = batchClass.getMethod(mNameGetReturnValues, new Class[0]);
    		Properties returnValues = (Properties)getReturnValuesMethod.invoke(batchObject, new Object[0]);
    		jobContext.setReturnValues(returnValues);
    		log.info(batchObject+" returnValues="+returnValues);
    	}catch(Exception ignore) { // 이 메소드 없다고 에러를 낼 수 는 없다. 로깅하고 skip 한다.
    		log.warn(batchObject+" has no '"+mNameGetReturnValues+"' method.");
    	} // 이 메소드는 없어도 된다.

		// 최종 progress 세팅
		fillProgressStatusForPojo(jobContext);
	}
	
	/**
	 * POJO 배치 호출인 경우 progress 정보를 메소드 호출로 획득 한 후 
	 * JobExecution 에 채워 넣는다.
	 * @param context
	 */
	public void fillProgressStatusForPojo(JobContext jobContext) {
		if (jobContext == null) {
			// 아직 초기화 안된상태..(시작하자마자) 그냥 pass 한다.
			return;
		}
		JobExecution jobexe = jobContext.getJobExecution();

		Method prgsTotalMethod   = (Method) jobContext.getAttribute("_MethodObj_getProgressTotal");
		if (prgsTotalMethod != null) {
			try {
				jobexe.setProgressTotal((Long)prgsTotalMethod.invoke(jobContext.getBatchObject(), new Object[0]));
			}catch(Exception ignore) {}
		}
		Method prgsCurrentMethod = (Method) jobContext.getAttribute("_MethodObj_getProgressCurrent");
		if (prgsCurrentMethod != null) {
			try {
				jobexe.setProgressCurrent((Long)prgsCurrentMethod.invoke(jobContext.getBatchObject(), new Object[0]));
			}catch(Exception ignore) {}
		}
	}

	/**
	 * 강제종료 버튼을 누를 경우 호출되며 여기서 BatchContext 에 flag 전달한다. 
	 * @param jobContext
	 */
	public void transferStopForceEvent(JobContext jobContext) {
		// POJO 방식인 경우는 onStopForced 메소드를 호출한다.
		Method onStopForcedMethod = (Method)jobContext.getAttribute("_MethodObj_onStopForced");
		if (onStopForcedMethod != null) {
			try {
				onStopForcedMethod.invoke(jobContext.getBatchObject(), new Object[0]);
			}catch(Throwable e) {
				jobContext.getLogger().error("stop error", e);
			}
		}
	}

	/**
	 * 일시정지 버튼을 누를 경우 호출되며 여기서 BatchContext 에 flag 전달한다.
	 * 실제 suspend 는 배치실행쓰레드가 스스로 해야하며,
	 * @param jobContext
	 */
	public void transferSuspendEvent(JobContext jobContext) {
		// POJO 방식인 경우는 onSuspend 메소드를 호출한다.
		Method onSuspendMethod = (Method)jobContext.getAttribute("_MethodObj_onSuspend");
		if (onSuspendMethod != null) {
			try {
				onSuspendMethod.invoke(jobContext.getBatchObject(), new Object[0]);
			}catch(Throwable e) {
				jobContext.getLogger().error("suspend error", e);
			}
		}
	}

	/**
	 * 계속실행 버튼을 누를 경우 호출되며 여기서 BatchContext 에 flag 전달한다. 
	 * @param jobContext
	 */
	public void transferResumeEvent(JobContext jobContext) {
		Method onResumeMethod = (Method)jobContext.getAttribute("_MethodObj_onResume");
		if (onResumeMethod != null) {
			try {
				onResumeMethod.invoke(jobContext.getBatchObject(), new Object[0]);
			}catch(Throwable e) {
				jobContext.getLogger().error("resume error", e);
			}
		}
	}

	/**
	 * 일시정지 상태 여부를 리턴함   
	 * @param jobContext
	 * @return true if suspended or false
	 */
	public boolean isSuspendedStatus(JobContext jobContext) {
		Method isSuspendedMethod = (Method)jobContext.getAttribute("_MethodObj_isSuspended");
		if (isSuspendedMethod != null) {
			try {
				return (Boolean)isSuspendedMethod.invoke(jobContext.getBatchObject(), new Object[0]);
			}catch(Throwable e) {
				jobContext.getLogger().error("isSuspended method error", e);
			}
		}
		return false;
	}
    
}
