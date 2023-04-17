package org.jeyzer.recorder.accessor;

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





import java.io.File;

import org.jeyzer.recorder.accessor.error.JzrGenerationException;
import org.jeyzer.recorder.accessor.error.JzrValidationException;

public interface JzrAccessor {
	
	public long threadDump(File file)  throws JzrGenerationException;
	
	public void validate()  throws JzrValidationException;
	
	public void initiate(File file)  throws JzrGenerationException;
	
	public String getTimeZoneId();

}
