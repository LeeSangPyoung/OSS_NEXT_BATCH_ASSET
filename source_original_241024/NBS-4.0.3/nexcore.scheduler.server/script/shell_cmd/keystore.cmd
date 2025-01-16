@echo off
SETLOCAL

call setenv.cmd

set KEY_STORE=%1
set KEY_PASSWORD=%2
set STORE_PASSWORD=%3


if "%KEY_STORE%" == "" goto usage
if "%KEY_PASSWORD%" == "" goto usage
if "%STORE_PASSWORD%" == "" goto usage

 if "x%JAVA_HOME%" == "x" (
   set  KEYTOOL=keytool
 ) else (
   set "KEYTOOL=%JAVA_HOME%\bin\keytool"
 )

set KEYTOOL_OPTION=-genkey -keyalg RSA -noprompt -alias jetty
rem set KEYTOOL_OPTION=%KEYTOOL_OPTION% -dname "CN=commonName, OU=OrganizationUnit, O=OrganizaionName, L=Local, S=State, C=Country(2 letter)"
set KEYTOOL_OPTION=%KEYTOOL_OPTION% -dname "CN=, OU=, O=, L=, S=, C="
set KEYTOOL_OPTION=%KEYTOOL_OPTION% -keystore %KEY_STORE% -storepass %STORE_PASSWORD% -keypass %KEY_PASSWORD%
 
 "%KEYTOOL%" %KEYTOOL_OPTION%
 
goto end
 
:usage
echo "Usage: %0 <key_store_name> <key_password> <store_password>"

:end

ENDLOCAL