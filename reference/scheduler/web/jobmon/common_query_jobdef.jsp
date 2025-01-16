<%@page language="java" pageEncoding="euc-kr"
%><%@include file= "common.jsp" 
%><%
    if (!checkLogin(request, response)) return;
	
    String jobid        = null;
    String jobgroup     = null;
    String owner        = null;
    String jobtype      = null;
    String agentid      = null;
    String component    = null;
    String desc         = null;
    String prejobid     = null;
    String triggerjobid = null;
    String viewfilter   = null;
    String jobfilter    = null;
    String plandate     = null;

    ControllerAdminLocal admin = getControllerAdmin();
    
    String orderby      = nvl(request.getParameter("orderby"),  "jobId");
    String orderdir     = nvl(request.getParameter("orderdir"), "ASC");     // ASC, DESC
    
    boolean deepQuery   = Util.toBoolean(request.getParameter("deep_query"));  // 선행 Job, 파라미터 조회 여부
    
    List<JobDefinition> jobdefList = null;
    
    if (!Util.isBlank(request.getQueryString())) { /* 검색조건 없이 처음 클릭시 */
        jobid        = nvl(request.getParameter("jobid")).trim();
        jobgroup     = nvl(request.getParameter("jobgroup")).trim();
        owner        = nvl(request.getParameter("owner")).trim();
        jobtype      = nvl(request.getParameter("jobtype")).trim();
        agentid      = nvl(request.getParameter("agentid")).trim();
        component    = nvl(request.getParameter("component")).trim();
        desc         = nvl(request.getParameter("desc")).trim();
        prejobid     = nvl(request.getParameter("prejobid")).trim();
        triggerjobid = nvl(request.getParameter("triggerjobid")).trim();
        viewfilter   = nvl(request.getParameter("viewfilter"));
        jobfilter    = (jobfilter=nvl(request.getParameter("jobfilter"))).length() == 0 ? null : jobfilter;
        plandate     = nvl(request.getParameter("plandate"));

    	Map queryParamMap = new HashMap();
   		queryParamMap.put("viewFilterId",        toInt(viewfilter,-1));
    	queryParamMap.put("jobIdLike",           Util.isBlank(jobid)         ? null : jobid+"%");
   		queryParamMap.put("jobGroupIdLike",      Util.isBlank(jobgroup)      ? null : jobgroup+"%");
   		queryParamMap.put("componentNameLike",   Util.isBlank(component)     ? null : "%"+component+"%");
   		queryParamMap.put("jobDescLike",         Util.isBlank(desc)          ? null : "%"+desc+"%");
   		queryParamMap.put("jobType",             jobtype);
   		queryParamMap.put("preJobIdLike",        Util.isBlank(prejobid)      ? null : prejobid+"%");
   		queryParamMap.put("triggerJobIdLike",    Util.isBlank(triggerjobid)  ? null : triggerjobid+"%");
   		queryParamMap.put("agentId",             "%"+agentid+"%");
   		queryParamMap.put("ownerLike",           Util.isBlank(owner)         ? null : "%"+owner+"%");
   		
   		User user = getUser(request);
   		if (!user.isAdmin()) { /* Admin 이 아닌 경우는 허가된 JobGroup만 조회 가능함. 등록되지 않은 경우 empty list, empty list 을 경우 전체 조회 */ 
   			queryParamMap.put("authorizedJobGroupIdViewList", user.getAuthList("VIEW_JOBGROUP"));
   		}

        jobdefList = admin.getJobDefinitionList(queryParamMap, deepQuery);

		if (!Util.isBlank(plandate)) { /* 예정일 (PLAN_DATE) 이 조건에 들어있는 경우는 여기서 한번 걸러낸 */ 
	        Util.checkDateYYYYMMDD(plandate);
        	Iterator<JobDefinition> iter = jobdefList.iterator();
        	while(iter.hasNext()) {
        		JobDefinition jobdef = iter.next();
				try { 
	        		if (!admin.isScheduledDay(jobdef,plandate)) {
	        			iter.remove();
	        		}
				}catch(Exception eee) {
					out.println("<br><b>ERROR:</b>"+eee.toString()+"<br>"+jobdef+"<br>");
				}
        	}
        }
    }else {
        jobdefList = new ArrayList();
    }
    
    // 검색 결과를 jobfilter로 걸러냄.
    Iterator iter = jobdefList.iterator();
    while(iter.hasNext()) {
        JobDefinition jobdef = (JobDefinition)iter.next();
        if (!filterJobList(jobdef.getJobId(), jobfilter)) {
            iter.remove();
        }
    }

	/* 정렬함 */
	JobUtil.sort(jobdefList, orderby);
	if ("DESC".equals(orderdir)) {
		Collections.reverse(jobdefList);
	}

	if (!"true".equals(request.getParameter("_IS_AJAX_"))) {
	    /* 조회 결과는 includer 페이지에서 사용된다. */
	    request.setAttribute("jobdef_query_result", jobdefList);
	} else {
	    StringBuffer result = new StringBuffer();
	    
	    result.append("{\"list1\":[");
	    int i=0;
	    for (JobDefinition jobdef : jobdefList) {
	    	if (i>0) result.append(",");
	    	result.append("\"" + jobdef.getJobId() + "\"");
	    	i++;
	    }
	    result.append("]");
	    
	    result.append(",\"list2\":[");
	    i=0;
	    for (JobDefinition jobdef : jobdefList) {
	    	if (i>0) result.append(",");
	    	result.append("{");
	    	result.append("\"jobid\":\"" + jobdef.getJobId() + "\",");
			result.append("\"desc\":\"" + jobdef.getDescription() + "\"");
			result.append("}");
	    	i++;
	    }
	    result.append("]}");
	    
	    response.setContentType("application/json;charset=UTF-8");
	    response.getWriter().print(result.toString());
	}
%>