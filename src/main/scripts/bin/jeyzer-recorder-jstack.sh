#!/bin/sh

echo -------------------------------------
echo Jeyzer Recorder Jstack client
echo -------------------------------------

if [ -z "$1" ]; then
	echo "Invalid command"
	echo "Usage : $0 <pid>"
	exit 1
fi

# -----------------------------------------------------------
# Jeyzer Recorder parameters
# See also the set-jeyzer-params.sh
# Some parameters below are intended be set externally 
#    once the Recorder gets integrated in any DevOps platform
# -----------------------------------------------------------

# The recorder profile
if [ -z "$JEYZER_RECORD_PROFILE" ]; then
  JEYZER_RECORD_PROFILE=standard
  export JEYZER_RECORD_PROFILE
fi

# Process card : dump process info at startup
JEYZER_RECORD_PROCESS_CARD_ENABLED=true
export JEYZER_RECORD_PROCESS_CARD_ENABLED

# Generate global data dump capture time 
JEYZER_RECORD_CAPTURE_DURATION=true
export JEYZER_RECORD_CAPTURE_DURATION

# Target process pid 
JEYZER_TARGET_PID=$1
export JEYZER_TARGET_PID

# -----------------------------------------------------------
# Jeyzer Recorder Archiving parameters (zip or tar.gz)
# -----------------------------------------------------------

# The recording archiving offset. 
# Offset used to define the end limit of the archiving time slot.
# Must at least be be multiple of the recording period.
# If Jeyzer monitoring is enabled, must be higher than the scanning period.
JEYZER_RECORD_ARCHIVE_TIME_OFFSET=25s
export JEYZER_RECORD_ARCHIVE_TIME_OFFSET


# -----------------------------------------------------------
# Internals - do not edit
# -----------------------------------------------------------

# Dump access method (Jstack or JstackInShell)
JEYZER_RECORD_DUMP_METHOD=Jstack
export JEYZER_RECORD_DUMP_METHOD

# Dump start delay
JEYZER_RECORD_START_DELAY=0s
export JEYZER_RECORD_START_DELAY

# Jeyzer Recorder home (parent directory)
# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# set JEYZER_RECORDER_HOME
[ -z "$JEYZER_RECORDER_HOME" ] && JEYZER_RECORDER_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

# The Jeyzer Recorder configuration directory
JEYZER_RECORD_CONFIG_DIR="$JEYZER_RECORDER_HOME"/config
export JEYZER_RECORD_CONFIG_DIR

# Ensure Jeyzer parameters are set
if [ -r "$JEYZER_RECORDER_HOME"/bin/set-jeyzer-params.sh ]; then
  . "$JEYZER_RECORDER_HOME"/bin/set-jeyzer-params.sh $1
else
  echo "Cannot find $JEYZER_RECORDER_HOME/bin/set-jeyzer-params.sh"
  echo "This file is needed to run this program"
  exit 1
fi

# Ensure JAVA_HOME is set
if [ -r "$JEYZER_RECORDER_HOME"/bin/check-java.sh ]; then
  . "$JEYZER_RECORDER_HOME"/bin/check-java.sh
else
  echo "Cannot find $JEYZER_RECORDER_HOME/bin/check-java.sh"
  echo "This file is needed to run this program"
  exit 1
fi

# The recorder log file
JEYZER_RECORDER_LOG_FILE="$JEYZER_RECORDER_HOME/log/jeyzer-recorder-jstack-"$JEYZER_RECORD_PROFILE"-"$JEYZER_TARGET_PID".log"
export JEYZER_RECORDER_LOG_FILE

JEYZER_RECORD_PARAMS=-Djeyzer.record.config="$JEYZER_RECORD_CONFIG_DIR"/profiles/"$JEYZER_RECORD_PROFILE"/"$JEYZER_RECORD_PROFILE"_generation.xml
export JEYZER_RECORD_PARAMS

# logging + commons-compress libraries
CLASSPATH="$JEYZER_RECORDER_HOME/lib/slf4j-api-${slf4j-api.version}.jar:$JEYZER_RECORDER_HOME/lib/logback-core-${logback-core.version}.jar:$JEYZER_RECORDER_HOME/lib/logback-classic-${ch.qos.logback.logback-classic.version}.jar:$JEYZER_RECORDER_HOME/lib/commons-compress-${org.apache.commons.commons-compress.version}.jar"

# jeyzer-publish library
CLASSPATH=""$CLASSPATH":$JEYZER_RECORDER_HOME/lib/jeyzer-publish.jar"

# jeyzer-recorder library and logback config directory
CLASSPATH=""$CLASSPATH":$JEYZER_RECORDER_HOME/lib/jeyzer-recorder.jar:$JEYZER_RECORDER_HOME/config"
export CLASSPATH

# JVM options
JAVA_OPTS="-Xmn15m -Xms20m -Xmx20m"
export JAVA_OPTS

# JMX options
# JMX_PORT=2500
# JAVA_OPTS="$JAVA_OPTS" -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port="$JMX_PORT" -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false

# Java debug options
# JAVA_OPTS="$JAVA_OPTS" -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5000

"$JAVA_HOME"/bin/java $JAVA_OPTS -cp $CLASSPATH $JEYZER_RECORD_PARAMS org.jeyzer.recorder.JeyzerRecorder