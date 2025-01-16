package nexcore.scheduler.core.internal;

import java.sql.SQLException;

import org.apache.commons.logging.Log;

import nexcore.framework.license.LicenseException;
import nexcore.framework.license.LicenseValidator;
import nexcore.framework.license.ProductInfo;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.monitor.internal.AgentInfoManager;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.Util;

public class LicenseManager implements Runnable {
	private final String LICENSE_PRODUCT_CODE     = "NBS";
	private final String LICENSE_ATTR_AGENT_LIMIT = "agentLimit";
	private final String LICENSE_ATTR_JOB_LIMIT   = "jobLimit";
	private final String LICENSE_ATTR_DUAL_SERVER = "dualServer";
	
	private AgentInfoManager     agentInfoManager;
	private JobDefinitionManager jobDefinitionManager;
	private String               filename = "/license/nexcore.license";
	
	private Thread thisThread;
	
	private String licenseEdition;
	private int maxAgentCount;
	private int maxJobDefinitionCount;
	private boolean ha;
	
	private int agentCount;
	private int jobDefinitionCount;
	
	private Log       		log;
	
	public LicenseManager() {
	}
	
	public AgentInfoManager getAgentInfoManager() {
		return agentInfoManager;
	}

	public void setAgentInfoManager(AgentInfoManager agentInfoManager) {
		this.agentInfoManager = agentInfoManager;
	}

	public JobDefinitionManager getJobDefinitionManager() {
		return jobDefinitionManager;
	}

	public void setJobDefinitionManager(JobDefinitionManager jobDefinitionManager) {
		this.jobDefinitionManager = jobDefinitionManager;
	}

	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void init() {
		log = LogManager.getSchedulerLog();
		
		// 라이센스 정보 추출
		LicenseValidator validator = new LicenseValidator();
		try {
			validator.verifyLicense(LICENSE_PRODUCT_CODE, LicenseValidator.HOST, getFilename());
			ProductInfo productInfo = validator.getProductInfo(LICENSE_PRODUCT_CODE);

			licenseEdition = productInfo.getProductEdition();
			maxAgentCount = Integer.parseInt(productInfo.getAttribute(LICENSE_ATTR_AGENT_LIMIT));
			maxJobDefinitionCount = Integer.parseInt(productInfo.getAttribute(LICENSE_ATTR_JOB_LIMIT));
			ha = Boolean.parseBoolean(productInfo.getAttribute(LICENSE_ATTR_DUAL_SERVER));
			
			Util.logServerInitConsole("LicenseManager");
		} catch (LicenseException e) {
			if(e.getErrorCode() == LicenseValidator.ERR_INVALID_LICENSE){
				Util.logErrorConsole(MSG.get("main.license.invalid"));
			}
			else if(e.getErrorCode() == LicenseValidator.ERR_INVALID_HOST){
				Util.logErrorConsole(MSG.get("main.license.incorrect.hostname"));
			}
			else if(e.getErrorCode() == LicenseValidator.ERR_INVALID_DATE){
				Util.logErrorConsole(MSG.get("main.license.expired.date"));
			}
			else {
				Util.logErrorConsole(MSG.get("main.license.no.file"));
			}
			System.exit(2);
		}
		
		// Thread 시작
		thisThread = new Thread(this, "LicenseManager");
		thisThread.setDaemon(true);
		thisThread.start();
	}
	

	
	public void run() {
		while(!Thread.interrupted()) {
			
			// AgentInfoManager로부터 Agent Count 추출
			try {
				agentCount = agentInfoManager.getAgentInfoCountExceptInternalInCache();
			} catch(SQLException e){
				Util.logError(log, MSG.get("main.license.get.agentcount.error"));
				throw new SchedulerException("main.license.get.agentcount.error", e);
			}

			// JobDefinitionManager로부터 JobDefinition Count 추출
			try {
				jobDefinitionCount = jobDefinitionManager.getJobDefinitionsCount();
			} catch(SQLException e){
				Util.logError(log, MSG.get("main.license.get.jobdefcount.error"));
				throw new SchedulerException("main.license.get.jobdefcount.error", e); 
			}
			
			Util.logInfo(log, MSG.get("main.license.get.agent.jobdef.count", agentCount, maxAgentCount, jobDefinitionCount, maxJobDefinitionCount));
			
			Util.sleep(60000);
		}	
	}
	
	public void destroy() {
	}

	public String getLicenseEdition() {
		return licenseEdition;
	}

	public int getMaxAgentCount() {
		return maxAgentCount;
	}

	public int getMaxJobDefinitionCount() {
		return maxJobDefinitionCount;
	}

	public boolean canHA() {
		return ha;
	}
	
	public int getAgentCountInCache() {
		return agentCount;
	}
	
	public int getJobDefinitionCountInCache() {
		return jobDefinitionCount;
	}

	public boolean checkValidUsingCache(){
		if((maxJobDefinitionCount != 0 && maxJobDefinitionCount < jobDefinitionCount) || (maxAgentCount != 0 && maxAgentCount < agentCount))
			return false;
		return true;
	}
}
