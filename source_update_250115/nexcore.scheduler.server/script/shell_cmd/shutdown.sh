#!/bin/sh

. ./setenv.sh

ADMIN_PORT=8124
ADMIN_USER=admin
ADMIN_PASSWD={AES}0wIcgRJ/yQiozEGmLtpZMQ==

$JAVA \
  $JAVA_OPTIONS \
  -DNEXCORE_HOME=$SCHEDULER_HOME \
  -DNEXCORE_LOG_HOME=$SCHEDULER_LOG_HOME \
  -Dencryption.keyfile=$ENCRYPTION_KEY_FILE \
  nexcore.scheduler.startup.StopMain localhost $ADMIN_PORT $ADMIN_USER $ADMIN_PASSWD
