package org.jeyzer.recorder.accessor.mx.advanced.thread;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeyzer.mx.JzrThreadInfo;
import org.jeyzer.recorder.accessor.mx.advanced.JzrAbstractBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractJeyzerAccessor;
import org.jeyzer.recorder.util.FileUtil;

public abstract class JzrAbstractThreadJeyzerAccessor extends JzrAbstractBeanFieldAccessor{

	private static final String THREAD_JEYZER_USER = "thread:jeyzer_thread_user";
	private static final String THREAD_JEYZER_CONTEXT_ID = "thread:jeyzer_thread_context_id";
	private static final String THREAD_JEYZER_ACTION = "thread:jeyzer_thread_action";
	private static final String THREAD_JEYZER_ACTION_START_TIME = "thread:jeyzer_thread_action_start_time";
	private static final String THREAD_JEYZER_ACTION_ID = "thread:jeyzer_thread_action_id";
	private static final String THREAD_JEYZER_PARAMETERS = "thread:jeyzer_thread_parameters";
	
	private static final String JZR_THREAD_JEYZER_USER_FIELD = FileUtil.JZR_FIELD_JZ_JZ_PREFIX + "user" + FileUtil.JZR_FIELD_EQUALS;
	
	private static final String JZR_THREAD_JEYZER_CONTEXT_ID_FIELD = FileUtil.JZR_FIELD_JZ_JZ_PREFIX + "context id" + FileUtil.JZR_FIELD_EQUALS;
	private static final String JZR_THREAD_JEYZER_ACTION_FIELD = FileUtil.JZR_FIELD_JZ_JZ_PREFIX + "action" + FileUtil.JZR_FIELD_EQUALS;
	private static final String JZR_THREAD_JEYZER_ACTION_START_TIME_FIELD = FileUtil.JZR_FIELD_JZ_JZ_PREFIX + "action start time" + FileUtil.JZR_FIELD_EQUALS;
	private static final String JZR_THREAD_JEYZER_ACTION_START_TIME_FIELD_DISABLED_VALUE = JZR_THREAD_JEYZER_ACTION_START_TIME_FIELD + "-1";
	private static final String JZR_THREAD_JEYZER_ACTION_ID_FIELD = FileUtil.JZR_FIELD_JZ_JZ_PREFIX + "action id" + FileUtil.JZR_FIELD_EQUALS;
	private static final String JZR_THREAD_JEYZER_PARAMETERS_FIELD = FileUtil.JZR_FIELD_JZ_JZ_PREFIX + "cxt param-";	
	
	private static final List<String> ACCESSOR_NAMES = new ArrayList<>(
			Arrays.asList(
					THREAD_JEYZER_USER, 
					THREAD_JEYZER_CONTEXT_ID,
					THREAD_JEYZER_ACTION,
					THREAD_JEYZER_ACTION_START_TIME,
					THREAD_JEYZER_ACTION_ID,
					THREAD_JEYZER_PARAMETERS)
					);

	protected List<String> threadInfoKeyList = new ArrayList<>(); 

	protected Map<Long, String> userValues = new HashMap<>();
	protected Map<Long, String> contextIdValues = new HashMap<>();
	protected Map<Long, String> actionValues = new HashMap<>();
	protected Map<Long, Long> actionStartTimeValues = new HashMap<>();
	protected Map<Long, String> actionIdValues = new HashMap<>();
	
	protected Map<Long, Map<String, String>> threadCtxParams = new HashMap<>();	
	
	public static boolean isThreadJeyzerAccessorField(String name) {
		return ACCESSOR_NAMES.contains(name);
	}
	
	public void addThreadField(String name) {
		threadInfoKeyList.add(name);
	}
	
