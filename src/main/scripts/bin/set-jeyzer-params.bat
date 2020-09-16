@echo off

rem If Jeyzer installation result, paths get set automatically
set JEYZER_INSTALLER_DEPLOYMENT=${jeyzer.installer.deployment}
if "%JEYZER_INSTALLER_DEPLOYMENT%" == "true" goto gotPathsSetByInstaller

rem ---------------------------------------------------------------------------------------------------------------
rem Set Jeyzer Recorder parameters
rem All parameters below are intended be set externally 
rem    once the Recorder gets integrated in any DevOps platform
rem Edit this section if installation was done manually (no installer)
rem ---------------------------------------------------------------------------------------------------------------

rem Target profile : your_analysis_profile, demo-features, demo-features-mx..
rem   only used at this stage to determine the final recording sub-directory (below)
rem   Could be accessed by the Jeyzer Monitor so the best is to make it match a real profile.
if not "%JEYZER_TARGET_PROFILE%" == "" goto gotTargetProfile
set JEYZER_TARGET_PROFILE=demo-features-mx
:gotTargetProfile

rem Recording profile : standard, amq, tomcat, demo-features..
if not "%JEYZER_RECORD_PROFILE%" == "" goto gotRecordProfile
set JEYZER_RECORD_PROFILE=standard
:gotRecordProfile

rem The recording directory
if not "%JEYZER_RECORD_DIRECTORY%" == "" goto gotRecordDir
set "JEYZER_RECORD_DIRECTORY=%JEYZER_RECORDER_HOME%/recordings/%JEYZER_TARGET_PROFILE%"
:gotRecordDir

rem The recording archive directory
if not "%JEYZER_RECORD_ARCHIVE_DIR%" == "" goto gotRecordArchiveDir
set "JEYZER_RECORD_ARCHIVE_DIR=%JEYZER_RECORD_DIRECTORY%/archive"
:gotRecordArchiveDir

rem The recording generation period
if not "%JEYZER_RECORD_PERIOD%" == "" goto gotRecordPeriod
set JEYZER_RECORD_PERIOD=30s
:gotRecordPeriod

rem The recording archiving period
if not "%JEYZER_RECORD_ARCHIVE_PERIOD%" == "" goto gotRecordArchivePeriod
set JEYZER_RECORD_ARCHIVE_PERIOD=5m
:gotRecordArchivePeriod

rem The recording archive file number retention size. Archive files beyond the limit (so old files) are automatically deleted.
if not "%JEYZER_RECORD_ARCHIVE_FILE_LIMIT%" == "" goto gotRecordArchiveFileLimit
set JEYZER_RECORD_ARCHIVE_FILE_LIMIT=10
:gotRecordArchiveFileLimit

rem Check the input for the JMX methods
if "%JEYZER_RECORD_DUMP_METHOD%" == "Jstack" goto okInput
if "%JEYZER_RECORD_DUMP_METHOD%" == "JstackInShell" goto okInput
if not "%1" == "" goto okInput
echo Invalid command 
echo Usage : jeyzer-recorder-jmx.bat ^<JMX host^>:^<JMX port^>
goto exit
:okInput

if "%JEYZER_RECORD_DUMP_METHOD%" == "Jstack" goto okParams
if "%JEYZER_RECORD_DUMP_METHOD%" == "JstackInShell" goto okParams
rem Target process JMX host and port
set JEYZER_TARGET_JMX_ADDRESS=%1

:okParams

goto end

rem ---------------------------------------------------------------------------------------------------------------
rem Jeyzer Recorder paths automatically set  
rem All parameters below are intended be set externally 
rem    once the Recorder gets integrated in any DevOps platform
rem Feel free to adjust manually or re-run the installer
rem ---------------------------------------------------------------------------------------------------------------

:gotPathsSetByInstaller

rem Target profile : your_analysis_profile, demo-features, demo-features-mx..
rem   only used at this stage to determine the final recording sub-directory (below)
rem   Could be accessed by the Jeyzer Monitor so the best is to make it match a real profile.
if not "%JEYZER_TARGET_PROFILE%" == "" goto gotTargetProfile
set JEYZER_TARGET_PROFILE=${jeyzer.recorder.target.profile}
:gotTargetProfile

rem Recording profile : standard, amq, tomcat, demo-features..
if not "%JEYZER_RECORD_PROFILE%" == "" goto gotRecordProfile
set JEYZER_RECORD_PROFILE=standard
:gotRecordProfile

rem The recording directory
if not "%JEYZER_RECORD_DIRECTORY%" == "" goto gotRecordDir
set JEYZER_RECORD_DIRECTORY=${jeyzer.recorder.work.dir}/%JEYZER_TARGET_PROFILE%
:gotRecordDir

rem The recording archive directory
if not "%JEYZER_RECORD_ARCHIVE_DIR%" == "" goto gotRecordArchiveDir
set JEYZER_RECORD_ARCHIVE_DIR=%JEYZER_RECORD_DIRECTORY%/archive
:gotRecordArchiveDir

rem The recording generation period
if not "%JEYZER_RECORD_PERIOD%" == "" goto gotRecordPeriod
set JEYZER_RECORD_PERIOD=${jeyzer.recorder.period}
:gotRecordPeriod

rem The recording archiving period
if not "%JEYZER_RECORD_ARCHIVE_PERIOD%" == "" goto gotRecordArchivePeriod
set JEYZER_RECORD_ARCHIVE_PERIOD=${jeyzer.recorder.archiving.period}
:gotRecordArchivePeriod

rem The recording archive file number retention size. Archive files beyond the limit (so old files) are automatically deleted.
if not "%JEYZER_RECORD_ARCHIVE_FILE_LIMIT%" == "" goto gotRecordArchiveFileLimit
set JEYZER_RECORD_ARCHIVE_FILE_LIMIT=${jeyzer.recorder.archiving.retention}
:gotRecordArchiveFileLimit

if "%JEYZER_RECORD_DUMP_METHOD%" == "Jstack" goto okParams
if "%JEYZER_RECORD_DUMP_METHOD%" == "JstackInShell" goto okParams
rem Target process JMX host and port
if not "%JEYZER_TARGET_JMX_ADDRESS%" == "" goto gotRecordTargetJmxAddress
set JEYZER_TARGET_JMX_ADDRESS=${jeyzer.recorder.jmx.host}:${jeyzer.recorder.jmx.port}
:gotRecordTargetJmxAddress

:okParams

goto end

:exit
exit /b 1

:end
exit /b 0
