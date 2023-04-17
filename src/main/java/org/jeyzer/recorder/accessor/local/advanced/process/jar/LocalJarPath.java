package org.jeyzer.recorder.accessor.local.advanced.process.jar;

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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.jeyzer.recorder.config.mx.advanced.JzrManifestConfig;
import org.jeyzer.recorder.util.ManifestReader;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class LocalJarPath {
	
	protected static final Logger logger = LoggerFactory.getLogger(LocalJarPath.class);
	
	private URI uri;
	private Map<String, String> manifestAttributes = new LinkedHashMap<>();
	
	public LocalJarPath(URI uri, JzrManifestConfig manifestConfig) {
		this.uri = uri;
		if (manifestConfig != null) {
			Matcher match = ManifestReader.JAR_REGEX_PATTERN.matcher(uri.toString());
			URI manifestURI;
			try {
				manifestURI = new URI("jar:" + uri.toString() + ManifestReader.MANIFEST_PATH);
			} catch (URISyntaxException e) {
				logger.error("Failed to create URI from : " + "jar:" + uri.toString() + ManifestReader.MANIFEST_PATH);
				return;
			}
			
			if (match.matches())
			    ManifestReader.loadJarManifestAttributes(
			    		match.group(1),
			    		manifestURI,
			        	manifestConfig.getJarConfigs(),
			        	manifestAttributes
			        	);
		}
	}

	public String getJarPath() {
		return this.uri.toString();
	}

	public void store(BufferedWriter writer) throws IOException {
		writer.write(uri.toString());
		for (Entry<String,String> attribute : manifestAttributes.entrySet()) {
			writer.write(';');
			writer.write(attribute.getKey());
			writer.write('=');
			writer.write(attribute.getValue());
		}
		writer.newLine();
	}
}
