<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="euc-kr"%>
<%@include file= "common.jsp" %>
<%!
	JobInstance getJobInstance(javax.servlet.http.HttpServletRequest request) {
		JobInstance jobins = new JobInstance();

		// JobDefinition 객체 생성.
		BeanMaker.makeFromHttpParameter(request.getParameterMap(), jobins);
		
		//에이전트 이중화 agentNode2를 추가 하여 set한다.
		if(!Util.isBlank(request.getParameter("agentNode2"))){
			String agentNode = jobins.getAgentNodeMaster();
			String agentNode2 = request.getParameter("agentNode2").toString();
			
			jobins.setAgentNode(agentNode+"/"+agentNode2);
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
		jobins.setPreJobConditions(preJobCondList);
		
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
		jobins.setTriggerList(triggerList);
		
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
		jobins.setInParameters(paramMap);

		return jobins;
	}

	void checkPassword(javax.servlet.http.HttpServletRequest request) throws Exception {
		String password = request.getParameter("password");
		String userId   = getUserId(request);
		
		ControllerAdminLocal admin = getControllerAdmin();
		admin.login(userId, password, request.getRemoteHost());
	}
			
%><%
	String cmd            = nvl(request.getParameter("cmd"), "");
	String jobinstanceid  = request.getParameter("jobinstanceid");
	String returnUrl      = request.getParameter("returnurl");

	try {
		ControllerAdminLocal admin = getControllerAdmin();
		AdminAuth auth = new AdminAuth(getUserId(request), getUserIp(request));

		if ("forcerun".equals(cmd)) {
			String jobExeId = admin.forceRunJob(jobinstanceid, auth);
            putMsg(session, MSG.get("main.jobmon.forcerun.ok", jobExeId));
		}else if ( "rerun".equals(cmd)) {
			admin.reRunJob(jobinstanceid, auth);
			putMsg(session, MSG.get("main.jobmon.rerun.ok", jobinstanceid));
		}else if ( "stop".equals(cmd)) {
			admin.stopJob(jobinstanceid, auth);
			putMsg(session, MSG.get("main.jobmon.stop.ok", jobinstanceid));
		}else if ( "suspend".equals(cmd)) {
			admin.suspendJob(jobinstanceid, auth);
			putMsg(session, MSG.get("main.jobmon.suspend.ok", jobinstanceid));
		}else if ( "resume".equals(cmd)) { 
			admin.resumeJob(jobinstanceid, auth);
			putMsg(session, MSG.get("main.jobmon.resume.ok", jobinstanceid));
		}else if ( "forceendok".equals(cmd)) {
			admin.forceEndOk(jobinstanceid, auth);
			putMsg(session, MSG.get("main.jobmon.forceendok.ok", jobinstanceid));
		}else if ( "confirm".equals(cmd)) {
			admin.confirm(jobinstanceid, auth);
			putMsg(session, MSG.get("main.jobmon.confirm.ok", jobinstanceid));
		}else if ( "toghost".equals(cmd)) {
			admin.forceChangeToGhost(jobinstanceid, auth);
			putMsg(session, MSG.get("main.jobmon.toghost.ok", jobinstanceid));
		}else if ( "lock".equals(cmd)) {
			admin.lockJob(jobinstanceid, auth);
			putMsg(session, MSG.get("main.jobmon.lock.ok", jobinstanceid));
		}else if ( "unlock".equals(cmd)) {
			admin.unlockJob(jobinstanceid, auth);
			putMsg(session, MSG.get("main.jobmon.unlock.ok", jobinstanceid));
		}else if ("change_loglevel".equals(cmd)) {
		    String newLevel = request.getParameter("newlevel");
            admin.changeJobInstanceLogLevel(jobinstanceid, newLevel, auth);
            putMsg(session, MSG.get("main.jobmon.change.loglevel.ok", jobinstanceid));
        }else if ("modify_jobins".equals(cmd)) {
            JobInstance jobins = getJobInstance(request);
            
            List jobinsCheck = admin.validateJobInstance(jobins);
            if (jobinsCheck.size() > 0) {
                throw new ServletException(Util.toString(jobinsCheck, "\\n"));
            }else {
	            admin.modifyJobInstance(jobins, auth);
	            putMsg(session, MSG.get("main.jobmon.modify.jobins.ok", jobins.getJobInstanceId()));
            }
		}else if (cmd.endsWith("_multi")) {
			if (isRequirePasswordForJobAction(request)) {
				checkPassword(request);
			}
			String changeAgentId = request.getParameter("toagentid"); /* 일괄 에이전트 변경시 변경할 에이전트 ID */
			List<String> jobinsidList = Util.toList(request.getParameter("jobinsid_list"));
            int succCnt = 0, failCnt = 0;
		    for (int i=0; i<jobinsidList.size(); i++) {
		        String jobinsid = jobinsidList.get(i);
		        try {
		            if (cmd.equals("forcerun_multi")) {
		                admin.forceRunJob(jobinsid, auth);
		            }else if (cmd.equals("rerun_multi")) {
		                admin.reRunJob(jobinsid, auth);
		            }else if (cmd.equals("stop_multi")) {
                        admin.stopJob(jobinsid, auth);
		            }else if (cmd.equals("suspend_multi")) {
                        admin.suspendJob(jobinsid, auth);
		            }else if (cmd.equals("resume_multi")) {
                        admin.resumeJob(jobinsid, auth);
		            }else if (cmd.equals("forceendok_multi")) {
                        admin.forceEndOk(jobinsid, auth);
		            }else if (cmd.equals("toghost_multi")) {
                        admin.forceChangeToGhost(jobinsid, auth);
		            }else if (cmd.equals("lock_multi")) {
                        admin.lockJob(jobinsid, auth);
		            }else if (cmd.equals("unlock_multi")) {
                        admin.unlockJob(jobinsid, auth);
		            }else if (cmd.equals("confirm_multi")) {
                        admin.confirm(jobinsid, auth);
                    }else if (cmd.equals("changeagent_multi")) {
                    	admin.modifyJobInstanceAgentId(jobinsid, changeAgentId, auth);
                    }
		            succCnt ++;
		        }catch(Exception ee) {
		            getMainLog().error("action_jobins.jsp", ee);
	        		putMsg(session, "▶▶ ("+i+") ["+jobinsid+"]"+ee.getMessage());
		            failCnt ++;
		        }
		    }
		    String actionName = cmd.substring(0, cmd.indexOf("_"));
		    putMsg(session, Label.get("action_jobins.complete.success.fail", Label.get("jobctl.action.name."+actionName), succCnt, failCnt));
        }
	}catch(Exception e) {
        getMainLog().error("action_jobins.jsp", e);
		putMsg(session, "▶▶ "+e.getMessage());
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

