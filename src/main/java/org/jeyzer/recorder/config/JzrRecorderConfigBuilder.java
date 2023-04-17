package org.jeyzer.recorder.config;

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





import java.io.InputStream;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.config.jmx.JzrJMXConfig;
import org.jeyzer.recorder.config.jmx.advanced.JzrAdvancedJMXConfig;
import org.jeyzer.recorder.config.jstack.JzrJstackConfig;
import org.jeyzer.recorder.config.jstack.JzrJstackInShellConfig;
import org.jeyzer.recorder.config.local.advanced.JzrAdvancedMXAgentConfig;
import org.jeyzer.recorder.util.ConfigUtil;
import org.jeyzer.recorder.util.SystemHelper;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JzrRecorderConfigBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(JzrRecorderConfigBuilder.class);
	
	public static final String CONFIG_FILE_PROPERTY = "jeyzer.record.config";
	
	private static final String PARAM_METHOD_JMX    = "jmx";
	private static final String PARAM_METHOD_ADVANCED_JMX = "advancedjmx";
	public static final String PARAM_METHOD_AGENT = "advancedmxagent";
	private static final String PARAM_METHOD_JSTACK_IN_SHELL = "jstackinshell";
	private static final String PARAM_METHOD_JSTACK = "jstack";
	
	private static final JzrRecorderConfigBuilder builder = new JzrRecorderConfigBuilder();
	
	private JzrRecorderConfigBuilder(){
	}
	
	public static JzrRecorderConfigBuilder newInstance(){
		return builder;
	}
	
	public JzrRecorderConfig buildConfig() throws JzrInitializationException {
		InputStream input = null; 
		
		try{
			String method = null;
			String tdgFilepath = System.getProperty(CONFIG_FILE_PROPERTY);
			
			if (tdgFilepath == null){
				throw new JzrInitializationException("System property " + CONFIG_FILE_PROPERTY + " is not set. Please fix it");
			}
			
			// resolve any variable or system property (ex: ${user.dir})
			tdgFilepath = ConfigUtil.resolveValue(tdgFilepath);
			tdgFilepath = SystemHelper.sanitizePathSeparators(tdgFilepath);
			logger.info("Loading Jeyzer Recorder config file : " + tdgFilepath);
			
			// load the file from the file system 
			input = ConfigUtil.getInputStream(tdgFilepath);
			if (input == null)
				throw new JzrInitializationException("Jeyzer configuration file \"" + tdgFilepath + " not found. Please fix it.");
			
			Element recorder = loadConfiguration(tdgFilepath, input);
			
			method = loadMethod(recorder); 
			
			if (PARAM_METHOD_AGENT.equals(method.toLowerCase())){
				return new JzrAdvancedMXAgentConfig(recorder);
			}
			else if (PARAM_METHOD_JSTACK_IN_SHELL.equals(method.toLowerCase())){
				return new JzrJstackInShellConfig(recorder);
			}
			else if (PARAM_METHOD_JSTACK.equals(method.toLowerCase())){
				return new JzrJstackConfig(recorder);
			}
			else if (PARAM_METHOD_JMX.equals(method.toLowerCase())){
				return new JzrJMXConfig(recorder);
			}
			else if (PARAM_METHOD_ADVANCED_JMX.equals(method.toLowerCase())){
				return new JzrAdvancedJMXConfig(recorder);
			}
			else{
				throw new JzrInitializationException("Invalid Jeyzer dump method : " + method + ". Please fix it.");
			}
		
		}
		catch (JzrInitializationException ex){
			throw ex;
		}		
		catch (Exception ex){
			throw new JzrInitializationException("Failed to load the thread dump configuration", ex);
		}
		finally{
			if (input != null)
				try { input.close(); } catch (Exception e) {}
		}
	}
	
	
	private String loadMethod(Element generation) {
		// snapshot node
		Element snapshotNode = ConfigUtil.getFirstChildNode(generation, JzrRecorderConfig.JZR_SNAPSHOT);

		// method node
		Element methodNode = ConfigUtil.getFirstChildNode(snapshotNode, JzrRecorderConfig.JZR_METHOD);
		
		// Method
		return ConfigUtil.getAttributeValue(methodNode, JzrRecorderConfig.JZR_NAME);
	}

	private Element loadConfiguration(String tdgFilepath, InputStream input) throws JzrInitializationException {
		Document doc = ConfigUtil.loadDOM(tdgFilepath, input);
		if (doc == null)
			throw new JzrInitializationException("Failed to load the Jeyzer Recorder configuration file : " + tdgFilepath);
		
		// recorder
		NodeList nodes = doc.getElementsByTagName(JzrRecorderConfig.JZR_RECORDER);		
		return (Element)nodes.item(0);
	}

}
