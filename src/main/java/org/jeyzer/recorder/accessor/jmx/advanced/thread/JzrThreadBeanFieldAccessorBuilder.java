package org.jeyzer.recorder.accessor.jmx.advanced.thread;

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





import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractThreadCPUAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractThreadJeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractThreadMemoryAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractlThreadUserTimeAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrBeanFieldConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class JzrThreadBeanFieldAccessorBuilder {

	private static final Logger logger = LoggerFactory.getLogger(JzrThreadBeanFieldAccessorBuilder.class);
	
	private static final JzrThreadBeanFieldAccessorBuilder builder = new JzrThreadBeanFieldAccessorBuilder();
	
	private JzrThreadBeanFieldAccessorBuilder(){
	}
	
	public static JzrThreadBeanFieldAccessorBuilder newInstance(){
		return builder;
	}
	
	public List<JzrThreadBeanFieldAccessor> buildBeanFieldConfigsforCategory(List<JzrBeanFieldConfig> configs) {
		List<JzrThreadBeanFieldAccessor> threadBeanFiledaccessors = new ArrayList<>(); 
		JzrThreadBeanFieldAccessor accessor;
		
		if (configs == null || configs.isEmpty())
			return threadBeanFiledaccessors;
		
		ThreadJeyzerAccessor jhThreadAccessor = null;
		for (JzrBeanFieldConfig config : configs){
			accessor = null;
			String name = config.getCategory() + ":" + config.getName();
			
			logger.debug("Loading thread bean accessor : " + name);
			if (JzrAbstractThreadMemoryAccessor.ACCESSOR_NAME.equals(name))
				accessor = new ThreadMemoryAccessor();
			else if (JzrAbstractThreadCPUAccessor.ACCESSOR_NAME.equals(name))
				accessor = new ThreadCPUAccessor();
			else if (JzrAbstractlThreadUserTimeAccessor.ACCESSOR_NAME.equals(name))
				accessor = new ThreadUserTimeAccessor();
			else if (JzrAbstractThreadJeyzerAccessor.isThreadJeyzerAccessorField(name)){
				if (jhThreadAccessor == null){
					jhThreadAccessor = new ThreadJeyzerAccessor();
					accessor = jhThreadAccessor;
				}
				jhThreadAccessor.addThreadField(name);
			}
			else
				logger.error("Thread bean configuration " + name + " is invalid. No corresponding accessor found.");
			
			if (accessor != null)
				threadBeanFiledaccessors.add(accessor);
		}
		
		return threadBeanFiledaccessors;
	}

}
