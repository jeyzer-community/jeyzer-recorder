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
import java.lang.management.RuntimeMXBean;

import org.jeyzer.recorder.accessor.mx.advanced.JzrAbstractBeanFieldAccessor;
import org.jeyzer.recorder.util.FileUtil;

public abstract class JzrAbstractRuntimeUpTimeAccessor extends JzrAbstractBeanFieldAccessor{
	
	public static final String ACCESSOR_NAME = "process:process_up_time";
	
	private static final String PROCESS_UP_TIME_FIELD = FileUtil.JZR_FIELD_JZ_PREFIX + "process up time" + FileUtil.JZR_FIELD_EQUALS;
	private static final String PROCESS_UP_TIME_FIELD_DISABLED_VALUE = PROCESS_UP_TIME_FIELD + "-1";	
	
	protected Long upTimeValue;


	protected boolean checkSupport(RuntimeMXBean runtimeMXBean) {
		this.supported =  true;
		
		try{
			long upTime = runtimeMXBean.getUptime();

			if (upTime == -1) {
				this.supported = false;
				logger.warn("Monitored JVM doesn't provide process up time.");
				}
		}catch(Exception ex){
			logger.warn("Monitored JVM doesn't provide runtime info. Failed to access process up time", ex);
			this.supported = false;
		}
		
		return this.supported;
	}	
	
	public void printValue(BufferedWriter out) throws IOException {
		printValue(out, 
				PROCESS_UP_TIME_FIELD,
				upTimeValue.toString(),
				PROCESS_UP_TIME_FIELD_DISABLED_VALUE
				);
	}
}
