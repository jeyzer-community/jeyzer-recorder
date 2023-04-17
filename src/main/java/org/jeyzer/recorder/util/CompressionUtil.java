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





import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class CompressionUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(CompressionUtil.class);	

	public static final String FILE_ZIP_EXTENSION = ".zip";	
	public static final String FILE_GZ_EXTENSION = ".tar.gz";
	
	private CompressionUtil(){
	}

	public static void zipFiles(File[] files, String zip){
		String tdPath = null;
		
		// Create a buffer for reading the files
		byte[] buf = new byte[1024];		

		try (
			    // Create the ZIP file
				FileOutputStream fos = new FileOutputStream(zip);
				ZipOutputStream out = new ZipOutputStream(fos);
			)
		{
		    for (File td : files){
		    	tdPath = td.getPath();
		    	
		    	try (
		    			// Add each file
		    			FileInputStream in = new FileInputStream(tdPath);
		    	)
				{
			    	if (logger.isDebugEnabled())
			    		logger.debug("Zipping file : " + tdPath);

			        // Keep last modified date
			        ZipEntry entry = new ZipEntry(td.getName());
			        File file = new File(tdPath);
			        entry.setTime(file.lastModified());
			        
			        // Add ZIP entry to output stream.
			        out.putNextEntry(entry);

			        // Transfer bytes from the file to the ZIP file
			        int len;
			        while ((len = in.read(buf)) > 0) {
			            out.write(buf, 0, len);
			        }

			        // Complete the entry
			        out.closeEntry();
				}
		    }
		} catch (IOException e) {
			logger.error("Failed to zip file " + zip + " while processing file " + tdPath, e);
			return;
		}
		
    	if (logger.isDebugEnabled())
    		logger.debug(zip + " file generated with " + files.length + " thread dumps");
	}
	
    public static void tarGzFiles(File[] files, String tarGz)
    {
		String tdPath = null;

        File outputTarGzFile = new File(tarGz);
        try {
			if (!outputTarGzFile.createNewFile()) {
				logger.error("Failed to create tar gzip file "+ tarGz);
				return;
			}
				
		} catch (IOException ex) {
			logger.error("Failed to create tar gzip file "+ tarGz, ex);
			return;
		}
		
    	try (
    			// Create the output stream for the output file
    	    	FileOutputStream fos = new FileOutputStream(outputTarGzFile);
    			
    	        // Wrap the output file stream in streams that will tar and gzip everything
    			BufferedOutputStream bos = new BufferedOutputStream(fos);
    			GZIPOutputStream gos = new GZIPOutputStream(bos);
    	    	TarArchiveOutputStream taos = new TarArchiveOutputStream(gos);
    		)
    	{
	        // TAR originally didn't support long file names, so enable the support for it
	        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
	 
	        // Get to putting all the files in the compressed output file
	        for (File f : files)
	        {
	        	tdPath = f.getParent(); 
	        	if (logger.isDebugEnabled())
	        		logger.debug("Tar file : " + tdPath);
	            addFilesToCompression(taos, f);
	        }
			
		} catch (Exception e) {
			logger.error("Failed to tar gzip file " + tarGz + " while procesing file " + tdPath);
			logger.error("Exception : ", e);
		}
        
    	if (logger.isDebugEnabled())
        	logger.debug("tarGz file generated with " + files.length + " thread dumps");
    }
 
    /**
     * Does the work of compression and going recursive for nested directories
     * <p/>
     *
     * Borrowed heavily from http://www.thoughtspark.org/node/53
     *
     * @param taos The archive
     * @param file The file to add to the archive
     * @throws IOException
     */
    private static void addFilesToCompression(TarArchiveOutputStream taos, File file)
                    throws IOException
    {
        // Create an entry for the file
        taos.putArchiveEntry(new TarArchiveEntry(file, file.getName()));
        if (file.isFile())
        {
        	try (
        			FileInputStream fis = new FileInputStream(file);
        	    	BufferedInputStream bis = new BufferedInputStream(fis);
        		)
        	{
                // Add the file to the archive
                IOUtils.copy(bis, taos);
                taos.closeArchiveEntry();
			} catch (Exception e) {
				logger.error("Failed to tar file " + file.getName());
				logger.error("Exception : ", e);
			}
        }
        else if (file.isDirectory())
        {
            // close the archive entry
            taos.closeArchiveEntry();
            // go through all the files in the directory and using recursion, add them to the archive
            for (File childFile : file.listFiles())
            {
                addFilesToCompression(taos, childFile);
            }
        }
    }
	
}
