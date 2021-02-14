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





import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class JzrTimeZone {

	// Origin possible values
	public enum Origin { CUSTOM, PROCESS, JZR };		
	
	public static final String PROPERTY_USER_TIMEZONE = "user.timezone";	
	
	private static final Logger logger = LoggerFactory.getLogger(JzrTimeZone.class);
	
	// not thread safe. Initialized once by JeyzerRecorder
	private static TimeZone timeZone = TimeZone.getDefault();
	private static Origin origin = Origin.JZR;

	public static void setTimeZone(TimeZone timeZone, Origin origin) {
		JzrTimeZone.timeZone = timeZone;
		JzrTimeZone.origin = origin;
	}
	
	public static TimeZone getTimeZone() {
		return timeZone;
	}

	public static Origin getOrigin() {
		return origin;
	}

	public static boolean isValidTimeZone(String candidate) {
		if (candidate == null || candidate.isEmpty())
			return false;
		
		List<String> ids = Arrays.asList(TimeZone.getAvailableIDs());
		
		if (!ids.contains(candidate)){
			logger.error("Invalid time zone id provided : " + candidate);
			return false;
		}
			
		return true;
	}
	
	public static String getTimeStamp(Date dateStamp, String format, TimeZone timeZone) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(timeZone);
		return sdf.format(dateStamp);
	}	
	
}
