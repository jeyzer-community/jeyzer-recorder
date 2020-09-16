package org.jeyzer.recorder.accessor.local.advanced;

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

public interface JzrLocalBeanFieldAccessor {

	public boolean isSupported();
	
	public boolean checkSupport();
	
	public void collect();
	
	public void printValue(BufferedWriter out) throws IOException;
	
}
