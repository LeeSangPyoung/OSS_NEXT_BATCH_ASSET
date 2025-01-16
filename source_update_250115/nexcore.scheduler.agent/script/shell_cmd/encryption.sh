#!/bin/sh

. ./setenv.sh

ENCRYPTION_ALGORITHM=AES

$JAVA \
   $JAVA_OPTIONS \
   -cp $CLASSPATH \
   -Dencryption.keyfile=$ENCRYPTION_KEY_FILE \
   nexcore.framework.supports.EncryptionUtils $ENCRYPTION_ALGORITHM $*

