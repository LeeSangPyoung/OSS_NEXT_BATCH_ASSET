package nexcore.scheduler.entity;

import java.io.Serializable;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 그룹 관리에서 사용될 그룹 속성 </li>
 * <li>작성일 : 2013. 01. 14.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobGroupAttr implements Serializable {
	private static final long serialVersionUID = -8121396374420626896L;
	
	private String     groupId; // owner group id
	private String     id;      // attr id
	private String     value;   // attr value
	
	public JobGroupAttr() {
	}

	public JobGroupAttr(String groupId, String id, String value) {
		this.groupId = groupId;
		this.id      = id;
		this.value   = value;
	}
	
	public boolean equals(Object obj) { // 키가 같으면 같은 것으로 본다.
		return id.equals(((JobGroupAttr)obj).id);
	}
	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString() {
		return "[JobGroupAttr: groupId="+groupId+", attrId="+id+", attrValue="+value+"]";
	}

}
