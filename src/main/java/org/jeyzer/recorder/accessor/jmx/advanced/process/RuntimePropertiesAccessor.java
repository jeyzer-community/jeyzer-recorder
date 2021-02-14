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

import static org.jeyzer.recorder.util.RuntimeProperties.*;





import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jeyzer.recorder.accessor.jmx.advanced.JzrMXBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.JzrAbstractBeanFieldAccessor;
import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.util.JMXUtil;
import org.jeyzer.recorder.util.JzrTimeZone;
import org.jeyzer.recorder.util.SystemHelper;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class RuntimePropertiesAccessor extends JzrAbstractBeanFieldAccessor implements JzrMXBeanFieldAccessor {

	private static final String PROCESS_AVAILABLE_PROCESSORS_ATTRIBUTE = "AvailableProcessors";
	private static final String SYSTEM_MAX_FILE_DESCRIPTOR_COUNT_ATTRIBUTE = "MaxFileDescriptorCount";
	
	private static final Logger logger = LoggerFactory.getLogger(RuntimePropertiesAccessor.class);	
	
	private ObjectName osBeanName;
	
	private Map<String, String> properties = new HashMap<>();
	
	public RuntimePropertiesAccessor(final JzrRecorderConfig config)  {
		supported = config.isProcessCardEnabled();
		osBeanName = JMXUtil.getOperatingSystemMXBeanName();
	}

	@Override
	public boolean checkSupport(MBeanServerConnection server) {
		return supported;
	}

	@Override
	public void collect(MBeanServerConnection server) {
		long startTime = 0;
		this.captureDuration = 0;

		if (!isSupported()){
			return;
		}		
		
		try {
			startTime = System.currentTimeMillis();
		
			if (logger.isDebugEnabled())
				logger.debug("Parsing Runtime MX bean info");
        
			RuntimeMXBean rtmxBean= JMXUtil.createRuntimeMXBean(server);
			collectProperties(server, rtmxBean);
			
			long endTime = System.currentTimeMillis();
			this.captureDuration = endTime - startTime;

			if (logger.isDebugEnabled())
				logger.debug("Runtime MX bean parsing time : " + (endTime - startTime) + " ms");
		
		} catch (IOException e) {
			logger.error("Failed to access runtime info", e);
		}		
	}

	private void collectProperties(MBeanServerConnection server, RuntimeMXBean rtmxBean) throws IOException {
		Map<String, String> systemProps;
		
		// system properties
		systemProps = rtmxBean.getSystemProperties();
		this.properties.putAll(systemProps);
		
		// startup time
		long startTime = rtmxBean.getStartTime();
		this.properties.put(PROPERTY_START_TIME, Long.toString(startTime));
		
		// up time
		long upTime = rtmxBean.getUptime();
		this.properties.put(PROPERTY_UP_TIME, Long.toString(upTime));
		
		// input parameters
		List<String> params = rtmxBean.getInputArguments();
		StringBuilder value = new StringBuilder();
		boolean start = true;
		for (String param : params){
			if (!start)
				value.append(" ");
			value.append(param);
			start = false;
		}
		this.properties.put(PROPERTY_INPUT_PARAMETERS, value.toString());
		
		// management spec version
		String mgmtSpecVersion = rtmxBean.getSpecVersion();
		this.properties.put(PROPERTY_MGMT_SPEC_VERSION, mgmtSpecVersion);
		
		// available processors
		int cpuCount= JMXUtil.getIntegerAttribute(server, this.osBeanName, PROCESS_AVAILABLE_PROCESSORS_ATTRIBUTE);
		this.properties.put(PROPERTY_AVAILABLE_PROCESSORS, Integer.toString(cpuCount));

		// access com.sun.management.UnixOperatingSystemMXBean		
		if (SystemHelper.isUnix() || SystemHelper.isSolaris()){
			// max file descriptors
			long maxFileDescriptorCount = JMXUtil.getLongAttribute(server, this.osBeanName, SYSTEM_MAX_FILE_DESCRIPTOR_COUNT_ATTRIBUTE);
			this.properties.put(PROPERTY_MAX_FILE_DESCRIPTOR_COUNT, Long.toString(maxFileDescriptorCount));
		}
	}

	@Override
	public void printValue(BufferedWriter out) throws IOException  {
		List<String> keys = new ArrayList<>();
		keys.addAll(this.properties.keySet());
		java.util.Collections.sort(keys);
		for (String key : keys){
			String value = key + "=" + this.properties.get(key);
			try {
				out.write(value);
				out.newLine();
			} catch (IOException e) {
				logger.error("Failed to write in process-card file.");
				throw e;
			}
		}
	}

	@Override
	public long getCaptureDuration() {
		return captureDuration;
	}
	
	public String getTimeZoneId(){
		return this.properties.get(JzrTimeZone.PROPERTY_USER_TIMEZONE); // can be null
	}
	
}
