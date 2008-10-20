/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.core.io.temp.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.paxle.core.io.temp.ITempDir;

public abstract class ATempDir implements ITempDir {
	
	protected static final AtomicInteger num = new AtomicInteger(0);
	protected static int numLength = 3;
	
	protected final String prefix;
	
	public ATempDir(String prefix) {
		this.prefix = prefix;
	}
	
	public void releaseTempFile(File file) throws FileNotFoundException, IOException {
		file.delete();
	}
	
	protected String generateNewName(String prefix, String suffix) {
		final StringBuilder sb = new StringBuilder();
		if (this.prefix != null)
			sb.append(this.prefix).append('_');
		if (prefix != null)
			sb.append(prefix).append('_');
		sb.append(formatUID(generateNewUID()));
		if (suffix != null)
			sb.append('.').append(suffix);
		return sb.toString();
	}
	
	protected String formatUID(int uid) {
		return String.format("%" + numLength + "d", Integer.valueOf(uid));
	}
	
	protected synchronized int generateNewUID() {
		return num.getAndIncrement();
	}
}
