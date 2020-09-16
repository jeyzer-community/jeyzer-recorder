package org.jeyzer.recorder.accessor.local.advanced.process.jar;

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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.jeyzer.recorder.accessor.mx.advanced.JzrAbstractBeanFieldAccessor;
import org.jeyzer.recorder.config.mx.advanced.JzrManifestConfig;
import org.jeyzer.recorder.config.mx.advanced.JzrManifestConfig.TDJarConfig;
import org.jeyzer.recorder.util.FileUtil;
import org.jeyzer.recorder.util.ManifestReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalManifestAccessor extends JzrAbstractBeanFieldAccessor{

	protected static final Logger logger = LoggerFactory.getLogger(LocalManifestAccessor.class);
	
	private static final String MANIFEST = "META-INF/MANIFEST.MF";
	
	private static final String PROPERTY_PREFIX = "jzr.jar.";
	
	private List<TDJarConfig> jarConfigs;
	private Map<String, String> manifestAttributes = new LinkedHashMap<String, String>();
	
	public LocalManifestAccessor(JzrManifestConfig manifestConfig) {
		jarConfigs = manifestConfig.getJarConfigs();
		this.supported = true; // by default
	}

	public void collectProcessCardFigures() {
		if (jarConfigs.isEmpty())
			return;
		
        Enumeration<URL> resources;
        
		try {
			resources = Thread.currentThread().getContextClassLoader().getResources(MANIFEST);
		} catch (IOException ex) {
			logger.error("Manifest attributes info collect failed : access to all Manifests through current context class loader failed.", ex);
			return;
		}
		
        while (resources.hasMoreElements()) {
        	URI uri;
            try {
            	uri = resources.nextElement().toURI();
            	logger.debug("Found URI : " + uri.toString());
            } catch (URISyntaxException ex) {
            	logger.error("Manifest attributes info collect failed : invalid URI.", ex);
            	continue;
            }
            Matcher match = ManifestReader.MANIFEST_REGEX_PATTERN.matcher(uri.toString());
            if (!match.matches())
            	continue; // not jar
            
            ManifestReader.loadJarManifestAttributes(
            		match.group(1), 
            		uri, 
            		jarConfigs,
            		manifestAttributes,
            		PROPERTY_PREFIX + match.group(1) + "."
            		);
        }
	}

	public void printProcessCardValues(BufferedWriter writer) throws IOException {
		for (Entry<String,String> entry : this.manifestAttributes.entrySet()){
			printValue(
					writer, 
					entry.getKey() + "=",
					entry.getValue(), 
					FileUtil.JZR_FIELD_JZ_PREFIX + entry.getKey() + "=-1"
					);
		}
	}

	public void processCardClose() {
		this.manifestAttributes.clear();
	}
}
