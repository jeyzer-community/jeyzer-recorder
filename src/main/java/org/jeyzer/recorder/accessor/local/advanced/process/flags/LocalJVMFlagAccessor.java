package org.jeyzer.recorder.accessor.local.advanced.process.flags;

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


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.config.mx.advanced.JzrJVMFlagConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalJVMFlagAccessor {
	
	protected static final Logger logger = LoggerFactory.getLogger(LocalJVMFlagAccessor.class);

	private ScheduledExecutorService executor;
	private JzrSecurityManager securityMgr;
	private JzrJVMFlagConfig config;
	
	public LocalJVMFlagAccessor(JzrJVMFlagConfig jvmFlagConfig, JzrSecurityManager securityMgr) {
		if (jvmFlagConfig.getSchedulerConfig().isActive())
			logger.debug("Loading LocalJVMFlagAccessor"); // disturbing otherwise
		this.config = jvmFlagConfig;
		this.securityMgr = securityMgr;
	}
	
	public void start() {
		if (!this.config.getSchedulerConfig().isActive())
			return;
		
		logger.debug("Starting LocalModuleAccessor");
		LocalJVMFlagTask task = new LocalJVMFlagTask(
				config, 
				securityMgr
				);
		executor = Executors.newSingleThreadScheduledExecutor(
				new LocalJVMFlagTask.LocalJVMFlagThreadFactory());
		executor.scheduleWithFixedDelay(task, 
				config.getSchedulerConfig().getStartOffset().getSeconds(), 
				config.getSchedulerConfig().getPeriod().getSeconds(),
				TimeUnit.SECONDS);
	}
}
