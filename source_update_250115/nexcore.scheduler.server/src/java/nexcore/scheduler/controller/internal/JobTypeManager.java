package nexcore.scheduler.controller.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nexcore.scheduler.controller.internal.jobtype.CBatchJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.CenterCutJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.DBProcedureJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.DummyJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.FileWatchJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.IJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.JBatchJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.PojoJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.ProcJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.QuartzJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.RestAPIJobTypeChecker;
import nexcore.scheduler.controller.internal.jobtype.SleepJobTypeChecker;
import nexcore.scheduler.entity.JobType;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 실행 가능한 Job Type 목록을 관리하고 허가되지 않은 Job Type 인 경우 에러를 내도록 validation 함</li>
 * <li>작성일 : 2012. 01. 16.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobTypeManager {
	private Map<String, IJobTypeChecker>  jobTypeCheckerMap;
	private String                        jobTypeUsing; 
	private List<String>                  jobTypeUseList;       // 이 사이트에서 사용중인 Job Type 목록. 사이트 별로 사용하는 Job 타입이 다를 수 있다 

	public JobTypeManager() {
	}
	
	public void init() {
		jobTypeCheckerMap = new HashMap<String, IJobTypeChecker>();
		loadJobTypeChecker(new JBatchJobTypeChecker());
		loadJobTypeChecker(new PojoJobTypeChecker());
		loadJobTypeChecker(new ProcJobTypeChecker());
		loadJobTypeChecker(new CenterCutJobTypeChecker());
		loadJobTypeChecker(new FileWatchJobTypeChecker());
		loadJobTypeChecker(new SleepJobTypeChecker());
		loadJobTypeChecker(new DummyJobTypeChecker());
		loadJobTypeChecker(new CBatchJobTypeChecker());   // CBATCH 타입은 기본 타입에서 제외함. nexcore.bat.scheduler.jobtype.list 에 추가해야함.
		loadJobTypeChecker(new DBProcedureJobTypeChecker());  // DB 프로시저. 기본타입. 3.6.5 부터
		loadJobTypeChecker(new QuartzJobTypeChecker());  // Quartz 타입. 3.8 부터
		loadJobTypeChecker(new RestAPIJobTypeChecker());  // RestAPI 타입. 4.1 부터
	}
	
	public void destroy() {
	}
	
	public String getJobTypeUsing() {
		return jobTypeUsing;
	}

	public void setJobTypeUsing(String jobTypeUseListString) {
		this.jobTypeUsing = jobTypeUseListString;
		if (!Util.isBlank(this.jobTypeUsing)) {
			if (this.jobTypeUsing.indexOf("${") < 0) { // 설정값이 제대로 되어있는 경우에만...
				jobTypeUseList = Arrays.asList(this.jobTypeUsing.split(","));
			}
		}
		
		if (jobTypeUseList == null) {
			// 기본 Job 타입. 
			this.jobTypeUsing = 
				JobType.JOB_TYPE_JBATCH+","+JobType.JOB_TYPE_POJO+","+
				JobType.JOB_TYPE_PROC+","+JobType.JOB_TYPE_DBPROC+","+
				JobType.JOB_TYPE_FILEWATCH+","+
				JobType.JOB_TYPE_SLEEP+","+JobType.JOB_TYPE_DUMMY+","+JobType.JOB_TYPE_CENTERCUT;
			
			jobTypeUseList = Arrays.asList(this.jobTypeUsing.split(","));
		}
	}

	public List<String> getJobTypeUsingList() {
		return jobTypeUseList;
	}

	public Map<String, IJobTypeChecker> getJobTypeCheckerMap() {
		return jobTypeCheckerMap;
	}

	public IJobTypeChecker getJobTypeChecker(String jobType) {
		return jobTypeCheckerMap.get(jobType);
	}
	
	private void loadJobTypeChecker(IJobTypeChecker checker) {
		if (jobTypeUseList.contains(checker.getTypeName())) { // 이 프로젝트에서 사용중인 Job 타입에 대해서만 checker를 로드한다.
			jobTypeCheckerMap.put(checker.getTypeName(), checker);
		}
	}
	
}
