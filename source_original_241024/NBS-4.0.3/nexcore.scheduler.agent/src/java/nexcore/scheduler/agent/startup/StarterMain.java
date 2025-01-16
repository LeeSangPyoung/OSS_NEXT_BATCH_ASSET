package nexcore.scheduler.agent.startup;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nexcore.scheduler.agent.VERSION;
import nexcore.scheduler.ioc.BeanRegistry;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : 배치 Agent 을 Non-WAS 환경에서 구동시키기 위한 메인 메소드</li>
 * <li>작성일 : 2010. 10. 27.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

// argument 로 agent 만 로드할지, cc도 같이 로드할지 결정.
public class StarterMain {

	public static void main(String[] args) {
	    Util.logInfoConsole("===========================================");
	    Util.logInfoConsole(MSG.get("agent.starting.message", System.getProperty("NEXCORE_ID")));

		try {
			// nexcore-scheduler-agent.xml 파일이 위치한 디렉토리에서 scan 하여 xml 파일들을 읽는다.
			URL xmlFile = StarterMain.class.getClassLoader().getResource("beans/nexcore-scheduler-agent.xml");
			File xmlDirFile = new File(xmlFile.toURI().getPath()).getParentFile();
			File[] xmlFiles = xmlDirFile.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml");
				}
			});
			
			List<String> filenames = new ArrayList();
			filenames.add("beans/nexcore-scheduler-agent.xml.boot"); // 이 파일은 확장자가 xml 이 아니므로 수동으로 넣어주어야한다.
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

            Util.logInfoConsole(MSG.get("agent.started.message", System.getProperty("NEXCORE_ID")));
            Util.logInfoConsole("Version : "+VERSION.toVersionString());
            Util.logInfoConsole("===========================================");
		}catch(Exception e) {
			throw Util.toRuntimeException(e);
		}

	}
}
