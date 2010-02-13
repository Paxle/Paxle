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

package org.paxle.desktop.backend.impl.jre6;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.paxle.desktop.backend.desktop.IDesktop;

public class Desktop implements IDesktop {
	
	private static java.awt.Desktop desktop = null;
	
	public Desktop() {
		desktop = java.awt.Desktop.getDesktop();
	}
	
	public boolean browse(String url) throws MalformedURLException {
		return browse(new URL(url));
	}
	
	public boolean browse(URL url) {
		try {
			desktop.browse(url.toURI());
		} catch (URISyntaxException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
