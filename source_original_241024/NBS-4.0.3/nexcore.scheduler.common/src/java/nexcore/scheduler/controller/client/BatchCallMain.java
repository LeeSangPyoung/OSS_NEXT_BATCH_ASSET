/**
 * 
 */
package nexcore.scheduler.controller.client;

import java.util.LinkedHashMap;
import java.util.Properties;

import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 :  Command line 에서 BatchCaller를 이용하여 </li>
 * <li>작성일 : 2012. 5. 7.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class BatchCallMain {

	/**
	 * 예) BatchCallMain -IP=127.0.0.1 -PORT=8124 -CALLER=CLIENT1 -JOBID=JOB01 -TIMEOUT=60 -PARAM=A=10 -PARAM=B=30 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String schedulerIp    = "127.0.0.1"; // 기본값
		int    schedulerPort  = 8124;        // 기본값
		String schedulerIp2   = null;        // 기본값
		int    schedulerPort2 = 9124;        // 기본값
		String jobId          = null;
		String callerId       = null;
		int    timeoutSec     = 60;          // 기본값
		LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
		
		for (String arg : args) {
			if (arg.startsWith("-IP=")) {
				schedulerIp = getValue(arg);
			}else if (arg.startsWith("-PORT=")) {
				schedulerPort = Integer.parseInt(getValue(arg));
			}else if (arg.startsWith("-IP2=")) {
				schedulerIp2 = getValue(arg);
			}else if (arg.startsWith("-PORT2=")) {
				schedulerPort2 = Integer.parseInt(getValue(arg));
			}else if (arg.startsWith("-JOBID=")) {
				jobId = getValue(arg);
			}else if (arg.startsWith("-CALLER=")) {
				callerId = getValue(arg);
			}else if (arg.startsWith("-TIMEOUT=")) {
				timeoutSec = Integer.parseInt(getValue(arg));
			}else if (arg.startsWith("-PARAM=")) {
				String paramNV = getValue(arg);
				int idx = paramNV.indexOf("=");
				if (idx < 0) continue;
				try {
					String key   = paramNV.substring(0, idx);
					String value = paramNV.substring(idx+1);
					params.put(key, value);
				}catch(Exception e) {
					throw new IllegalArgumentException(arg, e);
				}
			}
		}
		
		if (Util.isBlank(jobId)) {
			throw new IllegalArgumentException("Wrong argument (-JOBID="+jobId+")");
		}
		if (Util.isBlank(callerId)) {
			throw new IllegalArgumentException("Wrong argument (-CALLER="+callerId+")");
		}
		
		BatchCaller caller = null;
		if (Util.isBlank(schedulerIp2)) {
			// 단일 스케줄러
			caller = new BatchCaller(schedulerIp, schedulerPort);
		}else {
			// 다중 스케줄러
			caller = new BatchCaller(schedulerIp, schedulerPort, schedulerIp2, schedulerPort2);
		}
		String jobExecutionId = caller.callBatchJob(jobId, params, callerId);
		
		caller.waitJobEnd(jobExecutionId, timeoutSec * 1000);
		
		int retcode = caller.getJobReturnCode(jobExecutionId);
		Properties returnValues = caller.getJobReturnValues(jobExecutionId);

		System.out.println("RETURN VALUE="+returnValues);

		System.exit(retcode);
		
	}
	
	private static String getValue(String arg) {
		return arg.substring(arg.indexOf("=")+1);
	}
	
}
