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

public class JzrMemoryConfig {

	public static final String JZR_MEMORY = "memory";
	public static final String JZR_HEAP = "heap";
	public static final String JZR_NON_HEAP = "non_heap";

	private static final String JZR_OBJ_PENDING_FINALIZATION_COUNT = "object_pending_finalization_count";
		
	// heap and non heap
	private JzrMemoryUsageConfig heapMemoryUsage;  // can be null
	private JzrMemoryUsageConfig nonHeapMemoryUsage;  // can be null
	
	private boolean collectFinalizationCountEnabled = false; 
	
	public JzrMemoryConfig() {
		// default (fully disabled) if not specified. 
	}
	
	public JzrMemoryConfig(Element node) throws JzrInitializationException {
		heapMemoryUsage = loadMemoryUsageConfig(node, JZR_HEAP);
		nonHeapMemoryUsage = loadMemoryUsageConfig(node, JZR_NON_HEAP);
		
		Element finalizationNode = ConfigUtil.getFirstChildNode(node, JZR_OBJ_PENDING_FINALIZATION_COUNT);
		if (finalizationNode != null)
			collectFinalizationCountEnabled = true;
	}

	private JzrMemoryUsageConfig loadMemoryUsageConfig(Element item, String type) throws JzrInitializationException {
		Element typeNode = ConfigUtil.getFirstChildNode(item, type);
		if (typeNode == null){
			return null;
		}
		
		Element node = ConfigUtil.getFirstChildNode(typeNode, JzrMemoryUsageConfig.JZR_USAGE);
		if (node != null)
			return new JzrMemoryUsageConfig(
				JzrMemoryUsageConfig.JZR_USAGE, 
				(Element)node);
		else 
			return null;
	}
	
	public boolean iscollectFinalizationCountEnabled(){
		return this.collectFinalizationCountEnabled;
	}
	
	public JzrMemoryUsageConfig getHeapMemoryUsage(){
		return this.heapMemoryUsage;
	}
	
	public JzrMemoryUsageConfig getNonHeapMemoryUsage(){
		return this.nonHeapMemoryUsage;
	}	
}
