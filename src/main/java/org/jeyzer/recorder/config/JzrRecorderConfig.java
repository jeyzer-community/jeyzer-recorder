package org.jeyzer.recorder.config;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Recorder
 * --
 * Copyright (C) 2020, 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */





import org.threeten.bp.Duration;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.jeyzer.recorder.util.SystemHelper;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.w3c.dom.Element;

public abstract class JzrRecorderConfig {

	private static final Logger logger = LoggerFactory.getLogger(JzrRecorderConfig.class);

	public static final String JZR_RECORDER = "recorder";
	public static final String JZR_PROCESS_CARD = "process_card";
	public static final String JZR_ENABLED = "enabled";
	public static final String JZR_TIME_ZONE = "time_zone";
	public static final String JZR_TIME_ZONE_ID = "id";
	public static final String JZR_SNAPSHOT = "snapshot";
	public static final String JZR_METHOD = "method";
	public static final String JZR_METHODS = "methods";
	public static final String JZR_NAME = "name";

	private static final String JZR_PROFILE = "profile";
	
	// thread dump parameters
	private static final String JZR_PERIOD = "period";
	private static final String JZR_START_DELAY = "start_delay";
	private static final String JZR_OUTPUT_DIR = "output_dir";
	private static final String JZR_CAPTURE_DURATION = "capture_duration";

	// archiving parameters
	private static final String JZR_ARCHIVING = "archiving";
	private static final String JZR_ARCHIVING_STORAGE = "storage";
	private static final String JZR_ARCHIVING_DIR = "archive_dir";
	private static final String JZR_ARCHIVING_PREFIX = "archive_prefix";
	private static final String JZR_ARCHIVING_ON_SHUTDOWN = "archive_on_shutdown";
	private static final String JZR_ARCHIVING_PERIOD = "period";
	private static final String JZR_ARCHIVING_RETENTION = "retention";
	private static final String JZR_ARCHIVING_RETENTION_FILE_LIMIT = "file_limit";
	private static final String JZR_ARCHIVING_TIME_OFFSET = "archiving_time_offset";
	private static final String JZR_ARCHIVING_TIME_OFFSET_DURATION = "duration";

	private String profile;	
	
	private Duration period;
	private Duration startDelay;
	private String tdDir;
	private boolean captureDurationEnabled;
	
	private boolean processCardEnabled;

	private String timeZoneId;
	
	// archiving parameters
	private Duration archiveZipPeriod;
	private int archiveZipLimit;
	private String archiveDir;
	private String archivePrefix;
	private Duration archiveZipTimeOffset;
	private boolean archiveOnShutdown;
	
	public String getProfile() {
		return profile;
	}
	
	public Duration getArchiveZipPeriod() {
		return archiveZipPeriod;
	}

	public int getArchiveZipLimit() {
		return archiveZipLimit;
	}

	public Duration getArchiveZipTimeOffset() {
		return archiveZipTimeOffset;
	}	
	
	public String getArchiveDir() {
		return archiveDir;
	}
	
	public String getArchivePrefix() {
		return archivePrefix;
	}
	
	public boolean isArchiveOnShutdown() {
		return archiveOnShutdown;
	}
	
	public Duration getPeriod() {
		return period;
	}
	
	public Duration getStartDelay() {
		return startDelay;
	}	

	public String getLogDirectory() {
		return tdDir;
	}

	public String getThreadDumpDirectory() {
		return tdDir;
	}

	public boolean isCaptureDurationEnabled() {
		return captureDurationEnabled;
	}

	public abstract boolean isEncryptionEnabled();	
	
	public abstract boolean isEncryptionKeyPublished();
	
	public boolean isProcessCardEnabled() {
		return processCardEnabled;
	}

	public String getTimeZoneId() {
		return timeZoneId;
	}
	
