package org.jeyzer.recorder.accessor.local.advanced.thread;

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





import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.accessor.local.advanced.thread.LocalThreadMemoryAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractThreadJeyzerAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrBeanFieldConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JzrThreadBeanFieldAccessorBuilder {
	private static final Logger logger = LoggerFactory.getLogger(JzrThreadBeanFieldAccessorBuilder.class);
	
	private static final JzrThreadBeanFieldAccessorBuilder builder = new JzrThreadBeanFieldAccessorBuilder();
	
	private JzrThreadBeanFieldAccessorBuilder(){
	}
	
	public static JzrThreadBeanFieldAccessorBuilder newInstance(){
		return builder;
	}
	
	public List<JzrLocalThreadBeanFieldAccessor> buildBeanFieldConfigsforCategory(List<JzrBeanFieldConfig> configs) {
		List<JzrLocalThreadBeanFieldAccessor> threadBeanFiledaccessors = new ArrayList<>(); 
		JzrLocalThreadBeanFieldAccessor accessor;
		
		if (configs == null || configs.isEmpty())
			return threadBeanFiledaccessors;
		
		LocalThreadJeyzerAccessor jhThreadAccessor = null;
		for (JzrBeanFieldConfig config : configs){
			accessor = null;
			String name = config.getCategory() + ":" + config.getName();
			
			logger.debug("Loading thread bean accessor : {}", name);
			if (LocalThreadMemoryAccessor.ACCESSOR_NAME.equals(name))
				accessor = new LocalThreadMemoryAccessor();
			else if (LocalThreadCPUAccessor.ACCESSOR_NAME.equals(name))
				accessor = new LocalThreadCPUAccessor();
			else if (LocalThreadUserTimeAccessor.ACCESSOR_NAME.equals(name))
				accessor = new LocalThreadUserTimeAccessor();
			else if (JzrAbstractThreadJeyzerAccessor.isThreadJeyzerAccessorField(name)){
				if (jhThreadAccessor == null){
					jhThreadAccessor = new LocalThreadJeyzerAccessor();
					accessor = jhThreadAccessor;
				}
				jhThreadAccessor.addThreadField(name);
			}
			else
				logger.error("Thread bean configuration {} is invalid. No corresponding accessor found.", name);
			
			if (accessor != null)
				threadBeanFiledaccessors.add(accessor);
		}
		
		return threadBeanFiledaccessors;
	}
}
