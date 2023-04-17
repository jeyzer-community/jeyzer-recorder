package org.jeyzer.recorder.accessor.local.advanced.process;

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





import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;

import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractGarbageCollectorAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrGarbageCollectorConfig;
import org.jeyzer.recorder.config.mx.advanced.JzrLastGCInfoConfig;

import com.sun.management.GcInfo;

public class LocalGarbageCollectorAccessor extends JzrAbstractGarbageCollectorAccessor {

	public LocalGarbageCollectorAccessor(List<JzrGarbageCollectorConfig> collectorConfigs) {
		super(collectorConfigs);
	}

	@Override
	protected long preAccessGarbageCollectorAttribute(String garbageCollectorName, String attribute) {
		// do nothing
		return 0;
	}

	@Override
	protected void postAccessGarbageCollectorAttribute(long time) {
		// sdo nothing
	}

	public boolean checkCollectors(){
		Map<String, GarbageCollectorMXBean> garbageCollectorBeans;
		
		try{
			garbageCollectorBeans= accessGarbageCollectorBeans();
			
			return super.checkCollectors(garbageCollectorBeans);

		}catch(Exception ex){
			logger.warn("Garbage Collector beans access error. Garbage Collector access disabled", ex);
			enabled = false;
			return false;
		}
	}	
	
	public void collect() {
		Map<String, GarbageCollectorMXBean> garbageCollectorBeans = new HashMap<>(2);
		List<com.sun.management.GarbageCollectorMXBean> sunmxBeans = new ArrayList<>();
		
		if (!isGarbageCollectorEnabled()){
			return;
		}
		
		try {
			garbageCollectorBeans = accessGarbageCollectorBeans(); 
		} catch (Exception e) {
			logger.error("Garbage collector info collect failed : garbage collector accesss error.", e);
		}

		try {
			for (GarbageCollectorMXBean gcBean : garbageCollectorBeans.values()){
				if (gcBean instanceof com.sun.management.GarbageCollectorMXBean){
					sunmxBeans.add((com.sun.management.GarbageCollectorMXBean)gcBean);
				}
			}
//			sunmxBeans = ManagementFactory.getPlatformMXBeans(com.sun.management.GarbageCollectorMXBean.class);
		} catch (Exception e) {
			logger.debug("Garbage collector SUN bean object access failed : probably not supported by the current JVM.", e);
		}
		
		for (JzrGarbageCollectorConfig config : collectorConfigs){
			String configName = config.getName();
			GarbageCollectorMXBean gcBean = garbageCollectorBeans.get(configName);
			if (config.isEnabled() && gcBean != null){
		    	collectAttributes(gcBean, configName, config);
		    	collectLastGCInfo(sunmxBeans, gcBean, configName, config.getLastGCInfoConfig());
			}
		}		
	}

	private void collectLastGCInfo(List<com.sun.management.GarbageCollectorMXBean> sunmxBeans, GarbageCollectorMXBean gcBean,
			String configName, JzrLastGCInfoConfig lastGCInfoConfig) {
		GcInfo info = null;
		Object value = null;
		String name = gcBean.getName();

		if (sunmxBeans == null || sunmxBeans.isEmpty()){
			logger.debug("Garbage collector last GC info not available on current JVM");
			setLastGCInfoFailedValues(name, lastGCInfoConfig, NOT_AVAILABLE_VALUE);
			return;			
		}
		
		boolean found = false;
		for (com.sun.management.GarbageCollectorMXBean sungc : sunmxBeans) {
			if (name.equals(sungc.getName())){
			       // Get the standard attribute "CollectionCount"
			       info = sungc.getLastGcInfo();
			       found = true;
			       break;
			}
		}
		
		if (found && info == null)
			value = GC_NOT_YET_OCCURED;
		else
			value = info;
		
		if (GC_NOT_YET_OCCURED.equals(value)){
			logger.debug("Garbage collector last GC info not yet available.");
			setLastGCInfoFailedValues(name, lastGCInfoConfig, GC_NOT_YET_OCCURED_VALUE);
			return;
		}
		
		if (value == null) {
			logger.error("Garbage collector last GC info not found on the sungc.");
			setLastGCInfoFailedValues(name, lastGCInfoConfig, NOT_AVAILABLE_VALUE);
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

	private Map<String, GarbageCollectorMXBean> accessGarbageCollectorBeans() {
		List<GarbageCollectorMXBean> garbageCollectorBeans = ManagementFactory.getGarbageCollectorMXBeans();
		Map<String, GarbageCollectorMXBean> garbageCollectorBeanMap = new HashMap<>(garbageCollectorBeans.size()); 
		
		for (GarbageCollectorMXBean gcBean : garbageCollectorBeans){
			garbageCollectorBeanMap.put(gcBean.getName(), gcBean);
		}
		
		return garbageCollectorBeanMap;
	}	

}