	public JzrRecorderConfig(Element recorder) throws JzrInitializationException {

		profile = ConfigUtil.loadStringValue(recorder, JZR_PROFILE); 

		// process card node
		Element processCardNode = ConfigUtil.getFirstChildNode(recorder, JZR_PROCESS_CARD);
		processCardEnabled = Boolean.valueOf(ConfigUtil.loadStringValue(processCardNode, JZR_ENABLED));
		
		// time zone
		Element timeZoneNode = ConfigUtil.getFirstChildNode(recorder, JZR_TIME_ZONE);
		timeZoneId = ConfigUtil.getAttributeValue(timeZoneNode, JZR_TIME_ZONE_ID);
		
		// snapshot node
		Element snapshotNode = ConfigUtil.getFirstChildNode(recorder, JZR_SNAPSHOT);
		
		period = ConfigUtil.getAttributeDuration(snapshotNode, JZR_PERIOD);

		if (period.getSeconds() < 1) {
			logger.error("Configuration error - Invalid " + JZR_PERIOD + " parameter : " + period + ". Value must be positive.");
			throw new JzrInitializationException("Configuration error - Invalid " + JZR_PERIOD + " parameter : " + period + ". Value must be positive.");
		}
		
		startDelay = ConfigUtil.getAttributeDuration(snapshotNode, JZR_START_DELAY);
		
		tdDir = ConfigUtil.loadStringValue(snapshotNode, JZR_OUTPUT_DIR);
		tdDir = SystemHelper.sanitizePathSeparators(tdDir);
		
		// capture thread dump generation time
		try{
			captureDurationEnabled = Boolean.valueOf(ConfigUtil.loadStringValue(snapshotNode, JZR_CAPTURE_DURATION));
		}catch(Exception ex){
			logger.warn("Configuration error - Invalid or missing " + JZR_CAPTURE_DURATION + " parameter : " + ConfigUtil.loadStringValue(snapshotNode, JZR_CAPTURE_DURATION) +  ". ");
			captureDurationEnabled = false;
		}
		
		// archiving node
		Element archivingNode = ConfigUtil.getFirstChildNode(recorder, JZR_ARCHIVING);
		
		Element storageNode = ConfigUtil.getFirstChildNode(recorder, JZR_ARCHIVING_STORAGE);
		archiveDir = ConfigUtil.loadStringValue(storageNode, JZR_ARCHIVING_DIR);
		archiveDir = SystemHelper.sanitizePathSeparators(archiveDir);

		archivePrefix = ConfigUtil.loadStringValue(storageNode, JZR_ARCHIVING_PREFIX);
		
		// capture thread dump generation time
		try{
			archiveOnShutdown = Boolean.valueOf(ConfigUtil.loadStringValue(archivingNode, JZR_ARCHIVING_ON_SHUTDOWN));
		}catch(Exception ex){
			logger.warn("Configuration error - Invalid or missing " + JZR_ARCHIVING_ON_SHUTDOWN + " parameter : " + ConfigUtil.loadStringValue(archivingNode, JZR_ARCHIVING_ON_SHUTDOWN) +  ".");
			archiveOnShutdown = false;
		}
		
		archiveZipPeriod = ConfigUtil.getAttributeDuration(archivingNode, JZR_ARCHIVING_PERIOD); 
		
		if (archiveZipPeriod.getSeconds() < 10) {
			logger.error("Configuration error - Invalid " + JZR_ARCHIVING_PERIOD + " parameter : " + archiveZipPeriod + ". Value must be greater than 10.");
			throw new JzrInitializationException("Configuration error - Invalid " + JZR_ARCHIVING_PERIOD + " archiving parameter : " + archiveZipPeriod + ". Value must be greater than 10.");
		}

		// retention node
		Element retentionNode = ConfigUtil.getFirstChildNode(archivingNode, JZR_ARCHIVING_RETENTION);

		archiveZipLimit = ConfigUtil.loadIntegerValue(retentionNode, JZR_ARCHIVING_RETENTION_FILE_LIMIT);
		
		if (archiveZipLimit < 1) {
			logger.error("Configuration error - Invalid " + JZR_ARCHIVING_RETENTION_FILE_LIMIT + " parameter : " + archiveZipLimit + ". Value must be greater than 1.");
			throw new JzrInitializationException("Configuration error - Invalid " + JZR_ARCHIVING_RETENTION_FILE_LIMIT + " archiving parameter : " + archiveZipLimit + ". Value must be greater than 1.");
		}

		// offset node
		Element offsetNode = ConfigUtil.getFirstChildNode(archivingNode, JZR_ARCHIVING_TIME_OFFSET);
		
		archiveZipTimeOffset = ConfigUtil.getAttributeDuration(offsetNode, JZR_ARCHIVING_TIME_OFFSET_DURATION);

		if (archiveZipTimeOffset.getSeconds() < 10) {
			logger.error("Configuration error - Invalid " + JZR_ARCHIVING_TIME_OFFSET_DURATION + " parameter : " + archiveZipTimeOffset + ". Value must be greater than 10.");
			throw new JzrInitializationException("Configuration error - Invalid " + JZR_ARCHIVING_TIME_OFFSET_DURATION + " archiving parameter : " + archiveZipTimeOffset + ". Value must be greater than 10.");
		}
	}

	public String toString() {
		StringBuilder b = new StringBuilder();

		b.append("Configuration :\n");
		b.append("\tProfile                   : " + profile + "\n");
		b.append("\tStorage directory         : " + tdDir + "\n");
		b.append("\tThread dump period     	  : " + period.getSeconds() + " sec\n");
		b.append("\tThread dump start delay	  : " + startDelay.getSeconds() + " sec\n");
		b.append("\tDuration capture enabled  : " + captureDurationEnabled +"\n");
		b.append("\tProcess card enabled      : " + processCardEnabled +"\n");
		b.append("\tEncryption enabled        : " + isEncryptionEnabled() +"\n");
		if (isEncryptionEnabled())
			b.append("\tEncryption key exported   : " + isEncryptionKeyPublished() +"\n");
		b.append("\tArchiving period          : " + archiveZipPeriod.toHours() + " hours\n");
		b.append("\tMax zip files             : " + archiveZipLimit + "\n");
		b.append("\tZip archive directory     : " + archiveDir + "\n");
		b.append("\tZip time offset           : " + archiveZipTimeOffset.getSeconds() + " sec\n");

		return b.toString();
	}
}
