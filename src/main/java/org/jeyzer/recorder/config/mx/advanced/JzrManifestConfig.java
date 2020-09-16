package org.jeyzer.recorder.config.mx.advanced;

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





import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jeyzer.recorder.accessor.error.JzrInitializationException;
import org.jeyzer.recorder.util.ConfigUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JzrManifestConfig {
	
	public static final String JZR_MANIFEST = "manifest";

	private List<TDJarConfig> jarConfigs = new ArrayList<>();
	
	public JzrManifestConfig(){
		// default (fully disabled) if not specified. 
	}
	
	public JzrManifestConfig(Element manifestNode) throws JzrInitializationException{
		loadJarConfigs(manifestNode);
	}
	
	public List<TDJarConfig> getJarConfigs() {
		return jarConfigs;
	}

	private void loadJarConfigs(Element manifestNode) throws JzrInitializationException {
		NodeList nodes = manifestNode.getChildNodes();
		for (int i=0; i<nodes.getLength(); i++){
			Node item = nodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE){
				String name = item.getNodeName();
				if (TDJarConfig.JZR_JAR.equals(name)){
					TDJarConfig config = new TDJarConfig((Element)item);
					jarConfigs.add(config);
				}
			}
		}
	}

	public final class TDJarConfig{

		public static final String JZR_JAR = "jar";
		public static final String JZR_FILTER = "filter";
		public static final String JZR_PATTERN = "pattern";
		public static final String JZR_MANIFEST_INFO = "manifest_info";

		private Pattern nameFilter;
		private List<Pattern> attributeFilters = new ArrayList<>();
		
		public TDJarConfig(Element jarNode) throws JzrInitializationException {
			loadNameFilter(jarNode);
			loadManifestInfo(jarNode);
		}

		public Pattern getNameFilter() {
			return nameFilter;
		}

		public List<Pattern> getAttributeFilters() {
			return attributeFilters;
		}

		private void loadNameFilter(Element jarNode) throws JzrInitializationException {
			Element filterNode = ConfigUtil.getFirstChildNode(jarNode, JZR_FILTER);
			if (filterNode != null){
				String pattern = ConfigUtil.loadStringValue(filterNode, JZR_PATTERN);
				nameFilter = Pattern.compile(pattern);
			}
			else
				nameFilter = Pattern.compile(".*"); // default : any jar file
		}

		private void loadManifestInfo(Element jarNode) throws JzrInitializationException {
			Element manifestInfoNode = ConfigUtil.getFirstChildNode(jarNode, JZR_MANIFEST_INFO);
			if (manifestInfoNode == null)
				throw new JzrInitializationException("Process card manifest configuration load failed : the " + JZR_MANIFEST_INFO + " node is missing.");
			
			NodeList filterNodes = manifestInfoNode.getChildNodes();
			if (filterNodes.getLength() == 0)
				throw new JzrInitializationException("Process card manifest configuration load failed : the " + JZR_MANIFEST_INFO + " cannot be empty.");
			
			for (int i=0; i<filterNodes.getLength(); i++){
				Node filterNode = filterNodes.item(i);
				if (filterNode.getNodeType() == Node.ELEMENT_NODE){
					String name = filterNode.getNodeName();
					if (TDJarConfig.JZR_FILTER.equals(name)){
						String pattern = ConfigUtil.loadStringValue((Element)filterNode, JZR_PATTERN);
						Pattern attributeFilter = Pattern.compile(pattern);
						attributeFilters.add(attributeFilter);
					}
				}
			}
		}
	}
}
