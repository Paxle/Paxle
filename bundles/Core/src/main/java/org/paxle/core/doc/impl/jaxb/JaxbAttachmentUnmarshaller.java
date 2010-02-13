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

package org.paxle.core.doc.impl.jaxb;

import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JaxbAttachmentUnmarshaller extends AttachmentUnmarshaller {
	private final Map<String, DataHandler> attachments;
	
	private final Log logger = LogFactory.getLog(this.getClass());
	
	public JaxbAttachmentUnmarshaller(Map<String, DataHandler> attachments) {
		this.attachments = attachments;
	}

	@Override
	public boolean isXOPPackage() {
		return true;
	}
	
	@Override
	public byte[] getAttachmentAsByteArray(String cid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DataHandler getAttachmentAsDataHandler(String cid) {
		if (this.attachments == null) return null;
		final DataHandler handler =  attachments.get(cid);
		if (handler == null) {
			this.logger.error(String.format(
				"Unable to find the attachment with id '%s'.", cid
			));
		}
		return handler;
	}

}
