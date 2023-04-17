package org.jeyzer.recorder.accessor.local.advanced.system;

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




import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.config.mx.advanced.JzrDiskSpaceConfig;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalDiskSpaceAccessor {

	protected static final Logger logger = LoggerFactory.getLogger(LocalDiskSpaceAccessor.class);

	private static final String DISK_SPACE_FIELD_PREFIX = FileUtil.JZR_FIELD_JZ_PREFIX + "disk space:";
	private static final String DIRECTORY_FIELD = ":directory" + FileUtil.JZR_FIELD_EQUALS;
	private static final String FREE_SPACE_FIELD = ":free_space" + FileUtil.JZR_FIELD_EQUALS;
	private static final String USED_SPACE_FIELD = ":used_space" + FileUtil.JZR_FIELD_EQUALS;
	private static final String TOTAL_SPACE_FIELD = ":total_space" + FileUtil.JZR_FIELD_EQUALS;
	
	private static final long DIRECTORY_VALID = 0;
	private static final long DIRECTORY_IS_FILE = -2;
	private static final long DIRECTORY_NOT_EXIST = -3;
	private static final long DIRECTORY_ACCESS_DENIED = -4;
	
	private static final long CHECK_NOT_REQUESTED = -1;
	
	protected List<JzrDiskSpaceConfig> diskSpaceConfigs;
	
	protected List<DiskSpace> diskSpaces = new ArrayList<>(9);
	
	public LocalDiskSpaceAccessor(List<JzrDiskSpaceConfig> list) {
		this.diskSpaceConfigs = list;
	}
	
	public void collect() {
		for(JzrDiskSpaceConfig config : this.diskSpaceConfigs){
			File dir = config.getDirectory();
			
	        if (logger.isDebugEnabled())
	        	logger.debug("Accessing disk space info for directory : " + dir.getAbsolutePath());
			
			DiskSpace diskSpace = new DiskSpace(config);
			diskSpaces.add(diskSpace);
			
			long result = validateDirectory(dir, diskSpace);	
			if (result != DIRECTORY_VALID)
				continue;

			// directory valid, let's get the values
			if (config.isFreeSpaceCollected()){
				try{
					diskSpace.setFreeSpace(dir.getUsableSpace());
				}
				catch(SecurityException ex){
					diskSpace.setFreeSpace(DIRECTORY_ACCESS_DENIED);
			        if (logger.isDebugEnabled())
			        	logger.debug("Accessing disk space info for directory : " + dir.getAbsolutePath() + " failed. Error is : " + ex.getMessage());
				}
			}

			if (config.isTotalSpaceCollected()){
				try{
					diskSpace.setTotalSpace(dir.getTotalSpace());
				}
				catch(SecurityException ex){
					diskSpace.setTotalSpace(DIRECTORY_ACCESS_DENIED);
				}
			}

			if (config.isUsedSpaceCollected()){
				// just total minus free space operation
				if (diskSpace.isFreeSpaceNotReachable())
					diskSpace.setUsedSpace(diskSpace.getFreeSpace()); // copy the error code
				else if (diskSpace.isTotalSpaceNotReachable())
					diskSpace.setUsedSpace(diskSpace.getTotalSpace()); // copy the error code
				else {
					long free = diskSpace.isFreeSpaceRequested() ?  diskSpace.getFreeSpace() : dir.getUsableSpace();
					long total = diskSpace.isTotalSpaceRequested() ?  diskSpace.getTotalSpace() : dir.getTotalSpace();
					diskSpace.setUsedSpace(total - free);
				}
			}
		}
	}
	
	public void printDiskSpaceInfo(BufferedWriter out) throws IOException {
		for (DiskSpace diskSpace : diskSpaces){
			
			StringBuilder line = buildHeader(diskSpace);
			line.append(DIRECTORY_FIELD);
			line.append(diskSpace.getConfig().getDirectory());
    		writeln(line.toString(), out);
			
    		if (diskSpace.getConfig().isFreeSpaceCollected()){
    			line = buildHeader(diskSpace);
    			line.append(FREE_SPACE_FIELD);
    			line.append(diskSpace.getFreeSpace());
        		writeln(line.toString(), out);    			
    		}

    		if (diskSpace.getConfig().isUsedSpaceCollected()){
    			line = buildHeader(diskSpace);
    			line.append(USED_SPACE_FIELD);
    			line.append(diskSpace.getUsedSpace());
        		writeln(line.toString(), out);    			
    		}
    		
    		if (diskSpace.getConfig().isTotalSpaceCollected()){
    			line = buildHeader(diskSpace);
    			line.append(TOTAL_SPACE_FIELD);
    			line.append(diskSpace.getTotalSpace());
        		writeln(line.toString(), out);    			
    		}
		}
	}
	
	public void close() {
		this.diskSpaces.clear();
	}

	private long validateDirectory(File directory, DiskSpace diskSpace) {
		if (!directory.exists()){
			diskSpace.invalidate(DIRECTORY_NOT_EXIST);
	        if (logger.isDebugEnabled())
	        	logger.debug("Accessing disk space info for directory : " + directory.getAbsolutePath() + " failed. Directory doesn't exist.");
			return DIRECTORY_NOT_EXIST;
		}

		if (directory.isFile()){
			diskSpace.invalidate(DIRECTORY_IS_FILE);
	        if (logger.isDebugEnabled())
	        	logger.debug("Accessing disk space info for directory : " + directory.getAbsolutePath() + " failed. Directory is a file.");
			return DIRECTORY_IS_FILE;
		}

		return DIRECTORY_VALID;
	}
	
	private StringBuilder buildHeader(DiskSpace diskSpace) {
		StringBuilder line = new StringBuilder();
		line.append(DISK_SPACE_FIELD_PREFIX);
		line.append(diskSpace.getConfig().getName());
		return line;
	}

	private void writeln(String info, BufferedWriter out) throws IOException {
		try {
			out.write(info);
			out.newLine();
		} catch (IOException e) {
			logger.error("Failed to write disk space info in thread dump file.");
			throw e;
		}
	}

	private final class DiskSpace{
		
		private JzrDiskSpaceConfig config;
		private long usedSpace = CHECK_NOT_REQUESTED;
		private long totalSpace = CHECK_NOT_REQUESTED;
		private long freeSpace = CHECK_NOT_REQUESTED;
		
		public DiskSpace(JzrDiskSpaceConfig config){
			this.config = config;
		}

		public boolean isFreeSpaceRequested() {
			return freeSpace != CHECK_NOT_REQUESTED;
		}
		
		public boolean isTotalSpaceRequested() {
			return totalSpace != CHECK_NOT_REQUESTED;
		}
		
		public boolean isFreeSpaceNotReachable() {
			return freeSpace == DIRECTORY_IS_FILE 
				|| freeSpace == DIRECTORY_NOT_EXIST 
				|| freeSpace == DIRECTORY_ACCESS_DENIED;
		}
		
		public boolean isTotalSpaceNotReachable() {
			return totalSpace == DIRECTORY_IS_FILE 
				|| totalSpace == DIRECTORY_NOT_EXIST 
				|| totalSpace == DIRECTORY_ACCESS_DENIED;
		}

		public void invalidate(long code) {
			this.usedSpace = code;
			this.totalSpace = code;
			this.freeSpace = code;
		}

		public JzrDiskSpaceConfig getConfig() {
			return config;
		}

		public long getUsedSpace() {
			return usedSpace;
		}

		public long getTotalSpace() {
			return totalSpace;
		}

		public long getFreeSpace() {
			return freeSpace;
		}

		public void setUsedSpace(long usedSpace) {
			this.usedSpace = usedSpace;
		}

		public void setTotalSpace(long totalSpace) {
			this.totalSpace = totalSpace;
		}

		public void setFreeSpace(long freeSpace) {
			this.freeSpace = freeSpace;
		}
	}
}
