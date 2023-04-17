package org.jeyzer.recorder.accessor.local.advanced.system;

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


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalDiskWriteAccessor {

	protected static final Logger logger = LoggerFactory.getLogger(LocalDiskWriteAccessor.class);
	
	private static final String DISK_WRITE_FIELD_PREFIX = FileUtil.JZR_FIELD_JZ_PREFIX + "disk write:prev:";
	private static final String TIME_FIELD = "time" + FileUtil.JZR_FIELD_EQUALS;
	private static final String SIZE_FIELD = "size" + FileUtil.JZR_FIELD_EQUALS;
	
	private static final long NOT_AVAILABLE = -1;
	
	private boolean enabled;
	private long startTime = NOT_AVAILABLE;
	private long time = NOT_AVAILABLE;
	private long size = NOT_AVAILABLE;
	
	public LocalDiskWriteAccessor(boolean diskWriteTimeEnabled) {
		this.enabled = diskWriteTimeEnabled;
		if (logger.isDebugEnabled() && this.enabled)
			logger.debug("Localdisk write accessor loaded");
	}

	public void collectPrintStart(){
		if (isDiskWriteTimeEnabled())
			this.startTime = System.currentTimeMillis();
	}
	
	public void collectPrintEnd(File file){
		if (!isDiskWriteTimeEnabled())
			return;
		
		if(this.startTime != NOT_AVAILABLE){
			long endTime = System.currentTimeMillis();
			this.time = endTime - startTime;
			if (this.time == 0)
				this.time = 1;  // get 1 ms as minimum
			if (logger.isDebugEnabled())
				logger.debug("Print duration : " + this.time + " ms");
		}
		else{
			if (logger.isDebugEnabled())
				logger.debug("Print duration : start time not available");
		}
		
		// get the file size
		this.size = file.length();
		
		// reset for next usage
		this.startTime = NOT_AVAILABLE;
	}
	
	public void printDiskWriteInfo(BufferedWriter out) throws IOException {
		if (!isDiskWriteTimeEnabled()){
			return;
		}
		
		try {
			out.write(DISK_WRITE_FIELD_PREFIX + TIME_FIELD + time);
			out.newLine();
			out.write(DISK_WRITE_FIELD_PREFIX + SIZE_FIELD + size);
			out.newLine();
		} catch (IOException e) {
			logger.error("Failed to write disk write info in thread dump file.");
			throw e;
		}
	}
		
	public void failDiskWriteInfo(){
		this.startTime = NOT_AVAILABLE;
		this.time = NOT_AVAILABLE;
		this.size = NOT_AVAILABLE;
	}
	
	private boolean isDiskWriteTimeEnabled() {
		return this.enabled;
	}
}
