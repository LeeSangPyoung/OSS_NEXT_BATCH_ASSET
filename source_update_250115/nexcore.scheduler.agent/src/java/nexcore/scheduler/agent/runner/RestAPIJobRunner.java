/* 
* Copyright (c) 2017 SK C&C Co., Ltd. All rights reserved. 
* 
* This software is the confidential and proprietary information of SK C&C. 
* You shall not disclose such confidential information and shall use it 
* only in accordance with the terms of the license agreement you entered into with SK C&C. 
*/
package nexcore.scheduler.agent.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import nexcore.scheduler.agent.IJobRunnerCallBack;
import nexcore.scheduler.agent.JobContext;
import nexcore.scheduler.agent.joblog.ILogger;
import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.exception.AgentException;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Rest API 기반으로 처리하는 Job Type
 */
public class RestAPIJobRunner extends AbsJobRunner {

	protected long   statusPollingInterval = 3000;
	
	public RestAPIJobRunner() {
		super();
	}
	
	/**
	 * @return the statusPollingInterval
	 */
	public long getStatusPollingInterval() {
		return statusPollingInterval;
	}

	/**
	 * @param statusPollingInterval the statusPollingInterval to set
	 */
	public void setStatusPollingInterval(long statusPollingInterval) {
		this.statusPollingInterval = statusPollingInterval;
	}

	public void init() {
	}
	
	public void destroy() {
	}

	public void start(final JobExecution je, final JobContext context, final IJobRunnerCallBack jobRunnerCallBack) {
		getJobRunThreadManager().newThreadAndStart(context,
				new Runnable() {
					public void run() {
						int          returnCode   = 0; // 0 이면 정상 종료 그외 숫자는 에러
						ILogger      logger       = getAgentMain().getJobLogManager().getLog(context);
						String       errorMsg     = null;
						
						Throwable throwable  = null;
						try {
						    // 선처리 실행
		                    doJobExePreProcessors(context);

							logJobStart(context);
							
							// 시작 요청
							Map<String, String> map = new LinkedHashMap<String, String>();
							map.put("jobId",       je.getJobId());
							map.put("jobInsId",    je.getJobInstanceId());
							map.put("jobExeId",    je.getJobExecutionId());
							map.put("procDate",    je.getProcDate());
							map.put("programName", je.getComponentName());
							
							ObjectNode requestJson  = createJsonObject(map);
							ObjectNode paramJson = createJsonObject(je.getInParameters());
							requestJson.set("jobParameters", paramJson);
							
							invokeHttpPost(makeInvokeHttpUri(context, "run"), requestJson.toString(), logger);
							
							// 상태 모니터링
							pollingStatus(je, context, jobRunnerCallBack, logger);
						}catch (IOException e) {
							throwable = e;
							returnCode = -1; // TODO 에러코드 정리. 
							errorMsg = Util.fitLength(e.toString(), 1000);
							Util.logError(logger, e.toString(), e);
						}catch (Throwable e) {
						    throwable = e;
							returnCode = -1; // TODO 에러코드 정리. 
							Throwable cause = Util.getCauseException(e);
							errorMsg = Util.fitLength(cause.getMessage(), 1000);
							Util.logError(logger, cause.toString(), e);
						}finally {
							if(returnCode == -1) {
								jobEnd(je, context, jobRunnerCallBack, returnCode, System.currentTimeMillis(), errorMsg, throwable);
							}
						}
					}
				}
			);
    }

	public void suspend(String jobExecutionId) {
		JobContext context = getJobExecutionBoard().getJobContext(jobExecutionId);
		if (context == null) {
			throw new AgentException("agent.jobexecution.notfound", jobExecutionId);
		}
		
		try {
			invokeHttpPut(makeInvokeHttpUri(context, jobExecutionId + "/suspend"));
		}catch(Exception e) {
			ILogger logger = getAgentMain().getJobLogManager().getLog(context);
			Util.logError(logger, e.toString(), e);
			throw new AgentException("agent.fail.suspend.job", e, jobExecutionId);
		}
	}

	public void resume(String jobExecutionId) {
		JobContext context = getJobExecutionBoard().getJobContext(jobExecutionId);
		if (context == null) {
			throw new AgentException("agent.jobexecution.notfound", jobExecutionId);
		}
		
		try {
			invokeHttpPut(makeInvokeHttpUri(context, jobExecutionId + "/resume"));
		}catch(Exception e) {
			ILogger logger = getAgentMain().getJobLogManager().getLog(context);
			Util.logError(logger, e.toString(), e);
			throw new AgentException("agent.fail.resume.job", e, jobExecutionId);
		}
	}

