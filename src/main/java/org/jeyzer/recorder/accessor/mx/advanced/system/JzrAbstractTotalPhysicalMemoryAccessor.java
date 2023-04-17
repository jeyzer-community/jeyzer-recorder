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

public abstract class JzrAbstractTotalPhysicalMemoryAccessor extends JzrAbstractBeanFieldAccessor{

	public static final String ACCESSOR_NAME = "system:total_physical_memory";
	
	private static final String TOTAL_MEMORY_FIELD = FileUtil.JZR_FIELD_JZ_PREFIX + "system total memory" + FileUtil.JZR_FIELD_EQUALS;
	private static final String TOTAL_MEMORY_FIELD_DISABLED_VALUE = TOTAL_MEMORY_FIELD + "-1";
	
	protected Long memoryValue;
	
	public void printValue(BufferedWriter out) throws IOException {
		printValue(out, 
				TOTAL_MEMORY_FIELD,
				memoryValue.toString(),
				TOTAL_MEMORY_FIELD_DISABLED_VALUE
				);		
	}
	
	
}
