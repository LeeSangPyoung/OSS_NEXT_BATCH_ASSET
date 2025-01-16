package nexcore.scheduler.entity;

import java.io.Serializable;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 로그 파일 조회시, 파일의 위치, 길이 internal 여부 등을 담는 VO. </li>
 * <li>작성일 : 2012. 11. 29.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobLogFileLocation implements Serializable {
	private static final long serialVersionUID = -3221756250427574470L;
	
	private String  agentId;
	private boolean isPeerInternal; // 해당 에이전트가 peer 의 internal 인지?
	private String  filename;
	private long    length;
	private String  encoding;   // 해당 에이전트의 job log encoding.
	
	public JobLogFileLocation() {
	}
	
	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	public boolean isPeerInternal() {
		return isPeerInternal;
	}
	public void setPeerInternal(boolean isPeerInternal) {
		this.isPeerInternal = isPeerInternal;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
	
	
	
}
