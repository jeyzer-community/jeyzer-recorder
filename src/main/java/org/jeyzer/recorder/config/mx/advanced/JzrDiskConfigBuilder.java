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





import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JzrDiskConfigBuilder {

	private static final String JZR_DISK = "disk";
	private static final String JZR_DISK_SPACES = "disk_spaces";
	private static final String JZR_DISK_WRITE = "jeyzer_agent_recording_write_time";
	
	private static final JzrDiskConfigBuilder builder = new JzrDiskConfigBuilder();
	
	private JzrDiskConfigBuilder(){}
	
	public static JzrDiskConfigBuilder instance(){
		return builder;
	}
	
	public List<JzrDiskSpaceConfig> buildDiskSpaceConfigs(Element advancedJmxNode) throws JzrInitializationException {
		List<JzrDiskSpaceConfig> diskSpaces = new ArrayList<>(3);

		// disk node
		Element diskNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JZR_DISK);
		if (diskNode == null)
			return diskSpaces;
		
		// disk_spaces node
		Element diskSpaceNode = ConfigUtil.getFirstChildNode(diskNode, JZR_DISK_SPACES);
		if (diskSpaceNode == null)
			return diskSpaces;

		NodeList nodes = diskSpaceNode.getElementsByTagName(JzrDiskSpaceConfig.JZR_DISK_SPACE);
		for (int i=0; i<nodes.getLength(); i++){
			diskSpaces.add(new JzrDiskSpaceConfig((Element)nodes.item(i)));
		}
		
		return diskSpaces;
	}
	
	public boolean isDiskSpaceWriteEnabled(Element advancedJmxNode){
		// disk node
		Element diskNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JZR_DISK);
		if (diskNode == null)
			return false;
		
		Element diskWriteNode = ConfigUtil.getFirstChildNode(diskNode, JZR_DISK_WRITE);
		return diskWriteNode != null;
	}
}
