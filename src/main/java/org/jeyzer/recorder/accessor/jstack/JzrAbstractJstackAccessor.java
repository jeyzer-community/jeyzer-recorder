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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.Properties;
import java.util.Scanner;

import org.jeyzer.recorder.accessor.JzrAccessor;
import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.error.JzrProcessNotAvailableException;
import org.jeyzer.recorder.accessor.error.JzrValidationException;
import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.util.ConfigUtil;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.util.JzrTimeZone;
import org.jeyzer.recorder.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JzrAbstractJstackAccessor implements JzrAccessor{

	private static final Logger logger = LoggerFactory.getLogger(JzrAbstractJstackAccessor.class);
	
	protected static final String JZR_CAPTURE_DURATION_EMPTY_VALUE = "                             ";  // Time will be inserted here. Large enough to accept capture duration
	protected static final String JZR_CAPTURE_DURATION_INITIAL = FileUtil.JZR_FIELD_CAPTURE_DURATION + JZR_CAPTURE_DURATION_EMPTY_VALUE;
	
	protected static final String JSTACK_COMMAND = File.separator + "bin" + File.separator + "jstack";
	protected static final String JINFO_COMMAND = File.separator + "bin" + File.separator + "jinfo";
	protected static final String JINFO_OPTION = " -sysprops ";
	protected static final String JAVA_HOME = System.getenv("JAVA_HOME");
	
	protected static final String JZR_PROPERTY_PID = "jzr.ext.process.pid";
	
	protected boolean processInfoEnabled = false;
	protected boolean jinfoAvailable = false;
	
	protected String timeZoneId = null;
	
	public JzrAbstractJstackAccessor(JzrRecorderConfig cfg) {
		processInfoEnabled = cfg.isProcessCardEnabled();
	}
	
	@Override
	public void validate() throws JzrValidationException {
		// check the OS
		if (!SystemHelper.isWindows() && !SystemHelper.isUnix() && !SystemHelper.isSolaris())
			throw new JzrValidationException("Operating system not supported : " + SystemHelper.PLATFORM);
		
		// check that Jstack process is available
		File jstack = new File(JAVA_HOME + JSTACK_COMMAND + (SystemHelper.isWindows() ? ".exe" : ""));
		if (!jstack.exists())
			throw new JzrValidationException("Jstack executable not found. JAVA_HOME must refer to a JDK installation (not JRE). Current JAVA_HOME is : " + JAVA_HOME);
		
		// check that Jinfo process is available
		if (processInfoEnabled){
			File jinfo = new File(JAVA_HOME + JINFO_COMMAND + (SystemHelper.isWindows() ? ".exe" : ""));
			jinfoAvailable = jinfo.exists();
			if (!jinfoAvailable)
				logger.warn("Jinfo executable not found. Process card content will be reduced to the minimum.");
		}
	}
	
	@Override
	public void initiate(File file) throws JzrProcessNotAvailableException, JzrGenerationException {
		if (!this.processInfoEnabled)
			return;

        File processCardFile = new File(file.getAbsolutePath());		
        BufferedWriter writer =  null;
        try {
			writer = new BufferedWriter(new OutputStreamWriter(
			        new FileOutputStream(processCardFile), "utf-8"));

    		dumpPid(writer, getMonitoredPid());
    		dumpRecorderVersion(writer);
        	
    		if (this.jinfoAvailable)
    			dumpJinfo(file, writer);
    		
		} catch (Exception e) {
			String msg = "Failed to generate process card file : " + e.getMessage();
			logger.error(msg);
			throw new JzrGenerationException(msg, e);
		} finally {
	        FileUtil.closeWriter(file, writer);
		}
        
        // read the process card to load some properties (like time zone, etc)
        loadProcessProperties(processCardFile);
	}

	@Override
	public String getTimeZoneId() {
		return this.timeZoneId;
	}	
	
	protected abstract int getMonitoredPid();
	
	protected abstract void dumpJinfo(File file, BufferedWriter writer) throws IOException;
	
	protected abstract String getHeaderPrefix();
	
	protected void prepareHeader(BufferedWriter writer, String command) throws IOException{
    	writer.write(getHeaderPrefix() + command);
    	writer.newLine();
    	writer.write(JZR_CAPTURE_DURATION_INITIAL);
    	writer.newLine();
    	writer.write("");
    	writer.newLine();		
	}
	
	protected void insertCaptureDuration(File file, long duration, String command) {
		int pos = getHeaderPrefix().length() 
				+ command.length() 
				+ (SystemHelper.isWindows() ? 2 : 1)  // carriage return
				+ FileUtil.JZR_FIELD_CAPTURE_DURATION.length();
		
		byte[] data = Long.valueOf(duration).toString().getBytes();
		
		try (
				RandomAccessFile f = new RandomAccessFile(file, "rw");
			)
		{
			f.getChannel().position(pos);  	// insert at pos
			f.write(data);   				// override
	    } catch (IOException ex) {       
	            logger.error("Failed to insert capture duration time", ex);
	    }
	}

	protected void dumpPid(BufferedWriter writer, int pid) throws IOException {
    	writer.write(JZR_PROPERTY_PID + "=" + Integer.toString(pid));
    	writer.newLine();
    	writer.flush();
	}
	
	protected void dumpRecorderVersion(BufferedWriter writer) throws IOException {
		String version= ConfigUtil.loadRecorderVersion();
    	writer.write(ConfigUtil.JZR_PROPERTY_RECORDER_VERSION + "=" + version);
    	writer.newLine();
    	writer.flush();
	}

	private void loadProcessProperties(File processCardFile) {
		Properties props = new Properties();
		
		try (
				FileReader reader = new FileReader(processCardFile);
				InputStream is = preprocessPropertiesFile(reader);
			)
		{
			// load the property file
			props.load(is);
		} catch (FileNotFoundException e1) {
			logger.error("Process card not found.", e1);
		} catch (IOException e) {
			logger.error("Failed to load the process card properties file.", e);
		}
		
		// set the timezone, can be null
		this.timeZoneId = props.getProperty(JzrTimeZone.PROPERTY_USER_TIMEZONE); 
	}
	
	private InputStream preprocessPropertiesFile(FileReader reader) throws IOException{
		try (
				Scanner in = new Scanner(reader);
			    ByteArrayOutputStream out = new ByteArrayOutputStream();
			)
		{
		    while(in.hasNext()){
		        out.write(in.nextLine().replace("\\","\\\\").getBytes());		    	
		    	out.write("\n".getBytes());
		    }
		    return new ByteArrayInputStream(out.toByteArray());
		}
	}
}
