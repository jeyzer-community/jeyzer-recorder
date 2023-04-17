package org.jeyzer.recorder.config.mx.advanced;

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

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.jeyzer.recorder.util.ConfigUtil;
import org.threeten.bp.Duration;
import org.w3c.dom.Element;

public class JzrSchedulerConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(JzrSchedulerConfig.class);

	private static final String JZR_PERIOD = "period";
	private static final String JZR_START_OFFSET = "start_offset";

	private Duration period;
	private Duration offset;
	private String tdDir;
	private boolean active = false;
	
	public JzrSchedulerConfig() {
		// disabled
	}
	
	public JzrSchedulerConfig(Element schedulerNode, String tdDir, String name) throws JzrInitializationException {
		period = ConfigUtil.getAttributeDuration(schedulerNode, JZR_PERIOD);
		if (period.getSeconds() < 1) {
			logger.error("Configuration error - Invalid " + name + " " + JZR_PERIOD + " parameter : " + period + ". Value must be positive.");
			throw new JzrInitializationException("Configuration error - Invalid " + name + " " + JZR_PERIOD + " parameter : " + period + ". Value must be positive.");
		}
		
		offset = ConfigUtil.getAttributeDuration(schedulerNode, JZR_START_OFFSET);
		if (offset.getSeconds() < 1) {
			logger.error("Configuration error - Invalid " + name + " " + JZR_START_OFFSET + " parameter : " + offset + ". Value must be positive.");
			throw new JzrInitializationException("Configuration error - Invalid " + name + " " + JZR_START_OFFSET + " parameter : " + offset + ". Value must be positive.");
		}
		
		this.tdDir = tdDir;
		this.active = true;
	}
	
	public Duration getPeriod() {
		return period;
	}

	public Duration getStartOffset() {
		return offset;
	}
	
	public String getOutputDirectory() {
		return tdDir;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean value) {
		this.active = value;
	}
}
