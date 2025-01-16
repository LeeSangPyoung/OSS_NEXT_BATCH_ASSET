package nexcore.scheduler.monitor;

import java.util.List;

import nexcore.scheduler.entity.JobExecution;
import nexcore.scheduler.entity.JobNotify;
import nexcore.scheduler.entity.JobNotifyReceiver;

public interface IJobEndNotifyProcessor {

	public void doNotify(JobExecution jobexe, JobNotify notify, List<JobNotifyReceiver> receivers);
}
