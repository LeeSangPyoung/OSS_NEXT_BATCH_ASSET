#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBatGis64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20200429/GIS_FH_PLAN_RECV202004290001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/GIS_FH_PLAN_RECV202004290001000118-exit.log
exit $NCRETURNCODE

