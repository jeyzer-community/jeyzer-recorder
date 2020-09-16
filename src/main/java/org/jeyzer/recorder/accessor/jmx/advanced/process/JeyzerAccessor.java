package org.jeyzer.recorder.accessor.jmx.advanced.process;

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





import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.AttributeNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.jeyzer.mx.JeyzerMXBean;
import org.jeyzer.mx.event.JzrEventInfo;
import org.jeyzer.mx.event.JzrPublisherEvent;
import org.jeyzer.recorder.accessor.jmx.advanced.JzrMXBeanFieldAccessor;
import org.jeyzer.recorder.accessor.mx.advanced.process.JzrAbstractJeyzerAccessor;
import org.jeyzer.recorder.data.JeyzerEventInfo;
import org.jeyzer.recorder.data.JeyzerPublisherEvent;
import org.jeyzer.recorder.util.JMXUtil;

public class JeyzerAccessor extends JzrAbstractJeyzerAccessor implements JzrMXBeanFieldAccessor{
	
	@Override
	public boolean checkSupport(MBeanServerConnection server) {
		
		if (initialized)
			return this.isSupported();
		
		try{
			if (logger.isDebugEnabled())
				dumpJeyzerMXInfo(server);
			
			JeyzerMXBean jhmBean = createJeyzerMXBean(server);
			return checkSupport(jhmBean);
			
		}catch(Exception ex){
			logger.warn("Jeyzer MX accesss error. Jeyzer MX access disabled.");
			if (!logger.isDebugEnabled())
				logger.debug("Exception is :", ex);
			this.supported = false;
			return false;
		}
	}
	
	@Override
	public void collect(MBeanServerConnection server) {
		if (!this.isSupported())
			return;

		if (logger.isDebugEnabled())
			logger.debug("Accessing Jeyzer MX bean process info");
		
		collectApplicativeData(server);
		collectApplicativeEvents(server);
		collectPublisherEvents(server);
		
		if (logger.isDebugEnabled())
			logger.debug("Jeyzer MX bean process info access completed");
	}	

	private void collectPublisherEvents(MBeanServerConnection server) {
		long startTime = System.currentTimeMillis();
    	CompositeData[] publisherEventsCD = null;
    	
		if (logger.isDebugEnabled())
			logger.debug("Parsing Jeyzer MX bean process publisher events");
    	
		try {
			publisherEventsCD = (CompositeData[])
					server.invoke(new ObjectName(JeyzerMXBean.JEYZER_MXBEAN_NAME), 
									"consumePublisherEvents", 
									new Object[] {}, 
									new String[] {});
		} catch (Exception e) {
			logWarning("Failed to collect Jeyzer MX bean process publisher events.", e);
			return;
		}
		
    	long endTime = System.currentTimeMillis();
    	this.captureDuration = this.captureDuration + endTime - startTime;
		if (logger.isDebugEnabled()) 
			logger.debug("Jeyzer MX bean process publisher events access time : {} ms", endTime - startTime);

    	for (int i=0; i<publisherEventsCD.length; i++)
    		if (publisherEventsCD[i] != null){
    			JzrPublisherEvent event = JeyzerPublisherEvent.from(publisherEventsCD[i]);
    			publisherEvents.add(event);
    		}
	}

	private void collectApplicativeData(MBeanServerConnection server) {
		long startTime = 0;
		
		if (logger.isDebugEnabled())
			logger.debug("Parsing Jeyzer MX bean process applicative dynamic data");

		try {		
			JeyzerMXBean jhmBean = createJeyzerMXBean(server);

			startTime = System.currentTimeMillis();
			processDynamicCtxParams = JMXUtil.convertHiddenTabularDataToStringMap(jhmBean.getDynamicProcessContextParams());
		}catch(Exception ex){
			logger.error("Failed to collect Jeyzer MX bean process applicative dynamic data", ex);
		}
		
		long endTime = System.currentTimeMillis();
		this.captureDuration = this.captureDuration + endTime - startTime;
		
		if (logger.isDebugEnabled())
			logger.debug("Jeyzer MX bean process applicative dynamic data parsing time : {} ms", endTime - startTime);

	}

