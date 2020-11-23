package org.jeyzer.recorder.accessor.jstack;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.error.JzrProcessNotAvailableException;
import org.jeyzer.recorder.config.jstack.JzrJstackInShellConfig;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JstackInShellAccessor extends JzrAbstractJstackAccessor{

	private static final Logger logger = LoggerFactory.getLogger(JstackInShellAccessor.class);	
	
	private static final String JZR_HEADER_PREFIX = "Full Java thread dump from Jstack in shell : ";	
	
	private final JzrJstackInShellConfig cfg;
	
	public JstackInShellAccessor(final JzrJstackInShellConfig cfg) {
		super(cfg);
		this.cfg = cfg;
	}
	
	@Override
	public long threadDump(File file) throws JzrProcessNotAvailableException, JzrGenerationException {
		long startTime, endTime, duration; 
	
		try {
			String command;
			if (this.cfg.isCaptureDurationEnabled())
				command = JAVA_HOME +  JSTACK_COMMAND + " " + cfg.getOptions() + " " + cfg.getPid() + " >> " + file.getAbsolutePath(); // append
			else
				command = JAVA_HOME +  JSTACK_COMMAND + " " + cfg.getOptions() + " " + cfg.getPid() + " > " + file.getAbsolutePath(); // override
			
			String[] args = buildExecutionArgs(command);
			
			if (logger.isDebugEnabled()){
				logger.debug("Executing command line : {}", command);
			}

			if (this.cfg.isCaptureDurationEnabled()){
				// prepare header
			    File tdFile = new File(file.getAbsolutePath());
			    BufferedWriter writer = null;
			    try {
			        writer = new BufferedWriter(new OutputStreamWriter(
			                new FileOutputStream(tdFile), "utf-8"));
			       	prepareHeader(writer, command);
				} finally {
			    	FileUtil.closeWriter(file, writer);
				}
			}
			
			startTime = System.currentTimeMillis();
			final Process p = Runtime.getRuntime().exec(args);
			int result = p.waitFor();
			endTime = System.currentTimeMillis();
			duration = endTime - startTime;
			
			if (logger.isDebugEnabled())
				logger.debug("Jstack execution time : {} ms", duration);
			
			if (result != 0){
				FileUtil.emptyFile(file);
				throw new JzrProcessNotAvailableException("Failed to execute jstack command : " + command);
			}
			
			if (this.cfg.isCaptureDurationEnabled())
				insertCaptureDuration(file, duration, command);
		} catch (Exception e) {
			String msg = "Failed to generate thread dump file : " + e.getMessage();
			throw new JzrGenerationException(msg, e);
		}
        
        return startTime + (duration) / 2;
	}

	@Override
	protected String getHeaderPrefix() {
		return JZR_HEADER_PREFIX;
	}

	@Override
	protected int getMonitoredPid() {
		return this.cfg.getPid();
	}

	@Override
	protected void dumpJinfo(File file, BufferedWriter writer) throws IOException {
		long startTime, endTime, duration; 
	
		FileUtil.closeWriter(file, writer); // close the writer now, otherwise append is lost
		
		String command;
		command = JAVA_HOME +  JINFO_COMMAND + JINFO_OPTION + cfg.getPid() + " >> " + file.getAbsolutePath(); // append
        
		String[] args = buildExecutionArgs(command);
        
        if (logger.isDebugEnabled()){
        	logger.debug("Executing command line : {}", command);
        }
        
    	startTime = System.currentTimeMillis();
        final Process p = Runtime.getRuntime().exec(args);
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
        	logger.debug("Jinfo execution time : {} ms", duration);
        
        if (result != 0)
        	logger.error("Failed to execute jinfo command : " + command);
        else
    		if (logger.isDebugEnabled())
    			logger.debug("Process card file successfully generated into file : {}", file.getAbsolutePath());
        
	}

	private String[] buildExecutionArgs(String command) {
		String[] args = new String[3];
		
        if (SystemHelper.isWindows()){
        	args[0] = "cmd.exe";
        	args[1] = "/C";
        	args[2] = command;
        }
        else if (SystemHelper.isUnix() || SystemHelper.isSolaris()){
        	args[0] = "/bin/sh";
        	args[1] = "-c";
        	args[2] = command;
        }

        return args;
	}	
	
}
