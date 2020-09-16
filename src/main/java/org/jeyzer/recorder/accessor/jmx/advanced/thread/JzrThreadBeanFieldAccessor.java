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





import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ThreadMXBean;

import javax.management.MBeanServerConnection;

import org.jeyzer.recorder.accessor.jmx.advanced.process.JeyzerAccessor;

public interface JzrThreadBeanFieldAccessor {

	public boolean checkSupport(MBeanServerConnection server, ThreadMXBean tmbean, JeyzerAccessor jeyzerAccessor, long[] threadIds);
	
	public boolean isSupported();
	
	public void collect(MBeanServerConnection server, ThreadMXBean tmbean, JeyzerAccessor jeyzerAccessor, long[] threadIds);
	
	public void printValue(BufferedWriter out, long id) throws IOException;
	
	public long getCaptureDuration();
	
	public void close();

}