	private void collectApplicativeEvents(MBeanServerConnection server) {
		long startTime = System.currentTimeMillis();
    	CompositeData[] applicativeEventsCD = null;
    	
		if (logger.isDebugEnabled())
			logger.debug("Parsing Jeyzer MX bean process applicative events");
    	
		try {
			applicativeEventsCD = (CompositeData[])
					server.invoke(new ObjectName(JeyzerMXBean.JEYZER_MXBEAN_NAME), 
								"consumeEvents", 
								new Object[] {}, 
								new String[] {});
		} catch (Exception e) {
			logWarning("Failed to collect Jeyzer MX bean process applicative events.", e);
			return;
		}
		
    	long endTime = System.currentTimeMillis();
    	this.captureDuration = this.captureDuration + endTime - startTime;
		if (logger.isDebugEnabled()) 
			logger.debug("Jeyzer MX bean process applicative events access time : {} ms", endTime - startTime);

    	for (int i=0; i<applicativeEventsCD.length; i++)
    		if (applicativeEventsCD[i] != null){
    			JzrEventInfo event = JeyzerEventInfo.from(applicativeEventsCD[i]);
    			applicativeEvents.add(event);
    		}
	}

	@Override
	public long getCaptureDuration() {
		return this.captureDuration;
	}
	
	public void collectProcessCardFigures(MBeanServerConnection server) {
		JeyzerMXBean jhmBean = null;
		
		if (!this.isSupported())
			return;
		
		try {
			jhmBean = createJeyzerMXBean(server);
		}catch(Exception ex){
			logger.error("Failed to access Jeyzer process MX info", ex);
		}
		
		if(jhmBean != null){
			try {
				publisherVersion = jhmBean.getPublisherVersion();
				nodeName = jhmBean.getNodeName();
				processName = jhmBean.getProcessName();
				processVersion = jhmBean.getProcessVersion();
				processBuildNumber = jhmBean.getProcessBuildNumber();
				profileName = jhmBean.getProfileName();
				processStaticCtxParams = JMXUtil.convertHiddenTabularDataToStringMap(jhmBean.getStaticProcessContextParams());	
			} catch(UndeclaredThrowableException ex) {
				if (ex.getCause() instanceof AttributeNotFoundException)
					logger.error("Failed to read Jeyzer process MX info : API compatibility issue between the Jeyzer Publisher and the Jeyzer Recorder.", ex);
				else
					logger.error("Failed to read Jeyzer process MX info", ex);
			}
		}
	}
	
	public JeyzerMXBean createJeyzerMXBean(MBeanServerConnection server) throws IOException, MalformedObjectNameException {
		if (this.jeyzerBean!= null)
			return this.jeyzerBean; // let's cache it
		
		long startTime = 0;
		
        if (logger.isDebugEnabled()){
    		logger.debug("Creating MX bean : {}", JeyzerMXBean.JEYZER_MXBEAN_NAME);
        	startTime = System.currentTimeMillis();
        }

        this.jeyzerBean = JMX.newMBeanProxy(
        		server, 
        		new ObjectName(JeyzerMXBean.JEYZER_MXBEAN_NAME),
				JeyzerMXBean.class);
		
        if (logger.isDebugEnabled()){
        	long endTime = System.currentTimeMillis();
        	logger.debug("Jeyzer MX bean creation time : {} ms", endTime - startTime);
        }
        
        return this.jeyzerBean;
	}
	
