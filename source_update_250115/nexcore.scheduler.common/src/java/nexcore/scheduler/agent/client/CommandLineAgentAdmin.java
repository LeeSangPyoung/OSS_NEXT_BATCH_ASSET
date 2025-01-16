package nexcore.scheduler.agent.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexcore.scheduler.entity.IAgentService;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.util.NRMIClientSocketFactory;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 배치 스케줄러 </li>
 * <li>서브 업무명 : 베이스 모듈</li>
 * <li>설  명 : 스케줄러 없이 에이전트에 connect 하여 Job 실행할 수 있는 client 모듈. </li>
 * <li>작성일 : 2013.  5. 20.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class CommandLineAgentAdmin {
	protected   static String              agentIp;
	protected   static int                 agentPort;
	protected   static String              targetSystemId;
	
	protected	static String				localIp;
	
	protected	static IAgentService	    agentService;
	protected	static RmiProxyFactoryBean	rmiProxyFactoryBean;
	
	private static void connect() {
		RmiProxyFactoryBean rmiProxyFactory = new RmiProxyFactoryBean();
		rmiProxyFactory.setServiceUrl("rmi://"+agentIp+":"+agentPort+"/BatchAgent");
		rmiProxyFactory.setServiceInterface(IAgentService.class);
		rmiProxyFactory.setRefreshStubOnConnectFailure(true);
		rmiProxyFactory.setLookupStubOnStartup(false);
		rmiProxyFactory.setRegistryClientSocketFactory(new NRMIClientSocketFactory(5000, 30000));
		rmiProxyFactory.afterPropertiesSet();
		
		// Set Service Object
		agentService = (IAgentService)rmiProxyFactory.getObject();
		try {
			localIp = InetAddress.getLocalHost().getHostAddress();
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

	private static void p(String s) {
		System.out.println(s);
	}
	
	private static void printMainMenu() {
		p(               "");
		p(               "**********************************************");
		p(               "*          Batch agent Admin Console         *");
		p(               "**********************************************");
		p( String.format("*  Target Agent System ID : %15s  *", targetSystemId));
		p( String.format("*  Target Agent IP        : %15s  *", agentIp));
		p( String.format("*  Target Agent Port      : %15d  *", agentPort));
		p(               "**********************************************");
		p(               "*                                            *");
		p(               "*        1. View running applications        *");
		p(               "*        2. Stop application                 *");
		p(               "*        3. Exit                             *");
		p(               "*                                            *");
		p(               "**********************************************");
	}
	
	/**
	 * Job 목록 조회
	 * @return
	 */
	private static Map<Integer, JobExecution> listJobExe() {
		List<JobExecution> jobexeList = agentService.getRunningJobExecutions();
		Map<Integer, JobExecution> map = new HashMap<Integer, JobExecution>();
		p("");
		p(              "===== Running Batch Applications =============================");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
		int i=0;
		for (JobExecution jobexe : jobexeList) {
			i++;
			p(String.format("[%d] [%s] [%s] [%s] [%s] (%d/%d)", 
				i, jobexe.getJobInstanceId(), sdf.format(new Date(jobexe.getStartTime())), jobexe.getComponentName(), jobexe.getDescription(), jobexe.getProgressCurrent(), jobexe.getProgressTotal()));
			map.put(i, jobexe);
		}
		p(String.format("===== Total : %3d ============================================", i));
		p("");
		return map;
	}

	/**
	 * Job 상세 조회
	 */
	private static void viewJob() {
		try {
			while(true) {
				Map<Integer, JobExecution> jobexeMap = listJobExe();
				String line = inputKey("Select # for detail, Enter for refresh, 0 for return >");
				if (line==null || line.trim().length()==0) continue; // 그냥 엔터치면 새로고침됨.
				if (line.equals("0")) break;
				int selectedNo = Integer.parseInt(line);
				
				JobExecution jobexe = jobexeMap.get(selectedNo);
				JobExecution jobexe2 = agentService.getJobExecution(jobexe.getJobExecutionId());
				
				p("==============================================================");
				p(" JOB ID     : "+jobexe2.getJobId());
				p(" JOB INS ID : "+jobexe2.getJobInstanceId());
				p(" JOB EXE ID : "+jobexe2.getJobExecutionId());
				p(" GROUP ID   : "+jobexe2.getJobGroupId());
				p(" JOB TYPE   : "+jobexe2.getJobType());
				p(" JOB DESC   : "+jobexe2.getDescription());
				p(" COMPONENT  : "+jobexe2.getComponentName());
				p(" JOB STATE  : "+jobexe2.getStateString());
				p(" PROC_DATE  : "+jobexe2.getProcDate());
				p(" BASE_DATE  : "+jobexe2.getBaseDate());
				p(" LOG LEVEL  : "+jobexe2.getLogLevel());
				p(" OPER ID    : "+jobexe2.getOperatorId());
				p(" OPER TYPE  : "+jobexe2.getOperatorType());
				p(" OPER IP    : "+jobexe2.getOperatorIp());
				p(" START TIME : "+new Date(jobexe2.getStartTime()));
				p(" RUN TIME   : "+(System.currentTimeMillis() - jobexe2.getStartTime())+" ms");
				p(" Parameters > ");
				Map<String, String> params = jobexe2.getInParameters();
				for (Map.Entry<String, String> param : params.entrySet()) {
					p("              "+param.getKey()+" : "+param.getValue());
				}
				p("==============================================================");		
				inputKey("Enter to return >");
			}
			
		}catch(Exception e) {
			return;
		}
	}

	/**
	 * Job 강제 종료
	 */
	private static void stopJob() {
		try {
			while(true) {
				Map<Integer, JobExecution> jobexeMap = listJobExe();
				String line = inputKey("Select # for stop, Enter for refresh, 0 for return >");
				if (line==null || line.trim().length()==0) continue; // 그냥 엔터치면 새로고침됨.
				if (line.equals("0")) break;
				int selectedNo = Integer.parseInt(line);
				
				JobExecution jobexe = jobexeMap.get(selectedNo);
				agentService.stop(jobexe.getJobExecutionId());
			}
			
		}catch(Exception e) {
			return;
		}
	}
	
	/**
	 * command line 으로부터 Key 입력 받기.
	 * @return
	 */
	private static String inputKey(String inputMsg) {
		System.out.print(inputMsg);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(System.in));
			String line = in.readLine();
			return line;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 예) BatchCallToAgentMain -IP=127.0.0.1 -PORT=8124 -CALLER=CLIENT1 -JOBID=JOB01 -TIMEOUT=60 -PARAM=A=10 -PARAM=B=30 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		for (String arg : args) {
			if (arg.startsWith("-IP=")) {
				agentIp = getValue(arg);
			}else if (arg.startsWith("-PORT=")) {
				agentPort = Integer.parseInt(getValue(arg));
			}
		}
		
		connect();
		targetSystemId = agentService.getSystemId();
		while(true) {
			printMainMenu();
			
			String line = inputKey("Select Menu >");
			if (line == null || line.equals("3")) {
				break;
			}
			if (line.equals("1")) {
				viewJob();
			}else if (line.equals("2")) {
				stopJob();
			}
			
			
		}
		
	}
	
	private static String getValue(String arg) {
		return arg.substring(arg.indexOf("=")+1);
	}

}
