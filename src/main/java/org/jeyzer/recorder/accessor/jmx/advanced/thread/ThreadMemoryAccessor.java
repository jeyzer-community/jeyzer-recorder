package org.jeyzer.recorder.accessor.jmx.advanced.thread;

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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jeyzer.recorder.accessor.jmx.advanced.process.JeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractThreadMemoryAccessor;

public class ThreadMemoryAccessor extends JzrAbstractThreadMemoryAccessor implements JzrThreadBeanFieldAccessor{
	
	private static final String THREAD_ALLOCATED_BYTES_METHOD = "getThreadAllocatedBytes";
	
	public ThreadMemoryAccessor() {
		super();
	}
	
	@Override
	public boolean checkSupport(MBeanServerConnection server, ThreadMXBean tmbean, JeyzerAccessor jeyzerAccessor, long[] threadIds) {
		try {

			// boolean result = (Boolean)this.server.invoke(
			// new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
			// "isThreadAllocatedMemoryEnabled",
			// null,
			// null
			// );
			// System.out.print("Result : isThreadAllocatedMemoryEnabled " +
			// result);

			/*
			 * isThreadAllocatedMemorySupported is not visible on jdk 8
			 * Windows ps: same result with empty arrays
			 * javax.management.ReflectionException: No such operation:
			 * isThreadAllocatedMemoryEnabled at
			 * com.sun.jmx.mbeanserver.PerInterface
			 * .noSuchMethod(PerInterface.java:170) ~[na:1.8.0_25] at
			 * com.sun
			 * .jmx.mbeanserver.PerInterface.invoke(PerInterface.java:112)
			 * ~[na:1.8.0_25] at
			 * com.sun.jmx.mbeanserver.MBeanSupport.invoke(
			 * MBeanSupport.java:252) ~[na:1.8.0_25] at
			 * javax.management.StandardMBean.invoke(StandardMBean.java:405)
			 * ~[na:1.8.0_25]
			 */
			// result = (Boolean)this.server.invoke(
			// new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
			// "isThreadAllocatedMemorySupported",
			// null,
			// null
			// );
			// System.out.print("Result : isThreadAllocatedMemorySupported "
			// + result);

			Object obj = server.invoke(new ObjectName(
					ManagementFactory.THREAD_MXBEAN_NAME),
					THREAD_ALLOCATED_BYTES_METHOD, new Object[] { threadIds },
					new String[] { "[J" });

			long[] tMemorySizes = (long[]) obj;

			this.supported = false;
			for (int i = 0; i < tMemorySizes.length; i++) {
				if (tMemorySizes[i] != -1) {
					this.supported = true;
					break;
				}
			}

			if (!this.supported)
				logger.warn("Monitored JVM doesn't provide thread allocated memory info.");

			/*
			 * Bad idea : main thread may end up
			 */
			// Long byteSize = (Long)this.server.invoke(
			// new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
			// THREAD_ALLOCATED_BYTES_METHOD,
			// new Object[] { MAIN_THREAD_ID },
			// new String[] { long.class.getName() }
			// );
			// if (byteSize != -1){
			// memoryThreadSupport = true;
			// }else {
			// // retry once more after short pause
			// try {Thread.sleep(500);} catch (InterruptedException e) {}
			// byteSize = (Long)this.server.invoke(
			// new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
			// THREAD_ALLOCATED_BYTES_METHOD,
			// new Object[] { MAIN_THREAD_ID },
			// new String[] { long.class.getName() }
			// );
			// if (byteSize != -1)
			// memoryThreadSupport = true;
			// else{
			// memoryThreadSupport = false;
			// logger.warn("Monitored JVM doesn't provide thread memory info.");
			// }
			// }
			
			// doesn't work through the proxy for
			// com.sun.management.ThreadMXBean (jdk8 Windows)
			// long[] value = getLongArrayAttribute(this.threadMxBeanName,
			// THREAD_ALLOCATED_BYTES_ATTRIBUTE);
			// if (value == null){
			// // retry once more after short pause
			// try {Thread.sleep(10);} catch (InterruptedException e) {}
			// value = getLongArrayAttribute(this.threadMxBeanName,
			// THREAD_ALLOCATED_BYTES_ATTRIBUTE);
			// if (value == null){
			// memoryThreadSupport = false;
			

		} catch (Throwable ex) {
			logger.warn("Monitored JVM doesn't provide thread allocated memory info.", ex);
			this.supported = false;
		}

		return this.supported; 
	}
	
	@Override
	public void collect(MBeanServerConnection server, ThreadMXBean tmbean, JeyzerAccessor jeyzerAccessor, long[] threadIds) {
		Object obj;
		long startTime = 0;
		this.captureDuration = 0;

		if (supported){
			allocatedBytesPerThread.clear();
			
	        if (logger.isDebugEnabled())
	        	logger.debug("Accessing thread allocated memory from MX thread bean for all threads");
	        
        	startTime = System.currentTimeMillis();
			
			try {
				obj = server.invoke(
						new ObjectName(ManagementFactory.THREAD_MXBEAN_NAME),
						THREAD_ALLOCATED_BYTES_METHOD, 
						new Object[] { threadIds }, 
						new String[] { "[J" });
			} catch (Exception e) {
				logger.warn("Failed to collect thread allocated memory info.");
				return;
			}

        	long endTime = System.currentTimeMillis();
        	this.captureDuration = this.captureDuration + endTime - startTime; // cumulative as per each thread
			
	        if (logger.isDebugEnabled())
	        	logger.debug("MX thread bean all thread allocated memory access time : " + (endTime - startTime) + " ms");
			
			long[] tMemorySizes = (long[]) obj;
			
			for (int i=0; i<tMemorySizes.length; i++){
				allocatedBytesPerThread.put(Long.valueOf(threadIds[i]), Long.valueOf(tMemorySizes[i]));
			}
		}
	}

	@Override
	public long getCaptureDuration() {
		return this.captureDuration;
	}

}
