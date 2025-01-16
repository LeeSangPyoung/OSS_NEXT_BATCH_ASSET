set CURRDIR=%~dp0
cd %CURRDIR%
set SCHEDULER_HOME=.
set SCHEDULER_LOG_HOME=%SCHEDULER_HOME%\log

set SYSTEM_ID=DNBS01

set ENCRYPTION_KEY_FILE=

REM ######################################
REM set JAVA_HOME=
REM ######################################

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

set CLASSPATH=%SCHEDULER_HOME%\config
for %%i in (%SCHEDULER_HOME%\lib\*.jar) do call appendcp.cmd %%i
for %%i in (%SCHEDULER_HOME%\lib\ext\*.jar) do call appendcp.cmd %%i
