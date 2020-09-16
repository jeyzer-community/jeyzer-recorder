package org.jeyzer.recorder.accessor.local.advanced.process;

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





import java.lang.management.ManagementFactory;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jeyzer.mx.JeyzerMXBean;
import org.jeyzer.recorder.accessor.local.advanced.JzrLocalBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractJeyzerAccessor;

public class LocalJeyzerAccessor extends JzrAbstractJeyzerAccessor implements JzrLocalBeanFieldAccessor{

	@Override
	public boolean checkSupport() {
	
		if (initialized)
			return this.isSupported();
		
		try{
			return this.checkSupport(getJeyzerMXBean());
		}catch(Exception ex){
			logger.warn("Monitored JVM doesn't provide Jeyzer process info. Failed to access the Jeyzer process info");
			if (!logger.isDebugEnabled())
				logger.debug("Exception is :", ex);
			this.supported = false;
		}
		 
		return false;
	}
	
	public JeyzerMXBean getJeyzerMXBean(){
		if (this.jeyzerBean!= null)
			return this.jeyzerBean; // let's cache it
		
		ObjectName mxbeanName = null;
		
		try {
			mxbeanName = new ObjectName(JeyzerMXBean.JEYZER_MXBEAN_NAME);
		} catch (MalformedObjectNameException e) {
			logger.error("Failed to instanciate object " + JeyzerMXBean.JEYZER_MXBEAN_NAME + ". Error is : " + e.getMessage());
		}
		
		MBeanServer localServer = ManagementFactory.getPlatformMBeanServer();
		
		this.jeyzerBean = JMX.newMXBeanProxy(localServer, mxbeanName, JeyzerMXBean.class);
		
		return this.jeyzerBean;
	}

	@Override
	public void collect() {
		if (!isSupported())
			return;
		
		collectApplicativeData();
		collectApplicativeEvents();
		collectPublisherEvents();
	}

	private void collectApplicativeData() {
		try {
			processDynamicCtxParams = this.jeyzerBean.getDynamicProcessContextParams();
		} catch (Exception e) {
			logger.error("Failed to access Jeyzer MX bean process applicative dynamic data", e);
		}
	}
	
	private void collectApplicativeEvents() {
		try {
			applicativeEvents = this.jeyzerBean.consumeEvents();
		} catch (Exception e) {
			logger.error("Failed to consume the Jeyzer MX bean process applicative events", e);
		}
	}
	
	private void collectPublisherEvents() {
		try {
			publisherEvents = this.jeyzerBean.consumePublisherEvents();
		} catch (Exception e) {
			logger.error("Failed to consume the Jeyzer MX bean process publisher events", e);
		}
	}

	public void collectProcessCardFigures() {
		JeyzerMXBean jhmBean = null;
		
		if (!this.isSupported())
			return;
		
		try {
			jhmBean = getJeyzerMXBean();
		}catch(Exception ex){
			logger.error("Failed to access Jeyzer MX bean", ex);
		}
		
		if(jhmBean != null){
			publisherVersion = jhmBean.getPublisherVersion();
			nodeName = jhmBean.getNodeName();
			processName = jhmBean.getProcessName();
			processVersion = jhmBean.getProcessVersion();
			processBuildNumber = jhmBean.getProcessBuildNumber();
			profileName = jhmBean.getProfileName();
			processStaticCtxParams = jhmBean.getStaticProcessContextParams();
		}
	}
}
