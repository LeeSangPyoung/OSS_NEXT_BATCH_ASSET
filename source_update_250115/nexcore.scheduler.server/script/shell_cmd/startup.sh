#!/bin/sh

. ./setenv.sh

JAVA_OPTIONS="-Xms512m -Xmx512m "

$JAVA \
  $JAVA_OPTIONS \
  -DNC_SCHEDULER \
  -DNEXCORE_ID=$SYSTEM_ID \
  -DNEXCORE_HOME=$SCHEDULER_HOME \
  -DNEXCORE_LOG_HOME=$SCHEDULER_LOG_HOME \
  -Dencryption.keyfile=$ENCRYPTION_KEY_FILE \
  nexcore.scheduler.startup.StarterMain > $OUT_LOG_FILE 2>&1 &

echo Starting NEXCORE Batch Scheduler 
echo stdout/err redirected to $OUT_LOG_FILE
echo tail -f $OUT_LOG_FILE
tail -f $OUT_LOG_FILE
