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

import static org.jeyzer.recorder.logger.util.LoggerConstants.*;

public class BootLogger {
	
	public static final String LOGGER_BOOT_DEBUG_PREFIX = "  Jeyzer log boot debug - ";
	public static final String LOGGER_BOOT_ERROR_PREFIX = "  Jeyzer log boot error - ";
	
	private static  boolean bootDebug = Boolean.parseBoolean(System.getProperty(PROPERTY_JEYZER_RECORDER_BOOT_DEBUG));
	
	private BootLogger() {
	}
	
	public static void debug(String message) {
		if (bootDebug)
			System.out.println(LOGGER_BOOT_DEBUG_PREFIX + message);
	}
	
	public static void error(String message, Exception ex) {
		System.err.println(LOGGER_BOOT_ERROR_PREFIX + message);
		ex.printStackTrace();
	}
	
	public static void error(String message) {
		System.err.println(LOGGER_BOOT_ERROR_PREFIX + message);
	}
}
