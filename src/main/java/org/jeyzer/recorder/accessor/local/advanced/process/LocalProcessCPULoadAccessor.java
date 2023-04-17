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





import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import org.jeyzer.recorder.accessor.local.advanced.JzrLocalBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractProcessCPULoadAccessor;

public class LocalProcessCPULoadAccessor extends JzrAbstractProcessCPULoadAccessor implements JzrLocalBeanFieldAccessor {
	
	public LocalProcessCPULoadAccessor() {
		super();
	}

	@Override
	public boolean checkSupport() {
		this.supported =  true;
		
		try{
			OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
			
			if (osBean instanceof com.sun.management.OperatingSystemMXBean){
				com.sun.management.OperatingSystemMXBean sunOSBean; 
				sunOSBean = (com.sun.management.OperatingSystemMXBean)osBean;
				double processCpuLoad = sunOSBean.getProcessCpuLoad();
				
				if (processCpuLoad == -1) {
					this.supported = false;
					logger.warn("Monitored JVM doesn't provide process CPU time info.");					
				}
			}
			else{
				this.supported = false;
				logger.warn("Monitored JVM doesn't provide process CPU time info.");
			}
			
		}catch(Exception ex){
			logger.warn("Monitored JVM doesn't provide process CPU time info. Failed to access process CPU time info.", ex);
			this.supported = false;
		}
		
		return this.supported;
	}

	@Override
	public void collect() {
		
		if (!isSupported()){
			return;
		}

		try {
			if (logger.isDebugEnabled())
				logger.debug("Accessing Process CPU info from MX bean");
			
			com.sun.management.OperatingSystemMXBean sunOSBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
			
			// access it
			this.cpuValue = sunOSBean.getProcessCpuLoad();
			
		} catch (Exception e) {
			logger.error("Failed to access process CPU {}", e);
			this.cpuValue = (double)-1;
		}
	}
}
