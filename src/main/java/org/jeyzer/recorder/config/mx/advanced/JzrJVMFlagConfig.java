package org.jeyzer.recorder.config.mx.advanced;

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


import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.w3c.dom.Element;

public class JzrJVMFlagConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(JzrJVMFlagConfig.class);

	public static final String JZR_JVM_FLAGS = "jvm_flags";
	
	private JzrSchedulerConfig schedulerConfig;
	
	public JzrJVMFlagConfig() {
		// default (fully disabled) if not specified.
		this.schedulerConfig = new JzrSchedulerConfig();
	}
	
	public JzrJVMFlagConfig(Element jvmFlagsNode, String tdDir) throws JzrInitializationException {
		this.schedulerConfig = new JzrSchedulerConfig(jvmFlagsNode, tdDir, JZR_JVM_FLAGS);
		validateSunHotspotAvailability();
	}

	public JzrSchedulerConfig getSchedulerConfig() {
		return this.schedulerConfig;
	}
	
	private void validateSunHotspotAvailability() {
        try {
			Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
		} catch (ClassNotFoundException e) {
			logger.warn("JVM Flag access disabled : the sun management package is not available on the current JVM.");
			schedulerConfig.setActive(false);
		}
	}
}
