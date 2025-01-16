<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
    ParallelGroup getParallelGroup(javax.servlet.http.HttpServletRequest request) {
		// Http request parameter �� ���� ParallelGroup ��ü ����.
		ParallelGroup pg = new ParallelGroup();
		BeanMaker.makeFromHttpParameter(request.getParameterMap(), pg);
		return pg;
	}
%>
<%!
    JobGroupAttrDef getJobGroupAttrDef(javax.servlet.http.HttpServletRequest request) {
        // Http request parameter �� ���� JobGroupAttrDef ��ü ����.
        JobGroupAttrDef attrDef = new JobGroupAttrDef();
        BeanMaker.makeFromHttpParameter(request.getParameterMap(), attrDef);
        return attrDef;
    }
%>
<%!
    JobGroup getJobGroup(javax.servlet.http.HttpServletRequest request) {
        // Http request parameter �� ���� JobGroupAttrDef ��ü ����.
        JobGroup jobgroup = new JobGroup();
        BeanMaker.makeFromHttpParameter(request.getParameterMap(), jobgroup);
        
        Enumeration en = request.getParameterNames();
        while(en.hasMoreElements()) {
        	String paramName = (String)en.nextElement();
        	if (paramName.startsWith("attr_")) {
        		String attrName  = paramName.substring(5);
        		String attrValue = request.getParameter(paramName);
        		jobgroup.setAttribute(attrName, attrValue);
        	}
        }
        
        return jobgroup;
    }
%>
<%
    String  suburl       = nvl(request.getParameter("suburl"), "");
    String  cmd          = nvl(request.getParameter("cmd"), "");

    boolean closeThisWin = false; /* ���� window close �� ������?. �������� close, �ű� ���� close ���� */
	String  returnUrl    = "view_setting.jsp?suburl="+suburl;  /* closeThisWin=false �� ���� sendRedirect �� URL�� �������־���Ѵ�. */

    String errorMsg = null;
	try {
		ControllerAdminLocal admin = getControllerAdmin();
		AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));
		if ("add_global_param".equals(cmd)) {
		    String paramName  = request.getParameter("param_name");
		    String paramValue = request.getParameter("param_value");
		    admin.addGlobalParameter(paramName, paramValue, auth);
		}else if ("modify_global_param".equals(cmd)) {
		    String paramName  = request.getParameter("param_name");
		    String paramValue = request.getParameter("param_value");
		    admin.modifyGlobalParameter(paramName, paramValue, auth);
		}else if ("delete_global_param".equals(cmd)) {
		    String paramName  = request.getParameter("param_name_for");
		    admin.deleteGlobalParameter(paramName, auth);
	    }else if ("add_parallel_group".equals(cmd)) {
	        ParallelGroup pg = getParallelGroup(request);
		    admin.addParallelGroup(pg, auth);
		}else if ("modify_parallel_group".equals(cmd)) {
	        ParallelGroup pg = getParallelGroup(request);
		    admin.modifyParallelGroup(pg, auth);
		}else if ("delete_parallel_group".equals(cmd)) {
		    String pgName = request.getParameter("parallel_group_for");
		    admin.deleteParallelGroup(pgName, auth);
	    }else if ("reload_calendar".equals(cmd)) {
	        admin.reloadCalendar();
	        returnUrl = "view_setting.jsp?suburl=calendar&calendarId="+nvl(request.getParameter("calendarId"))+"&yyyy="+nvl(request.getParameter("yyyy"));
        }else if ("add_jobgroupattrdef".equals(cmd)) {
        	JobGroupAttrDef attrDef = getJobGroupAttrDef(request);
        	admin.addJobGroupAttrDef(attrDef, auth);
        }else if ("modify_jobgroupattrdef".equals(cmd)) {
        	JobGroupAttrDef attrDef = getJobGroupAttrDef(request);
        	admin.modifyJobGroupAttrDef(attrDef, auth);
        }else if ("delete_jobgroupattrdef".equals(cmd)) {
		    String idFor = request.getParameter("attrid_for");
        	admin.removeJobGroupAttrDef(idFor, auth);
        }else if ("add_jobgroup".equals(cmd)) {
            JobGroup jobgroup = getJobGroup(request);
            admin.addJobGroup(jobgroup, auth);
            putMsg(session, Label.get("common.add.ok"));
            returnUrl = "form_setting_jobgroup.jsp?doreload=yes&parentid="+jobgroup.getParentId(); /* �ű��� ���� form â �����ϰ� parent �� reload �� */
        }else if ("modify_jobgroup".equals(cmd)) {
            JobGroup jobgroup = getJobGroup(request);
            admin.modifyJobGroup(jobgroup, auth);
            putMsg(session, Label.get("common.edit.ok"));
		    /* ������ form â close �ϰ� parent reload �� */
		    closeThisWin = true;
        }else if ("delete_jobgroup".equals(cmd)) {
        	String[] jobGroupIdList    = request.getParameterValues("chkjobgroupid");
        	String   deleteRecursively = request.getParameter("delete_recursively");
        	for (String jobGroupId : jobGroupIdList) {
        		if ("true".equals(deleteRecursively)) {
       				admin.removeJobGroupRecursively(jobGroupId, auth);        			
        		}else {
       				admin.removeJobGroup(jobGroupId, auth);        			
        		}
        	}
            putMsg(session, Label.get("common.delete.ok"));
	    }else if ("upload_jobgroup".equals(cmd)) {
	    	String filename = request.getParameter("filename");
    		List<JobGroup> jobGroupList = null;  /* excel upload ���Ϸ� ���� �Ľ��� JobGroup ���� */
	    	
	    	File file = new File(filename);
	    	ObjectInputStream fin = null;
	    	try {
	    		fin = new ObjectInputStream(new FileInputStream(file));
	    		jobGroupList = (List<JobGroup>)fin.readObject();
	    	}finally {
	    		try { fin.close(); }catch(Exception ignore) {}
	    		file.delete();
	    	}
	    	
	    	/* DB �� ���� ����ִ� JobGroup ������ �̸� ��ȸ��. insert, update ���� üũ�� */
	    	List<JobGroup> oldJobGroupList = admin.getJobGroupsByQuery("", "");
	    	Set oldJobGroupIdSet = new HashSet();
	    	for (JobGroup jobgroup : oldJobGroupList) {
	    		oldJobGroupIdSet.add(jobgroup.getId());
	    	}
	    	oldJobGroupList = null;

	    	/* Insert, Update */
	    	int addCnt = 0, modifyCnt = 0;
	    	for (JobGroup jobgroup : jobGroupList) {
	    		if (oldJobGroupIdSet.contains(jobgroup.getId())) {
		    		admin.modifyJobGroup(jobgroup, auth);
		    		modifyCnt++;
	    		}else {
	    			admin.addJobGroup(jobgroup, auth);
	    			addCnt++;
	    		}
	    	}

            putMsg(session, Label.get("common.add", addCnt)+", "+Label.get("common.modify", modifyCnt));
            closeThisWin = true;
	    }
	}catch(Exception e) {
		getMainLog().error("action_setting.jsp", e);
        //putMsg(session, "���� "+e.getMessage());
        throw new ServletException(e);
	}

	if (closeThisWin) {
%>
<html>
<head>
<jsp:include page="display_msg.jsp" flush="true"/>
</head>
<body onload="displayMsg();opener.window.location.reload(true);window.close();">
</body>
</html>
<%
	}else {
    	response.sendRedirect(returnUrl);
	}
%>
