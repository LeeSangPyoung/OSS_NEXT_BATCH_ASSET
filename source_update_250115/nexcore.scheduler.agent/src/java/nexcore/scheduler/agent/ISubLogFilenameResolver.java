package nexcore.scheduler.agent;

import java.io.File;

import nexcore.scheduler.entity.JobLogFilenameInfo;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 기본 Job 로그 파일 이외에 Sub log 파일을 읽어오기 위해 sublog 파일명을 조립하는 resolver</li>
 * <li>작성일 : 2013. 1. 31.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface ISubLogFilenameResolver {
	
	public File getSubLogFile(JobLogFilenameInfo filenameinfo);
}
