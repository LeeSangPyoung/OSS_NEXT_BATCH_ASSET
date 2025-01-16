package nexcore.scheduler.monitor.internal;

import java.util.List;

import org.apache.commons.logging.Log;

import com.ibatis.sqlmap.client.SqlMapClient;

import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobNotify;
import nexcore.scheduler.entity.JobNotifyReceiver;
import nexcore.scheduler.entity.JobNotifySendInfo;
import nexcore.scheduler.log.LogManager;
import nexcore.scheduler.monitor.IJobEndNotifyProcessor;
import nexcore.scheduler.msg.MSG;
import nexcore.scheduler.util.DateUtil;
import nexcore.scheduler.util.Util;

/**
 * 
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : Job 통지를 위해 통지 리스트 테이블에 insert함 </li>
 * <li>작성일 : 2011. 4. 6.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */
public class DefaultJobNotifyProcessor implements IJobEndNotifyProcessor {
	private boolean                enable;
	private SqlMapClient           sqlMapClient;
	private int                    keepDaysForSendList;
	private Log                    log;

	public void init() {
		log = LogManager.getSchedulerLog();
	}
	
	public void destroy() {
	}
	
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public int getKeepDaysForSendList() {
		return keepDaysForSendList;
	}

	public void setKeepDaysForSendList(int keepDaysForSendList) {
		this.keepDaysForSendList = keepDaysForSendList;
	}

	public void doNotify(JobExecution jobexe, JobNotify notify, List<JobNotifyReceiver> receivers) {
		if (!enable) return;
		try {
			sqlMapClient.startTransaction();
			sqlMapClient.startBatch();
			for (JobNotifyReceiver receiver : receivers) {
				JobNotifySendInfo info = new JobNotifySendInfo();
				info.setJobExecutionId	(jobexe.getJobExecutionId());
				info.setJobId			(jobexe.getJobId());
				info.setJobDesc			(jobexe.getDescription());
				info.setAgentNode       (jobexe.getAgentNode());
				info.setReturnCode		(jobexe.getReturnCode());
				info.setErrorMsg		(jobexe.getErrorMsg());
				info.setReceiverId		(receiver.getId());
				info.setReceiverName	(receiver.getName());
				info.setSendState		("I");
				info.setCreateTime      (DateUtil.getCurrentTimestampString());
				info.setNotifyId        (notify.getId());
				info.setCheckValue1     (notify.getCheckValue1());
				info.setCheckValue2     (notify.getCheckValue2());
				info.setCheckValue3     (notify.getCheckValue3());
				
				int cnt = 0;
				if (receiver.isRecvByEmail()) {
					info.setRecvType  ("EMAIL");
					info.setRecvPoint (receiver.getEmailAddr());
					
					cnt += sqlMapClient.update("nbs.monitor.insertJobNotifySendList", info);
				}
				if (receiver.isRecvBySms()) {
					info.setRecvType  ("SMS");
					info.setRecvPoint (receiver.getSmsNum());
					cnt += sqlMapClient.update("nbs.monitor.insertJobNotifySendList", info);
				}
				if (receiver.isRecvByTerminal()) {
					info.setRecvType  ("TERMINAL");
					info.setRecvPoint (receiver.getTerminalId());
					cnt += sqlMapClient.update("nbs.monitor.insertJobNotifySendList", info);
				}
				if (receiver.isRecvByMessenger()) {
					info.setRecvType  ("MESSENGER");
					info.setRecvPoint (receiver.getMessengerId());
					cnt += sqlMapClient.update("nbs.monitor.insertJobNotifySendList", info);
				}
				if (receiver.isRecvByDev1()) {
					info.setRecvType  ("DEV1");
					info.setRecvPoint (receiver.getDev1Point());
					cnt += sqlMapClient.update("nbs.monitor.insertJobNotifySendList", info);
				}
				if (receiver.isRecvByDev2()) {
					info.setRecvType  ("DEV2");
					info.setRecvPoint (receiver.getDev2Point());
					cnt += sqlMapClient.update("nbs.monitor.insertJobNotifySendList", info);
				}
				if (receiver.isRecvByDev3()) {
					info.setRecvType  ("DEV3");
					info.setRecvPoint (receiver.getDev3Point());
					cnt += sqlMapClient.update("nbs.monitor.insertJobNotifySendList", info);
				}
				Util.logDebug(log, cnt+" row inserted to NBS_NOTIFY_SEND_LIST");
			}
			sqlMapClient.executeBatch();
			sqlMapClient.commitTransaction();
		}catch(Exception e) {
			Util.logError(log, MSG.get("main.jobnotify.notify.process.error", jobexe.getJobExecutionId(), "INSERT SEND LIST"), e);
		}finally {
			try { sqlMapClient.endTransaction(); }catch(Exception e) {}
		}
	}
	
	public String toString() {
		return "DefaultJobEndNotifyProcessor";
	}

}
