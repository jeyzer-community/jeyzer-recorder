package org.jeyzer.recorder.accessor.local.advanced.process.jar;

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
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.jeyzer.recorder.accessor.local.advanced.process.LocalTask;
import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.config.mx.advanced.JzrJarPathConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalJarPathTask extends LocalTask {

	private static final Logger logger = LoggerFactory.getLogger(LocalJarPathTask.class);
	
	public static final String JAR_PATHS_DISPLAY = "jar path";

	public static final String JAR_PATHS_FILE = "process-jar-paths.txt";
	private static final String JAR_PATHS_TEMP_FILE = "process-jar-paths-in-progress.tmp";
	
	private Instrumentation instrumentation;
	private JzrJarPathConfig config;
	
	private Map<String, LocalJarPath> paths = new HashMap<>();
	
	public static class LocalJarPathThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("Jeyzer-recorder-jar-path-collector");
			t.setDaemon(true);
			return t;
		}
	}	
	
	public LocalJarPathTask(JzrJarPathConfig config, JzrSecurityManager securityMgr, Instrumentation instrumentation) {
		super(securityMgr);
		this.config = config;
		this.instrumentation = instrumentation;
	}

	@Override
	public void run() {
		try {
			collectJarPaths();
			store(this.config.getSchedulerConfig().getOutputDirectory(), JAR_PATHS_FILE, JAR_PATHS_TEMP_FILE, JAR_PATHS_DISPLAY);
			this.paths.clear();
		}catch(Exception ex) {
			logger.error("Jar path collector failed to collect the paths", ex);
		}
	}

	protected void storeData(BufferedWriter writer) throws IOException {
		List<LocalJarPath> pathsToPrint = new ArrayList<>(this.paths.values());
		Collections.sort(pathsToPrint, new Comparator<LocalJarPath>(){
			@Override
			public int compare(LocalJarPath o1, LocalJarPath o2) {
				return o1.getJarPath().compareTo(o2.getJarPath());
			}
			
		});
		for (LocalJarPath path : pathsToPrint)
			path.store(writer);
	}

	@SuppressWarnings("rawtypes")
	private void collectJarPaths() {
		Class[] classes = instrumentation.getAllLoadedClasses();

		if (logger.isDebugEnabled())
			logger.debug("Number of classes loaded by the JVM : " + classes.length);
		
		for (Class cl : classes) {
			ProtectionDomain domain = cl.getProtectionDomain();
			if (domain == null) {
				continue;
			}
			
			CodeSource source = domain.getCodeSource();
			if (source == null) {
				continue;
			}
			
			URL url = source.getLocation();
			if (url == null) {
				continue;
			}
			
			String path = url.toString();
			if (path.endsWith(".jar") && !this.paths.containsKey(path)) {
				URI uri;
				try {
					uri = url.toURI();
				} catch (URISyntaxException e) {
					logger.error("Failed to convert as URI the URL : " + url);
					continue;
				}
				LocalJarPath jarPath = new LocalJarPath(uri, config.getManifestConfig());
				this.paths.put(path, jarPath);
			}
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Number of jar paths found : " + this.paths.size());
	}
}
