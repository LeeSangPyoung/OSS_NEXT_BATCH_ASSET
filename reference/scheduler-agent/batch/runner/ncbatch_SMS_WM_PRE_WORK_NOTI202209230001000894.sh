#!/bin/sh
/svc/tango/batch/tango-o/bin/exeBatOwm64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20220923/SMS_WM_PRE_WORK_NOTI202209230001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/SMS_WM_PRE_WORK_NOTI202209230001000894-exit.log
exit $NCRETURNCODE

