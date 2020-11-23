
---------------------------------
>>                             <<
>>>      Jeyzer Recorder      <<<
>>                             <<
---------------------------------

Jeyzer Recorder is initially a light weight Java thread dump collector. 
Its advanced versions permit to get process and system information as well as applicative data and events.

Thread dumps are helpful for production monitoring, customer support and R&D services to get snapshots of server activities.
A sequence of thread dumps will for example permit to identify performance bottlenecks, contention points, deadlocks 
and even memory outage problems or concurrency issues. In a more advanced manner, when executed over long time period, 
it permits to collect statistics on the server usage and establish application profiling. 

To skip the tedious and quite technical analysis of the thread dumps, use the Jeyzer Analyzer to generate JZR reports. 
JZR reports will show the Java process activity in a comprehensive and human readable manner, highlighting any technical problem or performance issue 
as well as providing statistics and profiling figures and charts. JZR reports are Excel files.

It requires Java 7 or above.

For more info, please refer to :
 https://jeyzer.org/jeyzer-recorder
Documentation :
 https://jeyzer.org/docs/jzr-recording/



--------------------------------
===  Recording generation  ===
--------------------------------

Jeyzer Recorder can generates periodically recording snapshots through one of these 3 ways :

 a) Agent : started within the monitored process, the Jeyzer Recorder agent will access the Java MX Management interface to collect a data snapshot.
    Like for the JMX solution, the agent enables to collect Jeyzer MX publisher metrics (data and events), standard MX bean metrics and Java MX Platform figures.
    This is the recommended approach : please read the README-AGENT.txt. This doc contains also instructions to monitor multiple Java applications.

 b) JMX : based on the java process JMX connection details, the Jeyzer Recorder will access the JVM JMX API to collect a data snapshot.
    Go to the <recorder home>/bin directory. 
	Usage : >jeyzer-recorder-jmx.bat|.sh <jmx host>:<jmx port>
	Optionally, the JMX user and password as well as the SSL certificate configuration can be set up within the startup script.
	AdvancedMX enables to collect more info like Java MX Platform figures such as CPU, memory and garbage collector.
	AdvancedMX enables also to collect metrics and events published through the Jeyzer MX publisher or through standard MX beans (metrics only). 

 c) Jstack : based on the java process pid, the Jeyzer Recorder is calling the JDK jstack tool and redirect its output.
    Go to the <recorder home>/bin directory. 
	Usage : >jeyzer-recorder-jstack.bat|.sh <pid>

Recording snapshot collection is not invasive for the the monitored Java process 
as it takes usually less than 10 ms (according to the GC pause logs).
To evaluate the impact, activate the capture time printing in the recording snapshots 
(which is a good performance indicator of the monitored process health).  

Generation period is configurable. It is set by default to 30 seconds.
For a production environment, it is recommended to set it to 30-60 seconds. 
To track concurrency issues, you may set it down to a few seconds.

Each recording snapshot will be generated in a dedicated file with the following naming convention : 
 snap-<time zone origin>-<date>---<time>-<time zone>.jzr
with <time> set with millisecond precision.
In case the server is not reachable, the file may be either absent or empty.

Jeyzer configuration uses the ISO-8601 time format. The PT prefix is optional. ex: period=30s, time=3m30s, time_to_live=10h


--------------------------------
==== Recording encryption  ====
--------------------------------

To secure exposed sensitive data exposed in the advanced mode, the Jeyzer recording can be encrypted.
This applies only for the Jeyzer agent and the Jeyzer recorder when started in the AdvancedMX method.
The security must be enabled in the mx_advanced configuration.
Encryption is AES 128 based. AES key is itself secured through RSA.

Recording encryption can be achieved in 2 ways:

 a) AES key is generated at recording time and stored within the Jeyzer recording (jzr-recording.key file).
    The RSA public key used to encrypt the AES key is stored locally in the master-public.key file
    The Jeyzer analyzer must contain the RSA private key to decrypt it.
    This makes the recording side fully secured, although the transport is slightly exposed (as encrypted key gets transmitted).
    The Jeyzer password manager permits to generate the RSA public key.

 b) Encrypted AES key is kept locally.
    The AES key is already encrypted at installation time and deployed on both Jeyzer analyzer and recording sides.
    Each side owns a Jeyzer RSA public key which permits to decrypt the AES key.
    This makes the transport fully secured, although the recording side is slightly exposed.
    The Jeyzer password manager permits to generate the encrypted AES key.


-------------------------------
==== Recording archiving ====
-------------------------------

