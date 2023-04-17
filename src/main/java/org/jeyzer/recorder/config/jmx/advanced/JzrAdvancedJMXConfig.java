package org.jeyzer.recorder.config.jmx.advanced;

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


import org.jeyzer.recorder.config.jmx.JzrJMXConfig;
import org.jeyzer.recorder.config.mx.advanced.JzrAdvancedConfig;
import org.w3c.dom.Element;

public class JzrAdvancedJMXConfig extends JzrAdvancedConfig{

	private JzrJMXConfig jmxConfig;
	
	public JzrAdvancedJMXConfig(Element recorder) throws Exception {
		super(recorder);
		jmxConfig = new JzrJMXConfig(recorder);
	}
	
	public JzrJMXConfig getTDJMXConfig(){
		return this.jmxConfig;
	}

}
