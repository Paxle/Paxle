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

package org.paxle.filter.languageidentification.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.FilterQueuePosition;
import org.paxle.core.filter.FilterTarget;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;

import de.spieleck.app.cngram.NGramProfiles;

/**
 * This helper class determines the language of a document and inserts its finding into a parser-doc and all of its subdocs 
 */
@Component(immediate=true, metatype=true, label="FilterLanguageManager", description="A filter which determines the language of a given document")
@Service(IFilter.class)
@FilterTarget(@FilterQueuePosition(
		queueId = FilterQueuePosition.PARSER_OUT, 
		position = Integer.MAX_VALUE-1000)
)
public class LanguageManager implements IFilter<ICommand> {

	@Property(label="%sdt.label", description="%sdt.desc", floatValue=0.6f)
	static final String SDT = "sdtv";

	private Log logger = LogFactory.getLog(this.getClass());
	NGramProfiles nps = null;

	/** If a language has a value >= this, it is considered the (sole) language of the document */
	float sdt;

	public LanguageManager() throws IOException  {
		this.nps = new NGramProfiles();
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

		double start = System.currentTimeMillis();

		NGramProfiles.RankResult res = null;
		Reader pdr = null;

		try {
			pdr = parserDoc.getTextAsReader();
			if (pdr != null) {
				NGramProfiles.Ranker ranker = nps.getRanker();
				ranker.account(pdr);
				res = ranker.getRankResult();
			} else {
				logger.info("No language for document '" + parserDoc.getOID() + "', as it contains no text");
			}
		} catch (IOException e) {
			logger.warn("Exception while trying to determine language of document '" +  parserDoc.getOID() + "'", e);
		} finally {
			try {
				if (pdr != null) pdr.close();
			} catch (IOException e) { logger.error("Unable to close ParserDocReader");}
		}

		double end = System.currentTimeMillis();

		logger.debug("Language detection took " + (end - start) + "ms");

		HashSet<String> lngs = null;
		if (res != null) {
			lngs = new HashSet<String>(1);
			if (res.getScore(0) > this.sdt) {
				logger.debug("Primary language of document '" + parserDoc.getOID() + "' is: " + res.getName(0) + ", " + res.getScore(0));
				lngs.add(res.getName(0));
			} else if (res.getScore(0) > 0.35 && res.getScore(1) > 0.35) {
				logger.debug("Primary languages of document '" + parserDoc.getOID() + "' are: " + res.getName(0) + ", " + res.getScore(0) + 
						" - " + res.getName(1) + ", " + res.getScore(1));
				lngs.add(res.getName(0));
				lngs.add(res.getName(1));
			}
			parserDoc.setLanguages(lngs);
		} else {
			logger.debug("Primary language of document '" + parserDoc.getOID() + "' is unknown");
			parserDoc.setLanguages(lngs); //set to null
		}
	}

	/**
	 * Inserts the ISO 639-1 code of the primary document language into the parser- and recursively all subparser docs
	 */
	public void filter(ICommand command, IFilterContext context) {
		if (command.getResult() != ICommand.Result.Passed) {
			logger.debug("Command didn't pass, aborting language detection.");
			return;
		}

		IParserDocument pdoc = command.getParserDocument();
		if (pdoc == null) {
			this.logger.debug(String.format(
					"No language detection possible for command '%s'. pdoc was null.",
					command.getLocation().toString()
			));
			return;
		} else if (pdoc.getStatus() != IParserDocument.Status.OK) {
			logger.debug(String.format(
					"Language of pDoc '%d' can't be determined. pDoc status was '%s': %s",
					Integer.valueOf(pdoc.getOID()),
					pdoc.getStatus().toString(),
					pdoc.getStatusText()
			));
			return;
		}

		getLanguage(pdoc);

	}

	@Activate
	protected void activate(Map<String, Object> props) {
		this.init(props);
	}

	void init(Map<String, Object> config) {
		if (config != null) {
			this.sdt = ((Float) config.get(SDT)).floatValue();
			logger.debug("SDT set to " + this.sdt);
		}
	}
}
