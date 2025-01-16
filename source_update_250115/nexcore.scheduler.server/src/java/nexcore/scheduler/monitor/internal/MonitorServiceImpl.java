package nexcore.scheduler.monitor.internal;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.core.internal.SystemMonitor;
import nexcore.scheduler.entity.AdminAuth;
import nexcore.scheduler.entity.AgentInfo;
import nexcore.scheduler.entity.AgentMonitoringSummary;
import nexcore.scheduler.entity.IMonitorService;
import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobGroup;
import nexcore.scheduler.entity.JobGroupAttrDef;
import nexcore.scheduler.entity.JobNotify;
import nexcore.scheduler.entity.JobNotifyReceiver;
import nexcore.scheduler.entity.JobNotifySendInfo;
import nexcore.scheduler.entity.User;
import nexcore.scheduler.entity.ViewFilter;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Monitor RMI Service.</li>
 * <li>작성일 : 2011. 1. 14.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class MonitorServiceImpl implements IMonitorService {
	private transient MonitorMain monitorMain;
	private transient SystemMonitor systemMonitor;

	public void init() {
		Util.logServerInitConsole("MonitorService");
	}
	public void destroy() {
	}

	public MonitorMain getMonitorMain() {
		return monitorMain;
	}
	public void setMonitorMain(MonitorMain monitorMain) {
		this.monitorMain = monitorMain;
	}

	public SystemMonitor getSystemMonitor() {
		return systemMonitor;
	}

	public void setSystemMonitor(SystemMonitor systemMonitor) {
		this.systemMonitor = systemMonitor;
	}


	public boolean addViewFilter(ViewFilter vf, AdminAuth auth) {
		return monitorMain.addViewFilter(vf, auth);
	}
	
	public ViewFilter getViewFilter(int id) {
		return monitorMain.getViewFilter(id);
	}
	
	public ViewFilter getViewFilterDeep(int id) {
		return monitorMain.getViewFilterDeep(id);
	}
	
	public List<ViewFilter> getViewFiltersByQuery(String query, String orderBy) {
		return monitorMain.getViewFiltersByQuery(query, orderBy);
	}
	
	public List<JobDefinition> getJobDefinitionsByViewFilter(int id) {
		return monitorMain.getJobDefinitionsByViewFilter(id);
	}
	
	public boolean removeViewFilter(int id, AdminAuth auth) {
		return monitorMain.removeViewFilter(id, auth);
	}
	
	public boolean modifyViewFilter(ViewFilter vf, AdminAuth auth) {
		return monitorMain.modifyViewFilter(vf, auth);
	}
	
	public boolean modifyViewFilterNoJobList(ViewFilter vf, AdminAuth auth) {
		return monitorMain.modifyViewFilterNoJobList(vf, auth);
	}
	
	public boolean modifyViewFilterAddJobList(int id, List<String> jobIdList, AdminAuth auth) {
		return monitorMain.modifyViewFilterAddJobList(id, jobIdList, auth);
	}
	
	public boolean modifyViewFilterDelJobList(int id, List<String> jobIdList, AdminAuth auth) {
		return monitorMain.modifyViewFilterDelJobList(id, jobIdList, auth);	
	}

	public List<AgentInfo> getAllAgentInfos() {
		return monitorMain.getAllAgentInfos();
	}

	public AgentInfo getAgentInfo(String id) {
		return monitorMain.getAgentInfo(id);
	}

	public boolean addAgentInfo(AgentInfo agentInfo, AdminAuth auth) {
		return monitorMain.addAgentInfo(agentInfo, auth);
	}

	public boolean removeAgentInfo(String agentId, AdminAuth auth) {
		return monitorMain.removeAgentInfo(agentId, auth);
	}
	
	public boolean modifyAgentInfo(AgentInfo agentInfo, AdminAuth auth) {
		return monitorMain.modifyAgentInfo(agentInfo, auth);
	}

	/**
	 * 해당 agent 에서 실행중, 일시정지 상태인 Job Execution 정보를 얻음.
	 * @return
	 */
	public List<JobExecution> getAgentRunningJobExecutions(String agentId) {
		return monitorMain.getAgentRunningJobExecutions(agentId);
	}

	public Map getAgentAllThreadStackTrace(String agentId) {
		return monitorMain.getAgentAllThreadStackTrace(agentId);
	}
	
	/**
	 * 에이전트의 JVM Properties 조회
	 */
	public Properties getAgentSystemProperties(String agentId) {
		return monitorMain.getAgentSystemProperties(agentId);
	}

	/**
	 * 에이전트의 JVM System Env 조회
	 * @param agentId
	 * @return
	 */
	public Map getAgentSystemEnv(String agentId) {
		return monitorMain.getAgentSystemEnv(agentId);
	}

	/**
	 * 에이전트의 설정 파일 목록을 조회
	 * @param agentId
	 * @return
	 */
	public List<String> getAgentConfigFiles(String agentId) {
		return monitorMain.getAgentConfigFiles(agentId);
	}

	/**
	 * agent 리스트 들의 상태를 체크해서 리턴함.
	 * @return Map&lt;String:(agentId), String:(checkResult)&gt;. checkResult="OK"이면 정상 
	 */
	public Map getAgentCheckList() {
		return monitorMain.getAgentCheckList();
	}
	
	/**
	 * 에이전트의 상태 체크. AgentMonitor 개 2초마다 폴링하여 캐쉬한 정보를 리턴함
	 * @param agentId
	 * @return "OK" if normal, other message if abnormal
	 */
	public String getAgentCheck(String agentId) {
		return monitorMain.getAgentCheck(agentId);
	}

	/**
	 * 에이전트의 상태 체크. 캐쉬정보를 이용하지 않고 즉시 체크한다.
	 * @param agentId
	 * @return "OK" if normal, other message if abnormal
	 */
	public String getAgentCheckNoCache(String agentId) {
		return monitorMain.getAgentCheckNoCache(agentId);
	}

	/**
	 * 하나의 Job Execution 의 진행 상태 값 (Progress)을 조회.
	 * 에이전트와 통신하여 지금 실시간 진행률 정보조회.
	 * end 된 Job은 조회 불가
	 * @return long[] ([0]=progressTotal, [1]=progressCurrent)
	 */
	public long[] getJobProgressNoCache(String jobExecutionId, String agentId) {
		return monitorMain.getJobProgressNoCache(jobExecutionId, agentId);
	}
	
	/**
	 * 하나의 Job Execution 의 진행 상태 값 (Progress)을 조회.
	 * 서버에 캐쉬된 진행률 정보 조회
	 * end 된 job 은 DB에서 조회
	 * @return long[] ([0]=progressTotal, [1]=progressCurrent)
	 */
	public long[] getJobProgress(String jobExecutionId) {
		return monitorMain.getJobProgress(jobExecutionId);
	}

	/**
	 * Job Instance 의 진행 상태 값 (Progress)을 조회. 에이전트 메모리
	 * @return &lt; jobInstanceId, Integer[] ([0]=progressTotal, [1]=progressCurrent) &gt;
	 */
	public Map<String, long[]> getJobProgressMapFromAgent() {
		return monitorMain.getJobProgressMapFromAgent();
	}

	public AgentMonitoringSummary getAgentMonitoringSummary(String agentId) {
		return monitorMain.getAgentMonitoringSummary(agentId);
	}

	public boolean closeOrOpenAgent(String agentId, boolean close, AdminAuth auth) {
		return monitorMain.closeOrOpenAgent(agentId, close, auth);
	}
	
	// =================================== JobNotify =========================================

	public boolean addJobNotify(JobNotify jobNotify, AdminAuth auth) {
		return monitorMain.addJobNotify(jobNotify, auth);
	}
	public boolean removeJobNotify(int id, AdminAuth auth) {
		return monitorMain.removeJobNotify(id, auth);
	}
	public boolean modifyJobNotify(JobNotify jobNotify, AdminAuth auth) {
		return monitorMain.modifyJobNotify(jobNotify, auth);
	}
	public JobNotify getJobNotify(int id) {
		return monitorMain.getJobNotify(id);
	}
	public List<JobNotify> getAllJobNotifies() {
		return monitorMain.getAllJobNotifies();
	}
	public boolean addJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver, AdminAuth auth) {
		return monitorMain.addJobNotifyReceiver(jobNotifyReceiver, auth);
	}
	public boolean removeJobNotifyReceiver(int id, AdminAuth auth) {
		return monitorMain.removeJobNotifyReceiver(id, auth);
	}
	public boolean modifyJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver, AdminAuth auth) {
		return monitorMain.modifyJobNotifyReceiver(jobNotifyReceiver, auth);
	}
	public JobNotifyReceiver getJobNotifyReceiver(int id) {
		return monitorMain.getJobNotifyReceiver(id);
	}
	public List<JobNotifyReceiver> getAllJobNotifyReceivers() {
		return monitorMain.getAllJobNotifyReceivers();
	}
	public Map<Integer, JobNotifyReceiver> getAllJobNotifyReceiversMap() {
		return monitorMain.getAllJobNotifyReceiversMap();
	}
	public List<JobNotifyReceiver> getJobNotifyReceiversByNotifyId(int id) {
		return monitorMain.getJobNotifyReceiversByNotifyId(id);
	}
	public List<JobNotifySendInfo> getJobNotifySendList(Map queryParamMap) {
		return monitorMain.getJobNotifySendList(queryParamMap);
	}
	public int getJobNotifySendListCount(Map queryParamMap) {
		return monitorMain.getJobNotifySendListCount(queryParamMap);
	}
	public List<JobNotifyReceiver> getJobNotifyReceiverList(String jobid, String event) {
		return monitorMain.getJobNotifyReceiverList(jobid, event);
	}
	public String getSystemMonitorText() {
		return systemMonitor.getCurrentSystemText();
	}

	// ============= JobGroupAttrDef =============
	
	public boolean addJobGroupAttrDef(JobGroupAttrDef jobGroupAttrDef, AdminAuth auth) {
		return monitorMain.addJobGroupAttrDef(jobGroupAttrDef, auth);
	}
	
	public boolean removeJobGroupAttrDef(String id, AdminAuth auth) { 
		return monitorMain.removeJobGroupAttrDef( id,  auth);
	}
	
	public boolean modifyJobGroupAttrDef(JobGroupAttrDef jobGroupAttrDef, AdminAuth auth) { 
		return monitorMain.modifyJobGroupAttrDef( jobGroupAttrDef,  auth);
	}
	
	public JobGroupAttrDef getJobGroupAttrDef(String jobGroupAttrDefId) { 
		return monitorMain.getJobGroupAttrDef( jobGroupAttrDefId);
	}
	
	public List<JobGroupAttrDef> getAllJobGroupAttrDefs() { 
		return monitorMain.getAllJobGroupAttrDefs();
	}
	
	public List<JobGroupAttrDef> getJobGroupAttrDefsByQuery(String queryCondition, String orderBy) { 
		return monitorMain.getJobGroupAttrDefsByQuery( queryCondition,  orderBy);
	}
	
	// ============= JobGroup =============
	public boolean addJobGroup(JobGroup jobGroup, AdminAuth auth) {
		return monitorMain.addJobGroup( jobGroup,  auth);
	}
	
	public boolean removeJobGroup(String id, AdminAuth auth) {
		return monitorMain.removeJobGroup( id,  auth);
	}
	
	public boolean removeJobGroupRecursively(String id, AdminAuth auth) {
		return monitorMain.removeJobGroupRecursively( id,  auth);
	}
	
	public boolean modifyJobGroup(JobGroup jobGroup, AdminAuth auth) {
		return monitorMain.modifyJobGroup( jobGroup,  auth);
	}
	
	public JobGroup getJobGroup(String jobGroupId) {
		return monitorMain.getJobGroup( jobGroupId);
	}
	
	public List<JobGroup> getAllJobGroups() {
		return monitorMain.getAllJobGroups();
	}
	
	public List<JobGroup> getJobGroupsByQuery(String queryCondition, String orderBy) {
		return monitorMain.getJobGroupsByQuery( queryCondition,  orderBy);
	}
	
	public List<JobGroup> getJobGroupsByDynamicQuery(Map queryParamMap) { 
		return monitorMain.getJobGroupsByDynamicQuery(queryParamMap);
	}
	
	public List<JobGroup> analyzeToJobGroupsTreeList(List<JobGroup> flatJobGroupList) {
		return monitorMain.analyzeToJobGroupsTreeList(flatJobGroupList);
	}
	
	public List<JobGroup> getJobGroupsTreeListByQuery(String queryCondition, String orderBy) {
		return monitorMain.getJobGroupsTreeListByQuery( queryCondition,  orderBy);
	}

	// ============= User =============
	
	public boolean addUser(User user, AdminAuth auth) {
		return monitorMain.addUser(user, auth);
	}
	
	public boolean removeUser(String id, AdminAuth auth) {
		return monitorMain.removeUser(id, auth);
	}
	
	public boolean modifyUser(User user, AdminAuth auth) {
		return monitorMain.modifyUser(user, auth);
	}

	public boolean modifyUserPassword(User user, AdminAuth auth) {
		return monitorMain.modifyUserPassword(user, auth);
	}
	
	public User getUser(String userId) {
		return monitorMain.getUser(userId);
	}
	
	public List<User> getAllUsers() {
		return monitorMain.getAllUsers();
	}
	
	public List<User> getUsersByQuery(String queryCondition, String orderBy) {
		return monitorMain.getUsersByQuery(queryCondition, orderBy);
	}

	public boolean isAllowedForOperation(String jobGroupId, String jobId, User user) {
		return monitorMain.isAllowedForOperation(jobGroupId, jobId, user);
	}

	public User login(String id, String password, String ip) {
		return monitorMain.login(id, password, ip);
	}
}