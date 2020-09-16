package org.jeyzer.recorder.accessor.local.advanced.process;

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





import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.jeyzer.recorder.accessor.local.advanced.JzrLocalBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractRuntimeUpTimeAccessor;

public class LocalRuntimeUpTimeAccessor extends JzrAbstractRuntimeUpTimeAccessor implements JzrLocalBeanFieldAccessor{
	
	@Override
	public boolean checkSupport() {
		return checkSupport(ManagementFactory.getRuntimeMXBean());
	}

	@Override
	public void collect() {

		if (!isSupported()){
			return;
		}		
		
		try {
			if (logger.isDebugEnabled())
				logger.debug("Parsing Runtime MX bean up time info");
        
			RuntimeMXBean rtmxBean= ManagementFactory.getRuntimeMXBean();
			upTimeValue = rtmxBean.getUptime();
		
		} catch (Exception e) {
			logger.error("Failed to access runtime up time info {}", e);
			this.upTimeValue = (long)-1;			
		}		
		
	}
}
