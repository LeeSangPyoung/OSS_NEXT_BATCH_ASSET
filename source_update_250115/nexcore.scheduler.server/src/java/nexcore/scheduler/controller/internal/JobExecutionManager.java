package nexcore.scheduler.controller.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.SchedulerException;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;
import nexcore.scheduler.util.XmlUtil;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job Run 종료된 ReturnCode, ReturnValue 를 관리함. DB 에 테이블 형태로 관리함. </li>
 * <li>작성일 : 2010. 9. 2.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class JobExecutionManager {
	private SqlMapClient sqlMapClient;

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

	private int insertJobExecution(JobExecution je) throws SQLException {
		// NBS_JOB_EXE 테이블 insert. insert count 를 리턴하기 위해 update() 를 이용함.
		je.setLastModifyTime(DateUtil.getCurrentTimestampString());
		return sqlMapClient.update("nbs.controller.insertJobExecution", je);
	}
	
	private void insertJobExecutionParam(JobExecution je) throws SQLException  {
		// NBS_JOB_EXE_PARAM 테이블 insert
		Map map = new HashMap();
		map.put("jobExecutionId", je.getJobExecutionId());
		map.put("xml",            XmlUtil.toXml(je.getInParameters()));
		sqlMapClient.update("nbs.controller.insertJobExeParam", map);
	}
	
	private void insertJobExecutionReturnValue(JobExecution je) throws SQLException  {
		// NBS_JOB_EXE_RETVAL 테이블 insert
		if (je.getReturnValues().size() > 0) {
			Map map = new HashMap();
			map.put("jobExecutionId", je.getJobExecutionId());
			map.put("xml",            XmlUtil.toXml(je.getReturnValues()));
			sqlMapClient.update("nbs.controller.insertJobExeReturnValue", map);
		}
	}
	
	private JobExecution selectJobExecution(String jobExecutionId) throws SQLException  {
		JobExecution je = (JobExecution)sqlMapClient.queryForObject("nbs.controller.selectJobExecution", jobExecutionId);
		return je;
	}
	private Map<String, String> selectJobExecutionParam(String jobExecutionId) throws SQLException  {
		Map retval = (Map)sqlMapClient.queryForObject("nbs.controller.selectJobExeParams", jobExecutionId);
		String xml = retval == null ? null : (String)retval.get("DATA_XML");
		return xml == null ? new HashMap() : XmlUtil.toMap(xml); 
	}

	private Properties selectJobExecutionReturnValues(String jobExecutionId) throws SQLException  {
		Map retval = (Map)sqlMapClient.queryForObject("nbs.controller.selectJobExeReturnValues", jobExecutionId);
		String xml = retval == null ? null : (String)retval.get("DATA_XML");
		return xml == null ? new Properties() : XmlUtil.toMapToProperties(xml); 
	}

	/**
	 * JobExecution 생성시 최초 insert 함.
	 * @param je
	 */
	public int addJobExecution(JobExecution je) throws SQLException {
		int cnt = insertJobExecution(je);
		insertJobExecutionParam(je);
		insertJobExecutionReturnValue(je);  // 아마 이 로직은 여기서는 돌지 않을 것 같다. 그래도 구색맞추기.
		return cnt;
	}

	/**
	 * JobExecution 실행 종료된 상태 또는 suspend 된 상태에서 실행결과, 상태, 시각, return value 저장함.
	 * @param je
	 * @return 정상여부 리턴. 이 값이 false 리턴하면 RunResultProcessor가 안돌아감.
	 */
	public boolean updateJobExecutionForJobEnd(JobExecution je) throws SQLException {
		je.setLastModifyTime(DateUtil.getCurrentTimestampString());
		if (!Util.isBlank(je.getErrorMsg()) && je.getErrorMsg().length() >= 300) {
			je.setErrorMsg(Util.fitLength(je.getErrorMsg(), 990)); //  varchar(1000) 에 잘 들어가게 하기 위해 990 으로 자른다.
		}
		// 현재 상태가 ENDED 가 아닌지 체크해서 update함. 장애처리를 위해 여러번 callback될 수 있음.
		int updateCnt = sqlMapClient.update("nbs.controller.updateJobExecutionForEnd", je);
		if (updateCnt == 1) {
			insertJobExecutionReturnValue(je);
			return true; 
		}else {
			return false; // 이미 END 처리가 된 건은 update 건수가 0 이 된다. 
		}
	}

	/**
	 * JobExecution 의 상태만 변경함
	 * @param jobExecutionId
	 * @param newState
	 * @param oldState
	 * @return true if success
	 * @throws SQLException
	 */
	public boolean updateJobExecutionStateOnly(String jobExecutionId, int newState, int oldState) throws SQLException {
		Map map = new HashMap();
		map.put("jobExecutionId",   jobExecutionId);
		map.put("state",            newState);
		map.put("oldState",         oldState);
		map.put("lastModifyTime",   DateUtil.getCurrentTimestampString());

		int updateCnt = sqlMapClient.update("nbs.controller.updateJobExecutionStateOnly", map);
		if (updateCnt == 1) {
			return true; 
		}else {
			return false; // 이미 END 처리가 된 건은 update 건수가 0 이 된다. 
		}
	}

	public int getJobExecutionState(String jobExecutionId) throws SQLException {
		JobExecution je = selectJobExecution(jobExecutionId);
		if (je == null) {
			throw new SchedulerException("main.jobexe.notfound", jobExecutionId);
		}
		return je.getState();
	}

	public String getJobExecutionStateString(String jobExecutionId) throws SQLException {
		JobExecution je = selectJobExecution(jobExecutionId);
		if (je == null) {
			throw new SchedulerException("main.jobexe.notfound", jobExecutionId);
		}
		return je.getStateString();
	}

	/**
	 * JobExecution 의 AgentNode ID를 찾는다.
	 * 
	 * @param jobExecutionId
	 * @return agent ID if normal, or null if jobexecutionid is null or "-" 
	 * @throws SQLException
	 */
	public String getJobExecutionAgentNode(String jobExecutionId) throws SQLException {
		if (!Util.isBlank(jobExecutionId) && !"-".equals(jobExecutionId)) {
		    JobExecution je = selectJobExecution(jobExecutionId);
		    if (je != null) {
		        return je.getAgentNode();
		    }
		}
		return null;
	}
	
	public int getReturnCode(String jobExecutionId) throws SQLException {
		JobExecution je = selectJobExecution(jobExecutionId);
		if (je == null) {
			throw new SchedulerException("main.jobexe.notfound", jobExecutionId);
		}
		return je.getReturnCode();
	}

	public Properties getReturnValues(String jobExecutionId) throws SQLException {
		return selectJobExecutionReturnValues(jobExecutionId);
	}

	/**
	 * admin 에서 JobExecution 정보를 검색할때 사용함. PARAMETER, RETURN Value 는 읽지 않음
	 * @param query
	 * @return
	 */
	public List<JobExecution> getJobExecutionsByQuery(String query) throws SQLException  {
		List<JobExecution> jobexeList = (List<JobExecution>)sqlMapClient.queryForList("nbs.controller.selectJobExecutionsByQuery", query);
		return jobexeList;
	}
	
	/**
	 * admin 에서 JobExecution 정보를 검색할때 사용함. PARAMETER, RETURN Value 도 모두 읽어 리턴함.
	 * 
	 * @param query
	 * @return
	 */
	// 쿼리 성능을 위해 Parameter, Return value 를 한번에 다 읽은 후에 여기서 조립한다. 
	public List<JobExecution> getJobExecutionsDeepByQuery(String query) throws SQLException {
		List<JobExecution> jobexeList = (List<JobExecution>)sqlMapClient.queryForList("nbs.controller.selectJobExecutionsByQuery", query);
		List<Map> paramList        = (List<Map>)sqlMapClient.queryForList("nbs.controller.selectJobExeParamsByQuery", query);
		List<Map> returnValueList  = (List<Map>)sqlMapClient.queryForList("nbs.controller.selectJobExeReturnValuesByQuery", query);
		
		// JobExecution map 구성
		Map<String, JobExecution> jobExeMap = new LinkedHashMap<String, JobExecution>();
		for (JobExecution jobexe : jobexeList) {
			jobExeMap.put(jobexe.getJobExecutionId(), jobexe);
		}
		
		// 리턴값 구성 
		for (Map<String, String> row : returnValueList) {
			JobExecution jobexe = jobExeMap.get(row.get("JOB_EXECUTION_ID"));

			if (jobexe != null) {
				String xml = (String)row.get("DATA_XML");
				Map rvalues = xml == null ? new HashMap() : XmlUtil.toMap(xml);
				jobexe.getReturnValues().putAll(rvalues);
			}
		}
		
		// 파라미터 구성
		for (Map<String, String> paramMap : paramList) {
			JobExecution jobexe = jobExeMap.get(paramMap.get("JOB_EXECUTION_ID"));
			if (jobexe != null) {
				String xml = (String)paramMap.get("DATA_XML");
				Map params = xml == null ? new HashMap() : XmlUtil.toMap(xml);
				jobexe.getInParameters().putAll(params);
			}			
		}
		
		return new ArrayList<JobExecution>(jobExeMap.values());
	}
	
	/**
	 * 마지막으로 종료된 JobExecutionId 조회
	 * @param jobInstanceId
	 * @return
	 * @throws SQLException
	 */
	public String getLastEndedJobExecutionId(String jobInstanceId) throws SQLException {
		return (String)sqlMapClient.queryForObject("nbs.controller.selectLastEndedExecutionId", jobInstanceId);
	}

	public JobExecution getJobExecution(String jobexeId) throws SQLException {
		JobExecution jobexe = selectJobExecution(jobexeId);
		return jobexe;
	}
	
	/**
	 * 
	 * @param jobexeId
	 * @return null if not exist
	 * @throws SQLException
	 */
	public JobExecution getJobExecutionDeep(String jobexeId) throws SQLException {
		JobExecution jobexe = selectJobExecution(jobexeId);
		if (jobexe == null) {
			return null;
		}
		Map params = selectJobExecutionParam(jobexeId);
		if (params != null) {
			jobexe.setInParameters(params);
		}
		Properties retvalues = selectJobExecutionReturnValues(jobexeId);
		if (retvalues != null) {
			jobexe.setReturnValues(retvalues);
		}
		return jobexe;
	}
}
