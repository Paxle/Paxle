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
package org.paxle.core.doc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.activation.DataHandler;
import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;

public interface IDocumentFactory {
	public static final String DOCUMENT_TYPE = "docType";
	
	/**
	 * A function to create a new document of the given Type
	 * @param docInterface the interface of the class that should be created
	 * @return a newly created document of the given type
	 * @throws IOException
	 */
	public <Doc> Doc createDocument(@Nonnull Class<Doc> docInterface) throws IOException;
	
	/**
	 * @param docInterface
	 * @return <code>true</code> if this {@link IDocumentFactory} is capable to create a document
	 * of the given type
	 */
	public boolean isSupported(@Nonnull Class<?> docInterface);
	
	/**
	 * TODO: 
	 * 
	 * @param document the document that should be marshalled
	 * @param output an {@link OutputStream} where the XML should be written to
	 * @return a {@link Map} of attachments, representing the binary data of the serialized document. 
	 * 		The key of the {@link Map} is an unique content-id. 
	 * The value is a {@link DataHandler} that can be used to read the attachment content. 
	 * @throws IOException
	 * @since 0.1.43-SNAPSHOT
	 */
	public <Doc> Map<String, DataHandler> marshal(@Nonnull Doc document, @WillNotClose OutputStream output) throws IOException;

	/**
	 * TODO: 
	 * 
	 * @param input
	 * @param attachments a {@link Map} of attachments, representing the binary data of the serialized document. 
	 * 		The key of the {@link Map} is an unique content-id. 
	 * 		The value is a {@link DataHandler} that can be used to read the attachment content. 
	 * @return the document that was read from stream and build with the given attachments
	 * @throws IOException
	 * @since 0.1.43-SNAPSHOT
	 */
	public <Doc> Doc unmarshal(@WillNotClose InputStream input, Map<String, DataHandler> attachments) throws IOException;
}
