package org.jeyzer.recorder.accessor.jstack;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.error.JzrProcessNotAvailableException;
import org.jeyzer.recorder.accessor.jstack.output.JstackErrorProcessor;
import org.jeyzer.recorder.accessor.jstack.output.JstackOutputProcessor;
import org.jeyzer.recorder.config.jstack.JzrJstackConfig;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class JstackAccessor extends JzrAbstractJstackAccessor{

	private static final Logger logger = LoggerFactory.getLogger(JstackAccessor.class);	
	
	private static final String JZR_HEADER_PREFIX = "Full Java thread dump from Jstack : ";

	private final JzrJstackConfig cfg;
	
	public JstackAccessor(final JzrJstackConfig cfg) {
		super(cfg);
		this.cfg = cfg;
	}
	
	@Override
	public long threadDump(File file) throws JzrProcessNotAvailableException, JzrGenerationException {
		long startTime;
		long endTime;
		long duration;
		int result = -1;
        
		String command = JAVA_HOME +  JSTACK_COMMAND + " " + cfg.getOptions() + " " + cfg.getPid();
        
		BufferedWriter writer = null;
		try {
	        if (logger.isDebugEnabled())
	        	logger.debug("Executing command line : " + command);

	        File tdFile = new File(file.getAbsolutePath());
	        writer = new BufferedWriter(new OutputStreamWriter(
	             new FileOutputStream(tdFile), "utf-8"));
	        
	        if (this.cfg.isCaptureDurationEnabled())
	        	prepareHeader(writer, command);
	        
	    	startTime = System.currentTimeMillis();
	        final Process p = Runtime.getRuntime().exec(command);
	        
	        Thread outputThread = new Thread(
	        		new JstackOutputProcessor(
	        				p.getInputStream(),
	        				writer,
	        				"stack info"
	        				));

	        Thread errorThread = new Thread(
	        		new JstackErrorProcessor(
	        				p.getErrorStream()
	        				));
	        
	        outputThread.start();        
	        errorThread.start();
	        
	        result = p.waitFor();        

	        endTime = System.currentTimeMillis();
	        duration = endTime - startTime;

	        if (logger.isDebugEnabled())
	        	logger.debug("Jstack execution time : " + duration + " ms");        
			
		}catch (Exception e) {
			String msg = "Failed to generate thread dump file : " + e.getMessage();
			throw new JzrGenerationException(msg, e);
		} finally {
	        FileUtil.closeWriter(file, writer);
		}
        
        if (result != 0){
        	FileUtil.emptyFile(file);        	
        	throw new JzrProcessNotAvailableException("Failed to execute jstack command : " + command);
       }

        if (this.cfg.isCaptureDurationEnabled())
        	insertCaptureDuration(file, duration, command);
        
        return startTime + (duration) / 2;
	}

	@Override
	protected String getHeaderPrefix() {
		return JZR_HEADER_PREFIX;
	}

	protected void dumpJinfo(File file, BufferedWriter writer) throws IOException {
		long startTime;
		long endTime;
		long duration;
        
		String command = JAVA_HOME +  JINFO_COMMAND + JINFO_OPTION + cfg.getPid();
        
        if (logger.isDebugEnabled())
        	logger.debug("Executing command line : " + command);
        
    	startTime = System.currentTimeMillis();
        final Process p = Runtime.getRuntime().exec(command);
        
        Thread outputThread = new Thread(
        		new JstackOutputProcessor(
        				p.getInputStream(),
        				writer,
        				"jinfo output"
        				));

        Thread errorThread = new Thread(
        		new JstackErrorProcessor(
        				p.getErrorStream()
        				));
        
        outputThread.start();        
        errorThread.start();
        
        int result = -1;
		try {
			result = p.waitFor();
		} catch (InterruptedException e) {
			logger.error("Jinfo execution interrupted", e);
			Thread.currentThread().interrupt();
		}

        endTime = System.currentTimeMillis();
        duration = endTime - startTime;

        if (logger.isDebugEnabled())
        	logger.debug("Jinfo execution time : " + duration + " ms");        
        
        if (result != 0)
        	logger.error("Failed to execute jinfo command : " + command);
        else
    		if (logger.isDebugEnabled())
    			logger.debug("Process card file successfully generated into file : " + file.getAbsolutePath());        	
    }

	@Override
	protected int getMonitoredPid() {
		return this.cfg.getPid();
	}
	
}
