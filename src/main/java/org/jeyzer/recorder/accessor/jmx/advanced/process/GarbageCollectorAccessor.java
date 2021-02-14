package org.jeyzer.recorder.accessor.jmx.advanced.process;

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





import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractGarbageCollectorAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrGarbageCollectorConfig;
import org.jeyzer.recorder.config.mx.advanced.JzrLastGCInfoConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

import com.sun.management.GcInfo;

import java.lang.management.GarbageCollectorMXBean;

public class GarbageCollectorAccessor extends JzrAbstractGarbageCollectorAccessor{

	protected static final Logger logger = LoggerFactory.getLogger(GarbageCollectorAccessor.class);
	
	protected long captureDuration;
	
	public GarbageCollectorAccessor(List<JzrGarbageCollectorConfig> collectorConfigs) {
		super(collectorConfigs);
	}
	
	public long getCaptureDuration() {
		return this.captureDuration;
	}
	
	
	public boolean checkCollectors(MBeanServerConnection server){
		Map<String, GarbageCollectorMXBean> garbageCollectorBeans;
		
		try{
			
			if (logger.isDebugEnabled())
				dumpGarbageCollectors(server);			
			
			garbageCollectorBeans= accessRemotGarbageCollectorBeans(server);
			
			return super.checkCollectors(garbageCollectorBeans);

		}catch(Exception ex){
			logger.warn("Garbage Collector beans access error. Garbage Collector access disabled", ex);
			enabled = false;
			return false;
		}
	}

	public void collect(MBeanServerConnection server) {
		Map<String, GarbageCollectorMXBean> garbageCollectorBeans = new HashMap<>(2);
		this.captureDuration = 0;
		
		if (!isGarbageCollectorEnabled()){
			return;
		}
		
		try {
			garbageCollectorBeans = accessRemotGarbageCollectorBeans(server);
		} catch (Exception e) {
			logger.error("Garbage collector info collect failed : garbage collector accesss error.", e);
		}
		
		for (JzrGarbageCollectorConfig config : collectorConfigs){
			String configName = config.getName();
			GarbageCollectorMXBean gcBean = garbageCollectorBeans.get(configName);
			if (config.isEnabled() && gcBean != null){
		    	collectAttributes(gcBean, configName, config);
		    	collectLastGCInfo(server, gcBean, configName, config.getLastGCInfoConfig());
			}
		}		
	}
	
	@Override
	protected long preAccessGarbageCollectorAttribute(String garbageCollectorName, String attribute){
		// get starting time
        if (logger.isDebugEnabled())
        	logger.debug("Accessing GC attribute from " + garbageCollectorName + " garbage collector : " + attribute);
    	return System.currentTimeMillis();
	}
	
	@Override
	protected void postAccessGarbageCollectorAttribute(long startTime){
		// update the capture time
    	long endTime = System.currentTimeMillis();
    	this.captureDuration += endTime - startTime; 
        if (logger.isDebugEnabled())
        	logger.debug("Garbage collector access time : " + (endTime - startTime) + " ms");
	}

	private Map<String, GarbageCollectorMXBean> accessRemotGarbageCollectorBeans(MBeanServerConnection server) throws MalformedObjectNameException, IOException{
		Map<String, GarbageCollectorMXBean> garbageCollectorBeans = new HashMap<>(2);
		ObjectName garbageCollectorName = null;
		long startTime = 0;
		
		// all GC bean names
		garbageCollectorName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
		
        if (logger.isDebugEnabled())
        	logger.debug("Creating GC beans : " + ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE);

    	startTime = System.currentTimeMillis();
        
    	// query the GC beans. Should be 2 beans
		Set<ObjectName> mbeans = server.queryNames(garbageCollectorName, null);
		if (mbeans != null) {
			Iterator<ObjectName> iterator = mbeans.iterator();
			while (iterator.hasNext()) {
				ObjectName objName = iterator.next();
				GarbageCollectorMXBean gcBean = ManagementFactory.newPlatformMXBeanProxy(server,
						objName.getCanonicalName(), GarbageCollectorMXBean.class);
				garbageCollectorBeans.put(gcBean.getName(), gcBean);
			}
		}
		
    	long endTime = System.currentTimeMillis();		
		this.captureDuration += endTime - startTime;
		
        if (logger.isDebugEnabled())
        	logger.debug("MX beans creation time : " + (endTime - startTime) + " ms");
		
		return garbageCollectorBeans;
	}	
	
	
	private void collectLastGCInfo(MBeanServerConnection server, GarbageCollectorMXBean garbageCollectorBean, String name, JzrLastGCInfoConfig lastGCInfoConfig) {
		
		// fetch the GC info attribute
		Object value = getGCInfoAttribute(server, name);
		if (value == null) {
			setLastGCInfoFailedValues(name, lastGCInfoConfig, NOT_AVAILABLE_VALUE);
			return;
		}
		
		if (GC_NOT_YET_OCCURED.equals(value)){
			logger.debug("Garbage collector last GC info not yet available.");
			setLastGCInfoFailedValues(name, lastGCInfoConfig, GC_NOT_YET_OCCURED_VALUE);
			return;
		}
		
		if (!(value instanceof CompositeData)) {
			logger.error("Garbage collector last GC info collect failed : unable to cast object to : " + CompositeData.class 
					+ ". Object is instance of class " + value.getClass());
			setLastGCInfoFailedValues(name, lastGCInfoConfig, NOT_AVAILABLE_VALUE);
			return;
		}
			
		GcInfo gcInfo = GcInfo.from((CompositeData)value);
		setLastGCInfoValues(name, gcInfo, lastGCInfoConfig);
	}
	

