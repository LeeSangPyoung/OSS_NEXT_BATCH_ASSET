package nexcore.scheduler.agent.runner.proc;

import java.io.IOException;
import java.util.List;

import nexcore.scheduler.agent.JobContext;

import org.apache.commons.logging.Log;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Proc 타입 Job (쉘실행) 을 실행시킬때 OS 별로 다른 방법으로 처리한다.</li>
 * <li>작성일 : 2013. 1. 28.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public interface IProcessHelper {

	/**
	 * 프로세스 실행을 위한 runner sh (또는 cmd)
	 * @param context
	 * @return runner sh 의 파일명 (./batch/runner/ncbatch_[JOB_EXE_ID].sh)
	 */
	public String createRunnerShell(JobContext context) throws IOException ;

	/**
	 * 프로세스 실행을 위한 runner sh (또는 cmd) 파일명 리턴
	 * @param jobExeId
	 * @return runner sh 의 파일명 (./batch/runner/ncbatch_[JOB_EXE_ID].sh)
	 */
	public String getRunnerShellFilename(String jobExeId) ;

	/**
	 * runner 파일을 실행
	 * @param runnerFile
	 * @param context
	 * @return Process 객체
	 * @throws IOException
	 */
	public Process execProcess(String runnerFile, JobContext context) throws IOException ;
	
	/**
	 * Job Exe Id로 프로세스를 찾아내서 kill 함. 
	 * 하위 프로세스 까지 kill 함.
	 * @param jobExeId
	 * @param log
	 * @return 성공시 true, 실패시 false
	 */
	public boolean killProcess(String jobExeId, Log log);
	
	/**
	 * ps -ef 결과를 리턴함. 필요에 따라 필터링힌다.
	 *  
	 * @param filterString 필터링할 String을 입력한다.
	 * @param log
	 * @return ps -ef 의 결과 string 리스트
	 */
	public List<String> listProcesses(String filterString, Log log);
}
