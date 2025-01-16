package nexcore.scheduler.entity;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import nexcore.scheduler.msg.MSG;


/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 선행 Job 조건 객체. Job Instance, Job Definition 의 member </li>
 * <li>작성일 : 2010. 5. 6.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class PreJobCondition implements Serializable {
	private static final long serialVersionUID = 82202101288368572L;

	public static final String END_OK                 = "OK";                  // OK 이면 true
	public static final String END_FAIL               = "FAIL";                // FAIL 이면 true
	public static final String END_OKFAIL             = "OKFAIL";              // OK 이던 FAIL 이던 END 이기만하면 true
	public static final String INS_EXIST              = "INSEXIST";            // 인스턴스가 있으면 true
	public static final String INS_NONE               = "INSNONE";             // 인스턴스가 없으면 true
	public static final String END_OK_OR_INS_NONE 	  = "OK_OR_INSNONE";       // OK 이거나 인스턴스 없으면 true
	public static final String END_FAIL_OR_INS_NONE   = "FAIL_OR_INSNONE";     // FAIL 이거나 인스턴스 없으면 true
	public static final String END_OKFAIL_OR_INS_NONE = "OKFAIL_OR_INSNONE";   // END 이거나 인스턴스 없으면 true
	
	// 2013-06-03. 멀티트리거의 전체 child 가 End 되는 것을 wait 하기 위한 기능.
	public static final String ALLINS_END_OK          = "ALLINS_OK";           // 모든 인스턴스가 End OK 이면 true
	public static final String ALLINS_END_FAIL        = "ALLINS_FAIL";         // 모든 인스턴스가 End FAIL 이면 true
	public static final String ALLINS_END_OKFAIL      = "ALLINS_OKFAIL";       // 모든 인스턴스가 OK/Fail 상관없이 End 이면 true
	
	protected String  preJobId;  // 선행 Job
	protected String  okFail;    // 선행 Job 이 OK 일때? /  Fail 일때?    ["OK", "FAIL", "OKFAIL", "INSEXIST", "INSNONE", "OK_OR_INSNONE", "FAIL_OR_INSNONE", "OKFAIL_OR_INSNONE"]. (2011-03-03) 다음 두개 추가됨 ["INSNONE":인스턴스 미존재, "INSEXIST":인스턴스 존재]
	protected String  andOr;     // 여러개 선행 Job 일때 and / or 조건.   ["AND", "OR"]
	
	public PreJobCondition() {
	}
	
	public PreJobCondition(String preJobId, String okFail, String andOr) {
		this.preJobId = preJobId;
		this.okFail = okFail;
		this.andOr = andOr;
	}

	public PreJobCondition(Map<String, String> mapFromSQL) {
		this.preJobId = mapFromSQL.get("PRE_JOB_ID");
		this.okFail   = mapFromSQL.get("OK_FAIL");
		this.andOr    = mapFromSQL.get("AND_OR");
	}

	public String getPreJobId() {
		return preJobId;
	}

	public void setPreJobId(String preJobId) {
		this.preJobId = preJobId;
	}

	public String getOkFail() {
		return okFail;
	}

	public void setOkFail(String okFail) {
		this.okFail = okFail;
	}

	public String getAndOr() {
		return andOr;
	}

	public void setAndOr(String andOr) {
		this.andOr = andOr;
	}
	
	public String getOkFailText() {
		return getOkFailText(okFail);
	}
	
	public static String getOkFailText(String okfail) {
		return MSG.get("main.prejob.cond."+okfail);
	}

	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof PreJobCondition) {
			PreJobCondition pjc = (PreJobCondition)obj;

			return 
				this.preJobId != null && this.preJobId.equals(pjc.preJobId) &&
				this.okFail   != null && this.okFail.  equals(pjc.okFail) &&
				this.andOr    != null && this.andOr.   equals(pjc.andOr);
		}else {
			return false;
		}
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
