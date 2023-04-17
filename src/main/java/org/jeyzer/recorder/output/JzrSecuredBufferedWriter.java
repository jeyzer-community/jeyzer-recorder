package org.jeyzer.recorder.output;

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


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public final class JzrSecuredBufferedWriter extends BufferedWriter {
	
	public JzrSecuredBufferedWriter(Writer out) {
		super(out);
	}

	public JzrSecuredBufferedWriter(Writer out, int sz) {
		super(out, sz);
	}
	
	@Override
    public void newLine() throws IOException {
		super.newLine();
		super.flush();
	}
	
	@Override
	public Writer append(char c) throws IOException {
		super.append(c);
		super.flush();
		return this;
	}
	
	@Override
    public Writer append(CharSequence csq) throws IOException {
		super.append(csq);
		super.flush();
		return this;
    }

	@Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
		super.append(csq, start, end);
        super.flush();
        return this;
    }

	@Override
	public void write(char cbuf[]) throws IOException {
		super.write(cbuf);
        super.flush();
	}
	
	@Override
	public void write(int c) throws IOException {
		super.write(c);
        super.flush();
	}

	@Override
	public void write(String str) throws IOException {
		super.write(str);
        super.flush(); // flush must be done every time because lost otherwise in the output stream writer (required for gzip ?)
	}
	
	@Override
	public void write(String str, int off, int len) throws IOException {
		super.write(str, off, len);
        super.flush();
	}
	
	@Override
	public void write(char cbuf[], int off, int len) throws IOException {
		super.write(cbuf, off, len);
        super.flush();
	}
}
