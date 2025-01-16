#!/bin/sh
/svc/tango/batch/tango-c/bin/exeBat64.sh com.skt.tango.common.batch.main.BatchMain >> /workspace/svc/nexcore/scheduler-agent/log/job/20191126/CCO_API_AUTH_LOG_JOB201911260001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/CCO_API_AUTH_LOG_JOB201911260001000002-exit.log
exit $NCRETURNCODE

