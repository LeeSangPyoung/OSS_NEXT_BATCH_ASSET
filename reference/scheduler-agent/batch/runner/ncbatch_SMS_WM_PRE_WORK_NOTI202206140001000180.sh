#!/bin/sh
/svc/tango/batch/tango-o/bin/exeBatOwm64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20220614/SMS_WM_PRE_WORK_NOTI202206140001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/SMS_WM_PRE_WORK_NOTI202206140001000180-exit.log
exit $NCRETURNCODE

