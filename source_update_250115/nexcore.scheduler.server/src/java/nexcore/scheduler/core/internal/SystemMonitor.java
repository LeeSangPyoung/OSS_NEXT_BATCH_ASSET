package nexcore.scheduler.core.internal;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.List;

import org.apache.commons.logging.Log;

import nexcore.scheduler.controller.ha.IPeerClient;
import nexcore.scheduler.core.IMonitorDisplayable;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 스레드로 동작하며, 스케줄로 메모리 내부 컴포넌트들을 주기적으로 모니터링하여 로그함. </li>
 * <li>작성일 : 2010. 12. 15.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class SystemMonitor implements Runnable {
	private IPeerClient                peerClient;
	private long                       interval = 60*1000;
	private long                       bootTime;
	
	private List<IMonitorDisplayable>  monitorTargets;
	
	private Thread                     thisThread;
	private boolean                    destroyed;
	private Log                        log;

	public void init() {
		bootTime = System.currentTimeMillis();
		log = LogManager.getSchedulerLog();
		thisThread = new Thread(this, "SchedulerSystemMonitor");
		thisThread.setDaemon(true);
		thisThread.start();
	}

	public void destroy() {
		destroyed = true;
		thisThread.interrupt();
	}
	
	public long getBootTime() {
		return bootTime;
	}

	public void setBootTime(long bootTime) {
		this.bootTime = bootTime;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}
	
	public IPeerClient getPeerClient() {
		return peerClient;
	}

	public void setPeerClient(IPeerClient peerClient) {
		this.peerClient = peerClient;
	}

	public List<IMonitorDisplayable> getMonitorTargets() {
		return monitorTargets;
	}
	
	public void setMonitorTargets(List<IMonitorDisplayable> monitorTargets) {
		this.monitorTargets = monitorTargets;
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
		
		for (IMonitorDisplayable service : monitorTargets) {
			s.append(time);
			s.append(printOneLine(service.getDisplayName(), service.getDisplayString()));
			s.append(newline);
		}
		s.append(time);
		s.append(printOneLine("Boot time", Util.getDatetimeLocalizedText(bootTime)));
		s.append(newline);
		
		s.append(time);
		s.append(printOneLine("Peer isAlive",        peerClient.isPeerExist() ? String.valueOf(peerClient.isAlive()) + " ["+peerClient.getPeerAddress()+"]"  : "none (Single Server)"));
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
			}catch(Exception e) {
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
