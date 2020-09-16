package org.jeyzer.recorder.accessor.jmx.advanced.thread;

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





import java.lang.management.ThreadMXBean;

import javax.management.MBeanServerConnection;

import org.jeyzer.recorder.accessor.jmx.advanced.process.JeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractlThreadUserTimeAccessor;

public class ThreadUserTimeAccessor extends JzrAbstractlThreadUserTimeAccessor
		implements JzrThreadBeanFieldAccessor {
	
	public ThreadUserTimeAccessor() {
		super();
	}

	@Override
	public boolean checkSupport(MBeanServerConnection server, ThreadMXBean tmbean, JeyzerAccessor jeyzerAccessor, long[] threadIds) {
		return checkSupport(tmbean, jeyzerAccessor, threadIds);
	}

	@Override
	public void collect(MBeanServerConnection server, ThreadMXBean tmbean, JeyzerAccessor jeyzerAccessor, long[] threadIds) {
		long startTime = 0;
		Long value;
		this.captureDuration = 0;
		
		if (!isSupported()){
			return;
		}

		for (long id : threadIds){
			if (logger.isDebugEnabled())
				logger.debug("Accessing user time info from MX bean");

			startTime = System.currentTimeMillis();
			
			// access it remotely
			value = tmbean.getThreadUserTime(id);

			long endTime = System.currentTimeMillis();
			this.captureDuration = this.captureDuration + endTime - startTime; // cumulative as per each thread
			
			if (logger.isDebugEnabled()) 
				logger.debug("MX bean user time info access time : {} ms", endTime - startTime);
				
			// store it
			this.userTimePerThread.put(id, value);
		}
	}

	@Override
	public long getCaptureDuration(){
		return this.captureDuration;
	}	
	
}
