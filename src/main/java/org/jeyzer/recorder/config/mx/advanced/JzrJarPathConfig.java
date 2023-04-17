package org.jeyzer.recorder.config.mx.advanced;

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


import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.w3c.dom.Element;

public class JzrJarPathConfig {

	public static final String JZR_JAR_PATHS = "jar_paths";
	
	private JzrSchedulerConfig schedulerConfig;
	private JzrManifestConfig manifestConfig;
	
	public JzrJarPathConfig() {
		// default (fully disabled) if not specified.
		schedulerConfig = new JzrSchedulerConfig();
	}
	
	public JzrJarPathConfig(Element jarPathsNode, String tdDir) throws JzrInitializationException {
		schedulerConfig = new JzrSchedulerConfig(jarPathsNode, tdDir, JZR_JAR_PATHS);
		
		Element manifestNode = ConfigUtil.getFirstChildNode(jarPathsNode, JzrManifestConfig.JZR_MANIFEST);
		if (manifestNode != null)
			manifestConfig = new JzrManifestConfig(manifestNode);
	}
	
	public JzrSchedulerConfig getSchedulerConfig() {
		return this.schedulerConfig;
	}
	
	public JzrManifestConfig getManifestConfig() {
		return manifestConfig; // can be null
	}
}
