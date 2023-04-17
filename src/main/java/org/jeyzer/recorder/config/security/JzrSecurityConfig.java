package org.jeyzer.recorder.config.security;

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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.jeyzer.recorder.util.SystemHelper;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JzrSecurityConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(JzrSecurityConfig.class);

	public enum EncryptionMode { DYNAMIC, STATIC };

	// AES encryption algorithm 
	// Stronger algorithm is "AES/GCM/NoPadding" but requires the "IV" cipher parameter which is random bytes sequence. 
	//  The "IV" must also be used in the decryption and therefore passed in the pay load or stored in the configuration.
	public static final String ENCRYPTION_ALGORITHM = "AES"; 
	
	// AES key algorithm 
	public static final String KEY_ALGORITHM = "AES";
	
	// RSA local encryption algorithm (signature like security : private to public)
	//   OAEP is not supported by definition in signature approach
	public static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
	
	// RSA encryption algorithm used to encrypt the AES key in dynamic mode 
	//   OAEP makes it the strongest (encryption like security : public to private)
	public static final String RSA_OAEP_ALGORITHM = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
	
	public static final String KEY_TYPE = "RSA";
		
	private static final String JZR_SECURITY = "security";
	private static final String JZR_SECURITY_CONFIG_FILE = "config_file";
	private static final String JZR_ENCRYPTION = "encryption";
	private static final String JZR_MODE = "mode";
	private static final String JZR_STATIC = "static";
	private static final String JZR_ENCRYPTED_KEY_FILE = "encrypted_key_file";
	private static final String JZR_DYNAMIC = "dynamic";
	private static final String JZR_MASTER_PUBLIC_KEY_FILE = "master_public_key_file";
	
	private static final String DECRYPTION_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDo32co2tbUGr12vjhM2BJo2MqrOaXoXgY9T4mRxWhDFOimSPD+dHusyPcKg9NO12YeIX7pl8f5Y/pjdRXGKp5hQdsEAynOGLjGf89x6p4lqRtSoJZNzYFypCVllQNVbysVcxKA7/h66XqtXbNJw42apAFVRmEBhdzvXDSz1UY9uwIDAQAB";
	
	private boolean encryptionEnabled;
	private byte[] masterPublicKey;
	private SecretKey encryptionKey;
	private EncryptionMode mode;
	
	public JzrSecurityConfig(Element node) throws JzrInitializationException {
		if (node == null)
			return;
		Element securityNode = ConfigUtil.getFirstChildNode(node, JZR_SECURITY);
		if (securityNode == null)
			return; // disabled
		
		loadSecurity(securityNode);
	}

	public boolean isEncryptionEnabled() {
		return encryptionEnabled;
	}
	
	public byte[] getMasterPublicKey() {
		return masterPublicKey;
	}

	public SecretKey getEncryptionKey() {
		return encryptionKey;
	}

	public boolean isEncryptionKeyPublished() {
		return EncryptionMode.DYNAMIC.equals(mode);
	}

	private void loadSecurity(Element securityNode) throws JzrInitializationException {
		String path = ConfigUtil.loadStringValue(securityNode, JZR_SECURITY_CONFIG_FILE);
		path = SystemHelper.sanitizePathSeparators(path);
		
		try (
				InputStream input = ConfigUtil.getInputStream(path);
			)
		{
			if (input == null){
				logger.error("Security file " + path + " not found.");
				throw new JzrInitializationException("Security file " + path + " not found.");
			}
			
			Document doc = ConfigUtil.loadDOM(path, input);
			if (doc == null)
				throw new JzrInitializationException("Failed to load the Jeyzer Recorder security configuration file : " + path);
			
			// security
			NodeList nodes = doc.getElementsByTagName(JZR_SECURITY);
			Element secNode = (Element)nodes.item(0);			
			if (secNode == null)
				throw new JzrInitializationException("Invalid security file " + path + ". Security node not found.");

			Element encryptionNode = ConfigUtil.getFirstChildNode(secNode, JZR_ENCRYPTION);
			if (encryptionNode == null)
				throw new JzrInitializationException("Invalid security file " + path + ". Encryption node not found.");
						
			loadMode(encryptionNode, path);
			loadEncryptionKey(encryptionNode, path);
			
			if (isEncryptionKeyPublished())
				loadMasterPublicKey(secNode, path);
			
		} catch (IOException e) {
			logger.error("Security file " + path + " reading failed.");
			throw new JzrInitializationException("Security file " + path + " reading failed.");
		}
		
		encryptionEnabled = true;
	}

	private void loadMode(Element encryptionNode, String path) throws JzrInitializationException {
		String value = ConfigUtil.loadStringValue(encryptionNode, JZR_MODE);
		if (value == null || value.isEmpty())
			throw new JzrInitializationException("Invalid security file " + path + ". Encryption mode not found.");

		try{
			mode = EncryptionMode.valueOf(value.toUpperCase());			
		}
		catch (IllegalArgumentException ex){
			throw new JzrInitializationException("Invalid security file " + path + ". Encryption mode is invalid : " + value);
		}
	}

	private void loadEncryptionKey(Element encryptionNode, String path) throws JzrInitializationException {		
		switch(mode){
			case DYNAMIC:
				generateDynamicKey();
				break;
			case STATIC:
				loadEncryptionKeyFile(encryptionNode, path);
				break;
			default:
				throw new JzrInitializationException("Invalid security file " + path + ". Encryption mode not supported : " + mode);
		}
	}

	private void loadEncryptionKeyFile(Element encryptionNode, String path) throws JzrInitializationException {
		Element staticNode = ConfigUtil.getFirstChildNode(encryptionNode, JZR_STATIC);
		if (staticNode == null)
			throw new JzrInitializationException("Invalid security file " + path + ". Static node not found.");
		
		String keyPath = ConfigUtil.loadStringValue(staticNode, JZR_ENCRYPTED_KEY_FILE);
		if (keyPath == null || keyPath.isEmpty())
			throw new JzrInitializationException("Invalid security file " + path + ". Encryption key path not found.");
		
		File file = new File(keyPath);
		if (!file.isFile() || !file.exists())
			throw new JzrInitializationException("Invalid encryption key file path : " + keyPath);

		String encodedEncryptedAESKey;
		try (
				FileReader fr = new FileReader(file);
				BufferedReader buffer = new BufferedReader(fr);
			)
		{
			encodedEncryptedAESKey = buffer.readLine();
		} catch (IOException ex) {
			throw new JzrInitializationException("Failed to read the encryption key file : " + keyPath, ex);
		}
		
		byte[] decodedAESKey = decryptAESKey(encodedEncryptedAESKey);
		
		this.encryptionKey = new SecretKeySpec(decodedAESKey, ENCRYPTION_ALGORITHM);
	}

	private byte[] decryptAESKey(String encodedEncryptedAESKey) throws JzrInitializationException {
		byte[] decodedEncryptedAESKey = Base64.getDecoder().decode(encodedEncryptedAESKey);
		
        Cipher cipher;
		try {
			cipher = Cipher.getInstance(RSA_ALGORITHM);
		} catch (GeneralSecurityException ex) {
			throw new JzrInitializationException("Failed to instanciate the security configuration for the AES key decryption due to invalid RSA encryption algorithm : " + RSA_ALGORITHM, ex);
		}
        
		PublicKey publicKey = getJeyzerMasterPublicKey();
    	try {
			cipher.init(Cipher.PRIVATE_KEY, publicKey);
		} catch (InvalidKeyException ex) {
			throw new JzrInitializationException("Failed to instanciate the security configuration for the AES key decryption due to invalid RSA key. " + ex.getMessage(), ex);
		}
    	
    	byte[] decryptedAESKey;
		try {
			decryptedAESKey = cipher.doFinal(decodedEncryptedAESKey);
		} catch (GeneralSecurityException ex) {
			throw new JzrInitializationException("Failed to decrypt the AES key. " + ex.getMessage(), ex);
		}

		return decryptedAESKey;
	}

	private void generateDynamicKey() throws JzrInitializationException {
    	KeyGenerator generator;
		try {
			generator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
	    	generator.init(128); // The AES key size in number of bits
	    	this.encryptionKey = generator.generateKey();
		} catch (NoSuchAlgorithmException ex) {
			throw new JzrInitializationException("Failed to generate the encryption key file.", ex);
		}
	}

	private void loadMasterPublicKey(Element securityNode, String path) throws JzrInitializationException {
		Element dynamicNode = ConfigUtil.getFirstChildNode(securityNode, JZR_DYNAMIC);
		if (dynamicNode == null)
			throw new JzrInitializationException("Invalid security file " + path + ". Dynamic node not found.");
		
		String keyPath = ConfigUtil.loadStringValue(dynamicNode, JZR_MASTER_PUBLIC_KEY_FILE);
		if (keyPath == null || keyPath.isEmpty())
			throw new JzrInitializationException("Invalid security file " + path + ". Master public key path not found.");
		
		File file = new File(keyPath);
		if (!file.isFile() || !file.exists())
			throw new JzrInitializationException("Invalid master public key file : " + keyPath);

		List<String> chunks;
		try {
			chunks = readKeyFileChunks(file);
		} catch (IOException ex) {
			throw new JzrInitializationException("Failed to read the master public key file : " + keyPath, ex);
		}
		
		this.masterPublicKey = decryptRSAKey(chunks);
	}
	
	private List<String> readKeyFileChunks(File file) throws IOException{
		List<String> chunks = new ArrayList<>();
		
		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			String line = reader.readLine();
			while(line != null){
				chunks.add(line);
				line = reader.readLine();
			}
		}
		
		return chunks;
	}
	
	private byte[] decryptRSAKey(List<String> chunks) throws JzrInitializationException {
		PublicKey jeyzerPubKey = getJeyzerMasterPublicKey();
		byte[] loadedKey = new byte[0];
		int globalpos = 0;
		for (String chunk : chunks){
			byte[] bytes = Base64.getDecoder().decode(chunk);
			
			Cipher cipher;
			try {
				cipher = Cipher.getInstance(RSA_ALGORITHM);
			} catch (GeneralSecurityException ex) {
				throw new JzrInitializationException("Failed to instanciate the security configuration for the dynamic RSA public key decryption due to invalid RSA encryption algorithm : " + RSA_ALGORITHM, ex);
			}
			try {
				cipher.init(Cipher.DECRYPT_MODE, jeyzerPubKey);
			} catch (InvalidKeyException ex) {
				throw new JzrInitializationException("Failed to instanciate the security configuration for the dynamic RSA public key decryption due to invalid RSA key. " + ex.getMessage(), ex);
			}
			byte[] decryptedLoadedKey;
			try {
				decryptedLoadedKey = cipher.doFinal(bytes);
			} catch (GeneralSecurityException ex) {
				throw new JzrInitializationException("Failed to decrypt the dynamic RSA public key. " + ex.getMessage(), ex);
			}
			
			loadedKey = Arrays.copyOf(loadedKey, loadedKey.length + decryptedLoadedKey.length);
			for (byte element : decryptedLoadedKey){
				loadedKey[globalpos] = element;
				globalpos++;
			}
		}
		return loadedKey;
	}
	
	private PublicKey getJeyzerMasterPublicKey() throws JzrInitializationException {
        PublicKey publicKey;
        
		byte[] decodedRSAKey = Base64.getDecoder().decode(DECRYPTION_PUBLIC_KEY);
		try {
			publicKey = KeyFactory.getInstance(KEY_TYPE).generatePublic(new X509EncodedKeySpec(decodedRSAKey));
		} catch (InvalidKeySpecException ex) {
			throw new JzrInitializationException("Failed to instanciate the security configuration for the dynamic RSA public key decryption due to invalid RSA key spec. " + ex.getMessage(), ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new JzrInitializationException("Failed to instanciate the security configuration for the dynamic RSA public key decryption due to invalid RSA alggorithm : " + RSA_ALGORITHM + ". " + ex.getMessage(), ex);
		}
		return publicKey;
	}
}
