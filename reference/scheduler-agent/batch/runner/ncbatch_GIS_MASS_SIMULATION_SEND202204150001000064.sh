#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBatGis64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20220415/GIS_MASS_SIMULATION_SEND202204150001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/GIS_MASS_SIMULATION_SEND202204150001000064-exit.log
exit $NCRETURNCODE

