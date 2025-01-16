@echo off

call setenv.cmd

set ADMIN_PORT=8124
set ADMIN_USER=admin
set ADMIN_PASSWD={AES}0wIcgRJ/yQiozEGmLtpZMQ==

rem #######################################
rem ADMIN_PASSWD should be modified.  
rem #######################################

"%JAVA%" ^
  %JAVA_OPTIONS% ^
  -DNEXCORE_HOME=%SCHEDULER_HOME% ^
  -DNEXCORE_LOG_HOME=%SCHEDULER_LOG_HOME% ^
  -Dencryption.keyfile=%ENCRYPTION_KEY_FILE% ^
  nexcore.scheduler.startup.StopMain localhost %ADMIN_PORT% %ADMIN_USER% %ADMIN_PASSWD%

