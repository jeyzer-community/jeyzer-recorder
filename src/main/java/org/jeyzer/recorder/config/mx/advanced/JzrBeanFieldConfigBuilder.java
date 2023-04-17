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





import java.util.ArrayList;
import java.util.List;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JzrBeanFieldConfigBuilder {

	private static final String JZR_SYSTEM = "system";
	private static final String JZR_PROCESS = "process";
	private static final String JZR_THREAD = "thread";
	private static final String JZR_PROCESS_CARD = "process_card";
	
	private static final JzrBeanFieldConfigBuilder builder = new JzrBeanFieldConfigBuilder();
	
	private JzrBeanFieldConfigBuilder(){}
	
	public static JzrBeanFieldConfigBuilder instance(){
		return builder;
	}
	
	public List<JzrBeanFieldConfig> buildBeanFieldConfigs(Element advancedJmxNode) throws JzrInitializationException {
		List<JzrBeanFieldConfig> beanFieldConfigs = new ArrayList<>(10);
		
		buildBeanFieldConfigsforCategory(advancedJmxNode, JZR_SYSTEM, beanFieldConfigs);
		buildBeanFieldConfigsforCategory(advancedJmxNode, JZR_PROCESS, beanFieldConfigs);
	
		return beanFieldConfigs;
	}
	
	private List<JzrBeanFieldConfig> buildBeanFieldConfigsforCategory(Element advancedJmxNode, String category, List<JzrBeanFieldConfig> beanFieldConfigs) throws JzrInitializationException {
		// category node
		Element categoryNode = ConfigUtil.getFirstChildNode(advancedJmxNode, category);

		if (categoryNode == null)
			return beanFieldConfigs;
		
		NodeList nodes = categoryNode.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++){
			Node item = nodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE){
				JzrBeanFieldConfig config;
				String name = item.getNodeName();
				if (JzrGenericMXBeanConfig.CONFIG_NODE_NAME.equals(name))
					config = new JzrGenericMXBeanConfig(category, name, item.getAttributes());
				else
					config = new JzrBeanFieldConfig(category, name);
				beanFieldConfigs.add(config);
			}
		}
		
		return beanFieldConfigs;
	}
	
	public List<JzrBeanFieldConfig> buildThreadBeanFieldConfigs(Element advancedJmxNode) throws JzrInitializationException {
		List<JzrBeanFieldConfig> threadBeanFieldConfigs = new ArrayList<>(10);
		
		// thread node
		Element threadNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JZR_THREAD);

		if (threadNode == null)
			return threadBeanFieldConfigs;
		
		NodeList nodes = threadNode.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++){
			Node item = nodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE){
				String name = item.getNodeName();
				JzrBeanFieldConfig config = new JzrBeanFieldConfig(JZR_THREAD, name);
				threadBeanFieldConfigs.add(config);
			}
		}
		
		return threadBeanFieldConfigs;
	}
	
	public List<JzrGenericMXBeanConfig> buildProcessCardBeanFieldConfigs(Element advancedJmxNode) throws JzrInitializationException {
		List<JzrGenericMXBeanConfig> processCardBeanFieldConfigs = new ArrayList<>();
		
		// process card node
		Element processCardNode = ConfigUtil.getFirstChildNode(advancedJmxNode, JZR_PROCESS_CARD);

		if (processCardNode == null)
			return processCardBeanFieldConfigs;
		
		NodeList nodes = processCardNode.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++){
			Node item = nodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE){
				String name = item.getNodeName();
				if (JzrGenericMXBeanConfig.CONFIG_NODE_NAME.equals(name)){
					JzrGenericMXBeanConfig config = new JzrGenericMXBeanConfig(JZR_PROCESS_CARD, name, item.getAttributes());
					processCardBeanFieldConfigs.add(config);
				}
			}
		}
		
		return processCardBeanFieldConfigs;
	}	

}
