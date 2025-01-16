package nexcore.scheduler.controller.http;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.Util;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class JettyStarter {
	private boolean   httpEnable = true;     // 기동 여부. war 방식 배포에서는 jetty 를 기동할 필요없다.
	private String    httpPort;
	private boolean   httpsEnable = false;
	private String    httpsPort;
	private String    httpsKeyStore;
	private String    httpsKeyPassword;
	private String    httpsTrustStore;
	private String    httpsTrustPassword;
	private String    contextPath;
//	private boolean   useRealm = false;	// Realm 사용 여부, nexcore-bat-scheduler.xml 에서 지정
	private int       maxFormContentLength = 10*1024*1024; // 10 M
	private List<Connector> connectorList = new ArrayList<Connector>();
	
	public JettyStarter() {
	}
	
	public void init() {
		start();
	}
	
	public void destroy() {
	}
	
	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
	
//	public void setUseRealm(boolean useRealm) {
//		this.useRealm = useRealm;
//	}
//	
//	public boolean useRealm() {
//		return useRealm;
//	}

	public boolean isHttpEnable() {
		return httpEnable;
	}

	public void setHttpEnable(boolean httpEnable) {
		this.httpEnable = httpEnable;
	}

	public String getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(String httpPort) {
		this.httpPort = httpPort;
	}

	public boolean isHttpsEnable() {
		return httpsEnable;
	}

	public void setHttpsEnable(boolean httpsEnable) {
		this.httpsEnable = httpsEnable;
	}

	public String getHttpsPort() {
		return httpsPort;
	}

	public void setHttpsPort(String httpsPort) {
		this.httpsPort = httpsPort;
	}

	public String getHttpsKeyStore() {
		return httpsKeyStore;
	}

	public void setHttpsKeyStore(String httpsKeyStore) {
		this.httpsKeyStore = httpsKeyStore;
	}

	public String getHttpsKeyPassword() {
		return httpsKeyPassword;
	}

	public void setHttpsKeyPassword(String httpsKeyPassword) {
		this.httpsKeyPassword = httpsKeyPassword;
	}

	public String getHttpsTrustStore() {
		return httpsTrustStore;
	}

	public void setHttpsTrustStore(String httpsTrustStore) {
		this.httpsTrustStore = httpsTrustStore;
	}

	public String getHttpsTrustPassword() {
		return httpsTrustPassword;
	}

	public void setHttpsTrustPassword(String httpsTrustPassword) {
		this.httpsTrustPassword = httpsTrustPassword;
	}

	public int getMaxFormContentLength() {
		return maxFormContentLength;
	}

	public void setMaxFormContentLength(int maxFormContentLength) {
		this.maxFormContentLength = maxFormContentLength;
	}
	
	private Connector makeHttpConnector() {
		if(!httpEnable) {
			Util.logServerInitConsole("HttpServer", "off");
			return null;
		}
		
//		SelectChannelConnector connector = new SelectChannelConnector(); 
		// NIO 버그 때문에 BIO 로 변경함. 
		SocketConnector connector =  new SocketConnector();
		connector.setName("HttpServer");
		connector.setPort(Integer.parseInt(httpPort));
		if(httpsEnable){
			connector.setConfidentialPort(Integer.parseInt(httpsPort));
		}
		
		return connector;
	}
	
	private Connector makeHttpsConnector() {
		if(!httpsEnable) {
			Util.logServerInitConsole("HttpsServer", "off");
			return null;
		}
		
		SslSocketConnector connector =  new SslSocketConnector();
		connector.setName("HttpsServer");
		connector.setPort(Integer.parseInt(httpsPort));
		connector.setKeystore(httpsKeyStore);
		connector.setKeyPassword(httpsKeyPassword);
		connector.setPassword(httpsTrustPassword);
		connector.setTruststore(httpsTrustStore);
		connector.setTrustPassword(httpsTrustPassword);		
		
		return connector;
	}
	
	private SecurityHandler makeRedirectHandler(){
		Constraint constraint = new Constraint();
		constraint.setDataConstraint(Constraint.DC_CONFIDENTIAL);
		
		ConstraintMapping constraintMapping = new ConstraintMapping();
		constraintMapping.setConstraint(constraint);
		constraintMapping.setPathSpec("/*");
		
		SecurityHandler securityHandler = new SecurityHandler();
		securityHandler.setConstraintMappings(new ConstraintMapping[]{constraintMapping});
		
		return securityHandler;
	}
	
	private void start() {
		if(!httpEnable && !httpsEnable) {
			Util.logServerInitConsole("WebServer", "off");
			return;
		}
		
		try {
			System.setProperty("org.mortbay.log.class", "nexcore.scheduler.controller.http.Log4jBridge");
			System.setProperty("org.mortbay.jetty.Request.maxFormContentSize", String.valueOf(maxFormContentLength));

			WebAppContext webappcontext = new WebAppContext(System.getProperty("NEXCORE_HOME")+"/web", contextPath);
			webappcontext.setDescriptor(System.getProperty("NEXCORE_HOME")+"/etc/web.xml");
			webappcontext.setTempDirectory(new File(System.getProperty("NEXCORE_HOME")+"/tmp"));
//			webappcontext.getServletContext().getContextHandler().setMaxFormContentSize(maxFormContentLength);
			
			// Http Connector
			Connector httpConnector = makeHttpConnector();
			if(httpConnector != null){
				connectorList.add(httpConnector);
			}
			
			// Https Connector
			Connector httpsConnector = makeHttpsConnector();
			if(httpsConnector != null) {
				connectorList.add(httpsConnector);
				webappcontext.addHandler(makeRedirectHandler());
			}
			
			Server server = new Server();
			server.setHandler(webappcontext);
			server.setStopAtShutdown(true);
			server.setSendServerVersion(true);
			server.setGracefulShutdown(1000);
			server.setSendDateHeader(true);
			for(Connector connector : connectorList){
				server.addConnector(connector);
			}
			
//			// IoC XML(nexcore-bat-scheduler.xml) 설정에 따라 Realm 사용을 적용한다. 
//			// realm 파일 : SCHEDULER_HOME/etc/realm.properties 
//			if (useRealm) {
//				
//				Constraint constraint = new Constraint();
//				constraint.setName(Constraint.__BASIC_AUTH);;
//				constraint.setRoles(new String[]{"user","admin","operator"});
//				constraint.setAuthenticate(true);
//				
//				ConstraintMapping cm = new ConstraintMapping();
//				cm.setConstraint(constraint);
//				cm.setPathSpec("/*");
//				
//				SecurityHandler sh = new SecurityHandler();
//				sh.setUserRealm(new HashUserRealm("NEXCORE BATCH",System.getProperty("NEXCORE_HOME")+"/etc/realm.properties"));
//				sh.setConstraintMappings(new ConstraintMapping[]{cm});
//				
//				webappcontext.addHandler(sh);				
//			}

			server.start();
			
			for(Connector connector : connectorList){
				Util.logServerInitConsole(connector.getName(), "("+connector.getPort()+")");
			}
		}catch (Exception e) {
			throw new SchedulerException("main.http.start.error", e);
		}
	}
}
