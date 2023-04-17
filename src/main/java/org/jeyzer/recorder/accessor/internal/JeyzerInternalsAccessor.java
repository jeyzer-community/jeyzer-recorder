package org.jeyzer.recorder.accessor.internal;

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

import java.io.BufferedWriter;
import java.io.IOException;

import org.jeyzer.recorder.logger.LoggerManager;
import org.jeyzer.recorder.util.ConfigUtil;

public class JeyzerInternalsAccessor {
	
	public static final String JZR_PROPERTY_RECORDER_VERSION = "jzr.recorder.version";
	
	public static final String JZR_PROPERTY_RECORDER_LOG_LEVEL = "jzr.recorder.log.level";
	public static final String JZR_PROPERTY_RECORDER_LOG_RELOADABLE = "jzr.recorder.log.reloadable";
	
	public static final String JZR_PROPERTY_RECORDER_LOG_FILE_PATH = "jzr.recorder.log.file.path";
	public static final String JZR_PROPERTY_RECORDER_LOG_FILE_ACTIVE = "jzr.recorder.log.file.active";
	public static final String JZR_PROPERTY_RECORDER_LOG_FILE_LEVEL = "jzr.recorder.log.file.level";
	
	public static final String JZR_PROPERTY_RECORDER_LOG_CONSOLE_ACTIVE = "jzr.recorder.log.console.active";
	public static final String JZR_PROPERTY_RECORDER_LOG_CONSOLE_LEVEL = "jzr.recorder.log.console.level";

	public void dumpRecorderValues(BufferedWriter writer) throws IOException {
		dumpRecorderVersion(writer);
		dumpRecorderLogValues(writer);
	}
	
	private void dumpRecorderLogValues(BufferedWriter writer) throws IOException {
		LoggerManager manager = LoggerManager.instance();
		
		writeLine(writer, JZR_PROPERTY_RECORDER_LOG_LEVEL, manager.getLogLevel().toString());
		writeLine(writer, JZR_PROPERTY_RECORDER_LOG_RELOADABLE, manager.isReloadable().toString());
		
		writeLine(writer, JZR_PROPERTY_RECORDER_LOG_FILE_PATH, manager.getFileConfiguration().getPath());
		writeLine(writer, JZR_PROPERTY_RECORDER_LOG_FILE_ACTIVE, manager.getFileConfiguration().isActive().toString());
		writeLine(writer, JZR_PROPERTY_RECORDER_LOG_FILE_LEVEL, manager.getFileConfiguration().getLevel().toString());
		
		writeLine(writer, JZR_PROPERTY_RECORDER_LOG_CONSOLE_ACTIVE, manager.getConsoleConfiguration().isActive().toString());
		writeLine(writer, JZR_PROPERTY_RECORDER_LOG_CONSOLE_LEVEL, manager.getConsoleConfiguration().getLevel().toString());
	}

	private void dumpRecorderVersion(BufferedWriter writer) throws IOException {
		String version= ConfigUtil.loadRecorderVersion();
		writeLine(writer, JZR_PROPERTY_RECORDER_VERSION, version);
	}
	
	private void writeLine(BufferedWriter writer, String key, String value) throws IOException {
    	writer.write(key + "=" + value);
    	writer.newLine();
    	writer.flush();
	}
}
