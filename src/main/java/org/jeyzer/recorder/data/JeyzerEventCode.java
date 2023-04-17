package org.jeyzer.recorder.data;

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


import javax.management.openmbean.CompositeData;

import org.jeyzer.mx.event.JzrEventCode;
import org.jeyzer.mx.event.JzrEventLevel;
import org.jeyzer.mx.event.JzrEventSubLevel;

public class JeyzerEventCode implements JzrEventCode{

	public static final String MX_FIELD_NAME_TYPE = "type";
	public static final String MX_FIELD_NAME_NAME = "name";
	public static final String MX_FIELD_NAME_ABBREVIATION = "abbreviation";
	public static final String MX_FIELD_NAME_DESCRIPTION = "description";
	public static final String MX_FIELD_NAME_TICKET = "ticket";
	public static final String MX_FIELD_NAME_LEVEL = "level";
	public static final String MX_FIELD_NAME_SUB_LEVEL = "subLevel";

	private String type;
	private String name;
	private String abbreviation;
	private String description;
	private String ticket;
	private JzrEventLevel level;
	private JzrEventSubLevel subLevel;	
	
	@Override
	public String getAbbreviation() {
		return abbreviation;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public String getTicket() {
		return ticket;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public JzrEventLevel getLevel() {
		return level;
	}

	@Override
	public JzrEventSubLevel getSubLevel() {
		return subLevel;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public void setLevel(JzrEventLevel level) {
		this.level = level;
	}

	public void setSubLevel(JzrEventSubLevel subLevel) {
		this.subLevel = subLevel;
	}
	
	static public JeyzerEventCode from(CompositeData compositeData) {
		JeyzerEventCode code = new JeyzerEventCode();
		
		code.setType((String)compositeData.get(MX_FIELD_NAME_TYPE));
		code.setAbbreviation((String)compositeData.get(MX_FIELD_NAME_ABBREVIATION));
		code.setName((String)compositeData.get(MX_FIELD_NAME_NAME));
		code.setDescription((String)compositeData.get(MX_FIELD_NAME_DESCRIPTION));
		code.setTicket((String)compositeData.get(MX_FIELD_NAME_TICKET));
		code.setLevel(JzrEventLevel.valueOf((String)compositeData.get(MX_FIELD_NAME_LEVEL)));
		code.setSubLevel(JzrEventSubLevel.valueOf((String)compositeData.get(MX_FIELD_NAME_SUB_LEVEL)));
		
		return code;
	}
}
