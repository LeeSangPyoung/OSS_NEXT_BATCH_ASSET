#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBatGis64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20191126/GIS_MASS_SIMULATION_SEND201911260001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/GIS_MASS_SIMULATION_SEND201911260001000002-exit.log
exit $NCRETURNCODE

