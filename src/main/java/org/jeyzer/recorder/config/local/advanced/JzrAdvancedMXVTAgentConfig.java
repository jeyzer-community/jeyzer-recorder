package org.jeyzer.recorder.config.local.advanced;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;

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
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.jeyzer.recorder.util.ConfigUtil;
import org.w3c.dom.Element;

public class JzrAdvancedMXVTAgentConfig extends JzrAdvancedConfig{

	public static final String VT_DUMP_FORMAT_TXT = "txt";
	public static final String VT_DUMP_FORMAT_JSON = "json";
	
	private static final Logger logger = LoggerFactory.getLogger(JzrAdvancedMXVTAgentConfig.class);	
	
	private static final String JZR_MX_ADVANCED_VT = "mx_advanced_vt";
	private static final String JZR_FORMAT = "format";
	
	private String format;

	
	public JzrAdvancedMXVTAgentConfig(Element recorder) throws JzrInitializationException {
		super(recorder);
		
		// snapshot node
		Element snapshotNode = ConfigUtil.getFirstChildNode(recorder, JZR_SNAPSHOT);

		// methods node
		Element methodsNode = ConfigUtil.getFirstChildNode(snapshotNode, JZR_METHODS);
		
		// mx_advanced_vt node
		Element mxAvancedVTNode = ConfigUtil.getFirstChildNode(methodsNode, JZR_MX_ADVANCED_VT);
		
		String value = ConfigUtil.getAttributeValue(mxAvancedVTNode, JZR_FORMAT);
		if (value == null || value.isEmpty()) {
			logger.error("Error - Format parameter empty. Must be txt or json.");
			throw new JzrInitializationException("Error - Format parameter empty. Must be txt or json.");
		}
		this.format = value.toLowerCase();
		if (!VT_DUMP_FORMAT_TXT.equals(this.format) && !VT_DUMP_FORMAT_JSON.equals(this.format)) {
			logger.error("Error - Format parameter invalid : " + value + ". Must be txt or json.");
			throw new JzrInitializationException("Error - Format parameter empty. Must be txt or json.");
		}
	}

	public String getFormat() {
		return format;
	}
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder(); 
		
		b.append(super.toString());
	    b.append("\tFormat                    : " + format+ "\n");
	    
	    return b.toString();
	}

	@Override
	protected String getAdvancedNodeName() {
		return JZR_MX_ADVANCED_VT;
	}

}