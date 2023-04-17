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
import org.w3c.dom.Element;

public class JzrModuleConfig {

	public static final String JZR_MODULES = "modules";
	
	private JzrSchedulerConfig schedulerConfig;
		
	public JzrModuleConfig() {
		// default (fully disabled) if not specified.
		schedulerConfig = new JzrSchedulerConfig();
	}
	
	public JzrModuleConfig(Element modulesNode, String tdDir) throws JzrInitializationException {
		schedulerConfig = new JzrSchedulerConfig(modulesNode, tdDir, JZR_MODULES);
	}
	
	public JzrSchedulerConfig getSchedulerConfig() {
		return this.schedulerConfig;
	}
}
