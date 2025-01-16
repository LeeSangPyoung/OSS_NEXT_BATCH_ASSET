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

	// ��ȸ ����
    String searchdatetype           = nvl(request.getParameter("searchdatetype")).trim();
    String searchdatefrom           = nvl(request.getParameter("searchdatefrom")).trim();
    String searchdateto             = nvl(request.getParameter("searchdateto")).trim();
    String jobgroupid               = request.getParameter("jobgroupid");
    String jobgroupname             = request.getParameter("jobgroupname");
	boolean autoReload              = Util.toBoolean(request.getParameter("autoreload"), true);
	final boolean showNoInsGroup    = Util.toBoolean(request.getParameter("shownoinsgroup"), true); /* �ν��Ͻ����� �׷� ����? */
	final boolean failOnly          = Util.toBoolean(request.getParameter("failonly"), false); /* fail �Ǹ� ��ȸ�� */
    final String  jobfilter         = request.getParameter("jobfilter");
	String orderby                  = nvl( request.getParameter("orderby"),  "getId");
    final String orderdir           = nvl( request.getParameter("orderdir"), "ASC");
	
	if (Util.isBlank(searchdatetype)) {
		searchdatetype = "activationDate";
       	String todayDate;
    	if (Util.getCurrentHHMMSS().compareTo(admin.getSystemConfigValue("DAILY_ACTIVATION_TIME")+"00") >=0) {
    		/* daily activation ���� */ 
        	todayDate = Util.getCurrentYYYYMMDD();
    	}else {
    		/* daily activation ���� */
    		todayDate = Util.getYesterdayYYYYMMDD();
    	}

		searchdatefrom = todayDate; /* jobgroup ����͸��� ����ڿ� ������� ���� ��ȸ���Ѵ�.*/
		searchdateto   = Util.getCurrentYYYYMMDD();
	}
	
	/************************** Job Group ���� ��ȸ ***************************/
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
    if (orderby.startsWith("attr_")) { /* ���� �Ӽ����� sort */
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
    }else { /* �⺻ �Ӽ����� sort */
	    Collections.sort(jobGroupList, getComparator(orderby, "ASC".equals(orderdir)));
    }
    
    List<JobGroup>        jobGroupTreeList = admin.analyzeToJobGroupsTreeList(jobGroupList);
    
    final Map<String, JobGroupRunStats> jobGroupRunStatsMap = new HashMap();
    final Map<String, JobGroupRunStats> naJobGroupRunStatsMap = new HashMap();
    for (JobGroup jobgroup : jobGroupTreeList) {
    	jobGroupRunStatsMap.put(jobgroup.getId(), new JobGroupRunStats(jobgroup.getId()));
    }

    /************************** Job Instance ��ȸ *******************************/
	queryParamMap.clear();
	queryParamMap.put("jobGroupIdLike",   Util.isBlank(jobgroupid)   ? null : jobgroupid+"%");
	queryParamMap.put("jobGroupNameLike", Util.isBlank(jobgroupname) ? null : "%"+jobgroupname+"%");
	if ("activationDate".equals(searchdatetype)) { /* �����Ϸ� ��ȸ */
		queryParamMap.put("activationTimeFrom",    searchdatefrom+"000000");
		queryParamMap.put("activationTimeTo",      searchdateto  +"235959");
	}else if ("procDate".equals(searchdatetype)) { /* ó���Ϸ� ��ȸ */
		queryParamMap.put("procDateFrom",    searchdatefrom);
		queryParamMap.put("procDateTo",      searchdateto);
	}

	if (!user.isAdmin()) { /* Admin �� �ƴ� ���� �㰡�� JobGroup�� ��ȸ ������. ��ϵ��� ���� ��� empty list, empty list �� ��� ��ü ��ȸ */ 
		queryParamMap.put("authorizedJobGroupIdViewList", user.getAuthList("VIEW_JOBGROUP"));
	}

    /************** Job Instance ������� ���� ********************/
	/* �뷮 ��ȸ�̹Ƿ� �޸� ȿ���� ���� RowHandler ������� ������. */
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
	/* ������ ���� ���迡 �ʿ��� �÷��� ��ȸ�� */
	queryParamMap.put("columnList", "JOB_ID, JOB_GROUP_ID, TIME_FROM, TIME_UNTIL, RUN_COUNT, END_OK_COUNT, JOB_STATE, LAST_JOB_EXE_ID, LAST_START_TIME, LAST_END_TIME"); 
	admin.getJobInstanceListFreeColumnWithRowHandler(queryParamMap, rowHandlerJobIns); 

	/************************** Job Definition ��ȸ *******************************/
	if (showNoInsGroup) {
	    queryParamMap.clear();
		queryParamMap.put("jobGroupIdLike",   Util.isBlank(jobgroupid)   ? null : jobgroupid+"%");
		queryParamMap.put("jobGroupNameLike", Util.isBlank(jobgroupname) ? null : "%"+jobgroupname+"%");
	
		if (!user.isAdmin()) { /* Admin �� �ƴ� ���� �㰡�� JobGroup�� ��ȸ ������. ��ϵ��� ���� ��� empty list, empty list �� ��� ��ü ��ȸ */ 
			queryParamMap.put("authorizedJobGroupIdViewList", user.getAuthList("VIEW_JOBGROUP"));
		}
	
		/* 
		 * Job GroupId, Job ID ������ �ʿ��ϴ�. ���ʿ��� �޸� ���� ����.
		 * JobDef �� JobIns ��ŭ �Ǽ��� ���� �����Ƿ� RowHandler ������� ������ �ʴ´�.
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

