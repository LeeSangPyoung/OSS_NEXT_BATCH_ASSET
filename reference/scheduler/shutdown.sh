#!/bin/sh

#. ./setenv.sh
. /svc/nexcore/scheduler/setenv.sh

ADMIN_PORT=8124
ADMIN_USER=admin
#ADMIN_PASSWD={AES}lyfqkGZ5EPgAa+skeXfG5A==
#ADMIN_PASSWD={AES}Mn08yuUfJn3r8haUGW//Fg==
ADMIN_PASSWD=nexcore
JAVA="/opt/glibc-2.18/lib/ld-linux-x86-64.so.2 --library-path /opt/glibc-2.18/lib:/usr/lib64 /opt/sapmachine-jdk-23.0.1/bin/java"

$JAVA \
  $JAVA_OPTIONS \
  -DNEXCORE_HOME=$SCHEDULER_HOME \
  -DNEXCORE_LOG_HOME=$SCHEDULER_LOG_HOME \
  -Dencryption.keyfile=$ENCRYPTION_KEY_FILE \
  nexcore.scheduler.startup.StopMain localhost $ADMIN_PORT $ADMIN_USER $ADMIN_PASSWD
