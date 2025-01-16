#!/bin/sh
/svc/tango/batch/tango-o/bin/exeBatOwm64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20190612/PKMS_DATA_LINK201906120001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/PKMS_DATA_LINK201906120001000225-exit.log
exit $NCRETURNCODE

