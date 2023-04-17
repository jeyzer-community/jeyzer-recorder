package org.jeyzer.recorder.accessor.local.advanced.process.jar;

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


import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.config.mx.advanced.JzrJarPathConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalJarPathAccessor {
	
	protected static final Logger logger = LoggerFactory.getLogger(LocalJarPathAccessor.class);

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
		if (!this.config.getSchedulerConfig().isActive())
			return;
		
		logger.debug("Starting LocalJarPathAccessor");
		LocalJarPathTask task = new LocalJarPathTask(
				config, 
				securityMgr,
				instrumentation);
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
				new LocalJarPathTask.LocalJarPathThreadFactory());
		executor.scheduleWithFixedDelay(task, 
				config.getSchedulerConfig().getStartOffset().getSeconds(), 
				config.getSchedulerConfig().getPeriod().getSeconds(),
				TimeUnit.SECONDS);
	}
}
