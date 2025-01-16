#!/bin/sh

export LANG=ko_KR.UTF-8

PWD=`pwd`
HOSTNAME=`hostname`
HOME_DIR=`dirname $0`
#SCHEDULER_HOME=$PWD/$HOME_DIR
SCHEDULER_HOME=/svc/nexcore/scheduler
SCHEDULER_LOG_HOME=$SCHEDULER_HOME/log

SYSTEM_ID=DNBS01

#JAVA_HOME=/usr/java/jdk1.8.0_92

ENCRYPTION_KEY_FILE=

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi
JAVA="java"

CLASSPATH=$SCHEDULER_HOME/config
for line in $SCHEDULER_HOME/lib/*.jar
do
  CLASSPATH="$CLASSPATH:$line"
done
for line in $SCHEDULER_HOME/lib/ext/*.jar
do
  CLASSPATH="$CLASSPATH:$line"
done
for line in $SCHEDULER_HOME/lib/notify/*.jar
do
  CLASSPATH="$CLASSPATH:$line"
done
export CLASSPATH

# Setup the OUT LOG 
OUT_LOG_FILE=$SCHEDULER_LOG_HOME/out.log

