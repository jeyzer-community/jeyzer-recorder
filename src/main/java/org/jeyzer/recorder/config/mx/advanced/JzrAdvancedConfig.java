package org.jeyzer.recorder.config.mx.advanced;

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

import java.io.IOException;





import java.io.InputStream;
import java.util.List;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.config.security.JzrSecurityConfig;
import org.jeyzer.recorder.util.ConfigUtil;
import org.jeyzer.recorder.util.SystemHelper;
import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class JzrAdvancedConfig extends JzrRecorderConfig{

	private static final Logger logger = LoggerFactory.getLogger(JzrAdvancedConfig.class);

	private static final String JZR_MX_ADVANCED = "mx_advanced";
	private static final String JZR_MX_ADVANCED_CONFIG_FILE = "config_file";
	private static final String JZR_MX_THREADS = "threads";
	private static final String JZR_MX_CAPTURE_DEADLOCKS = "capture_deadlocks";	
	
	private JzrSecurityConfig securityCfg;
	
	private JzrMemoryConfig memoryConfig;
	private List<JzrMemoryPoolConfig> poolConfigs;
	private List<JzrGarbageCollectorConfig> collectorConfigs;
	private List<JzrDiskSpaceConfig> diskSpaceConfigs;
	private boolean diskWriteTimeEnabled;
	
	private List<JzrBeanFieldConfig> beanFieldConfigs;
	private List<JzrBeanFieldConfig> threadBeanFieldConfigs;
	
	private List<JzrGenericMXBeanConfig> processCardBeanFieldConfigs;
	private JzrManifestConfig manifestConfig;
	
	private JzrJarPathConfig jarPathConfig;
	private JzrModuleConfig moduleConfig;
	private JzrJVMFlagConfig jvmFlagConfig;
	
	private boolean captureDeadlockEnabled = false;

	public JzrAdvancedConfig(Element recorder) throws JzrInitializationException {
		super(recorder);
		
		// dump node
		Element dumpNode = ConfigUtil.getFirstChildNode(recorder, JzrRecorderConfig.JZR_SNAPSHOT);

		// methods node
		Element methodsNode = ConfigUtil.getFirstChildNode(dumpNode, JzrRecorderConfig.JZR_METHODS);
		
		// jmx node
		Element jmxAdvancedNode = ConfigUtil.getFirstChildNode(methodsNode, JZR_MX_ADVANCED);

		String path = ConfigUtil.loadStringValue(jmxAdvancedNode, JZR_MX_ADVANCED_CONFIG_FILE);
		path = SystemHelper.sanitizePathSeparators(path);

		logger.info("Loading Advanced MX config file : " + path);
		
		try (
				InputStream input = ConfigUtil.getInputStream(path);
			)
		{
			if (input == null){
				logger.error("Advanced MX configuration file " + path + " not found.");
				throw new JzrInitializationException("Advanced MX configuration file " + path + " not found.");
			}
			loadConfiguration(path, input);
			
		} catch (IOException e) {
			logger.error("Advanced MX configuration file " + path + " reading failed.");
			throw new JzrInitializationException("Advanced MX configuration file " + path + " reading failed.");
		}
	}
	
	@Override
	public boolean isEncryptionEnabled() {
		return this.securityCfg.isEncryptionEnabled();
	}
	
	@Override
	public boolean isEncryptionKeyPublished() {
		return this.securityCfg.isEncryptionKeyPublished();
	}
	
	public JzrSecurityConfig getSecurityCfg() {
		return securityCfg;
	}
	
	public JzrMemoryConfig getMemoryConfig(){
		return memoryConfig;
	}
	
	public List<JzrMemoryPoolConfig> getPoolConfigs() {
		return poolConfigs;
	}

	public List<JzrGarbageCollectorConfig> getCollectorConfigs() {
		return collectorConfigs;
	}	
	
	public List<JzrBeanFieldConfig> getBeanFieldConfigs() {
		return beanFieldConfigs;
	}
	
	public List<JzrBeanFieldConfig> getThreadBeanFieldConfigs() {
		return threadBeanFieldConfigs;
	}	

	public List<JzrGenericMXBeanConfig> getProcessCardBeanFieldConfigs() {
		return processCardBeanFieldConfigs;
	}
	
	public JzrManifestConfig getManifestConfig() {
		return manifestConfig;
	}
	
	public JzrJarPathConfig getJarPathConfig() {
		return jarPathConfig;
	}
	
	public JzrModuleConfig getModuleConfig() {
		return moduleConfig;
	}
	
	public JzrJVMFlagConfig getJVMFlagConfig() {
		return jvmFlagConfig;
	}

	public List<JzrDiskSpaceConfig> getDiskSpaceConfigs(){
		return diskSpaceConfigs;
	}
	
	public boolean isDiskWriteTimeEnabled(){
		return diskWriteTimeEnabled;
	}

	public boolean isDeadlockCaptureEnabled(){
		return captureDeadlockEnabled;
	}
	
	private void loadConfiguration(String path, InputStream file) throws JzrInitializationException{
		Document doc = ConfigUtil.loadDOM(path, file);
		if (doc == null)
			throw new JzrInitializationException("Failed to load the Jeyzer Recorder advanced configuration file : " + path);
			
		// report
		NodeList nodes = doc.getElementsByTagName(JZR_MX_ADVANCED);
		Element advancedJmxNode = (Element)nodes.item(0);
		
		// Security, optional
		securityCfg = new JzrSecurityConfig(advancedJmxNode);

		poolConfigs= JzrMemoryPoolConfigBuilder.newInstance().buildPoolConfigs(advancedJmxNode);
		collectorConfigs = JzrGarbageCollectorConfigBuilder.newInstance().buildGarbargeCollectorConfigs(advancedJmxNode);
		beanFieldConfigs = JzrBeanFieldConfigBuilder.instance().buildBeanFieldConfigs(advancedJmxNode);
		threadBeanFieldConfigs = JzrBeanFieldConfigBuilder.instance().buildThreadBeanFieldConfigs(advancedJmxNode);
		processCardBeanFieldConfigs = JzrBeanFieldConfigBuilder.instance().buildProcessCardBeanFieldConfigs(advancedJmxNode);
		diskSpaceConfigs = JzrDiskConfigBuilder.instance().buildDiskSpaceConfigs(advancedJmxNode);
		diskWriteTimeEnabled = JzrDiskConfigBuilder.instance().isDiskSpaceWriteEnabled(advancedJmxNode);
		
		// manifest node in process card node, optional
		loadManifestNode(advancedJmxNode);
		
		// jar paths, optional
		loadJarPathsNode(advancedJmxNode, getThreadDumpDirectory());
		
		// modules, optional
		loadModulesNode(advancedJmxNode, getThreadDumpDirectory());
		
		// JVM flags, optional
		loadJVMFlagNode(advancedJmxNode, getThreadDumpDirectory());
		
		// memory node
		Element memoryNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JzrMemoryConfig.JZR_MEMORY);
		if (memoryNode != null)
			memoryConfig = new JzrMemoryConfig(memoryNode);
		else 
			memoryConfig = new JzrMemoryConfig();
		
		Element threadsNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JZR_MX_THREADS);
		captureDeadlockEnabled = Boolean.parseBoolean(ConfigUtil.loadStringValue(threadsNode, JZR_MX_CAPTURE_DEADLOCKS));
	}

	private void loadManifestNode(Element advancedJmxNode) throws JzrInitializationException {
		Element processCardNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JZR_PROCESS_CARD);
		if (processCardNode != null){
			Element manifestNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JzrManifestConfig.JZR_MANIFEST);
			if (manifestNode != null)
				manifestConfig = new JzrManifestConfig(manifestNode);
		}
		// default empty one
		if (manifestConfig == null)
			manifestConfig = new JzrManifestConfig();
	}
	
	private void loadJarPathsNode(Element advancedJmxNode, String tdDir) throws JzrInitializationException {
		Element jarPathsNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JzrJarPathConfig.JZR_JAR_PATHS);
		if (jarPathsNode != null) {
			logger.debug("Jar paths accessor configuration found.");
			jarPathConfig = new JzrJarPathConfig(jarPathsNode, tdDir);
		}
		else {
			logger.debug("Jar paths accessor configuration not found.");
			jarPathConfig = new JzrJarPathConfig();
		}
	}
	
	private void loadJVMFlagNode(Element advancedJmxNode, String tdDir) throws JzrInitializationException {
		Element jvmFlagsNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JzrJVMFlagConfig.JZR_JVM_FLAGS);
		if (jvmFlagsNode != null) {
			logger.debug("JVM flags accessor configuration found.");
			jvmFlagConfig = new JzrJVMFlagConfig(jvmFlagsNode, tdDir);
		}
		else {
			logger.debug("JVM flags accessor configuration not found.");
			jvmFlagConfig = new JzrJVMFlagConfig();
		}
	}
	
	private void loadModulesNode(Element advancedJmxNode, String tdDir) throws JzrInitializationException {
		boolean moduleSupport = SystemHelper.isAtLeastJdK9();
		Element modulesNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JzrModuleConfig.JZR_MODULES);
		if (moduleSupport && modulesNode != null) {
			logger.debug("Module accessor configuration found.");
			moduleConfig = new JzrModuleConfig(modulesNode, tdDir);
		}
		else {
			if (!moduleSupport)
				logger.debug("Modules not supported on this Java version.");
			else
				logger.debug("Module accessor configuration not found.");
			moduleConfig = new JzrModuleConfig();
		}
	}
}
