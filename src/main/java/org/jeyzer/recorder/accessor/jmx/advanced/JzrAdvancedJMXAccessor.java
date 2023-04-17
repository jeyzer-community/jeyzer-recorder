package org.jeyzer.recorder.accessor.jmx.advanced;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Recorder
 * --
 * Copyright (C) 2020, 2023 Jeyzer
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
import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.accessor.jmx.JzrJMXAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.process.GarbageCollectorAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.process.GenericMXBeanAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.process.JeyzerAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.process.MemoryAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.process.MemoryPoolAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.thread.JzrThreadBeanFieldAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.thread.JzrThreadBeanFieldAccessorBuilder;
import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.config.jmx.advanced.JzrAdvancedJMXConfig;
import org.jeyzer.recorder.output.JzrWriterFactory;

public class JzrAdvancedJMXAccessor extends JzrJMXAccessor {

	private static final String JZR_HEADER_PREFIX = "Full Advanced Java thread dump";

	private List<JzrThreadBeanFieldAccessor> threadFieldBeanAccessors;
	private List<JzrMXBeanFieldAccessor> fieldBeanAccessors;
	
	private MemoryAccessor memAccessor;
	private MemoryPoolAccessor memPoolAccessor;
	private GarbageCollectorAccessor garbageCollectorAccessor;
	
	private JeyzerAccessor jeyzerAccessor;
	private GenericMXBeanAccessor genericAccessor;
	
	private JzrSecurityManager securityMgr;

	public JzrAdvancedJMXAccessor(JzrAdvancedJMXConfig jmxcfg) throws JzrInitializationException {
		super(jmxcfg.getTDJMXConfig());

		securityMgr = new JzrSecurityManager(jmxcfg.getSecurityCfg(), jmxcfg.getThreadDumpDirectory());
		
		memAccessor = new MemoryAccessor(jmxcfg.getMemoryConfig());
		
		memPoolAccessor = new MemoryPoolAccessor(jmxcfg.getPoolConfigs());
		
		garbageCollectorAccessor = new GarbageCollectorAccessor(jmxcfg.getCollectorConfigs());

		jeyzerAccessor = new JeyzerAccessor();
		
		genericAccessor = new GenericMXBeanAccessor(jmxcfg.getProcessCardBeanFieldConfigs());
		
		threadFieldBeanAccessors = JzrThreadBeanFieldAccessorBuilder.newInstance().buildBeanFieldConfigsforCategory(
				jmxcfg.getThreadBeanFieldConfigs());
		
		fieldBeanAccessors = JzrJMXBeanFieldAccessorBuilder.newInstance().buildBeanFieldConfigsforCategory(
				jmxcfg.getBeanFieldConfigs(), jeyzerAccessor, genericAccessor);
	}

	@Override
	protected String getHeaderPrefix() {
		return JZR_HEADER_PREFIX;
	}

	@Override
	public void printThreadHeader(ThreadInfo ti) throws IOException {
		super.printThreadHeader(ti);
		
		for (JzrThreadBeanFieldAccessor threadFieldBeanAccessor : threadFieldBeanAccessors){
			threadFieldBeanAccessor.printValue(out, ti.getThreadId());
		}
	}
	
	@Override
	protected BufferedWriter getWriter(File file) throws IOException {
		return JzrWriterFactory.newInstance().createWriter(file, securityMgr);
	}
	
	@Override
	protected void processCardDump(BufferedWriter writer) throws IOException{
		super.processCardDump(writer);
		
		genericAccessor.collectProcessCardFigures(server);
		
		if (jeyzerAccessor.isSupported())
			jeyzerAccessor.collectProcessCardFigures(server);
		
		genericAccessor.printProcessCardValues(writer);
		if (jeyzerAccessor.isSupported())
			jeyzerAccessor.printProcessCardValues(writer);
	}
	
	@Override
	protected void processCardClose() {
		super.processCardClose();
		
		this.jeyzerAccessor.processCardClose();
		this.genericAccessor.processCardClose();
	}
	
	@Override
	protected long collectTDInfo() {
		
		// 1. collect thread dumps
		long timestamp = super.collectTDInfo();
		
		// 2. collect thread dump info
		for (JzrThreadBeanFieldAccessor threadFieldBeanAccessor : threadFieldBeanAccessors){
			threadFieldBeanAccessor.collect(this.server, this.tmbean, this.jeyzerAccessor, this.tids);
		}
		
		// 3. collect memory info
		memAccessor.collect(this.server);
		
		// 4. collect memory pool info
		memPoolAccessor.collect(this.server);
		
		// 5. collect garbage collector info
		garbageCollectorAccessor.collect(this.server);
		
		// 6. collect process info, including Jeyzer and MX generic ones
		for (JzrMXBeanFieldAccessor fieldBeanAccessor : fieldBeanAccessors){
			fieldBeanAccessor.collect(this.server);
		}
		
		return timestamp;
	}
	
