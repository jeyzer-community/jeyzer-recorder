
echo off

echo .
echo ==========================================================================================
echo      jcmd periodic
echo ==========================================================================================
echo .

TITLE=Jcmd periodic

rem The process name obtained with jps
set TARGET_PROCESS=DemoVT21

rem json or txt
set JCMD_DUMP_FORMAT=txt



rem The Jeyzer Recorder home (parent directory)
set "CURRENT_DIR=%cd%"
cd ..
set "JEYZER_RECORDER_HOME=%cd%"
cd ..
set "JEYZER_HOME=%cd%"
cd "%CURRENT_DIR%"


rem Ensure JAVA_HOME is set
if exist "%JEYZER_RECORDER_HOME%\bin\check-java.bat" goto okCheckJava
echo Cannot find "%JEYZER_RECORDER_HOME%\bin\check-java.bat"
echo This file is needed to run this program
goto exit
:okCheckJava
call "%JEYZER_RECORDER_HOME%\bin\check-java.bat"
if errorlevel 1 goto exit

set DUMP_DIR=%JEYZER_HOME%\work\recordings\%TARGET_PROCESS%


mkdir %DUMP_DIR%

rem ------------------------------
rem Find pid
rem ------------------------------

:findPid

set APP=
set PID=
del %DUMP_DIR%\pid.txt

%JAVA_HOME%\bin\jps |Findstr %TARGET_PROCESS% > %DUMP_DIR%\pid.txt


for /f "tokens=2" %%i in (%DUMP_DIR%\pid.txt) do set APP=%%i
rem echo %APP%

for /f "tokens=1" %%i in (%DUMP_DIR%\pid.txt) do set PID=%%i
rem echo %PID%

if not "%PID%" == "" goto start

echo Application %TARGET_PROCESS% not found with jps. Will retry in 5 sec...
echo .

rem wait 5 seconds
timeout 5

goto findPid

rem ------------------------------
rem Dump loop
rem ------------------------------

:start

set datetimef=%date:~-4%-%date:~3,2%-%date:~0,2%-%time:~0,2%-%time:~3,2%-%time:~6,2%
set datetimef=%datetimef: =0%

rem relative to target/classes
set DUMP_FILE_PATH=%DUMP_DIR%\%APP%-%datetimef%.%JCMD_DUMP_FORMAT%

echo "%JAVA_HOME%\bin\jcmd.exe %PID% Thread.dump_to_file -format=%JCMD_DUMP_FORMAT% %DUMP_FILE_PATH%"
%JAVA_HOME%\bin\jcmd.exe %PID% Thread.dump_to_file -format=%JCMD_DUMP_FORMAT% %DUMP_FILE_PATH%

if errorlevel 1 goto catchError

rem wait 5 seconds
timeout 5

rem echo done

goto start


:catchError

rem process probably stopped
rem do not keep empty files - Not required with Thread.dump_to_file
rem del %DUMP_FILE_PATH%

rem wait 5 seconds
timeout 5

rem Process will be restarted with new pid
goto findPid


:exit

pause