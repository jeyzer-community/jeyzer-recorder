package org.jeyzer.recorder.logger.util;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Recorder
 * --
 * Copyright (C) 2020, 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

public class LoggerConstants {
	
	// The logger -D configuration param
	public static final String PROPERTY_JEYZER_AGENT_HOME = "jeyzer.agent.home";
	public static final String PROPERTY_JEYZER_RECORD_AGENT_PROFILE = "jeyzer.record.agent.profile";
	public static final String PROPERTY_JEYZER_RECORDER_LOG_CONFIGURATION_FILE = "jeyzer.recorder.log.configuration.file";
	public static final String PROPERTY_JEYZER_RECORDER_LOG_FILE = "jeyzer.recorder.log.file";
	
	public static final String PROPERTY_JEYZER_RECORDER_BOOT_DEBUG = "jeyzer.recorder.boot.debug";
	
	// The logger configuration file name
	public static final String DEFAULT_LOG_CONFIG_FILE = "jeyzer-log.properties";

	// The logger properties
	public static final String PROPERTY_RECORDER_LOG_LEVEL = "jeyzer.recorder.log.level";
	public static final String PROPERTY_RECORDER_LOG_RELOAD = "jeyzer.recorder.log.reload";
	
	// Environment variables
	public static final String ENV_JEYZER_AGENT_HOME = "JEYZER_AGENT_HOME";
	public static final String ENV_JEYZER_RECORD_APP_CONFIG_REPOSITORY = "JEYZER_RECORD_APP_CONFIG_REPOSITORY";
	public static final String ENV_JEYZER_RECORD_APP_RECORDING_HOME = "JEYZER_RECORD_APP_RECORDING_HOME";
	public static final String ENV_JEYZER_RECORD_AGENT_PROFILE = "JEYZER_RECORD_AGENT_PROFILE";
	public static final String ENV_JEYZER_RECORDER_LOG_CONFIGURATION_FILE = "JEYZER_RECORDER_LOG_CONFIGURATION_FILE";
	public static final String ENV_JEYZER_RECORDER_LOG_FILE = "JEYZER_RECORDER_LOG_FILE";
	
	public static final String LOG_DEBUG_VALUE = "DEBUG";
	public static final String LOG_WARN_VALUE = "WARN";
	public static final String LOG_ERROR_VALUE = "ERROR";
	
	private LoggerConstants() {
	}
}
