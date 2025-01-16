#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBatGis64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20200201/GIS_FH_PLAN_SEND202002010001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/GIS_FH_PLAN_SEND202002010001000011-exit.log
exit $NCRETURNCODE

