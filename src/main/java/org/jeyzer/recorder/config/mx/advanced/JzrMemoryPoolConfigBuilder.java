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

public class JzrMemoryPoolConfigBuilder {

	private static final String JZR_MEMORY_POOLS = "memory_pools";
	private static final String JZR_POOL = "pool";
	
	private static final JzrMemoryPoolConfigBuilder builder = new JzrMemoryPoolConfigBuilder();
	
	private JzrMemoryPoolConfigBuilder(){}
	
	public static JzrMemoryPoolConfigBuilder newInstance(){
		return builder;
	}
	
	public List<JzrMemoryPoolConfig> buildPoolConfigs(Element advancedJmxNode) throws JzrInitializationException {
		List<JzrMemoryPoolConfig> pools = new ArrayList<>(10);
		
		// pools node
		Element poolsNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JZR_MEMORY_POOLS);

		NodeList nodes = poolsNode.getElementsByTagName(JZR_POOL);
		for (int i=0; i<nodes.getLength(); i++){
			pools.add(new JzrMemoryPoolConfig((Element)nodes.item(i)));
		}
		
		return pools;
	}
}
