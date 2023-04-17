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





import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ThreadMXBean;

import org.jeyzer.recorder.accessor.local.advanced.process.LocalJeyzerAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractJeyzerAccessor;

public interface JzrLocalThreadBeanFieldAccessor {

	public boolean checkSupport(ThreadMXBean tmbean, JzrAbstractJeyzerAccessor jeyzerAccessor, long[] threadIds);
	
	public boolean isSupported();
	
	public void collect(ThreadMXBean tmbean, LocalJeyzerAccessor jeyzerAccessor, long[] threadIds);
	
	public void printValue(BufferedWriter out, long id) throws IOException;
	
	public void close();

}
