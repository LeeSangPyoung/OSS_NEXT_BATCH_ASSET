/**
 * 
 */
package nexcore.scheduler.util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : JobInstance 조회 화면에서 페이지 navigation bar 를 print 하기 위해 필요한 유틸 </li>
 * <li>작성일 : 2012. 9. 3.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class WebPagingNavigator {
	// INPUT 변수
	private int screenSize       = 10;     // 한 스크린 size. 한 화면에 표시되는 page 번호 최대값. 기본:10
	private int itemSizePerPage  = 100;    // 한 페이지에 표시된 row 개수. 기본 : 300
	private int totalItemCount;            // 전체 item 건수
	private int currPageNo;                // 현재 페이지 번호
	
	// 계산된 변수
	private int maxPageNo;                 // 최대 페이지 번호. totalItemCount 로부터 계산된
	private int startPageNo;               // 스크린 내의 시작 페이지 번호
	private int endPageNo;                 // 스크린 내의 끝 페이지 번호
	private int skipItemCount;             // currPage를 표시하기 위해 조회(또는 출력)시 skip 해야할 item 개수. 1페이지에서는 skipItemCount = 0;
	private int startItemNoForCurrPage;    // currPage를 표시할때 사용할 첫 항목의 출력 번호. 

	public WebPagingNavigator() {
	}

	public int getScreenSize() {
		return screenSize;
	}

	public void setScreenSize(int screenSize) {
		this.screenSize = screenSize;
	}

	public int getItemSizePerPage() {
		return itemSizePerPage;
	}

	public void setItemSizePerPage(int itemSizePerPage) {
		this.itemSizePerPage = itemSizePerPage;
	}

	public int getTotalItemCount() {
		return totalItemCount;
	}

	public void setTotalItemCount(int totalItemCount) {
		this.totalItemCount = totalItemCount;
	}

	public int getCurrPageNo() {
		return currPageNo;
	}

	public void setCurrPageNo(int currPageNo) {
		this.currPageNo = currPageNo;
	}

	public int getMaxPageNo() {
		return maxPageNo;
	}

	public int getStartPageNo() {
		return startPageNo;
	}

	public int getEndPageNo() {
		return endPageNo;
	}
	
	public int getSkipItemCount() {
		return skipItemCount;
	}
	
	public int getStartItemNoForCurrPage() {
		return startItemNoForCurrPage;
	}
	
	public void calculate() {
		maxPageNo      = (int)Math.ceil((double)totalItemCount / (double)itemSizePerPage); 
		skipItemCount  = (currPageNo-1) * itemSizePerPage;

		if (currPageNo < (screenSize/2)) { // 첫 페이지
		    startPageNo = 1;
		    endPageNo   = Math.min(screenSize, maxPageNo);
		}else if (currPageNo > maxPageNo - (screenSize/2)) { // 마지막 페이지
		    startPageNo = Math.max(1, maxPageNo - screenSize + 1);
		    endPageNo   = maxPageNo;
		}else {
		    startPageNo = currPageNo - ((screenSize/2)-1);
		    endPageNo   = currPageNo + (screenSize/2);
		}

		startItemNoForCurrPage = skipItemCount + 1;
	}
	
	
	
	public static void main(String[] args) {
		WebPagingNavigator p = new WebPagingNavigator();
		p.setTotalItemCount(1000);
		p.setItemSizePerPage(300);
		p.calculate();
		
		
		System.out.println(p.getMaxPageNo());
		
	}
	
}
