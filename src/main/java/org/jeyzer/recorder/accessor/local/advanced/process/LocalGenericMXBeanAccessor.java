package org.jeyzer.recorder.accessor.local.advanced.process;

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





import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jeyzer.recorder.accessor.local.advanced.JzrLocalBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractGenericMXBeanAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrGenericMXBeanConfig;

public class LocalGenericMXBeanAccessor extends JzrAbstractGenericMXBeanAccessor  implements JzrLocalBeanFieldAccessor{

	public LocalGenericMXBeanAccessor(List<JzrGenericMXBeanConfig> list) {
		super(list);
	}

	@Override
	public void collect() {
		if (!this.isSupported())
			return;
		
		if (collectDone)
			return;

		try {
			// collect all in one go
			MBeanServerConnection server = ManagementFactory.getPlatformMBeanServer();
			for (JzrGenericMXBeanConfig config : this.dynamicBeanConfigs){
				collectAttributes(server, config, processDynamicMXAttributes);
			}
		} finally {
			collectDone = true;
		}
	}
	
	public void collectProcessCardFigures() {
		MBeanServerConnection server = ManagementFactory.getPlatformMBeanServer();
		this.processCardMXAttributes = new HashMap<>();
		for (JzrGenericMXBeanConfig config : this.processCardBeanConfigs){
			collectAttributes(server, config, this.processCardMXAttributes);
		}
	}
	
	private void collectAttributes(MBeanServerConnection server, JzrGenericMXBeanConfig config, Map<String, String> collectedAttributes) {
		Set<ObjectName> objectNames = null;
		
		objectNames = queryNames(server, config.getObjectName());
		if (objectNames == null)
			return;

        try {
    		String value;
    		
            for(ObjectName objectName: objectNames){
            	for (String attribute : config.getAttributes()){
            		value = getAttributeAsString(server, objectName, attribute);
            		if (value != null){            			
            			String name = buildAttributeName(objectName, attribute);
            			collectedAttributes.put(name, value);
            		}
            		// else we could remove the entry from the config attributes once done
            	}
            }
		}  catch (IOException e) {
			logger.error("Failed to access MX bean.", e); //should not happen
		}		
	}
}
