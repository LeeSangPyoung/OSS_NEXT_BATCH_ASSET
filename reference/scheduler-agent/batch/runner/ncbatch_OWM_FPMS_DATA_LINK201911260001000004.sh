#!/bin/sh
/svc/tango/batch/tango-o/bin/exeBatOwm64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20191126/OWM_FPMS_DATA_LINK201911260001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/OWM_FPMS_DATA_LINK201911260001000004-exit.log
exit $NCRETURNCODE

