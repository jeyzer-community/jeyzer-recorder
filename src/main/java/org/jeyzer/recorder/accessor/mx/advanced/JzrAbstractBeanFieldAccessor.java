package org.jeyzer.recorder.accessor.mx.advanced;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JzrAbstractBeanFieldAccessor {

	protected static final Logger logger = LoggerFactory.getLogger(JzrAbstractBeanFieldAccessor.class);	
	
	protected long captureDuration;
	
	protected boolean supported;
	
	public boolean isSupported() {
		return supported;
	}
	
	protected void writeln(BufferedWriter out, String info) throws IOException {
		try {
			out.write(info);
			out.newLine();
		} catch (IOException e) {
			logger.error("Failed to write in thread dump file.");
			throw e;
		}
	}

	protected void printValue(BufferedWriter out, String field, String value, String disabledValue) throws IOException {
		if (isSupported()){
			StringBuilder sb = new StringBuilder(field);
			
			if (value == null)
				value = "-1";  // probably failed to access the value
			
			sb.append(value);
			writeln(out, sb.toString());
		}else {
			writeln(out, disabledValue);
		}
	}		
	
}
