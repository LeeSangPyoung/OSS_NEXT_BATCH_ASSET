/**
 * 
 */
package nexcore.scheduler.agent.runner.proc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.internal.AgentConstants;
import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.util.Util;

import org.apache.commons.logging.Log;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Windows 환경에서 프로세스 기동 종료 helper.</li>
 * <li>작성일 : 2013. 1. 28.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class WindowsProcessHelperImpl extends AbsProcessHelper {
	private String psAllCommand       = "wmic process where name='cmd.exe' get commandline, processid";   // 전체 프로세스를 보는 command
	private String getChildPidCommand = "wmic process where (ParentProcessId=%s) get processid";          // %s 프로세스의 child 프로세스 PID를 찾음.
	private String killCommand        = "taskkill /F /T /PID";   // 프로세스 kill. /T : subtree 모두 kill
	private String pidIndex           = "L";                     // ps 의 결과 중에 PID 를 포함하는 위치 컬럼 위치. 기본값은 L (Last)

	public WindowsProcessHelperImpl() {
	}
	
	public String getKillCommand() {
		return killCommand;
	}

	public void setKillCommand(String killCommand) {
		this.killCommand = killCommand;
	}

	public String getPsAllCommand() {
		return psAllCommand;
	}

	public void setPsAllCommand(String psAllCommand) {
		this.psAllCommand = psAllCommand;
	}

	public String getGetChildPidCommand() {
		return getChildPidCommand;
	}

	public void setGetChildPidCommand(String getChildPidCommand) {
		this.getChildPidCommand = getChildPidCommand;
	}

	public String getPidIndex() {
		return pidIndex;
	}

	public void setPidIndex(String pidIndex) {
		this.pidIndex = pidIndex;
	}

	/**
	 * 프로세스 실행을 위한 runner sh (또는 cmd)
	 * @param context
	 * @return runner sh 의 파일명 (./batch/runner/ncbatch_[JOB_EXE_ID].cmd)
	 */
	public String createRunnerShell(JobContext context) throws IOException {
		ILogger log = context.getLogger();
		File runnerFile = new File(getRunnerShellFilename(context.getJobExecution().getJobExecutionId()));
		if (!runnerFile.getParentFile().exists()) {
			runnerFile.getParentFile().mkdirs();
		}
		PrintWriter out = null;
		try {
			out = new PrintWriter(runnerFile);

			// 홈디렉토리 처리
			String homeDir = context.getInParameter("HOME_PATH");
			if (!Util.isBlank(homeDir)) { // HOME_PATH 환경변수가 설정되어있으면 cd 한다.
				out.println("cd "+homeDir);
			}

			// 메인 실행
			out.println("@echo off");  // @echo off 를 하지 않으면 아래 echo 부분에서 cmd 파일 실행 자체가 멈춘다.
			out.print("CALL "); // CALL. windows 배치에서 CALL 은 현재 실행중인 배치 파일을 종료하지 않고 필요한 다른 배치파일을 호출하여 실행한 다음 원래의 배치파일로  다시 돌아오려고 할 때 사용한다. 
			out.print(context.getJobExecution().getComponentName()); // 메인 프로그램 실행 구문.
			if (!Util.isBlank(context.getInParameter("ARG1"))) {
				out.print(" %*");                                  // ARGn 파라미터가 존재하면 Argument list 전달
			}
			out.println(" >> "+getStdoutFile(context)+" 2>&1");
			
			out.println("SET NCRETURNCODE=%ERRORLEVEL%");
			out.println("echo [%date% %time%] exit with %NCRETURNCODE% >> "+getStdoutFile(context));
			out.println("echo %NCRETURNCODE% > "+getExitValueFile(context));
			out.println("exit %NCRETURNCODE%");
			
			out.println("");
			return runnerFile.getAbsolutePath();
		} catch (IOException e) {
			Util.logError(log, "createRunnerShell ["+runnerFile+"] error", e);
			throw e;
		} finally {
			if (out != null) out.close();
		}
	}
	
	public String getRunnerShellFilename(String jobExeId) {
		return Util.getHomeDirectory()+AgentConstants.RUNNER_FILE_DIRECTORY+"/ncbatch_"+jobExeId+".cmd";
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
		cmdList.add(runnerFile);
		cmdList.addAll(makeArgumentList(context));
		
		ProcessBuilder pb = new ProcessBuilder(cmdList);
		pb.redirectErrorStream(true); // stderr -> stdout
		
		// ■ 홈디렉토리를 파라미터로 설정했을 경우 적용함.
		String homePath = context.getInParameter("HOME_PATH");
		if (!Util.isBlank(homePath)) {
			pb.directory(new File(homePath));
		}

		// ■ 파라미너터에 환경변수 set 할 것이 있으면 넣어준다.
		setEnvironment(context, pb.environment());

		// ■ 프로세스 기동.
		Process proc = pb.start();
		
		context.getLogger().info("Process environment : "+pb.environment());
		context.getLogger().info("Process started. "+cmdList);

		return proc;
	}
	
	/**
	 * Job Exe Id로 프로세스를 찾아내서 kill 함. 
	 * 하위 프로세스 까지 kill 함.
	 * @param jobExeId
	 * @param log
	 * @return 성공시 true, 실패시 false
	 */
	public boolean killProcess(String jobExeId, Log log) {
		String thisPid = null;
		BufferedReader in = null;
		try {
			// tasklist.exe 를 통해 전체 프로세스 리스트를 보고, pid ppid 를 가지고 child process list 를 구한다.
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
				
				int pidIndexInt = "L".equals(pidIndex) ? ss.length-1 : Util.toInt(pidIndex); // L 이면 오른쪽 마지막 필드.
				
				try {
					Long.parseLong(ss[pidIndexInt]);
				}catch (Exception e) {
					continue; // pid 가 아니라 아마 title line 인가보다. 이 라인 무시한다.
				}

				if (line.indexOf("ncbatch_"+jobExeId+".cmd") > 0) {   // 여기서 PID 를 찾는다.
					thisPid = ss[pidIndexInt];
					break;
				}
			}
		
			if (thisPid == null) { // 프로세스를 못찾을 경우 그냥 끝낸다.
				Util.logInfo(log, "Process of '"+jobExeId+"' not found. Can not kill.");
				return false;
			}else {
				/*
				 * 2013.07.15
				 * 최상위 프로세스 (ncbatch***.cmd) 는 kill 하지 않는다. 
				 * 그게 kill 되면 -exit.log 파일이 생성이 안되므로 에이전트 reboot 이후에 복구된 프로세스들은 exit 여부를 감지할 방법이 없다.
				 * 그 child 프로세스 pid 들을 조회하여 그놈을 죽인다. (이 프로세스가 Job 등록정보에 등록된 COMPONENT_NAME 이다)
				 */

				Util.logInfo(log, "Parent PID : "+thisPid);
				String childPid = getChildPid(thisPid, log);
				
				Util.logInfo(log, "PID : "+childPid+". Trying to kill.");
				// windows 에서는 taskkill.exe /T pid 하면 pid 의 sub 들도 모두 kill 된다.
				StringBuilder killStmt = new StringBuilder(128);
				killStmt.append(killCommand);
				killStmt.append(" ");
				killStmt.append(childPid);
				
				// Kill 날림.
				Util.logInfo(log, killStmt.toString());
				
				Process p = Runtime.getRuntime().exec(killStmt.toString());
				p.waitFor();
				
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
	 * child 프로세스 PID 를 리턴한다.
	 * 여러개일 경우 최초 1개만 리턴한다. 
	 * @param parentPid
	 * @param log
	 * @return
	 */
	private String getChildPid(String parentPid, Log log) {
		BufferedReader in = null;
		try {
			// tasklist.exe 를 통해 전체 프로세스 리스트를 보고 필터링한다.
			String command = String.format(getChildPidCommand, parentPid);
			log.debug(command);
			Process ps = Runtime.getRuntime().exec(command);
			in = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			
			boolean processIdPass = false;
			String line = null;
			while((line = in.readLine()) != null) {
				if (log.isDebugEnabled()) {
					log.debug("child>>"+line);
				}

				line = line.trim();
				
				if (processIdPass) {
					try {
						Integer.parseInt(line); // 이 line 이 PID 임
						return line;
					}catch(Exception ignore) {
						continue;
					}
				}else {
					processIdPass = line.equalsIgnoreCase("ProcessId");
				}
			}
		} catch (Exception e) {
			Util.logError(log, "getChildPid("+parentPid+") error", e);
			throw Util.toRuntimeException(e);
		} finally {
			try { 
				in.close(); 
			}catch(Exception ignore) {
				ignore.printStackTrace();
			}
		}
		throw new RuntimeException("getChildPid("+parentPid+") fail");
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
