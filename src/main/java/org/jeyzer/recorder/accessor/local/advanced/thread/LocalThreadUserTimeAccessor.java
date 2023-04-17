package org.jeyzer.recorder.accessor.local.advanced.thread;

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





import java.lang.management.ThreadMXBean;

import org.jeyzer.recorder.accessor.local.advanced.process.LocalJeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractlThreadUserTimeAccessor;

public class LocalThreadUserTimeAccessor extends JzrAbstractlThreadUserTimeAccessor implements JzrLocalThreadBeanFieldAccessor{

	@Override
	public void collect(ThreadMXBean tmbean, LocalJeyzerAccessor jeyzerAccessor, long[] threadIds) {
		Long value;
		
		if (!isSupported()){
			return;
		}

		for (long id : threadIds){
			if (logger.isDebugEnabled())
				logger.debug("Accessing user time info from MX bean");
			
			// access it
			value = tmbean.getThreadUserTime(id);
				
			// store it
			this.userTimePerThread.put(id, value);
		}
	}
}
