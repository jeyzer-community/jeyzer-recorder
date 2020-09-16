package org.jeyzer.recorder;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Recorder
 * --
 * Copyright (C) 2020 Jeyzer SAS
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.accessor.error.JzrValidationException;
import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.config.JzrRecorderConfigBuilder;
import org.jeyzer.recorder.util.ConfigUtil;
import org.jeyzer.recorder.util.SanitizedPathProperties;

public class JeyzerRecorderAgent {
	
	private static final String JEYZER_RECORDER_PROPS_PROPERTY = "jeyzer-recorder-agent.props";
	private static final String JEYZER_RECORDER_PROPS_DEFAULT_PATH = "./jeyzer/config/agent/jeyzer-record.properties";
	
	private static final String JEYZER_RECORDER_CONFIG_FILE_PROPERTY = "jeyzer-record.JEYZER_RECORD_CONFIG_FILE";
	private static final String JEYZER_RECORDER_CONFIG_DIR_PROPERTY = "jeyzer-record.JEYZER_RECORD_CONFIG_DIR";
	private static final String JEYZER_RECORDER_PROFILE_PROPERTY = "jeyzer-record.JEYZER_RECORD_PROFILE";
	
	private static final String JEYZER_RECORDER_METHOD_PROPERTY_NAME = "JEYZER_RECORD_DUMP_METHOD";
	
	private static final String JEYZER_RECORDER_PROP_PREFIX = "jeyzer-record.";
	private static final String JEYZER_ERROR_MSG_PREFIX = "Jeyzer Recorder - ";
	private static final String JEYZER_ERROR_MSG_SUFFIX = " - Agent could not be loaded";
	
