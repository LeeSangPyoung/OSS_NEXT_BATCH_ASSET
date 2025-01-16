
/**
 * 
 */
package nexcore.scheduler.controller.admin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nexcore.framework.supports.EncryptionUtils;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.User;
import nexcore.scheduler.util.Util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : command line 에서 job control 기능 제공. </li>
 * <li>작성일 : 2013. 8. 16.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class CommandLineJobControl {
	private static final Set<String> listJobInsOptionSet = new HashSet(Arrays.asList(new String[]{
		  "viewFilterId"
		 ,"jobInstanceIdLike"
		 ,"jobGroupIdLike"
		 ,"jobDescLike"
		 ,"componentNameLike"
		 ,"jobType"
		 ,"preJobIdLike"
		 ,"triggerJobIdLike"
		 ,"lastRetCode"
		 ,"agentId"
		 ,"jobState"
		 ,"ownerLike"
		 ,"activationTimeFrom"
		 ,"activationTimeTo"
		 ,"procDate"
		 ,"procDateFrom"
		 ,"procDateTo"
		 ,"output_file"}));

	private String                    configFile = "./etc/jobcontrol.conf";
	private String                    schedulerAddress;
	private String                    loginId;
	private String                    loginPassword;
	
	private List<String>              schedulerAddressList;
	private ControllerAdmin           admin;
	private AdminAuth                 auth; 
	
	public static void main(String[] args) throws IOException {
		Logger.getRootLogger().setLevel(Level.OFF);
		CommandLineJobControl control = new CommandLineJobControl();
		
		if (args.length < 2) {
			control.printUsage();
			return;
		}
		
		String njsJobcontrolConfig = System.getProperty("njs.jobcontrol.config");
		if (!Util.isBlank(njsJobcontrolConfig)) {
			control.configFile = njsJobcontrolConfig; 
		}
		try {
			control.loadConfigFile();
			control.connect();
			control.login();
			control.runMain(args);
		}catch (Exception e) {
			System.out.println("[FAIL] Check '"+control.configFile+"' file. error: "+e.getMessage());
		}
	}

	private void printUsage() {
		
		printfln("Usage: jobcontrol <options> <jobinsid | jobid | -file=filename> [target] ");
		printfln("   or  jobcontrol list_jobinsid <search conditions...> [-output_file=filename]");
		printfln("options : ");
		printfln("    lock              <jobinsid> ");
		printfln("    unlock            <jobinsid> ");
		printfln("    forcerun          <jobinsid> ");
		printfln("    rerun             <jobinsid> ");
		printfln("    stop              <jobinsid> ");
		printfln("    suspend           <jobinsid> ");
		printfln("    resume            <jobinsid> ");
		printfln("    forceendok        <jobinsid> ");
		printfln("    confirm           <jobinsid> ");
		printfln("    changetoghost     <jobinsid> ");
		printfln("    modifyagentid     <jobinsid> <agentid>");
		printfln("    activate          <jobid> <proc_date>");
		printfln("    activatewithlock  <jobid> <proc_date>");
		printfln("    list_jobinsid <-search field name=search value ...>");
		printfln("                  -viewFilterId=5");
		printfln("                  -jobInstanceIdLike=JOB01%%");
		printfln("                  -jobGroupIdLike=GRP%%");
		printfln("                  -jobDescLike=%%배치설명%%");
		printfln("                  -componentNameLike=%%BABC0010%%");
		printfln("                  -jobType=SLEEP");
		printfln("                  -preJobIdLike=JOB01%%");
		printfln("                  -triggerJobIdLike=JOB02%%");
		printfln("                  -lastRetCode=0 (정상) | 1 (에러) | -1 (미실행)");
		printfln("                  -agentId=agent1");
		printfln("                  -jobState=R");
		printfln("                  -ownerLike=담당자%%");
		printfln("                  -activationTimeFrom=yyyymmdd000000");
		printfln("                  -activationTimeTo=yyyymmdd235959");
		printfln("                  -procDate=yyyymmdd");
		printfln("                  -procDateFrom=yyyymmdd");
		printfln("                  -procDateTo=yyyymmdd");
		printfln("                  -output_file=./tmp/output.txt");
	}
	
	private void loadConfigFile() throws IOException {
		Properties p = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(configFile);
			p.load(in);
			
			schedulerAddress = p.getProperty("SCHEDULER_ADDRESS");
			schedulerAddressList  = Util.toList(schedulerAddress, ",");
			if (schedulerAddressList.size() > 2) {
				throw new RuntimeException("Scheduler list of config exceeded ("+schedulerAddressList.size()+").");
			}

			loginId          = p.getProperty("LOGIN_ID");
			loginPassword    = p.getProperty("LOGIN_PASSWORD");
		}finally {
			try { in.close(); }catch(Exception ignore) {}
		}
	}
	
	private void connect() {
		for (String address : schedulerAddressList) {
			if (Util.isBlank(address)) {
				throw new RuntimeException ("Wrong configuration. SCHEDULER_ADDRESS is empty");
			}

			String schedulerIp   = address.substring(0, address.indexOf(":")).trim();
			String schedulerPort = address.substring(address.indexOf(":")+1).trim();
			ControllerAdmin _admin = new ControllerAdmin(schedulerIp, schedulerPort);
			
			boolean isalive = _admin.isAlive();
			if (isalive) {
//				System.out.println("["+schedulerIp+":"+schedulerPort+"] connection success.");
				this.admin = _admin;
				return;
			}else {
//				System.out.println("["+schedulerIp+":"+schedulerPort+"] connection fail.");
			}
		}
		
		if (this.admin == null) {
			throw new RuntimeException("All scheduler connection ("+schedulerAddressList+") fail.");
		}
	}
	
	private void login() {
		String password = loginPassword;
		
//        if (!Util.isBlank(loginPassword) && (loginPassword.contains("{DES}") || loginPassword.contains("{AES}"))) {
        if (!Util.isBlank(loginPassword)) {
            password =  EncryptionUtils.decode(loginPassword);
		}

		User u = admin.login(loginId, password, Util.getLocalIp());
		if (u!=null) {
			auth = new AdminAuth(loginId, Util.getLocalIp());
		}
	}
	
	private void printfln(String format, Object ... o) {
		System.out.printf(format+"\n", o);
	}

	/**
	 * command line 으로 받은 string array 를 Map 으로 변환
	 * <br>
	 * 예) -jobInstanceIdLike=JOB01% -jobType=EJB
	 * =>
	 * {"jobInstanceIdLike"="JOB01%", "jobType"="EJB"}}
	 * 
	 * @param args
	 * @return
	 */
	private Map commandLineToMap(String[] args) {
		Map map = new HashMap();
		List errorKeyList = new ArrayList();
		for (String arg : args) {
			if (Util.isBlank(arg)) continue;
			if (!arg.startsWith("-")) continue;
			
			int eqIdx = arg.indexOf("=");
			String key   = arg.substring(1, eqIdx);
			String value = arg.substring(eqIdx+1);
			
			if (!listJobInsOptionSet.contains(key)) {
				errorKeyList.add(key);
			}
			
			map.put(key, value);
		}
		
		if (errorKeyList.size() > 0) {
			throw new RuntimeException("Wrong search options "+errorKeyList);
		}
		return map;
	}
	
	/**
	 * 검색 조건으로 검색한 Job 인스턴스 ID 목록
	 * @param args
	 * @return 조회된 인스턴스 개수
	 */
	private int listJobInsId(String[] args) throws IOException {
		Map queryParamMap = commandLineToMap(args);
		queryParamMap.put("columnList", "JOB_INSTANCE_ID"); // 불필요한 정보는 조회 하지 않음

		PrintWriter fout = null;
		try {
			if (queryParamMap.containsKey("output_file")) { /* 결과를 파일로 저장 */
				fout = new PrintWriter(new FileWriter((String)queryParamMap.get("output_file")));
			}
	
			List<Map> jobinsidList = admin.getJobInstanceListFreeColumn(queryParamMap);
			for (Map map : jobinsidList) {
				String jobinsid = (String)map.get("JOB_INSTANCE_ID");
				if (fout!=null) fout.println(jobinsid);
				System.out.println(jobinsid); // stdout 으로도 출력한다.
			}
			return jobinsidList.size();
		}finally {
			if (fout != null) {
				fout.close();
			}
		}
	}
	
	/**
	 * jobcontrol.sh forcerun -file=filename 
	 * 이런 경우 filename 파일에서 jobinsid 를 읽어 그 만큼 forcerun 한다.
	 * @param filename
	 * @return 파일에서 읽은 List of line string
	 */
	private List<String> loadJobInsIdListFromFile(String filename) throws IOException {
		List<String> lines = new ArrayList<String>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filename));
			String line = null;
			while((line = in.readLine()) != null) {
				lines.add(line.trim());
			}
		}finally {
			if (in!=null) {
				try { 
					in.close(); 
				}catch(Exception e) {}
			}
		}
		
		return lines;
	}
	
	/**
	 * 메인
	 * @param args commandline argument list. args[0] : command, args[1] : jobinsid, ...
	 */
	private void runMain(String[] args) throws Exception {
		String command = args[0];
		
		if ("list_jobinsid".equals(command)) {
			int jobinsCount = listJobInsId(args);
			printfln("[SUCCESS] %d job instances selected.", jobinsCount);
			return;
		}
		
		List<String> targetIdList = null;
		if (args[1].startsWith("-file")) {
			targetIdList = loadJobInsIdListFromFile(args[1].substring(args[1].indexOf("=")+1).trim());
		}else {
			targetIdList = new ArrayList<String>();
			targetIdList.add(args[1]);
		}
		
		for (String targetId : targetIdList) {
			try {
				if ("lock".equals(command)) {
					boolean b = admin.lockJob(targetId, auth);
					if (b) {
						printfln("[SUCCESS] lock %s ok", targetId);
					}else {
						printfln("[FAIL] lock %s fail", targetId);
					}
		
				}else if ("unlock".equals(command)) {
					boolean b = admin.unlockJob(targetId, auth);
					if (b) {
						printfln("[SUCCESS] unlock %s ok", targetId);
					}else {
						printfln("[FAIL] unlock %s fail", targetId);
					}
				
				}else if ("forcerun".equals(command)) {
					String jobexeid = admin.forceRunJob(targetId, auth);
					printfln("[SUCCESS] forcerun %s ok. JobExecutionId=%s", targetId, jobexeid);
					
				}else if ("rerun".equals(command)) {
					admin.reRunJob(targetId, auth);
					printfln("[SUCCESS] rurun %s ok.", targetId);
					
				}else if ("stop".equals(command)) {
					admin.stopJob(targetId, auth);
					printfln("[SUCCESS] stop %s ok.", targetId);
					
				}else if ("suspend".equals(command)) {
					admin.suspendJob(targetId, auth);
					printfln("[SUCCESS] suspend %s ok.", targetId);
					
				}else if ("resume".equals(command)) {
					admin.resumeJob(targetId, auth);
					printfln("[SUCCESS] resume %s ok.", targetId);
					
				}else if ("forceendok".equals(command)) {
					admin.forceEndOk(targetId, auth);
					printfln("[SUCCESS] forceendok %s ok.", targetId);
					
				}else if ("confirm".equals(command)) {
					admin.confirm(targetId, auth);
					printfln("[SUCCESS] confirm %s ok.", targetId);
					
				}else if ("changetoghost".equals(command)) {
					admin.forceChangeToGhost(targetId, auth);
					printfln("[SUCCESS] forcetoghost %s ok.", targetId);
					
				}else if ("modifyagentid".equals(command)) {
					admin.modifyJobInstanceAgentId(targetId, args[2], auth);
					printfln("[SUCCESS] modify agentid of %s, agent id : %s ok.", targetId, args[2]);
					
				}else if ("activate".equals(command)) {
					if (args.length < 3) {
						printUsage();
						return;
					}
					admin.activateJob(targetId, args[2], auth);
					printfln("[SUCCESS] activate of %s, PROC_DATE : %s ok.", targetId, args[2]);
		
				}else if ("activatewithlock".equals(command)) {
					if (args.length < 3) {
						printUsage();
						return;
					}
					admin.activateAndLockJob(targetId, args[2], auth);
					printfln("[SUCCESS] activate with lock of %s, PROC_DATE : %s ok.", targetId, args[2]);
				}else {
					printfln("[FAIL] Wrong command ("+command+")");
					printUsage();
				}
			}catch(Exception e) {
				System.out.println("[FAIL] "+e.getMessage());
			}
		}
	}
	
}