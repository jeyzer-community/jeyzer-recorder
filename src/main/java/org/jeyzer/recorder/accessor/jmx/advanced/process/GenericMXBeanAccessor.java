package org.jeyzer.recorder.accessor.jmx.advanced.process;

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





import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jeyzer.recorder.accessor.jmx.advanced.JzrMXBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractGenericMXBeanAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrGenericMXBeanConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class GenericMXBeanAccessor extends JzrAbstractGenericMXBeanAccessor implements JzrMXBeanFieldAccessor{

	private static final Logger logger = LoggerFactory.getLogger(GenericMXBeanAccessor.class);

	public GenericMXBeanAccessor(List<JzrGenericMXBeanConfig> list) {
		super(list);
	}

	@Override
	public void collect(MBeanServerConnection server) {
		if (!this.isSupported())
			return;
		
		if (collectDone)
			return;

		this.captureDuration = 0;		
		try {
			// collect all in one go
			for (JzrGenericMXBeanConfig config : this.dynamicBeanConfigs){
				collectAttributes(server, config, processDynamicMXAttributes);
			}
		} finally {
			collectDone = true;
		}
	}
	
	private void collectAttributes(MBeanServerConnection server, JzrGenericMXBeanConfig config, Map<String, String> collectedAttributes) {
		Set<ObjectName> objectNames = null;
		
		long startTime = System.currentTimeMillis();
		objectNames = queryNames(server, config.getObjectName());
		if (objectNames == null)
			return;
		
		long endTime = System.currentTimeMillis();
		this.captureDuration += endTime - startTime;

		if (logger.isDebugEnabled())
			logger.debug("Generic MX bean object name query time : " + (endTime - startTime) + " ms");

        try {
    		String value;
    		
            for(ObjectName objectName: objectNames){
            	for (String attribute : config.getAttributes()){
            		startTime = System.currentTimeMillis();
            		
            		value = getAttributeAsString(server, objectName, attribute);
            		if (value != null){
            			endTime = System.currentTimeMillis();
            			this.captureDuration += endTime - startTime;

            			if (logger.isDebugEnabled())
            				logger.debug("Generic MX bean object name access time for MX attribute " + attribute + " on object " + objectName + " : " + (endTime - startTime) + " ms");            			
            			
            			String name = buildAttributeName(objectName, attribute);
            			collectedAttributes.put(name, value);
            		}
            		// else we could remove the entry from the config attributes once done
            	}
            }
		}  catch (IOException e) {
			logger.error("Failed to access MX bean.", e);
		}
	}

	public void collectProcessCardFigures(MBeanServerConnection server) {
		this.processCardMXAttributes = new HashMap<>();
		for (JzrGenericMXBeanConfig config : this.processCardBeanConfigs){
			collectAttributes(server, config, this.processCardMXAttributes);
		}
	}

	@Override
	public long getCaptureDuration() {
		return captureDuration;
	}

	@Override
	public boolean checkSupport(MBeanServerConnection server) {
		// if MX configurations are defined, we assume that accessed beans may appear at any time
		return super.checkSupport();
	}

}
