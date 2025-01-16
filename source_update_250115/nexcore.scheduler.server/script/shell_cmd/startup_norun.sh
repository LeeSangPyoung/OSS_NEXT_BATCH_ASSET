#!/bin/sh

. ./setenv.sh

$JAVA \
  $JAVA_OPTIONS \
  -DNC_SCHEDULER \
  -DNEXCORE_ID=$SYSTEM_ID \
  -DNEXCORE_HOME=$SCHEDULER_HOME \
  -DNEXCORE_LOG_HOME=$SCHEDULER_LOG_HOME \
  -Dencryption.keyfile=$ENCRYPTION_KEY_FILE \
  -DNC_SCHEDULER_NORUN=true \
  nexcore.scheduler.startup.StarterMain > $OUT_LOG_FILE &
