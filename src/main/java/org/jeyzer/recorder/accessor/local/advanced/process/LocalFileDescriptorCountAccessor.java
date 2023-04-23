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
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractFileDescriptorCountAccessor;
import org.jeyzer.recorder.util.SystemHelper;

import com.sun.management.UnixOperatingSystemMXBean;

public class LocalFileDescriptorCountAccessor extends JzrAbstractFileDescriptorCountAccessor implements JzrLocalBeanFieldAccessor {
	
	public LocalFileDescriptorCountAccessor() {
		super();
	}

	@Override
	public boolean checkSupport() {
		this.supported =  true;
		
		try{
			OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

			if (SystemHelper.isWindows() || SystemHelper.isMac()) {
				// Only supported on Unix
				this.supported =  false;
				return this.supported;
			}
			
			if (osBean instanceof com.sun.management.UnixOperatingSystemMXBean){
				UnixOperatingSystemMXBean unixosmxBean = (UnixOperatingSystemMXBean) osBean;
				long openFDCount = unixosmxBean.getOpenFileDescriptorCount();
				
				if (openFDCount == -1) {
					this.supported = false;
					logger.warn("Monitored JVM doesn't provide process open file descriptor count info.");					
				}
			}
			else{
				this.supported = false;
				logger.warn("Monitored JVM doesn't provide process open file descriptor count info.");
			}
			
		}catch(Exception ex){
			logger.warn("Monitored JVM doesn't provide process open file descriptor count info. Failed to access process open file descriptor count info.", ex);
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
				logger.debug("Accessing Process open file descriptor count info from MX bean");
			
			com.sun.management.UnixOperatingSystemMXBean sunOSBean = (com.sun.management.UnixOperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
			
			// access it
			this.fdCountValue = Long.valueOf(sunOSBean.getOpenFileDescriptorCount());
			
		} catch (Exception e) {
			logger.error("Failed to access process open file descriptor count {}", e);
			this.fdCountValue = (long)-1;
		}
	}
}
