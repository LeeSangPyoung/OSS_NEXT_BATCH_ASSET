package nexcore.scheduler.agent.nsc;

import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : NSC 통신 헤어 </li>
 * <li>작성일 : 2012. 9. 12.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class NSCHeader {
	// 헤더 항목
	private String     totalLen;           // char(8) : 전문전체 길이 - 8. 자신은 미포함. 
	private String     trKind;             // char(4) : 전문종류.              [1000:배치상태조회, 2000:배치상태변경, 3000:배치관리]
	private String     rsFlag;             // char(1) : 송수신구분.            [0:요청, 1:응답]
	private String     clientType = "S";   // char(1) : 클라이언트 타임.       [A:운영도구, S:스케줄러. 기본값]
	private String     arrayCount;         // char(4) : 본문 배열(키값) 개수.  [0000:없음, 0001~9999:있음]
	
	// 필드.
	private int        totalLenInt;
	private int        arrayCountInt;

	public String getTotalLen() {
		return totalLen;
	}

	public void setTotalLen(String totalLen) {
		this.totalLen = totalLen;
		this.totalLenInt = Util.toInt(totalLen);
	}
	
	public int getTotalLenInt() {
		return totalLenInt;
	}

	public void setTotalLenInt(int totalLenInt) {
		this.totalLenInt = totalLenInt;
		this.totalLen = String.format("%08d", totalLenInt);
	}

	public String getTrKind() {
		return trKind;
	}

	public void setTrKind(String trKind) {
		this.trKind = trKind;
	}

	public String getRsFlag() {
		return rsFlag;
	}

	public void setRsFlag(String rsFlag) {
		this.rsFlag = rsFlag;
	}

	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	public String getArrayCount() {
		return arrayCount;
	}

	public void setArrayCount(String arrayCount) {
		this.arrayCount = arrayCount;
		this.arrayCountInt = Util.toInt(arrayCount);
	}

	public int getArrayCountInt() {
		return arrayCountInt;
	}

	public void setArrayCountInt(int arrayCountInt) {
		this.arrayCountInt = arrayCountInt;
		this.arrayCount    = String.format("%04d", arrayCountInt);
	}  
	
}
