package org.jeyzer.recorder.accessor.mx.advanced.process;

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





import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.jeyzer.recorder.accessor.mx.advanced.JzrAbstractBeanFieldAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrGenericMXBeanConfig;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.util.JMXUtil;

public abstract class JzrAbstractGenericMXBeanAccessor extends JzrAbstractBeanFieldAccessor{

	public static final String ACCESSOR_NAME = "process:mx_parameters";
	
	private static final String MX_PARAM = FileUtil.JZR_FIELD_JZ_PREFIX + "mx:";
	
	protected List<JzrGenericMXBeanConfig> processCardBeanConfigs;
	protected List<JzrGenericMXBeanConfig> dynamicBeanConfigs;
	
	protected Map<String,String> processCardMXAttributes = null; // created on demand
	protected Map<String,String> processDynamicMXAttributes = new HashMap<>();
	protected boolean collectDone = false;
	protected boolean printDone = false;
	
	public JzrAbstractGenericMXBeanAccessor(List<JzrGenericMXBeanConfig> list) {
		this.processCardBeanConfigs = list;
		this.dynamicBeanConfigs = new ArrayList<JzrGenericMXBeanConfig>();
		
		// we assume that accessed beans may appear at any time
		this.supported = true;
	}

	public void addDynamicFieldConfig(JzrGenericMXBeanConfig config) {
		this.dynamicBeanConfigs.add(config);
	}

	public void printProcessCardValues(BufferedWriter out) throws IOException {
		for(Entry<String, String> entry : processCardMXAttributes.entrySet())
			printValue(out, 
					entry.getKey() + "=",
					entry.getValue(),
					"" // should not happen
					);
	}
	
	public void printValue(BufferedWriter out) throws IOException {
		if (printDone)
			return;
		
		try {
			// print all in  one go
			for(Entry<String, String> entry : processDynamicMXAttributes.entrySet())
				printValue(out, 
						MX_PARAM + entry.getKey() + FileUtil.JZR_FIELD_EQUALS,
						entry.getValue(),
						MX_PARAM + entry.getKey() + FileUtil.JZR_FIELD_EQUALS // should not happen
						);			
		} finally {
			printDone = true;
		}
	}
	
	public void close() {
		this.processDynamicMXAttributes.clear();
		this.collectDone = false;
		this.printDone = false;
	}

	public void processCardClose() {
		if (this.processCardMXAttributes != null)
			this.processCardMXAttributes.clear();
		this.processCardMXAttributes = null;
	}

	public boolean checkSupport() {
		this.supported = !this.dynamicBeanConfigs.isEmpty();
		return this.supported;
	}
	
	protected Set<ObjectName> queryNames(MBeanServerConnection server, ObjectName objectName) {
		Set<ObjectName> objectNames = null;
				
		if (logger.isDebugEnabled())
			logger.debug("Querying MX bean process object names : " + objectName.toString());
		
        try {
        	objectNames = server.queryNames(objectName, null);
		} catch (IOException e) {
			logger.error("Failed to query MX bean object names.", e);
			return null;
		}
        
        if (objectNames.isEmpty()){
        	logger.warn("MX bean object names is empty for object name query : " +  objectName.toString());
        	return null;
        }
        
		return objectNames;
	}
	
	protected String getAttributeAsString(MBeanServerConnection server, ObjectName objectName, String objectAttribute) throws IOException {
		Object value = null;
		
		if (logger.isDebugEnabled())
			logger.debug("Accessing MX bean process object attribute : " + objectName.toString() + ":" + objectAttribute);
		
		try {
			value = JMXUtil.getAttribute(server, objectName, objectAttribute);
		} catch (AttributeNotFoundException ex) {
			logger.warn("MX attribute " + objectAttribute + " not found on object : " + objectName);
			return null;
		}
		
		if (value instanceof CompositeData) {
			logger.warn("MX attribute " + objectAttribute + " on object " + objectName + " is instance of composite data : cannot convert it to String.");
			return null;
		}
		return value.toString();
	}
	
	protected String buildAttributeName(ObjectName objectName, String attribute) {
		StringBuilder name = new StringBuilder(objectName.getDomain());
		
		name.append(FileUtil.JZR_FIELD_SEPARATOR);
		
		for (String keyValue : getPropertyKeyValues(objectName)){
			name.append(keyValue);
			name.append(FileUtil.JZR_FIELD_SEPARATOR);
		}
		
		name.append(attribute);
		
		return name.toString();
	}

	private List<String> getPropertyKeyValues(ObjectName objectName) {
		List<String> keyValues = new ArrayList<>();
		
		// get in right order, parse objectName.getKeyPropertyListString
		String KeyPropertyList = objectName.getKeyPropertyListString();
		
		int start = KeyPropertyList.indexOf("=", 0);
		int end = 0;
		
		while(end != -1){
			end = KeyPropertyList.indexOf(",", start);
			String value;
			if (end != -1)
				value = KeyPropertyList.substring(start+1, end);
			else
				value = KeyPropertyList.substring(start+1);
			keyValues.add(value);
			start = KeyPropertyList.indexOf("=", end);
		}
		
		return keyValues;
	}	
	
}
