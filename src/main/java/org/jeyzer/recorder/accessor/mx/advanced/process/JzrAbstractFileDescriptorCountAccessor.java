package org.jeyzer.recorder.accessor.mx.advanced.process;

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

public abstract class JzrAbstractFileDescriptorCountAccessor extends JzrAbstractBeanFieldAccessor{

	public static final String ACCESSOR_NAME = "process:process_open_file_desc_count";
	
	private static final String PROCESS_FD_DESC_COUNT_FIELD = FileUtil.JZR_FIELD_JZ_PREFIX + "open file desc count" + FileUtil.JZR_FIELD_EQUALS;
	private static final String PROCESS_FD_DESC_COUNT_FIELD_DISABLED_VALUE = PROCESS_FD_DESC_COUNT_FIELD + "-1";	

	protected Long fdCountValue;	
	
	public void printValue(BufferedWriter out) throws IOException {
		printValue(out, 
				PROCESS_FD_DESC_COUNT_FIELD,
				fdCountValue.toString(),
				PROCESS_FD_DESC_COUNT_FIELD_DISABLED_VALUE
				);
	}
	
}
