<%@page language="java" contentType="text/xml; charset=UTF-8" pageEncoding="euc-kr"
%><%@include file= "common_functions.jsp" 
%><%!
    void escapeString(String s, JspWriter out) throws IOException {
        if (s == null) return;
        int fromIdx  = 0;
        int totallen = s.length();
        for (int i=0; i<totallen; i++) {
            if (s.charAt(i) == '&') {
                out.write(s, fromIdx, i-fromIdx);
                out.write("&amp;");
                fromIdx = i+1;
                continue;
            }
    
            if (s.charAt(i) == '\"') {
                out.write(s, fromIdx, i-fromIdx);
                out.write("&quot;");
                fromIdx = i+1;
                continue;
            }
    
            if (s.charAt(i) == '<') {
                out.write(s, fromIdx, i-fromIdx);
                out.write("&lt;");
                fromIdx = i+1;
                continue;
            }
    
            if (s.charAt(i) == '>') {
                out.write(s, fromIdx, i-fromIdx);
                out.write("&gt;");
                fromIdx = i+1;
            }
        }
        out.write(s, fromIdx, totallen-fromIdx);
    }

	String makeAgentNodeText(String agentList, String executeAgent){
		StringBuilder builder = new StringBuilder();
		
		String[] list = agentList.split("/");
		for(String item : list){
			if(item.equals(executeAgent))
				builder.append(">>").append(item).append("<<").append("/");
			else
				builder.append(item).append("/");
		}
		builder.deleteCharAt(builder.length() - 1);
		
		return builder.toString();
	}
%><%
    long current = System.currentTimeMillis(); /* 쿼리 전 시각으로 담아야 변경분 update 시에 정확해진다. */

