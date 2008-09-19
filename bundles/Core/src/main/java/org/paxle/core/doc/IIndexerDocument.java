package org.paxle.core.doc;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public interface IIndexerDocument extends Iterable<Map.Entry<Field<?>,Object>> {
	
	public static final Field<String>                   AUTHOR        = new Field<String>                   (true,  true,  true,  "Author",       String.class);
	public static final Field<String>                   INTERNAL_NAME = new Field<String>                   (true,  true,  true, "InternalName", String.class);
	public static final Field<String[]>                 KEYWORDS      = new Field<String[]>                 (true,  true,  true, "Keywords",     String[].class);
	public static final Field<Date>                     LAST_CRAWLED  = new Field<Date>	                    (false, true,  false, "LastCrawled",  Date.class);
	public static final Field<Date>                     LAST_MODIFIED = new Field<Date>	                    (true,  true,  false, "LastModified", Date.class);
	public static final Field<Language[]>               LANGUAGES     = new Field<Language[]>               (true,  true,  false, "Languages",    Language[].class);
	public static final Field<String>                   LOCATION      = new Field<String>                   (true,  true,  false, "Location",     String.class);
	public static final Field<byte[]>                   MD5           = new Field<byte[]>                   (false, true,  false, "MD5",          byte[].class);
	public static final Field<String>                   MIME_TYPE     = new Field<String>                   (true,  false, false, "MimeType",     String.class);
	public static final Field<String>                   PROTOCOL      = new Field<String>                   (true,  false, false, "Protocol",     String.class);
	public static final Field<Long>                     SIZE          = new Field<Long>	                    (false, true,  false, "Size",         Long.class);
	public static final Field<String>                   SUMMARY       = new Field<String>                   (true,  true,  true,  "Summary",      String.class);
	public static final Field<File>                     TEXT          = new Field<File>                     (true,  false, true,  "Text",         File.class);
	public static final Field<String>                   TITLE         = new Field<String>                   (true,  true,  true,  "Title",        String.class);
	public static final Field<IIndexerDocument.Topic[]> TOPICS        = new Field<IIndexerDocument.Topic[]> (true,  true,  false, "Topics",       Topic[].class);
	public static final Field<String>                   SNIPPET       = new Field<String>                   (true,  true,  true,  "Snippet",      String.class);
	
	public static final String xyz = "ddadfda";
	
    public int getOID(); 
    public void setOID(int OID);	
	
	public <Type extends Serializable> void set(Field<Type> prop, Type data);
	public <Type extends Serializable> Type get(Field<Type> prop);
	
	public void setFields(Map<Field<?>, ?> fields);
	public Map<Field<?>, ?> getFields();
	
	public Iterator<Field<? extends Serializable>> fieldIterator();
	public Iterator<Map.Entry<Field<? extends Serializable>,Object>> iterator();
	
	public IIndexerDocument.Status getStatus();
	public String getStatusText();
	public void setStatus(IIndexerDocument.Status status);
	public void setStatus(IIndexerDocument.Status status, String text);
	public void setStatusText(String text);
	
	public static enum Status {
		OK,
		IndexerError,
		IndexError,
		IOError
	}
	
	public static enum Topic {
		Pictures,
		Videos,
		Audio,
		Applications,
		IndexDocument,
		OpenContent,
		Business,
		Health,
		Sports,
		Travel,
		Politics,
		NewsBlog,
		Children,
		CultureEntertainment,
		Science,
		Computer,
		P2PFileSharing,
		Sex,
		Spam,
		OperatingSystem
	}
	
	/**
	 * @see <a href="http://www.sub.uni-goettingen.de/ssgfi/projekt/doku/sprachcode.html">List of ISO 639-1 languages</a>
	 */
	/* When you change the names of the constants, please be aware that the
	 * org.paxle.se.index.lucene.impl.StopwordManager depends on them */
	public static enum Language {
		aa, //	Afar 
		ab, //	Abkhazian 
		af, //	Afrikaans 
		am, //	Amharic 
		ar, //	Arabic 
		as, //	Assamese 
		ay, //	Aymara 
		az, //	Azerbaijani 
		ba, //	Bashkir 
		be, //	Byelorussian 
		bg, //	Bulgarian 
		bh, //	Bihari 
		bi, //	Bislama 
		bn, //	Bengali 
		bo, //	Tibetan 
		br, //	Breton 
		ca, //	Catalan 
		co, //	Corsican 
		cs, //	Czech 
		cy, //	Welch 
		da, //	Danish 
		de, //	German 
		dz, //	Bhutani 
		el, //	Greek 
		en, //	English 
		eo, //	Esperanto 
		es, //	Spanish 
		et, //	Estonian 
		eu, //	Basque 
		fa, //	Persian 
		fi, //	Finnish 
		fj, //	Fiji 
		fo, //	Faeroese 
		fr, //	French 
		fy, //	Frisian 
		ga, //	Irish 
		gd, //	Scots Gaelic 
		gl, //	Galician 
		gn, //	Guarani 
		gu, //	Gujarati 
		ha, //	Hausa 
		hi, //	Hindi 
		he, //	Hebrew 
		hr, //	Croatian 
		hu, //	Hungarian 
		hy, //	Armenian 
		ia, //	Interlingua 
		id, //	Indonesian 
		ie, //	Interlingue 
		ik, //	Inupiak 
		in, //	former Indonesian 
		is, //	Icelandic 
		it, //	Italian 
		iu, //	Inuktitut (Eskimo) 
		iw, //	former Hebrew 
		ja, //	Japanese 
		ji, //	former Yiddish 
		jw, //	Javanese 
		ka, //	Georgian 
		kk, //	Kazakh 
		kl, //	Greenlandic 
		km, //	Cambodian 
		kn, //	Kannada 
		ko, //	Korean 
		ks, //	Kashmiri 
		ku, //	Kurdish 
		ky, //	Kirghiz 
		la, //	Latin 
		ln, //	Lingala 
		lo, //	Laothian 
		lt, //	Lithuanian 
		lv, //	Latvian, Lettish 
		mg, //	Malagasy 
		mi, //	Maori 
		mk, //	Macedonian 
		ml, //	Malayalam 
		mn, //	Mongolian 
		mo, //	Moldavian 
		mr, //	Marathi 
		ms, //	Malay 
		mt, //	Maltese 
		my, //	Burmese 
		na, //	Nauru 
		ne, //	Nepali 
		nl, //	Dutch 
		no, //	Norwegian 
		oc, //	Occitan 
		om, //	(Afan) Oromo 
		or, //	Oriya 
		pa, //	Punjabi 
		pl, //	Polish 
		ps, //	Pashto, Pushto 
		pt, //	Portuguese 
		qu, //	Quechua 
		rm, //	Rhaeto-Romance 
		rn, //	Kirundi 
		ro, //	Romanian 
		ru, //	Russian 
		rw, //	Kinyarwanda 
		sa, //	Sanskrit 
		sd, //	Sindhi 
		sg, //	Sangro 
		sh, //	Serbo-Croatian 
		si, //	Singhalese 
		sk, //	Slovak 
		sl, //	Slovenian 
		sm, //	Samoan 
		sn, //	Shona 
		so, //	Somali 
		sq, //	Albanian 
		sr, //	Serbian 
		ss, //	Siswati 
		st, //	Sesotho 
		su, //	Sudanese 
		sv, //	Swedish 
		sw, //	Swahili 
		ta, //	Tamil 
		te, //	Tegulu 
		tg, //	Tajik 
		th, //	Thai 
		ti, //	Tigrinya 
		tk, //	Turkmen 
		tl, //	Tagalog 
		tn, //	Setswana 
		to, //	Tonga 
		tr, //	Turkish 
		ts, //	Tsonga 
		tt, //	Tatar 
		tw, //	Twi 
		ug, //	Uigur 
		uk, //	Ukrainian 
		ur, //	Urdu 
		uz, //	Uzbek 
		vi, //	Vietnamese 
		vo, //	Volapuk 
		wo, //	Wolof 
		xh, //	Xhosa 
		yi, //	Yiddish 
		yo, //	Yoruba 
		za, //	Zhuang 
		zh, //	Chinese 
		zu, //	Zulu
	}
}