	private Object getGCInfoAttribute(MBeanServerConnection server, String name){
		ObjectName objectName;
		Object value;
		long startTime = 0;
		
		try {
			objectName = new ObjectName(
					ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",name=" + name);
		} catch (MalformedObjectNameException ex) {
			logger.error("Garbage collector last GC info collect failed : object name error.", ex);
			return null;
		}

		startTime = System.currentTimeMillis();
		
		try{
			value = server.getAttribute(objectName, LAST_GC_INFO_ATTRIBUTE);
			if (value == null)
				value = GC_NOT_YET_OCCURED;
		} catch (Exception ex) {
			logger.error("Garbage collector last GC info collect failed : attribute access error.", ex);
			return null;
		}

    	long endTime = System.currentTimeMillis();		
		this.captureDuration += endTime - startTime;
		
        if (logger.isDebugEnabled())
        	logger.debug("MX bean GC info for Garbage Collector " + name + " access time : " + (endTime - startTime) + " ms");
		
		return value;
	}

	private void dumpGarbageCollectors(MBeanServerConnection server) {
		Map<String, GarbageCollectorMXBean> garbageCollectorBeans;
    	
    	try {
			garbageCollectorBeans = accessRemotGarbageCollectorBeans(server);
		} catch (Exception e) {
			logger.error("Failed to access the remote garbage collector bean to dump its values", e);
			return;
		}
    	
    	logger.info("=================================================================");
    	
    	for (GarbageCollectorMXBean gcBean : garbageCollectorBeans.values()){
    		String name = gcBean.getName();
    		logger.info("GC bean name : " + name);
    		logger.info("Valid : " + Boolean.valueOf(gcBean.isValid()));
    		logger.info("Collection count : " + gcBean.getCollectionCount());
    		logger.info("Collection time : " + gcBean.getCollectionTime());
    		
    		logger.info("Garbage collector " + name + " handling pools : ");
    		for (String poolName : gcBean.getMemoryPoolNames())
    			logger.info(" - " + poolName);
    		Object value = getGCInfoAttribute(server, name);
    		if (GC_NOT_YET_OCCURED.equals(value)){
    			logger.info("No GC info for the GC : " + name + ". GC phase did not yet occured.");
        		logger.info("Important : GC info is null until first GC is executed.");
        		logger.info("            GC info is usually attached to the young space GC.");
    			logger.info("=================================================================");
    			continue;
    		}
    		if (!(value instanceof CompositeData)){
    			logger.info("No GC info available for the GC : " + name);
    			logger.info("=================================================================");
    			continue;
    		}
    		GcInfo gcInfo = GcInfo.from((CompositeData)value);
    		logger.info(" " + name + "-Composite type : " + gcInfo.getCompositeType().toString());
    		logger.info(" " + name + "-Id : " + gcInfo.getId());    			
    		logger.info(" " + name + "-Duration : " + gcInfo.getDuration());
    		logger.info(" " + name + "-Start time : " + gcInfo.getStartTime());
    		logger.info(" " + name + "-End time : " + gcInfo.getEndTime());
    		logger.info(" Memory usage before GC :");
    		Map<String, MemoryUsage> memUsages = gcInfo.getMemoryUsageAfterGc();
    		for (String key : memUsages.keySet()){
    			MemoryUsage memUsage = memUsages.get(key);
    			logger.info("  Pool : " + key);
    			logger.info("   - init : " + memUsage.getInit());
    			logger.info("   - used : " + memUsage.getUsed());
    			logger.info("   - committed : " + memUsage.getCommitted());
    			logger.info("   - max : " + memUsage.getMax());
    		}
    		
    		logger.info("=================================================================");
    	}
	}	
	
}
