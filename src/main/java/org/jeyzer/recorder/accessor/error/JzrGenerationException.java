package org.jeyzer.recorder.accessor.error;

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





public class JzrGenerationException extends Exception {

	private static final long serialVersionUID = -5930193070552428846L;

	public JzrGenerationException(String message) {
		super(message);
	}	
	
	public JzrGenerationException(Throwable cause) {
		super(cause);
	}

	public JzrGenerationException(String message, Throwable cause) {
		super(message, cause);
	}

	public JzrGenerationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
