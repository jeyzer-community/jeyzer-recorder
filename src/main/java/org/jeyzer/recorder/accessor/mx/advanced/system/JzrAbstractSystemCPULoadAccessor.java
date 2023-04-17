package org.jeyzer.recorder.accessor.mx.advanced.system;

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





import java.io.BufferedWriter;
import java.io.IOException;

import org.jeyzer.recorder.accessor.mx.advanced.JzrAbstractBeanFieldAccessor;
import org.jeyzer.recorder.util.FileUtil;

public abstract class JzrAbstractSystemCPULoadAccessor extends JzrAbstractBeanFieldAccessor{

	public static final String ACCESSOR_NAME = "system:system_cpu_load";	
	
	private static final String SYSTEM_CPU_FIELD = FileUtil.JZR_FIELD_JZ_PREFIX + "system cpu" + FileUtil.JZR_FIELD_EQUALS;
	private static final String SYSTEM_CPU_FIELD_DISABLED_VALUE = SYSTEM_CPU_FIELD + "-1";	
	
	protected Double cpuValue;
	
	public void printValue(BufferedWriter out) throws IOException {
		printValue(out, 
				SYSTEM_CPU_FIELD,
				cpuValue.toString(),
				SYSTEM_CPU_FIELD_DISABLED_VALUE
				);
	}

	
	
}
