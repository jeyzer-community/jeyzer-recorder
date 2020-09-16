package org.jeyzer.recorder.accessor.error;

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





public class JzrProcessNotAvailableException extends JzrGenerationException {

	private static final long serialVersionUID = -3789133090244717368L;

	public JzrProcessNotAvailableException(String message) {
		super(message);
	}	
	
	public JzrProcessNotAvailableException(Throwable cause) {
		super(cause);
	}

	public JzrProcessNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public JzrProcessNotAvailableException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
