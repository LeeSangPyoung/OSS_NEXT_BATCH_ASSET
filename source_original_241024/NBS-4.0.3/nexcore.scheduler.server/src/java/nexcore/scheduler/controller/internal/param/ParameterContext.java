package nexcore.scheduler.controller.internal.param;

import java.util.Map;
import java.util.Properties;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어 </li>
 * <li>설  명 : 파라미터를 해석하는데 필요한 부가 환경값들을 담고 있는 클래스. agent 의 system properties, system env 값들 </li>
 * <li>작성일 : 2010. 10. 21.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class ParameterContext {
	private Properties   agentSystemProperties;
	private Map          agentSystemEnv;
	private Map          parameters;
	
	public ParameterContext() {
	}

	public Properties getAgentSystemProperties() {
		return agentSystemProperties;
	}

	public void setAgentSystemProperties(Properties agentSystemProperties) {
		this.agentSystemProperties = agentSystemProperties;
	}

	public Map getAgentSystemEnv() {
		return agentSystemEnv;
	}

	public void setAgentSystemEnv(Map agentSystemEnv) {
		this.agentSystemEnv = agentSystemEnv;
	}

	public Map getParameters() {
		return parameters;
	}

	public void setParameters(Map parameters) {
		this.parameters = parameters;
	}

}
