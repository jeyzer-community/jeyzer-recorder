package org.jeyzer.recorder.logger.util;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Recorder
 * --
 * Copyright (C) 2020 - 2021 Jeyzer SAS
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import static org.jeyzer.recorder.logger.util.LoggerConstants.*;

import java.io.File;
import java.security.CodeSource;
import java.util.Properties;

import org.jeyzer.recorder.logger.BootLogger;
import org.jeyzer.recorder.logger.LoggerManager;

public class FileHandlerConfig extends HandlerConfig{

	private static final String HANDLER_NAME = "jeyzer.recorder.log.file";
	
	private static final String LOGGER_PROPERTY_PATH = ".path";
	private static final String LOGGER_PROPERTY_APPEND = ".append";
	private static final String LOG_FILE_PATH_SUFFIX = "/log/jeyzer-recorder.log";

	private String path;
	private boolean append;
	
	public FileHandlerConfig(Properties props) {
		super(HANDLER_NAME, props);
		path = loadLogFilePath(props);
		append = loadAppend(props);
	}
	
	private boolean loadAppend(Properties props) {
		String value = props.getProperty(getName() + LOGGER_PROPERTY_APPEND);
		if (value != null) {
			BootLogger.debug(logPrefix() + " - Log file append read from the configuration " + getName() + LOGGER_PROPERTY_APPEND + " property.");
			BootLogger.debug(logPrefix() + " - Log file append is : " + value);
			return Boolean.parseBoolean(value);
		}
		BootLogger.debug(logPrefix() + " - Log file append set to default : true");
		return true;
	}

	private String loadLogFilePath(Properties props) {
		String logPath;
		
		// property log file entry
		logPath = props.getProperty(getName() + LOGGER_PROPERTY_PATH);
		if (logPath != null) {
			BootLogger.debug(logPrefix() + " - Log file path read from the configuration " + getName() + LOGGER_PROPERTY_PATH + " property.");
			BootLogger.debug(logPrefix() + " - Log file path is : " + logPath);
			return logPath;
		}
		
		// property log file (below env mirror)
		logPath = System.getProperty(PROPERTY_JEYZER_RECORDER_LOG_FILE);
		if (logPath != null) {
			BootLogger.debug(logPrefix() + " - Log file path read from the -D" + PROPERTY_JEYZER_RECORDER_LOG_FILE + " property.");
			BootLogger.debug(logPrefix() + " - Log file path is : " + logPath);
			return logPath;
		}
		
		// environment log file
		logPath = System.getenv().get(ENV_JEYZER_RECORDER_LOG_FILE);
		if (logPath != null) {
			BootLogger.debug(logPrefix() + " - Log file path read from the " + ENV_JEYZER_RECORDER_LOG_FILE + " env variable.");
			BootLogger.debug(logPrefix() + " - Log file path is : " + logPath);
			return logPath;
		}
		
		// scaling environment with variables
		boolean recordingHomeProp = true;
		String recordingHome = System.getProperty(ENV_JEYZER_RECORD_APP_RECORDING_HOME);
		if (recordingHome == null) {
			recordingHome = System.getenv().get(ENV_JEYZER_RECORD_APP_RECORDING_HOME);
			recordingHomeProp = false;
		}
		boolean recordingProfileProp = true;
		String profile = System.getProperty(PROPERTY_JEYZER_RECORD_AGENT_PROFILE);
		if (profile == null) {
			profile = System.getenv().get(ENV_JEYZER_RECORD_AGENT_PROFILE);
			recordingProfileProp = false;
			if (profile == null)
				profile = System.getProperty(ENV_JEYZER_RECORD_AGENT_PROFILE);
		}
		if (recordingHome != null && new File(recordingHome).isDirectory() && profile != null && !profile.isEmpty()) {
			logPath = recordingHome + File.separatorChar + profile  + File.separatorChar + "log" + File.separatorChar + "jeyzer-recorder-" + profile + ".log";
			if (recordingHomeProp)
				BootLogger.debug(logPrefix() + " - Log file path built with the -D" + ENV_JEYZER_RECORD_APP_RECORDING_HOME + " property. Value is : " + recordingHome);
			else
				BootLogger.debug(logPrefix() + " - Log file path built with the " + ENV_JEYZER_RECORD_APP_RECORDING_HOME + " env variable (or agent configuration). Value is : " + recordingHome);
			if (recordingProfileProp)
				BootLogger.debug(logPrefix() + " -    and the agent profile from the -D" + PROPERTY_JEYZER_RECORD_AGENT_PROFILE + "property. Value is : " + profile);
			else
				BootLogger.debug(logPrefix() + " -    and the agent profile from the : " + ENV_JEYZER_RECORD_AGENT_PROFILE + " env variable (or agent configuration). Value is : " + profile);
			BootLogger.debug(logPrefix() + " - Log file path is : " + logPath);

			return logPath;
		}
		
		// agent home (property)
		String agentHome = System.getProperty(PROPERTY_JEYZER_AGENT_HOME);
		if (agentHome != null && new File(agentHome).isDirectory()) {
			logPath = sanitizePathSeparators(agentHome + LOG_FILE_PATH_SUFFIX);
			BootLogger.debug(logPrefix() + " - Log file path read from the -D" + PROPERTY_JEYZER_AGENT_HOME + " property. Value is : " + agentHome);
			BootLogger.debug(logPrefix() + " - Log file path is : " + logPath);
			return logPath;
		}
		
		// agent home (env)
		agentHome = System.getenv().get(ENV_JEYZER_AGENT_HOME);
		if (agentHome != null && new File(agentHome).isDirectory()) {
			logPath = sanitizePathSeparators(agentHome + LOG_FILE_PATH_SUFFIX);
			BootLogger.debug(logPrefix() + " - Log file path read from the " + ENV_JEYZER_AGENT_HOME + " env variable. Value is : " + agentHome);
			BootLogger.debug(logPrefix() + " - Log file path is : " + logPath);
			return logPath;
		}
		
		// recorder deduced home
		logPath = sanitizePathSeparators(getRecorderHomePath() + LOG_FILE_PATH_SUFFIX);
		BootLogger.debug(logPrefix() + " - Logger file path deduced from the recorder jar location. Home is : " + getRecorderHomePath());
		BootLogger.debug(logPrefix() + " - Log file path is : " + logPath);

		return logPath;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public boolean isAppend() {
		return this.append;
	}
	
	// Additional
	public String getRecorderHomePath() {
		CodeSource src = LoggerManager.class.getProtectionDomain().getCodeSource();
		File jarFile = new File(src.getLocation().getFile());
		return jarFile.getParentFile().getParentFile().getAbsolutePath(); // get lib dir and then recorder home dir
	}
	
	// Do not depend on ConfigUtil to prevent early logger creation
	public String sanitizePathSeparators(String path){
		if (isWindows()){
			return path.replace('/', '\\');
		}
		else{
			return path.replace('\\', '/');
		}
	}
	
	public static final String PROPERTY_OS_NAME = "os.name";
	public static final String PLATFORM = System.getProperty(PROPERTY_OS_NAME).toLowerCase();
	
	public boolean isWindows() {
		return (PLATFORM.indexOf("win") >= 0);
	}
}
