package org.jeyzer.recorder.accessor.mx.advanced.process;

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





import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.recorder.config.mx.advanced.JzrGarbageCollectorConfig;
import org.jeyzer.recorder.config.mx.advanced.JzrLastGCInfoConfig;
import org.jeyzer.recorder.config.mx.advanced.JzrMemoryPoolConfig;
import org.jeyzer.recorder.config.mx.advanced.JzrMemoryUsageConfig;
import org.jeyzer.recorder.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.management.GcInfo;

public abstract class JzrAbstractGarbageCollectorAccessor {
	
	protected static final Logger logger = LoggerFactory.getLogger(JzrAbstractGarbageCollectorAccessor.class);	

	protected static final String LAST_GC_INFO_ATTRIBUTE = "LastGcInfo";
	
	private static final String GC_FIELD_PREFIX = FileUtil.JZR_FIELD_JZ_PREFIX + "gc:";
	private static final String GC_FIELD_LAST_GC_INFO = "last gc";
	private static final String GC_FIELD_AFTER_GC = "after";
	private static final String GC_FIELD_BEFORE_GC = "before";
	
	protected static Object GC_NOT_YET_OCCURED = new Object();
	protected static Long GC_NOT_YET_OCCURED_VALUE = Long.valueOf(0);
	protected static Long NOT_AVAILABLE_VALUE = Long.valueOf(-1);	
	
	protected List<JzrGarbageCollectorConfig> collectorConfigs;

	protected Map<String, Long> figures = new HashMap<>(10);	
	
	protected boolean enabled = false;
	
	public JzrAbstractGarbageCollectorAccessor(List<JzrGarbageCollectorConfig> collectorConfigs) {
		this.collectorConfigs = collectorConfigs;
	}
	
	public boolean isGarbageCollectorEnabled(){
		return enabled;
	}
	
	public boolean checkCollectors(Map<String, GarbageCollectorMXBean> garbageCollectorBeans){
		List<JzrGarbageCollectorConfig> failedGarbageCollectors;
		
		if (collectorConfigs == null || collectorConfigs.isEmpty()){
			enabled = false;
			return false;
		}
		
		failedGarbageCollectors = new ArrayList<JzrGarbageCollectorConfig>(2); 
		
		try{
			if (garbageCollectorBeans.isEmpty()){
				logger.warn("Remote Garbage Collector bean list is empty.");
				enabled = false;
				return false;				
			}

			for (JzrGarbageCollectorConfig config : collectorConfigs){
				if (!garbageCollectorBeans.containsKey(config.getName())){
					logger.warn("Garbage Collector {} not supported. Will be removed.", config.getName());
					failedGarbageCollectors.add(config);
					config.disable();
				}
			}
		}catch(Exception ex){
			logger.warn("Garbage Collector beans access error. Garbage Collector access disabled", ex);
			enabled = false;
			return false;
		}

		collectorConfigs.removeAll(failedGarbageCollectors);
		
		// check again
		if (collectorConfigs.isEmpty()){
			logger.warn("All Garbage Collectors aren't supported. Garbage Collector access disabled");
			enabled = false;
			return false;
		}
		
		enabled = true;
		return true;
	}
	
	protected void collectAttributes(GarbageCollectorMXBean garbageCollectorBean, String garbageCollectorName, JzrGarbageCollectorConfig config) {
    	for (String attribute : config.getAttributes()){
   			long value = accessGarbageCollectorAttribute(garbageCollectorBean, garbageCollectorName, attribute);
   			figures.put(garbageCollectorName + attribute, value);
    	}
	}
	
	protected abstract long preAccessGarbageCollectorAttribute(String garbageCollectorName, String attribute);
	
	protected abstract void postAccessGarbageCollectorAttribute(long time);

	private long accessGarbageCollectorAttribute(GarbageCollectorMXBean garbageCollectorBean, String garbageCollectorName, String attribute) {
		long value = -1;
		
		long startTime = preAccessGarbageCollectorAttribute(garbageCollectorName, attribute);
		
		try {
			// usage
			if (JzrGarbageCollectorConfig.JZR_COUNT.equals(attribute)){
				value = garbageCollectorBean.getCollectionCount();
			}
			else if (JzrGarbageCollectorConfig.JZR_TIME.equals(attribute)){
				value = garbageCollectorBean.getCollectionTime();
			}
		} catch (Exception e) {
			logger.error("Failed to access gc attribute from {} garbage collector, {} attribute", garbageCollectorName, attribute);
			return -1;
		}
		
		postAccessGarbageCollectorAttribute(startTime);
		
		return value;
	}
	
