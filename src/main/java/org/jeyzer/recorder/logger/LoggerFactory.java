package org.jeyzer.recorder.logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

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

public class LoggerFactory {
	
	private static List<Logger> loggers = Collections.synchronizedList(new ArrayList<Logger>());
	
	private LoggerFactory(){
	}

	public static Logger getLogger(String name) {
		Logger logger = new Logger(name);
		loggers.add(logger);
		return logger;
	}

	public static Logger getLogger(Class<?> callerClass) {
		Logger logger = new Logger(callerClass);
		loggers.add(logger);
		return logger;
	}
	
	static void updateLogLevel(Level level) {
		synchronized(loggers) {
			for (Logger logger : loggers)
				logger.setLevel(level);			
		}
	}
}
