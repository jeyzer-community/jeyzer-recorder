package org.jeyzer.recorder.accessor.local.advanced.process;

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





import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractMemoryPoolAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrMemoryPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalMemoryPoolAccessor extends JzrAbstractMemoryPoolAccessor{
	
	protected static final Logger logger = LoggerFactory.getLogger(LocalMemoryPoolAccessor.class);
	
	public LocalMemoryPoolAccessor(List<JzrMemoryPoolConfig> list) {
		super(list);
	}
	
	public boolean checkPools(){
		List<MemoryPoolMXBean> memPoolBeans;
		
		try{
			
			memPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
		
			return this.checkPools(memPoolBeans);
			
		}catch(Exception ex){
			logger.warn("Memory Pool {} accesss error. Memory Pool access disabled", ex);
			enabled = false;
			return false;
		}
	}
	
	public void collect() {
		List<MemoryPoolMXBean> memPoolBeans = new ArrayList<>();

		if (!isMemoryPoolEnabled()){
			return;
		}
		
		try {
			memPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
		} catch (Exception e) {
			logger.error("Memory pool info collect failed : memory Pool accesss error.", e);
		}
		
		collectFigures(memPoolBeans);
	}
	
}
