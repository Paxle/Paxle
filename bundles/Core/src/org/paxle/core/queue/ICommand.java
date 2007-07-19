package org.paxle.core.queue;

import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;

/**
 * Represents a command-object that is passed to components
 * such as:
 * <ul>
 * 	<li>Core-Crawler</li>
 * 	<li>Core-Parser</li>
 * 	<li>Indexer</li>
 * </ul>
 * 
 * This command is enqueued by a data-provider in the {@link IInputQueue input-queue}
 * of one of the above components. The component processes the {@link ICommand command}
 * and enqueues the modified {@link ICommand command} in the {@link IOutputQueue output-queue}
 * where it is fetched by a data-consumer and written to disk or DB.
 */
public interface ICommand {
	/* =======================================================
	 * General information
	 * ======================================================= */
	public static enum Result {
		Passed,
		Rejected,
		Failure
	}
	public Result getResult();
	public String getResultText();
	public void setResult(Result result);
	public void setResult(Result result, String description);
	
	public String getLocation();
	public void setLocation(String location);
	
	public ICrawlerDocument getCrawlerDocument();
	public void setCrawlerDocument(ICrawlerDocument crawlerDoc);
	
	public IParserDocument getParserDocument();
	public void setParserDocument(IParserDocument parserDoc);
	