	public void stop(String jobExecutionId) {
		JobContext context = getJobExecutionBoard().getJobContext(jobExecutionId);
		if (context == null) {
			throw new AgentException("agent.jobexecution.notfound", jobExecutionId);
		}
		
		try {
			invokeHttpPut(makeInvokeHttpUri(context, jobExecutionId + "/stop"));
		}catch(Exception e) {
			ILogger logger = getAgentMain().getJobLogManager().getLog(context);
			Util.logError(logger, e.toString(), e);
			throw new AgentException ("agent.thread.stop.fail", e, jobExecutionId);
		}
	}
	
	protected void pollingStatus(JobExecution je, JobContext context, IJobRunnerCallBack jobRunnerCallBack, ILogger logger) throws Exception {
		// TODO polling logic
		CloseableHttpClient httpClient = null;
		HttpGet             httpGet = null;
		try {
			httpClient = createHttpClient(); 
			httpGet    = createHttpGet(makeInvokeHttpUri(context, je.getJobExecutionId() + "/status"));
			
			while(true) {
				Util.logDebug(logger, "GET(" + httpGet.getURI() + ") start.");

				HttpResponse response = httpClient.execute(httpGet);
				int    statusCode   = response.getStatusLine().getStatusCode();
				String responseData = readResponseData(response);
				
				Util.logDebug(logger, "GET(" + httpGet.getURI() + ") end. " + statusCode + " - " + responseData);
		
				if(HttpStatus.SC_OK != statusCode){
					throw new Exception(responseData);
				}
				
				ObjectMapper mapper = new ObjectMapper();
				JsonNode responseJson = mapper.readTree(responseData);
				
				String status          = getString(responseJson, "status");
				String message         = getString(responseJson, "message");
				int    returnCode      = toInt(getString(responseJson, "returnCode"));
//				String startTime       = getString(responseJson, "startTime");
				String endTime         = getString(responseJson, "endTime");
				long   progressCurrent = toLong(getString(responseJson, "progressCurrent"));
				long   progressTotal   = toLong(getString(responseJson, "progressTotal"));
				
				if(progressCurrent > 0 || progressTotal > 0) {
					je.setProgressCurrent(progressCurrent);
					je.setProgressTotal(progressTotal);
				}
				
				// 종료
				if("ENDED".equals(status)) {
					Util.logInfo(logger, "ENDED " + je.getJobExecutionId() + " - RetrunCode:" + returnCode + "\n" + message);
					jobEnd(je, context, jobRunnerCallBack, returnCode, DateUtil.getTimestampLong(endTime), message, null);
					break;
				}
				// 일시정지
				else if("SUSPENDED".equals(status)) {
					if(je.getState() != JobExecution.STATE_SUSPENDED){
						je.setState(JobExecution.STATE_SUSPENDED);
						jobRunnerCallBack.callBackJobSuspend(je);
					}
				}
				// 실행중
				else if("RUNNING".equals(status)) {
					// RESUME (SUSPENDED ==> RUNNING)
					if(je.getState() == JobExecution.STATE_SUSPENDED){
						je.setState(JobExecution.STATE_RUNNING);
						jobRunnerCallBack.callBackJobResume(je);
					}
				}
				
				try {
					Thread.sleep(statusPollingInterval);
				}catch(Exception e) {
				}
			}
		} finally {
			close(httpClient);
		}
	}
	
    /**
     * Job 종료 처리. 
     * @param je
     * @param context
     * @param returnCode
     * @param errorMsg
     * @param jobRunnerCallBack
     */
    protected void jobEnd(JobExecution je, JobContext context, IJobRunnerCallBack jobRunnerCallBack, int returnCode, long endTime, String message, Throwable throwable) {
        je.setEndTime(endTime);
        je.setReturnCode(returnCode);
        je.setErrorMsg(returnCode != 0 ? message : "");
        je.setReturnValues(context.getReturnValues());
        je.setState(JobExecution.STATE_ENDED);
        try {
            logJobEnd(context);
        }catch(Throwable e) {
            Util.logError(log, "logJobEnd() fail/"+je.getJobExecutionId(), e); 
        }
        
        try {
        	jobRunnerCallBack.callBackJobEnd(je);
        }finally {
            getJobExecutionBoard().remove(je.getJobExecutionId());
            
            // 후처리 실행
            doJobExePostProcessors(context, throwable);
        }
    }
    
    protected String makeInvokeHttpUri(JobContext context, String functionName) {
    	String baseUri = context.getInParameter("REST_BASE_URI");
    	if(baseUri == null || baseUri.length() < 1) {
    		throw new RuntimeException(MSG.get("main.jobinfo.check.missing.param", "RestAPI", "REST_BASE_URI"));  // RestAPI Job 은 'REST_BASE_URI' 파라미터 값이 필수입니다.
    	}
    	return baseUri + "/" + functionName;
    }
    
