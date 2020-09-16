
---------------------------------
>>                             <<
>>>     Jeyzer Recorder       <<<
>>>  Agent Scaling addendum   <<<
>>                             <<
---------------------------------

Prerequisites
Please read first the README.txt and the README-AGENT.txt to get the Jeyzer Recorder basics.


Introduction
This section covers the Jeyzer Agent Recorder deployment strategy in any type of environment (production, QA..)
Main goal is to scale the monitoring of your different Java applications in a standardized and centralized way.



-------------------------------------
===     Deployment principles     ===
-------------------------------------

Monitoring multiple Java applications requires some good Jeyzer agent setup practices.
First of all, those principles are applied :
 
 - Binaries must be shared
 
 - Configurations must be centralized and reused as much as possible
   Configurations should refer to - important - environment variables whenever possible.
 
 - Recordings should be centralized to simplify the life of the IT operators.  
   Note that you still have the choice to follow any alternative log strategy in place in your company.



-------------------------------------
===      Deployment locations     ===
-------------------------------------

Based on the above principles, 3 locations must be considered :

 1. The Jeyzer agent installation (jeyzer-recorder below)
    It contains all the Jeyzer binaries used to monitor any Java application.
    You need only 1 installation, whatever the number of Java applications to monitor.

 2. The Jeyzer recorder configuration repository (jeyzer-rec-configs below)
    It contains all the Jeyzer recording configurations.
    You need 1 Jeyzer recording configuration per application, but some parts get shared with others.

 3. The Jeyzer agent recording directories
    This is the place where where the Jeyzer agent will generate the recording snapshots and its own log file.
    Create a Jeyzer recording home directory for all your applications (jeyzer-recordings below)
    Inside it, you will need 1 Jeyzer recording directory for each application.
    PS : as an alternative, you can put Jeyzer recording directory inside your application log directory if you prefer.



-------------------------------------
===      Recording profile        ===
-------------------------------------

All monitored applications share initially the same standard recording profile. 
The recording profile contains the recording configuration and describes the data to retrieve.
See the standard_generation.xml and standard_advanced_mx.xml files.

At any time, a recording profile can be customized at application level (any-other-customized-profile below), 
for example to fetch specific applicative JMX data or to activate the recording encryption.
By default, it is set to "standard".
To setup and customize your own applicative profile, follow these steps :

	A. Duplicate the directory <jeyzer-rec-configs>/profiles/profile-template and rename it as <jeyzer-agent-profile>

	B. Rename the profile-template_generation.xml as <jeyzer-agent-profile>_generation.xml
	
	C. Rename the profile-template_advanced_mx.xml as <jeyzer-agent-profile>_advanced_mx.xml

	D. Edit the <jeyzer-rec-configs>/agent/<jeyzer-agent-profile>/jeyzer-record.properties 
	   to update this parameter  : 
	     jeyzer-record.JEYZER_RECORD_PROFILE=${JEYZER_RECORD_AGENT_PROFILE}
	   and this parameter :
	     jeyzer-record.JEYZER_RECORD_CONFIG_FILE=${JEYZER_RECORD_AGENT_PROFILE}_generation.xml

	E. Customize the <jeyzer-agent-profile>_generation.xml and the <jeyzer-agent-profile>_advanced_mx.xml
	   to adapt it to your needs. Files are self-documented.



-------------------------------------------
===      Recording Agent profile        ===
-------------------------------------------

This is bootstrap configuration for the Jeyzer agent known as <jeyzer-agent-profile> below.
It is composed of 2 files : 

 - jeyzer-agent.xml is generic 

 - jeyzer-record.properties is application specific
   You can define there variables which will be used in the recording profile
    
Recording Agent profile is set by the JEYZER_RECORD_AGENT_PROFILE. 
It is mandatory (see Instructions below).



-------------------------------------
===      File structure           ===
-------------------------------------

Your Jeyzer recording file structures should finally like the ones below.

 1. Jeyzer agent configuration repository :
  	
	<jeyzer-rec-configs>
		/agent
			jeyzer-agent.xml    (S)
			/application-1
				jeyzer-record.properties
			/application-2
				jeyzer-record.properties
			/<application-profile N>
				jeyzer-record.properties
			/application-template        (S)
				jeyzer-record.properties (S)
		/profiles
			/standard
				standard_generation.xml  (S)
				standard_advanced_mx.xml (S)
			/application-2-customized
				application-2-customized_generation.xml
				application-2-customized_advanced_mx.xml
			/<any-other-customized-profile>
				<any-other-customized-profile>_generation.xml
				<any-other-customized-profile>_advanced_mx.xml
			/profile-template
				profile-template_generation.xml  (S)
				profile-template_advanced_mx.xml (S)
		/log
			logback.xml  (S)

  (S) = default files provided in the scaling-template package. 


 2. Jeyzer recording home directory :
    
	<jeyzer-recordings>
		/application-1
			recording snapshots
			/archive
				zip or tar.gz recording archives
			/log
				jeyzer-application-1.log
		/application-2
			recording snapshots
			/archive
				zip or tar.gz recording archives
			/log
				jeyzer-application-2.log
		/<jeyzer-agent-profile-N>
			recording snapshots
			/archive
				zip or tar.gz recording archives
			/log
				jeyzer-<application-profile-N>.log

     <jeyzer-recordings> content is entirely dynamic : no need to create any directory/file.


