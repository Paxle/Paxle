
package org.paxle.se.index.lucene.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.se.index.IFieldManager;

public class Converter {
	
	private static final Log logger = LogFactory.getLog(Converter.class);
	
	// package private
	static IFieldManager fieldManager = null;
	
	private final PaxleAnalyzer pa;
	private final List<Counting> counters = new ArrayList<Counting>();
	
	public Converter(final PaxleAnalyzer pa) {
		this.pa = pa;
	}
	
	public PaxleAnalyzer getAnalyzer() {
		return pa;
	}
	
	public int getCountersAccumulated() {
		int r = 0;
		for (final Counting c : counters)
			r += c.getTokenCount();
		return r;
	}
	
	public void resetCounters() {
		counters.clear();
	}
	
	public Document iindexerDoc2LuceneDoc(IIndexerDocument document) {
		final Document doc = new Document();
		for (final Map.Entry<org.paxle.core.doc.Field<?>,Object> entry : document) {
			org.paxle.core.doc.Field<?> key = entry.getKey();
			if (key == IIndexerDocument.LOCATION) {
				doc.add(new Field(IIndexerDocument.LOCATION.getName(), (String)entry.getValue(), Field.Store.YES, Field.Index.UN_TOKENIZED));
			} else {
				Fieldable field = any2field(key, entry.getValue());
				if (field == null) {
					logger.error("Found null-field: " + entry.getKey() + " / " + entry.getValue());
				} else {
					doc.add(field);
				}
			}
		}
		return doc;
	}
	
	public Fieldable any2field(org.paxle.core.doc.Field<?> field, Object data) {
		try {
			if (String.class.isAssignableFrom(field.getType())) {
				return string2field(field, (String)data);
				
			} else if (Reader.class.isAssignableFrom(field.getType())) {
				return reader2field(field, (Reader)data);
	
			} else if (File.class.isAssignableFrom(field.getType())) {
				return file2field(field, (File)data);
				
			} else if (Date.class.isAssignableFrom(field.getType())) {
				return date2field(field, (Date)data);
				
			} else if (Number.class.isAssignableFrom(field.getType())) {
				return number2field(field, (Number)data);
				
			} else if (byte[].class.isAssignableFrom(field.getType()) && field.isSavePlain()) {
				return byteArray2field(field, (byte[])data);
				
			} else if (field.getType().isArray()) {
				return array2field(field, (Object[])data);
				/* double?
			} else if (File.class.isAssignableFrom(field.getType())) {
				try {
					return reader2field(field, new FileReader((File)data));
				} catch (FileNotFoundException e) {
					// TODO what to do in this situation?
					logger.error("Backing file for field " + field + " not found!", e);
					return null;
				}*/
			} else {
				// TODO
				return null;
			}
		} catch (FileNotFoundException e) {
			logger.error("File '" + data + "' not found for field '" + field + "'");
			return null;
		} catch (IOException e) {
			logger.error("I/O exception during conversion of field '" + field + "'");
			return null;
		}
	}
	
	private static Fieldable string2field(org.paxle.core.doc.Field<?> field, String data) {
		/* ===========================================================
		 * Strings
		 * - may be stored (if so, then compressed)
		 * - may be indexed (for tokenization see LuceneWriter.write(IIndexerDocument))
		 * - position term vectors
		 * =========================================================== */
		return new Field(
				field.getName(),
				data,
				store(field, true),
				index(field),
				termVector(field, TV_POSITIONS));
	}
	
	private Fieldable array2field(org.paxle.core.doc.Field<?> field, Object[] data) {
		/* ===========================================================
		 * Arrays
		 * - is not stored
		 * - is indexed (for tokenization see LuceneWriter.write(IIndexerDocument))
		 * - no term vectors
		 * =========================================================== */
		final ArrayTokenStream ats = new ArrayTokenStream(data);
		counters.add(ats);
		return new Field(field.getName(), pa.wrapDefaultFilters(ats, false));
	}
	
	private static Fieldable number2field(org.paxle.core.doc.Field<?> field, Number data) {
		/* ===========================================================
		 * Numbers
		 * - may be stored (if so, not compressed)
		 * - may be indexed (no tokenization)
		 * - no term vectors
		 * =========================================================== */
		final long num;
		if (data instanceof Double) {
			num = Double.doubleToLongBits(data.doubleValue());
		} else if (data instanceof Float) {
			num = Float.floatToIntBits(data.floatValue());
		} else {
			num = data.longValue();
		}
		
		if (field.isIndex()) {
			return new Field(
					field.getName(),
					PaxleNumberTools.longToString(num),
					store(field, false),
					Field.Index.UN_TOKENIZED);
		} else {
			return new Field(
					field.getName(),
					PaxleNumberTools.toBytes(num),
					store(field, false));
		}
	}
	
	private static Fieldable date2field(org.paxle.core.doc.Field<?> field, Date data) {
		/* ===========================================================
		 * Dates
		 * - may be stored (if so, then not compressed)
		 * - may be indexed (no tokenization)
		 * - no term vectors
		 * =========================================================== */
		return new Field(
				field.getName(),
				DateTools.dateToString(data, DateTools.Resolution.MILLISECOND),
				store(field, false),
				index(field));
	}
	
	private Fieldable reader2field(org.paxle.core.doc.Field<?> field, Reader data) throws IOException {
		/* ===========================================================
		 * Readers
		 * - not stored
		 * - indexed (for tokenization see LuceneWriter.write(IIndexerDocument))
		 * - position term vectors
		 * =========================================================== */
		final PaxleTokenizer pt = pa.createTokenizer(data);
		counters.add(pt);
		return new Field(
				field.getName(),
				pa.wrapDefaultFilters(pt, true),
				termVector(field, TV_POSITIONS));
	}
	
