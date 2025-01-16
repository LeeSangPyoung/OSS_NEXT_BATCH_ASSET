#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBat64.sh com.skt.tango.transmission.batch.main.BatchMain >> /workspace/svc/nexcore/scheduler-agent/log/job/20191126/THREE_G_EQP_SET_SVLN_BATCH201911260001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/THREE_G_EQP_SET_SVLN_BATCH201911260001000001-exit.log
exit $NCRETURNCODE

