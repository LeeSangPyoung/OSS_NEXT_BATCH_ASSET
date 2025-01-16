#!/bin/sh
sleep 10 >> /workspace/svc/nexcore/scheduler-agent/log/job/20241106/MINUTE_TEST_LOOP202411060001-stdout.log 2>&1
NCRETURNCODE=$?
echo $NCRETURNCODE >> /svc/nexcore/scheduler-agent/batch/runner/MINUTE_TEST_LOOP202411060001003320-exit.log
exit $NCRETURNCODE

