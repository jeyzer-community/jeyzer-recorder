package org.jeyzer.recorder.accessor.jcmd;

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

import org.jeyzer.recorder.accessor.JzrAccessor;
import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.error.JzrProcessNotAvailableException;
import org.jeyzer.recorder.accessor.error.JzrValidationException;
import org.jeyzer.recorder.config.jcmd.JzrJcmdConfig;
import org.jeyzer.recorder.util.JzrTimeZone;

import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalJcmdAccessor implements JzrAccessor{

	private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
	private static final String HOTSPOT_BEAN_CLASS = "com.sun.management.HotSpotDiagnosticMXBean$ThreadDumpFormat";
	private static final String HOTSPOT_BEAN_METHOD = "dumpThreads";

	private static final Logger logger = LoggerFactory.getLogger(LocalJcmdAccessor.class);	

	private final JzrJcmdConfig cfg;

	public LocalJcmdAccessor(final JzrJcmdConfig cfg) {
		this.cfg = cfg;
	}

	@Override
	public long threadDump(File file) throws JzrProcessNotAvailableException, JzrGenerationException {
		long startTime;
		long endTime;
		long duration;

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
			m.invoke( hotspotMBean , file.getAbsolutePath(), format);
		} catch (Exception ex) {
			String msg = "Failed to generate jcmd thread dump file : " + ex.getMessage();
			throw new JzrGenerationException(msg, ex);
		}

		endTime = System.currentTimeMillis();
		duration = endTime - startTime;
		if (logger.isDebugEnabled())
			logger.debug("Jcmd dumpThreads execution time : " + duration + " ms");

		return startTime + (duration) / 2;
	}

	@Override
	public void validate() throws JzrValidationException {
		try {
			Class.forName(HOTSPOT_BEAN_CLASS);
		} catch (ClassNotFoundException e) {
			logger.error("Jcmd initialisation failed : the jcmd thread dump is not available on the current JVM. JVM must be Java 21+. Requested class, not found, is : " + HOTSPOT_BEAN_CLASS);
			throw new JzrValidationException("Jcmd initialisation failed : the jcmd thread dump is not available on the current JVM. JVM must be Java 21+");
		}
	}

	@Override
	public void initiate(File file) throws JzrGenerationException {
		// Nothing to do
	}

	@Override
	public String getTimeZoneId() {
		return System.getProperty(JzrTimeZone.PROPERTY_USER_TIMEZONE);
	}
}
