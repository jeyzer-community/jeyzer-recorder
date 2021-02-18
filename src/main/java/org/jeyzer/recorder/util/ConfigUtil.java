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




import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jeyzer.recorder.JeyzerRecorder;
import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigUtil {

	private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);	
	
	private static final String VARIABLE_PREFIX = "${";
	private static final String VARIABLE_SUFFIX = "}";
	
	public static final String ISO_8601_DURATION_PREFIX = "PT";
	
	public static final String XERCES_DOC_BUILDER_FACTORY_IMPL = "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";
	public static final String XERCES_FEATURE_LOAD_EXT_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
	public static final String XERCES_FEATURE_LOAD_EXT_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
	public static final String XERCES_FEATURE_LOAD_EXT_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
	
	private ConfigUtil(){
	}
	
	public static InputStream getInputStream(String resourcePath){
		InputStream inputStream =  null;
		
		File tdgFile = new File(resourcePath);
		if (tdgFile.exists()){
			// get it from file system
			try {
				inputStream = new FileInputStream(resourcePath);
			} catch (FileNotFoundException e) {
				return null;
			}
		}else{
			// get it from the class loader
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			inputStream = classloader.getResourceAsStream(resourcePath);
		}

		return inputStream;
	}
	
	public static Properties loadFile(File file){
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(file.getPath()));
		} catch (FileNotFoundException e) {
			logger.error("Failed to open " + file.getPath(), e);
		} catch (IOException e) {
			logger.error("Failed to open " + file.getPath(), e);
		}
		return props;
	}
	
	public static boolean isValidURI(String candidate){
		try {
			URL u = new URL(candidate);
			u.toURI();
			return true;
		} catch (Exception e) {
			return false;
		}		
	}
	
	public static DocumentBuilder getDocumentBuilder() {
		DocumentBuilder db = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			if (XERCES_DOC_BUILDER_FACTORY_IMPL.equals(dbf.getClass().getName())){
				logger.debug("Loading the Apache Xerces document builder factory : " + dbf.getClass().getName());
				// Security : see http://apache-xml-project.6118.n7.nabble.com/Disabling-XML-External-Entites-td39499.html
				dbf.setFeature(XERCES_FEATURE_LOAD_EXT_DTD, false);
				dbf.setFeature(XERCES_FEATURE_LOAD_EXT_GENERAL_ENTITIES, false);
				dbf.setFeature(XERCES_FEATURE_LOAD_EXT_PARAMETER_ENTITIES, false);
			}
			else {
				// Can be com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl on JDK 8 and 11
				logger.debug("Loading the document builder factory : " + dbf.getClass().getName());
				// Security
				dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
				dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			}
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Failed to load the document builder.", e);
		}
		return db;
	}
	
	public static Document loadDOM(String tdgFilepath, InputStream input){
		Document doc = null;
		try {
			DocumentBuilder db = getDocumentBuilder();
			if (db == null)
				return null;
			doc = db.parse(input);
			doc.getDocumentElement().normalize();
		} catch (IOException e) {
			logger.error("Failed to open " + tdgFilepath, e);
		} catch (SAXException e) {
			logger.error("Failed to parse " + tdgFilepath, e);
		}
		return doc;
	}
	
	public static Element getFirstChildNode(Element node, String name){
		NodeList nodes = node.getElementsByTagName(name);
		if (nodes.getLength() == 0)
			return null;
		else
			return (Element)nodes.item(0);
	}
	
	/**
	 * Reads the node attribute and resolves variables ${VARIABLE} if present, 
	 * looking first for system property, second for environment variable.
	 */
	public static String getAttributeValue(Element node, String name){
		String value;
		
		value = node.getAttribute(name);
		
		return resolveValue(value);
	}
	
	/**
	 * Creates a duration from an ISO-8601 date. Examples : 10m, 1H30M, 30s.
	 * The "PT" prefix is optional. Any contained variable is expanded first.
	 * See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-.
	 */
	public static Duration getAttributeDuration(Element node, String attribute){
		String value = ConfigUtil.getAttributeValue(node, attribute);
		return parseDuration(value);
	}
	
	/**
	 * Creates a duration from an ISO-8601 date. Examples : 10m, 1H30M, 30s.
	 * The "PT" prefix is optional. Any contained variable is expanded first.
	 * See https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-.
	 */
	public static Duration parseDuration(String value){
		if (value == null || value.isEmpty())
			return null;
		
		// digits only, convert it in seconds by default
		try{
			Integer testValue = Integer.valueOf(value);
			if (testValue != -1)
				logger.info("Time value given without ISO-8601 time unit : " + testValue + ". Defaulting to time unit in seconds.");
			value += "s";
		}
		catch(NumberFormatException ex){
			// ignore
		}
	
		if (!value.startsWith(ISO_8601_DURATION_PREFIX))
			value = ISO_8601_DURATION_PREFIX + value;
		
		return Duration.parse(value);
	}
	
	/**
	 * Resolves the variables ${VARIABLE} if present, 
	 * looking first for system property, second for environment variable.
	 */
	public static String resolveVariable(String value){

		if (value !=null && value.startsWith(VARIABLE_PREFIX) && value.endsWith(VARIABLE_SUFFIX)){
			String variable = value.substring(2, value.length()-1);
			String resolvedValue;
			
			// exotic case, return ${}
			if (variable.length()==0)
				return value;
			
			resolvedValue = System.getProperty(variable);
			if (resolvedValue != null){
				if (logger.isDebugEnabled())
					logger.debug("Variable \"" + value + "\" resolved through system property. Value : " + resolvedValue.replace('\\', '/'));
				if (resolvedValue.contains(VARIABLE_PREFIX)){
					if (logger.isDebugEnabled())
						logger.debug("Resolving inner variable for value \"" + resolvedValue.replace('\\', '/') + "\".");
					resolvedValue = resolveValue(resolvedValue);
					if (logger.isDebugEnabled())
						logger.debug("Inner variable resolved");
				}
				return resolvedValue;
			}
			
			resolvedValue = System.getenv(variable);
			if (resolvedValue != null){
				if (logger.isDebugEnabled())
					logger.debug("Variable \"{" + value + "}\" resolved through environment variable. Value : " + resolvedValue.replace('\\', '/'));
				if (resolvedValue.contains(VARIABLE_PREFIX)){
					if (logger.isDebugEnabled())
						logger.debug("Resolving inner variable for value \"" + resolvedValue.replace('\\', '/') + "\".");
					resolvedValue = resolveValue(resolvedValue);
					if (logger.isDebugEnabled())
						logger.debug("Inner variable resolved");
				}				
				return resolvedValue;
			}
			
			logger.warn("Variable " + value.replace('\\', '/') + " cannot be resolved. Returning variable name.");
		}
		
		return value;
	}

	/**
	 * Resolves the variables ${VARIABLE} if present, 
	 * looking first for system property, second for environment variable.
	 */
	public static String resolveValue(String value){
		StringBuilder resolvedValue = new StringBuilder(10);
		int end = 0;
		int pos = 0;
		int prev = 0;
		
		while(pos != -1){
			pos = value.indexOf(VARIABLE_PREFIX, pos);
			
			if (pos != -1){
				// get the beginning
				resolvedValue.append(value.substring(prev, pos));
								
				end = value.indexOf(VARIABLE_SUFFIX, pos);
				if (end == -1){
					logger.warn("Incomplete variable definition");
					resolvedValue.append(value.substring(pos));
					return resolvedValue.toString();
				}
				else{
					resolvedValue.append(resolveVariable(value.substring(pos, end+1)));
				}
				
				prev = end +1;
				end++;
				pos = end;
			}else {
				// end reached
				resolvedValue.append(value.substring(end, value.length()));
			}
			
		}
		
		if (end != 0 && logger.isDebugEnabled())
			logger.debug("Value loaded \"" + value + "\" mapped to \"" + resolvedValue.toString().replace('\\', '/') + "\"");
		
		return resolvedValue.toString();
	}

	
	public static String loadStringValue(Element node, String param) throws JzrInitializationException{
		String value = ConfigUtil.getAttributeValue(node, param);
		
		if (value == null || value.isEmpty()) {
			logger.error("Configuration error - parameter " + param + " is missing on node " + node.getNodeName());
			throw new JzrInitializationException("Configuration error - Parameter " + param + " is missing on node " + node.getNodeName());
		}
		
		return value;
	}
	
	
	public static int loadIntegerValue(Element node, String param) throws JzrInitializationException{
		int intValue;

		String value = loadStringValue(node, param);

		try {
			intValue = Integer.parseInt(value);
		} catch (NumberFormatException x) {
			logger.error("Configuration error - Invalid " + param + " parameter : " + value);
			throw new JzrInitializationException("Configuration error - Invalid " + param + " parameter : " + value, x);
		}
		
		return intValue;
		
	}
	
	public static void validate(Properties props, String[] params) throws JzrInitializationException{
		for (int i=0;i<params.length;i++){
			String param = params[i];
			if (!props.containsKey(param))
				throw new JzrInitializationException("Missing parameter  : " + param);
			if (props.getProperty(param) == null)
				throw new JzrInitializationException("Missing parameter value for param : " + param);
		}
		
	}
	
	public static String loadRecorderVersion() {
		try {
			Class<JeyzerRecorder> clazz = JeyzerRecorder.class;
			String className = clazz.getSimpleName() + ".class";
			String classPath = clazz.getResource(className).toString();
			if (!classPath.startsWith("jar"))
				// Class not loaded from JAR
				return "Not available : classes mode";
			
			String manifestPath = classPath.substring(0,
					classPath.lastIndexOf('!') + 1)
					+ "/META-INF/MANIFEST.MF";
			Manifest manifest = new Manifest(new URL(manifestPath).openStream());
			Attributes attr = manifest.getMainAttributes();
			String value = attr.getValue("Specification-Version");
			if (value == null)
				// Class loaded from JAR within war file
				return "Not available : war mode";
			
			return value;
		} catch (IOException ex) {
			logger.warn("Failed to access the Recorder version from its Manifest file", ex);
			return "Not available - Manifest read error";
		}
	}

	/*
	 * Test
	 */
//	public static void main(String[] args) {
//		String result;
//		
//		System.setProperty("China", "Pekin");
//		System.setProperty("France", "Paris");
//		System.setProperty("Corea", "Seoul");
//
//		System.out.println("==========================");
//		String test = "Hello world";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "${China} Hello world";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "Hello world ${China}";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);	
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "Hello ${China} world";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);		
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "Hello ${China}${France} world ${Corea}";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "${China} Hello ${France} world ${Corea}";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);		
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "Hello ${China}${France} world ${}";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//
//		System.out.println("==========================");
//		test = "Hello ${China${France} world ${}";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//		
//		System.out.println("==========================");
//		test = "";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//		
//		System.out.println("==========================");
//		test = "${";
//		System.out.println(test);
//		result = resolveValue(test);
//		System.out.println(result);
//		System.out.println("==========================");
//				
//	}
	
	
}
