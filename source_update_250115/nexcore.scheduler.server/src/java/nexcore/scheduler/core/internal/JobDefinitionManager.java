package nexcore.scheduler.core.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ibatis.sqlmap.client.SqlMapClient;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.entity.PreJobCondition;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Definition 정보 DB access </li>
 * <li>작성일 : 2010. 5. 13.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobDefinitionManager {
	private SqlMapClient   sqlMapClient;
	
	public void init() {
	}
	
	public void destroy() {
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	/**
	 * NBS_JOB_DEF 테이블에서 하나 읽음. PARAMETER, PRE_JOB CONDITION 은 읽지 않음.
	 * Daily activater 에서 scheduler 정보 조회시 사용됨.
	 * @param jobId
	 * @return
	 */
	public JobDefinition getJobDefinition(String jobId) throws SQLException {
		return (JobDefinition)sqlMapClient.queryForObject("nbs.scheduler.selectJobDefinitionByJobId", jobId);
	}
	
	/**
	 * NBS_JOB_DEF 테이블에서 하나 읽음. PARAMETER, PRE_JOB CONDITION 도 읽음
	 * @param jobId
	 * @return
	 */
	public JobDefinition getJobDefinitionDeep(String jobId) throws SQLException {
		JobDefinition jobdef = getJobDefinition(jobId);
		if (jobdef == null) {
			throw new SchedulerException("main.jobdef.notfound", jobId); // 존재하지 않는 Job 입니다.
		}
		loadPreJobConditions(jobdef);
		loadPostJobTriggers(jobdef);
		loadParameters(jobdef);
		return jobdef;
	}
	
	
	/**
	 * 선행 조건 정보를 DB 테이블에서 로드함
	 * @param jobdef
	 */
	public void loadPreJobConditions(JobDefinition jobdef) throws SQLException {
		List<Map> preJobCondList = sqlMapClient.queryForList("nbs.scheduler.selectJobDefPreJobConditionsByJobId", jobdef.getJobId());
		
		List<PreJobCondition> list = new ArrayList();
		for (Map<String, String> preJobCond : preJobCondList) {
			list.add(new PreJobCondition(preJobCond));
		}

		jobdef.setPreJobConditions(list);
	}

	/**
	 * Post Trigger 정보를 DB 테이블에서 로드함
	 * @param jobdef
	 */
	public void loadPostJobTriggers(JobDefinition jobdef) throws SQLException {
		List<Map> postJobTriggerList = sqlMapClient.queryForList("nbs.scheduler.selectJobDefPostJobTriggersByJobId", jobdef.getJobId());
		
		List<PostJobTrigger> list = new ArrayList();
		for (Map<String, String> postJobTrigger : postJobTriggerList) {
			list.add(new PostJobTrigger(postJobTrigger)); 
		}

		jobdef.setTriggerList(list);
	}

	/**
	 * 선행 조건 정보를 DB 테이블에서 로드함
	 * @param jobdef
	 */
	public void loadParameters(JobDefinition jobdef) throws SQLException {
		List<Map> parameters = sqlMapClient.queryForList("nbs.scheduler.selectJobDefParamsByJobId", jobdef.getJobId());
		
		Map inParam = new LinkedHashMap<String, String>();
		for (Map param : parameters) {
			inParam.put(param.get("PARAM_NAME"), param.get("PARAM_VALUE"));
		}
		jobdef.setInParameters(inParam);
	}
	
	/**
	 * admin 에서 JobDefiniton 정보를 검색할때 사용함. PARAMETER, PRE_JOB CONDITION 은 읽지 않음
	 * @param query
	 * @return
	 */
	public List<JobDefinition> getJobDefinitionsByQuery(String query) throws SQLException {
		List<JobDefinition> jobdef = (List<JobDefinition>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefinitionsByQuery", query);
		return jobdef;
	}
	
	/**
	 * Dynamic 쿼리 방식으로 JobDefinition들 조회
	 * admin 에서 JobDefiniton 정보를 검색할때 사용함. PARAMETER, PRE_JOB CONDITION 은 읽지 않음
	 * @param queryParamMap 검색 조건을 담고 있는 Map.
	 * {viewFilterId, jobIdLike, jobGroupIdLike, jobDescLike, jobType, preJobIdLike, triggerJobIdLike, agentId, ownerLike, authorizedJobGroupIdViewList} 
	 * @return
	 */
	public List<JobDefinition> getJobDefinitionsByDynamicQuery(Map queryParamMap) throws SQLException {
		List<JobDefinition> list = (List<JobDefinition>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefinitionsByDynamicQuery", queryParamMap);
		return list;
	}
	
	
	/**
	 * JobDefinition들 개수 조회
	 * 
	 * @param  
	 * @return Job Definition Count
	 */
	public int getJobDefinitionsCount() throws SQLException {
		return (Integer)sqlMapClient.queryForObject("nbs.scheduler.selectJobDefinitionsCount");
	}


	/**
	 * JOB_DEF, JOB_DEF_PARAM, JOB_DEF_PREJOB 테이블 조회 결과를 List<JobDefinition> 으로 조립.
	 * @param jobdefList
	 * @param preJobsList
	 * @param triggersList
	 * @param paramsList
	 * @return
	 */
	private List<JobDefinition> assembleJobDefinition(List<JobDefinition> jobdefList, List<Map> preJobsList, List<Map> triggersList, List<Map> paramsList) {
		// JobDefinition map 구성
		Map<String, JobDefinition> jobDefMap = new LinkedHashMap<String, JobDefinition>();
		for (JobDefinition jobdef : jobdefList) {
			jobDefMap.put(jobdef.getJobId(), jobdef);
		}
		
		// Prejob 구성 
		for (Map<String, String> preJob : preJobsList) {
			JobDefinition jobdef = jobDefMap.get(preJob.get("JOB_ID"));
			if (jobdef != null) {
				jobdef.getPreJobConditions().add(new PreJobCondition(preJob));
			}
		}
		
		// Trigger 구성 
		for (Map<String, String> trigger : triggersList) {
			JobDefinition jobdef = jobDefMap.get(trigger.get("JOB_ID"));
			if (jobdef != null) {
				jobdef.getTriggerList().add(new PostJobTrigger(trigger));
			}
		}

		// 파라미터 구성
		for (Map<String, String> param : paramsList) {
			JobDefinition jobdef = jobDefMap.get(param.get("JOB_ID"));
			if (jobdef != null) {
				jobdef.getInParameters().put(param.get("PARAM_NAME"), param.get("PARAM_VALUE"));
			}
		}
		
		return new ArrayList<JobDefinition>(jobDefMap.values());
	}

	
	/**
	 * admin 에서 JobDefiniton 정보를 검색할때 사용함. PARAMETER, PRE_JOB CONDITION 도 모두 읽어 리턴함.
	 * 
	 * @param query
	 * @return
	 */
	public List<JobDefinition> getJobDefinitionsDeepByQuery(String query) throws SQLException {
		List<JobDefinition> jobdefList = (List<JobDefinition>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefinitionsByQuery", query);
		List<Map> preJobsList = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefPreJobConditionsByQuery", query);
		List<Map> triggersList= (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefPostJobTriggersByQuery", query);
		List<Map> paramsList  = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefParamsByQuery", query);
		
		return assembleJobDefinition(jobdefList, preJobsList, triggersList, paramsList);
	}

	/**
	 * Dynamic 쿼리 방식으로 JobDefinition들 조회
	 * admin 에서 JobDefiniton 정보를 검색할때 사용함. PARAMETER, PRE_JOB CONDITION 은 읽지 않음
	 * @param queryParamMap 검색 조건을 담고 있는 Map.
	 * {viewFilterId, jobIdLike, jobGroupIdLike, jobDescLike, jobType, preJobIdLike, triggerJobIdLike, agentId, ownerLike, authorizedJobGroupIdViewList} 
	 * @return
	 */
	public List<JobDefinition> getJobDefinitionsDeepByDynamicQuery(Map queryParamMap) throws SQLException {
		List<JobDefinition> jobdefList = (List<JobDefinition>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefinitionsByDynamicQuery", queryParamMap);
		List<Map> preJobsList = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefPreJobConditionsByDynamicQuery", queryParamMap);
		List<Map> triggersList= (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefPostJobTriggersByDynamicQuery", queryParamMap);
		List<Map> paramsList  = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefParamsByDynamicQuery", queryParamMap);
		
		return assembleJobDefinition(jobdefList, preJobsList, triggersList, paramsList);
	}
	
	/**
	 * admin 에서 JobDefiniton 정보를 검색할때 사용함. (no-deep)
	 * 
	 * @param jobIdList
	 * @return
	 */
	public List<JobDefinition> getJobDefinitionsByJobIdList(List<String> jobIdList) throws SQLException {
		if (jobIdList == null || jobIdList.size() == 0) {
			return new ArrayList<JobDefinition>();
		}else {
			Map in = new HashMap();
			in.put("jobIdList", jobIdList);
			List<JobDefinition> jobdefList = (List<JobDefinition>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefinitionsByJobIdList", in);
			return jobdefList;
		}
	}

	/**
	 * admin 에서 JobDefiniton 정보를 검색할때 사용함. (deep)
	 * 
	 * @param jobIdList
	 * @return
	 */
	// 쿼리 성능을 위해 PreJob, Parameter 를 한번에 다 읽은 후에 여기서 조립한다. 
	public List<JobDefinition> getJobDefinitionsDeepByJobIdList(List<String> jobIdList) throws SQLException {
		if (jobIdList == null || jobIdList.size() == 0) {
			return new ArrayList<JobDefinition>();
		}else {
			Map in = new HashMap();
			in.put("jobIdList", jobIdList);
			List<JobDefinition> jobdefList = (List<JobDefinition>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefinitionsByJobIdList", in);
			List<Map> preJobsList  = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefPreJobConditionsByJobIdList", in);
			List<Map> triggersList = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefPostJobTriggersByJobIdList", in);
			List<Map> paramsList   = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefParamsByJobIdList", in);
			
			return assembleJobDefinition(jobdefList, preJobsList, triggersList, paramsList);
		}
	}

	/**
	 * JobDefinition 정보중 원하는 컬럼만 조회한다.
	 * queryParamMap 에 "columnList" 키값으로 컬럼 리스트 지정함
	 * NO-DEEP 쿼리함.
	 * @param queryParamMap 
	 * @return
	 */
	public List<Map> getJobDefinitionsFreeColumnByDynamicQuery(Map queryParamMap) throws SQLException {
		return sqlMapClient.queryForList("nbs.scheduler.selectJobDefinitionsFreeColumnByDynamicQuery", queryParamMap);
	}
	
	/**
	 * NBS_JOB_DEF 테이블 insert.
	 * @param jobdef
	 * @return
	 * @throws SQLException
	 */
	public int insertJobDefinition(JobDefinition jobdef) throws SQLException {
		// NBS_JOB_DEF 테이블 insert 
		jobdef.setLastModifyTime(DateUtil.getCurrentTimestampString());
		int cnt = sqlMapClient.update("nbs.scheduler.insertJobDefinition", jobdef);
		
		if (cnt > 0) {
			// NBS_JOB_DEF_PREJOB 테이블 insert
			insertPreJobCondition(jobdef);

			// NBS_JOB_DEF_TRIGGER 테이블 insert
			insertPostJobTriggers(jobdef);
			
			// NBS_JOB_DEF_PARAM 테이블 insert
			insertParameter(jobdef);
		}
		return cnt;
	}
	
	public void insertPreJobCondition(JobDefinition jobdef) throws SQLException {
		int i=0;
		sqlMapClient.startBatch();
		for (PreJobCondition cond : jobdef.getPreJobConditions()) {
			Map map = new HashMap();
			map.put("jobId",         jobdef.getJobId());
			map.put("seq",           (++i));
			map.put("preJobId",      cond.getPreJobId());
			map.put("okFail",        cond.getOkFail());
			map.put("andOr",         cond.getAndOr());
			sqlMapClient.update("nbs.scheduler.insertJobDefPreJobList", map);
		}
		sqlMapClient.executeBatch();
	}

	public void insertPostJobTriggers(JobDefinition jobdef) throws SQLException {
		int i=0;
		sqlMapClient.startBatch();
		for (PostJobTrigger trigger : jobdef.getTriggerList()) {
			Map map = new HashMap();
			map.put("jobId",         jobdef.getJobId());
			map.put("seq",           (++i));
			map.put("when",          trigger.getWhen());
			map.put("checkValue1",   trigger.getCheckValue1());
			map.put("checkValue2",   trigger.getCheckValue2());
			map.put("checkValue3",   trigger.getCheckValue3());
			map.put("triggerJobId",  trigger.getTriggerJobId());
			map.put("instanceCount", trigger.getJobInstanceCount());
			sqlMapClient.update("nbs.scheduler.insertJobDefPostJobTrigger", map);
		}
		sqlMapClient.executeBatch();
	}
	
	public void insertParameter(JobDefinition jobdef) throws SQLException {
		int i=0;
		sqlMapClient.startBatch();
		for (Map.Entry<String, String> param : jobdef.getInParameters().entrySet()) {
			Map map = new HashMap();
			map.put("jobId",         jobdef.getJobId());
			map.put("seq",           (++i));
			map.put("paramName",     param.getKey());
			map.put("paramValue",    param.getValue());
			sqlMapClient.update("nbs.scheduler.insertJobDefParam", map);
		}
		sqlMapClient.executeBatch();
	}

	/**
	 * NBS_JOB_DEF, NBS_JOB_DEF_PREJOB, NBS_JOB_DEF_PARAM 테이블만 UDPATE 함
	 * @param jobdef
	 * @throws SQLException
	 */
	public int updateJobDefinition(JobDefinition jobdef) throws SQLException {
		// NBS_JOB_DEF 테이블 update 
		JobDefinition beforeJobdef = getJobDefinitionDeep(jobdef.getJobId()); // modify 하기 이전값.

		jobdef.setLastModifyTime(DateUtil.getCurrentTimestampString());
		int updateCnt = sqlMapClient.update("nbs.scheduler.updateJobDefinition", jobdef);
		
		if (updateCnt > 0) {
			// NBS_JOB_DEF 테이블 정상 UPDATE
			
			if (beforeJobdef.getPreJobConditions() != null && !beforeJobdef.getPreJobConditions().equals(jobdef.getPreJobConditions())) {
				// 선행조건 변경함. delete & insert
				deletePreJobCondition(jobdef.getJobId());
				insertPreJobCondition(jobdef);
			}
			
			if (beforeJobdef.getTriggerList() != null && !beforeJobdef.getTriggerList().equals(jobdef.getTriggerList())) {
				// Trigger 변경함. delete & insert
				deletePostJobTriggers(jobdef.getJobId());
				insertPostJobTriggers(jobdef);
			}
			
			/* 2013.11.13. 파라미터의 순서의 변경도 변경으로 봐야하므로 String 으로 변환하여 비교한다. */
			if (beforeJobdef.getInParameters() != null && !Util.equals(beforeJobdef.getInParameters().toString(), jobdef.getInParameters().toString())) {
				// 파라미터 변경함. delete & insert
				deleteParameter(jobdef.getJobId());
				insertParameter(jobdef);
			}
		}

		return updateCnt;
	}

	/**
	 * 로그 레벨 변경
	 * @param jobId
	 * @param logLevel
	 * @return
	 * @throws SQLException
	 */
	public int updateJobDefinitionLogLevel(String jobId, String logLevel) throws SQLException {
		Map param = new HashMap();
		param.put("logLevel",       logLevel==null ? null : logLevel.toUpperCase());
		param.put("jobId",          jobId);
		return sqlMapClient.update("nbs.scheduler.updateJobDefinitionLogLevel", param);
	}
	
	/**
	 * NBS_JOB_DEF, NBS_JOB_DEF_PREJOB, NBS_JOB_DEF_PARAM 테이블 DELETE 함
	 * @param jobDefId
	 * @throws SQLException
	 */
	public int deleteJobDefinition(String jobDefId) throws SQLException {
		// NBS_JOB_DEF 테이블 delete
		int cnt = sqlMapClient.delete("nbs.scheduler.deleteJobDefinition", jobDefId);
		if (cnt > 0) {
			deletePreJobCondition(jobDefId);
			deletePostJobTriggers(jobDefId);
			deleteParameter(jobDefId);
		}
		return cnt;
	}

	/**
	 * NBS_JOB_DEF_PREJOB 테이블에서 Def 하나의 정보를 삭제함
	 * @param jobDefId
	 * @throws SQLException
	 */
	public int deletePreJobCondition(String jobDefId) throws SQLException {
		return sqlMapClient.delete("nbs.scheduler.deleteJobDefPreJobList", jobDefId);
	}

	/**
	 * NBS_JOB_DEF_TRIGGER 테이블에서 Def 하나의 정보를 삭제함
	 * @param jobDefId
	 * @throws SQLException
	 */
	public int deletePostJobTriggers(String jobDefId) throws SQLException {
		return sqlMapClient.delete("nbs.scheduler.deleteJobDefPostJobTrigger", jobDefId);
	}
	
	/**
	 * NBS_JOB_DEF_PREJOB 테이블에서 Def 하나의 정보를 삭제함
	 * @param jobDefId
	 * @throws SQLException
	 */
	public int deleteParameter(String jobDefId) throws SQLException {
		return sqlMapClient.delete("nbs.scheduler.deleteJobDefParam", jobDefId);
	}

	/**
	 * 해당 Job 의 그룹 ID 를 리턴함.
	 * @param jobDefId
	 * @return
	 * @throws SQLException
	 */
	public String getJobGroupId(String jobDefId) throws SQLException {
		return (String)sqlMapClient.queryForObject("nbs.scheduler.getJobDefinitionGroupId", jobDefId);
	}
}
