#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBat64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20200201/ENG_APPROVAL_MMS_SEND_JOB202002010001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/ENG_APPROVAL_MMS_SEND_JOB202002010001000021-exit.log
exit $NCRETURNCODE

