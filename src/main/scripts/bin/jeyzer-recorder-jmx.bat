@echo off

echo -------------------------------------
echo Jeyzer Recorder JMX client
echo -------------------------------------

rem -----------------------------------------------------------
rem Jeyzer Recorder parameters
rem See also the set-jeyzer-params.bat
rem Some parameters below are intended be set externally 
rem    once the Recorder gets integrated in any DevOps platform
rem -----------------------------------------------------------

rem Process card : dump process info at startup
set JEYZER_RECORD_PROCESS_CARD_ENABLED=true

rem Generate global data dump capture time 
set JEYZER_RECORD_CAPTURE_DURATION=true

rem JMX connection user and password(optional)
if not "%JEYZER_TARGET_JMX_USER%" == "" goto gotTargetJmxUser
set JEYZER_TARGET_JMX_USER=admin
:gotTargetJmxUser

if not "%JEYZER_TARGET_JMX_PASSWORD%" == "" goto gotTargetJmxPassword
set JEYZER_TARGET_JMX_PASSWORD=changeit
:gotTargetJmxPassword

rem -----------------------------------------------------------
rem Jeyzer Recorder Archiving parameters (zip or tar.gz)
rem -----------------------------------------------------------

rem The recording archiving offset. 
rem Offset used to define the end limit of the archiving time slot.
rem Must at least be be multiple of the recording period.
rem If Jeyzer monitoring is enabled, must be higher than the scanning period.  
set JEYZER_RECORD_ARCHIVE_TIME_OFFSET=25s


rem -----------------------------------------------------------
rem Internals - do not edit
rem -----------------------------------------------------------

rem Dump access method (JMX or AdvancedJMX)
set JEYZER_RECORD_DUMP_METHOD=AdvancedJMX

rem Dump start delay
set JEYZER_RECORD_START_DELAY=0s

TITLE=Jeyzer JMX Recorder

rem The Jeyzer Recorder home (parent directory)
set "CURRENT_DIR=%cd%"
cd ..
set "JEYZER_RECORDER_HOME=%cd%"
cd "%CURRENT_DIR%"

rem The Jeyzer Recorder configuration directory
set "JEYZER_RECORD_CONFIG_DIR=%JEYZER_RECORDER_HOME%\config"

rem Ensure Jeyzer parameters get set
if exist "%JEYZER_RECORDER_HOME%\bin\set-jeyzer-params.bat" goto okSetJeyzerParams
echo Cannot find "%JEYZER_RECORDER_HOME%\bin\set-jeyzer-params.bat"
echo This file is needed to run this program
goto exit
:okSetJeyzerParams
call "%JEYZER_RECORDER_HOME%\bin\set-jeyzer-params.bat" %1
if errorlevel 1 goto exit

rem Ensure JAVA_HOME is set
if exist "%JEYZER_RECORDER_HOME%\bin\check-java.bat" goto okCheckJava
echo Cannot find "%JEYZER_RECORDER_HOME%\bin\check-java.bat"
echo This file is needed to run this program
goto exit
:okCheckJava
call "%JEYZER_RECORDER_HOME%\bin\check-java.bat"
if errorlevel 1 goto exit

rem The recorder log file
set LOG_FILE_SUFFIX=%JEYZER_TARGET_JMX_ADDRESS::=-%
set "JEYZER_RECORDER_LOG_FILE=%JEYZER_RECORDER_HOME%\log\jeyzer-recorder-jmx-%JEYZER_RECORD_PROFILE%-%LOG_FILE_SUFFIX%.log"

set "JEYZER_RECORD_PARAMS=-Djeyzer.record.config=%JEYZER_RECORD_CONFIG_DIR%\profiles\%JEYZER_RECORD_PROFILE%\%JEYZER_RECORD_PROFILE%_generation.xml"

rem commons-compress and threeten libraries
set "CLASSPATH=%JEYZER_RECORDER_HOME%\lib\commons-compress-${org.apache.commons.commons-compress.version}.jar;%JEYZER_RECORDER_HOME%\lib\threetenbp-${org.threeten.threetenbp.version}.jar"

rem jeyzer-publish library
set "CLASSPATH=%CLASSPATH%;%JEYZER_RECORDER_HOME%\lib\jeyzer-publish.jar"

rem jeyzer-logger library
set "CLASSPATH=%CLASSPATH%;%JEYZER_RECORDER_HOME%\lib\jeyzer-logger.jar"

rem jeyzer-recorder library
set "CLASSPATH=%CLASSPATH%;%JEYZER_RECORDER_HOME%\lib\jeyzer-recorder.jar"

rem JVM options
set JAVA_OPTS=-Xmn15m -Xms20m -Xmx20m

rem JMX options
rem set JMX_PORT=2500
rem set "JAVA_OPTS=%JAVA_OPTS% -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=%JMX_PORT% -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

rem Java debug options
rem set "JAVA_OPTS=%JAVA_OPTS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5000"

rem Recorder boot debug traces
rem set "JAVA_OPTS="$JAVA_OPTS" -Djeyzer.recorder.boot.debug=true"

call "%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -cp %CLASSPATH% %JEYZER_RECORD_PARAMS% org.jeyzer.recorder.JeyzerRecorder 
goto end

:exit
exit /b 1

:end
exit /b 0