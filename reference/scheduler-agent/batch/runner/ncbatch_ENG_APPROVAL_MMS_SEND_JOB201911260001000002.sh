#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBat64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20191126/ENG_APPROVAL_MMS_SEND_JOB201911260001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/ENG_APPROVAL_MMS_SEND_JOB201911260001000002-exit.log
exit $NCRETURNCODE

