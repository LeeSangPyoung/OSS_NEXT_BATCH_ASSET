<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	int getIntValue(Object obj) {
		if (obj instanceof Integer) {
			return (Integer)obj;
		}else {
			return Integer.parseInt(String.valueOf(obj));
		}
	} 
%><%
    if (!checkLogin(request, response)) return;

	final ControllerAdminLocal  admin       = getControllerAdmin();

	// 조회 조건
    String searchdatetype           = nvl(request.getParameter("searchdatetype")).trim();
    String searchdatefrom           = nvl(request.getParameter("searchdatefrom")).trim();
    String searchdateto             = nvl(request.getParameter("searchdateto")).trim();
    String jobgroupid               = request.getParameter("jobgroupid");
    String jobgroupname             = request.getParameter("jobgroupname");
	boolean autoReload              = Util.toBoolean(request.getParameter("autoreload"), true);
	final boolean showNoInsGroup    = Util.toBoolean(request.getParameter("shownoinsgroup"), true); /* 인스턴스없는 그룹 포함? */
	final boolean failOnly          = Util.toBoolean(request.getParameter("failonly"), false); /* fail 건만 조회함 */
    final String  jobfilter         = request.getParameter("jobfilter");
	String orderby                  = nvl( request.getParameter("orderby"),  "getId");
    final String orderdir           = nvl( request.getParameter("orderdir"), "ASC");
	
	if (Util.isBlank(searchdatetype)) {
		searchdatetype = "activationDate";
       	String todayDate;
    	if (Util.getCurrentHHMMSS().compareTo(admin.getSystemConfigValue("DAILY_ACTIVATION_TIME")+"00") >=0) {
    		/* daily activation 이후 */ 
        	todayDate = Util.getCurrentYYYYMMDD();
    	}else {
    		/* daily activation 이전 */
    		todayDate = Util.getYesterdayYYYYMMDD();
    	}

		searchdatefrom = todayDate; /* jobgroup 모니터링은 사용자에 상관없이 당일 조회만한다.*/
		searchdateto   = Util.getCurrentYYYYMMDD();
	}
	
	/************************** Job Group 정보 조회 ***************************/
    List<JobGroupAttrDef> jobGroupAttrDefs = admin.getJobGroupAttrDefsByQuery("", " ORDER BY DISPLAY_ORDER ");

	Map queryParamMap = new HashMap();
    queryParamMap.put("groupIdLike",   Util.isBlank(jobgroupid)   ? null : jobgroupid+"%");
    queryParamMap.put("groupNameLike", Util.isBlank(jobgroupname) ? null : "%"+jobgroupname+"%");
	
    User user = getUser(request);
    if (!user.isAdmin()) {
    	queryParamMap.put("authorizedJobGroupIdViewList", user.getAuthList("VIEW_JOBGROUP"));
    }
    List<JobGroup>        jobGroupList     = admin.getJobGroupsByDynamicQuery(queryParamMap);

    /* sort */
    if (orderby.startsWith("attr_")) { /* 가변 속성으로 sort */
    	final String attrId = orderby.substring("attr_".length());
    	final boolean ascending = "ASC".equals(orderdir);
    	Collections.sort(jobGroupList, new Comparator() {
   			public int compare(Object o1, Object o2) {
   				JobGroup g1 = (JobGroup)o1;
   				JobGroup g2 = (JobGroup)o2;
   				if (ascending) {
   					return nvl(g1.getAttribute(attrId)).compareTo(nvl(g2.getAttribute(attrId)));
   				}else {
   					return nvl(g2.getAttribute(attrId)).compareTo(nvl(g1.getAttribute(attrId)));
   				}
   			}
   		});
    }else { /* 기본 속성으로 sort */
	    Collections.sort(jobGroupList, getComparator(orderby, "ASC".equals(orderdir)));
    }
    
    List<JobGroup>        jobGroupTreeList = admin.analyzeToJobGroupsTreeList(jobGroupList);
    
    final Map<String, JobGroupRunStats> jobGroupRunStatsMap = new HashMap();
    final Map<String, JobGroupRunStats> naJobGroupRunStatsMap = new HashMap();
    for (JobGroup jobgroup : jobGroupTreeList) {
    	jobGroupRunStatsMap.put(jobgroup.getId(), new JobGroupRunStats(jobgroup.getId()));
    }

    /************************** Job Instance 조회 *******************************/
	queryParamMap.clear();
	queryParamMap.put("jobGroupIdLike",   Util.isBlank(jobgroupid)   ? null : jobgroupid+"%");
	queryParamMap.put("jobGroupNameLike", Util.isBlank(jobgroupname) ? null : "%"+jobgroupname+"%");
	if ("activationDate".equals(searchdatetype)) { /* 생성일로 조회 */
		queryParamMap.put("activationTimeFrom",    searchdatefrom+"000000");
		queryParamMap.put("activationTimeTo",      searchdateto  +"235959");
	}else if ("procDate".equals(searchdatetype)) { /* 처리일로 조회 */
		queryParamMap.put("procDateFrom",    searchdatefrom);
		queryParamMap.put("procDateTo",      searchdateto);
	}

	if (!user.isAdmin()) { /* Admin 이 아닌 경우는 허가된 JobGroup만 조회 가능함. 등록되지 않은 경우 empty list, empty list 을 경우 전체 조회 */ 
		queryParamMap.put("authorizedJobGroupIdViewList", user.getAuthList("VIEW_JOBGROUP"));
	}

    /************** Job Instance 목록으로 집계 ********************/
	/* 대량 조회이므로 메모리 효율을 위해 RowHandler 방식으로 쿼리함. */
	RowHandler rowHandlerJobIns = new RowHandler() {
		public void handleRow(Object _row) {
			Map row = (Map)_row;
			if (!filterJobList((String)row.get("JOB_ID"), jobfilter)) {
				return;
			}
			JobInstance jobins = new JobInstance();
			jobins.setJobId(         (String)row.get("JOB_ID"));
			jobins.setJobGroupId(    (String)row.get("JOB_GROUP_ID"));
			jobins.setTimeFrom(      (String)row.get("TIME_FROM"));
			jobins.setTimeUntil(     (String)row.get("TIME_UNTIL"));
			jobins.setRunCount(      getIntValue(row.get("RUN_COUNT")));
			jobins.setEndOkCount(    getIntValue(row.get("END_OK_COUNT")));
			jobins.setJobState(      (String)row.get("JOB_STATE"));
			jobins.setLastJobExeId(  (String)row.get("LAST_JOB_EXE_ID"));
			jobins.setLastStartTime( (String)row.get("LAST_START_TIME"));
			jobins.setLastEndTime(   (String)row.get("LAST_END_TIME"));
	    	JobGroupRunStats runStats = jobGroupRunStatsMap.get(jobins.getJobGroupId());
	    	if (runStats == null) {
	    		runStats = naJobGroupRunStatsMap.get(jobins.getJobGroupId());
	    		if (runStats == null) {
	    			runStats = new JobGroupRunStats(jobins.getJobGroupId());
	    			naJobGroupRunStatsMap.put(jobins.getJobGroupId(), runStats);
	    		}
	    	}
	    	long[] progress = admin.getJobProgress(jobins.getLastJobExeId());
	    	runStats.sumToThis(jobins, getJobFilterCode(jobins.getJobId()), progress==null ? 0 : progress[0], progress==null ? 0 : progress[1]);
	    	if (!showNoInsGroup) {
	    		runStats.addJobDefId(jobins.getJobId(), jobins.getJobGroupId(), getJobFilterCode(jobins.getJobId()));
	    	}
		}
	};
	/* 성능을 위해 집계에 필요한 컬럼만 조회함 */
	queryParamMap.put("columnList", "JOB_ID, JOB_GROUP_ID, TIME_FROM, TIME_UNTIL, RUN_COUNT, END_OK_COUNT, JOB_STATE, LAST_JOB_EXE_ID, LAST_START_TIME, LAST_END_TIME"); 
	admin.getJobInstanceListFreeColumnWithRowHandler(queryParamMap, rowHandlerJobIns); 

	/************************** Job Definition 조회 *******************************/
	if (showNoInsGroup) {
	    queryParamMap.clear();
		queryParamMap.put("jobGroupIdLike",   Util.isBlank(jobgroupid)   ? null : jobgroupid+"%");
		queryParamMap.put("jobGroupNameLike", Util.isBlank(jobgroupname) ? null : "%"+jobgroupname+"%");
	
		if (!user.isAdmin()) { /* Admin 이 아닌 경우는 허가된 JobGroup만 조회 가능함. 등록되지 않은 경우 empty list, empty list 을 경우 전체 조회 */ 
			queryParamMap.put("authorizedJobGroupIdViewList", user.getAuthList("VIEW_JOBGROUP"));
		}
	
		/* 
		 * Job GroupId, Job ID 정보만 필요하다. 불필요한 메모리 낭비 방지.
		 * JobDef 는 JobIns 만큼 건수가 많지 않으므로 RowHandler 방식으로 하지는 않는다.
		 */ 
		queryParamMap.put("columnList", "JOB_ID, JOB_GROUP_ID");
		List<Map>     jobdefList  = admin.getJobDefinitionListFreeColumn(queryParamMap);
	
	    for (Map jobdefMap : jobdefList) {
	    	String jobId      = (String)jobdefMap.get("JOB_ID");
	    	String jobGroupId = (String)jobdefMap.get("JOB_GROUP_ID");
	    	if (!filterJobList(jobId, jobfilter)) {
	    		continue;
	    	}
	    	JobGroupRunStats runStats = jobGroupRunStatsMap.get(jobGroupId);
	    	if (runStats == null) {
	    		runStats = naJobGroupRunStatsMap.get(jobGroupId);
	    		if (runStats == null) {
	    			runStats = new JobGroupRunStats(jobGroupId);
	    			naJobGroupRunStatsMap.put(jobGroupId, runStats);
	    		}
	    	}
	    	runStats.addJobDefId(jobId, jobGroupId, getJobFilterCode(jobId));
	    }
	}
    
	request.setAttribute("jobGroupTreeList",       jobGroupTreeList);
	request.setAttribute("naJobGroupRunStatsMap",  naJobGroupRunStatsMap);
	request.setAttribute("jobGroupRunStatsMap",    jobGroupRunStatsMap);
	
%>

