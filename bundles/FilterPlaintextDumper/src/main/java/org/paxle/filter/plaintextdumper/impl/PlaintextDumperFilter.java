package org.paxle.filter.plaintextdumper.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

public class PlaintextDumperFilter implements IFilter {
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

	public File store(IParserDocument pDoc) {
		
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
			if (pDoc.getStatus() != IParserDocument.Status.OK) return targetFile;
			
			// getting source file, create target
			br = new BufferedReader(new InputStreamReader(new FileInputStream(pDoc.getTextFile()), "UTF-8"));
			targetFile = File.createTempFile("datadumper-", ".txt", this.dataDir);
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile),"UTF-8"));
			
			// copy files
			IOTools.copy(br, bw);

		} catch (Exception e) {
			this.logger.error(String.format(
					"Unexpected %s while dumping plain-text of URI into file.",
					e.getClass().getName()
			),e);
		} finally {
            if (br != null) try { br.close(); } catch (Exception e) {/* ignore this */}
            if (bw != null) try { bw.close();	} catch (Exception e) {/* ignore this */}	
		}
		return targetFile;
	}
	
	public void filter(ICommand command, IFilterContext context) {
		if (command == null) throw new NullPointerException("The command object is null.");
		if (command.getResult() != ICommand.Result.Passed) return;
		store(command.getParserDocument());
	}

}
