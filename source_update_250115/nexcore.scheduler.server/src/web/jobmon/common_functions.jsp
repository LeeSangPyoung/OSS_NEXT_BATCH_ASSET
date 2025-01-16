<%@page language="java" pageEncoding="euc-kr"
%><%@page import="java.io.*"
%><%@page import="java.net.*"
%><%@page import="java.util.*"
%><%@page import="java.text.*"
%><%@page import="java.lang.reflect.*"
%><%@page import="nexcore.scheduler.entity.*"
%><%@page import="nexcore.scheduler.util.*"
%><%@page import="nexcore.scheduler.msg.*"
%><%@page import="nexcore.scheduler.controller.admin.*"
%><%@page import="nexcore.scheduler.monitor.*"
%><%@page import="nexcore.scheduler.monitor.internal.*"
%><%@page import="nexcore.scheduler.exception.*"
%><%@page import="nexcore.scheduler.core.internal.CalendarUtil"
%><%@page import="nexcore.scheduler.log.LogManager"
%><%@page import="org.apache.commons.lang.time.FastDateFormat"
%><%@page import="org.apache.commons.logging.Log"
%><%@page import="com.ibatis.sqlmap.client.event.RowHandler"
%><%@include file= "customize.jsp" 
%><%!
    String getSystemId() {
        return System.getProperty("NEXCORE_ID");
    }

    String getServerName() {
        return getSystemId();
    }
	
    String getHostName() {
    	return Util.getHostName();
    }
    
	User getUser(javax.servlet.http.HttpServletRequest request) {
	    User user = (User)request.getSession().getAttribute("user");
        return user;
	}

	String getUserId(javax.servlet.http.HttpServletRequest request) {
	    User user = getUser(request);
	    if (user != null) {
	        return user.getId();
        }
        return null;
	}

	String getUserName(javax.servlet.http.HttpServletRequest request) {
	    User user = getUser(request);
	    if (user != null) {
	        return user.getName();
        }
        return null;
	}

	String getUserIp(javax.servlet.http.HttpServletRequest request) {
		return request.getRemoteAddr();
	}

    long getLoginTime(javax.servlet.http.HttpServletRequest request) {
        Long loginTime = (Long)request.getSession().getAttribute("loginTime");
        return loginTime.longValue();
    }

    boolean isAdmin(javax.servlet.http.HttpServletRequest request) {
        User user = getUser(request);
	    if (user != null) {
	        return user.isAdmin();
        }
        return false;
	}

    boolean isOperator(javax.servlet.http.HttpServletRequest request) {
        User user = getUser(request);
	    if (user != null) {
	        return user.isOperator();
        }
        return false;
	}

    boolean checkLogin(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        String uri = request.getRequestURI();
        if (uri != null && uri.indexOf("login.jsp")>-1) {
            return true;  /* do not check for login.jsp */
        }else {
            if (getUser(request) == null) {
                response.sendRedirect("login.jsp");
                return false;
            }
        }
        return true;
    }

	String nvl(Object s) {
        return s==null ? "" : String.valueOf(s);
    }

	String nvl(Object s, String defaultValue) {
        return s==null ? defaultValue : String.valueOf(s);
    }

	String getCurrYYYYYMMDD() {
		return new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
	}

	String formatDatetime(long time, String format) {
		return time==0 ? "" : new java.text.SimpleDateFormat(format).format(new java.util.Date(time));
	}

	String formatDatetime(java.sql.Timestamp time, String format) {
		return time==null ? "" : new java.text.SimpleDateFormat(format).format(new java.util.Date(time.getTime()));
	}

    Calendar parseYYYMMDDHHMMSS(String yyyymmddhhmmss) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,         Integer.parseInt(yyyymmddhhmmss.substring(0,4)));
        cal.set(Calendar.MONTH,        Integer.parseInt(yyyymmddhhmmss.substring(4,6))-1);
        cal.set(Calendar.DATE,         Integer.parseInt(yyyymmddhhmmss.substring(6,8)));
        cal.set(Calendar.HOUR_OF_DAY,  Integer.parseInt(yyyymmddhhmmss.substring(8,10)));
        cal.set(Calendar.MINUTE,       Integer.parseInt(yyyymmddhhmmss.substring(10,12)));
        cal.set(Calendar.SECOND,       Integer.parseInt(yyyymmddhhmmss.substring(12,14)));
        return cal;
    }
    
	String toDatetimeString(String yyyymmddhhmmss) {
		return toDatetimeString(yyyymmddhhmmss, true);
	}
	
    String toDatetimeString(String yyyymmddhhmmss, boolean split) {
        if (yyyymmddhhmmss == null) {
            return "";
        }
        if (yyyymmddhhmmss.length() != 14) {
            return yyyymmddhhmmss;
        }
    	if (split) {
    	    return FastDateFormat.getInstance(localDatetimePatternSplit).format(parseYYYMMDDHHMMSS(yyyymmddhhmmss));
    	}else {
    		return FastDateFormat.getInstance(localDatetimePattern).format(parseYYYMMDDHHMMSS(yyyymmddhhmmss));
    	}
    }

	String toDatetimeString(java.util.Date date) {
        if (date==null) return "";
        return toDatetimeString(date.getTime(), true);
    }

    String toDatetimeString(java.util.Date date, boolean split) {
        if (date==null) return "";
        return toDatetimeString(date.getTime(), split);
    }

	String toDatetimeString(java.sql.Timestamp timestamp) {
        if (timestamp==null) return "";
        return toDatetimeString(timestamp.getTime(), true);
    }

    String toDatetimeString(java.sql.Timestamp timestamp, boolean split) {
    	if (timestamp==null) return "";
    	return toDatetimeString(timestamp.getTime(), split);
    }
    
    String toDatetimeString(long time) {
        return toDatetimeString(time, true);
    }

    String toDatetimeString(long time, boolean split) {
    	if (time==0) return "";
        if (split) {
            return FastDateFormat.getInstance(localDatetimePatternSplit).format(time);
        }else {
            return FastDateFormat.getInstance(localDatetimePattern).format(time);
        }
    }


    String toDateString(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) {
            return yyyymmdd;
        }

        return FastDateFormat.getInstance(localDatePattern).format(parseYYYMMDDHHMMSS(yyyymmdd));
    }
	    
    String secondsToTime(int s){
    	if (s >= 3600) {
            int h  = (int)Math.floor( s / ( 60 * 60 ) );
            s -= h * ( 60 * 60 );
            int m  = (int)Math.floor( s / 60 );
            s -= m * 60;
            return (h==0?"":h+"h ") + (m==0?"":m+"m ") + (s==0?"":s+"s");
    	}else if (s >= 60) {
            int m  = (int)Math.floor( s / 60 );
            s -= m * 60;
            return (m==0?"":m+"m ") + (s==0?"":s+"s");
    	}else {
    		return s+"s";
    	}
    }

    String getYNSign(String s) {
        return "Y".equals(s) ? "��" : "N".equals(s) ? "" : "N/A";
    }

	String formatNumber(double val) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("###,###,###,###");
		return df.format(val);
    }

	String printTrFlip(boolean flip) {
	    return flip ? "bgcolor='#EEEEEE'" : "bgcolor='#FFFFFF'";
    }
    
	String printFlipBgcolor(boolean flip) {
	    return flip ? "'#EEEEEE'" : "'#FFFFFF'";
    }

    String printYNSelectOptions(String yn) {
        StringBuffer sb = new StringBuffer(100);
        if ("Y".equalsIgnoreCase(yn)) {
            sb.append("<option value=\"Y\" selected>Y</option>");
            sb.append("<option value=\"N\">N</option>");
        }else if ("N".equalsIgnoreCase(yn)) {
            sb.append("<option value=\"Y\">Y</option>");
            sb.append("<option value=\"N\" selected>N</option>");
        }else {
            sb.append("<option value=\"Y\">Y</option>");
            sb.append("<option value=\"N\" selected>N</option>");
        }
        return sb.toString();
    }
    
    String printSelectOption(String optionValue, String s2) {
        boolean selected = optionValue != null && optionValue.equals(s2);
        return "<option value=\""+optionValue+"\" "+(selected?"selected":"")+">"+optionValue+"</option>";
    }

    String printSelectOption(String optionValue, String optionDisplay, String s2) {
        boolean selected = optionValue != null && optionValue.equals(s2);
        return "<option value=\""+optionValue+"\" "+(selected?"selected":"")+">"+optionDisplay+" </option>";
    }

    String printRadioOption(String groupName, String value, String inValue, String label) {
        boolean checked = value != null && value.equals(inValue);
        return "<label><input type='radio' name='"+groupName+"' value='"+value+"' "+(checked?"checked":"")+">"+label+"</label>";
    }
    
    String printRadioOptionAlx(String groupName, String value, String inValue, String label) {
        boolean checked = value != null && value.equals(inValue);
        return "<label><input class='Radio Margin-right-5' type='radio' name='"+groupName+"' value='"+value+"' "+(checked?"checked":"")+">"+label+"</label>";
    }
    
    String printCheckbox(String name, String label, String value, Object inValue) {
    	return "<input type='checkbox' name='"+name+"' value='"+value+"' "+(Util.nvl(value).equals(inValue) ? "checked":"")+">"+label;
    }
    
    String printCheckboxAlx(String name, String label, String value, Object inValue) {
    	return "<input class='Checkbox Margin-right-5' type='checkbox' name='"+name+"' value='"+value+"' "+(Util.nvl(value).equals(inValue) ? "checked":"")+">"+label;
    }

    String getStateColor(String state) {
        if ("I".equals(state)) {
            return "#000000";
        }else if ("W".equals(state)) {
            return "#000000";
        }else if ("O".equals(state)) {
            return "#0000CD";
        }else if ("F".equals(state)) {
            return "#EB0000";
        }else if ("R".equals(state)) {
            return "#DD8200";
        }else if ("P".equals(state)) {
            return "#52E222";
        }else if ("S".equals(state)) {
            return "#8B4513";
        }else if ("G".equals(state)) {
            return "#008080";
        }else if ("X".equals(state)) {
            return "#239933";
        }else {
            return "#000000";
        }
    }

    String getJobTypeText(String jobtype) {
		return Label.get("jobtype."+jobtype);
    }
    
    void putMsg(HttpSession session, String msg) {
        java.util.List<String> mymsg = (java.util.List<String>)session.getAttribute("MY_MESSAGE");
        if (mymsg == null) {
            mymsg = new java.util.ArrayList();
        }
        mymsg.add(msg);
        session.setAttribute("MY_MESSAGE", mymsg);
    }

    String toProgressString(long[] progress) {
		if (progress == null) {
			return "N/A";
		}else {
			return progress[1]+"/"+progress[0];
		}
	}
    String toProgressPercentage(long[] progress) {
		if (progress == null) {
			return "";
		}else if (progress[0] == 0) {
            return "";
        }else {
			return String.valueOf(progress[1] * 100 / progress[0]);
		}
	}

	String toProgressSpeed(long startTime, long currentCount) {
		long elapTime = System.currentTimeMillis() - startTime;
		return String.valueOf(currentCount*1000 / elapTime);
	}

    String toRunTimeString(String jobState, long startTimeMs, long endTimeMs, boolean twoLine) {
        long   second = 0;
        if (startTimeMs > 0 && endTimeMs <=0) {
            if (JobInstance.JOB_STATE_RUNNING.equals(jobState) || JobInstance.JOB_STATE_SUSPENDED.equals(jobState)) {
                second = (System.currentTimeMillis() - startTimeMs) / 1000l;
            }
        }else if (startTimeMs > 0 && endTimeMs > 0) {
            second = (endTimeMs - startTimeMs) / 1000l;
        }

        return second == 0 ? "" : second <= 60 ? second+Label.get("common.second") : 
                                                 second+Label.get("common.second") + (twoLine?"<BR>":"")+"("+(double)((second*10/60)/10.0)+Label.get("common.minute")+")";
    }

	String toRunTimeString(String jobState, String startTime, String endTime, boolean twoLine) {
	    try {
		    return toRunTimeString(jobState, Util.parseYYYYMMDDHHMMSS(startTime), Util.parseYYYYMMDDHHMMSS(endTime), twoLine);
        }catch(Exception e) {
        	return "";
        }
	}
    
    String printViewFilterSelect(nexcore.scheduler.controller.admin.ControllerAdminLocal admin, String currViewFilter) {
        List<ViewFilter> viewFilters = admin.getViewFiltersByQuery("", "ORDER BY VF_OWNER, VF_TEAM, VF_NAME");
        StringBuilder sb = new StringBuilder(100);
        for (ViewFilter vf : viewFilters) {
            sb.append(printSelectOption(String.valueOf(vf.getId()), "["+vf.getOwner()+"]-"+substring(vf.getName(),10), currViewFilter));
        }
        return sb.toString();
    }
    
    ControllerAdminLocal getControllerAdmin() throws Exception {
        ControllerAdminLocal admin = new ControllerAdminLocal();
        return admin;
    }
    
    String byteToMega(long b) {
        return String.valueOf(b >> 10 >> 10);
    }

    String getClassNameOnly(nexcore.scheduler.entity.JobInstance jobins) {
        /* 
           JOB ������ JBATCH �Ǵ� POJO Ÿ���� ��� ��Ű�� ���� ������ Ŭ���� ���� ��ȯ�Ѵ�.
           ó�� �� ������ �߻��ϰų� JOB ������ JBATCH �Ǵ� POJO Ÿ���� �ƴ� ��� null�� ��ȯ�Ѵ�.
        */
        
        if ("JBATCH".equals(jobins.getJobType()) || "POJO".equals(jobins.getJobType())) {  /* �۾� ������ EJB �Ǵ� POJO �� ��츸 REPORT ���� View */
            
            String className = jobins.getComponentName();
        
            try {
                return className.substring(className.lastIndexOf('.') + 1);
            } catch (Exception e) {
                e.printStackTrace();
                className = null;
            }
        }
        
        return null; /* ���� �Ǵ� JOB ������ JBATCH �Ǵ� POJO Ÿ���� �ƴ� ��� */

    }   

    String printJobTypeSelectOptionList(nexcore.scheduler.controller.admin.ControllerAdminLocal admin, String selectedJobType) {
        List<String> jobTypeUseList = admin.getJobTypeUsingList();

        StringBuilder temp = new StringBuilder(100);
        for (String jobType : jobTypeUseList) {
	        temp.append(printSelectOption(jobType, getJobTypeText(jobType), selectedJobType));
        }
        
        return temp.toString();
    }
    

    
    String printCancelLine(String text, boolean isCancel) {
		return isCancel ? "<s>"+text+"</s>" : text;
	}
	
    String printSortMark(String orderby, String orderdir, String orderby2) {
        if (orderby!=null && orderby.equals(orderby2)) {
            if (orderdir.startsWith("A")) {
                return "��";
            } else {
                return "��";
            }
        }
        return "";
    }
    
    /**
     * Job Ÿ�Կ� ���� componentName �κ��� ����Ͽ� ǥ���Ѵ�.
     * JBATCH, POJO �� ���� Ŭ������ �����Ѵ�.
     */
    String getShortComponentNameNoTag(String jobType, String componentName) {
        if ("JBATCH".equals(jobType) || "POJO".equals(jobType)) { /* classname only */
            return componentName.substring(componentName.lastIndexOf('.') + 1);
        }else if ("PROC".equals(jobType)) { /* filename only */
        	if (componentName.indexOf("/") > -1) {
        		// unix ȯ�濡�� / �� �������� �ǳ� ���ϸ� �߶�
        		return componentName.substring(componentName.lastIndexOf("/")+1);
        	}else if (componentName.indexOf("\\") > -1) {
        		// windows ȯ�濡�� \ �� �������� �ǳ� ���ϸ� �߶�
        		return componentName.substring(componentName.lastIndexOf("\\")+1);
        	}else { // ���ϸ� ��� �ִ� ���
        		return componentName;
        	}
        }else {
            return componentName;
        }
    }

    /**
     * Job Ÿ�Կ� ���� componentName �κ��� ����Ͽ� ǥ���Ѵ�.
     * JBATCH, POJO �� ���� ��Ű������ ǳ�����򸻷� ǥ���ϰ�, Ŭ������ ����Ѵ�.
     */
    String getShortComponentName(String jobType, String componentName) {
        if ("JBATCH".equals(jobType) || "POJO".equals(jobType) || "PROC".equals(jobType)) { /* classname, filename only */
            return "<b title='"+componentName+"'>"+getShortComponentNameNoTag(jobType, componentName)+"</b>";
        }else {
            return nvl(componentName);
        }
    }

    String getShortDescription(String desc) {
        return shortenRight(desc, descShortLimit);
    }

    String shortenMiddle(String str, int length) {
        if (Util.isBlank(str)) {
            return "";
        }else if (str.length() > length) {
            return 
                "<a title='"+conv(str)+"'>"+
                    conv(str.substring(0, length/2 - 5)+"..."+str.substring(str.length()-length/2))+
                "</a>";
        }else {
            return str;
        }
    }

    String shortenRight(String str, int length) {
        if (Util.isBlank(str)) {
            return "";
        }else if (str.length() > length) {
            return "<a title='"+conv(str)+"'>"+conv(str.substring(0, length - 3))+"...</a>";
        }else {
            return str;
        }
    }

    String substring(String str, int length) {
        if (Util.isBlank(str)) {
            return "";
        }else if (str.length() > length) {
            return str.substring(0, length);
        }else {
            return str;
        }
    }
    
	String splitJobInstanceId(String jobinsid) {
		return jobinsid.substring(0, jobinsid.length()-12) + "<br>" + jobinsid.substring(jobinsid.length()-12);
	}

	String splitJobExecutionId(String jobexeid) {
		return jobexeid.substring(0, jobexeid.length()-6) + "<br>" + jobexeid.substring(jobexeid.length()-6);
	}


    Properties schedulerProperties = null;
    
    String getProperty(String name) {
    	
    	if (schedulerProperties == null) {
    		Properties p = new Properties();
    		InputStream propIn = null;
    		try {
    			propIn = this.getClass().getClassLoader().getResourceAsStream("properties/nexcore-scheduler-server.properties");
	    		p.load(propIn);
    		}catch(Exception e) {
    			throw Util.toRuntimeException(e);
    		}finally {
    			try {
    				propIn.close();
    			}catch(Exception ignore) {}
    		}
    		schedulerProperties = p;
    	}
    	return schedulerProperties.getProperty(name);
    	
    }
    
    boolean iSOracle(){
        return "ORACLE".equalsIgnoreCase(getProperty("scheduler.db.vendor"));
    }

    boolean isSybaseDB(){
		return "SYBASE".equalsIgnoreCase(getProperty("scheduler.db.vendor"));
	}

    boolean isDB2(){
    	return "DB2".equalsIgnoreCase(getProperty("scheduler.db.vendor"));
    }

    boolean isMSSQL(){
        return "MSSQL".equalsIgnoreCase(getProperty("scheduler.db.vendor"));
    }

    int toInt(String s, int defaultValue) {
    	if (Util.isBlank(s)) {
    		return defaultValue;
    	}else {
    		return Integer.parseInt(s.trim());
    	}
    }
    
    long toLong(String s, long defaultValue) {
    	if (Util.isBlank(s)) {
    		return defaultValue;
    	}else {
    		return Long.parseLong(s.trim());
    	}
    }
    
    Log getMainLog() {
    	return LogManager.getSchedulerLog();	
    }
    
    String conv(String s) {
        if (s == null) return "";
        int fromIdx  = 0;
        int totallen = s.length();
        StringBuilder tmp = null;
        for (int i=0; i<totallen; i++) {
            if (s.charAt(i) == '&') {
            	if (tmp==null) tmp = new StringBuilder((int)(s.length() * 1.5)); /* create only if needs */
                tmp.append(s.substring(fromIdx, i));
                tmp.append("&amp;");
                fromIdx = i+1;
                continue;
            }
    
            if (s.charAt(i) == '\"') {
            	if (tmp==null) tmp = new StringBuilder((int)(s.length() * 1.5)); /* create only if needs */
                tmp.append(s.substring(fromIdx, i));
                tmp.append("&quot;");
                fromIdx = i+1;
                continue;
            }
    
            if (s.charAt(i) == '<') {
            	if (tmp==null) tmp = new StringBuilder((int)(s.length() * 1.5)); /* create only if needs */
                tmp.append(s.substring(fromIdx, i));
                tmp.append("&lt;");
                fromIdx = i+1;
                continue;
            }
    
            if (s.charAt(i) == '>') {
            	if (tmp==null) tmp = new StringBuilder((int)(s.length() * 1.5)); /* create only if needs */
                tmp.append(s.substring(fromIdx, i));
                tmp.append("&gt;");
                fromIdx = i+1;
            }
        }
        if (tmp==null) {
        	return s; /* no special chars found */
        }else {
	        tmp.append(s.substring(fromIdx, totallen));
	        return tmp.toString();
        }
    }
    
	void printTextWithLTGT(byte[] b, int offset, int length, javax.servlet.jsp.JspWriter out) throws IOException {
		boolean findLTGT = false;
		int beginIndex = offset; 
		int writeLength = 0; 
		for (int i=0; i<length; i++) {
			if (b[i+offset] == '<') {
				out.print(new String(b, beginIndex, writeLength));
				out.print("&lt;");
				writeLength = 0;
				beginIndex = i+offset+1;
			}else if (b[i+offset] == '>') {
				out.print(new String(b, beginIndex, writeLength));
				out.print("&gt;");
				writeLength = 0;
				beginIndex = i+offset+1;
			}else {
				writeLength ++;
			}
		}
		out.print(new String(b, beginIndex, writeLength));
	}

	void printTextWithLTGT(byte[] b, int offset, int length, String charset, javax.servlet.jsp.JspWriter out) throws IOException {
		boolean findLTGT = false;
		int beginIndex = offset; 
		int writeLength = 0; 
		for (int i=0; i<length; i++) {
			if (b[i+offset] == '<') {
				out.print(new String(b, beginIndex, writeLength, charset));
				out.print("&lt;");
				writeLength = 0;
				beginIndex = i+offset+1;
			}else if (b[i+offset] == '>') {
				out.print(new String(b, beginIndex, writeLength, charset));
				out.print("&gt;");
				writeLength = 0;
				beginIndex = i+offset+1;
			}else {
				writeLength ++;
			}
		}
		out.print(new String(b, beginIndex, writeLength, charset));
	}

    String printSpace(int len, int spaceSize) {
    	StringBuilder sb = new StringBuilder(len * 6);
    	for (int i=0; i<len; i++) {
    		for (int j=0; j<spaceSize; j++) {
    			sb.append("&nbsp;");
    		}
    	}
    	return sb.toString();
    }
    
	/* ���� ���� ���� �Լ�  */
	String printJobNotifyReceiveInfo(JobNotifyReceiver receiver) {

		StringBuilder temp = new StringBuilder(128);
		if (receiver.isRecvByEmail()) {
			temp.append("[Email : <b>"     + conv(receiver.getEmailAddr())   + "</b>] ");
		}
		if (receiver.isRecvBySms()) {
			temp.append("[SMS : <b>"       + conv(receiver.getSmsNum())      + "</b>] ");
		}
		if (receiver.isRecvByTerminal()) {
			temp.append("[Terminal : <b>"  + conv(receiver.getTerminalId())  + "</b>] ");
		}
		if (receiver.isRecvByMessenger()) {
			temp.append("[Messenger : <b>" + conv(receiver.getMessengerId()) + "</b>] ");
		}
		if (receiver.isRecvByDev1()) {
			temp.append("[Dev1 : <b>"      + conv(receiver.getDev1Point())   + "</b>] ");
		}
		if (receiver.isRecvByDev2()) {
			temp.append("[Dev2 : <b>"      + conv(receiver.getDev2Point())   + "</b>] ");
		}
		if (receiver.isRecvByDev3()) {
			temp.append("[Dev3 : <b>"      + conv(receiver.getDev3Point())   + "</b>] ");
		}
		return temp.toString();
	}

	Comparator getComparator(final String methodName, final boolean ascending) {
		Comparator comparator = new Comparator () {
			Method m = null;
		    public int compare(Object o1, Object o2) {
		    	try {
		    		if (m == null) {
			    		this.m = o1.getClass().getMethod(methodName, null);
		    		}
		    		Comparable v1 = (Comparable)m.invoke(o1, null);
		    		Comparable v2 = (Comparable)m.invoke(o2, null);
			    	int result;
		    		if (v1 == null && v2 == null) {
	    				result = 0;
		    		}else if (v1 == null && v2 != null) {
		    			result = 1;
		    		}else if (v1 != null && v2 == null) {
		    			result = -1;
		    		}else {
		    			result = v1.compareTo(v2);
		    		}
			    		
			    	if (ascending) {
			        	return result;
			    	}else {
			    		return result * -1;
			    	}
		    	}catch(Exception e) {
		    		throw Util.toRuntimeException(e);
		    	}
		    }
		};
		return comparator;
	}
	
	void eliminateListByFilter(List list, Map<String/* method name */, String/* filter value */> filterFieldMap) {
		if (list == null || list.size() == 0) {
			return ;
		}
		
    	try {
			Object o = list.get(0);
			Map<String, Method> methodMap = new HashMap();
			for (Map.Entry<String, String> filterField : filterFieldMap.entrySet()) {
				String methodName = filterField.getKey();
				Method m = o.getClass().getMethod(methodName, null);
				methodMap.put(methodName, m);
			}
			
			Iterator iter = list.iterator();
			while(iter.hasNext()) {
				Object obj = iter.next();
				for (Map.Entry<String, String> filterField : filterFieldMap.entrySet()) {
					if (Util.isBlank(filterField.getValue())) {
						continue;
					}else {
						Method m = methodMap.get(filterField.getKey());
						String valueOfObject = String.valueOf(m.invoke(obj, null));
						if (!valueOfObject.contains(filterField.getValue())) {
							iter.remove();
							break;
						}
					}
				}
			}
    	}catch(Exception e) {
    		throw Util.toRuntimeException(e);
    	}
	}
    
%>