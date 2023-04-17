package org.jeyzer.recorder.accessor.local.advanced.thread;

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
import java.lang.management.ThreadMXBean;

import org.jeyzer.recorder.accessor.local.advanced.process.LocalJeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractJeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractThreadMemoryAccessor;

public class LocalThreadMemoryAccessor extends JzrAbstractThreadMemoryAccessor implements JzrLocalThreadBeanFieldAccessor{
	
	@Override
	public boolean checkSupport(ThreadMXBean tmbean, JzrAbstractJeyzerAccessor jeyzerAccessor, long[] threadIds) {
		try {
			com.sun.management.ThreadMXBean sunBean = null;
			
			ThreadMXBean tBean = ManagementFactory.getThreadMXBean();

			this.supported = false;
			if (tBean instanceof com.sun.management.ThreadMXBean){
				sunBean = (com.sun.management.ThreadMXBean) tBean;
				long[] tMemorySizes = sunBean.getThreadAllocatedBytes(threadIds);

				for (int i = 0; i < tMemorySizes.length; i++) {
					if (tMemorySizes[i] != -1) {
						this.supported = true;
						break;
					}
				}
			}
			
			if (!this.supported)
				logger.warn("Monitored JVM doesn't provide thread allocated memory info.");

		} catch (Exception e1) {
			logger.warn(
					"Monitored JVM doesn't provide thread allocated memory info.", e1);
			this.supported = false;
		}

		return this.supported; 
	}
	
	@Override
	public void collect(ThreadMXBean tmbean, LocalJeyzerAccessor jeyzerAccessor, long[] threadIds) {
		if (supported){
			allocatedBytesPerThread.clear();

			com.sun.management.ThreadMXBean threadMXBean = (com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
			
	        if (logger.isDebugEnabled())
	        	logger.debug("Accessing thread allocated memory from MX thread bean for all threads");
			
	        long[] tMemorySizes = threadMXBean.getThreadAllocatedBytes(threadIds);
			
			for (int i=0; i<tMemorySizes.length; i++){
				allocatedBytesPerThread.put(Long.valueOf(threadIds[i]), Long.valueOf(tMemorySizes[i]));
			}
		}
	}

}
