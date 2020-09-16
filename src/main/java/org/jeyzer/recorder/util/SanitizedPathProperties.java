package org.jeyzer.recorder.util;

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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class SanitizedPathProperties extends Properties {

	private static final long serialVersionUID = -7802777138945747196L;

	public void loadAndSanitizePaths(FileInputStream fis) throws IOException {
		if (SystemHelper.isWindows()) {
			try (
					Scanner in = new Scanner(fis);
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
				)
			{
                while(in.hasNext()) {
                	// double escape \ -> \\\\ as properties may be re-injected as system properties
                    out.write(in.nextLine().replace("\\","\\\\").getBytes());
                    out.write("\n".getBytes());
                }
                try (
            			InputStream is = new ByteArrayInputStream(out.toByteArray());
                	)
                {
        			super.load(is);
                }
			}
		}
		else{
			super.load(fis);
		}
    }
}
