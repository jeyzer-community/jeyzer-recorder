package org.jeyzer.recorder.accessor.local;

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
import java.lang.instrument.Instrumentation;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.accessor.error.JzrValidationException;
import org.jeyzer.recorder.accessor.internal.JeyzerInternalsAccessor;
import org.jeyzer.recorder.accessor.local.advanced.JzrLocalBeanFieldAccessor;
import org.jeyzer.recorder.accessor.local.advanced.JzrLocalBeanFieldAccessorBuilder;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalGarbageCollectorAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalGenericMXBeanAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalJeyzerAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalMemoryAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalMemoryPoolAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalRuntimePropertiesAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.flags.LocalJVMFlagAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.jar.LocalJarPathAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.jar.LocalManifestAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.module.LocalModuleAccessor;
import org.jeyzer.recorder.accessor.local.advanced.system.LocalDiskSpaceAccessor;
import org.jeyzer.recorder.accessor.local.advanced.system.LocalDiskWriteAccessor;
import org.jeyzer.recorder.accessor.local.advanced.thread.JzrLocalThreadBeanFieldAccessor;
import org.jeyzer.recorder.accessor.local.advanced.thread.JzrThreadBeanFieldAccessorBuilder;
import org.jeyzer.recorder.accessor.mx.JzrAbstractAccessor;
import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.config.mx.advanced.JzrAdvancedConfig;
import org.jeyzer.recorder.output.JzrWriterFactory;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.util.JzrTimeZone;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalAdvancedAccessor extends JzrAbstractAccessor {
	
	private static final String JZR_HEADER_PREFIX = "Full Agent Advanced Java thread dump";
	
	protected static final Logger logger = LoggerFactory.getLogger(LocalAdvancedAccessor.class);
	
	private JzrAdvancedConfig cfg;
	
	private JzrSecurityManager securityMgr;
	
	private LocalMemoryPoolAccessor memPoolAccessor;
	private LocalGarbageCollectorAccessor garbageCollectorAccessor;
	
	private LocalDiskSpaceAccessor diskSpaceAccessor;
	
	private LocalDiskWriteAccessor diskWriteAccessor;
	
	private List<JzrLocalThreadBeanFieldAccessor> threadFieldBeanAccessors;
	private List<JzrLocalBeanFieldAccessor> fieldBeanAccessors;
	
	private LocalMemoryAccessor memoryAccessor;
	
	private LocalJeyzerAccessor jeyzerAccessor;
	
	private LocalGenericMXBeanAccessor genericMXAccessor;
	
	private LocalManifestAccessor manifestAccessor;
	
	private LocalJarPathAccessor jarPathAccessor;
	
	private LocalModuleAccessor moduleAccessor;
	
	private LocalJVMFlagAccessor jvmFlagAccessor;
	
	public LocalAdvancedAccessor(JzrAdvancedConfig cfg, Instrumentation instrumentation) throws JzrInitializationException {
		this.cfg = cfg;
		
		securityMgr = new JzrSecurityManager(cfg.getSecurityCfg(), cfg.getThreadDumpDirectory());
		
		memoryAccessor = new LocalMemoryAccessor(cfg.getMemoryConfig());
		
		memPoolAccessor = new LocalMemoryPoolAccessor(cfg.getPoolConfigs());
		
		garbageCollectorAccessor = new LocalGarbageCollectorAccessor(cfg.getCollectorConfigs());
		
		diskSpaceAccessor = new LocalDiskSpaceAccessor(cfg.getDiskSpaceConfigs());
		
		diskWriteAccessor = new LocalDiskWriteAccessor(cfg.isDiskWriteTimeEnabled());
		
		jeyzerAccessor = new LocalJeyzerAccessor();
		
		genericMXAccessor = new LocalGenericMXBeanAccessor(cfg.getProcessCardBeanFieldConfigs());
		
		manifestAccessor= new LocalManifestAccessor(cfg.getManifestConfig());
		
		threadFieldBeanAccessors = JzrThreadBeanFieldAccessorBuilder.newInstance().buildBeanFieldConfigsforCategory(
				cfg.getThreadBeanFieldConfigs());
		
		fieldBeanAccessors = JzrLocalBeanFieldAccessorBuilder.newInstance().buildBeanFieldConfigsforCategory(
				cfg.getBeanFieldConfigs(), jeyzerAccessor, genericMXAccessor);
		
		jarPathAccessor = new LocalJarPathAccessor(cfg.getJarPathConfig(), instrumentation, securityMgr);
		
		moduleAccessor = new LocalModuleAccessor(cfg.getModuleConfig(), instrumentation, securityMgr);
		
		jvmFlagAccessor = new LocalJVMFlagAccessor(cfg.getJVMFlagConfig(), securityMgr);
	}

	@Override
	public long threadDump(File file) throws JzrGenerationException {
		long startTime, endTime;
		
		try {
			createDumpFile(file);

    		startTime = System.currentTimeMillis();
			
			// collect all figures
			collectTDInfo();

    		endTime = System.currentTimeMillis();
            this.captureDuration = endTime - startTime; 
            
            if (logger.isDebugEnabled())
            	logger.debug("MX bean info global access time : " + captureDuration + " ms");
    		
			// dump it into file
			printTDInfo(file);
			
		} finally {
			close();
		}
		
		return startTime + (captureDuration) / 2;
	}

	@Override
	public void validate() throws JzrValidationException {
		// nothing to do
	}

	@Override
	public void initiate(File file) throws JzrGenerationException {
		if (initialized)
			return;
		
		try {
			// create the MX thread bean
			tmbean = ManagementFactory.getThreadMXBean();

			// check that JVM supports accessors
			checkTDMXSupportedFeatures();
			
			// create process card and get time zone
			processCardDump(file);
			
			// Start the jar path collector. Stopped implicitly when recorder shutdown occurs
			jarPathAccessor.start();
			
			// Start the modules collector. Stopped implicitly when recorder shutdown occurs
			moduleAccessor.start();
			
			// Start the JVM flags collector. Stopped implicitly when recorder shutdown occurs
			jvmFlagAccessor.start();
			
		} finally {
			processCardClose();
		}
		initialized = true;
	}

	private void processCardClose() {
		jeyzerAccessor.processCardClose();
		genericMXAccessor.processCardClose();
		manifestAccessor.processCardClose();
	}

	@Override
	public String getTimeZoneId() {
		return System.getProperty(JzrTimeZone.PROPERTY_USER_TIMEZONE);
	}
	
	@Override
	protected BufferedWriter getWriter(File file) throws IOException {
		return JzrWriterFactory.newInstance().createWriter(file, securityMgr);
	}
	
	private void processCardDump(File file) throws JzrGenerationException {
		JeyzerInternalsAccessor internalsAccessor = new JeyzerInternalsAccessor();
		LocalRuntimePropertiesAccessor runtimeAccessor;

		runtimeAccessor = new LocalRuntimePropertiesAccessor(this.cfg);
		if (!runtimeAccessor.isSupported())
			return;
		
		runtimeAccessor.collect();
		jeyzerAccessor.collectProcessCardFigures();
		genericMXAccessor.collectProcessCardFigures();
		manifestAccessor.collectProcessCardFigures();
		
		try (
				BufferedWriter writer = getWriter(file);
			)
		{
			runtimeAccessor.printValue(writer);
			jeyzerAccessor.printProcessCardValues(writer);
			genericMXAccessor.printProcessCardValues(writer);
			manifestAccessor.printProcessCardValues(writer); 
			internalsAccessor.dumpRecorderValues(writer);
		}catch(IOException ex){
			String msg = "Failed to print process card file";
			logger.error(msg);
			throw new JzrGenerationException(msg, ex);
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Process card file successfully generated into file : " + file.getAbsolutePath());
		
		this.timeZoneId = runtimeAccessor.getTimeZoneId();
	}
	
	private void printTDInfo(File file) throws JzrGenerationException {
		if (logger.isDebugEnabled())
			logger.debug("Printing collected info");
		
		this.diskWriteAccessor.collectPrintStart();
		
		try{
			printFileHeader();
			
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
			this.diskWriteAccessor.failDiskWriteInfo();
			String msg = "Failed to print thread dump info : " + ex.getMessage();
			logger.error(msg);
			throw new JzrGenerationException(msg, ex);
		}
		finally{
			try {
				this.out.close();
			} catch (Exception e) {
			}
			this.diskWriteAccessor.collectPrintEnd(file);
		}
	}

	protected void checkTDMXSupportedFeatures() throws JzrGenerationException {

		// look for findDeadlockedThreads operations :
		// if findDeadlockedThreads operation doesn't exist,
		// the target VM is running on JDK 5 and details about
		// synchronizers and locks cannot be dumped.
		boolean jdk6AndAbove = false;
		try{
			this.tmbean.findDeadlockedThreads();
			jdk6AndAbove = true;
		}catch(Exception e){
		}

		if (jdk6AndAbove
				&& tmbean.isObjectMonitorUsageSupported()
				&& tmbean.isSynchronizerUsageSupported()) 
			canDumpLocks = tmbean.isObjectMonitorUsageSupported() 
							&& tmbean.isSynchronizerUsageSupported();
		
		this.memoryAccessor.checkSupport();
		
		this.memPoolAccessor.checkPools();
		
		this.garbageCollectorAccessor.checkCollectors();
		
		// No need to check disk spaces : 
		//   always enabled even if disk space cannot be accessed yet
		
		this.jeyzerAccessor.checkSupport();
		
		this.genericMXAccessor.checkSupport();
		
		// thread bean accessors
		checkThreadBeanFieldAccessors();
		
		// bean accessors
		checkBeanFieldAccessors();
	}	

	/*
	 * Must be called only once in the process lifetime
	 */
	private void checkThreadBeanFieldAccessors(){
		long[] ids = null;
		List<JzrLocalThreadBeanFieldAccessor> notSupportedBeans = new ArrayList<>();
		
		// check each thread field accessor
		for (JzrLocalThreadBeanFieldAccessor threadFieldBeanAccessor : threadFieldBeanAccessors){
			if (ids == null)
				ids = tmbean.getAllThreadIds();
			 threadFieldBeanAccessor.checkSupport(this.tmbean, this.jeyzerAccessor, ids);
			 if (!threadFieldBeanAccessor.isSupported())
				 notSupportedBeans.add(threadFieldBeanAccessor);
		}
		
		// remove unsupported ones
		for (JzrLocalThreadBeanFieldAccessor threadFieldBeanAccessor : notSupportedBeans){
			threadFieldBeanAccessors.remove(threadFieldBeanAccessor);
		}
	}
	
	private void checkBeanFieldAccessors() {
		List<JzrLocalBeanFieldAccessor> notSupportedBeans = new ArrayList<>();
		
		// check each field accessor
		for (JzrLocalBeanFieldAccessor fieldBeanAccessor : fieldBeanAccessors){
			fieldBeanAccessor.checkSupport();
			 if (!fieldBeanAccessor.isSupported())
				 notSupportedBeans.add(fieldBeanAccessor);
		}
		
		// remove unsupported ones
		for (JzrLocalBeanFieldAccessor fieldBeanAccessor : notSupportedBeans){
			fieldBeanAccessors.remove(fieldBeanAccessor);
		}
	}	
	
	@Override
	protected void close() {
		super.close();
		
		// not required for Jeyzer MX bean as it is cached 
		
		for (JzrLocalThreadBeanFieldAccessor threadFieldBeanAccessor : threadFieldBeanAccessors)
			threadFieldBeanAccessor.close();
		this.genericMXAccessor.close();
		this.diskSpaceAccessor.close();
		this.memoryAccessor.close();
		this.memPoolAccessor.close();
		this.garbageCollectorAccessor.close();
	}
	
	protected void collectTDInfo() {
        if (logger.isDebugEnabled())
        	logger.debug("Accessing thread dump info from MX bean");
		
        // 1. collect thread dumps
        if (this.canDumpLocks){
    		tinfos = tmbean.dumpAllThreads(true, true); // synchronized lock monitor support
    		int i =0;
    		this.tids = new long[tinfos.length];
            for (ThreadInfo ti : tinfos) {
            	this.tids[i] = ti.getThreadId();
            	i++;
            }
        }else{
            this.tids = tmbean.getAllThreadIds();
    		tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
        }
        
        // get threads in deadlock
        if (this.cfg.isDeadlockCaptureEnabled() && this.canDumpLocks){
           	long[] ids = tmbean.findDeadlockedThreads();
           	if (ids != null)
           		for (int i=0; i<ids.length; i++){
           			deadlockTids.add(ids[i]);
           	}
        }
        
		// 2. collect thread dump info
		for (JzrLocalThreadBeanFieldAccessor threadFieldBeanAccessor : threadFieldBeanAccessors){
			threadFieldBeanAccessor.collect(this.tmbean, this.jeyzerAccessor, this.tids);
		}

		// 3. collect heap/non heap memory info
		memoryAccessor.collect();
		
		// 4. collect memory pool info
		memPoolAccessor.collect();
		
		// 5. collect garbage collector info
		garbageCollectorAccessor.collect();

		// 6. collect disk space info
		diskSpaceAccessor.collect();
		
		// 7. collect process info, including Jeyzer and MX generic ones
		for (JzrLocalBeanFieldAccessor fieldBeanAccessor : fieldBeanAccessors){
			fieldBeanAccessor.collect();
		}
	}

	protected String getHeaderPrefix(){
		return JZR_HEADER_PREFIX;
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
		
		// print advanced thread data 
		for (JzrLocalThreadBeanFieldAccessor threadFieldBeanAccessor : threadFieldBeanAccessors){
			threadFieldBeanAccessor.printValue(out, ti.getThreadId());
		}
	}
	
	protected void printFileHeader() throws IOException {
		if (this.canDumpLocks)
			writeln(this.getHeaderPrefix() + " with locks info");
		else
			writeln(this.getHeaderPrefix());

		if (this.cfg.isCaptureDurationEnabled())
			printCaptureDuration();

		for (JzrLocalBeanFieldAccessor fieldBeanAccessor : fieldBeanAccessors){
			fieldBeanAccessor.printValue(this.out);
		}

		memoryAccessor.printMemoryInfo(this.out);
		
		memPoolAccessor.printPoolMemoryInfo(this.out);
		
		garbageCollectorAccessor.printGarbageCollectorInfo(this.out);
		
		diskSpaceAccessor.printDiskSpaceInfo(this.out);
		
		diskWriteAccessor.printDiskWriteInfo(this.out);
	}	

	private void printCaptureDuration() throws IOException {
		if (logger.isDebugEnabled())
        	logger.debug("Global JMX info access time : " + this.captureDuration + " ms");
	
		writeln(FileUtil.JZR_FIELD_CAPTURE_DURATION + this.captureDuration);
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
	
	private void printLockInfo(LockInfo[] locks) throws IOException {
		writeln(INDENT + "Locked synchronizers: count = " + locks.length);
		for (LockInfo li : locks) {
			writeln(INDENT + "  - " + li);
		}
		writeln("");
	}	
}
