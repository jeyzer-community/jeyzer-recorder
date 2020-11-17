package org.jeyzer.recorder.accessor.mx.advanced.process;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeyzer.mx.JeyzerMXBean;
import org.jeyzer.mx.event.JzrEventInfo;
import org.jeyzer.mx.event.JzrPublisherEvent;
import org.jeyzer.recorder.accessor.mx.advanced.JzrAbstractBeanFieldAccessor;
import org.jeyzer.recorder.util.FileUtil;

public abstract class JzrAbstractJeyzerAccessor extends JzrAbstractBeanFieldAccessor{

	public static final String ACCESSOR_NAME = "process:jeyzer_process_parameters";	

	private static final String PUBLISHER_VERSION_FIELD = "jzr.publisher.version=";
	private static final String PUBLISHER_VERSION_FIELD_DISABLED_VALUE = PUBLISHER_VERSION_FIELD + "-1";
	
	private static final String PROCESS_NODE_NAME_FIELD = "jzr.node.name=";
	private static final String PROCESS_NODE_NAME_FIELD_DISABLED_VALUE = PROCESS_NODE_NAME_FIELD + "-1"; 

	private static final String PROCESS_NAME_FIELD = "jzr.process.name=";
	private static final String PROCESS_NAME_FIELD_DISABLED_VALUE = PROCESS_NAME_FIELD + "-1";
	
	private static final String PROCESS_VERSION_FIELD = "jzr.process.version=";
	private static final String PROCESS_VERSION_FIELD_DISABLED_VALUE = PROCESS_VERSION_FIELD + "-1";
	
	private static final String PROCESS_BUILD_NUMBER_FIELD = "jzr.process.build.number=";
	private static final String PROCESS_BUILD_NUMBER_FIELD_DISABLED_VALUE = PROCESS_BUILD_NUMBER_FIELD + "-1";
	
	private static final String PROCESS_PROFILE_NAME_FIELD = "jzr.profile.name=";
	private static final String PROCESS_PROFILE_NAME_FIELD_DISABLED_VALUE = PROCESS_PROFILE_NAME_FIELD + "-1";
	
	private static final String STATIC_PROCESS_CONTEXT_PARAM = "jzr.cxt.param-";
	
	private static final String DYNAMIC_PROCESS_CONTEXT_PARAM = FileUtil.JZR_FIELD_JZ_JZ_PREFIX + "cxt param-";
	
	private static final String APPLICATIVE_EVENT_FIELD = FileUtil.JZR_FIELD_JZ_JZ_PREFIX + "app-evt=";
	private static final String PUBLISHER_EVENT_FIELD = FileUtil.JZR_FIELD_JZ_JZ_PREFIX + "pub-evt=";
	private static final String EVENT_SEPARATOR = "::";
	
	protected JeyzerMXBean jeyzerBean;
	protected boolean initialized = false;
	
	protected String publisherVersion;
	
	protected String nodeName;
	protected String processName;
	protected String processVersion;
	protected String processBuildNumber;
	protected String profileName;
	protected Map<String,String> processStaticCtxParams = new HashMap<>();
	
	protected Map<String,String> processDynamicCtxParams = new HashMap<>();
	protected List<JzrEventInfo> applicativeEvents = new ArrayList<>();
	protected List<JzrPublisherEvent> publisherEvents = new ArrayList<>();

	public void printProcessCardValues(BufferedWriter out) throws IOException {
		printValue(out, 
				PUBLISHER_VERSION_FIELD,
				this.publisherVersion,
				PUBLISHER_VERSION_FIELD_DISABLED_VALUE
				);
		printValue(out, 
				PROCESS_NODE_NAME_FIELD,
				this.nodeName,
				PROCESS_NODE_NAME_FIELD_DISABLED_VALUE
				);
		printValue(out, 
				PROCESS_NAME_FIELD,
				this.processName,
				PROCESS_NAME_FIELD_DISABLED_VALUE
				);
		printValue(out, 
				PROCESS_VERSION_FIELD,
				this.processVersion,
				PROCESS_VERSION_FIELD_DISABLED_VALUE
				);
		printValue(out, 
				PROCESS_BUILD_NUMBER_FIELD,
				this.processBuildNumber,
				PROCESS_BUILD_NUMBER_FIELD_DISABLED_VALUE
				);
		printValue(out, 
				PROCESS_PROFILE_NAME_FIELD,
				this.profileName,
				PROCESS_PROFILE_NAME_FIELD_DISABLED_VALUE
				);
		
		for(Entry<String, String> entry : processStaticCtxParams.entrySet())
			printValue(out, 
					STATIC_PROCESS_CONTEXT_PARAM + entry.getKey() + "=",
					entry.getValue(),
					STATIC_PROCESS_CONTEXT_PARAM // should not happen
					);
	}
	
