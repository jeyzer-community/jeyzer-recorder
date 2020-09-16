package org.jeyzer.recorder.accessor.jmx.advanced.thread;

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





import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.jeyzer.mx.JeyzerMXBean;
import org.jeyzer.mx.JzrThreadInfo;
import org.jeyzer.recorder.accessor.jmx.advanced.process.JeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.thread.JzrAbstractThreadJeyzerAccessor;
import org.jeyzer.recorder.data.JeyzerThreadInfo;

public class ThreadJeyzerAccessor extends JzrAbstractThreadJeyzerAccessor implements JzrThreadBeanFieldAccessor{

	@Override
	public boolean checkSupport(MBeanServerConnection server, ThreadMXBean tmbean, JeyzerAccessor jeyzerAccessor, long[] threadIds) {
		return checkSupport(jeyzerAccessor);		
	}

	@Override
	public void collect(MBeanServerConnection server, ThreadMXBean tmbean, JeyzerAccessor jeyzerAccessor, long[] threadIds) {
		if (jeyzerAccessor == null || !jeyzerAccessor.isSupported())
			return;

		try{
			JeyzerMXBean jhmBean = jeyzerAccessor.createJeyzerMXBean(server);
			collect(server, jhmBean, threadIds);
		} catch (Exception e) {
			logWarning("Failed to collect thread Jeyzer info.", e);
			return;
		}
	}

	protected void collect(MBeanServerConnection server, JeyzerMXBean jhmBean, long[] threadIds) {
		long startTime;
    	startTime = System.currentTimeMillis();
    	CompositeData[] compositeThreadInfos = null;
    	
		try {
	    	compositeThreadInfos = (CompositeData[])
	    			server.getAttribute(new ObjectName(JeyzerMXBean.JEYZER_MXBEAN_NAME), "ThreadInfoList");
		} catch (Exception e) {
			logWarning("Failed to collect thread Jeyzer info.", e);
			return;
		}    	
    	
    	long endTime = System.currentTimeMillis();
    	this.captureDuration = this.captureDuration + endTime - startTime;
		if (logger.isDebugEnabled()) 
			logger.debug("MX bean Jeyzer thread info access time : {} ms", endTime - startTime);

    	List<JzrThreadInfo> threadInfos = new ArrayList<>();		
    	for (int i=0; i<compositeThreadInfos.length; i++)
    		if (compositeThreadInfos[i] != null){
    			JzrThreadInfo jti = JeyzerThreadInfo.from(compositeThreadInfos[i]);
    			threadInfos.add(jti);
    		}
		
		for (long threadId : threadIds){
			JzrThreadInfo threadInfo = findContext(threadInfos, threadId);
			if (threadInfo != null)
				storeValues(threadId, threadInfo);
			else
				storeEmptyValues(threadId);
		}
	}	

	@Override
	public long getCaptureDuration() {
		return this.captureDuration;
	}

}
