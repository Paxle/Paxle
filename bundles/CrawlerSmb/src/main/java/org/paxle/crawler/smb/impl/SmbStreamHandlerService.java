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

package org.paxle.crawler.smb.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import jcifs.smb.SmbFile;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

@Component(metatype=false)
@Service(URLStreamHandlerService.class)
@Property(name=URLConstants.URL_HANDLER_PROTOCOL,value="smb")
public class SmbStreamHandlerService extends AbstractURLStreamHandlerService implements URLStreamHandlerService  {

	@Override
	public URLConnection openConnection(URL url) throws IOException {
		return new SmbFile(url);
	}

	@Override
	public int getDefaultPort() {
		return 445;
	}
}
