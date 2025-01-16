package nexcore.scheduler.agent.runner;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.IJobClassLoadManager;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Pojo 방식으로 Job 을 실행시킴. </li>
 * <li>작성일 : 2016. 3. 7.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class PojoJobRunner extends AbsJavaThreadRunJobRunner {
	private IJobClassLoadManager jobClassLoadManager;  
	
	public PojoJobRunner() {
	}

	public void init() {
		// PojoJobRunner 는 일반 POJO 기반의 배치 프로그램을 실행시키므로 BatchContextAdapterForPOJO 를 생성해준다. 
		super.setBatchContextAdapter(new BatchContextAdapterForPojo());
	}
	
	public void destroy() {
	}

	public IJobClassLoadManager getJobClassLoadManager() {
		return jobClassLoadManager;
	}

	public void setJobClassLoadManager(IJobClassLoadManager jobClassLoadManager) {
		this.jobClassLoadManager = jobClassLoadManager;
	}

	/**
	 * POJO 타입 배치 호출 메인
	 * @param jobContext
	 */
	public void invokeBatchMain(JobContext jobContext) throws Exception {
        Log log = jobContext.getLogger();

        if (log.isDebugEnabled()) {
        	log.debug("Loading batch class. "+jobContext.getJobExecution().getComponentName());
        }
        
		Object batchObject = null;
		if (jobClassLoadManager == null) {
			// SLEEP, FILEWATCHER 등 내장 잡들은 기본 클래스로더에서 읽는다.
			Class c = Class.forName(jobContext.getJobExecution().getComponentName());
			batchObject = c.newInstance();
		}else {
			batchObject = jobClassLoadManager.loadAndInitClasses(jobContext.getJobExecution().getComponentName());
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Loading batch class ok. "+batchObject);
		}
		
		/* 
		 * 2013-10-26, 센터컷/CBATCH/DBProcedure Type등은 COMPONENT_NAME 이 강제로 내부 클래스로 설정된다.
		 * 여기서 클래스 로드를 마치고 나면 다시 원래의 값으로 복원한다. 
		 * (모니터링시 COMPONENT_NAME 을 보기 위해 ) 
		 */
		if (!Util.isBlank(jobContext.getInParameter("COMPONENT_NAME")) && 
			!Util.equals (jobContext.getJobExecution().getComponentName(), jobContext.getInParameter("COMPONENT_NAME"))) {
			jobContext.getJobExecution().setComponentName(jobContext.getInParameter("COMPONENT_NAME"));
		}
		jobContext.setBatchObject(batchObject);

		// JobType 별로 BatchContextAdapter 가 다르므로 여기서 넣어준다. 
		jobContext.setBatchContextAdapter(batchContextAdapter);
		
		log.info("Executing POJO invoker");

    	// ■■■ 1. 배치프로그램의 내부 메인 메소드 찾기  
        /*
         * 메인 메소드찾기. 
         * 파라미터로 "METHOD_execute" 의 값으로 지정된 메소드를 찾는다.
         * 없으면 기본값 execute 를 찾는다. 
         * execute 의 argument 는 없다. 
         */
    	String mNameExecute = Util.nvlBlank(jobContext.getInParameter("METHOD_execute"), "execute");           
    	Method executeMethod = null;
    	boolean existJobContextArgument = false; // execute 의 Argument 로 JobContext 가 있는지 없는지 여부. SleepJob 은 JobContext 가 있다.
    	try {
    		log.info("Finding POJO main method. "+mNameExecute);
    		executeMethod = batchObject.getClass().getMethod(mNameExecute);
    		log.info("Main method : "+executeMethod.toString());
		}catch(Exception e) {
			// execute(JobContext context) 와 같이 첫번째 파라미터를 받는 경우
			try {
				executeMethod = batchObject.getClass().getMethod(mNameExecute, JobContext.class);
				existJobContextArgument = true;
				log.info("Main method : "+executeMethod.toString());
			}catch(Exception e2) {
				throw new RuntimeException("Batch class MAIN method("+mNameExecute+") not found.");
			}			
		}
    	
		// ■■■ 2. POJO 는 BatchContext 가 없고 set 메소드로 POJO 객체에 넣어준다.
    	if (!existJobContextArgument) { // execute(JobContext execute) 인 경우는 BatchContextAdapter 가 필요없이 그대로 넘겨준다.
    		batchContextAdapter.convertJobContextToBatchContext(jobContext);
    	}
    	
		// ■■■ 3. 메인 메소드 호출 
		try {
			log.info("Invoking POJO main method : "+executeMethod.toString());
			if (existJobContextArgument) {
				executeMethod.invoke(batchObject, new Object[]{jobContext});  // execute(JobContext) 호출
			}else {
				executeMethod.invoke(batchObject, new Object[0]); // execute() 호출
			}
		}finally {
			// ■■■ 4. 결과 값 세팅한다. 에러가 발생하더라도 지금까지 담아뒀던 값들은 JobContext 로 세트한다.
			if (!existJobContextArgument) {
				batchContextAdapter.copyBatchContextReturnValuesToJobContext(null, jobContext);
			}
		}
	}
}
