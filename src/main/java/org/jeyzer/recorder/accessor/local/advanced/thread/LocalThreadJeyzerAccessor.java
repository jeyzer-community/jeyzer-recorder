package org.jeyzer.recorder.accessor.local.advanced.thread;

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





import java.lang.management.ThreadMXBean;
import java.util.List;

import org.jeyzer.mx.JeyzerMXBean;
import org.jeyzer.mx.JzrThreadInfo;
import org.jeyzer.recorder.accessor.local.advanced.process.LocalJeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractJeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractThreadJeyzerAccessor;

public class LocalThreadJeyzerAccessor extends JzrAbstractThreadJeyzerAccessor implements JzrLocalThreadBeanFieldAccessor{

	@Override
	public boolean checkSupport(ThreadMXBean tmbean, JzrAbstractJeyzerAccessor jeyzerAccessor, long[] threadIds) {
		return checkSupport(jeyzerAccessor);
	}

	@Override
	public void collect(ThreadMXBean tmbean, LocalJeyzerAccessor jeyzerAccessor, long[] threadIds) {
		if (jeyzerAccessor == null || !jeyzerAccessor.isSupported())
			return;

		try{
			JeyzerMXBean jhmBean = jeyzerAccessor.getJeyzerMXBean();
			collect(jhmBean, threadIds);
		} catch (Throwable e) {
			logger.warn("Failed to collect thread Jeyzer info.");
			return;
		}
	}

	
	private void collect(JeyzerMXBean jhmBean, long[] threadIds) {
		List<JzrThreadInfo> threadInfos = jhmBean.getThreadInfoList();

		if (logger.isDebugEnabled()) 
			logger.debug("Accessing Jeyzer thread info");    	
    	
		for (long threadId : threadIds){
			JzrThreadInfo threadInfo = findContext(threadInfos, threadId);
			if (threadInfo != null)
				storeValues(threadId, threadInfo);
			else
				storeEmptyValues(threadId);
		}
	}

}