	public void printValue(BufferedWriter out, long id) throws IOException {
		for (String key : threadInfoKeyList){
			if (THREAD_JEYZER_USER.equals(key))
				printValue(out, 
						JZR_THREAD_JEYZER_USER_FIELD,
						this.userValues.get(id),
						JZR_THREAD_JEYZER_USER_FIELD  // empty value
						);
			else if (THREAD_JEYZER_ACTION.equals(key))
				printValue(out, 
						JZR_THREAD_JEYZER_ACTION_FIELD,
						this.actionValues.get(id),
						JZR_THREAD_JEYZER_ACTION_FIELD  // empty value
						);
			else if (THREAD_JEYZER_ACTION_ID.equals(key))
				printValue(out, 
						JZR_THREAD_JEYZER_ACTION_ID_FIELD,
						this.actionIdValues.get(id),
						JZR_THREAD_JEYZER_ACTION_ID_FIELD  // empty value
						);
			else if (THREAD_JEYZER_ACTION_START_TIME.equals(key))
				printValue(out, 
						JZR_THREAD_JEYZER_ACTION_START_TIME_FIELD,
						Long.toString(this.actionStartTimeValues.get(id)),
						JZR_THREAD_JEYZER_ACTION_START_TIME_FIELD_DISABLED_VALUE  // -1
						);
			else if (THREAD_JEYZER_CONTEXT_ID.equals(key))
				printValue(out, 
						JZR_THREAD_JEYZER_CONTEXT_ID_FIELD,
						this.contextIdValues.get(id),
						JZR_THREAD_JEYZER_CONTEXT_ID_FIELD  // empty value
						);
			else if (THREAD_JEYZER_PARAMETERS.equals(key)){
				if (threadCtxParams.get(id) == null)
					continue;
				for (Entry<String, String> ctxParam : threadCtxParams.get(id).entrySet())
					printValue(out, 
							JZR_THREAD_JEYZER_PARAMETERS_FIELD + ctxParam.getKey() + FileUtil.JZR_FIELD_EQUALS,
							ctxParam.getValue(),
							JZR_THREAD_JEYZER_PARAMETERS_FIELD + FileUtil.JZR_FIELD_EQUALS // should not happen
							);
			}
		}
	}
	
	public void close() {
		userValues.clear();
		contextIdValues.clear();
		actionValues.clear();
		actionStartTimeValues.clear();
		actionIdValues.clear();
		threadCtxParams.clear();
	}
	
	protected boolean  checkSupport(JzrAbstractJeyzerAccessor jeyzerAccessor) {
		this.supported = jeyzerAccessor.isSupported();
		return this.supported;
	}

	protected void storeEmptyValues(long threadId) {
		for (String key : threadInfoKeyList){
			if (THREAD_JEYZER_USER.equals(key))
				userValues.put(threadId, "");
			else if (THREAD_JEYZER_ACTION.equals(key))
				actionValues.put(threadId, "");
			else if (THREAD_JEYZER_ACTION_START_TIME.equals(key))
				actionStartTimeValues.put(threadId, Long.valueOf(-1));
			else if (THREAD_JEYZER_ACTION_ID.equals(key))
				actionIdValues.put(threadId, "");
			else if (THREAD_JEYZER_CONTEXT_ID.equals(key))
				contextIdValues.put(threadId, "");
		}
	}

	protected void storeValues(long threadId, JzrThreadInfo threadInfo) {
		for (String key : threadInfoKeyList){
			if (THREAD_JEYZER_USER.equals(key))
				userValues.put(threadId, threadInfo.getUser());
			else if (THREAD_JEYZER_ACTION.equals(key))
				actionValues.put(threadId, threadInfo.getFunctionPrincipal());
			else if (THREAD_JEYZER_ACTION_START_TIME.equals(key))
				actionStartTimeValues.put(threadId, threadInfo.getStartTime());
			else if (THREAD_JEYZER_ACTION_ID.equals(key))
				actionIdValues.put(threadId, threadInfo.getActionId());
			else if (THREAD_JEYZER_CONTEXT_ID.equals(key))
				contextIdValues.put(threadId, threadInfo.getId());
			else if (THREAD_JEYZER_PARAMETERS.equals(key))
				threadCtxParams.put(threadId, threadInfo.getContextParams());
		}
	}

	protected JzrThreadInfo findContext(List<JzrThreadInfo> threadInfos, long threadId) {
		for(JzrThreadInfo ti : threadInfos){
			if (ti.getThreadId() == threadId)
				return ti;
		}
		return null;
	}
	
	protected void logWarning(String message, Exception ex) {
		if (logger.isDebugEnabled())
			logger.warn(message, ex);
		else
			logger.warn(message);
	}	
	
}
