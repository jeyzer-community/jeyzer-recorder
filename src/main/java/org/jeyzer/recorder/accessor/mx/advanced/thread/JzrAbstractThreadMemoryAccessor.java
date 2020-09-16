package org.jeyzer.recorder.accessor.mx.advanced.thread;

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
import java.util.HashMap;
import java.util.Map;

import org.jeyzer.recorder.accessor.mx.advanced.JzrAbstractBeanFieldAccessor;
import org.jeyzer.recorder.util.FileUtil;

public abstract class JzrAbstractThreadMemoryAccessor extends JzrAbstractBeanFieldAccessor {

	public static final String ACCESSOR_NAME = "thread:thread_allocated_bytes";
	
	private static final String JZR_ALLOCATED_BYTES_FIELD = FileUtil.JZR_FIELD_JZ_PREFIX + "memory" + FileUtil.JZR_FIELD_EQUALS;
	private static final String JZR_ALLOCATED_BYTES_DISABLED_VALUE = JZR_ALLOCATED_BYTES_FIELD + "-1";	
	
	protected Map<Long, Long> allocatedBytesPerThread = new HashMap<>(50);
	
	public void printValue(BufferedWriter out, long id) throws IOException {
		Long value = this.allocatedBytesPerThread.get(id);
		
		printValue(out, 
				JZR_ALLOCATED_BYTES_FIELD,
				value.toString(),
				JZR_ALLOCATED_BYTES_DISABLED_VALUE
				);
	}
	
	public void close() {
		this.allocatedBytesPerThread.clear();
	}
}
