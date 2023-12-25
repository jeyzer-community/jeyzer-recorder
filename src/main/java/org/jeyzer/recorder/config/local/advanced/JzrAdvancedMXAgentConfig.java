package org.jeyzer.recorder.config.local.advanced;

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


import org.jeyzer.recorder.config.mx.advanced.JzrAdvancedConfig;
import org.w3c.dom.Element;

public class JzrAdvancedMXAgentConfig extends JzrAdvancedConfig{

	private static final String JZR_MX_ADVANCED = "mx_advanced";
	
	public JzrAdvancedMXAgentConfig(Element recorder) throws Exception {
		super(recorder);
	}

	@Override
	protected String getAdvancedNodeName() {
		return JZR_MX_ADVANCED;
	}

}
