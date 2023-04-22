
---------------------------------------
>>                                   <<
>>>     Jeyzer Recorder Agent       <<<
>>                                   <<
---------------------------------------


The Jeyzer Recorder agent is a Jeyzer Recorder which is started within the monitored process. 
For the Jeyzer Recorder features, see its related README.txt.
It requires Java 7 or above.


--------------------------------
===     Immediate usage      ===
--------------------------------

Prerequisite : you installed the Jeyzer Recorder Agent with the Jeyzer Recorder Installer or the Jeyzer Ecosystem Installer

- Add the following VM parameter to your Java application command line (replace the <> as indicated) : 
   -javaagent:"<Jeyzer home>/recorder/lib/jeyzer-agent.jar"=<Jeyzer home>/recorder/config/agent/jeyzer-agent.xml;jeyzer-record-agent-profile=<app name>
     <Jeyzer home> is the Jeyzer installation path
     <app name> is the application name you provided at installation time
- Start your application. Let it run for some time 
- Collect the JZR recording in the <Jeyzer home>/work/recordings/<app name>/archive and analyze it with the Jeyzer Analyzer
- For real time analysis, use the Jeyzer Monitor and make it point to the application recording directory


--------------------------------
===   Scaling the Recorder   ===
--------------------------------

Scaling means applying the Jeyzer Recorder Agent to multiple Java applications.

Idea to isolate the binaries from the recorder configuration and recordings.
Each monitored application will therefore get its Jeyzer Recorder configuration, logging and recording directories 
under these 2 central places :

- Recorder configuration repository (known as "scaling" configuration)
  It is by default deployed under the <Jeyzer home>/recorder/config/scaling directory.
  You can change its location by setting the JEYZER_RECORD_APP_CONFIG_REPOSITORY environment variable 
    or the -DJEYZER_RECORD_APP_CONFIG_REPOSITORY system property (to add on the application command line)
    or by updating the agent configuration stored in <Jeyzer home>/recorder/config/agent/jeyzer-agent.xml
  It is referred hereafter as <Configuration repository>

- Recording home 
  It is by default deployed under the <Jeyzer home>/work/recordings directory
  You can change its location by setting the JEYZER_RECORD_APP_RECORDING_HOME environment variable 
    or the -DJEYZER_RECORD_APP_RECORDING_HOME system property (to add on the application command line)
    or by updating the agent configuration stored in <Jeyzer home>/recorder/config/agent/jeyzer-agent.xml
  All directories get created dynamically.
  It is referred hereafter as <Recording home>


------------------------------------------
===   Monitor a new Java application   ===
------------------------------------------

There are 2 ways to apply the Jeyzer Recorder Agent on any Java application :

1- Jeyzer Recorder Installer : run it over the current Jeyzer installation.
  In the component panel, select only the "Jeyzer Recorder Agent"
  In the Jeyzer Recorder Agent panel, set the configuration home to the right location (<Configuration repository>).
  In the Recording Home panel, set the "Application name" of the new application to monitor, referred herafter as <app name>
  and set the recording home to the right location (<Recording home>).
  Important : you must use the Jeyzer Recorder Installer that created the current installation.

2- Manual : in the Recorder configuration repository - under the log and agent directories only - 
  duplicate the *-template directories and rename it with the desired application name, referred herafter as <app name>.

Then :

- Add the following VM parameter to your Java application command line (replace the <>) : 
   -javaagent:"<Jeyzer home>/recorder/lib/jeyzer-agent.jar"=<Jeyzer home>/recorder/config/agent/jeyzer-agent.xml;jeyzer-record-agent-profile=<app name>
  Note: you can remove the jeyzer-record-agent-profile parameter and rely instead on the JEYZER_RECORD_AGENT_PROFILE environment variable or system property.

- Start your application and check that the recording directory gets created and populated.
  Recording directory is located in the <Recording home>/<app name> directory.
  By default the <Recording home> is the <Jeyzer home>/work/recordings directory.


-------------------------------------
===  Agent default configuration  ===
-------------------------------------

Default configuration is adapted for production environments.
Snapshots are by default generated in the Jeyzer working directory (see <Jeyzer recording home>/<application name>) every 30 seconds.
Snapshots get compressed and archived every 6 hours in the <Jeyzer recording home>/<application name>/archive directory.
Archive retention is 5 days (20 zip/gzip files kept).

Edit the jeyzer-record.properties to change any of these settings.


-------------------------------------
===     Agent configuration       ===
-------------------------------------

2 levels of configuration are available :

