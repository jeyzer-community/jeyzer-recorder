package org.jeyzer.recorder.config.jstack;

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


import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.util.ConfigUtil;
import org.w3c.dom.Element;

public class JzrJstackInShellConfig extends JzrRecorderConfig {
	
	private static final String JZR_JSTACK_IN_SHELL = "jstack_in_shell";
	private static final String JZR_PID = "pid";
	private static final String JZR_OPTIONS = "options";
	
	private int pid;
	private String options;
	
	public JzrJstackInShellConfig(Element recorder) throws Exception {
		super(recorder);
		
		// snapshot node
		Element snapshotNode = ConfigUtil.getFirstChildNode(recorder, JZR_SNAPSHOT);

		// methods node
		Element methodsNode = ConfigUtil.getFirstChildNode(snapshotNode, JZR_METHODS);
		
		// jstack node
		Element jstackNode = ConfigUtil.getFirstChildNode(methodsNode, JZR_JSTACK_IN_SHELL);
		
		this.pid = ConfigUtil.loadIntegerValue(jstackNode, JZR_PID);
		this.options = ConfigUtil.loadStringValue(jstackNode, JZR_OPTIONS);
	}
	
	@Override
	public boolean isEncryptionEnabled() {
		return false;
	}
	
	@Override
	public boolean isEncryptionKeyPublished() {
		return false;
	}
	
	public int getPid() {
		return pid;
	}

	public String getOptions() {
		return options;
	}
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder(); 
		
		b.append(super.toString());
	    b.append("\tMonitored pid             : " + pid+ "\n");
	    b.append("\tJstack options            : " + options+ "\n");
	    
	    return b.toString();		
	}
}
