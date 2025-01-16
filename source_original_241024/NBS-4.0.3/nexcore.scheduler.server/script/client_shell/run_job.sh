#!/bin/sh

CLASSPATH=nexcore-bat-client.jar:../lib/spring.jar:../lib/commons-logging.jar:../lib/commons-lang-2.1.jar

java nexcore.scheduler.controller.client.BatchCallMain -IP=127.0.0.1 -PORT=8124 -IP2=127.0.0.1 -PORT2=9124 -TIMEOUT=60 -CALLER="$1" -JOBID="$2" -PARAM="$3" -PARAM="$4" -PARAM="$5" -PARAM="$6" -PARAM="$7" -PARAM="$8" -PARAM="$9"

exit $?
