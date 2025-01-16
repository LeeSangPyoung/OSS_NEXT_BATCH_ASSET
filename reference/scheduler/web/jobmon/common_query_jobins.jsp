<%@page language="java" pageEncoding="euc-kr"
%><%@include file= "common_functions.jsp" 
%><%!
	String jobStateOrderBySQL = 
		"(case JOB_STATE "+
		" when 'R' then 0 "+
		" when 'P' then 1 "+
		" when 'F' then 2 "+
		" when 'O' then 3 "+
		" when 'S' then 4 "+
		" when 'W' then 5 "+
		" when 'I' then 6 "+
		" when 'G' then 7 "+
		" when 'X' then 8 "+
		" else 10 end)";
%><%
    if (!checkLogin(request, response)) return;
	
    // 조회 조건
	String lastretcode        = request.getParameter("lastretcode");
    String jobstate           = request.getParameter("jobstate");
	String jobinstanceid      = nvl(request.getParameter("jobinstanceid")).trim();
	String jobgroup           = nvl(request.getParameter("jobgroup")).trim();
	String jobdesc            = nvl(request.getParameter("jobdesc")).trim();
	String component          = nvl(request.getParameter("component")).trim();
    String jobtype            = nvl(request.getParameter("jobtype")).trim();
    String agentid            = nvl(request.getParameter("agentid")).trim();
    String prejobid           = nvl(request.getParameter("prejobid")).trim();
    String triggerjobid       = nvl(request.getParameter("triggerjobid")).trim();
    String searchdatetype     = nvl(request.getParameter("searchdatetype")).trim();
    String searchdatefrom     = nvl(request.getParameter("searchdatefrom")).trim();
    String searchdateto       = nvl(request.getParameter("searchdateto")).trim();
    String owner              = request.getParameter("owner");
    String viewfilter         = request.getParameter("viewfilter");
    String jobfilter          = (jobfilter=nvl(request.getParameter("jobfilter"))).length() == 0 ? null : jobfilter;
    String orderby            = nvl(request.getParameter("orderby"), "LAST_MODIFY_TIME");
    String orderdir           = nvl(request.getParameter("orderdir"), "DESC"); // ASC, DESC
    String orderSQL           = "ORDER BY "+("JOB_STATE".equals(orderby) ? jobStateOrderBySQL : orderby)+" "+orderdir;
    String lastModifyTimeFrom = request.getParameter("lastmodifytimefrom");
    int    currPageNo         = Util.toInt(request.getParameter("currpageno"), 1);
    int    userJobInsPageSize = Util.toInt(request.getParameter("jobins_page_size"), jobInsPageSize);
	boolean deepQuery         = Util.toBoolean(request.getParameter("deep_query"));  // 선행 Job, 파라미터 조회 여부

	Map queryParamMap = new HashMap();
	queryParamMap.put("viewFilterId",        toInt(viewfilter,-1));
	if (!"%".equals(jobinstanceid)) { /* PK 이므로 값이 % 일때는 PK 를 못타게 검색 조건에서 아예 뺀다. */
		queryParamMap.put("jobInstanceIdLike",   Util.isBlank(jobinstanceid) ? null : jobinstanceid+"%");
	}
	queryParamMap.put("jobGroupIdLike",      Util.isBlank(jobgroup)      ? null : jobgroup+"%");
	queryParamMap.put("jobDescLike",         Util.isBlank(jobdesc)       ? null : "%"+jobdesc+"%");
	queryParamMap.put("componentNameLike",   Util.isBlank(component)     ? null : "%"+component+"%");
	queryParamMap.put("jobType",             jobtype);
	queryParamMap.put("preJobIdLike",        Util.isBlank(prejobid)      ? null : prejobid+"%");
	queryParamMap.put("triggerJobIdLike",    Util.isBlank(triggerjobid)  ? null : triggerjobid+"%");
	queryParamMap.put("lastRetCode",         lastretcode);
	queryParamMap.put("agentId",             "%"+agentid+"%");
	queryParamMap.put("jobState",            jobstate);
	queryParamMap.put("ownerLike",           Util.isBlank(owner)         ? null : "%"+owner+"%");

	if ("activationDate".equals(searchdatetype)) { /* 생성일로 조회 */
		queryParamMap.put("activationTimeFrom",    searchdatefrom+"000000");
		queryParamMap.put("activationTimeTo",      searchdateto  +"235959");
	}else if ("procDate".equals(searchdatetype)) { /* 처리일로 조회 */
		queryParamMap.put("procDateFrom",    searchdatefrom);
		queryParamMap.put("procDateTo",      searchdateto);
	}

	if (!Util.isBlank(lastModifyTimeFrom) && !"0".equals(lastModifyTimeFrom)) {
		  queryParamMap.put("lastModifyTimeFrom", DateUtil.getTimestampString(Util.toLong(lastModifyTimeFrom)-1000)); /* 1 초 오차 보정 */
	}
	queryParamMap.put("orderBy",             orderSQL);

	User user = getUser(request);
	if (!user.isAdmin()) { /* Admin 이 아닌 경우는 허가된 JobGroup만 조회 가능함. 등록되지 않은 경우 empty list, empty list 을 경우 전체 조회 */ 
		queryParamMap.put("authorizedJobGroupIdViewList", user.getAuthList("VIEW_JOBGROUP"));
	}

	ControllerAdminLocal  admin         = getControllerAdmin();
	List<JobInstance>     jobinsList    = null;
	int                   totalCount    = -1;
	Object                rowHandler    = request.getAttribute("rowHandler"); /* 엑셀 다운로드시에는 패이징하지 않고 full 조회 하며, 대량 조회이므로 rowHandler 를 사용한다. */ 
	if (!Util.isBlank(lastModifyTimeFrom) && !"0".equals(lastModifyTimeFrom)) { /* 변경분만 조회시에는 페이징하지 않는다. */
		jobinsList = admin.getJobInstanceList(queryParamMap, deepQuery);
	}else if (rowHandler != null ) {
		/* 엑셀 다운로드 */
		admin.getJobInstanceListWithRowHandler(queryParamMap, rowHandler);
	}else {
	    int skip = (currPageNo-1) * userJobInsPageSize;
	    jobinsList = admin.getJobInstanceList(queryParamMap, deepQuery, skip, userJobInsPageSize);
        totalCount = admin.getJobInstanceCount(queryParamMap);
	}

	if (jobinsList != null) {
	    // 검색 결과를 jobfilter로 걸러냄.
	    Iterator iter = jobinsList.iterator();
	    while(iter.hasNext()) {
	    	JobInstance jobins = (JobInstance)iter.next();
	        if (!filterJobList(jobins.getJobId(), jobfilter)) {
	            iter.remove();
	        }
	    }
	    
	    request.setAttribute("jobins_query_result", jobinsList);
	    if (totalCount > -1) {
	        request.setAttribute("jobins_query_total", new Integer(totalCount));
	    }
	}
%>