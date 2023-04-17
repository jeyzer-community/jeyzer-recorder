package org.jeyzer.recorder.accessor.jmx.advanced;

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

import javax.management.MBeanServerConnection;

public interface JzrMXBeanFieldAccessor {

	public boolean isSupported();
	
	public boolean checkSupport(MBeanServerConnection server);
	
	public void collect(MBeanServerConnection server);
	
	public void printValue(BufferedWriter out) throws IOException;
	
	public long getCaptureDuration();
	
}
