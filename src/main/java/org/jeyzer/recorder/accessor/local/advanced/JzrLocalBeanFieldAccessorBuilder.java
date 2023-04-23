package org.jeyzer.recorder.accessor.local.advanced;

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





import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.accessor.local.advanced.process.LocalFileDescriptorCountAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalGenericMXBeanAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalJeyzerAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalProcessCPULoadAccessor;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalRuntimeUpTimeAccessor;
import org.jeyzer.recorder.accessor.local.advanced.system.LocalFreePhysicalMemoryAccessor;
import org.jeyzer.recorder.accessor.local.advanced.system.LocalSystemCPULoadAccessor;
import org.jeyzer.recorder.accessor.local.advanced.system.LocalTotalPhysicalMemoryAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractFileDescriptorCountAccessor;
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

public class JzrLocalBeanFieldAccessorBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(JzrLocalBeanFieldAccessorBuilder.class);
	
	private static final JzrLocalBeanFieldAccessorBuilder builder = new JzrLocalBeanFieldAccessorBuilder();

	public static JzrLocalBeanFieldAccessorBuilder newInstance(){
		return builder;
	}

	private JzrLocalBeanFieldAccessorBuilder() {
	}
	
	public List<JzrLocalBeanFieldAccessor> buildBeanFieldConfigsforCategory(List<JzrBeanFieldConfig> configs, LocalJeyzerAccessor jeyzerAccessor, LocalGenericMXBeanAccessor genericAccessor) {
		List<JzrLocalBeanFieldAccessor> beanFieldaccessors = new ArrayList<>(); 
		JzrLocalBeanFieldAccessor accessor;
		
		if (configs == null || configs.isEmpty())
			return beanFieldaccessors;
		
		for (JzrBeanFieldConfig config : configs){
			accessor = null;
			String name = config.getCategory() + ":" + config.getName();
			
			if (logger.isDebugEnabled())
				logger.debug("Loading bean accessor : " + name);
			
			if (JzrAbstractProcessCPULoadAccessor.ACCESSOR_NAME.equals(name))
				accessor = new LocalProcessCPULoadAccessor();
			else if (JzrAbstractSystemCPULoadAccessor.ACCESSOR_NAME.equals(name))
				accessor = new LocalSystemCPULoadAccessor();
			else if (JzrAbstractFreePhysicalMemoryAccessor.ACCESSOR_NAME.equals(name))
				accessor = new LocalFreePhysicalMemoryAccessor();
			else if (JzrAbstractTotalPhysicalMemoryAccessor.ACCESSOR_NAME.equals(name))
				accessor = new LocalTotalPhysicalMemoryAccessor();
			else if (JzrAbstractRuntimeUpTimeAccessor.ACCESSOR_NAME.equals(name))
				accessor = new LocalRuntimeUpTimeAccessor();
			else if (JzrAbstractFileDescriptorCountAccessor.ACCESSOR_NAME.equals(name))
				accessor = new LocalFileDescriptorCountAccessor();
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
