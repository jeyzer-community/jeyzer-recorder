package org.jeyzer.recorder.accessor.local.advanced.process;

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
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.recorder.accessor.local.advanced.JzrLocalBeanFieldAccessor;
import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.util.JzrTimeZone;
import org.jeyzer.recorder.util.SystemHelper;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

import com.sun.management.UnixOperatingSystemMXBean;

public class LocalRuntimePropertiesAccessor implements JzrLocalBeanFieldAccessor {
	
	private static final Logger logger = LoggerFactory.getLogger(LocalRuntimePropertiesAccessor.class);	
	
	private Map<String, String> properties = new HashMap<String, String>();
	private boolean supported = false; 
	
	public LocalRuntimePropertiesAccessor(final JzrRecorderConfig config)  {
		supported = config.isProcessCardEnabled();
	}

	@Override
	public void collect() {
		if (!isSupported()){
			return;
		}		
		
		try {
			RuntimeMXBean rtmxBean= ManagementFactory.getRuntimeMXBean();
			collectProperties(rtmxBean);
		} catch (IOException e) {
			logger.error("Failed to access runtime info {}", e);
		}		
	}

	private void collectProperties(RuntimeMXBean rtmxBean) throws IOException {
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
		OperatingSystemMXBean osmxBean = ManagementFactory.getOperatingSystemMXBean();
		
		this.properties.put(PROPERTY_AVAILABLE_PROCESSORS, Integer.toString(osmxBean.getAvailableProcessors()));

		// access com.sun.management.UnixOperatingSystemMXBean		
		if (SystemHelper.isUnix() || SystemHelper.isSolaris()
				&& osmxBean instanceof com.sun.management.UnixOperatingSystemMXBean){
			// max file descriptors
			UnixOperatingSystemMXBean unixosmxBean = (UnixOperatingSystemMXBean) osmxBean;
			this.properties.put(PROPERTY_MAX_FILE_DESCRIPTOR_COUNT, Long.toString(unixosmxBean.getMaxFileDescriptorCount()));
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

	public String getTimeZoneId(){
		return this.properties.get(JzrTimeZone.PROPERTY_USER_TIMEZONE); // can be null
	}

	@Override
	public boolean isSupported() {
		return supported;
	}

	@Override
	public boolean checkSupport() {
		// do nothing
		return true;
	}
}
