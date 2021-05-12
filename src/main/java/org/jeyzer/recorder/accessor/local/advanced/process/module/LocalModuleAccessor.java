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


import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.config.mx.advanced.JzrModuleConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalModuleAccessor {
	
	protected static final Logger logger = LoggerFactory.getLogger(LocalModuleAccessor.class);

	private ScheduledExecutorService executor;
	private JzrSecurityManager securityMgr;
	private JzrModuleConfig config;
	private Instrumentation instrumentation;
	
	public LocalModuleAccessor(JzrModuleConfig moduleConfig, Instrumentation instrumentation, JzrSecurityManager securityMgr) {
		if (moduleConfig.getSchedulerConfig().isActive())
			logger.debug("Loading LocalModuleAccessor"); // disturbing otherwise
		this.config = moduleConfig;
		this.instrumentation = instrumentation;
		this.securityMgr = securityMgr;
	}
	
	public void start() {
		if (!this.config.getSchedulerConfig().isActive())
			return;
		
		logger.debug("Starting LocalModuleAccessor");
		LocalModuleTask task = new LocalModuleTask(
				config, 
				securityMgr,
				instrumentation);
		executor = Executors.newSingleThreadScheduledExecutor(
				new LocalModuleTask.LocalModuleThreadFactory());
		executor.scheduleWithFixedDelay(task, 
				config.getSchedulerConfig().getStartOffset().getSeconds(), 
				config.getSchedulerConfig().getPeriod().getSeconds(),
				TimeUnit.SECONDS);
	}
}
