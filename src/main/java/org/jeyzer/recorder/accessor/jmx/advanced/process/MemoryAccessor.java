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
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.MBeanServerConnection;

import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractMemoryAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrMemoryConfig;

public class MemoryAccessor extends JzrAbstractMemoryAccessor{

	protected long captureDuration;
	
	public MemoryAccessor(JzrMemoryConfig config) {
		super(config);
	}

	public boolean checkSupport(MBeanServerConnection server) {
		
		try{
			
			if (logger.isDebugEnabled())
				dumpMemoryMXInfo(server);
			
			createMemoryMXBean(server);
			
		}catch(Exception ex){
			logger.warn("Memory MX accesss error. Memory MX access disabled", ex);
			enabled = false;
			return false;
		}
		enabled = true;
		
		return true;
	}

	private void dumpMemoryMXInfo(MBeanServerConnection server) throws IOException {
		MemoryMXBean memBean = createMemoryMXBean(server);
		
		logger.info("=================================================================");
		logger.info("Memory MX bean info");
		logger.info("Heap memory");
		logger.info(" Usage - used      : "+ memBean.getHeapMemoryUsage().getUsed());
		logger.info(" Usage - committed : "+ memBean.getHeapMemoryUsage().getCommitted());
		logger.info(" Usage - max       : "+ memBean.getHeapMemoryUsage().getMax());
		logger.info(" Usage - init      : "+ memBean.getHeapMemoryUsage().getInit());
		logger.info("Non Heap memory");
		logger.info(" Usage - used      : "+ memBean.getNonHeapMemoryUsage().getUsed());
		logger.info(" Usage - committed : "+ memBean.getNonHeapMemoryUsage().getCommitted());
		logger.info(" Usage - max       : "+ memBean.getNonHeapMemoryUsage().getMax());
		logger.info(" Usage - init      : "+ memBean.getNonHeapMemoryUsage().getInit());
		logger.info("Objects wating for finalization : "+ memBean.getObjectPendingFinalizationCount());
		logger.info("=================================================================");
	}

	public void collect(MBeanServerConnection server) {
		MemoryMXBean memBean = null;
		long startTime = 0;
		this.captureDuration = 0;

		if (!isMemoryEnabled()){
			return;
		}		
		
		try {
			startTime = System.currentTimeMillis();
		
			if (logger.isDebugEnabled())
				logger.debug("Parsing Memory MX bean info");
        
			memBean = createMemoryMXBean(server);
			
			long endTime = System.currentTimeMillis();
			this.captureDuration = endTime - startTime;

			if (logger.isDebugEnabled())
				logger.debug("Memory MX bean parsing time : " + (endTime - startTime) + " ms");
		
		} catch (IOException e) {
			logger.error("Failed to access memory MX info {}", e);

		}		

		if (memBean != null)
			collectFigures(memBean);
	}	

	private MemoryMXBean createMemoryMXBean(MBeanServerConnection server) throws IOException {
		long startTime = 0;
		MemoryMXBean memBean;
		
        if (logger.isDebugEnabled()){
    		logger.debug("Creating MX bean : " + ManagementFactory.MEMORY_MXBEAN_NAME);
        	startTime = System.currentTimeMillis();
        }

        memBean = ManagementFactory.newPlatformMXBeanProxy(
				server, 
				ManagementFactory.MEMORY_MXBEAN_NAME,
				MemoryMXBean.class);
		
        if (logger.isDebugEnabled()){
        	long endTime = System.currentTimeMillis();
        	logger.debug("MX bean creation time : " + (endTime - startTime) + " ms");
        }
        
        return memBean;
	}			

	public long getCaptureDuration() {
		return this.captureDuration;
	}
	
}
