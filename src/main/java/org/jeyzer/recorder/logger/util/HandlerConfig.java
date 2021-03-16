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

import java.util.Properties;
import java.util.logging.Level;

import org.jeyzer.recorder.logger.BootLogger;

public class HandlerConfig {
	
	private static final String LOGGER_PROPERTY_LOG_LEVEL = ".level";
	private static final String LOGGER_PROPERTY_LOG_ACTIVE = ".active";
	
	private String name;
	private Level level;
	private boolean active;
	
	public HandlerConfig(String name, Properties props) {
		this.name = name;   // ex : jeyzer.recorder.log.console
		this.level = loadLoglevel(props);
		this.active = loadLogActive(props);
	}
	
	public Boolean isActive() {
		return this.active;
	}

	public Level getLevel() {
		return this.level;
	}
	
	public String getName() {
		return this.name;
	}
	
	private boolean loadLogActive(Properties props) {
		String value = props.getProperty(this.name + LOGGER_PROPERTY_LOG_ACTIVE);
		
		if (value == null || value.isEmpty()) {
			BootLogger.debug(logPrefix() + " - Activation not specified in the configuration. Defaulting to false");
			return false;
		}
		
		BootLogger.debug(logPrefix() + " - Activation specified by configuration : " + value);			
		return Boolean.parseBoolean(value);
	}

	private Level loadLoglevel(Properties props) {
		String value = props.getProperty(this.name + LOGGER_PROPERTY_LOG_LEVEL);
		
		if (value == null || value.isEmpty()) {
			BootLogger.debug(logPrefix() + " - Log level not specified in the configuration. Defaulting to : " + Level.INFO);
			return Level.INFO;
		}
		
		try {
			Level logLevel;
			
			if (LoggerConstants.LOG_DEBUG_VALUE.equals(value)) // Map any logj, slf4j value
				logLevel = Level.FINE;
			else if (LoggerConstants.LOG_WARN_VALUE.equals(value)) // Map any warn mistake
				logLevel = Level.WARNING; 
			else 
			    logLevel = Level.parse(value.toUpperCase());
			
			BootLogger.debug(logPrefix() + " - Log level specified by configuration : " + logLevel);
			
			return logLevel;			
		}catch(IllegalArgumentException ex) {
			BootLogger.debug(logPrefix() + " - Invalid log level : " + value + ". Defaulting to " + Level.INFO);
			return Level.INFO;
		}
	}
	
	protected String logPrefix() {
		return "Handler " + this.name;
	}
}
