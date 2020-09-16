package org.jeyzer.recorder.output;

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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

import javax.crypto.CipherOutputStream;

import org.jeyzer.recorder.accessor.mx.security.JzrSecurityManager;

public class JzrWriterFactory {
	
	private static final JzrWriterFactory factory = new JzrWriterFactory();
	
	private JzrWriterFactory(){
	}
	
	public static JzrWriterFactory newInstance(){
		return factory;
	}
	
	public BufferedWriter createWriter(File file) throws IOException{
		return createStandardWriter(file);
	}	

	public BufferedWriter createWriter(File file, JzrSecurityManager securityMgr) throws IOException{
		if (!securityMgr.isEncryptionEnabled())
			return createStandardWriter(file);
		else
			return createSecuredWriter(file, securityMgr);
	}

	private BufferedWriter createStandardWriter(File file) throws IOException {
		FileWriter fstream = new FileWriter(file, false); // do not append
		return new BufferedWriter(fstream);
	}

	private BufferedWriter createSecuredWriter(File file, JzrSecurityManager securityMgr) throws IOException {
        FileOutputStream ostream = null;
        CipherOutputStream cstream = null;
        GZIPOutputStream gzip = null;		
        
        try {
            // file
    		ostream = new FileOutputStream(file, false);
    		
            // encryption
            cstream = new CipherOutputStream(ostream, securityMgr.getEncryptionCipher());

            // compression
            gzip = new GZIPOutputStream(cstream);
		} catch (IOException ex) {
			try {
				if (cstream != null)
					cstream.close();
			} catch (IOException ex1) {}
			throw ex;
		}
        
        // writer
        OutputStreamWriter ouputWriter = new OutputStreamWriter(gzip);
        BufferedWriter bufferedWriter = new BufferedWriter(ouputWriter);
        JzrSecuredBufferedWriter securedWriter = new JzrSecuredBufferedWriter(bufferedWriter);
        
        return securedWriter;
	}
}