	protected String invokeHttpPost(String uri, String requestJson, ILogger logger) throws Exception {
		CloseableHttpClient httpClient = null;
		HttpPost            httpPost = null;
		try {
			httpClient = createHttpClient(); 
			httpPost   = createHttpPost(uri, requestJson);
			
			Util.logInfo(logger, "POST(" + httpPost.getURI() + ") start. " + requestJson);

			HttpResponse response = httpClient.execute(httpPost);
			
			int    statusCode   = response.getStatusLine().getStatusCode();
			String responseData = readResponseData(response);
			
			Util.logInfo(logger, "POST(" + httpPost.getURI() + ") end. " + statusCode + " - " + responseData);

			if(HttpStatus.SC_OK != statusCode){
				throw new RuntimeException(responseData);
			}
			
			return responseData;
		} finally {
			close(httpPost);
			close(httpClient);
		}
	}
	
	protected String invokeHttpGet(String uri) throws Exception {
		CloseableHttpClient httpClient = null;
		HttpGet             httpGet = null;
		try {
			httpClient = createHttpClient(); 
			httpGet   = createHttpGet(uri);
			
			HttpResponse response = httpClient.execute(httpGet);
			
			int    statusCode   = response.getStatusLine().getStatusCode();
			String responseData = readResponseData(response);
	
			if(HttpStatus.SC_OK != statusCode){
				throw new RuntimeException(responseData);
			}
			
			return responseData;
		} finally {
			close(httpClient);
		}
	}

	protected String invokeHttpPut(String uri) throws Exception {
		CloseableHttpClient httpClient = null;
		HttpPut             httpPut = null;
		try {
			httpClient = createHttpClient(); 
			httpPut    = createHttpPut(uri);
			
			HttpResponse response = httpClient.execute(httpPut);
			
			int    statusCode   = response.getStatusLine().getStatusCode();
			String responseData = readResponseData(response);
	
			if(HttpStatus.SC_OK != statusCode){
				throw new RuntimeException(responseData);
			}
			
			return responseData;
		} finally {
			close(httpClient);
		}
	}

	protected HttpPost createHttpPost(String uri, String requestJson) {
    	HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("User-Agent", "NEXCORE Batch Scheduler");
		httpPost.setEntity(new StringEntity(requestJson, ContentType.APPLICATION_JSON));
    	return httpPost;
    }
    
    protected HttpGet createHttpGet(String uri) {
    	HttpGet http = new HttpGet(uri);
    	http.addHeader("User-Agent", "NEXCORE Batch Scheduler");
    	return http;
    }
    
    protected HttpPut createHttpPut(String uri) {
    	HttpPut http = new HttpPut(uri);
    	http.addHeader("User-Agent", "NEXCORE Batch Scheduler");
    	return http;
    }
    
    protected CloseableHttpClient createHttpClient() {
		return HttpClientBuilder.create().build(); //HttpClients.createDefault();
    }
    
    protected void close(Reader reader) {
    	if(reader != null){
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
    }
	
    protected void close(HttpPost post) {
    	if(post != null) {
    		post.releaseConnection();
		}
    }
    
    protected void close(CloseableHttpClient client) {
		if(client != null) {
			try {
				client.close();
			} catch (IOException e) {
			}
		}
    }

    protected String readResponseData(HttpResponse response) throws Exception {
    	BufferedReader reader = null; 
    	try {
    		reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuilder responseDataBuff = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				responseDataBuff.append(line);
			}
			return responseDataBuff.toString();
    	}
    	finally {
    		close(reader);
    	}
    }
    
	protected ObjectNode createJsonObject(Map<String, String> map){
		ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
		if(map != null) {
			for(Entry<String, String> entry : map.entrySet()) {
				node.put(entry.getKey(), entry.getValue());
			}
		}
		return node;
	}
	
	protected String getString(JsonNode node, String name) {
		JsonNode obj = node.get(name);
		if(obj == null || obj.isNull()) {
			return null;
		}
		return obj.asText();
	}
	
	protected long toLong(String value) {
		if(value == null || value.length() < 1) {
			return 0;
		}
		try {
			return Long.parseLong(value);
		}catch(Exception e) {
			return 0;
		}
	}
	
	protected int toInt(String value) {
		if(value == null || value.length() < 1) {
			return 0;
		}
		try {
			return Integer.parseInt(value);
		}catch(Exception e) {
			return 0;
		}
	}
	
}
