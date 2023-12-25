package org.jeyzer.recorder.accessor.local.advanced.process;

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

import static org.jeyzer.recorder.config.jcmd.JzrJcmdConfig.JCMD_TXT;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

import javax.management.MBeanServer;

import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.config.local.advanced.JzrAdvancedMXVTAgentConfig;

import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.jeyzer.recorder.util.FileUtil;

public class LocalVTAccessor {
	
	private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
	private static final String HOTSPOT_BEAN_CLASS = "com.sun.management.HotSpotDiagnosticMXBean$ThreadDumpFormat";
	private static final String HOTSPOT_BEAN_METHOD = "dumpThreads";

	private static final Logger logger = LoggerFactory.getLogger(LocalVTAccessor.class);	

	private final JzrAdvancedMXVTAgentConfig cfg;

	public LocalVTAccessor(final JzrAdvancedMXVTAgentConfig cfg) {
		this.cfg = cfg;
	}

	public long collect(File file) throws JzrGenerationException {
		long startTime;
		long endTime;
		long duration;

		String virtualThreadFilePath = file.getAbsolutePath() + FileUtil.JZR_FILE_JZR_VT_EXTENSION;
		
		// Protection as the ThreadDumpFormat does not override any existing output file
		File previousFile = new File(virtualThreadFilePath);
		if (previousFile.exists() && !previousFile.delete())
			logger.warn("Failed to delete the " + previousFile.getAbsolutePath());
		
        if (logger.isDebugEnabled())
        	logger.debug("Accessing virtual thread dump info from HotSpotDiagnostic MX bean");
		
		startTime = System.currentTimeMillis();
		try {
			// Must use reflection for this Java 21+ code execution to stay compatible with Java 7
			Class<?> hotspotDiagClass = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			Object hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, hotspotDiagClass);

			Class<?> dumpFormatEnum = Class.forName(HOTSPOT_BEAN_CLASS);
			Object[] constants = dumpFormatEnum.getEnumConstants();
			// constants are 0 : TEXT_PLAIN and 1 : JSON 
			Object format = JCMD_TXT.equals(this.cfg.getFormat()) ? constants[0] : constants[1];

			Method m = hotspotDiagClass.getMethod(HOTSPOT_BEAN_METHOD, String.class, dumpFormatEnum);
			m.invoke( hotspotMBean , virtualThreadFilePath, format);
		} catch (Exception ex) {
			String msg = "Failed to generate virtual thread thread dump file : " + ex.getMessage();
			throw new JzrGenerationException(msg, ex);
		}

		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		if (logger.isDebugEnabled())
			logger.debug("Virtual thread dump execution time : " + duration + " ms");

		return startTime + (duration) / 2;
	}

	public boolean checkSupport() {
		try {
			Class.forName(HOTSPOT_BEAN_CLASS);
		} catch (ClassNotFoundException e) {
			logger.warn("Local virtual thread accessor initialisation failed. Mandatory class is not found :" + HOTSPOT_BEAN_CLASS);
			return false;
		}
		return true;
	}

}
