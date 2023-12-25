package org.jeyzer.recorder.config.jcmd;

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


import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.jeyzer.recorder.util.ConfigUtil;
import org.w3c.dom.Element;

public class JzrJcmdConfig extends JzrRecorderConfig {

	public static final String JCMD_TXT = "txt";
	public static final String JCMD_JSON = "json";
	
	private static final Logger logger = LoggerFactory.getLogger(JzrJcmdConfig.class);	
	
	private static final String JZR_JCMD = "jcmd";
	private static final String JZR_FORMAT = "format";
	
	private String format;
	
	public JzrJcmdConfig(Element recorder) throws JzrInitializationException {
		super(recorder);
		
		// snapshot node
		Element snapshotNode = ConfigUtil.getFirstChildNode(recorder, JZR_SNAPSHOT);

		// methods node
		Element methodsNode = ConfigUtil.getFirstChildNode(snapshotNode, JZR_METHODS);
		
		// jcmd node
		Element jcmdNode = ConfigUtil.getFirstChildNode(methodsNode, JZR_JCMD);
		
		String value = ConfigUtil.getAttributeValue(jcmdNode, JZR_FORMAT);
		if (value == null || value.isEmpty()) {
			logger.error("Error - Format parameter empty. Must be txt or json.");
			throw new JzrInitializationException("Error - Format parameter empty. Must be txt or json.");
		}
		this.format = value.toLowerCase();
		if (!JCMD_TXT.equals(this.format) && !JCMD_JSON.equals(this.format)) {
			logger.error("Error - Format parameter invalid : " + value + ". Must be txt or json.");
			throw new JzrInitializationException("Error - Format parameter empty. Must be txt or json.");
		}
	}
	
	@Override
	public boolean isEncryptionEnabled() {
		return false;
	}
	
	@Override
	public boolean isEncryptionKeyPublished() {
		return false;
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
}
