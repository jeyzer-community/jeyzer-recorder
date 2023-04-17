package org.jeyzer.recorder.config.mx.advanced;

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





public class JzrBeanFieldConfig {

	private String name;
	private String category;
	
	public JzrBeanFieldConfig(String category, String name) {
		this.category = category;
		this.name = name;
	}

	public String getName(){
		return name;
	}
	
	public String getCategory(){
		return category;
	}	
	
}
