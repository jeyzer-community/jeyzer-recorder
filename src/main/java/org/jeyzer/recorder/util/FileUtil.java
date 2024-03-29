package org.jeyzer.recorder.util;

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





import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import static org.jeyzer.recorder.util.JzrTimeZone.Origin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;

import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	// The 'z' at the end has been removed as it does not put official time zone ids. 
	//   Example : "US/Pacific" ends up as "PDT" which is not standard.
	public static final String JZR_FILE_DATE_FORMAT = "yyyy-MM-dd---HH-mm-ss-SSS-"; 
	public static final String JZR_FILE_JZR_PREFIX = "snap-";
	public static final String JZR_FILE_JZR_EXTENSION = ".jzr";
	public static final String JZR_FILE_JZR_VT_EXTENSION = ".jzr.vt";
	
	public static final String TEMP_FILE_NAME = "jzr-snap-in-progress.tmp";
	
	public static final String JZR_FILE_TIME_ZONE_CUSTOM_ORIGIN = "c-";
	public static final String JZR_FILE_TIME_ZONE_PROCESS_ORIGIN = "p-";
	public static final String JZR_FILE_TIME_ZONE_JZR_ORIGIN = "jzr-";
	
	public static final String JZR_FIELD_JZ_PREFIX = "\tJz>\t";
	public static final String JZR_FIELD_JZ_JZ_PREFIX = "\tJz>\tJz ";
	public static final String JZR_FIELD_EQUALS = "\t";
	public static final char   JZR_FIELD_SEPARATOR = ':';
	
	public static final String JZR_FIELD_CAPTURE_DURATION = JZR_FIELD_JZ_PREFIX + "capture time" + JZR_FIELD_EQUALS;
	
	private FileUtil(){
	}
	
	public static class ThreadDumpFileFilter implements FilenameFilter {
		String ext;
		String prefix;
		long maxUpperTime;

		public ThreadDumpFileFilter(String prefix, String ext, long maxUpperTime) {
			this.prefix = prefix;
			this.ext = ext;
			this.maxUpperTime = maxUpperTime;
		}

		@Override
		public boolean accept(File dir, String name) {
			File file = new File(dir.getPath() + File.separator + name);
			return name.startsWith(prefix) 
					&& name.endsWith(ext) 
					&& file.lastModified() < maxUpperTime;
		}
	}
	
	public static String getTimeZoneOriginFileMarker() {
		if (Origin.CUSTOM.equals(JzrTimeZone.getOrigin()))
			return JZR_FILE_TIME_ZONE_CUSTOM_ORIGIN;
		else if (Origin.PROCESS.equals(JzrTimeZone.getOrigin()))
			return JZR_FILE_TIME_ZONE_PROCESS_ORIGIN;
		else
			return JZR_FILE_TIME_ZONE_JZR_ORIGIN;
	}
	
	public static String getTimeStampedFileName(String prefix, Date dateStamp, String extension) {
		// format is thread-dump-<P or JZR>-<yyyy-MM-dd---HH-mm-ss-SSS-z>.txt
		StringBuilder name = new StringBuilder(prefix);
		name.append(getTimeZoneOriginFileMarker());
		name.append(JzrTimeZone.getFileTimeStamp(dateStamp, JZR_FILE_DATE_FORMAT, JzrTimeZone.getTimeZone()));
		name.append(extension);
		return name.toString();
	}
	
	public static String getTimeDurationField(long duration) {
		return JZR_FIELD_CAPTURE_DURATION + duration; 
	}
	
	public static void closeWriter(File file, Writer writer) {
		if (writer == null)
			return;
		
        try {        
        	writer.flush();
		} catch (IOException e) {
			logger.warn("Failed to flush file " + file.getAbsolutePath());	
		}
        
        try {
			writer.close();
		} catch (IOException e) {
			logger.warn("Failed to close file " + file.getAbsolutePath());	
		}
	}

	public static void emptyFile(File file) {
		PrintWriter writer  = null; 
		
		try {
			writer = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			logger.warn("File not found " + file.getAbsolutePath());
			return;
		}
		
		writer.print("");
		
		closeWriter(file, writer);
	}	
	
	public static byte[] read(File file) throws IOException{
	    byte[] buffer = new byte[(int) file.length()];
	    try (
	    	    InputStream ios = new FileInputStream(file);
	    	)
	    {
	        if (ios.read(buffer) == -1) {
	            throw new IOException("EOF reached while trying to read the whole file");
	        }
	    }
	    return buffer;
	}
}
