package org.jeyzer.recorder;

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


import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jeyzer.recorder.accessor.local.advanced.process.jar.LocalJarPathTask;
import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;
import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.config.local.advanced.JzrAdvancedMXAgentConfig;
import org.jeyzer.recorder.util.CompressionUtil;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.util.JzrTimeZone;
import org.jeyzer.recorder.util.SystemHelper;
import org.jeyzer.recorder.util.FileUtil.ThreadDumpFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Runnable for the scheduler
// Thread for the shutdown hook
public class JzrArchiverTask extends Thread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(JzrArchiverTask.class);	
	
	public static class ArchiverThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("Jeyzer-recorder-archiver");
			t.setDaemon(true);
			return t;
		}
	}
	
	private static final class ShutdownHook extends Thread {
		
		JzrArchiverTask archiver;
		
		public ShutdownHook(JzrArchiverTask archiver) {
			this.archiver = archiver;
		}
		
		@Override
		public void run() {
			this.archiver.archive(false);
		}
	}
	
	private long zipRetentionLimit;
	private long zipTimeOffset;
	private File tdDir;
	private File archiveDir;
	private String archivePrefix;
	
	private boolean processCardSupported;
	private String processCardPath;
	
	private boolean encryptionPublished;
	private String encryptionKeyFilePath;

	private boolean processJarPathsSupported;
	private String processJarPath;
	
	private boolean processModulesSupported;
	private String processModulesPath;
	
	long startZipTime;
	
	public JzrArchiverTask(JzrRecorderConfig cfg) {
		long taskPeriod = cfg.getArchiveZipPeriod().getSeconds();
		if (cfg.getPeriod().getSeconds() > taskPeriod){
			logger.warn("Zip period ({} sec) lower than recording snapshot period ({} sec)."
					+ "Defaulting to 10x recording snapshot period.", taskPeriod, cfg.getPeriod().getSeconds());
			taskPeriod = cfg.getArchiveZipPeriod().getSeconds() * 10L; 
		}
		
		zipRetentionLimit = cfg.getArchiveZipLimit();
		tdDir = new File(cfg.getThreadDumpDirectory());
		zipTimeOffset = cfg.getArchiveZipTimeOffset().getSeconds();
		
		archiveDir = new File(cfg.getArchiveDir());
		if (!archiveDir.exists() && !archiveDir.mkdirs()){
			logger.warn("Failed to create archive directory {}. "
					+ "Defaulting to recording snapshot directory.", archiveDir);
			archiveDir = tdDir; 
		}
		
		archivePrefix = cfg.getArchivePrefix();
		
		// optional process card
		processCardSupported = cfg.isProcessCardEnabled();
		processCardPath = cfg.getThreadDumpDirectory() + File.separator + JeyzerRecorder.PROCESS_CARD_FILE_NAME;
		
		// optional security key
		encryptionPublished = cfg.isEncryptionKeyPublished();
		encryptionKeyFilePath = cfg.getThreadDumpDirectory() + File.separator + JzrSecurityManager.PUBLISHED_ENCRYPTED_AES_KEY_FILE;
		
		// optional jar paths file
		processJarPathsSupported = cfg instanceof JzrAdvancedMXAgentConfig; // not sufficient, will check later the file existence. Should get it from the advanced configuration, but not accessible here
		processJarPath = cfg.getThreadDumpDirectory() + File.separator + LocalJarPathTask.JAR_PATHS_FILE;
		
		// optional modules file
		processModulesSupported = cfg instanceof JzrAdvancedMXAgentConfig; // not sufficient, will check later the file existence. Should get it from the advanced configuration, but not accessible here
		processModulesPath = cfg.getThreadDumpDirectory() + File.separator + LocalJarPathTask.JAR_PATHS_FILE;
		
		// initialize start time	
		startZipTime = Calendar.getInstance().getTimeInMillis() 
				- taskPeriod * 1000L 
				- zipTimeOffset * 1000L;
		
		if (cfg.isArchiveOnShutdown()) {
			Thread t = new ShutdownHook(this);
			t.setName("Jeyzer-recorder-archiver-shutdown");
			Runtime.getRuntime().addShutdownHook(t);			
		}

	}

	@Override
	public void run() {
		archive(true);
	}
	
	private void archive(boolean useOffset) {
		//manage possible competition between scheduler and shutdown hook
		Lock lock = new ReentrantLock();
		
		if (!lock.tryLock())
			return; // someone already archiving
		
		try {
			// archive recording snapshots
			archiveTDs(useOffset);
			
			// clean oldest zip file
			cleanOldArchives();
		}
		finally {
			lock.unlock();
		}
	}

	private void archiveTDs(boolean useOffset){
		long endZipTime = Calendar.getInstance().getTimeInMillis();
		if (useOffset)
			endZipTime -= zipTimeOffset * 1000L;
		
		ThreadDumpFileFilter filefilter = new ThreadDumpFileFilter(
				FileUtil.JZR_FILE_JZR_PREFIX, FileUtil.JZR_FILE_JZR_EXTENSION, endZipTime);
		File[] tds = this.tdDir.listFiles(filefilter);
		
		if (tds.length == 0)
			return; // required in case shutdown hook called after start
		
		java.util.Arrays.sort(tds, new java.util.Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return Long.valueOf(f1.lastModified()).compareTo(
						f2.lastModified());
			}
		});

		// Manage case where old files were left away from previous run
		if (tds.length >0 && tds[0].lastModified() < startZipTime)
			startZipTime = tds[0].lastModified();

		String archiveFile = archiveDir + File.separator + getPeriodStampedFileName(startZipTime, endZipTime); 
		
		File[] files = addFile(tds, processCardPath, processCardSupported);
		
		File[] files2 = addFile(files, processJarPath, processJarPathsSupported);
		File[] files3 = addFile(files2, processModulesPath, processModulesSupported);
		File[] filesToZip = addFile(files3, encryptionKeyFilePath, encryptionPublished);
		
		if (SystemHelper.isWindows())
			// zip the files
			CompressionUtil.zipFiles(filesToZip, archiveFile);
		else if (SystemHelper.isUnix() || SystemHelper.isSolaris())
			CompressionUtil.tarGzFiles(filesToZip, archiveFile);
		
		startZipTime = endZipTime;
		
		// delete the recording snapshot files 
		for (File td : tds){
			if (!td.delete())
				logger.warn("Failed to delete recording snapshot file {}", td.getAbsolutePath());
		}
	}

	private File[] addFile(File[] files, String path, boolean condition) {
		if (condition){
			File fileToAdd = new File(path);
			if (!fileToAdd.exists() || !fileToAdd.isFile())
				return files;  // test added for the jarpath file
			
			File[] newFiles = new File[files.length+1];
	    	System.arraycopy(files, 0, newFiles, 0, files.length);
	    	newFiles[newFiles.length-1] = fileToAdd;
			return newFiles;
	    }else{
	    	return files;
	    }
	}

	private void cleanOldArchives() {
		String ext = SystemHelper.isWindows() ?  CompressionUtil.FILE_ZIP_EXTENSION : CompressionUtil.FILE_GZ_EXTENSION;
		
		ThreadDumpFileFilter filefilter = new ThreadDumpFileFilter(
				archivePrefix, ext, Calendar.getInstance().getTimeInMillis());
		File[] files = this.archiveDir.listFiles(filefilter);

		java.util.Arrays.sort(files, new java.util.Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return Long.valueOf(f1.lastModified()).compareTo(
						f2.lastModified());
			}
		});

		for (int i=0 ; i< files.length-this.zipRetentionLimit; i++){
			if (!files[i].delete())
				logger.warn("Failed to delete archive file {}", files[i]);
		}
	}

	private String getPeriodStampedFileName(long start, long end) {
		String ext = SystemHelper.isWindows() ?  CompressionUtil.FILE_ZIP_EXTENSION : CompressionUtil.FILE_GZ_EXTENSION;
		
		StringBuilder name = new StringBuilder(archivePrefix);
		name.append(FileUtil.getTimeZoneOriginFileMarker());
		name.append(JzrTimeZone.getTimeStamp(new Date(start), FileUtil.JZR_FILE_DATE_FORMAT, JzrTimeZone.getTimeZone()));
		name.append("---");
		name.append(JzrTimeZone.getTimeStamp(new Date(end), FileUtil.JZR_FILE_DATE_FORMAT, JzrTimeZone.getTimeZone()));
		name.append(ext);
		return name.toString();
	}
}