	private void dumpJeyzerMXInfo(MBeanServerConnection server) throws IOException, MalformedObjectNameException {
		JeyzerMXBean jeyzerBean = createJeyzerMXBean(server);
		
		if (jeyzerBean.isActive()){
			logger.info("=================================================================");
			logger.info("Jeyzer MX bean info");
			logger.info("Process parameters");
			logger.info(" Node name           : "+ jeyzerBean.getNodeName());
			logger.info(" Process name        : "+ jeyzerBean.getProcessName());
			logger.info(" Process version     : "+ jeyzerBean.getProcessVersion());
			logger.info(" Profile name        : "+ jeyzerBean.getProfileName());
			logger.info(" Profile buildnumber : "+ jeyzerBean.getProcessBuildNumber());
			
			Map<String, String> staticContextParams = jeyzerBean.getStaticProcessContextParams();
			if (staticContextParams.isEmpty())
				logger.info(" Static process context list is empty.");
			else{
				Map<String, String> staticParams = JMXUtil.convertHiddenTabularDataToStringMap(staticContextParams);
				for (Entry<String, String> entry : staticParams.entrySet()){
					logger.info(" Static process context param name  : "+ entry.getKey());
					logger.info(" Static process context param value : "+ entry.getValue());
				}
			}
			
			Map<String, String> dynamicContextParams = jeyzerBean.getDynamicProcessContextParams();
			if (dynamicContextParams.isEmpty())
				logger.info(" Dynamic process context list is empty.");
			else{
				Map<String, String> dynamicParams = JMXUtil.convertHiddenTabularDataToStringMap(dynamicContextParams);
				for (Entry<String, String> entry : dynamicParams.entrySet()){
					logger.info(" Dynamic process context param name  : "+ entry.getKey());
					logger.info(" Dynamic process context param value : "+ entry.getValue());
				}
			}

	    	logger.info("=================================================================");

	    	CompositeData[] applicativeEventsCD = null;
	    	try {
				applicativeEventsCD = (CompositeData[])
						server.getAttribute(new ObjectName(JeyzerMXBean.JEYZER_MXBEAN_NAME), "Events");
			} catch (Exception e) {
				logger.info(" Failed to parse the applicative events.", e);
				return;
			}

			if (applicativeEventsCD.length == 0)
				logger.info(" Applicative events list is empty.");
			else{
		    	logger.info(" Applicative events :");
		    	for (int i=0; i<applicativeEventsCD.length; i++)
		    		if (applicativeEventsCD[i] != null){
		    			JzrEventInfo event = JeyzerEventInfo.from(applicativeEventsCD[i]);
		    			logger.info("");
		    			logger.info(" Applicative event  : ");
		    			logger.info("    source       : " + event.getSource());
		    			logger.info("    service      : " + event.getService());
		    			logger.info("    type         : " + event.getCode().getType());
						logger.info("    abbreviation : " + event.getCode().getAbbreviation());
						logger.info("    name         : " + event.getCode().getName());
						logger.info("    description  : " + event.getCode().getDescription());
						logger.info("    level        : " + event.getCode().getLevel().name());
						logger.info("    sub level    : " + event.getCode().getSubLevel().name());
						logger.info("    scope        : " + event.getScope().name());
						logger.info("    message      : " + event.getMessage());
						logger.info("    id           : " + event.getId());
						logger.info("    start time   : " + event.getStartTime());
						logger.info("    end time     : " + event.getEndTime());
						logger.info("    trust factor : " + event.getTrustFactor());
						logger.info("    thread id    : " + event.getThreadId());
						logger.info("    oneshot      : " + event.isOneshot());
					}
	    		}
			
			logger.info("=================================================================");

	    	CompositeData[] publisherEventsCD = null;
	    	try {
				publisherEventsCD = (CompositeData[])
						server.getAttribute(new ObjectName(JeyzerMXBean.JEYZER_MXBEAN_NAME), "PublisherEvents");
			} catch (Exception e) {
				logger.info(" Failed to parse the publisher events.", e);
				return;
			}

	    	if (publisherEventsCD.length == 0)
				logger.info(" Publisher events list is empty.");
			else{
		    	logger.info(" Publisher events  : ");
		    	for (int i=0; i<publisherEventsCD.length; i++)
		    		if (publisherEventsCD[i] != null){
		    			JzrPublisherEvent event = JeyzerPublisherEvent.from(publisherEventsCD[i]);
		    			logger.info("");
		    			logger.info(" Publisher event  : ");
						logger.info("    code      : " + event.getCode().getDisplayValue());
						logger.info("    level     : " + event.getLevel().name());
						logger.info("    sub level : " + event.getSubLevel().name());
						logger.info("    message   : " + event.getMessage());
						logger.info("    time      : " + event.getTime());
					}
	    		}
			
			// do not log thread
			logger.info("=================================================================");
		}
		else{
			logger.info("=================================================================");
			logger.info("Jeyzer MX bean is inactive");
			logger.info("=================================================================");
		}
	}

}
