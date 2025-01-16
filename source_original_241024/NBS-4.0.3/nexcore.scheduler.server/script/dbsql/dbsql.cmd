@echo off
SETLOCAL

rem ##################################################
rem # Set configs
rem ##################################################
rem NEXCORE_HOME
set NEXCORE_HOME=./

rem JAVA_HOME
rem set JAVA_HOME=C:\Java\jdk1.7.0_80

rem JAVA_OPTIONS
set JAVA_OPTIONS=


rem ##################################################
rem # Try to determine JAVA if not set
rem ##################################################
if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
)


rem ##################################################
rem # Try to determine NEXCORE_HOME if not set
rem ##################################################
set NEXCORE_PRGDIR=%~dp0
if "%NEXCORE_HOME%"=="" set NEXCORE_HOME=%NEXCORE_PRGDIR%
set NEXCORE_PRGDIR=


rem #####################################################
rem # Set CLASSPATH
rem #####################################################
setlocal EnableDelayedExpansion
set NEXCORE_CLASSPATH=%NEXCORE_HOME%;%NEXCORE_HOME%\lib
FOR %%c in ("%NEXCORE_HOME%\lib\*.jar") DO set NEXCORE_CLASSPATH=!NEXCORE_CLASSPATH!;%%c
FOR %%c in ("%NEXCORE_HOME%\lib\*.zip") DO set NEXCORE_CLASSPATH=!NEXCORE_CLASSPATH!;%%c


rem ##################################################
rem # Do the action
rem ##################################################
echo NEXCORE_HOME      : [%NEXCORE_HOME%]
echo NEXCORE_CLASSPATH : [%NEXCORE_CLASSPATH%]

"%JAVA%" %JAVA_OPTIONS% -cp %NEXCORE_CLASSPATH% org.apache.tools.ant.launch.Launcher -f dbsql.xml

ENDLOCAL