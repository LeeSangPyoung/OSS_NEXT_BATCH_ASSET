@echo off

set CURRDIR=%~dp0
cd %CURRDIR%

set CLASSPATH=nexcore-bat-client.jar;spring.jar;commons-logging.jar;commons-lang-2.1.jar

java -cp %CLASSPATH% nexcore.scheduler.controller.client.BatchCallMain -IP=127.0.0.1 -PORT=8124 -IP2=127.0.0.1 -PORT2=9124 -TIMEOUT=60 -CALLER=%1 -JOBID=%2 -PARAM=%3 -PARAM=%4 -PARAM=%5 -PARAM=%6 -PARAM=%7 -PARAM=%8 -PARAM=%9 

rem Get %ERRORLEVEL% to check error/normal
