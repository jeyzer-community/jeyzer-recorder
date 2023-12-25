package org.jeyzer.recorder.accessor;

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

import java.lang.instrument.Instrumentation;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.accessor.jcmd.LocalJcmdAccessor;
import org.jeyzer.recorder.accessor.jmx.JzrJMXAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.JzrAdvancedJMXAccessor;
import org.jeyzer.recorder.accessor.jstack.JstackAccessor;
import org.jeyzer.recorder.accessor.jstack.JstackInShellAccessor;
import org.jeyzer.recorder.accessor.local.LocalAdvancedAccessor;
import org.jeyzer.recorder.accessor.local.LocalAdvancedVTAccessor;
import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.config.jcmd.JzrJcmdConfig;
import org.jeyzer.recorder.config.jmx.JzrJMXConfig;
import org.jeyzer.recorder.config.jmx.advanced.JzrAdvancedJMXConfig;
import org.jeyzer.recorder.config.jstack.JzrJstackConfig;
import org.jeyzer.recorder.config.jstack.JzrJstackInShellConfig;
import org.jeyzer.recorder.config.local.advanced.JzrAdvancedMXAgentConfig;
import org.jeyzer.recorder.config.local.advanced.JzrAdvancedMXVTAgentConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class JzrAccessorBuilder {

	private static final Logger logger = LoggerFactory.getLogger(JzrAccessorBuilder.class);	
	private static final JzrAccessorBuilder builder = new JzrAccessorBuilder();

	private JzrAccessorBuilder() {
	}

	public static JzrAccessorBuilder newInstance() {
		return builder;
	}

	public JzrAccessor buildAccessor(JzrRecorderConfig cfg, Instrumentation instrumentation) throws JzrInitializationException {
		if (cfg instanceof JzrAdvancedJMXConfig){
			logger.debug("Loading Advanced JMX accessor.");
			return new JzrAdvancedJMXAccessor((JzrAdvancedJMXConfig)cfg);
		}
		else if (cfg instanceof JzrAdvancedMXAgentConfig){
			logger.debug("Loading Advanced MX accessor.");
			return new LocalAdvancedAccessor((JzrAdvancedMXAgentConfig)cfg, instrumentation);
		}
		else if (cfg instanceof JzrAdvancedMXVTAgentConfig){
			logger.debug("Loading Advanced MX VT accessor.");
			return new LocalAdvancedVTAccessor((JzrAdvancedMXVTAgentConfig)cfg, instrumentation);
		}
		else if (cfg instanceof JzrJcmdConfig){
			logger.debug("Loading Jcmd accessor.");
			return new LocalJcmdAccessor((JzrJcmdConfig)cfg);
		}
		else if (cfg instanceof JzrJMXConfig){
			logger.debug("Loading JMX accessor.");
			return new JzrJMXAccessor((JzrJMXConfig)cfg);
		}
		else if (cfg instanceof JzrJstackConfig){
			logger.debug("Loading JStack accessor.");
			return new JstackAccessor((JzrJstackConfig)cfg);
		}
		else if (cfg instanceof JzrJstackInShellConfig){
			logger.debug("Loading JStack In Shell accessor.");
			return new JstackInShellAccessor((JzrJstackInShellConfig)cfg);
		}		
		else{
			logger.error("No accessor found.");
			throw new JzrInitializationException("TD accessor creation failed. No accessor found.");
		}
	}
}
