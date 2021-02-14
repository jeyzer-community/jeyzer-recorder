package org.jeyzer.recorder.accessor.local.advanced.process.module;

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
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Exports;
import java.lang.module.ModuleDescriptor.Provides;
import java.lang.module.ModuleDescriptor.Requires;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.config.mx.advanced.JzrModuleConfig;
import org.jeyzer.recorder.output.JzrWriterFactory;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalModuleTask implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(LocalModuleTask.class);

	public static final String MODULES_FILE = "process-modules.txt";
	private static final String MODULES_TEMP_FILE = "process-modules-in-progress.tmp";
	
	private Instrumentation instrumentation;
	private JzrSecurityManager securityMgr;
	private JzrModuleConfig config;
	
	private Set<Module> modules = new HashSet<>();
	
	public static class LocalModuleThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("Jeyzer-recorder-module-collector");
			t.setDaemon(true);
			return t;
		}
	}	
	
	public LocalModuleTask(JzrModuleConfig config, JzrSecurityManager securityMgr, Instrumentation instrumentation) {
		this.config = config;
		this.securityMgr = securityMgr; 
		this.instrumentation = instrumentation;
	}

	@Override
	public void run() {
		try {
			collectModules();
			store();
			this.modules.clear();
		}catch(Exception ex) {
			logger.error("Module collector failed to collect the modules", ex);
		}
	}

	private void store() throws IOException, JzrGenerationException {
		File file = new File(this.config.getOutputDirectory() + File.separator + MODULES_TEMP_FILE);
		
		try (
				BufferedWriter writer = getWriter(file);
			)
		{
			storeModules(writer);
		}catch(IOException ex){
			logger.error("Failed to print into the process modules temp file");
			throw ex;
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Process modules file successfully generated into temp file : " + file.getAbsolutePath());
		
		boolean result;
		File finalFile = new File(this.config.getOutputDirectory() + File.separator + MODULES_FILE);
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
				throw new JzrGenerationException("Failed to rename the temp process modules and delete it afterwards");	
			throw new JzrGenerationException("Failed to rename the temp process modules file as : " + finalFile.getAbsolutePath());
		}
	}

	private void storeModules(BufferedWriter writer) throws IOException {
		List<Module> modulesToPrint = new ArrayList<>(this.modules);
		Collections.sort(modulesToPrint, new Comparator<Module>(){
			@Override
			public int compare(Module o1, Module o2) {
				return o1.getDescriptor().name().compareTo(o2.getDescriptor().name());
			}
			
		});
		for (Module module : modulesToPrint)
			storeModule(module, writer);
	}	

	private void storeModule(Module module, BufferedWriter writer) throws IOException {
		ModuleDescriptor desc = module.getDescriptor();
		
		writer.write(desc.name());
		writer.write(';');
		if (desc.version().isPresent())
			writer.write(desc.version().get().toString());
		writer.write(';');
		writer.write(Boolean.toString(desc.isOpen()));
		writer.write(';');
		writer.write(Boolean.toString(desc.isAutomatic()));
		writer.write(';');
		
		// requires
		String delimitor = "";
		for (Requires req : desc.requires()) {
			writer.write(delimitor);
			writer.write(req.name());
			delimitor = ",";
		}
		writer.write(';');

		// exports
		delimitor = "";
		for (Exports export : desc.exports()) {
			writer.write(delimitor);
			writer.write(export.source());
			delimitor = ",";
		}
		writer.write(';');
		
		// uses
		delimitor = "";
		for (String use : desc.uses()) {
			writer.write(delimitor);
			writer.write(use);
			delimitor = ",";
		}
		writer.write(';');
		
		// provides
		delimitor = "";
		for (Provides provide : desc.provides()) {
			writer.write(delimitor);
			writer.write(provide.service());
			delimitor = ",";
		}
		writer.write(';');
		
		// class loader
		if (module.getClassLoader() != null)
			writer.write(module.getClassLoader().getClass().getName());
		
		writer.newLine();
	}

	@SuppressWarnings("rawtypes")
	private void collectModules() {
		Class[] classes = instrumentation.getAllLoadedClasses();

		if (logger.isDebugEnabled())
			logger.debug("Number of classes loaded by the JVM : " + classes.length);
		
		for (Class cl : classes) {
			Module module = cl.getModule();
			// Modules with null name are jar files without module descriptor
			if (module.getName() != null && !this.modules.contains(module))
				this.modules.add(module);
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Number of modules found : " + this.modules.size());
	}
	
	protected BufferedWriter getWriter(File file) throws IOException {
		return JzrWriterFactory.newInstance().createWriter(file, securityMgr);
	}
}
