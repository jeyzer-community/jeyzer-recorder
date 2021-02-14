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





import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractMemoryAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrMemoryConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalMemoryAccessor extends JzrAbstractMemoryAccessor{

	protected static final Logger logger = LoggerFactory.getLogger(LocalMemoryAccessor.class);	
	
	public LocalMemoryAccessor(JzrMemoryConfig config) {
		super(config);
	}

	public boolean checkSupport() {
		this.enabled =  true;
		
		try{
			ManagementFactory.getMemoryMXBean();
		}catch(Exception ex){
			logger.warn("Monitored JVM doesn't provide heap/non heap memory info. Failed to access memory info.", ex);
			this.enabled = false;
			return false;
		}
		
		return true;
	}

	public void collect() {
		MemoryMXBean memBean = null;
		
		if (!isMemoryEnabled()){
			return;
		}

		try {
			if (logger.isDebugEnabled())
				logger.debug("Accessing heap/non heap memory info from MX bean");
			
			memBean = ManagementFactory.getMemoryMXBean();
		
		} catch (Exception e) {
			logger.error("Failed to access heap/non heap memory.", e);
		}
		
		if(memBean != null)
			collectFigures(memBean);
	}
	
}
