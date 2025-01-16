#!/bin/sh

#. setenv.sh
. /svc/nexcore/scheduler-agent/setenv.sh

ENCRYPTION_ALGORITHM=AES

$JAVA \
   $JAVA_OPTIONS \
   -cp $AGENT_CLASSPATH \
   -Dencryption.keyfile=$ENCRYPTION_KEY_FILE \
   nexcore.framework.supports.EncryptionUtils $ENCRYPTION_ALGORITHM $*

