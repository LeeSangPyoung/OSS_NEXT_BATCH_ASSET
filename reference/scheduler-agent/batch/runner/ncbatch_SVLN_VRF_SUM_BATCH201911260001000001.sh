#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBat256.sh com.skt.tango.transmission.batch.main >> /workspace/svc/nexcore/scheduler-agent/log/job/20191126/SVLN_VRF_SUM_BATCH201911260001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/SVLN_VRF_SUM_BATCH201911260001000001-exit.log
exit $NCRETURNCODE

