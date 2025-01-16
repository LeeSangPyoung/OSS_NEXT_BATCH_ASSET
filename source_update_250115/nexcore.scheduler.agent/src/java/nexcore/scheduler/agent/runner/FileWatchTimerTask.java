package nexcore.scheduler.agent.runner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 agent </li>
 * <li>설  명 : FileWatch 를 위해 주기적으로 디렉토리를 polling 하는 Timer Task  </li>
 * <li>작성일 : 2010. 10. 21.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
//  2011. 06. 28. MOZEN 변경분 merge. [file move], [log message 정리]

public class FileWatchTimerTask extends TimerTask {
	public static final long DEFAULT_POLLING_INTERVAL_SEC = 10;
	
	private JobContext           context;
	private FileWatchJobRunner   parent;
	private IJobRunnerCallBack   jobRunnerCallBack;

	private String               directory;
	private String               filename;
	private int                  pollingTimeMinute; // 분단위 polling time

	private long                 pollingStartTime;  // polling 시작한 시각
	
	
	// -----------------------------------------------------
	// [MOZEN] 파일 MOVE를 위한 속성 추가
	//         checkSeq, moveFlag, moveDir, moveOverWrite
	// -----------------------------------------------------		
	/**
	 * 파일 체크 회차
	 */
	private int 				 checkSeq = 0;
	
	/**
	 * 파일 수신 성공 시 파일 MOVE 여부.<br>
	 * 작업 파라미터 KEY=MOVE_FLAG, VALUE= true | TRUE | false | FALSE | 기타(false)<br>
	 * 기본값 = false 
	 */
	private boolean moveFlag			= false;		// default false
	
	/**
	 * 파일 MOVE 시 대상 디렉토리, moveFlag 가 false 인 경우 사용안함<br>
	 * 작업 파라미터 KEY=MOVE_DIR, VALUE=디렉토리 경로<br>
	 * 기본값 = null
	 */
	private String moveDir				= null;

	/**
	 * 파일 MOVE 시 대상 디렉토리를 생성할지 여부 설정
	 * 기본값 = false
	 */
	private boolean createMoveDir		= false;

	/**
	 * 파일 MOVE 시 대상 디렉토리에 동일 파일명이 존재하는 경우 OVERWRITE 여부, moveFlag 가 false 인 경우 사용안함<br>
	 * 대상 디렉토리에 동일 파일명이 존재하는 경우 MOVE_OVERWRITE 값이 true 인 경우 OVERWRITE를 수행하고 false 인 경우 에러 발생<br>
	 * 작업 파라미터 KEY=MOVE_OVERWRITE, VALUE= true | TRUE | false | FALSE | 기타(false)<br>
	 * 기본값 = true
	 */
	private boolean moveOverWrite		= true;			// default true
	
	
	
	/**
	 * 분단위 파일 체크 간격.
	 */
	private String pollingInterval;
	
	public FileWatchTimerTask() {
	}
	
	public void setJobContext(JobContext batchContext) {
		this.context = batchContext;
	}
	public JobContext getJobContext() {
		return context;
	}
	public void setFileWatchJobRunner(FileWatchJobRunner fileWatchJobRunner) {
		this.parent = fileWatchJobRunner;
	}
	public FileWatchJobRunner getFileWatchJobRunner() {
		return this.parent;
	}
	public void setJobRunnerCallBack(IJobRunnerCallBack jobRunnerCallBack) {
		this.jobRunnerCallBack = jobRunnerCallBack;
	}
	public IJobRunnerCallBack getJobRunnerCallBack() {
		return this.jobRunnerCallBack;
	}
	public int getPollingTimeMinute() {
		return pollingTimeMinute;
	}
	public void setPollingTimeMinute(int pollingTimeMinute) {
		this.pollingTimeMinute = pollingTimeMinute;
	}

