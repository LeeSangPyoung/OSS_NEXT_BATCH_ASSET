package nexcore.scheduler.entity;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;

import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 통지 설정 정보 </li>
 * <li>작성일 : 2011. 02. 05.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobNotify implements Serializable {
	private static final long serialVersionUID = 8504026366402668010L;
	
	private int                   id;
	private String                desc;
	private String                jobIdExpression;
	private Pattern               jobIdExpressionPattern;
	private String                when;
	private String                checkValue1;
	private String                checkValue2;
	private String                checkValue3;
	private String                receivers;
	private List<Integer>         receiverList;
	//private Timestamp             lastModifyTime;
	private long                  lastModifyTime;
		
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getJobIdExpression() {
		return jobIdExpression;
	}

	public void setJobIdExpression(String jobIdExpression) {
		this.jobIdExpression = jobIdExpression;
	}

	public String getWhen() {
		return when;
	}

	public void setWhen(String when) {
		this.when = when;
	}
	
	public String getCheckValue1() {
		return checkValue1;
	}

	public void setCheckValue1(String checkValue1) {
		this.checkValue1 = checkValue1;
	}

	public String getCheckValue2() {
		return checkValue2;
	}

	public void setCheckValue2(String checkValue2) {
		this.checkValue2 = checkValue2;
	}

	public String getCheckValue3() {
		return checkValue3;
	}

	public void setCheckValue3(String checkValue3) {
		this.checkValue3 = checkValue3;
	}

	public String getReceivers() {
		return receivers;
	}

	public void setReceivers(String receivers) {
		this.receivers = receivers;
		this.receiverList = Util.toListAsInt(receivers, ",");
	}

	public List<Integer> getReceiverList() {
		return receiverList;
	}
	/*
	public Timestamp getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(Timestamp lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}
	 */
	public String getLastModifyTime() {
		return DateUtil.getTimestampString(lastModifyTime);
	}

	public void setLastModifyTime(String lastModifyTime) {
		this.lastModifyTime = DateUtil.getTimestampLong(lastModifyTime);
	}

	public Pattern getJobIdExpressionPattern() {
		if (this.jobIdExpressionPattern == null) {
			this.jobIdExpressionPattern = Pattern.compile(jobIdExpression);
		}
		return this.jobIdExpressionPattern;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
