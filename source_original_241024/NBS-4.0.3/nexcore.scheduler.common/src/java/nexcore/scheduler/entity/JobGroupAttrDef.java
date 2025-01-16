package nexcore.scheduler.entity;

import java.io.Serializable;

import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 그룹 속성 정의 </li>
 * <li>작성일 : 2013. 01. 14.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobGroupAttrDef implements Serializable {
	private static final long serialVersionUID = -6945210668300204089L;
	
	private String                id;
	private String                name;
	private String                desc;              /* 속성 설명                               */
	private String                valueType;         /* 값 타입 (TEXT, TEXTAREA, LIST)          */
	private String                valueCheck;        /* 값의 validation 유효값(최대값, 목록값)  */
	private int                   displayLine;       /* 표시 라인수                             */
	private boolean               displayMonitor;    /* 모니터링 화면 표시 여부                 */
	private int                   displayOrder;      /* 표시 순서                               */
	private long                  lastModifyTime;
	
	public JobGroupAttrDef() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getValueCheck() {
		return valueCheck;
	}

	public void setValueCheck(String valueCheck) {
		this.valueCheck = valueCheck;
	}

	public int getDisplayLine() {
		return displayLine;
	}

	public void setDisplayLine(int displayLine) {
		this.displayLine = displayLine;
	}

	public boolean isDisplayMonitor() {
		return displayMonitor;
	}

	public void setDisplayMonitor(boolean displayMonitor) {
		this.displayMonitor = displayMonitor;
	}
	
	public String getDisplayMonitorYN() {
		return Util.toString(displayMonitor);
	}

	public void setDisplayMonitorYN(String displayMonitor) {
		this.displayMonitor = Util.toBoolean(displayMonitor);
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
	/*
	public Timestamp getLastModifyTime() {
		return lastModifyTime == 0 ? null : DateUtil.getTimestamp(lastModifyTime);
	}

	public void setLastModifyTime(Timestamp lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : lastModifyTime.getTime();
	}
	*/
	public String getLastModifyTime() {
		return lastModifyTime == 0 ? null : DateUtil.getTimestampString(lastModifyTime);
	}

	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = lastModifyTime==null ? 0 : DateUtil.getTimestampLong(lastModifyTime);
	}
	
}
