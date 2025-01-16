#!/bin/sh
PATH=.:$PATH
cd /svc/tango/batch/tango-i/etl/sh/TangoEtlJobMain
TangoEtlJobMain_run.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20200201/CM_EQP_BAS_TO_OM202002010001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/CM_EQP_BAS_TO_OM202002010001000001-exit.log
exit $NCRETURNCODE

