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

package org.paxle.parser;

import java.io.IOException;

import org.osgi.framework.InvalidSyntaxException;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IIOTools;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.norm.IReferenceNormalizer;

public interface IParserContext {

	/**
	 * @param mimeType the mime-type
	 * @return a {@link ISubParser parser} that is capable to parse a resource with the given mimeType
	 */
	public ISubParser getParser(String mimeType);
	
	/**
	 * @return a class that can be used to detect the mime-type of a file. 
	 * This reference may be <code>null</code> if no {@link IMimeTypeDetector mime-type-detector} is available.
	 */
	public IMimeTypeDetector getMimeTypeDetector();
	
	/**
	 * @return a class that can be used to detect the charset of a resource
	 * This reference may be <code>null</code> if no {@link ICharsetDetector charset-detector} is available.
	 */
	public ICharsetDetector getCharsetDetector();
	
	public ITempFileManager getTempFileManager();
	
	public IIOTools getIoTools();
	
	public IReferenceNormalizer getReferenceNormalizer();
		
	/**
	 * TODO: currently this is an read-only {@link ICommandProfile}. We should wrap it with a transparent proxy
	 * and should flush it back to db if one of the command-profile-properties were changed.
	 */
	public ICommandProfile getCommandProfile(int profileID);
	
	/**
	 * @return the {@link ICommandProfile} that belongs to the {@link ICommand}
	 * currently processed by the parser-worker thread
	 */
	public ICommandProfile getCommandProfile();
	
	/**
	 * A function to create a new and empty {@link IParserDocument} via 
	 * one of the {@link IDocumentFactory document-factories} registered to the system. 
	 * @return a newly created {@link IParserDocument}
	 */
	public IParserDocument createDocument() throws IOException;
	
	/**
	 * A function to create a new and empty document of the given type. The second arguments can be used
	 * to select between different {@link IDocumentFactory document-factories} generating the same type.
	 * 
	 * @param docInterface that interface of the document to be created
	 * @param props
	 * @return a newly created document of the given type
	 */
	public <DocInterface> DocInterface createDocument(Class<DocInterface> docInterface, String filter) throws InvalidSyntaxException, IOException;	
	
	/* ========================================================================
	 * Function operating on the property bag
	 * ======================================================================== */
	
	public Object getProperty(String name);
	
	public void setProperty(String name, Object value);
	
	public void removeProperty(String name);
	
	public void reset();
}