	public IIndexerDocument getIndexerDocument();
	public void setIndexerDocument(IIndexerDocument indexerDoc);
	
//	/* =======================================================
//	 * Crawler-related information
//	 * ======================================================= */
//	
//	public String getLocation();
//	public void setLocation(String location);
//	
//	public File getCrawlerContent();
//	public void setCrawlerContent(File content);	
//	public int getCrawlerContentSize();
//	
//	public String getCharset();	
//	public void setCharset(String charset);
//	public boolean isCharsetSet();
//	
//	public Date getDocumentDate();
//	public void setDocumentDate(Date documentDate);
//	
//	/* =======================================================
//	 * Indexer-related information
//	 * ======================================================= */
//	public static enum Topic {
//		Pictures,
//		Videos,
//		Audio,
//		Applications,
//		IndexDocument,
//		OpenContent,
//		Business,
//		Health,
//		Sports,
//		Travel,
//		Politics,
//		NewsBlog,
//		Children,
//		CultureEntertainment,
//		Science,
//		Computer,
//		P2PFileSharing,
//		Sex,
//		Spam,
//		OperatingSystem
//	}
//	
//	/**
//	 * @see <a href="http://www.sub.uni-goettingen.de/ssgfi/projekt/doku/sprachcode.html">List of ISO 639-1 languages</a>
//	 */
//	public static enum Language {
//		aa, //	Afar 
//		ab, //	Abkhazian 
//		af, //	Afrikaans 
//		am, //	Amharic 
//		ar, //	Arabic 
//		as, //	Assamese 
//		ay, //	Aymara 
//		az, //	Azerbaijani 
//		ba, //	Bashkir 
//		be, //	Byelorussian 
//		bg, //	Bulgarian 
//		bh, //	Bihari 
//		bi, //	Bislama 
//		bn, //	Bengali 
//		bo, //	Tibetan 
//		br, //	Breton 
//		ca, //	Catalan 
//		co, //	Corsican 
//		cs, //	Czech 
//		cy, //	Welch 
//		da, //	Danish 
//		de, //	German 
//		dz, //	Bhutani 
//		el, //	Greek 
//		en, //	English 
//		eo, //	Esperanto 
//		es, //	Spanish 
//		et, //	Estonian 
//		eu, //	Basque 
//		fa, //	Persian 
//		fi, //	Finnish 
//		fj, //	Fiji 
//		fo, //	Faeroese 
//		fr, //	French 
//		fy, //	Frisian 
//		ga, //	Irish 
//		gd, //	Scots Gaelic 
//		gl, //	Galician 
//		gn, //	Guarani 
//		gu, //	Gujarati 
//		ha, //	Hausa 
//		hi, //	Hindi 
//		he, //	Hebrew 
//		hr, //	Croatian 
//		hu, //	Hungarian 
//		hy, //	Armenian 
//		ia, //	Interlingua 
//		id, //	Indonesian 
//		ie, //	Interlingue 
//		ik, //	Inupiak 
//		in, //	former Indonesian 
//		is, //	Icelandic 
//		it, //	Italian 
//		iu, //	Inuktitut (Eskimo) 
//		iw, //	former Hebrew 
//		ja, //	Japanese 
//		ji, //	former Yiddish 
//		jw, //	Javanese 
//		ka, //	Georgian 
//		kk, //	Kazakh 
//		kl, //	Greenlandic 
//		km, //	Cambodian 
//		kn, //	Kannada 
//		ko, //	Korean 
//		ks, //	Kashmiri 
//		ku, //	Kurdish 
//		ky, //	Kirghiz 
//		la, //	Latin 
//		ln, //	Lingala 
//		lo, //	Laothian 
//		lt, //	Lithuanian 
//		lv, //	Latvian, Lettish 
//		mg, //	Malagasy 
//		mi, //	Maori 
//		mk, //	Macedonian 
//		ml, //	Malayalam 
//		mn, //	Mongolian 
//		mo, //	Moldavian 
//		mr, //	Marathi 
//		ms, //	Malay 
//		mt, //	Maltese 
//		my, //	Burmese 
//		na, //	Nauru 
//		ne, //	Nepali 
//		nl, //	Dutch 
//		no, //	Norwegian 
//		oc, //	Occitan 
//		om, //	(Afan) Oromo 
//		or, //	Oriya 
//		pa, //	Punjabi 
//		pl, //	Polish 
//		ps, //	Pashto, Pushto 
//		pt, //	Portuguese 
//		qu, //	Quechua 
//		rm, //	Rhaeto-Romance 
//		rn, //	Kirundi 
//		ro, //	Romanian 
//		ru, //	Russian 
//		rw, //	Kinyarwanda 
//		sa, //	Sanskrit 
//		sd, //	Sindhi 
//		sg, //	Sangro 
//		sh, //	Serbo-Croatian 
//		si, //	Singhalese 
//		sk, //	Slovak 
//		sl, //	Slovenian 
//		sm, //	Samoan 
//		sn, //	Shona 
//		so, //	Somali 
//		sq, //	Albanian 
//		sr, //	Serbian 
//		ss, //	Siswati 
//		st, //	Sesotho 
//		su, //	Sudanese 
//		sv, //	Swedish 
//		sw, //	Swahili 
//		ta, //	Tamil 
//		te, //	Tegulu 
//		tg, //	Tajik 
//		th, //	Thai 
//		ti, //	Tigrinya 
//		tk, //	Turkmen 
//		tl, //	Tagalog 
//		tn, //	Setswana 
//		to, //	Tonga 
//		tr, //	Turkish 
//		ts, //	Tsonga 
//		tt, //	Tatar 
//		tw, //	Twi 
//		ug, //	Uigur 
//		uk, //	Ukrainian 
//		ur, //	Urdu 
//		uz, //	Uzbek 
//		vi, //	Vietnamese 
//		vo, //	Volapuk 
//		wo, //	Wolof 
//		xh, //	Xhosa 
//		yi, //	Yiddish 
//		yo, //	Yoruba 
//		za, //	Zhuang 
//		zh, //	Chinese 
//		zu, //	Zulu
//	}
//	
//	public void setLanguage(Language language);
//	public void setTopic(Topic topic);
//	
//	/* =======================================================
//	 * Parser-related information
//	 * ======================================================= */
//	public String getMimeType();	
//	public void setMimeType(String mimeType);
//	public boolean isMimeTypeSet();
//	
//	/* =======================================================
//	 * SubParser-related information
//	 * ======================================================= */
//	public void addHeadline(String headline);
//	public void addKeyword(String keyword);
//	public void addReference(String ref, String name);
//	public void addReferenceImage(String ref, String name);
//	public void addText(CharSequence text);
//	public void setAuthor(String author);
//	public void setLastChanged(Date date);
//	public void setSummary(String summary);
//	public void setTitle(String title);
//	
}