%><jsp:include page="common_query_jobins.jsp"/><%
    if (!checkLogin(request, response)) return;
    
    ControllerAdminLocal  admin         = getControllerAdmin();
    List<JobInstance>     jobinsList    = (List<JobInstance>)request.getAttribute("jobins_query_result");
    Integer               totalCount    = (Integer)request.getAttribute("jobins_query_total");
    
    Map agentCheckMap = admin.getAgentCheckList();
    
    int currPageNo         = Util.toInt(request.getParameter("currpageno"), 1);
    int userJobInsPageSize = Util.toInt(request.getParameter("jobins_page_size"), jobInsPageSize);
    
    int jobInsSeqNo       = (currPageNo-1) * userJobInsPageSize + 1;

    out.print("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    out.print("<JobInstanceQuery>");
    out.print("<Header>");
    
    out.print("<Current>");
    out.print(toDatetimeString(current, false));
    out.print("</Current>");
    out.print("<CurrentTimeMs>");
    out.print(current);
    out.print("</CurrentTimeMs>");
    out.print("<CurrentYMD14>");
    out.print(Util.getYYYYMMDDHHMMSS(current));
    out.print("</CurrentYMD14>");
    if (totalCount != null) {
	    out.print("<TotalCount>");
	    out.print(totalCount);
	    out.print("</TotalCount>");
	    out.print("<MaxPageNo>");
	    out.print((int)Math.ceil((double)totalCount / (double)userJobInsPageSize));
	    out.print("</MaxPageNo>");
    }
    out.print("</Header>");
    out.print("<Body>");

    /* 조회한 JobInstance 결과 목록 */
    out.print("<JobInstanceList>");
    
    for (JobInstance jobins : jobinsList) {
        long[] progress = admin.getJobProgress(jobins.getLastJobExeId());
        
        out.print("<JobInstance");
        out.print(" jobId=\"");                out.print(nvl(jobins.getJobId()));                                   
        if (totalCount != null) { /* full update 모드일 경우만 seqNo 출력함 */
            out.print("\" seqNo=\"");          out.print(jobInsSeqNo++);
        }
        out.print("\" jobGroupId=\"");         out.print(nvl(jobins.getJobGroupId()));
        out.print("\" jobInstanceId=\"");      out.print(nvl(jobins.getJobInstanceId()));
        out.print("\" appCode=\"");            escapeString(getAppCode(jobins.getJobId()), out);
        out.print("\" description=\"");        escapeString(jobins.getDescription(), out);
        out.print("\" jobType=\"");            out.print(nvl(jobins.getJobType()));
        out.print("\" jobTypeText=\"");        escapeString(getJobTypeText(jobins.getJobType()), out);
        out.print("\" componentNameFull=\"");  escapeString(jobins.getComponentName(), out);
        out.print("\" componentName=\"");      escapeString(getShortComponentNameNoTag(jobins.getJobType(), jobins.getComponentName()), out);
        out.print("\" endOkCount=\"");         out.print(nvl(String.valueOf(jobins.getEndOkCount())));
        out.print("\" runCount=\"");           out.print(nvl(String.valueOf(jobins.getRunCount())));
        out.print("\" jobState=\"");           out.print(nvl(jobins.getJobState()));
        
        /*
         * 에이전트 이중화 기능 추가로 인해, agentCheck 를 현재 수행중인 실제 에이전트를 대상으로함
         * 
         */
        if ((JobInstance.JOB_STATE_RUNNING.equals(jobins.getJobState()) || JobInstance.JOB_STATE_SUSPENDED.equals(jobins.getJobState())) && 
        	!"OK".equals(agentCheckMap.get(jobins.getLastAgentNode()))) {
            out.print("\" jobStateText=\"");   escapeString(jobins.getJobStateText()+"<br>(Agent Fail)", out);
        }else {
            out.print("\" jobStateText=\"");   escapeString(jobins.getJobStateText(), out);
        }
        out.print("\" jobStateReason=\"");     escapeString(jobins.getJobStateReason(), out);
        out.print("\" activationDate=\"");     out.print(nvl(jobins.getActivationDate()));
        out.print("\" activationTime=\"");     out.print(nvl(jobins.getActivationTime()));
        out.print("\" activator=\"");          escapeString(jobins.getActivator(), out);
		//out.print("\" agentNode=\"");          escapeString(Util.nvlBlank(jobins.getLastAgentNode(), jobins.getAgentNode()), out);
        out.print("\" agentNode=\"");          escapeString(makeAgentNodeText(jobins.getAgentNode(),jobins.getLastAgentNode()), out);
        out.print("\" lastAgentNode=\"");      escapeString(jobins.getLastAgentNode(), out);
        out.print("\" baseDate=\"");           out.print(nvl(jobins.getBaseDate()));
        out.print("\" procDate=\"");           out.print(nvl(jobins.getProcDate()));
        out.print("\" confirmed=\"");          escapeString(jobins.getConfirmed(), out);
        out.print("\" confirmNeedYN=\"");      out.print(nvl(jobins.getConfirmNeedYN()));
        out.print("\" lastJobExeId=\"");       out.print(nvl(jobins.getLastJobExeId()));
        out.print("\" lastStartTime=\"");      escapeString(toDatetimeString(jobins.getLastStartTime(), true), out);
        out.print("\" lastEndTime=\"");        escapeString(toDatetimeString(jobins.getLastEndTime(), true), out);
        out.print("\" lockedBy=\"");           escapeString(jobins.getLockedBy(), out);
        out.print("\" logLevel=\"");           out.print(nvl(jobins.getLogLevel()));
        out.print("\" parallelGroup=\"");      escapeString(jobins.getParallelGroup(), out);
        out.print("\" timeFrom=\"");           out.print(nvl(jobins.getTimeFrom()));
        out.print("\" timeUntil=\"");          out.print(nvl(jobins.getTimeUntil()));
        out.print("\" repeatYN=\"");           out.print(nvl(jobins.getRepeatYN()));
        out.print("\" repeatIfError=\"");      out.print(nvl(jobins.getRepeatIfError()));
        out.print("\" repeatIntvalGb=\"");     out.print(nvl(jobins.getRepeatIntvalGb()));
        out.print("\" repeatIntval=\"");       out.print(nvl(String.valueOf(jobins.getRepeatIntval())));
        out.print("\" repeatMaxOk=\"");        out.print(nvl(String.valueOf(jobins.getRepeatMaxOk())));
        out.print("\" repeatExactExp=\"");     escapeString(jobins.getRepeatExactExp(), out);
        out.print("\" lastModifyTime=\"");     escapeString(toDatetimeString(DateUtil.getTimestamp(jobins.getLastModifyTime()), true), out);
        out.print("\" lastModifyTimeMS=\"");   out.print(String.valueOf(DateUtil.getTimestampLong(jobins.getLastModifyTime())));
        out.print("\"");
        
        if (progress != null) {
            out.print(" progressTotal=\"");    out.print(nvl(String.valueOf(progress[0]))); 
            out.print("\" progressCurr=\"");   out.print(nvl(String.valueOf(progress[1])));
            out.print("\"");
        }else {
            out.print(" progressTotal=\"\"");
            out.print(" progressCurr=\"\"");
        }
        
        out.print(" runningElapTime=\"");      escapeString(toRunTimeString(jobins.getJobState(), jobins.getLastStartTime(), jobins.getLastEndTime(), true), out);
        out.print("\"");
        out.println(" />");
    }

    /* 실행중인 Job 의 상태 */
    out.print("</JobInstanceList>");
    out.print("<RunningJobInstanceList>");
    
    Collection<JobExecution> runningJobExeList = admin.getRunningJobExecutions();
    long currentTimeMillis = System.currentTimeMillis();
    
    for (JobExecution jobexe : runningJobExeList) {
        long[] progress = admin.getJobProgress(jobexe.getJobExecutionId());
        if (progress == null) {
            continue;
        }

        /* 스케줄러 기준으로 시작시각 측정함. JobExecution 의 startTime() 은 running 중에는 스케줄러 시각이지만. end 되고 나면 에이전트에서 set 된 값으로 바뀐다. */

        out.println("<RunningJobInstance");
        
        out.print(" jobInstanceId=\"");         out.print(jobexe.getJobInstanceId());
        out.print("\" runningElapTime=\"");     escapeString(toRunTimeString("R", jobexe.getStartTime(), currentTimeMillis, true), out);
        out.print("\" progressTotal=\"");       out.print(nvl(String.valueOf(progress[0])));
        out.print("\" progressCurr=\"");        out.print(nvl(String.valueOf(progress[1])));
        out.print("\"");
        out.println(" />");
    }
    out.print("</RunningJobInstanceList>");
    out.print("</Body>");
    out.print("</JobInstanceQuery>");


%>