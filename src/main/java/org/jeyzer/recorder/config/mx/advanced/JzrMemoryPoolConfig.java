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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JzrMemoryPoolConfig {

	private static final String JZR_NAME = "name";
	// pool name : PS Old Gen, PS Survivor Space...
	private String name;
	
	private boolean enabled = true;
	
	List<JzrMemoryUsageConfig> memoryUsages = new ArrayList<>(3); 

	public JzrMemoryPoolConfig(Element node) throws JzrInitializationException {
		name = ConfigUtil.loadStringValue(node, JZR_NAME);
		
		// memory usages
		NodeList nodes = node.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++){
			Node item = nodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE){
				String usageName = item.getNodeName();
				if (JzrMemoryUsageConfig.JZR_PEAK.equals(usageName)){
					memoryUsages.add(new JzrMemoryUsageConfig(
							JzrMemoryUsageConfig.JZR_PEAK, 
							(Element)item));
				}
				if (JzrMemoryUsageConfig.JZR_USAGE.equals(usageName)){
					memoryUsages.add(new JzrMemoryUsageConfig(
							JzrMemoryUsageConfig.JZR_USAGE, 
							(Element)item));
				}
				if (JzrMemoryUsageConfig.JZR_COLLECTION.equals(usageName)){
					memoryUsages.add(new JzrMemoryUsageConfig(
							JzrMemoryUsageConfig.JZR_COLLECTION,
							(Element)item));
				}

			}
		}		

	}
	
	public String getName(){
		return this.name;
	}
	
	public List<JzrMemoryUsageConfig> getMemoryUsages() {
		return memoryUsages;
	}

	public void disable(){
		enabled = false;		
	}
	
	public boolean isEnabled(){
		return enabled;
	}

}

