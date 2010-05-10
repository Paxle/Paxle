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

package org.paxle.core.doc.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.impl.jaxb.JaxbAttachmentMarshaller;
import org.paxle.core.doc.impl.jaxb.JaxbAttachmentUnmarshaller;
import org.paxle.core.doc.impl.jaxb.JaxbDocAdapter;
import org.paxle.core.doc.impl.jaxb.JaxbFactory;
import org.paxle.core.doc.impl.jaxb.JaxbFieldMapAdapter;
import org.paxle.core.doc.impl.jaxb.JaxbFileAdapter;
import org.paxle.core.io.temp.ITempFileManager;

@Component(immediate=true, metatype=false)
@Service(IDocumentFactory.class)
@Property(
	name = IDocumentFactory.DOCUMENT_TYPE,
	value = {
		"org.paxle.core.doc.ICommand",
		"org.paxle.core.doc.ICommandProfile",
		"org.paxle.core.doc.ICrawlerDocument",
		"org.paxle.core.doc.IParserDocument",
		"org.paxle.core.doc.IIndexerDocument"
	}
)
public class BasicDocumentFactory implements IDocumentFactory {
	/**
	 * A list of documents that this {@link IDocumentFactory} is capable to create.
	 */
	@SuppressWarnings("serial")
	protected static final HashSet<Class<?>> SUPPORTED_CLASSES =  new HashSet<Class<?>>() {{
		// all supported interfaces
		add(ICrawlerDocument.class);
		add(IParserDocument.class);
		add(IIndexerDocument.class);
		add(ICommand.class);
		add(ICommandProfile.class);
		
		// all supported classes
		add(BasicCommand.class);
		add(BasicCrawlerDocument.class);
		add(CachedParserDocument.class);
		add(BasicParserDocument.class);
		add(BasicIndexerDocument.class);
		add(BasicCommandProfile.class);
	}};
	
	/**
	 * For logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());
	
	@Reference
	protected ITempFileManager tempFileManager;

	protected JAXBContext context;
	
	@Activate
	protected void activate(Map<String, Object> props) throws JAXBException {
		// init the jaxb factory
		JaxbFactory.setDocumentFactory(this);
		this.logger.info(this.getClass().getSimpleName() + " registered.");
				
		// init the jaxb context
		final ClassLoader loader = this.getClass().getClassLoader();
		context = JAXBContext.newInstance(
			"org.paxle.core.doc.impl",
			loader
		);
		
		this.logger.info(this.getClass().getSimpleName() + " activated.");
	}
	
	@SuppressWarnings("unchecked")
	public <Doc> Doc createDocument(Class<Doc> docInterface) throws IOException {
		if (docInterface == null) throw new NullPointerException("The document-interface must not be null.");
		else if (!SUPPORTED_CLASSES.contains(docInterface)) throw new IllegalArgumentException("Unsupported doc-type");

		// determine the implementation class to use
		// TODO: we could make this configurable
		Class<?> implClass = null;
		if (docInterface.equals(ICrawlerDocument.class)) {
			implClass = BasicCrawlerDocument.class;
		} else if (docInterface.equals(IParserDocument.class)) {
			implClass = CachedParserDocument.class;
		} else if (docInterface.equals(IIndexerDocument.class)) {
			implClass = BasicIndexerDocument.class;
		} else if (docInterface.equals(ICommand.class)) {
			implClass = BasicCommand.class;
		} else if (docInterface.equals(ICommandProfile.class)) {
			implClass = BasicCommandProfile.class;
		} else {
			implClass = docInterface;
		}
		
		// instantiate the class
		if (implClass.equals(BasicCrawlerDocument.class)) {		
			return (Doc) new BasicCrawlerDocument();
		} else if (implClass.equals(BasicParserDocument.class)) {
			return (Doc) new BasicParserDocument(this.tempFileManager);
		} else if (implClass.equals(CachedParserDocument.class)) {
			return (Doc) new CachedParserDocument(this.tempFileManager);
		} else if (implClass.equals(BasicIndexerDocument.class)) {
			return (Doc) new BasicIndexerDocument();
		} else if (implClass.equals(BasicCommand.class)) {
			return (Doc) new BasicCommand();
		} else if (implClass.equals(BasicCommandProfile.class)) {
			return (Doc) new BasicCommandProfile();
		}		
		throw new IllegalArgumentException("Unexpected doc-type");		
	}

	public boolean isSupported(Class<?> docInterface) {
		if (docInterface == null) return false;
		return SUPPORTED_CLASSES.contains(docInterface);
	}

	public <Doc> Map<String, DataHandler> marshal(Doc document, OutputStream output) throws IOException {
		try {

			final JaxbAttachmentMarshaller am = new JaxbAttachmentMarshaller();
			final Marshaller m = context.createMarshaller();
			
			m.setAdapter(JaxbDocAdapter.class,new JaxbDocAdapter());
			m.setAdapter(JaxbFileAdapter.class, new JaxbFileAdapter(this.tempFileManager));
			m.setAdapter(JaxbFieldMapAdapter.class, new JaxbFieldMapAdapter(this.tempFileManager));
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//			m.setProperty("com.sun.xml.bind.ObjectFactory", new BasicJaxbFactory());
			m.setAttachmentMarshaller(am);
			
			m.marshal(document, output);			
			output.flush();
			
			return am.getAttachmentsMap();
		} catch (JAXBException e) {
			final IOException ioe = new IOException(String.format(
					"Unable to marshal the document '%s'.",
					document.getClass().getName()
			));
			ioe.initCause(e);
			throw ioe;
		}
	}

	
	public <Doc> Doc unmarshal(InputStream input, Map<String, DataHandler> attachments) throws IOException  {
		try {
			final Unmarshaller u = context.createUnmarshaller();
			u.setAdapter(JaxbFileAdapter.class, new JaxbFileAdapter(this.tempFileManager, attachments));
			u.setAdapter(JaxbFieldMapAdapter.class, new JaxbFieldMapAdapter(this.tempFileManager));
			u.setAttachmentUnmarshaller(new JaxbAttachmentUnmarshaller(attachments));
//			u.setProperty("com.sun.xml.bind.ObjectFactory", new BasicJaxbFactory());
			
			@SuppressWarnings("unchecked")
			final Doc document = (Doc) u.unmarshal(input);
			return document;
		} catch (JAXBException e) {
			final IOException ioe = new IOException(String.format(
					"Unable to unmarshal the document from the stream."
			));
			ioe.initCause(e);
			throw ioe;
		}
	}
	
//	public class BasicJaxbFactory {
//		public BasicCommand createBasicCommand() throws IOException {
//			return createDocument(BasicCommand.class);
//		}
//		
//		public BasicCrawlerDocument createBasicCrawlerDocument() throws IOException {
//			return createDocument(BasicCrawlerDocument.class);
//		}
//		
//		public BasicParserDocument createBasicParserDocument()  throws IOException {
//			return createDocument(BasicParserDocument.class);
//		}
//		
//		public CachedParserDocument createCachedParserDocument() throws IOException {
//			return createDocument(CachedParserDocument.class);
//		}
//		
//		public BasicIndexerDocument createBasicIndexerDocument()  throws IOException {
//			return createDocument(BasicIndexerDocument.class);
//		}			
//	}
}
