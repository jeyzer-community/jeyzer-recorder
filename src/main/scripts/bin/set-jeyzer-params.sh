#!/bin/sh

JEYZER_INSTALLER_DEPLOYMENT=%{jeyzer.installer.deployment}
if [ "$JEYZER_INSTALLER_DEPLOYMENT" != "true" ]; then
  # ---------------------------------------------------------------------------------------------------------------
  # Set Jeyzer Recorder parameters
  # All parameters below are intended be set externally 
  #    once the Recorder gets integrated in any DevOps platform
  # Edit this section if installation was done manually (no installer)
  # ---------------------------------------------------------------------------------------------------------------

  # Jeyzer target profile : your_analysis_profile, demo-features, demo-features-mx..
  #    only used at this stage to determine the final recording sub-directory (below)
  #    Could be accessed by the Jeyzer Monitor so the best is to make it match a real profile.
  if [ -z "$JEYZER_TARGET_PROFILE" ]; then
    JEYZER_TARGET_PROFILE=demo-features-mx
    export JEYZER_TARGET_PROFILE 
  fi
  
  # Jeyzer target profile : standard, amq, tomcat, demo-features..
  if [ -z "$JEYZER_RECORD_PROFILE" ]; then
    JEYZER_RECORD_PROFILE=demo-features
    export JEYZER_RECORD_PROFILE 
  fi

  # The recording directory
  if [ -z "$JEYZER_RECORD_DIRECTORY" ]; then
    JEYZER_RECORD_DIRECTORY="$JEYZER_RECORDER_HOME/recordings/$JEYZER_TARGET_PROFILE"
	export JEYZER_RECORD_DIRECTORY
  fi

  # The recording archive directory 
  if [ -z "$JEYZER_RECORD_ARCHIVE_DIR" ]; then
    JEYZER_RECORD_ARCHIVE_DIR="$JEYZER_RECORD_DIRECTORY/archive"
    export JEYZER_RECORD_ARCHIVE_DIR
  fi
  
  # The recording period
  if [ -z "$JEYZER_RECORD_PERIOD" ]; then
    JEYZER_RECORD_PERIOD=30s
    export JEYZER_RECORD_PERIOD
  fi
  
  # The recording archiving period
  if [ -z "$JEYZER_RECORD_ARCHIVE_PERIOD" ]; then
    JEYZER_RECORD_ARCHIVE_PERIOD=3m
    export JEYZER_RECORD_ARCHIVE_PERIOD
  fi

  # The recording archive file number retention size. Archive files beyond the limit (so old files) are automatically deleted. 
  if [ -z "$JEYZER_RECORD_ARCHIVE_FILE_LIMIT" ]; then
    JEYZER_RECORD_ARCHIVE_FILE_LIMIT=10
    export JEYZER_RECORD_ARCHIVE_FILE_LIMIT
  fi

  # Check the input for the JMX methods
  if [ "$JEYZER_RECORD_DUMP_METHOD" == 'AdvancedJMX' ] || [ "$JEYZER_RECORD_DUMP_METHOD" == 'JMX' ]; then
    if [ -z "$1" ]; then
	  echo "Invalid command"
	  echo "Usage : jeyzer-recorder-jmx.sh <JMX host>:<JMX port>"
	  exit 1
	else
	  JEYZER_TARGET_JMX_ADDRESS=$1
	  export JEYZER_TARGET_JMX_ADDRESS
    fi
  fi
  
else
  
  # ---------------------------------------------------------------------------------------------------------------
  # Jeyzer Recorder paths automatically set  
  # All parameters below are intended be set externally 
  #    once the Recorder gets integrated in any DevOps platform
  # Feel free to adjust manually or re-run the installer
  # ---------------------------------------------------------------------------------------------------------------

  # Jeyzer target profile : your_analysis_profile, demo-features, demo-features-mx..
  #    only used at this stage to determine the final recording sub-directory (below)
  #    Could be accessed by the Jeyzer Monitor so the best is to make it match a real profile.
  if [ -z "$JEYZER_TARGET_PROFILE" ]; then
    JEYZER_TARGET_PROFILE=%{jeyzer.recorder.target.profile}
    export JEYZER_TARGET_PROFILE 
  fi
  
  # Jeyzer target profile : standard, amq, tomcat, demo-features..
  if [ -z "$JEYZER_RECORD_PROFILE" ]; then
    JEYZER_RECORD_PROFILE=demo-features
    export JEYZER_RECORD_PROFILE 
  fi
  
  # The recording directory
  if [ -z "$JEYZER_RECORD_DIRECTORY" ]; then
    JEYZER_RECORD_DIRECTORY="%{jeyzer.recorder.work.dir}/$JEYZER_TARGET_PROFILE"
	export JEYZER_RECORD_DIRECTORY
  fi

  # The recording archive directory 
  if [ -z "$JEYZER_RECORD_ARCHIVE_DIR" ]; then
    JEYZER_RECORD_ARCHIVE_DIR="$JEYZER_RECORD_DIRECTORY/archive"
    export JEYZER_RECORD_ARCHIVE_DIR
  fi
  
  # Recording period
  if [ -z "$JEYZER_RECORD_PERIOD" ]; then
    JEYZER_RECORD_PERIOD=%{jeyzer.recorder.period}
    export JEYZER_RECORD_PERIOD
  fi
  
  # Recording archiving period
  if [ -z "$JEYZER_RECORD_ARCHIVE_PERIOD" ]; then
    JEYZER_RECORD_ARCHIVE_PERIOD=%{jeyzer.recorder.archiving.period}
    export JEYZER_RECORD_ARCHIVE_PERIOD
  fi

  # Recording archive file number retention size. Archive files beyond the limit (so old files) are automatically deleted. 
  if [ -z "$JEYZER_RECORD_ARCHIVE_FILE_LIMIT" ]; then
    JEYZER_RECORD_ARCHIVE_FILE_LIMIT=%{jeyzer.recorder.archiving.retention}
    export JEYZER_RECORD_ARCHIVE_FILE_LIMIT
  fi

  # Target process JMX host and port
  if [ "$JEYZER_RECORD_DUMP_METHOD" == 'AdvancedJMX' ] || [ "$JEYZER_RECORD_DUMP_METHOD" == 'JMX' ]; then
    if [ -z "$JEYZER_TARGET_JMX_ADDRESS" ]; then
	  JEYZER_TARGET_JMX_ADDRESS=%{jeyzer.recorder.jmx.host}:%{jeyzer.recorder.jmx.port}
	  export JEYZER_TARGET_JMX_ADDRESS
    fi
  fi
  
fi
