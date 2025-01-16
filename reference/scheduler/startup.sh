#!/bin/sh

#. ./setenv.sh
. /svc/nexcore/scheduler/setenv.sh

JAVA_OPTIONS="-Xms512m -Xmx512m -Duser.language=ko -Duser.country=KR "

echo $JAVA
JAVA="/opt/glibc-2.18/lib/ld-linux-x86-64.so.2 --library-path /opt/glibc-2.18/lib:/usr/lib64 /opt/sapmachine-jdk-23.0.1/bin/java"


echo $JAVA
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
#echo tail -f $OUT_LOG_FILE
#tail -f $OUT_LOG_FILE