	protected void setLastGCInfoValues(String name, GcInfo gcInfo, JzrLastGCInfoConfig lastGCInfoConfig) {
		
		// fetch the last GC info attributes 
		for (String attribute : lastGCInfoConfig.getAttributes()){
			if (JzrLastGCInfoConfig.JZR_ID.equals(attribute)){
				long value = gcInfo.getId();
				this.figures.put(name + LAST_GC_INFO_ATTRIBUTE + JzrLastGCInfoConfig.JZR_ID, value);
			}
			else if (JzrLastGCInfoConfig.JZR_START_TIME.equals(attribute)){
				long value = gcInfo.getStartTime();
				this.figures.put(name + LAST_GC_INFO_ATTRIBUTE + JzrLastGCInfoConfig.JZR_START_TIME, value);
			}
			else if (JzrLastGCInfoConfig.JZR_END_TIME.equals(attribute)){
				long value = gcInfo.getEndTime();
				this.figures.put(name + LAST_GC_INFO_ATTRIBUTE + JzrLastGCInfoConfig.JZR_END_TIME, value);
			}
			else if (JzrLastGCInfoConfig.JZR_DURATION.equals(attribute)){
				long value = gcInfo.getDuration();
				this.figures.put(name + LAST_GC_INFO_ATTRIBUTE + JzrLastGCInfoConfig.JZR_DURATION, value);
			}
		}
		
		// fetch the last GC info before and after used memory pool figures
		setLastGCInfoPoolMemoryValues(name, gcInfo.getMemoryUsageBeforeGc(), lastGCInfoConfig.getBeforePoolConfig(), JzrLastGCInfoConfig.JZR_BEFORE);
		setLastGCInfoPoolMemoryValues(name, gcInfo.getMemoryUsageAfterGc(), lastGCInfoConfig.getAfterPoolConfig(), JzrLastGCInfoConfig.JZR_AFTER);		
	}

	private void setLastGCInfoPoolMemoryValues(String name, Map<String, MemoryUsage> memUsages, List<JzrMemoryPoolConfig> poolConfigs, String stage) {
		for (JzrMemoryPoolConfig poolCfg : poolConfigs){
			// Important : only usage pool is provided by the last gc info. 
			// Therefore poolCfg.getMemoryUsages() should return only 1 memUsageCfg object
			for (JzrMemoryUsageConfig memUsageCfg : poolCfg.getMemoryUsages()){
				MemoryUsage usage = memUsages.get(poolCfg.getName());
				for (String figure : memUsageCfg.getFigures()){
					long value = -1;
					if (JzrMemoryUsageConfig.JZR_USED.equals(figure)){
						value = usage.getUsed();
					}
					else if (JzrMemoryUsageConfig.JZR_MAX.equals(figure)){
						value = usage.getMax();
					}
					else if (JzrMemoryUsageConfig.JZR_COMMITTED.equals(figure)){
						value = usage.getCommitted();
					}					
					else if (JzrMemoryUsageConfig.JZR_INIT.equals(figure)){
						value = usage.getInit();
					}
					this.figures.put(
							name  + LAST_GC_INFO_ATTRIBUTE + stage + poolCfg.getName() + memUsageCfg.getName() + figure, 
							Long.valueOf(value)
							);
				}
			}
		}
	}

	protected void setLastGCInfoFailedValues(String name, JzrLastGCInfoConfig lastGCInfoConfig, Long value) {
		for (String attribute : lastGCInfoConfig.getAttributes()){
			this.figures.put(name + LAST_GC_INFO_ATTRIBUTE + attribute, value);
		}
		setLastGCInfoPoolMemoryFailedValues(name, lastGCInfoConfig.getBeforePoolConfig(), JzrLastGCInfoConfig.JZR_BEFORE, value);
		setLastGCInfoPoolMemoryFailedValues(name, lastGCInfoConfig.getAfterPoolConfig(), JzrLastGCInfoConfig.JZR_AFTER, value);
	}

