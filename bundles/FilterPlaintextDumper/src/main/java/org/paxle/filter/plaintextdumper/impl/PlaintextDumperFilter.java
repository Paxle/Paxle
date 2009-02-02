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
package org.paxle.filter.plaintextdumper.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.io.IOTools;
import org.paxle.core.queue.ICommand;

public class PlaintextDumperFilter implements IFilter<ICommand> {
	/**
	 * Path where the data should be stored
	 */
	private final File dataDir;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());

	public PlaintextDumperFilter(File dir) {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		this.dataDir = dir;
	}

	public File store(IParserDocument pDoc) throws IOException {
		
		Map<String,IParserDocument> subDocs = pDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.store(subDoc);
			}
		}
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		File targetFile = null;
		
		try {
			if (pDoc.getStatus() != IParserDocument.Status.OK) return null;
			
			// getting the source file
			final File sourceFile = pDoc.getTextFile();
			if (sourceFile == null || sourceFile.length() == 0) return null;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			
			// creating the target file			
			targetFile = File.createTempFile("datadumper-", ".txt", this.dataDir);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile),"UTF-8"));
			
			// copy files
			IOTools.copy(br, bw);
		} finally {
            if (br != null) try { br.close(); } catch (Exception e) {/* ignore this */}
            if (bw != null) try { bw.close(); } catch (Exception e) {/* ignore this */}	
		}
		return targetFile;
	}
	
	public void filter(ICommand command, IFilterContext context) {
		if (command == null) throw new NullPointerException("The command object is null.");
		if (command.getResult() != ICommand.Result.Passed) return;
		if (command.getParserDocument() == null) return;
		
		try {
			this.store(command.getParserDocument());
		} catch (Throwable e) {
			this.logger.error(String.format(
					"Unexpected %s while dumping plain-text of URI '%s' into file.",
					e.getClass().getName(),
					command.getLocation().toString()
			),e);
		}
	}

}
