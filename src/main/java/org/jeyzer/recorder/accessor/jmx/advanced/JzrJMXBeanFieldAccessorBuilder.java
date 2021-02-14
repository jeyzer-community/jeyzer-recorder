package org.jeyzer.recorder.accessor.jmx.advanced;

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

import org.jeyzer.recorder.accessor.jmx.advanced.process.GenericMXBeanAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.process.JeyzerAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.process.ProcessCPULoadAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.process.RuntimeUpTimeAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.system.FreePhysicalMemoryAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.system.SystemCPULoadAccessor;
import org.jeyzer.recorder.accessor.jmx.advanced.system.TotalPhysicalMemoryAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractGenericMXBeanAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractJeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractProcessCPULoadAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractRuntimeUpTimeAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.system.JzrAbstractFreePhysicalMemoryAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.system.JzrAbstractSystemCPULoadAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.system.JzrAbstractTotalPhysicalMemoryAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrBeanFieldConfig;
import org.jeyzer.recorder.config.mx.advanced.JzrGenericMXBeanConfig;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class JzrJMXBeanFieldAccessorBuilder {

	private static final Logger logger = LoggerFactory.getLogger(JzrJMXBeanFieldAccessorBuilder.class);
	
	private static final JzrJMXBeanFieldAccessorBuilder builder = new JzrJMXBeanFieldAccessorBuilder();

	public static JzrJMXBeanFieldAccessorBuilder newInstance(){
		return builder;
	}

	private JzrJMXBeanFieldAccessorBuilder() {
	}
	
	public List<JzrMXBeanFieldAccessor> buildBeanFieldConfigsforCategory(List<JzrBeanFieldConfig> configs, 
			JeyzerAccessor jeyzerAccessor, GenericMXBeanAccessor genericAccessor) {
		List<JzrMXBeanFieldAccessor> beanFieldaccessors = new ArrayList<>(); 
		JzrMXBeanFieldAccessor accessor;
		
		if (configs == null || configs.isEmpty())
			return beanFieldaccessors;
		
		for (JzrBeanFieldConfig config : configs){
			accessor = null;
			String name = config.getCategory() + ":" + config.getName();
			
			if (logger.isDebugEnabled())
				logger.debug("Loading bean accessor : " + name);
			
			if (JzrAbstractProcessCPULoadAccessor.ACCESSOR_NAME.equals(name))
				accessor = new ProcessCPULoadAccessor();
			else if (JzrAbstractSystemCPULoadAccessor.ACCESSOR_NAME.equals(name))
				accessor = new SystemCPULoadAccessor();
			else if (JzrAbstractFreePhysicalMemoryAccessor.ACCESSOR_NAME.equals(name))
				accessor = new FreePhysicalMemoryAccessor();
			else if (JzrAbstractTotalPhysicalMemoryAccessor.ACCESSOR_NAME.equals(name))
				accessor = new TotalPhysicalMemoryAccessor();
			else if (JzrAbstractRuntimeUpTimeAccessor.ACCESSOR_NAME.equals(name))
				accessor = new RuntimeUpTimeAccessor();
			else if (JzrAbstractJeyzerAccessor.ACCESSOR_NAME.equals(name))
				accessor = jeyzerAccessor;
			else if (JzrAbstractGenericMXBeanAccessor.ACCESSOR_NAME.equals(name)){
				accessor = genericAccessor;
				genericAccessor.addDynamicFieldConfig((JzrGenericMXBeanConfig)config);
			}
			
			if (accessor != null)
				beanFieldaccessors.add(accessor);
			else
				logger.error("Bean configuration " + name + " is invalid. No corresponding accessor found.");
		}
		
		return beanFieldaccessors;
	}	
	
}
