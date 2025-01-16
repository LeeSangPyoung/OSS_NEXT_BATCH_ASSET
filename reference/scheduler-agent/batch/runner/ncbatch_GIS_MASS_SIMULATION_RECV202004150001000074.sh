#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBatGis64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20200415/GIS_MASS_SIMULATION_RECV202004150001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/GIS_MASS_SIMULATION_RECV202004150001000074-exit.log
exit $NCRETURNCODE

