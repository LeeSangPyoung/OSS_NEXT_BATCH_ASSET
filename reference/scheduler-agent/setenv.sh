#!/bin/sh

export LANG=ko_KR.UTF-8

PWD=`pwd`
HOSTNAME=`hostname`
HOME_DIR=`dirname $0`
#BATAGENT_HOME=$PWD/$HOME_DIR
BATAGENT_HOME=/svc/nexcore/scheduler-agent
BATAGENT_LOG_HOME=$BATAGENT_HOME/log

# Batch Agent Config
export TANGO_BATCH_BASE=/svc/tango/batch


echo PWD=$PWD
echo HOST_NAME=$HOSTNAME
echo HOME_DIR=$HOME_DIR
echo BATAGENT_HOME=$BATAGENT_HOME
echo BATAGENT_LOG_HOME=$BATAGENT_LOG_HOME

#echo TANGO_BATCH_BASE=$TANGO_BATCH_BASE

SYSTEM_ID=DNBA01

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

CLASSPATH=$BATAGENT_HOME/config
for line in $BATAGENT_HOME/lib/*.jar
do
  CLASSPATH="$CLASSPATH:$line"
done
for line in $BATAGENT_HOME/lib/ext/*.jar
do
  CLASSPATH="$CLASSPATH:$line"
done
export CLASSPATH

# Setup the OUT LOG 
OUT_LOG_FILE=$BATAGENT_LOG_HOME/out.log


