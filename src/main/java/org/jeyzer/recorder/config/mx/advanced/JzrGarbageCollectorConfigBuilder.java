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





import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JzrGarbageCollectorConfigBuilder {

	private static final String JZR_GARBAGE_COLLECTORS = "garbage_collectors";
	private static final String JZR_COLLECTOR = "collector";
	
	private static final JzrGarbageCollectorConfigBuilder builder = new JzrGarbageCollectorConfigBuilder();
	
	private JzrGarbageCollectorConfigBuilder(){}
	
	public static JzrGarbageCollectorConfigBuilder newInstance(){
		return builder;
	}
	
	public List<JzrGarbageCollectorConfig> buildGarbargeCollectorConfigs(Element advancedJmxNode) throws JzrInitializationException {
		List<JzrGarbageCollectorConfig> collectors = new ArrayList<>(2);
		
		// collectors node
		Element collectorsNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JZR_GARBAGE_COLLECTORS);

		NodeList nodes = collectorsNode.getElementsByTagName(JZR_COLLECTOR);
		for (int i=0; i<nodes.getLength(); i++){
			collectors.add(new JzrGarbageCollectorConfig((Element)nodes.item(i)));
		}
		
		return collectors;
	}
}
