#!/bin/sh

#. setenv.sh
. /svc/nexcore/scheduler/setenv.sh

$JAVA \
  $JAVA_OPTIONS \
  -DNC_JOBCONRTOL \
  -DNEXCORE_HOME=$SCHEDULER_HOME \
  -DNEXCORE_LOG_HOME=$SCHEDULER_LOG_HOME \
  -Dencryption.keyfile=$ENCRYPTION_KEY_FILE \
  nexcore.scheduler.controller.admin.CommandLineJobControl $*