	@Override
	protected long computeGlobalCaptureDuration() {
		// 1. get thread dump capture duration
		long duration = super.computeGlobalCaptureDuration();
		
		// 2. get thread dump info capture duration
		for (JzrThreadBeanFieldAccessor threadFieldBeanAccessor : threadFieldBeanAccessors){
			duration += threadFieldBeanAccessor.getCaptureDuration();
		}
		
		// 3. get memory info capture duration
		duration += memAccessor.getCaptureDuration();
		
		// 4. get memory pool info capture duration
		duration += memPoolAccessor.getCaptureDuration();
		
		// 5. get garbage collector info capture duration
		duration += garbageCollectorAccessor.getCaptureDuration();
		
		// 6. get process info capture duration
		for (JzrMXBeanFieldAccessor fieldBeanAccessor : fieldBeanAccessors){
			duration += fieldBeanAccessor.getCaptureDuration();
		}
		
		return duration;
	}
	
	@Override
	protected void printFileHeader(String prefix) throws IOException {
		super.printFileHeader(prefix);

		for (JzrMXBeanFieldAccessor fieldBeanAccessor : fieldBeanAccessors){
			fieldBeanAccessor.printValue(out);
		}
		
		memAccessor.printMemoryInfo(this.out);
		
		memPoolAccessor.printPoolMemoryInfo(this.out);
		
		garbageCollectorAccessor.printGarbageCollectorInfo(this.out);
	}

	@Override
	protected void checkTDMXSupportedFeatures() throws JzrGenerationException {
		try {
			
			super.checkTDMXSupportedFeatures();
			
			this.memAccessor.checkSupport(this.server);
			
			this.memPoolAccessor.checkPools(this.server);
			
			this.garbageCollectorAccessor.checkCollectors(this.server);

			this.jeyzerAccessor.checkSupport(this.server);
			
			this.genericAccessor.checkSupport(this.server);
			
			// thread bean accessors
			checkThreadBeanFieldAccessors();
			
			// bean accessors
			checkBeanFieldAccessors();

		} catch (Exception e) {
			String msg = "Failed to check JMX supported features : " + e.getMessage();
			throw new JzrGenerationException(msg, e);
		}
	}
	
	private void checkBeanFieldAccessors() {
		List<JzrMXBeanFieldAccessor> notSupportedBeans = new ArrayList<>();
		
		// check each field accessor
		for (JzrMXBeanFieldAccessor fieldBeanAccessor : fieldBeanAccessors){
			fieldBeanAccessor.checkSupport(server);
			 if (!fieldBeanAccessor.isSupported())
				 notSupportedBeans.add(fieldBeanAccessor);
		}
		
		// remove unsupported ones
		for (JzrMXBeanFieldAccessor fieldBeanAccessor : notSupportedBeans){
			fieldBeanAccessors.remove(fieldBeanAccessor);
		}
	}

	/*
	 * Must be called only once in the process lifetime
	 */
	private void checkThreadBeanFieldAccessors(){
		long[] ids = null;
		List<JzrThreadBeanFieldAccessor> notSupportedBeans = new ArrayList<>();
		
		// check each thread field accessor
		for (JzrThreadBeanFieldAccessor threadFieldBeanAccessor : threadFieldBeanAccessors){
			if (ids == null)
				ids = tmbean.getAllThreadIds();
			 threadFieldBeanAccessor.checkSupport(server, this.tmbean, this.jeyzerAccessor, ids);
			 if (!threadFieldBeanAccessor.isSupported())
				 notSupportedBeans.add(threadFieldBeanAccessor);
		}
		
		// remove unsupported ones
		for (JzrThreadBeanFieldAccessor threadFieldBeanAccessor : notSupportedBeans){
			threadFieldBeanAccessors.remove(threadFieldBeanAccessor);
		}
	}
	
	/**
	 * Disconnect from JMX agent.
	 */
	@Override	
	protected void close() {
		for (JzrThreadBeanFieldAccessor threadFieldBeanAccessor : this.threadFieldBeanAccessors)
			threadFieldBeanAccessor.close();
		this.memAccessor.close();
		this.memPoolAccessor.close();
		this.garbageCollectorAccessor.close();
		this.jeyzerAccessor.close();
		this.genericAccessor.close();
		super.close();
	}

}
