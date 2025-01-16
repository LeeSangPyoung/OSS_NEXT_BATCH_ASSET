package nexcore.scheduler.startup;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import nexcore.scheduler.core.VERSION;
import nexcore.scheduler.ioc.BeanRegistry;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 배치 스케줄러를 실행 시키는 메인 서블릿. Webapp로 실행될 때 사용됨</li>
 * <li>작성일 : 2012. 4. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class StarterMainServlet extends HttpServlet {
	
	private static final long serialVersionUID = -859743121335779128L;

	public void init(ServletConfig config) throws ServletException {
		try {
			if (Util.isBlank(System.getProperty("NEXCORE_HOME"))) {
				String nexcoreHome = config.getInitParameter("NEXCORE_HOME");
				
				if (Util.isBlank(nexcoreHome)) { /* web.xml 에 설정되어있지 않는다면 WEB-INF 를 HOME 으로 본다. */
					nexcoreHome = config.getServletContext().getRealPath("/")+"/WEB-INF";
				}
				System.setProperty("NEXCORE_HOME", nexcoreHome);
			}
			Util.logInfoConsole("NEXCORE_HOME : "+System.getProperty("NEXCORE_HOME"));
			
			if (Util.isBlank(System.getProperty("NEXCORE_ID"))) {
				String systemId = config.getInitParameter("NEXCORE_ID");
				System.setProperty("NEXCORE_ID", systemId);
			}
			Util.logInfoConsole("NEXCORE_ID : " + System.getProperty("NEXCORE_ID"));
			
			
			Util.logInfoConsole("=================================================================");
			Util.logInfoConsole(MSG.get("main.starting.message", System.getProperty("NEXCORE_ID")));
			
			URL xmlFile = StarterMain.class.getClassLoader().getResource("beans/nexcore-scheduler-server.xml");
			File xmlDirFile = new File(xmlFile.toURI().getPath()).getParentFile();
			File[] xmlFiles = xmlDirFile.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml");
				}
			});
			
			List<String> filenames = new ArrayList();
			filenames.add("nexcore/scheduler/config/nexcore-scheduler-server-core.xml.notouch"); // 이 파일은 nexcore-scheduler-server.jar 에 묶여져 있다.
			for (File file : xmlFiles) {
				String filename = file.getName();
				int idx = filename.indexOf("@");
				if (idx > -1) {
					String systemId = filename.substring(idx+1, filename.indexOf(".", idx)); // NEXCORE_ID 비교
					if (!Util.equals(systemId, Util.getSystemId())) {
						continue; 
					}
				}
				
				filenames.add("beans/"+file.getName());
			}
			
			String[] filenamesArray = new String[filenames.size()];
			filenames.toArray(filenamesArray);
			
			Util.logInfoConsole("Configurations:"+Arrays.asList(filenamesArray));
			
			// 시작
			BeanRegistry.init(filenamesArray);
			
			Util.logInfoConsole(MSG.get("main.started.message", System.getProperty("NEXCORE_ID")));
			Util.logInfoConsole("Version : "+VERSION.toVersionString());
			Util.logInfoConsole("===========================================");
		}catch(Exception e) {
			throw new ServletException("NEXCORE Batch Scheduler start fail", e);
		}
	}
	
	public void destroy() {
		super.destroy();
		BeanRegistry.destroy();
	}
}
