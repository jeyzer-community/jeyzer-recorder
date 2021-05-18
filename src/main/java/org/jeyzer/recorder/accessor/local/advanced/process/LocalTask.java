package org.jeyzer.recorder.accessor.local.advanced.process;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.jeyzer.recorder.output.JzrWriterFactory;

public abstract class LocalTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(LocalTask.class);
	
	private JzrSecurityManager securityMgr;
	
	public LocalTask(JzrSecurityManager securityMgr) {
		this.securityMgr = securityMgr;
	}
	
	protected abstract void storeData(BufferedWriter writer) throws IOException;
	
	protected void store(String outputDir, String finalFileName, String tempFileName, String displayType) throws IOException, JzrGenerationException {
		File file = new File(outputDir + File.separator + tempFileName);
		
		try (
				BufferedWriter writer = getWriter(file);
			)
		{
			storeData(writer);
		}catch(IOException ex){
			logger.error("Failed to print into the process " + displayType + " temp file");
			throw ex;
		}
		
		if (logger.isDebugEnabled())
			logger.debug(displayType + " file successfully generated into temp file : " + file.getAbsolutePath());
		
		boolean result;
		File finalFile = new File(outputDir + File.separator + finalFileName);
		if (finalFile.exists()) {
			// delete it first
			result = finalFile.delete();
			if (!result)
				throw new JzrGenerationException("Failed to delete the previous : " + finalFile.getAbsolutePath());
		}
		
		result = file.renameTo(finalFile);
		if (!result) {
			result = file.delete();
			if (!result)
				throw new JzrGenerationException("Failed to rename the temp " + displayType + " file and delete it afterwards");	
			throw new JzrGenerationException("Failed to rename the temp " + displayType + " file as : " + finalFile.getAbsolutePath());
		}
	}
	
	private BufferedWriter getWriter(File file) throws IOException {
		return JzrWriterFactory.newInstance().createWriter(file, securityMgr);
	}
}
