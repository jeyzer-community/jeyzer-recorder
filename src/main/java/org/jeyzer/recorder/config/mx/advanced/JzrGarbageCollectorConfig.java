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

public class JzrGarbageCollectorConfig {

	private static final String JZR_NAME = "name";

	public static final String JZR_COUNT = "count";
	public static final String JZR_TIME = "time";	
	
	private static final String JZR_COLLECTION = "collection";
	private static final String JZR_LAST_GC_INFO = "last_gc_info";
	
	// pool name : PS Scavenge, PS MarkSweep...
	private String name;
	
	private List<String> attributes = new ArrayList<>(3);
	private JzrLastGCInfoConfig lastGCInfoCfg;
	
	private boolean enabled = true;

	public JzrGarbageCollectorConfig(Element node) throws JzrInitializationException {
		name = ConfigUtil.loadStringValue(node, JZR_NAME);
		
		// attributes : count, time, last gc info
		Element collectionNode = ConfigUtil.getFirstChildNode(node, JZR_COLLECTION);
		NodeList nodes = collectionNode.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++){
			Node item = nodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE){
				attributes.add(item.getNodeName());
			}
		}
		
		// last gc info
		Element lastGCInfoNode = ConfigUtil.getFirstChildNode(node, JZR_LAST_GC_INFO);
		if (lastGCInfoNode != null){
			lastGCInfoCfg =  new JzrLastGCInfoConfig(lastGCInfoNode);
		}
	}
	
	public String getName(){
		return this.name;
	}
	
	public JzrLastGCInfoConfig getLastGCInfoConfig() {
		return lastGCInfoCfg;
	}

	public List<String> getAttributes(){
		return this.attributes;
	}
	
	public void disable(){
		enabled = false;		
	}
	
	public boolean isEnabled(){
		return enabled;
	}

}

