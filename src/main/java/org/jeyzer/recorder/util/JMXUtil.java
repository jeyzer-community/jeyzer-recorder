package org.jeyzer.recorder.util;

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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;

import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class JMXUtil {

	private static final String STRING_MAP_KEY = "key";
	private static final String STRING_MAP_VALUE = "value";
	
	private static final Logger logger = LoggerFactory.getLogger(JMXUtil.class);	
	
	private JMXUtil(){
	}
	
	public static double getDoubleAttribute(MBeanServerConnection server, ObjectName objectName,
			String objectAttribute) throws IOException {
		Object value = null;
		try {
			value = getAttribute(server, objectName, objectAttribute);
		} catch (AttributeNotFoundException ex) {
			return -1;
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		return -1;
	}

	public static int getIntegerAttribute(MBeanServerConnection server, ObjectName objectName,
			String objectAttribute) throws IOException {
		Object value = null;
		try {
			value = getAttribute(server, objectName, objectAttribute);
		} catch (AttributeNotFoundException ex) {
			return -1;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return -1;
	}	
	
	public static long getLongAttribute(MBeanServerConnection server, ObjectName objectName, String objectAttribute)
			throws IOException {
		Object value = null;

		try {
			value = getAttribute(server, objectName, objectAttribute);
		} catch (AttributeNotFoundException ex) {
			return -1;
		}

		if (value instanceof Number) {
			return ((Number) value).longValue();
		}
		return -1;
	}
	
	public static Object getAttribute(MBeanServerConnection server, ObjectName objectName, String objectAttribute)
			throws IOException, AttributeNotFoundException {
		Object value = null;

		try {
			value = server.getAttribute(objectName, objectAttribute);
		} catch (InstanceNotFoundException ex) {
			throw new RuntimeException(ex);
		} catch (MBeanException ex) {
			throw new RuntimeException(ex);
		} catch (ReflectionException ex) {
			throw new RuntimeException(ex);
		}
		return value;
	}		

	public static ObjectName getOperatingSystemMXBeanName() {
		try {
			return new ObjectName(
					ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
		} catch (MalformedObjectNameException ex) {
			throw new IllegalArgumentException(ex);
		}
	}
	
	public static RuntimeMXBean createRuntimeMXBean(MBeanServerConnection server) throws IOException {
		long startTime = 0;
		RuntimeMXBean rtbean;
		
        if (logger.isDebugEnabled()){
    		logger.debug("Creating MX bean : " + ManagementFactory.RUNTIME_MXBEAN_NAME);
        	startTime = System.currentTimeMillis();
        }

        rtbean = ManagementFactory.newPlatformMXBeanProxy(
				server, 
				ManagementFactory.RUNTIME_MXBEAN_NAME,
				RuntimeMXBean.class);
		
        if (logger.isDebugEnabled()){
        	long endTime = System.currentTimeMillis();
        	logger.debug("MX bean creation time : " + (endTime - startTime) + " ms");
        }
        
        return rtbean;
	}
	
	public static Map<String, String> convertTabularDataToStringMap(TabularDataSupport tabData){
		Map<String, String> map = new HashMap<>();
		for (Object entry : tabData.values()){
			CompositeData cData = (CompositeData)entry;
			String key = (String)cData.get(STRING_MAP_KEY);
			String value = (String)cData.get(STRING_MAP_VALUE);
			if (key != null)
				map.put(key, value);
		}
		
		return map;
	}

	public static Map<String, String> convertHiddenTabularDataToStringMap(Map<String, String> hiddenTabData) {
		Map<String, String> map = new HashMap<>();
		for (Object entry : hiddenTabData.values()){
			CompositeData cData = (CompositeData)entry;
			String key = (String)cData.get(STRING_MAP_KEY);
			String value = (String)cData.get(STRING_MAP_VALUE);
			if (key != null)
				map.put(key, value);
		}
		
		return map;
	}
	
}
