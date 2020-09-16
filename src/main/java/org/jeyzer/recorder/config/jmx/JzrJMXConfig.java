package org.jeyzer.recorder.config.jmx;

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





import java.util.HashMap;
import java.util.Map;

import org.jeyzer.recorder.config.JzrRecorderConfig;
import org.jeyzer.recorder.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class JzrJMXConfig extends JzrRecorderConfig{

	private static final Logger logger = LoggerFactory.getLogger(JzrJMXConfig.class);
	
	private static final String JZR_JMX = "jmx";
	private static final String JZR_CONNECTION = "connection";
	private static final String JZR_USER = "user";
	private static final String JZR_PASSWORD = "password";
	
	private static final String JZR_CAPTURE_DEADLOCKS = "capture_deadlocks";

	private String user;
	private String password;
	private String host;
	private int port;
	
	private boolean captureDeadlocks = false;
	
	public JzrJMXConfig(Element recorder) throws Exception {
		super(recorder);
		
		// snapshot node
		Element snapshotNode = ConfigUtil.getFirstChildNode(recorder, JZR_SNAPSHOT);

		// methods node
		Element methodsNode = ConfigUtil.getFirstChildNode(snapshotNode, JZR_METHODS);
		
		// jmx node
		Element jmxNode = ConfigUtil.getFirstChildNode(methodsNode, JZR_JMX);
		
		String connect = ConfigUtil.loadStringValue(jmxNode, JZR_CONNECTION);
		
		parseConnectString(connect);
		
		user = ConfigUtil.loadStringValue(jmxNode, JZR_USER);
		
		password = ConfigUtil.loadStringValue(jmxNode, JZR_PASSWORD);
		
		captureDeadlocks = Boolean.parseBoolean(ConfigUtil.loadStringValue(jmxNode, JZR_CAPTURE_DEADLOCKS));
	}
	
	@Override
	public boolean isEncryptionEnabled() {
		return false;
	}
	
	@Override
	public boolean isEncryptionKeyPublished() {
		return false;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean isDeadlockCaptureEnabled(){
		return this.captureDeadlocks;
	}
	
	public Map<String, Object> getEnvironment(){
		Map<String, Object> environment = new HashMap<>(); 
		
	    if (user!= null && password!= null){
	    	String[] credentials = new String[] {user , password }; 
	    	environment.put("jmx.remote.credentials", credentials);
	    }
		
		return environment;
	}
	
	public String getConnectionUrl(){
		return "/jndi/rmi://" + host + ":" + port + "/jmxrmi";		
	}
	
	private void parseConnectString(String connect) throws Exception {
		int idx = connect.indexOf(':');
		if (idx == -1) {
			logger.error("Configuration error - Invalid {} parameter : {}", JZR_CONNECTION, connect);
			throw new Exception("Configuration error - Invalid " + JZR_CONNECTION + " archiving parameter : " + connect + ". Value must follow <host>:<port> pattern.");
		}

		host = connect.substring(0,idx);
		String portValue = connect.substring(idx+1);

		if (host == null || host.isEmpty() 
				|| portValue == null || portValue.isEmpty()) {
			logger.error("Configuration error - Invalid {} parameter : {}", JZR_CONNECTION, connect);
			throw new Exception("Configuration error - Invalid " + JZR_CONNECTION + " archiving parameter : " + connect + ". Invalid or missing value.");
		}
		
		try {
			port = Integer.parseInt(portValue);
		} catch (NumberFormatException x) {
			logger.error("Configuration error - Invalid port parameter : {}", port);
			throw new Exception("Configuration error - Invalid port parameter" + port, x);
		}
		if (port < 1 || port > 65535) {
			logger.error("Error - Invalid port parameter. Must be between 1 and 65535.");
			throw new Exception("Error - Invalid port parameter. Must be between 1 and 65535.");
		}
		
	}
	
	public String toString(){
		StringBuilder b = new StringBuilder(); 
		
		b.append(super.toString());
	    b.append("\tMonitored process        : " + host + ":" + port + "\n");
	    if (user!= null)
	    	b.append("\tConnection user          : " + user + "\n");
	    
	    return b.toString();
	}	
}
