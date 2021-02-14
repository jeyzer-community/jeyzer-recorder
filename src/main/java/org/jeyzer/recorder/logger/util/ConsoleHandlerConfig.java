package org.jeyzer.recorder.logger.util;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Recorder
 * --
 * Copyright (C) 2020 - 2021 Jeyzer SAS
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.util.Properties;

public class ConsoleHandlerConfig extends HandlerConfig{

	private static final String HANDLER_NAME = "jeyzer.recorder.log.console";
	
	public ConsoleHandlerConfig(Properties props) {
		super(HANDLER_NAME, props);
	}
	
}
