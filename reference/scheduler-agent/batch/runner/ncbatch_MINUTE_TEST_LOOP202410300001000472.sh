#!/bin/sh
sleep 10 >> /workspace/svc/nexcore/scheduler-agent/log/job/20241030/MINUTE_TEST_LOOP202410300001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/MINUTE_TEST_LOOP202410300001000472-exit.log
exit $NCRETURNCODE

