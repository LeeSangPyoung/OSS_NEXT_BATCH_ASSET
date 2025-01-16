#!/bin/sh

. ./setenv.sh

JAVA_OPTIONS="-Xms256m -Xmx512m "

$JAVA \
  $JAVA_OPTIONS \
  -DNC_BATAGENT \
  -DNEXCORE_ID=$SYSTEM_ID \
  -DNEXCORE_HOME=$BATAGENT_HOME \
  -DNEXCORE_LOG_HOME=$BATAGENT_LOG_HOME \
  -Dencryption.keyfile=$ENCRYPTION_KEY_FILE \
  nexcore.scheduler.agent.startup.StarterMain > $OUT_LOG_FILE 2>&1 &

echo Starting NEXCORE Batch Scheduler Agent
echo stdout/err redirected to $OUT_LOG_FILE
