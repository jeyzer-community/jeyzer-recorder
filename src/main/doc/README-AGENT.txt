
---------------------------------
>>                             <<
>>>     Jeyzer Recorder       <<<
>>>      Agent addendum       <<<
>>                             <<
---------------------------------


The Jeyzer Recorder agent is a Jeyzer Recorder which is started within the monitored process. 
For the Jeyzer Recorder features, see its related README.txt.
It requires Java 7 or above.



-----------------------------------------
===       Agent fast activation       ===
-----------------------------------------

To use the Jeyzer Agent recorder, follow these steps :

 1. Edit the start script of the process to monitor

    A. Insert the following parameter as part of the startup command line :
	   -javaagent:"<Jeyzer Recorder home>/lib/jeyzer-agent-3.0.jar"=<Jeyzer Recorder home>/config/agent/jeyzer-agent.xml

    B. Set the JEYZER_AGENT_HOME system environment variable 
    or update the jeyzer-agent-home variable in the <Jeyzer Recorder home>/config/agent/jeyzer-agent.xml file.

    C. Set the JEYZER_RECORDER_LOG_FILE system environment variable
    Recommended value is : JEYZER_RECORDER_LOG_FILE=%JEYZER_AGENT_HOME%/log/my-monitored-process/jeyzer-recorder-agent.log

 2. Edit the file : <Jeyzer Recorder home>/config/agent/jeyzer-record.properties
  
    A. Set the jeyzer-record.JEYZER_RECORD_DIRECTORY and jeyzer-record.JEYZER_RECORD_ARCHIVE_DIR properties
    
    B. Adapt the recording parameters to your needs : sampling period, archiving period..

Once you have successfully tested the Jeyzer Agent Recorder, next step is to industrialize its usage : see the README-AGENT-SCALING.txt.


-------------------------------------
===  Agent default configuration  ===
-------------------------------------

Snapshots are by default generated in the Jeyzer working directory (under the recordings/<profile> directory).
Snapshots get compressed and archived in the recordings/<profile> directory every 12 hours.
Archive retention is 2 days (4 zip/gzip files kept).

Edit the jeyzer-record.properties to change any of these settings.


-------------------------------------
===    Logger configuration       ===
-------------------------------------

The logger configuration and log file locations are automatically set with those default locations :
 - Log file             :  <Jeyzer Recorder home>/log/jeyzer-recorder.log.*  (file rotation)
 - Logger configuration :  <Jeyzer Recorder home>/config/log/jeyzer-log.properties
See the Jeyzer Recorder logging section in the README.txt for more details.


------------------------------------------
===  Agent deployment under Glassfish  ===
------------------------------------------

The Jeyzer agent must be declared in the Glassfish configuration file as below.
Glassfish configuration file can be edited manually or through the Glassfish admin console.
In case domain.xml is generated, update the appropriate asadmin command script.

Following entry must be added in the process domain.xml: 
	<jvm-options>-javaagent:/<Jeyzer Recorder home>/lib/jeyzer-agent-3.0.jar=/<Jeyzer Recorder home>/config/agent/jeyzer-agent.xml</jvm-options>
Do not put path between ""


-------------------------
===  Troubleshooting  ===
-------------------------

If on console, process cannot start with error:
	Error opening zip file or JAR manifest missing : "/opt/jeyzer/recorder/lib/jeyzer-agent-3.0.jar"
It means that the agent path is invalid or not accessible.

The Jeyzer logging bootstrap can be troubleshooted by adding the -Djeyzer.recorder.boot.debug=true start parameter : detailed bootstrap traces will appear in the console.