------------------------------------------------
===        Deployment instructions           ===
------------------------------------------------

This is a 20mn to 1H exercise depending on the number of applications and needs.

1. Decide about the locations of the <jeyzer-rec-configs> and <jeyzer-recordings> directories.
   List the Java applications to monitor and assign to each one a distinct <jeyzer-agent-profile> name.
   Take note of the Jeyzer recorder installation path, referenced below as <jeyzer-recorder>.

2. Set the following global system environment variables :

	A. JEYZER_AGENT_HOME 
       It must point to the <jeyzer-recorder> directory.

	B. JEYZER_RECORD_APP_CONFIG_REPOSITORY
       It must point to the <jeyzer-rec-configs> directory.

	C. JEYZER_RECORD_APP_RECORDING_HOME
       It must point to the <jeyzer-recordings> directory.
  
3. Create the <jeyzer-rec-configs> repository :

	A. Copy the directory content of <jeyzer-recorder>/config/scaling-template/jeyzer-rec-configs
       under your <jeyzer-rec-configs> directory.

	B. Inside the <jeyzer-rec-configs>/agent directory, for each listed <jeyzer-agent-profile>, 
	   duplicate the application-template directory and rename it as <jeyzer-agent-profile>.

4. Edit each monitored application startup script :

	A. Set the JEYZER_RECORD_AGENT_PROFILE environment variable
       This corresponds to the <jeyzer-agent-profile>
       ex: JEYZER_RECORD_AGENT_PROFILE=application-1

	B. Add this parameter to your Java application command line :
       -javaagent:"%JEYZER_AGENT_HOME%/lib/jeyzer-agent-3.0.jar"=%JEYZER_RECORD_APP_CONFIG_REPOSITORY%/agent/jeyzer-agent.xml
       See the README-AGENT.txt if your application is running under Glassfish.

	C. Set the JEYZER_RECORDER_LOG_FILE system environment variable
       Value should be : JEYZER_RECORDER_LOG_FILE=%JEYZER_RECORD_APP_RECORDING_HOME%/%JEYZER_RECORD_AGENT_PROFILE%/log/jeyzer-recorder-agent.log

5. Optional recording customization, per monitored application :
  
    A. Profile customization : this is recommended if you need to fetch specific MX applicative data exposed by your application.
       See the Recording profile section for instructions.
       By default the standard profile is used and is sufficient.
    
    B. Adapt the recording parameters to your needs : sampling period, archiving period..
       Edit the <jeyzer-rec-configs>/agent/<jeyzer-agent-profile>/jeyzer-record.properties 
       You can use ${VARIABLE_NAME} to reference Java system properties and system environment variables (resolved in that order).



-------------------------------------------
===     Example : Jeyzer demos          ===
-------------------------------------------

Jeyzer demos do follow the same deployment approach : just look at it for real live example.

Check for example these Jeyzer demo locations :

   <jeyzer-rec-configs> : <jeyzer-home>/demo/config

   <jeyzer-recordings> (demo must have been started once to get it) : <jeyzer-home>/work/recordings



-------------------------------------------
===     Agent logging                   ===
-------------------------------------------

Jeyzer Recorder logging configuration is initially shared.
Jeyzer Recorder traces are generated in the file referenced by the JEYZER_RECORDER_LOG_FILE environment variable.
Log level of each appender (console and log file) is controlled through the <jeyzer-rec-configs>/log/backup.xml.
By default it is set to INFO.
Log configuration update requires to restart the target application.

In case you would need to specialize it per application : 
- Create the directory : <jeyzer-rec-configs>/log/<jeyzer-agent-profile>
- Copy the <jeyzer-rec-configs>/log/backup.xml file in this new directory
- Edit the <jeyzer-rec-configs>/agent/jeyzer-agent.xml to activate this class path entry :
    <entry>${jeyzer-rec-configs}/log/${jeyzer-record-agent-profile}</entry>
- Edit the <jeyzer-rec-configs>/log/<jeyzer-agent-profile>/backup.xml to adapt it to your needs
- Restart the monitored application

Read also the Logger configuration section in the README-AGENT.txt