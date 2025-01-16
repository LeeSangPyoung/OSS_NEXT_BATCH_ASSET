#!/bin/sh

CLASSPATH=nexcore-bat-agentclient.jar:spring.jar:commons-logging.jar:commons-lang-2.1.jar

java -cp $CLASSPATH nexcore.scheduler.agent.client.CommandLineAgentAdmin -IP=127.0.0.1 -PORT=8125 


