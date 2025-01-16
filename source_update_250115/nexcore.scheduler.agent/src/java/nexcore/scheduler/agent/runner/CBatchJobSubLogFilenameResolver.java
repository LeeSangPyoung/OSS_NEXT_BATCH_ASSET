/**
 * 
 */
package nexcore.scheduler.agent.runner;

import java.io.File;

import nexcore.scheduler.agent.ISubLogFilenameResolver;
import nexcore.scheduler.agent.nsc.NSCIntegrator;
import nexcore.scheduler.entity.JobLogFilenameInfo;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : CBATCH Job 의 기본 sublog 파일명 조회 로직 </li>
 * <li>작성일 : 2013. 1. 31.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class CBatchJobSubLogFilenameResolver implements ISubLogFilenameResolver  {
	private NSCIntegrator nscIntegrator;

	public CBatchJobSubLogFilenameResolver() {
	}
	
	public void init() {
	}

	public void destroy() {
	}
	
	public NSCIntegrator getNscIntegrator() {
		return nscIntegrator;
	}

	public void setNscIntegrator(NSCIntegrator nscIntegrator) {
		this.nscIntegrator = nscIntegrator;
	}

	/**
	 * NSC 와 통신하여 sub 로그 파일명을 가져온다.
	 */
	public File getSubLogFile(JobLogFilenameInfo filenameinfo) {
		String file = nscIntegrator.getCBatchLogFilename(filenameinfo.getComponentName(), filenameinfo.getJobInstanceId());
		return Util.isBlank(file) ? null : new File(file);
	}
}
