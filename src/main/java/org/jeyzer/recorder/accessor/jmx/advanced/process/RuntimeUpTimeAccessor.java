package org.jeyzer.recorder.accessor.jmx.advanced.process;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Recorder
 * --
 * Copyright (C) 2020, 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */





import java.io.IOException;
import java.lang.management.RuntimeMXBean;

import javax.management.MBeanServerConnection;

import org.jeyzer.recorder.accessor.jmx.advanced.JzrMXBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractRuntimeUpTimeAccessor;
import org.jeyzer.recorder.util.JMXUtil;

public class RuntimeUpTimeAccessor extends JzrAbstractRuntimeUpTimeAccessor implements JzrMXBeanFieldAccessor{
	
	@Override
	public boolean checkSupport(MBeanServerConnection server) {
		this.supported =  true;
		
		try{
			RuntimeMXBean rtmxBean= JMXUtil.createRuntimeMXBean(server);
			return super.checkSupport(rtmxBean);
		}catch(Exception ex){
			logger.warn("Monitored JVM doesn't provide runtime info. Failed to access process up time", ex);
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
			startTime = System.currentTimeMillis();
		
			if (logger.isDebugEnabled())
				logger.debug("Parsing Runtime MX bean up time info");
        
			RuntimeMXBean rtmxBean= JMXUtil.createRuntimeMXBean(server);
			upTimeValue = rtmxBean.getUptime();
			
			long endTime = System.currentTimeMillis();
			this.captureDuration = endTime - startTime;

			if (logger.isDebugEnabled())
				logger.debug("Runtime MX bean parsing time : " + captureDuration + " ms");
		
		} catch (IOException e) {
			logger.error("Failed to access runtime up time info", e);
			this.upTimeValue = (long)-1;			
		}		
		
	}

	@Override
	public long getCaptureDuration() {
		return this.captureDuration;
	}

}
