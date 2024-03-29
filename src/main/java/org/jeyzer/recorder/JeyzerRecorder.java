package org.jeyzer.recorder;

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
import java.lang.instrument.Instrumentation;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.jeyzer.recorder.accessor.JzrAccessor;
import org.jeyzer.recorder.accessor.JzrAccessorBuilder;
import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.accessor.error.JzrProcessNotAvailableException;
import org.jeyzer.recorder.accessor.error.JzrValidationException;
import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.config.JzrRecorderConfigBuilder;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.util.ConfigUtil;
import org.jeyzer.recorder.util.JzrTimeZone;
import org.jeyzer.recorder.util.SystemHelper;
import org.jeyzer.recorder.util.JzrTimeZone.Origin;
import org.jeyzer.recorder.util.RecordingFileHandler;


public class JeyzerRecorder implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(JeyzerRecorder.class);

	public static class RecorderThreadFactory implements ThreadFactory {
		
		private boolean daemon;
		
		public RecorderThreadFactory(boolean daemon){
			this.daemon = daemon;
		}
		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("Jeyzer-recorder");
			t.setDaemon(daemon);
			return t;
		}
	}

	public static final String PROCESS_CARD_FILE_NAME = "process-card.properties";
	
	private JzrRecorderConfig cfg;
	
	private boolean initiated = false;
	private JzrAccessor monitor;

	public JeyzerRecorder(JzrRecorderConfig cfg) throws JzrInitializationException, JzrValidationException{
		this(cfg, null);
	}

	public JeyzerRecorder(JzrRecorderConfig cfg, Instrumentation instrumentation) throws JzrInitializationException, JzrValidationException{
		this.cfg = cfg;
		monitor = JzrAccessorBuilder.newInstance().buildAccessor(cfg, instrumentation);
		monitor.validate();
	}	
	
	public void dump() {
		RecordingFileHandler fileHandler = new RecordingFileHandler(cfg);
		long startTime = 0;
		long tdTimestamp = 0;
		File tdFile = null;

        try {
        	if (!initiated){
        		initiate();
        	}
        	
			if (logger.isDebugEnabled()){
				logger.debug("Generating recording snapshot...");
				startTime = System.currentTimeMillis();
			}
			
			tdTimestamp = monitor.threadDump(fileHandler.getTempFile());
			
			// rename temporary file(s)
			tdFile = fileHandler.renameTemporaryFile(tdTimestamp);
			
			if (logger.isDebugEnabled()){
				logger.debug("Recording snapshot successfully generated into file : " + tdFile.getAbsolutePath());
				long endTime = System.currentTimeMillis();
				logger.debug("Recording snapshot generation total time : " + (endTime - startTime) + " ms");
				SystemHelper.displayMemoryUsage();
			}
			
		} catch (JzrProcessNotAvailableException ex) {
			if (!logger.isDebugEnabled()){
				// process not available : log only 1 line
				logger.warn("Recording snapshot generation failed : process not available. Will retry in "
						+ cfg.getPeriod().getSeconds() + " sec.");
			}else{
				logger.error("Recording snapshot generation failed : process not available. Will retry in "
						+ cfg.getPeriod().getSeconds() + " sec.", 
						ex);
			}
		} catch (Throwable ex) {
			logger.error("Recording snapshot generation failed. Will retry in "
					+ cfg.getPeriod().getSeconds() + " sec.", 
					ex);
		}
        finally{
			if (tdFile == null)
				fileHandler.renameTemporaryFile(-1);
        }
	}

	private void initiate() throws JzrProcessNotAvailableException, JzrGenerationException {
		long startTime = 0;
		
		// initiate only once, even if the below raises an error
		initiated = true;
		
		if (logger.isDebugEnabled()){
			logger.debug("Initializing Recording snapshot session");
			startTime = System.currentTimeMillis();
		}
		
		File processCardFile = new File(cfg.getThreadDumpDirectory() + File.separator + PROCESS_CARD_FILE_NAME);
		try {
			monitor.initiate(processCardFile);
		}catch(JzrProcessNotAvailableException ex) {
			// For JMX and Jstack, re-init is mandatory if remote process is not here.
			initiated = false;
			throw ex;
		}

		initializeTimeZone();

		if (logger.isDebugEnabled()){
			long endTime = System.currentTimeMillis();
			logger.debug("Initializing Recording snapshot session total time : " + (endTime - startTime) +  " ms");
		}
	}

	private void initializeTimeZone() {
		String candidate;
		
		// 1. take it from configuration. Custom time zone like the end user one
		candidate = this.cfg.getTimeZoneId();
		if (JzrTimeZone.isValidTimeZone(candidate)){
			logger.info("Time zone issued from the configuration : " + candidate);
			JzrTimeZone.setTimeZone(TimeZone.getTimeZone(candidate), Origin.CUSTOM);
			return;
		}
		
		// 2. get it from the monitored process (process card file) 
		candidate = this.monitor.getTimeZoneId();
		if (JzrTimeZone.isValidTimeZone(candidate)){
			logger.info("Time zone issued from the process card : " + candidate);
			JzrTimeZone.setTimeZone(TimeZone.getTimeZone(candidate), Origin.PROCESS);
			return;
		}
		
		// 3. take the one from current process
		logger.info("Time zone issued from the current process : " + TimeZone.getDefault().getID());
		JzrTimeZone.setTimeZone(TimeZone.getDefault(), Origin.JZR);
	}

	public static void main(String[] args) {
		JeyzerRecorder ftd = null;
		JzrRecorderConfig config = null;

		logStart();
		try{
			config = JzrRecorderConfigBuilder.newInstance().buildConfig();
		}catch(Exception ex){
			logger.error("Failed to load the Jeyzer Recorder configuration", ex);
			help();
			System.exit(-1);
		}

		// create thread dump/log directory
		try {
			File tdDirectory = new File(config.getThreadDumpDirectory());
			tdDirectory.mkdirs();
		} catch (Exception e) {
			logger.error("Failed to create the recording snapshot directory : "
					+ e.getMessage());
			System.exit(-1);
		}
		
		logger.info(config.toString());
		logger.info("Generating recording snapshots ...");

		// start the archiver (delayed)		
		JzrArchiverTask archiver = new JzrArchiverTask(config);
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
				new JzrArchiverTask.ArchiverThreadFactory());
		executor.scheduleWithFixedDelay(archiver, 
				config.getArchiveZipPeriod().getSeconds(), 
				config.getArchiveZipPeriod().getSeconds(), 
				TimeUnit.SECONDS);

		try{
			ftd = new JeyzerRecorder(config);
		} catch(JzrValidationException ex){
			logger.error("Failed to start Jeyzer Recorder : " + ex.getMessage());
			System.exit(-1);
		} catch(JzrInitializationException ex){
			logger.error("Failed to start Jeyzer Recorder : " + ex.getMessage());
			System.exit(-1);
		} catch(Exception ex){
			logger.error("Failed to start Jeyzer Recorder.", ex);
			System.exit(-1);
		}	

		// start the Jeyzer Recorder now
		ScheduledExecutorService executor2 = Executors.newSingleThreadScheduledExecutor(
				new RecorderThreadFactory(false)
				);
		executor2.scheduleWithFixedDelay(ftd, 
				config.getStartDelay().getSeconds(),
				config.getPeriod().getSeconds(), 
				TimeUnit.SECONDS);

	}

	@Override
	public void run() {
		this.dump();
	}

	public static void help() {
		System.out.println("");
		System.out.println("---------------------------");
		System.out.println("Jeyzer Recorder");
		System.out.println("Usage: java org.jeyzer.recorder.JeyzerRecorder -D" 
				+ JzrRecorderConfigBuilder.CONFIG_FILE_PROPERTY + "=<configuration file>");
		System.out.println("---------------------------");
	}
	
	 private static void logStart() {
		 logger.info("==================================================================");
		 logger.info("");
		 logger.info("Jeyzer Recorder v" + ConfigUtil.loadRecorderVersion() + " started.");
		 logger.info("");
		 logger.info("==================================================================");
	}
}
