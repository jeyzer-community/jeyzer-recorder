package org.jeyzer.recorder.accessor.local.advanced.process.flags;

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
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import javax.management.MBeanServer;

import org.jeyzer.recorder.accessor.local.advanced.process.LocalTask;
import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.config.mx.advanced.JzrJVMFlagConfig;

import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalJVMFlagTask extends LocalTask {

	private static final Logger logger = LoggerFactory.getLogger(LocalJVMFlagTask.class);

	public static final String JVM_FLAGS_DISPLAY = "JVM flags";
	
	public static final String JVM_FLAGS_FILE = "jvm-flags.txt";
	private static final String JVM_FLAGS_TEMP_FILE = "jvm-flags-in-progress.tmp";
	
	public static final String SEPARATOR = "\t";
	
    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
	
	private JzrJVMFlagConfig config;
	
	private List<JVMFlag> jvmFlags = new ArrayList<>();
	
	public static class LocalJVMFlagThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("Jeyzer-recorder-jvm-flag-collector");
			t.setDaemon(true);
			return t;
		}
	}	
	
	public LocalJVMFlagTask(JzrJVMFlagConfig config, JzrSecurityManager securityMgr) {
		super(securityMgr);
		this.config = config;
	}

	@Override
	public void run() {
		try {
			collectJVMFlags();
			store(this.config.getSchedulerConfig().getOutputDirectory(), JVM_FLAGS_FILE, JVM_FLAGS_TEMP_FILE, JVM_FLAGS_DISPLAY);
			this.jvmFlags.clear();
		}catch(Exception ex) {
			logger.error("JVM flag collector failed to collect the modules : " + ex.getMessage(), ex);
		}
	}

	protected void storeData(BufferedWriter writer) throws IOException {
		Collections.sort(this.jvmFlags, new Comparator<JVMFlag>(){
			@Override
			public int compare(JVMFlag o1, JVMFlag o2) {
				return o1.getName().compareTo(o2.getName());
			}
			
		});
		for (JVMFlag flag : this.jvmFlags) {
			flag.print(writer);
		}
	}	

	private void collectJVMFlags() throws ClassNotFoundException, IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	// Must use reflection on Java 9+ as the com.sun.management is not public
        Class<?> hotspotDiagClass = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        
        // List<VMOption> hotspotMBean.getDiagnosticOptions()
		Object hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, hotspotDiagClass);
        Method m = hotspotDiagClass.getMethod("getDiagnosticOptions");
        Object obj = m.invoke(hotspotMBean);
        Class<?> listClass = Class.forName("java.util.List");
        Object flags = listClass.cast(obj);
        
        // int list.size()
        Method listSizeMethod = listClass.getMethod("size");
        Object objSize = listSizeMethod.invoke(flags);
        int size = (int)objSize;
        
		if (logger.isDebugEnabled())
			logger.debug("Number of JVM options to collect : " + size);
        
        for (int i=0; i<size; i++) {
        	// VMOption list.get(index)
            Method listGetMethod = listClass.getMethod("get", int.class);
            Object objOption = listGetMethod.invoke(flags, i);
            Class<?> optionClass = Class.forName("com.sun.management.VMOption");
            Object option = optionClass.cast(objOption);
            
            // option.getName
            Method optionGetNameMethod = optionClass.getMethod("getName");
            Object objName = optionGetNameMethod.invoke(option);
            String name = (String)objName;
                            
            // option.getValue
            Method optionGetValueMethod = optionClass.getMethod("getValue");
            Object objValue = optionGetValueMethod.invoke(option);
            String value = (String)objValue;
            
            // option.getOrigin
            Method optiongetOriginMethod = optionClass.getMethod("getOrigin");
            Object objOrigin = optiongetOriginMethod.invoke(option);
            Class<?> originClass = Class.forName("com.sun.management.VMOption$Origin");
            Object objOriginEnum = originClass.cast(objOrigin);
            
            Method toStringMethod = originClass.getMethod("toString");
            Object objOriginString = toStringMethod.invoke(objOriginEnum);
            String origin = (String)objOriginString;
            
        	JVMFlag flag = new JVMFlag(name, value, origin);
        	this.jvmFlags.add(flag);
        }
	}
	
	private static class JVMFlag {
		
		private String name;
		private String origin;
		private String value;
		
		public JVMFlag(String name, String value, String origin) {
			this.name = name;
			this.value = value;
			this.origin = origin;
		}
		
		public void print(BufferedWriter writer) throws IOException {
			String entry;
			
			entry = name;
			entry += SEPARATOR;
			entry += -1;
			entry += SEPARATOR;
			entry += value; 
			entry += SEPARATOR;
			entry += "";
			entry += SEPARATOR;
			entry += origin.substring(0, 1).toUpperCase() + origin.substring(1).toLowerCase();
			entry += SEPARATOR;
			entry += -1; // change date
			
			writer.write(entry + System.lineSeparator());
		}

		public String getName() {
			return name;
		}
	}
}
