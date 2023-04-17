package org.jeyzer.recorder.accessor.mx.advanced.thread;

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
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import org.jeyzer.recorder.accessor.mx.advanced.JzrAbstractBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractJeyzerAccessor;
import org.jeyzer.recorder.util.FileUtil;

public abstract class JzrAbstractThreadCPUAccessor extends JzrAbstractBeanFieldAccessor {

	public static final String ACCESSOR_NAME = "thread:thread_cpu_time";
	
	private static final String JZR_CPU_TIME_FIELD = FileUtil.JZR_FIELD_JZ_PREFIX + "cpu time" + FileUtil.JZR_FIELD_EQUALS;
	private static final String JZR_CPU_TIME_DISABLED_VALUE = JZR_CPU_TIME_FIELD + "-1";
	
	protected Map<Long, Long> cpuPerThread = new HashMap<>(50);
	
	public void printValue(BufferedWriter out, long id) throws IOException {
		Long value = this.cpuPerThread.get(id);
		
		printValue(out, 
				JZR_CPU_TIME_FIELD,
				value.toString(),
				JZR_CPU_TIME_DISABLED_VALUE
				);
	}
	
	public void close() {
		this.cpuPerThread.clear();
	}
	
	public boolean checkSupport(ThreadMXBean tmbean, JzrAbstractJeyzerAccessor jeyzerAccessor, long[] threadIds) {
		this.supported = tmbean.isThreadCpuTimeSupported();
		if (!supported)
			logger.warn("Monitored JVM doesn't provide thread CPU/user time info.");
		
		return supported;
	}	
	
	
}
