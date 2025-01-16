#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBatGis64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20200201/TANGO_GIS_RING_REG_WEST202002010001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/TANGO_GIS_RING_REG_WEST202002010001000001-exit.log
exit $NCRETURNCODE

