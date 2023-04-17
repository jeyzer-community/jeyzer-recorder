package org.jeyzer.recorder.logger;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Recorder
 * --
 * Copyright (C) 2020 - 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.util.logging.Level;

public class Logger {

	private java.util.logging.Logger utilLogger;
	
	Logger(String name) {
		utilLogger = LoggerManager.instance().getLogger(name);
	}
	
	Logger(Class<?> callerClass) {
		utilLogger = LoggerManager.instance().getLogger(callerClass.getSimpleName());
	}

	public boolean isDebugEnabled() {
		return utilLogger.isLoggable(Level.FINE);
	}
	
	public void debug(String message) {
		utilLogger.log(Level.FINE, message);
	}
	
	public void debug(String message, Exception ex) {
		utilLogger.log(Level.FINE, message, ex);
	}
	
	public void info(String message) {
		utilLogger.info(message);
	}
	
	public void info(String message, Exception ex) {
		utilLogger.log(Level.INFO, message, ex);
	}
	
	public void warn(String message) {
		utilLogger.log(Level.WARNING, message);
	}
	
	public void warn(String message, Throwable ex) {
		utilLogger.log(Level.WARNING, message, ex);
	}
	
	public void error(String message) {
		utilLogger.log(Level.SEVERE, message);
	}

	public void error(String message, Throwable ex) {
		utilLogger.log(Level.SEVERE, message, ex);
	}
	
	public Level getLevel() {
		return this.utilLogger.getLevel();
	}
	
	public void setLevel(Level level) {
		this.utilLogger.setLevel(level);
	}
}
