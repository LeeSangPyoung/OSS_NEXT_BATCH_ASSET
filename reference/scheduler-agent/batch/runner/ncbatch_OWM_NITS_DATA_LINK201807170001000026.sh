#!/bin/sh
/svc/tango/batch/tango-o/bin/exeBatOwm64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20180717/OWM_NITS_DATA_LINK201807170001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/OWM_NITS_DATA_LINK201807170001000026-exit.log
exit $NCRETURNCODE

