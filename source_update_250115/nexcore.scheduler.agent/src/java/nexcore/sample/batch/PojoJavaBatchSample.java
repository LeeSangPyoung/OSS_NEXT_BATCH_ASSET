package nexcore.sample.batch;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어 </li>
 * <li>설  명 : NEXCORE 의존성이 없는 POJO 배치 샘플 </li>
 * <li>작성일 : 2012. 5. 9.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class PojoJavaBatchSample {

    protected Map<String, String>   inParameters;        // 입력 파라미터
    protected long                  progressTotal;       // 진행률 total
    protected long                  progressCurrent;     // 진행률 current
    protected String                operatorId;          // 실행자 ID
    protected String                operatorIp;          // 실행자 IP
    protected String                operatorType;        // 실행자 구분. (USR, SCH, OND)
    protected String                auditId;             // 실행자 Audit ID
    protected boolean               isOnDemand;          // 온디멘드 인지?
    protected Log                   log;                 // 배치 Job 로그 파일에 저장하는 로거
    
    protected Properties            returnValues = new Properties(); // 결과 값. Key, Value 쌍
    
    protected boolean               stopForced;          // stop 버튼이 눌러졌는지?
    protected boolean               suspendForced;       // suspend 클릭시 true가 되고, resume 클릭시 false 가 된다. 
    protected boolean               suspended;           // suspend 클릭하여 suspend 작업이 정상적으로 이루어 지면 true가 됨 
    
    protected Object                suspendLock = new Object(); // suspend 에서 사용할 Lock 오브젝트 
    protected Thread                thisThread;
    
    /*
     * 아래 메소드는 배치 에이전트와 정보 교환을 위한 메소드.
     * get*** 메소드는 배치 에이전트가 POJO 로 부터 정보 획들을 위한 메소드이고
     * set*** 메소드는 배치 에이전트가 메인 메소드 시작전에 POJO 로 정보 전달을 위해 호출하는 메소드.
     * on*** 메소드는 이벤트 발생시 그 사실을 POJO 로 전달하기 위한 메소드. 
     */
    
    /**
     * 메인 메소드 실행 전에 입력 파라미터 설정을 위해 배치 에이전트에서 이 메소드를 호출함
     * @param inParameters
     */
    public void setInParameters(Map<String, String> inParameters) {
        this.inParameters = inParameters;
    }

    /**
     * 메인 메소드 실행 전에 Operator ID 설정을 위해 배치 에이전트에서 이 메소드를 호출함
     * @param operatorId
     */
    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    /**
     * 메인 메소드 실행 전에 Operator IP 설정을 위해 배치 에이전트에서 이 메소드를 호출함
     * @param operatorIp
     */
    public void setOperatorIp(String operatorIp) {
        this.operatorIp = operatorIp;
    }

    /**
     * 메인 메소드 실행 전에 Operator IP 설정을 위해 배치 에이전트에서 이 메소드를 호출함
     * @param operatorType
     */
    public void setOperatorType(String operatorType) {
        this.operatorType = operatorType;
    }
    
    /**
     * 메인 메소드 실행 전에 Audit ID 설정을 위해 배치 에이전트에서 이 메소드를 호출함
     * @param auditId
     */
    public void setAuditId(String auditId) {
        this.auditId = auditId;
    }

    /**
     * 메인 메소드 실행 전에 온디멘드 여부설정을 위해 배치 에이전트에서 이 메소드를 호출함
     * @param isOnDemand
     */
    public void setOnDemand(boolean isOnDemand) {
        this.isOnDemand = isOnDemand;
    }

    /**
     * 메인 메소드 실행 전에 로거 설정을 위해 배치 에이전트에서 이 메소드를 호출함
     * @param log
     */
    public void setLog(Log log) {
        this.log = log;
    }
    
    /**
     * 메인 메소드 수행 중에 현재 진행 상태 조회를 위해 수시로 이 메소드를 호출하여 ProgressTotal 값을 획득함
     * @return 전체 처리 건수
     */
    public long getProgressTotal() {
        return progressTotal;
    }

    /**
     * 메인 메소드 수행 중에 현재 진행 상태 조회를 위해 수시로 이 메소드를 호출하여 ProgressCurrent 값을 획득함
     * @return 현재 처리 건수
     */
    public long getProgressCurrent() {
        return progressCurrent;
    }

    /**
     * 일시정지 상태인지 여부
     * (스케줄러에서 일시정지 버튼을 누른 상태)
     * @return
     */
    public boolean isSuspended() {
		return suspended;
	}
    
    /**
     * 메인 메소드 완료 후에 처리 리턴값을 확인하기 위해 배치 에이전트에서 이 메소드를 호출하여 리턴 값을 확인함.
     * @return String 타입으로 구성됨 결과값 Properties 
     */
    public Properties getReturnValues() {
        return returnValues;
    }
    
    /**
     * 강제종료 버튼을 누를때 이 메소드 호출됨
     */
    public void onStopForced() {
        stopForced = true;
        thisThread.interrupt();
    }
    
    /**
     * 일시정지 버튼을 누를때 이 메소드 호출됨
     */
    public void onSuspend() {
        suspendForced = true;
        // 배치 메인 스레드가 wait() 상태에 정상적으로 들어갈때까지 기다린다.
        
        log.info("onSuspend invoked.");
        while (!suspended) {
            try {
            	log.info("suspended = "+suspended);
                Thread.sleep(1000); // 1초마다 상태 체크함. 이 스레드는 suspend 명령을 받은 스레드.
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                break;
            } 
        }
        log.info("suspended = "+suspended);
    }

    /**
     * 계속실행 버튼을 누를때 이 메소드 호출됨
     */
    public void onResume() {
        suspendForced = false;

        log.info("onResume invoked.");
        while (suspended) {
            try {
                Thread.sleep(1000); // 1초마다 상태 체크함. 이 스레드를 suspend 명령을 받은 스레드.
                synchronized (suspendLock) { /* resume 버튼 클릭시 notify 하여 계속 실행하도록 함 */
                    suspendLock.notifyAll();
                }
                log.info("suspended = "+suspended);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                break;
            } 
        }
        log.info("suspended = "+suspended);
    }

    public PojoJavaBatchSample() {
    }

    /**
     * 일시정지나, 강제 종료 상태 인지 체크함. loop 안에서 이 메소드 한번 호출함
     * 이 메소드는 배치 parent 클래스에 선언하고 일반 배치 app 들에서는 사용만 하도록 함
     */
    protected void checkSuspendAndStop() {
        /* 강제종료 클릭 여부 체크하여 종료 시킴 */
        if (stopForced) {
            throw new RuntimeException("강제종료됨");
        }
        log.info("checkSuspendAndStop() suspended = "+suspended);
        
        /* 일시정지 클릭 여부 체크하여 wait 시킴 */
        if (suspendForced) {
            suspended = true;
            synchronized(suspendLock) {
                try {
                    suspendLock.wait();
                } catch (InterruptedException ignore) {
                } finally {
                    suspended = false;
                }
            }
        }

    }
    
    /**
     * 배치 처리 메인 메소드 
     */
    public void execute() throws Exception {
        thisThread = Thread.currentThread();
        
        String jobId = inParameters.get("JOB_ID");
        String jobInsId = inParameters.get("JOB_INS_ID");
        String jobExeId = inParameters.get("JOB_EXE_ID");
        String procDate = inParameters.get("PROC_DATE");
        String baseDate = inParameters.get("BASE_DATE");
        String myDate   = inParameters.get("MY_DATE");
        String makeError= inParameters.get("MAKE_ERROR");
        
        int    totalCnt = Integer.parseInt(inParameters.get("TOTAL_CNT"));
        
        log.info("JOB ID        : "+jobId);
        log.info("JOB INS ID    : "+jobInsId);
        log.info("JOB EXE ID    : "+jobExeId);
        log.info("PROC_DATE     : "+procDate);
        log.info("BASE_DATE     : "+baseDate);
        log.info("Operator ID   : "+operatorId);
        log.info("Operator IP   : "+operatorIp);
        log.info("Operator Type : "+operatorType);
        log.info("Audit ID      : "+auditId);
        log.info("온디멘드 여부 : "+isOnDemand);
        log.info("MY_DATE       : "+myDate);
        log.info("전체 건수     : "+totalCnt);
        
        progressTotal = totalCnt;
        try {
            for (int i=1; i<=totalCnt; i++) {
                checkSuspendAndStop(); // 일시정지, 강제종료 여부를 체크함.
                progressCurrent = i;
                log.info(i+" 건 처리.");
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException ignore) {
                }
                
                /*
                 * 테스트를 위해 에러 유발 
                 */
                if (i > 100 && "true".equalsIgnoreCase(makeError)) {
                    throw new Exception("테스트 에러 유발 ");
                }
            }
        }catch (Throwable e) {
            throw new Exception("배치 처리 에러", e);
        }
        
        returnValues.setProperty("TEST_VAL", jobId+"_"+new Date()); // 테스트용으로 리턴값을 "JOB_ID + 시각" 으로 함
        
    }


}