	private Fieldable file2field(org.paxle.core.doc.Field<?> field, File data) throws IOException {
		/* ===========================================================
		 * Files
		 * - not stored
		 * - indexed (for tokenization see LuceneWriter.write(IIndexerDocument))
		 * - position term vectors
		 * =========================================================== */
		return reader2field(field, new FileReader(data));
	}
	
	private static Fieldable byteArray2field(org.paxle.core.doc.Field<?> field, byte[] data) {
		/* ===========================================================
		 * byte[]-arrays
		 * - is stored (not compressed)
		 * - is not indexed
		 * =========================================================== */
		return new Field(
				field.getName(),
				data,
				store(field, false));
	}
	
	private static Field.Index index(org.paxle.core.doc.Field<?> field) {
		return (field.isIndex()) ? Field.Index.TOKENIZED : Field.Index.NO;
	}
	
	private static Field.Store store(org.paxle.core.doc.Field<?> field, boolean compress) {
		return (field.isSavePlain()) ? (compress) ? Field.Store.COMPRESS : Field.Store.YES : Field.Store.NO;
	}
	
	private static final int TV_POSITIONS = 1;
	private static final int TV_OFFSETS = 2;
	
	private static Field.TermVector termVector(org.paxle.core.doc.Field<?> field, int options) {
		if (!field.isIndex())
			return Field.TermVector.NO;
		
		switch (options) {
			case 0:
				return Field.TermVector.NO;
			case TV_POSITIONS:
				return Field.TermVector.WITH_POSITIONS;
			case TV_OFFSETS:
				return Field.TermVector.WITH_OFFSETS;
			case (TV_OFFSETS | TV_POSITIONS):
				return Field.TermVector.WITH_POSITIONS_OFFSETS;
			default:
				throw new IllegalArgumentException("illegal value for options " + options + " during processing field '" + field + "'");
		}
	}
	
	/* ========================================================================== */
	/* ========================================================================== */
	/* ========================================================================== */
	
	public static IIndexerDocument luceneDoc2IIndexerDoc(Document ldoc) throws ParseException, IOException {
		final IndexerDocument doc = new IndexerDocument();
		final Iterator<?> it = ldoc.getFields().iterator();
		while (it.hasNext()) {
			final Fieldable field = (Fieldable)it.next();
			if (!field.isStored())
				continue;
			final org.paxle.core.doc.Field<?> pfield = fieldManager.get(field.name());
			if (pfield != null)
				doc.put(pfield, field2any(field, pfield));
		}
		return doc;
	}
	
	public static <E extends Serializable> E field2any(Fieldable lfield, org.paxle.core.doc.Field<E> pfield) throws ParseException, IOException {
		if (String.class.isAssignableFrom(pfield.getType())) {
			return pfield.getType().cast(field2string(lfield, pfield));
			
		} else if (Reader.class.isAssignableFrom(pfield.getType())) {
			return pfield.getType().cast(field2reader(lfield, pfield));
			
		} else if (File.class.isAssignableFrom(pfield.getType())) {
			return pfield.getType().cast(field2file(lfield, pfield));
			
		} else if (Date.class.isAssignableFrom(pfield.getType())) {
			return pfield.getType().cast(field2date(lfield, pfield));
			
		} else if (Number.class.isAssignableFrom(pfield.getType())) {
			return pfield.getType().cast(field2number(lfield, pfield));
			
		} else if (byte[].class.isAssignableFrom(pfield.getType()) && pfield.isSavePlain()) {
			return pfield.getType().cast(field2byteArray(lfield, pfield));
			
		} else if (pfield.getType().isArray()) {
			return pfield.getType().cast(field2array(lfield, pfield));
			
		} else {
			// TODO
			return null;
		}
	}
	
	private static String field2string(Fieldable lfield, org.paxle.core.doc.Field<?> pfield) {
		return lfield.stringValue();
	}
	
	private static Reader field2reader(Fieldable lfield, org.paxle.core.doc.Field<?> pfield) {
		return lfield.readerValue();
	}
	
	private static File field2file(Fieldable lfield, org.paxle.core.doc.Field<?> pfield) throws IOException {
		// TODO: create temp file, copy reader value into and return it
		return null;
	}
	
	private static Date field2date(Fieldable lfield, org.paxle.core.doc.Field<?> pfield) throws ParseException {
		return DateTools.stringToDate(lfield.stringValue());
	}
	
	private static Number field2number(Fieldable lfield, org.paxle.core.doc.Field<?> pfield) {
		final long num;
		if (pfield.isIndex()) {
			num = PaxleNumberTools.stringToLong(lfield.stringValue());
		} else {
			num = PaxleNumberTools.toLong(lfield.binaryValue());
		}
		
		if (Double.class.isAssignableFrom(pfield.getType())) {
			return Double.valueOf(Double.longBitsToDouble(num));
		} else if (Float.class.isAssignableFrom(pfield.getType())) {
			return Float.valueOf(Float.intBitsToFloat((int)num));
		} else {
			return Long.valueOf(num);
		}
	}
	
	private static byte[] field2byteArray(Fieldable lfield, org.paxle.core.doc.Field<?> pfield) {
		return lfield.binaryValue();
	}
	
	private static Object[] field2array(Fieldable lfield, org.paxle.core.doc.Field<?> pfield) throws IOException {
		final LinkedList<Object> r = new LinkedList<Object>();
		final TokenStream ts = lfield.tokenStreamValue();
		Token token;
		while ((token = ts.next()) != null)
			r.add(String.valueOf(token.termBuffer(), 0, token.termLength()));
		return r.toArray(new Object[r.size()]);
	}
}
