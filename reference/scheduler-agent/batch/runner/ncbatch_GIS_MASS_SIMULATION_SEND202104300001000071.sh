#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBatGis64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20210430/GIS_MASS_SIMULATION_SEND202104300001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/GIS_MASS_SIMULATION_SEND202104300001000071-exit.log
exit $NCRETURNCODE

