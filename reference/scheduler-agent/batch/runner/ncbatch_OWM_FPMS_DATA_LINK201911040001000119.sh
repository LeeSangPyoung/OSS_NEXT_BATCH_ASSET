#!/bin/sh
/svc/tango/batch/tango-o/bin/exeBatOwm64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20191104/OWM_FPMS_DATA_LINK201911040001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/OWM_FPMS_DATA_LINK201911040001000119-exit.log
exit $NCRETURNCODE

