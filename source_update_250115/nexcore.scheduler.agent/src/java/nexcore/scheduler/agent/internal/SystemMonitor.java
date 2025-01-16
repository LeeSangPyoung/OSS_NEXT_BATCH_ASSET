package nexcore.scheduler.agent.internal;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;

import org.apache.commons.logging.Log;

import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 스레드로 동작하며, 에이전트 메모리 내부 컴포넌트들을 주기적으로 모니터링하여 로그함. </li>
 * <li>작성일 : 2016. 1. 11.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class SystemMonitor implements Runnable {
	private boolean                    enabled;
	private long                       interval = 60*1000;
	private AgentMain                  agentMain;
	private JobExecutionBoard          jobExecutionBoard;
	
	private Thread                     thisThread;
	private boolean                    destroyed;
	private Log                        log;

	public void init() {
		log = LogManager.getAgentLog();
		if (enabled) {
			thisThread = new Thread(this, "AgentSystemMonitor");
			thisThread.setDaemon(true);
			thisThread.start();
		}else {
			log.info("SystemMonitor disabled");
		}
	}

	public void destroy() {
		destroyed = true;
		thisThread.interrupt();
	}
	
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public AgentMain getAgentMain() {
	    return agentMain;
	}
	
	public void setAgentMain(AgentMain agentMain) {
	    this.agentMain = agentMain;
	}
	
	public JobExecutionBoard getJobExecutionBoard() {
	    return jobExecutionBoard;
	}
	
	public void setJobExecutionBoard(JobExecutionBoard jobExecutionBoard) {
	    this.jobExecutionBoard = jobExecutionBoard;
	}
	
	// #################################################################################
	// #####                   본체
	// #################################################################################

    private String memprint(long mem) {
		return String.format("%4d", (int)(mem/1024/1024));
	}
	
	private String printOneLine(String name, String text) {
		return String.format("[%-23s] %s", name, text);
	}
	
	public static void main(String[] args) {
		SystemMonitor sm = new SystemMonitor();
		System.out.println(sm.memprint(11234234));
		System.out.println(sm.printOneLine("abcd", "aaaaaaaaaaaaaa"));
		
	}
	
	public String getCurrentSystemText() {
		String newline = System.getProperty("line.separator");
		
		MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapMem  = mem.getHeapMemoryUsage();
		MemoryUsage nonHeapMem = mem.getNonHeapMemoryUsage();
		
		ThreadMXBean threadmx = ManagementFactory.getThreadMXBean();
		String time = Util.getCurrentHHMMSS();
		time = "[SYSMON "+time.substring(0,2)+":"+time.substring(2,4)+":"+time.substring(4,6)+"] ";
		StringBuilder s = new StringBuilder(256);
		s.append(newline);
		s.append(time);
		s.append("===== System Monitoring ===========================================================");
		s.append(newline);
		
		s.append(time);
		s.append(printOneLine("JVM Memory Heap",     "Init="+memprint(heapMem.getInit())+"m, Max="+memprint(heapMem.getMax())+"m, Total="+memprint(heapMem.getCommitted())+"m, Used="+memprint(heapMem.getUsed())+"m"));
		s.append(newline);
		
		s.append(time);
		s.append(printOneLine("JVM Memory Non Heap", "Init="+memprint(nonHeapMem.getInit())+"m, Max="+memprint(nonHeapMem.getMax())+"m, Total="+memprint(nonHeapMem.getCommitted())+"m, Used="+memprint(nonHeapMem.getUsed())+"m"));
		s.append(newline);
		
		s.append(time);
		s.append(printOneLine("Thread active count", String.valueOf(threadmx.getThreadCount())));
		s.append(newline);
		
//		for (IMonitorDisplayable service : monitorTargets) {
//			s.append(time);
//			s.append(printOneLine(service.getDisplayName(), service.getDisplayString()));
//			s.append(newline);
//		}

		s.append(time);
		s.append(printOneLine("Boot time", Util.getDatetimeLocalizedText(agentMain.getBootTime())));
		s.append(newline);

		s.append(time);
		s.append(printOneLine("Running job count", String.valueOf(jobExecutionBoard.getJobExecutionCount())));
		s.append(newline);

		s.append(time);
		s.append("===================================================================================");
		s.append(newline);
		return s.toString();
	}
	
	public void run() {
		while(!destroyed) {
			try {
				Util.sleep(interval);
			}catch(Throwable e) {
				if (destroyed) {
					break; // destroy 로 인해 Exception 이 발생한것이라면 printStackTrace 없이 조용히 죽는다.
				}else {
					e.printStackTrace();
					break;
				}
			}
			
			String s = getCurrentSystemText();
			log.info(s);
		}
	}
}
