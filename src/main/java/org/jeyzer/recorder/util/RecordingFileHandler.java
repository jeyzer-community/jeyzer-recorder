package org.jeyzer.recorder.util;

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

import java.io.File;
import java.util.Date;

import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.config.local.advanced.JzrAdvancedMXVTAgentConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class RecordingFileHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(RecordingFileHandler.class);
	
	protected String threadDumpDirectory;
	private File tempFile;
	
	private boolean vtFileSupported;
	
	public RecordingFileHandler(JzrRecorderConfig cfg) {
		this.threadDumpDirectory = cfg.getThreadDumpDirectory();
		this.tempFile = new File(cfg.getThreadDumpDirectory() + File.separator + FileUtil.TEMP_FILE_NAME);
		this.vtFileSupported = cfg instanceof JzrAdvancedMXVTAgentConfig;
	}
	
	public File getTempFile() {
		return tempFile;
	}
	
	public File renameTemporaryFile(long tdTimestamp){
		if (tdTimestamp == -1)
			tdTimestamp = System.currentTimeMillis();
		
		File finalFile = renameFile(tempFile, FileUtil.JZR_FILE_JZR_EXTENSION, tdTimestamp);
		
		if (this.vtFileSupported) {
			File vtTempFile = new File(tempFile.getAbsolutePath() + FileUtil.JZR_FILE_JZR_VT_EXTENSION);
			renameFile(vtTempFile, FileUtil.JZR_FILE_JZR_VT_EXTENSION, tdTimestamp);			
		}
		
		return finalFile;
	}

	private File renameFile(File source, String jzrFileJzrExtension, long tdTimestamp) {
		String fileName = FileUtil.getTimeStampedFileName(
				FileUtil.JZR_FILE_JZR_PREFIX, 
				new Date(tdTimestamp),
				jzrFileJzrExtension); 
		String filePath = this.threadDumpDirectory + File.separator + fileName;
		File tdFile = new File(filePath);
		if (source.exists()){
			if (!source.renameTo(tdFile))
				logger.error("Failed to rename file " + source.getName() + " into file " + filePath);
		}
		else{
			logger.debug("Temp file " + source.getPath() + " doesn't exist. Skipping the renaming.");
		}
		
		return tdFile;
	}
}
