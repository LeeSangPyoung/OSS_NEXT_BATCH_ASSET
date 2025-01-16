package nexcore.scheduler.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import nexcore.scheduler.util.DateUtil;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 뷰 필터 기능</li>
 * <li>작성일 : 2011. 1. 5.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ViewFilter implements Serializable {
	private static final long serialVersionUID = 9026654503987688593L;
	
	private int             id;              // 생성시 자동생성되는 일련번호
	private String          name;            // 필터명
	private String          team;            // 팀명
	private String          owner;           // 담당자
	private String          description;     // 필터 설명
	private int             jobCount;        // 크기. 아래 jobIdList.size() 와 같은 값이지만, Deep 조회하지 않더라도 size 는 DB에서 가져온다.
	private List<String>    jobIdList;       // Job Id 목록  
	private long            lastModifyTime;
	
	public ViewFilter() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getJobCount() {
		return jobCount;
	}

	public void setJobCount(int jobCount) {
		this.jobCount = jobCount;
	}

	public List<String> getJobIdList() {
		return jobIdList == null ? jobIdList = new ArrayList<String>() : jobIdList;
	}

	public void setJobIdList(List<String> jobIdList) {
		this.jobIdList = jobIdList;
		this.jobCount  = jobIdList == null ? 0 : jobIdList.size();
	}
	/*
	public Timestamp getLastModifyTime() {
		return lastModifyTime==0 ? null : DateUtil.getTimestamp(lastModifyTime);
	}

	public void setLastModifyTime(Timestamp lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : lastModifyTime.getTime();
	}
	
	*/
	public String getLastModifyTime() {
		return lastModifyTime==0 ? null : DateUtil.getTimestampString(lastModifyTime);
	}

	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : DateUtil.getTimestampLong(lastModifyTime);
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
