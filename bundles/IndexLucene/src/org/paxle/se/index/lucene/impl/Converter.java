package org.paxle.se.index.lucene.impl;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

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
	
	public static Field any2field(org.paxle.core.doc.Field<?> field, Object data) {
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
	
	public static Field string2field(org.paxle.core.doc.Field<?> field, String data) {
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
				Field.TermVector.WITH_POSITIONS);
	}
	
	public static Field array2field(org.paxle.core.doc.Field<?> field, Object[] data) {
		/* ===========================================================
		 * Arrays
		 * - is not stored
		 * - is indexed (tokenized using lucene's standard analyzer)
		 * - no term vectors
		 * =========================================================== */
		return new Field(field.getName(), new ArrayTokenStream(data));
	}
	
	public static Field number2field(org.paxle.core.doc.Field<?> field, Number data) {
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
	
	public static Field date2field(org.paxle.core.doc.Field<?> field, Date data) {
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
	
	public static Field reader2field(org.paxle.core.doc.Field<?> field, Reader data) {
		/* ===========================================================
		 * Readers
		 * - not stored
		 * - indexed (tokenized using lucene's standard analyzer)
		 * - position term vectors
		 * =========================================================== */
		return new Field(
				field.getName(),
				data,
				Field.TermVector.WITH_POSITIONS);
	}
	
	public static Field byteArray2field(org.paxle.core.doc.Field<?> field, byte[] data) {
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
	
	public static Field.Index index(org.paxle.core.doc.Field<?> field) {
		return (field.isIndex()) ? Field.Index.TOKENIZED : Field.Index.NO;
	}
	
	public static Field.Store store(org.paxle.core.doc.Field<?> field, boolean compress) {
		return (field.isSavePlain()) ? (compress) ? Field.Store.COMPRESS : Field.Store.YES : Field.Store.NO;
	}
	
	/* ========================================================================== */
	/* ========================================================================== */
	/* ========================================================================== */
	
	public static <E> E field2any(Document doc, org.paxle.core.doc.Field<E> field) throws ParseException, IOException {
		if (!field.isSavePlain())
			throw new IllegalArgumentException("the field " + field + " has not been stored plainly, can't retrieve data");
		final Field lf = doc.getField(field.getName());
		if (lf == null)
			return null;

		if (String.class.isAssignableFrom(field.getType())) {
			return field.getType().cast(field2string(lf, field));
			
		} else if (Reader.class.isAssignableFrom(field.getType())) {
			return field.getType().cast(field2reader(lf, field));
			
		} else if (Date.class.isAssignableFrom(field.getType())) {
			return field.getType().cast(field2date(lf, field));
			
		} else if (Number.class.isAssignableFrom(field.getType())) {
			return field.getType().cast(field2number(lf, field));
			
		} else if (byte[].class.isAssignableFrom(field.getType()) && field.isSavePlain()) {
			return field.getType().cast(field2byteArray(lf, field));
			
		} else if (field.getType().isArray()) {
			return field.getType().cast(field2array(lf, field));
			
		} else {
			// TODO
			return null;
		}
	}
	
	public static String field2string(Field lfield, org.paxle.core.doc.Field<?> pfield) {
		return lfield.stringValue();
	}
	
	public static Reader field2reader(Field lfield, org.paxle.core.doc.Field<?> pfield) {
		return lfield.readerValue();
	}
	
	public static Date field2date(Field lfield, org.paxle.core.doc.Field<?> pfield) throws ParseException {
		return DateTools.stringToDate(lfield.stringValue());
	}
	
	public static Number field2number(Field lfield, org.paxle.core.doc.Field<?> pfield) {
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
	
	public static byte[] field2byteArray(Field lfield, org.paxle.core.doc.Field<?> pfield) {
		return lfield.binaryValue();
	}
	
	public static Object[] field2array(Field lfield, org.paxle.core.doc.Field<?> pfield) throws IOException {
		final LinkedList<Object> r = new LinkedList<Object>();
		final TokenStream ts = lfield.tokenStreamValue();
		Token token;
		while ((token = ts.next()) != null)
			r.add(token.termText());
		return r.toArray(new Object[r.size()]);
	}
}
