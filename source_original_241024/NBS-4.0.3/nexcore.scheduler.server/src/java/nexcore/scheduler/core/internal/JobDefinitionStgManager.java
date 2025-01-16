package nexcore.scheduler.core.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nexcore.scheduler.entity.JobDefinition;
import nexcore.scheduler.entity.JobDefinitionStg;
import nexcore.scheduler.entity.PostJobTrigger;
import nexcore.scheduler.entity.PreJobCondition;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.event.RowHandler;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Definition Stg 정보 DB access </li>
 * <li>작성일 : 2010. 10. 14.</li>
 * <li>작성일 : 2012. 12. 07. 테이블base 로 변경</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class JobDefinitionStgManager {
	private SqlMapClient            sqlMapClient;

	private TableBaseIdGenerator    tableBaseIdGenerator;
	
	public void init() {
		tableBaseIdGenerator = new TableBaseIdGenerator("REQ", sqlMapClient);
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
	 * JOB_DEF, JOB_DEF_PARAM, JOB_DEF_PREJOB 테이블 조회 결과를 List<JobDefinition> 으로 조립.
	 * @param jobdefList
	 * @param preJobsList
	 * @param triggersList
	 * @param paramsList
	 * @return
	 */
	private List<JobDefinitionStg> assembleJobDefinition(List<JobDefinitionStg> jobdefList, List<Map> preJobsList, List<Map> triggersList, List<Map> paramsList) {
		// JobDefinition map 구성
		Map<String, JobDefinitionStg> jobDefMap = new LinkedHashMap<String, JobDefinitionStg>();
		for (JobDefinitionStg jobdef : jobdefList) {
			jobDefMap.put(jobdef.getJobId(), jobdef);
		}
		
		// Prejob 구성 
		for (Map<String, String> preJobs : preJobsList) {
			JobDefinitionStg jobdef = jobDefMap.get(preJobs.get("JOB_ID"));
			if (jobdef != null) {
				jobdef.getPreJobConditions().add(new PreJobCondition(preJobs));
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
		for (Map<String, String> params : paramsList) {
			JobDefinitionStg jobdef = jobDefMap.get(params.get("JOB_ID"));
			if (jobdef != null) {
				jobdef.getInParameters().put(params.get("PARAM_NAME"), params.get("PARAM_VALUE"));
			}
		}
		
		return new ArrayList<JobDefinitionStg>(jobDefMap.values());
	}

	public JobDefinitionStg getJobDefinitionStg(String reqNo, String jobId) throws SQLException {
		Map sqlIn = new HashMap();
		sqlIn.put("reqNo", reqNo);
		sqlIn.put("jobId", jobId);
		return (JobDefinitionStg)sqlMapClient.queryForObject("nbs.scheduler.selectJobDefinitionStgById", sqlIn);
	}
	
	public JobDefinitionStg getJobDefinitionStgDeep(String reqNo, String jobId) throws SQLException {
		JobDefinitionStg jobdef = getJobDefinitionStg(reqNo, jobId);
		if (jobdef == null) {
			throw new SchedulerException("main.jobdefstg.notfound", reqNo, jobId);
		}
		loadPreJobConditionStgs(jobdef);
		loadPostJobTriggers(jobdef);
		loadParameters(jobdef);
		return jobdef;
	}
	
	public void loadPreJobConditionStgs(JobDefinitionStg jobdef) throws SQLException {
		Map sqlIn = new HashMap();
		sqlIn.put("reqNo", jobdef.getReqNo());
		sqlIn.put("jobId", jobdef.getJobId());
		List<Map> preJobCondList = sqlMapClient.queryForList("nbs.scheduler.selectJobDefPreJobConditionStgById", sqlIn);
		
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
	public void loadPostJobTriggers(JobDefinitionStg jobdef) throws SQLException {
		Map sqlIn = new HashMap();
		sqlIn.put("reqNo", jobdef.getReqNo());
		sqlIn.put("jobId", jobdef.getJobId());
		List<Map> postJobTriggerList = sqlMapClient.queryForList("nbs.scheduler.selectJobDefPostJobTriggerStgById", sqlIn);

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
	public void loadParameters(JobDefinitionStg jobdef) throws SQLException {
		Map sqlIn = new HashMap();
		sqlIn.put("reqNo", jobdef.getReqNo());
		sqlIn.put("jobId", jobdef.getJobId());
		List<Map> parameters = sqlMapClient.queryForList("nbs.scheduler.selectJobDefParamStgById", sqlIn);
		
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
	public List<JobDefinitionStg> getJobDefinitionStgsByQuery(String query) throws SQLException {
		List<JobDefinitionStg> jobdefList = (List<JobDefinitionStg>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefinitionStgsByQuery", query);
		return jobdefList;
	}
	
	/**
	 * admin 에서 JobDefiniton 정보를 검색할때 사용함. deep
	 * @param query
	 * @return
	 */
	public List<JobDefinitionStg> getJobDefinitionStgsDeepByQuery(String query) throws SQLException {
		List<JobDefinitionStg> jobdefList = (List<JobDefinitionStg>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefinitionStgsByQuery", query);
		List<Map> preJobsList             = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefPreJobConditionStgsByQuery", query);
		List<Map> triggersList            = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefPostJobTriggerStgsByQuery", query);
		List<Map> paramsList              = (List<Map>)sqlMapClient.queryForList("nbs.scheduler.selectJobDefParamStgsByQuery", query);
		return assembleJobDefinition(jobdefList, preJobsList, triggersList, paramsList);
	}
	
	/**
	 * admin 에서 요청목록 조회시 사용함.
	 * 대량 조회를 대비하여 rowHandler 를 사용함. 
	 * 나중에 페이징 기능이 구현되면 이 기능은 필요없어짐.
	 * @param query
	 * @param rowHandler 대량 조회를 대비하여 rowHandler 를 이용함.
	 * @return
	 */
	public void getJobDefinitionStgsByQueryWithRH(String query, Object rowHandler) throws SQLException {
		sqlMapClient.queryWithRowHandler("nbs.scheduler.selectJobDefinitionStgsByQuery", query, (RowHandler)rowHandler);
	}
	
	/**
	 * NBS_JOB_DEF_STG 테이블 insert.
	 * @param jobdef
	 * @return
	 * @throws SQLException
	 */
	public int insertJobDefinitionStg(JobDefinitionStg jobdef) throws SQLException {
		// NBS_JOB_DEF 테이블 insert 
		jobdef.setLastModifyTime(DateUtil.getCurrentTimestampString());
		int cnt = sqlMapClient.update("nbs.scheduler.insertJobDefinitionStg", jobdef);
		
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
	
	public void insertPreJobCondition(JobDefinitionStg jobdef) throws SQLException {
		int i=0;
		
		sqlMapClient.startBatch();
		for (PreJobCondition cond : jobdef.getPreJobConditions()) {
			Map map = new HashMap();
			map.put("reqNo",         jobdef.getReqNo());
			map.put("jobId",         jobdef.getJobId());
			map.put("seq",           (++i));
			map.put("preJobId",      cond.getPreJobId());
			map.put("okFail",        cond.getOkFail());
			map.put("andOr",         cond.getAndOr());
			sqlMapClient.update("nbs.scheduler.insertJobDefPreJobStgList", map);
		}
		sqlMapClient.executeBatch();
	}

	public void insertPostJobTriggers(JobDefinitionStg jobdef) throws SQLException {
		int i=0;
		sqlMapClient.startBatch();
		for (PostJobTrigger trigger : jobdef.getTriggerList()) {
			Map map = new HashMap();
			map.put("reqNo",         jobdef.getReqNo());
			map.put("jobId",         jobdef.getJobId());
			map.put("seq",           (++i));
			map.put("when",          trigger.getWhen());
			map.put("checkValue1",   trigger.getCheckValue1());
			map.put("checkValue2",   trigger.getCheckValue2());
			map.put("checkValue3",   trigger.getCheckValue3());
			map.put("triggerJobId",  trigger.getTriggerJobId());
			map.put("instanceCount", trigger.getJobInstanceCount());
			sqlMapClient.update("nbs.scheduler.insertJobDefPostJobTriggerStg", map);
		}
		sqlMapClient.executeBatch();
	}
	
	public void insertParameter(JobDefinitionStg jobdef) throws SQLException {
		int i=0;
		sqlMapClient.startBatch();
		for (Map.Entry<String, String> param : jobdef.getInParameters().entrySet()) {
			Map map = new HashMap();
			map.put("reqNo",         jobdef.getReqNo());
			map.put("jobId",         jobdef.getJobId());
			map.put("seq",           (++i));
			map.put("paramName",     param.getKey());
			map.put("paramValue",    param.getValue());
			sqlMapClient.update("nbs.scheduler.insertJobDefParamStg", map);
		}
		sqlMapClient.executeBatch();
	}
	
	public int updateJobDefinitionStgReqInfo(JobDefinitionStg jobdef) throws SQLException {
		jobdef.setLastModifyTime(DateUtil.getCurrentTimestampString());
		return sqlMapClient.update("nbs.scheduler.updateJobDefinitionStgReqInfo", jobdef);
		
	}
	
	/**
	 * NBS_JOB_DEF_STG, NBS_JOB_DEF_PREJOB_STG, NBS_JOB_DEF_TRIGGER_STG, NBS_JOB_DEF_PARAM_STG 테이블 DELETE 함
	 * 요청상태인것만 삭제 가능함.
	 * @param jobdef
	 * @throws SQLException
	 */
	public int deleteJobDefinitionStg(String reqNo) throws SQLException {
		// NBS_JOB_DEF 테이블 delete
		int cnt = sqlMapClient.delete("nbs.scheduler.deleteJobDefinitionStg", reqNo);
		if (cnt > 0) {
			deletePreJobCondition(reqNo);
			deletePostJobTriggers(reqNo);
			deleteParameter(reqNo);
		}
		return cnt;
	}

	/**
	 * NBS_JOB_DEF_PREJOB_STG 테이블 삭제
	 * @param reqNo
	 * @throws SQLException
	 */
	public int deletePreJobCondition(String reqNo) throws SQLException {
		return sqlMapClient.delete("nbs.scheduler.deleteJobDefPreJobStgList", reqNo);
	}

	/**
	 * NBS_JOB_DEF_TRIGGER_STG 테이블에서 Def 하나의 정보를 삭제함
	 * @param reqNo
	 * @throws SQLException
	 */
	public int deletePostJobTriggers(String reqNo) throws SQLException {
		return sqlMapClient.delete("nbs.scheduler.deleteJobDefPostJobTriggerStg", reqNo);
	}
	
	/**
	 * NBS_JOB_DEF_PREJOB_STG 테이블 삭제
	 * @param reqNo
	 * @throws SQLException
	 */
	public int deleteParameter(String reqNo) throws SQLException {
		return sqlMapClient.delete("nbs.scheduler.deleteJobDefParamStg", reqNo);
	}

	/**
	 * 새로운 요청번호 하나를 딴다. YYYYMMDD+SEQ(8)
	 * @return
	 */
	public String newReqNo() {
		try {
			String date = Util.getYYYYMMDD(System.currentTimeMillis());
			int newSeq = tableBaseIdGenerator.getNextSeq(date);
			
			return date + String.format("%08d", newSeq);
		}catch(Exception e) {
			throw Util.toRuntimeException(e);
		}
	}
}
