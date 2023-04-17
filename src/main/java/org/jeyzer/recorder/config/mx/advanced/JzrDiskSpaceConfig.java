package org.jeyzer.recorder.config.mx.advanced;

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



import java.io.File;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.w3c.dom.Element;

public class JzrDiskSpaceConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(JzrDiskSpaceConfig.class);	
	
	protected static final String JZR_DISK_SPACE = "disk_space";
	
	private static final String JZR_NAME = "name";
	private static final String JZR_DIRECTORY = "directory";
	
	private static final String FREE_SPACE_FIELD = "free_space";
	private static final String USED_SPACE_FIELD = "used_space";
	private static final String TOTAL_SPACE_FIELD = "total_space";
	
	private String name;
	private File directory;
	private Boolean usedSpace;
	private Boolean freeSpace;
	private Boolean totalSpace;
	
	public JzrDiskSpaceConfig(Element node) throws JzrInitializationException {
		name = ConfigUtil.loadStringValue(node, JZR_NAME);
		directory = new File(ConfigUtil.loadStringValue(node, JZR_DIRECTORY));
		loadDiskChecks(node);
	}

	private void loadDiskChecks(Element node) throws JzrInitializationException {
		usedSpace = ConfigUtil.getFirstChildNode(node, USED_SPACE_FIELD) != null ? true :null;
		freeSpace = ConfigUtil.getFirstChildNode(node, FREE_SPACE_FIELD) != null ? true :null;
		totalSpace = ConfigUtil.getFirstChildNode(node, TOTAL_SPACE_FIELD) != null ? true :null;
		
		if (usedSpace == null && freeSpace == null && totalSpace == null){
			String message = "Configuration error - node " + JZR_DISK_SPACE + " must contain at least one of these nodes : " + FREE_SPACE_FIELD + ", "+ USED_SPACE_FIELD + ", "+ TOTAL_SPACE_FIELD;
			logger.error(message);
			throw new JzrInitializationException(message);
		}
	}

	public String getName() {
		return name;
	}
	
	public File getDirectory() {
		return directory;
	}
	
	public boolean isUsedSpaceCollected() {
		return usedSpace != null? usedSpace : false ;
	}
	
	public boolean isFreeSpaceCollected() {
		return freeSpace != null? freeSpace : false ;
	}

	public boolean isTotalSpaceCollected() {
		return totalSpace != null? totalSpace : false ;
	}
}
