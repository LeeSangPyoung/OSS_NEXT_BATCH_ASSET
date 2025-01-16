@echo off

call setenv.cmd

set JAVA_OPTIONS=-Xmx512m

"%JAVA%" ^
  %JAVA_OPTIONS% ^
  -DNC_SCHEDULER ^
  -DNEXCORE_ID=%SYSTEM_ID% ^
  -DNEXCORE_HOME=%SCHEDULER_HOME% ^
  -DNEXCORE_LOG_HOME=%SCHEDULER_LOG_HOME% ^
  -Dencryption.keyfile=%ENCRYPTION_KEY_FILE% ^
  nexcore.scheduler.startup.StarterMain
