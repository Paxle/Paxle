/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.tools.console.impl.desktop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.osgi.framework.console.ConsoleSession;

public class Session extends ConsoleSession {
    private OutputStream consoleOS;
    private InputStream consoleIS;
    
    public Session(OutputStream os, InputStream is){
    	this.consoleOS = os;
    	this.consoleIS = is;  	
    }

	@Override
	protected void doClose() {
		try {
			this.consoleIS.close();
			this.consoleOS.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public InputStream getInput() {
		return this.consoleIS;
	}

	@Override
	public OutputStream getOutput() {
		return this.consoleOS;
	}
}