	public void printValue(BufferedWriter out) throws IOException {
		printApplicativeData(out);
		printApplicativeEvents(out);
		printPublisherEvents(out);
	}
	
	private void printApplicativeData(BufferedWriter out) throws IOException {
		for(Entry<String, String> entry : processDynamicCtxParams.entrySet())
			printValue(out, 
					DYNAMIC_PROCESS_CONTEXT_PARAM + entry.getKey() + FileUtil.JZR_FIELD_EQUALS,
					entry.getValue(),
					DYNAMIC_PROCESS_CONTEXT_PARAM + entry.getKey() + FileUtil.JZR_FIELD_EQUALS // should not happen
					);
	}
	
	private void printApplicativeEvents(BufferedWriter out) throws IOException {
		for(JzrEventInfo event : applicativeEvents)
			printValue(out, 
					APPLICATIVE_EVENT_FIELD,
					formatApplicativeEvent(event),
					APPLICATIVE_EVENT_FIELD // should not happen
					);
	}
	
	private String formatApplicativeEvent(JzrEventInfo event) {
		StringBuilder sb = new StringBuilder(event.getSource());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getService() !=null ? event.getService() : "");
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getCode().getType() !=null ? event.getCode().getType() : "");
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getCode().getAbbreviation());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getCode().getName());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getCode().getDescription() !=null ? event.getCode().getDescription() : "");
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getCode().getLevel().getCapital());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getCode().getSubLevel().value());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getScope().getCapital());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getId());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getMessage() !=null ? event.getMessage() : "");
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getStartTime());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getEndTime());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getThreadId());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getTrustFactor());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.isOneshot());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getCode().getTicket() !=null ? event.getCode().getTicket() : "");
		return sb.toString();
	}

	private void printPublisherEvents(BufferedWriter out) throws IOException {
		for(JzrPublisherEvent event : publisherEvents)
			printValue(out, 
					PUBLISHER_EVENT_FIELD,
					formatPublisherEvent(event),
					PUBLISHER_EVENT_FIELD // should not happen
					);
	}

	private String formatPublisherEvent(JzrPublisherEvent event) {
		StringBuilder sb = new StringBuilder(event.getCode().name());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getMessage());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getLevel().getCapital());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getSubLevel().value());
		sb.append(EVENT_SEPARATOR);
		sb.append(event.getTime());
		return sb.toString();
	}

	protected boolean checkSupport(JeyzerMXBean jeyzerManagerBean){
		this.initialized = true;
		if (jeyzerManagerBean.isActive()){
			this.supported = true;
			return true;
		}
		return false;
	}
	
	public void close() {
		this.jeyzerBean = null;
		this.initialized = false;
		
		this.processDynamicCtxParams.clear();
		this.applicativeEvents.clear();
		this.publisherEvents.clear();
	}
	
	public void processCardClose() {
		this.jeyzerBean = null; // do not keep as server object is closed as well
		
		this.publisherVersion = null;
		this.nodeName = null;
		this.processName = null;
		this.processVersion = null;
		this.processBuildNumber = null;
		this.profileName = null;
		if (this.processStaticCtxParams != null)
			this.processStaticCtxParams.clear();
		this.processStaticCtxParams= null;
	}
	
	protected void logWarning(String message, Exception ex) {
		if (logger.isDebugEnabled())
			logger.warn(message, ex);
		else
			logger.warn(message);
	}
}