- Recording parameters
  Parameters are set in the <Configuration repository>/agent/<application name>/jeyzer-record.properties
  You can define there many high level parameters like the recording period, the archive retention time and coverage.
  It defines also the Recorded data profile, by default set to "standard".

- Recording data profile
  The recording data profile defines the list of data to collect : CPU, memory, GC figures, process data, MX data, disk info, events..
  The recording data profile is set in the <Configuration repository>/profiles/<profile name> directory.
  By default, the <profile name> is standard.
  If you need to update this configuration, for example to add MX figures to collect, duplicate instead the profile-template direcory and rename it 
  with a proper profile name (which could be the <application name>). Rename also its files as <profile name>_generation.xml and <profile name>_advanced_mx.xml.
  Once done, update the jeyzer-record.JEYZER_RECORD_PROFILE value in the <Configuration repository>/agent/<application name>/jeyzer-record.properties.
  Then apply you changes as required (specific MX figures, recording encryption, extra disk space monitoring..)

Restart is required to load a new configuration.

Note that the <Jeyzer home>/recorder/config/agent/jeyzer-agent.xml file is generic and unique. 
  It is the Jeyzer Recorder Agent entry point (specified on the command line). 
  You should not modify it as it will be overriden on every new installation.


-------------------------------------
===    Logger configuration       ===
-------------------------------------

The logger configuration and log file locations are available in these locations :
 - Log file             :  <Recording home>/<application name>/log/jeyzer-recorder.log.0  (file rotation)
 - Logger configuration :  <Configuration repository>/log/<application name>/jeyzer-log.properties
See the Jeyzer Recorder logging section in the README.txt for more details.


------------------------------------------
===  Agent deployment under Glassfish  ===
------------------------------------------

The Jeyzer agent must be declared in the Glassfish configuration file as below.
Glassfish configuration file can be edited manually or through the Glassfish admin console.
In case domain.xml is generated, update the appropriate asadmin command script.

Following entry must be added in the process domain.xml: 
	<jvm-options>-javaagent:/<Jeyzer Recorder home>/lib/jeyzer-agent.jar=/<Jeyzer Recorder home>/config/agent/jeyzer-agent.xml</jvm-options>
Do not put path between ""


------------------------------------------
===  Agent deployment under JBoss      ===
------------------------------------------

The jeyzer-record-agent-profile parameter specified in the javaagent parameter may not be accepted by the JBoss startup script.
In such case, use only the following VM parameter on your Java application command line :
    -javaagent:"<Jeyzer home>/recorder/lib/jeyzer-agent.jar"=<Jeyzer home>/recorder/config/agent/jeyzer-agent.xml
and set the agent profile as a system environment variable through the JEYZER_RECORD_AGENT_PROFILE variable :
    JEYZER_RECORD_AGENT_PROFILE=<app name>


-------------------------
===     Upgrade       ===
-------------------------

By isolating the Recorder binaries from the recording home and configuration repositories, it is easy to upgrade the Jeyzer Recorder.
The best is to backup and rename the <Jeyzer home> directory and install a new version at the same place using the Jeyzer Recorder installer.
The Jeyzer configurations are backward compatible unless indicated.


--------------------------------
===     Jeyzer demos         ===
--------------------------------

The optional Jeyzer demos do not always follow the same setup approach and are highly customized.
As such, it is not the best setup example.
Check these Jeyzer demo locations :

   <Configuration repository> : <Jeyzer home>/demo/config/<demo application>
   <Recording home> :           <Jeyzer home>/work/recordings/<demo application>

The logging is forced through the JEYZER_RECORDER_LOG_FILE environment variable.
The jeyzer-agent.xml is also different and customized.


-------------------------
===  Troubleshooting  ===
-------------------------

First is to look at the jeyzer-log.properties log file.
By default, it contains INFO and ERROR traces.

Jeyzer troubleshooting is possible by activating these traces :

- Jeyzer agent bootstrap traces
  Add the -Djeyzer.agent.boot.debug=true parameter to your application command line.
  Traces will appear on the console.

- Jeyzer logging bootstrap traces
  Add the -Djeyzer.recorder.boot.debug=true parameter to your application command line.
  Traces will appear on the console.

- Jeyzer debug traces
  Edit the jeyzer-log.properties of your application
  Set the jeyzer.recorder.log.level to DEBUG
  Debug traces will appear in the Jeyzer Recorder log file
  To get the traces on the console, set the jeyzer.recorder.log.console.active to true
  
If on console, process cannot start with error:
	Error opening zip file or JAR manifest missing : "/opt/jeyzer/recorder/lib/jeyzer-agent.jar"
It means that the agent path is invalid or not accessible.