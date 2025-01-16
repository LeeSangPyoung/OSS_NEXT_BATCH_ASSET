#!/bin/sh
/svc/tango/batch/tango-o/bin/exeBatOwm64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20221030/OWM_NEOS_DATA_LINK202210300001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/OWM_NEOS_DATA_LINK202210300001000111-exit.log
exit $NCRETURNCODE

