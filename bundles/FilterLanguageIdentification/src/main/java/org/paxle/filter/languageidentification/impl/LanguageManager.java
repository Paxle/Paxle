package org.paxle.filter.languageidentification.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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

	/**
	 * The list of all available language profiles
	 */
	private ArrayList<TrigramSet> lngs = new ArrayList<TrigramSet>();
	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * Loads a language profile from a URL
	 * @param definition
	 * @throws IOException
	 */
	public void loadNewLanguage(URL definition) throws IOException {
		if (definition == null) {
			logger.warn("URL for language definition is null!");
			return;
		}
		TrigramSet nlng = new TrigramSet();
		nlng.load(definition);
		//set name to xx from filename /profiles/xx.txt
		nlng.setLanguageName(definition.getFile().substring(10, 12));
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
	 * Sets the language for the given ParserDocument and all its sub pDocs
	 * @param parserDoc
	 */
	private void getLanguage(IParserDocument parserDoc) {

		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.getLanguage(subDoc);
			}
		}

		TrigramSet test = new TrigramSet();
		String winner = null;
		double winvalue = Double.MAX_VALUE;

		double start = System.currentTimeMillis();

		try {
			if (parserDoc.getTextFile() != null) {
				test.init(parserDoc.getTextFile(), 10);
			} else {
				logger.info("No language for document '" + parserDoc.getOID() + "', as it contins no text");
			}

			Iterator<TrigramSet> it = this.lngs.iterator();
			while (it.hasNext()) {
				TrigramSet ref = it.next();
				double diff = ref.getDifference(test);
				logger.debug("Difference from " + ref.getLanguageName() + ": " + diff);
				if (diff < winvalue) {
					winner = ref.getLanguageName();
					winvalue = diff;
				}
			}
		} catch (IOException e) {
			logger.warn("Exception while trying to determine language of document '" +  parserDoc.getOID() + "'", e);
			winner = "unknown";
		}

		double end = System.currentTimeMillis();

		logger.debug("Language detection took " + (end - start) + "ms");

		HashSet<String> lngs = null;
		if (winvalue < 50) {
			logger.debug("Primary language of document '" + parserDoc.getOID() + "' is: " + winner + ", " + winvalue);
			lngs = new HashSet<String>(1);
			lngs.add(winner);
			parserDoc.setLanguages(lngs);
		} else {
			logger.debug("Primary language of document '" + parserDoc.getOID() + "' is unknown");
			parserDoc.setLanguages(lngs);
		}
	}

	/**
	 * Inserts the ISO 639-2 code of the primary document language into the parser and subparser docs
	 */
	public void filter(ICommand arg0, IFilterContext arg1) {
		if (arg0 == null) throw new NullPointerException("The command object is null.");

		if (arg0.getResult() != ICommand.Result.Passed) {
			logger.debug("Command didn't pass, aborting language detection.");
			return;
		}

		IParserDocument pdoc = arg0.getParserDocument();

		if (pdoc == null || pdoc.getStatus() != IParserDocument.Status.OK) {
			logger.debug("Language of pDoc '" + pdoc.getOID() + "' can't be determined");
			return;
		}

		getLanguage(arg0.getParserDocument());
	}

}
