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





import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;

import org.jeyzer.mx.JzrThreadInfo;
import org.jeyzer.recorder.util.JMXUtil;

public class JeyzerThreadInfo implements JzrThreadInfo {

	public static final String MX_FIELD_NAME_ID = "id";
	public static final String MX_FIELD_NAME_FUNCTION_PRINCIPAL = "functionPrincipal";
	public static final String MX_FIELD_NAME_START_TIME = "startTime";
	public static final String MX_FIELD_NAME_THREAD_ID = "threadId";
	public static final String MX_FIELD_NAME_ACTION_ID = "actionId";
	public static final String MX_FIELD_NAME_USER = "user";
	public static final String MX_FIELD_NAME_CONTEXT_PARAMS = "contextParams";
	
	private long threadId;    // unique id
	private String jhId;      // unique id
	private long startTime;
	private String actionId;
	private String user;
	private String functionPrincipal;
	private Map<String, String> contextParams = new HashMap<>();	
	
	@Override
	public String getActionId() {
		return actionId;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public long getThreadId() {
		return threadId;
	}

	@Override
	public String getId() {
		return jhId;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getFunctionPrincipal() {
		return functionPrincipal;
	}

	@Override
	public Map<String, String> getContextParams() {
		return contextParams;
	}

	public void setId(String jhId) {
		this.jhId = jhId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setFunctionPrincipal(String functionPrincipal) {
		this.functionPrincipal = functionPrincipal;
	}

	public void setContextParams(Map<String, String> contextParams) {
		this.contextParams = contextParams;
	}
	
	public static JeyzerThreadInfo from(CompositeData compositeData) {
		JeyzerThreadInfo ti = new JeyzerThreadInfo();
		
		ti.setId((String)compositeData.get(MX_FIELD_NAME_ID));
		ti.setFunctionPrincipal((String)compositeData.get(MX_FIELD_NAME_FUNCTION_PRINCIPAL));
		ti.setStartTime((Long)compositeData.get(MX_FIELD_NAME_START_TIME));
		ti.setThreadId((Long)compositeData.get(MX_FIELD_NAME_THREAD_ID));
		ti.setActionId((String)compositeData.get(MX_FIELD_NAME_ACTION_ID));
		ti.setUser((String)compositeData.get(MX_FIELD_NAME_USER));
		TabularDataSupport tabData = (TabularDataSupport)compositeData.get(MX_FIELD_NAME_CONTEXT_PARAMS);
		if (tabData != null)
			ti.setContextParams(JMXUtil.convertTabularDataToStringMap(tabData));
		
		return ti;
	}
}
