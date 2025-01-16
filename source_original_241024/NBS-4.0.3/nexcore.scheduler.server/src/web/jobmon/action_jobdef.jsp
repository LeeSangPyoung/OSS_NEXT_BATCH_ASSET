<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	int reqNoCount=0;

	JobDefinitionStg getJobDefinitionStg(javax.servlet.http.HttpServletRequest request) {
		JobDefinitionStg jobdef = new JobDefinitionStg();

		// JobDefinition 객체 생성.
		BeanMaker.makeFromHttpParameter(request.getParameterMap(), jobdef);
		
		//에이전트 이중화 agentNode2를 추가 하여 set한다.
		if(!Util.isBlank(request.getParameter("agentNode2"))){
			String agentNode = jobdef.getAgentNodeMaster();
			String agentNode2 = request.getParameter("agentNode2").toString();
			
			jobdef.setAgentNode(agentNode+"/"+agentNode2);
		}
		
		int lastPreJobIndex = Integer.parseInt(request.getParameter("lastPreJobIndex"));
		int lastTriggerJobIndex = Integer.parseInt(request.getParameter("lastTriggerJobIndex"));
		int lastParamIndex  = Integer.parseInt(request.getParameter("lastParamIndex"));
		
		// PreJobCondition 객체 생성
		List preJobCondList = new LinkedList();
		for (int i=0; i<lastPreJobIndex; i++) {
			String preJobId = request.getParameter("preJobId_"+i);
			if (Util.isBlank(preJobId)) {
				continue; // 없는 인덱스는 그냥 pass
			}
			String okFail = request.getParameter("okFail_"+i);
			String andOr  = request.getParameter("andOr_"+i);
			
			preJobCondList.add(new PreJobCondition(preJobId.trim(), okFail.trim(), andOr.trim()));
		}
		jobdef.setPreJobConditions(preJobCondList);
		
		// TriggerJobCondigion 객체 생성
		List triggerList = new LinkedList();
		for (int i=0; i<lastTriggerJobIndex; i++) {
			String jobId = request.getParameter("triggerJobId_"+i);
			if (Util.isBlank(jobId)) {
				continue;
			}
			String when = request.getParameter("triggerOkFail_"+i);
			int count = Util.toInt(request.getParameter("triggerCount_"+i));
			
			String chkValue1 = "";
			String chkValue2 = "";
			if ("RETVAL".equals(when)) {
				chkValue1 = request.getParameter("triggerChkValue1_"+i);
				chkValue2 = request.getParameter("triggerChkValue2_"+i);			
			}
			
			triggerList.add(new PostJobTrigger(when, chkValue1, chkValue2, "", jobId, count));
		}
		jobdef.setTriggerList(triggerList);
		
		
		// Parameter 객체 생성
		Map paramMap = new LinkedHashMap();
		for (int i=0; i<lastParamIndex; i++) {
			String paramName = request.getParameter("paramName_"+i);
			if (Util.isBlank(paramName)) {
				continue; // 없는 인덱스는 그냥 pass
			}
			String paramValue = request.getParameter("paramValue_"+i);
			
			paramMap.put(paramName.trim(), Util.trimIfNotNull(paramValue));
		}
		jobdef.setInParameters(paramMap);

		// 요청 부가 정보 세팅
		jobdef.setReqUserIp(request.getRemoteAddr());
		jobdef.setReqTime(Util.getCurrentYYYYMMDDHHMMSS());
		jobdef.setReqState("Q");

		return jobdef;
	}
