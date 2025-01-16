#!/bin/sh

export LANG=ko_KR

PWD=`pwd`
HOSTNAME=`hostname`
HOME_DIR=`dirname $0`
SCHEDULER_HOME=$PWD/$HOME_DIR
export SCHEDULER_HOME

SCHEDULER_LOG_HOME=$SCHEDULER_HOME/log
export SCHEDULER_LOG_HOME

SYSTEM_ID=DNBS01
export SYSTEM_ID

# JAVA_HOME=

ENCRYPTION_KEY_FILE=
export ENCRYPTION_KEY_FILE

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

CLASSPATH=$SCHEDULER_HOME/config
for line in $SCHEDULER_HOME/lib/*.jar
do
  CLASSPATH="$CLASSPATH:$line"
done
for line in $SCHEDULER_HOME/lib/ext/*.jar
do
  CLASSPATH="$CLASSPATH:$line"
done
export CLASSPATH

# Setup the OUT LOG 
OUT_LOG_FILE=$SCHEDULER_LOG_HOME/out.log

