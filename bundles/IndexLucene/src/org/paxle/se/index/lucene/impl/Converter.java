package org.paxle.se.index.lucene.impl;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.lucene.IndexerDocument;

public class Converter {
	
	private static class ArrayTokenStream extends TokenStream {
		
		private final Object[] data;
		private int pos = 0;
		private int textPos = 0;
		
		public ArrayTokenStream(final Object[] data) {
			this.data = data;
		}
		
		@Override
		public Token next() {
			Object o = null;
			while (o == null && this.pos < this.data.length)
				o = this.data[this.pos++];
			
			if (o == null)
				return null;
			
			int otp = this.textPos;
			final String text = o.toString();
			this.textPos += text.length();
			return new Token(text, otp, this.textPos++);
		}
	}
	
	private static final Hashtable<String,org.paxle.core.doc.Field<?>> fieldMap =
		new Hashtable<String,org.paxle.core.doc.Field<?>>();
	
	public static Document iindexerDoc2LuceneDoc(IIndexerDocument document) {
		final Document doc = new Document();
		for (final Map.Entry<org.paxle.core.doc.Field<?>,Object> entry : document) {
			fieldMap.put(entry.getKey().getName(), entry.getKey());
			doc.add(any2field(entry.getKey(), entry.getValue()));
		}
		return doc;
	}
	
	private static Fieldable any2field(org.paxle.core.doc.Field<?> field, Object data) {
		fieldMap.put(field.getName(), field);
		
		if (String.class.isAssignableFrom(field.getType())) {
			return string2field(field, (String)data);
			
		} else if (Reader.class.isAssignableFrom(field.getType())) {
			return reader2field(field, (Reader)data);
			
		} else if (Date.class.isAssignableFrom(field.getType())) {
			return date2field(field, (Date)data);
			
		} else if (Number.class.isAssignableFrom(field.getType())) {
			return number2field(field, (Number)data);
			
		} else if (byte[].class.isAssignableFrom(field.getType()) && field.isSavePlain()) {
			return byteArray2field(field, (byte[])data);
			
		} else if (field.getType().isArray()) {
			return array2field(field, (Object[])data);
			
		} else {
			// TODO
			return null;
		}
	}
	
	private static Fieldable string2field(org.paxle.core.doc.Field<?> field, String data) {
		/* ===========================================================
		 * Strings
		 * - may be stored (if so, then compressed)
		 * - may be indexed (tokenized using lucene's standard analyzer)
		 * - position term vectors
		 * =========================================================== */
		return new Field(
				field.getName(),
				data,
				store(field, true),
				index(field),
				termVector(field, TV_POSITIONS));
	}
	
	private static Fieldable array2field(org.paxle.core.doc.Field<?> field, Object[] data) {
		/* ===========================================================
		 * Arrays
		 * - is not stored
		 * - is indexed (tokenized using lucene's standard analyzer)
		 * - no term vectors
		 * =========================================================== */
		return new Field(field.getName(), new ArrayTokenStream(data));
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
			num = Double.doubleToLongBits((Double)data);
		} else if (data instanceof Float) {
			num = Float.floatToIntBits((Float)data);
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
	
	private static Fieldable reader2field(org.paxle.core.doc.Field<?> field, Reader data) {
		/* ===========================================================
		 * Readers
		 * - not stored
		 * - indexed (tokenized using lucene's standard analyzer)
		 * - position term vectors
		 * =========================================================== */
		return new Field(
				field.getName(),
				data,
				termVector(field, TV_POSITIONS));
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
		return (field.isIndex()) ? (
				((options & (TV_OFFSETS | TV_POSITIONS)) == 0) ? (
						((options & TV_OFFSETS) != 0) ? Field.TermVector.WITH_OFFSETS :
						((options & TV_POSITIONS) != 0) ? Field.TermVector.WITH_POSITIONS : Field.TermVector.NO
				) : Field.TermVector.WITH_POSITIONS_OFFSETS
		) : Field.TermVector.NO;
	}
	
	/* ========================================================================== */
	/* ========================================================================== */
	/* ========================================================================== */
	
	public static IIndexerDocument luceneDoc2IIndexerDoc(Document ldoc) throws ParseException, IOException {
		final IndexerDocument doc = new IndexerDocument();
		final Iterator it = ldoc.getFields().iterator();
		while (it.hasNext()) {
			final Fieldable field = (Fieldable)it.next();
			if (!field.isStored())
				continue;
			final org.paxle.core.doc.Field<?> pfield = fieldMap.get(field.name());
			if (pfield != null)
				doc.put(pfield, field2any(field, pfield));
		}
		return doc;
	}
	
	private static <E> E field2any(Fieldable lfield, org.paxle.core.doc.Field<E> pfield) throws ParseException, IOException {
		if (String.class.isAssignableFrom(pfield.getType())) {
			return pfield.getType().cast(field2string(lfield, pfield));
			
		} else if (Reader.class.isAssignableFrom(pfield.getType())) {
			return pfield.getType().cast(field2reader(lfield, pfield));
			
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
			return Double.longBitsToDouble(num);
		} else if (Float.class.isAssignableFrom(pfield.getType())) {
			return Float.intBitsToFloat((int)num);
		} else {
			return num;
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
			r.add(token.termText());
		return r.toArray(new Object[r.size()]);
	}
}