	// Testing main
	public static void main(String[] args) {
		premain("", null);
		
		try {
			Thread.sleep(50000000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	// java-agent-general main method
	// Used to :
	//  - load current agent in separate class loader
	//  - do not care about agent Manifest file
	//  - define jeyzer agent home directory
	 public static void premain(java.util.regex.Pattern[] includePatterns, java.util.regex.Pattern[] excludePatterns, Object config, Instrumentation instrumentation)
	 {
		 if (config instanceof Map){
			 @SuppressWarnings("unchecked")
			Map<String, String> configMap = (Map<String, String>)config;
			 String propertyFilePath = configMap.get(JEYZER_RECORDER_PROPS_PROPERTY);
			 if (propertyFilePath != null){
				 System.setProperty(JEYZER_RECORDER_PROPS_PROPERTY, propertyFilePath);
			 }else{
				 System.err.println(JEYZER_ERROR_MSG_PREFIX + "Configuration value " + JEYZER_RECORDER_PROPS_PROPERTY + " not found in the general java agent configuration" + JEYZER_ERROR_MSG_SUFFIX);
				 return;
			 }
		 }
		 else{
			 System.err.println(JEYZER_ERROR_MSG_PREFIX + "Invalid general java agent configuration" + JEYZER_ERROR_MSG_SUFFIX);
			 return;
		 }
		 
		 premain(null, instrumentation);
	 }
	
	 // standard agent main method
	 public static void premain(String args, Instrumentation inst) {
		 try{
			 try {
				loadAgentProperties();
			} catch (JzrInitializationException e) {
				System.err.println(JEYZER_ERROR_MSG_PREFIX + "Error - Failed to load the Jeyzer Recorder agent properties" + JEYZER_ERROR_MSG_SUFFIX);
				e.printStackTrace();
		        return;
			}
	
			JeyzerRecorder ftd = null;
			JzrRecorderConfig config = null;
	
			try {
				config = JzrRecorderConfigBuilder.newInstance().buildConfig();
			} catch (Exception e) {
				System.err.println(JEYZER_ERROR_MSG_PREFIX + "Error - Failed to load the Jeyzer Recorder configuration" + JEYZER_ERROR_MSG_SUFFIX);
				e.printStackTrace();
				return;
			}
				
				
			// create recording/log directory
			try {
				File tdDirectory = new File(config.getThreadDumpDirectory());
				tdDirectory.mkdirs();
			} catch (Exception e) {
				System.err.println(JEYZER_ERROR_MSG_PREFIX + "Error - Failed to create the recording snapshot directory : " + e.getMessage() + JEYZER_ERROR_MSG_SUFFIX);
				return;
			}
	
			// start the archiver (delayed)		
			JzrArchiverTask archiver = new JzrArchiverTask(config);
			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
					new JzrArchiverTask.ArchiverThreadFactory());
			executor.scheduleWithFixedDelay(archiver, 
					config.getArchiveZipPeriod().getSeconds(), 
					config.getArchiveZipPeriod().getSeconds(), 
					TimeUnit.SECONDS);
	
			try{
				ftd = new JeyzerRecorder(config, inst);
			} catch(JzrValidationException ex){
				System.err.println(JEYZER_ERROR_MSG_PREFIX + "Failed to start Jeyzer Recorder : " + ex.getMessage() + JEYZER_ERROR_MSG_SUFFIX);
				return;
			} catch(JzrInitializationException ex){
				System.err.println(JEYZER_ERROR_MSG_PREFIX + "Failed to start Jeyzer Recorder : " + ex.getMessage() + JEYZER_ERROR_MSG_SUFFIX);
				return;
			} catch(Exception ex){
				System.err.println(JEYZER_ERROR_MSG_PREFIX + "Failed to start Jeyzer Recorder." + ex + JEYZER_ERROR_MSG_SUFFIX);
				return;
			}
	
			// start the Jeyzer Recorder now
			ScheduledExecutorService executor2 = Executors.newSingleThreadScheduledExecutor(
					new JeyzerRecorder.RecorderThreadFactory(true)
					);
			executor2.scheduleWithFixedDelay(ftd, 
					config.getStartDelay().getSeconds(),
					config.getPeriod().getSeconds(),
					TimeUnit.SECONDS);
			
		} catch(Exception ex){
			System.err.println(JEYZER_ERROR_MSG_PREFIX + "Execution failed." + ex + JEYZER_ERROR_MSG_SUFFIX);
			return;
		}
	 }

	private static void loadAgentProperties() throws JzrInitializationException {
		String path = System.getProperty(JEYZER_RECORDER_PROPS_PROPERTY,JEYZER_RECORDER_PROPS_DEFAULT_PATH);
		SanitizedPathProperties props = new SanitizedPathProperties();
	    	
	    try (
	    		FileInputStream input = new FileInputStream(path);
	    	)
	    {
	    	props.loadAndSanitizePaths(input);
	    } catch (IOException ex) {
	    	throw new JzrInitializationException(JEYZER_ERROR_MSG_PREFIX + "Could not load configuration file : " + path);
	    }
	    
	    loadSystemProperties(props);
	}

	private static void loadSystemProperties(Properties props) {
		for (Object key : props.keySet()){
			String name = (String) key;
			if (name.startsWith(JEYZER_RECORDER_PROP_PREFIX)){
				// remove the jeyzer-record. to keep only the variable name
				String sysProperty = name.substring(JEYZER_RECORDER_PROP_PREFIX.length());

				// resolve any system property or environment variable
				String value = ConfigUtil.resolveValue(props.getProperty(name));
				
				System.setProperty(
						sysProperty,
						value
						);
			}
		}

		// configuration file. Ex : config/standard/standard_profile.xml
		System.setProperty(JzrRecorderConfigBuilder.CONFIG_FILE_PROPERTY, 
				props.getProperty(JEYZER_RECORDER_CONFIG_DIR_PROPERTY)
			+	"/profiles/"
			+	props.getProperty(JEYZER_RECORDER_PROFILE_PROPERTY)
			+	"/"
			+	props.getProperty(JEYZER_RECORDER_CONFIG_FILE_PROPERTY));
		
		// agent method
		System.setProperty(JEYZER_RECORDER_METHOD_PROPERTY_NAME, JzrRecorderConfigBuilder.PARAM_METHOD_AGENT); 
	}
}
