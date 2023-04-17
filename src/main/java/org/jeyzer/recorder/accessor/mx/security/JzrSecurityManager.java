package org.jeyzer.recorder.accessor.mx.security;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.config.security.JzrSecurityConfig;

public class JzrSecurityManager {
	
	public static final String PUBLISHED_ENCRYPTED_AES_KEY_FILE = "jzr-recording.key";
	
	private boolean encryptionEnabled;
	private Cipher encryptionCipher;
	
	public JzrSecurityManager(JzrSecurityConfig cfg, String outputDir) throws JzrInitializationException{
		encryptionEnabled = cfg.isEncryptionEnabled();
		if (!encryptionEnabled)
			return;
		
    	try {
			encryptionCipher = Cipher.getInstance(JzrSecurityConfig.ENCRYPTION_ALGORITHM);
		} catch (GeneralSecurityException ex) {
			throw new JzrInitializationException("Failed to instanciate the security manager : encryption algorithm " + JzrSecurityConfig.ENCRYPTION_ALGORITHM + " not recognized. " + ex.getMessage(), ex);
		}
    	
    	try {
			encryptionCipher.init(Cipher.ENCRYPT_MODE, cfg.getEncryptionKey());
		} catch (InvalidKeyException ex) {
			throw new JzrInitializationException("Failed to instanciate the security manager : encryption key is invalid. " + ex.getMessage(), ex);
		}
    	
    	if (cfg.isEncryptionKeyPublished()){
    		String encryptedAESKey = encryptAESKey(cfg);
    		writeKey(encryptedAESKey, outputDir);
    	}

	}

	private String encryptAESKey(JzrSecurityConfig cfg) throws JzrInitializationException {
        Cipher masterCipher;
		try {
			masterCipher = Cipher.getInstance(JzrSecurityConfig.RSA_OAEP_ALGORITHM);
		} catch (GeneralSecurityException ex) {
			throw new JzrInitializationException("Failed to instanciate the security manager : master algorithm " + JzrSecurityConfig.RSA_OAEP_ALGORITHM + " not recognized. " + ex.getMessage(), ex);
		}
		
        KeySpec spec = new X509EncodedKeySpec(cfg.getMasterPublicKey());
        PublicKey publicKey;
		try {
			publicKey = KeyFactory.getInstance(JzrSecurityConfig.KEY_TYPE).generatePublic(spec);
		} catch (NoSuchAlgorithmException ex) {
			throw new JzrInitializationException("Failed to instanciate the security manager : master key type " + JzrSecurityConfig.KEY_TYPE + " not recognized.", ex);
		} catch (InvalidKeySpecException ex) {
			throw new JzrInitializationException("Failed to instanciate the security manager : master key spec is invalid." + ex.getMessage(), ex);
		}
		
        try {
			masterCipher.init(Cipher.ENCRYPT_MODE, publicKey);
		} catch (InvalidKeyException ex) {
			throw new JzrInitializationException("Failed to instanciate the security manager : master key file is invalid.", ex);
		}

    	byte[] encodedSecKey;
		try {
			encodedSecKey = masterCipher.doFinal(cfg.getEncryptionKey().getEncoded());
		} catch (IllegalBlockSizeException ex) {
			throw new JzrInitializationException("Failed to instanciate the security manager : AES key encryption failed.", ex);
		} catch (BadPaddingException ex) {
			throw new JzrInitializationException("Failed to instanciate the security manager : AES key encryption failed.", ex);
		}
        
		return new String(Base64.getEncoder().encode(encodedSecKey));
	}

	private void writeKey(String encryptedAESKey, String outputDir) throws JzrInitializationException {
    	String path = outputDir + File.separator + PUBLISHED_ENCRYPTED_AES_KEY_FILE;
    	
        try (
                FileOutputStream ostream = new FileOutputStream(path, false);
                GZIPOutputStream gzip = new GZIPOutputStream(ostream);
        	)
        {
            gzip.write(encryptedAESKey.getBytes());
        } catch (IOException ex) {
			throw new JzrInitializationException("Failed to instanciate the security manager : AES key storage failed in " + path, ex);
		}
	}

	public Cipher getEncryptionCipher() {
		return encryptionCipher;
	}

	public boolean isEncryptionEnabled() {
		return encryptionEnabled;
	}
}
