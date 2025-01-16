package nexcore.scheduler.entity;

import java.util.List;
import java.util.Map;
import java.util.Properties;



/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Monitor 서비스 </li>
 * <li>작성일 : 2011. 1. 14.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public interface IMonitorService {

	/* =========== 뷰필터 제어 ================*/
	
	public boolean addViewFilter(ViewFilter vf, AdminAuth auth);
	
	public ViewFilter getViewFilter(int vfid);
	
	public ViewFilter getViewFilterDeep(int vfid);
	
	public List<ViewFilter> getViewFiltersByQuery(String query, String orderBy);

	public List<JobDefinition> getJobDefinitionsByViewFilter(int vfid);
	
	public boolean removeViewFilter(int vfid, AdminAuth auth);
	
	public boolean modifyViewFilter(ViewFilter vf, AdminAuth auth);

	public boolean modifyViewFilterNoJobList(ViewFilter vf, AdminAuth auth);
	
	public boolean modifyViewFilterAddJobList(int vfid, List<String> jobIdList, AdminAuth auth);
	
	public boolean modifyViewFilterDelJobList(int vfid, List<String> jobIdList, AdminAuth auth);
	

	public List<AgentInfo> getAllAgentInfos();

	public AgentInfo getAgentInfo(String id);

	public boolean addAgentInfo(AgentInfo agentInfo, AdminAuth auth);

	public boolean removeAgentInfo(String agentId, AdminAuth auth);
	
	public boolean modifyAgentInfo(AgentInfo agentInfo, AdminAuth auth);

	public AgentMonitoringSummary getAgentMonitoringSummary(String agentId);
	
	public boolean closeOrOpenAgent(String agentId, boolean close, AdminAuth auth);

	/**
	 * 해당 agent 에서 실행중, 일시정지 상태인 Job Execution 정보를 얻음.
	 * @return
	 */
	public List<JobExecution> getAgentRunningJobExecutions(String agentId);

	/**
	 * agent의 모든 스레드 StackTrace 조회
	 * @param agentId
	 * @return
	 */
	public Map getAgentAllThreadStackTrace(String agentId);
	
	/**
	 * 에이전트의 JVM SystemProperties 조회
	 * @param agentId
	 * @return
	 */
	public Properties getAgentSystemProperties(String agentId);

	/**
	 * 에이전트의 JVM System Env 조회
	 * @param agentId
	 * @return
	 */
	public Map getAgentSystemEnv(String agentId);

	/**
	 * 에이전트의 설정 파일 목록을 조회
	 * @param agentId
	 * @return
	 */
	public List<String> getAgentConfigFiles(String agentId);
	
	/**
	 * agent 리스트 들의 상태를 체크해서 리턴함.
	 * @return Map&lt;String:(agentId), String:(checkResult)&gt;. checkResult="OK"이면 정상 
	 */
	public Map getAgentCheckList();

	/**
	 * 에이전트의 상태 체크. AgentMonitor 개 2초마다 폴링하여 캐쉬한 정보를 리턴함
	 * @param agentId
	 * @return "OK" if normal, other message if abnormal
	 */
	public String getAgentCheck(String agentId);
	
	/**
	 * 에이전트의 상태 체크. 캐쉬정보를 이용하지 않고 즉시 체크한다.
	 * @param agentId
	 * @return "OK" if normal, other message if abnormal
	 */
	public String getAgentCheckNoCache(String agentId);

	public long[] getJobProgressNoCache(String jobExecutionId, String agentId);
	
	public long[] getJobProgress(String jobExecutionId);
	
	// =================================== JobNotify =========================================
	
	public boolean addJobNotify(JobNotify jobNotify, AdminAuth auth);
	public boolean removeJobNotify(int id, AdminAuth auth);
	public boolean modifyJobNotify(JobNotify jobNotify, AdminAuth auth);
	public JobNotify getJobNotify(int id);	
	public List<JobNotify> getAllJobNotifies();
	
	public boolean addJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver, AdminAuth auth);
	public boolean removeJobNotifyReceiver(int id, AdminAuth auth);
	public boolean modifyJobNotifyReceiver(JobNotifyReceiver jobNotifyReceiver, AdminAuth auth);
	public JobNotifyReceiver getJobNotifyReceiver(int receiverId);
	public List<JobNotifyReceiver> getAllJobNotifyReceivers();
	public Map<Integer, JobNotifyReceiver> getAllJobNotifyReceiversMap();
	public List<JobNotifyReceiver> getJobNotifyReceiversByNotifyId(int notifyId);
	public List<JobNotifySendInfo> getJobNotifySendList(Map queryParamMap);
	public int getJobNotifySendListCount(Map queryParamMap);
	public List<JobNotifyReceiver> getJobNotifyReceiverList(String jobid, String event);

	public String getSystemMonitorText();

	// =================================== JobGroupAttrDef ==============================
	
	public boolean addJobGroupAttrDef(JobGroupAttrDef jobGroupAttrDef, AdminAuth auth);
	public boolean removeJobGroupAttrDef(String id, AdminAuth auth);
	public boolean modifyJobGroupAttrDef(JobGroupAttrDef jobGroupAttrDef, AdminAuth auth); 
	public JobGroupAttrDef getJobGroupAttrDef(String jobGroupAttrDefId);
	public List<JobGroupAttrDef> getAllJobGroupAttrDefs();
	public List<JobGroupAttrDef> getJobGroupAttrDefsByQuery(String queryCondition, String orderBy); 
	
	// =================================== JobGroup ==============================

	public boolean addJobGroup(JobGroup jobGroup, AdminAuth auth);
	public boolean removeJobGroup(String id, AdminAuth auth);
	public boolean removeJobGroupRecursively(String id, AdminAuth auth);
	public boolean modifyJobGroup(JobGroup jobGroup, AdminAuth auth);
	public JobGroup getJobGroup(String jobGroupId);
	public List<JobGroup> getAllJobGroups();
	public List<JobGroup> getJobGroupsByQuery(String queryCondition, String orderBy);
	public List<JobGroup> getJobGroupsByDynamicQuery(Map queryParamMap); 
	public List<JobGroup> analyzeToJobGroupsTreeList(List<JobGroup> flatJobGroupList);
	public List<JobGroup> getJobGroupsTreeListByQuery(String queryCondition, String orderBy);
	
	// =================================== User =========================================
	
	public boolean addUser(User user, AdminAuth auth);
	public boolean removeUser(String id, AdminAuth auth);
	public boolean modifyUser(User user, AdminAuth auth);
	public boolean modifyUserPassword(User user, AdminAuth auth);
	public User getUser(String userId);
	public List<User> getAllUsers();
	public List<User> getUsersByQuery(String queryCondition, String orderBy);
	public boolean isAllowedForOperation(String jobGroupId, String jobId, User user) ;
	public User login(String id, String password, String ip);


}