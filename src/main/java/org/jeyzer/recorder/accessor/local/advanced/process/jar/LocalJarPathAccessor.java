package org.jeyzer.recorder.accessor.local.advanced.process.jar;

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


import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.config.mx.advanced.JzrJarPathConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalJarPathAccessor {
	
	protected static final Logger logger = LoggerFactory.getLogger(LocalJarPathAccessor.class);

	private ScheduledExecutorService executor;
	private JzrSecurityManager securityMgr;
	private JzrJarPathConfig config;
	private Instrumentation instrumentation;
	
	public LocalJarPathAccessor(JzrJarPathConfig jarPathConfig, Instrumentation instrumentation, JzrSecurityManager securityMgr) {
		logger.debug("Loading LocalJarPathAccessor");
		this.config = jarPathConfig;
		this.instrumentation = instrumentation;
		this.securityMgr = securityMgr;
	}
	
	public void start() {
		if (!this.config.isActive())
			return;
		
		logger.debug("Starting LocalJarPathAccessor");
		LocalJarPathTask task = new LocalJarPathTask(
				config, 
				securityMgr,
				instrumentation);
		executor = Executors.newSingleThreadScheduledExecutor(
				new LocalJarPathTask.LocalJarPathThreadFactory());
		executor.scheduleWithFixedDelay(task, 
				config.getStartOffset().getSeconds(), 
				config.getPeriod().getSeconds(),
				TimeUnit.SECONDS);
	}
}
