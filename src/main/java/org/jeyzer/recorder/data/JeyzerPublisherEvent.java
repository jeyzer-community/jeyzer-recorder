package org.jeyzer.recorder.data;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Recorder
 * --
 * Copyright (C) 2020 - 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


import javax.management.openmbean.CompositeData;

import org.jeyzer.mx.event.JzrEventLevel;
import org.jeyzer.mx.event.JzrEventSubLevel;
import org.jeyzer.mx.event.JzrPublisherEvent;
import org.jeyzer.mx.event.JzrPublisherEventCode;

public class JeyzerPublisherEvent implements JzrPublisherEvent {
	
	public static final String MX_FIELD_NAME_CODE = "code";
	public static final String MX_FIELD_NAME_LEVEL = "level";
	public static final String MX_FIELD_NAME_SUB_LEVEL = "subLevel";
	public static final String MX_FIELD_NAME_MESSAGE = "message";
	public static final String MX_FIELD_NAME_TIME = "time";
	
	private long time;
	private JzrPublisherEventCode code;
	private String message;
	
	@Override
	public long getTime() {
		return time;
	}

	@Override
	public JzrEventLevel getLevel() {
		return code.getLevel();
	}

	@Override
	public JzrEventSubLevel getSubLevel() {
		return code.getSubLevel();
	}

	@Override
	public JzrPublisherEventCode getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}
	
	public void setTime(long time) {
		this.time = time;
	}

	public void setCode(JzrPublisherEventCode code) {
		this.code = code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public static JeyzerPublisherEvent from(CompositeData compositeData) {
		JeyzerPublisherEvent event = new JeyzerPublisherEvent();
		
		event.setCode(JzrPublisherEventCode.valueOf((String)compositeData.get(MX_FIELD_NAME_CODE)));
		event.setMessage((String)compositeData.get(MX_FIELD_NAME_MESSAGE));
		event.setTime((Long)compositeData.get(MX_FIELD_NAME_TIME));
		
		return event;
	}
}
