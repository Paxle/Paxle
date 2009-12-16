/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.activation.DataSource;

/**
 * A helper class to stream a {@link Serializable} object via a {@link DataSource}
 */
class JaxbSerializableDataSource implements DataSource {
	private final String name;
	private final Serializable value;
	
	public JaxbSerializableDataSource(String name, Serializable value) {
		this.name = name;
		this.value = value;
	}
	
    public String getContentType() {
    	return "application/x-java-serialized-object";
    }

    public InputStream getInputStream() throws IOException {
    	ObjectOutputStream oout = null;
    	try {
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			oout = new ObjectOutputStream(bout);
			oout.writeObject(this.value);
			oout.close();
			
	        return new ByteArrayInputStream(bout.toByteArray());
    	} finally {
    		if (oout != null) oout.close();
    	}
    }

    public String getName() {
        return "IDOC: " + this.name;
    }

    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }	
}