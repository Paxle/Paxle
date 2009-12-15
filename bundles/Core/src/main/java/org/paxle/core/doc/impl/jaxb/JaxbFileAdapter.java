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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;

/**
 * This is a helper-class required to convert the {@link File files} of an 
 * {@link ICrawlerDocument}, {@link IParserDocument} or {@link IIndexerDocument} into
 * a {@link DataSource}, which can be serialized by Jaxb.  
 */
public class JaxbFileAdapter extends XmlAdapter<DataHandler, File> {
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The temp-file-manager used to create a temp-file
	 */
	private ITempFileManager tempFileManager;
	
	public JaxbFileAdapter(ITempFileManager tempFileManager) {
		this.tempFileManager = tempFileManager;
	}

	/**
	 * Converts the {@link File} into a {@link DataHandler}
	 */
	@Override
	public DataHandler marshal(File cDocFile) throws Exception {
		if (cDocFile == null) return null;
		return new DataHandler(new FileDataSource(cDocFile));
	}

	/**
	 * Converts the {@link DataHandler} into a {@link File}.
	 * The file is created via the {@link ITempFileManager}.
	 */
	@Override
	public File unmarshal(DataHandler dataHandler) throws Exception {
		if (dataHandler == null) return null;
		
		final DataSource dataSource = dataHandler.getDataSource();
		if (dataSource != null) {
			File tempFile = null;
			InputStream input = null;
			OutputStream output = null;
			
			try {
				// getting the input stream
				input = dataSource.getInputStream();
				
				// getting the output stream
				tempFile = this.tempFileManager.createTempFile();
				output = new BufferedOutputStream(new FileOutputStream(tempFile));
				
				// copy data
				long byteCount = IOUtils.copy(input, output);
				if (this.logger.isDebugEnabled()) {
					this.logger.debug(String.format(
						"%d bytes copied from the data-source into the temp-file '%s'.",
						Long.valueOf(byteCount),
						tempFile.getName()
					));
				}
				return tempFile;
			} catch (IOException e) {
				// delete the temp file on errors
				if (tempFile != null && this.tempFileManager.isKnown(tempFile)) {
					this.tempFileManager.releaseTempFile(tempFile);
				}
				
				// re-throw exception
				throw e;
			} finally {
				// closing streams
				if (input != null) input.close();
				if (output != null) output.close();
			}
		}
		return null;
	}

}
