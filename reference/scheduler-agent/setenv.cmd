set CURRDIR=%~dp0
cd %CURRDIR%
set BATAGENT_HOME=.
set BATAGENT_LOG_HOME=%BATAGENT_HOME%\log

set SYSTEM_ID=DNBA01

set ENCRYPTION_KEY_FILE=

REM ######################################
REM set JAVA_HOME=
REM ######################################

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)

set CLASSPATH=%BATAGENT_HOME%\config
for %%i in (%BATAGENT_HOME%\lib\*.jar) do call appendcp.cmd %%i
for %%i in (%BATAGENT_HOME%\lib\ext\*.jar) do call appendcp.cmd %%i

