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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class JzrTimeZone {

	// Origin possible values
	public enum Origin { CUSTOM, PROCESS, JZR };		
	
	public static final String PROPERTY_USER_TIMEZONE = "user.timezone";
	public static final Pattern TIME_ZONE_GMT_PATTERN = Pattern.compile("GMT(\\+|-)\\d\\d:\\d\\d");
	
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
		
		// If the user time zone provided through user.timezone is invalid, 
		//   the JVM will ignore it and use the local time expressed in GMT
		//   Example : GMT+02:00
		Matcher matcher = TIME_ZONE_GMT_PATTERN.matcher(candidate);
		if (matcher.matches())
			return true;
		
		List<String> ids = Arrays.asList(TimeZone.getAvailableIDs());
		
		if (!ids.contains(candidate)){
			logger.error("Invalid time zone id provided : " + candidate);
			return false;
		}
			
		return true;
	}
	
	public static String getFileTimeStamp(Date dateStamp, String format, TimeZone timeZone) {
		// Format must NOT contain any 'z' char
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(timeZone);
		String result = sdf.format(dateStamp);
		// Add the required time zone at the end
		result += timeZone.getID();
		// Handle time zone forbidden chars for files
		// ex : GMT+02:00, 
		return result.replace(':', '@').replace('/','$');
	}
}