To provide a consistent JZR recording packaging and reduce the disk space footprint of the recording snapshots : 

 1) Periodically, the Jeyzer Recorder automatically cleans the oldest recording snapshots after archiving it into a JZR recording file. 
    This JZR recording either a zip file on Windows or a tar.gz file on Unix.
    It is generated under a configurable archive directory. By default, the archive directory is located under the recording one. 
    Clean-up and archiving period is configurable. By default files are archived every 5 minutes. 
    For a production environment, it is recommended to set it to a period between 4 and 12 hours.
	Zip file will be named as follow : <archive_prefix>-<time zone origin>-<start date>---<start time>---<end date>---<end time>.zip
	The archive_prefix is configurable and usually set to jzr-rec-${JEYZER_RECORD_PROFILE}-

 2) Rolling archive mechanism applies on the JZR recordings : the Jeyzer Recorder deletes automatically the oldest JZR recording 
    when the number of those exceeds a certain configurable threshold.
    By default, the last 10 JZR recordings are kept.
	For a production environment, Jeyzer Recorder should be configured to keep the last few days of activity.

 Archiving can be enabled at process shutdown. 
 It is disabled by default to catch up the restarts upon Jeyzer analysis. 
 Points 1 and 2 are applied.

	
--------------------------------
===  Advanced configuration  ===
--------------------------------
   
a) Jeyzer Recorder configuration file
   Configuration file config/profiles/<profile>/<profile>_generation.xml permits to set standard and advanced parameters.
   Standard parameter default values are referring to environments variables, defined usually in the startup scripts.
   Profile is defined as well in the startup script through the JEYZER_RECORD_PROFILE environment variable.

b) Jeyzer Recorder memory pool configuration
   Memory pool configuration defines the set of memory pool figures to collect.
   Feature is available when the AdvancedMX method is enabled.
   Memory pools are JDK implementor dependent : the all_pools_advanced_mx.xml file defines the exhaustive 
   set of pools available (Oracle JDKs 7 and 8).
   Memory pools which are not available on the monitored JVM are ignored and therefore not dumped.
   If the memory pool info is not accessible, -1 value will be dumped. 

c) Jeyzer Recorder garbage collector configuration
   Garbage collector configuration defines the set of garbage collection figures to collect. 
   Usually, JVMs do use 2 garbage collectors : one for the young generation and one for the old generation memory zone. 
   Feature is available when the AdvancedMX method is enabled.
   Garbage collectors are JDK implementor dependent : the all_garbage_collectors_advanced_mx.xml file defines the exhaustive 
   set of garbage collectors available (JDKs 5 to 11).
   Garbage collector which are not available on the monitored JVM are ignored and therefore not dumped.
   If the garbage collector info is not accessible, -1 value will be dumped. 

d) Jeyzer Recorder logging
   Jeyzer Recorder traces are generated by default in the <Jeyzer Recorder home>/log directory.
   Log file path can be changed trough the JEYZER_RECORDER_LOG_FILE environment variable.
   Log level of each appender (console and log file) is controlled within the <Jeyzer Recorder home>/config/logback.xml.
   By default it is set to INFO.
   Log configuration update requires Jeyzer Recorder to be restarted.

e) Jeyzer Recorder configuration parameters - JMX and Jstack methods
   Jeyzer Recorder parameters are set within the startup scripts. 
   Please refer to it for more info.

f) For advanced JMX configuration such as security settings, please refer to :
   http://docs.oracle.com/javase/7/docs/technotes/guides/management/toc.html


---------------------------------
===  Process card generation  ===
---------------------------------

Optionally, Jeyzer Recorder can generate a process card file at startup named process-card.properties.
The process card file can contain : 
 - monitored process system properties	 					ex : java.runtime.version=1.8.0_45-b15 
 - pid process, obtained through jinfo utility (Jstack methods only)
 - start time and up time 									ex : jzr.ext.process.start.time=1514351531994
 - number of processors if applicable	  					ex : jzr.ext.process.available.processors=4 
 - process info published through the Jeyzer MX bean     	ex : jzr.cxt.param-flight-line-type=commercial
 - process info published through standard MX beans       	ex : org.apache.activemq:Broker:*:BrokerVersion=6.2
 - jar manifest attributes (AdvancedMXAgent method only)  	ex : jzr.jar.logback-classic-1.0.13.jar.Bundle-ManifestVersion=2

For the JMX and agent methods, the process properties are obtained through the Runtime MX bean.
Process card generation can be disabled by configuration.
The process card file is included in each recording archive file.


---------------------------------
===   Jar paths generation    ===
---------------------------------

Optionally, Jeyzer Recorder can generate a loaded jar paths file after startup named process-jar-paths.txt.
This file contains the list of jar files loaded by the JVM.
As jars may be loaded at any time, the file is re-generated every 15 minutes by default.
The path collection starts after 2 minutes by default.

For each jar file, it is possible to load its Manifest attributes, such as the jar version and project names.
Those can be used in case the jar path name doesn't contain any version.

Jar paths file generation can be disabled by configuration.
The loaded jar paths file is included in each recording archive file.


--------------------------------
===        Time zone         ===
--------------------------------

Time zone is important in case of issue investigation to correlate the JZR report data with other facts (logs, probes, user feedback, etc).
Time zone is set on the recording snapshot and archive file name as prefix, in general time zone format.
Example :
	snap-JZR-2020-06-10---22-11-52-183-CEST.jzr
	jzr-rec-P-2020-06-10---14-37-35-556-CST---2020-06-10---14-42-35-860-CST.zip
JZR reports do show this time zone information in the Session Details sheet.
	 
