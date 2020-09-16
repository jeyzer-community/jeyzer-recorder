package org.jeyzer.recorder.accessor.local.advanced.system;

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





import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import org.jeyzer.recorder.accessor.local.advanced.JzrLocalBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.system.JzrAbstractFreePhysicalMemoryAccessor;

public class LocalFreePhysicalMemoryAccessor extends JzrAbstractFreePhysicalMemoryAccessor implements JzrLocalBeanFieldAccessor{

	public LocalFreePhysicalMemoryAccessor() {
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
				long systemFreeMem = sunOSBean.getFreePhysicalMemorySize();
				
				if (systemFreeMem == -1) {
					this.supported = false;
					logger.warn("Monitored JVM doesn't provide system free memory info.");					
				}
			}
			else{
				this.supported = false;
				logger.warn("Monitored JVM doesn't provide system free memory info.");
			}
			
		}catch(Exception ex){
			logger.warn("Monitored JVM doesn't provide system free memory info. Failed to access system free memory info", ex);
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
				logger.debug("Accessing System free memory info from MX bean");
			
			com.sun.management.OperatingSystemMXBean sunOSBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
			
			// access it
			this.memoryValue = sunOSBean.getFreePhysicalMemorySize();
			
		} catch (Exception e) {
			logger.error("Failed to access System free memory {}", e);
			this.memoryValue = (long)-1;
		}	
	}
}
