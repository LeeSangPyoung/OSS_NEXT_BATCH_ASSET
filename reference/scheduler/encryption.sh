#!/bin/sh

#. setenv.sh
. /svc/nexcore/scheduler/setenv.sh

ENCRYPTION_ALGORITHM=AES
SCHEDULER_CLASSPATH=/svc/nexcore/scheduler/lib
$JAVA \
   $JAVA_OPTIONS \
   -cp $SCHEDULER_CLASSPATH \
   -Dencryption.keyfile=$ENCRYPTION_KEY_FILE \
   nexcore.framework.supports.EncryptionUtils $ENCRYPTION_ALGORITHM $*
