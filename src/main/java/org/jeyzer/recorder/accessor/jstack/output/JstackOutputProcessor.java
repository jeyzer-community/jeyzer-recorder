package org.jeyzer.recorder.accessor.jstack.output;

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





import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jeyzer.recorder.logger.Logger;
import org.jeyzer.recorder.logger.LoggerFactory;

public class JstackOutputProcessor implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(JstackOutputProcessor.class);	
	
	private InputStream is;
	private BufferedWriter writer;
	private String name;
	
	public JstackOutputProcessor(InputStream is, BufferedWriter writer, String name) {
		this.is = is;
		this.writer = writer;
		this.name = name;
	}

	@Override
	public void run() {
        try (
                InputStreamReader isr = new InputStreamReader(is);
        		BufferedReader br = new BufferedReader(isr);
        	)
        {
            String line=null;
            while ( (line = br.readLine()) != null)
            {
            	writer.write(line);
            	writer.newLine();
            }
            writer.flush();
        } catch (IOException ioe){
        	logger.error("Failed to write " + name + ".", ioe);  
		} catch (Exception e){
			logger.error("Failed to write " + name + ". Unexpected exception.", e);  
		}
	}
}
