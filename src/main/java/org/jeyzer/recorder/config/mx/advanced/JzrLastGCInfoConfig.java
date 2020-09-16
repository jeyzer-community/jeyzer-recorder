package org.jeyzer.recorder.config.mx.advanced;

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





import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JzrLastGCInfoConfig {

	public static final String JZR_ID = "id";
	public static final String JZR_DURATION = "duration";
	public static final String JZR_START_TIME = "start_time";
	public static final String JZR_END_TIME = "end_time";
	public static final String JZR_BEFORE = "before";
	public static final String JZR_AFTER = "after";
	
	private static final String JZR_POOL = "pool";
	
	private static List<String> excludeLastGCInfoFields = Arrays.asList(
			JzrLastGCInfoConfig.JZR_AFTER, 
			JzrLastGCInfoConfig.JZR_BEFORE);
	
	private List<String> attributes = new ArrayList<>(5);
	private List<JzrMemoryPoolConfig> beforePoolConfig = new ArrayList<>(2);
	private List<JzrMemoryPoolConfig> afterPoolConfig = new ArrayList<>(2);
	
	public JzrLastGCInfoConfig(Element node) throws JzrInitializationException {
		// attributes : id, duration...
		NodeList nodes = node.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++){
			Node item = nodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE){
				if (!excludeLastGCInfoFields.contains(item.getNodeName()))
					attributes.add(item.getNodeName());
			}
		}
		
		// before and after GC memory info
		loadMemoryPoolConfig(node, beforePoolConfig, JZR_BEFORE);
		loadMemoryPoolConfig(node, afterPoolConfig, JZR_AFTER);
	}

	public List<JzrMemoryPoolConfig> getBeforePoolConfig() {
		return beforePoolConfig;
	}

	public List<JzrMemoryPoolConfig> getAfterPoolConfig() {
		return afterPoolConfig;
	}

	private void loadMemoryPoolConfig(Element node, List<JzrMemoryPoolConfig> poolConfigs, String type) throws JzrInitializationException {
		Element beforeOrAfterNode = ConfigUtil.getFirstChildNode(node, type);
		if (beforeOrAfterNode != null){
			NodeList nodes = beforeOrAfterNode.getElementsByTagName(JZR_POOL);
			for (int i=0; i<nodes.getLength(); i++){
				poolConfigs.add(new JzrMemoryPoolConfig((Element)nodes.item(i)));
			}
		}
	}

	public List<String> getAttributes(){
		return this.attributes;
	}
	
}