Time zone can have 3 origins :

1) Monitored process time zone
Usual approach is to be based on the monitored process time zone. This is the default behavior.
Depending on the recording method, process time zone is either issued from jinfo execution or from the MX Runtime bean.
The -P- identifies a monitored process time zone :
	snap-P-2020-06-10---22-11-52-183-CEST.jzr

2) Custom time zone
In some cases, it may be better to define a custom time zone in order to be aligned with other environment context (global system time zone, end user time zone, etc).
Custom time zone is defined through the Jeyzer Recorder XML configuration. By default it is not set.  
The -C- identifies a monitored process time zone :
	snap-C-2020-06-10---22-11-52-183-CEST.jzr

3) Jeyzer Recorder time zone
This is the Jeyzer Recorder tool time zone. 
The -JZR- identifies a monitored process time zone :
	snap-JZR-2020-06-10---22-11-52-183-CEST.jzr

Time zone resolution is performed in this order : custom, monitored process (default), JZR.



--------------------------------
===      Advanced MX         ===
--------------------------------   
   
When enabled Advanced MX permits to collect additional data from the Java Platform MX interface :
	- Thread CPU elapsed time
	- Process CPU
	- System CPU
	- Thread memory
	- Pool memory	
	- Heap and non heap memory
	- System memory
	- Process up time
	- Pool memory before and after garbage collection
	- Garbage collection execution time and count
	- Free and used disk spaces (AdvancedMXAgent method only)
	- Recording write time (AdvancedMXAgent method only)
	- Loaded jar paths(AdvancedMXAgent method only)
In addition, it permits to collect process and thread data as well as applicative events published through the Jeyzer MX interface :
	- Thread context parameters  (ex : action request parameters)
	- Thread context id  (ex : request id)
	- Thread user
	- Thread action (Jeyzer action)
	- Thread action start time
	- Thread action id (Jeyzer unique id)
	- Process dynamic parameters (ex : number of processed requests)
	- Process static parameters (ex : product version)
	- Applicative system events (ex : specific runtime mode activated)
	- Applicative session events (ex : external service not accessible)
	- Applicative task events (ex : failing activity)
	- Jeyzer publisher events (ex : recording suspension/resume)
At last, it allows to collect process data published through standard MX names :
    - Process dynamic parameters (ex : number of received messages)
    - Process static parameters (ex : product version)
Jeyzer Analyzer allows to include all those figures in the JZR reports.




--------------------------------
===           FAQ            ===
--------------------------------

JMX and Jstack methods : what if the java monitored process is not available ?
If the server to monitor is not started, Jeyzer Recorder will retry periodically to connect to it.
Note that in case of slow down (server under high load), Jeyzer Recorder may not be able to connect (like for JConsole/Visual VM).
Connection errors are logged in the console and in the recorder log file :
01/05/2020 23:52:35 ERROR Recording snapshot generation failed : process not available. Will retry in 5 sec.


JMX and Jstack methods : Jeyzer Recorder automatically started ?
For an automatic startup, Jeyzer Recorder should reuse system tools like Unix crontab or Windows services.
In such case, the JMX connection method is the most appropriate.


JMX and Jstack methods : Jeyzer Recorder integration ?
Jeyzer Recorder can be integrated within any monitoring platform. 
In such case, both JMX connection method and Jstack method (pid collection) are appropriate.
Note that Jstack method requires a JDK (not a JRE).


Which method is the best ? JMX, agent or Jstack ?
All methods have their advantage :

- JMX method allows the recording snapshot generation from a remote location.
  It's also one time setup as JMX connection parameters of the monitored process are not supposed to change.

- Agent method is the most powerful method. 
  In case of critical slow downs, it is less affected than other methods which may fail to collect data.
  Both JMX and Agent methods have the optional advantage to collect more info like CPU and memory figures when AdvancedMX is enabled.

- Jstack is easy to use, but requires to fetch again the monitored process pid in case of restart, unless if a wrapping script allows to grep it.
  
Therefore, out of the box, JMX and agent methods are more appropriate for production monitoring needs 
and Jstack method is more appropriate for one time usage.
  


--------------------------------
===      Dependencies        ===
--------------------------------

Jeyzer Recorder includes the following libraries :

- Jeyzer publish : Jeyzer MX interface library
  https://jeyzer.org/jeyzer-publisher
  https://jeyzer.org/jeyzer-publisher-api
  https://github.com/jeyzer-community/jeyzer-publish

- Logback : logging library
  http://logback.qos.ch/

- SLF4J : logging facade library
  http://www.slf4j.org/

- Apache Commons Compress : compression utility library
  https://commons.apache.org/proper/commons-compress/
  
- Jeyzer Agent, fork of the General Java Agent project : agent wrapper library
  https://github.com/jeyzer-community/jeyzer-agent
  http://hapi.wikidot.com/java-agent-general

Note that the General Java Agent wrapper prevents any library conflict between Jeyzer Recorder and the monitored application
as it ensures the loading of the Jeyzer Recorder agent in a separate and isolated class loader.
 