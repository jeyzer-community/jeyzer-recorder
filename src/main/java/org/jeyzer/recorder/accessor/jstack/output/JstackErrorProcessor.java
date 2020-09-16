package org.jeyzer.recorder.accessor.jstack.output;

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





import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JstackErrorProcessor implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(JstackErrorProcessor.class);	
	
	private InputStream es;
	
	public JstackErrorProcessor(InputStream es) {
		this.es = es;
	}

	@Override
	public void run() {
        try (
                InputStreamReader esr = new InputStreamReader(es);
                BufferedReader br = new BufferedReader(esr);
        	)
        {
            String line=null;
            while ( (line = br.readLine()) != null)
            {
           		logger.error(line);
            }
        } catch (IOException ioe){
        	logger.error("Failed to write process error message in current log file.");  
        }
	}

}
