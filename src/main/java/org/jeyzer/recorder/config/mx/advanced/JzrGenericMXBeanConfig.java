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
import java.util.StringTokenizer;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

public class JzrGenericMXBeanConfig extends JzrBeanFieldConfig{

	private static final Logger logger = LoggerFactory.getLogger(JzrGenericMXBeanConfig.class);	

	public static final String CONFIG_NODE_NAME = "mx_parameters";
	
	private static final String NAME_FIELD = "name";
	private static final String ATTRIBUTES_FIELD = "attributes";	
	
	private ObjectName objectName;
	private List<String> attributes;
	
	public JzrGenericMXBeanConfig(String category, String name, NamedNodeMap nodeAttributes) throws JzrInitializationException {
		super(category, name);
		objectName = loadName(nodeAttributes);
		attributes = loadAttributes(nodeAttributes);
	}
	
	private List<String> loadAttributes(NamedNodeMap attributes) throws JzrInitializationException {
		String value = loadField(attributes, ATTRIBUTES_FIELD);
		
		List<String> attributeList = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(value, ",");
		while(tokenizer.hasMoreTokens()){
			String attribute = tokenizer.nextToken().trim();
			attributeList.add(attribute);
		}
		
		return attributeList;
	}

	private ObjectName loadName(NamedNodeMap attributes) throws JzrInitializationException {
		String name = loadField(attributes, NAME_FIELD);
		
		try {
			return new ObjectName(name);
		} catch (MalformedObjectNameException e) {
			logger.error("Configuration error - parameter " + NAME_FIELD + " with value " + name + " is not an object name.");
			throw new JzrInitializationException("Configuration error - Parameter " + NAME_FIELD + " with value " + name + " is not an object name.", e);
		}
	}

	public String loadField(NamedNodeMap attributes, String name) throws JzrInitializationException{
		String value;
		
		Attr attr = (Attr)attributes.getNamedItem(name);
		if (attr == null) {
			logger.error("Configuration error - attribute " + name + " is missing in MX configuration.");
			throw new JzrInitializationException("Configuration error - attribute " + name + " is missing in MX configuration");
		}
		
		value = ConfigUtil.resolveValue(attr.getNodeValue());
		if (value == null || value.isEmpty()) {
			logger.error("Configuration error - parameter " + name + " is empty in MX configuration.");
			throw new JzrInitializationException("Configuration error - Parameter " + name + " is empty in MX configuration");
		}		
		
		return value;
	}

	public ObjectName getObjectName() {
		return objectName;
	}

	public List<String> getAttributes() {
		return attributes;
	}	
	
}
