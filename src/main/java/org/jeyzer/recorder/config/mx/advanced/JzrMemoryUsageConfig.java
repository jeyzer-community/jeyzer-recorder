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
import java.util.List;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JzrMemoryUsageConfig {

	// memory usages
	public static final String JZR_PEAK = "peak";
	public static final String JZR_USAGE = "usage";
	public static final String JZR_COLLECTION = "collection";	
	
	// figures
	public static final String JZR_COMMITTED = "committed";
	public static final String JZR_INIT = "init";
	public static final String JZR_MAX = "max";
	public static final String JZR_USED = "used";

	// threshold
	private static final String JZR_TRESHOLD = "threshold";
	private static final String JZR_TRESHOLD_SIZE = "size";
	
	// memory usage name : peak, collection, usage
	private String name;
	
	private List<String> figures = new ArrayList<>(4);
	
	int treshold = -1;
	
	public JzrMemoryUsageConfig(String name, Node node) throws JzrInitializationException {
		this.name = name;
		
		// figures
		NodeList nodes = node.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++){
			Node item = nodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE){
				String figure = item.getNodeName();
				if (JZR_COMMITTED.equals(figure)
						|| JZR_INIT.equals(figure)
						|| JZR_MAX.equals(figure)
						|| JZR_USED.equals(figure)
					)
				{
					figures.add(figure);
				}
			}
		}
		
		// treshold node
		Element tresholdNode = ConfigUtil.getFirstChildNode((Element)node, JZR_TRESHOLD);
		
		if (tresholdNode != null)
			treshold = ConfigUtil.loadIntegerValue(tresholdNode, JZR_TRESHOLD_SIZE); 

	}
	
	public String getName(){
		return this.name;
	}
	
	public List<String> getFigures() {
		return figures;
	}

}