	private void setLastGCInfoPoolMemoryFailedValues(String name, List<JzrMemoryPoolConfig> list, String stage, Long value) {
		for (JzrMemoryPoolConfig poolCfg : list){
			for (JzrMemoryUsageConfig memUsage : poolCfg.getMemoryUsages()){
				for (String figure : memUsage.getFigures()){
					this.figures.put(name  + LAST_GC_INFO_ATTRIBUTE + stage + poolCfg.getName() + memUsage.getName() + figure, value);
				}
			}
		}
	}
	
	
	public void printGarbageCollectorInfo(BufferedWriter out) throws IOException {
		if (!isGarbageCollectorEnabled()){
			return;
		}
		
		// Full Advanced Java thread dump with locks info from : /jndi/rmi://localhost:2500/jmxrmi
		//	process cpu=0.008129644681811833	system cpu=0.22768383344079524
		//	system free memory=2786799616	system total memory=7474524160
		
		//	gc:<name1>:<attribute1>=value
		//	gc:<name1>:<attribute2>=value
		//	gc:<name1>:last gc:<attribute1>=value
		//	gc:<name1>:last gc:<attribute2>=value
		//	gc:<name1>:last gc:before:<pool1>:usage:<figure1>=value
		//	gc:<name1>:last gc:after:<pool1>:usage:<figure1>=value
		
		//	gc:<name2>:<attribute1>=value
		//	gc:<name2>:<attribute2>=value
		
		for (JzrGarbageCollectorConfig config : this.collectorConfigs){
			String garbageCollectorName = config.getName();
			
			for (String attribute : config.getAttributes()){
    			StringBuilder line = new StringBuilder();
    			Long value = null;
    			if (config.isEnabled())
    				value = this.figures.get(garbageCollectorName + attribute);
    			if (value == null)
    				value = (long)-1;  // config not enabled or value not found    				

    			line.append(GC_FIELD_PREFIX);
    			line.append(garbageCollectorName);
    			line.append(FileUtil.JZR_FIELD_SEPARATOR);
    			line.append(attribute);
    			line.append(FileUtil.JZR_FIELD_EQUALS);
    			line.append(value);
        		writeln(line.toString(), out);
			}
			
			printGCInfo(config, out);
		}
	}
	
	public void close() {
		this.figures.clear();
	}
	
	private void printGCInfo(JzrGarbageCollectorConfig config, BufferedWriter out) throws IOException {
		String garbageCollectorName = config.getName();
		
		for (String attribute : config.getLastGCInfoConfig().getAttributes()){
			StringBuilder line = new StringBuilder();
			Long value = null;
			if (config.isEnabled())
				value = this.figures.get(garbageCollectorName + LAST_GC_INFO_ATTRIBUTE + attribute);
			if (value == null)
				value = (long)-1;  // config not enabled or value not found    				

			line.append(GC_FIELD_PREFIX);
			line.append(garbageCollectorName);
			line.append(FileUtil.JZR_FIELD_SEPARATOR);
			line.append(GC_FIELD_LAST_GC_INFO);
			line.append(FileUtil.JZR_FIELD_SEPARATOR);
			line.append(attribute);
			line.append(FileUtil.JZR_FIELD_EQUALS);
			line.append(value);
    		writeln(line.toString(), out);
		}
		
		printGCInfoPoolMemory(config, config.getLastGCInfoConfig().getBeforePoolConfig(), JzrLastGCInfoConfig.JZR_BEFORE, GC_FIELD_BEFORE_GC, out);
		printGCInfoPoolMemory(config, config.getLastGCInfoConfig().getAfterPoolConfig(), JzrLastGCInfoConfig.JZR_AFTER, GC_FIELD_AFTER_GC, out);
	}

	private void printGCInfoPoolMemory(JzrGarbageCollectorConfig config, List<JzrMemoryPoolConfig> poolConfigs, String stage, String printStage, BufferedWriter out) throws IOException {
		String garbageCollectorName = config.getName();
		
		for (JzrMemoryPoolConfig memPool : poolConfigs){
			for (JzrMemoryUsageConfig usageConfig : memPool.getMemoryUsages()){
				for (String figure : usageConfig.getFigures()){
					StringBuilder line = new StringBuilder();
					Long value = null;
					if (config.isEnabled())
						value = this.figures.get(garbageCollectorName + LAST_GC_INFO_ATTRIBUTE + stage + memPool.getName() + usageConfig.getName() + figure);
					if (value == null)
						value = (long)-1;  // config not enabled or value not found    				

					line.append(GC_FIELD_PREFIX);
					line.append(garbageCollectorName);
					line.append(FileUtil.JZR_FIELD_SEPARATOR);
					line.append(GC_FIELD_LAST_GC_INFO);
					line.append(FileUtil.JZR_FIELD_SEPARATOR);
					line.append(printStage);
					line.append(FileUtil.JZR_FIELD_SEPARATOR);
					line.append(memPool.getName());
					line.append(FileUtil.JZR_FIELD_SEPARATOR);
					line.append(usageConfig.getName());			
					line.append(FileUtil.JZR_FIELD_SEPARATOR);
					line.append(figure);					
					line.append(FileUtil.JZR_FIELD_EQUALS);
					line.append(value);
		    		writeln(line.toString(), out);					
				}
			}
		}
	}
	

	protected void writeln(String info, BufferedWriter out) throws IOException {
		try {
			out.write(info);
			out.newLine();
		} catch (IOException e) {
			logger.error("Failed to write GC data in thread dump file.");
			throw e;
		}
	}	
	
}