	public FileWatchTimerTask copy() {
		FileWatchTimerTask t = new FileWatchTimerTask();
		t.context      		= this.context;
		t.parent           	= this.parent;
		t.jobRunnerCallBack = this.jobRunnerCallBack;
		t.directory 		= this.directory;
		t.filename			= this.filename;
		t.pollingTimeMinute	= this.pollingTimeMinute;
		t.pollingStartTime  = this.pollingStartTime;
		return t;
	}
	
	private int getPollingTime() {
		String pollingTime = context.getInParameter("POLLING_TIME");
		if (Util.isBlank(pollingTime)) {
			// 파라미터로 주어지지 않을 경우는 23:50 분까지의 시간을 측정하여 설정한다. (분단위)
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE,      50);
			
			pollingTime = String.valueOf((cal.getTime().getTime() - System.currentTimeMillis()) / 1000 / 60); 
		}
		try {
			return Integer.parseInt(pollingTime);
		}catch(Exception e) {
			throw new AgentException("com.job.wrong.parameter", "POLLING_TIME", pollingTime);
		}
	}

	public void init() {
		directory = context.getInParameter("DIRECTORY");
		if (Util.isBlank(directory)) {
			throw new AgentException("com.job.parameter.required", "DIRECTORY");
		}
		filename  = context.getInParameter("FILENAME");
		if (Util.isBlank(filename)) {
			throw new AgentException("com.job.parameter.required", "FILENAME");
		}
		pollingTimeMinute = getPollingTime();
		
		pollingStartTime  = System.currentTimeMillis();
		
		
		// MOVE_FLAG 존재 시 moveFlag 변수 설정 
		if (context.getInParameter("MOVE_FLAG") != null) {
			moveFlag = Util.toBoolean(context.getInParameter("MOVE_FLAG"));
		}
		
		// 파일 이동(MOVE_FLAG) 옵션이 TRUE 인 경우, 체크
		if (moveFlag) {

			// 이동 대상 디렉토리(MOVE_DIR) 체크
			moveDir = context.getInParameter("MOVE_DIR");

			// MOVE_DIR 파라미터 값 확인
			if (Util.isBlank(moveDir)) {
				throw new AgentException("com.job.parameter.required", "MOVE_DIR");
			}
			
			// 이동 대상 디렉토리 동적 생성 여부 설정
			createMoveDir = Util.toBoolean(context.getInParameter("CREATE_MOVE_DIR"));
			
			// 이동 대상 디렉토리(MOVE_DIR) 확인
			File moveDirFile = new File(moveDir);

			// 이동 대상 디렉토리 생성
			if (createMoveDir) {
				if (!moveDirFile.exists()) {
					if (!moveDirFile.mkdirs()) { // 이동 대상 디렉토리 생성
						throw new AgentException("agent.filewatch.noperm.write", moveDir);	// {0} 에 쓰기 권한이 없습니다
					}
				}
			}
				
			// 이동 대상 디렉토리 여부 확인
			if (!moveDirFile.isDirectory()) {
				throw new AgentException("agent.filewatch.dirnotfound", moveDir);	// {0} 디렉토리 경로가 존재하지 않습니다
			}
			
			// 이동 대상 디렉토리 쓰기 권한 확인
			if (!moveDirFile.canWrite()) {
				throw new AgentException("agent.filewatch.noperm.write", moveDir);	// {0} 에 쓰기 권한이 없습니다
			}
			
			// OVERWRITE 값 설정
			if (!Util.isBlank(context.getInParameter("MOVE_OVERWRITE"))) {
				moveOverWrite = Util.toBoolean(context.getInParameter("MOVE_OVERWRITE"));
			}
		}
		
		
		ILogger logger = context.getLogger();
		
		if (logger.isInfoEnabled()) {
			
			logger.info("******************** "+MSG.get("agent.filewatch.check.info1")+" ********************");									// ### 파일 체크 정보
			logger.info("*** "+MSG.get("agent.filewatch.check.info2", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(pollingStartTime)));	// ### 파일 체크 시작 시각 = {0}
			logger.info("*** "+MSG.get("agent.filewatch.check.info3", directory));																	// ### 체크 디렉토리 = {0} (DIRECTORY 파라미터 값)		
			logger.info("*** "+MSG.get("agent.filewatch.check.info4", filename));																	// ### 체크 파일 명 = {0} (FILENAME 파라미터 값)
		
			pollingInterval = context.getInParameter("POLLING_INTVAL");
			
			if (Util.isBlank( pollingInterval )) {
				pollingInterval = "60";
			}

			logger.info("*** "+MSG.get("agent.filewatch.check.info5", pollingInterval));           // ### 체크 간격 초(SEC) = {0} 초	
			logger.info("*** "+MSG.get("agent.filewatch.check.info6", new Date(pollingStartTime + pollingTimeMinute * 60l * 1000l)));         // ### 체크 유효 시간(MIN) =  [{0,date,full} {0,time,full}]
			logger.info("*** "+MSG.get("agent.filewatch.check.info7", String.valueOf(moveFlag)));  // ### 수신 파일 이동 처리 = {0}			
			
			if (moveFlag) {
				logger.info("*** "+MSG.get("agent.filewatch.check.info8",   moveDir));               // ### 수신 파일 이동 경로 = {0}
				logger.info("*** "+MSG.get("agent.filewatch.check.info8.2", createMoveDir));         // ### 수신 파일 이동 경로 생성 = {0}
				
				if (moveOverWrite) {
					logger.info("*** "+MSG.get("agent.filewatch.check.info9", 0, context.getInParameter("MOVE_OVERWRITE")));   // ### 수신 파일 이동 시 덮어쓰기 여부 = OVERWRITE 수행 (MOVE_OVERWRITE={1})
				} else {
					logger.info("*** "+MSG.get("agent.filewatch.check.info9", 1, context.getInParameter("MOVE_OVERWRITE")));   // ### 수신 파일 이동 시 덮어쓰기 여부 = 에러 발생 (MOVE_OVERWRITE={1})
				}
				
			}
			logger.info("******************** "+MSG.get("agent.filewatch.check.info1")+" ********************");													// ### 파일 체크 정보

			if (logger.isInfoEnabled()) logger.info("");		// 그냥 빈 라인 추가
		}
			
	}
	
	public void destroy() {
		cancel();
	}
	
	public void run() {
		
		checkSeq++;
		
		/*
		 * 2013-10-10. 디렉토리에 <CURRENT_YYYYMMDD> 와 같은 예약어로 당일 일자를 넣을 수 있다. #21558.
		 * 외부기관으로 부터 파일 수신일이 일정하지 않을 경우 
		 * 몇일동안 RUNNING 하면서 파일 수신 여부를 체크할 수 있다.
		 * 이 경우 디렉토리에 당일자가 들어가는 경우도 있을 수 있다.
		 */
		String realDir = getEvaluatedDirectory(directory);
		
		ILogger logger = context.getLogger();
		
		logger.info(MSG.get("agent.filewatch.check1", checkSeq, realDir, filename));	// 파일 체크 {0} 회차 : {1} 디렉토리 내 {2} 파일(들)의 존재 여부를 체크합니다
		
		/*
		if (logger.isDebugEnabled()) {
			logger.debug("Start to check file.");
		}
		*/
		
		if (parent.getAgentMain().isDestroyed()) {
			// 서버 정지중이라면 이 Task 도 cancel 한다.
			
			if (logger.isInfoEnabled())
				logger.info(MSG.get("agent.filewatch.end.ok3"));	// 배치 에이전트 서버가 정지 중이므로 파일 배치를 종료합니다
			
			cancel();
			return;
		}

		/*
		if (logger.isInfoEnabled()) {
			logger.info("polling start date&time=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(pollingStartTime)) 
					+ ", \t start millis=" + pollingStartTime + " millis" 
					+ ", \t pollingTimeMinute=" +pollingTimeMinute + " min.");
		}
		*/
		
		long elapsedMillis =  System.currentTimeMillis() - pollingStartTime;	// 체크 시작 후 경과 시간, 밀리세컨즈 단위
		long pollignTimeMillis = pollingTimeMinute * 60 * 1000;
				
		
		if (elapsedMillis >= pollignTimeMillis) {
			
			// logger.debug("Polling time ["+pollingTimeMinute+" min] exceed. will stop.");
			
			if (logger.isInfoEnabled()) 
				logger.info(MSG.get("agent.filewatch.end.ok2", pollingTimeMinute));		// 체크 유효 시간({0} 분)을 경과하였습니다. 파일 체크를 종료합니다
			
			// POLLING Time 이 지났다. cancel 한다.
			cancel();
			// callback 한다.
			parent.end(context, -2, MSG.get("agent.filewatch.filenotfound"), jobRunnerCallBack);
			return;
			
		} 
		
		// 파일 유무 체크
		File dir = new File(realDir);
		FileFilter fileFilter = new WildcardFileFilter(filename);
		File[] matchFiles = dir.listFiles(fileFilter);
		
		if (matchFiles == null || matchFiles.length == 0) {
			// 파일이 없다. 그냥 쉰다.
			return;
		}
		
		
		// 이름에 match 되는 파일이 존재한다.
		List filenameList = new ArrayList();
		for (File matchFile : matchFiles) {
			try {
				filenameList.add(matchFile.getCanonicalPath());
			}catch (Exception e) {
				filenameList.add(matchFile.getAbsolutePath()); // 이럴일이 있을까...혹시나 모르니 이렇게 두자.
			}
		}
		
		if (logger.isInfoEnabled()) {
			logger.info(MSG.get("agent.filewatch.end.ok1", filenameList.size() , filenameList));	// {0} 건의 체크 대상 파일(들)을 검색하였습니다. 파일 체크를 종료 합니다. 검색된 파일 목록={1}
		}


		//
		// 파일 이동 옵션이 true 인 경우 File MOVE - 이동 중 에러가 발생하면 에러 기록 후 작업 종료
		//
		if (moveFlag) {
			try {
				filenameList = moveReceivedFile(matchFiles);
			} catch (Exception e) {
				if (logger.isErrorEnabled())
					logger.error(MSG.get("agent.filewatch.end.error1"), e);	// 검색된 파일 이동 중 예외가 발생하였습니다. 파일 체크를 종료합니다
				cancel();
				// callback 한다.
				parent.end(context, -2, e.getMessage(), jobRunnerCallBack);
				
				return;
			}
		}

		context.setReturnValue("FILE_COUNT", String.valueOf(filenameList.size()));
		context.setReturnValue("FILE_LIST",  Util.toString(filenameList, ","));
		
		// 파일을 찾았으니 이 Job 을 종료시킨다. (계속 fire 되지 않도록 한다. )
		cancel();
		
		// callback 한다.
		parent.end(context, 0, null, jobRunnerCallBack);
	}
	
	
	/**
	 * 체크된 파일(들)을 이동한다.
	 * 
	 * @param watchedFiles 체크된 파일 목록
	 */
	private List<String> moveReceivedFile(File[] watchedFiles) {
		ILogger logger = context.getLogger();
		if (logger.isInfoEnabled()) {
			logger.info(MSG.get("agent.filewatch.file.move.ing", moveDir));		// 체크된 파일(들)을 {0} 디렉토리로 이동합니다...
		}
		
		File t_srcFile 		= null;		// source 파일 임시 변수
		File t_destFile 	= null;		// target 파일 임시 변수
		
		//
		// 이동하려는 디렉토리에 동일 명칭의 파일이 존재하는 경우 MOVE를 위한 선처리 수행
		//
		for (int i = 0; i < watchedFiles.length; i++) {
			t_destFile = new File(moveDir, watchedFiles[i].getName());
			
			if (t_destFile.exists()) {
				if (moveOverWrite) {
					// t_desfFile 삭제 -- 만일 t_desfFile 이 디렉토리 인 경우는 에러 발생
					if (t_destFile.isFile()) {
						// t_desfFile 파일 삭제
						if (!t_destFile.delete()) {
							throw new AgentException("agent.filewatch.del.file.error", t_destFile);	// {0} 파일 삭제에 실패하였습니다. 
						}
						
					} else {
						// 디렉토리 이므로 에러를 발생하도록 한다. -- 운영환경에서 디렉토리를 맘대로 삭제하지 못하도록 하기 위함
						throw new AgentException("agent.filewatch.exist.dir.error", t_destFile);	// {0} 디렉토리가 이미 존재합니다.
					}
				} else {
					// 이동하려는 파일이 대상 디렉토리에 존재하므로 에러 발생
					throw new AgentException("agent.filewatch.exist.file.error.noopt", t_destFile);	// MOVE_OVERWRITE 옵션은 false 이며 {0} 파일이 이미 존재합니다
				}
			}
		}
		
		List<String> filenameList = new ArrayList<String>(watchedFiles.length);
		
		//
		// 파일 MOVE 수행
		//
		for (int i = 0; i < watchedFiles.length; i++) {
		
			t_srcFile = watchedFiles[i];
			
			try {
				
				// 파일 MOVE를 위한 FileUtils 이용 시 createDestDir 플래그를 false 로 설정
				// 	-- 이동 대상 디렉토리(moveDir)는 FileWatch 수행 시작 시에 검증을 끝낸 것이므로 유효한 디렉토리임
				//	-- FileWatch 내에서 마음대로 디렉토리 생성을 못하도록 하기 위함
				//
				// FileUtils.moveFileToDirectory 메소드는 이동 대상 디렉토리에 동일한 이름을 갖는 파일 존재 시 에러가 발생하지만
				// MOVE 하기 전에 위에서 MOVE_OVERWRITE 에 따른 작업을 미리 수행한다.
				FileUtils.moveFileToDirectory(t_srcFile, new File(moveDir), false); 	 
				
				if (logger.isInfoEnabled()) {
					logger.info(MSG.get("agent.filewatch.file.move.ok2", t_srcFile, moveDir));	// {0} 파일이 {1} 디렉토리로 이동되었습니다
				}
				
				t_destFile = new File(moveDir, t_srcFile.getName());
				
				try {
					// 원작자 스타일로 코딩함
					filenameList.add(t_destFile.getCanonicalPath());
					
				} catch (Exception e) {
					filenameList.add(t_destFile.getAbsolutePath());	
				}
			} catch (IOException e) {
				throw new AgentException("agent.filewatch.file.move.error", e, t_srcFile);	// {0} 파일 이동 중 에러가 발생하였습니다
			}			
		}

		if (logger.isInfoEnabled())
			logger.info(MSG.get("agent.filewatch.file.move.ok1", moveDir));	// 체크된 파일(들)이 {0} 디렉토리로 이동되었습니다
		
		return filenameList;
	}
	
	private String getEvaluatedDirectory(String directory) {
		if (Util.isBlank(directory)) {
			return directory;
		}
		String retval = directory;
		if (directory.contains("<CURRENT_")) {
			// 일,월,년 별로 디렉토리가 달라질 수 있다. 이 것을 반영함 
			retval = retval.replaceAll("<CURRENT_YYYYMMDD>", Util.getCurrentYYYYMMDD());
			retval = retval.replaceAll("<CURRENT_YYMMDD>",   Util.getCurrentYYYYMMDD().substring(2,8));
			retval = retval.replaceAll("<CURRENT_MMDD>",     Util.getCurrentYYYYMMDD().substring(4,8));
			retval = retval.replaceAll("<CURRENT_DD>",       Util.getCurrentYYYYMMDD().substring(6,8));
			retval = retval.replaceAll("<CURRENT_MM>",       Util.getCurrentYYYYMMDD().substring(4,6));
			retval = retval.replaceAll("<CURRENT_YYYY>",     Util.getCurrentYYYYMMDD().substring(0,4));
			retval = retval.replaceAll("<CURRENT_YY>",       Util.getCurrentYYYYMMDD().substring(2,4));
		}		
		return retval;
	}
}
