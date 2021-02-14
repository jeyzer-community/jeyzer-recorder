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

import java.io.File;
import java.util.concurrent.ThreadFactory;

public class LoggerWatchdogTask extends Thread implements Runnable {

	public static class LoggerWatchdogThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("Jeyzer-recorder-log-watchdog");
			t.setDaemon(true);
			return t;
		}
	}

	private final LoggerManager logManager;
	private final File fileToWatch;
	private long lastModified;
	
	public LoggerWatchdogTask(File fileToWatch, LoggerManager logManager) {
		this.logManager = logManager;
		this.fileToWatch = fileToWatch;
		this.lastModified = fileToWatch.lastModified();
	}

	@Override
	public void run() {
		if (this.fileToWatch.lastModified() > this.lastModified)
			reload();
	}

	private void reload() {
		Logger logger = LoggerFactory.getLogger(LoggerWatchdogTask.class);
		logger.info("Reloading the logging configuration : " + this.fileToWatch.getPath());
		try {
			this.lastModified = this.fileToWatch.lastModified();
			logManager.init();
		}
		catch(Exception ex) {
			logger.error("Failed to reload the logging configuration : " + this.fileToWatch.getPath(), ex);
		}
	}
}
