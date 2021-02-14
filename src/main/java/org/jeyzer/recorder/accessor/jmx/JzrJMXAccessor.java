package org.jeyzer.recorder.accessor.jmx;

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
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.MalformedURLException;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.error.JzrProcessNotAvailableException;
import org.jeyzer.recorder.accessor.error.JzrValidationException;
import org.jeyzer.recorder.accessor.internal.JeyzerInternalsAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.process.RuntimePropertiesAccessor;
import org.jeyzer.recorder.accessor.mx.JzrAbstractAccessor;
import org.jeyzer.recorder.config.jmx.JzrJMXConfig;
import org.jeyzer.recorder.output.JzrWriterFactory;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.util.SystemHelper;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class JzrJMXAccessor extends JzrAbstractAccessor {
	
	protected static final Logger logger = LoggerFactory.getLogger(JzrJMXAccessor.class);
	
	// default - JDK 6+ VM
	private static final String JDK6_FIND_DEAD_LOCK_METHOD_NAME = "findDeadlockedThreads";

	private static final String JZR_HEADER_PREFIX = "Full Java thread dump";

	private JzrJMXConfig cfg;

	protected MBeanServerConnection server;
	private JMXConnector jmxc;
	
	public JzrJMXAccessor(JzrJMXConfig cfg) {
		this.cfg = cfg;
	}

	@Override
	public void initiate(File file) throws JzrProcessNotAvailableException, JzrGenerationException {
		
		if (initialized)
			return;
		
		try {
			// connect to the remote JVM
			connect();

			// create the JMX thread bean
			createTDMXBean();

			// check that remote JVM supports accessors
			checkTDMXSupportedFeatures();
			
			// create process card and get time zone
			processCardDump(file);
			
		} finally {
			processCardClose();
		}
		initialized = true;
	}

	private void processCardDump(File file) throws JzrGenerationException {	
		try (
				BufferedWriter writer = getWriter(file);
			)
		{
			processCardDump(writer);
		}catch(IOException ex){
			String msg = "Failed to print process card file";
			logger.error(msg);
			throw new JzrGenerationException(msg, ex);
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Process card file successfully generated into file : " + file.getAbsolutePath());
	}
	
	// ca be overriden, must then be super called.
	protected void processCardDump(BufferedWriter writer) throws IOException {
		JeyzerInternalsAccessor internalsAccessor = new JeyzerInternalsAccessor(); 
		internalsAccessor.dumpRecorderValues(writer);
		
		// Runtime properties
		RuntimePropertiesAccessor runtimeAccessor = new RuntimePropertiesAccessor(this.cfg);
		if (!runtimeAccessor.isSupported())
			return;
		
		runtimeAccessor.collect(server);
		runtimeAccessor.printValue(writer);
		
		this.timeZoneId = runtimeAccessor.getTimeZoneId();
	}

	protected void processCardClose() {
		disconnect();
		// nothing else to do : runtimeAccessor was local variable
	}

	/**
	 * Prints the thread dump information
	 * @throws JzrGenerationException 
	 */
	@Override	
	public long threadDump(File file) throws JzrProcessNotAvailableException, JzrGenerationException {
		long timestamp = -1;
		
		try {
			createDumpFile(file);

			// connect to the remote JVM
			connect();

			// create the JMX thread bean
			createTDMXBean();
			
			// collect all figures
			timestamp = collectTDInfo();

			// dump it into file
			printTDInfo();
			
		} finally {
			close();
		}
		
		return timestamp;
	}

	@Override
	protected void close() {
		super.close();
		disconnect();
	}

	@Override
	public void validate() throws JzrValidationException {
		// nothing to do
	}	

	@Override
	public String getTimeZoneId() {
		return this.timeZoneId;
	}
	

	@Override
	protected BufferedWriter getWriter(File file) throws IOException {
		return JzrWriterFactory.newInstance().createWriter(file);
	}
	
	protected long collectTDInfo() {
		long startTime, endTime;
		
        if (logger.isDebugEnabled())
        	logger.debug("Accessing thread dump info from MX bean");
		
        if (this.canDumpLocks){
    		startTime = System.currentTimeMillis();
    		tinfos = tmbean.dumpAllThreads(true, true); // synchronized lock monitor support
    		endTime = System.currentTimeMillis();
            
    		int i =0;
    		this.tids = new long[tinfos.length];
            for (ThreadInfo ti : tinfos) {
            	this.tids[i] = ti.getThreadId();
            	i++;
            }
        	
        }else{
            this.tids = tmbean.getAllThreadIds();
            
        	startTime = System.currentTimeMillis();
    		tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
    		endTime = System.currentTimeMillis();
        }
        
        // get threads in deadlock
        if (this.cfg.isDeadlockCaptureEnabled() && this.canDumpLocks){
        	long[] ids = tmbean.findDeadlockedThreads();
           	if (ids != null)
           		for (int i=0; i<ids.length; i++){
           			deadlockTids.add(ids[i]);
           		}
        }
		
        this.captureDuration = endTime - startTime; 
        
        if (logger.isDebugEnabled())
        	logger.debug("MX bean thread dump info access time : " + captureDuration + " ms");
		
		return startTime + (captureDuration) / 2;
	}

	/**
	 * Connect to a JMX agent of a given URL.
	 * @throws JzrGenerationException 
	 * @throws IOException, JzrProcessNotAvailableException
	 */
	private void connect() throws JzrProcessNotAvailableException, JzrGenerationException {
		long startTime = 0;

        if (logger.isDebugEnabled()){
    		logger.debug("Connecting to JMX server : " + cfg.getConnectionUrl());
        	startTime = System.currentTimeMillis();
        }
		
		try {
			JMXServiceURL jmxurl = new JMXServiceURL("rmi", "", 0,
					cfg.getConnectionUrl());
			if (cfg.getEnvironment().isEmpty())
				this.jmxc = JMXConnectorFactory.connect(jmxurl);
			else
				this.jmxc = JMXConnectorFactory.connect(jmxurl,
						cfg.getEnvironment());
			this.server = jmxc.getMBeanServerConnection();
			
		} catch (MalformedURLException e) {
			String msg = "Malformed URL error : " + e.getMessage();
			logger.error(msg);
			throw new JzrGenerationException(msg, e);
		} catch (IOException e) {
			if (SystemHelper.isRootCause(java.net.ConnectException.class, e)){
				// process not available. Do not log error
				throw new JzrProcessNotAvailableException(e);
			}
			else{
				String msg = "Monitoring communication error : " + e.getMessage();
				logger.error(msg);
				throw new JzrGenerationException(msg, e);
			}
		} 
		
        if (logger.isDebugEnabled()){
        	long endTime = System.currentTimeMillis();
        	logger.debug("JMX server connection time : " + (endTime - startTime) + " ms");
        }
	}

	protected void createTDMXBean() throws JzrGenerationException {
		long startTime = 0;
		
        if (logger.isDebugEnabled()){
    		logger.debug("Creating MX bean : " + ManagementFactory.THREAD_MXBEAN_NAME);
        	startTime = System.currentTimeMillis();
        }

        try{
    		tmbean = ManagementFactory.newPlatformMXBeanProxy(
    				server, 
    				ManagementFactory.THREAD_MXBEAN_NAME,
    				ThreadMXBean.class);
        }catch (IOException e) {
			String msg = "Failed to create Threading MX bean : " + e.getMessage();
			logger.error(msg);
			throw new JzrGenerationException(msg, e);
        }
		
        if (logger.isDebugEnabled()){
        	long endTime = System.currentTimeMillis();
        	logger.debug("MX bean creation time : " + (endTime - startTime) + " ms");
        }		
	}

	/*
	 * Must be called only once in the process life
	 */
	protected void checkTDMXSupportedFeatures() throws JzrGenerationException {
		ObjectName threadingObj;
		long startTime = 0;
		
		if (logger.isDebugEnabled()){
			logger.debug("Parsing Thread MX bean info");
			startTime = System.currentTimeMillis();
		}

		try {
			threadingObj = new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME);
		} catch (MalformedObjectNameException e) {
			String msg = "Failed to create Threading MX bean info : " + e.getMessage();
			logger.error(msg);
			throw new JzrGenerationException(msg, e);
		}        
        
		try {
			MBeanOperationInfo[] mopis = server.getMBeanInfo(threadingObj)
					.getOperations();

			// look for findDeadlockedThreads operations :
			// if findDeadlockedThreads operation doesn't exist,
			// the target VM is running on JDK 5 and details about
			// synchronizers and locks cannot be dumped.
			boolean found = false;
			for (MBeanOperationInfo op : mopis) {
				if (op.getName().equals(JDK6_FIND_DEAD_LOCK_METHOD_NAME)) {
					found = true;
					break;
				}
			}
			if (found) {
				// Print lock info if both object monitor usage
				// and synchronizer usage are supported.
				if (tmbean.isObjectMonitorUsageSupported()
						&& tmbean.isSynchronizerUsageSupported()) 
					canDumpLocks = true;
			}
		} catch (Exception e) {
			String msg = "Failed to parse Thread MX bean info : " + e.getMessage();
			logger.error(msg);
			throw new JzrGenerationException(msg, e);
		}
		
        if (logger.isDebugEnabled()){
        	long endTime = System.currentTimeMillis();
        	logger.debug("Thread MX bean parsing time : " + (endTime - startTime) + " ms");
        }
	}	
	
	private void printTDInfo() throws JzrGenerationException {
		long startTime = 0;
		
		if (logger.isDebugEnabled()) {
			logger.debug("Printing collected info");
			startTime = System.currentTimeMillis();
		}
		
		try{
			if (this.canDumpLocks)
				printFileHeader(this.getHeaderPrefix() + " with locks info from : ");
			else
				printFileHeader(this.getHeaderPrefix()+ " from : ");
			
			writeln("");
			
			for (ThreadInfo ti : tinfos) {
				printThreadInfo(ti);

				if (this.canDumpLocks){
					LockInfo[] syncs = ti.getLockedSynchronizers();
					printLockInfo(syncs);
					writeln("");
				}
			}
			
		}catch(IOException ex){
			String msg = "Failed to print thread dump info : " + ex.getMessage();
			logger.error(msg);
			throw new JzrGenerationException(msg, ex);
		}
		
		if (logger.isDebugEnabled()) {
			long endTime = System.currentTimeMillis();
			logger.debug("Print duration : " + (endTime - startTime) + " ms");
		}
	}

	private void printThreadInfo(ThreadInfo ti) throws IOException {
		// print thread information
		printThreadHeader(ti);

		// print stack trace with locks
		StackTraceElement[] stacktrace = ti.getStackTrace();
		MonitorInfo[] monitors = ti.getLockedMonitors();
		for (int i = 0; i < stacktrace.length; i++) {
			StackTraceElement ste = stacktrace[i];
			writeln(INDENT + "at " + ste.toString());
			for (MonitorInfo mi : monitors) {
				if (mi.getLockedStackDepth() == i) {
					writeln(INDENT + "  - locked " + mi);
				}
			}
		}
		writeln("");
	}

	/**
	 * Disconnect from JMX agent.
	 */
	protected void disconnect() {
		long startTime = 0;
		
        if (logger.isDebugEnabled()){
    		logger.debug("Disconnecting from JMX server : " + cfg.getConnectionUrl());
        	startTime = System.currentTimeMillis();
        }

		try {
			this.jmxc.close();
		} catch (Exception e) {
		}

        if (logger.isDebugEnabled()){
        	long endTime = System.currentTimeMillis();
        	logger.debug("JMX server disconnection time : " + (endTime - startTime) + " ms");
        }		
		
		try {
			this.out.close();
		} catch (Exception e) {
		}		
        
		this.jmxc = null;
		this.server = null;
	}	
	
	protected String getHeaderPrefix(){
		return JZR_HEADER_PREFIX;
	}
	
	protected void printFileHeader(String prefix) throws IOException{
		writeln(prefix + this.cfg.getConnectionUrl());
		
		if (this.cfg.isCaptureDurationEnabled())
			printCaptureDuration();
	}
	
	private void printCaptureDuration() throws IOException {
		long duration = computeGlobalCaptureDuration();

		if (logger.isDebugEnabled())
        	logger.debug("Global JMX info access time : " + duration + " ms");
	
		writeln(FileUtil.JZR_FIELD_CAPTURE_DURATION + duration);
	}

	protected long computeGlobalCaptureDuration() {
		return this.captureDuration;
	}

	protected void printThreadHeader(ThreadInfo ti) throws IOException {

		StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\""
				+ " Id=" + ti.getThreadId() + " in " + ti.getThreadState());
		if (ti.getLockName() != null) {
			sb.append(" on lock=" + ti.getLockName());
		}
		if (ti.isSuspended()) {
			sb.append(" (suspended)");
		}
		if (ti.isInNative()) {
			sb.append(" (running in native)");
		}
		writeln(sb.toString());

		if (ti.getLockOwnerName() != null) {
			writeln(INDENT + " owned by " + ti.getLockOwnerName() + " Id="
					+ ti.getLockOwnerId());
		}
		
		if (canDumpLocks && cfg.isDeadlockCaptureEnabled() && deadlockTids.contains(ti.getThreadId()))
			writeln(INDENT + DEADLOCK);
	}

	private void printLockInfo(LockInfo[] locks) throws IOException {
		writeln(INDENT + "Locked synchronizers: count = " + locks.length);
		for (LockInfo li : locks) {
			writeln(INDENT + "  - " + li);
		}
		writeln("");
	}
}
