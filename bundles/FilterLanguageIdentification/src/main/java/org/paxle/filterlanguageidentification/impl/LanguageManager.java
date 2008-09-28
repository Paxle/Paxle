package org.paxle.filterlanguageidentification.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

/**
 * This class stores the available language-profiles, determines the language of a document and inserts its finding into a parser-doc
 */
public class LanguageManager implements IFilter<ICommand> {
	
	private ArrayList<TrigramSet> lngs = new ArrayList<TrigramSet>();
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Loads a language profile from a URL
	 * @param definition
	 * @throws IOException
	 */
	public void loadNewLanguage(URL definition) throws IOException {
		TrigramSet nlng = new TrigramSet();
		nlng.load(definition);
		//set name to xxx from filename /profiles/xxx.txt
		nlng.setLanguageName(definition.getFile().substring(10, 13));
		this.lngs.add(nlng);
		logger.debug("Loaded language '" + nlng.getLanguageName() +"' from file " + definition.toExternalForm());
	}

	/**
	 * Return the number of currently registered language profiles
	 */
	public int getNumberOfRegisteredProfile() {
		return this.lngs.size();
	}
	
	/**
	 * Inserts the ISO 639-2 code of the primary document language into the parser doc
	 */
	public void filter(ICommand arg0, IFilterContext arg1) {
		if (arg0 == null) throw new NullPointerException("The command object is null.");
		
		if (arg0.getResult() != ICommand.Result.Passed) return;
		
		IParserDocument pDoc = arg0.getParserDocument();
		if (pDoc.getStatus() != IParserDocument.Status.OK) return;
		
		TrigramSet test = new TrigramSet();
		String winner = null;
		double winvalue = Double.MAX_VALUE;
		
		double start = System.currentTimeMillis();
		
		try {
			test.init(pDoc.getTextFile(), 10);
			Iterator<TrigramSet> i = this.lngs.iterator();
			while (i.hasNext()) {
				TrigramSet ref = i.next();
				double diff = ref.getDifference(test);
				logger.debug("Difference from " + ref.getLanguageName() + ": " + diff);
				if (diff < winvalue) {
					winner = ref.getLanguageName();
					winvalue = diff;
				}
			}
		} catch (IOException e) {
			logger.warn("Exception while trying to determine language of document '" + arg0.getLocation() + "' : ", e);
			winner = "unknown";
		}
		
		double end = System.currentTimeMillis();
		
		logger.debug("Language detection took " + (end - start) + "ms");
		
		if (winvalue < 50) {
			logger.info("Primary language of document '" + arg0.getLocation() + "' is: " + winner + ", " + winvalue);
		} else {
			logger.warn("Primary language of document '" + arg0.getLocation() + "' is unknown");
		}
	}
	
}
