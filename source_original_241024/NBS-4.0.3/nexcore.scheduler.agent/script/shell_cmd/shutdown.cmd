@echo off

call setenv.cmd

"%JAVA%" ^
  %JAVA_OPTIONS% ^
  -DNC_BAT_AGENT ^
  -DNEXCORE_ID=%SYSTEM_ID% ^
  -DNEXCORE_HOME=%BATAGENT_HOME% ^
  -DNEXCORE_LOG_HOME=%BATAGENT_LOG_HOME% ^
  -Dencryption.keyfile=%ENCRYPTION_KEY_FILE% ^
  nexcore.scheduler.agent.startup.StopMain localhost 8126 user password