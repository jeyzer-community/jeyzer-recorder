package org.jeyzer.recorder.logger;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.security.CodeSource;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.jeyzer.recorder.logger.util.ConsoleHandlerConfig;
import org.jeyzer.recorder.logger.util.FileHandlerConfig;

public class LoggerManager {
	
	private static LoggerManager manager = new LoggerManager();
	
	private static final String LOG_LINE_FORMAT = "%1$tF %1$tT %2$s %3$s [%4$s] %5$s %n";
	
	private static final long LOG_RELOAD_PERIOD = 60; // seconds
	private static final long LOG_RELOAD_START_DELAY = 30;  // seconds
	
	private static final int LOG_FILE_LIMIT = 1024 * 1024 * 5; // 5 Mbs
	private static final int LOG_FILE_COUNT = 2;
	
	private boolean init;
	
	private boolean initWatch;
	private boolean reload;

	private File propertyFile;
	
	private ConsoleHandlerConfig consoleConfig;
	private FileHandlerConfig fileConfig;
	
	private Level level;
	private FileHandler fileHandler;
	private ConsoleHandler consoleHandler;
	
	private LoggerManager() {
		createConsoleHandler();
	}

	public static LoggerManager instance() {
		if (!manager.init)
			manager.init();
		return manager;
	}
	
	public ConsoleHandlerConfig getConsoleConfiguration() {
		return this.consoleConfig;
	}
	
	public FileHandlerConfig getFileConfiguration() {
		return this.fileConfig;
	}
	
	public Level getLogLevel() {
		return this.level;
	}
	
	public Boolean isReloadable() {
		return this.reload;
	}
	
	void init() {
		this.init = true;
		Properties props = loadConfigProperties();
		
		consoleConfig = new ConsoleHandlerConfig(props);
		fileConfig = new FileHandlerConfig(props);
		
		setupLevel(props);
		
		BootLogger.debug("File logging active : " + fileConfig.isActive());
		if (fileConfig.isActive())
			setupFileHandler();
		else
			disableFileHandler();

		BootLogger.debug("Log console active : " + consoleConfig.isActive());
		if (consoleConfig.isActive())
			setupConsoleHandler();
		else
			disableConsoleHandler();
		
		if (!fileConfig.isActive() && !consoleConfig.isActive())
			BootLogger.debug("Warning : No Jeyzer logger active !");

		reload = loadLogReload(props);
		if (reload && !initWatch && propertyFile.exists())
			initWatchDog();
	}

	private void disableConsoleHandler() {
		BootLogger.debug("Disabling console handler");
		consoleHandler.setLevel(Level.OFF);
	}

	private void disableFileHandler() {
		if (fileHandler == null)
			createFileHandler();

		BootLogger.debug("Disabling file handler");
		fileHandler.setLevel(Level.OFF);
	}

	private void setupLevel(Properties props) {
		Level value = loadLoglevel(props);
		if (initWatch && level != value) {
			BootLogger.debug("Global level changed : updating all loggers");
			LoggerFactory.updateLogLevel(value);
		}
		this.level = value;
	}
	
	private void setupConsoleHandler() {
		if (consoleHandler.getLevel() != consoleConfig.getLevel())
			consoleHandler.setLevel(consoleConfig.getLevel());
	}

	private void setupFileHandler() {
		if (fileHandler == null)
			createFileHandler();
		
		if (fileHandler.getLevel() != fileConfig.getLevel())
			fileHandler.setLevel(fileConfig.getLevel());
	}
	
	private void createConsoleHandler() {
		consoleHandler = new ConsoleHandler();
		consoleHandler.setFormatter(new SimpleFormatter() {
			@Override
			public synchronized String format(LogRecord line) {
		              return String.format(LOG_LINE_FORMAT,
		                      new Date(line.getMillis()),
		                      line.getLevel().getLocalizedName(),
		                      line.getLoggerName(),
		                      Thread.currentThread().getName(),
		                      line.getMessage()
		              );
		          }
			});
	}

