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





public class JzrValidationException extends Exception {

	private static final long serialVersionUID = -6485807526499499332L;

	public JzrValidationException(String message) {
		super(message);
	}	
	
	public JzrValidationException(Throwable cause) {
		super(cause);
	}

	public JzrValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public JzrValidationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
