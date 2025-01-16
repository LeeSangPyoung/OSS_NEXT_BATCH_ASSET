<%@page import="java.util.*" pageEncoding="euc-kr"
%><%@page import="nexcore.scheduler.entity.*"
%><%@page import="nexcore.scheduler.util.*"
%><%@page import="nexcore.scheduler.controller.admin.*"
%><%!
	/*********************************************************************/
	/*                         CUSTOMIZE ���� ����                       */
	/*********************************************************************/

	/**
	 * ########## DO NOT EDIT THIS METHOD ##########
     *  Job ��� ��ȸ�� ȭ�鿡 print �ϱ� ���� �� �޼ҵ带 �ѹ� ȣ���ϰ� 
     *  �� �޼ҵ尡 true�� �����ϸ� �� Job�� ȭ�鿡 ����Ʈ��
     *  @param jobid üũ�� Job ID
     *  @param filterBy : ���͸� �� ���а�. printJobFilter() ������ <select �±��� ����. null�϶��� ��� Job �����
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
     * ��ȸ ����� ���� <select> �±׸� print �� 
     * @param filter ���� ���õ� filter
     * @param request ����ں��� �������� ǥ���� �ʿ䰡 ������ ����ϱ� ���� request ��ü ����
     */
	String printJobFilter(String filter, javax.servlet.http.HttpServletRequest request) {
        StringBuilder out = new StringBuilder(256);
		Map<String, String> jobFilterCodeList = getJobFilterCodeList(request);
		out.append(printSelectOption("", "��ü", filter));
		for (Map.Entry<String, String> entry : jobFilterCodeList.entrySet()) {
			out.append(printSelectOption(entry.getKey(), entry.getValue(), filter));
		}
		return out.toString();
	}

    /**
     * Job ID �� ���� filter �ڵ� ���� ã�Ƴ�.
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
     * Job ID filter �ڵ� ����Ʈ�� ������
     * @param request ����ں��� �ٸ� ����Ʈ�� ������ �� �ְ� �ϱ� ���ؼ��� request ��ü�� Ȱ���� �� �ִ�.
     * @return Map<code, name>
     */
	Map getJobFilterCodeList(javax.servlet.http.HttpServletRequest request) {
		Map map = new LinkedHashMap();
/*
		map.put("999", "����");
		map.put("028", "1����");
		map.put("023", "2����");
		map.put("406", "3����");
		map.put("621", "4����");
*/
		return map;
	}

    String getAppCode(String str) {
/* (����)
		char bnkCode   = str.charAt(10);
        String appCode = str.substring(1,4);
        StringBuilder temp = new StringBuilder(12);
        temp.append("[");
        temp.append(appCode);
        temp.append("]");
        temp.append("[");
        switch(bnkCode) {
            case 'S' : temp.append("����");break;
            case 'G' : temp.append("���");break;
            case 'H' : temp.append("ȣ��");break;
            case 'B' : temp.append("�λ�");break;
            default  : temp.append("����");break;
        }
        temp.append("]");
        return temp.toString();
*/
		return "";
    }

	String printMigrateServerList() {
		StringBuilder sb = new StringBuilder();
		sb.append(printSelectOption("","",""));
		sb.append(printSelectOption("1.1.1.1:8124","���߽����ٷ�",""));
		sb.append(printSelectOption("203.235.212.178:8124","������ٷ�",""));
		return sb.toString();
	}
	
	String printMigrateAgentList() {
		StringBuilder sb = new StringBuilder();
		sb.append(printSelectOption("","",""));
		sb.append(printSelectOption("dbt1","dbt1",""));
		sb.append(printSelectOption("dbt2","dbt2",""));
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
	/*                         CUSTOMIZE ���� ��                         */
	/*********************************************************************/
%>