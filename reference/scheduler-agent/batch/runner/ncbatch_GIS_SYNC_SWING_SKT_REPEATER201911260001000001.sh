#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBatGis64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20191126/GIS_SYNC_SWING_SKT_REPEATER201911260001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/GIS_SYNC_SWING_SKT_REPEATER201911260001000001-exit.log
exit $NCRETURNCODE

