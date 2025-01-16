/**
 * 
 */
package nexcore.scheduler.agent.nsc;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  </li>
 * <li>작성일 : 2012. 12. 26.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class NSCClientTest {
	static NSCClientFactory factory = new NSCClientFactory();

	private static PrintStream log;
	
	public static void main(String[] args) throws IOException {
		factory.setNscHostname("203.235.212.178");
		factory.setNscPort(7777);
		factory.setTimeoutInMillis(100000);
		factory.setDoLogData(true);
		
		GenericObjectPool pool = new GenericObjectPool();
		pool.setMaxActive(10);
		pool.setMinIdle(2);
		pool.setMaxWait(30000);
		pool.setTestOnBorrow(true);
		pool.setTestOnReturn(false);
		pool.setTestWhileIdle(false);
		pool.setFactory(factory);
		
		factory.setObjectPool(pool);
		

		log = new PrintStream(new FileOutputStream("C:/temp/nsc.out")); 
		
		NSCClientTest nsc = new NSCClientTest();
//		new Thread(new Worker1()).start();
//		new Thread(new Worker2()).start();
		new Thread(new WorkerTR1000()).start();
//		new Thread(new WorkerTR1001()).start();
	}


	static class Worker1 implements Runnable {
		public Worker1() {
		}
		
		public void run() {
			try {		
//				INSCClient client = factory.getClient();
			
				INSCClient client = new NSCClientImpl("203.235.212.178", 7777, 1000000, true);
				
				for (int i=0; i<10000; i++) {
					String filename = client.getLogFilename("AA5000B1", "AA5000B1201302010001");
					System.out.println(Thread.currentThread()+" getLogFilename() "+i+" "+filename);
					Thread.sleep(500);
				}
		
			}catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

	static class Worker2 implements Runnable {
		public Worker2() {
		}
		
		public void run() {
			try {		
				INSCClient client = new NSCClientImpl("203.235.212.178", 7777, 1000000, true);
				
				for (int i=0; i<10000; i++) {
					List l = client.getAllJobProcessStatus();
					System.out.println(Thread.currentThread()+"  getAllJobProcessStatus() "+i+" "+l.size());
					Thread.sleep(500);
				}
		
			}catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	static class WorkerTR1000 implements Runnable {
		private Socket      s;
		private DataInputStream in;
		
		private OutputStream out;
		
		public WorkerTR1000() {
			Socket s;
			try {
				s = new Socket ("203.235.212.178", 7777);
				in  = new DataInputStream(s.getInputStream());
				out = s.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			byte[] buffer = new byte[1024];
			for (int i=0;; i++) {
				try {
					Thread.sleep(500);

					out.write("0000006010000S0000all                                               ".getBytes());
					out.flush();
					
					in.readFully(buffer, 0, 8); // length 읽기
					String totalLenStr = new String(buffer, 0, 8);
					int totalLen = Integer.parseInt(totalLenStr);
					System.out.println("LEN:"+totalLen);
					
					in.readFully(buffer, 0, totalLen);
					synchronized(log) {
						log.print(totalLenStr);
						log.write(buffer, 0, totalLen);
						log.println();
					}
					System.out.write(buffer, 0, totalLen);
					
					
					System.out.println("<< END");
					
				} catch (EOFException e) {
					System.out.println("<<EOF");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}			
		}
	}

	static class WorkerTR1001 implements Runnable {
		private Socket      s;
		private DataInputStream in;
		private OutputStream out;
		
		private OutputStream log;
		
		public WorkerTR1001() {
			Socket s;
			try {
				s = new Socket ("203.235.212.178", 7777);
				in  = new DataInputStream(s.getInputStream());
				out = s.getOutputStream();
				log = new FileOutputStream("C:/temp/nsc.out"); 
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			byte[] buffer = new byte[1024];
			for (int i=0;; i++) {
				try {
					Thread.sleep(500);

					out.write("0000021010010S0000AA5000B1-1-2                                                                                        AA5000B1-1-2201303200001                                                                            ".getBytes());
					out.flush();
					
					in.readFully(buffer, 0, 8); // length 읽기
					log.write(buffer, 0, 8);
					int totalLen = Integer.parseInt(new String(buffer, 0, 8));
					System.out.println("LEN:"+totalLen);
					
					in.readFully(buffer, 0, totalLen);
					log.write(buffer, 0, totalLen);
					log.write(0x0d);
					log.write(0x0a);
					System.out.write(buffer, 0, totalLen);
					
					System.out.println("<< END");
					
				} catch (EOFException e) {
					System.out.println("<<EOF");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}			
		}
	}
}
