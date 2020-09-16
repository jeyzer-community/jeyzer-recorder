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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.jeyzer.recorder.config.mx.advanced.JzrManifestConfig.TDJarConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManifestReader {
	
	public static final String MANIFEST_PATH = "!/META-INF/MANIFEST.MF";
	public static final Pattern MANIFEST_REGEX_PATTERN = Pattern.compile(".*/(.*jar)!/META-INF/MANIFEST.MF$");
	public static final Pattern JAR_REGEX_PATTERN = Pattern.compile(".*/(.*jar)$");
	
	protected static final Logger logger = LoggerFactory.getLogger(ManifestReader.class);

	private ManifestReader() {
	}
	
    public static void loadJarManifestAttributes(String jarName, URI uri, List<TDJarConfig> jarConfigs, Map<String, String> manifestAttributes) {
    	loadJarManifestAttributes(jarName, uri, jarConfigs, manifestAttributes, "");
    }
	
    public static void loadJarManifestAttributes(String jarName, URI uri, List<TDJarConfig> jarConfigs, Map<String, String> manifestAttributes, String prefix) {
		for (TDJarConfig jarConfig: jarConfigs){
			if (jarConfig.getNameFilter().matcher(jarName).find()){
				InputStream stream = null;

				try {
					stream = uri.toURL().openStream();
				} catch (MalformedURLException ex) {
					logger.error("Manifest attributes info collect failed for jar " + jarName + " : invalid URL.", ex);
					continue;
				} catch (IOException ex) {
					logger.error("Manifest attributes info collect failed for jar " + jarName + " : failed to open the jar.", ex);
					continue;
				}
				
				try{
					Manifest manifest = new Manifest(stream);
					loadAttributes(jarName, manifest.getMainAttributes(), jarConfig, manifestAttributes, prefix);
				} catch(RuntimeException ex){
	        	    logger.error("Manifest attributes info collect failed for jar " + jarName + ".", ex);
				} catch (IOException ex) {
					logger.error("Manifest attributes info collect failed for jar " + jarName + " : failed to read the jar manifest.", ex);
				}
				finally{
	        	   if  (stream != null)
	        		   try {
		        		   stream.close();						
	        		   } catch (Exception ex) {
	        			   logger.error("Failed to close " + jarName + ".", ex);
	        		   }
	           }
			}
		}
	}

	private static void loadAttributes(String jarName, Attributes attributes, TDJarConfig jarConfig, Map<String, String> manifestAttributes, String prefix) {
		for (Object key : attributes.keySet()) {
            String name = Attributes.Name.class.cast(key).toString();
            if (isMatchingAttribute(name, jarConfig))
            	manifestAttributes.put(prefix + name, attributes.getValue(Attributes.Name.class.cast(key)));
		}
	}

	private static boolean isMatchingAttribute(String name, TDJarConfig jarConfig) {
		for (Pattern pattern : jarConfig.getAttributeFilters()){
			if (pattern.matcher(name).find())
				return true;
		}
		return false;
	}
}
