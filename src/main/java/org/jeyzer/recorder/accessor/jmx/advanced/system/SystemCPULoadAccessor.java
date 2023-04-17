package org.jeyzer.recorder.accessor.jmx.advanced.system;

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

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jeyzer.recorder.accessor.jmx.advanced.JzrMXBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.system.JzrAbstractSystemCPULoadAccessor;
import org.jeyzer.recorder.util.JMXUtil;

public class SystemCPULoadAccessor extends JzrAbstractSystemCPULoadAccessor implements JzrMXBeanFieldAccessor {

	private static final String SYSTEM_CPU_LOAD_ATTRIBUTE = "SystemCpuLoad";	
	
	private ObjectName osBeanName;
	
	public SystemCPULoadAccessor(){
		super();
		osBeanName = JMXUtil.getOperatingSystemMXBeanName();
	}
	
	@Override
	public boolean checkSupport(MBeanServerConnection server) {
		this.supported =  true;
		
		try{
			double systemCpu = JMXUtil.getDoubleAttribute(server, this.osBeanName,
					SYSTEM_CPU_LOAD_ATTRIBUTE);
			if (systemCpu == -1) {
				// retry once more after short pause
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				systemCpu = JMXUtil.getDoubleAttribute(server, this.osBeanName,
						SYSTEM_CPU_LOAD_ATTRIBUTE);
				if (systemCpu == -1) {
					// retry once more after short pause
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					systemCpu = JMXUtil.getDoubleAttribute(server, this.osBeanName,
							SYSTEM_CPU_LOAD_ATTRIBUTE);
					if (systemCpu == -1) {
						this.supported = false;
						logger.warn("Monitored JVM doesn't provide system CPU time info.");
					}
				}
			}
		}catch(Exception ex){
			logger.warn("Monitored JVM doesn't provide system CPU time info. Failed to access system CPU info", ex);
			this.supported = false;
		}
		
		return this.supported;
	}

	@Override
	public void collect(MBeanServerConnection server) {
		long startTime = 0;
		this.captureDuration = 0;
		
		if (!isSupported()){
			return;
		}

		try {
			if (logger.isDebugEnabled()) 
				logger.debug("Accessing System CPU info from MX bean");

			startTime = System.currentTimeMillis();
			
			// access it remotely
			this.cpuValue = JMXUtil.getDoubleAttribute(server, this.osBeanName, SYSTEM_CPU_LOAD_ATTRIBUTE);

			long endTime = System.currentTimeMillis();
			this.captureDuration = endTime - startTime; 
			
			if (logger.isDebugEnabled()) 
				logger.debug("MX bean System CPU info access time : " + captureDuration + " ms");
			
		} catch (IOException e) {
			logger.error("Failed to access System CPU", e);
			this.cpuValue = (double)-1;
		}	
	}

	@Override
	public long getCaptureDuration() {
		return this.captureDuration;
	}
}
