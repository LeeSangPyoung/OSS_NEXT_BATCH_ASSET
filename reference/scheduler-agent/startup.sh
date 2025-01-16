#!/bin/sh

#. ./setenv.sh
. /svc/nexcore/scheduler-agent/setenv.sh

JAVA_OPTIONS="-Xms256m -Xmx512m "
JAVA="/opt/glibc-2.18/lib/ld-linux-x86-64.so.2 --library-path /opt/glibc-2.18/lib:/usr/lib64 /opt/sapmachine-jdk-23.0.1/bin/java"

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
