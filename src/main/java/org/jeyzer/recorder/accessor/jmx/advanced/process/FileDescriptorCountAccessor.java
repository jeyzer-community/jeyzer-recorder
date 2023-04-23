package org.jeyzer.recorder.accessor.jmx.advanced.process;

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
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractFileDescriptorCountAccessor;
import org.jeyzer.recorder.util.JMXUtil;
import org.jeyzer.recorder.util.SystemHelper;

public class FileDescriptorCountAccessor extends JzrAbstractFileDescriptorCountAccessor implements JzrMXBeanFieldAccessor {
	
	private static final String OPEN_FILE_DESCRIPTOR_COUNT_ATTRIBUTE = "OpenFileDescriptorCount";
	
	private ObjectName osBeanName;
	
	public FileDescriptorCountAccessor() {
		super();
		osBeanName = JMXUtil.getOperatingSystemMXBeanName();
	}

	@Override
	public boolean checkSupport(MBeanServerConnection server) {
		this.supported =  true;
		
		try{
			if (SystemHelper.isWindows() || SystemHelper.isMac()) {
				// Only supported on Unix
				this.supported =  false;
				return this.supported;
			}
			
			long openFDCount = JMXUtil.getLongAttribute(server, this.osBeanName,
					OPEN_FILE_DESCRIPTOR_COUNT_ATTRIBUTE);
			if (openFDCount == -1) {
				// retry once more after short pause
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				openFDCount = JMXUtil.getLongAttribute(server, this.osBeanName,
						OPEN_FILE_DESCRIPTOR_COUNT_ATTRIBUTE);
				if (openFDCount == -1) {
					// retry once more after short pause
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					openFDCount = JMXUtil.getLongAttribute(server, this.osBeanName,
							OPEN_FILE_DESCRIPTOR_COUNT_ATTRIBUTE);
					if (openFDCount == -1) {
						this.supported = false;
						logger.warn("Monitored JVM doesn't provide process open file descriptor count info.");
					}
				}
			}
		}catch(Exception ex){
			logger.warn("Monitored JVM doesn't provide process open file descriptor count info. Failed to access process open file descriptor count info", ex);
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
				logger.debug("Accessing Process open file descriptor count info from MX bean");
			
			startTime = System.currentTimeMillis();
			
			// access it remotely
			this.fdCountValue = JMXUtil.getLongAttribute(server, this.osBeanName, OPEN_FILE_DESCRIPTOR_COUNT_ATTRIBUTE);
			
			long endTime = System.currentTimeMillis();
			this.captureDuration = endTime - startTime;
			
			if (logger.isDebugEnabled()) 
				logger.debug("MX bean Process CPU info access time : " + captureDuration + " ms");
			
		} catch (IOException e) {
			logger.error("Failed to access process open file descriptor count", e);
			this.fdCountValue = (long)-1;
		}
	}

	@Override
	public long getCaptureDuration() {
		return this.captureDuration;
	}
}
