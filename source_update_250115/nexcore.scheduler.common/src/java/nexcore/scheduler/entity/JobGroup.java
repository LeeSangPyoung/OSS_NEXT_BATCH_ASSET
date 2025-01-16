package nexcore.scheduler.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nexcore.scheduler.util.DateUtil;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 그룹 관리에서 사용될 그룹 정보 </li>
 * <li>작성일 : 2013. 01. 14.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobGroup implements Serializable {
	private static final long serialVersionUID = -4659898276139338244L;
	
	private String                id;
	private String                name;
	private String                desc;
	private String                parentId = "ROOT";   // 부모 Job그룹ID
	private String                creatorId;           // 생성자 ID. (관리자 또는 운영자)
	private String                ownerId;             // 담당자 ID. (주로 PL급 운영자)
	private long                  createTime;          // YYYYMMDDHHMMSS
	private long                  lastModifyTime;      // YYYYMMDDHHMMSS
	
	private int                   depth;               // 트리구조에서 깊이 값.
	
	private List<JobGroupAttr>    attributeList = new ArrayList();  // 속성값들
	
	public JobGroup() {
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
		if (attributeList != null && attributeList.size() > 0) {
			for (JobGroupAttr attr : attributeList) {
				attr.setGroupId(id);
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getParentId() {
		return parentId;
	}
	
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	public String getCreatorId() {
		return creatorId;
	}
	
	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}
	
	public String getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	/*
	public Timestamp getCreateTime() {
		return createTime == 0 ? null : DateUtil.getTimestamp(createTime);
	}
	
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime==null ? 0 : createTime.getTime();
	}
	
	public Timestamp getLastModifyTime() {
		return lastModifyTime == 0 ? null : DateUtil.getTimestamp(lastModifyTime);
	}

	public void setLastModifyTime(Timestamp lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : lastModifyTime.getTime();
	}
	 */
	public String getCreateTime() {
		return createTime == 0 ? null : DateUtil.getTimestampString(createTime);
	}
	
	public void setCreateTime(String createTime) {
		this.createTime = createTime==null ? 0 : DateUtil.getTimestampLong(createTime);
	}
	
	public String getLastModifyTime() {
		return lastModifyTime == 0 ? null : DateUtil.getTimestampString(lastModifyTime);
	}

	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : DateUtil.getTimestampLong(lastModifyTime);
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public List<JobGroupAttr> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(List<JobGroupAttr> attributeList) {
		if (attributeList == null) {
			this.attributeList = new ArrayList<JobGroupAttr>();
		}
	}
	
	public String getAttribute(String attrId) {
		JobGroupAttr newAttr = new JobGroupAttr(this.id, attrId, null);
		int idx = attributeList.indexOf(newAttr); 
		return idx > -1 ? attributeList.get(idx).getValue() : null;
	}
	
	public void setAttribute(String attrId, String attrValue) {
		/* 속성ID 는 PK 이므로 중복되지 않도록 한다. */
		JobGroupAttr newAttr = new JobGroupAttr(this.id, attrId, attrValue);
		int oldIdx = attributeList.indexOf(newAttr); // JobGroupAttr.equals() 안에서는 id 만 가지고 equals 비교한다.
		if (oldIdx > -1) {
			attributeList.set(oldIdx, newAttr);
		}else {
			attributeList.add(newAttr);
		}
	}
	
	public void clearAttributes() {
		attributeList.clear();
	}

	public String toString() {
		return "[JobGroup: id="+id+", name="+name+", desc="+desc+", parentId="+parentId+", creatorId="+creatorId+
			", ownerId="+ownerId+", createTime="+createTime+", lastModifyTime="+lastModifyTime +", attrList="+attributeList+"]";
	}

}
