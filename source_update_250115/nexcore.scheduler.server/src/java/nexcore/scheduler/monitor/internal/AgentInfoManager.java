package nexcore.scheduler.monitor.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import nexcore.scheduler.entity.AgentInfo;
import nexcore.scheduler.entity.IAgentService;
import nexcore.scheduler.agent.client.AgentClientByRmi;
import nexcore.scheduler.agent.client.IAgentClient;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.NRMIClientSocketFactory;
import nexcore.scheduler.util.Util;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : AgentInfo 매니저 </li>
 * <li>작성일 : 2011. 1. 22.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

// AgentInfo 는 거의 변경없이 readonly 로 주로 사용되므로. 메모리에 올려놓고 사용한다.
public class AgentInfoManager {
	private SqlMapClient                sqlMapClient;
	private IAgentClient                internalAgentClient;

	private Map<String, AgentInfo>      cache          = new ConcurrentHashMap<String, AgentInfo>();
	private Map<String, IAgentClient>   agentClientMap = new ConcurrentHashMap<String, IAgentClient>();

	private long lastCacheRefreshTime = 0;
	
	public void init() {
		Util.logServerInitConsole("AgentInfoManager");
	}
	
	public void destroy() {
	}
	
	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}
	
	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}
	
	public IAgentClient getInternalAgentClient() {
		return internalAgentClient;
	}

	public void setInternalAgentClient(IAgentClient internalAgentClient) {
		this.internalAgentClient = internalAgentClient;
	}
	
	// =====================================================

	private int insertAgentInfo(AgentInfo agentInfo) throws SQLException {
		int cnt = sqlMapClient.update("nbs.monitor.insertAgentInfo", agentInfo);
		return cnt;
	}

	private AgentInfo selectAgentInfo(String id) throws SQLException {
		return (AgentInfo)sqlMapClient.queryForObject("nbs.monitor.selectAgentInfo", id); 
	}

	private List<AgentInfo> selectAgentInfoByQuery(String query, String orderBy) throws SQLException {
		Map m = new HashMap();
		m.put("queryCondition", query);
		m.put("orderBy",        orderBy);
		return (List<AgentInfo>)sqlMapClient.queryForList("nbs.monitor.selectAgentInfoByQuery", m);
	}
	
	private int selectAgentInfoCountByQuery(String query) throws SQLException {
		Map<String, String> m = new HashMap<String, String>();
		m.put("queryCondition", query);
		return (Integer)sqlMapClient.queryForObject("nbs.monitor.selectAgentInfoCountByQuery", m);
	}

	private int updateAgentInfo(AgentInfo agentInfo) throws SQLException {
		return sqlMapClient.update("nbs.monitor.updateAgentInfo", agentInfo);
	}
	
	private int deleteAgentInfo(String id) throws SQLException {
		return sqlMapClient.delete("nbs.monitor.deleteAgentInfo", id);
	}

	public int addAgentInfo(AgentInfo agentInfo) throws SQLException {
		agentInfo.setLastModifyTime(DateUtil.getCurrentTimestampString());
		int cnt = insertAgentInfo(agentInfo);
		return cnt;
	}

	public AgentInfo getAgentInfo(String id) throws SQLException {
		if (Util.isBlank(id)) return null;
		AgentInfo agentInfo = cache.get(id);  // 캐쉬에서 읽어옴.
		if (agentInfo == null) { 
			agentInfo = selectAgentInfo(id);
			if (agentInfo != null) {
				cache.put(id, agentInfo);
			}
		}
		return agentInfo;
	}
	
	/**
	 * DB에 저장된 Internal 을 제외한 Agent 의 개수를 리턴함
	 * @return Agent 수
	 * @throws SQLException
	 */
	public int getAgentInfoCountExceptInternal() throws SQLException {
		
		return selectAgentInfoCountByQuery("WHERE AGENT_IP <> '@INTERNAL'");
	}
	
	/**
	 * 캐쉬에 저장된 Internal 을 제외한 Agent 의 개수를 리턴함
	 * @return Agent 수
	 * @throws SQLException
	 */
	public int getAgentInfoCountExceptInternalInCache() throws SQLException {
		int count = 0;
		List<AgentInfo> agentList = getCachedAgentInfos();
		for(AgentInfo agentInfo : agentList){
			if(!agentInfo.getIp().equals("@INTERNAL"))
				count++;
				
		}
		
		return count;
	}
	
	/**
	 * 전체 조회는 테이블에서 다시 읽으며 메모리 cache 도 refresh 한다.
	 * @return
	 * @throws SQLException
	 */
	public List<AgentInfo> getAllAgentInfos() throws SQLException {
		List<AgentInfo> agentInfoList = selectAgentInfoByQuery("", " ORDER BY AGENT_ID "); // 전체 리스트 조회는 캐쉬에서 읽지 않음
		Map<String, AgentInfo> cacheTmp = new ConcurrentHashMap<String, AgentInfo>();
		for (AgentInfo agentInfo : agentInfoList) {
			cacheTmp.put(agentInfo.getId(), agentInfo);
		}
		Map cacheOld = cache;
		cache = cacheTmp;
		cacheOld.clear();
		lastCacheRefreshTime = System.currentTimeMillis();
		return agentInfoList;
	}

	/** 
	 * 모니터링시 성능을 위해 캐쉬 메모리를 리턴한다.
	 * 혹시 모를 정합성을 위해 1분에 한번은 캐쉬를 refresh 한다.
	 * @return
	 * @throws SQLException
	 */
	public List<AgentInfo> getCachedAgentInfos() throws SQLException {
		if (System.currentTimeMillis() > lastCacheRefreshTime + 60000) {
			getAllAgentInfos(); // refresh cache
		}
		return new ArrayList<AgentInfo>(cache.values());
	}
	
	public int modifyAgentInfo(AgentInfo agentInfo) throws SQLException {
		agentInfo.setLastModifyTime(DateUtil.getCurrentTimestampString());
		int cnt = updateAgentInfo(agentInfo);
		if (cnt > 0) {
			agentClientMap.remove(agentInfo.getId());
			cache.put(agentInfo.getId(), agentInfo);
		}
		return cnt;
	}
	
	public int removeAgentInfo(String id) throws SQLException {
		int cnt = deleteAgentInfo(id);
		agentClientMap.remove(id);
		cache.remove(id);
		return cnt;
	}
	
	/**
	 * cache 를 삭제한다.
	 * @param id
	 */
	public void removeCache(String id) {
		agentClientMap.remove(id);
		cache.remove(id);
	}
	
	
	/**
	 * AgentClient 와 RMI Connection 을 설정함. Spring RmiProxyFactoryBean 을 이용함.
	 */
	private IAgentClient initAgentClient(AgentInfo agentInfo) {
		IAgentClient agentClient = null;
		/* ip 가 "@INTERNAL" 일 경우는 RMI 연결하지 않고 LOCAL CALL 한다. */

		if ("@INTERNAL".equalsIgnoreCase(agentInfo.getIp())) {
			agentClient = internalAgentClient; 
		}else {
			// RMI Proxy 생성
			// nexcore-bat-scheduler.xml 파일에서 설정으로 해야하지만, 동적으로 생성하기 위해서 이렇게 한다.
			RmiProxyFactoryBean rmiProxyFactory = new RmiProxyFactoryBean();
			rmiProxyFactory.setServiceUrl("rmi://"+agentInfo.getIp()+":"+agentInfo.getPort()+"/BatchAgent");
			rmiProxyFactory.setServiceInterface(IAgentService.class);
			rmiProxyFactory.setRefreshStubOnConnectFailure(true);
			rmiProxyFactory.setLookupStubOnStartup(false);
			rmiProxyFactory.setRegistryClientSocketFactory(new NRMIClientSocketFactory(5000, 30000));
			rmiProxyFactory.afterPropertiesSet();
			
			// AgentClient object 생성
			agentClient = new AgentClientByRmi();
			((AgentClientByRmi)agentClient).setAgentService((IAgentService)rmiProxyFactory.getObject());
			((AgentClientByRmi)agentClient).setAgentId(agentInfo.getId());
			((AgentClientByRmi)agentClient).init();
		}
		
		// map 에 넣기
		agentClientMap.put(agentInfo.getId(), agentClient);
		return agentClient;
	}
	
	/**
	 * 통신을 할 수 있는 AgentClient 객체를 생성 & get 함.
	 * @param agentId
	 * @return
	 */
	public IAgentClient getAgentClient(String agentId) {
		AgentInfo      agentInfo    = null;
		IAgentClient   agentClient  = null;
		try {
			agentInfo = getAgentInfo(agentId);
			if (agentInfo == null) {
				throw new SchedulerException("main.agent.wrong.agentid", agentId);
			}
			if (!agentInfo.isInUse()) {
				throw new SchedulerException("main.agent.disabled.agent", agentId);
			}

			// 이미 생성된 놈이 있는지 찾는다.
			agentClient = (IAgentClient) agentClientMap.get(agentId);
			if (agentClient != null) {
				return agentClient;
			}else {
				agentClient = initAgentClient(agentInfo);
				agentClientMap.put(agentId, agentClient);
			}
		}catch (SQLException e) {
			throw new SchedulerException("main.agent.get.agentinfo.error", e, agentId);
		}catch (SchedulerException e) {
			throw e;
		}catch (Exception e) {
			throw new SchedulerException("main.agent.agentclient.connect.error", e, agentId);
		}

		return agentClient;
	}
	
	/**
	 * 해당 에이전트가 internal 에이전트인지?
	 * @param agentId
	 * @return
	 * @throws SQLException 
	 * @throws SchedulerException
	 */
	public boolean isInternalAgent (String agentId) throws SQLException {
		AgentInfo info = getAgentInfo(agentId);
		if (info == null) {
			throw new SchedulerException("main.agent.wrong.agentid", agentId);
		}
		return info.isInternal();
	}
	
	/**
	 * 사용중인 에이전트인지?
	 * @param agentId
	 * @return
	 * @throws SQLException
	 */
	public boolean isInUseAgent (String agentId) throws SQLException {
		AgentInfo info = getAgentInfo(agentId);
		if (info == null) {
			throw new SchedulerException("main.agent.wrong.agentid", agentId);
		}
		return info.isInUse();
	}
}