%><%
	String cmd            = nvl(request.getParameter("cmd"), "");
	String returnUrl      = request.getParameter("returnurl");

    String errorMsg = null;
	try {
		ControllerAdminLocal admin = getControllerAdmin();
		AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));

		if ("activate".equals(cmd)) {
			String jobid          = request.getParameter("jobid");
			String activteLockYn  = request.getParameter("activate_lock_yn");
			String jobInstanceId  = null;
			try {
				if ("1".equals(activteLockYn)) {
            		jobInstanceId = admin.activateAndLockJob(jobid, request.getParameter("procdate"), auth);
                    putMsg(session, MSG.get("main.jobmon.activatelock.ok", jobInstanceId));
            	}else {
            		jobInstanceId = admin.activateJob(jobid, request.getParameter("procdate"), auth);
            		putMsg(session, MSG.get("main.jobmon.activate.ok", jobInstanceId));
            	}
        	}catch(Exception e) {
                putMsg(session, e.getMessage());
        	}
		}else if ("activate_run".equals(cmd)) {
			String jobid          = request.getParameter("jobid");
			String activteLockYn  = request.getParameter("activate_lock_yn");
			String jobInstanceId  = null;
			try {
				if ("1".equals(activteLockYn)) {
            		jobInstanceId = admin.activateAndLockJob(jobid, request.getParameter("procdate"), auth);
            	}else {
            		jobInstanceId = admin.activateJob(jobid, request.getParameter("procdate"), auth);
            	}
				admin.forceRunJob(jobInstanceId, auth);
				putMsg(session, MSG.get("main.jobmon.activaterun.ok", jobInstanceId));
        	}catch(Exception e) {
                putMsg(session, e.getMessage());
        	}
		}else if ("request_edit".equals(cmd)) {
			JobDefinitionStg jobdef = getJobDefinitionStg(request);
			List jobdefCheck = admin.validateJobDefinition(jobdef);
			if (jobdefCheck.size() > 0) {
                throw new ServletException(Util.toString(jobdefCheck, "<br>"));
			}else {
    			String reqNo = admin.addJobDefinitionStg(jobdef, auth);
    			putMsg(session, "▶▶ "+Label.get("action_jobdef.edit.requested"));
    			
    			String autoApproval = request.getParameter("autoApproval");
    			if ("true".equalsIgnoreCase(autoApproval)) {
    				admin.approveJobDefinitionStgToJobDefinition(reqNo, jobdef.getJobId(), "Auto", auth);
    				putMsg(session, "▶▶ "+Label.get("action_jobdef.approved"));
    			}
    		}
		}else if ("request_add".equals(cmd)) {
			JobDefinitionStg jobdef = getJobDefinitionStg(request);
			List jobdefCheck = admin.validateJobDefinition(jobdef);
			if (jobdefCheck.size() > 0) {
                throw new ServletException(Util.toString(jobdefCheck, "<br>"));
			}else {
    			jobdef.setCreateTime(Util.getCurrentYYYYMMDDHHMMSS());
    			String reqNo = admin.addJobDefinitionStg(jobdef, auth);
	    		if ("add".equals(jobdef.getReqType())) {
        			putMsg(session, "▶▶ "+Label.get("action_jobdef.add.requested"));
        		}else if ("delete".equals(jobdef.getReqType())) {
        		    putMsg(session, "▶▶ "+Label.get("action_jobdef.delete.requested"));
        		}
    			String autoApproval = request.getParameter("autoApproval");
    			if ("true".equalsIgnoreCase(autoApproval)) {
    				admin.approveJobDefinitionStgToJobDefinition(reqNo, jobdef.getJobId(), "Auto", auth);
    				putMsg(session, "▶▶ "+Label.get("action_jobdef.approved"));
    			}
	    	}
		}else if ("request_delete_multi".equals(cmd)) {
		    String[] jobidList   = request.getParameterValues("chkjobid");
            int succCnt = 0;
            int failCnt = 0;
            if (jobidList != null) {
                for (int i=0; jobidList!=null && i<jobidList.length; i++) {
                    String jobid = jobidList[i];

                    try {
	                    JobDefinition    jobdef    = admin.getJobDefinition(jobid);
	                    JobDefinitionStg jobdefstg = new JobDefinitionStg(jobdef);
	                    jobdefstg.setReqType("delete");
	                    jobdefstg.setReqComment("multi");
	                    jobdefstg.setReqUserIp(request.getRemoteAddr());
	                    jobdefstg.setReqUserName(getUserId(request));
	                    jobdefstg.setReqTime(Util.getCurrentYYYYMMDDHHMMSS());
	                    jobdefstg.setReqState("Q");
	
	                    admin.addJobDefinitionStg(jobdefstg, auth);
                        succCnt ++;
                    }catch(Exception e) {
                        failCnt ++;
                        getMainLog().error("action_jobdef.jsp", e);
                        putMsg(session, "▶▶ ("+(i+1)+") "+e.getMessage());
                    }
                }
            }
            putMsg(session, MSG.get("main.jobmon.multi.request.delete.ok", succCnt, failCnt));
		}else if ("admin_approve_reject".equals(cmd)) {
			String jobid       = request.getParameter("jobid");
			String reqState    = request.getParameter("reqState");
			String reqno       = request.getParameter("reqno");
			String reqARReason = request.getParameter("reqARReason");

			if ("A".equals(reqState)) { // do approve
				admin.approveJobDefinitionStgToJobDefinition(reqno, jobid, reqARReason, auth);
				putMsg(session, "▶▶ "+Label.get("action_jobdef.approved"));
			}else if ("R".equals(reqState)) { // do reject
				admin.rejectJobDefinitionStgToJobDefinition(reqno, jobid, reqARReason, auth);
				putMsg(session, "▶▶ "+Label.get("action_jobdef.rejected"));
			}
		}else if ("admin_multi_approve".equals(cmd)) {
   		    String[] reqNoJobIdList = request.getParameterValues("chkreqno_jobid");

            int succCnt = 0, failCnt = 0;
            for (int i=0; reqNoJobIdList != null && i<reqNoJobIdList.length; i++) {
                String s = reqNoJobIdList[i];
                String reqNo = s.substring(0, s.indexOf("_"));
                String jobId = s.substring(s.indexOf("_")+1);
                try {
    				admin.approveJobDefinitionStgToJobDefinition(reqNo, jobId, "", auth);
    				succCnt++;
    			}catch(Exception e) {
    			    failCnt++;
    			    getMainLog().error("action_jobdef.jsp", e);
					putMsg(session, "▶▶ ("+(i+1)+") "+e.getMessage());
    			}
			}
			putMsg(session, MSG.get("main.jobmon.multi.approve.ok", succCnt, failCnt));
		}else if ("admin_multi_reject".equals(cmd)) {
   		    String[] reqNoJobIdList = request.getParameterValues("chkreqno_jobid");

            int succCnt = 0, failCnt = 0;
            for (int i=0; reqNoJobIdList != null && i<reqNoJobIdList.length; i++) {
                String s = reqNoJobIdList[i];
                String reqNo = s.substring(0, s.indexOf("_"));
                String jobId = s.substring(s.indexOf("_")+1);
                try {
    				admin.rejectJobDefinitionStgToJobDefinition(reqNo, jobId, "multi_reject", auth);
    				succCnt++;
    			}catch(Exception e) {
    			    failCnt++;
    			    getMainLog().error("action_jobdef.jsp", e);
					putMsg(session, "▶▶ ("+(i+1)+") "+e.getMessage());
    			}
			}
			putMsg(session, MSG.get("main.jobmon.multi.reject.ok", succCnt, failCnt));
		}else if ("activate_multi".equals(cmd)) { // 일괄 activation
		    String   procDate  = request.getParameter("activate_multi_procdate");
            String   activteMultiLockYn = request.getParameter("activate_multi_lock_yn");
   		    String[] jobidList = request.getParameterValues("chkjobid");
   		    
		    int succCnt = 0;
		    int failCnt = 0;
		    if (jobidList != null) {
    	        for (int i=0; jobidList!=null && i<jobidList.length; i++) {
    	            String jobid = jobidList[i];
		            try {
		                if ("1".equals(activteMultiLockYn)) {
    		                admin.activateAndLockJob(jobid, procDate, auth);
		                }else {
    		                admin.activateJob(jobid, procDate, auth);
		                }
                        succCnt ++;
    		        }catch(Exception e) {
    		            failCnt ++;
    		            getMainLog().error("action_jobdef.jsp", e);
    					putMsg(session, "▶▶ ("+(i+1)+") "+e.getMessage());
    		        }
		        }
		    }
		    if ("1".equals(activteMultiLockYn)) {
		        putMsg(session, MSG.get("main.jobmon.multi.activatelock.ok", succCnt, failCnt));
		    }else {
                putMsg(session, MSG.get("main.jobmon.multi.activate.ok", succCnt, failCnt));
            }
		}else if ("activate_run_multi".equals(cmd)) { //일괄 즉시 수행
		
		    String   procDate  = request.getParameter("activate_multi_procdate");
            String   activteMultiLockYn = request.getParameter("activate_multi_lock_yn");
   		    String[] jobidList = request.getParameterValues("chkjobid");
   		    
		    int succCnt = 0;
		    int failCnt = 0;
		    
		    String jobInstanceId = null;
		    
		    if (jobidList != null) {
    	        for (int i=0; jobidList!=null && i<jobidList.length; i++) {
    	            String jobid = jobidList[i];
		            try {
		                if ("1".equals(activteMultiLockYn)) {
		                	jobInstanceId = admin.activateAndLockJob(jobid, procDate, auth);
		                }else {
		                	jobInstanceId = admin.activateJob(jobid, procDate, auth);
		                }
		                admin.forceRunJob(jobInstanceId, auth);
                        succCnt ++;
    		        }catch(Exception e) {
    		            failCnt ++;
    		            getMainLog().error("action_jobdef.jsp", e);
    					putMsg(session, "▶▶ ("+(i+1)+") "+e.getMessage());
    		        }
		        }
		    }
			
		    putMsg(session, MSG.get("main.jobmon.multi.activaterun.ok", succCnt, failCnt));
			
		}else if ("change_loglevel".equals(cmd)) {
			String jobid    = request.getParameter("jobid");
		    String newLevel = request.getParameter("newlevel");
            admin.changeJobDefinitionLogLevel(jobid, newLevel, auth);
            putMsg(session, MSG.get("main.jobmon.change.loglevel.ok", newLevel));
		}else if ("migrate".equals(cmd)) { // 이관. 개발 -> 운영, 운영 -> 개발 
		    String migToServer   = request.getParameter("mig_to_server");
		    String migToAgent    = request.getParameter("mig_to_agent");
		    String migToAgent2  = request.getParameter("mig_to_agent2");
   		    String[] jobidList   = request.getParameterValues("chkjobid");

		    int succCnt = 0;
		    int failCnt = 0;
		    if (jobidList != null) {
		        String migToIp   = migToServer.substring(0, migToServer.indexOf(":"));
		        String migToPort = migToServer.substring(migToServer.indexOf(":")+1);
    		    ControllerAdmin migControllerAdmin = new ControllerAdmin(migToIp, migToPort);
    	        for (int i=0; jobidList!=null && i<jobidList.length; i++) {
    	            String jobid = jobidList[i];
		            try {
		            	// JobDefinition 객체 생성.
		                JobDefinitionStg fromJobDef = new JobDefinitionStg(admin.getJobDefinition(jobid));
		                
						//에이전트 이중화 migToAgent2 추가 하여 set한다.
						if(!Util.isBlank(migToAgent2))
							fromJobDef.setAgentNode(migToAgent+"/"+migToAgent2);
						else
							fromJobDef.setAgentNode(migToAgent);
		                
		             	// 요청 부가 정보 세팅
		        		fromJobDef.setReqUserIp(request.getRemoteAddr());
		        		fromJobDef.setReqTime(Util.getCurrentYYYYMMDDHHMMSS());
		        		fromJobDef.setReqState("Q");
		        		fromJobDef.setReqUserName(getUserId(request));
		        		fromJobDef.setReqComment("From "+Util.getSystemId());
		                
		        		List jobdefCheck = admin.validateJobDefinition(fromJobDef);
		    			if (jobdefCheck.size() > 0) {
		                    throw new ServletException(Util.toString(jobdefCheck, "<br>"));
		    			}else {
		    				boolean alreadyExist = migControllerAdmin.existJobDefinition(jobid);
	                        if (alreadyExist) {
	                        	fromJobDef.setReqType("edit");
	                        } else {
	                        	fromJobDef.setReqType("add");
	                        	fromJobDef.setCreateTime(Util.getCurrentYYYYMMDDHHMMSS());
	                        }
	                        migControllerAdmin.addJobDefinitionStg(fromJobDef, auth);
		    	    	}
		        		
                        succCnt ++;
    		        }catch(Exception e) {
    		            failCnt ++;
    		            getMainLog().error("action_jobdef.jsp", e);
    					putMsg(session, "▶▶ ("+(i+1)+") "+e.getMessage());
    		        }
		        }
		    }
		    putMsg(session, MSG.get("main.jobmon.migrate.ok", succCnt, failCnt, migToAgent));
		}
	}catch(Exception e) {
		getMainLog().error("action_jobdef.jsp", e);
		//putMsg(session, "▶▶ "+e.getMessage());
		throw new ServletException(e);
	}

    if (!Util.isBlank(returnUrl)) {
    	response.sendRedirect(returnUrl);
    }else {
%>
<html>
<head>
<jsp:include page="display_msg.jsp" flush="true"/>
</head>
<body onload="displayMsg();window.close();">
</body>
</html>
<%
    }
%>

