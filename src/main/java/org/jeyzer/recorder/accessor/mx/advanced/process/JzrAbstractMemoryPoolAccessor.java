package org.jeyzer.recorder.accessor.mx.advanced.process;

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





import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.recorder.config.mx.advanced.JzrMemoryPoolConfig;
import org.jeyzer.recorder.config.mx.advanced.JzrMemoryUsageConfig;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public abstract class JzrAbstractMemoryPoolAccessor {

	protected static final Logger logger = LoggerFactory.getLogger(JzrAbstractMemoryPoolAccessor.class);
	
	protected static final String POOL_FIELD_PREFIX = FileUtil.JZR_FIELD_JZ_PREFIX + "mem pool:";

	protected List<JzrMemoryPoolConfig> poolsConfigs;

	protected Map<String, Long> figures = new HashMap<>(20);	
	
	protected boolean enabled = false;

	public JzrAbstractMemoryPoolAccessor(List<JzrMemoryPoolConfig> list) {
		this.poolsConfigs = list;
	}
	
	public boolean isMemoryPoolEnabled(){
		return enabled;
	}
	
	public void printPoolMemoryInfo(BufferedWriter out) throws IOException {
		if (!isMemoryPoolEnabled()){
			return;
		}
		
		// Full Advanced Java thread dump with locks info from : /jndi/rmi://localhost:2500/jmxrmi
		//	process cpu=0.008129644681811833	system cpu=0.22768383344079524
		//	system free memory=2786799616	system total memory=7474524160
		
		//	mem pool:<name>:<usage>:<figure>=value	mem pool:<name>:<usage>:<figure2>=value
		//	mem pool:<name>:<usage2>:<figure>=value	mem pool:<name>:<usage2>:<figure2>=value
		
		//	mem pool:<name2>:<usage>:<figure>=value	mem pool:<name2>:<usage>:<figure2>=value
		//  mem pool:<name2>:<usage2>:<figure>=value	mem pool:<name2>:<usage2>:<figure2>=value

		for (JzrMemoryPoolConfig config : poolsConfigs)
			printPoolValues(config, out);
	}
	
	public void close() {
		this.figures.clear();
	}

	private void printPoolValues(JzrMemoryPoolConfig config, BufferedWriter out) throws IOException {
    	String poolName = config.getName();

    	for (JzrMemoryUsageConfig memUsageConfig : config.getMemoryUsages()){
    		String usageName = memUsageConfig.getName();
    	
    		for (String figure : memUsageConfig.getFigures()){
    			StringBuilder line = new StringBuilder();
    			Long value = null;
    			if (config.isEnabled())
    				value = this.figures.get(poolName + usageName + figure);
    			if (value == null)
    				value = (long)-1;  // config not enabled or value not found    				

    			line.append(POOL_FIELD_PREFIX);
    			line.append(poolName);
    			line.append(FileUtil.JZR_FIELD_SEPARATOR);
    			line.append(usageName);
    			line.append(FileUtil.JZR_FIELD_SEPARATOR);
    			line.append(figure);
    			line.append(FileUtil.JZR_FIELD_EQUALS);
    			line.append(value);
        		writeln(line.toString(), out);
    		}
    	}
    	
	}	

	protected void writeln(String info, BufferedWriter out) throws IOException {
		try {
			out.write(info);
			out.newLine();
		} catch (IOException e) {
			logger.error("Failed to write pool memory info in thread dump file.");
			throw e;
		}
	}
	
	protected long accessMemoryFigure(List<MemoryPoolMXBean> memPoolBeans, String poolName, String usageName, String figure) {
		long value = -1;
		
        if (logger.isDebugEnabled())
        	logger.debug("Accessing memory info from " + poolName + " pool, " + usageName + " usage, " + figure + " figure");
        
		try {
			for (MemoryPoolMXBean memPoolBean : memPoolBeans){
				if (memPoolBean.getName().equals(poolName)){
					
					// usage
					if (JzrMemoryUsageConfig.JZR_USAGE.equals(usageName)){

						if (JzrMemoryUsageConfig.JZR_USED.equals(figure)){
							value = memPoolBean.getUsage().getUsed();
							break;
						}
						else if (JzrMemoryUsageConfig.JZR_MAX.equals(figure)){
							value = memPoolBean.getUsage().getMax();
							break;
						}
						else if (JzrMemoryUsageConfig.JZR_COMMITTED.equals(figure)){
							value = memPoolBean.getUsage().getCommitted();
							break;
						}					
						else if (JzrMemoryUsageConfig.JZR_INIT.equals(figure)){
							value = memPoolBean.getUsage().getInit();
							break;
						}
						
					}
					
					// peak
					else if (JzrMemoryUsageConfig.JZR_PEAK.equals(usageName)){
						
						if (JzrMemoryUsageConfig.JZR_USED.equals(figure)){
							value = memPoolBean.getPeakUsage().getUsed();
							break;
						}
						else if (JzrMemoryUsageConfig.JZR_MAX.equals(figure)){
							value = memPoolBean.getPeakUsage().getMax();
							break;
						}
						else if (JzrMemoryUsageConfig.JZR_COMMITTED.equals(figure)){
							value = memPoolBean.getPeakUsage().getCommitted();
							break;
						}					
						else if (JzrMemoryUsageConfig.JZR_INIT.equals(figure)){
							value = memPoolBean.getPeakUsage().getInit();
							break;
						}
						
					}
					
					// collection
					else if (JzrMemoryUsageConfig.JZR_COLLECTION.equals(usageName)){
						
						if (JzrMemoryUsageConfig.JZR_USED.equals(figure)){
							value = memPoolBean.getCollectionUsage().getUsed();
							break;
						}
						else if (JzrMemoryUsageConfig.JZR_MAX.equals(figure)){
							value = memPoolBean.getCollectionUsage().getMax();
							break;
						}
						else if (JzrMemoryUsageConfig.JZR_COMMITTED.equals(figure)){
							value = memPoolBean.getCollectionUsage().getCommitted();
							break;
						}					
						else if (JzrMemoryUsageConfig.JZR_INIT.equals(figure)){
							value = memPoolBean.getCollectionUsage().getInit();
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Failed to access memory pool " + poolName + " for memory usage " + usageName + " for figure " + figure);
			return -1;
		}
		
		return value;
	}
	
	protected void collectFigures(List<MemoryPoolMXBean> memPoolBeans){
		for (JzrMemoryPoolConfig config : poolsConfigs){
			if (config.isEnabled()){
		    	String poolName = config.getName();
		    	for (JzrMemoryUsageConfig memUsageConfig : config.getMemoryUsages()){
		    		String usageName = memUsageConfig.getName();
		    		for (String figure : memUsageConfig.getFigures()){
		    			long value = accessMemoryFigure(memPoolBeans, poolName, usageName, figure);
		    			figures.put(poolName + usageName + figure, value);
		    		}
		    	}
			}
		}
	}
	
	public boolean checkPools(List<MemoryPoolMXBean> memPoolBeans){
		List<JzrMemoryPoolConfig> failedPools;
		
		if (poolsConfigs == null || poolsConfigs.isEmpty()){
			enabled = false;
			return false;
		}
		
		failedPools = new ArrayList<JzrMemoryPoolConfig>(10); 
		
		try{
			if (memPoolBeans.isEmpty()){
				logger.warn("Remote memory pool bean list is empty.");
				enabled = false;
				return false;				
			}

			for (JzrMemoryPoolConfig config : poolsConfigs){
				boolean found = false;
				for (MemoryPoolMXBean pool : memPoolBeans){
					found = config.getName().equals(pool.getName());
					if (found)
						break;
				}
				if (!found){
					if (logger.isDebugEnabled())
						logger.debug("Memory Pool " + config.getName() + " not used. Its data collection will be therefore ignored and skipped.");
					failedPools.add(config);
					config.disable();
				}
			}
		}catch(Exception ex){
			logger.warn("Memory Pool accesss error. Memory Pool access disabled", ex);
			enabled = false;
			return false;
		}

		poolsConfigs.removeAll(failedPools);
		
		// check again
		if (poolsConfigs.isEmpty()){
			logger.warn("All memory pools aren't supported. Memory Pool access disabled");
			enabled = false;
			return false;
		}
		
		enabled = true;
		return true;
	}	
	
}
