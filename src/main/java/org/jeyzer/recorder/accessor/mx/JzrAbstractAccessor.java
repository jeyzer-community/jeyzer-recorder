package org.jeyzer.recorder.accessor.mx;

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





import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.accessor.JzrAccessor;
import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public abstract class JzrAbstractAccessor implements JzrAccessor{

	protected static final Logger logger = LoggerFactory.getLogger(JzrAbstractAccessor.class);
	
	public static final String INDENT = "    ";
	
	protected static final String DEADLOCK = " deadlock participant";
	
	protected BufferedWriter out;
	
	protected boolean initialized = false;
	protected boolean canDumpLocks = false;
	
	protected ThreadMXBean tmbean;

	protected ThreadInfo[] tinfos;
	
	protected long[] tids;
	
	protected List<Long> deadlockTids = new ArrayList<>();
	
	protected String timeZoneId = null;
	
	protected long captureDuration;	
	
	protected abstract BufferedWriter getWriter(File file) throws IOException;
	
	protected void writeln(String info) throws IOException {
		try {
			this.out.write(info);
			this.out.newLine();
		} catch (IOException e) {
			logger.error("Failed to write in thread dump file.");
			throw e;
		}
	}		
	
	protected void write(String info) throws IOException {
		try {
			this.out.write(info);
		} catch (IOException e) {
			logger.error("Failed to write in thread dump file.");
			throw e;
		}
	}
	

	protected void createDumpFile(File file) throws JzrGenerationException {
		try {
			// Create file
			out = getWriter(file);
		} catch (Exception e) {
			String msg = "Failed to create thread dump file : " + file.getAbsolutePath();
			logger.error(msg);
			logger.error(e.getMessage());
			throw new JzrGenerationException(msg, e);
		}		
	}
	
	protected void close() {
		this.tids = null;
		this.tinfos = null;
		this.deadlockTids.clear();
	}
}
