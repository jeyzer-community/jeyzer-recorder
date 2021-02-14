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
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;

import org.jeyzer.recorder.config.mx.advanced.JzrMemoryConfig;
import org.jeyzer.recorder.config.mx.advanced.JzrMemoryUsageConfig;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class JzrAbstractMemoryAccessor {
	
	protected static final Logger logger = LoggerFactory.getLogger(JzrAbstractMemoryAccessor.class);

	protected static final String MEMORY_FIELD_PREFIX = FileUtil.JZR_FIELD_JZ_PREFIX + "memory:";
	
	protected static final String HEAP_FIELD = "heap";
	protected static final String NON_HEAP_FIELD = "non heap";
	protected static final String OBJ_PENDING_FINALIZATION_FIELD = "obj pending finalization";
	
	protected Map<String, Long> figures = new HashMap<>(8);
	protected int objectPendingFinalizationCount = -1;

	private JzrMemoryConfig memConfig;
	
	protected boolean enabled = false;
	
	public JzrAbstractMemoryAccessor(JzrMemoryConfig config) {
		this.memConfig = config;
	}

	public boolean isMemoryEnabled(){
		return enabled;
	}
	
	public void close(){
		this.figures.clear();
	}
	
	protected void collectFigures(MemoryMXBean memBean){
		// heap
		if (memConfig.getHeapMemoryUsage() != null){
    		for (String figure : memConfig.getHeapMemoryUsage().getFigures()){
    			long value = accessMemoryFigure(memBean, JzrMemoryConfig.JZR_HEAP, figure);
    			figures.put(JzrMemoryConfig.JZR_HEAP + figure, value);
    		}
		}
		// non heap
		if (memConfig.getNonHeapMemoryUsage() != null){
    		for (String figure : memConfig.getNonHeapMemoryUsage().getFigures()){
    			long value = accessMemoryFigure(memBean, JzrMemoryConfig.JZR_NON_HEAP, figure);
    			figures.put(JzrMemoryConfig.JZR_NON_HEAP + figure, value);
    		}
		}
		if (memConfig.iscollectFinalizationCountEnabled())
			objectPendingFinalizationCount = memBean.getObjectPendingFinalizationCount();
	}
	
	private long accessMemoryFigure(MemoryMXBean memBean, String heapType, String figure) {
		long value = -1;
		
        if (logger.isDebugEnabled())
        	logger.debug("Accessing heap/non heap memory info from " + heapType +  ", " + figure + " figure");
        
		try {
			
			// heap memory
			if (JzrMemoryConfig.JZR_HEAP.equals(heapType)){

				if (JzrMemoryUsageConfig.JZR_USED.equals(figure)){
					value = memBean.getHeapMemoryUsage().getUsed();
				}
				else if (JzrMemoryUsageConfig.JZR_MAX.equals(figure)){
					value = memBean.getHeapMemoryUsage().getMax();
				}
				else if (JzrMemoryUsageConfig.JZR_COMMITTED.equals(figure)){
					value = memBean.getHeapMemoryUsage().getCommitted();
				}
				else if (JzrMemoryUsageConfig.JZR_INIT.equals(figure)){
					value = memBean.getHeapMemoryUsage().getInit();
				}
			}
		
			// non heap memory
			else if (JzrMemoryConfig.JZR_NON_HEAP.equals(heapType)){
				
				if (JzrMemoryUsageConfig.JZR_USED.equals(figure)){
					value = memBean.getNonHeapMemoryUsage().getUsed();
				}
				else if (JzrMemoryUsageConfig.JZR_MAX.equals(figure)){
					value = memBean.getNonHeapMemoryUsage().getMax();
				}
				else if (JzrMemoryUsageConfig.JZR_COMMITTED.equals(figure)){
					value = memBean.getNonHeapMemoryUsage().getCommitted();
				}
				else if (JzrMemoryUsageConfig.JZR_INIT.equals(figure)){
					value = memBean.getNonHeapMemoryUsage().getInit();
				}
			
			}
		} catch (Exception e) {
			logger.error("Failed to access heap/non heap memory info " + heapType + " for figure " + figure);
			return -1;
		}
		
		return value;
	}
	
	public void printMemoryInfo(BufferedWriter out) throws IOException {
		if (!isMemoryEnabled()){
			return;
		}
		
		// Full Advanced Java thread dump with locks info from : /jndi/rmi://localhost:2500/jmxrmi
		//	process cpu=0.008129644681811833	system cpu=0.22768383344079524
		//	system free memory=2786799616	system total memory=7474524160
		
		//	mem:<heap type>:<figure1>=value
		//	mem:<heap type>:<figure2>=value

		if (memConfig.getHeapMemoryUsage() != null){
			printMemoryUsageValues(JzrMemoryConfig.JZR_HEAP, HEAP_FIELD, memConfig.getHeapMemoryUsage(), out);
		}

		if (memConfig.getNonHeapMemoryUsage() != null){
			printMemoryUsageValues(JzrMemoryConfig.JZR_NON_HEAP, NON_HEAP_FIELD, memConfig.getNonHeapMemoryUsage(), out);
		}

		if (memConfig.iscollectFinalizationCountEnabled()){
			StringBuilder line = new StringBuilder();
			line.append(MEMORY_FIELD_PREFIX);
			line.append(OBJ_PENDING_FINALIZATION_FIELD);
			line.append(FileUtil.JZR_FIELD_EQUALS);
			line.append(objectPendingFinalizationCount);
			writeln(line.toString(), out);
		}
	}

	private void printMemoryUsageValues(String heapType, String fieldName, JzrMemoryUsageConfig memoryUsage, BufferedWriter out) throws IOException {
		
    	for (String figure : memoryUsage.getFigures()){
    		StringBuilder line = new StringBuilder();
    		Long value = null;
   			value = this.figures.get(heapType + figure);
    		if (value == null)
    			value = (long)-1;  // value not found
    		line.append(MEMORY_FIELD_PREFIX);
   			line.append(fieldName);
   			line.append(FileUtil.JZR_FIELD_SEPARATOR);
   			line.append(figure);
   			line.append(FileUtil.JZR_FIELD_EQUALS);
   			line.append(value);
       		writeln(line.toString(), out);
   		}
	}

	protected void writeln(String info, BufferedWriter out) throws IOException {
		try {
			out.write(info);
			out.newLine();
		} catch (IOException e) {
			logger.error("Failed to write memory info in thread dump file.");
			throw e;
		}
	}
	
}
