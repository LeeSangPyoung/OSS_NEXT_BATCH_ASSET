package nexcore.scheduler.controller.http;

import org.apache.commons.logging.Log;
import org.mortbay.log.Logger;

import nexcore.scheduler.log.LogManager;

/**
 * 
 * @author 정호철
 *
 */
public class Log4jBridge implements Logger {
	private Log  		log;

	public Log4jBridge() {
		log = LogManager.getLog("jetty");
	}
	
	public void debug(String msg, Throwable th) {
		log.debug(msg, th);
	}

	public void debug(String msg, Object arg0, Object arg1) {
		log.debug(msg+(arg0==null?"":","+arg0)+(arg1==null?"":","+arg1));
	}

	public Logger getLogger(String name) {
		return new Log4jBridge();
	}

	public void info(String msg, Object arg0, Object arg1) {
		log.info(msg+(arg0==null?"":","+arg0)+(arg1==null?"":","+arg1));
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public void setDebugEnabled(boolean enabled) {
		// can not
	}

	public void warn(String msg, Throwable th) {
		log.warn(msg, th);
		
	}

	public void warn(String msg, Object arg0, Object arg1) {
		log.warn(msg+(arg0==null?"":","+arg0)+(arg1==null?"":","+arg1));
	}

	public String toString() {
		return "Logger";
	}
}
