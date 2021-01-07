package org.jeyzer.recorder.util;

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



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemHelper {

	private static final Logger logger = LoggerFactory.getLogger(SystemHelper.class);

	public static final String PROPERTY_OS_NAME = "os.name";
	public static final String PROPERTY_JAVA_HOME_NAME = "java.home";
	public static final String PROPERTY_JAVA_RUNTIME_VERSION = "java.runtime.version";
	
	public static final String PLATFORM = System.getProperty(PROPERTY_OS_NAME).toLowerCase();
	public static final String JAVA_HOME = System.getProperty(PROPERTY_JAVA_HOME_NAME).toLowerCase();
	public static final String JAVA_VERSION = System.getProperty(PROPERTY_JAVA_RUNTIME_VERSION).toLowerCase();
	
	private SystemHelper(){
	}
	
	public static void displayMemoryUsage(){
        int mb = 1024*1024;
        
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
         
        logger.debug("Heap utilization statistics :");
         
        //Print used memory
        logger.debug(" - Used Memory  : {} Mb", (runtime.totalMemory() - runtime.freeMemory()) / mb);
 
        //Print free memory
        logger.debug(" - Free Memory  : {} Mb", runtime.freeMemory() / mb);
         
        //Print total available memory
        logger.debug(" - Total Memory : {} Mb", runtime.totalMemory() / mb);
 
        //Print Maximum available memory
        logger.debug(" - Max Memory   : {} Mb", runtime.maxMemory() / mb);
	}
	
	public static boolean isWindows() {
		return (PLATFORM.indexOf("win") >= 0);
	}
 
	public static boolean isMac() {
		return (PLATFORM.indexOf("mac") >= 0);
	}
 
	public static boolean isUnix() {
		return (PLATFORM.indexOf("nix") >= 0 || PLATFORM.indexOf("nux") >= 0 || PLATFORM.indexOf("aix") >= 0 );
	}
 
	public static boolean isSolaris() {
		return (PLATFORM.indexOf("sunos") >= 0);
	}
	
	public static boolean isAtLeastJdK9() {
		return !JAVA_VERSION.startsWith("1."); // Stands for 1.8, 1.7. From Java 9, "1." is not used anymore.
	}
	
	public static String sanitizePathSeparators(String path){
		if (isWindows()){
			return path.replace('/', '\\');
		}
		else{
			return path.replace('\\', '/');
		}
	}
	
	public static boolean isRootCause(@SuppressWarnings("rawtypes") Class exceptionClass, Exception e){
		Throwable cause = e.getCause();
		
		while(cause != null){
			if (cause.getClass().isAssignableFrom(exceptionClass))
				return true;
			cause = cause.getCause(); 
		}
		
		return false;
	}
}
