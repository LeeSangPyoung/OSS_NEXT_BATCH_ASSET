/**
 * 
 */
package nexcore.scheduler.agent.runner.proc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.internal.AgentConstants;
import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.util.Util;

import org.apache.commons.logging.Log;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Unix 환경에서 프로세스 기동 종료 helper.</li>
 * <li>작성일 : 2013. 1. 28.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class UnixProcessHelperImpl extends AbsProcessHelper {
	private String psAllCommand = "ps -ef";     // 전체 프로세스를 보는 command
	private String killCommand  = "kill -9 ";   // 프로세스를 kill 하는 command
	private String shellName    = "/bin/sh";    // interpreter 쉘 지정.
	private int    pidIndex     = 1;            // ps -ef 의 결과 중에 PID 를 포함하는 위치 컬럼 위치. 기본값은 1 (2번째)  
	private int    ppidIndex    = 2;            // ps -ef 의 결과 중에 PPID (parent pid) 를 포함하는 위치 컬럼 위치. 기본값은 2 (3번째)
	
	private String shellFileCharset;            // z/OS 의 경우 shell 파일을 EBCDIC 코드로 만들어야 돌아간다. "CP933"
	
	public UnixProcessHelperImpl() {
	}
	
	public String getPsAllCommand() {
		return psAllCommand;
	}

	public void setPsAllCommand(String psAllCommand) {
		this.psAllCommand = psAllCommand;
	}

	public String getKillCommand() {
		return killCommand;
	}
	
	public void setKillCommand(String killCommand) {
		this.killCommand = killCommand;
	}

	public String getShellName() {
		return shellName;
	}

	public void setShellName(String shellName) {
		this.shellName = shellName;
	}

	public int getPidIndex() {
		return pidIndex;
	}

	public void setPidIndex(int pidIndex) {
		this.pidIndex = pidIndex;
	}

	public int getPpidIndex() {
		return ppidIndex;
	}

	public void setPpidIndex(int ppidIndex) {
		this.ppidIndex = ppidIndex;
	}
	
	public String getShellFileCharset() {
        return shellFileCharset;
    }

    public void setShellFileCharset(String shellFileCharset) {
        this.shellFileCharset = shellFileCharset;
    }

    /**
	 * 프로세스 실행을 위한 runner sh (또는 cmd)
	 * @param context
	 * @return runner sh 의 파일명 (./batch/runner/ncbatch_[JOB_EXE_ID].sh)
	 */
	public String createRunnerShell(JobContext context) throws IOException {
		ILogger log = context.getLogger();
		File runnerFile = new File(getRunnerShellFilename(context.getJobExecution().getJobExecutionId()));
		if (!runnerFile.getParentFile().exists()) {
			runnerFile.getParentFile().mkdirs();
		}
		PrintWriter out = null;
		try {
		    if (shellFileCharset != null) {
		        out = new PrintWriter(runnerFile, shellFileCharset);
		    }else {
		        out = new PrintWriter(runnerFile);
		    }
			out.println("#!"+shellName);
			
			// 홈디렉토리 처리
			String homeDir = context.getInParameter("HOME_PATH");
			if (!Util.isBlank(homeDir)) { // HOME_PATH 환경변수가 설정되어있으면 cd 한다.
				out.println("PATH=.:$PATH");
				out.println("cd "+homeDir);
			}
			
			// 메인 실행
			out.print(context.getJobExecution().getComponentName()); // 메인 프로그램 실행 구문.
			if (!Util.isBlank(context.getInParameter("ARG1"))) {
				out.print(" \"$@\"");                                  // ARGn 파라미터가 존재하면 Argument list 전달
			}
			out.println(" >> "+getStdoutFile(context)+" 2>&1");

			out.println("NCRETURNCODE=$?");
			out.println("echo $NCRETURNCODE >> "+getExitValueFile(context));
			out.println("exit $NCRETURNCODE");
			out.println("");
			return runnerFile.getAbsolutePath();
		} catch (IOException e) {
			log.error("createRunnerShell ["+runnerFile+"] error", e);
			throw e;
		} finally {
			if (out != null) out.close();
		}
	}
	
	public String getRunnerShellFilename(String jobExeId) {
		return Util.getHomeDirectory()+AgentConstants.RUNNER_FILE_DIRECTORY+"/ncbatch_"+jobExeId+".sh";
	}
	
	/**
	 * runner 파일을 실행
	 * @param runnerFile
	 * @param context
	 * @return Process 객체
	 * @throws IOException
	 */
	public Process execProcess(String runnerFile, JobContext context) throws IOException {
		// ■ 프로그램 & argument 설정
		
		List<String> cmdList = new ArrayList();
		cmdList.add(shellName);
		cmdList.add(runnerFile);
		cmdList.addAll(makeArgumentList(context));
		
		ProcessBuilder pb = new ProcessBuilder(cmdList);
		pb.redirectErrorStream(true); // stderr -> stdout
		
		// ■ 홈디렉토리를 파라미터로 설정했을 경우 적용함.
		String homePath = context.getInParameter("HOME_PATH");
		if (!Util.isBlank(homePath)) {
			pb.directory(new File(homePath));
		}

		// ■ 파라미터에 환경변수 set 할 것이 있으면 넣어준다.
		setEnvironment(context, pb.environment());

		// ■ 프로세스 기동.
		Process proc = pb.start();
		
		context.getLogger().info("Process environment : "+pb.environment());
		context.getLogger().info("Process started. "+cmdList);

		return proc;
	}
	
	/**
	 * Job Exe Id로 프로세스를 찾아내서 kill 함. 
	 * 하위 프로세스 까지 kill 함. (unix 에서는 하위프로세스 kill 하는 기능이 없으므로 전체를 ps 한 후 트리 관계를 찾아야한다. )
	 * @param jobExeId
	 * @param log
	 * @return 성공시 true, 실패시 false
	 */
	public boolean killProcess(String jobExeId, Log log) {
		Map<String, String>        ppidMap         = new HashMap();      // <PID, PPID>
		Map<String, List<String>>  childPidListMap = new HashMap();      // <PPID, LIst<PID>>
		
		String thisPid = null;
		BufferedReader in = null;
		try {
			// ps -ef 를 통해 전체 프로세스 리스트를 보고, pid ppid 를 가지고 child process list 를 구한다.
			Process ps = Runtime.getRuntime().exec(psAllCommand);
			in = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			
			String line = null;
			while((line = in.readLine()) != null) {
				if (log.isDebugEnabled()) {
					log.debug("ps>>"+line);
				}
				String[] ss = line.trim().split(" +");
				if (ss.length < 1) {
					continue;
				}
				
				try {
					Long.parseLong(ss[pidIndex]);
					Long.parseLong(ss[ppidIndex]);
				}catch (Exception e) {
					continue; // pid 가 아니라 아마 title line 인가보다. 이 라인 무시한다.
				}

				if (line.indexOf("ncbatch_"+jobExeId+".sh") > -1) {   // 여기서 PID 를 찾는다.
					thisPid = ss[pidIndex];
				}
				
				// ppidMap 구성
				ppidMap.put(ss[pidIndex], ss[ppidIndex]);
				
				// childPidListMap 구성
				List<String> childPidList = childPidListMap.get(ss[ppidIndex]);
				if (childPidList == null) {
					childPidList = new ArrayList();
					childPidListMap.put(ss[ppidIndex], childPidList);
				}
				childPidList.add(ss[pidIndex]);
			}
		
			if (thisPid == null) { // 프로세스를 못찾을 경우 그냥 끝낸다.
				Util.logInfo(log, "Process of '"+jobExeId+"' not found. Can not kill.");
				return false;
			}else {
				Util.logInfo(log, "PID : "+thisPid+". Trying to kill.");
				// 삭제 대상 PID 스캔. sub process 까지 스캔 한다.
				/*
				 * 2013.07.15
				 * 최상위 프로세스 (ncbatch***.sh) 는 kill 하지 않는다. 
				 * 그게 kill 되면 -exit.log 파일이 생성이 안되므로 에이전트 reboot 이후에 복구된 프로세스들은 exit 여부를 감지할 방법이 없다.
				 */
				List<String> childPidTreeListOfThis = getChildPidList(thisPid, childPidListMap);
				childPidTreeListOfThis.remove(0); // 최상위 PID (ncbatch***.sh) 는 kill 되지 않고 자연스럽게 죽도록 여기 리스트에서 제거한다.
				Collections.reverse(childPidTreeListOfThis); // 프로세스 트리의 leaf 부터 꺼꾸로 kill 날림.
				
				StringBuilder killStmt = new StringBuilder(128);
				killStmt.append(killCommand);
				for (String killTargetPid : childPidTreeListOfThis) {
					killStmt.append(" ");
					killStmt.append(killTargetPid);
				}
				
				Util.logInfo(log, killStmt.toString());
				
				// Kill 날림.
				Process p = Runtime.getRuntime().exec(killStmt.toString());
				p.waitFor();
				
				/*
				 * 2013.07.16 추가. #21355
				 * Linux, HP-UX 에서는 에이전트가 reboot 된 상황에서는 kill 시에 ncbatch.sh 도 같이 죽는 현상이 발생한다. 
				 * 이로 인해 -exit.log 파일이 생성이 안되고 프로세스 종료 여부를 확인할 수 없다.
				 * 이 문제를 보완하기 위해 -exit.log 파일 생성 여부와 ncbatch.sh 프로세스 alive 여부를 체크하여, 필요시 강제로 -exit.log 파일을 만들어준다.
				 */
				for (int i=0; i<10; i++) { // 최대 10 회 반복
					if (isExitLogFileExist(jobExeId)) {
						// 이 경우가 가장 일반적인 정상 상황.
						return true;
					}
					
					Util.logInfo(log, jobExeId+"-exit.log file not found");
					
					if (isRunnerShellProcessAlive(jobExeId, log)) {
						Util.logInfo(log, "ncbatch_"+jobExeId+".sh is alive.");
						// ncbatch.sh 가 아직 살아있으면 잠시 쉬고 다시 한다.
						Util.sleep(200); 
						continue;
					}else {
						Util.logInfo(log, "ncbatch_"+jobExeId+".sh is dead.");
						// ncbatch.sh 가 죽어있을때 -exit.log 파일이 없는 경우는 강제로 만든다.
						if (isExitLogFileExist(jobExeId)) {
							Util.logInfo(log, jobExeId+"-exit.log file found. exit");
							return true;
						}else { // 강제로 -exit.log 만든다.
							Util.logInfo(log, jobExeId+"-exit.log file not found. create");
							createExitLogFile(jobExeId, "99"); // TODO 에러코드 정리.
							return true;
						}
					}
				}
				
				return true;
			}
		} catch (Exception e) {
			Util.logError(log, "killProcess("+jobExeId+") error", e);
			return false;
		} finally {
			try { 
				in.close(); 
			}catch(Exception ignore) {
				ignore.printStackTrace();
			}
		}
	}
	
	/**
	 * process tree 로 부터 child list (sub 포함) 를 구한다. 
	 * @param thisPid
	 * @param childPidListMap
	 * @return
	 */
	private List<String> getChildPidList(String thisPid, Map<String, List<String>> childPidListMap) {
		List<String> childPidTreeListOfThis = new ArrayList();
		
		childPidTreeListOfThis.add(thisPid);
		
		List<String> childPidList = childPidListMap.get(thisPid);
		if (childPidList != null) {
			for (String childPid : childPidList) {
				childPidTreeListOfThis.addAll(getChildPidList(childPid, childPidListMap));
			}
		}		
		return childPidTreeListOfThis;
	}

	/**
	 * ncbatch_jobexe.sh 프로세스가 아직 살아있는지 확인.
	 * @param jobExeId
	 * @param log
	 * @return
	 */
	private boolean isRunnerShellProcessAlive(String jobExeId, Log log) {
		List<String> ps = listProcesses("ncbatch_"+jobExeId+".sh", log);
		return ps.size() > 0;
	}
	
	/**
	 * runner/jobexeid-exit.log 파일이 존재하는지 확인.
	 * @param jobExeId
	 * @return
	 */
	private boolean isExitLogFileExist(String jobExeId) {
		File exitLogFile = new File(Util.getHomeDirectory()+AgentConstants.RUNNER_FILE_DIRECTORY, jobExeId+"-exit.log");
		return exitLogFile.exists();
	}
	
	/**
	 * 강제로 exit.log 파일 생성 
	 * @param jobExeId
	 */
	private void createExitLogFile(String jobExeId, String exitValue) {
		File exitLogFile = new File(Util.getHomeDirectory()+AgentConstants.RUNNER_FILE_DIRECTORY, jobExeId+"-exit.log");
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(exitLogFile));
			out.println(exitValue);
		}catch(Exception e) {
			throw Util.toRuntimeException(e);
		}finally {
			try { out.close(); }catch(Exception ignore) {} 
		}
	}
	
	/**
	 * ps -ef 결과를 리턴함. 필요에 따라 필터링힌다.
	 *  
	 * @param filterString 필터링할 string을 입력한다.
	 * @return
	 */
	public List<String> listProcesses(String filterString, Log log) {
		List<String> result = new ArrayList();
		BufferedReader in = null;
		try {
			// tasklist.exe 를 통해 전체 프로세스 리스트를 보고 필터링한다.
			Process ps = Runtime.getRuntime().exec(psAllCommand);
			in = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			
			String line = null;
			while((line = in.readLine()) != null) {
				if (log.isDebugEnabled()) {
					log.debug("ps>>"+line);
				}

				if (filterString != null && line.indexOf(filterString) >= 0) {   // 여기서 PID 를 찾는다.
					result.add(line);
				}
			}
		
		} catch (Exception e) {
			Util.logError(log, "listProcesses("+filterString+") error", e);
			throw Util.toRuntimeException(e);
		} finally {
			try { 
				in.close(); 
			}catch(Exception ignore) {
				ignore.printStackTrace();
			}
		}
		return result;
	}

}
