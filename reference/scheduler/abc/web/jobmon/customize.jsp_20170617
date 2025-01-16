<%@page import="java.util.*" pageEncoding="euc-kr"
%><%@page import="nexcore.scheduler.entity.*"
%><%@page import="nexcore.scheduler.util.*"
%><%@page import="nexcore.scheduler.controller.admin.*"
%><%!
	/*********************************************************************/
	/*                         CUSTOMIZE 영역 시작                       */
	/*********************************************************************/

	/**
	 * ########## DO NOT EDIT THIS METHOD ##########
     *  Job 목록 조회시 화면에 print 하기 전에 이 메소드를 한번 호출하고 
     *  이 메소드가 true를 리턴하면 그 Job을 화면에 리스트함
     *  @param jobid 체크할 Job ID
     *  @param filterBy : 필터링 할 구분값. printJobFilter() 생성한 <select 태그의 값들. null일때는 모든 Job 출력함
     */
	boolean filterJobList(String jobid, String filterBy) {
		try {
			return Util.isBlank(filterBy) ? true : Util.equals( getJobFilterCode(jobid), filterBy );
		} catch (Exception e) {
			return true;
		}
	}

    /**
     * ########## DO NOT EDIT THIS METHOD ########## 
     * 조회 상단의 구분 <select> 태그를 print 함 
     * @param filter 현재 선택된 filter
     * @param request 사용자별로 동적으로 표시할 필요가 있을때 사용하기 위해 request 객체 전달
     */
	String printJobFilter(String filter, javax.servlet.http.HttpServletRequest request) {
        StringBuilder out = new StringBuilder(256);
		Map<String, String> jobFilterCodeList = getJobFilterCodeList(request);
		out.append(printSelectOption("", "전체", filter));
		for (Map.Entry<String, String> entry : jobFilterCodeList.entrySet()) {
			out.append(printSelectOption(entry.getKey(), entry.getValue(), filter));
		}
		return out.toString();
	}

    /**
     * Job ID 로 부터 filter 코드 값을 찾아냄.
     * @param jobid 
     */
	String getJobFilterCode(String jobid) {
		try {
			return jobid.substring(2, 5);
		} catch (Exception e) {
			return null;
		}
	}

    /**
     * Job ID filter 코드 리스트를 리턴함
     * @param request 사용자별로 다른 리스트를 보여줄 수 있게 하기 위해서는 request 객체를 활용할 수 있다.
     * @return Map<code, name>
     */
	Map getJobFilterCodeList(javax.servlet.http.HttpServletRequest request) {
		Map map = new LinkedHashMap();
/*
		map.put("999", "공통");
		map.put("028", "1은행");
		map.put("023", "2은행");
		map.put("406", "3은행");
		map.put("621", "4은행");
*/
		return map;
	}

    String getAppCode(String str) {
/* (예제)
		char bnkCode   = str.charAt(10);
        String appCode = str.substring(1,4);
        StringBuilder temp = new StringBuilder(12);
        temp.append("[");
        temp.append(appCode);
        temp.append("]");
        temp.append("[");
        switch(bnkCode) {
            case 'S' : temp.append("서울");break;
            case 'G' : temp.append("경기");break;
            case 'H' : temp.append("호남");break;
            case 'B' : temp.append("부산");break;
            default  : temp.append("공통");break;
        }
        temp.append("]");
        return temp.toString();
*/
		return "";
    }

	String printMigrateServerList() {
		StringBuilder sb = new StringBuilder();
		sb.append(printSelectOption("","",""));
		//sb.append(printSelectOption("1.1.1.1:8124","개발스케줄러",""));
		sb.append(printSelectOption("90.90.215.131:8124","스테이징스케줄러",""));
		//sb.append(printSelectOption("203.235.212.178:8124","운영스케줄러",""));
		return sb.toString();
	}
	
	String printMigrateAgentList() {
		StringBuilder sb = new StringBuilder();
		sb.append(printSelectOption("","",""));
		sb.append(printSelectOption("sbt01","sbt01",""));
		sb.append(printSelectOption("sbt02","sbt02",""));
		return sb.toString();
	}

    String localDatePattern          = "yyyy/MM/dd";
    String localDatetimePattern      = "yyyy/MM/dd HH:mm:ss";
    String localDatetimePatternSplit = "yyyy/MM/dd'<br'>HH:mm:ss";
//  String localDatetimePattern      = "MM/dd/yyyy HH:mm:ss";
//  String localDatetimePatternSplit = "MM/dd/yyyy'<br'>HH:mm:ss";
    int    descShortLimit            = 60;
    int    jobInsPageSize            = 200;
  
	int defaultDiagramNodeWidth        = 230;
	int defaultDiagramNodeHeight       = 80;
	int defaultDiagramNodeWidthSpace   = 40;
	int defaultDiagramNodeHeightSpace  = 60;

	boolean useSubLogForProcJobType      = false;
	boolean useMultiSuspendResumeButton  = false;
	
    boolean isRequirePasswordForJobAction(javax.servlet.http.HttpServletRequest request) {
    	return false;
    }

	boolean useEndFailAlert(javax.servlet.http.HttpServletRequest request) {
		return true;
	}

    String[] jobinsViewRefreshIntervalList = new String[] {"2", "10", "30", "60"};
    
	/*********************************************************************/
	/*                         CUSTOMIZE 영역 끝                         */
	/*********************************************************************/
%>