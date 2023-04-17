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

import org.jeyzer.mx.event.JzrEventCode;
import org.jeyzer.mx.event.JzrEventInfo;
import org.jeyzer.mx.event.JzrEventScope;

public class JeyzerEventInfo implements JzrEventInfo {
	
	public static final String MX_FIELD_NAME_CODE = "code";
	public static final String MX_FIELD_NAME_SCOPE = "scope";
	public static final String MX_FIELD_NAME_SOURCE = "source";
	public static final String MX_FIELD_NAME_SERVICE = "service";
	public static final String MX_FIELD_NAME_ID = "id";
	public static final String MX_FIELD_NAME_MESSAGE = "message";
	public static final String MX_FIELD_NAME_START_TIME = "startTime";
	public static final String MX_FIELD_NAME_END_TIME = "endTime";
	public static final String MX_FIELD_NAME_THREAD_ID = "threadId";
	public static final String MX_FIELD_NAME_TRUST_FACTOR = "trustFactor";
	public static final String MX_FIELD_NAME_ONESHOT = "oneshot";

	private String source;
	private String service;
	private long startTime;
	private long endTime = -1;
	private JzrEventScope scope;
	private long threadId = -1;
	private boolean oneshot;
	
	// applicative event fields
	private JzrEventCode code;
	private String id;
	private String message;
	private short trustFactor;
	
	@Override
	public String getSource() {
		return source;
	}

	@Override
	public String getService() {
		return service;
	}

	@Override
	public JzrEventCode getCode() {
		return code;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public JzrEventScope getScope() {
		return scope;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public short getTrustFactor() {
		return trustFactor;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public long getEndTime() {
		return endTime;
	}

	@Override
	public long getThreadId() {
		return threadId;
	}

	@Override
	public boolean isOneshot() {
		return oneshot;
	}
	
	public void setSource(String source) {
		this.source = source;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setScope(JzrEventScope scope) {
		this.scope = scope;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public void setOneshot(boolean oneshot) {
		this.oneshot = oneshot;
	}

	public void setCode(JzrEventCode code) {
		this.code = code;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setTrustFactor(short trustFactor) {
		this.trustFactor = trustFactor;
	}

	public static JeyzerEventInfo from(CompositeData compositeData) {
		JeyzerEventInfo event = new JeyzerEventInfo();
		
		event.setSource((String)compositeData.get(MX_FIELD_NAME_SOURCE));
		event.setService((String)compositeData.get(MX_FIELD_NAME_SERVICE));
		event.setId((String)compositeData.get(MX_FIELD_NAME_ID));
		event.setMessage((String)compositeData.get(MX_FIELD_NAME_MESSAGE));
		event.setStartTime((Long)compositeData.get(MX_FIELD_NAME_START_TIME));
		event.setEndTime((Long)compositeData.get(MX_FIELD_NAME_END_TIME));
		event.setThreadId((Long)compositeData.get(MX_FIELD_NAME_THREAD_ID));
		event.setTrustFactor((Short)compositeData.get(MX_FIELD_NAME_TRUST_FACTOR));
		event.setOneshot((Boolean)compositeData.get(MX_FIELD_NAME_ONESHOT));
		event.setScope(JzrEventScope.valueOf((String)compositeData.get(MX_FIELD_NAME_SCOPE)));
		event.setCode(JeyzerEventCode.from((CompositeData)compositeData.get(MX_FIELD_NAME_CODE)));
		
		return event;
	}

}
