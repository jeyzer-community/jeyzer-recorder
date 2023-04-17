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
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractMemoryPoolAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrMemoryPoolConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class MemoryPoolAccessor extends JzrAbstractMemoryPoolAccessor{
	
	protected static final Logger logger = LoggerFactory.getLogger(MemoryPoolAccessor.class);
	
	public MemoryPoolAccessor(List<JzrMemoryPoolConfig> list) {
		super(list);
	}
	
	protected long captureDuration;
	
	private List<MemoryPoolMXBean> accessRemotMemoryPoolBeans(MBeanServerConnection server) throws MalformedObjectNameException, IOException{
		List<MemoryPoolMXBean> memPoolBeans = new ArrayList<>();
		ObjectName poolName = null;
		long startTime = 0;
		
		poolName = new ObjectName(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",*");
		
        if (logger.isDebugEnabled())
        	logger.debug("Creating memory pool beans : " + ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE);

    	startTime = System.currentTimeMillis();
        
		Set<ObjectName> mbeans = server.queryNames(poolName, null);
		if (mbeans != null) {
			Iterator<ObjectName> iterator = mbeans.iterator();
			while (iterator.hasNext()) {
				ObjectName objName = iterator.next();
				MemoryPoolMXBean pool = ManagementFactory.newPlatformMXBeanProxy(server,
						objName.getCanonicalName(), MemoryPoolMXBean.class);
				memPoolBeans.add(pool);
			}
		}
		
    	long endTime = System.currentTimeMillis();
		this.captureDuration += endTime - startTime;
		
        if (logger.isDebugEnabled())
        	logger.debug("MX beans creation time : " + (endTime - startTime) + " ms");
		
		return memPoolBeans;
	}
	
	public boolean checkPools(MBeanServerConnection server){
		List<MemoryPoolMXBean> memPoolBeans;
		
		try{
			
			if (logger.isDebugEnabled())
				dumpMemoryPools(server);
			
			memPoolBeans= accessRemotMemoryPoolBeans(server);
			
			return checkPools(memPoolBeans);
			
		}catch(Exception ex){
			logger.warn("Memory Pool accesss error. Memory Pool access disabled", ex);
			enabled = false;
			return false;
		}
	}
	
	public void collect(MBeanServerConnection server) {
		List<MemoryPoolMXBean> memPoolBeans = new ArrayList<>();
		this.captureDuration = 0;
		
		if (!isMemoryPoolEnabled()){
			return;
		}
		
		try {
			memPoolBeans = accessRemotMemoryPoolBeans(server);
		} catch (Exception e) {
			logger.error("Memory pool info collect failed : memory Pool accesss error.", e);
		}
		
		collectFigures(memPoolBeans);
	}

	@Override
	protected long accessMemoryFigure(List<MemoryPoolMXBean> memPoolBeans, String poolName, String usageName, String figure) {
		long value = -1;
		long startTime = 0;
		
		startTime = System.currentTimeMillis();
		
		value = super.accessMemoryFigure(memPoolBeans, poolName, usageName, figure);
		if (value != -1){
	    	long endTime = System.currentTimeMillis();
	    	this.captureDuration += endTime - startTime; 
			
	        if (logger.isDebugEnabled())
	        	logger.debug("Pool memory info access time : " + (endTime - startTime) + " ms");
		}
		
		return value;
	}

	private void dumpMemoryPools(MBeanServerConnection server) throws MalformedObjectNameException, IOException {
		List<MemoryPoolMXBean> memPoolBeans;
    	
    	memPoolBeans= accessRemotMemoryPoolBeans(server);
    	
    	for (MemoryPoolMXBean pool : memPoolBeans){
    		String name = pool.getName();
    		logger.info(name);
    		logger.info(Arrays.toString(pool.getMemoryManagerNames()));
    		logger.info(pool.getType().toString());
    		logger.info("Peak usage");
    		logger.info(" " + name + "-Peak-Committed :" + pool.getPeakUsage().getCommitted());
    		logger.info(" " + name + "-Peak-Init :" + pool.getPeakUsage().getInit());
    		logger.info(" " + name + "-Peak-Max :" + pool.getPeakUsage().getMax());
    		logger.info(" " + name + "-Peak-Used :" + pool.getPeakUsage().getUsed());
    		logger.info("Current usage");
    		logger.info(" " + name + "-Current-Committed :" + pool.getUsage().getCommitted());
    		logger.info(" " + name + "-Current-Init :" + pool.getUsage().getInit());
    		logger.info(" " + name + "-Current-Max :" + pool.getUsage().getMax());
    		logger.info(" " + name + "-Current-Used :" + pool.getUsage().getUsed());
    		if (pool.isCollectionUsageThresholdSupported()){
        		logger.info("Collection usage");
        		logger.info(" " + name + "-Collection-Committed :" + pool.getCollectionUsage().getCommitted());
        		logger.info(" " + name + "-Collection-Init :" + pool.getCollectionUsage().getInit());
        		logger.info(" " + name + "-Collection-Max :" + pool.getCollectionUsage().getMax());
        		logger.info(" " + name + "-Collection-Used :" + pool.getCollectionUsage().getUsed());
        		logger.info("" + name + "-Collection threshold :" + pool.getCollectionUsageThreshold());
        		logger.info("" + name + "-Collection threshold count :" + pool.getCollectionUsageThresholdCount());
        		pool.setCollectionUsageThreshold(5000);
    		}
    		if (pool.isUsageThresholdSupported()){
        		logger.info("" + name + "-Usage threshold :" + pool.getUsageThreshold());
        		logger.info("" + name + "-Usage threshold count :" + pool.getUsageThresholdCount());
        		pool.setUsageThreshold(5000);
    		}
    		logger.info("=================================================================");
    		pool.resetPeakUsage();
    	}	
    }

	public long getCaptureDuration() {
		return this.captureDuration;
	}

}