	private void createFileHandler() {
		try {
			createLogDirectory(); // mandatory on JUL
			fileHandler = new FileHandler(
					fileConfig.getPath(),
					LOG_FILE_LIMIT,
					LOG_FILE_COUNT,
					fileConfig.isAppend()
				);
			fileHandler.setFormatter(new SimpleFormatter() {
		          @Override
		          public synchronized String format(LogRecord line) {
		              return String.format(LOG_LINE_FORMAT,
		                      new Date(line.getMillis()),
		                      line.getLevel().getLocalizedName(),
		                      line.getLoggerName(),
		                      Thread.currentThread().getName(),
		                      line.getMessage()
		              );
		          }
		     });
		} catch (SecurityException | IOException ex) {
			BootLogger.error("Failed to create the JUL file handler", ex);
		}
	}

	private void initWatchDog() {
		LoggerWatchdogTask watchdog = new LoggerWatchdogTask(propertyFile, this);
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
				new LoggerWatchdogTask.LoggerWatchdogThreadFactory());
		executor.scheduleWithFixedDelay(watchdog, 
				LOG_RELOAD_PERIOD, 
				LOG_RELOAD_START_DELAY, 
				TimeUnit.SECONDS);
		BootLogger.debug("Logger watch dog started");
		initWatch = true;
	}

	private void createLogDirectory() {
		File file = new File(fileConfig.getPath());
		if (!file.getParentFile().exists())
			if (!file.getParentFile().mkdirs())
				BootLogger.error("Failed to create the JUL file logging directory : " + file.getParentFile().getPath());
	}

	private Properties loadConfigProperties() {
		Properties props;
		
		String propsPath = System.getProperty(PROPERTY_JEYZER_RECORDER_LOG_CONFIGURATION_FILE);
		if (propsPath != null) {
			props = loadPropertyFile(propsPath);
			if (props != null) {
				propertyFile = new File(propsPath);
				BootLogger.debug("Jeyzer Recorder log configuration file loaded through system property : " + PROPERTY_JEYZER_RECORDER_LOG_CONFIGURATION_FILE);
				BootLogger.debug("Jeyzer Recorder log configuration file path is : " + propsPath);
				return props;
			}
		}
		BootLogger.debug("Jeyzer Recorder logging file not specified on the command line.");
		
		// environment log file
		propsPath = System.getenv().get(ENV_JEYZER_RECORDER_LOG_CONFIGURATION_FILE);
		if (propsPath != null) {
			props = loadPropertyFile(propsPath);
			if (props != null) {
				propertyFile = new File(propsPath);
				BootLogger.debug("Jeyzer Recorder log configuration file loaded through system env variable : " + ENV_JEYZER_RECORDER_LOG_CONFIGURATION_FILE);
				BootLogger.debug("Jeyzer Recorder log configuration file path is : " + propsPath);
				return props;
			}
		}
		BootLogger.debug("Jeyzer Recorder logging file not specified in the environment.");
		
		// scaling environment with variables
		String configHome = System.getProperty(PROPERTY_JEYZER_RECORD_APP_CONFIG_REPOSITORY);
		if (configHome == null)
			configHome = System.getenv().get(ENV_JEYZER_RECORD_APP_CONFIG_REPOSITORY);
		String profile = System.getProperty(PROPERTY_JEYZER_RECORD_AGENT_PROFILE);
		if (profile == null)
			profile = System.getenv().get(ENV_JEYZER_RECORD_AGENT_PROFILE);
		if (configHome != null && new File(configHome).isDirectory() && profile != null && !profile.isEmpty()) {
			propsPath = configHome + File.separator + "log" + File.separator + profile  + File.separator + DEFAULT_LOG_CONFIG_FILE;
			props = loadPropertyFile(propsPath);
			if (props != null) {
				propertyFile = new File(propsPath);
				BootLogger.debug("Jeyzer Recorder log configuration file loaded from the Jeyzer Recorder configuration repository.");
				BootLogger.debug("Jeyzer Recorder log configuration file path is : " + propsPath);
				return props;
			}
		}
		BootLogger.debug("Jeyzer Recorder logging file not specified in the Jeyzer Recorder configuration repository.");
		
		// agent home
		String agentHome = System.getProperty(PROPERTY_JEYZER_AGENT_HOME);
		if (agentHome == null)
			agentHome = System.getenv().get(ENV_JEYZER_AGENT_HOME);
		if (agentHome != null && new File(agentHome).isDirectory()) {
			propsPath = agentHome + File.separator + "config" + File.separator + "log" + File.separator + DEFAULT_LOG_CONFIG_FILE;
			props = loadPropertyFile(propsPath);
			if (props != null) {
				propertyFile = new File(propsPath);
				BootLogger.debug("Jeyzer Recorder log configuration file loaded from the Jeyzer Recorder home.");
				BootLogger.debug("Jeyzer Recorder log configuration file path is : " + propsPath);
				return props;
			}
		}
		
		// recorder deduced home
		CodeSource src = LoggerManager.class.getProtectionDomain().getCodeSource(); // jar located in lib
		File recorderHome = new File(src.getLocation().getFile()).getParentFile().getParentFile();
		propsPath = recorderHome.getPath() + File.separator + "config" + File.separator + "log" + File.separator + DEFAULT_LOG_CONFIG_FILE;
		props = loadPropertyFile(propsPath);
		if (props != null) {
			propertyFile = new File(propsPath);
			BootLogger.debug("Jeyzer Recorder log configuration file loaded from the deduced Jeyzer Recorder home.");
			BootLogger.debug("Jeyzer Recorder log configuration file path is : " + propsPath);
			return props;
		}
		
		return new Properties(); // empty
	}

	private Properties loadPropertyFile(String propsPath) {
		File file = new File(propsPath);
		if (!file.exists()) {
			BootLogger.error("Jeyzer Recorder logger configuration file not found : " + propsPath);
			return null;
		}
		
		if (file.isDirectory()) {
			BootLogger.error("Jeyzer Recorder logger configuration file is a directory (!) : " + propsPath);
			return null;
		}

		try {
			Properties props = new Properties();
			props.load(new FileInputStream(file.getPath()));
			return props;
		} catch (IOException ex) {
			BootLogger.error("Failed to open the Jeyzer Recorder logger configuration file : " + file.getPath(), ex);
			return null;
		}
	}

	public Logger getLogger(String name) {
		Logger logger = Logger.getLogger(name);
		logger.setUseParentHandlers(false);
		logger.setLevel(level);
		if (fileConfig.isActive())
			logger.addHandler(fileHandler);
		if (consoleConfig.isActive())
			logger.addHandler(consoleHandler);
		return logger;
	}
	
	private Level loadLoglevel(Properties props) {
		String value = props.getProperty(PROPERTY_RECORDER_LOG_LEVEL);
		if (value == null || value.isEmpty()) {
			BootLogger.debug("Logger log level not specified in the configuration. Defaulting to : " + Level.INFO);
			return Level.INFO;
		}
		
		try {
			Level level;
			
			if (LOG_DEBUG_VALUE.equals(value)) // Map any logj, slf4j value
				level = Level.FINE;
			else if (LOG_WARN_VALUE.equals(value)) // Map any warn mistake
				level = Level.WARNING;
			else if (LOG_ERROR_VALUE.equals(value)) // Map any logj, slf4j value
				level = Level.SEVERE;
			else 
			    level = Level.parse(value.toUpperCase());
			
			BootLogger.debug("Logger log level specified by configuration : " + level);
			
			return level;			
		}catch(IllegalArgumentException ex) {
			BootLogger.debug("Invalid logger log level : " + value + ". Defaulting to " + Level.INFO);
			return Level.INFO;
		}
	}
	
	private boolean loadLogReload(Properties props) {
		String value = props.getProperty(PROPERTY_RECORDER_LOG_RELOAD);
		if (value == null || value.isEmpty()) {
			BootLogger.debug("Logger reload not specified in the configuration. Defaulting to false");
			return false;
		}
		
		boolean reload = Boolean.parseBoolean(value);
		BootLogger.debug("Logger reload read from the configuration. Value is : " + reload);
		return reload;
	}
}
