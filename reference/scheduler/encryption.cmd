@echo off
SETLOCAL

call setenv.cmd

set ENCRYPTION_ALGORITHM=AES

REM set JAVA_HOME=

rem ##################################################
rem set boot parameter
rem ##################################################
set BOOT_PARAMETER=%*

"%JAVA%" ^
  %JAVA_OPTIONS% ^
  -Dencryption.keyfile=%ENCRYPTION_KEY_FILE% ^
  nexcore.framework.supports.EncryptionUtils %ENCRYPTION_ALGORITHM% %BOOT_PARAMETER%

ENDLOCAL
