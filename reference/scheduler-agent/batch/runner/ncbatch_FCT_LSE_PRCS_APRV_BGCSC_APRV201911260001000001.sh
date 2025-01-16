#!/bin/sh
/svc/tango/batch/tango-t/bin/exeBatGis64.sh >> /workspace/svc/nexcore/scheduler-agent/log/job/20191126/FCT_LSE_PRCS_APRV_BGCSC_APRV201911260001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/FCT_LSE_PRCS_APRV_BGCSC_APRV201911260001000001-exit.log
exit $NCRETURNCODE

