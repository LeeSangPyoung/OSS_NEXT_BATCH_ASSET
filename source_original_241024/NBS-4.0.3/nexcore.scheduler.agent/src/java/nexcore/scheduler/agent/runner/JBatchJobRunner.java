package nexcore.scheduler.agent.runner;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.exception.AgentException;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : NEXCORE Batch Framework (7.0 이상) 기반의 배치 app 를 실행함        <br>
 *              7.0 부터는 EjbjobRunner 를 JBatchJobRunner 로 변경함                <br>
 *              배치 프레임워크에서는 EJB 를 걷어내고, UserTransaction 을 직접 lookup 하는 방식으로 함    <br>
 *              txBegin() 안에서 lookup 일어남                                      <br>
 *               
 * <li>작성일 : 2016. 2. 17.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 * 
 * JBatchJobRunner 는 에이전트에서 직접 배치 클래스를 로딩하지 않고 
 * BatchFacade 를 호출하고 그 놈이 실제 배치 클래스를 로딩하도록 한다.
 * 배치 클래스 로딩을 배치 FWK 에서 일관되게 하기 위함이다.
 * SingleJvm, Local배치 테스트, Non-NBS WAS 배치 실행등등 모든 경우에 배치 FWK 에서 배치 클래스 로딩하는것이 맞다
 *  
 */
public class JBatchJobRunner extends AbsJavaThreadRunJobRunner {
	
	/**
	 * 강제종료, 일시정지 등의 기능을 사용하지 않을 경우 true 로함. 기본값 false.
	 * 배치가 DB Procedure 만 호출하는 경우는 프로시저 특성상 APP 에서 강제종료, 일실정지를 할 수 없음. 착오를 방지하기 위해 아예 기능을 꺼버림
	 * 이전 버전의 EjbStartOnlyJobRunner 타입을 이 속성으로 대체함
	 */
	private boolean startOnly;
	private String  batchFacadeBeanId = "nc.batch.fwk.BatchFacade";
	
	private Object batchFacade; 
	private Method batchFacadeInvokeMethod;
	
	public JBatchJobRunner() {
	}

	public boolean isStartOnly() {
		return startOnly;
	}

	public void setStartOnly(boolean startOnly) {
		this.startOnly = startOnly;
	}

	public String getBatchFacadeBeanId() {
		return batchFacadeBeanId;
	}
	
	public void setBatchFacadeBeanId(String batchFacadeBeanId) {
		this.batchFacadeBeanId = batchFacadeBeanId;
	}
	
	public void init() {
		// JBatchJobRunner 는 NBF7 기반의 배치 프로그램을 실행시키므로 BatchContextAdapterForNBF7 를 생성해준다.
		super.setBatchContextAdapter(new BatchContextAdapterForNBF7());
	}
	
	private void setupBatchFacadeReference() {
		if (batchFacadeInvokeMethod == null) {
			/*
			 * 스케줄러가 아닌 프레임워크 영역의 BeanRegistry 를 사용해야한다.
			 * 의존성을 제거하기 위해 reflection 으로 한다.
			 */
			Method registryLookupMethod = null;
			try {
				Class c = Class.forName("nexcore.framework.core.ioc.BeanRegistry");
				log.info("Core BeanRegisty : "+c);
				registryLookupMethod = c.getMethod("lookup", String.class);
			} catch (Exception e) {
				throw new RuntimeException("BeanRegistry class not found. Check CLASSPATH", e);
			}
	
			// 배치 실행 Facade 찾기. BatchFacade.invoke() 메소드를 찾아놓는다.
			try {
				batchFacade = registryLookupMethod.invoke(null, batchFacadeBeanId);
			} catch (Exception e) {
				throw new RuntimeException("BatchFacade lookup fail. check nexcore-batch.xml", e);
			}
			Method[] methods = batchFacade.getClass().getDeclaredMethods();
			for (Method method : methods) {
				if ("invoke".equals(method.getName())) {
					batchFacadeInvokeMethod = method; // 찾았다
					break;
				}
			}
		}
	}
	
	public void destroy() {
	}

	public void stop(String jobExecutionId) {
		if (startOnly) {
			throw new AgentException("agent.thistype.cannot.stop", "STOP");
		}else {
			super.stop(jobExecutionId);
		}
	}
	
	public void suspend(String jobExecutionId) {
		if (startOnly) {
			throw new AgentException("agent.thistype.cannot.suspend", "SUSPEND");
		}
		super.suspend(jobExecutionId);
	}
	
	public void resume(String jobExecutionId) {
		if (startOnly) {
			throw new AgentException("agent.thistype.cannot.resume", "RESUME");
		}
		super.resume(jobExecutionId);
	}
	
	/**
	 * 배치 호출 메인
	 * @param jobContext
	 */
	public void invokeBatchMain(JobContext jobContext) throws Exception {
        Log log = jobContext.getLogger();

//        if (batchFacadeInvokeMethod == null) {
        setupBatchFacadeReference();
//        }
        
        jobContext.setBatchContextAdapter(batchContextAdapter);
    	
        // ■■■ 1. BatchContext 객체를 만든다.
        Object batchContext = batchContextAdapter.convertJobContextToBatchContext(jobContext);
        jobContext.setBatchContext(batchContext);
        
        // ■■■ 2. 메인 메소드 호출. 배치 FWK 의 BatchFacade 를 reflection 방식으로 호출한다.  
        try {
        	log.info("Invoking JBatch main method.");
        	batchFacadeInvokeMethod.invoke(batchFacade, batchContext);
        }finally {
        	// ■■■ 3. 결과 값 세팅한다. 에러가 발생하더라도 지금 까지 담아뒀던 값들은 JobContext 로 넣는다.
        	batchContextAdapter.copyBatchContextReturnValuesToJobContext(batchContext, jobContext);
        }
	}

}
