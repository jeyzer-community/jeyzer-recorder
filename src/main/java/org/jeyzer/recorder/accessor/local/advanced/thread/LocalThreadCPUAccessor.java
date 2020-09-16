package org.jeyzer.recorder.accessor.local.advanced.thread;

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

import org.jeyzer.recorder.accessor.local.advanced.process.LocalJeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractThreadCPUAccessor;

public class LocalThreadCPUAccessor extends JzrAbstractThreadCPUAccessor implements JzrLocalThreadBeanFieldAccessor{
	
	public LocalThreadCPUAccessor() {
		super();
	}

	@Override
	public void collect(ThreadMXBean tmbean, LocalJeyzerAccessor jeyzerAccessor, long[] threadIds) {
		Long value;
		
		if (!isSupported()){
			return;
		}

		for (long id : threadIds){
			if (logger.isDebugEnabled())
				logger.debug("Accessing CPU thread info from MX bean");
				
			// access it 
			value = tmbean.getThreadCpuTime(id);
			
			// store it
			this.cpuPerThread.put(id, value);
		}
	}
	
}
