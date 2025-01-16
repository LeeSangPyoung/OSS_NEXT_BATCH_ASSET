/**
 * 
 */
package nexcore.scheduler.agent.nsc;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : NSC 통신에서 </li>
 * <li>작성일 : 2012. 9. 12.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class NSCJobStatus {

	public static final String PROC_STATE_INIT             = "0";
	public static final String PROC_STATE_RUNNING          = "1";
	public static final String PROC_STATE_BEFORE_SUSPENDED = "2";
	public static final String PROC_STATE_SUSPENDED        = "3";
	public static final String PROC_STATE_AFTER_SUSPENDED  = "4";
	public static final String PROC_STATE_EXIT_NORMAL      = "5";
	public static final String PROC_STATE_EXIT_FORCED      = "6";
	public static final String PROC_STATE_EXIT_DEL_MEM     = "7";
	
	private String jobExeId;           // char(50). Job Execution Id
	private String startTime;          // char(14). YYYYMMDDHHMMSS
	private String endTime;            // char(14). YYYYMMDDHHMMSS
	private String type;               // char(1).  배치 타입 [N:일반배치, C:연속배치, O:온디멘드배치, D:데몬배치]
	private String memStatus;          // char(1).  메모리 상태 플래그 [0:미할당, 1:할당, 2:반납접수, 3:반납]
	private String procStatus;         // char(1).  프로세스 상태 플래그 [0:대기, 1:동작, 2:중지전, 3:중지, 4:중지후, 5:정상종료, 6:강제종료, 7:종료확정(메모리반납)]
	private long   progressTotal;      // long.     전체건수
	private long   progressCurrent;    // long.     현재처리중 건수
	private String etc;                // char(31). 예비필드
	
	public NSCJobStatus() {
	}
	
	public String getJobExeId() {
		return jobExeId;
	}
	public void setJobExeId(String jobExeId) {
		this.jobExeId = jobExeId;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMemStatus() {
		return memStatus;
	}
	public void setMemStatus(String memStatus) {
		this.memStatus = memStatus;
	}
	public String getProcStatus() {
		return procStatus;
	}
	public void setProcStatus(String procStatus) {
		this.procStatus = procStatus;
	}
	public long getProgressTotal() {
		return progressTotal;
	}
	public void setProgressTotal(long progressTotal) {
		this.progressTotal = progressTotal;
	}
	public long getProgressCurrent() {
		return progressCurrent;
	}
	public void setProgressCurrent(long progressCurrent) {
		this.progressCurrent = progressCurrent;
	}
	public String getEtc() {
		return etc;
	}
	public void setEtc(String etc) {
		this.etc = etc;
	}
	
	
	
	
}
